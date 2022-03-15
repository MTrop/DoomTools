package net.mtrop.doom.tools.gui.doommake;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.doommake.ProjectGenerator;
import net.mtrop.doom.tools.doommake.ProjectModule;
import net.mtrop.doom.tools.doommake.ProjectTemplate;
import net.mtrop.doom.tools.doommake.ProjectTokenReplacer;
import net.mtrop.doom.tools.doommake.ProjectTokenReplacer.GUIHint;
import net.mtrop.doom.tools.doommake.generators.WADProjectGenerator;
import net.mtrop.doom.tools.exception.UtilityException;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.DoomToolsConstants.FileFilters;
import net.mtrop.doom.tools.gui.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.DoomToolsLanguageManager;
import net.mtrop.doom.tools.struct.swing.SwingUtils;

import static javax.swing.BorderFactory.*;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FileChooserFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.SwingUtils.*;

/**
 * The DoomMake New Project application.
 * @author Matthew Tropiano
 */
public class DoomMakeNewProjectApp extends DoomToolsApplicationInstance
{
	private static final String STATE_TARGET_DIRECTORY = "targetDirectory";

	/** Utils. */
	private DoomToolsGUIUtils utils;
    /** Language manager. */
    private DoomToolsLanguageManager language;
	
	/** Project generator. */
	private ProjectGenerator projectGenerator;

	/** Target directory. */
	private File targetDirectory;
	/** Set of used templates. */
	private Set<String> templateNameSet;
	
	/**
	 * Creates a new New Project app instance.
	 */
	public DoomMakeNewProjectApp() 
	{
		this(null);
	}
	
	/**
	 * Creates a new New Project app instance.
	 * @param targetDirectoryPath the starting directory path, if any.
	 */
	public DoomMakeNewProjectApp(String targetDirectoryPath) 
	{
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		
		this.projectGenerator = new WADProjectGenerator();
		
		this.targetDirectory = Common.isEmpty(targetDirectoryPath) ? null : new File(targetDirectoryPath);
		this.templateNameSet = new TreeSet<>();
	}
	
	@Override
	public String getTitle()
	{
		return language.getText("doommake.newproject.title");
	}
	
	@Override
	public Map<String, String> getApplicationState()
	{
		return apply(super.getApplicationState(), (state) -> {
			if (targetDirectory != null)
				state.put(STATE_TARGET_DIRECTORY, targetDirectory.getAbsolutePath());
		});
	}

	@Override
	public void setApplicationState(Map<String, String> state)
	{
		this.targetDirectory = state.containsKey(STATE_TARGET_DIRECTORY) ? new File(state.get(STATE_TARGET_DIRECTORY)) : null;
	}

	@Override
	public Container createContentPane()
	{
		JPanel out = new JPanel();
		
		// Hardcode only WAD Project for now.
		Container projectTypePanel = titlePanel(language.getText("doommake.newproject.type"),
			containerOf(node(comboBox(comboBoxModel(Arrays.asList(language.getText("doommake.newproject.type.wad"))), (c, i) -> {
				// Do nothing on change - no other options.
			})))
		);
		
		Container projectDirectoryPanel = titlePanel(language.getText("doommake.newproject.directory"),
			containerOf(node(fileField(targetDirectory,
				(current) -> chooseDirectory(
					out, 
					language.getText("doommake.newproject.directory.browse.title"), 
					current, 
					language.getText("doommake.newproject.directory.browse.accept"), 
					FileFilters.DIRECTORIES
				), 
				(selected) -> { 
					targetDirectory = selected;
					if (targetDirectory != null && targetDirectory.isDirectory() && targetDirectory.listFiles().length > 0)
						SwingUtils.warning(out, language.getText("doommake.newproject.directory.browse.notempty"));
				}
			)))
		);
		
		Container controlPane = containerOf(new FlowLayout(FlowLayout.TRAILING, 4, 4), node(
			utils.createButtonFromLanguageKey("doommake.newproject.create", (c, e) -> createProject())
		));
	
		JPanel projectPanel = new JPanel();
		JScrollPane scrollPane = apply(
			scroll(projectPanel), 
			(p) -> p.setBorder(null)
		);
		containerOf(projectPanel, new BoxLayout(projectPanel, BoxLayout.Y_AXIS), getWADGeneratorOptionNodes());
		
		return containerOf(
			createEmptyBorder(4, 4, 4, 4),
			node(BorderLayout.CENTER, containerOf(out, createEmptyBorder(4, 4, 4, 4), new BorderLayout(),
				node(BorderLayout.NORTH, containerOf(
					node(BorderLayout.NORTH, projectTypePanel),
					node(BorderLayout.SOUTH, projectDirectoryPanel)
				)),
				node(BorderLayout.CENTER, containerOf(
					node(BorderLayout.NORTH, scrollPane),
					node(BorderLayout.CENTER, containerOf())
				)),
				node(BorderLayout.SOUTH, controlPane)
			))
		);
	}

	// The title panel.
	private static Container titlePanel(String title, Container container)
	{
		Border border = createTitledBorder(
			createLineBorder(Color.GRAY, 1), title, TitledBorder.LEADING, TitledBorder.TOP
		);
		return containerOf(border, 
			node(containerOf(createEmptyBorder(4, 4, 4, 4),
				node(BorderLayout.CENTER, container)
			))
		);
	}

	// The options for WAD Generators.
	private Node[] getWADGeneratorOptionNodes()
	{
		final String patchNone = language.getText("doommake.newproject.wadgen.patch.none");
		final String patchDECOHack = language.getText("doommake.newproject.wadgen.patch.decohack");
		final String patchOther = language.getText("doommake.newproject.wadgen.patch.other");
		Collection<String> patchOptions = Arrays.asList(patchNone, patchDECOHack, patchOther);

		final String textureNone = language.getText("doommake.newproject.wadgen.textures.none");
		final String textureWads = language.getText("doommake.newproject.wadgen.textures.wads");
		final String textureProject = language.getText("doommake.newproject.wadgen.textures.project");
		final String textureProjectBoom = language.getText("doommake.newproject.wadgen.textures.projectboom");
		Collection<String> textureOptions = Arrays.asList(textureNone, textureWads, textureProject, textureProjectBoom);
		
		final String versionControlNone = language.getText("doommake.newproject.wadgen.vctrl.none");
		final String versionControlGit = language.getText("doommake.newproject.wadgen.vctrl.git");
		final String versionControlMercurial = language.getText("doommake.newproject.wadgen.vctrl.hg");
		Collection<String> versionControlOptions = Arrays.asList(versionControlNone, versionControlGit, versionControlMercurial);

		final String runNo = language.getText("doommake.newproject.wadgen.run.no");
		final String runYes = language.getText("doommake.newproject.wadgen.run.yes");
		Collection<String> runOptions = Arrays.asList(runNo, runYes);

		return new Node[]
		{
			node(titlePanel(language.getText("doommake.newproject.wadgen.contain"), containerOf(new BorderLayout(),
				node(BorderLayout.NORTH, checkBox(language.getText("doommake.newproject.wadgen.contain.maps"), false, (c, e) -> {
					if (c.isSelected())
						addTemplateName(WADProjectGenerator.TEMPLATE_MAPS);
					else
						removeTemplateName(WADProjectGenerator.TEMPLATE_MAPS);
				})),
				node(BorderLayout.SOUTH, checkBox(language.getText("doommake.newproject.wadgen.contain.assets"), false, (c, e) -> {
					if (c.isSelected())
						addTemplateName(WADProjectGenerator.TEMPLATE_ASSETS);
					else
						removeTemplateName(WADProjectGenerator.TEMPLATE_ASSETS);
				}))
			))),
			node(titlePanel(language.getText("doommake.newproject.wadgen.patch"), containerOf(
				node(comboBox(comboBoxModel(patchOptions), (c, i) -> {
					removeTemplateCategory(WADProjectGenerator.CATEGORY_PATCHES);
					if (i == patchDECOHack)
						addTemplateName(WADProjectGenerator.TEMPLATE_DECOHACK);
					else if (i == patchOther)
						addTemplateName(WADProjectGenerator.TEMPLATE_PATCH);
				}))
			))),
			node(titlePanel(language.getText("doommake.newproject.wadgen.textures"), containerOf(
				node(comboBox(comboBoxModel(textureOptions), (c, i) -> {
					removeTemplateCategory(WADProjectGenerator.CATEGORY_TEXTURES);
					if (i == textureWads)
						addTemplateName(WADProjectGenerator.TEMPLATE_TEXTUREWADS);
					else if (i == textureProject)
						addTemplateName(WADProjectGenerator.TEMPLATE_TEXTURES);
					else if (i == textureProjectBoom)
						addTemplateName(WADProjectGenerator.TEMPLATE_TEXTURES_BOOM);
				}))
			))),
			node(titlePanel(language.getText("doommake.newproject.wadgen.vctrl"), containerOf(
				node(comboBox(comboBoxModel(versionControlOptions), (c, i) -> {
					removeTemplateCategory(WADProjectGenerator.CATEGORY_REPOSITORY);
					if (i == versionControlGit)
						addTemplateName(WADProjectGenerator.TEMPLATE_GIT);
					else if (i == versionControlMercurial)
						addTemplateName(WADProjectGenerator.TEMPLATE_MERCURIAL);
				}))
			))),
			node(titlePanel(language.getText("doommake.newproject.wadgen.run"), containerOf(
				node(comboBox(comboBoxModel(runOptions), (c, i) -> {
					removeTemplateCategory(WADProjectGenerator.CATEGORY_EXECUTION);
					if (i == runYes)
						addTemplateName(WADProjectGenerator.TEMPLATE_RUN);
				}))
			)))
		};
	}
	
	// Adds a template after removing associated ones.
	private void addTemplateName(String templateToAdd)
	{
		templateNameSet.add(templateToAdd);
	}

	// Removes a category of templates.
	private void removeTemplateCategory(String category)
	{
		for (ProjectTemplate template : projectGenerator.getTemplatesByCategory(category))
			removeTemplateName(template.getName());
	}

	// Removes a template name.
	private void removeTemplateName(String templateToRemove)
	{
		templateNameSet.remove(templateToRemove);
	}

	private Modal<String> createOptionModal(String title, String prompt, String defaultValue, GUIHint guiHint)
	{
		final AtomicReference<String> stringValue = new AtomicReference<>();
		
		Component field;
		switch (guiHint)
		{
			default:
			case STRING:
				field = stringTextField(
					defaultValue, false, (value) -> stringValue.set(value)
				);
				break;
			case FILE:
				field = fileField(
					(current) -> chooseFile(
						language.getText("doommake.newproject.directory.browse.title"), 
						current, 
						language.getText("doommake.newproject.directory.browse.accept"), 
						FileFilters.DIRECTORIES
					), 
					(selected) -> { 
						stringValue.set(selected == null ? "" : selected.getAbsolutePath());
					}
				);
				break;
		}
		
		final Container contentPane = containerOf(
			apply(new JPanel(), (p) -> p.setBorder(createEmptyBorder(8, 8, 8, 8))), 
			new BorderLayout(8, 8),
			node(BorderLayout.CENTER, label(prompt)),
			node(BorderLayout.SOUTH, field)
		);
		return modal(
			utils.getWindowIcons(),
			title,
			contentPane,
			choice(language.getText("doomtools.ok"), KeyEvent.VK_ENTER, () -> stringValue.get())
		);
	}
	
	private Map<String, String> getReplacerMap(SortedSet<ProjectModule> selectedModules)
	{
		Map<String, String> outputMap = new HashMap<>();
		for (ProjectTokenReplacer replacer : ProjectGenerator.getReplacers(selectedModules))
		{
			String key = replacer.getToken();
			String prompt = replacer.getPrompt();
			
			while (true)
			{
				String value = createOptionModal(
					language.getText("doommake.newproject.modal.option.title"),
					prompt,
					replacer.getDefaultValue(),
					replacer.getGUIHint()
				).openThenDispose();
				
				if (value == null)
					return null;
				
				value = replacer.getSanitizer().apply(value.trim());
				
				String error;
				if ((error = replacer.getValidator().apply(value)) != null)
				{
					SwingUtils.error(receiver.getApplicationContainer(), error);
				}
				else if (value.length() == 0)
				{
					outputMap.put(key, replacer.getDefaultValue());
					break; 
				}
				else
				{
					outputMap.put(key, value);
					break; 
				}
				
			}
		}
		return outputMap;
	}
	
	// Creates a project.
	private void createProject()
	{
		if (targetDirectory == null)
		{
			SwingUtils.error(language.getText("doommake.newproject.error.nulldirectory"));
			return;
		}
		else if (targetDirectory.exists() && !targetDirectory.isDirectory())
		{
			SwingUtils.error(language.getText("doommake.newproject.error.notadirectory"));
			return;
		}
		
		File[] files = targetDirectory.listFiles(); 
		if (files != null && files.length > 0)
		{
			SwingUtils.error(language.getText("doommake.newproject.error.baddirectory"));
			return;
		}
		
		SortedSet<ProjectModule> selectedModules;
		try {
			selectedModules = projectGenerator.getSelectedModules(templateNameSet);
		} catch (UtilityException e) {
			SwingUtils.error(e.getLocalizedMessage());
			return;
		}
		
		Map<String, String> replacerMap;
		if ((replacerMap = getReplacerMap(selectedModules)) == null)
			return;
		
		try {
			projectGenerator.createProject(selectedModules, replacerMap, targetDirectory);
		} catch (IOException e) {
			SwingUtils.error(e.getLocalizedMessage());
			return;
		}
		
		// Returns a trinary.
		Boolean result = modal(
			receiver.getApplicationContainer(), 
			utils.getWindowIcons(), 
			language.getText("doommake.newproject.modal.openproject.title"),
			containerOf(new BorderLayout(4, 4),
				node(BorderLayout.CENTER, wrappedLabel(language.getText("doommake.newproject.modal.openproject.message")))
			),
			utils.createChoiceFromLanguageKey("doommake.newproject.modal.openproject.choice.folder", true),
			utils.createChoiceFromLanguageKey("doommake.newproject.modal.openproject.choice.tools", false),
			utils.createChoiceFromLanguageKey("doommake.newproject.modal.openproject.choice.none", null)
		).openThenDispose();

		if (result == null)
		{
			// Do nothing (also happens on close).
		}
		else if (result) // Open folder.
		{
			try {
				if (!SwingUtils.open(targetDirectory))
				{
					SwingUtils.error(receiver.getApplicationContainer(), 
						language.getText("doommake.newproject.modal.openproject.folder.error", targetDirectory.getAbsolutePath())
					);
				}
			} catch (IOException e) {
				SwingUtils.error(receiver.getApplicationContainer(), e.getLocalizedMessage());
			}
		}
		else // Open in Doom Tools
		{
			receiver.startApplication(new DoomMakeOpenProjectApp(targetDirectory));
			receiver.attemptClose();
		}
		
	}
	
}

/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import net.mtrop.doom.tools.doommake.ProjectGenerator;
import net.mtrop.doom.tools.doommake.ProjectModule;
import net.mtrop.doom.tools.doommake.ProjectTemplate;
import net.mtrop.doom.tools.doommake.ProjectTokenReplacer;
import net.mtrop.doom.tools.doommake.ProjectTokenReplacer.GUIHint;
import net.mtrop.doom.tools.doommake.generators.TextureProjectGenerator;
import net.mtrop.doom.tools.doommake.generators.WADProjectGenerator;
import net.mtrop.doom.tools.exception.UtilityException;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.DoomToolsConstants.FileFilters;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.ArrayUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

import static javax.swing.BorderFactory.*;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FileChooserFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;
import static net.mtrop.doom.tools.struct.swing.ModalFactory.*;
import static net.mtrop.doom.tools.struct.util.ObjectUtils.apply;


/**
 * The DoomMake New Project application.
 * @author Matthew Tropiano
 */
public class DoomMakeNewProjectApp extends DoomToolsApplicationInstance
{
	private static final String STATE_TARGET_DIRECTORY = "targetDirectory";

	/** Project generator. */
	private ProjectGenerator projectGenerator;
	/** Target directory. */
	private File targetDirectory;

	/** Selected template names per generator. */
	private Map<ProjectGenerator, Set<String>> templateNameSet;
	
	/** Open the Studio version? */
	private boolean studio;
	
	/**
	 * Creates a new New Project app instance.
	 */
	public DoomMakeNewProjectApp() 
	{
		this(null, false);
	}
	
	/**
	 * Creates a new New Project app instance.
	 * @param targetDirectoryPath the starting directory path, if any.
	 * @param studio if true, opens DoomMake Studio instead of just the regular open project. 
	 */
	public DoomMakeNewProjectApp(String targetDirectoryPath, boolean studio) 
	{
		this.projectGenerator = new WADProjectGenerator();
		
		this.targetDirectory = ObjectUtils.isEmpty(targetDirectoryPath) ? null : new File(targetDirectoryPath);
		this.templateNameSet = new HashMap<>();
		this.studio = studio;
	}
	
	@Override
	public String getTitle()
	{
		return language.getText("doommake.newproject.title");
	}
	
	@Override
	public Map<String, String> getApplicationState()
	{
		Map<String, String> state = super.getApplicationState();
		if (targetDirectory != null)
			state.put(STATE_TARGET_DIRECTORY, targetDirectory.getAbsolutePath());
		return state;
	}

	@Override
	public void setApplicationState(Map<String, String> state)
	{
		this.targetDirectory = state.containsKey(STATE_TARGET_DIRECTORY) ? new File(state.get(STATE_TARGET_DIRECTORY)) : null;
	}

	@Override
	public Container createContentPane()
	{
		final String projectWad = language.getText("doommake.newproject.type.wad");
		final String projectTexture = language.getText("doommake.newproject.type.texture");

		final CardLayout cards = new CardLayout();

		final JComponent projectCardPanel = containerOf(new JPanel(), cards,
			node(projectWad, containerOf(getWADGeneratorOptionNodes())),
			node(projectTexture, containerOf(getTextureGeneratorOptionNodes()))
		);

		JScrollPane scrollPane = apply(
			scroll(projectCardPanel), 
			(p) -> p.setBorder(null)
		);
		
		final ProjectGenerator wadGenerator = new WADProjectGenerator();
		final ProjectGenerator textureGenerator = new TextureProjectGenerator();
		
		final ComboBoxChangeHandler<String> handler = (item) -> {
			((CardLayout)projectCardPanel.getLayout()).show(projectCardPanel, item);
			if (item.equalsIgnoreCase(projectWad))
				projectGenerator = wadGenerator;
			else if (item.equalsIgnoreCase(projectTexture))
				projectGenerator = textureGenerator;
		};
		
		Container projectTypePanel = titlePanel(language.getText("doommake.newproject.type"),
			containerOf(node(comboBox(comboBoxModel(Arrays.asList(projectWad, projectTexture)), handler)))
		);
		
		// Set default.
		handler.onChange(projectWad);
		
		Container projectDirectoryPanel = titlePanel(language.getText("doommake.newproject.directory"),
			containerOf(node(fileField(targetDirectory,
				(current) -> chooseDirectory(
					getApplicationContainer(), 
					language.getText("doommake.newproject.directory.browse.title"), 
					current, 
					language.getText("doommake.newproject.directory.browse.accept"), 
					FileFilters.DIRECTORIES
				), 
				(selected) -> { 
					targetDirectory = selected;
					if (targetDirectory != null && targetDirectory.isDirectory() && targetDirectory.listFiles().length > 0)
						SwingUtils.warning(getApplicationContainer(), language.getText("doommake.newproject.directory.browse.notempty"));
				}
			)))
		);
		
		Container controlPane = containerOf(flowLayout(Flow.TRAILING, 4, 4), node(
			utils.createButtonFromLanguageKey("doommake.newproject.create", (i) -> createProject())
		));
	
		return containerOf(
			node(BorderLayout.NORTH, containerOf(
				node(BorderLayout.NORTH, projectTypePanel),
				node(BorderLayout.SOUTH, projectDirectoryPanel)
			)),
			node(BorderLayout.CENTER, containerOf(
				node(BorderLayout.NORTH, scrollPane),
				node(BorderLayout.CENTER, containerOf())
			)),
			node(BorderLayout.SOUTH, controlPane)
		);
	}

	@Override
	public JMenuBar createDesktopMenuBar() 
	{
		return menuBar(createHelpMenu());
	}
	
	@Override
	public JMenuBar createInternalMenuBar() 
	{
		return menuBar(createHelpMenu());
	}
	
	@Override
	public boolean shouldClose(Object frame) 
	{
		return SwingUtils.yesTo(language.getText("doomtools.application.close"));
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

		JPanel panel = new JPanel();
		
		return ArrayUtils.arrayOf(
			node(BorderLayout.NORTH, containerOf(panel, boxLayout(panel, BoxAxis.PAGE_AXIS),
				node(titlePanel(language.getText("doommake.newproject.wadgen.contain"), containerOf(
					node(BorderLayout.NORTH, checkBox(language.getText("doommake.newproject.wadgen.contain.maps"), false, (v) -> {
						if (v)
							addTemplateName(WADProjectGenerator.TEMPLATE_MAPS);
						else
							removeTemplateName(WADProjectGenerator.TEMPLATE_MAPS);
					})),
					node(BorderLayout.SOUTH, checkBox(language.getText("doommake.newproject.wadgen.contain.assets"), false, (v) -> {
						if (v)
							addTemplateName(WADProjectGenerator.TEMPLATE_ASSETS);
						else
							removeTemplateName(WADProjectGenerator.TEMPLATE_ASSETS);
					}))
				))),
				node(titlePanel(language.getText("doommake.newproject.wadgen.patch"), containerOf(
					node(comboBox(comboBoxModel(patchOptions), (i) -> {
						removeTemplateCategory(WADProjectGenerator.CATEGORY_PATCHES);
						if (i == patchDECOHack)
							addTemplateName(WADProjectGenerator.TEMPLATE_DECOHACK);
						else if (i == patchOther)
							addTemplateName(WADProjectGenerator.TEMPLATE_PATCH);
					}))
				))),
				node(titlePanel(language.getText("doommake.newproject.wadgen.textures"), containerOf(
					node(comboBox(comboBoxModel(textureOptions), (i) -> {
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
					node(comboBox(comboBoxModel(versionControlOptions), (i) -> {
						removeTemplateCategory(WADProjectGenerator.CATEGORY_REPOSITORY);
						if (i == versionControlGit)
							addTemplateName(WADProjectGenerator.TEMPLATE_GIT);
						else if (i == versionControlMercurial)
							addTemplateName(WADProjectGenerator.TEMPLATE_MERCURIAL);
					}))
				))),
				node(titlePanel(language.getText("doommake.newproject.wadgen.run"), containerOf(
					node(comboBox(comboBoxModel(runOptions), (i) -> {
						removeTemplateCategory(WADProjectGenerator.CATEGORY_EXECUTION);
						if (i == runYes)
							addTemplateName(WADProjectGenerator.TEMPLATE_RUN);
					}))
				)))
			)),
			node(BorderLayout.CENTER, containerOf())
		);
	}
	
	// The options for Texture WAD Generators.
	private Node[] getTextureGeneratorOptionNodes()
	{
		final String textureProject = language.getText("doommake.newproject.texwad.textures.none");
		final String textureProjectVanilla = language.getText("doommake.newproject.texwad.textures.vanilla");
		final String textureProjectBoom = language.getText("doommake.newproject.texwad.textures.boom");
		Collection<String> textureOptions = Arrays.asList(textureProject, textureProjectVanilla, textureProjectBoom);
		
		final String versionControlNone = language.getText("doommake.newproject.wadgen.vctrl.none");
		final String versionControlGit = language.getText("doommake.newproject.wadgen.vctrl.git");
		final String versionControlMercurial = language.getText("doommake.newproject.wadgen.vctrl.hg");
		Collection<String> versionControlOptions = Arrays.asList(versionControlNone, versionControlGit, versionControlMercurial);

		JPanel panel = new JPanel();

		return ArrayUtils.arrayOf(
			node(BorderLayout.NORTH, containerOf(panel, boxLayout(panel, BoxAxis.PAGE_AXIS),		
				node(titlePanel(language.getText("doommake.newproject.texwad.textures"), containerOf(
					node(comboBox(comboBoxModel(textureOptions), (i) -> {
						removeTemplateCategory(TextureProjectGenerator.CATEGORY_TEXTURES);
						if (i == textureProjectVanilla)
							addTemplateName(TextureProjectGenerator.TEMPLATE_TEXTURES);
						else if (i == textureProjectBoom)
							addTemplateName(TextureProjectGenerator.TEMPLATE_TEXTURES_BOOM);
					}))
				))),
				node(titlePanel(language.getText("doommake.newproject.wadgen.vctrl"), containerOf(
					node(comboBox(comboBoxModel(versionControlOptions), (i) -> {
						removeTemplateCategory(TextureProjectGenerator.CATEGORY_REPOSITORY);
						if (i == versionControlGit)
							addTemplateName(TextureProjectGenerator.TEMPLATE_GIT);
						else if (i == versionControlMercurial)
							addTemplateName(TextureProjectGenerator.TEMPLATE_MERCURIAL);
					}))
				)))
			)),
			node(BorderLayout.CENTER, containerOf())
		);
	}
	
	// Adds a template after removing associated ones.
	private void addTemplateName(String templateToAdd)
	{
		Set<String> set = templateNameSet.get(projectGenerator);
		if (set == null)
			templateNameSet.put(projectGenerator, set = new TreeSet<>());
		set.add(templateToAdd);
	}

	// Removes a template name.
	private void removeTemplateName(String templateToRemove)
	{
		Set<String> set = templateNameSet.get(projectGenerator);
		if (set == null)
			templateNameSet.put(projectGenerator, set = new TreeSet<>());
		set.remove(templateToRemove);
	}

	// Removes a category of templates.
	private void removeTemplateCategory(String category)
	{
		for (ProjectTemplate template : projectGenerator.getTemplatesByCategory(category))
			removeTemplateName(template.getName());
	}

	private Modal<String> createOptionModal(String title, String prompt, final ProjectTokenReplacer replacer)
	{
		final AtomicReference<String> stringValue = new AtomicReference<>();

		String defaultValue = replacer.getDefaultValue(); 
		Function<String, String> validator = replacer.getValidator(); 
		GUIHint guiHint = replacer.getGUIHint();
		
		Component field;
		switch (guiHint)
		{
			default:
			case STRING:
				field = stringField(defaultValue, (value) -> stringValue.set(value));
				break;
			case FILE:
				field = fileField(
					(current) -> chooseFile(
						language.getText("doommake.newproject.directory.browse.title"), 
						current, 
						language.getText("doommake.newproject.directory.browse.accept")
					), 
					(selected) -> { 
						stringValue.set(selected == null ? "" : selected.getAbsolutePath());
					}
				);
				break;
		}
		
		final Container contentPane = containerOf(
			borderLayout(0, 8),
			node(BorderLayout.CENTER, label(prompt)),
			node(BorderLayout.SOUTH, field)
		);
		return modal(
			utils.getWindowIcons(),
			title,
			contentPane,
			(value) -> {
				String error;
				value = replacer.getSanitizer().apply(value.trim());
				if ((error = validator.apply(value)) != null)
				{
					SwingUtils.error(error);
					return false;
				}
				return true;
			},
			choice(language.getText("doomtools.ok"), KeyEvent.VK_ENTER, true, () -> stringValue.get())
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
					replacer
				).openThenDispose();
				
				if (value == null)
					return null;
				
				value = replacer.getSanitizer().apply(value.trim());
				
				if (value.length() == 0)
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
			selectedModules = projectGenerator.getSelectedModules(templateNameSet.get(projectGenerator));
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
		
		try {
			File todoPath = new File(targetDirectory.getPath() + File.separator + "TODO.md");
			List<String> todoList = ProjectGenerator.getTODOs(selectedModules);
			
			if (!todoList.isEmpty())
			{
				try (PrintStream todoPrinter = new PrintStream(new FileOutputStream(todoPath)))
				{
					todoPrinter.println("# Stuff To Do\n");
					int i = 1;
					for (String t : todoList)
					{
						todoPrinter.println(i + ") " + t);
						i++;
					}
				}
			}
		} catch (IOException e) {
			SwingUtils.error(e.getLocalizedMessage());
			return;
		}
		
		String toolKey = studio 
			? "doommake.newproject.modal.openproject.choice.studio"
			: "doommake.newproject.modal.openproject.choice.tools"
		;
		
		// Returns a trinary.
		Boolean result = modal(
			getApplicationContainer(), 
			utils.getWindowIcons(), 
			language.getText("doommake.newproject.modal.openproject.title"),
			containerOf(borderLayout(4, 4),
				node(BorderLayout.CENTER, label(language.getText("doommake.newproject.modal.openproject.message")))
			),
			utils.createChoiceFromLanguageKey("doommake.newproject.modal.openproject.choice.folder", true),
			utils.createChoiceFromLanguageKey(toolKey, false),
			utils.createChoiceFromLanguageKey("doommake.newproject.modal.openproject.choice.none", () -> null)
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
					SwingUtils.error(
						language.getText("doommake.newproject.modal.openproject.folder.error", targetDirectory.getAbsolutePath())
					);
				}
			} catch (IOException e) {
				SwingUtils.error(getApplicationContainer(), e.getLocalizedMessage());
			}
		}
		else // Open in Doom Tools
		{
			startApplication(studio ? new DoomMakeStudioApp(targetDirectory) : new DoomMakeOpenProjectApp(targetDirectory));
			attemptClose();
		}
		
	}

	// Make help menu for internal and desktop.
	private JMenu createHelpMenu()
	{
		return utils.createMenuFromLanguageKey("doomtools.menu.help",
			utils.createItemFromLanguageKey("doomtools.menu.help.item.changelog", (i) -> onHelpChangelog())
		); 
	}

	private void onHelpChangelog()
	{
		utils.createHelpModal(utils.helpResource("docs/changelogs/CHANGELOG-doommake.md")).open();
	}

}

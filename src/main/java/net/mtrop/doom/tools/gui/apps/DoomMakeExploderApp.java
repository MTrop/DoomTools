/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import net.mtrop.doom.Wad;
import net.mtrop.doom.WadFile;
import net.mtrop.doom.exception.WadException;
import net.mtrop.doom.graphics.Palette;
import net.mtrop.doom.tools.DoomMakeMain.ProjectType;
import net.mtrop.doom.tools.doommake.ProjectGenerator;
import net.mtrop.doom.tools.doommake.ProjectModule;
import net.mtrop.doom.tools.doommake.ProjectTemplate;
import net.mtrop.doom.tools.doommake.ProjectTokenReplacer;
import net.mtrop.doom.tools.doommake.ProjectTokenReplacer.GUIHint;
import net.mtrop.doom.tools.doommake.WADExploder;
import net.mtrop.doom.tools.doommake.generators.WADProjectGenerator;
import net.mtrop.doom.tools.exception.UtilityException;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.managers.AppCommon;
import net.mtrop.doom.tools.gui.managers.DoomToolsTaskManager;
import net.mtrop.doom.tools.gui.managers.settings.DoomMakeSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.struct.InstancedFuture;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.ArrayUtils;

import static javax.swing.BorderFactory.*;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.FormFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;
import static net.mtrop.doom.tools.struct.swing.ModalFactory.*;


/**
 * The DoomMake WAD Exploder application.
 * @author Matthew Tropiano
 */
public class DoomMakeExploderApp extends DoomToolsApplicationInstance
{
	private static final String STATE_SOURCE_WAD = "sourceWAD";
	private static final String STATE_PALETTE_FILE = "paletteFile";
	private static final String STATE_TARGET_DIRECTORY = "targetDirectory";

	/** Source WAD File. */
	private File sourceWAD;
	/** Target directory. */
	private File targetDirectory;

	/** Conversion flag.. */
	private boolean convert;
	/** Source Palette File. */
	private File paletteFile;
	
	/** Additional template names. */
	private List<String> templateNameList;
	
	/**
	 * Creates a new New Project app instance.
	 */
	public DoomMakeExploderApp() 
	{
		this(null);
	}
	
	/**
	 * Creates a new New Project app instance.
	 * @param sourceWADFile the source WAD file.
	 */
	public DoomMakeExploderApp(String sourceWADFile) 
	{
		this.sourceWAD = sourceWADFile != null ? new File(sourceWADFile) : null;
		this.paletteFile = null;
		this.targetDirectory = null;
		this.templateNameList = new ArrayList<>();
	}
	
	@Override
	public String getTitle()
	{
		return language.getText("doommake.exploder.title");
	}
	
	@Override
	public Map<String, String> getApplicationState()
	{
		Map<String, String> state = super.getApplicationState();
		if (sourceWAD != null)
			state.put(STATE_SOURCE_WAD, sourceWAD.getAbsolutePath());
		if (paletteFile != null)
			state.put(STATE_PALETTE_FILE, paletteFile.getAbsolutePath());
		if (targetDirectory != null)
			state.put(STATE_TARGET_DIRECTORY, targetDirectory.getAbsolutePath());
		return state;
	}

	@Override
	public void setApplicationState(Map<String, String> state)
	{
		this.sourceWAD = state.containsKey(STATE_SOURCE_WAD) ? new File(state.get(STATE_SOURCE_WAD)) : null;
		this.paletteFile = state.containsKey(STATE_PALETTE_FILE) ? new File(state.get(STATE_PALETTE_FILE)) : null;
		this.targetDirectory = state.containsKey(STATE_TARGET_DIRECTORY) ? new File(state.get(STATE_TARGET_DIRECTORY)) : null;
	}

	@Override
	public Container createContentPane()
	{
		final DoomMakeSettingsManager settings = DoomMakeSettingsManager.get();
		
		Container wadPanel = titlePanel(language.getText("doommake.exploder.wad"),
			containerOf(node(fileField(sourceWAD,
				(current) -> utils.chooseFile(
					getApplicationContainer(), 
					language.getText("doommake.exploder.wad.browse.title"), 
					language.getText("doommake.exploder.wad.browse.accept"), 
					() -> current != null ? current : settings.getLastTouchedFile(),
					settings::setLastTouchedFile,
					utils.createWADFileFilter()
				), 
				(selected) -> { 
					sourceWAD = selected;
				}
			)))
		);
		
		Container convertFormPanel = titlePanel(language.getText("doommake.exploder.convert"),
			containerOf(
				node(BorderLayout.CENTER, utils.createForm(form(language.getInteger("doommake.exploder.convert.formwidth")),
					utils.formField("doommake.exploder.convert.convert", checkBoxField(checkBox((c) -> convert = c))),
					utils.formField("doommake.exploder.convert.palette", fileField(paletteFile,
						(current) -> utils.chooseFile(
							getApplicationContainer(), 
							language.getText("doommake.exploder.palette.browse.title"), 
							language.getText("doommake.exploder.palette.browse.accept"), 
							() -> current != null ? current : settings.getLastTouchedFile(),
							settings::setLastTouchedFile,
							utils.createAllFilesFilter()
						), 
						(selected) -> { 
							sourceWAD = selected;
						}
					))
				))
			)
		);
		
		Container projectDirectoryPanel = titlePanel(language.getText("doommake.newproject.directory"),
			containerOf(node(fileField(targetDirectory,
				(current) -> utils.chooseDirectory(
					getApplicationContainer(), 
					language.getText("doommake.newproject.directory.browse.title"), 
					language.getText("doommake.newproject.directory.browse.accept"), 
					() -> current != null ? current : settings.getLastTouchedFile(),
					settings::setLastTouchedFile
				), 
				(selected) -> { 
					targetDirectory = selected;
					if (targetDirectory != null && targetDirectory.isDirectory() && targetDirectory.listFiles().length > 0)
						SwingUtils.warning(getApplicationContainer(), language.getText("doommake.newproject.directory.browse.notempty"));
				}
			)))
		);
		
		Container controlPane = containerOf(flowLayout(Flow.TRAILING, 4, 4), node(
			utils.createButtonFromLanguageKey("doommake.exploder.create", (i) -> doExplodeProject())
		));
	
		JPanel panel = new JPanel();
		
		return containerOf(dimension(385, 350),
			node(BorderLayout.NORTH, containerOf(panel, boxLayout(panel, BoxAxis.PAGE_AXIS),
				node(wadPanel),
				node(convertFormPanel),
				node(projectDirectoryPanel),
				node(scroll(
					containerOf(getWADGeneratorOptionNodes())
				))
			)),
			node(BorderLayout.CENTER, containerOf()),
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
	public boolean shouldClose(Object frame, boolean fromWorkspaceClear) 
	{
		return fromWorkspaceClear || SwingUtils.yesTo(language.getText("doomtools.application.close"));
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
	
	// Adds a template after removing associated ones.
	private void addTemplateName(String templateToAdd)
	{
		templateNameList.add(templateToAdd);
	}

	// Removes a template name.
	private void removeTemplateName(String templateToRemove)
	{
		templateNameList.remove(templateToRemove);
	}

	private void removeTemplateCategory(String categoryRepository)
	{
		for (ProjectTemplate template : (new WADProjectGenerator()).getTemplatesByCategory(categoryRepository))
			removeTemplateName(template.getName());
	}

	private Modal<String> createOptionModal(String title, String prompt, final ProjectTokenReplacer replacer)
	{
		final AtomicReference<String> stringValue = new AtomicReference<>();

		String defaultValue = replacer.getDefaultValue(); 
		Function<String, String> validator = replacer.getValidator(); 
		GUIHint guiHint = replacer.getGUIHint();
		
		final DoomMakeSettingsManager settings = DoomMakeSettingsManager.get();
		
		Component field;
		switch (guiHint)
		{
			default:
			case STRING:
				field = stringField(defaultValue, (value) -> stringValue.set(value));
				break;
			case FILE:
				field = fileField(
					(current) -> utils.chooseFile(
						getApplicationContainer(),
						language.getText("doommake.newproject.directory.browse.title"), 
						language.getText("doommake.newproject.directory.browse.accept"),
						() -> current != null ? current : settings.getLastTouchedFile(),
						settings::setLastTouchedFile
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
			if (replacer == WADProjectGenerator.REPLACER_PROJECT_DECOHACK)
				continue;
			
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
	private void doExplodeProject()
	{
		if (sourceWAD == null)
		{
			SwingUtils.error(language.getText("doommake.exploder.error.nullwad"));
			return;
		}
		else if (!sourceWAD.exists())
		{
			SwingUtils.error(language.getText("doommake.exploder.error.wadnotexist"));
			return;
		}
		else
		{
			try {
				if (!Wad.isWAD(sourceWAD))
				{
					SwingUtils.error(language.getText("doommake.exploder.error.badwad"));
					return;
				}
			} catch (IOException e) {
				SwingUtils.error(language.getText("doommake.exploder.error.badwad"));
				return;
			}
		}
		
		if (targetDirectory == null)
		{
			SwingUtils.error(language.getText("doommake.exploder.error.nulldirectory"));
			return;
		}
		else if (targetDirectory.exists() && !targetDirectory.isDirectory())
		{
			SwingUtils.error(language.getText("doommake.exploder.error.notadirectory"));
			return;
		}
		
		File[] files = targetDirectory.listFiles(); 
		if (files != null && files.length > 0)
		{
			SwingUtils.error(language.getText("doommake.exploder.error.baddirectory"));
			return;
		}

		Palette convertPalette = null;
		
		try (WadFile sourceWADFile = new WadFile(sourceWAD))
		{
			if (convert)
			{
				if (paletteFile != null)
				{
					try {
						if (Wad.isWAD(paletteFile))
						{
							try (WadFile palWad = new WadFile(paletteFile))
							{
								convertPalette = palWad.getDataAs("PLAYPAL", Palette.class);
							}
						}
						else
						{
							convertPalette = new Palette();
							convertPalette.readFile(paletteFile);
						}
					} catch (IOException e) {
						SwingUtils.error(language.getText("doommake.exploder.error.badpalettewad"));
						return;
					}
				}
				else if (sourceWADFile.contains("PLAYPAL"))
				{
					convertPalette = sourceWADFile.getDataAs("PLAYPAL", Palette.class);
				}
				
				if (convertPalette == null)
				{
					SwingUtils.error(language.getText("doommake.exploder.error.mustprovidepalette"));
					return;
				}
			}
			
			startProjectExplodeProcess(sourceWADFile, convertPalette);
			
		} catch (IOException e) {
			SwingUtils.error(language.getText("doommake.exploder.error.palioerror"));
			return;
		}
	}

	private void startProjectExplodeProcess(WadFile sourceWADFile, Palette convertPalette)
	{
		utils.createProcessModal(getApplicationContainer(), language.getText("doommake.exploder.process.title"), null, 
			(stdout, stderr, stdin) -> AppCommon.get().execute(
				new DoomToolsStatusPanel(),
				language.getText("doommake.exploder.activity"),
				language.getText("doommake.exploder.success"),
				language.getText("doommake.exploder.interrupt"),
				language.getText("doommake.exploder.error"),
				InstancedFuture.instance(() -> explodeProject(stdout, sourceWADFile, convertPalette)).spawn()
			)
		).start(DoomToolsTaskManager.get(), null, (result) -> {if (result == 0) finishExplode();});
	}

	private int explodeProject(PrintStream log, Wad wadFile, Palette convertPalette)
	{
		ProjectType projectType = WADExploder.getProjectTypeFromWAD(wadFile);
		if (projectType == null)
		{
			log.println("ERROR: Could not detect project type from provided WAD.");
			return 2;
		}
		
		log.println("Detected project type: " + projectType.name());
		
		ProjectGenerator projectGenerator = projectType.createGenerator();

		List<String> outTemplates = new ArrayList<>();
		outTemplates.addAll(templateNameList);
		
		try {
			WADExploder.getTemplatesFromWAD(projectType, wadFile, outTemplates);
		} catch (WadException e) {
			log.println("ERROR: " + e.getLocalizedMessage());
			return 1;
		} 
		
		log.println("Using templates: " + outTemplates.toString());
		
		SortedSet<ProjectModule> selectedModules;
		try {
			selectedModules = projectGenerator.getSelectedModules(outTemplates);
		} catch (UtilityException e) {
			SwingUtils.error(e.getLocalizedMessage());
			return 3;
		}
		
		Map<String, String> replacerMap;
		if ((replacerMap = getReplacerMap(selectedModules)) == null)
			return 3;
		
		try {
			projectGenerator.createProject(selectedModules, replacerMap, targetDirectory);
		} catch (IOException e) {
			SwingUtils.error(e.getLocalizedMessage());
			return 3;
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
			return 3;
		}

		log.println("Exploding WAD...");
		try {
			WADExploder.explodeIntoProject(log, wadFile, targetDirectory, convert, convertPalette);
		} catch (IOException e) {
			log.println("ERROR: " + e.getLocalizedMessage());
			return 1;
		}
		
		log.println("Done!");
		return 0;
	}
	
	private void finishExplode()
	{
		// Returns a trinary.
		Boolean result = modal(
			getApplicationContainer(), 
			utils.getWindowIcons(), 
			language.getText("doommake.newproject.modal.openproject.title"),
			containerOf(borderLayout(4, 4),
				node(BorderLayout.CENTER, label(language.getText("doommake.newproject.modal.openproject.message")))
			),
			utils.createChoiceFromLanguageKey("doommake.newproject.modal.openproject.choice.folder", true),
			utils.createChoiceFromLanguageKey("doommake.newproject.modal.openproject.choice.tools", false),
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
			startApplication(new DoomMakeOpenProjectApp(targetDirectory));
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

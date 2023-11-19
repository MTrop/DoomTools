/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.BevelBorder;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import net.mtrop.doom.Wad;
import net.mtrop.doom.tools.DoomMakeMain;
import net.mtrop.doom.tools.WadScriptMain;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.DoomToolsGUIMain;
import net.mtrop.doom.tools.gui.DoomToolsGUIMain.ApplicationNames;
import net.mtrop.doom.tools.gui.RepositoryHelper.Git;
import net.mtrop.doom.tools.gui.RepositoryHelper.Mercurial;
import net.mtrop.doom.tools.gui.apps.data.MergeSettings;
import net.mtrop.doom.tools.gui.apps.data.ScriptExecutionSettings;
import net.mtrop.doom.tools.gui.managers.AppCommon;
import net.mtrop.doom.tools.gui.managers.DoomToolsEditorProvider;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIPreWarmer;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsIconManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.settings.DoomMakeSettingsManager;
import net.mtrop.doom.tools.gui.managers.settings.DoomMakeStudioSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DirectoryTreePanel.DirectoryTreeListener;
import net.mtrop.doom.tools.gui.swing.panels.DoomMakeExecuteWithArgsPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomMakeExecutionPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsTextOutputPanel;
import net.mtrop.doom.tools.gui.swing.panels.EditorDirectoryTreePanel;
import net.mtrop.doom.tools.gui.swing.panels.EditorMultiFilePanel;
import net.mtrop.doom.tools.gui.swing.panels.EditorMultiFilePanel.ActionNames;
import net.mtrop.doom.tools.gui.swing.panels.EditorMultiFilePanel.EditorHandle;
import net.mtrop.doom.tools.gui.swing.panels.settings.DoomMakeSettingsPanel;
import net.mtrop.doom.tools.gui.swing.panels.GitRepositoryPanel;
import net.mtrop.doom.tools.gui.swing.panels.MercurialRepositoryPanel;
import net.mtrop.doom.tools.gui.swing.panels.ProjectSearchPanel;
import net.mtrop.doom.tools.gui.swing.panels.WadMergeExecuteWithArgsPanel;
import net.mtrop.doom.tools.gui.swing.panels.WadScriptExecuteWithArgsPanel;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.ProcessCallable;
import net.mtrop.doom.tools.struct.WatchServiceThread;
import net.mtrop.doom.tools.struct.swing.ComponentFactory.MenuNode;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.ArrayUtils;
import net.mtrop.doom.tools.struct.util.FileUtils.TempFile;
import net.mtrop.doom.util.MapUtils;
import net.mtrop.doom.util.WadUtils;

import static javax.swing.BorderFactory.*;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.ModalFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;


/**
 * The DoomMake Studio application.
 * @author Matthew Tropiano
 */
public class DoomMakeStudioApp extends DoomToolsApplicationInstance
{
    private static final AtomicLong NEW_COUNTER = new AtomicLong(1L);

	/** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(DoomMakeStudioApp.class); 

    // Singletons

    private final DoomMakeStudioApp SELF = this;
    
    /** Settings manager. */
	private DoomMakeSettingsManager settings;
	private DoomMakeStudioSettingsManager studioSettings;

	// Components

	private JTabbedPane tabPanel;
	private JSplitPane filesSplitPaneHorizontal;
	private JSplitPane filesSplitPaneVertical;
	private ProjectSearchPanel searchPanel;
	private JComponent repositoryPanel;
	private DoomToolsTextOutputPanel loggingPanel;
	private EditorDirectoryTreePanel treePanel;
	private DoomMakeExecutionPanel executionPanel;
	private DoomMakeEditorPanel editorPanel;
	private DoomToolsStatusPanel statusPanel;
	
	private Action runWadMergeAction;
	private Action runWadMergeParametersAction;
	private Action runWadScriptAction;
	private Action runWadScriptParametersAction;
	private Action runDoomMakeAction;
	private Action runDoomMakeParametersAction;
	
	// Fields
    
    /** Project directory. */
    private File projectDirectory;

	// State

    private EditorHandle currentHandle;
	private Map<EditorHandle, ScriptExecutionSettings> handleToWadScriptSettingsMap;
	private Map<EditorHandle, MergeSettings> handleToWadMergeSettingsMap;
	private ScriptExecutionSettings doomMakeSettings;
	
	private ProjectWatcher watcher;
	
	/**
	 * Creates a new open project application.
	 */
	public DoomMakeStudioApp()
	{
		this(null);
	}
	
    /**
	 * Creates a new open project application from a project directory.
     * @param targetDirectory 
	 */
	public DoomMakeStudioApp(File targetDirectory)
	{
		DoomToolsGUIPreWarmer.get();
		
		this.settings = DoomMakeSettingsManager.get();
		this.studioSettings = DoomMakeStudioSettingsManager.get();
		
		this.editorPanel = new DoomMakeEditorPanel(new EditorMultiFilePanel.Options() 
		{
			@Override
			public boolean hideStyleChangePanel() 
			{
				return false;
			}

			@Override
			public boolean hideTreeActions()
			{
				return false;
			}
		}, 
		new DoomMakeEditorPanel.Listener()
		{
			@Override
			public void onCurrentEditorChange(EditorHandle previous, EditorHandle next) 
			{
				currentHandle = next;
				onHandleChange();
			}

			@Override
			public void onSave(EditorHandle handle) 
			{
				File sourceFile = handle.getContentSourceFile();
				statusPanel.setSuccessMessage(language.getText("doommake.status.message.saved", sourceFile.getName()));
				refreshRepository();
				onHandleChange();
			}

			@Override
			public void onOpen(EditorHandle handle) 
			{
				statusPanel.setSuccessMessage(language.getText("doommake.status.message.editor.open", handle.getEditorTabName()));
			}

			@Override
			public void onClose(EditorHandle handle) 
			{
				statusPanel.setSuccessMessage(language.getText("doommake.status.message.editor.close", handle.getEditorTabName()));
				handleToWadScriptSettingsMap.remove(handle);
				handleToWadMergeSettingsMap.remove(handle);
			}

			@Override
			public void onTreeDirectoryRequest(EditorHandle handle)
			{
				File dir = handle.getContentSourceFile();
				if (dir != null)
					onOpenDirectory(dir.getParentFile());
			}

			@Override
			public void onTreeRevealRequest(EditorHandle handle) 
			{
				if (handle.getContentSourceFile() != null)
					treePanel.setSelectedFile(handle.getContentSourceFile());
			}
		});
		this.statusPanel = new DoomToolsStatusPanel();
		
		this.treePanel = new DoomMakeTreePanel();
		this.treePanel.setRootDirectory(targetDirectory);
		this.treePanel.setLabel(targetDirectory.getName());
		
		this.searchPanel = new ProjectSearchPanel(targetDirectory, (result) -> {
			onOpenFile(result.getSource(), (int)result.getOffset());
		});
		
		if (Git.isGit(targetDirectory))		
			this.repositoryPanel = new GitRepositoryPanel(targetDirectory);
		else if (Mercurial.isMercurial(targetDirectory))		
			this.repositoryPanel = new MercurialRepositoryPanel(targetDirectory);
		else
			this.repositoryPanel = containerOf(label(JLabel.CENTER, language.getText("doommake.tab.norepo")));
		
		this.loggingPanel = new DoomToolsTextOutputPanel();

		this.executionPanel = new DoomMakeExecutionPanel(statusPanel, targetDirectory, this.loggingPanel, true, loggingPanel.getPrintStream());		

		DoomToolsIconManager icons = DoomToolsIconManager.get();
		
		final int DEFAULT_WIDTH = 340;
		final int DEFAULT_HEIGHT = 350;
		
		JComponent fileTab = containerOf(createEmptyBorder(4, 4, 4, 4), 
			node(this.filesSplitPaneVertical = split(SplitOrientation.VERTICAL,
				containerOf(dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT), node(treePanel)),
				containerOf(dimension(DEFAULT_WIDTH, 150), node(executionPanel))
			))
		);
		JComponent searchTab = containerOf(createEmptyBorder(4, 4, 4, 4), 
			node(searchPanel)
		);
		JComponent repoTab = containerOf(createEmptyBorder(4, 4, 4, 4), 
			node(repositoryPanel)
		);
		JComponent loggingTab = containerOf(createEmptyBorder(4, 4, 4, 4), 
			node(containerOf(createBevelBorder(BevelBorder.LOWERED),
				node(scroll(loggingPanel))
			))
		);

		this.tabPanel = tabs(TabPlacement.LEFT, TabLayoutPolicy.SCROLL, 
			tab(icons.getImage("doommake-folder.png"), fileTab), 
			tab(icons.getImage("doommake-search.png"), searchTab),
			tab(icons.getImage("doommake-repo.png"), repoTab),
			tab(icons.getImage("doommake-log.png"), loggingTab)
		);

		this.filesSplitPaneHorizontal = split(
			containerOf( 
				node(BorderLayout.CENTER, tabPanel)
			),
			containerOf(
				node(BorderLayout.CENTER, editorPanel)
			)
		);
		this.filesSplitPaneHorizontal.setDividerLocation(DEFAULT_WIDTH);
		this.filesSplitPaneVertical.setDividerLocation(DEFAULT_HEIGHT);
		
		this.runWadMergeAction = utils.createActionFromLanguageKey("doommake.menu.run.item.wadmerge.run", (e) -> onRunWadMergeAgain());
		this.runWadMergeParametersAction = utils.createActionFromLanguageKey("doommake.menu.run.item.wadmerge.params", (e) -> onRunWadMergeWithArgs());
		this.runWadScriptAction = utils.createActionFromLanguageKey("doommake.menu.run.item.wadscript.run", (e) -> onRunWadScriptAgain());
		this.runWadScriptParametersAction = utils.createActionFromLanguageKey("doommake.menu.run.item.wadscript.params", (e) -> onRunWadScriptWithArgs());
		this.runDoomMakeAction = utils.createActionFromLanguageKey("doommake.menu.run.item.doommake.run", (e) -> onRunDoomMakeAgain());
		this.runDoomMakeParametersAction = utils.createActionFromLanguageKey("doommake.menu.run.item.doommake.params", (e) -> onRunDoomMakeWithArgs());

		this.handleToWadScriptSettingsMap = new HashMap<>();
		this.handleToWadMergeSettingsMap = new HashMap<>();
		this.doomMakeSettings = null;
		
		this.projectDirectory = targetDirectory;
		onHandleChange();
	}
	
	/**
	 * Opens a dialog for opening a directory, checks
	 * if the directory is a project directory, and then returns the directory. 
	 * @param parent the parent window for the dialog.
	 * @return the valid directory selected, or null if not valid.
	 */
	public static File openAndGetDirectory(Component parent)
	{
		DoomToolsLanguageManager language = DoomToolsLanguageManager.get();
		DoomMakeStudioSettingsManager studioSettings = DoomMakeStudioSettingsManager.get();
		DoomToolsGUIUtils utils = DoomToolsGUIUtils.get();
		
		File projectDir = utils.chooseDirectory(
			parent,
			language.getText("doommake.project.open.browse.title"),
			language.getText("doommake.project.open.browse.accept"),
			studioSettings::getLastProjectDirectory,
			studioSettings::setLastProjectDirectory
		);
		
		if (projectDir == null)
			return null;
		
		if (!isProjectDirectory(projectDir))
		{
			SwingUtils.error(parent, language.getText("doommake.project.open.browse.baddir", projectDir.getAbsolutePath()));
			return null;
		}
		
		return projectDir;
	}
	
	/**
	 * Opens a dialog for opening a directory, checks
	 * if the directory is a project directory, and returns an application instance. 
	 * @param parent the parent window for the dialog.
	 * @return a new app instance, or null if bad directory selected.
	 */
	public static DoomMakeStudioApp openAndCreate(Component parent)
	{
		File directory;
		if ((directory = openAndGetDirectory(parent)) == null)
			return null;
		return new DoomMakeStudioApp(directory);
	}
	
	/**
	 * Checks if a directory is a project directory.
	 * @param directory the directory to check.
	 * @return true if it is, false if not.
	 */
	public static boolean isProjectDirectory(File directory)
	{
		if (!directory.isDirectory())
			return false;
		if (!(new File(directory.getAbsolutePath() + File.separator + "doommake.script")).exists())
			return false;
		return true;
	}
	
	@Override
	public String getTitle()
	{
		return language.getText("doommake.studio.project.title", projectDirectory.getName());
	}

	@Override
	public Container createContentPane()
	{
		return containerOf(borderLayout(0, 8), 
			node(BorderLayout.CENTER, filesSplitPaneHorizontal),
			node(BorderLayout.SOUTH, statusPanel)
		);
	}

	@Override
	public JMenuBar createDesktopMenuBar() 
	{
		return menuBar(
			utils.createMenuFromLanguageKey("doommake.menu.file", ArrayUtils.joinArrays(
				createCommonFileMenuItems(),
				ArrayUtils.arrayOf(
					separator(),
					utils.createItemFromLanguageKey("doommake.menu.file.item.settings", (i) -> openSettings())
				),
				ArrayUtils.arrayOf(
					separator(),
					utils.createItemFromLanguageKey("doommake.menu.file.item.exit", (i) -> attemptClose())
				)
			)),
			utils.createMenuFromLanguageKey("doommake.menu.edit", createCommonEditMenuItems()),
			utils.createMenuFromLanguageKey("doommake.menu.run", createRunMenuItems()),
			utils.createMenuFromLanguageKey("doommake.menu.editor", createCommonEditorMenuItems()),
			createHelpMenu()
		);
	}
	
	@Override
	public JMenuBar createInternalMenuBar() 
	{
		return menuBar(
			utils.createMenuFromLanguageKey("doommake.menu.file", createCommonFileMenuItems()),
			utils.createMenuFromLanguageKey("doommake.menu.edit", createCommonEditMenuItems()),
			utils.createMenuFromLanguageKey("doommake.menu.run", createRunMenuItems()),
			utils.createMenuFromLanguageKey("doommake.menu.editor", createCommonEditorMenuItems()),
			createHelpMenu()
		);
	}

	@Override
	public void onCreate(Object frame) 
	{
		if (frame instanceof JFrame)
		{
			JFrame f = (JFrame)frame;
			Rectangle bounds = studioSettings.getBounds();
			boolean maximized = studioSettings.getBoundsMaximized();
			f.setBounds(bounds);
			if (maximized)
				f.setExtendedState(f.getExtendedState() | JFrame.MAXIMIZED_BOTH);
			filesSplitPaneHorizontal.setDividerLocation(studioSettings.getHorizontalDividerWidth());
			filesSplitPaneVertical.setDividerLocation(studioSettings.getVerticalDividerHeight());
		}
	}
	
	@Override
	public void onOpen(Object frame) 
	{
		if (projectDirectory == null)
			throw new IllegalStateException("Project directory not set!");
		
		// Set the last directory successfully opened.
		settings.setLastProjectDirectory(projectDirectory);
		onNewEditor();
		watcher = new ProjectWatcher();
		watcher.start();
	}
	
	@Override
	public void onClose(Object frame) 
	{
		watcher.interrupt();
		executionPanel.shutDownAgent();
		if (frame instanceof JFrame)
		{
			JFrame f = (JFrame)frame;
			studioSettings.setBounds(f);
			studioSettings.setHorizontalDividerWidth(filesSplitPaneHorizontal.getDividerLocation());
			studioSettings.setVerticalDividerHeight(filesSplitPaneVertical.getDividerLocation());
		}
	}
	
	@Override
	public boolean shouldClose(Object frame, boolean fromWorkspaceClear) 
	{
		if (!fromWorkspaceClear && !SwingUtils.yesTo(language.getText("doomtools.application.close")))
			return false;
		if (editorPanel.getUnsavedEditorCount() > 0)
			return editorPanel.closeAllEditors(false);
		return true;
	}
	
	@Override
	public Map<String, String> getApplicationState()
	{
		Map<String, String> state = super.getApplicationState();
		// Not executable as internal application.
		return state;
	}

	@Override
	public void setApplicationState(Map<String, String> state)
	{
		// Not executable as internal application.
	}

	private MenuNode[] createCommonFileMenuItems()
	{
		return ArrayUtils.arrayOf(
			utils.createItemFromLanguageKey("doommake.menu.file.item.new",
				utils.createItemFromLanguageKey("doommake.menu.file.item.new.item.blank", (i) -> onNewEditor()),
				utils.createItemFromLanguageKey("doommake.menu.file.item.new.item.project", (i) -> openNewProject())
			),
			utils.createItemFromLanguageKey("doommake.menu.file.item.open", (i) -> onOpenEditor()),
			utils.createItemFromLanguageKey("doommake.menu.file.item.open.project", (i) -> openOpenProject()),
			separator(),
			utils.createItemFromLanguageKey("texteditor.action.close", editorPanel.getActionFor(ActionNames.ACTION_CLOSE)),
			utils.createItemFromLanguageKey("texteditor.action.closeallbutcurrent", editorPanel.getActionFor(ActionNames.ACTION_CLOSE_ALL_BUT_CURRENT)),
			utils.createItemFromLanguageKey("texteditor.action.closeall", editorPanel.getActionFor(ActionNames.ACTION_CLOSE_ALL)),
			separator(),
			utils.createItemFromLanguageKey("texteditor.action.save", editorPanel.getActionFor(ActionNames.ACTION_SAVE)),
			utils.createItemFromLanguageKey("texteditor.action.saveas", editorPanel.getActionFor(ActionNames.ACTION_SAVE_AS)),
			utils.createItemFromLanguageKey("texteditor.action.saveall", editorPanel.getActionFor(ActionNames.ACTION_SAVE_ALL))
		);
	}

	private MenuNode[] createCommonEditMenuItems()
	{
		return ArrayUtils.arrayOf(
			utils.createItemFromLanguageKey("texteditor.action.undo", editorPanel.getActionFor(ActionNames.ACTION_UNDO)),
			utils.createItemFromLanguageKey("texteditor.action.redo", editorPanel.getActionFor(ActionNames.ACTION_REDO)),
			separator(),
			utils.createItemFromLanguageKey("texteditor.action.cut", editorPanel.getActionFor(ActionNames.ACTION_CUT)),
			utils.createItemFromLanguageKey("texteditor.action.copy", editorPanel.getActionFor(ActionNames.ACTION_COPY)),
			utils.createItemFromLanguageKey("texteditor.action.paste", editorPanel.getActionFor(ActionNames.ACTION_PASTE)),
			separator(),
			utils.createItemFromLanguageKey("texteditor.action.delete", editorPanel.getActionFor(ActionNames.ACTION_DELETE)),
			utils.createItemFromLanguageKey("texteditor.action.selectall", editorPanel.getActionFor(ActionNames.ACTION_SELECT_ALL))
		);
	}

	private MenuNode[] createRunMenuItems()
	{
		return ArrayUtils.arrayOf(
			utils.createItemFromLanguageKey("doommake.menu.run.item.wadmerge.run", runWadMergeAction),
			utils.createItemFromLanguageKey("doommake.menu.run.item.wadmerge.params", runWadMergeParametersAction),
			utils.createItemFromLanguageKey("doommake.menu.run.item.wadscript.run", runWadScriptAction),
			utils.createItemFromLanguageKey("doommake.menu.run.item.wadscript.params", runWadScriptParametersAction),
			utils.createItemFromLanguageKey("doommake.menu.run.item.doommake.run", runDoomMakeAction),
			utils.createItemFromLanguageKey("doommake.menu.run.item.doommake.params", runDoomMakeParametersAction)
		);
	}

	private MenuNode[] createCommonEditorMenuItems()
	{
		return ArrayUtils.arrayOf(
			utils.createItemFromLanguageKey("texteditor.action.goto", editorPanel.getActionFor(ActionNames.ACTION_GOTO)),
			utils.createItemFromLanguageKey("texteditor.action.find", editorPanel.getActionFor(ActionNames.ACTION_FIND)),
			separator(),
			editorPanel.getToggleLineWrapMenuItem(),
			editorPanel.getChangeEncodingMenuItem(),
			editorPanel.getChangeSpacingMenuItem(),
			editorPanel.getChangeLineEndingMenuItem(),
			separator(),
			editorPanel.getEditorPreferencesMenuItem()
		);
	}

	// Make help menu for internal and desktop.
	private JMenu createHelpMenu()
	{
		return utils.createMenuFromLanguageKey("doomtools.menu.help", ArrayUtils.joinArrays(
			ArrayUtils.arrayOf(
				utils.createItemFromLanguageKey("doomtools.menu.help.item.changelog", (i) -> onHelpChangelog()),
				separator()
			),
			AppCommon.get().getCommonHelpMenuItems(),
			ArrayUtils.arrayOf(
				separator(),
				utils.createItemFromLanguageKey("doommake.menu.help.item.rookscript", (i) -> onRookScriptReference()),
				utils.createItemFromLanguageKey("doommake.menu.help.item.wadscript.functions", (i) -> onWadScriptFunctionReference()),
				utils.createItemFromLanguageKey("doommake.menu.help.item.doommake.functions", (i) -> onDoomMakeFunctionReference())
			)
		)); 
	}

	// Open new project app (new instance).
	private void openNewProject()
	{
		try {
			DoomToolsGUIMain.startGUIAppProcess(ApplicationNames.DOOMMAKE_NEW, "", "true");
		} catch (IOException e) {
			LOG.error(e, "Couldn't start New Project!");
			SwingUtils.error(getApplicationContainer(), language.getText("doommake.error.app.newproject"));
		}
	}

	// Open Open project app (new instance).
	private void openOpenProject()
	{
		File dir;
		if ((dir = openAndGetDirectory(getApplicationContainer())) == null)
			return;
		
		try {
			DoomToolsGUIMain.startGUIAppProcess(ApplicationNames.DOOMMAKE_STUDIO, dir.getAbsolutePath());
		} catch (IOException e) {
			LOG.error(e, "Couldn't start Open Project: " + dir.getAbsolutePath());
			SwingUtils.error(getApplicationContainer(), language.getText("doommake.error.app.openproject", dir.getAbsolutePath()));
		}
	}

	// Open settings.
	private void openSettings()
	{
		modal(
			getApplicationContainer(),
			language.getText("doommake.project.settings.title"),
			new DoomMakeSettingsPanel()
		).openThenDispose();
	}

	private void onHelpChangelog()
	{
		utils.createHelpModal(utils.helpResource("docs/changelogs/CHANGELOG-doommake.md")).open();
	}

	private void onRookScriptReference()
	{
		utils.createHelpModal(utils.helpResource("docs/RookScript Quick Guide.md")).open();
	}

	private void onWadScriptFunctionReference()
	{
		utils.createHelpModal(utils.helpProcess(WadScriptMain.class, "--function-help")).open();
	}

	private void onDoomMakeFunctionReference()
	{
		utils.createHelpModal(utils.helpProcess(DoomMakeMain.class, "--function-help")).open();
	}

	// ====================================================================
	
	private void onHandleChange()
	{
		if (currentHandle != null)
		{
			boolean wadscript = currentHandle.getCurrentStyleType().equalsIgnoreCase(DoomToolsEditorProvider.SYNTAX_STYLE_WADSCRIPT);
			boolean wadmerge = currentHandle.getCurrentStyleType().equalsIgnoreCase(DoomToolsEditorProvider.SYNTAX_STYLE_WADMERGE);
			runWadMergeAction.setEnabled(wadmerge);
			runWadMergeParametersAction.setEnabled(wadmerge);
			runWadScriptAction.setEnabled(wadscript);
			runWadScriptParametersAction.setEnabled(wadscript);
		}
		else
		{
			runWadMergeAction.setEnabled(false);
			runWadMergeParametersAction.setEnabled(false);
			runWadScriptAction.setEnabled(false);
			runWadScriptParametersAction.setEnabled(false);
		}
	}

	private void onNewEditor()
	{
		String editorName = "New " + NEW_COUNTER.getAndIncrement();
		editorPanel.newEditor(editorName, "", Charset.defaultCharset(), RSyntaxTextArea.SYNTAX_STYLE_NONE, 0);
	}

	private void onOpenEditor()
	{
		final Container parent = getApplicationContainer();
		
		File file = utils.chooseFile(
			parent, 
			language.getText("doommake.open.title"), 
			language.getText("doommake.open.accept"),
			settings::getLastTouchedFile,
			settings::setLastTouchedFile
		);
		
		if (file != null)
			onOpenFile(file);
	}

	private EditorHandle onOpenFile(File file)
	{
		return onOpenFile(file, null);
	}
	
	private EditorHandle onOpenFile(File file, Integer offset)
	{
		final Container parent = getApplicationContainer();

		Boolean wadHandled = openWadFile(file); 
		
		if (wadHandled == Boolean.TRUE)
			return null;
		if (openUnknownFile(file, wadHandled == Boolean.FALSE))
			return null;
		
		try {
			return editorPanel.openFileEditor(file, offset != null ? offset : 0, Charset.defaultCharset());
		} catch (FileNotFoundException e) {
			LOG.errorf(e, "Selected file could not be found: %s", file.getAbsolutePath());
			statusPanel.setErrorMessage(language.getText("doommake.status.message.editor.error", file.getName()));
			SwingUtils.error(parent, language.getText("doommake.open.error.notfound", file.getAbsolutePath()));
		} catch (IOException e) {
			LOG.errorf(e, "Selected file could not be read: %s", file.getAbsolutePath());
			statusPanel.setErrorMessage(language.getText("doommake.status.message.editor.error", file.getName()));
			SwingUtils.error(parent, language.getText("doommake.open.error.ioerror", file.getAbsolutePath()));
		} catch (SecurityException e) {
			LOG.errorf(e, "Selected file could not be read (access denied): %s", file.getAbsolutePath());
			statusPanel.setErrorMessage(language.getText("doommake.status.message.editor.error.security", file.getName()));
			SwingUtils.error(parent, language.getText("doommake.open.error.security", file.getAbsolutePath()));
		}
		
		return null;
	}

	// Returns true if handled.
	private boolean openUnknownFile(File file, boolean recognized)
	{
		if (DoomToolsEditorProvider.get().getStyleByFile(file) != null)
			return false;
		
		if (SwingUtils.yesTo(language.getText(recognized ? "doommake.open.system.recognized" : "doommake.open.system")))
		{
			try {
				SwingUtils.open(file);
				return true;
			} catch (IOException e) {
				SwingUtils.error(language.getText("doommake.open.system.ioerror"));
			} catch (SecurityException e) {
				SwingUtils.error(language.getText("doommake.open.system.security"));
			}
		}
		return false;
	}
	
	// Returns true if handled.
	// Returns false if WAD, and not handled.
	// Returns null if not a WAD.
	private Boolean openWadFile(File file)
	{
		String[] mapHeaders = null;
		try {
			if (Wad.isWAD(file))
				mapHeaders = WadUtils.openWadAndGet(file, (wad) -> MapUtils.getAllMapHeaders(wad));
			else
				return null;
		} catch (IOException e) {
			LOG.errorf(e, "Selected file could not be read: %s", file.getAbsolutePath());
			statusPanel.setErrorMessage(language.getText("doommake.status.message.editor.error", file.getName()));
			SwingUtils.error(getApplicationContainer(), language.getText("doommake.open.error.ioerror", file.getAbsolutePath()));
			return true;
		}
		
		if (mapHeaders != null && mapHeaders.length > 0 && SwingUtils.yesTo(language.getText("doommake.open.mapeditor", mapHeaders.length)))
		{
			File editorExe = settings.getPathToMapEditor();
			if (editorExe == null || !editorExe.exists())
			{
				SwingUtils.error(getApplicationContainer(), language.getText("doommake.open.mapeditor.notfound", file.getAbsolutePath()));
				return true;
			}
			
			ProcessCallable callable = ProcessCallable.create(editorExe)
				.setWorkingDirectory(editorExe.getParentFile());
			callable.arg(file.getAbsolutePath());
			
			try {
				callable.exec();
				return true;
			} catch (IOException e) {
				SwingUtils.error(getApplicationContainer(), language.getText("doommake.open.mapeditor.ioerror", file.getAbsolutePath()));
				return true;
			} catch (SecurityException e) {
				SwingUtils.error(getApplicationContainer(), language.getText("doommake.open.mapeditor.security", file.getAbsolutePath()));
				return true;
			}
		}
		else if (SwingUtils.yesTo(language.getText("doommake.open.slade")))
		{
			File editorExe = settings.getPathToSlade();
			if (editorExe == null || !editorExe.exists())
			{
				SwingUtils.error(getApplicationContainer(), language.getText("doommake.open.slade.notfound", file.getAbsolutePath()));
				return true;
			}
			
			ProcessCallable callable = ProcessCallable.create(editorExe)
				.setWorkingDirectory(editorExe.getParentFile());
			callable.arg(file.getAbsolutePath());
			
			try {
				callable.exec();
				return true;
			} catch (IOException e) {
				SwingUtils.error(getApplicationContainer(), language.getText("doommake.open.slade.ioerror", file.getAbsolutePath()));
				return true;
			} catch (SecurityException e) {
				SwingUtils.error(getApplicationContainer(), language.getText("doommake.open.slade.security", file.getAbsolutePath()));
				return true;
			}
		}
		return false;
	}

	private void onOpenDirectory(File directory)
	{
		treePanel.setTemporaryRootDirectory(directory);
	}

	private void onRunWadMergeAgain()
	{
		if (!saveBeforeExecute())
			return;
	
		// Source file should be set if saveBeforeExecute() succeeds.
		// If no file, then make a temporary one for execution.
		
		if (currentHandle.getContentSourceFile() != null)
		{
			File scriptFile = currentHandle.getContentSourceFile();
			executeWadMergeAgain(scriptFile, scriptFile.getParentFile());
		}
		else
		{
			try (TempFile scriptFile = currentHandle.createTempCopy())
			{
				executeWadMergeAgain(scriptFile, treePanel.getRootDirectory());
			}
		}
	}

	private void onRunWadMergeWithArgs()
	{
		if (!saveBeforeExecute())
			return;
		
		// Source file should be set if saveBeforeExecute() succeeds.
		// If no file, then make a temporary one for execution.
		
		if (currentHandle.getContentSourceFile() != null)
		{
			File scriptFile = currentHandle.getContentSourceFile();
			executeWadMergeWithArgs(scriptFile, scriptFile.getParentFile());
		}
		else
		{
			try (TempFile scriptFile = currentHandle.createTempCopy())
			{
				executeWadMergeWithArgs(scriptFile, treePanel.getRootDirectory());
			}
		}
	}

	private void onRunWadScriptAgain()
	{
		if (!saveBeforeExecute())
			return;
	
		// Source file should be set if saveBeforeExecute() succeeds.
		// If no file, then make a temporary one for execution.
		
		if (currentHandle.getContentSourceFile() != null)
		{
			File scriptFile = currentHandle.getContentSourceFile();
			executeWadScriptAgain(scriptFile, scriptFile.getParentFile());
		}
		else
		{
			try (TempFile scriptFile = currentHandle.createTempCopy())
			{
				executeWadScriptAgain(scriptFile, treePanel.getRootDirectory());
			}
		}
	}

	private void onRunWadScriptWithArgs()
	{
		if (!saveBeforeExecute())
			return;
		
		// Source file should be set if saveBeforeExecute() succeeds.
		// If no file, then make a temporary one for execution.
		
		if (currentHandle.getContentSourceFile() != null)
		{
			File scriptFile = currentHandle.getContentSourceFile();
			executeWadScriptWithArgs(scriptFile, currentHandle.getContentCharset(), scriptFile.getParentFile());
		}
		else
		{
			try (TempFile scriptFile = currentHandle.createTempCopy())
			{
				executeWadScriptWithArgs(scriptFile, currentHandle.getContentCharset(), treePanel.getRootDirectory());
			}
		}
	}

	private void onRunDoomMakeAgain()
	{
		File primaryScript = new File(projectDirectory.getAbsolutePath() + File.separator + "doommake.script");
		executeDoomMakeAgain(primaryScript, projectDirectory);
	}

	private void onRunDoomMakeWithArgs()
	{
		File primaryScript = new File(projectDirectory.getAbsolutePath() + File.separator + "doommake.script");
		executeDoomMakeWithArgs(primaryScript, projectDirectory);
	}

	private boolean saveBeforeExecute()
	{
		final Container parent = getApplicationContainer();
	
		if (currentHandle.getContentSourceFile() != null && currentHandle.needsToSave())
		{
			Boolean saveChoice = modal(parent, utils.getWindowIcons(), 
				language.getText("wadscript.run.save.modal.title"),
				containerOf(label(language.getText("wadscript.run.save.modal.message", currentHandle.getEditorTabName()))), 
				utils.createChoiceFromLanguageKey("texteditor.action.save.modal.option.save", true),
				utils.createChoiceFromLanguageKey("texteditor.action.save.modal.option.nosave", false),
				utils.createChoiceFromLanguageKey("doomtools.cancel", (Boolean)null)
			).openThenDispose();
			
			if (saveChoice == null)
				return false;
			else if (saveChoice == true)
			{
				if (!editorPanel.saveCurrentEditor())
					return false;
			}
			// else, continue on.
		}
		
		return true;
	}

	private void executeWadScriptAgain(File scriptFile, File workDir)
	{
		ScriptExecutionSettings executionSettings;
		if ((executionSettings = handleToWadScriptSettingsMap.get(currentHandle)) == null)
			executionSettings = createExecutionSettings(new ScriptExecutionSettings(workDir));
		
		if (executionSettings == null)
			return;
		
		handleToWadScriptSettingsMap.put(currentHandle, executionSettings);
		appCommon.onExecuteWadScript(getApplicationContainer(), statusPanel, scriptFile, currentHandle.getContentCharset(), executionSettings);
	}

	private void executeWadScriptWithArgs(File scriptFile, Charset encoding, File workDir) 
	{
		ScriptExecutionSettings executionSettings;
		executionSettings = handleToWadScriptSettingsMap.get(currentHandle);
		executionSettings = createExecutionSettings(executionSettings != null ? executionSettings : new ScriptExecutionSettings(workDir));
	
		if (executionSettings == null)
			return;
		
		handleToWadScriptSettingsMap.put(currentHandle, executionSettings);
		appCommon.onExecuteWadScript(getApplicationContainer(), statusPanel, scriptFile, encoding, executionSettings);
	}

	private void executeDoomMakeAgain(File scriptFile, File workDir)
	{
		ScriptExecutionSettings executionSettings;
		if ((executionSettings = doomMakeSettings) == null)
		{
			ScriptExecutionSettings exe = new ScriptExecutionSettings(workDir);
			exe.setEntryPoint("make");
			executionSettings = createDoomMakeExecutionSettings(exe);
		}
		
		if (executionSettings == null)
			return;
		
		doomMakeSettings = executionSettings;
		
		final File standardInPath = executionSettings.getStandardInPath();
		final String target = executionSettings.getEntryPoint();
		final String[] args = executionSettings.getArgs();
		
		appCommon.onExecuteDoomMake(getApplicationContainer(), statusPanel, workDir, standardInPath, target, args, true, null, null);
	}

	private void executeDoomMakeWithArgs(File scriptFile, File workDir) 
	{
		ScriptExecutionSettings executionSettings;
		executionSettings = doomMakeSettings;
		
		if (executionSettings != null)
			executionSettings = createDoomMakeExecutionSettings(executionSettings);
		else
		{
			ScriptExecutionSettings exe = new ScriptExecutionSettings(workDir);
			exe.setEntryPoint("make");
			executionSettings = createDoomMakeExecutionSettings(exe);
		}
	
		if (executionSettings == null)
			return;
		
		doomMakeSettings = executionSettings;
		
		final File standardInPath = executionSettings.getStandardInPath();
		final String target = executionSettings.getEntryPoint();
		final String[] args = executionSettings.getArgs();
		
		appCommon.onExecuteDoomMake(getApplicationContainer(), statusPanel, workDir, standardInPath, target, args, true, null, null);
	}

	private ScriptExecutionSettings createExecutionSettings(ScriptExecutionSettings initSettings) 
	{
		final WadScriptExecuteWithArgsPanel argsPanel = new WadScriptExecuteWithArgsPanel(initSettings);
		ScriptExecutionSettings settings = utils.createSettingsModal(
			language.getText("wadscript.run.withargs.title"),
			argsPanel,
			(panel) -> {
				ScriptExecutionSettings out = new ScriptExecutionSettings();
				out.setWorkingDirectory(panel.getWorkingDirectory());
				out.setStandardInPath(panel.getStandardInPath());
				out.setEntryPoint(panel.getEntryPoint());
				out.setArgs(panel.getArgs());
				return out;
			},
			utils.createChoiceFromLanguageKey("wadscript.run.withargs.choice.run", true),
			utils.createChoiceFromLanguageKey("doomtools.cancel")
		);
		
		return settings;
	}

	private ScriptExecutionSettings createDoomMakeExecutionSettings(ScriptExecutionSettings initSettings) 
	{
		final DoomMakeExecuteWithArgsPanel argsPanel = new DoomMakeExecuteWithArgsPanel(initSettings);
		ScriptExecutionSettings settings = utils.createSettingsModal(
			language.getText("wadscript.run.withargs.title"),
			argsPanel,
			(panel) -> {
				ScriptExecutionSettings out = new ScriptExecutionSettings();
				out.setStandardInPath(panel.getStandardInPath());
				out.setEntryPoint(panel.getEntryPoint());
				out.setArgs(panel.getArgs());
				return out;
			},
			utils.createChoiceFromLanguageKey("doommake.run.withargs.choice.run", true),
			utils.createChoiceFromLanguageKey("doomtools.cancel")
		);
		
		return settings;
	}

	private void executeWadMergeWithArgs(File scriptFile, File workDir) 
	{
		MergeSettings mergeSettings;
		mergeSettings = handleToWadMergeSettingsMap.get(currentHandle);
		mergeSettings = createMergeSettings(mergeSettings != null ? mergeSettings : new MergeSettings(workDir));
	
		if (mergeSettings == null)
			return;
		
		handleToWadMergeSettingsMap.put(currentHandle, mergeSettings);
		appCommon.onExecuteWadMerge(getApplicationContainer(), statusPanel, scriptFile, currentHandle.getContentCharset(), mergeSettings);
	}

	private void executeWadMergeAgain(File scriptFile, File workDir)
	{
		MergeSettings mergeSettings;
		if ((mergeSettings = handleToWadMergeSettingsMap.get(currentHandle)) == null)
			mergeSettings = createMergeSettings(new MergeSettings(workDir));
		
		if (mergeSettings == null)
			return;
		
		handleToWadMergeSettingsMap.put(currentHandle, mergeSettings);
		appCommon.onExecuteWadMerge(getApplicationContainer(), statusPanel, scriptFile, currentHandle.getContentCharset(), mergeSettings);
	}

	private MergeSettings createMergeSettings(MergeSettings initSettings) 
	{
		final WadMergeExecuteWithArgsPanel argsPanel = new WadMergeExecuteWithArgsPanel(initSettings);
		MergeSettings settings = utils.createSettingsModal(
			language.getText("wadmerge.run.withargs.title"),
			argsPanel,
			(panel) -> {
				MergeSettings out = new MergeSettings();
				out.setWorkingDirectory(panel.getWorkingDirectory());
				out.setVerboseOutput(panel.getVerboseOutput());
				out.setArgs(panel.getArgs());
				return out;
			},
			utils.createChoiceFromLanguageKey("wadmerge.run.withargs.choice.run", true),
			utils.createChoiceFromLanguageKey("doomtools.cancel")
		);
		
		return settings;
	}
	
	private void refreshRepository() 
	{
		if (repositoryPanel instanceof GitRepositoryPanel)
			((GitRepositoryPanel)repositoryPanel).refreshEntries();
		else if (repositoryPanel instanceof MercurialRepositoryPanel)
			((MercurialRepositoryPanel)repositoryPanel).refreshEntries();
	}

	private void onProjectFileCreated(File file)
	{
		searchPanel.registerFile(file);
	}
	
	private void onProjectFileModified(File file)
	{
		searchPanel.registerFile(file);
	}
	
	private void onProjectFileDeleted(File file)
	{
		searchPanel.deregisterFile(file);
	}
	
	private void onWatcherError(String message)
	{
		statusPanel.setErrorMessage(message);
	}

	private class DoomMakeTreePanel extends EditorDirectoryTreePanel
	{
		private static final long serialVersionUID = -7123360958593821728L;

		public DoomMakeTreePanel()
		{
			super();
			setListener(new DirectoryTreeListener() 
			{
				@Override
				public void onFileSelectionChange(File[] selectedFiles)
				{
					// Do nothing.
				}
	
				@Override
				public void onFilesDeleted(File[] deletedFiles) 
				{
					statusPanel.setSuccessMessage(language.getText("doommake.dirtree.delete.result", deletedFiles.length));
				}
				
				@Override
				public void onFilesCopied(File[] copiedFiles) 
				{
					statusPanel.setSuccessMessage(language.getText("doommake.dirtree.copy.result", copiedFiles.length));
				}
				
				@Override
				public void onFileRename(File changedFile, String newName)
				{
					statusPanel.setSuccessMessage(language.getText("doommake.dirtree.rename.result", changedFile.getName(), newName));
				}
				
				@Override
				public void onFileConfirmed(File confirmedFile)
				{
					onOpenFile(confirmedFile);
				}
			});
		}
	}

	private class DoomMakeEditorPanel extends EditorMultiFilePanel
	{
		private static final long serialVersionUID = 5776973924467063595L;
		
		public DoomMakeEditorPanel(Options options, Listener listener)
		{
			super(options, listener);
		}
	
		@Override
		protected File getLastPathTouched() 
		{
			return settings.getLastTouchedFile();
		}
		
		@Override
		protected void setLastPathTouched(File saved) 
		{
			settings.setLastTouchedFile(saved);
		}
	}
	
	private class ProjectWatcher extends WatchServiceThread
	{
		private ProjectWatcher()
		{
			super(
				projectDirectory,
				true,
				SELF::onProjectFileCreated,
				SELF::onProjectFileModified,
				SELF::onProjectFileDeleted,
				SELF::onWatcherError
			);
		}
	}
}

package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JSplitPane;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import net.mtrop.doom.tools.WadScriptMain;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.DoomToolsGUIMain;
import net.mtrop.doom.tools.gui.DoomToolsGUIMain.ApplicationNames;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.settings.DoomMakeSettingsManager;
import net.mtrop.doom.tools.gui.managers.settings.DoomMakeStudioSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DirectoryTreePanel.DirectoryTreeListener;
import net.mtrop.doom.tools.gui.swing.panels.DoomMakeExecutionPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomMakeSettingsPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.gui.swing.panels.EditorDirectoryTreePanel;
import net.mtrop.doom.tools.gui.swing.panels.EditorMultiFilePanel;
import net.mtrop.doom.tools.gui.swing.panels.EditorMultiFilePanel.ActionNames;
import net.mtrop.doom.tools.gui.swing.panels.EditorMultiFilePanel.EditorHandle;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.swing.ComponentFactory.MenuNode;
import net.mtrop.doom.tools.struct.swing.SwingUtils;
import net.mtrop.doom.tools.struct.util.ArrayUtils;

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
    private static final String STATE_PROJECT_DIRECTORY = "projectDirectory";

    private static final AtomicLong NEW_COUNTER = new AtomicLong(1L);

	/** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(DoomMakeStudioApp.class); 

    // Singletons

    /** Settings manager. */
	private DoomMakeSettingsManager settings;
	private DoomMakeStudioSettingsManager studioSettings;

	// Components

	private EditorDirectoryTreePanel treePanel;
	private DoomMakeExecutionPanel executionPanel;
	private DoomMakeEditorPanel editorPanel;
	private DoomToolsStatusPanel statusPanel;
	
	// Fields
    
    /** Project directory. */
    private File projectDirectory;

	// State

    private EditorHandle currentHandle;

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
		this.settings = DoomMakeSettingsManager.get();
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
		onOpenDirectory(targetDirectory);
		
		this.executionPanel = new DoomMakeExecutionPanel(statusPanel, targetDirectory);
		this.projectDirectory = targetDirectory;
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
		return language.getText("doommake.project.title", projectDirectory.getName());
	}

	@Override
	public Container createContentPane()
	{
		JSplitPane sideBar = split(SplitOrientation.VERTICAL,
			containerOf(dimension(215, 350), node(treePanel)),
			containerOf(dimension(215, 150), node(executionPanel))
		);
		
		JSplitPane splitPane = split(
			containerOf(dimension(215, 500), 
				node(BorderLayout.CENTER, sideBar)
			),
			containerOf(dimension(610, 500),
				node(BorderLayout.CENTER, editorPanel)
			)
		);
		splitPane.setDividerLocation(215);
		sideBar.setDividerLocation(350);
		
		return containerOf(borderLayout(0, 8), 
			node(BorderLayout.CENTER, splitPane),
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
	public void onOpen(Object frame) 
	{
		if (projectDirectory == null)
			throw new IllegalStateException("Project directory not set!");
		
		// Set the last directory successfully opened.
		settings.setLastProjectDirectory(projectDirectory);
	}
	
	@Override
	public void onClose(Object frame) 
	{
		executionPanel.shutDownAgent();
	}
	
	@Override
	public boolean shouldClose() 
	{
		if (editorPanel.getUnsavedEditorCount() > 0)
			return editorPanel.closeAllEditors(false);
		return true;
	}
	
	@Override
	public Map<String, String> getApplicationState()
	{
		Map<String, String> state = super.getApplicationState();
		state.put(STATE_PROJECT_DIRECTORY, projectDirectory.getAbsolutePath());
		executionPanel.saveState(state);
		return state;
	}

	@Override
	public void setApplicationState(Map<String, String> state)
	{
		this.projectDirectory = state.containsKey(STATE_PROJECT_DIRECTORY) ? new File(state.get(STATE_PROJECT_DIRECTORY)) : null;
		executionPanel.loadState(state);
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

	// Open new project app (new instance).
	private void openNewProject()
	{
		try {
			DoomToolsGUIMain.startGUIAppProcess(ApplicationNames.DOOMMAKE_NEW);
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

	// Make help menu for internal and desktop.
	private JMenu createHelpMenu()
	{
		return utils.createMenuFromLanguageKey("doomtools.menu.help",
			utils.createItemFromLanguageKey("doomtools.menu.help.item.changelog", (i) -> onHelpChangelog()),
			separator(),
			utils.createItemFromLanguageKey("doommake.menu.help.item.rookscript", (i) -> onRookScriptReference()),
			utils.createItemFromLanguageKey("doommake.menu.help.item.wadscript.functions", (i) -> onWadScriptFunctionReference())
		); 
	}
	
	private void onHelpChangelog()
	{
		utils.createHelpModal(utils.helpResource("docs/changelogs/CHANGELOG-doommake.md")).open();
	}

	// ====================================================================
	
	private void onHandleChange()
	{
		if (currentHandle != null)
		{
			// Do nothing, for now.
		}
		else
		{
			// Do nothing, for now.
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

	private void onOpenFile(File file)
	{
		final Container parent = getApplicationContainer();
		try {
			editorPanel.openFileEditor(file, Charset.defaultCharset());
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
	}

	private void onOpenDirectory(File directory)
	{
		treePanel.setRootDirectory(directory);
		treePanel.setLabel(directory.getName());
	}

	private void onRookScriptReference()
	{
		utils.createHelpModal(utils.helpResource("docs/RookScript Quick Guide.md")).open();
	}

	private void onWadScriptFunctionReference()
	{
		utils.createHelpModal(utils.helpProcess(WadScriptMain.class, "--function-help")).open();
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
	
}

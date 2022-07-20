package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.DoomToolsGUIMain;
import net.mtrop.doom.tools.gui.DoomToolsGUIMain.ApplicationNames;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.settings.DoomMakeSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DoomMakeExecutionPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomMakeSettingsPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.swing.SwingUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.ModalFactory.*;


/**
 * The DoomMake Open Project application.
 * @author Matthew Tropiano
 */
public class DoomMakeOpenProjectApp extends DoomToolsApplicationInstance
{
    private static final String STATE_PROJECT_DIRECTORY = "projectDirectory";

    /** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(DoomMakeOpenProjectApp.class); 

    // Singletons

    /** Settings manager. */
	private DoomMakeSettingsManager settings;

	// Components

	private DoomMakeExecutionPanel executionPanel;
	private DoomToolsStatusPanel statusPanel;
	
	// Fields
    
    /** Project directory. */
    private File projectDirectory;

    /**
	 * Creates a new open project application.
	 */
	public DoomMakeOpenProjectApp()
	{
		this(null);
	}
	
    /**
	 * Creates a new open project application from a project directory.
     * @param targetDirectory 
	 */
	public DoomMakeOpenProjectApp(File targetDirectory)
	{
		this.settings = DoomMakeSettingsManager.get();
		this.statusPanel = new DoomToolsStatusPanel();
		this.executionPanel = new DoomMakeExecutionPanel(statusPanel, targetDirectory, false);
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
		DoomMakeSettingsManager settings = DoomMakeSettingsManager.get();
		DoomToolsGUIUtils utils = DoomToolsGUIUtils.get();
		
		File projectDir = utils.chooseDirectory(
			parent,
			language.getText("doommake.project.open.browse.title"),
			language.getText("doommake.project.open.browse.accept"),
			settings::getLastProjectDirectory,
			settings::setLastProjectDirectory
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
	public static DoomMakeOpenProjectApp openAndCreate(Component parent)
	{
		File directory;
		if ((directory = openAndGetDirectory(parent)) == null)
			return null;
		return new DoomMakeOpenProjectApp(directory);
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
		return containerOf(
			dimension(300, 225),
			node(BorderLayout.CENTER, executionPanel),
			node(BorderLayout.SOUTH, statusPanel)
		);
	}

	@Override
	public JMenuBar createDesktopMenuBar() 
	{
		return menuBar(
			// File
			utils.createMenuFromLanguageKey("doommake.menu.file",
				utils.createItemFromLanguageKey("doommake.menu.file.item.new", (i) -> openNewProject()),
				utils.createItemFromLanguageKey("doommake.menu.file.item.open", (i) -> openOpenProject()),
				separator(),
				utils.createItemFromLanguageKey("doommake.menu.file.item.settings", (i) -> openSettings()),
				separator(),
				utils.createItemFromLanguageKey("doommake.menu.file.item.exit", (i) -> attemptClose())
			),
			createHelpMenu()
		);
	}
	
	@Override
	public JMenuBar createInternalMenuBar() 
	{
		return menuBar(createHelpMenu());
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
	public Map<String, String> getApplicationState()
	{
		Map<String, String> state = super.getApplicationState();
		state.put(STATE_PROJECT_DIRECTORY, projectDirectory.getAbsolutePath());
		executionPanel.saveState("doommake", state);
		return state;
	}

	@Override
	public void setApplicationState(Map<String, String> state)
	{
		this.projectDirectory = state.containsKey(STATE_PROJECT_DIRECTORY) ? new File(state.get(STATE_PROJECT_DIRECTORY)) : null;
		executionPanel.loadState("doommake", state);
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
			DoomToolsGUIMain.startGUIAppProcess(ApplicationNames.DOOMMAKE_OPEN, dir.getAbsolutePath());
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
			utils.createItemFromLanguageKey("doomtools.menu.help.item.changelog", (i) -> onHelpChangelog())
		); 
	}
	
	private void onHelpChangelog()
	{
		utils.createHelpModal(utils.helpResource("docs/changelogs/CHANGELOG-doommake.md")).open();
	}
	
}

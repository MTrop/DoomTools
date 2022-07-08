package net.mtrop.doom.tools.gui.apps;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JMenuBar;

import net.mtrop.doom.tools.doommake.AutoBuildAgent;
import net.mtrop.doom.tools.doommake.AutoBuildAgent.Listener;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.DoomToolsGUIMain;
import net.mtrop.doom.tools.gui.DoomToolsGUIMain.ApplicationNames;
import net.mtrop.doom.tools.gui.managers.DoomMakeProjectHelper;
import net.mtrop.doom.tools.gui.managers.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.managers.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.managers.DoomToolsLogger;
import net.mtrop.doom.tools.gui.managers.DoomMakeProjectHelper.ProcessCallException;
import net.mtrop.doom.tools.gui.managers.settings.DoomMakeSettingsManager;
import net.mtrop.doom.tools.gui.swing.panels.DoomMakeProjectControlPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomMakeProjectTargetListPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomMakeSettingsPanel;
import net.mtrop.doom.tools.gui.swing.panels.DoomToolsStatusPanel;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.swing.SwingUtils;

import static javax.swing.BorderFactory.*;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;
import static net.mtrop.doom.tools.struct.swing.LayoutFactory.*;
import static net.mtrop.doom.tools.struct.swing.ModalFactory.*;
import static net.mtrop.doom.tools.struct.util.ObjectUtils.apply;


/**
 * The DoomMake New Project application.
 * @author Matthew Tropiano
 */
public class DoomMakeOpenProjectApp extends DoomToolsApplicationInstance
{
    private static final String STATE_PROJECT_DIRECTORY = "projectDirectory";

    /** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(DoomMakeOpenProjectApp.class); 

    private static final String[] NO_ARGS = new String[0];

    // Singletons

	/** Utils. */
	private DoomToolsGUIUtils utils;
    /** Language manager. */
    private DoomToolsLanguageManager language;
    /** Project helper. */
    private DoomMakeProjectHelper helper;
    /** Settings manager. */
	private DoomMakeSettingsManager settings;
    /** App Common. */
	private AppCommon appCommon;

	// Components
	
    /** Targets component. */
    private DoomMakeProjectTargetListPanel listPanel;
    /** Checkbox for flagging auto-build. */
    private JCheckBox autoBuildCheckbox;
    /** Target run action. */
    private Action targetRunAction;
    /** Status messages. */
    private DoomToolsStatusPanel statusPanel;

	// Fields
    
    /** Project directory. */
    private File projectDirectory;

    // State
    
    /** Current target. */
    private String currentTarget;
    /** Auto build agent. */
    private AutoBuildAgent autoBuildAgent;

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
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		this.helper = DoomMakeProjectHelper.get();
		this.settings = DoomMakeSettingsManager.get();
		this.appCommon = AppCommon.get();

		this.listPanel = new DoomMakeProjectTargetListPanel(
			Collections.emptySortedSet(),
			(target) -> setCurrentTarget(target), 
			(target) -> { 
				setCurrentTarget(target);
				runCurrentTarget();
			}
		);
		this.autoBuildCheckbox = checkBox(language.getText("doommake.project.autobuild"), false, (v) -> {
			if (v)
				startAgent();
			else
				shutDownAgent();
		});
		
		this.targetRunAction = actionItem(language.getText("doommake.project.buildaction"), (e) -> runCurrentTarget());

		this.statusPanel = new DoomToolsStatusPanel();
		this.statusPanel.setSuccessMessage(language.getText("doommake.project.build.message.ready"));

		this.projectDirectory = targetDirectory;
		
		this.currentTarget = null;
		this.autoBuildAgent = null;
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
		DoomMakeProjectControlPanel control = new DoomMakeProjectControlPanel(projectDirectory);
		refreshTargets();
		
		return containerOf(
			dimension(300, 225),
			node(containerOf(
				node(BorderLayout.NORTH, containerOf(createEmptyBorder(0, 4, 0, 4),
					node(BorderLayout.CENTER, label(language.getText("doommake.project.targets"))),
					node(BorderLayout.EAST, control)
				)),
				node(BorderLayout.CENTER, containerOf(borderLayout(0, 4),
					node(BorderLayout.CENTER, containerOf(createEmptyBorder(4, 4, 4, 4), 
						node(scroll(listPanel))
					)),
					node(BorderLayout.SOUTH, containerOf(borderLayout(0, 4),
						node(BorderLayout.CENTER, autoBuildCheckbox),
						node(BorderLayout.EAST, button(targetRunAction)),
						node(BorderLayout.SOUTH, statusPanel)
					))
				))
			))
		);
	}

	@Override
	public JMenuBar createDesktopMenuBar() 
	{
		return menuBar(
			// File
			utils.createMenuFromLanguageKey("doommake.menu.file",
				utils.createItemFromLanguageKey("doommake.menu.file.item.new",
					(c, e) -> openNewProject()
				),
				utils.createItemFromLanguageKey("doommake.menu.file.item.open",
					(c, e) -> openOpenProject()
				),
				separator(),
				utils.createItemFromLanguageKey("doommake.menu.file.item.settings",
					(c, e) -> openSettings()
				),
				separator(),
				utils.createItemFromLanguageKey("doommake.menu.file.item.exit",
					(c, e) -> attemptClose()
				)
			)
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
		if (autoBuildAgent != null)
			autoBuildAgent.shutDown();
	}
	
	@Override
	public Map<String, String> getApplicationState()
	{
		return apply(super.getApplicationState(), (state) -> {
			state.put(STATE_PROJECT_DIRECTORY, projectDirectory.getAbsolutePath());
		});
	}

	@Override
	public void setApplicationState(Map<String, String> state)
	{
		this.projectDirectory = state.containsKey(STATE_PROJECT_DIRECTORY) ? new File(state.get(STATE_PROJECT_DIRECTORY)) : null;
	}

	// Starts the agent.
	private void startAgent()
	{
		if (autoBuildAgent != null)
			throw new IllegalStateException("INTERNAL ERROR: Start agent while agent running!");
		
		autoBuildAgent = new AutoBuildAgent(projectDirectory, new Listener() 
		{
			@Override
			public void onAgentStarted() 
			{
				LOG.info("Agent started: " + projectDirectory.getPath());
				updateTargetsEnabled(false);
			}
			
			@Override
			public void onAgentStartupException(String message, Exception exception) 
			{
				LOG.error("Agent startup error: " + message);
			}

			@Override
			public void onAgentStopped() 
			{
				LOG.info("Agent stopped: " + projectDirectory.getPath());
				updateTargetsEnabled(true);
			}

			@Override
			public void onAgentStoppedException(String message, Exception exception) 
			{
				LOG.error("Agent stop error: " + message);
			}

			@Override
			public void onBuildPrepared() 
			{
				LOG.info("Change detected. Build prepared.");
			}

			@Override
			public void onBuildStart() 
			{
				LOG.info("Build started.");
			}

			@Override
			public void onBuildEnd(int result) 
			{
				LOG.info("Build ended.");
			}
			
			@Override
			public int callBuild(String target)
			{
				try {
					return AppCommon.callDoomMake(projectDirectory, target, true, NO_ARGS, null, null, null).get();
				} catch (InterruptedException e) {
					LOG.warn("DoomMake call interrupted!");
					return -1;
				} catch (ExecutionException e) {
					LOG.error(e, "Exception occurred on DoomMake call!");
					return -1;
				}
			}
			
		});
		
		autoBuildAgent.start();
	}
	
	private void shutDownAgent()
	{
		if (autoBuildAgent == null)
			throw new IllegalStateException("INTERNAL ERROR: Shutdown agent while agent not running!");

		autoBuildAgent.shutDown();
		autoBuildAgent = null;
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

	// Refresh targets.
	private void refreshTargets()
	{
		String absolutePath = projectDirectory.getAbsolutePath();
		try {
			listPanel.refreshTargets(helper.getProjectTargets(projectDirectory));
			LOG.infof("Targets refreshed for %s", absolutePath);
		} catch (FileNotFoundException e) {
			SwingUtils.error(getApplicationContainer(), language.getText("doommake.project.targets.error.nodirectory", absolutePath));
			LOG.errorf("Project directory does not exist: %s", absolutePath);
		} catch (ProcessCallException e) {
			SwingUtils.error(getApplicationContainer(), language.getText("doommake.project.targets.error.gettargets", absolutePath));
			LOG.errorf("Could not invoke `doommake --targets` in %s", absolutePath);
		}
	}
	
	/**
	 * Sets the current target to execute.
	 * @param target the new target.
	 */
	private void setCurrentTarget(String target)
	{
		currentTarget = target;
		updateTargetsEnabled(currentTarget != null);
	}
	
	/**
	 * Runs the current target.
	 */
	private void runCurrentTarget()
	{
		// execution failsafe
		if (!targetRunAction.isEnabled())
			return;
		if (autoBuildAgent != null && autoBuildAgent.isRunning())
			return;
		if (currentTarget == null)
			return;
		
		appCommon.onExecuteDoomMake(getApplicationContainer(), statusPanel, projectDirectory, null, currentTarget, NO_ARGS, false);
	}

	private void updateTargetsEnabled(boolean enabled)
	{
		final boolean state = enabled && (autoBuildAgent == null || !autoBuildAgent.isRunning());
		SwingUtils.invoke(() -> {
			listPanel.setEnabled(state);
			targetRunAction.setEnabled(state);
		});
	}
	
}

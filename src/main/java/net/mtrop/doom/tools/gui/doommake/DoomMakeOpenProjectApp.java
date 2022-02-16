package net.mtrop.doom.tools.gui.doommake;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JMenuBar;

import net.mtrop.doom.tools.doommake.AutoBuildAgent;
import net.mtrop.doom.tools.doommake.AutoBuildAgent.Listener;
import net.mtrop.doom.tools.gui.DoomToolsApplicationControlReceiver;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.DoomToolsLogger;
import net.mtrop.doom.tools.gui.DoomToolsTaskManager;
import net.mtrop.doom.tools.gui.doommake.DoomMakeProjectHelper.ProcessCallException;
import net.mtrop.doom.tools.gui.doommake.swing.panels.DoomMakeProjectControlPanel;
import net.mtrop.doom.tools.gui.doommake.swing.panels.DoomMakeProjectTargetListPanel;
import net.mtrop.doom.tools.gui.doommake.swing.panels.DoomMakeSettingsPanel;
import net.mtrop.doom.tools.gui.swing.panels.StatusPanel;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.swing.SwingUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;

/**
 * The DoomMake New Project application.
 * @author Matthew Tropiano
 */
public class DoomMakeOpenProjectApp implements DoomToolsApplicationInstance
{
    /** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(DoomMakeOpenProjectApp.class); 

    // Singletons

	/** Utils. */
	private DoomToolsGUIUtils utils;
    /** Language manager. */
    private DoomToolsLanguageManager language;
    /** Task manager. */
    private DoomToolsTaskManager tasks;
    /** Project helper. */
    private DoomMakeProjectHelper helper;

	// Control

    /** The app control receiver. */
	private DoomToolsApplicationControlReceiver receiver;

	// Components
	
    /** Targets component. */
    private DoomMakeProjectTargetListPanel listPanel;
    /** Checkbox for flagging auto-build. */
    private JCheckBox autoBuildCheckbox;
    /** Target run action. */
    private AbstractAction targetRunAction;
    /** Status messages. */
    private StatusPanel statusPanel;

	// Fields
    
    /** Project directory. */
    private final File projectDirectory;

    // State
    
    /** Current target. */
    private String currentTarget;
    /** Auto build agent. */
    private AutoBuildAgent autoBuildAgent;
    
    /**
	 * Creates a new open project application from a project directory.
	 * @param projectDirectory the project directory.
	 */
	public DoomMakeOpenProjectApp(File projectDirectory)
	{
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		this.tasks = DoomToolsTaskManager.get();
		this.helper = DoomMakeProjectHelper.get();
		this.receiver = null;
		
		this.listPanel = new DoomMakeProjectTargetListPanel(
			Collections.emptySortedSet(),
			(target) -> setCurrentTarget(target), 
			(target) -> { 
				setCurrentTarget(target);
				runCurrentTarget();
			}
		);
		this.autoBuildCheckbox = checkBox(language.getText("doommake.project.autobuild"), false, (c, e) -> 
		{
			if (c.isSelected())
				this.autoBuildAgent.start();
			else
				this.autoBuildAgent.shutDown();
		});
		this.targetRunAction = action(language.getText("doommake.project.buildaction"), (e) -> runCurrentTarget());

		this.statusPanel = new StatusPanel();
		this.statusPanel.setSuccessMessage(language.getText("doommake.project.build.message.ready"));

		this.projectDirectory = projectDirectory;
		
		this.currentTarget = null;
		
		Listener listener = new Listener() 
		{
			@Override
			public void onAgentStarted() 
			{
				LOG.info("Agent started: " + projectDirectory.getPath());
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
			}

			@Override
			public void onAgentStoppedException(String message, Exception exception) 
			{
				LOG.error("Agent stop error: " + message);
			}

			// TODO: Finish this.
			
		};
		
		this.autoBuildAgent = new AutoBuildAgent(projectDirectory, listener);
	}
	
	/**
	 * Opens a dialog for opening a directory, then checks
	 * if the directory is a project directory, and is a project directory. 
	 * @param parent the parent window for the dialog.
	 * @param initPath the init path for the dialog.
	 * @return a new app instance.
	 */
	public static DoomMakeOpenProjectApp openAndCreate(Component parent, File initPath)
	{
		DoomToolsLanguageManager language = DoomToolsLanguageManager.get();
		File directory = SwingUtils.directory(
			parent,
			language.getText("doommake.project.open.browse.title"),
			initPath,
			language.getText("doommake.project.open.browse.accept")
		);
		
		if (directory == null)
			return null;
		
		if (!isProjectDirectory(directory))
		{
			SwingUtils.error(parent, language.getText("doommake.project.open.browse.baddir", directory.getAbsolutePath()));
			return null;
		}
		
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
	public String getName()
	{
		return language.getText("doommake.project.title", projectDirectory.getName());
	}

	@Override
	public Container createContentPane()
	{
		DoomMakeProjectControlPanel control = new DoomMakeProjectControlPanel(projectDirectory);
		refreshTargets();
		Container out = containerOf(
			node(BorderFactory.createEmptyBorder(4, 4, 4, 4), new BorderLayout(), node(containerOf(
				node(BorderLayout.NORTH, containerOf(
					node(BorderLayout.EAST, control)
				)),
				node(BorderLayout.CENTER, containerOf(new BorderLayout(0, 4),
					node(BorderLayout.CENTER, scroll(listPanel)),
					node(BorderLayout.SOUTH, containerOf(new BorderLayout(0, 4),
						node(BorderLayout.CENTER, autoBuildCheckbox),
						node(BorderLayout.EAST, button(targetRunAction)),
						node(BorderLayout.SOUTH, statusPanel)
					))
				))
			)))
		);
		out.setPreferredSize(new Dimension(240, 200));
		return out;
	}

	@Override
	public JMenuBar createMenuBar() 
	{
		// TODO: Finish this.
		return null;
	}

	@Override
	public JMenuBar createInternalMenuBar() 
	{
		// TODO: Finish this.
		return null;
	}
	
	@Override
	public void setApplicationControlReceiver(DoomToolsApplicationControlReceiver receiver) 
	{
		this.receiver = receiver;
	}
	
	@Override
	public void onClose() 
	{
		autoBuildAgent.shutDown();
	}
	
	// Open settings.
	private void openSettings()
	{
		modal(
			receiver.getApplicationContainer(),
			language.getText("doommake.project.settings.title"),
			new DoomMakeSettingsPanel()
		).openThenDispose();
	}

	// Refresh targets.
	private void refreshTargets()
	{
		Container parent = receiver != null ? receiver.getApplicationContainer() : null;
		String absolutePath = projectDirectory.getAbsolutePath();
		try {
			listPanel.refreshTargets(helper.getProjectTargets(projectDirectory));
			LOG.infof("Targets refreshed for %s", absolutePath);
		} catch (FileNotFoundException e) {
			SwingUtils.error(parent, language.getText("doommake.project.targets.error.nodirectory", absolutePath));
			LOG.errorf("Project directory does not exist: %s", absolutePath);
		} catch (ProcessCallException e) {
			SwingUtils.error(parent, language.getText("doommake.project.targets.error.gettargets", absolutePath));
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
		targetRunAction.setEnabled(currentTarget != null);
	}
	
	/**
	 * Runs the current target.
	 */
	private void runCurrentTarget()
	{
		// execution failsafe
		if (!targetRunAction.isEnabled())
			return;
		
		runTarget(currentTarget);
	}

	private void runTarget(final String targetName)
	{
		tasks.spawn(() -> {
			try {
				statusPanel.setActivityMessage(language.getText("doommake.project.build.message.running", targetName));
				// TODO: Hook up to window, throw stuff into language file.
				int result =  helper.callDoomMakeTarget(projectDirectory, null, null, targetName).get();
				if (result == 0)
				{
					statusPanel.setSuccessMessage(language.getText("doommake.project.build.message.success"));
				}
				else
				{
					LOG.errorf("Error on DoomMake invoke (%s) result was %d: %s", targetName, result, projectDirectory.getAbsolutePath());
					statusPanel.setErrorMessage(language.getText("doommake.project.build.message.error"));
				}
			} catch (FileNotFoundException | ProcessCallException e) {
				LOG.errorf(e, "Error on DoomMake invoke (%s): %s", targetName, projectDirectory.getAbsolutePath());
				statusPanel.setErrorMessage("ERROR on build!");
			} catch (InterruptedException e) {
				LOG.warnf("Call to DoomMake invoke interrupted (%s): %s", targetName, projectDirectory.getAbsolutePath());
				statusPanel.setErrorMessage("Build interrupted!");
			} catch (ExecutionException e) {
				LOG.errorf(e, "Error on DoomMake invoke (%s): %s", targetName, projectDirectory.getAbsolutePath());
				statusPanel.setErrorMessage("ERROR on build!");
			}
		});
	}
	
}

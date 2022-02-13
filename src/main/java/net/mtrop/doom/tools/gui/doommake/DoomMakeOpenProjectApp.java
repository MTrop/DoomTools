package net.mtrop.doom.tools.gui.doommake;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collections;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JMenuBar;

import net.mtrop.doom.tools.common.Common;
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
import net.mtrop.doom.tools.struct.util.IOUtils;

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
    private Action targetRunAction;
    /** Status messages. */
    private StatusPanel statusPanel;

	// Fields
    
    /** Project directory. */
    private final File projectDirectory;
    /** Watcher thread. */
    private WatchThread watchThread;

    // State
    
    /** Current target. */
    private String currentTarget;
    /** Auto build flag. */
    private boolean autoBuild;

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
		this.autoBuildCheckbox = checkBox(language.getText("doommake.project.autobuild"), false, (c, e) -> autoBuild = c.isSelected());
		this.targetRunAction = action(currentTarget, (e) -> runCurrentTarget());
		this.statusPanel = new StatusPanel();
		this.statusPanel.setSuccessMessage("Ready.");

		this.projectDirectory = projectDirectory;
		this.watchThread = null;
		
		this.currentTarget = null;
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
		
		return containerOf(
			node(BorderFactory.createEmptyBorder(4, 4, 4, 4), new BorderLayout(), node(containerOf(
				node(BorderLayout.NORTH, containerOf(
					node(BorderLayout.EAST, control)
				)),
				node(BorderLayout.CENTER, containerOf(new BorderLayout(0, 4),
					node(BorderLayout.CENTER, listPanel),
					node(BorderLayout.SOUTH, containerOf(new BorderLayout(0, 4),
						node(BorderLayout.NORTH, autoBuildCheckbox),
						node(BorderLayout.SOUTH, statusPanel)
					))
				))
			)))
		);
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
	public void onOpen() 
	{
		this.watchThread = new WatchThread(projectDirectory);
		this.watchThread.start();
	}
	
	@Override
	public void onClose() 
	{
		this.watchThread.interrupt();
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
		String absolutePath = projectDirectory.getAbsolutePath();
		try {
			listPanel.refreshTargets(helper.getProjectTargets(projectDirectory));
			LOG.infof("Targets refreshed for %s", absolutePath);
		} catch (FileNotFoundException e) {
			SwingUtils.error(receiver.getApplicationContainer(), language.getText("doommake.project.targets.error.nodirectory", absolutePath));
			LOG.errorf("Project directory does not exist: %s", absolutePath);
		} catch (ProcessCallException e) {
			SwingUtils.error(receiver.getApplicationContainer(), language.getText("doommake.project.targets.error.gettargets", absolutePath));
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
		
		tasks.spawn(() -> {
			
		});
	}
	
	// Project directory watcher thread.
	private class WatchThread extends Thread
	{
		private File directory;
		private WatchService service;
		
		private WatchThread(File directory)
		{
			this.directory = directory;
			this.service = null;
		}
		
		@Override
		public void run() 
		{
			try {
				service = FileSystems.getDefault().newWatchService();
				File[] directories = Common.getSubdirectories(directory, true, (d) -> !d.isHidden());
				for (File d : directories)
				{
					Path dirPath = Paths.get(d.getPath());
					dirPath.register(service, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
				}
			} catch (UnsupportedOperationException e) {
				LOG.warn("Could not start filesystem monitor for auto-build: unsupported by platform.");
				return;
			} catch (IOException e) {
				LOG.warn("Could not start filesystem monitor for auto-build.");
				service = null;
				return;
			}
			
			autoBuildCheckbox.setEnabled(true);
			
			try {
				// Important files.
				
				try {
					WatchKey key;
					while ((key = service.take()) != null)
					{
						for (WatchEvent<?> event : key.pollEvents())
						{
							Path p = ((Path)key.watchable()).resolve(((Path)event.context()));
							LOG.debugf("Received event: %s: %s", event.kind().name(), p.toFile());
						}
						key.reset();
					}
				} catch (ClosedWatchServiceException e) {
					LOG.warnf("Watch Service for %s closed unexpectedly. Terminating.", directory.getAbsolutePath());
				} catch (InterruptedException e) {
					LOG.infof("Watcher thread for %s interrupted. Terminating.", directory.getAbsolutePath());
				}
				
			} finally {
				autoBuildCheckbox.setEnabled(false);
				IOUtils.close(service);
				LOG.infof("Closed Watcher service for %s", directory.getAbsolutePath());
			}
		}
	}
	
	// Auto-build kickoff thread.
	private class AutoBuildKickoffThread extends Thread
	{
		
	}
	
}

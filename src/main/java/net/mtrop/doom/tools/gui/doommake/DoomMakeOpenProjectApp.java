package net.mtrop.doom.tools.gui.doommake;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.WatchService;
import java.util.Collections;

import javax.swing.JMenuBar;

import net.mtrop.doom.tools.gui.DoomToolsApplicationControlReceiver;
import net.mtrop.doom.tools.gui.DoomToolsApplicationInstance;
import net.mtrop.doom.tools.gui.DoomToolsGUIUtils;
import net.mtrop.doom.tools.gui.DoomToolsLanguageManager;
import net.mtrop.doom.tools.gui.doommake.DoomMakeProjectHelper.ProcessCallException;
import net.mtrop.doom.tools.gui.doommake.swing.panels.DoomMakeProjectControlPanel;
import net.mtrop.doom.tools.gui.doommake.swing.panels.DoomMakeProjectTargetListPanel;
import net.mtrop.doom.tools.gui.doommake.swing.panels.DoomMakeSettingsPanel;
import net.mtrop.doom.tools.struct.swing.SwingUtils;

import static net.mtrop.doom.tools.struct.swing.ContainerFactory.*;
import static net.mtrop.doom.tools.struct.swing.ComponentFactory.*;

/**
 * The DoomMake New Project application.
 * @author Matthew Tropiano
 */
public class DoomMakeOpenProjectApp implements DoomToolsApplicationInstance
{
	/** Utils. */
	private DoomToolsGUIUtils utils;
    /** Language manager. */
    private DoomToolsLanguageManager language;
    /** Project helper. */
    private DoomMakeProjectHelper helper;
    /** The app control receiver. */
	private DoomToolsApplicationControlReceiver receiver;

    /** Project directory. */
    private File projectDirectory;
    /** FileSystem monitor. */
    private WatchService watchService;

    /** Current target. */
    private String currentTarget;

    /**
	 * Creates a new open project application from a project directory.
	 * @param projectDirectory the project directory.
	 */
	public DoomMakeOpenProjectApp(File projectDirectory)
	{
		this.utils = DoomToolsGUIUtils.get();
		this.language = DoomToolsLanguageManager.get();
		this.helper = DoomMakeProjectHelper.get();
		this.receiver = null;
		
		this.projectDirectory = projectDirectory;
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
		DoomMakeProjectTargetListPanel targets = new DoomMakeProjectTargetListPanel(
			Collections.emptySortedSet(),
			(target) -> setCurrentTarget(target), 
			(target) -> { 
				setCurrentTarget(target);
				
			}
		); 
		
		return containerOf(new BorderLayout(4, 4),
			node(BorderLayout.EAST, control),
			node(BorderLayout.SOUTH, containerOf(new BorderLayout(), 
				node(BorderLayout.CENTER, targets)
			))
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
		try {
			helper.getProjectTargets(projectDirectory);
		} catch (FileNotFoundException e) {
			// TODO: Directory not found!
			e.printStackTrace();
		} catch (ProcessCallException e) {
			// TODO: Could not call DoomMake!
			e.printStackTrace();
		}
	}
	
	/**
	 * Sets the current target to execute.
	 * @param target the new target.
	 */
	private void setCurrentTarget(String target)
	{
		currentTarget = target;
	}
	
	/**
	 * Runs the current target.
	 */
	private void runCurrentTarget()
	{
		
	}
	
}

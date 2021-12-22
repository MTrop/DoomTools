package net.mtrop.doom.tools.doommake.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;

import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.SingletonProvider;

/**
 * Utility singleton for invoking DoomMake functions for a project.
 * @author Matthew Tropiano
 */
public final class DoomMakeProjectHelper 
{
    /** Logger. */
    private static final Logger LOG = DoomMakeLogger.getLogger(DoomMakeProjectHelper.class); 
    
    /** The instance encapsulator. */
    private static final SingletonProvider<DoomMakeProjectHelper> INSTANCE = new SingletonProvider<>(() -> 
    {
		return new DoomMakeProjectHelper();
    });
    
	/**
	 * @return the singleton instance of this object.
	 */
	public static DoomMakeProjectHelper get()
	{
		return INSTANCE.get();
	}
	
	/* ==================================================================== */
	
	private DoomMakeSettings settings;
	
	private DoomMakeProjectHelper()
	{
		this.settings = DoomMakeSettings.get();
	}

	/**
	 * Opens the project folder shell.
	 * @param projectDirectory the project directory.
	 * @return true.
	 * @throws IllegalArgumentException if the provided file does not exist or is not a directory.
	 * @throws ProcessCallException if the process could not be started.
	 */
	public boolean openShell(File projectDirectory) throws ProcessCallException
	{
		checkProjectDirectory(projectDirectory);
		try {
			checkDesktopOpen().open(projectDirectory);
			return true;
		} catch (SecurityException e) {
			throw new ProcessCallException("Project folder could not be opened. Operating system prevented the call.", e);
		} catch (IOException e) {
			throw new ProcessCallException("Project folder could not be opened.", e);
		}
	}
	
	/**
	 * Opens VSCode for the project folder.
	 * @param projectDirectory the project directory.
	 * @return true.
	 * @throws IllegalArgumentException if the provided file does not exist or is not a directory.
	 * @throws ProcessCallException if the process could not be started.
	 * @throws RequiredSettingException if a missing or bad setting is preventing the call from succeeding.
	 */
	public boolean openVSCode(File projectDirectory) throws ProcessCallException, RequiredSettingException
	{
		checkProjectDirectory(projectDirectory);
		
		File vsCodePath = settings.getPathToVSCode();
		if (vsCodePath == null)
			throw new RequiredSettingException("The path to VSCode is not configured. Cannot open VSCode.");
		if (!vsCodePath.exists())
			throw new RequiredSettingException("The path to VSCode could not be found. Cannot open VSCode.");
		if (vsCodePath.isDirectory())
			throw new RequiredSettingException("The path to VSCode is not a file. Cannot open VSCode.");
		
		try {
			(new ProcessBuilder())
				.command(vsCodePath.getAbsolutePath(), projectDirectory.getAbsolutePath())
				.start();
			return true;
		} catch (SecurityException e) {
			throw new ProcessCallException("VSCode could not be started. Operating system prevented the call.", e);
		} catch (IOException e) {
			throw new ProcessCallException("VSCode could not be started. Operating system prevented the call.", e);
		}
	}
	
	/**
	 * Gets the list of project targets.
	 * @param projectDirectory 
	 * @return the list of project targets.
	 */
	public List<String> getProjectTargets(File projectDirectory)
	{
		throw new UnsupportedOperationException("NOT FINISHED");
	}
	
	private void checkProjectDirectory(File projectDirectory)
	{
		if (!projectDirectory.exists())
			throw new IllegalArgumentException("Provided file does not exist.");
		if (!projectDirectory.isDirectory())
			throw new IllegalArgumentException("Provided file is not a directory.");		
	}

	private Desktop checkDesktopOpen() throws ProcessCallException
	{
		if (!Desktop.isDesktopSupported())
			throw new ProcessCallException("The OS cannot open the project folder: not supported.");
		
		Desktop desktop = Desktop.getDesktop();
		if (!desktop.isSupported(Desktop.Action.OPEN))
			throw new ProcessCallException("The OS cannot open the project folder: \"open\" operation not supported.");
		
		return desktop;
	}

	// TODO: Finish this.
	
	/**
	 * Thrown if a process call fails.
	 */
	public static class ProcessCallException extends Exception
	{
		private static final long serialVersionUID = -4333144551300673788L;

		public ProcessCallException(String message) 
		{
			super(message);
		}

		public ProcessCallException(String message, Throwable cause)
		{
			super(message, cause);
		}
	}
	
	/**
	 * Thrown if a required setting is missing.
	 */
	public static class RequiredSettingException extends Exception
	{
		private static final long serialVersionUID = -5585027536317225616L;

		public RequiredSettingException(String message) 
		{
			super(message);
		}

		public RequiredSettingException(String message, Throwable cause)
		{
			super(message, cause);
		}
	}
	
}

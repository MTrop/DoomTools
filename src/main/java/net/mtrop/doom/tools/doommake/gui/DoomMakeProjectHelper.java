package net.mtrop.doom.tools.doommake.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

import net.mtrop.doom.tools.struct.AsyncFactory.Instance;
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
    private static final SingletonProvider<DoomMakeProjectHelper> INSTANCE = new SingletonProvider<>(() -> new DoomMakeProjectHelper());
    
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
	 * @throws FileNotFoundException if the provided file does not exist or is not a directory.
	 * @throws ProcessCallException if the process could not be started.
	 */
	public boolean openShell(File projectDirectory) throws ProcessCallException, FileNotFoundException
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
	 * Opens the local properties file.
	 * @param projectDirectory the project directory.
	 * @return true.
	 * @throws FileNotFoundException if the provided file does not exist or is not a directory.
	 * @throws ProcessCallException if the process could not be started.
	 */
	public boolean openLocalProperties(File projectDirectory) throws ProcessCallException, FileNotFoundException
	{
		checkProjectDirectory(projectDirectory);
		File propsFile = new File(projectDirectory + File.separator + "doommake.properties");
		try {
			checkDesktopOpen().open(propsFile);
			return true;
		} catch (SecurityException e) {
			throw new ProcessCallException("Properties file could not be opened. Operating system prevented the call.", e);
		} catch (IOException e) {
			throw new ProcessCallException("Properties file could not be opened.", e);
		}
	}
	
	/**
	 * Opens VSCode for the project folder.
	 * @param projectDirectory the project directory.
	 * @return true.
	 * @throws FileNotFoundException if the provided file does not exist or is not a directory.
	 * @throws ProcessCallException if the process could not be started.
	 * @throws RequiredSettingException if a missing or bad setting is preventing the call from succeeding.
	 */
	public boolean openVSCode(File projectDirectory) throws ProcessCallException, RequiredSettingException, FileNotFoundException
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
			throw new ProcessCallException("VSCode could not be started.", e);
		}
	}
	
	/**
	 * Opens SLADE for the project source folder.
	 * The source folder is lifted straight from the local properties.
	 * @param projectDirectory the project directory.
	 * @return true.
	 * @throws FileNotFoundException if the provided file does not exist or is not a directory.
	 * @throws ProcessCallException if the process could not be started.
	 * @throws RequiredSettingException if a missing or bad setting is preventing the call from succeeding.
	 */
	public boolean openSourceFolderInSlade(File projectDirectory) throws ProcessCallException, RequiredSettingException, FileNotFoundException
	{
		checkProjectDirectory(projectDirectory);
		
		File sladePath = settings.getPathToSlade();
		if (sladePath == null)
			throw new RequiredSettingException("The path to SLADE is not configured. Cannot open SLADE.");
		if (!sladePath.exists())
			throw new RequiredSettingException("The path to SLADE could not be found. Cannot open SLADE.");
		if (sladePath.isDirectory())
			throw new RequiredSettingException("The path to SLADE is not a file. Cannot open SLADE.");
		
		Properties props = getProjectProperties(projectDirectory);
		String folder = props.getProperty("doommake.dir.src", "src");
		File sourceDir = new File(projectDirectory + File.separator + folder);
		
		try {
			(new ProcessBuilder())
				.command(sladePath.getAbsolutePath(), sourceDir.getAbsolutePath())
				.start();
			return true;
		} catch (SecurityException e) {
			throw new ProcessCallException("SLADE could not be started. Operating system prevented the call.", e);
		} catch (IOException e) {
			throw new ProcessCallException("SLADE could not be started.", e);
		}
	}
	
	/**
	 * Gets the list of project targets.
	 * Leverages a DoomMake call to fetch the project targets.
	 * @param projectDirectory the project directory.
	 * @return the list of project targets.
	 */
	public List<String> getProjectTargets(File projectDirectory)
	{
		throw new UnsupportedOperationException("NOT FINISHED");
	}
	
	/**
	 * Gets the list of project targets.
	 * @param projectDirectory the project directory.
	 * @param stdout the standard out stream. 
	 * @param stderr the standard error stream. 
	 * @param stdin the standard in stream.
	 * @param targetname the target name.
	 * @return the list of project targets.
	 */
	public Instance<Integer> callDoomMakeTarget(File projectDirectory, PrintStream stdout, PrintStream stderr, InputStream stdin, String targetname)
	{
		throw new UnsupportedOperationException("NOT FINISHED");
	}
	
	// TODO: Finish this.
	
	private Properties getProjectProperties(File projectDirectory)
	{
		Properties properties = new Properties();
		File projectPropertiesFile = new File(projectDirectory + File.separator + "doommake.project.properties");
		File propertiesFile = new File(projectDirectory + File.separator + "doommake.properties");
		mergeProperties(properties, projectPropertiesFile);
		mergeProperties(properties, propertiesFile);
		return properties;
	}

	private void mergeProperties(Properties properties, File projectPropertiesFile) 
	{
		if (projectPropertiesFile.exists()) try (FileInputStream fis = new FileInputStream(projectPropertiesFile)) 
		{
			properties.load(fis);
		} 
		catch (Exception e) 
		{
			LOG.error(e, "Could not open properties file: " + projectPropertiesFile.getAbsolutePath());
		}
	}

	private void checkProjectDirectory(File projectDirectory) throws FileNotFoundException
	{
		if (!projectDirectory.exists())
			throw new FileNotFoundException("Project directory does not exist: " + projectDirectory.getAbsolutePath());
		if (!projectDirectory.isDirectory())
			throw new FileNotFoundException("Provided file is not a directory: " + projectDirectory.getAbsolutePath());		
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

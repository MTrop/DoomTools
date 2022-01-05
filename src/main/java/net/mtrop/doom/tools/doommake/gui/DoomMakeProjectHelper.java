package net.mtrop.doom.tools.doommake.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.SingletonProvider;
import net.mtrop.doom.tools.struct.util.IOUtils;
import net.mtrop.doom.tools.struct.util.OSUtils;

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
	 * Opens the project folder GUI shell (Explorer in Windows, Finder on macOS, etc.).
	 * @param projectDirectory the project directory.
	 * @return true.
	 * @throws FileNotFoundException if the provided file does not exist or is not a directory.
	 * @throws ProcessCallException if the process could not be started.
	 */
	public boolean openExplorer(File projectDirectory) throws ProcessCallException, FileNotFoundException
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
	 * @throws FileNotFoundException 
	 * @throws ProcessCallException 
	 */
	public SortedSet<String> getProjectTargets(File projectDirectory) throws FileNotFoundException, ProcessCallException
	{
		checkProjectDirectory(projectDirectory);
		checkDoomMake();
		
		List<String> cmdLine = createDoomMakeCmdLine();
		cmdLine.add("--targets");
		
		StringWriter sw = new StringWriter();
		StringWriter errorsw = new StringWriter();
		int result;
		
		try {
			result = IOUtils.ProcessWrapper.create(Runtime.getRuntime().exec(cmdLine.toArray(new String[cmdLine.size()]), null, projectDirectory))
				.stdout(sw)
				.stderr(errorsw)
			.waitFor();
			LOG.infof("Call to DoomMake returned %d", result);
		} catch (InterruptedException e) {
			throw new ProcessCallException("DoomMake target could not be completed. Process thread was interrupted.", e);
		} catch (SecurityException e) {
			throw new ProcessCallException("DoomMake target could not be completed. Operating system prevented the call.", e);
		} catch (IOException e) {
			throw new ProcessCallException("DoomMake target could not be completed.", e);
		}
		
		if (result != 0)
			return Collections.emptySortedSet();
		
		SortedSet<String> out = new TreeSet<>((a, b) -> a.equalsIgnoreCase("make") ? -1 : a.compareTo(b));
		for (String entryName : sw.toString().split("\\n+"))
			out.add(entryName.trim());
		return out;
	}
	
	/**
	 * Calls a DoomMake project target.
	 * @param projectDirectory the project directory.
	 * @param stdout the standard out stream. 
	 * @param stderr the standard error stream. 
	 * @param stdin the standard in stream.
	 * @param targetName the target name.
	 * @return the list of project targets.
	 * @throws FileNotFoundException 
	 * @throws ProcessCallException 
	 */
	public IOUtils.ProcessWrapper callDoomMakeTarget(File projectDirectory, PrintStream stdout, PrintStream stderr, InputStream stdin, String targetName) throws FileNotFoundException, ProcessCallException
	{
		checkProjectDirectory(projectDirectory);
		checkDoomMake();
		
		List<String> cmdLine = createDoomMakeCmdLine();
		cmdLine.add(targetName);
		
		try {
			LOG.infof("Calling DoomMake.");
			return IOUtils.ProcessWrapper.create(Runtime.getRuntime().exec(cmdLine.toArray(new String[cmdLine.size()]), null, projectDirectory))
				.stdout(stdout)
				.stderr(stderr)
				.stdin(stdin);
		} catch (SecurityException e) {
			throw new ProcessCallException("DoomMake target could not be completed. Operating system prevented the call.", e);
		} catch (IOException e) {
			throw new ProcessCallException("DoomMake target could not be completed.", e);
		}
	}

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

	private void checkDoomMake() throws ProcessCallException
	{
		if (!OSUtils.onPath("doommake"))
		{
			throw new ProcessCallException(
				"DoomMake was not found on your PATH. " +
				"DoomTools may not have been installed properly, or this is being called from an embedded instance."
			);
		}
	}

	private List<String> createDoomMakeCmdLine() 
	{
		List<String> cmdLine = new LinkedList<>();
		// If Windows, call DoomMake from CMD.
		if (OSUtils.isWindows())
		{
			cmdLine.add("cmd");
			cmdLine.add("/c");
		}
		cmdLine.add("doommake");
		return cmdLine;
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

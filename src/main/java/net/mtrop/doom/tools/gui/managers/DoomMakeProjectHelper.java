package net.mtrop.doom.tools.gui.managers;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import net.mtrop.doom.tools.struct.InstancedFuture;
import net.mtrop.doom.tools.struct.LoggingFactory.Logger;
import net.mtrop.doom.tools.struct.ProcessCallable;
import net.mtrop.doom.tools.DoomMakeMain;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.struct.SingletonProvider;
import net.mtrop.doom.tools.struct.util.OSUtils;

/**
 * Utility singleton for invoking DoomMake functions for a project.
 * @author Matthew Tropiano
 */
public final class DoomMakeProjectHelper 
{
    /** Logger. */
    private static final Logger LOG = DoomToolsLogger.getLogger(DoomMakeProjectHelper.class); 
    
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
	
	private DoomToolsSettingsManager settings;
	
	private DoomMakeProjectHelper()
	{
		this.settings = DoomToolsSettingsManager.get();
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
	 * Gets a file path for a project's path that is in 
	 * @param projectDirectory the project directory.
	 * @param property the property name.
	 * @param defaultValue the default value, if not found.
	 * @return the project path.
	 */
	public File getProjectPropertyPath(File projectDirectory, String property, String defaultValue)
	{
		Properties props = getProjectProperties(projectDirectory);
		String path = props.getProperty(property);
		if (Common.isEmpty(path))
			path = defaultValue;
		return new File(projectDirectory + File.separator + path);
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
		
		File sourceDir = getProjectPropertyPath(projectDirectory, "doommake.dir.src", "src");
		
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
		
		StringWriter sw = new StringWriter();
		StringWriter errorsw = new StringWriter();
		int result;
		
		try {
			result = Common.spawnJava(DoomMakeMain.class)
				.setWorkingDirectory(projectDirectory)
				.arg("--targets")
				.setOut(sw)
				.setErr(errorsw)
			.call();
			LOG.infof("Call to DoomMake returned %d", result);
		} catch (Exception e) {
			throw new ProcessCallException("DoomMake call could not be completed.", e);
		}
		
		if (result != 0)
			throw new ProcessCallException("DoomMake targets could not be retrieved.");
		
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
	 * @param targetName the target name.
	 * @param agentOverride if true, bypasses agent detection.
	 * @return the list of project targets.
	 * @throws FileNotFoundException 
	 * @throws ProcessCallException 
	 */
	public InstancedFuture<Integer> callDoomMakeTarget(File projectDirectory, PrintStream stdout, PrintStream stderr, String targetName, boolean agentOverride) throws FileNotFoundException, ProcessCallException
	{
		checkProjectDirectory(projectDirectory);
		checkDoomMake();
		
		ProcessCallable callable = Common.spawnJava(DoomMakeMain.class).setWorkingDirectory(projectDirectory);
		if (agentOverride)
			callable.arg(DoomMakeMain.SWITCH_AGENT_BYPASS);
		
		callable.arg(targetName)
			.setOut(stdout)
			.setErr(stderr)
			.setOutListener((exception) -> LOG.errorf(exception, "Exception occurred on DoomMake STDOUT."))
			.setErrListener((exception) -> LOG.errorf(exception, "Exception occurred on DoomMake STDERR."));
		
		LOG.infof("Calling DoomMake (%s).", targetName);
		return InstancedFuture.instance(callable).spawn();
	}

	private static Properties getProjectProperties(File projectDirectory)
	{
		Properties properties = new Properties();
		File projectPropertiesFile = new File(projectDirectory + File.separator + "doommake.project.properties");
		File propertiesFile = new File(projectDirectory + File.separator + "doommake.properties");
		mergeProperties(properties, projectPropertiesFile);
		mergeProperties(properties, propertiesFile);
		return properties;
	}

	private static void mergeProperties(Properties properties, File projectPropertiesFile) 
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

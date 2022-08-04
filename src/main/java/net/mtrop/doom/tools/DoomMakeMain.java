/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;

import com.blackrook.json.JSONConversionException;
import com.blackrook.json.JSONObject;
import com.blackrook.json.JSONReader;
import com.blackrook.json.JSONWriter;

import net.mtrop.doom.tools.WadScriptMain.Mode;
import net.mtrop.doom.tools.WadScriptMain.Resolver;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.doommake.AutoBuildAgent;
import net.mtrop.doom.tools.doommake.ProjectGenerator;
import net.mtrop.doom.tools.doommake.ProjectModule;
import net.mtrop.doom.tools.doommake.ProjectTemplate;
import net.mtrop.doom.tools.doommake.ProjectTokenReplacer;
import net.mtrop.doom.tools.doommake.functions.DoomMakeFunctions;
import net.mtrop.doom.tools.doommake.functions.ToolInvocationFunctions;
import net.mtrop.doom.tools.doommake.generators.TextureProjectGenerator;
import net.mtrop.doom.tools.doommake.generators.WADProjectGenerator;
import net.mtrop.doom.tools.exception.OptionParseException;
import net.mtrop.doom.tools.exception.UtilityException;
import net.mtrop.doom.tools.gui.DoomToolsGUIMain;
import net.mtrop.doom.tools.gui.DoomToolsGUIMain.ApplicationNames;
import net.mtrop.doom.tools.struct.InstancedFuture;
import net.mtrop.doom.tools.struct.util.EnumUtils;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.IOUtils;
import net.mtrop.doom.tools.struct.util.OSUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;
import net.mtrop.doom.tools.struct.util.StringUtils;

/**
 * Main class for DoomMake.
 * @author Matthew Tropiano
 */
public final class DoomMakeMain 
{
	private static final int ERROR_NONE = 0;
	private static final int ERROR_BAD_OPTIONS = 1;
	private static final int ERROR_BAD_PROPERTIES = 2;
	private static final int ERROR_BAD_SCRIPT = 3;
	private static final int ERROR_BAD_PROJECT = 4;
	private static final int ERROR_SECURITY = 5;
	private static final int ERROR_BAD_TOOL_PATH = 6;
	private static final int ERROR_IOERROR = 7;
	private static final int ERROR_AGENT_RUNNING = 8;
	private static final int ERROR_UNKNOWN = -1;

	public static final String JSON_AGENT_LOCK_KEY = "agentIsRunning";
	
	public static final String SWITCH_HELP = "--help";
	public static final String SWITCH_HELP2 = "-h";
	public static final String SWITCH_VERSION = "--version";
	public static final String SWITCH_CHANGELOG = "--changelog";
	public static final String SWITCH_FUNCHELP1 = "--function-help";
	public static final String SWITCH_FUNCHELP2 = "--function-help-markdown";
	public static final String SWITCH_FUNCHELP3 = "--function-help-html";
	public static final String SWITCH_FUNCHELP4 = "--function-help-html-div";
	public static final String SWITCH_TARGETS = "--targets";
	
	public static final String SWITCH_PROJECTTYPE = "--project-type";
	public static final String SWITCH_LISTTEMPLATES = "--list-templates";
	public static final String SWITCH_LISTTEMPLATES2 = "-t";
	public static final String SWITCH_NEWPROJECT = "--new-project";
	public static final String SWITCH_NEWPROJECT2 = "-n";
	
	public static final String SWITCH_NEWPROJECT_GUI = "--new-project-gui";

	public static final String SWITCH_EMBED = "--embed";
	public static final String SWITCH_GUI = "--gui";
	public static final String SWITCH_STUDIO = "--studio";
	public static final String SWITCH_AGENT = "--auto-build";
	public static final String SWITCH_AGENT_VERBOSE = "--auto-build-verbose";
	public static final String SWITCH_AGENT_BYPASS = "--agent-bypass";
	
	public static final String SWITCH_SCRIPTFILE = "--script";
	public static final String SWITCH_SCRIPTFILE2 = "-s";
	public static final String SWITCH_PROPERTYFILE = "--properties";
	public static final String SWITCH_PROPERTYFILE2 = "-p";
	public static final String SWITCH_RUNAWAYLIMIT1 = "--runaway-limit";
	public static final String SWITCH_ACTIVATIONDEPTH1 = "--activation-depth";
	public static final String SWITCH_STACKDEPTH1 = "--stack-depth";
	public static final String SWITCH_DISASSEMBLE1 = "--disassemble";

	private static final String SHELL_OPTIONS = "-Xms64M -Xmx768M";
	private static final String SHELL_RESOURCE_CMD = "shell/embed/app-name.cmd";
	private static final String SHELL_RESOURCE_SH = "shell/embed/app-name.sh";
	
	private static final String PROPERTY_DOOMMAKE_PROJECT_ENCODING = "doommake.project.encoding";
	
	// WadScript-specific
	private static final Resolver[] RESOLVERS_DOOMMAKE = 
	{
		new Resolver("DoomMake Functions", DoomMakeFunctions.createResolver()),
		new Resolver("Tool Invocation", "TOOL", ToolInvocationFunctions.createResolver())
	};

	/**
	 * Project types.
	 */
	public enum ProjectType
	{
		WAD(WADProjectGenerator.class),
		TEXTURE(TextureProjectGenerator.class),
		;
		
		public static final Map<String, ProjectType> TYPES = EnumUtils.createCaseInsensitiveNameMap(ProjectType.class);
		
		final Class<? extends ProjectGenerator> generatorClass; 
		private ProjectType(Class<? extends ProjectGenerator> generatorClass)
		{
			this.generatorClass = generatorClass;
		}
		
		public ProjectGenerator createGenerator()
		{
			return Common.create(generatorClass);
		}
	}
	
	/**
	 * Program options.
	 */
	public static class Options
	{
		private PrintStream stdout;
		private PrintStream stderr;
		private InputStream stdin;
		private boolean help;
		private boolean version;
		private boolean listModules;
		private boolean embed;
		private boolean changelog;
		private boolean gui;
		private boolean guiStudio;
		private boolean guiNewProject;
		private boolean agent;
		private boolean verboseAgent;

		private boolean agentBypass;
		private ProjectType projectType;
		private List<String> templateNames;
		
		private Mode mode;
		private Integer runawayLimit;
		private Integer activationDepth;
		private Integer stackDepth;

		private String targetName;

		private File propertiesFile;
		private File scriptFile;
		private List<String> args;
		
		private Options()
		{
			this.stdout = null;
			this.stderr = null;
			this.stdin = null;
			this.help = false;
			this.version = false;
			this.listModules = false;
			this.embed = false;
			this.changelog = false;
			this.gui = false;
			this.guiStudio = false;
			this.guiNewProject = false;
			this.agent = false;
			this.verboseAgent = false;
			
			this.agentBypass = false;
			this.projectType = ProjectType.WAD;
			this.templateNames = null;

			this.mode = Mode.EXECUTE;
			this.runawayLimit = 0;
			this.activationDepth = 256;
			this.stackDepth = 2048;
			this.propertiesFile = new File("doommake.properties");
			this.scriptFile = new File("doommake.script");
			this.targetName = "make";
			this.args = new LinkedList<>();
		}
		
		public Options setStdout(OutputStream out) 
		{
			this.stdout = new PrintStream(out, true);;
			return this;
		}
		
		public Options setStderr(OutputStream err) 
		{
			this.stderr = new PrintStream(err, true);
			return this;
		}
		
		public Options setStdin(InputStream stdin) 
		{
			this.stdin = stdin;
			return this;
		}

		public Options setRunawayLimit(Integer runawayLimit)
		{
			this.runawayLimit = runawayLimit;
			return this;
		}
		
		public Options setActivationDepth(Integer activationDepth)
		{
			this.activationDepth = activationDepth;
			return this;
		}
		
		public Options setStackDepth(Integer stackDepth)
		{
			this.stackDepth = stackDepth;
			return this;
		}

		public Options setTargetName(String targetName) 
		{
			this.targetName = targetName;
			return this;
		}
		
		public Options setPropertiesFile(File propertiesFile) 
		{
			this.propertiesFile = propertiesFile;
			return this;
		}
		
		public Options setScriptFile(File scriptFile) 
		{
			this.scriptFile = scriptFile;
			return this;
		}
		
		public Options setArgs(String[] args)
		{
			this.args.clear();
			for (String s : args)
				this.args.add(s);
			return this;
		}
		
		public Options setProjectType(ProjectType projectType) 
		{
			this.projectType = projectType;
			return this;
		}
		
		public Options setNewProjectTemplateNames(String[] templateNames)
		{
			this.templateNames = Arrays.asList(templateNames);
			return this;
		}

		public Options addArg(String arg)
		{
			this.args.add(arg);
			return this;
		}
		
	}
	
	/**
	 * Program context.
	 */
	private static class Context implements Callable<Integer>
	{
		private Options options;
	
		private Context(Options options)
		{
			this.options = options;
		}
		
		@Override
		public Integer call()
		{
			if (options.gui)
			{
				try {
					DoomToolsGUIMain.startGUIAppProcess(ApplicationNames.DOOMMAKE_OPEN, OSUtils.getWorkingDirectoryPath());
				} catch (IOException e) {
					options.stderr.println("ERROR: Could not start DoomMake GUI!");
					return ERROR_IOERROR;
				}
				return ERROR_NONE;
			}
			
			if (options.guiStudio)
			{
				try {
					DoomToolsGUIMain.startGUIAppProcess(ApplicationNames.DOOMMAKE_STUDIO, OSUtils.getWorkingDirectoryPath());
				} catch (IOException e) {
					options.stderr.println("ERROR: Could not start DoomMake Studio!");
					return ERROR_IOERROR;
				}
				return ERROR_NONE;
			}
			
			if (options.guiNewProject)
			{
				try {
					DoomToolsGUIMain.startGUIAppProcess(ApplicationNames.DOOMMAKE_NEW, OSUtils.getWorkingDirectoryPath());
				} catch (IOException e) {
					options.stderr.println("ERROR: Could not start DoomMake New Project GUI!");
					return ERROR_IOERROR;
				}
				return ERROR_NONE;
			}
			
			if (options.help)
			{
				splash(options.stdout);
				usage(options.stdout);
				options.stdout.println();
				help(options.stdout);
				return ERROR_NONE;
			}
			
			if (options.version)
			{
				splash(options.stdout);
				return ERROR_NONE;
			}

			if (options.changelog)
			{
				changelog(options.stdout, "doommake");
				return ERROR_NONE;
			}
			
			if (options.projectType != null)
			{
				ProjectGenerator generator = options.projectType.createGenerator();
				
				if (options.listModules)
				{
					Set<String> templateNames = generator.getTemplateNames();
					TreeMap<String, TreeSet<ProjectTemplate>> categoryList = new TreeMap<>(); 
					for (String name : templateNames)
					{
						ProjectTemplate template = generator.getTemplate(name);
						
						String category = template.getCategory();
						TreeSet<ProjectTemplate> set;
						if ((set = categoryList.get(category)) == null)
							categoryList.put(category, (set = new TreeSet<ProjectTemplate>()));
						set.add(template);
					}
					
					options.stdout.println("List of templates by category:");
					for (Entry<String, TreeSet<ProjectTemplate>> categoryEntry : categoryList.entrySet())
					{
						options.stdout.println(categoryEntry.getKey() + ":");
						for (ProjectTemplate template : categoryEntry.getValue())
						{
							if (template.isHidden())
								continue;
							
							String description = template.getDescription();
							options.stdout.println("    " + template.getName());
							StringUtils.printWrapped(options.stdout, ObjectUtils.isEmpty(description) ? "" : description, 0, 8, 72);
							options.stdout.println();
						}
						options.stdout.println();
					}
					return ERROR_NONE;
				}
				else if (options.templateNames != null)
				{
					return createProject(generator);
				}
			}

			// Embed DoomMake.
			if (options.embed)
			{
				if (!options.scriptFile.exists())
				{
					options.stderr.printf("ERROR: Script file \"%s\" could not be found!\n", options.scriptFile.getPath());
					return ERROR_BAD_SCRIPT;
				}
				
				return embedDoomMake();
			}

			// Detect project.
			if (options.mode == Mode.EXECUTE && !options.scriptFile.exists())
			{
				options.stderr.printf("ERROR: Script file \"%s\" could not be found!\n", options.scriptFile.getPath());
				return ERROR_BAD_SCRIPT;
			}
			
			loadProperties(new File("doommake.project.properties"));
			loadProperties(options.propertiesFile);

			boolean agentRunning;
			try {
				agentRunning = isAgentRunning();
			} catch (Exception e) {
				options.stderr.println("ERROR: Can't read lock file: " + e.getClass().getSimpleName() + ": " + e.getLocalizedMessage());
				return ERROR_IOERROR;
			}

			// Is agent running?
			if (agentRunning)
			{
				if (options.agent || !options.agentBypass)
				{
					agentMessage(options.stderr, getLockFile(new File("."), System.getProperties()));
					return ERROR_AGENT_RUNNING;
				}
			}
			
			if (options.agent)
				return startAgent();
			else
				return executeTarget();
		}

		private int startAgent()
		{
			File workDir = new File(".");
			
			final AutoBuildAgent agent = new AutoBuildAgent(workDir, new AutoBuildAgent.Listener() 
			{
				@Override
				public int callBuild(String target) 
				{
					try {
						return InstancedFuture.instance(Common.spawnJava(DoomMakeMain.class)
							.arg(SWITCH_AGENT_BYPASS)
							.setOut(options.stdout)
							.setErr(options.stderr)
						).spawn().result();
					} catch (Throwable t) {
						options.stderr.println("ERROR: " + t.getClass().getSimpleName() + ": " + t.getLocalizedMessage());
						return ERROR_UNKNOWN;
					}
				}
				
				@Override
				public void onBuildPrepared() 
				{
					options.stdout.println("Change detected...");
				}

				@Override
				public void onBuildStart() 
				{
					options.stdout.println("***** Calling DoomMake");
				}
				
				@Override
				public void onBuildEnd(int result) 
				{
					options.stdout.println("***** Build Ended: " + (result == 0 ? "Success" : "FAILED"));
				}
				
				@Override
				public void onAgentStarted() 
				{
					options.stdout.println("Agent started.");
					options.stdout.println("Send SIGTERM/SIGINT to this process to stop it safely (CTRL-C).");
				}

				@Override
				public void onAgentStopped() 
				{
					options.stdout.println("Agent stopped.");
				}

				@Override
				public void onFileCreate(File file) 
				{
					if (options.verboseAgent)
						options.stdout.println("File created: " + file.getPath());
				}

				@Override
				public void onFileModify(File file) 
				{
					if (options.verboseAgent)
						options.stdout.println("File modified: " + file.getPath());
				}
				
				@Override
				public void onFileDelete(File file) 
				{
					if (options.verboseAgent)
						options.stdout.println("File deleted: " + file.getPath());
				}

				@Override
				public void onErrorMessage(Throwable t, String message) 
				{
					options.stderr.println("ERROR: " + message);
					if (t != null)
						options.stderr.println(t.getClass().getSimpleName() + ": " + t.getLocalizedMessage());
				}
				
				@Override
				public void onInfoMessage(String message) 
				{
					options.stdout.println("INFO: " + message);
				}
				
				@Override
				public void onWarningMessage(String message) 
				{
					options.stdout.println("WARN: " + message);
				}
				
				@Override
				public void onVerboseMessage(String message) 
				{
					if (options.verboseAgent)
						options.stdout.println(message);
				}
			});

			agent.start();

			// Handle SIGTERM/SIGINT.
			Runtime.getRuntime().addShutdownHook(new Thread(() -> 
			{
				options.stdout.println("Signal received. Shutting down...");
				agent.shutDown();
			}));
			
			// Wait for SIGTERM/SIGINT.
			Object waitLock = new Object();
			synchronized (waitLock) 
			{
				try {
					waitLock.wait();
				} catch (InterruptedException e) {
					options.stderr.println("WAIT LOCK INTERRUPTED.");
				}
			}
			
			return ERROR_NONE;
		}
		
		private int executeTarget() 
		{
			String encodingName = System.getProperty(PROPERTY_DOOMMAKE_PROJECT_ENCODING);
			try {
				encodingName = (ObjectUtils.isEmpty(encodingName) ? Charset.defaultCharset() : Charset.forName(encodingName)).displayName();
			} catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
				encodingName = Charset.defaultCharset().displayName();
			}
			
			try {
				WadScriptMain.Options wsOptions = WadScriptMain.options(options.stdout, options.stderr, options.stdin)
					.setMode(options.mode)
					.setDocsTitle("DoomMake Functions")
					.setEntryPointName(options.targetName)
					.setStackDepth(options.stackDepth)
					.setActivationDepth(options.activationDepth)
					.setRunawayLimit(options.runawayLimit)
					.setScriptFile(options.scriptFile)
					.setScriptCharsetName(encodingName)
					.addResolver("DoomMake Functions", DoomMakeFunctions.createResolver())
					.addResolver("Tool Invocation", "TOOL", ToolInvocationFunctions.createResolver())
				;
				for (Object obj : options.args)
					wsOptions.addArg(obj);
				return WadScriptMain.call(wsOptions);
			} catch (OptionParseException e) {
				/** Will not be thrown. */
				return ERROR_UNKNOWN;
			}
		}

		private boolean isAgentRunning() throws IOException
		{
			File workingDirectory = new File(".");
			JSONObject lock = readLockObject(workingDirectory, System.getProperties());
			return lock.get(JSON_AGENT_LOCK_KEY).getBoolean();
		}
		
		private int createProject(ProjectGenerator generator)
		{
			if (ObjectUtils.isEmpty(options.targetName))
			{
				options.stderr.println("ERROR: No directory path.");
				return ERROR_BAD_PROJECT;
			}
			
			File targetDirectory = new File(options.targetName);
			if (targetDirectory.exists() && targetDirectory.listFiles().length > 0)
			{
				options.stderr.println("ERROR: Target directory contains files. Project creation aborted.");
				return ERROR_BAD_PROJECT;
			}

			try {
				SortedSet<ProjectModule> selectedModules = generator.getSelectedModules(options.templateNames);
				List<ProjectTokenReplacer> projectReplacers = ProjectGenerator.getReplacers(selectedModules);
				Map<String, String> replacerMap = ProjectGenerator.consoleReplacer(projectReplacers, options.stdout, options.stdin);

				options.stdout.println("Creating...");
				generator.createProject(selectedModules, replacerMap, targetDirectory);
				options.stdout.println("SUCCESSFULLY Created project: " + options.targetName);
				options.stdout.println();
				
				File todoPath = new File(targetDirectory.getPath() + File.separator + "TODO.md");
				List<String> todoList = ProjectGenerator.getTODOs(selectedModules);
				
				if (!todoList.isEmpty())
				{
					options.stdout.println("You should also probably do the following:");
					try (PrintStream todoPrinter = new PrintStream(new FileOutputStream(todoPath)))
					{
						todoPrinter.println("# Stuff To Do\n");
						int i = 1;
						for (String t : todoList)
						{
							options.stdout.println(i + ") " + t);
							todoPrinter.println(i + ") " + t);
							i++;
						}
					}
				}
			} catch (IOException e) {
				options.stderr.println("ERROR: Project creation error: " + e.getLocalizedMessage());
				return ERROR_BAD_PROJECT;
			} catch (UtilityException e) {
				options.stderr.println("ERROR: " + e.getLocalizedMessage());
				return ERROR_BAD_PROJECT;
			}
			
			options.stdout.println("Project created.");
			return ERROR_NONE;
		}

		// Embeds DoomMake into the project.
		private int embedDoomMake()
		{
			String path;
			String jarName;
			try {
				path = Environment.getDoomToolsPath();
				jarName = Environment.getDoomToolsJarPath();
			} catch (SecurityException e) {
				options.stderr.println("ERROR: Could not fetch ENVVAR value.");
				return ERROR_SECURITY;
			}
			
			if (ObjectUtils.isEmpty(path))
			{
				options.stderr.println("ERROR: DOOMTOOLS_PATH ENVVAR not set. Not invoked via shell?");
				return ERROR_BAD_TOOL_PATH;
			}
			
			if (ObjectUtils.isEmpty(jarName))
			{
				options.stderr.println("ERROR: DOOMTOOLS_JAR ENVVAR not set. Not invoked via shell?");
				return ERROR_BAD_TOOL_PATH;
			}
			
			if (path.charAt(path.length() - 1) != File.separatorChar)
				path = path + File.separatorChar;
			
			File targetJARFile = new File("tools/" + (new File(jarName)).getName());
			File targetShellCmdFile = new File("doommake.cmd");
			File targetShellBashFile = new File("doommake.sh");
			
			try 
			{
				if (!FileUtils.createPathForFile(targetJARFile))
				{
					options.stderr.println("ERROR: Could not create tools directory for embed.");
					return ERROR_IOERROR;
				}
			} 
			catch (SecurityException e)
			{
				options.stderr.println("ERROR: Could not create tools directory for embed. Access denied.");
				return ERROR_SECURITY;
			}
			
			try (FileInputStream fis = new FileInputStream(new File(path + jarName)); FileOutputStream fos = new FileOutputStream(targetJARFile))
			{
				IOUtils.relay(fis, fos);
				Common.copyShellScript(SHELL_RESOURCE_CMD, DoomMakeMain.class, SHELL_OPTIONS, targetJARFile.getPath().replace("/", "\\"), "java", targetShellCmdFile);
				Common.copyShellScript(SHELL_RESOURCE_SH, DoomMakeMain.class, SHELL_OPTIONS, targetJARFile.getPath().replace("\\", "/"), "java", targetShellBashFile);
			} 
			catch (SecurityException e)
			{
				options.stderr.println("ERROR: Could not copy DoomTools binaries. Access denied.");
				return ERROR_SECURITY;
			}
			catch (FileNotFoundException e) 
			{
				options.stderr.println("ERROR: Could not copy DoomTools binaries. " + e.getLocalizedMessage());
				return ERROR_IOERROR;
			} 
			catch (IOException e) 
			{
				options.stderr.println("ERROR: Could not copy DoomTools binaries. " + e.getLocalizedMessage());
				return ERROR_IOERROR;
			}

			// Will always fail on Windows, but not super important for local Windows use.
			targetShellBashFile.setExecutable(true, false);
			
			options.stdout.println("DoomMake embed complete.");
			
			return ERROR_NONE;
		}
		
		// Load a properties file into System.properties.
		private int loadProperties(File propertiesFile) 
		{
			if (propertiesFile.exists())
			{
				try (Reader reader = new InputStreamReader(new FileInputStream(propertiesFile)))
				{
					System.getProperties().load(reader);
				} 
				catch (IOException e) 
				{
					options.stderr.printf("ERROR: Properties file \"%s\" could not be loaded: %s\n", propertiesFile.getPath(), e.getLocalizedMessage());
					return ERROR_BAD_PROPERTIES;
				}
			}
			return ERROR_NONE;
		}
		
	}
	
	/**
	 * Gets all known host function resolvers specifically for WadScript.
	 * @return an array of all of the resolvers.
	 */
	public static Resolver[] getAllDoomMakeResolvers()
	{
		return RESOLVERS_DOOMMAKE;
	}
	
	/**
	 * Reads command line arguments and sets options.
	 * @param out the standard output print stream.
	 * @param err the standard error print stream.
	 * @param in the standard in print stream. 
	 * @param args the argument args.
	 * @return the parsed options.
	 * @throws OptionParseException if a parse exception occurs.
	 */
	public static Options options(PrintStream out, PrintStream err, InputStream in, String ... args) throws OptionParseException
	{
		Options options = new Options();
		options.stdout = out;
		options.stderr = err;
		options.stdin = in;
	
		final int STATE_START = 0;
		final int STATE_SCRIPTFILE = 1;
		final int STATE_PROPERTYFILE = 2;
		final int STATE_SWITCHES_ACTIVATION = 3;
		final int STATE_SWITCHES_STACK = 4;
		final int STATE_SWITCHES_RUNAWAY = 5;
		final int STATE_MODULENAME = 6;
		final int STATE_PROJECTTYPE = 7;
		int state = STATE_START;
		
		boolean target = false;
		
		int i = 0;
		while (i < args.length)
		{
			String arg = args[i];
			switch (state)
			{
				case STATE_START:
				{
					if (arg.equalsIgnoreCase(SWITCH_HELP) || arg.equalsIgnoreCase(SWITCH_HELP2))
						options.help = true;
					else if (arg.equalsIgnoreCase(SWITCH_VERSION))
						options.version = true;
					else if (arg.equalsIgnoreCase(SWITCH_EMBED))
						options.embed = true;
					else if (arg.equalsIgnoreCase(SWITCH_AGENT))
						options.agent = true;
					else if (arg.equalsIgnoreCase(SWITCH_AGENT_VERBOSE))
					{
						options.agent = true;
						options.verboseAgent = true;
					}
					else if (arg.equalsIgnoreCase(SWITCH_AGENT_BYPASS))
						options.agentBypass = true;
					else if (arg.equalsIgnoreCase(SWITCH_GUI))
						options.gui = true;
					else if (arg.equalsIgnoreCase(SWITCH_CHANGELOG))
						options.changelog = true;
					else if (arg.equalsIgnoreCase(SWITCH_STUDIO))
						options.guiStudio = true;
					else if (arg.equalsIgnoreCase(SWITCH_NEWPROJECT_GUI))
						options.guiNewProject = true;
					else if (arg.equalsIgnoreCase(SWITCH_PROJECTTYPE))
					{
						state = STATE_PROJECTTYPE;
					}
					else if (arg.equalsIgnoreCase(SWITCH_LISTTEMPLATES) || arg.equalsIgnoreCase(SWITCH_LISTTEMPLATES2))
					{
						options.listModules = true;
					}
					else if (arg.equalsIgnoreCase(SWITCH_NEWPROJECT) || arg.equalsIgnoreCase(SWITCH_NEWPROJECT2))
					{
						options.templateNames = new LinkedList<>();
						state = STATE_MODULENAME;
					}
					else if (SWITCH_TARGETS.equalsIgnoreCase(arg))
						options.mode = Mode.ENTRYPOINTS;
					else if (SWITCH_FUNCHELP1.equalsIgnoreCase(arg))
						options.mode = Mode.FUNCTIONHELP;
					else if (SWITCH_FUNCHELP2.equalsIgnoreCase(arg))
						options.mode = Mode.FUNCTIONHELP_MARKDOWN;
					else if (SWITCH_FUNCHELP3.equalsIgnoreCase(arg))
						options.mode = Mode.FUNCTIONHELP_HTML;
					else if (SWITCH_FUNCHELP4.equalsIgnoreCase(arg))
						options.mode = Mode.FUNCTIONHELP_HTML_DIV;
					else if (SWITCH_DISASSEMBLE1.equalsIgnoreCase(arg))
						options.mode = Mode.DISASSEMBLE;
					else if (SWITCH_SCRIPTFILE.equalsIgnoreCase(arg) || SWITCH_SCRIPTFILE2.equalsIgnoreCase(arg))
						state = STATE_SCRIPTFILE;
					else if (SWITCH_PROPERTYFILE.equalsIgnoreCase(arg) || SWITCH_PROPERTYFILE2.equalsIgnoreCase(arg))
						state = STATE_PROPERTYFILE;
					else if (SWITCH_RUNAWAYLIMIT1.equalsIgnoreCase(arg))
						state = STATE_SWITCHES_RUNAWAY;
					else if (SWITCH_ACTIVATIONDEPTH1.equalsIgnoreCase(arg))
						state = STATE_SWITCHES_ACTIVATION;
					else if (SWITCH_STACKDEPTH1.equalsIgnoreCase(arg))
						state = STATE_SWITCHES_STACK;
					else if (target)
						options.args.add(arg);
					else
					{
						options.targetName = arg;
						target = true;
					}
				}
				break;
				
				case STATE_SCRIPTFILE:
				{
					options.scriptFile = new File(arg);
					state = STATE_START;
				}
				break;
				
				case STATE_PROPERTYFILE:
				{
					options.propertiesFile = new File(arg);
					state = STATE_START;
				}
				break;
				
				case STATE_MODULENAME:
				{
					options.templateNames.add(arg);
				}
				break;
				
				case STATE_SWITCHES_RUNAWAY:
				{
					int n;
					try {
						n = Integer.parseInt(arg);
						options.runawayLimit = n > 0 ? n : null;
					} catch (NumberFormatException e) {
						options.runawayLimit = null;
						throw new OptionParseException("Runaway limit needs to be a number greater than 0.");
					}
					state = STATE_START;
				}
				break;
				
				case STATE_SWITCHES_ACTIVATION:
				{
					int n;
					try {
						n = Integer.parseInt(arg);
						options.activationDepth = n > 0 ? n : null;
					} catch (NumberFormatException e) {
						options.activationDepth = null;
						throw new OptionParseException("Activation depth needs to be a number greater than 0.");
					}
					state = STATE_START;
				}
				break;
				
				case STATE_SWITCHES_STACK:
				{
					int n;
					try {
						n = Integer.parseInt(arg);
						options.stackDepth = n > 0 ? n : null;
					} catch (NumberFormatException e) {
						options.stackDepth = null;
						throw new OptionParseException("Stack depth needs to be a number greater than 0.");
					}
					state = STATE_START;
				}
				break;
				
				case STATE_PROJECTTYPE:
				{
					if ((options.projectType = ProjectType.TYPES.get(arg)) == null)
						throw new OptionParseException("Bad project type.");
					state = STATE_START;
				}
				break;
				
			}
			i++;
		}
		
		if (state == STATE_SCRIPTFILE)
			throw new OptionParseException("ERROR: Expected script file after switch.");
		if (state == STATE_PROPERTYFILE)
			throw new OptionParseException("ERROR: Expected properties file after switch.");
		if (state == STATE_SWITCHES_ACTIVATION)
			throw new OptionParseException("ERROR: Expected number after activation depth switch.");
		if (state == STATE_SWITCHES_STACK)
			throw new OptionParseException("ERROR: Expected number after stack depth switch.");
		if (state == STATE_SWITCHES_RUNAWAY)
			throw new OptionParseException("ERROR: Expected number after runaway limit switch.");
		if (state == STATE_PROJECTTYPE)
			throw new OptionParseException("ERROR: Expected project type name after project type switch.");
		
		return options;
	}
	
	/**
	 * Calls the utility using a set of options.
	 * @param options the options to call with.
	 * @return the error code.
	 */
	public static int call(Options options)
	{
		try {
			return (int)(asCallable(options).call());
		} catch (Exception e) {
			e.printStackTrace(options.stderr);
			return ERROR_UNKNOWN;
		}
	}
	
	/**
	 * Creates a {@link Callable} for this utility.
	 * @param options the options to use.
	 * @return a Callable that returns the process error.
	 */
	public static Callable<Integer> asCallable(Options options)
	{
		return new Context(options);
	}
	
	public static void main(String[] args) throws IOException
	{
		try {
			Options options = options(System.out, System.err, System.in, args);
			int status = call(options);
			if (status != 0 && args.length == 0 && !options.scriptFile.exists())
			{
				splash(System.out);
				usage(System.out);
				System.exit(-1);
				return;
			}
			System.exit(status);
		} catch (OptionParseException e) {
			System.err.println(e.getMessage());
			System.exit(ERROR_BAD_OPTIONS);
		}
	}
	
	/**
	 * Gets a file path for a project's path that is in a project directory. 
	 * @param projectDirectory the project directory root.
	 * @param properties the properties to look into.
	 * @param property the property name.
	 * @param defaultValue the default value, if not found.
	 * @return the project path.
	 */
	public static File getProjectPropertyPath(File projectDirectory, Properties properties, String property, String defaultValue)
	{
		String path = properties.getProperty(property);
		if (ObjectUtils.isEmpty(path))
			path = defaultValue;
		return new File(projectDirectory.getPath() + File.separator + path);
	}

	/**
	 * Reads in the lock JSON file and returns it as an object.
	 * If the file does not exist, an empty object is returned.
	 * @param projectDirectory the project directory root.
	 * @param properties the properties to inspect for the lock file name.
	 * @return the parsed object.
	 * @throws IOException if the file could not be opened or read.
	 * @throws JSONConversionException if the JSON is malformed.
	 */
	public static JSONObject readLockObject(File projectDirectory, Properties properties) throws IOException
	{
		File fullFilePath = getLockFile(projectDirectory, properties);
	
		if (!projectDirectory.exists())
			return JSONObject.createEmptyObject();
		
		if (!FileUtils.createPathForFile(fullFilePath))
			throw new IOException("Could not create directories for lock file.");
		
		if (!fullFilePath.exists())
			return JSONObject.createEmptyObject();
		
		JSONObject lockRoot;
		try (Reader reader = new InputStreamReader(new FileInputStream(fullFilePath), "UTF-8"))
		{
			lockRoot = JSONReader.readJSON(reader);
		}
		
		return lockRoot;
	}

	/**
	 * Writes the lock JSON file.
	 * @param projectDirectory the project directory root.
	 * @param properties the properties to inspect for the lock file name.
	 * @param lockRoot the lock object.
	 * @throws IOException if the file could not be opened or written.
	 */
	public static void writeLockObject(File projectDirectory, Properties properties, JSONObject lockRoot) throws IOException
	{
		File fullFilePath = getLockFile(projectDirectory, properties);
	
		if (!FileUtils.createPathForFile(fullFilePath))
			throw new IOException("Could not create directories for lock file.");
		
		JSONWriter.Options jsonOptions = new JSONWriter.Options();
		jsonOptions.setIndentation("\t");
	
		try (Writer writer = new OutputStreamWriter(new FileOutputStream(fullFilePath), "UTF-8"))
		{
			JSONWriter.writeJSON(lockRoot, jsonOptions, writer);
		}
	}

	/**
	 * Prints the "agent running" message.
	 * @param out the print stream to print to.
	 */
	private static void agentMessage(PrintStream out, File lockFile)
	{
		out.println("ERROR: The DoomMake Auto-Build agent is running for this project.");
		out.println("If this is incorrect, then delete the \"" + JSON_AGENT_LOCK_KEY + "\" key from the lock file:");
		try {
			out.println(lockFile.getCanonicalPath());
		} catch (IOException e) {
			out.println(lockFile.getAbsolutePath());
		}
	}

	// Gets the lock file path.
	private static File getLockFile(File projectDirectory, Properties properties) 
	{
		File buildDir = DoomMakeMain.getProjectPropertyPath(projectDirectory, properties, "doommake.dir.build", "build"); 
		String lockFile = properties.getProperty("doommake.file.lock", "lock.json");
		if (ObjectUtils.isEmpty(lockFile))
			lockFile = "lock.json";
		File fullFilePath = new File(buildDir.getPath() + File.separator + lockFile);
		return fullFilePath;
	}

	/**
	 * Prints the splash.
	 * @param out the print stream to print to.
	 */
	private static void splash(PrintStream out)
	{
		out.println("DoomMake v" + Version.DOOMMAKE + " by Matt Tropiano");
		out.println("(using DoomStruct v" + Version.DOOMSTRUCT + " , WadScript v" + Version.WADSCRIPT + ", RookScript v" + Version.ROOKSCRIPT + ", RookScript-Desktop v" + Version.ROOKSCRIPT_DESKTOP + ")");
	}

	/**
	 * Prints the usage.
	 * @param out the print stream to print to.
	 */
	private static void usage(PrintStream out)
	{
		out.println("Usage: doommake [target] [args] [switches]");
		out.println("                [directory] --new-project [templates...]");
		out.println("                [--gui | --new-project-gui]");
		out.println("                [--list-templates | -t]");
		out.println("                [--help | -h | --version]");
		out.println("                [--function-help | --function-help-markdown]");
		out.println("                [--disassemble]");
		out.println("                [--embed]");
	}
	
	/**
	 * Prints the changelog.
	 * @param out the print stream to print to.
	 */
	private static void changelog(PrintStream out, String name)
	{
		String line;
		int i = 0;
		try (BufferedReader br = IOUtils.openTextStream(IOUtils.openResource("docs/changelogs/CHANGELOG-" + name + ".md")))
		{
			while ((line = br.readLine()) != null)
			{
				if (i >= 3) // eat the first three lines
					out.println(line);
				i++;
			}
		} 
		catch (IOException e) 
		{
			out.println("****** ERROR: Cannot read CHANGELOG ******");
		}
	}
	
	/**
	 * Prints the help.
	 * @param out the print stream to print to.
	 */
	private static void help(PrintStream out)
	{
		out.println("[target]:");
		out.println("    The target entry point to execute. Default is \"make\".");
		out.println();
		out.println("[args]:");
		out.println("    The additional arguments to pass along to the script.");
		out.println();
		out.println("[templates]:");
		out.println("    The names of the template(s) to use or combine for the new project");
		out.println("    (applied altogether). Only one from each category is allowed.");
		out.println("    E.g.: --new-project maps git");
		out.println("        Combines the \"maps\", and \"git\" templates together.");
		out.println();
		out.println("[directory]:");
		out.println("    The directory/name for the new project.");
		out.println();
		out.println("[switches]:");
		out.println("    --help, -h                     Prints this help.");
		out.println("    --version                      Prints the version of this utility.");
		out.println("    --changelog                    Prints the changelog, and exits.");
		out.println("    --function-help                Prints all available function usages.");
		out.println("    --function-help-markdown       Prints all available function usages in");
		out.println("                                       Markdown format.");
		out.println("    --function-help-html           Prints all available function usages in");
		out.println("                                       HTML format.");
		out.println("    --function-help-html-div       Prints all available function usages in");
		out.println("                                       HTML format, but just the content.");
		out.println("    --disassemble                  Prints the disassembly for the make script");
		out.println("                                       in use and exits.");
		out.println();
		out.println("-----------------------------------------------------------------------------");
		out.println();
		out.println("    --project-type [type]          Sets the project type. Possible values:");
		out.println("                                      WAD, TEXTURE");
		out.println("                                      Default: WAD");
		out.println();
		out.println("    --new-project, -n [templates]  Creates a new project made up of a set of");
		out.println("                                      templates (requires [directory]).");
		out.println();
		out.println("    --list-templates, -t           Lists all available project templates.");
		out.println();
		out.println("-----------------------------------------------------------------------------");
		out.println();
		out.println("    --gui                          Opens this project in a graphical interface");
		out.println("                                       mode.");
		out.println();
		out.println("    --studio                       Opens this project in DoomMake Studio.");
		out.println();
		out.println("    --new-project-gui              Opens the \"New Project\" GUI.");
		out.println();
		out.println("-----------------------------------------------------------------------------");
		out.println();
		out.println("    --script, -s [filename]        Use [filename] for the root build script.");
		out.println("                                       Default is \"doommake.script\".");
		out.println("    --properties, -p [filename]    Use [filename] for the project properties.");
		out.println("                                       Default is \"doommake.properties\".");
		out.println();
		out.println("    --runaway-limit [num]          Sets the runaway limit (in operations)");
		out.println("                                       before the soft protection on infinite");
		out.println("                                       loops triggers. 0 is no limit.");
		out.println("                                       Default: 0");
		out.println("    --activation-depth [num]       Sets the activation depth to [num].");
		out.println("                                       Default: 256");
		out.println("    --stack-depth [num]            Sets the stack value depth to [num].");
		out.println("                                       Default: 2048");
		out.println();
		out.println("-----------------------------------------------------------------------------");
		out.println();
		out.println("    --targets                      Displays all available targets for this");
		out.println("                                       project.");
		out.println();
		out.println("-----------------------------------------------------------------------------");
		out.println();
		out.println("    --embed                        Embeds DoomTools/DoomMake into the current");
		out.println("                                       project.");
		out.println();
		out.println("-----------------------------------------------------------------------------");
		out.println();
		out.println("    --auto-build                   Starts DoomMake as an agent for detecting");
		out.println("                                       changes in the current project and");
		out.println("                                       kicking off full builds when changes");
		out.println("                                       are detected.");
		out.println();
		out.println("    --auto-build-verbose           Same as above, except it produces more");
		out.println("                                       verbose output.");
		out.println();
		out.println("While the agent is listening on a project, any attempt to run any DoomMake");
		out.println("targets on that project will error out. In order to run targets, you can use:");
		out.println();
		out.println("    --agent-bypass                 Bypasses agent detection. USE WITH CAUTION.");
		out.println("                                       Used internally.");
		out.println();
		out.println("-----------------------------------------------------------------------------");
		out.println();
		out.println("Scopes");
		out.println("------");
		out.println("DoomMake has a variable scope called `global` that serves as a common variable");
		out.println("scope for sharing values outside of functions or for data that you would want");
		out.println("to initialize once.");
	}

}

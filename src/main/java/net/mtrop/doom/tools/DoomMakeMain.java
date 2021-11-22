package net.mtrop.doom.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import net.mtrop.doom.struct.io.IOUtils;
import net.mtrop.doom.tools.WadScriptMain.Mode;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.doommake.ProjectGenerator;
import net.mtrop.doom.tools.doommake.ProjectModule;
import net.mtrop.doom.tools.doommake.ProjectTemplate;
import net.mtrop.doom.tools.doommake.ProjectTokenReplacer;
import net.mtrop.doom.tools.doommake.functions.DoomMakeFunctions;
import net.mtrop.doom.tools.doommake.functions.ToolInvocationFunctions;
import net.mtrop.doom.tools.doommake.generators.WADProjectGenerator;
import net.mtrop.doom.tools.exception.OptionParseException;
import net.mtrop.doom.tools.exception.UtilityException;

/**
 * Main class for DoomMake.
 * @author Matthew Tropiano
 */
public final class DoomMakeMain 
{
	private static final String DOOM_VERSION = Common.getVersionString("doom");
	private static final String ROOKSCRIPT_VERSION = Common.getVersionString("rookscript");
	private static final String ROOKSCRIPT_DESKTOP_VERSION = Common.getVersionString("rookscript-desktop");
	private static final String WADSCRIPT_VERSION = Common.getVersionString("wadscript");
	private static final String VERSION = Common.getVersionString("doommake");

	private static final String SHELL_OPTIONS = "-Xms64M -Xmx784M";
	private static final String SHELL_RESOURCE_CMD = "shell/embed/app-name.cmd";
	private static final String SHELL_RESOURCE_SH = "shell/embed/app-name.sh";
	
	private static final String ENVVAR_DOOMTOOLS_PATH = "DOOMTOOLS_PATH";
	private static final String ENVVAR_DOOMTOOLS_JAR = "DOOMTOOLS_JAR";

	private static final int ERROR_NONE = 0;
	private static final int ERROR_BAD_OPTIONS = 1;
	private static final int ERROR_BAD_PROPERTIES = 2;
	private static final int ERROR_BAD_SCRIPT = 3;
	private static final int ERROR_BAD_PROJECT = 4;
	private static final int ERROR_SECURITY = 5;
	private static final int ERROR_BAD_TOOL_PATH = 6;
	private static final int ERROR_IOERROR = 7;
	private static final int ERROR_UNKNOWN = 100;

	private static final String SWITCH_HELP = "--help";
	private static final String SWITCH_HELP2 = "-h";
	private static final String SWITCH_VERSION = "--version";
	private static final String SWITCH_FUNCHELP1 = "--function-help";
	private static final String SWITCH_FUNCHELP2 = "--function-help-markdown";
	private static final String SWITCH_FUNCHELP3 = "--function-help-html";
	private static final String SWITCH_FUNCHELP4 = "--function-help-html-div";
	private static final String SWITCH_LISTMODULES = "--list-templates";
	private static final String SWITCH_LISTMODULES2 = "-t";
	private static final String SWITCH_NEWPROJECT = "--new-project";
	private static final String SWITCH_NEWPROJECT2 = "-n";
	private static final String SWITCH_EMBED = "--embed";
	
	private static final String SWITCH_SCRIPTFILE = "--script";
	private static final String SWITCH_SCRIPTFILE2 = "-s";
	private static final String SWITCH_PROPERTYFILE = "--properties";
	private static final String SWITCH_PROPERTYFILE2 = "-p";
	private static final String SWITCH_RUNAWAYLIMIT1 = "--runaway-limit";
	private static final String SWITCH_ACTIVATIONDEPTH1 = "--activation-depth";
	private static final String SWITCH_STACKDEPTH1 = "--stack-depth";
	private static final String SWITCH_DISASSEMBLE1 = "--disassemble";

	/**
	 * Project types.
	 */
	public enum ProjectType
	{
		WAD(WADProjectGenerator.class);
		
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
			
			this.projectType = null;
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

		public Options addArg(String arg)
		{
			this.args.add(arg);
			return this;
		}
		
	}
	
	/**
	 * Program context.
	 */
	private static class Context
	{
		private Options options;
	
		private Context(Options options)
		{
			this.options = options;
		}
		
		public int call()
		{
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
							if (ProjectGenerator.TEMPLATE_BASE.equals(template.getName()))
								continue;
							
							String description = template.getDescription();
							options.stdout.println("    " + template.getName());
							Common.printWrapped(options.stdout, 0, 8, 72, Common.isEmpty(description) ? "" : description);
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
			
			// Detect project.
			if (options.mode == Mode.EXECUTE && !options.scriptFile.exists())
			{
				options.stderr.printf("ERROR: Script file \"%s\" could not be found!\n", options.scriptFile.getPath());
				return ERROR_BAD_SCRIPT;
			}
			
			if (options.embed)
			{
				return embedDoomMake();
			}

			loadProperties(new File("doommake.project.properties"));
			loadProperties(options.propertiesFile);
			
			try {
				WadScriptMain.Options wsOptions = WadScriptMain.options(options.stdout, options.stderr, options.stdin)
					.setMode(options.mode)
					.setDocsTitle("DoomMake Functions")
					.setEntryPointName(options.targetName)
					.setStackDepth(options.stackDepth)
					.setActivationDepth(options.activationDepth)
					.setRunawayLimit(options.runawayLimit)
					.setScriptFile(options.scriptFile)
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

		private int createProject(ProjectGenerator generator)
		{
			if (Common.isEmpty(options.targetName))
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

			if (options.templateNames.isEmpty())
			{
				options.templateNames.add(ProjectGenerator.TEMPLATE_BASE);
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
				path = System.getenv(ENVVAR_DOOMTOOLS_PATH);
				jarName = System.getenv(ENVVAR_DOOMTOOLS_JAR);
			} catch (SecurityException e) {
				options.stderr.println("ERROR: Could not fetch value of ENVVAR " + ENVVAR_DOOMTOOLS_PATH);
				return ERROR_SECURITY;
			}
			
			if (Common.isEmpty(path))
			{
				options.stderr.println("ERROR: DOOMTOOLS_PATH ENVVAR not set. Not invoked via shell?");
				return ERROR_BAD_TOOL_PATH;
			}
			
			if (Common.isEmpty(jarName))
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
				if (!Common.createPathForFile(targetJARFile))
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
				Common.copyShellScript(SHELL_RESOURCE_CMD, DoomMakeMain.class, SHELL_OPTIONS, targetJARFile.getPath().replace("/", "\\"), targetShellCmdFile);
				Common.copyShellScript(SHELL_RESOURCE_SH, DoomMakeMain.class, SHELL_OPTIONS, targetJARFile.getPath().replace("\\", "/"), targetShellBashFile);
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
					else if (arg.equalsIgnoreCase(SWITCH_LISTMODULES) || arg.equalsIgnoreCase(SWITCH_LISTMODULES2))
					{
						options.projectType = ProjectType.WAD;
						options.listModules = true;
					}
					else if (arg.equalsIgnoreCase(SWITCH_NEWPROJECT) || arg.equalsIgnoreCase(SWITCH_NEWPROJECT2))
					{
						options.projectType = ProjectType.WAD;
						options.templateNames = new LinkedList<>();
						state = STATE_MODULENAME;
					}
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
		
		return options;
	}
	
	/**
	 * Calls the utility using a set of options.
	 * @param options the options to call with.
	 * @return the error code.
	 */
	public static int call(Options options)
	{
		return (new Context(options)).call();
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
	 * Prints the splash.
	 * @param out the print stream to print to.
	 */
	private static void splash(PrintStream out)
	{
		out.println("DoomMake v" + VERSION + " by Matt Tropiano");
		out.println("(using DoomStruct v" + DOOM_VERSION + " , WadScript v" + WADSCRIPT_VERSION + ", RookScript v" + ROOKSCRIPT_VERSION + ", RookScript-Desktop v" + ROOKSCRIPT_DESKTOP_VERSION + ")");
	}

	/**
	 * Prints the usage.
	 * @param out the print stream to print to.
	 */
	private static void usage(PrintStream out)
	{
		out.println("Usage: doommake [target] [args] [switches]");
		out.println("                [directory] --new-project [templates...]");
		out.println("                [--list-templates | -t]");
		out.println("                [--help | -h | --version]");
		out.println("                [--function-help | --function-help-markdown]");
		out.println("                [--disassemble]");
		out.println("                [--embed]");
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
		out.println("    --new-project, -n [templates]  Creates a new project made up of a set of");
		out.println("                                      templates (requires [directory]).");
		out.println();
		out.println("    --list-templates, -t           Lists all available project templates.");
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
		out.println("    --embed                        Embeds DoomTools/DoomMake into the current");
		out.println("                                       project.");
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

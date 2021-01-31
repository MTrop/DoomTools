package net.mtrop.doom.tools;

import java.io.File;
import java.io.FileInputStream;
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
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import net.mtrop.doom.tools.WadScriptMain.Mode;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.doommake.ProjectModule;
import net.mtrop.doom.tools.doommake.functions.DoomMakeFunctions;
import net.mtrop.doom.tools.doommake.functions.ToolInvocationFunctions;
import net.mtrop.doom.tools.exception.OptionParseException;

import static net.mtrop.doom.tools.doommake.ProjectModule.create;
import static net.mtrop.doom.tools.doommake.ProjectModule.file;
import static net.mtrop.doom.tools.doommake.ProjectModule.dir;
import static net.mtrop.doom.tools.doommake.ProjectModule.fileAppend;
import static net.mtrop.doom.tools.doommake.ProjectModule.fileContentAppend;

/**
 * Main class for DoomMake.
 * @author Matthew Tropiano
 */
public final class DoomMakeMain 
{
	private static final String DOOM_VERSION = Common.getVersionString("doom");
	private static final String ROOKSCRIPT_VERSION = Common.getVersionString("rookscript");
	private static final String WADSCRIPT_VERSION = Common.getVersionString("wadscript");
	private static final String VERSION = Common.getVersionString("doommake");

	private static final int ERROR_NONE = 0;
	private static final int ERROR_BAD_OPTIONS = 1;
	private static final int ERROR_BAD_PROPERTIES = 2;
	private static final int ERROR_BAD_SCRIPT = 3;
	private static final int ERROR_BAD_MODULE = 4;
	private static final int ERROR_BAD_PROJECT = 5;
	private static final int ERROR_UNKNOWN = 100;

	private static final String SWITCH_HELP = "--help";
	private static final String SWITCH_HELP2 = "-h";
	private static final String SWITCH_VERSION = "--version";
	private static final String SWITCH_FUNCHELP1 = "--function-help";
	private static final String SWITCH_FUNCHELP2 = "--function-help-markdown";
	private static final String SWITCH_LISTMODULES = "--list-modules";
	private static final String SWITCH_LISTMODULES2 = "-lm";
	private static final String SWITCH_NEWPROJECT = "--new-project";
	private static final String SWITCH_NEWPROJECT2 = "-n";
	
	private static final String SWITCH_SCRIPTFILE = "--script";
	private static final String SWITCH_SCRIPTFILE2 = "-s";
	private static final String SWITCH_PROPERTYFILE = "--properties";
	private static final String SWITCH_PROPERTYFILE2 = "-p";
	private static final String SWITCH_RUNAWAYLIMIT1 = "--runaway-limit";
	private static final String SWITCH_ACTIVATIONDEPTH1 = "--activation-depth";
	private static final String SWITCH_STACKDEPTH1 = "--stack-depth";

	/** The main templates. */
	private static final Map<String, ProjectModule> MODULES = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	/** The release template fragments. */
	private static final Map<String, ProjectModule> RELEASE_SCRIPT = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	/** The release template fragments. */
	private static final Map<String, ProjectModule> RELEASE_SCRIPT_MERGE = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	/** The release WadMerge line. */
	private static final Map<String, String> RELEASE_WADMERGE_LINE = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	
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

		private List<String> createProject;
		
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
			this.createProject = null;

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

			if (options.listModules)
			{
				setUpTemplates();
				for (Entry<String, ProjectModule> descriptor : MODULES.entrySet())
				{
					String description = descriptor.getValue().getDescription();
					options.stdout.println(descriptor.getKey());
					Common.printWrapped(options.stdout, 0, 4, 76, Common.isEmpty(description) ? "" : description);
					options.stdout.println();
				}
				return ERROR_NONE;
			}

			if (options.createProject != null)
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
				
				try {
					return createProject(options);
				} catch (IOException e) {
					options.stderr.println("ERROR: Project creation error: " + e.getLocalizedMessage());
					return ERROR_BAD_PROJECT;
				}
			}

			if (options.mode == Mode.EXECUTE && !options.scriptFile.exists())
			{
				options.stderr.printf("ERROR: Script file \"%s\" could not be found!\n", options.scriptFile.getPath());
				return ERROR_BAD_SCRIPT;
			}
			
			loadProperties(new File("doommake.project.properties"));
			loadProperties(options.propertiesFile);
			
			try {
				WadScriptMain.Options wsOptions = WadScriptMain.options(options.stdout, options.stderr, options.stdin)
					.setMode(options.mode)
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

		// Creates a project.
		private int createProject(Options options) throws IOException
		{
			setUpTemplates();

			// Get Templates.
			SortedSet<ProjectModule> selected = new TreeSet<>();
			for (String name : options.createProject)
			{
				ProjectModule found;
				if ((found = MODULES.get(name)) == null)
				{
					options.stderr.println("ERROR: No such project module: " + name);
					options.stderr.println("Try running DoomMake with `--list-modules` for the list of available modules.");
					return ERROR_BAD_MODULE;
				}
				selected.add(found);
			}
			
			File targetDirectory = new File(options.targetName);

			// Common structure.
			create(
				dir("build"),
				dir("dist"),
				file("src/wadinfo.txt", 
					"doommake/common/wadinfo.txt"),
				file("src/credits.txt", 
					"doommake/common/credits.txt"),
				file("doommake.script",
					"doommake/doommake.script"),
				file("scripts/doommake-init.script",
					"doommake/doommake-init.script"),
				file("scripts/doommake-lib.script",
					"doommake/doommake-lib.script"),
				file("doommake.properties",
					"doommake/doommake.properties"),
				file("doommake.project.properties",
					"doommake/doommake.project.properties"),
				file("README.md",
					"doommake/README.md")
			).createIn(targetDirectory);
			
			// Modules.
			for (ProjectModule module : selected)
				module.createIn(targetDirectory);

			// Add release script header.
			create(
				fileAppend("doommake.script",
					"doommake/projects/doommake-header.script")
			).createIn(targetDirectory);

			// Project Modules.
			for (ProjectModule module : selected)
			{
				ProjectModule found;
				if ((found = RELEASE_SCRIPT.get(module.getName())) != null)
					found.createIn(targetDirectory);
			}

			// WadMerge Properties Start
			create(
				fileContentAppend("doommake.script", 
					"\n\twadmerge(file(MERGESCRIPT_RELEASE), [",
            		"\t\tgetBuildDirectory()",
            		"\t\t,getProjectWad()"
            	)
			).createIn(targetDirectory);
			
			// Add merge script.
			create(
				file("scripts/merge-release.txt",
					"doommake/projects/wadmerge-header.txt")
			).createIn(targetDirectory);

			// Project Modules.
			int x = 2;
			for (ProjectModule module : selected)
			{
				ProjectModule found;
				if ((found = RELEASE_SCRIPT_MERGE.get(module.getName())) != null)
				{
					found.createIn(targetDirectory);
					
					String line;
					if ((line = RELEASE_WADMERGE_LINE.get(module.getName())) != null)
					{
						create(
							fileContentAppend("scripts/merge-release.txt", line + (x++))
						).createIn(targetDirectory);
					}
				}
			}

			// WadMerge Properties End
			create(
				fileContentAppend("doommake.script", 
					"\t]);"
            	)
			).createIn(targetDirectory);
			
			// Add release script footer.
			create(
				fileAppend("doommake.script",
					"doommake/projects/doommake-footer.script")
			).createIn(targetDirectory);
			
			// Add merge script ending.
			create(
				fileContentAppend("scripts/merge-release.txt",
					"\nfinish out $0/$1",
					"end")
			).createIn(targetDirectory);
			
			options.stdout.println("Created project: " + options.targetName);
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
					else if (arg.equalsIgnoreCase(SWITCH_LISTMODULES) || arg.equalsIgnoreCase(SWITCH_LISTMODULES2))
						options.listModules = true;
					else if (arg.equalsIgnoreCase(SWITCH_NEWPROJECT) || arg.equalsIgnoreCase(SWITCH_NEWPROJECT2))
					{
						options.createProject = new LinkedList<>();
						state = STATE_MODULENAME;
					}
					else if (SWITCH_FUNCHELP1.equalsIgnoreCase(arg))
						options.mode = Mode.FUNCTIONHELP;
					else if (SWITCH_FUNCHELP2.equalsIgnoreCase(arg))
						options.mode = Mode.FUNCTIONHELP_MARKDOWN;
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
					options.createProject.add(arg);
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
		out.println("(using DoomStruct v" + DOOM_VERSION + ", RookScript v" + ROOKSCRIPT_VERSION + ", WadScript v" + WADSCRIPT_VERSION + ")");
	}

	/**
	 * Prints the usage.
	 * @param out the print stream to print to.
	 */
	private static void usage(PrintStream out)
	{
		out.println("Usage: doommake [target] [args] [switches]");
		out.println("                [directory] --new-project [modules...]");
		out.println("                [--list-modules | -lm]");
		out.println("                [--help | -h | --version]");
		out.println("                [--function-help | --function-help-markdown]");
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
		out.println("[modules]:");
		out.println("    The names of the modules(s) to use or combine for the new ");
		out.println("    project (applied altogether).");
		out.println("    E.g.: --new-project maps git");
		out.println("        Combines the \"maps\", and \"git\" modules together.");
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
		out.println("    --list-modules, -lm            Lists all available project modules.");
		out.println();
		out.println("-----------------------------------------------------------------------------");
		out.println();
		out.println("    --new-project, -n [modules]    Creates a new project made up of a set of");
		out.println("                                      templates (requires [directory]).");
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
	}

	/**
	 * Sets up the used templates (for lazy init).
	 */
	private static void setUpTemplates()
	{
		// ................................................................
		
		MODULES.put("git",
			create(0, "git", 
				"Adds files for Git repository support (ignores, attributes).",
				file(".gitignore", 
					"doommake/git/gitignore.txt"),
				file(".gitattributes", 
					"doommake/git/gitattributes.txt")
			)
		);

		// ................................................................

		MODULES.put("decohack",
			create(1, "decohack", 
				"A module that compiles a DeHackEd patch.",
				file("src/decohack/main.dh",
					"doommake/decohack/main.dh"),
				fileAppend("doommake.properties",
					"doommake/decohack/doommake.properties"),
				fileAppend("doommake.script", 
					"doommake/decohack/doommake.script"),
				fileAppend("README.md",
					"doommake/decohack/README.md")
			)
		);
		RELEASE_SCRIPT.put("decohack",
			create(
				fileContentAppend("doommake.script",
					"\tdoPatch(false);"
				)
			)
		);
		RELEASE_SCRIPT_MERGE.put("decohack",
			create(
				fileContentAppend("doommake.script",
					"\t\t,getPatchFile()"
				)
			)
		);
		RELEASE_WADMERGE_LINE.put("decohack",
			"mergefile  out $0/$"
		);
		
		// ................................................................

		MODULES.put("maps",
			create(2, "maps", 
				"A module that builds a set of maps.",
				dir("src/maps"),
				file("scripts/merge-maps.txt",
					"doommake/common/maps/wadmerge.txt"),
				fileAppend("doommake.properties",
					"doommake/common/maps/doommake.properties"),
				fileAppend("doommake.script", 
					"doommake/common/maps/doommake.script"),
				fileAppend("README.md",
					"doommake/common/maps/README.md")
			)
		);
		RELEASE_SCRIPT.put("maps",
			create(
				fileContentAppend("doommake.script",
					"\tdoMaps();"
				)
			)
		);
		RELEASE_SCRIPT_MERGE.put("maps",
			create(
				fileContentAppend("doommake.script",
					"\t\t,getMapsWad()"
				)
			)
		);
		RELEASE_WADMERGE_LINE.put("maps",
			"mergewad   out $0/$"
		);
		
		// ................................................................

		MODULES.put("assets",
			create(3, "assets", 
				"A module that builds maps and non-texture assets together.",
				dir("src/assets/_global"),
				dir("src/assets/graphics"),
				dir("src/assets/music"),
				dir("src/assets/sounds"),
				dir("src/assets/sprites"),
				file("scripts/merge-assets.txt",
					"doommake/common/assets/wadmerge.txt"),
				fileAppend("doommake.properties", 
					"doommake/common/assets/doommake.properties"),
				fileAppend("doommake.script",
					"doommake/common/assets/doommake.script"),
				fileAppend("README.md",
					"doommake/common/assets/README.md")
			)
		);
		RELEASE_SCRIPT.put("assets",
			create(
				fileContentAppend("doommake.script",
					"\tdoAssets();"
				)
			)
		);
		RELEASE_SCRIPT_MERGE.put("assets",
			create(
				fileContentAppend("doommake.script",
					"\t\t,getAssetsWAD()"
				)
			)
		);
		RELEASE_WADMERGE_LINE.put("assets",
			"mergewad   out $0/$"
		);
		
		// ................................................................

		MODULES.put("textures",
			create(4, "textures", 
				"A module that builds texture WADs." +
				"\nIf this is used, do NOT use the \"texwad\" module.",
				dir("src/textures/flats"),
				dir("src/textures/patches"),
				file("scripts/merge-textures.txt",
					"doommake/common/textures/wadmerge.txt"),
				file("src/textures/texture1.txt", 
					"doommake/common/textures/texture1.txt"),
				file("src/textures/texture2.txt", 
					"doommake/common/textures/texture2.txt"),
				fileAppend("doommake.properties", 
					"doommake/common/textures/doommake.properties"),
				fileAppend("doommake.script", 
					"doommake/common/textures/doommake.script"),
				fileAppend("README.md",
					"doommake/common/textures/README.md")
			)
		);
		RELEASE_SCRIPT.put("textures",
			create(
				fileContentAppend("doommake.script",
					"\tdoTextures();",
					"\tdoMapTextures();"
				)
			)
		);
		RELEASE_SCRIPT_MERGE.put("textures",
			create(
				fileContentAppend("doommake.script",
					"\t\t,getMapTexWad()"
				)
			)
		);
		RELEASE_WADMERGE_LINE.put("textures",
			"mergewad   out $0/$"
		);
		
		// ................................................................

		MODULES.put("texwad",
			create(5, "texwad", 
				"A module that uses textures from a set of provided texture WADs." +
				"\nIf this is used, do NOT use the \"textures\" module.",
				dir("src/wads/textures"),
				fileAppend("doommake.properties",
					"doommake/common/texwad/doommake.properties"),
				fileAppend("doommake.script",
					"doommake/common/texwad/doommake.script"),
				fileAppend("README.md",
					"doommake/common/texwad/README.md")
			)
		);
		RELEASE_SCRIPT.put("texwad",
			create(
				fileContentAppend("doommake.script",
					"\tdoMapTextures();"
				)
			)
		);
		RELEASE_SCRIPT_MERGE.put("texwad",
			create(
				fileContentAppend("doommake.script",
					"\t\t,getMapTexWad()"
				)
			)
		);
		RELEASE_WADMERGE_LINE.put("texwad",
			"mergewad   out $0/$"
		);
	}

}

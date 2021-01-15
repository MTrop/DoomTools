/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import com.blackrook.rookscript.Script;
import com.blackrook.rookscript.ScriptAssembler;
import com.blackrook.rookscript.ScriptEnvironment;
import com.blackrook.rookscript.ScriptInstance;
import com.blackrook.rookscript.ScriptInstanceBuilder;
import com.blackrook.rookscript.ScriptValue;
import com.blackrook.rookscript.ScriptValue.ErrorType;
import com.blackrook.rookscript.exception.ScriptExecutionException;
import com.blackrook.rookscript.exception.ScriptParseException;
import com.blackrook.rookscript.functions.MathFunctions;
import com.blackrook.rookscript.functions.RegexFunctions;
import com.blackrook.rookscript.functions.SystemFunctions;
import com.blackrook.rookscript.functions.ZipFunctions;
import com.blackrook.rookscript.functions.common.BufferFunctions;
import com.blackrook.rookscript.functions.common.ErrorFunctions;
import com.blackrook.rookscript.functions.common.ListFunctions;
import com.blackrook.rookscript.functions.common.MapFunctions;
import com.blackrook.rookscript.functions.common.MiscFunctions;
import com.blackrook.rookscript.functions.common.StringFunctions;
import com.blackrook.rookscript.functions.io.DataIOFunctions;
import com.blackrook.rookscript.functions.io.FileIOFunctions;
import com.blackrook.rookscript.functions.io.StreamingIOFunctions;
import com.blackrook.rookscript.lang.ScriptFunctionType;
import com.blackrook.rookscript.lang.ScriptFunctionType.Usage;
import com.blackrook.rookscript.lang.ScriptFunctionType.Usage.ParameterUsage;
import com.blackrook.rookscript.lang.ScriptFunctionType.Usage.TypeUsage;
import com.blackrook.rookscript.resolvers.ScriptFunctionResolver;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.exception.OptionParseException;
import net.mtrop.doom.tools.scripting.DoomMapFunctions;
import net.mtrop.doom.tools.scripting.PK3Functions;
import net.mtrop.doom.tools.scripting.UtilityFunctions;
import net.mtrop.doom.tools.scripting.WadFunctions;

import com.blackrook.rookscript.functions.DateFunctions;
import com.blackrook.rookscript.functions.DigestFunctions;
import com.blackrook.rookscript.functions.FileSystemFunctions;
import com.blackrook.rookscript.functions.JSONFunctions;
import com.blackrook.rookscript.functions.PrintFunctions;

/**
 * Main class for executing scripts.
 * @author Matthew Tropiano
 */
public final class WadScriptMain
{
	private static final String DOOM_VERSION = Common.getVersionString("doom");
	private static final String ROOKSCRIPT_VERSION = Common.getVersionString("rookscript");
	private static final String VERSION = Common.getVersionString("wadscript");

	private static final int ERROR_NONE = 0;
	private static final int ERROR_BAD_SCRIPT = 4;
	private static final int ERROR_BAD_SCRIPT_ENTRY = 5;
	private static final int ERROR_SCRIPT_EXECUTION_ERROR = 6;
	private static final int ERROR_SCRIPT_RETURNED_ERROR = 7;
	private static final int ERROR_SCRIPT_INSTANCE_EXECUTION = 8;
	private static final int ERROR_SCRIPT_NOT_STARTED = 9;

	private static final String SWITCH_VERSION1 = "--version";
	private static final String SWITCH_HELP1 = "--help";
	private static final String SWITCH_HELP2 = "-h";
	private static final String SWITCH_FUNCHELP1 = "--function-help";
	private static final String SWITCH_FUNCHELP2 = "--function-help-markdown";
	private static final String SWITCH_DISASSEMBLE1 = "--disassemble";
	private static final String SWITCH_ENTRY1 = "--entry";
	private static final String SWITCH_RUNAWAYLIMIT1 = "--runaway-limit";
	private static final String SWITCH_ACTIVATIONDEPTH1 = "--activation-depth";
	private static final String SWITCH_STACKDEPTH1 = "--stack-depth";
	private static final String SWITCH_SEPARATOR = "--";
	private static final String SWITCH_SEPARATORBASH = "--X";
	
	private static final Resolver[] RESOLVERS = 
	{
		new Resolver("Common", MiscFunctions.createResolver()),
		new Resolver("Printing/Logging", PrintFunctions.createResolver()),
		new Resolver("String", StringFunctions.createResolver()),
		new Resolver("List / Set", ListFunctions.createResolver()),
		new Resolver("Map", MapFunctions.createResolver()),
		new Resolver("Buffer", BufferFunctions.createResolver()),
		new Resolver("Error", ErrorFunctions.createResolver()),
		new Resolver("Math", MathFunctions.createResolver()),
		new Resolver("RegEx", RegexFunctions.createResolver()),
		new Resolver("Date / Time", DateFunctions.createResolver()),
		new Resolver("File System", FileSystemFunctions.createResolver()),
		new Resolver("File I/O", FileIOFunctions.createResolver()),
		new Resolver("Zip Files / GZIP Streams", ZipFunctions.createResolver()),
		new Resolver("Stream I/O", StreamingIOFunctions.createResolver()),
		new Resolver("Data I/O", DataIOFunctions.createResolver()),
		new Resolver("Digest", DigestFunctions.createResolver()),
		new Resolver("JSON", JSONFunctions.createResolver()),
		new Resolver("System", SystemFunctions.createResolver()),
		new Resolver("WADs", WadFunctions.createResolver()),
		new Resolver("PK3s", PK3Functions.createResolver()),
		new Resolver("Doom / Hexen / ZDoom / UDMF Maps", "MAP", DoomMapFunctions.createResolver()),
		new Resolver("Utilities", "UTIL", UtilityFunctions.createResolver())
	};
	
	private static class Resolver
	{
		private String sectionName;
		private String namespace;
		private ScriptFunctionResolver resolver;
		
		private Resolver(String sectionName, ScriptFunctionResolver resolver)
		{
			this.sectionName = sectionName;
			this.namespace = null;
			this.resolver = resolver;
		}

		private Resolver(String sectionName, String namespace, ScriptFunctionResolver resolver)
		{
			this.sectionName = sectionName;
			this.namespace = namespace;
			this.resolver = resolver;
		}
		
	}

	private interface UsageRendererType
	{
		/**
		 * Called on render start.
		 */
		void startRender();
		
		/**
		 * Renders a section break.
		 * @param title the section title.
		 */
		void renderSection(String title);

		/**
		 * Renders a single function usage doc.
		 * @param namespace the function namespace.
		 * @param functionName the function name.
		 * @param usage the usage to render (can be null).
		 */
		void renderUsage(String namespace, String functionName, Usage usage);

		/**
		 * Called on render finish.
		 */
		void finishRender();
		
	}
	
	private static class UsageTextRenderer implements UsageRendererType
	{
		private static final String NEWLINE_INDENT = "\n            ";
		
		private PrintStream out;
		
		private UsageTextRenderer(PrintStream out) 
		{
			this.out = out;
		}
		
		@Override
		public void startRender()
		{
			// Do nothing
		}

		@Override
		public void renderSection(String title) 
		{
			out.println("=================================================================");
			out.println("==== " + title);
			out.println("=================================================================");
			out.println();
		}

		private void renderTypeUsage(TypeUsage tu)
		{
			out.append("        (").append(tu.getType() != null 
					? (tu.getType().name() + (tu.getSubType() != null ? ":" + tu.getSubType() : "")) 
					: "ANY"
				).append(") ").println(tu.getDescription().replace("\n", NEWLINE_INDENT));
		}

		@Override
		public void renderUsage(String namespace, String functionName, Usage usage)
		{
			if (usage == null)
			{
				if (namespace != null)
					out.append(namespace + "::");
				out.append(functionName).append("(...)").println();
				out.println();
				return;
			}
			
			if (namespace != null)
				out.append(namespace + "::");
			
			out.append(functionName).append('(');
			List<ParameterUsage> pul = usage.getParameterInstructions();
			for (int i = 0; i < pul.size(); i++)
			{
				out.append(pul.get(i).getParameterName());
				if (i < pul.size() - 1)
					out.append(", ");
			}
			out.append(')').print('\n');
			
			out.append("    ").println(usage.getInstructions().replace("\n", NEWLINE_INDENT));
			if (!pul.isEmpty()) for (ParameterUsage pu : pul)
			{
				out.append("    ").append(pu.getParameterName()).println(":");
				for (TypeUsage tu : pu.getTypes())
					renderTypeUsage(tu);
			}
			
			out.append("    ").println("Returns:");
			for (TypeUsage tu : usage.getReturnTypes())
				renderTypeUsage(tu);
			out.println();
		}

		@Override
		public void finishRender() 
		{
			// Do nothing
		}

	}
	
	private static class UsageMarkdownRenderer implements UsageRendererType
	{
		final String NEWLINE_INDENT = "\n        - ";

		private PrintStream out;
		
		private UsageMarkdownRenderer(PrintStream out)
		{
			this.out = out;
		}
		
		@Override
		public void startRender()
		{
			// Do nothing
		}

		@Override
		public void renderSection(String title) 
		{
			out.append("# ").println(title);
			out.println();
		}
		
		private void renderTypeUsage(TypeUsage tu)
		{
			out.append("- `").append(tu.getType() != null ? tu.getType().name() : "ANY").append("` ");
			out.append((tu.getSubType() != null ? "*" + tu.getSubType() + "*" : ""));
			out.println();
			out.append("    - ").println(tu.getDescription().replace("\n", NEWLINE_INDENT));
		}
		
		@Override
		public void renderUsage(String namespace, String functionName, Usage usage)
		{
			if (usage == null)
			{
				out.append("## ");
				if (namespace != null)
					out.append(namespace + "::");
				out.append(functionName).append("(...)").println();
				out.println();
				return;
			}
			
			out.append("## ");
			if (namespace != null)
				out.append(namespace + "::");
			out.append(functionName).append('(');
			List<ParameterUsage> pul = usage.getParameterInstructions();
			for (int i = 0; i < pul.size(); i++)
			{
				out.append(pul.get(i).getParameterName());
				if (i < pul.size() - 1)
					out.append(", ");
			}
			out.append(')').print('\n');
			out.println();
			
			out.println(usage.getInstructions().replace("\n", NEWLINE_INDENT));
			out.println();
			if (!pul.isEmpty())
			{
				for (ParameterUsage pu : pul)
				{
					out.append("**").append(pu.getParameterName()).append("**").println(":");
					out.println();
					for (TypeUsage tu : pu.getTypes())
						renderTypeUsage(tu);
					out.println();
				}
				out.println();
			}

			out.append("**Returns**").println(":");
			out.println();
			for (TypeUsage tu : usage.getReturnTypes())
				renderTypeUsage(tu);
			out.println();
			out.println();
		}

		@Override
		public void finishRender() 
		{
			// Do nothing
		}

	}
	
	private enum Mode
	{
		VERSION,
		HELP,
		FUNCTIONHELP,
		FUNCTIONHELP_MARKDOWN,
		DISASSEMBLE,
		EXECUTE;
	}
	
	public static class Options
	{
		private PrintStream stdout;
		private PrintStream stderr;
		private Mode mode;
		private File scriptFile;
		private String entryPointName;
		private Integer runawayLimit;
		private Integer activationDepth;
		private Integer stackDepth;
		private List<Object> argList;
		
		private Options()
		{
			this.stdout = null;
			this.stderr = null;
			this.mode = Mode.EXECUTE;
			this.scriptFile = null;
			this.entryPointName = "main";
			this.runawayLimit = 0;
			this.activationDepth = 256;
			this.stackDepth = 2048;
			this.argList = new LinkedList<>();
		}

		public Options setMode(Mode mode) 
		{
			this.mode = mode;
			return this;
		}

		public Options setScriptFile(File scriptFile) 
		{
			this.scriptFile = scriptFile;
			return this;
		}
		
		public Options setEntryPointName(String entryPointName) 
		{
			this.entryPointName = entryPointName;
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
		
		public Options addArg(Object arg)
		{
			this.argList.add(arg);
			return this;
		}
		
	}

	private static class Context
	{
		private Options options;
		
		private Context(Options options)
		{
			this.options = options;
		}
		
		private int call()
		{
			if (options.mode == Mode.VERSION)
			{
				splash(options.stdout);
				return ERROR_NONE;
			}

			if (options.mode == Mode.HELP)
			{
				usage(options.stdout);
				printHelp(options.stdout);
				return ERROR_NONE;
			}
			
			if (options.mode == Mode.FUNCTIONHELP)
			{
				printFunctionHelp(new UsageTextRenderer(options.stdout));
				return ERROR_NONE;
			}
			
			if (options.mode == Mode.FUNCTIONHELP_MARKDOWN)
			{
				printFunctionHelp(new UsageMarkdownRenderer(options.stdout));
				return ERROR_NONE;
			}
			
			if (options.scriptFile == null)
			{
				options.stderr.println("ERROR: Bad script file.");
				return ERROR_BAD_SCRIPT;
			}
			if (!options.scriptFile.exists())
			{
				options.stderr.println("ERROR: Script file does not exist: " + options.scriptFile);
				return ERROR_BAD_SCRIPT;
			}
			if (options.scriptFile.isDirectory())
			{
				options.stderr.println("ERROR: Bad script file. Is directory.");
				return ERROR_BAD_SCRIPT;
			}
		
			ScriptInstance instance;
			
			try 
			{
				ScriptInstanceBuilder builder = ScriptInstance.createBuilder()
					.withSource(options.scriptFile)
					.withEnvironment(ScriptEnvironment.createStandardEnvironment())
					.withScriptStack(options.activationDepth, options.stackDepth)
					.withRunawayLimit(options.runawayLimit);

				// ============ Add Functions =============
				
				for (int i = 0; i < RESOLVERS.length; i++)
				{
					if (i == 0)
					{
						if (RESOLVERS[i].namespace != null)
							builder.withFunctionResolver(RESOLVERS[i].namespace, RESOLVERS[i].resolver);
						else
							builder.withFunctionResolver(RESOLVERS[i].resolver);
					}
					else 
					{
						if (RESOLVERS[i].namespace != null)
							builder.andFunctionResolver(RESOLVERS[i].namespace, RESOLVERS[i].resolver);
						else
							builder.andFunctionResolver(RESOLVERS[i].resolver);
					} 
				}
				
				// ========================================

				instance = builder.createInstance();
				
			} 
			catch (ScriptInstanceBuilder.BuilderException e) 
			{
				Throwable cause = e.getCause();
				if (cause instanceof ScriptParseException)
				{
					options.stderr.println("Script ERROR: " + cause.getLocalizedMessage());
					return ERROR_SCRIPT_INSTANCE_EXECUTION;
				}
				else if (cause != null)
				{
					options.stderr.println("ERROR: Script could not be started: " + cause.getLocalizedMessage());
					return ERROR_SCRIPT_INSTANCE_EXECUTION;
				}
				else
				{
					options.stderr.println("ERROR: " + e.getLocalizedMessage());
					return ERROR_SCRIPT_NOT_STARTED;
				}
			}
			
			if (options.mode == Mode.DISASSEMBLE)
			{
				options.stdout.println("Disassembly of \"" + options.scriptFile + "\":");
				doDisassemble(options.stdout, instance);
				return ERROR_NONE;
			}
		
			if (options.mode == Mode.EXECUTE)
			{
				if (options.entryPointName == null)
				{
					options.stderr.println("ERROR: Bad entry point.");
					return ERROR_BAD_SCRIPT;
				}
				if (options.activationDepth == null)
				{
					options.stderr.println("ERROR: Bad activation depth.");
					return ERROR_BAD_SCRIPT;
				}
				if (options.stackDepth == null)
				{
					options.stderr.println("ERROR: Bad stack depth.");
					return ERROR_BAD_SCRIPT;
				}
				if (options.runawayLimit == null)
				{
					options.stderr.println("ERROR: Bad runaway limit.");
					return ERROR_BAD_SCRIPT;
				}
				
				Script.Entry entryPoint;
				
				if ((entryPoint = instance.getScript().getScriptEntry(options.entryPointName)) == null)
				{
					options.stderr.println("ERROR: Entry point not found: " + options.entryPointName);
					return ERROR_BAD_SCRIPT_ENTRY;
				}
				
				Object[] args = options.argList.toArray(new Object[options.argList.size()]);
				try {
					ScriptValue retval = ScriptValue.create(null);

					if (entryPoint.getParameterCount() > 0)
						instance.call(options.entryPointName, new Object[]{args});
					else
						instance.call(options.entryPointName);

					instance.popStackValue(retval);
					
					if (retval.isError())
					{
						ErrorType error = retval.asObjectType(ErrorType.class);
						options.stderr.println("ERROR: [" + error.getType() + "]: " + error.getLocalizedMessage());
						return ERROR_SCRIPT_RETURNED_ERROR;
					}
					return retval.asInt();
				} catch (ScriptExecutionException e) {
					options.stderr.println("Script ERROR: " + e.getLocalizedMessage());
					return ERROR_SCRIPT_EXECUTION_ERROR;
				} catch (ClassCastException e) {
					options.stderr.println("Script return ERROR: " + e.getLocalizedMessage());
					return ERROR_SCRIPT_EXECUTION_ERROR;
				}
			}
			
			options.stderr.println("ERROR: Bad mode - INTERNAL ERROR.");
			return -1;
		}

		private void doDisassemble(PrintStream out, ScriptInstance instance)
		{
			StringWriter sw = new StringWriter();
			try {
				ScriptAssembler.disassemble(instance.getScript(), new PrintWriter(sw));
			} catch (IOException e) {
				// Do nothing.
			}
			out.print(sw);
		}

		private void printHelp(PrintStream out)
		{
			out.println("[filename]:");
			out.println("    The script filename.");
			out.println();
			out.println("[switches]:");
			out.println("    --help, -h                   Prints this help.");
			out.println("    --version                    Prints the version of this utility.");
			out.println("    --function-help              Prints all available function usages.");
			out.println("    --disassemble                Prints the disassembly for this script");
			out.println("                                     and exits.");
			out.println("    --entry [name]               Use a different entry point named [name].");
			out.println("                                     Default: \"main\"");
			out.println("    --runaway-limit [num]        Sets the runaway limit (in operations)");
			out.println("                                     before the soft protection on infinite");
			out.println("                                     loops triggers. 0 is no limit.");
			out.println("                                     Default: 0");
			out.println("    --activation-depth [num]     Sets the activation depth to [num].");
			out.println("                                     Default: 256");
			out.println("    --stack-depth [num]          Sets the stack value depth to [num].");
			out.println("                                     Default: 2048");
			out.println("    --                           All tokens after this one are interpreted");
			out.println("                                     literally as args for the script.");
			out.println("                                     Normally, all unrecognized switches");
			out.println("                                     become arguments to the script. This");
			out.println("                                     forces the alternate interpretation.");
			out.println("    --X                          Bash script special: [DEPRECATED]");
			out.println("                                     First argument after this is the script");
			out.println("                                     file, and every argument after are args");
			out.println("                                     to pass to the script.");
		}

		private void printFunctionUsages(UsageRendererType renderer, String sectionName, String namespace, ScriptFunctionResolver resolver)
		{
			renderer.renderSection(sectionName);
			for (ScriptFunctionType sft : resolver.getFunctions())
				renderer.renderUsage(namespace, sft.name(), sft.getUsage());
			options.stdout.println();
		}

		private void printFunctionHelp(UsageRendererType renderer)
		{
			renderer.startRender();
			for (int i = 0; i < RESOLVERS.length; i++)
				printFunctionUsages(renderer, RESOLVERS[i].sectionName, RESOLVERS[i].namespace, RESOLVERS[i].resolver);		
			renderer.finishRender();
		}
		
	}
	
	/**
	 * Reads command line arguments and sets options.
	 * @param out the standard output print stream.
	 * @param err the standard error print stream. 
	 * @param args the argument args.
	 * @return the parsed options.
	 * @throws OptionParseException if a parse exception occurs.
	 */
	public static Options options(PrintStream out, PrintStream err, String ... args) throws OptionParseException
	{
		Options options = new Options();
		options.stdout = out;
		options.stderr = err;
		
		final int STATE_START = 0;
		final int STATE_ARGS = 1;
		final int STATE_BASH_FILE = 2;
		final int SWITCHES = 10;
		final int STATE_SWITCHES_ENTRY = SWITCHES + 0;
		final int STATE_SWITCHES_ACTIVATION = SWITCHES + 1;
		final int STATE_SWITCHES_STACK = SWITCHES + 2;
		final int STATE_SWITCHES_RUNAWAY = SWITCHES + 3;
		int state = STATE_START;
		
		for (int i = 0; i < args.length; i++)
		{
			String arg = args[i];
			switch (state)
			{
				case STATE_START:
				{
					if (SWITCH_HELP1.equalsIgnoreCase(arg) || SWITCH_HELP2.equalsIgnoreCase(arg))
					{
						options.mode = Mode.HELP;
						return options;
					}
					else if (SWITCH_VERSION1.equalsIgnoreCase(arg))
						options.mode = Mode.VERSION;
					else if (SWITCH_DISASSEMBLE1.equalsIgnoreCase(arg))
						options.mode = Mode.DISASSEMBLE;
					else if (SWITCH_FUNCHELP1.equalsIgnoreCase(arg))
						options.mode = Mode.FUNCTIONHELP;
					else if (SWITCH_FUNCHELP2.equalsIgnoreCase(arg))
						options.mode = Mode.FUNCTIONHELP_MARKDOWN;
					else if (SWITCH_ENTRY1.equalsIgnoreCase(arg))
						state = STATE_SWITCHES_ENTRY;
					else if (SWITCH_RUNAWAYLIMIT1.equalsIgnoreCase(arg))
						state = STATE_SWITCHES_RUNAWAY;
					else if (SWITCH_ACTIVATIONDEPTH1.equalsIgnoreCase(arg))
						state = STATE_SWITCHES_ACTIVATION;
					else if (SWITCH_STACKDEPTH1.equalsIgnoreCase(arg))
						state = STATE_SWITCHES_STACK;
					else if (SWITCH_SEPARATOR.equalsIgnoreCase(arg))
						state = STATE_ARGS;
					else if (SWITCH_SEPARATORBASH.equalsIgnoreCase(arg))
						state = STATE_BASH_FILE;
					else if (options.scriptFile == null)
						options.scriptFile = new File(arg);
					else
						options.argList.add(arg);
				}
				break;
				
				case STATE_SWITCHES_ENTRY:
				{
					arg = arg.trim();
					options.entryPointName = arg.length() > 0 ? arg : null;
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
				
				case STATE_ARGS:
				{
					options.argList.add(arg);
				}
				break;

				case STATE_BASH_FILE:
				{
					options.scriptFile = new File(arg);
					state = STATE_ARGS;
				}
				break;			
			}
		}
		
		if (state == STATE_SWITCHES_ENTRY)
			throw new OptionParseException("ERROR: Expected entry point name after switches.");
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
	
	public static void main(String[] args)
	{
		if (args.length == 0)
		{
			splash(System.out);
			usage(System.out);
			System.exit(-1);
			return;
		}

		try {
			System.exit(call(options(System.out, System.err, args)));
		} catch (OptionParseException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}

	/**
	 * Prints the splash.
	 * @param out the print stream to print to.
	 */
	private static void splash(PrintStream out)
	{
		out.println("WadScript v" + VERSION + " by Matt Tropiano");
		out.println("(using DoomStruct v" + DOOM_VERSION + ", RookScript v" + ROOKSCRIPT_VERSION + ")");
	}

	/**
	 * Prints the usage.
	 * @param out the print stream to print to.
	 */
	private static void usage(PrintStream out)
	{
		out.println("Usage: wadscript [filename] [switches | scriptargs]");
		out.println("                 [--help | -h | --version]");
		out.println("                 [--function-help | --function-help-markdown]");
		out.println("                 [--disassemble] [filename]");
	}
	
}


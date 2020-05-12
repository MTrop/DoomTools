/*******************************************************************************
 * Copyright (c) 2017-2020 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
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
			
			if (namespace != null)
				out.append(namespace + "::");

			out.append("## ").append(functionName).append('(');
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
		HELP,
		FUNCTIONHELP,
		FUNCTIONHELP_MARKDOWN,
		DISASSEMBLE,
		EXECUTE;
	}

	private Mode mode;
	private File scriptFile;
	private String entryPointName;
	private Integer runawayLimit;
	private Integer activationDepth;
	private Integer stackDepth;
	private List<String> argList;
	
	private WadScriptMain()
	{
		this.mode = Mode.EXECUTE;
		this.scriptFile = null;
		this.entryPointName = "main";
		this.runawayLimit = 0;
		this.activationDepth = 256;
		this.stackDepth = 2048;
		this.argList = new LinkedList<>();
	}

	private int execute()
	{
		if (mode == Mode.HELP)
		{
			usage(System.out);
			printHelp(System.out);
			return 0;
		}
		
		if (mode == Mode.FUNCTIONHELP)
		{
			printFunctionHelp(new UsageTextRenderer(System.out));
			return 0;
		}
		
		if (mode == Mode.FUNCTIONHELP_MARKDOWN)
		{
			printFunctionHelp(new UsageMarkdownRenderer(System.out));
			return 0;
		}
		
		if (scriptFile == null)
		{
			System.err.println("ERROR: Bad script file.");
			return 4;
		}
		if (!scriptFile.exists())
		{
			System.err.println("ERROR: Script file does not exist: " + scriptFile);
			return 4;
		}
		if (scriptFile.isDirectory())
		{
			System.err.println("ERROR: Bad script file. Is directory.");
			return 4;
		}
	
		ScriptInstance instance;
		
		try 
		{
			ScriptInstanceBuilder builder = ScriptInstance.createBuilder()
				.withSource(scriptFile)
				.withEnvironment(ScriptEnvironment.createStandardEnvironment())
				.withScriptStack(activationDepth, stackDepth)
				.withRunawayLimit(runawayLimit);

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
				System.err.println("Script ERROR: " + cause.getLocalizedMessage());
				return 8;
			}
			else if (cause != null)
			{
				System.err.println("ERROR: Script could not be started: " + cause.getLocalizedMessage());
				return 8;
			}
			else
			{
				System.err.println("ERROR: " + e.getLocalizedMessage());
				return 9;
			}
		}
		
		if (mode == Mode.DISASSEMBLE)
		{
			System.out.println("Disassembly of \"" + scriptFile + "\":");
			doDisassemble(System.out, instance);
			return 0;
		}
	
		if (mode == Mode.EXECUTE)
		{
			if (entryPointName == null)
			{
				System.err.println("ERROR: Bad entry point.");
				return 4;
			}
			if (activationDepth == null)
			{
				System.err.println("ERROR: Bad activation depth.");
				return 4;
			}
			if (stackDepth == null)
			{
				System.err.println("ERROR: Bad stack depth.");
				return 4;
			}
			if (runawayLimit == null)
			{
				System.err.println("ERROR: Bad runaway limit.");
				return 4;
			}
			
			Script.Entry entryPoint;
			
			if ((entryPoint = instance.getScript().getScriptEntry(entryPointName)) == null)
			{
				System.err.println("ERROR: Entry point not found: " + entryPointName);
				return 5;
			}
			
			Object[] args = new Object[argList.size()];
			argList.toArray(args);
			try {
				ScriptValue retval = ScriptValue.create(null);

				if (entryPoint.getParameterCount() > 0)
					instance.call(entryPointName, new Object[]{args});
				else
					instance.call(entryPointName);

				instance.popStackValue(retval);
				
				if (retval.isError())
				{
					ErrorType error = retval.asObjectType(ErrorType.class);
					System.err.println("ERROR: [" + error.getType() + "]: " + error.getLocalizedMessage());
					return 7;
				}
				return retval.asInt();
			} catch (ScriptExecutionException e) {
				System.err.println("Script ERROR: " + e.getLocalizedMessage());
				return 6;
			} catch (ClassCastException e) {
				System.err.println("Script return ERROR: " + e.getLocalizedMessage());
				return 6;
			}
		}
		
		System.err.println("ERROR: Bad mode - INTERNAL ERROR.");
		return -1;
	}

	private int parseCommandLine(WadScriptMain executor, String[] args)
	{
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
						mode = Mode.HELP;
						return 0;
					}
					else if (SWITCH_DISASSEMBLE1.equalsIgnoreCase(arg))
						mode = Mode.DISASSEMBLE;
					else if (SWITCH_FUNCHELP1.equalsIgnoreCase(arg))
						mode = Mode.FUNCTIONHELP;
					else if (SWITCH_FUNCHELP2.equalsIgnoreCase(arg))
						mode = Mode.FUNCTIONHELP_MARKDOWN;
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
					else if (scriptFile == null)
						scriptFile = new File(arg);
					else
						argList.add(arg);
				}
				break;
				
				case STATE_SWITCHES_ENTRY:
				{
					arg = arg.trim();
					entryPointName = arg.length() > 0 ? arg : null;
				}
				break;
				
				case STATE_SWITCHES_ACTIVATION:
				{
					int n;
					try {
						n = Integer.parseInt(arg);
						activationDepth = n > 0 ? n : null;
					} catch (NumberFormatException e) {
						activationDepth = null;
						return 2;
					}
					state = STATE_START;
				}
				break;
				
				case STATE_SWITCHES_STACK:
				{
					int n;
					try {
						n = Integer.parseInt(arg);
						stackDepth = n > 0 ? n : null;
					} catch (NumberFormatException e) {
						stackDepth = null;
						return 2;
					}
					state = STATE_START;
				}
				break;
				
				case STATE_SWITCHES_RUNAWAY:
				{
					int n;
					try {
						n = Integer.parseInt(arg);
						runawayLimit = n > 0 ? n : null;
					} catch (NumberFormatException e) {
						runawayLimit = null;
						return 2;
					}
					state = STATE_START;
				}
				break;
				
				case STATE_ARGS:
				{
					argList.add(arg);
				}
				break;

				case STATE_BASH_FILE:
				{
					scriptFile = new File(arg);
					state = STATE_ARGS;
				}
				break;			
			}
		}
		
		if (state == STATE_SWITCHES_ENTRY)
		{
			System.err.println("ERROR: Expected entry point name after switches.");
			return 3;
		}
		if (state == STATE_SWITCHES_ACTIVATION)
		{
			System.err.println("ERROR: Expected number after activation depth switch.");
			return 3;
		}
		if (state == STATE_SWITCHES_STACK)
		{
			System.err.println("ERROR: Expected number after stack depth switch.");
			return 3;
		}
		if (state == STATE_SWITCHES_RUNAWAY)
		{
			System.err.println("ERROR: Expected number after runaway limit switch.");
			return 3;
		}
		
		return 0;
	}

	private static void doDisassemble(PrintStream out, ScriptInstance instance)
	{
		StringWriter sw = new StringWriter();
		try {
			ScriptAssembler.disassemble(instance.getScript(), new PrintWriter(sw));
		} catch (IOException e) {
			// Do nothing.
		}
		out.print(sw);
	}

	private static void printHelp(PrintStream out)
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
		out.println("                                     as args for the script.");
		out.println("    --X                          Bash script special: First argument after");
		out.println("                                     this is the script file, and every");
		out.println("                                     argument after are args to pass to the");
		out.println("                                     script.");
	}

	private static void printFunctionUsages(UsageRendererType renderer, String sectionName, String namespace, ScriptFunctionResolver resolver)
	{
		renderer.renderSection(sectionName);
		for (ScriptFunctionType sft : resolver.getFunctions())
			renderer.renderUsage(namespace, sft.name(), sft.getUsage());
		System.out.println();
	}
	
	private static void printFunctionHelp(UsageRendererType renderer)
	{
		renderer.startRender();
		for (int i = 0; i < RESOLVERS.length; i++)
			printFunctionUsages(renderer, RESOLVERS[i].sectionName, RESOLVERS[i].namespace, RESOLVERS[i].resolver);		
		renderer.finishRender();
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
	
	public static void main(String[] args) throws Exception
	{
		if (args.length == 0)
		{
			splash(System.out);
			usage(System.out);
			System.exit(-1);
			return;
		}
		
		WadScriptMain executor = new WadScriptMain();
		int out;
		if ((out = executor.parseCommandLine(executor, args)) > 0)
			System.exit(out);
		else
			System.exit(executor.execute());
	}
	
}


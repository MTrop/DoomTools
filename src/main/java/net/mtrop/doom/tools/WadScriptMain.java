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
import com.blackrook.rookscript.exception.ScriptExecutionException;
import com.blackrook.rookscript.exception.ScriptParseException;
import com.blackrook.rookscript.functions.MathFunctions;
import com.blackrook.rookscript.functions.RegexFunctions;
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

import com.blackrook.rookscript.functions.CommonFunctions;
import com.blackrook.rookscript.functions.DateFunctions;
import com.blackrook.rookscript.functions.DigestFunctions;
import com.blackrook.rookscript.functions.FileSystemFunctions;
import com.blackrook.rookscript.functions.IOFunctions;
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
	
	private enum Mode
	{
		HELP,
		VERSION,
		FUNCTIONHELP,
		FUNCTIONHELP_MARKDOWN,
		DISASSEMBLE,
		EXECUTE
	}
	
	private enum UsageRenderer
	{
		TEXT
		{
			final String NEWLINE_INDENT = "\n            ";
			
			@Override
			protected void renderSection(PrintStream out, String title) 
			{
				out.println("=================================================================");
				out.println("==== " + title);
				out.println("=================================================================");
				out.println();
			}
	
			private void renderTypeUsage(PrintStream out, TypeUsage tu)
			{
				out.append("        (").append(tu.getType() != null 
						? (tu.getType().name() + (tu.getSubType() != null ? ":" + tu.getSubType() : "")) 
						: "ANY"
					).append(") ").println(tu.getDescription().replace("\n", NEWLINE_INDENT));
			}
	
			@Override
			protected void renderUsage(PrintStream out, String namespace, String functionName, Usage usage)
			{
				if (usage == null)
				{
					out.println((namespace != null ? namespace + "::" : "") + functionName + "(...)");
					out.println();
					return;
				}
				
				out.append(namespace != null ? namespace + "::" : "").append(functionName).append('(');
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
						renderTypeUsage(out, tu);
				}
				
				out.append("    ").println("Returns:");
				for (TypeUsage tu : usage.getReturnTypes())
					renderTypeUsage(out, tu);
				out.println();
			}
			
		},
		
		MARKDOWN
		{
			final String NEWLINE_INDENT = "\n        - ";
	
			@Override
			protected void renderSection(PrintStream out, String title) 
			{
				out.append("# ").println(title);
				out.println();
			}
			
			private void renderTypeUsage(PrintStream out, TypeUsage tu)
			{
				out.append("- `").append(tu.getType() != null ? tu.getType().name() : "ANY").append("` ");
				out.append((tu.getSubType() != null ? "*" + tu.getSubType() + "*" : ""));
				out.println();
				out.append("    - ").println(tu.getDescription().replace("\n", NEWLINE_INDENT));
			}
			
			@Override
			protected void renderUsage(PrintStream out, String namespace, String functionName, Usage usage)
			{
				if (usage == null)
				{
					out.println((namespace != null ? namespace + "::" : "") + functionName + "(...)");
					out.println();
					return;
				}
				
				out.append("## ").append(namespace != null ? namespace + "::" : "").append(functionName).append('(');
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
							renderTypeUsage(out, tu);
						out.println();
					}
					out.println();
				}
	
				out.append("**Returns**").println(":");
				out.println();
				for (TypeUsage tu : usage.getReturnTypes())
					renderTypeUsage(out, tu);
				out.println();
				out.println();
			}
			
		}
		
		;
		
		/**
		 * Renders a section break.
		 * @param out the output stream.
		 * @param title the section title.
		 */
		protected abstract void renderSection(PrintStream out, String title);
	
		/**
		 * Renders a single function usage doc.
		 * @param out the output stream.
		 * @param namespace the function namespace.
		 * @param functionName the function name.
		 * @param usage the usage to render (can be null).
		 */
		protected abstract void renderUsage(PrintStream out, String namespace, String functionName, Usage usage);
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
		
		if (mode == Mode.VERSION)
		{
			splash(System.out);
			return 0;
		}
		
		if (mode == Mode.FUNCTIONHELP)
		{
			printFunctionHelp(System.out, UsageRenderer.TEXT);
			return 0;
		}
		
		if (mode == Mode.FUNCTIONHELP_MARKDOWN)
		{
			printFunctionHelp(System.out, UsageRenderer.MARKDOWN);
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
	
		ScriptInstance instance;
		
		try 
		{
			instance = ScriptInstance.createBuilder()
				.withSource(scriptFile)
				.withEnvironment(ScriptEnvironment.createStandardEnvironment())
				.withFunctionResolver(CommonFunctions.createResolver())
					.andFunctionResolver(IOFunctions.createResolver())
					.andFunctionResolver(DateFunctions.createResolver())
					.andFunctionResolver(FileSystemFunctions.createResolver())
					.andFunctionResolver(MathFunctions.createResolver())
					.andFunctionResolver(PrintFunctions.createResolver())
					.andFunctionResolver(RegexFunctions.createResolver())
					.andFunctionResolver(ZipFunctions.createResolver())
					.andFunctionResolver(DigestFunctions.createResolver())
					.andFunctionResolver(WadFunctions.createResolver())
					.andFunctionResolver(PK3Functions.createResolver())
					.andFunctionResolver("map", DoomMapFunctions.createResolver())
					.andFunctionResolver("util", UtilityFunctions.createResolver())
				.withScriptStack(activationDepth, stackDepth)
				.withRunawayLimit(runawayLimit)
				.createInstance();
		} 
		catch (ScriptInstanceBuilder.BuilderException e) 
		{
			Throwable cause = e.getCause();
			if (cause instanceof ScriptParseException)
			{
				System.err.println("Script ERROR: " + cause.getLocalizedMessage());
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
					System.err.println("ERROR: " + retval);
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
					else
						scriptFile = new File(arg);
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
		out.println("    --                           Pass parameters as-is after this token");
		out.println("                                     to the script.");
		out.println("    --X                          Bash script special: First argument after");
		out.println("                                     this is the script file, and every");
		out.println("                                     argument after are args to pass to the");
		out.println("                                     script.");
	}

	private static void printFunctionUsages(PrintStream out, UsageRenderer renderer, ScriptFunctionResolver resolver)
	{
		printFunctionUsages(out, renderer, null, resolver);
	}
	
	private static void printFunctionUsages(PrintStream out, UsageRenderer renderer, String namespace, ScriptFunctionResolver resolver)
	{
		for (ScriptFunctionType sft : resolver.getFunctions())
			renderer.renderUsage(out, namespace, sft.name(), sft.getUsage());
		out.println();
	}
	
	private static void printFunctionHelp(PrintStream out, UsageRenderer renderer)
	{
		renderer.renderSection(out, "Common");
		printFunctionUsages(out, renderer, MiscFunctions.createResolver());
		renderer.renderSection(out, "Printing/Logging");
		printFunctionUsages(out, renderer, PrintFunctions.createResolver());
		renderer.renderSection(out, "String");
		printFunctionUsages(out, renderer, StringFunctions.createResolver());
		renderer.renderSection(out, "List / Set");
		printFunctionUsages(out, renderer, ListFunctions.createResolver());
		renderer.renderSection(out, "Map");
		printFunctionUsages(out, renderer, MapFunctions.createResolver());
		renderer.renderSection(out, "Buffer");
		printFunctionUsages(out, renderer, BufferFunctions.createResolver());
		renderer.renderSection(out, "Error");
		printFunctionUsages(out, renderer, ErrorFunctions.createResolver());
		renderer.renderSection(out, "Math");
		printFunctionUsages(out, renderer, MathFunctions.createResolver());
		renderer.renderSection(out, "RegEx");
		printFunctionUsages(out, renderer, RegexFunctions.createResolver());
		renderer.renderSection(out, "File System");
		printFunctionUsages(out, renderer, FileSystemFunctions.createResolver());
		renderer.renderSection(out, "File I/O");
		printFunctionUsages(out, renderer, FileIOFunctions.createResolver());
		renderer.renderSection(out, "Zip Files / GZIP Streams");
		printFunctionUsages(out, renderer, ZipFunctions.createResolver());
		renderer.renderSection(out, "Stream I/O");
		printFunctionUsages(out, renderer, StreamingIOFunctions.createResolver());
		renderer.renderSection(out, "Data I/O");
		printFunctionUsages(out, renderer, DataIOFunctions.createResolver());
		renderer.renderSection(out, "Digest");
		printFunctionUsages(out, renderer, DigestFunctions.createResolver());
		renderer.renderSection(out, "WADs");
		printFunctionUsages(out, renderer, WadFunctions.createResolver());
		renderer.renderSection(out, "PK3s");
		printFunctionUsages(out, renderer, PK3Functions.createResolver());
		renderer.renderSection(out, "Doom / Hexen / ZDoom / UDMF Maps");
		printFunctionUsages(out, renderer, "MAP", DoomMapFunctions.createResolver());
		renderer.renderSection(out, "Utilities");
		printFunctionUsages(out, renderer, "UTIL", UtilityFunctions.createResolver());
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
		out.println("Usage: wscript [filename] [switches] -- [scriptargs]");
		out.println("               [--help | -h | --version]");
		out.println("               [--function-help | --function-help-markdown]");
		out.println("               [--disassemble] [filename]");
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


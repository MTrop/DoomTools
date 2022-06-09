/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import net.mtrop.doom.struct.io.IOUtils;
import net.mtrop.doom.tools.exception.OptionParseException;
import net.mtrop.doom.tools.wadmerge.WadMergeCommand;
import net.mtrop.doom.tools.wadmerge.WadMergeContext;

/**
 * Main class for WadMerge.
 * @author Matthew Tropiano
 */
public final class WadMergeMain
{
	private static final int ERROR_NONE = 0;
	private static final int ERROR_BAD_INPUT_FILE = 1;
	private static final int ERROR_BAD_SCRIPT = 2;
	private static final int ERROR_BAD_OPTIONS = 3;
	private static final int ERROR_UNKNOWN = -1;

	public static final String SWITCH_HELP = "--help";
	public static final String SWITCH_HELP2 = "-h";
	public static final String SWITCH_VERBOSE = "--verbose";
	public static final String SWITCH_VERBOSE2 = "-v";
	public static final String SWITCH_VERSION = "--version";

	public static final String SWITCH_SYSTEMIN = "--";
	
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
		private boolean verbose;
		private boolean useStdin;
		private File inputFile;
		private List<String> args;
		
		private Options()
		{
			this.stdout = null;
			this.stderr = null;
			this.stdin = null;
			this.help = false;
			this.version = false;
			this.verbose = false;
			this.useStdin = false;
			this.inputFile = new File("wadmerge.txt");
			this.args = new LinkedList<>();
		}

		public Options setStdout(OutputStream out) 
		{
			this.stdout = new PrintStream(out, true);
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

		public Options setVerbose(boolean verbose) 
		{
			this.verbose = verbose;
			return this;
		}
		
		public Options setUseStdin(boolean useStdin) 
		{
			this.useStdin = useStdin;
			return this;
		}

		public Options setInputFile(File inputFile) 
		{
			this.inputFile = inputFile;
			return this;
		}
		
		public Options addArg(String arg)
		{
			this.args.add(arg);
			return this;
		}
		
	}
	
	/**
	 * Utility context.
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
				splash(System.out);
				return ERROR_NONE;
			}
			
			String streamName;
			BufferedReader reader;
			if (options.useStdin)
			{
				streamName = "STDIN";
				reader = new BufferedReader(new InputStreamReader(options.stdin));
			}
			else
			{
				try
				{
					reader = new BufferedReader(new InputStreamReader(new FileInputStream(options.inputFile)));
					streamName = options.inputFile.getPath();
				}
				catch (FileNotFoundException e)
				{
					options.stderr.printf("ERROR: File %s not found.\n", options.inputFile.getPath());
					return ERROR_BAD_INPUT_FILE;
				}
				catch (SecurityException e)
				{
					options.stderr.printf("ERROR: File %s not readable (access denied).\n", options.inputFile.getPath());
					return ERROR_BAD_INPUT_FILE;
				}
			}
		
			try 
			{
				if (!WadMergeCommand.callScript(streamName, reader, new WadMergeContext(options.stdout, options.verbose), options.args.toArray(new String[options.args.size()])))
					return ERROR_BAD_SCRIPT;
			}
			catch (IOException e)
			{
				options.stderr.printf("ERROR: File %s not found.\n", options.inputFile.getPath());
				return ERROR_BAD_INPUT_FILE;
			}
			finally
			{
				IOUtils.close(reader);
			}
			
			return ERROR_NONE;
		}

	}
	
	/**
	 * Reads command line arguments and sets options.
	 * @param out the standard output print stream.
	 * @param err the standard error print stream. 
	 * @param in the standard input buffered reader.
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
		
		boolean sawInput = false;
		int i = 0;
		while (i < args.length)
		{
			String arg = args[i];
			
			if (arg.equals(SWITCH_HELP) || arg.equals(SWITCH_HELP2))
				options.help = true;
			else if (arg.equals(SWITCH_VERBOSE) || arg.equals(SWITCH_VERBOSE2))
				options.verbose = true;
			else if (arg.equals(SWITCH_VERSION))
				options.version = true;
			else if (arg.equals(SWITCH_SYSTEMIN))
				options.useStdin = true;
			else if (!sawInput)
			{
				options.inputFile = new File(arg);
				sawInput = true;
			}
			else
				options.args.add(arg);
			i++;
		}
		
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
	
	public static void main(String[] args)
	{
		try {
			Options options = options(System.out, System.err, System.in, args);
			int status = call(options);
			if (status == ERROR_BAD_INPUT_FILE && args.length == 0)
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
		out.println("WadMerge v" + Version.WADMERGE + " by Matt Tropiano (using DoomStruct v" + Version.DOOMSTRUCT + ")");
	}

	/**
	 * Prints the usage.
	 * @param out the print stream to print to.
	 */
	private static void usage(PrintStream out)
	{
		out.println("Usage: wadmerge [--help | -h | --version]");
		out.println("                [switches] [scriptfile] [arguments]");
	}
	
	/**
	 * Prints the help.
	 * @param out the print stream to print to.
	 */
	private static void help(PrintStream out)
	{
		out.println("    --help        Prints help and exits.");
		out.println("    -h");
		out.println();
		out.println("    --version     Prints version, and exits.");
		out.println();
		out.println("[switches]:");
		out.println("    --verbose     Prints verbose output.");
		out.println("    -v");
		out.println();
		out.println("[scriptfile]:");
		out.println("    <filename>    The input script file.");
		out.println();
		out.println("    --            Script input is from Standard In, not a file.");
		out.println();
		out.println("    If a file is not specified, ./wadmerge.txt is the default file.");
		out.println("    The parent directory of the provided script becomes the working directory.");
		out.println();
		out.println("[arguments]:");
		out.println("    The arguments to pass to the script. Script arguments are injected into");
		out.println("    commands by just adding \"$\" plus the argument index. (e.g. $0) A");
		out.println("    doubled-up $ ($$) is interpreted as one \"$\".");
		out.println();
		out.println("    The argument index references can be in any part of the parsed tokens, not");
		out.println("    just by themselves as a singular token. They are parsed at the token level,");
		out.println("    not the line level.");
		out.println();
		out.println("    Arguments: [\"apple\", \"banana\", \"doom stuff\"]");
		out.println("    mergedir $1 ./$0/$1phone $2");
		out.println("    Expands to:");
		out.println("    mergedir \"banana\" \"./apple/bananaphone\" \"doom stuff\"");
		out.println();
		out.println("Script Commands");
		out.println("...............");
		out.println();
		for (WadMergeCommand command : WadMergeCommand.values())
		{
			command.help(out);
			out.println();
		}
	}

}

package net.mtrop.doom.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import net.mtrop.doom.struct.io.IOUtils;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.wadmerge.WadMergeCommand;
import net.mtrop.doom.tools.wadmerge.WadMergeContext;

/**
 * Main class for WadMerge.
 * @author Matthew Tropiano
 */
public final class WadMergeMain
{
	private static final String DOOM_VERSION = Common.getVersionString("doom");
	private static final String VERSION = Common.getVersionString("wadmerge");

	private static final int ERROR_NONE = 0;
	private static final int ERROR_BAD_INPUT_FILE = 1;
	private static final int ERROR_BAD_SCRIPT = 2;

	private static final String SWITCH_HELP = "--help";
	private static final String SWITCH_HELP2 = "-h";
	private static final String SWITCH_VERBOSE = "--verbose";
	private static final String SWITCH_VERBOSE2 = "-v";
	private static final String SWITCH_VERSION = "--version";
	private static final String SWITCH_SYSTEMIN = "--";
	
	/**
	 * Program options.
	 */
	private static class Options
	{
		private PrintStream out;
		private PrintStream err;
		private BufferedReader in;
		private boolean help;
		private boolean version;
		private boolean verbose;
		private boolean systemIn;
		private File inputFile;
		
		Options()
		{
			this.help = false;
			this.version = false;
			this.verbose = false;
			this.systemIn = false;
			this.inputFile = new File("wadmerge.txt");
		}
	}
	
	/**
	 * Reads command line arguments and sets options.
	 * @param out the standard output print stream.
	 * @param err the standard error print stream. 
	 * @param in the standard input buffered reader.
	 * @param args the argument args.
	 * @return the parsed options.
	 */
	public static Options options(PrintStream out, PrintStream err, BufferedReader in, String[] args)
	{
		Options options = new Options();
		options.out = out;
		options.err = err;
		options.in = in;
		
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
				options.systemIn = true;
			else
				options.inputFile = new File(arg);
			i++;
		}
		
		return options;
	}
	
	/**
	 * Calls this program.
	 * @param options the program options.
	 * @return the return code.
	 */
	public static int call(Options options)
	{
		if (options.help)
		{
			splash(options.out);
			usage(options.out);
			options.out.println();
			help(options.out);
			options.out.println();
			return ERROR_NONE;
		}
		
		if (options.version)
		{
			splash(System.out);
			return ERROR_NONE;
		}
		
		String streamName;
		BufferedReader reader;
		if (options.systemIn)
		{
			streamName = "STDIN";
			reader = options.in;
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
				options.err.printf("ERROR: File %s not found.\n", options.inputFile.getPath());
				return ERROR_BAD_INPUT_FILE;
			}
			catch (SecurityException e)
			{
				options.err.printf("ERROR: File %s not readable (access denied).\n", options.inputFile.getPath());
				return ERROR_BAD_INPUT_FILE;
			}
		}
	
		try 
		{
			if (!WadMergeCommand.callScript(streamName, reader, new WadMergeContext(options.out, options.verbose)))
				return ERROR_BAD_SCRIPT;
		}
		catch (IOException e)
		{
			options.err.printf("ERROR: File %s not found.\n", options.inputFile.getPath());
			return ERROR_BAD_INPUT_FILE;
		}
		finally
		{
			IOUtils.close(reader);
		}
		
		return ERROR_NONE;
	}

	public static void main(String[] args)
	{
		System.exit(call(options(System.out, System.err, new BufferedReader(new InputStreamReader(System.in)), args)));
	}

	/**
	 * Prints the splash.
	 * @param out the print stream to print to.
	 */
	private static void splash(PrintStream out)
	{
		out.println("WadMerge v" + VERSION + " by Matt Tropiano (using DoomStruct v" + DOOM_VERSION + ")");
	}

	/**
	 * Prints the usage.
	 * @param out the print stream to print to.
	 */
	private static void usage(PrintStream out)
	{
		out.println("Usage: wadmerge [--help | -h | --version] [switches] [scriptfile]");
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

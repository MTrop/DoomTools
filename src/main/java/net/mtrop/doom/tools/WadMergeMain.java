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
		boolean help;
		boolean version;
		boolean verbose;
		boolean systemIn;
		File inputFile;
		
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
	 * @param args the argument args.
	 */
	private static Options options(String[] args)
	{
		Options out = new Options();
		
		int i = 0;
		while (i < args.length)
		{
			String arg = args[i];
			
			if (arg.equals(SWITCH_HELP) || arg.equals(SWITCH_HELP2))
				out.help = true;
			else if (arg.equals(SWITCH_VERBOSE) || arg.equals(SWITCH_VERBOSE2))
				out.verbose = true;
			else if (arg.equals(SWITCH_VERSION))
				out.version = true;
			else if (arg.equals(SWITCH_SYSTEMIN))
				out.systemIn = true;
			else
				out.inputFile = new File(arg);
			i++;
		}
		
		return out;
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
	
	public static void main(String[] args)
	{
		Options options = options(args);
		
		if (options.help)
		{
			splash(System.out);
			usage(System.out);
			System.out.println();
			help(System.out);
			System.out.println();
			System.exit(ERROR_NONE);
		}
		
		if (options.version)
		{
			splash(System.out);
			System.exit(ERROR_NONE);
		}
		
		String streamName;
		BufferedReader reader;
		if (options.systemIn)
		{
			streamName = "STDIN";
			reader = new BufferedReader(new InputStreamReader(System.in));
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
				System.out.printf("ERROR: File %s not found.\n", options.inputFile.getPath());
				System.exit(ERROR_BAD_INPUT_FILE);
				return;
			}
			catch (SecurityException e)
			{
				System.out.printf("ERROR: File %s not readable (access denied).\n", options.inputFile.getPath());
				System.exit(ERROR_BAD_INPUT_FILE);
				return;
			}
		}

		try 
		{
			if (!WadMergeCommand.callScript(streamName, reader, new WadMergeContext(System.out, options.verbose)))
				System.exit(ERROR_BAD_SCRIPT);
		}
		catch (IOException e)
		{
			System.out.printf("ERROR: File %s not found.\n", options.inputFile.getPath());
			System.exit(ERROR_BAD_INPUT_FILE);
		}
		finally
		{
			IOUtils.close(reader);
		}
		
		System.exit(ERROR_NONE);
	}

}

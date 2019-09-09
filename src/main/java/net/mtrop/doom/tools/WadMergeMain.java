package net.mtrop.doom.tools;

import java.io.File;
import java.io.PrintStream;

/**
 * Main class for WadMerge.
 * @author Matthew Tropiano
 */
public final class WadMergeMain
{
	private static final String VERSION = "1.0";

	private static final int ERROR_NONE = 0;
	private static final int ERROR_BAD_SWITCH = 1;
	private static final int ERROR_BAD_INPUT_FILE = 2;

	private static final String SWITCH_HELP = "--help";
	private static final String SWITCH_VERBOSE = "--verbose";
	private static final String SWITCH_VERBOSE2 = "-v";
	private static final String SWITCH_SYSTEMIN = "--";
	
	private static class Options
	{
		boolean help;
		boolean verbose;
		boolean systemIn;
		File inputFile;
		
		Options()
		{
			this.help = false;
			this.verbose = false;
			this.systemIn = false;
			this.inputFile = new File("wadmerge.txt");
		}
	}
	
	/**
	 * Reads command line arguments and sets options.
	 * @param args the argument args.
	 */
	public static Options readSwitches(String[] args)
	{
		Options out = new Options();
		
		int i = 0;
		while (i < args.length)
		{
			String arg = args[i];
			
			if (arg.equals(SWITCH_HELP))
				out.help = true;
			else if (arg.equals(SWITCH_VERBOSE) || arg.equals(SWITCH_VERBOSE2))
				out.verbose = true;
			else if (arg.equals(SWITCH_SYSTEMIN))
				out.inputFile = null;
			
			i++;
		}
		
		return out;
	}
	
	public static void printSplash(PrintStream out)
	{
		// TODO: Finish.
	}

	public static void printUsage(PrintStream out)
	{
		// TODO: Finish.
	}
	
	public static void main(String[] args)
	{
		// TODO: Finish this.
		System.exit(0);
	}

}

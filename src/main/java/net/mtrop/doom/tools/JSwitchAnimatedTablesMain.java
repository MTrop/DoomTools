package net.mtrop.doom.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import net.mtrop.doom.WadFile;
import net.mtrop.doom.io.IOUtils;
import net.mtrop.doom.texture.Animated;
import net.mtrop.doom.texture.Switches;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.common.ParseException;

/**
 * Main class for JSwantbls.
 * @author Matthew Tropiano
 */
public final class JSwitchAnimatedTablesMain
{
	private static final String VERSION = "1.0";

	private static final int ERROR_NONE = 0;
	private static final int ERROR_BAD_INPUT_FILE = 1;
	private static final int ERROR_BAD_PARSE = 2;
	private static final int ERROR_MISSING_DATA = 3;

	private static final String SWITCH_HELP = "--help";
	private static final String SWITCH_HELP2 = "-h";
	private static final String SWITCH_VERBOSE = "--verbose";
	private static final String SWITCH_VERBOSE2 = "-v";
	private static final String SWITCH_VERSION = "--version";
	private static final String SWITCH_EXPORT = "--export";
	private static final String SWITCH_EXPORT2 = "-x";
	private static final String SWITCH_IMPORT = "--import";
	private static final String SWITCH_IMPORT2 = "-i";

	/**
	 * Program options.
	 */
	private static class Options
	{
		boolean help;
		boolean version;
		boolean verbose;
		Boolean exportMode;
		File sourceFile;
		File wadFile;
		
		Options()
		{
			this.help = false;
			this.version = false;
			this.verbose = false;
			this.exportMode = null;
			this.sourceFile = null;
			this.wadFile = null;
		}
	}
	
	/**
	 * Reads command line arguments and sets options.
	 * @param args the argument args.
	 */
	private static Options options(String[] args)
	{
		Options out = new Options();
		final int STATE_START = 0;
		final int STATE_IMPORTEXPORT = 1;
		int state = STATE_START;
		
		int i = 0;
		while (i < args.length)
		{
			String arg = args[i];
			switch (state)
			{
				case STATE_START:
				{
					if (arg.equals(SWITCH_HELP) || arg.equals(SWITCH_HELP2))
						out.help = true;
					else if (arg.equals(SWITCH_VERBOSE) || arg.equals(SWITCH_VERBOSE2))
						out.verbose = true;
					else if (arg.equals(SWITCH_VERSION))
						out.version = true;
					else if (arg.equals(SWITCH_EXPORT) || arg.equals(SWITCH_EXPORT2))
					{
						state = STATE_IMPORTEXPORT;
						out.exportMode = true;
					}
					else if (arg.equals(SWITCH_IMPORT) || arg.equals(SWITCH_IMPORT2))
					{
						state = STATE_IMPORTEXPORT;
						out.exportMode = false;
					}
					else
						out.wadFile = new File(arg);
				}
				break;

				case STATE_IMPORTEXPORT:
				{
					out.sourceFile = new File(arg);
					state = STATE_START;
				}
				break;
			}
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
		out.println("jswantbls v" + VERSION + " by Matt Tropiano");
	}

	/**
	 * Prints the usage.
	 * @param out the print stream to print to.
	 */
	private static void usage(PrintStream out)
	{
		out.println("Usage: jswantbls [--help | -h | --version] [file] [switches]");
	}
	
	/**
	 * Prints the help.
	 * @param out the print stream to print to.
	 */
	private static void help(PrintStream out)
	{
		out.println("    --help              Prints help and exits.");
		out.println("    -h");
		out.println();
		out.println("    --version           Prints version, and exits.");
		out.println();
		out.println("[file]:");
		out.println("    <filename>          The input WAD file.");
		out.println();
		out.println("[switches]:");
		out.println("    --verbose           Prints verbose output.");
		out.println("    -v");
		out.println();
	    out.println("    --export [srcfile]  Export mode.");
	    out.println("    -x [srcfile]        [file] is WAD, exports ANIMATED and SWITCHES to [srcfile].");
		out.println();
	    out.println("    --import [srcfile]  Import mode.");
	    out.println("    -i [srcfile]        [file] is WAD, creates ANIMATED and SWITCHES from [srcfile].");
		out.println();
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
		
		if (options.wadFile == null)
		{
			System.out.println("ERROR: No WAD file specified.");
			usage(System.out);
			System.exit(ERROR_MISSING_DATA);
			return;
		}

		if (options.exportMode == null)
		{
			System.out.println("ERROR: Import or export mode not specified.");
			usage(System.out);
			System.exit(ERROR_MISSING_DATA);
			return;
		}

		if (options.sourceFile == null)
		{
			System.out.println("ERROR: No source file specified.");
			usage(System.out);
			System.exit(ERROR_MISSING_DATA);
			return;
		}

		String streamName;
		BufferedReader reader;
		try
		{
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(options.sourceFile)));
			streamName = options.sourceFile.getPath();
		}
		catch (FileNotFoundException e)
		{
			System.out.printf("ERROR: File %s not found.\n", options.sourceFile.getPath());
			System.exit(ERROR_BAD_INPUT_FILE);
			return;
		}
		catch (SecurityException e)
		{
			System.out.printf("ERROR: File %s not readable (access denied).\n", options.sourceFile.getPath());
			System.exit(ERROR_BAD_INPUT_FILE);
			return;
		}

		WadFile wad = null;
		try 
		{
			wad = new WadFile(options.wadFile);
		}
		catch (FileNotFoundException e)
		{
			System.out.printf("ERROR: File %s not found.\n", options.sourceFile.getPath());
			System.exit(ERROR_BAD_INPUT_FILE);
			IOUtils.close(reader);
			return;
		}
		catch (IOException e)
		{
			System.out.printf("ERROR: %s.\n", e.getLocalizedMessage());
			System.exit(ERROR_BAD_INPUT_FILE);
			IOUtils.close(reader);
			return;
		}
		catch (SecurityException e)
		{
			System.out.printf("ERROR: File %s not readable (access denied).\n", options.sourceFile.getPath());
			System.exit(ERROR_BAD_INPUT_FILE);
			IOUtils.close(reader);
			return;
		}
		
		try
		{
			Animated animated;
			boolean replaceAnimated = true;
			if ((animated = wad.getDataAs("ANIMATED", Animated.class)) == null)
			{
				animated = new Animated();
				replaceAnimated = false;
			}
			
			Switches switches;
			boolean replaceSwitches = true;
			if ((switches = wad.getDataAs("SWITCHES", Switches.class)) == null)
			{
				switches = new Switches();
				replaceSwitches = false;
			}

			if (options.exportMode)
			{
				// TODO: Finish this.
			}
			else // import mode
			{
				Common.parseSwitchAnimatedTables(reader, animated, switches);

				if (replaceAnimated)
				{
					wad.replaceEntry(wad.indexOf("ANIMATED"), animated.toBytes());
					if (options.verbose)
						System.out.printf("Replaced `ANIMATED` in `%s`.\n", options.wadFile.getPath());
				}
				else
				{
					wad.addData("ANIMATED", animated);
					if (options.verbose)
						System.out.printf("Added `ANIMATED` to `%s`.\n", options.wadFile.getPath());
				}
				
				if (replaceSwitches)
				{
					wad.replaceEntry(wad.indexOf("SWITCHES"), switches.toBytes());
					if (options.verbose)
						System.out.printf("Replaced `SWITCHES` in `%s`.\n", options.wadFile.getPath());
				}
				else
				{
					wad.addData("SWITCHES", switches);
					if (options.verbose)
						System.out.printf("Added `SWITCHES` to `%s`.\n", options.wadFile.getPath());
				}
			}
		}
		catch (IOException e)
		{
			System.out.printf("ERROR: %s\n", e.getLocalizedMessage());
			System.exit(ERROR_BAD_INPUT_FILE);
		}
		catch (ParseException e)
		{
			System.out.printf("ERROR: %s, %s\n", streamName, e.getLocalizedMessage());
			System.exit(ERROR_BAD_PARSE);
		}
		finally
		{
			IOUtils.close(reader);
			IOUtils.close(wad);
		}
		
		System.exit(ERROR_NONE);
	}

}

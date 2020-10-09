package net.mtrop.doom.tools;

import java.io.IOException;
import java.io.PrintStream;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.exception.OptionParseException;

/**
 * Main class for DECOHack.
 * Special thanks to Esselfortium and Exl (for WhackEd4)
 * @author Matthew Tropiano
 */
public final class DecoHackMain 
{
	private static final String DOOM_VERSION = Common.getVersionString("doom");
	private static final String VERSION = Common.getVersionString("decohack");
	private static final String SPLASH_VERSION = "DECOHack v" + VERSION + " by Matt Tropiano (using DoomStruct v" + DOOM_VERSION + ")";

	private static final int ERROR_NONE = 0;
	private static final int ERROR_BAD_OPTIONS = 1;

	private static final String SWITCH_HELP = "--help";
	private static final String SWITCH_HELP2 = "-h";
	private static final String SWITCH_VERSION = "--version";
		
	/**
	 * Program options.
	 */
	public static class Options
	{
		private PrintStream stdout;
		private PrintStream stderr;
		private boolean help;
		private boolean version;
		
		public Options()
		{
			this.stdout = null;
			this.stderr = null;
			this.help = false;
			this.version = false;
		}
		
		public Options setHelp(boolean help) 
		{
			this.help = help;
			return this;
		}
		
		public Options setVersion(boolean version) 
		{
			this.version = version;
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
		
		private String fileExtension(byte[] bytes)
		{
			// TODO: Finish
			return ".lmp";
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
		
			// TODO: Finish.
			
			return ERROR_NONE;
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
	
		int i = 0;
		while (i < args.length)
		{
			String arg = args[i];
			if (arg.equals(SWITCH_HELP) || arg.equals(SWITCH_HELP2))
				options.setHelp(true);
			else if (arg.equals(SWITCH_VERSION))
				options.setVersion(true);
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
		return (new Context(options)).call();
	}
	
	public static void main(String[] args) throws IOException
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
			System.exit(ERROR_BAD_OPTIONS);
		}
	}
	
	/**
	 * Prints the splash.
	 * @param out the print stream to print to.
	 */
	private static void splash(PrintStream out)
	{
		out.println(SPLASH_VERSION);
	}
	
	/**
	 * Prints the usage.
	 * @param out the print stream to print to.
	 */
	private static void usage(PrintStream out)
	{
		out.println("Usage: decohack [--help | -h | --version]");
		out.println("                [files] [switches]");
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
	}

}

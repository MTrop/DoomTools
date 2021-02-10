package net.mtrop.doom.tools;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.exception.OptionParseException;

/**
 * Main class for Utility.
 * @author Matthew Tropiano
 */
public final class DoomImageConvertMain 
{
	private static final String DOOM_VERSION = Common.getVersionString("doom");
	private static final String VERSION = Common.getVersionString("dimgconv");
	private static final String SPLASH_VERSION = "DImgConv v" + VERSION + " by Matt Tropiano (using DoomStruct v" + DOOM_VERSION + ")";

	private static final int ERROR_NONE = 0;
	private static final int ERROR_BAD_OPTIONS = 1;

	private static final String SWITCH_HELP = "--help";
	private static final String SWITCH_HELP2 = "-h";
	private static final String SWITCH_VERSION = "--version";
		
	public enum Mode
	{
		PALETTE,
		COLORMAP,
		GRAPHIC,
		FLAT;
	}
	
	/**
	 * Program options.
	 */
	public static class Options
	{
		private PrintStream stdout;
		private PrintStream stderr;
		private boolean help;
		private boolean version;
		
		// Palette source for conversion.
		private File paletteSourcePath;
		
		// Source path.
		private File sourcePath;
		private boolean recursive;
		
		// Destination path.
		private File outputPath;
		private boolean outputIsWad;

		// Filename for meta info.
		private String metaInfoFilename;
		
		public Options()
		{
			this.stdout = null;
			this.stderr = null;
			this.help = false;
			this.version = false;
			
			this.sourcePath = null;
			this.recursive = false;
			this.outputPath = null;
			this.outputIsWad = false;
			this.paletteSourcePath = null;
			this.metaInfoFilename = "dimgconv.txt";
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

		// TODO: Finish this.
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
				options.help = true;
			else if (arg.equals(SWITCH_VERSION))
				options.version = true;
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
		out.println("Usage: dimgconv [--help | -h | --version]");
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

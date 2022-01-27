/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.decohack.DecoHackJoiner;
import net.mtrop.doom.tools.decohack.DecoHackParser;
import net.mtrop.doom.tools.decohack.contexts.AbstractPatchContext;
import net.mtrop.doom.tools.decohack.data.enums.DEHFeatureLevel;
import net.mtrop.doom.tools.decohack.exception.DecoHackParseException;
import net.mtrop.doom.tools.exception.OptionParseException;
import net.mtrop.doom.tools.struct.PreprocessorLexer.PreprocessorException;

/**
 * Main class for DECOHack.
 * Special thanks to Esselfortium and Exl (for WhackEd4)
 * @author Matthew Tropiano
 */
public final class DecoHackMain 
{
	private static final String VERSION_LINE = "DECOHack v" + Version.DECOHACK + " by Matt Tropiano";
	private static final String SPLASH_VERSION = VERSION_LINE + " (using DoomStruct v" + Version.DOOMSTRUCT + ")";

	private static final Charset ASCII = Charset.forName("ASCII");
	
	private static final String DEFAULT_OUTFILENAME = "dehacked.deh";
	private static final String RESOURCE_HELP_CONSTANTS = "decohack/help-constants.txt";
	
	private static final int ERROR_NONE = 0;
	private static final int ERROR_BAD_OPTIONS = 1;
	private static final int ERROR_MISSING_INPUT = 2;
	private static final int ERROR_MISSING_INPUT_FILE = 3;
	private static final int ERROR_IOERROR = 4;
	private static final int ERROR_SECURITY = 5;
	private static final int ERROR_PARSEERROR = 6;
	private static final int ERROR_MISSING_RESOURCE = 7;

	private static final String SWITCH_HELP = "--help";
	private static final String SWITCH_HELP2 = "-h";
	private static final String SWITCH_HELPFULL = "--help-full";
	private static final String SWITCH_VERSION = "--version";

	private static final String SWITCH_DUMPCONSTANTS = "--dump-constants";
	private static final String SWITCH_DUMPRESOURCE = "--dump-resource";
	
	private static final String SWITCH_OUTPUT = "--output";
	private static final String SWITCH_OUTPUT2 = "-o";
	private static final String SWITCH_OUTPUTCHARSET = "--output-charset";
	private static final String SWITCH_OUTPUTCHARSET2 = "-oc";
	private static final String SWITCH_BUDGET = "--budget";
	private static final String SWITCH_BUDGET2 = "-b";
	private static final String SWITCH_SOURCE_OUTPUT = "--source-output";
	private static final String SWITCH_SOURCE_OUTPUT2 = "-s";

	/**
	 * Program options.
	 */
	public static class Options
	{
		private PrintStream stdout;
		private PrintStream stderr;
		
		private boolean help;
		private boolean full;
		private boolean version;
		private String dumpResource;

		private List<File> inFiles;
		
		private Charset outCharset;
		private File outFile;
		private boolean outputBudget;

		private File outSourceFile;
		
		private Options()
		{
			this.stdout = null;
			this.stderr = null;
			this.help = false;
			this.version = false;

			this.inFiles = new LinkedList<>();
			
			this.outCharset = ASCII;
			this.outFile = null;
			this.outputBudget = false;
			
			this.outSourceFile = null;
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

		public Options setInFile(File inFile) 
		{
			return setInFiles(new File[]{inFile});
		}
		
		public Options setInFiles(File[] inFiles) 
		{
			this.inFiles = Arrays.asList(inFiles);
			return this;
		}
		
		public Options setOutCharsetName(String charsetName) 
		{
			return setOutCharset(Charset.forName(charsetName));
		}
		
		public Options setOutCharset(Charset outCharset) 
		{
			this.outCharset = outCharset;
			return this;
		}
		
		public Options setOutFile(File outFile) 
		{
			this.outFile = outFile;
			return this;
		}
		
		public Options setOutputBudget(boolean outputBudget) 
		{
			this.outputBudget = outputBudget;
			return this;
		}
	
		public Options setOutSourceFile(File outSourceFile) 
		{
			this.outSourceFile = outSourceFile;
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
		
		public int call()
		{
			if (options.help)
			{
				splash(options.stdout);
				usage(options.stdout);
				options.stdout.println();
				help(options.stdout, options.full);
				return ERROR_NONE;
			}
			
			if (options.version)
			{
				splash(options.stdout);
				return ERROR_NONE;
			}
			
			if (options.dumpResource != null)
			{
				if (!options.dumpResource.startsWith("decohack/"))
				{
					options.stderr.println("ERROR: Bad resource path (must start with \"decohack/\").");
					return ERROR_MISSING_RESOURCE;
				}
				
				InputStream resIn = Common.openResource(options.dumpResource);
				if (resIn == null)
				{
					options.stderr.printf("ERROR: Bad resource path (%s not found).\n", options.dumpResource);
					return ERROR_MISSING_RESOURCE;
				}
				
				try (BufferedReader br = new BufferedReader(new InputStreamReader(resIn))) {
					String line;
					while ((line = br.readLine()) != null)
						options.stdout.println(line);
				} catch (IOException e) {
					options.stderr.printf("I/O ERROR: %s.\n", e.getLocalizedMessage());
					return ERROR_IOERROR;
				}
				return ERROR_NONE;
			}

			if (options.inFiles.isEmpty())
			{
				options.stderr.println("ERROR: Missing input file.");
				return ERROR_MISSING_INPUT;
			}

			for (File f : options.inFiles)
			{
				if (!f.exists())
				{
					options.stderr.println("ERROR: Input file `" + f.getPath() + "` does not exist.");
					return ERROR_MISSING_INPUT_FILE;
				}
			}

			if (options.outFile == null)
			{
				options.stdout.printf("NOTE: Output file not specified, defaulting to %s.\n", DEFAULT_OUTFILENAME);
				options.outFile = new File(DEFAULT_OUTFILENAME);
			}

			// Read script.
			AbstractPatchContext<?> context;
			try 
			{
				context = DecoHackParser.read(options.inFiles);
			} 
			catch (PreprocessorException e) 
			{
				options.stderr.println("ERROR: " + e.getLocalizedMessage());
				return ERROR_PARSEERROR;
			} 
			catch (DecoHackParseException e) 
			{
				options.stderr.println("ERROR: " + e.getLocalizedMessage());
				return ERROR_PARSEERROR;
			} 
			catch (FileNotFoundException e) 
			{
				options.stderr.println("ERROR: Input file does not exist.");
				return ERROR_MISSING_INPUT_FILE;
			} 
			catch (IOException e) 
			{
				options.stderr.println("ERROR: I/O Error: " + e.getLocalizedMessage());
				return ERROR_IOERROR;
			} 
			catch (SecurityException e) 
			{
				options.stderr.println("ERROR: Could not open input file (access denied).");
				return ERROR_SECURITY;
			}
			
			// warn export if [Ultimate] Doom 1.9 and last state is replaced.
			if (context.getSupportedFeatureLevel() == DEHFeatureLevel.DOOM19
				&& ! (context.getState(context.getStateCount() - 1).equals(context.getSourcePatch().getState(context.getStateCount() - 1))
			))
			{
				options.stdout.println("WARNING: Final state was replaced in the exported patch - DHE 3.1 may not import this correctly!");
			}
			
			if (options.outputBudget)
			{
				options.stdout.printf("--- Patch State Budget ---\n");
				options.stdout.printf(
					"States: %d used / %d total (%d remaining).\n", 
					context.getStateCount() - context.getFreeStateCount(), 
					context.getStateCount(),
					context.getFreeStateCount()
				);
				if (!context.supports(DEHFeatureLevel.BOOM))
				{
					options.stdout.printf(
						"Action Pointers: %d used / %d total (%d remaining).\n", 
						context.getActionPointerCount() - context.getFreePointerStateCount(), 
						context.getActionPointerCount(),
						context.getFreePointerStateCount()
					);
				}
				options.stdout.printf("--------------------------\n");
			}
			
			// Combine source.
			if (options.outSourceFile != null)
			{
				try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(options.outSourceFile)), true))
				{
					for (File file : options.inFiles)
					{
						DecoHackJoiner.joinSourceFrom(file, Charset.defaultCharset(), writer);
					}
					options.stdout.printf("Wrote source to %s.\n", options.outSourceFile.getPath());
				} 
				catch (FileNotFoundException e) 
				{
					options.stderr.println("ERROR: I/O Error: " + e.getLocalizedMessage());
					return ERROR_IOERROR;
				}
				catch (IOException e)
				{
					options.stderr.println("ERROR: I/O Error: " + e.getLocalizedMessage());
					return ERROR_IOERROR;
				}
			}
			
			// Write Patch.
			try (Writer writer = new OutputStreamWriter(new FileOutputStream(options.outFile), options.outCharset)) 
			{
				context.writePatch(writer, "Created with " + VERSION_LINE);
				options.stdout.printf("Wrote %s.\n", options.outFile.getPath());
			} 
			catch (IOException e) 
			{
				options.stderr.println("ERROR: I/O Error: " + e.getLocalizedMessage());
				return ERROR_IOERROR;
			}
			catch (SecurityException e) 
			{
				options.stderr.println("ERROR: Could not open input file (access denied).");
				return ERROR_SECURITY;
			}
			
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
	
		final int STATE_START = 0;
		final int STATE_OUTFILE = 1;
		final int STATE_OUTCHARSET = 2;
		final int STATE_DUMPRES = 3;
		final int STATE_SOURCEOUTFILE = 4;
		int state = STATE_START;

		for (int i = 0; i < args.length; i++)
		{
			String arg = args[i];
			
			switch (state)
			{
				case STATE_START:
				{
					if (arg.equals(SWITCH_HELP) || arg.equals(SWITCH_HELP2))
						options.help = true;
					if (arg.equals(SWITCH_HELPFULL))
					{
						options.help = true;
						options.full = true;
					}
					else if (arg.equals(SWITCH_VERSION))
						options.version = true;
					else if (arg.equals(SWITCH_DUMPCONSTANTS))
						options.dumpResource = RESOURCE_HELP_CONSTANTS;
					else if (arg.equals(SWITCH_DUMPRESOURCE))
						state = STATE_DUMPRES;
					else if (arg.equals(SWITCH_BUDGET) || arg.equals(SWITCH_BUDGET2))
						options.outputBudget = true;
					else if (arg.equals(SWITCH_OUTPUT) || arg.equals(SWITCH_OUTPUT2))
						state = STATE_OUTFILE;
					else if (arg.equals(SWITCH_SOURCE_OUTPUT) || arg.equals(SWITCH_SOURCE_OUTPUT2))
						state = STATE_SOURCEOUTFILE;
					else if (arg.equals(SWITCH_OUTPUTCHARSET) || arg.equals(SWITCH_OUTPUTCHARSET2))
						state = STATE_OUTCHARSET;
					else
						options.inFiles.add(new File(arg));
				}
				break;
				
				case STATE_OUTFILE:
				{
					options.outFile = new File(arg);
					state = STATE_START;
				}
				break;

				case STATE_DUMPRES:
				{
					options.dumpResource = arg;
					state = STATE_START;
				}
				break;

				case STATE_OUTCHARSET:
				{
					try {
						options.outCharset = Charset.forName(arg);
					} catch (IllegalCharsetNameException e) {
						throw new OptionParseException("ERROR: Bad charset name: " + arg);
					} catch (UnsupportedCharsetException e) {
						throw new OptionParseException("ERROR: Unsupported charset name: " + arg);
					}
					state = STATE_START;
				}
				break;
				
				case STATE_SOURCEOUTFILE:
				{
					options.outSourceFile = new File(arg);
					state = STATE_START;
				}
				break;
			}
		}
		
		if (state == STATE_OUTFILE)
			throw new OptionParseException("ERROR: Expected output file.");
		if (state == STATE_OUTCHARSET)
			throw new OptionParseException("ERROR: Expected output charset name.");
		
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
		out.println("Addtional feature support by Xaser Acheron.");
		out.println("With special thanks to Simon \"fraggle\" Howard (DEH9000)");
		out.println("and Dennis \"exl\" Meuwissen (WhackEd).");
	}
	
	/**
	 * Prints the usage.
	 * @param out the print stream to print to.
	 */
	private static void usage(PrintStream out)
	{
		out.println("Usage: decohack [--help | -h | --version]");
		out.println("                --dump-constants");
		out.println("                --dump-resource [path]");
		out.println("                [filename] [switches]");
	}
	
	/**
	 * Prints the help.
	 * @param out the print stream to print to.
	 */
	private static void help(PrintStream out, boolean full)
	{
		out.println("    --help                   Prints help and exits.");
		out.println("    -h");
		out.println();
		out.println("    --help-full              Prints full help (not just usage) and exits.");
		out.println();
		out.println("    --version                Prints version, and exits.");
		out.println();
		out.println("    --dump-constants         Dumps the list of available defined constants");
		out.println("                             to STDOUT.");
		out.println();
		out.println("    --dump-resource [path]   Dumps an internal resource (starting with");
		out.println("                             \"decohack/\" ) to STDOUT.");
		out.println();
		out.println("[filenames]:");
		out.println("    <filename> ...           The input filenames. One or more can be added,");
		out.println("                             parsed in the order specified.");
		out.println();
		out.println("[switches]:");
		out.println("    --output [file]          Outputs the resultant patch to [file].");
		out.println("    -o [file]");
		out.println();
		out.println("    --output-charset [name]  Sets the output charset to [name]. The default");
		out.println("    -oc [name]               charset is ASCII, and there are not many reasons");
		out.println("                             to change.");
		out.println();
		out.println("    --budget                 Prints the state budget after compilation.");
		out.println("    -b");
		out.println();
		out.println("    --source-output [file]   Outputs the combined source to a single file.");
		out.println("    -s [file]");
		out.println();
		if (full)
		{
			try (BufferedReader br = new BufferedReader(new InputStreamReader(Common.openResource("decohack/help.txt")))) {
				String line;
				while ((line = br.readLine()) != null)
					out.println(line);
			} catch (IOException e) {
				/* Do nothing. */
			}
		}
	}

}

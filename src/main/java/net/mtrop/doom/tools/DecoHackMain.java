/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiPredicate;

import net.mtrop.doom.Wad;
import net.mtrop.doom.WadFile;
import net.mtrop.doom.tools.decohack.DecoHackJoiner;
import net.mtrop.doom.tools.decohack.DecoHackParser;
import net.mtrop.doom.tools.decohack.contexts.AbstractPatchContext;
import net.mtrop.doom.tools.decohack.data.DEHActionPointer;
import net.mtrop.doom.tools.decohack.data.DEHActionPointer.Usage;
import net.mtrop.doom.tools.decohack.data.DEHActionPointer.Usage.PointerParameter;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerDoom19;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerMBF;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerMBF21;
import net.mtrop.doom.tools.decohack.data.enums.DEHFeatureLevel;
import net.mtrop.doom.tools.exception.OptionParseException;
import net.mtrop.doom.tools.gui.DoomToolsGUIMain;
import net.mtrop.doom.tools.gui.DoomToolsGUIMain.ApplicationNames;
import net.mtrop.doom.tools.struct.HTMLWriter;
import net.mtrop.doom.tools.struct.HTMLWriter.HTMLStringWriter;
import net.mtrop.doom.tools.struct.PreprocessorLexer.PreprocessorException;
import net.mtrop.doom.tools.struct.util.IOUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

/**
 * Main class for DECOHack.
 * Special thanks to Esselfortium and Exl (for WhackEd4)
 * @author Matthew Tropiano
 */
public final class DecoHackMain 
{
	private static final String VERSION_LINE = "DECOHack v" + Version.DECOHACK + " by Matt Tropiano";
	private static final String SPLASH_VERSION = VERSION_LINE + " (using DoomStruct v" + Version.DOOMSTRUCT + ")";

	private static final String DEFAULT_OUTFILENAME = "dehacked.deh";
	private static final String RESOURCE_HELP_CONSTANTS = "docs/DECOHack Constants.txt";
	
	private static final int ERROR_NONE = 0;
	private static final int ERROR_BAD_OPTIONS = 1;
	private static final int ERROR_MISSING_INPUT = 2;
	private static final int ERROR_MISSING_INPUT_FILE = 3;
	private static final int ERROR_IOERROR = 4;
	private static final int ERROR_SECURITY = 5;
	private static final int ERROR_PARSEERROR = 6;
	private static final int ERROR_MISSING_RESOURCE = 7;
	private static final int ERROR_UNKNOWN = -1;

	public static final String SWITCH_CHANGELOG = "--changelog";
	public static final String SWITCH_GUI = "--gui";
	public static final String SWITCH_HELP = "--help";
	public static final String SWITCH_HELP2 = "-h";
	public static final String SWITCH_HELPFULL = "--help-full";
	public static final String SWITCH_VERSION = "--version";
	public static final String SWITCH_DRYRUN = "--dry-run";

	public static final String SWITCH_CHARSET1 = "--charset";
	public static final String SWITCH_CHARSET2 = "-c";

	public static final String SWITCH_DUMPPOINTERS = "--dump-pointers";
	public static final String SWITCH_DUMPPOINTERS_HTML = "--dump-pointers-html";
	public static final String SWITCH_DUMPCONSTANTS = "--dump-constants";
	public static final String SWITCH_DUMPRESOURCE = "--dump-resource";
	
	public static final String SWITCH_OUTPUT = "--output";
	public static final String SWITCH_OUTPUT2 = "-o";
	public static final String SWITCH_OUTPUTCHARSET = "--output-charset";
	public static final String SWITCH_OUTPUTCHARSET2 = "-oc";
	public static final String SWITCH_BUDGET = "--budget";
	public static final String SWITCH_BUDGET2 = "-b";
	public static final String SWITCH_SOURCE_OUTPUT = "--source-output";
	public static final String SWITCH_SOURCE_OUTPUT2 = "-s";

	public static final String SWITCH_SYSTEMIN = "--";

	/**
	 * Program options.
	 */
	public static class Options
	{
		private PrintStream stdout;
		private PrintStream stderr;
		private InputStream stdin;
		
		private boolean gui;
		private boolean help;
		private boolean full;
		private boolean version;
		private boolean changelog;
		private boolean dumpActionPointers;
		private boolean dumpActionPointersHTML;
		private String dumpResource;
		private boolean dryRun;

		private boolean useStdin;
		private List<File> inFiles;
		private Charset inCharset;
		
		private Charset outCharset;
		private File outFile;
		private boolean outputBudget;

		private File outSourceFile;
		
		private Options()
		{
			this.stdout = null;
			this.stderr = null;
			this.stdin = null;
			
			this.gui = false;
			this.help = false;
			this.version = false;
			this.changelog = false;
			this.dumpActionPointers = false;
			this.dumpResource = null;
			this.dryRun = false;

			this.useStdin = false;
			this.inFiles = new LinkedList<>();
			this.inCharset = Charset.defaultCharset();
			
			this.outCharset = StandardCharsets.US_ASCII;
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

		public Options setUseStdin(boolean useStdin) 
		{
			this.useStdin = useStdin;
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

		public Options setInCharsetName(String scriptCharsetName) 
		{
			try {
				this.inCharset = ObjectUtils.isEmpty(scriptCharsetName) ? Charset.forName(scriptCharsetName) : Charset.defaultCharset();
			} catch (Exception e) {
				this.inCharset = Charset.defaultCharset();
			}
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
			if (options.gui)
			{
				try {
					DoomToolsGUIMain.startGUIAppProcess(ApplicationNames.DECOHACK);
				} catch (IOException e) {
					options.stderr.println("ERROR: Could not start DECOHack GUI!");
					return ERROR_IOERROR;
				}
				return ERROR_NONE;
			}

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
			
			if (options.changelog)
			{
				changelog(options.stdout, "decohack");
				return ERROR_NONE;
			}
			
			if (options.dumpActionPointersHTML)
			{
				final BiPredicate<DEHActionPointer, DEHActionPointer> BREAK = (p1, p2) -> 
					p1.isWeapon() != p2.isWeapon() || p1.getType() != p2.getType();
				
				List<DEHActionPointer> pointerList = new LinkedList<>();
				pointerList.addAll(Arrays.asList(DEHActionPointerDoom19.values()));
				pointerList.addAll(Arrays.asList(DEHActionPointerMBF.values()));
				pointerList.addAll(Arrays.asList(DEHActionPointerMBF21.values()));
				
				HTMLStringWriter htmlwriter = HTMLWriter.createHTMLString(HTMLWriter.Options.PRETTY, HTMLWriter.Options.SLASHES_IN_SINGLE_TAGS);
				
				boolean firstCategory = true;
				DEHActionPointer prev = null;
				for (DEHActionPointer pointer : pointerList)
				{
					// Will skip A_NULL
					if (prev != null)
					{
						if (BREAK.test(prev, pointer))
						{
							if (!firstCategory)
								htmlwriter.pop().pop();
							htmlwriter.push("div", HTMLWriter.classes("dh-content"));

							htmlwriter.push("h1", HTMLWriter.classes("dh-pointer-type"));
							htmlwriter.text(pointer.getType().name());
							htmlwriter.tag("span", pointer.isWeapon() ? "Weapon Pointer" : "Thing Pointer", HTMLWriter.classes("dh-pointer-subtype"));
							htmlwriter.pop();
							
							htmlwriter.push("div", HTMLWriter.classes("dh-pointer-type-section"));
							firstCategory = false;
						}
						
						// Print pointer and usage.
						htmlwriter.push("div", HTMLWriter.id("A_" + pointer.getMnemonic()), HTMLWriter.classes("dh-pointer"));
						htmlwriter.push("h3", HTMLWriter.classes("dh-pointer-name"));
						htmlwriter.text("A_" + pointer.getMnemonic() + "(");

						Usage usage = pointer.getUsage();
						boolean first = true;
						for (PointerParameter parameter : usage.getParameters())
						{
							if (!first)
								htmlwriter.text(", ");
							htmlwriter.tag("span", parameter.getName(), HTMLWriter.classes("dh-pointer-parameter"));
							first = false;
						}
						htmlwriter.text(")");
						htmlwriter.pop();

						htmlwriter.push("div", HTMLWriter.classes("instructions"));
						for (String instruction : usage.getInstructions())
						{
							htmlwriter.tag("p", instruction.trim());
						}
						htmlwriter.pop();
						
						if (usage.hasParameters())
						{
							htmlwriter.push("ul", HTMLWriter.classes("parameter-list"));
							for (PointerParameter parameter : usage.getParameters())
							{
								htmlwriter.push("li", HTMLWriter.classes("parameter"));
								htmlwriter.tag("span", parameter.getName(), HTMLWriter.classes("parameter-name"));
								htmlwriter.tag("span", "(" + parameter.getType().name() + ")", HTMLWriter.classes("parameter-type"));
								htmlwriter.push("div", HTMLWriter.classes("instructions"));
								for (String instruction : parameter.getInstructions())
								{
									htmlwriter.tag("p", instruction.trim());
								}
								htmlwriter.pop().pop();
							}
							htmlwriter.pop();
						}
						htmlwriter.pop();
					}

					prev = pointer;
				}
				
				options.stdout.println(htmlwriter.toString());
				
				return ERROR_NONE;
			}
			
			if (options.dumpActionPointers)
			{
				final BiPredicate<DEHActionPointer, DEHActionPointer> BREAK = (p1, p2) -> 
					p1.isWeapon() != p2.isWeapon() || p1.getType() != p2.getType(); 

				List<DEHActionPointer> pointerList = new LinkedList<>();
				pointerList.addAll(Arrays.asList(DEHActionPointerDoom19.values()));
				pointerList.addAll(Arrays.asList(DEHActionPointerMBF.values()));
				pointerList.addAll(Arrays.asList(DEHActionPointerMBF21.values()));
					
				boolean firstCategory = true;
				DEHActionPointer prev = null;
				for (DEHActionPointer pointer : pointerList)
				{
					// Will skip A_NULL
					if (prev != null)
					{
						if (BREAK.test(prev, pointer))
						{
							if (!firstCategory)
								options.stdout.print("\n");
							
							options.stdout.print("# ");
							options.stdout.print(pointer.getType().name());
							options.stdout.println(pointer.isWeapon() ? " Weapon Pointer" : " Thing Pointer");
							options.stdout.println();
							firstCategory = false;
						}
						
						// Print pointer and usage.
						options.stdout.print("A_");
						options.stdout.print(pointer.getMnemonic());
						options.stdout.print("(");

						Usage usage = pointer.getUsage();
						boolean first = true;
						for (PointerParameter parameter : usage.getParameters())
						{
							if (!first)
								options.stdout.print(", ");
							options.stdout.print(parameter.getName());
							first = false;
						}
						options.stdout.println(")");
						
						for (String instruction : usage.getInstructions())
						{
							options.stdout.print("    ");
							options.stdout.println(instruction.trim());
						}
						for (PointerParameter parameter : usage.getParameters())
						{
							options.stdout.print("    - ");
							options.stdout.print(parameter.getName());
							options.stdout.println(" (" + parameter.getType().name() + ")");
							for (String instruction : parameter.getInstructions())
							{
								options.stdout.print("        ");
								options.stdout.println(instruction.trim());
							}
						}
						options.stdout.println();
					}

					prev = pointer;
				}
				
				return ERROR_NONE;
			}
			
			if (options.dumpResource != null)
			{
				if (!options.dumpResource.equals(RESOURCE_HELP_CONSTANTS) && !options.dumpResource.startsWith("decohack/"))
				{
					options.stderr.println("ERROR: Bad resource path (must start with \"decohack/\").");
					return ERROR_MISSING_RESOURCE;
				}
				
				InputStream resIn = IOUtils.openResource(options.dumpResource);
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
			
			// Read script.
			AbstractPatchContext<?> context;
			if (options.useStdin)
			{
				if (options.outFile == null)
				{
					if (!options.dryRun)
						options.stdout.printf("NOTE: Output file not specified, defaulting to %s.\n", DEFAULT_OUTFILENAME);
					options.outFile = new File(DEFAULT_OUTFILENAME);
				}

				try (Reader reader = new BufferedReader(new InputStreamReader(options.stdin, options.inCharset))) 
				{
					DecoHackParser.Result result;
					result = DecoHackParser.read("STDIN", options.stdin, options.inCharset);
					context = result.getContext();
					for (String message : result.getWarnings())
						options.stderr.println("WARNING: " + message);
					if (context == null)
					{
						for (String message : result.getErrors())
							options.stderr.println("ERROR: " + message);
						return ERROR_PARSEERROR;
					}
				} 
				catch (PreprocessorException e) 
				{
					options.stderr.println("ERROR: " + e.getLocalizedMessage());
					return ERROR_PARSEERROR;
				} 
				catch (IOException e) 
				{
					options.stderr.println("ERROR: I/O Error: " + e.getLocalizedMessage());
					return ERROR_IOERROR;
				} 
				catch (SecurityException e) 
				{
					options.stderr.println("ERROR: Could not open standard in for reading (access denied).");
					return ERROR_SECURITY;
				}
			}
			else
			{
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
					if (!options.dryRun)
						options.stdout.printf("NOTE: Output file not specified, defaulting to %s.\n", DEFAULT_OUTFILENAME);
					options.outFile = new File(DEFAULT_OUTFILENAME);
				}

				try 
				{
					DecoHackParser.Result result;
					result = DecoHackParser.read(options.inFiles, options.inCharset);
					context = result.getContext();
					for (String message : result.getWarnings())
						options.stderr.println("WARNING: " + message);
					if (context == null)
					{
						for (String message : result.getErrors())
							options.stderr.println("ERROR: " + message);
						return ERROR_PARSEERROR;
					}
				} 
				catch (PreprocessorException e) 
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
			}			

			// warn export if [Ultimate] Doom 1.9 and last state is replaced.
			if (context.getSupportedFeatureLevel() == DEHFeatureLevel.DOOM19
				&& ! (context.getState(context.getStateCount() - 1).equals(context.getSourcePatch().getState(context.getStateCount() - 1))
			))
			{
				options.stderr.println("WARNING: Final state was replaced in the exported patch - DHE 3.1 may not import this correctly!");
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
			
			if (!options.dryRun)
			{
				// Combine source.
				if (options.outSourceFile != null)
				{
					boolean isWad;
					try {
						isWad = Wad.isWAD(options.outSourceFile);
					} catch (IOException e) {
						options.stderr.println("ERROR: Source output file " + options.outSourceFile.getPath() + " could not be read!");
						return ERROR_IOERROR;
					} catch (SecurityException e) {
						options.stderr.println("ERROR: Source output file " + options.outSourceFile.getPath() + " could not be read! Access denied.");
						return ERROR_IOERROR;
					}

					if (isWad)
					{
						try (WadFile wad = new WadFile(options.outSourceFile)) 
						{
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(bos, options.inCharset), true))
							{
								for (File file : options.inFiles)
								{
									DecoHackJoiner.joinSourceFrom(file, Charset.defaultCharset(), writer);
								}
							}
							
							int index;
							if ((index = wad.indexOf("DECOHACK")) >= 0)
								wad.replaceEntry(index, bos.toByteArray());
							else
								wad.addData("DECOHACK", bos.toByteArray());
							
							options.stdout.printf("Wrote source into %s as `DECOHACK`.\n", options.outSourceFile.getPath());
						} 
						catch (IOException e) 
						{
							options.stderr.println("ERROR: I/O Error: " + e.getLocalizedMessage());
							return ERROR_IOERROR;
						}
					}
					else
					{
						try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(options.outSourceFile), options.inCharset), true))
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
					
				}
				
				boolean isWad;
				try {
					isWad = Wad.isWAD(options.outFile);
				} catch (IOException e) {
					options.stderr.println("ERROR: Source output file " + options.outSourceFile.getPath() + " could not be read!");
					return ERROR_IOERROR;
				} catch (SecurityException e) {
					options.stderr.println("ERROR: Source output file " + options.outSourceFile.getPath() + " could not be read! Access denied.");
					return ERROR_IOERROR;
				}
				
				if (isWad)
				{
					try (WadFile wad = new WadFile(options.outFile)) 
					{
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						try (Writer writer = new OutputStreamWriter(bos, options.outCharset)) 
						{
							context.writePatch(writer, "Created with " + VERSION_LINE);
						} 
						
						int index;
						if ((index = wad.indexOf("DEHACKED")) >= 0)
							wad.replaceEntry(index, bos.toByteArray());
						else
							wad.addData("DEHACKED", bos.toByteArray());
						
						options.stdout.printf("Wrote patch into %s as `DEHACKED`.\n", options.outFile.getPath());
					} 
					catch (IOException e) 
					{
						options.stderr.println("ERROR: I/O Error: " + e.getLocalizedMessage());
						return ERROR_IOERROR;
					}
				}
				else
				{
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
				}
			}
			
			return ERROR_NONE;
		}
	}
	
	/**
	 * Reads command line arguments and sets options.
	 * @param out the standard output print stream.
	 * @param err the standard error print stream. 
	 * @param in the standard in input stream.
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
	
		final int STATE_START = 0;
		final int STATE_OUTFILE = 1;
		final int STATE_OUTCHARSET = 2;
		final int STATE_DUMPRES = 3;
		final int STATE_SOURCEOUTFILE = 4;
		final int STATE_CHARSET = 5;
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
					else if (arg.equals(SWITCH_HELPFULL))
					{
						options.help = true;
						options.full = true;
					}
					else if (arg.equalsIgnoreCase(SWITCH_GUI))
						options.gui = true;
					else if (arg.equalsIgnoreCase(SWITCH_CHANGELOG))
						options.changelog = true;
					else if (arg.equals(SWITCH_VERSION))
						options.version = true;
					else if (arg.equals(SWITCH_DRYRUN))
						options.dryRun = true;
					else if (arg.equals(SWITCH_DUMPPOINTERS))
						options.dumpActionPointers = true;
					else if (arg.equals(SWITCH_DUMPPOINTERS_HTML))
						options.dumpActionPointersHTML = true;
					else if (arg.equals(SWITCH_DUMPCONSTANTS))
						options.dumpResource = RESOURCE_HELP_CONSTANTS;
					else if (arg.equals(SWITCH_DUMPRESOURCE))
						state = STATE_DUMPRES;
					else if (arg.equals(SWITCH_CHARSET1) || arg.equals(SWITCH_CHARSET2))
						state = STATE_CHARSET;
					else if (arg.equals(SWITCH_BUDGET) || arg.equals(SWITCH_BUDGET2))
						options.outputBudget = true;
					else if (arg.equals(SWITCH_OUTPUT) || arg.equals(SWITCH_OUTPUT2))
						state = STATE_OUTFILE;
					else if (arg.equals(SWITCH_SOURCE_OUTPUT) || arg.equals(SWITCH_SOURCE_OUTPUT2))
						state = STATE_SOURCEOUTFILE;
					else if (arg.equals(SWITCH_OUTPUTCHARSET) || arg.equals(SWITCH_OUTPUTCHARSET2))
						state = STATE_OUTCHARSET;
					else if (arg.equals(SWITCH_SYSTEMIN))
						options.setUseStdin(true);
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
						throw new OptionParseException("ERROR: Unknown charset name: " + arg);
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

				case STATE_CHARSET:
				{
					try {
						options.inCharset = Charset.forName(arg);
					} catch (IllegalCharsetNameException e) {
						throw new OptionParseException("ERROR: Unknown charset name: " + arg);
					} catch (UnsupportedCharsetException e) {
						throw new OptionParseException("ERROR: Unsupported charset name: " + arg);
					}
					state = STATE_START;
				}
				break;
			}
		}
		
		if (state == STATE_OUTFILE)
			throw new OptionParseException("ERROR: Expected output file.");
		if (state == STATE_OUTCHARSET)
			throw new OptionParseException("ERROR: Expected output charset name.");
		if (state == STATE_CHARSET)
			throw new OptionParseException("ERROR: Expected input charset name.");
		
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
			System.exit(call(options(System.out, System.err, System.in, args)));
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
	 * Prints the changelog.
	 * @param out the print stream to print to.
	 */
	private static void changelog(PrintStream out, String name)
	{
		String line;
		int i = 0;
		try (BufferedReader br = IOUtils.openTextStream(IOUtils.openResource("docs/changelogs/CHANGELOG-" + name + ".md")))
		{
			while ((line = br.readLine()) != null)
			{
				if (i >= 3) // eat the first three lines
					out.println(line);
				i++;
			}
		} 
		catch (IOException e) 
		{
			out.println("****** ERROR: Cannot read CHANGELOG ******");
		}
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
		out.println("    --changelog              Prints the changelog, and exits.");
		out.println();
		out.println("    --gui                    Starts the GUI version of this program.");
		out.println();
		out.println("    --dump-constants         Dumps the list of available defined constants");
		out.println("                             to STDOUT.");
		out.println();
		out.println("    --dump-resource [path]   Dumps an internal resource (starting with");
		out.println("                             \"decohack/\" ) to STDOUT.");
		out.println();
		out.println("    --dump-pointers          Dumps the list of Action Pointers and their");
		out.println("                             parameter types to STDOUT.");
		out.println();
		out.println("    --dump-pointers-html     Dumps the list of Action Pointers and their");
		out.println("                             parameter types to STDOUT in HTML form.");
		out.println();
		out.println("[filenames]:");
		out.println("    <filename> ...           The input filenames. One or more can be added,");
		out.println("                             parsed in the order specified.");
		out.println();
		out.println("    --                       Source input is from Standard In, not a file.");
		out.println();
		out.println("[switches]:");
		out.println("    --output [file]          Outputs the resultant patch to [file].");
		out.println("    -o [file]                If [file] is a WAD file, the output patch is added");
		out.println("                             or replaced in the WAD file as \"DEHACKED\".");
		out.println();
		out.println("    --charset [name]         Sets the input charset to [name]. The default");
		out.println("    -c [name]                charset is " + Charset.defaultCharset().displayName() + " (system default).");
		out.println();
		out.println("    --source-output [file]   Outputs the combined source to a single file.");
		out.println("    -s [file]                If [file] is a WAD file, the source is added");
		out.println("                             or replaced in the WAD file as \"DECOHACK\".");
		out.println();
		out.println("    --output-charset [name]  Sets the output charset to [name]. The default");
		out.println("    -oc [name]               charset is ASCII, and there are not many reasons");
		out.println("                             to change.");
		out.println();
		out.println("    --budget                 Prints the state budget after compilation.");
		out.println("    -b");
		out.println();
		out.println("    --dry-run                Does no output - only attempts to compile and");
		out.println("                             return errors and/or warnings. Overrides all");
		out.println("                             output switches.");
		out.println();
		if (full)
		{
			try (BufferedReader br = new BufferedReader(new InputStreamReader(IOUtils.openResource("docs/DECOHack Help.txt")))) {
				String line;
				while ((line = br.readLine()) != null)
					out.println(line);
			} catch (IOException e) {
				/* Do nothing. */
			}
		}
	}

}

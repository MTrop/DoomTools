/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import net.mtrop.doom.Wad;
import net.mtrop.doom.WadBuffer;
import net.mtrop.doom.WadEntry;
import net.mtrop.doom.WadFile;
import net.mtrop.doom.exception.TextureException;
import net.mtrop.doom.exception.WadException;
import net.mtrop.doom.texture.TextureSet;
import net.mtrop.doom.tools.exception.OptionParseException;
import net.mtrop.doom.tools.gui.DoomToolsGUIMain;
import net.mtrop.doom.tools.gui.DoomToolsGUIMain.ApplicationNames;
import net.mtrop.doom.tools.struct.util.IOUtils;
import net.mtrop.doom.util.TextureUtils;

/**
 * Main class for TexScan.
 * @author Matthew Tropiano
 */
public final class WTexListMain
{
	private static final String SPLASH_VERSION = "WTexList v" + Version.WTEXLIST + " by Matt Tropiano (using DoomStruct v" + Version.DOOMSTRUCT + ")";

	private static final int ERROR_NONE = 0;
	private static final int ERROR_BAD_FILE = 1;
	private static final int ERROR_BAD_OPTIONS = 2;
	private static final int ERROR_IOERROR = 3;
	private static final int ERROR_UNKNOWN = -1;

	public static final String SWITCH_HELP = "--help";
	public static final String SWITCH_HELP2 = "-h";
	public static final String SWITCH_VERSION = "--version";
	public static final String SWITCH_QUIET = "--quiet";
	public static final String SWITCH_QUIET2 = "-q";
	public static final String SWITCH_TEXTURES = "--textures";
	public static final String SWITCH_TEXTURES2 = "-t";
	public static final String SWITCH_FLATS = "--flats";
	public static final String SWITCH_FLATS2 = "-f";
	public static final String SWITCH_CHANGELOG = "--changelog";
	public static final String SWITCH_GUI = "--gui";

	/**
	 * Program options.
	 */
	public static class Options
	{
		private PrintStream stdout;
		private PrintStream stderr;
		private boolean changelog;
		private boolean gui;
		private boolean help;
		private boolean version;
		private boolean quiet;
		private boolean outputTextures;
		private boolean outputFlats;
		private List<File> wadFiles;
		
		private Options()
		{
			this.stdout = null;
			this.stderr = null;
			this.gui = false;
			this.changelog = false;
			this.help = false;
			this.version = false;
			this.quiet = false;
			this.outputTextures = false;
			this.outputFlats = false;
			this.wadFiles = new LinkedList<>();
		}
		
		void println(Object msg)
		{
			if (!quiet)
				stdout.println(msg);
		}
		
		void errln(Object msg)
		{
			if (!quiet)
				stderr.println(msg);
		}
		
		void errf(String fmt, Object... args)
		{
			if (!quiet)
				stderr.printf(fmt, args);
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

		public Options setQuiet(boolean quiet) 
		{
			this.quiet = quiet;
			return this;
		}
		
		public Options setOutputTextures(boolean outputTextures) 
		{
			this.outputTextures = outputTextures;
			return this;
		}
		
		public Options setOutputFlats(boolean outputFlats) 
		{
			this.outputFlats = outputFlats;
			return this;
		}
		
		public Options addWadFile(File file)
		{
			this.wadFiles.add(file);
			return this;
		}
		
	}
	
	/**
	 * Program context.
	 */
	private static class Context implements Callable<Integer>
	{
		private Options options;
		private SortedSet<String> textureList;
		private SortedSet<String> flatList;

		private Context(Options options)
		{
			this.options = options;
			this.textureList = new TreeSet<>();
			this.flatList = new TreeSet<>();
		}
		
		// Process PK3/ZIP
		private void processPK3(String fileName, File f) throws ZipException, IOException
		{
			options.println("# Inspecting " + fileName + "...");
			ZipFile zf = new ZipFile(f);
			
			@SuppressWarnings("unchecked")
			Enumeration<ZipEntry> en = (Enumeration<ZipEntry>)zf.entries();
			while (en.hasMoreElements())
			{
				ZipEntry ze = en.nextElement();
				if (ze.isDirectory())
					continue;
				
				String zeName = ze.getName().toLowerCase();
				
				if (zeName.endsWith(".wad"))
				{
					WadBuffer wm = null;
					try (InputStream zin = zf.getInputStream(ze)) 
					{
						wm = new WadBuffer(zin);
						inspectWAD(wm);
					} 
					catch (IOException e) 
					{
						options.errln("ERROR: Could not read entry "+ze.getName()+".");
					}
				}
			}
			
			zf.close();
		}

		// Process WAD
		private void processWAD(File f) throws WadException, IOException
		{
			options.println("# Inspecting " + f.getPath() + "...");
			try (WadFile wf = new WadFile(f))
			{
				inspectWAD(wf);
			}
		}

		// Inspect WAD contents.
		private void inspectWAD(Wad wad) throws IOException
		{
			if (options.outputTextures)
				inspectTextures(wad);
			if (options.outputFlats)
				inspectFlats(wad);
		}
		
		private void inspectTextures(Wad wad)
		{
			TextureSet textureSet = null;
			try {
				textureSet = TextureUtils.importTextureSet(wad);
			} catch (TextureException e) {
				options.println("#     " + e.getLocalizedMessage());
				return;
			} catch (IOException e) {
				options.println("#     (ERROR) " + e.getLocalizedMessage());
				return;
			}

			for (TextureSet.Texture texture : textureSet)
				addTexture(texture.getName());
		}
		
		private void inspectFlats(Wad wad)
		{
			int flatStartEntryIndex = wad.indexOf("F_START");
			if (flatStartEntryIndex == -1)
				flatStartEntryIndex = wad.indexOf("FF_START");
			
			// if no flats...
			if (flatStartEntryIndex == -1)
				return;
			
			int flatEndEntryIndex = wad.indexOf("F_END");
			if (flatEndEntryIndex == -1)
				flatEndEntryIndex = wad.indexOf("FF_END");
			
			WadEntry[] flatEntries = wad.mapEntries(flatStartEntryIndex + 1, flatEndEntryIndex - (flatStartEntryIndex + 1));
			
			for (WadEntry entry : flatEntries)
				if (entry.getSize() > 0) // skip markers/invalid flats
					addFlat(entry.getName());
		}

		private void addTexture(String texture)
		{
			if (!textureList.contains(texture) && texture != null && !texture.trim().isEmpty() && !texture.equals("-"))
				textureList.add(texture);
		}

		private void addFlat(String flat)
		{
			if (!flatList.contains(flat) && flat != null && !flat.trim().isEmpty())
				flatList.add(flat);
		}

		@Override
		public Integer call()
		{
			if (options.gui)
			{
				try {
					DoomToolsGUIMain.startGUIAppProcess(ApplicationNames.WTEXLIST);
				} catch (IOException e) {
					options.stderr.println("ERROR: Could not start WTexList GUI!");
					return ERROR_IOERROR;
				}
				return ERROR_NONE;
			}

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
		
			if (options.changelog)
			{
				changelog(options.stdout, "wtexlist");
				return ERROR_NONE;
			}
			
			if (options.wadFiles.isEmpty())
			{
				splash(options.stdout);
				usage(options.stdout);
				return ERROR_NONE;
			}
		
			boolean atLeastOneError = false;
			for (File f : options.wadFiles)
			{
				try
				{
					if (f.getName().toLowerCase().endsWith(".wad"))
						processWAD(f);
					else if (f.getName().toLowerCase().endsWith(".pk3"))
						processPK3(f.getPath(), f);
					else if (f.getName().toLowerCase().endsWith(".pke"))
						processPK3(f.getPath(), f);
					else if (f.getName().toLowerCase().endsWith(".zip"))
						processPK3(f.getPath(), f);
					else
					{
						options.errf("ERROR: %s is not a WAD, PK3, PKE, or ZIP.\n", f.getPath());
						atLeastOneError = true;
					}
				}
				catch (IOException e)
				{
					options.errf("ERROR: %s: %s\n", e.getClass().getSimpleName(), e.getLocalizedMessage());
					atLeastOneError = true;
				}
			}
			
			if (atLeastOneError)
				return ERROR_BAD_FILE;
			
			if (options.help || options.version)
			{
				return ERROR_NONE;
			}
			
			if (!options.wadFiles.isEmpty())
			{
				if (textureList.isEmpty())
				{
					options.stdout.println("# No textures.");
				}
				else
				{
					options.stdout.println(":textures");
					for (String t : textureList)
						options.stdout.println(t);
				}
			
				if (flatList.isEmpty())
				{
					options.println("# No flats.");
				}
				else
				{
					options.stdout.println(":flats");
					for (String f : flatList)
						options.stdout.println(f);
				}
			
				options.stdout.println(":end");
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
	
		final int STATE_INIT = 0;
	
		int state = STATE_INIT;
		int i = 0;
		while (i < args.length)
		{
			String arg = args[i];
			switch (state)
			{
				case STATE_INIT:
				{
					if (arg.equals(SWITCH_HELP) || arg.equals(SWITCH_HELP2))
						options.help = true;
					else if (arg.equals(SWITCH_VERSION))
						options.version = true;
					else if (arg.equals(SWITCH_GUI))
						options.gui = true;
					else if (arg.equalsIgnoreCase(SWITCH_CHANGELOG))
						options.changelog = true;
					else if (arg.equals(SWITCH_QUIET) || arg.equals(SWITCH_QUIET2))
						options.setQuiet(true);
					else if (arg.equals(SWITCH_TEXTURES) || arg.equals(SWITCH_TEXTURES2))
						options.setOutputTextures(true);
					else if (arg.equals(SWITCH_FLATS) || arg.equals(SWITCH_FLATS2))
						options.setOutputFlats(true);
					else
						options.addWadFile(new File(arg));
				}
				break;
			}
			i++;
		}
		
		if (!options.outputFlats && !options.outputTextures)
		{
			options.setOutputFlats(true);
			options.setOutputTextures(true);
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
		out.println("Usage: wtexlist [--help | -h | --version] [files] [switches]");
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
	private static void help(PrintStream out)
	{
		out.println("    --help              Prints help and exits.");
		out.println("    -h");
		out.println();
		out.println("    --version           Prints version, and exits.");
		out.println();
		out.println("    --changelog         Prints the changelog, and exits.");
		out.println();
		out.println("[files]:");
		out.println("    <filename>          The files to inspect (WAD/PK3/PKE, accepts wildcards).");
		out.println();
		out.println("[switches]:");
		out.println("    --textures          Output just textures.");
		out.println("    -t");
		out.println();
		out.println("    --flats             Output just flats.");
		out.println("    -f");
		out.println();
		out.println("  If the above switches are not specified, both textures and flats are output.");
		out.println();
		out.println("    --quiet             Output no comment-messages.");
		out.println("    -q");
	}
	
}

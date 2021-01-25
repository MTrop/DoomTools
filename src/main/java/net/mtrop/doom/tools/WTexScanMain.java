/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import net.mtrop.doom.Wad;
import net.mtrop.doom.WadBuffer;
import net.mtrop.doom.WadFile;
import net.mtrop.doom.exception.WadException;
import net.mtrop.doom.map.MapFormat;
import net.mtrop.doom.map.data.DoomSector;
import net.mtrop.doom.map.data.DoomSidedef;
import net.mtrop.doom.map.udmf.UDMFObject;
import net.mtrop.doom.map.udmf.UDMFReader;
import net.mtrop.doom.map.udmf.UDMFTable;
import net.mtrop.doom.map.udmf.attributes.UDMFDoomSectorAttributes;
import net.mtrop.doom.map.udmf.attributes.UDMFDoomSidedefAttributes;
import net.mtrop.doom.struct.io.IOUtils;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.exception.OptionParseException;
import net.mtrop.doom.util.MapUtils;
import net.mtrop.doom.util.NameUtils;

/**
 * Main class for TexScan.
 * @author Matthew Tropiano
 */
public final class WTexScanMain
{
	private static final String DOOM_VERSION = Common.getVersionString("doom");
	private static final String VERSION = Common.getVersionString("wtexscan");
	private static final String SPLASH_VERSION = "WTexScan v" + VERSION + " by Matt Tropiano (using DoomStruct v" + DOOM_VERSION + ")";

	private static final int ERROR_NONE = 0;
	private static final int ERROR_BAD_FILE = 1;
	private static final int ERROR_BAD_OPTIONS = 2;
	
	private static final String SWITCH_HELP = "--help";
	private static final String SWITCH_HELP2 = "-h";
	private static final String SWITCH_VERSION = "--version";
	private static final String SWITCH_QUIET = "--quiet";
	private static final String SWITCH_QUIET2 = "-q";
	private static final String SWITCH_TEXTURES = "--textures";
	private static final String SWITCH_TEXTURES2 = "-t";
	private static final String SWITCH_FLATS = "--flats";
	private static final String SWITCH_FLATS2 = "-f";
	private static final String SWITCH_NOSKIES = "--no-skies";
	private static final String SWITCH_MAP = "--map";
	private static final String SWITCH_MAP2 = "-m";

	/** Regex pattern for Episode, Map. */
	private static final Pattern EPISODE_PATTERN = Pattern.compile("E[1-5]M[1-9]");
	/** Regex pattern for Map only. */
	private static final Pattern MAP_PATTERN = Pattern.compile("MAP[0-9][0-9]");
	
	/**
	 * Program options.
	 */
	public static class Options
	{
		private PrintStream stdout;
		private PrintStream stderr;
		private boolean help;
		private boolean version;
		private boolean quiet;
		private boolean outputTextures;
		private boolean outputFlats;
		private boolean skipSkies;
		private List<File> wadFiles;
		private SortedSet<String> mapsToScan;
		
		private Options()
		{
			this.stdout = null;
			this.stderr = null;
			this.help = false;
			this.version = false;
			this.quiet = false;
			this.outputTextures = false;
			this.outputFlats = false;
			this.skipSkies = false;
			this.wadFiles = new LinkedList<>();
			this.mapsToScan = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
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
		
		public Options setSkipSkies(boolean skipSkies) 
		{
			this.skipSkies = skipSkies;
			return this;
		}
		
		public Options setMapsToScan(String[] maps)
		{
			this.mapsToScan.clear();
			for (String map : maps)
				addMapToScan(map);
			return this;
		}
		
		public Options addWadFile(File file)
		{
			this.wadFiles.add(file);
			return this;
		}
		
		public Options addMapToScan(String mapname)
		{
			this.mapsToScan.add(mapname);
			return this;
		}

	}
	
	/**
	 * Program context.
	 */
	private static class Context
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
				if (ze.getName().toLowerCase().endsWith(".wad"))
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
				else if (ze.getName().toLowerCase().endsWith(".pk3"))
				{
					File pk3 = File.createTempFile("texspy", "pk3tmp");
					try (InputStream zin = zf.getInputStream(ze); FileOutputStream fos = new FileOutputStream(pk3)) 
					{
						IOUtils.relay(zin, fos);
						IOUtils.close(fos);
						processPK3(fileName + File.separator + ze.getName(), pk3);
					} 
					catch (IOException e) 
					{
						options.errln("ERROR: Could not read entry "+ze.getName()+".");
					} 
					pk3.deleteOnExit();
				}
			}
			
			zf.close();
		}

		// Process WAD
		private void processWAD(File f) throws WadException, IOException
		{
			options.println("# Inspecting " + f.getPath() + "...");
			WadFile wf = new WadFile(f);
			inspectWAD(wf);
			wf.close();
		}

		// Inspect WAD contents.
		private void inspectWAD(Wad wad) throws IOException
		{
			String[] mapHeaders = MapUtils.getAllMapHeaders(wad);
			for (String mapName : mapHeaders)
				if (options.mapsToScan.isEmpty() || options.mapsToScan.contains(mapName))
					inspectMap(wad, mapName);
		}

		/**
		 * Returns the episode and map as (x,y) in the provided pair.
		 * If p.x and p.y = -1, the episode and map was not detected.
		 * Map only lumps have p.x = 0.
		 * @param mapName the map lump
		 * @param p the output Pair.
		 */
		private void getEpisodeAndMap(String mapName, Pair p)
		{
			if (EPISODE_PATTERN.matcher(mapName).matches())
			{
				p.x = Integer.parseInt(mapName.substring(1, 2));;
				p.y = Integer.parseInt(mapName.substring(3));			
			}
			else if (MAP_PATTERN.matcher(mapName).matches())
			{
				p.x = 0;
				p.y = Integer.parseInt(mapName.substring(3));
			}
		}

		// Inspect a map in a WAD.
		private void inspectMap(Wad wad, String mapName) throws IOException
		{
			options.println("#    Opening map "+mapName+"...");
			
			MapFormat format = MapUtils.getMapFormat(wad, mapName);
			
			if (format == null)
			{
				options.println("#    ERROR: NOT A MAP!");
				return;
			}
		
			options.println("#    Format is "+format.name()+"...");
		
			// filled in if UDMF.
			UDMFTable udmf = null;
			
			if (format == MapFormat.UDMF)
			{
				try (InputStream in = wad.getInputStream("TEXTMAP", wad.lastIndexOf(mapName)))
				{
					udmf = UDMFReader.readData(in);
				}
			}
					
			if (options.outputTextures)
			{
				options.println("#        Reading SIDEDEFS...");
		
				switch (format)
				{
					default:
					case DOOM:
					case HEXEN:
					{
						inspectSidedefs(wad.getDataAs("SIDEDEFS", wad.lastIndexOf(mapName), DoomSidedef.class, DoomSidedef.LENGTH));
					}
					break;
		
					case UDMF:
					{
						inspectSidedefs(udmf.getObjects("sidedef"));
					}
					break;
				}
			}
		
			if (options.outputFlats)
			{
				options.println("#        Reading SECTORS...");
		
				switch (format)
				{
					default:
					case DOOM:
					case HEXEN:
					{
						inspectSectors(wad.getDataAs("SECTORS", wad.lastIndexOf(mapName), DoomSector.class, DoomSector.LENGTH));
					}
					break;
		
					case UDMF:
					{
						inspectSectors(udmf.getObjects("sector"));
					}
					break;
						
				}
			}
			
			if (!options.skipSkies)
			{
				inspectMap(mapName);
			}
			
		}

		private void inspectMap(String mapName)
		{
			Pair p = new Pair();
			getEpisodeAndMap(mapName, p);
			if (p.x == 0)
			{
				if (p.y >= 21)
				{
					if (!textureList.contains("SKY3"))
						textureList.add("SKY3");
				}
				else if (p.y >= 12)
				{
					if (!textureList.contains("SKY2"))
						textureList.add("SKY2");
				}
				else
				{
					if (!textureList.contains("SKY1"))
						textureList.add("SKY1");
				}
			}
			else if (p.x == 1)
			{
				if (!textureList.contains("SKY1"))
					textureList.add("SKY1");
			}
			else if (p.x == 2)
			{
				if (!textureList.contains("SKY2"))
					textureList.add("SKY2");
			}
			else if (p.x == 3)
			{
				if (!textureList.contains("SKY3"))
					textureList.add("SKY3");
			}
			else if (p.x == 4)
			{
				if (!textureList.contains("SKY4"))
					textureList.add("SKY4");
				if (!textureList.contains("SKY1"))
					textureList.add("SKY1");
			}
			else if (p.x == 5)
			{
				if (!textureList.contains("SKY3"))
					textureList.add("SKY3");
			}
		}

		// Adds sidedef textures to the list.
		private void inspectSidedefs(DoomSidedef[] sidedefs)
		{
			for (DoomSidedef s : sidedefs)
			{
				addTexture(s.getTextureTop());
				addTexture(s.getTextureMiddle());
				addTexture(s.getTextureBottom());
			}
		}

		// Adds sidedef textures to the list.
		private void inspectSidedefs(UDMFObject[] sidedefs)
		{
			for (UDMFObject s : sidedefs)
			{
				addTexture(s.getString(UDMFDoomSidedefAttributes.ATTRIB_TEXTURE_TOP, NameUtils.EMPTY_TEXTURE_NAME));
				addTexture(s.getString(UDMFDoomSidedefAttributes.ATTRIB_TEXTURE_MIDDLE, NameUtils.EMPTY_TEXTURE_NAME));
				addTexture(s.getString(UDMFDoomSidedefAttributes.ATTRIB_TEXTURE_BOTTOM, NameUtils.EMPTY_TEXTURE_NAME));
			}
		}

		// Adds sector textures to the list.
		private void inspectSectors(DoomSector[] sectors)
		{
			for (DoomSector s : sectors)
			{
				addFlat(s.getTextureFloor());
				addFlat(s.getTextureCeiling());
			}
		}

		// Adds sector textures to the list.
		private void inspectSectors(UDMFObject[] sectors)
		{
			for (UDMFObject s : sectors)
			{
				addFlat(s.getString(UDMFDoomSectorAttributes.ATTRIB_TEXTURE_FLOOR));
				addFlat(s.getString(UDMFDoomSectorAttributes.ATTRIB_TEXTURE_CEILING));
			}
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
					else if (f.getName().toLowerCase().endsWith(".zip"))
						processPK3(f.getPath(), f);
					else
					{
						options.errf("ERROR: %s is not a WAD, PK3, or ZIP.\n", f.getPath());
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
	
	private static class Pair
	{
		public int x;
		public int y;
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
	public static Options options(PrintStream out, PrintStream err, String ... args) throws OptionParseException
	{
		Options options = new Options();
		options.stdout = out;
		options.stderr = err;
	
		final int STATE_INIT = 0;
		final int STATE_MAP = 1;
	
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
					else if (arg.equals(SWITCH_QUIET) || arg.equals(SWITCH_QUIET2))
						options.setQuiet(true);
					else if (arg.equals(SWITCH_TEXTURES) || arg.equals(SWITCH_TEXTURES2))
						options.setOutputTextures(true);
					else if (arg.equals(SWITCH_FLATS) || arg.equals(SWITCH_FLATS2))
						options.setOutputFlats(true);
					else if (arg.equals(SWITCH_NOSKIES))
						options.setSkipSkies(true);
					else if (arg.equals(SWITCH_MAP) || arg.equals(SWITCH_MAP2))
						state = STATE_MAP;
					else
						options.addWadFile(new File(arg));
				}
				break;
			
				case STATE_MAP:
				{
					options.addMapToScan(arg);
					state = STATE_INIT;
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
		out.println("Usage: wtexscan [--help | -h | --version] [files] [switches]");
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
		out.println("[files]:");
		out.println("    <filename>          The files to inspect (WAD/PK3, accepts wildcards).");
		out.println();
		out.println("[switches]:");
		out.println("    --quiet             Output no messages.");
		out.println("    -q");
		out.println();
		out.println("    --textures          Output textures.");
		out.println("    -t");
		out.println();
		out.println("    --flats             Output flats.");
		out.println("    -f");
		out.println();
		out.println("    --no-skies          Skip adding associated skies per map.");
		out.println();
		out.println("    --map [mapname]     Map to scan. If not specified, all maps will be scanned.");
		out.println("    -m");
	}
	
}

package net.mtrop.doom.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
	private static final String SPLASH_VERSION = "TexScan v" + VERSION + " by Matt Tropiano (using DoomStruct v" + DOOM_VERSION + ")";

	private static final int ERROR_NONE = 0;
	private static final int ERROR_BAD_FILE = 1;
	
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

	/** Regex pattern for Episode, Map. */
	private static final Pattern EPISODE_PATTERN = Pattern.compile("E[1-5]M[1-9]");
	/** Regex pattern for Map only. */
	private static final Pattern MAP_PATTERN = Pattern.compile("MAP[0-9][0-9]");
	
	/**
	 * Program options.
	 */
	public static class Options
	{
		private PrintStream out;
		private PrintStream err;
		private BufferedReader in;
		private boolean help;
		private boolean version;
		private boolean quiet;
		private boolean outputTextures;
		private boolean outputFlats;
		private boolean skipSkies;
		private List<File> wadFiles;
		private SortedSet<String> textureList;
		private SortedSet<String> flatList;
		
		Options()
		{
			this.out = null;
			this.err = null;
			this.in = null;
			this.help = false;
			this.version = false;
			this.quiet = false;
			this.outputTextures = false;
			this.outputFlats = false;
			this.skipSkies = false;
			this.wadFiles = new LinkedList<>();
			this.textureList = new TreeSet<>();
			this.flatList = new TreeSet<>();
		}
		
		void println(Object msg)
		{
			if (!quiet)
				out.println(msg);
		}
		
		void errln(Object msg)
		{
			if (!quiet)
				err.println(msg);
		}
		
		void errf(String fmt, Object... args)
		{
			if (!quiet)
				err.printf(fmt, args);
		}

		char readChar() throws IOException
		{
			return (char)in.read();
		}
		
		String readLine() throws IOException
		{
			return in.readLine();
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
			else if (arg.equals(SWITCH_VERSION))
				options.version = true;
			else if (arg.equals(SWITCH_QUIET) || arg.equals(SWITCH_QUIET2))
				options.quiet = true;
			else if (arg.equals(SWITCH_TEXTURES) || arg.equals(SWITCH_TEXTURES2))
				options.outputTextures = true;
			else if (arg.equals(SWITCH_FLATS) || arg.equals(SWITCH_FLATS2))
				options.outputFlats = true;
			else if (arg.equals(SWITCH_NOSKIES))
				options.skipSkies = true;
			else
				options.wadFiles.add(new File(arg));
			i++;
		}
		
		if (!options.outputFlats && !options.outputTextures)
		{
			options.outputFlats = true;
			options.outputTextures = true;
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
			return ERROR_NONE;
		}
		
		if (options.version)
		{
			splash(options.out);
			return ERROR_NONE;
		}
	
		if (options.wadFiles.isEmpty())
		{
			splash(options.out);
			usage(options.out);
			return ERROR_NONE;
		}
	
		options.println("# " + SPLASH_VERSION);
	
		boolean atLeastOneError = false;
		for (File f : options.wadFiles)
		{
			try
			{
				if (f.getName().toLowerCase().endsWith(".wad"))
					processWAD(options, f);
				else if (f.getName().toLowerCase().endsWith(".pk3"))
					processPK3(options, f.getPath(), f);
				else if (f.getName().toLowerCase().endsWith(".zip"))
					processPK3(options, f.getPath(), f);
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
	
		if (options.textureList.isEmpty())
		{
			options.println("# No textures.");
		}
		else
		{
			options.out.println("-TEXTURES");
			for (String t : options.textureList)
				options.out.println(t);
		}
	
		if (options.flatList.isEmpty())
		{
			options.println("# No flats.");
		}
		else
		{
			options.out.println("-FLATS");
			for (String f : options.flatList)
				options.out.println(f);
		}
	
		options.out.println("-END");
	
		return atLeastOneError ? ERROR_BAD_FILE : ERROR_NONE;
	}

	public static void main(String[] args) throws IOException
	{
		System.exit(call(options(System.out, System.err, Common.openTextStream(System.in), args)));
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
	}
	
	// Process PK3/ZIP
	private static void processPK3(Options options, String fileName, File f) throws ZipException, IOException
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
				InputStream zin = zf.getInputStream(ze);
				WadBuffer wm = null;
				try {
					wm = new WadBuffer(zin);
					inspectWAD(options, wm);
				} catch (IOException e) {
					options.errln("ERROR: Could not read entry "+ze.getName()+".");
				}
				IOUtils.close(zin);
			}
			else if (ze.getName().toLowerCase().endsWith(".pk3"))
			{
				File pk3 = File.createTempFile("texspy", "pk3tmp");
				InputStream zin = zf.getInputStream(ze);
				FileOutputStream fos = new FileOutputStream(pk3);
				try {
					IOUtils.relay(zin, fos);
					IOUtils.close(fos);
					processPK3(options, fileName + File.separator + ze.getName(), pk3);
				} catch (IOException e) {
					options.errln("ERROR: Could not read entry "+ze.getName()+".");
				} finally {
					IOUtils.close(fos);
					IOUtils.close(zin);
				}
				pk3.deleteOnExit();
			}
		}
		
		zf.close();
	}

	// Process WAD
	private static void processWAD(Options options, File f) throws WadException, IOException
	{
		options.println("# Inspecting " + f.getPath() + "...");
		WadFile wf = new WadFile(f);
		inspectWAD(options, wf);
		wf.close();
	}
	
	// Inspect WAD contents.
	private static void inspectWAD(Options options, Wad wad) throws IOException
	{
		String[] mapHeaders = MapUtils.getAllMapHeaders(wad);
		for (String mapName : mapHeaders)
			inspectMap(options, wad, mapName);
	}

	/**
	 * Returns the episode and map as (x,y) in the provided pair.
	 * If p.x and p.y = -1, the episode and map was not detected.
	 * Map only lumps have p.x = 0.
	 * @param mapName the map lump
	 * @param p the output Pair.
	 */
	private static void getEpisodeAndMap(String mapName, Pair p)
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
	private static void inspectMap(Options options, Wad wad, String mapName) throws IOException
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
			InputStream in = wad.getInputStream("TEXTMAP", wad.lastIndexOf(mapName));
			udmf = UDMFReader.readData(in);
			Common.close(in);
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
					inspectSidedefs(options, wad.getDataAs("SIDEDEFS", wad.lastIndexOf(mapName), DoomSidedef.class, DoomSidedef.LENGTH));
				}
				break;

				case UDMF:
				{
					inspectSidedefs(options, udmf.getObjects("sidedef"));
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
					inspectSectors(options, wad.getDataAs("SECTORS", wad.lastIndexOf(mapName), DoomSector.class, DoomSector.LENGTH));
				}
				break;

				case UDMF:
				{
					inspectSectors(options, udmf.getObjects("sector"));
				}
				break;
					
			}
		}
		
		if (!options.skipSkies)
		{
			inspectMap(options, mapName);
		}
		
	}
	
	private static void inspectMap(Options options, String mapName)
	{
		Pair p = new Pair();
		getEpisodeAndMap(mapName, p);
		if (p.x == 0)
		{
			if (p.y >= 21)
			{
				if (!options.textureList.contains("SKY3"))
					options.textureList.add("SKY3");
			}
			else if (p.y >= 12)
			{
				if (!options.textureList.contains("SKY2"))
					options.textureList.add("SKY2");
			}
			else
			{
				if (!options.textureList.contains("SKY1"))
					options.textureList.add("SKY1");
			}
		}
		else if (p.x == 1)
		{
			if (!options.textureList.contains("SKY1"))
				options.textureList.add("SKY1");
		}
		else if (p.x == 2)
		{
			if (!options.textureList.contains("SKY2"))
				options.textureList.add("SKY2");
		}
		else if (p.x == 3)
		{
			if (!options.textureList.contains("SKY3"))
				options.textureList.add("SKY3");
		}
		else if (p.x == 4)
		{
			if (!options.textureList.contains("SKY4"))
				options.textureList.add("SKY4");
			if (!options.textureList.contains("SKY1"))
				options.textureList.add("SKY1");
		}
		else if (p.x == 5)
		{
			if (!options.textureList.contains("SKY3"))
				options.textureList.add("SKY3");
		}
	}
	
	// Adds sidedef textures to the list.
	private static void inspectSidedefs(Options options, DoomSidedef[] sidedefs)
	{
		for (DoomSidedef s : sidedefs)
		{
			addTexture(options, s.getTextureTop());
			addTexture(options, s.getTextureMiddle());
			addTexture(options, s.getTextureBottom());
		}
	}
	
	// Adds sidedef textures to the list.
	private static void inspectSidedefs(Options options, UDMFObject[] sidedefs)
	{
		for (UDMFObject s : sidedefs)
		{
			addTexture(options, s.getString(UDMFDoomSidedefAttributes.ATTRIB_TEXTURE_TOP, NameUtils.EMPTY_TEXTURE_NAME));
			addTexture(options, s.getString(UDMFDoomSidedefAttributes.ATTRIB_TEXTURE_MIDDLE, NameUtils.EMPTY_TEXTURE_NAME));
			addTexture(options, s.getString(UDMFDoomSidedefAttributes.ATTRIB_TEXTURE_BOTTOM, NameUtils.EMPTY_TEXTURE_NAME));
		}
	}
	
	// Adds sector textures to the list.
	private static void inspectSectors(Options options, DoomSector[] sectors)
	{
		for (DoomSector s : sectors)
		{
			addFlat(options, s.getTextureFloor());
			addFlat(options, s.getTextureCeiling());
		}
	}
	
	// Adds sector textures to the list.
	private static void inspectSectors(Options options, UDMFObject[] sectors)
	{
		for (UDMFObject s : sectors)
		{
			addFlat(options, s.getString(UDMFDoomSectorAttributes.ATTRIB_TEXTURE_FLOOR));
			addFlat(options, s.getString(UDMFDoomSectorAttributes.ATTRIB_TEXTURE_CEILING));
		}
	}
	
	private static void addTexture(Options options, String texture)
	{
		if (!options.textureList.contains(texture) && texture != null && !texture.trim().isEmpty() && !texture.equals("-"))
			options.textureList.add(texture);
	}

	private static void addFlat(Options options, String texture)
	{
		if (!options.flatList.contains(texture) && texture != null && !texture.trim().isEmpty())
			options.flatList.add(texture);
	}
	
}

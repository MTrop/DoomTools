package net.mtrop.doom.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
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
		
		public Options(PrintStream out, PrintStream err, Reader in)
		{
			this.out = out;
			this.err = err;
			this.in = new BufferedReader(in);

			this.help = false;
			this.version = false;
			this.quiet = false;
			this.outputTextures = false;
			this.outputFlats = false;
			this.skipSkies = false;
			this.wadFiles = new LinkedList<>();
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
		
		public void setHelp(boolean help) 
		{
			this.help = help;
		}
		
		public void setVersion(boolean version) 
		{
			this.version = version;
		}
		
		public void setQuiet(boolean quiet) 
		{
			this.quiet = quiet;
		}
		
		public void setOutputTextures(boolean outputTextures) 
		{
			this.outputTextures = outputTextures;
		}
		
		public void setOutputFlats(boolean outputFlats) 
		{
			this.outputFlats = outputFlats;
		}
		
		public void setSkipSkies(boolean skipSkies) 
		{
			this.skipSkies = skipSkies;
		}
		
		public void addWadFile(File file)
		{
			this.wadFiles.add(file);
		}

	}
	
	/**
	 * Program context.
	 */
	public static class Context
	{
		private SortedSet<String> textureList;
		private SortedSet<String> flatList;

		Context()
		{
			this.textureList = new TreeSet<>();
			this.flatList = new TreeSet<>();
		}
		
		public void addTexture(String texture)
		{
			this.textureList.add(texture);
		}

		public void addFlat(String flat)
		{
			this.flatList.add(flat);
		}

	}
	
	private static class Pair
	{
		public int x;
		public int y;
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
	private static void processPK3(Options options, Context context, String fileName, File f) throws ZipException, IOException
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
					inspectWAD(options, context, wm);
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
					processPK3(options, context, fileName + File.separator + ze.getName(), pk3);
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
	private static void processWAD(Options options, Context context, File f) throws WadException, IOException
	{
		options.println("# Inspecting " + f.getPath() + "...");
		WadFile wf = new WadFile(f);
		inspectWAD(options, context, wf);
		wf.close();
	}

	// Inspect WAD contents.
	private static void inspectWAD(Options options, Context context, Wad wad) throws IOException
	{
		String[] mapHeaders = MapUtils.getAllMapHeaders(wad);
		for (String mapName : mapHeaders)
			inspectMap(options, context, wad, mapName);
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
	private static void inspectMap(Options options, Context context, Wad wad, String mapName) throws IOException
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
					inspectSidedefs(context, wad.getDataAs("SIDEDEFS", wad.lastIndexOf(mapName), DoomSidedef.class, DoomSidedef.LENGTH));
				}
				break;
	
				case UDMF:
				{
					inspectSidedefs(context, udmf.getObjects("sidedef"));
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
					inspectSectors(context, wad.getDataAs("SECTORS", wad.lastIndexOf(mapName), DoomSector.class, DoomSector.LENGTH));
				}
				break;
	
				case UDMF:
				{
					inspectSectors(context, udmf.getObjects("sector"));
				}
				break;
					
			}
		}
		
		if (!options.skipSkies)
		{
			inspectMap(context, mapName);
		}
		
	}

	private static void inspectMap(Context context, String mapName)
	{
		Pair p = new Pair();
		getEpisodeAndMap(mapName, p);
		if (p.x == 0)
		{
			if (p.y >= 21)
			{
				if (!context.textureList.contains("SKY3"))
					context.textureList.add("SKY3");
			}
			else if (p.y >= 12)
			{
				if (!context.textureList.contains("SKY2"))
					context.textureList.add("SKY2");
			}
			else
			{
				if (!context.textureList.contains("SKY1"))
					context.textureList.add("SKY1");
			}
		}
		else if (p.x == 1)
		{
			if (!context.textureList.contains("SKY1"))
				context.textureList.add("SKY1");
		}
		else if (p.x == 2)
		{
			if (!context.textureList.contains("SKY2"))
				context.textureList.add("SKY2");
		}
		else if (p.x == 3)
		{
			if (!context.textureList.contains("SKY3"))
				context.textureList.add("SKY3");
		}
		else if (p.x == 4)
		{
			if (!context.textureList.contains("SKY4"))
				context.textureList.add("SKY4");
			if (!context.textureList.contains("SKY1"))
				context.textureList.add("SKY1");
		}
		else if (p.x == 5)
		{
			if (!context.textureList.contains("SKY3"))
				context.textureList.add("SKY3");
		}
	}

	// Adds sidedef textures to the list.
	private static void inspectSidedefs(Context context, DoomSidedef[] sidedefs)
	{
		for (DoomSidedef s : sidedefs)
		{
			addTexture(context, s.getTextureTop());
			addTexture(context, s.getTextureMiddle());
			addTexture(context, s.getTextureBottom());
		}
	}

	// Adds sidedef textures to the list.
	private static void inspectSidedefs(Context context, UDMFObject[] sidedefs)
	{
		for (UDMFObject s : sidedefs)
		{
			addTexture(context, s.getString(UDMFDoomSidedefAttributes.ATTRIB_TEXTURE_TOP, NameUtils.EMPTY_TEXTURE_NAME));
			addTexture(context, s.getString(UDMFDoomSidedefAttributes.ATTRIB_TEXTURE_MIDDLE, NameUtils.EMPTY_TEXTURE_NAME));
			addTexture(context, s.getString(UDMFDoomSidedefAttributes.ATTRIB_TEXTURE_BOTTOM, NameUtils.EMPTY_TEXTURE_NAME));
		}
	}

	// Adds sector textures to the list.
	private static void inspectSectors(Context context, DoomSector[] sectors)
	{
		for (DoomSector s : sectors)
		{
			addFlat(context, s.getTextureFloor());
			addFlat(context, s.getTextureCeiling());
		}
	}

	// Adds sector textures to the list.
	private static void inspectSectors(Context context, UDMFObject[] sectors)
	{
		for (UDMFObject s : sectors)
		{
			addFlat(context, s.getString(UDMFDoomSectorAttributes.ATTRIB_TEXTURE_FLOOR));
			addFlat(context, s.getString(UDMFDoomSectorAttributes.ATTRIB_TEXTURE_CEILING));
		}
	}

	private static void addTexture(Context context, String texture)
	{
		if (!context.textureList.contains(texture) && texture != null && !texture.trim().isEmpty() && !texture.equals("-"))
			context.textureList.add(texture);
	}

	private static void addFlat(Context context, String texture)
	{
		if (!context.flatList.contains(texture) && texture != null && !texture.trim().isEmpty())
			context.flatList.add(texture);
	}

	/**
	 * Reads command line arguments and sets options.
	 * @param options the options to alter. 
	 * @param args the argument args.
	 */
	public static void scanOptions(Options options, String[] args)
	{
		int i = 0;
		while (i < args.length)
		{
			String arg = args[i];
			if (arg.equals(SWITCH_HELP) || arg.equals(SWITCH_HELP2))
				options.setHelp(true);
			else if (arg.equals(SWITCH_VERSION))
				options.setVersion(true);
			else if (arg.equals(SWITCH_QUIET) || arg.equals(SWITCH_QUIET2))
				options.setQuiet(true);
			else if (arg.equals(SWITCH_TEXTURES) || arg.equals(SWITCH_TEXTURES2))
				options.setOutputTextures(true);
			else if (arg.equals(SWITCH_FLATS) || arg.equals(SWITCH_FLATS2))
				options.setOutputFlats(true);
			else if (arg.equals(SWITCH_NOSKIES))
				options.setSkipSkies(true);
			else
				options.addWadFile(new File(arg));
			i++;
		}
		
		if (!options.outputFlats && !options.outputTextures)
		{
			options.setOutputFlats(true);
			options.setOutputTextures(true);
		}		
	}
	
	/**
	 * Calls this program.
	 * @param options the program options.
	 * @param context the program context.
	 * @return the return code.
	 */
	public static int call(Options options, Context context)
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
					processWAD(options, context, f);
				else if (f.getName().toLowerCase().endsWith(".pk3"))
					processPK3(options, context, f.getPath(), f);
				else if (f.getName().toLowerCase().endsWith(".zip"))
					processPK3(options, context, f.getPath(), f);
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
		
		return atLeastOneError ? ERROR_BAD_FILE : ERROR_NONE;
	}

	public static void main(String[] args) throws IOException
	{
		Options options = new Options(System.out, System.err, Common.openTextStream(System.in));
		scanOptions(options, args);

		Context context = new Context();

		int ret;
		if ((ret = call(options, context)) != 0)
			System.exit(ret);
		
		if (options.help || options.version)
		{
			System.exit(0);
			return;
		}
		
		if (context.textureList.isEmpty())
		{
			options.out.println("# No textures.");
		}
		else
		{
			options.out.println("-TEXTURES");
			for (String t : context.textureList)
				options.out.println(t);
		}
	
		if (context.flatList.isEmpty())
		{
			options.println("# No flats.");
		}
		else
		{
			options.out.println("-FLATS");
			for (String f : context.flatList)
				options.out.println(f);
		}
	
		options.out.println("-END");
		
		System.exit(0);
	}
	
}

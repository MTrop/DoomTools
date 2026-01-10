package net.mtrop.doom.tools.doommake;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import com.blackrook.json.JSONConversionException;
import com.blackrook.json.JSONReader;

import net.mtrop.doom.Wad;
import net.mtrop.doom.WadBuffer;
import net.mtrop.doom.WadEntry;
import net.mtrop.doom.WadFile;
import net.mtrop.doom.exception.WadException;
import net.mtrop.doom.graphics.Flat;
import net.mtrop.doom.graphics.Palette;
import net.mtrop.doom.graphics.Picture;
import net.mtrop.doom.sound.MUS;
import net.mtrop.doom.struct.io.SerialReader;
import net.mtrop.doom.text.Text;
import net.mtrop.doom.texture.Animated;
import net.mtrop.doom.texture.CommonTextureList;
import net.mtrop.doom.texture.DoomTextureList;
import net.mtrop.doom.texture.PatchNames;
import net.mtrop.doom.texture.StrifeTextureList;
import net.mtrop.doom.texture.Switches;
import net.mtrop.doom.texture.TextureSet;
import net.mtrop.doom.tools.DoomMakeMain.ProjectType;
import net.mtrop.doom.tools.Version;
import net.mtrop.doom.tools.common.Utility;
import net.mtrop.doom.tools.doommake.generators.WADProjectGenerator;
import net.mtrop.doom.tools.struct.util.IOUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;
import net.mtrop.doom.util.GraphicUtils;
import net.mtrop.doom.util.MapUtils;
import net.mtrop.doom.util.TextureUtils;
import net.mtrop.doom.util.WadUtils;

/**
 * The WAD exploder process.
 * Takes a WAD file and epxlodes it into 
 * @author Matthew Tropiano
 */
public final class WADExploder 
{
	// Primary magic numbers.
	private static final byte[] MNUM_MIDI = "MThd".getBytes(StandardCharsets.US_ASCII);
	private static final byte[] MNUM_OGG  = "OggS".getBytes(StandardCharsets.US_ASCII);
	private static final byte[] MNUM_IT   = "IMPM".getBytes(StandardCharsets.US_ASCII);
	private static final byte[] MNUM_MUS  = MUS.MUS_ID;
	private static final byte[] MNUM_XM   = "Extended Module".getBytes(StandardCharsets.US_ASCII);

	private static final byte[] MNUM_RIFF = "RIFF".getBytes(StandardCharsets.US_ASCII);

	// Secondary or offset magic numbers.
	private static final byte[] MNUM_WAVE = "WAVE".getBytes(StandardCharsets.US_ASCII); // at 8
	private static final byte[] MNUM_MOD  = "M.K.".getBytes(StandardCharsets.US_ASCII); // at 0x438
	private static final byte[] MNUM_S3M  = "SCRM".getBytes(StandardCharsets.US_ASCII); // at 44
	
	private static final Set<String> JSON_NAMES = ObjectUtils.createCaseInsensitiveSortedSet(
		"GAMECONF", "DEMOLOOP", "SBARDEF", "SKYDEFS", "EXDEFS"
	);

	private static final Set<String> TXT_NAMES = ObjectUtils.createCaseInsensitiveSortedSet(
		"OPTIONS", "WADINFO", "CREDITS", "COMPLVL", "DSDACR", "DSDATC", "DECORATE", "TEXTURES",
		"ZSCRIPT", "MAPINFO", "EMAPINFO", "ZMAPINFO", "UMAPINFO", "LOCKDEFS"
	);
	
	private static final Set<String> ANSI_NAMES = ObjectUtils.createCaseInsensitiveSortedSet(
		"LOADING", "ENDOOM"
	);
	
	private static final Set<String> FLATGRAPHIC_NAMES = ObjectUtils.createCaseInsensitiveSortedSet(
		"TITLE", "E2END"
	);
	
	private static final WadEntry[] NO_ENTRIES = new WadEntry[0];
	
	/**
	 * Examines a WAD and figures out what type of project it is.
	 * @param wad the WAD to examine.
	 * @return the project type, or null if no project type.
	 */
	public static ProjectType getProjectTypeFromWAD(Wad wad)
	{
		// at least texture project
		if (wadContainsEntry(wad, "TEXTURE1", "TEXTURE2"))
		{
			// no maps?
			if (!wadContainsEntry(wad, "THINGS", "TEXTMAP"))
			{
				return ProjectType.TEXTURE;
			}
			else
			{
				return ProjectType.WAD;
			}
		}
		// anything else in it?
		else if (wad.getEntryCount() > 0)
		{
			return ProjectType.WAD;
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Examines a WAD and figures out what templates to generate.
	 * @param projectType the starting project type.
	 * @param wad the WAD to examine.
	 * @param outTemplateList the output template list for the templates.
	 * @throws WadException if the input WAD is malformed in some way.
	 */
	public static void getTemplatesFromWAD(ProjectType projectType, Wad wad, List<String> outTemplateList) throws WadException
	{
		Set<String> outTemplates = new HashSet<>();
		switch (projectType)
		{
			case TEXTURE:
				getTemplatesForTextureWAD(wad, outTemplates);
				break;
			case WAD:
				getTemplatesForWAD(wad, outTemplates);
				break;
		}
		
		outTemplateList.addAll(outTemplates);
	}
	
	private static void getTemplatesForTextureWAD(Wad wad, Set<String> outTemplates) throws WadException
	{
		Set<WadEntry> entrySet = new HashSet<>(Arrays.asList(wad.getAllEntries()));
		
		// start eliminating entries until we get potential asset lumps outside of a namespace.
		
		if (wadContainsEntry(wad, "ANIMATED", "SWITCHES"))
		{
			WadEntry entry;
			entry = wad.getEntry("ANIMATED");
			if (entry != null)
				entrySet.remove(entry);
			entry = wad.getEntry("SWITCHES");
			if (entry != null)
				entrySet.remove(entry);
			outTemplates.add(WADProjectGenerator.TEMPLATE_TEXTURES_BOOM);
		}
		
		int ffStart;
		if ((ffStart = wadIndexOfAny(wad, "FF_START", "F_START")) >= 0)
		{
			// clear out FF namespace.
			int ffEnd = wadIndexOfAny(wad, "FF_END", "F_END");
			if (ffEnd < 0)
				throw new WadException("Could not find end of flat namespace (F_END or FF_END).");
			
			for (int i = ffStart; i <= ffEnd; i++)
				entrySet.remove(wad.getEntry(i));
			
			if (!outTemplates.contains(WADProjectGenerator.TEMPLATE_TEXTURES_BOOM))
				outTemplates.add(WADProjectGenerator.TEMPLATE_TEXTURES);
		}

		int ppStart;
		if ((ppStart = wadIndexOfAny(wad, "PP_START", "P_START")) >= 0)
		{
			// clear out FF namespace.
			int ppEnd = wadIndexOfAny(wad, "PP_END", "P_END");
			if (ppEnd < 0)
				throw new WadException("Could not find end of patch namespace (P_END or PP_END).");
			
			for (int i = ppStart; i <= ppEnd; i++)
				entrySet.remove(wad.getEntry(i));

			if (!outTemplates.contains(WADProjectGenerator.TEMPLATE_TEXTURES_BOOM))
				outTemplates.add(WADProjectGenerator.TEMPLATE_TEXTURES);
		}
		
		int ssStart;
		if ((ssStart = wadIndexOfAny(wad, "SS_START", "S_START")) >= 0)
		{
			// clear out FF namespace.
			int ssEnd = wadIndexOfAny(wad, "SS_END", "S_END");
			if (ssEnd < 0)
				throw new WadException("Could not find end of sprite namespace (S_END or SS_END).");
			
			for (int i = ssStart; i <= ssEnd; i++)
				entrySet.remove(wad.getEntry(i));

			outTemplates.add(WADProjectGenerator.TEMPLATE_ASSETS);
		}

		// first break.
		if (outTemplates.contains(WADProjectGenerator.TEMPLATE_ASSETS) 
			&& (outTemplates.contains(WADProjectGenerator.TEMPLATE_TEXTURES) || outTemplates.contains(WADProjectGenerator.TEMPLATE_TEXTURES_BOOM)))
		{
			return;
		}
		
		int tIndex;
		if ((tIndex = wad.indexOf("TEXTURE1")) >= 0)
		{
			entrySet.remove(wad.getEntry(tIndex));
			if (!outTemplates.contains(WADProjectGenerator.TEMPLATE_TEXTURES_BOOM))
				outTemplates.add(WADProjectGenerator.TEMPLATE_TEXTURES);
		}
		
		if ((tIndex = wad.indexOf("TEXTURE2")) >= 0)
		{
			entrySet.remove(wad.getEntry(tIndex));
			if (!outTemplates.contains(WADProjectGenerator.TEMPLATE_TEXTURES_BOOM))
				outTemplates.add(WADProjectGenerator.TEMPLATE_TEXTURES);
		}
		
		if ((tIndex = wad.indexOf("PNAMES")) >= 0)
		{
			entrySet.remove(wad.getEntry(tIndex));
			if (!outTemplates.contains(WADProjectGenerator.TEMPLATE_TEXTURES_BOOM))
				outTemplates.add(WADProjectGenerator.TEMPLATE_TEXTURES);
		}
		
		if (wad.contains("C_START"))
		{
			outTemplates.add(WADProjectGenerator.TEMPLATE_ASSETS);
		}
		
		// second break.
		if (outTemplates.contains(WADProjectGenerator.TEMPLATE_ASSETS) 
			&& (outTemplates.contains(WADProjectGenerator.TEMPLATE_TEXTURES) || outTemplates.contains(WADProjectGenerator.TEMPLATE_TEXTURES_BOOM)))
		{
			return;
		}
		
		if (!entrySet.isEmpty())
		{
			outTemplates.add(WADProjectGenerator.TEMPLATE_ASSETS);
		}
		
	}
	
	private static void getTemplatesForWAD(Wad wad, Set<String> outTemplates) throws WadException
	{
		// "assets" template can be safely assumed.
		outTemplates.add(WADProjectGenerator.TEMPLATE_ASSETS);

		if (wadContainsEntry(wad, "ANIMATED", "SWITCHES"))
			outTemplates.add(WADProjectGenerator.TEMPLATE_TEXTURES_BOOM);
		else if (wadContainsEntry(wad, "TEXTURE1", "TEXTURE2", "PNAMES"))
			outTemplates.add(WADProjectGenerator.TEMPLATE_TEXTURES);

		if (wadContainsEntry(wad, "THINGS", "TEXTMAP"))
			outTemplates.add(WADProjectGenerator.TEMPLATE_MAPS);

		if (wadContainsEntry(wad, "DECOHACK"))
			outTemplates.add(WADProjectGenerator.TEMPLATE_DECOHACK);
		else if (wadContainsEntry(wad, "DEHACKED"))
			outTemplates.add(WADProjectGenerator.TEMPLATE_PATCH);
	}

	/**
	 * Explodes a WAD into a project scaffolding.
	 * @param log the logging stream for files written.
	 * @param wad the WAD to explode.
	 * @param targetDirectory the target directory.
	 * @param convertible if true, opt to output data to the conversion directories.
	 * @param palette the source palette.
	 * @throws IOException if a write error occurs.
	 */
	public static void explodeIntoProject(PrintStream log, Wad wad, File targetDirectory, boolean convertible, Palette palette) throws IOException
	{
		Set<WadEntry> entrySet = new HashSet<>(Arrays.asList(wad.getAllEntries()));

		explodeMapsIntoProject(log, entrySet, wad, targetDirectory);
		explodeTexturesIntoProject(log, entrySet, wad, targetDirectory, convertible, palette);
		explodePalettesIntoProject(log, entrySet, wad, targetDirectory, convertible);
		// TODO: Finish this.
	}
	
	private static void explodePatchIntoProject(PrintStream log, Set<WadEntry> entrySet, Wad wad, File targetDirectory) throws IOException
	{
		// TODO: Finish this.
	}
	
	private static void explodePalettesIntoProject(PrintStream log, Set<WadEntry> entrySet, Wad wad, File targetDirectory, boolean convertible) throws IOException
	{
		if (convertible)
		{
			File outPaletteDir = new File(targetDirectory.getPath() + "/src/convert/palettes");
			File outColormapDir = new File(targetDirectory.getPath() + "/src/convert/colormaps");
			File outColormap2Dir = new File(targetDirectory.getPath() + "/src/convert/colormaps-secondary");
			// TODO: Finish this.
		}
		else
		{
			File outPaletteDir = new File(targetDirectory.getPath() + "/src/assets/palettes");
			File outColormapDir = new File(targetDirectory.getPath() + "/src/assets/colormaps");
			File outColormap2Dir = new File(targetDirectory.getPath() + "/src/assets/colormaps-secondary");
			// TODO: Finish this.
		}
	}
	
	private static void explodeGraphicsIntoProject(PrintStream log, Set<WadEntry> entrySet, Wad wad, File targetDirectory, boolean convertible, Palette palette) throws IOException
	{
		// TODO: Finish this.
	}
	
	private static void explodeMusicIntoProject(PrintStream log, Set<WadEntry> entrySet, Wad wad, File targetDirectory) throws IOException
	{
		File outDir = new File(targetDirectory.getPath() + "/src/assets/music");
		// TODO: Finish this.
	}
	
	private static void explodeSoundsIntoProject(PrintStream log, Set<WadEntry> entrySet, Wad wad, File targetDirectory, boolean convertible) throws IOException
	{
		if (convertible)
		{
			File outDir = new File(targetDirectory.getPath() + "/src/convert/sounds");
			// TODO: Finish this.
		}
		else
		{
			File outDir = new File(targetDirectory.getPath() + "/src/assets/sounds");
			// TODO: Finish this.
		}
	}
	
	private static void explodeTexturesIntoProject(PrintStream log, Set<WadEntry> entrySet, Wad wad, File targetDirectory, boolean convertible, Palette palette) throws IOException
	{
		File outAnimFlatWad = new File(targetDirectory.getPath() + "/src/textures/animflats.wad");
		File outDefswani = new File(targetDirectory.getPath() + "/src/textures/defswani.txt");
		File outTexture1 = new File(targetDirectory.getPath() + "/src/textures/texture1.txt");
		File outTexture2 = new File(targetDirectory.getPath() + "/src/textures/texture2.txt");
		
		// Export ANIMFLATS
		Animated animated = wad.getDataAs("ANIMATED", Animated.class);
		if (animated != null)
		{
			WadEntry[] flatEntries = WadUtils.getEntriesInNamespace(wad, "FF");
			exportAnimFlatsToWAD(log, entrySet, wad, animated, flatEntries, outAnimFlatWad);
		}
		
		// Export Patches and Remaining Flats
		if (convertible)
		{
			File outPatchDir = new File(targetDirectory.getPath() + "/src/convert/patches");
			File outFlatDir = new File(targetDirectory.getPath() + "/src/convert/flats");

			WadEntry[] flatEntries = WadUtils.getEntriesInNamespace(wad, "FF");
			exportFlatGraphicsToDirectory(log, entrySet, palette, wad, flatEntries, outFlatDir);
			flatEntries = WadUtils.getEntriesInNamespace(wad, "F", Pattern.compile("F[1-9]_(START|END)"));
			exportFlatGraphicsToDirectory(log, entrySet, palette, wad, flatEntries, outFlatDir);

			WadEntry[] patchEntries = WadUtils.getEntriesInNamespace(wad, "PP");
			exportPatchGraphicsToDirectory(log, entrySet, palette, wad, patchEntries, outPatchDir);
			patchEntries = WadUtils.getEntriesInNamespace(wad, "P", Pattern.compile("P[1-9]_(START|END)"));
			exportPatchGraphicsToDirectory(log, entrySet, palette, wad, patchEntries, outPatchDir);			
		}
		else
		{
			File outPatchDir = new File(targetDirectory.getPath() + "/src/textures/patches");
			File outFlatDir = new File(targetDirectory.getPath() + "/src/textures/flats");

			WadEntry[] flatEntries = WadUtils.getEntriesInNamespace(wad, "FF");
			exportEntriesToDirectory(log, entrySet, wad, flatEntries, outFlatDir);
			flatEntries = WadUtils.getEntriesInNamespace(wad, "F", Pattern.compile("F[1-9]_(START|END)"));
			exportEntriesToDirectory(log, entrySet, wad, flatEntries, outFlatDir);
			
			WadEntry[] patchEntries = WadUtils.getEntriesInNamespace(wad, "PP");
			exportEntriesToDirectory(log, entrySet, wad, patchEntries, outPatchDir);
			patchEntries = WadUtils.getEntriesInNamespace(wad, "P", Pattern.compile("P[1-9]_(START|END)"));
			exportEntriesToDirectory(log, entrySet, wad, patchEntries, outPatchDir);			
		}
		
		// Export DEFSWANI
		Switches switches = wad.getDataAs("SWITCHES", Switches.class);
		if (animated != null && switches != null)
		{
			try (PrintWriter writer = new PrintWriter(new FileOutputStream(outDefswani)))
			{
				Utility.writeSwitchAnimatedTables(switches, animated, "# Generated by DoomMake v" + Version.DOOMMAKE, writer);
				log.println("Wrote `" + outDefswani.getPath() + "`.");
			}
			entrySet.remove(wad.getEntry("ANIMATED"));
			entrySet.remove(wad.getEntry("SWITCHES"));
		}
		
		// Export TEXTURE1/TEXTURE2
		PatchNames pnames = wad.getDataAs("PNAMES", PatchNames.class); 
		if (pnames != null)
		{
			byte[] texData = wad.getData("TEXTURE1");
			CommonTextureList<?> texture1, texture2;
			if (TextureUtils.isStrifeTextureData(texData))
			{
				texture1 = wad.getDataAs("TEXTURE1", StrifeTextureList.class);
				texture2 = wad.getDataAs("TEXTURE2", StrifeTextureList.class);
			}
			else
			{
				texture1 = wad.getDataAs("TEXTURE1", DoomTextureList.class);
				texture2 = wad.getDataAs("TEXTURE2", DoomTextureList.class);
			}
			
			if (texture1 != null)
			{
				TextureSet textureSet = new TextureSet(pnames, texture1);
				try (PrintWriter writer = new PrintWriter(new FileOutputStream(outTexture1)))
				{
					Utility.writeDEUTEXFile(textureSet, "; Generated by DoomMake v" + Version.DOOMMAKE, writer);
					log.println("Wrote `" + outTexture1.getPath() + "`.");
				}
				entrySet.remove(wad.getEntry("TEXTURE1"));
			}

			if (texture2 != null)
			{
				TextureSet textureSet = new TextureSet(pnames, texture2);
				try (PrintWriter writer = new PrintWriter(new FileOutputStream(outTexture2)))
				{
					Utility.writeDEUTEXFile(textureSet, "; Generated by DoomMake v" + Version.DOOMMAKE, writer);
					log.println("Wrote `" + outTexture2.getPath() + "`.");
				}
				entrySet.remove(wad.getEntry("TEXTURE2"));
			}
			entrySet.remove(wad.getEntry("PNAMES"));
		}
	}
	
	private static void explodeMapsIntoProject(PrintStream log, Set<WadEntry> entrySet, Wad wad, File targetDirectory) throws IOException
	{
		File outDir = new File(targetDirectory.getPath() + "/src/maps");
		
		for (String header : MapUtils.getAllMapHeaders(wad))
		{
			WadEntry[] entries = MapUtils.getMapEntries(wad, header);
			WadBuffer buffer = WadBuffer.extract(wad, entries);
			File outWad = new File(outDir.getPath() + "/" + (header.toUpperCase() + ".wad"));
			buffer.writeToFile(outWad);
			log.println("Wrote `" + outWad.getPath() + "`.");
			for (WadEntry e : entries)
				entrySet.remove(e);
		}
	}
	
	private static void exportEntriesToDirectory(PrintStream log, Set<WadEntry> entrySet, Wad wad, WadEntry[] entries, File dir) throws IOException
	{
		for (int i = 0; i < entries.length; i++) 
		{
			WadEntry entry = entries[i];
			if (!entrySet.contains(entry)) // catch flats exported to ANIMFLATS.WAD
				continue;
			
			byte[] data = wad.getData(entry);
			File out = new File(dir.getPath() + "/" + entry.getName() + "." + getExtensionFor(entry.getName(), data));
			try (FileOutputStream fos = new FileOutputStream(out))
			{
				fos.write(data);
				log.println("Wrote `" + out.getPath() + "`.");
			}
			entrySet.remove(entry);
		}
	}
	
	private static void exportAnimFlatsToWAD(PrintStream log, Set<WadEntry> entrySet, Wad wad, Animated animated, WadEntry[] flatEntries, File outAnimFlatWad) throws IOException
	{
		try (WadFile animFlatWad = new WadFile(outAnimFlatWad))
		{
			int ffEnd = animFlatWad.indexOf("FF_END");
			try (WadFile.Adder adder = animFlatWad.createAdder())
			{
				for (Animated.Entry ae : animated)
				{
					if (!ae.isTexture())
					{
						for (WadEntry animFlatEntry : scanEntriesForAnimatedRange(flatEntries, ae.getFirstName(), ae.getLastName()))
						{
							adder.addDataAt(ffEnd, animFlatEntry.getName(), wad.getData(animFlatEntry));
							entrySet.remove(animFlatEntry);
						}
					}
				}
			}
		}
	}

	private static void exportFlatGraphicsToDirectory(PrintStream log, Set<WadEntry> entrySet, Palette pal, Wad wad, WadEntry[] entries, File dir) throws IOException
	{
		for (int i = 0; i < entries.length; i++) 
		{
			WadEntry entry = entries[i];
			
			if (!entrySet.contains(entry)) // catch flats exported to ANIMFLATS.WAD
				continue;
			
			Flat flatData = wad.getDataAs(entry, Flat.class);
			File out = new File(dir.getPath() + "/" + entry.getName() + ".png");
			try (FileOutputStream fos = new FileOutputStream(out))
			{
				ImageIO.write(GraphicUtils.createImage(flatData, pal), "PNG", out);
				log.println("Wrote `" + out.getPath() + "`.");
			}
			entrySet.remove(entry);
		}
	}
	
	private static void exportPatchGraphicsToDirectory(PrintStream log, Set<WadEntry> entrySet, Palette pal, Wad wad, WadEntry[] entries, File dir) throws IOException
	{
		for (int i = 0; i < entries.length; i++) 
		{
			WadEntry entry = entries[i];
			Picture pictureData = wad.getDataAs(entry, Picture.class);
			File out = new File(dir.getPath() + "/" + entry.getName() + ".png");
			try (FileOutputStream fos = new FileOutputStream(out))
			{
				ImageIO.write(GraphicUtils.createImage(pictureData, pal), "PNG", out);
				log.println("Wrote `" + out.getPath() + "`.");
			}
			entrySet.remove(entry);
		}
	}
	
	private static WadEntry[] scanEntriesForAnimatedRange(WadEntry[] entries, String startName, String endName)
	{
		int startEntry = -1, endEntry = -1;
		for (int i = 0; i < entries.length; i++)
		{
			if (startEntry < 0 && entries[i].getName().equalsIgnoreCase(startName))
				startEntry = i;
			if (endEntry < 0 && entries[i].getName().equalsIgnoreCase(endName))
				endEntry = i;
			
			if (startEntry >= 0 && endEntry >= 0)
				break;
		}
		
		if (startEntry < 0 || endEntry < 0)
		{
			return NO_ENTRIES;
		}
		else
		{
			List<WadEntry> out = new ArrayList<>(endEntry - startEntry + 1);
			for (int i = startEntry; i <= endEntry; i++)
				out.add(entries[i]);
			return out.toArray(new WadEntry[out.size()]);
		}
	}
	
	private static int wadIndexOfAny(Wad wad, String ... entryNames)
	{
		int out;
		for (int i = 0; i < entryNames.length; i++)
			if ((out = wad.indexOf(entryNames[i])) >= 0)
				return out;
		return -1;
	}
	
	private static boolean wadContainsEntry(Wad wad, String ... entryNames)
	{
		for (int i = 0; i < entryNames.length; i++)
			if (wad.contains(entryNames[i]))
				return true;
		return false;
	}
	
	private static String getExtensionFor(String entryName, byte[] data)
	{
		if (entryName.equalsIgnoreCase("DEHACKED"))
			return "deh";
		if (entryName.equalsIgnoreCase("DECOHACK"))
			return "dh";

		if (entryName.equalsIgnoreCase("PLAYPAL"))
			return "pal";
		if (entryName.equalsIgnoreCase("COLORMAP"))
			return "cmp";
		if (entryName.equalsIgnoreCase("TRANTBL"))
			return "cmp";

		if (ANSI_NAMES.contains(entryName))
			return "ansi";
		if (JSON_NAMES.contains(entryName))
			return "json";
		if (TXT_NAMES.contains(entryName))
			return "txt";

		if (matchMagicNumber(data, 0, MNUM_MIDI))
			return "mid";
		if (matchMagicNumber(data, 0, MNUM_OGG))
			return "ogg";
		if (matchMagicNumber(data, 0, MNUM_IT))
			return "it";
		if (matchMagicNumber(data, 0, MNUM_MUS))
			return "mus";
		if (matchMagicNumber(data, 0, MNUM_XM))
			return "xm";
		
		if (matchMagicNumber(data, 0, MNUM_RIFF))
		{
			if (matchMagicNumber(data, 8, MNUM_WAVE))
				return "wav";
		}

		if (matchMagicNumber(data, 0x0438, MNUM_MOD))
			return "mod";
		if (matchMagicNumber(data, 44, MNUM_S3M))
			return "s3m";

		if (isJSONData(data))
			return "json";
		else if (isBinaryData(data))
			return "lmp";
		else
			return "txt";
	}

	private static boolean matchMagicNumber(byte[] data, int offset, byte[] magicNumber)
	{
		byte[] cmp = new byte[magicNumber.length];
		if (offset + magicNumber.length > data.length)
			return false;
		System.arraycopy(data, offset, cmp, 0, cmp.length);
		return Arrays.equals(cmp, magicNumber);
	}
	
	// Test if the data is musical data.
	private static boolean isMusicData(String entryName, byte[] data)
	{
		if (entryName.substring(0, 2).toUpperCase().startsWith("D_"))
			return true;
		
		if (matchMagicNumber(data, 0, MNUM_MIDI))
			return true;
		if (matchMagicNumber(data, 0, MNUM_OGG))
			return true;
		if (matchMagicNumber(data, 0, MNUM_IT))
			return true;
		if (matchMagicNumber(data, 0, MNUM_MUS))
			return true;
		if (matchMagicNumber(data, 0, MNUM_XM))
			return true;
		
		if (matchMagicNumber(data, 0, MNUM_RIFF))
		{
			if (matchMagicNumber(data, 8, MNUM_WAVE))
				return true;
		}

		if (matchMagicNumber(data, 0x0438, MNUM_MOD))
			return true;
		if (matchMagicNumber(data, 44, MNUM_S3M))
			return true;

		return false;
	}

	// Test if the data is Doom graphic data.
	private static boolean isGraphicData(byte[] data)
	{
		InputStream in = new ByteArrayInputStream(data);
		SerialReader sr = new SerialReader(SerialReader.LITTLE_ENDIAN);
		
		int w, h, ox, oy;
		
		try {
			w = sr.readShort(in);
			h = sr.readShort(in);
			ox = sr.readShort(in);
			oy = sr.readShort(in);
		} catch (IOException e) {
			return false;
		}

		// check for reasonable bounds
		if (w > 0 && w < 8192 && h > 0 && h < 8192 && Math.abs(ox) < 1024 && Math.abs(oy) < 1024)
			return true;
		
		return false;
	}

	// Test if the data is Doom sound data.
	private static boolean isDMXSoundData(String entryName, byte[] data)
	{
		InputStream in = new ByteArrayInputStream(data);
		SerialReader sr = new SerialReader(SerialReader.LITTLE_ENDIAN);
		
		try {
			int type = sr.readUnsignedShort(in);
			if (type != 3)
				return false;
			
			sr.readUnsignedShort(in); // sample rate
			long samples = sr.readUnsignedInt(in);
			if (data.length - 8 != samples)
				return false;
				
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	/**
	 * Checks if a file is a binary file via a simple data test.
	 * @param file the file to inspect.
	 * @return true if so, false if not.
	 */
	private static boolean isBinaryData(byte[] data)
	{
		return isBinaryData(new ByteArrayInputStream(data));
	}

	/**
	 * Checks if a file is a binary file via a simple data test.
	 * @param file the file to inspect.
	 * @return true if so, false if not.
	 */
	private static boolean isBinaryData(InputStream in)
	{
		byte[] buffer = new byte[512];
		try
		{
			int amt = in.read(buffer);
			for (int i = 0; i < amt; i++)
			{
				int value = (buffer[i] & 0x0ff); 
				if (value > 127 || (value >= 0 && value <= 5))
					return true;
			}
			return false;
		} 
		catch (FileNotFoundException e) 
		{
			return false;
		} 
		catch (IOException e) 
		{
			return false;
		} 
		catch (SecurityException e) 
		{
			return false;
		}
		
	}

	// Test if the data is JSON data.
	private static boolean isJSONData(byte[] data)
	{
		try {
			JSONReader.readJSON(new InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.UTF_8));
			return true;
		} catch (JSONConversionException e) {
			return false;
		} catch (IOException e) {
			// Shouldn't be thrown.
			return false;
		}
	}

}

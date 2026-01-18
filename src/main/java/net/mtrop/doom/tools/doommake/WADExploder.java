/*******************************************************************************
 * Copyright (c) 2020-2026 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.doommake;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.blackrook.json.JSONConversionException;
import com.blackrook.json.JSONReader;

import net.mtrop.doom.Wad;
import net.mtrop.doom.WadBuffer;
import net.mtrop.doom.WadEntry;
import net.mtrop.doom.WadFile;
import net.mtrop.doom.demo.Demo;
import net.mtrop.doom.exception.WadException;
import net.mtrop.doom.graphics.Colormap;
import net.mtrop.doom.graphics.Flat;
import net.mtrop.doom.graphics.PNGPicture;
import net.mtrop.doom.graphics.Palette;
import net.mtrop.doom.graphics.Picture;
import net.mtrop.doom.object.BinaryObject;
import net.mtrop.doom.sound.DMXSound;
import net.mtrop.doom.sound.MUS;
import net.mtrop.doom.struct.io.SerialReader;
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
import net.mtrop.doom.util.SoundUtils;
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

	private static final byte[] MNUM_ACS  = "ACS".getBytes(StandardCharsets.US_ASCII);

	// Secondary or offset magic numbers.
	private static final byte[] MNUM_WAVE = "WAVE".getBytes(StandardCharsets.US_ASCII); // at 8

	private static final byte[] MNUM_MOD1  = "M.K.".getBytes(StandardCharsets.US_ASCII); // at 0x438
	private static final byte[] MNUM_MOD2  = "4CHN".getBytes(StandardCharsets.US_ASCII); // at 0x438
	private static final byte[] MNUM_MOD3  = "6CHN".getBytes(StandardCharsets.US_ASCII); // at 0x438
	private static final byte[] MNUM_MOD4  = "8CHN".getBytes(StandardCharsets.US_ASCII); // at 0x438
	private static final byte[] MNUM_MOD5  = "4FLT".getBytes(StandardCharsets.US_ASCII); // at 0x438
	private static final byte[] MNUM_MOD6  = "8FLT".getBytes(StandardCharsets.US_ASCII); // at 0x438
	private static final byte[] MNUM_MOD7  = "FLT4".getBytes(StandardCharsets.US_ASCII); // at 0x438
	private static final byte[] MNUM_MOD8  = "FLT8".getBytes(StandardCharsets.US_ASCII); // at 0x438
	
	private static final byte[] MNUM_S3M  = "SCRM".getBytes(StandardCharsets.US_ASCII); // at 44
	
	private static final Set<String> JSON_NAMES = ObjectUtils.createCaseInsensitiveSortedSet(
		"GAMECONF", "DEMOLOOP", "SBARDEF", "SKYDEFS", "EXDEFS"
	);

	private static final Set<String> TXT_NAMES = ObjectUtils.createCaseInsensitiveSortedSet(
		"OPTIONS", "WADINFO", "CREDITS", "COMPLVL", "DSDACR", "DSDATC", "DECORATE", "TEXTURES",
		"ZSCRIPT", "MAPINFO", "EMAPINFO", "ZMAPINFO", "UMAPINFO", "LOCKDEFS"
	);
	
	private static final Set<String> ANSI_NAMES = ObjectUtils.createCaseInsensitiveSortedSet(
		"LOADING", "ENDOOM", "ENDTEXT"
	);
	
	private static final Set<String> PALETTE_NAMES = ObjectUtils.createCaseInsensitiveSortedSet(
		"PLAYPAL", "E2PAL"
	);
	
	private static final Set<String> COLORMAP_NAMES = ObjectUtils.createCaseInsensitiveSortedSet(
		"COLORMAP", "TINTTAB", "FOGMAP", 
		"TRANTBL0", "TRANTBL1", "TRANTBL2", "TRANTBL3", "TRANTBL4", "TRANTBL5", 
		"TRANTBL6", "TRANTBL7", "TRANTBL8", "TRANTBL9", "TRANTBLA", "TRANTBLB", 
		"TRANTBLC", "TRANTBLD", "TRANTBLE", "TRANTBLF", "TRANTBLG", "TRANTBLH", 
		"TRANTBLI", "TRANTBLJ", "TRANTBLK" 
	);

	// stuff that's actually binary data
	private static final Set<String> BINARY_NAMES = ObjectUtils.createCaseInsensitiveSortedSet(
		"SNDCURVE", "AUTOPAGE"
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
			// clear out PP namespace.
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
			// clear out SS namespace.
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

		if (wadContainsEntry(wad, "A_START", "A_END", "S_START", "S_END", "SS_START", "SS_END"))
			outTemplates.add(WADProjectGenerator.TEMPLATE_ASSETS);

		if (wadContainsEntry(wad, "ANIMATED", "SWITCHES"))
			outTemplates.add(WADProjectGenerator.TEMPLATE_TEXTURES_BOOM);
		else if (wadContainsEntry(wad, "TEXTURE1", "TEXTURE2", "PNAMES", "FF_START", "F_START", "FF_END", "F_END", "PP_START", "P_START", "PP_END", "P_END"))
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
		explodePatchIntoProject(log, entrySet, wad, targetDirectory);
		explodeMapsIntoProject(log, entrySet, wad, targetDirectory);
		explodeTexturesIntoProject(log, entrySet, wad, targetDirectory, convertible, palette);
		explodeSpritesIntoProject(log, entrySet, wad, targetDirectory, convertible, palette);
		explodePalettesIntoProject(log, entrySet, wad, targetDirectory, convertible, palette);
		explodeACSLibrariesIntoProject(log, entrySet, wad, targetDirectory);
		explodeInfoAndCreditsIntoProject(log, entrySet, wad, targetDirectory);
		explodeRemainingGlobalsIntoProject(log, entrySet, wad, targetDirectory, convertible, palette);
	}
	
	private static void explodePatchIntoProject(PrintStream log, Set<WadEntry> entrySet, Wad wad, File targetDirectory) throws IOException
	{
		WadEntry entry;
		if ((entry = wad.getEntry("DECOHACK")) != null)
		{
			File outDecoHackDir = new File(targetDirectory.getPath() + "/src/decohack");
			File out = new File(outDecoHackDir + "/main.dh");
			try (FileOutputStream fos = new FileOutputStream(out))
			{
				fos.write(wad.getData(entry));
				log.println("Wrote `" + out.getPath() + "`.");
			}
			entrySet.remove(entry);

			// If DECOHACK source found, don't bother exporting the DEHACKED.
			entry = wad.getEntry("DEHACKED");
			if (entry != null)
				entrySet.remove(entry);
		}
		else if ((entry = wad.getEntry("DEHACKED")) != null)
		{
			File outPatchDir = new File(targetDirectory.getPath() + "/src/patch");
			File out = new File(outPatchDir + "/dehacked.deh");
			try (FileOutputStream fos = new FileOutputStream(out))
			{
				fos.write(wad.getData(entry));
				log.println("Wrote `" + out.getPath() + "`.");
			}
			entrySet.remove(entry);
		}
	}
	
	private static void explodePalettesIntoProject(PrintStream log, Set<WadEntry> entrySet, Wad wad, File targetDirectory, boolean convertible, Palette palette) throws IOException
	{
		if (convertible)
		{
			File outPaletteDir = new File(targetDirectory.getPath() + "/src/convert/palettes");
			File outColormapDir = new File(targetDirectory.getPath() + "/src/convert/colormaps");
			File outColormap2Dir = new File(targetDirectory.getPath() + "/src/convert/colormaps-secondary");
			
			Palette primary = palette;

			for (String entryName : PALETTE_NAMES)
			{
				WadEntry entry = wad.getEntry(entryName);
				if (entry != null)
				{
					Palette[] playpal = wad.getDataAs(entry, Palette.class, Palette.LENGTH);
					File out = new File(outPaletteDir.getPath() + "/" + entryName + ".png");
					ImageIO.write(convertPaletteToImage(playpal), "PNG", out);
					log.println("Wrote `" + out.getPath() + "`.");
					entrySet.remove(entry);
					if (entryName.equals("PLAYPAL"))
						primary = playpal[0];
				}
			}

			for (String entryName : COLORMAP_NAMES)
			{
				WadEntry entry = wad.getEntry(entryName);
				if (entry != null)
				{
					Colormap[] colormap = wad.getDataAs(entry, Colormap.class, Colormap.LENGTH);
					File out = new File(outColormapDir.getPath() + "/" + entryName + ".png");
					ImageIO.write(convertColormapToImage(colormap, primary), "PNG", out);
					log.println("Wrote `" + out.getPath() + "`.");
					entrySet.remove(entry);
				}
			}
			
			int cstart = wad.indexOf("C_START");
			if (cstart >= 0)
			{
				entrySet.remove(wad.getEntry("C_START"));
				int cend = wad.indexOf("C_END");
				if (cend < 0)
					throw new WadException("Found C_START without C_END.");
				entrySet.remove(wad.getEntry("C_END"));
				
				for (WadEntry colormapEntry : wad.mapEntries(cstart + 1, cend - cstart - 1))
				{
					Colormap[] colormap = wad.getDataAs(colormapEntry, Colormap.class, Colormap.LENGTH);
					File out = new File(outColormap2Dir.getPath() + "/" + colormapEntry.getName() + ".png");
					ImageIO.write(convertColormapToImage(colormap, primary), "PNG", out);
					log.println("Wrote `" + out.getPath() + "`.");
					entrySet.remove(colormapEntry);
				}
			}
		}
		else
		{
			File outPaletteDir = new File(targetDirectory.getPath() + "/src/assets/palettes");
			File outColormapDir = new File(targetDirectory.getPath() + "/src/assets/colormaps");
			File outColormap2Dir = new File(targetDirectory.getPath() + "/src/assets/colormaps-secondary");
			
			List<WadEntry> entries;
			
			entries = new LinkedList<>(); 
			for (String entryName : PALETTE_NAMES)
			{
				WadEntry entry = wad.getEntry(entryName);
				if (entry != null)
					entries.add(entry);
			}
			exportEntriesToDirectory(log, entrySet, wad, entries, outPaletteDir);

			entries = new LinkedList<>(); 
			for (String entryName : COLORMAP_NAMES)
			{
				WadEntry entry = wad.getEntry(entryName);
				if (entry != null)
					entries.add(entry);
			}
			exportEntriesToDirectory(log, entrySet, wad, entries, outColormapDir);

			int cstart = wad.indexOf("C_START");
			if (cstart >= 0)
			{
				entrySet.remove(wad.getEntry("C_START"));
				int cend = wad.indexOf("C_END");
				if (cend < 0)
					throw new WadException("Found C_START without C_END.");
				entrySet.remove(wad.getEntry("C_END"));
				
				exportEntriesToDirectory(log, entrySet, wad, Arrays.asList(wad.mapEntries(cstart + 1, cend - cstart - 1)), outColormap2Dir);
			}
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
			WadEntry[] flatEntries = getEntriesInNamespace(wad, "F", "FF", Pattern.compile("F[1-9]_(START|END)"));
			exportAnimFlatsToWAD(log, entrySet, wad, animated, flatEntries, outAnimFlatWad);
			log.println("Amended `" + outAnimFlatWad.getPath() + "`.");
		}
		
		// Export Patches and Remaining Flats
		if (convertible)
		{
			File outPatchDir = new File(targetDirectory.getPath() + "/src/convert/patches");
			File outFlatDir = new File(targetDirectory.getPath() + "/src/convert/flats");
	
			WadEntry[] flatEntries = getEntriesInNamespace(wad, "F", "FF", Pattern.compile("F[1-9]_(START|END)"));
			exportFlatGraphicsToDirectory(log, entrySet, palette, wad, flatEntries, outFlatDir);
	
			WadEntry[] patchEntries = getEntriesInNamespace(wad, "P", "PP", Pattern.compile("P[1-9]_(START|END)"));
			exportPictureGraphicsToDirectory(log, entrySet, palette, wad, patchEntries, outPatchDir);
		}
		else
		{
			File outPatchDir = new File(targetDirectory.getPath() + "/src/textures/patches");
			File outFlatDir = new File(targetDirectory.getPath() + "/src/textures/flats");
	
			WadEntry[] flatEntries = getEntriesInNamespace(wad, "F", "FF", Pattern.compile("F[1-9]_(START|END)"));
			exportEntriesToDirectory(log, entrySet, wad, Arrays.asList(flatEntries), outFlatDir);
			
			WadEntry[] patchEntries = getEntriesInNamespace(wad, "P", "PP", Pattern.compile("P[1-9]_(START|END)"));
			exportEntriesToDirectory(log, entrySet, wad, Arrays.asList(patchEntries), outPatchDir);
		}
		
		WadEntry[] headers = WadUtils.withEntries(withoutNulls(
			wad.getEntry("FF_START"),
			wad.getEntry("F_START"),
			wad.getEntry("FF_END"),
			wad.getEntry("F_END"),
			wad.getEntry("PP_START"),
			wad.getEntry("P_START"),
			wad.getEntry("PP_END"),
			wad.getEntry("P_END")
		)).and(withoutNulls(
			wad.getEntry("F1_START"),
			wad.getEntry("F2_START"),
			wad.getEntry("F3_START"),
			wad.getEntry("F4_START"),
			wad.getEntry("F5_START"),
			wad.getEntry("F6_START"),
			wad.getEntry("F7_START"),
			wad.getEntry("F8_START"),
			wad.getEntry("F9_START"),
			wad.getEntry("F1_END"),
			wad.getEntry("F2_END"),
			wad.getEntry("F3_END"),
			wad.getEntry("F4_END"),
			wad.getEntry("F5_END"),
			wad.getEntry("F6_END"),
			wad.getEntry("F7_END"),
			wad.getEntry("F8_END"),
			wad.getEntry("F9_END")
		)).and(withoutNulls(
			wad.getEntry("P1_START"),
			wad.getEntry("P2_START"),
			wad.getEntry("P3_START"),
			wad.getEntry("P4_START"),
			wad.getEntry("P5_START"),
			wad.getEntry("P6_START"),
			wad.getEntry("P7_START"),
			wad.getEntry("P8_START"),
			wad.getEntry("P9_START"),
			wad.getEntry("P1_END"),
			wad.getEntry("P2_END"),
			wad.getEntry("P3_END"),
			wad.getEntry("P4_END"),
			wad.getEntry("P5_END"),
			wad.getEntry("P6_END"),
			wad.getEntry("P7_END"),
			wad.getEntry("P8_END"),
			wad.getEntry("P9_END")
		)).get();
		
		for (WadEntry h : headers)
			entrySet.remove(h);
		
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

	private static void explodeACSLibrariesIntoProject(PrintStream log, Set<WadEntry> entrySet, Wad wad, File targetDirectory) throws IOException
	{
		File outDir = new File(targetDirectory.getPath() + "/src/assets/acslib");

		WadEntry[] flatEntries = WadUtils.getEntriesInNamespace(wad, "A");
		exportEntriesToDirectory(log, entrySet, wad, Arrays.asList(flatEntries), outDir);
		for (WadEntry entry : WadUtils.withEntries(withoutNulls(wad.getEntry("A_START"), wad.getEntry("A_END"))).get())
			entrySet.remove(entry);
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

	private static void explodeSpritesIntoProject(PrintStream log, Set<WadEntry> entrySet, Wad wad, File targetDirectory, boolean convertible, Palette palette) throws IOException
	{
		if (convertible)
		{
			File outSpriteDir = new File(targetDirectory.getPath() + "/src/convert/sprites");
			WadEntry[] spriteEntries;
			
			spriteEntries = getEntriesInNamespace(wad, "S", "SS");
			exportPictureGraphicsToDirectory(log, entrySet, palette, wad, spriteEntries, outSpriteDir);
		}
		else
		{
			File outSpriteDir = new File(targetDirectory.getPath() + "/src/assets/sprites");
			WadEntry[] spriteEntries;

			spriteEntries = getEntriesInNamespace(wad, "S", "SS");
			exportEntriesToDirectory(log, entrySet, wad, Arrays.asList(spriteEntries), outSpriteDir);
		}
		
		WadEntry[] headers = WadUtils.withEntries(withoutNulls(
			wad.getEntry("SS_START"),
			wad.getEntry("S_START"),
			wad.getEntry("SS_END"),
			wad.getEntry("S_END")
		)).get();

		for (WadEntry h : headers)
			entrySet.remove(h);
	}

	private static void explodeInfoAndCreditsIntoProject(PrintStream log, Set<WadEntry> entrySet, Wad wad, File targetDirectory) throws IOException
	{
		File outDir = new File(targetDirectory.getPath() + "/src");
		WadEntry entry;
		
		entry = wad.getEntry("WADINFO");
		if (entry != null)
		{
			File out = new File(outDir.getPath() + "/wadinfo.txt");
			try (FileOutputStream fos = new FileOutputStream(out))
			{
				fos.write(wad.getData(entry));
				log.println("Wrote `" + out.getPath() + "`.");
			}
			entrySet.remove(entry);
		}
		
		entry = wad.getEntry("CREDITS");
		if (entry != null)
		{
			File out = new File(outDir.getPath() + "/credits.txt");
			try (FileOutputStream fos = new FileOutputStream(out))
			{
				fos.write(wad.getData(entry));
				log.println("Wrote `" + out.getPath() + "`.");
			}
			entrySet.remove(entry);
		}	
	}
	
	private static void explodeRemainingGlobalsIntoProject(PrintStream log, Set<WadEntry> entrySet, Wad wad, File targetDirectory, boolean convertible, Palette palette) throws IOException
	{
		StringBuilder flatGraphicData = new StringBuilder();
		
		for (WadEntry entry : entrySet)
		{
			String entryName = entry.getName();
			byte[] entryData = wad.getData(entry);
			
			if (entryData.length == 0) // skip markers
				continue;
			
			if (isMusicData(entryName, entryData))
			{
				File outMusicDir = new File(targetDirectory.getPath() + "/src/assets/music");
				File out = new File(outMusicDir.getPath() + "/" + sanitizeEntryName(entryName) + "." + getMusicExtensionFor(entryData));
				try (FileOutputStream fos = new FileOutputStream(out))
				{
					fos.write(wad.getData(entry));
					log.println("Wrote `" + out.getPath() + "`.");
				}
			}
			else if (isDMXSoundData(entryData))
			{
				if (isDigitalSoundData(entryData) && convertible)
				{
					File outSoundsDir = new File(targetDirectory.getPath() + "/src/convert/sounds");
					File out = new File(outSoundsDir.getPath() + "/" + sanitizeEntryName(entryName) + ".wav");
					DMXSound sound = BinaryObject.create(DMXSound.class, entryData);
					try {
						SoundUtils.writeSoundToFile(sound, Type.WAVE, out);
						log.println("Wrote `" + out.getPath() + "`.");
					} catch (IOException e) {
						throw e;
					} catch (UnsupportedAudioFileException e) {
						log.println("COULD NOT WRITE `" + out.getPath() + "`. INTERNAL ERROR.");
					}
				}
				else
				{
					File outSoundsDir = new File(targetDirectory.getPath() + "/src/assets/sounds");
					File out = new File(outSoundsDir.getPath() + "/" + sanitizeEntryName(entryName) + ".dmx");
					try (FileOutputStream fos = new FileOutputStream(out))
					{
						fos.write(wad.getData(entry));
						log.println("Wrote `" + out.getPath() + "`.");
					}
				}
				
			}
			else if (isRawScreenGraphicData(entryData)) // for Heretic/Hexen flat screens
			{
				if (convertible)
				{
					File outGraphicsDir = new File(targetDirectory.getPath() + "/src/convert/graphics");
					File out = new File(outGraphicsDir.getPath() + "/" + sanitizeEntryName(entryName) + ".png");

					Flat flat = new Flat(entryData.length / 200, 200);
					flat.fromBytes(entryData);

					try (FileOutputStream fos = new FileOutputStream(out))
					{
						ImageIO.write(GraphicUtils.createImage(flat, palette), "PNG", out);
						log.println("Wrote `" + out.getPath() + "`.");
						
						flatGraphicData.append(sanitizeEntryName(entryName) + " flat").append('\n');
					}
				}
				else
				{
					File outGraphicsDir = new File(targetDirectory.getPath() + "/src/assets/graphics");
					File out = new File(outGraphicsDir.getPath() + "/" + sanitizeEntryName(entryName) + ".lmp");
					try (FileOutputStream fos = new FileOutputStream(out))
					{
						fos.write(wad.getData(entry));
						log.println("Wrote `" + out.getPath() + "`.");
					}
				}
			}
			else if (isGraphicData(entryData))
			{
				if (convertible)
				{
					File outGraphicsDir = new File(targetDirectory.getPath() + "/src/convert/graphics");
					File out = new File(outGraphicsDir.getPath() + "/" + sanitizeEntryName(entryName) + ".png");
					Picture pictureData = BinaryObject.create(Picture.class, entryData);
					PNGPicture outpic = GraphicUtils.createPNGImage(pictureData, palette);
					outpic.setOffsetX(pictureData.getOffsetX());
					outpic.setOffsetY(pictureData.getOffsetY());
					try (FileOutputStream fos = new FileOutputStream(out))
					{
						fos.write(outpic.toBytes());
						log.println("Wrote `" + out.getPath() + "`.");
					}
				}
				else
				{
					File outGraphicsDir = new File(targetDirectory.getPath() + "/src/assets/graphics");
					File out = new File(outGraphicsDir.getPath() + "/" + sanitizeEntryName(entryName) + ".lmp");
					try (FileOutputStream fos = new FileOutputStream(out))
					{
						fos.write(wad.getData(entry));
						log.println("Wrote `" + out.getPath() + "`.");
					}
				}
			}
			else if (isDemoData(entryData))
			{
				File outDir = new File(targetDirectory.getPath() + "/src/assets/_global");
				File out = new File(outDir.getPath() + "/" + sanitizeEntryName(entryName) + ".lmp");
				try (FileOutputStream fos = new FileOutputStream(out))
				{
					fos.write(wad.getData(entry));
					log.println("Wrote `" + out.getPath() + "`.");
				}
			}
			else
			{
				File outDir = new File(targetDirectory.getPath() + "/src/assets/_global");
				File out = new File(outDir.getPath() + "/" + sanitizeEntryName(entryName) + "." + getExtensionFor(entryName, entryData));
				try (FileOutputStream fos = new FileOutputStream(out))
				{
					fos.write(wad.getData(entry));
					log.println("Wrote `" + out.getPath() + "`.");
				}
			}
		}

		// write exceptions for some graphic data
		if (flatGraphicData.length() > 0)
		{
			flatGraphicData.append("* graphic").append('\n');
			File outGraphicsDir = new File(targetDirectory.getPath() + "/src/convert/graphics");
			File out = new File(outGraphicsDir.getPath() + "/dimgconv.txt");
			try (Reader reader = new StringReader(flatGraphicData.toString()); Writer writer = new OutputStreamWriter(new FileOutputStream(out), StandardCharsets.UTF_8))
			{
				IOUtils.relay(reader, writer);
				writer.flush();
				log.println("Wrote `" + out.getPath() + "`.");
			}
		}
	}
	
	private static BufferedImage convertColormapToImage(Colormap[] colormap, Palette palette)
	{
		BufferedImage out = new BufferedImage(256, colormap.length, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < colormap.length; y++)
			for (int x = 0; x < 256; x++)
				out.setRGB(x, y, palette.getColorARGB(colormap[y].getPaletteIndex(x)));
		return out;
	}
	
	private static BufferedImage convertPaletteToImage(Palette[] palette)
	{
		BufferedImage out = new BufferedImage(256, palette.length, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < palette.length; y++)
			for (int x = 0; x < 256; x++)
				out.setRGB(x, y, palette[y].getColorARGB(x));
		return out;
	}
	
	private static void exportEntriesToDirectory(PrintStream log, Set<WadEntry> entrySet, Wad wad, Iterable<WadEntry> entries, File dir) throws IOException
	{
		for (WadEntry entry : entries) 
		{
			if (!entrySet.contains(entry)) // catch flats exported to ANIMFLATS.WAD
				continue;
			
			byte[] data = wad.getData(entry);
			File out = new File(dir.getPath() + "/" + sanitizeEntryName(entry.getName()) + "." + getExtensionFor(entry.getName(), data));
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
							adder.addDataAt(ffEnd++, animFlatEntry.getName(), wad.getData(animFlatEntry));
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
			
			byte[] data = wad.getData(entry);
			if (data.length == 0) // ignore markers.
			{
				entrySet.remove(entry);
				continue;
			}
			
			Flat flatData;
			if (data.length == 4) // Heretic F_SKY, which 2x2 for whatever reason
				flatData = new Flat(2, 2);
			else
				flatData = new Flat(64, 64);
			
			flatData.fromBytes(data);
			
			File out = new File(dir.getPath() + "/" + sanitizeEntryName(entry.getName()) + ".png");
			try (FileOutputStream fos = new FileOutputStream(out))
			{
				ImageIO.write(GraphicUtils.createImage(flatData, pal), "PNG", out);
				log.println("Wrote `" + out.getPath() + "`.");
			}
			entrySet.remove(entry);
		}
	}
	
	private static void exportPictureGraphicsToDirectory(PrintStream log, Set<WadEntry> entrySet, Palette pal, Wad wad, WadEntry[] entries, File dir) throws IOException
	{
		for (int i = 0; i < entries.length; i++) 
		{
			WadEntry entry = entries[i];
			
			byte[] data = wad.getData(entry);
			if (data.length == 0) // ignore markers.
			{
				entrySet.remove(entry);
				continue;
			}
			
			Picture pictureData = BinaryObject.create(Picture.class, data);
			PNGPicture outpic = GraphicUtils.createPNGImage(pictureData, pal);
			outpic.setOffsetX(pictureData.getOffsetX());
			outpic.setOffsetY(pictureData.getOffsetY());

			File out = new File(dir.getPath() + "/" + sanitizeEntryName(entry.getName()) + ".png");
			try (FileOutputStream fos = new FileOutputStream(out))
			{
				fos.write(outpic.toBytes());
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
	
	private static WadEntry[] getEntriesInNamespace(Wad wad, String name, String altName)
	{
		return getEntriesInNamespace(wad, name, altName, null);
	}
	
	private static WadEntry[] getEntriesInNamespace(Wad wad, String name, String altName, Pattern ignorePattern)
	{
		List<WadEntry> entryList = new ArrayList<>(128);
		
		int start = wadIndexOfAny(wad, name + "_START", altName + "_START");
		if (start >= 0)
		{
			int end = wadIndexOfAny(wad, name + "_END", altName + "_END");
			for (int i = start + 1; i < end; i++)
			{
				WadEntry entry = wad.getEntry(i);
				if (ignorePattern != null && ignorePattern.matcher(entry.getName()).matches())
					continue;
				entryList.add(entry);
			}
		}
		
		return entryList.toArray(new WadEntry[entryList.size()]);
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
	
	private static WadEntry[] withoutNulls(WadEntry ... entries)
	{
		List<WadEntry> out = new ArrayList<>(entries.length);
		for (int i = 0; i < entries.length; i++)
			if (entries[i] != null)
				out.add(entries[i]);
		return out.toArray(new WadEntry[out.size()]);
	}

	private static String sanitizeEntryName(String input)
	{
		return input.replace("\\", "^");
	}
	
	private static String getMusicExtensionFor(byte[] data)
	{
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

		if (matchMagicNumber(data, 0x0438, MNUM_MOD1))
			return "mod";
		if (matchMagicNumber(data, 0x0438, MNUM_MOD2))
			return "mod";
		if (matchMagicNumber(data, 0x0438, MNUM_MOD3))
			return "mod";
		if (matchMagicNumber(data, 0x0438, MNUM_MOD4))
			return "mod";
		if (matchMagicNumber(data, 0x0438, MNUM_MOD5))
			return "mod";
		if (matchMagicNumber(data, 0x0438, MNUM_MOD6))
			return "mod";
		if (matchMagicNumber(data, 0x0438, MNUM_MOD7))
			return "mod";
		if (matchMagicNumber(data, 0x0438, MNUM_MOD8))
			return "mod";
		
		if (matchMagicNumber(data, 44, MNUM_S3M))
			return "s3m";

		return "dat";
	}
	
	private static String getExtensionFor(String entryName, byte[] data)
	{
		if (entryName.equalsIgnoreCase("DEHACKED"))
			return "deh";
		if (entryName.equalsIgnoreCase("DECOHACK"))
			return "dh";

		if (matchMagicNumber(data, 0, MNUM_ACS))
			return "o";

		if (BINARY_NAMES.contains(entryName))
			return "lmp";
		if (PALETTE_NAMES.contains(entryName))
			return "pal";
		if (COLORMAP_NAMES.contains(entryName))
			return "cmp";

		if (ANSI_NAMES.contains(entryName))
			return "ansi";
		if (JSON_NAMES.contains(entryName))
			return "json";
		if (TXT_NAMES.contains(entryName))
			return "txt";

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

		if (matchMagicNumber(data, 0x0438, MNUM_MOD1))
			return true;
		if (matchMagicNumber(data, 0x0438, MNUM_MOD2))
			return true;
		if (matchMagicNumber(data, 0x0438, MNUM_MOD3))
			return true;
		if (matchMagicNumber(data, 0x0438, MNUM_MOD4))
			return true;
		if (matchMagicNumber(data, 0x0438, MNUM_MOD5))
			return true;
		if (matchMagicNumber(data, 0x0438, MNUM_MOD6))
			return true;
		if (matchMagicNumber(data, 0x0438, MNUM_MOD7))
			return true;
		if (matchMagicNumber(data, 0x0438, MNUM_MOD8))
			return true;

		if (matchMagicNumber(data, 44, MNUM_S3M))
			return true;

		return false;
	}

	// Test if the data is Doom DEMO data.
	private static boolean isDemoData(byte[] data)
	{
		// Maybe make this better one day but DEMOs are already inscrutable as hell.
		try {
			BinaryObject.create(Demo.class, data);
			return true;
		} catch (IllegalArgumentException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
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

	private static boolean isRawScreenGraphicData(byte[] entryData)
	{
		return isBinaryData(entryData) && entryData.length / 200 >= 320 && entryData.length % 200 == 0;
	}
	
	// Test if the data is Doom sound data.
	private static boolean isDMXSoundData(byte[] data)
	{
		InputStream in = new ByteArrayInputStream(data);
		SerialReader sr = new SerialReader(SerialReader.LITTLE_ENDIAN);
		
		try {
			int type = sr.readUnsignedShort(in);
			if (type == 0) // pc speaker
			{
				long samples = sr.readUnsignedShort(in);
				if (data.length - 4 != samples)
					return false;
			}
			else if (type == 3) // digital
			{
				sr.readUnsignedShort(in); // sample rate
				long samples = sr.readUnsignedInt(in);
				if (data.length - 8 != samples)
					return false;
			}
			else
			{
				return false;
			}
				
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	// Test if the data is Doom digital sound data.
	private static boolean isDigitalSoundData(byte[] data)
	{
		InputStream in = new ByteArrayInputStream(data);
		SerialReader sr = new SerialReader(SerialReader.LITTLE_ENDIAN);
		
		try {
			int type = sr.readUnsignedShort(in);
			if (type == 3) // digital
			{
				sr.readUnsignedShort(in); // sample rate
				long samples = sr.readUnsignedInt(in);
				if (data.length - 8 != samples)
					return false;
			}
			else
			{
				return false;
			}
				
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
		try {
			int amt = in.read(buffer);
			for (int i = 0; i < amt; i++)
			{
				int value = (buffer[i] & 0x0ff); 
				if (value > 127 || (value >= 0 && value <= 5))
					return true;
			}
			return false;
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e)	{
			return false;
		} catch (SecurityException e) {
			return false;
		}
	}

	// Test if the data is JSON data.
	private static boolean isJSONData(byte[] data)
	{
		try {
			JSONReader.readJSON(new InputStreamReader(new ByteArrayInputStream(data), StandardCharsets.UTF_8));
			return true;
		} catch (EmptyStackException e) {
			return false;
		} catch (JSONConversionException e) {
			return false;
		} catch (IOException e) {
			// Shouldn't be thrown.
			return false;
		}
	}

}

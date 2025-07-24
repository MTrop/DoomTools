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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import net.mtrop.doom.Wad;
import net.mtrop.doom.WadFile;
import net.mtrop.doom.exception.TextureException;
import net.mtrop.doom.exception.WadException;
import net.mtrop.doom.object.BinaryObject;
import net.mtrop.doom.struct.io.SerializerUtils;
import net.mtrop.doom.texture.Animated;
import net.mtrop.doom.texture.CommonTexture;
import net.mtrop.doom.texture.CommonTextureList;
import net.mtrop.doom.texture.DoomTextureList;
import net.mtrop.doom.texture.PatchNames;
import net.mtrop.doom.texture.StrifeTextureList;
import net.mtrop.doom.texture.Switches;
import net.mtrop.doom.texture.TextureSet;
import net.mtrop.doom.texture.TextureSet.Texture;
import net.mtrop.doom.tools.exception.OptionParseException;
import net.mtrop.doom.tools.gui.DoomToolsGUIMain;
import net.mtrop.doom.tools.gui.DoomToolsGUIMain.ApplicationNames;
import net.mtrop.doom.tools.struct.util.IOUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;
import net.mtrop.doom.tools.wtexport.TextureTables;
import net.mtrop.doom.util.NameUtils;
import net.mtrop.doom.util.TextureUtils;

/**
 * Main class for TexMove.
 * @author Matthew Tropiano
 */
public final class WTExportMain
{
	private static final int ERROR_NONE = 0;
	private static final int ERROR_BAD_FILE = 1;
	private static final int ERROR_NO_FILES = 2;
	private static final int ERROR_IOERROR = 3;
	private static final int ERROR_BAD_OPTIONS = 4;
	private static final int ERROR_UNKNOWN = -1;

	private static final Pattern PATCH_MARKER = Pattern.compile("P[0-9]*_(START|END)");
	private static final Pattern FLAT_MARKER = Pattern.compile("F[0-9]*_(START|END)");
	
	public static final String SWITCH_CHANGELOG = "--changelog";
	public static final String SWITCH_GUI = "--gui";
	public static final String SWITCH_GUI2 = "--gui-wtexscan";
	public static final String SWITCH_HELP1 = "--help";
	public static final String SWITCH_HELP2 = "-h";
	public static final String SWITCH_VERSION = "--version";
	public static final String SWITCH_BASE1 = "--base-wad";
	public static final String SWITCH_BASE2 = "-b";
	public static final String SWITCH_OUTPUT1 = "--output";
	public static final String SWITCH_OUTPUT2 = "-o";
	public static final String SWITCH_CREATE1 = "--create";
	public static final String SWITCH_CREATE2 = "-c";
	public static final String SWITCH_ADDITIVE1 = "--add";
	public static final String SWITCH_ADDITIVE2 = "-a";
	public static final String SWITCH_NULLTEX = "--null-texture";
	public static final String SWITCH_NOANIMATED = "--no-animated";
	public static final String SWITCH_NOSWITCH = "--no-switches";

	/**
	 * Context.
	 */
	public static class Options
	{
		private PrintStream stdout;
		private PrintStream stderr;
		private InputStream stdin;
		
		private boolean help;
		private boolean version;
		private boolean changelog;
		private boolean gui;
		private boolean wtexscan;
		private boolean quiet;

		/** Path to base wad. */
		private File baseWad;
		/** Path to output wad. */
		private File outWad;
		/** No animated. */
		private boolean noAnimated;
		/** No switches. */
		private boolean noSwitches;
		/** Additive output? */
		private Boolean additive;
		/** Null comparator. */
		private NullComparator nullComparator;
		/** File List. */
		private List<String> filePaths;
		/** List of texture names. */
		private List<String> extractTextureList; 
		/** List of flat names. */
		private List<String> extractFlatList; 

		private Options()
		{
			this.stdout = null;
			this.stderr = null;
			this.stdin = null;
			
			this.help = false;
			this.version = false;
			this.gui = false;
			this.changelog = false;
			this.wtexscan = false; 
			this.quiet = false;
			
			this.baseWad = null;
			this.outWad = null;
			this.noAnimated = false;
			this.noSwitches = false;
			this.additive = null;
			this.nullComparator = new NullComparator(null);
			this.filePaths = new ArrayList<>();
			this.extractTextureList = new ArrayList<>();
			this.extractFlatList = new ArrayList<>();
		}
		
		void println(Object msg)
		{
			if (!quiet)
				stdout.println(msg);
		}
		
		void printf(String fmt, Object... args)
		{
			if (!quiet)
				stdout.printf(fmt, args);
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

		char readChar() throws IOException
		{
			return (char)stdin.read();
		}
		
		public Options setStdout(OutputStream out) 
		{
			this.stdout = new PrintStream(out, true);;
			return this;
		}
		
		public Options setStderr(OutputStream err) 
		{
			this.stderr = new PrintStream(err, true);
			return this;
		}

		public Options setStdin(InputStream stdin) 
		{
			this.stdin = stdin;
			return this;
		}

		public Options setBaseWad(File baseWad) 
		{
			this.baseWad = baseWad;
			return this;
		}
		
		public Options setOutWad(File outWad) 
		{
			this.outWad = outWad;
			return this;
		}
		
		public Options setNoAnimated(boolean noAnimated) 
		{
			this.noAnimated = noAnimated;
			return this;
		}
		
		public Options setNoSwitches(boolean noSwitches) 
		{
			this.noSwitches = noSwitches;
			return this;
		}
		
		public Options setAdditive(Boolean additive) 
		{
			this.additive = additive;
			return this;
		}
		
		public Options setNullTexture(String texture) 
		{
			this.nullComparator = new NullComparator(texture);
			return this;
		}
		
		public Options addFilePath(String path)
		{
			filePaths.add(path);
			return this;
		}
	
		public Options addTexture(String name)
		{
			extractTextureList.add(name.toUpperCase());
			return this;
		}

		public Options addFlat(String name)
		{
			extractFlatList.add(name.toUpperCase());
			return this;
		}
	}

	private static class Context implements Callable<Integer>
	{
		/** Options. */
		private Options options;

		/** Base Unit. */
		private WadUnit baseUnit;
		/** WAD priority queue. */
		private List<WadUnit> wadPriority;

		/** List of texture names (need this list because order matters). */
		private List<String> textureList; 
		/** List of flat names (need this list because order matters). */
		private List<String> flatList; 
		/** Set of texture names (dupe test). */
		private Set<String> textureSet; 
		/** Set of flat names (dupe test). */
		private Set<String> flatSet; 

		private Context(Options options)
		{
			this.options = options;
			this.baseUnit = null;
			this.wadPriority = new LinkedList<WadUnit>();
			this.textureSet = new HashSet<>();
			this.flatSet = new HashSet<>();
			this.textureList = new ArrayList<>();
			this.flatList = new ArrayList<>();
		}

		/**
		 * Scans a WAD file, building a texture library profile of it.
		 * @param path the path to the WAD.
		 * @param isBase if true, does not add to the WAD priority, otherwise, it is set as the base.
		 * @return true if the WAD was read and accounted for, false on error.
		 */
		private boolean scanWAD(File path, boolean isBase)
		{
			options.printf("Scanning %s...\n", path);
			WadFile wf = openWadFile(path, false);
			if (wf == null)
				return false;
			
			WadUnit unit = new WadUnit(wf);
			
			try {
				if (!scanTexturesAndPNames(unit, wf))
					return false;
			} catch (IOException e) {
				options.printf("ERROR: \"%s\" could not be read.\n", path.getPath());
				return false;
			}
			
			options.println("    Scanning patch entries...");
			if (!scanNamespace("P", "PP", PATCH_MARKER, unit, wf, unit.patchIndices, null))
				return false;
			if (!scanNamespace("PP", "P", null, unit, wf, unit.patchIndices, null))
				return false;
			options.printf("        %d patches.\n", unit.patchIndices.size());
			options.println("    Scanning flat entries...");
			if (!scanNamespace("F", "FF", FLAT_MARKER, unit, wf, unit.flatIndices, unit.flatList))
				return false;
			if (!scanNamespace("FF", "F", null, unit, wf, unit.flatIndices, unit.flatList))
				return false;
			options.printf("        %d flats.\n", unit.flatIndices.size());
			options.println("    Scanning texture namespace entries...");
			if (!scanNamespace("TX", null, null, unit, wf, unit.texNamespaceIndices, unit.textureList))
				return false;
			options.printf("        %d namespace textures.\n", unit.texNamespaceIndices.size());
			
			for (TextureSet.Texture tex : unit.textureSet)
				if (!unit.textureList.contains(tex.getName()))
					unit.textureList.add(tex.getName());
		
			try {
				if (!scanAnimated(unit, wf))
					return false;
			} catch (IOException e) {
				options.printf("ERROR: \"%s\" could not be read: an ANIMATED or SWITCHES lump may be corrupt.\n", path.getPath());
				return false;
			}
			
			if (!isBase)
				wadPriority.add(unit);
			else
				baseUnit = unit;
			
			return true;
		}

		/**
		 * Scan for TEXTUREx and PNAMES.
		 * @param unit the WAD unit.
		 * @param wf the corresponding WadFile.
		 * @return true if successful, false if not.
		 * @throws IOException if a read error occurs.
		 */
		private boolean scanTexturesAndPNames(WadUnit unit, WadFile wf) throws IOException
		{
			options.println("    Scanning TEXTUREx/PNAMES...");
			
			PatchNames patchNames = null;
			CommonTextureList<?> textureList1 = null;
			CommonTextureList<?> textureList2 = null;
			byte[] textureData = null;
			
			// Scan TEXTURE1 =====================================
			
			try {
				textureData = wf.getData("TEXTURE1");
			} catch (WadException e) {
				options.printf("ERROR: %s: %s\n", wf.getFilePath(), e.getMessage());
				return false;
			} catch (IOException e) {
				options.printf("ERROR: %s: %s\n", wf.getFilePath(), e.getMessage());
				return false;
			}
			
			if (textureData != null)
			{
				// figure out if Strife or Doom Texture Lump.
				if (TextureUtils.isStrifeTextureData(textureData))
				{
					textureList1 = BinaryObject.create(StrifeTextureList.class, textureData);
					unit.strife = true;
				}
				else
				{
					textureList1 = BinaryObject.create(DoomTextureList.class, textureData);
					unit.strife = false;
				}
			
				unit.tex1names = new HashSet<String>(textureList1.size());
				for (CommonTexture<?> ct : textureList1)
					unit.tex1names.add(ct.getName());
			
				options.printf("        %d entries in TEXTURE1.\n", textureList1.size());
			}
			else
			{
				unit.tex1names = Collections.emptySet();
			}
		

			// Scan TEXTURE2 =====================================
		
			try {
				textureData = wf.getData("TEXTURE2");
			} catch (WadException e) {
				options.printf("ERROR: %s: %s\n", wf.getFilePath(), e.getMessage());
				return false;
			} catch (IOException e) {
				options.printf("ERROR: %s: %s\n", wf.getFilePath(), e.getMessage());
				return false;
			}
		
			if (textureData != null)
			{
				// figure out if Strife or Doom Texture Lump.
				if (TextureUtils.isStrifeTextureData(textureData))
				{
					textureList2 = BinaryObject.create(StrifeTextureList.class, textureData);
					unit.strife = true;
				}
				else
				{
					textureList2 = BinaryObject.create(DoomTextureList.class, textureData);
					unit.strife = false;
				}
				
				options.printf("        %d entries in TEXTURE2.\n", textureList2.size());
				unit.tex2exists = true;
			}
			
			try {
				if (!wf.contains("PNAMES"))
				{
					options.printf("ERROR: %s: TEXTUREx without PNAMES!\n", wf.getFilePath());
					return false;
				}
				patchNames = wf.getDataAs("PNAMES", PatchNames.class);
			} catch (WadException e) {
				options.printf("ERROR: %s: %s\n", wf.getFilePath(), e.getMessage());
				return false;
			} catch (IOException e) {
				options.printf("ERROR: %s: %s\n", wf.getFilePath(), e.getMessage());
				return false;
			}
			
			options.printf("        %d entries in PNAMES.\n", patchNames.size());
		
			if (textureList1 == null)
				textureList1 = unit.strife ? new StrifeTextureList() : new DoomTextureList();
			
			if (textureList2 != null)
				unit.textureSet = new TextureSet(patchNames, textureList1, textureList2);
			else
				unit.textureSet = new TextureSet(patchNames, textureList1);
			
			return true;
		}

		/**
		 * Scan for ANIMATED. Add combinations of textures to animated mapping.
		 * @param unit the WAD unit.
		 * @param wf the corresponding WadFile.
		 * @return true if successful, false if not.
		 * @throws IOException if a read error occurs.
		 */
		private boolean scanAnimated(WadUnit unit, WadFile wf) throws IOException
		{
			if (!options.noAnimated)
			{
				if (wf.contains("ANIMATED"))
				{
					options.println("    Scanning ANIMATED...");
					unit.animated = wf.getDataAs("ANIMATED", Animated.class);
					processAnimated(unit, unit.animated);
				}
				
				processAnimated(unit, TextureTables.ALL_ANIMATED);
			}
		
			if (!options.noSwitches)
			{
				if (wf.contains("SWITCHES"))
				{
					options.println("    Scanning SWITCHES...");
					unit.switches = wf.getDataAs("SWITCHES", Switches.class);
					
					for (Switches.Entry entry : unit.switches)
					{
						unit.switchMap.put(entry.getOffName(), entry.getOnName());
						unit.switchMap.put(entry.getOnName(), entry.getOffName());
					}
				}
			}
			
			return true;
		}

		private void processAnimated(WadUnit unit, Animated animated)
		{
			for (Animated.Entry entry : animated)
			{
				if (entry.isTexture())
				{
					String[] seq = getTextureSequence(unit, entry.getFirstName(), entry.getLastName());
					if (seq != null) for (String s : seq)
						unit.animatedTexture.put(s, seq);
				}
				else
				{
					String[] seq = getFlatSequence(unit, entry.getFirstName(), entry.getLastName());
					if (seq != null) for (String s : seq)
						unit.animatedFlat.put(s, seq);
				}
			}
		}

		/**
		 * Get animated texture sequence.
		 * @param unit the WAD unit.
		 * @param firstName the first name of the sequence.
		 * @param lastName the last name of the sequence.
		 * @return true if successful, false if not.
		 */
		private String[] getTextureSequence(WadUnit unit, String firstName, String lastName)
		{
			Deque<String> out = new LinkedList<>();
			int index = unit.textureList.indexOf(firstName);
			if (index >= 0)
			{
				int index2 = unit.textureList.indexOf(lastName);
				if (index2 >= 0)
				{
					int min = Math.min(index, index2);
					int max = Math.max(index, index2);
					for (int i = min; i <= max; i++)
						out.add(unit.textureList.get(i));
				}
				else
					return null;
			}
			else
				return null;
			
			String[] outList = new String[out.size()];
			out.toArray(outList);
			return outList;
		}

		/**
		 * Get animated flat sequence.
		 * @param unit the WAD unit.
		 * @param firstName the first name of the sequence.
		 * @param lastName the last name of the sequence.
		 * @return the textures between firstName and lastName.
		 */
		private String[] getFlatSequence(WadUnit unit, String firstName, String lastName)
		{
			Deque<String> out = new LinkedList<String>();
			int index = unit.flatList.indexOf(firstName);
			if (index >= 0)
			{
				int index2 = unit.flatList.indexOf(lastName);
				if (index2 >= 0)
				{
					int min = Math.min(index, index2);
					int max = Math.max(index, index2);
					for (int i = min; i <= max; i++)
						out.add(unit.flatList.get(i));
				}
				else
					return null;
			}
			else
				return null;
			
			String[] outList = new String[out.size()];
			out.toArray(outList);
			return outList;
		}

		/**
		 * Scans namespace entries.
		 * @param name the actual namespace prefix.
		 * @param equivName an equivalent namespace prefix.
		 * @param ignorePattern a RegEx pattern for what entries to ignore on scan.
		 * @param unit associated the WAD unit.
		 * @param wf the WadFile to scan.
		 * @param outputMap the output mapping of entry name to WAD entry index.
		 * @param outputList the output list of entry names (no duplicates).
		 * @return true if successful, false if a scan error occurs.
		 */
		private boolean scanNamespace(String name, String equivName, Pattern ignorePattern, WadUnit unit, WadFile wf, HashMap<String, Integer> outputMap, List<String> outputList)
		{
			// scan patch namespace
			int start = wf.indexOf(name+"_START");
			if (start < 0)
				start = wf.indexOf(equivName+"_START");
			
			if (start >= 0)
			{
				int end = wf.indexOf(name+"_END");
				if (end < 0)
					end = wf.indexOf(equivName+"_END");
				
				if (end >= 0)
				{
					for (int i = start + 1; i < end; i++)
					{
						String ename = wf.getEntry(i).getName();
						if (ignorePattern != null && ignorePattern.matcher(ename).matches())
							continue;
						if (!outputMap.containsKey(ename))
						{
							outputMap.put(ename, i);
							if (outputList != null)
								outputList.add(ename);
						}
					}
				}
				else
				{
					options.printf("ERROR: %s: %s_START without %s_END!\n", unit.wad, name.toUpperCase(), name.toUpperCase());
					return false;
				}
			}		
			
			return true;
		}

		/**
		 * Adds a string (usually some kind of entry) to a list and set, if it does not exist in the set.
		 * Addition adds it to the provided set and the end of the provided list.
		 * @param outputSet the output set.
		 * @param outputList the output list.
		 * @param entry the entry name.
		 * @return true if added, false if not (name exists in the set).
		 */
		private static boolean addToLists(Set<String> outputSet, List<String> outputList, String entry)
		{
			if (!outputSet.contains(entry))
			{
				outputSet.add(entry);
				outputList.add(entry);
				return true;
			}
			return false;
		}

		// Adds a texture to the main list (and associated ones, if possible).
		private void readAndAddTextures(String textureName)
		{
			// Textures can be flats (entries), too.
			if (!NameUtils.isValidEntryName(textureName))
			{
				options.errln("ERROR: Texture \""+textureName+"\" has an invalid name. Skipping.");
				return;
			}
			
			if (!textureSet.contains(textureName))
			{
				for (WadUnit unit : wadPriority)
				{
					// Check if animated, and if so, do NOT copy that texture over first - just copy over the animation sequence.
					// Copying the texture first can break animations from the middle.
					if (unit.animatedTexture.containsKey(textureName))
					{
						if (!options.noAnimated)
							readAndAddAnimatedTextures(unit, textureName);
					}
					
					if (!options.noSwitches)
						readAndAddSwitchTextures(unit, textureName);
					
					// Break early if added.
					if (textureSet.contains(textureName))
						break;
				}

				// attempt to add anyway - if it was already copied from the previous code, nothing happens.
				addToLists(textureSet, textureList, textureName);
			}
			
		}

		private void readAndAddAnimatedTextures(WadUnit unit, String textureName)
		{
			if (unit.animatedTexture.containsKey(textureName))
			{
				for (String s : unit.animatedTexture.get(textureName))
					addToLists(textureSet, textureList, s);

				// iterate again for associated switches.
				if (!options.noSwitches)
				{
					for (String s : unit.animatedTexture.get(textureName))
						readAndAddSwitchTextures(unit, s);
				}
			}
		}

		private void readAndAddSwitchTextures(WadUnit unit, String textureName)
		{
			if (unit.switchMap.containsKey(textureName))
			{
				if (addToLists(textureSet, textureList, textureName) && !options.noAnimated)
					readAndAddAnimatedTextures(unit, textureName);
				if (addToLists(textureSet, textureList, unit.switchMap.get(textureName)) && !options.noAnimated)
					readAndAddAnimatedTextures(unit, unit.switchMap.get(textureName));
			}
			else if (TextureTables.SWITCH_TABLE.containsKey(textureName))
			{
				if (addToLists(textureSet, textureList, textureName) && !options.noAnimated)
					readAndAddAnimatedTextures(unit, textureName);
				if (addToLists(textureSet, textureList, TextureTables.SWITCH_TABLE.get(textureName)) && !options.noAnimated)
					readAndAddAnimatedTextures(unit, TextureTables.SWITCH_TABLE.get(textureName));
			}
		}
		
		private void readAndAddFlats(String textureName)
		{
			// Check if animated, and if so, do NOT copy that texture over first - just copy over the animation sequence.
			// Copying the texture first can break animations from the middle.
			if (!options.noAnimated)
			{
				for (WadUnit unit : wadPriority)
				{
					if (unit.animatedFlat.containsKey(textureName))
						for (String s : unit.animatedFlat.get(textureName))
							addToLists(flatSet, flatList, s);
					
					// Break early if added.
					if (flatSet.contains(textureName))
						break;
				}
			}
			
			addToLists(flatSet, flatList, textureName);
		}

		/**
		 * Searches for the flat to extract. 
		 * @param unitList the list to sequentially search for the desired flat.
		 * @param flatName the flat name.
		 * @return the unit that contains it, or null if not found.
		 */
		private WadUnit searchForFlat(List<WadUnit> unitList, String flatName)
		{
			for (WadUnit unit : unitList)
			{
				if (unit.flatIndices.containsKey(flatName))
					return unit;
			}
			
			return null;
		}

		/** 
		 * Searches for the texture to extract. 
		 * @param unitList the list to sequentially search for the desired texture.
		 * @param textureName the texture name.
		 * @return the unit that contains it, or null if not found.
		 */
		private WadUnit searchForTexture(List<WadUnit> unitList, String textureName)
		{
			for (WadUnit unit : unitList)
			{
				if (unit.textureSet.contains(textureName))
					return unit;
			}
			
			return null;
		}

		/** 
		 * Searches for the texture to extract within the "texture" namespace. 
		 * @param unitList the list to sequentially search for the desired texture.
		 * @param textureName the texture name.
		 * @return the unit that contains it, or null if not found.
		 */
		private WadUnit searchForNamespaceTexture(List<WadUnit> unitList, String textureName)
		{
			for (WadUnit unit : unitList)
			{
				if (unit.texNamespaceIndices.containsKey(textureName))
					return unit;
			}
			
			return null;
		}

		/**
		 * Extracts flats and adds them to the provided {@link ExportSet}.
		 * @param exportSet the export set.
		 * @return true if successful, false on read error.
		 */
		private boolean extractFlats(ExportSet exportSet)
		{
			options.println("    Extracting flats...");
			for (String flat : flatList)
			{
				WadUnit unit = null;
				
				if ((unit = searchForFlat(wadPriority, flat)) != null)
				{
					// does a matching texture entry exist?
					if (unit.flatIndices.containsKey(flat))
					{
						Integer pidx = unit.flatIndices.get(flat);
						if (pidx != null)
						{
							try {
								options.printf("        Extracting flat %s (%s)...\n", flat, unit.wad.getFileName());
								EntryData data = new EntryData(flat, unit.wad.getData(pidx));
								exportSet.flatData.add(data);
								exportSet.flatHash.add(flat);
							} catch (IOException e) {
								options.printf("ERROR: %s: Could not read entry %s.", unit.wad.getFilePath(), flat);
								return false;
							}
						}
					}
				}
			}
			return true;
		}

		/**
		 * Extracts textures and adds them to the provided {@link ExportSet}.
		 * @param exportSet the export set.
		 * @return true if successful, false on read error.
		 */
		private boolean extractTextures(ExportSet exportSet)
		{
			options.println("    Extracting textures...");
			for (String textureName : textureList)
			{
				WadUnit unit = null;
				
				// found texture.
				if ((unit = searchForTexture(wadPriority, textureName)) != null)
				{
					// for figuring out if we've found a replaced/added patch.
					boolean foundPatches = false;
					
					TextureSet.Texture unitEntry = unit.textureSet.getTextureByName(textureName);
					
					for (int i = 0; i < unitEntry.getPatchCount(); i++)
					{
						TextureSet.Patch p = unitEntry.getPatch(i);
						String pname = p.getName();
						
						// does a matching patch exist?
						if (unit.patchIndices.containsKey(pname))
						{
							foundPatches = true;
							Integer pidx = unit.patchIndices.get(pname);
							if (pidx != null && !exportSet.patchHash.contains(pname))
							{
								try {
									options.printf("        Extracting patch %s (%s)...\n", pname, unit.wad.getFileName());
									EntryData data = new EntryData(pname, unit.wad.getData(pidx));
									exportSet.patchData.add(data);
									exportSet.patchHash.add(pname);
								} catch (IOException e) {
									options.printf("ERROR: %s: Could not read entry %s.\n", unit.wad.getFilePath(), pname);
									return false;
								}
							}
						}
					}
					
					// if we've found patches, extract the texture.
					if (foundPatches)
					{
						options.printf("        Copying texture %s (%s)...\n", textureName, unit.wad.getFileName());
						
						// check if potential overwrite.
						if (exportSet.textureSet.contains(textureName))
							exportSet.textureSet.removeTextureByName(textureName);
						
						moveTextureAndPatches(exportSet, textureName, exportSet.textureSet.createTexture(textureName), unitEntry);
					}
					// if texture is new, extract the texture.
					else if (!exportSet.textureSet.contains(textureName))
					{
						options.printf("        Copying texture %s (%s)...\n", textureName, unit.wad.getFileName());
						moveTextureAndPatches(exportSet, textureName, exportSet.textureSet.createTexture(textureName), unitEntry);
					}
					// if texture is not new, do a compare and replace.
					else if (!texturesAreEqual(exportSet.textureSet.getTextureByName(textureName), unitEntry))
					{
						options.printf("        Replacing texture %s (%s)...\n", textureName, unit.wad.getFileName());
						moveTextureAndPatches(exportSet, textureName, exportSet.textureSet.replaceTextureByName(textureName), unitEntry);
					}
					
				}
				// unit not found
				else if ((unit = searchForNamespaceTexture(wadPriority, textureName)) != null)
				{
					// does a matching texture entry exist?
					if (unit.texNamespaceIndices.containsKey(textureName))
					{
						Integer pidx = unit.texNamespaceIndices.get(textureName);
						if (pidx != null)
						{
							try {
								options.printf("        Extracting namespace texture %s (%s)...\n", textureName, unit.wad.getFileName());
								EntryData data = new EntryData(textureName, unit.wad.getData(pidx));
								exportSet.textureData.add(data);
							} catch (IOException e) {
								options.printf("ERROR: %s: Could not read entry %s.\n", unit.wad.getFilePath(), textureName);
								return false;
							}
						}
					}
				}
			}
			
			return true;
		}

		/**
		 * Moves a texture entry over to the export set. 
		 * @param exportSet the export set.
		 * @param textureName the texture name.
		 * @param newTexture the new texture entry.
		 * @param entry the texture entry.
		 */
		private void moveTextureAndPatches(ExportSet exportSet, String textureName, TextureSet.Texture newTexture, TextureSet.Texture entry)
		{
			newTexture.setHeight(entry.getHeight());
			newTexture.setWidth(entry.getWidth());
			for (TextureSet.Patch p : entry)
			{
				TextureSet.Patch newpatch = newTexture.createPatch(p.getName());
				newpatch.setOriginX(p.getOriginX());
				newpatch.setOriginY(p.getOriginY());
			}
			
			exportSet.textureHash.add(textureName);
		}
		
		/**
		 * Creates a hash of a texture entry.
		 * The purpose of this is to quickly (enough) determine texture composition equality between two textures.
		 * @param entry the entry to hash.
		 * @param algorithm the algorithm to hash with.
		 * @return the hash array of the result.
		 * @throws NoSuchAlgorithmException if the provided algorithm is unavailable or invalid. 
		 */
		private byte[] hashTextureEntry(TextureSet.Texture entry, String algorithm) throws NoSuchAlgorithmException
		{
			MessageDigest digest = MessageDigest.getInstance(algorithm);
						
			byte[] intbytes = new byte[4];
			
			// Digest texture itself.
			digest.update(entry.getName().getBytes());
			SerializerUtils.intToBytes(entry.getWidth(), SerializerUtils.LITTLE_ENDIAN, intbytes, 0);
			digest.update(intbytes);
			SerializerUtils.intToBytes(entry.getHeight(), SerializerUtils.LITTLE_ENDIAN, intbytes, 0);
			digest.update(intbytes);
			
			for (TextureSet.Patch p : entry)
			{
				// Digest patch.
				digest.update(p.getName().getBytes());
				SerializerUtils.intToBytes(p.getOriginX(), SerializerUtils.LITTLE_ENDIAN, intbytes, 0);
				digest.update(intbytes);
				SerializerUtils.intToBytes(p.getOriginY(), SerializerUtils.LITTLE_ENDIAN, intbytes, 0);
				digest.update(intbytes);
			}
			
			return digest.digest();
		}
		
		/**
		 * Checks if two texture entries are equal. 
		 * @param entry1 the first entry.
		 * @param entry2 the second entry.
		 * @return true if so, false if not.
		 */
		private boolean texturesAreEqual(TextureSet.Texture entry1, TextureSet.Texture entry2)
		{
			try {
				return MessageDigest.isEqual(hashTextureEntry(entry1, "MD5"), hashTextureEntry(entry2, "MD5"));
			} catch (NoSuchAlgorithmException e) {
				// Will not happen. MD5 is in all implementations of the JRE.
				return false;
			}
		}

		/**
		 * Merges ANIMATED and SWITCHES from inputs to an ExportSet, adding the entries if it finds the corresponding textures/flats.
		 * @param exportSet the export set.
		 * @return true if successful, false on read error.
		 */
		private boolean mergeAnimatedAndSwitches(ExportSet exportSet)
		{
			if (!options.noAnimated)
			{
				options.println("    Merging ANIMATED...");
				for (WadUnit unit : wadPriority)
				{
					// did we pull any animated textures? if so, copy the entries.
					
					for (Animated.Entry entry : unit.animated)
					{
						if (entry.isTexture())
						{
							if (exportSet.textureSet.contains(entry.getFirstName()))
							{
								exportSet.animatedData.addEntry(Animated.texture(entry.getLastName(), entry.getFirstName(), entry.getTicks(), entry.getAllowsDecals()));
								options.printf("        Texture %s to %s (%d tics)...\n", entry.getFirstName(), entry.getLastName(), entry.getTicks());
							}
						}
						else
						{
							if (exportSet.flatHash.contains(entry.getFirstName()))
							{
								exportSet.animatedData.addEntry(Animated.flat(entry.getLastName(), entry.getFirstName(), entry.getTicks()));
								options.printf("        Flat %s to %s (%d tics)...\n", entry.getFirstName(), entry.getLastName(), entry.getTicks());
							}
							else if (baseUnit.flatIndices.containsKey(entry.getFirstName()))
							{
								exportSet.animatedData.addEntry(Animated.flat(entry.getLastName(), entry.getFirstName(), entry.getTicks()));
								options.printf("        Flat %s to %s (%d tics)...\n", entry.getFirstName(), entry.getLastName(), entry.getTicks());
							}
						}
					}
				}
			}
			
			if (!options.noSwitches)
			{
				options.println("    Merging SWITCHES...");
				for (WadUnit unit : wadPriority)
				{
					// did we pull any switch textures? if so, copy the entries.
					for (Switches.Entry entry : unit.switches)
					{
						if (exportSet.textureSet.contains(entry.getOffName()))
						{
							exportSet.switchesData.addEntry(entry.getOffName(), entry.getOnName(), entry.getGame());
							options.printf("        Switch %s / %s (%s)...\n", entry.getOffName(), entry.getOnName(), entry.getGame().name());
						}
						else if (exportSet.textureSet.contains(entry.getOnName()))
						{
							exportSet.switchesData.addEntry(entry.getOffName(), entry.getOnName(), entry.getGame());
							options.printf("        Switch %s / %s (%s)...\n", entry.getOffName(), entry.getOnName(), entry.getGame().name());
						}
					}
				}
			}
			
			return true;
		}

		/**
		 * Dumps the contents of the ExportSet to a WAD file.
		 * @param exportSet the export set.
		 * @param wf the output WAD file.
		 * @return true.
		 * @throws IOException if a write error occurs.
		 */
		private boolean dumpToOutputWad(ExportSet exportSet, WadFile wf) throws IOException
		{
			options.println("Sorting entries...");
			exportSet.textureSet.sort(options.nullComparator);
			
			// Some data can be sorted - it's referred to by the stuff that has a specific order.
			Collections.sort(exportSet.patchData);
			
			options.println("Dumping entries...");
		
			List<CommonTextureList<?>> tlist = new ArrayList<>();
			PatchNames pnames;
			
			// if Strife-formatted source, export to Strife.
			if (baseUnit.strife)
			{
				pnames = new PatchNames();
				StrifeTextureList tex1 = new StrifeTextureList();
				StrifeTextureList tex2 = baseUnit.tex2exists ? new StrifeTextureList() : null;
				Set<String> tex1names = tex2 != null ? baseUnit.tex1names : null;
				exportSet.textureSet.export(pnames, tex1, tex2, tex1names);
				tlist.add(tex1);
				if (tex2 != null)
					tlist.add(tex2);
			}
			// if not, Doom format.
			else
			{
				pnames = new PatchNames();
				DoomTextureList tex1 = new DoomTextureList();
				DoomTextureList tex2 = baseUnit.tex2exists ? new DoomTextureList() : null;
				Set<String> tex1names = tex2 != null ? baseUnit.tex1names : null;
				exportSet.textureSet.export(pnames, tex1, tex2, tex1names);
				tlist.add(tex1);
				if (tex2 != null)
					tlist.add(tex2);
			}
			
			for (int i = 0; i < tlist.size(); i++)
			{
				String tentry = String.format("TEXTURE%01d", i+1);
				int idx = wf.indexOf(tentry);
				if (idx >= 0)
					wf.replaceEntry(idx, tlist.get(i).toBytes());
				else
					wf.addData(tentry, tlist.get(i).toBytes());
			}
			
			int idx = wf.indexOf("PNAMES");
			if (idx >= 0)
				wf.replaceEntry(idx, pnames.toBytes());
			else
				wf.addData("PNAMES", pnames.toBytes());
			
			if (!options.noAnimated && !exportSet.animatedData.isEmpty())
			{
				idx = wf.indexOf("ANIMATED");
				if (idx >= 0)
					wf.replaceEntry(idx, exportSet.animatedData);
				else
					wf.addData("ANIMATED", exportSet.animatedData);
			}
		
			if (!options.noSwitches && exportSet.switchesData.getEntryCount() > 0)
			{
				idx = wf.indexOf("SWITCHES");
				if (idx >= 0)
					wf.replaceEntry(idx, exportSet.switchesData);
				else
					wf.addData("SWITCHES", exportSet.switchesData);
			}
			
			dumpListToOutputWad(exportSet.patchData, "PP", wf);
			dumpListToOutputWad(exportSet.flatData, "FF", wf);
			dumpListToOutputWad(exportSet.textureData, "TX", wf);
			
			return true;
		}

		/**
		 * Bulk-writes a list of entries to a WAD file.
		 * @param entries the list of entry data to write, in order of writing.
		 * @param namespace the WAD namespace to write (affixes START and END).
		 * @param wf the output WAD file.
		 * @return true.
		 * @throws IOException if a write error occurs.
		 */
		private boolean dumpListToOutputWad(List<EntryData> entries, String namespace, WadFile wf) throws IOException
		{
			if (entries.size() == 0)
				return true;
			
			String[] names = new String[entries.size() + 2];
			byte[][] data = new byte[entries.size() + 2][];
			
			names[0] = namespace + "_START";
			data[0] = Wad.NO_DATA;
			
			for (int i = 0; i < entries.size(); i++)
			{
				names[1 + i] = entries.get(i).key;
				data[1 + i] = entries.get(i).value;
			}
		
			names[names.length - 1] = namespace + "_END";
			data[data.length - 1] = Wad.NO_DATA;
			
			try (WadFile.Adder adder = wf.createAdder())
			{
				for (int i = 0; i < names.length; i++)
					adder.addData(names[i], data[i]);
			}
			
			return true;
		}

		/**
		 * Extracts the necessary stuff for output.
		 * This is the entry point for pulling and extracting.
		 * @param options the Options object.
		 * @return true if successful, false if an error occurs.
		 */
		private boolean extractToOutputWad(Options options)
		{
			File outFile = options.outWad;
			WadFile outWadFile = options.additive ? openWadFile(outFile, true) : newWadFile(outFile);
			if (outWadFile == null)
				return false;
		
			File baseFile = options.baseWad;
			WadFile baseWadFile = openWadFile(baseFile, false);
			if (baseWadFile == null)
			{
				IOUtils.close(outWadFile);
				return false;
			}
		
			ExportSet exportSet = new ExportSet();
			try {
				exportSet.textureSet = TextureUtils.importTextureSet(baseWadFile);
				extractTextures(exportSet);
				extractFlats(exportSet);
				mergeAnimatedAndSwitches(exportSet);
				dumpToOutputWad(exportSet, outWadFile);
			} catch (TextureException | IOException e) {
				options.printf("ERROR: %s: %s\n", baseWadFile.getFilePath(), e.getMessage());
				return false;
			} finally {
				IOUtils.close(baseWadFile);
				IOUtils.close(outWadFile);
			}
			
			return true;
		}

		/** 
		 * Attempts to make a new WAD file.
		 * @param f the file path.
		 * @return the created, open WadFile.
		 */
		private WadFile newWadFile(File f)
		{
			WadFile outWad = null;
			try {
				outWad = WadFile.createWadFile(f);
			} catch (SecurityException e) {
				options.printf("ERROR: \"%s\" could not be created. Access denied.\n", f.getPath());
				return null;
			} catch (IOException e) {
				options.printf("ERROR: \"%s\" could not be created.\n", f.getPath());
				return null;
			}
			
			return outWad;
		}

		/** 
		 * Attempts to open an existing WAD file.
		 * @param f the file path.
		 * @param create if true, create it if it does not exist.
		 * @return the created, open WadFile.
		 */
		private WadFile openWadFile(File f, boolean create)
		{
			WadFile outWad = null;
			try {
				if (f.exists())
					outWad = new WadFile(f);
				else if (create)
					outWad = WadFile.createWadFile(f);
				else
					options.printf("ERROR: \"%s\" could not be opened.\n", f.getPath());
			} catch (SecurityException e) {
				options.printf("ERROR: \"%s\" could not be read. Access denied.\n", f.getPath());
				return null;
			} catch (WadException e) {
				options.printf("ERROR: \"%s\" is not a WAD file.\n", f.getPath());
				return null;
			} catch (IOException e) {
				options.printf("ERROR: \"%s\" could not be read.\n", f.getPath());
				return null;
			}
			
			return outWad;
		}

		/**
		 * Reads the texture/flat list from the STDIN mapping on Options.
		 * @throws OptionParseException if the list is misordered or malformed.
		 * @throws IOException if a read error occurs.
		 */
		private void readList() throws OptionParseException, IOException
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(options.stdin));
			
			final String TEXTURES = ":textures";
			final String FLATS = ":flats";
			final String END = ":end";
			
			final int STATE_NONE = 0;
			final int STATE_TEXTURES = 1;
			final int STATE_FLATS = 2;
			
			String line = null;
			int state = STATE_NONE;
			boolean keepGoing = true;
			
			while (keepGoing && ((line = reader.readLine()) != null))
			{
				line = line.trim();
				// skip blank lines
				if (line.length() == 0)
					continue;
				// skip commented lines.
				if (line.charAt(0) == '#')
					continue;
				
				if (line.equalsIgnoreCase(TEXTURES))
				{
					state = STATE_TEXTURES;
					continue;
				}
				else if (line.equalsIgnoreCase(FLATS))
				{
					state = STATE_FLATS;
					continue;
				}
				else if (line.equalsIgnoreCase(END))
				{
					keepGoing = false;
					continue;
				}
				
				switch (state)
				{
					case STATE_NONE:
						reader.close();
						throw new OptionParseException("ERROR: Name before ':textures' or ':flats'.");
					case STATE_TEXTURES:
						options.addTexture(line);
						break;
					case STATE_FLATS:
						options.addFlat(line);
						break;
				}
			}
			
			reader.close();
		}

		@Override
		public Integer call() 
		{
			if (options.gui)
			{
				try {
					DoomToolsGUIMain.startGUIAppProcess(options.wtexscan ? ApplicationNames.WTEXSCAN_WTEXPORT : ApplicationNames.WTEXPORT);
				} catch (IOException e) {
					options.stderr.println("ERROR: Could not start WTExport GUI!");
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
				changelog(options.stdout, "wtexport");
				return ERROR_NONE;
			}
			
			if (options.filePaths == null || options.filePaths.isEmpty())
			{
				options.errln("ERROR: No input WAD(s) specified.");
				usage(options.stdout);
				return ERROR_NO_FILES;
			}
		
			if (ObjectUtils.isEmpty(options.baseWad))
			{
				options.errln("ERROR: No base WAD specified.");
				usage(options.stdout);
				return ERROR_NO_FILES;
			}
		
			if (ObjectUtils.isEmpty(options.outWad))
			{
				options.errln("ERROR: No output WAD specified.");
				usage(options.stdout);
				return ERROR_NO_FILES;
			}
		
			if (options.additive == null)
			{
				options.errln("ERROR: Must specify --create or --add for output.");
				usage(options.stdout);
				return ERROR_NO_FILES;
			}

			if (!options.help && !options.version && !options.filePaths.isEmpty() && !ObjectUtils.isEmpty(options.outWad) && options.additive != null)
			{
				// Read list from Standard In		
				options.println("Read texture/flat list...");
				try {
					readList();
				} catch (OptionParseException e) {
					options.errln("ERROR: " + e.getLocalizedMessage());
					return ERROR_BAD_OPTIONS;
				} catch (IOException e) {
					options.errln("ERROR: " + e.getLocalizedMessage());
					return ERROR_BAD_OPTIONS;
				}
			}

			/* STEP 1 : Scan all incoming WADs so we know where crap is. */
			
			// scan base.
			if (!scanWAD(options.baseWad, true))
				return ERROR_BAD_FILE;
			
			// scan patches. 
			for (String f : options.filePaths)
				if (!scanWAD(new File(f), false))
					return ERROR_BAD_FILE;
		
			/* STEP 2 : Compile list of what we want. */
		
			for (String t : options.extractTextureList)
				readAndAddTextures(t);
			for (String f : options.extractFlatList)
				readAndAddFlats(f);
			
			/* STEP 3 : Extract the junk and put it in the output wad. */
		
			if (options.nullComparator.nullName != null)
				options.println("Using "+ options.nullComparator.nullName.toUpperCase() + " as the null texture in TEXTURE1...");
			
			if (!extractToOutputWad(options))
				return ERROR_BAD_FILE;
			
			options.println("Done!");
			return ERROR_NONE;
		}
		
	}
	
	/**
	 * Comparator class for Null Texture name (shuffles the null texture to the top). 
	 */
	private static class NullComparator implements Comparator<Texture>
	{
		/** Null texture set. */
		private static final Set<String> NULL_NAMES = new HashSet<String>()
		{
			private static final long serialVersionUID = 7124019549131283301L;
			{
				add("AASTINKY");
				add("AASHITTY");
				add("BADPATCH");
				add("ABADONE");
			}
		};
		
		private String nullName;
		
		private NullComparator(String nullName)
		{
			this.nullName = nullName;
		}
		
		@Override
		public int compare(Texture o1, Texture o2)
		{
			if (nullName == null)
			{
				return 
					NULL_NAMES.contains(o1.getName()) ? -1 :
					NULL_NAMES.contains(o2.getName()) ? 1 :
					0;
			}
			else
			{
				return 
					o1.getName().equalsIgnoreCase(nullName) ? -1 :
					o2.getName().equalsIgnoreCase(nullName) ? 1 :
					0;
			}
		}
		
	}

	private static class ExportSet
	{
		private Set<String> patchHash;
		private Set<String> flatHash;
		private Set<String> textureHash;
		
		private TextureSet textureSet;
		private List<EntryData> patchData;
		private List<EntryData> flatData;
		private List<EntryData> textureData;
		private Animated animatedData;
		private Switches switchesData;
		
		public ExportSet()
		{
			this.flatHash = new HashSet<>();
			this.patchHash = new HashSet<>();
			this.textureHash = new HashSet<>();
			
			this.textureSet = null;
			this.patchData = new ArrayList<EntryData>();
			this.flatData = new ArrayList<EntryData>();
			this.textureData = new ArrayList<EntryData>();
			this.animatedData = new Animated();
			this.switchesData = new Switches();
		}
	}
	
	/** Pair for grouping WAD and entry index. */
	private static class EntryData implements Comparable<EntryData>
	{
		private String key;
		private byte[] value;
		
		EntryData(String key, byte[] value)
		{
			this.key = key;
			this.value = value;
		}
		
		@Override
		public int compareTo(EntryData o)
		{
			return key.compareTo(o.key);
		}
		
	}
	
	/**
	 * A WAD-Texture unit that is stored in a queue
	 * for figuring out from where textures should be extracted.
	 */
	private static class WadUnit
	{
		/** WAD path. */
		WadFile wad; 

		/** Names in TEXTURE1. */
		Set<String> tex1names;
		/** Texture Set. */
		TextureSet textureSet;
		/** Texture 2 */
		boolean tex2exists;
		/** Is Strife-formatted list? */
		boolean strife;
		
		/** Patch ENTRY indices. */
		HashMap<String, Integer> patchIndices; 		
		/** Flat ENTRY indices. */
		HashMap<String, Integer> flatIndices;
		/** Namespace texture ENTRY indices. */
		HashMap<String, Integer> texNamespaceIndices; 

		/** Animated texture map. */
		HashMap<String, String[]> animatedTexture;
		/** Animated texture map. */
		HashMap<String, String[]> animatedFlat;
		/** Switches map. */
		HashMap<String, String> switchMap;

		// NOTE: The following lists are NOT sorted for a reason.
		// In order to grab the correct textures between the indices of the Animated
		// textures, the order must be preserved ALWAYS!

		// In the future, there should maybe be a better type of check to speed some things
		// up, but I'm leaving it for now since I'm really f'in tired and it works. -MTrop
		
		/** Texture names. */
		List<String> textureList;
		/** Flat names. */
		List<String> flatList;
		
		Animated animated;
		Switches switches;
		
		private WadUnit(WadFile file)
		{
			this.wad = file;
			this.textureSet = null;
			this.tex1names = null;
			this.tex2exists = false;
			this.flatIndices = new HashMap<String, Integer>();
			this.patchIndices = new HashMap<String, Integer>();
			this.texNamespaceIndices = new HashMap<String, Integer>();
			this.animatedTexture = new HashMap<String, String[]>();
			this.animatedFlat = new HashMap<String, String[]>();
			this.switchMap = new HashMap<String, String>();
			this.textureList = new ArrayList<>();
			this.flatList = new ArrayList<>();
			this.animated = new Animated();
			this.switches = new Switches();
		}
	}
	
	/**
	 * Reads command line arguments and sets options.
	 * @param out the standard output print stream.
	 * @param err the standard error print stream. 
	 * @param in the standard input buffered reader.
	 * @param args the arguments.
	 * @return the parsed options.
	 * @throws OptionParseException if an error happens parsing the arguments.
	 */
	public static Options options(PrintStream out, PrintStream err, InputStream in, String ... args) throws OptionParseException
	{
		Options options = new Options();
		options.stdout = out;
		options.stderr = err;
		options.stdin = in;
	
		final int STATE_INIT = 0;
		final int STATE_BASE = 1;
		final int STATE_OUT = 2;
		final int STATE_NULLTEX = 3;
		
		int state = STATE_INIT;
		int i = 0;
		while (i < args.length)
		{
			String arg = args[i];
			switch (state)
			{
				case STATE_INIT:
				{
					if (arg.equals(SWITCH_HELP1) || arg.equals(SWITCH_HELP2))
						options.help = true;
					else if (arg.equals(SWITCH_VERSION))
						options.version = true;
					else if (arg.equals(SWITCH_GUI))
						options.gui = true;
					else if (arg.equals(SWITCH_GUI2))
					{
						options.gui = true;
						options.wtexscan = true;
					}
					else if (arg.equalsIgnoreCase(SWITCH_CHANGELOG))
						options.changelog = true;
					else if (arg.equals(SWITCH_NOANIMATED))
						options.setNoAnimated(true);
					else if (arg.equals(SWITCH_NOSWITCH))
						options.setNoSwitches(true);
					else if (arg.equals(SWITCH_CREATE1) || arg.equals(SWITCH_CREATE2))
						options.setAdditive(false);
					else if (arg.equals(SWITCH_ADDITIVE1) || arg.equals(SWITCH_ADDITIVE2))
						options.setAdditive(true);
					else if (arg.equals(SWITCH_BASE1) || arg.equals(SWITCH_BASE2))
						state = STATE_BASE;
					else if (arg.equals(SWITCH_OUTPUT1) || arg.equals(SWITCH_OUTPUT2))
						state = STATE_OUT;
					else if (arg.equals(SWITCH_NULLTEX))
						state = STATE_NULLTEX;
					else
						options.addFilePath(arg);
				}
				break;
			
				case STATE_BASE:
				{
					options.setBaseWad(new File(arg));
					state = STATE_INIT;
				}
				break;
				
				case STATE_OUT:
				{
					options.setOutWad(new File(arg));
					state = STATE_INIT;
				}
				break;
				
				case STATE_NULLTEX:
				{
					options.setNullTexture(arg);
					state = STATE_INIT;
				}
				break;
			}
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
	
	public static void main(String[] args)
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
		out.println("WTEXport v" + Version.WTEXPORT + " by Matt Tropiano (using DoomStruct v" + Version.DOOMSTRUCT + ")");
	}

	/**
	 * Prints the usage.
	 * @param out the print stream to print to.
	 */
	private static void usage(PrintStream out)
	{
		out.println("Usage: wtexport [--help | -h | --version]");
		out.println("                [files] --base-wad [base] --output [target] [--create | --add] [switches]");
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
	
	// Prints the usage message.
	private static void help(PrintStream out)
	{
		out.println("    --help                Prints help and exits.");
		out.println("    -h");
		out.println();
		out.println("    --version             Prints version, and exits.");
		out.println();
		out.println("    --changelog           Prints the changelog, and exits.");
		out.println();
		out.println("    --gui                 Starts the GUI version of this program.");
		out.println();
		out.println("    --gui-wtexscan        Starts the GUI version of this program that also");
		out.println("                          uses WTexScan to generate the list.");
		out.println();
		out.println("[files]:");
		out.println("    <filenames>           A valid WAD file (that contains the textures to");
		out.println("                          extract). Accepts wildcards for multiple WAD files.");
		out.println();
		out.println("    --base-wad [base]     The WAD file to use for reference for extraction.");
		out.println("    -b [base]             Any texture resources found in this file are NOT");
		out.println("                          extracted, except for the TEXTUREx and PNAMES lumps");
		out.println("                          to use as a base. (Usually an IWAD)");
		out.println();
		out.println("    --output [wad]        The output WAD file.");
		out.println("    -o [wad]");
		out.println();
		out.println("    --create              If specified, the specified output WAD file is");
		out.println("    -c                    created, TEXTUREx and PNAMES lumps are overwritten, and the");
		out.println("                          extracted contents are APPENDED to it. If the output");
		out.println("                          file already exists, it is replaced COMPLETELY");
		out.println("                          (be careful)!");
		out.println();
		out.println("    --add                 If specified, if the output WAD exists, the target's");
		out.println("    -a                    TEXTUREx and PNAMES lumps are overwritten, and the");
		out.println("                          extracted contents are APPENDED to it. If the WAD");
		out.println("                          does not exist, it is created.");
		out.println();
		out.println("[switches]:");
		out.println("    --null-texture [tex]  If specified, the next argument is the null");
		out.println("                          texture that is always sorted first.");
		out.println();
		out.println("    --no-animated         If specified, do not include other textures in");
		out.println("                          a texture's animation sequence, and ignore ANIMATED");
		out.println("                          lumps.");
		out.println();
		out.println("    --no-switches         If specified, do not include other textures in");
		out.println("                          a texture's switch sequence, and ignore SWITCHES");
		out.println("                          lumps.");
		out.println();
		out.println("Input List");
		out.println("==========");
		out.println();
		out.println("The input list of textures and flats are a newline-separated list read from");
		out.println("STDIN, with the texture and flat list separated by \":textures\" and \":flats\"");
		out.println("entries, terminated by \":end\". Blank lines and lines prefixed with \"#\"");
		out.println("are ignored.");
		out.println();
		out.println("Example:");
		out.println();
		out.println(":textures");
		out.println("GRENWAL1");
		out.println("GRENWAL2");
		out.println("GRENWAL3");
		out.println("GRENWAL4");
		out.println("RAILING");
		out.println("XXMETL02");
		out.println("XXMETL03");
		out.println();
		out.println(":flats");
		out.println("# This is a comment.");
		out.println("ICHOR01");
		out.println("ICHOR02");
		out.println("ICHOR03");
		out.println("ICHOR04");
		out.println();
		out.println(":end");
		out.println();
		out.println("The utilities WTEXLIST and WTEXSCAN already produce a list formatted this way.");
	}

}

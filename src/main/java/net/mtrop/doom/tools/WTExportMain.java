/*******************************************************************************
 * Copyright (c) 2020 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.mtrop.doom.Wad;
import net.mtrop.doom.WadFile;
import net.mtrop.doom.exception.TextureException;
import net.mtrop.doom.exception.WadException;
import net.mtrop.doom.object.BinaryObject;
import net.mtrop.doom.texture.Animated;
import net.mtrop.doom.texture.CommonTexture;
import net.mtrop.doom.texture.CommonTextureList;
import net.mtrop.doom.texture.DoomTextureList;
import net.mtrop.doom.texture.PatchNames;
import net.mtrop.doom.texture.StrifeTextureList;
import net.mtrop.doom.texture.Switches;
import net.mtrop.doom.texture.TextureSet;
import net.mtrop.doom.texture.TextureSet.Texture;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.wtexport.TextureTables;
import net.mtrop.doom.util.NameUtils;
import net.mtrop.doom.util.TextureUtils;

/**
 * Main class for TexMove.
 * @author Matthew Tropiano
 */
public final class WTExportMain
{
	private static final String DOOM_VERSION = Common.getVersionString("doom");
	private static final String VERSION = Common.getVersionString("wtexport");

	private static final int ERROR_NONE = 0;
	private static final int ERROR_BAD_FILE = 1;
	private static final int ERROR_NO_FILES = 2;
	private static final int ERROR_IO_ERROR = 3;
	private static final int ERROR_BAD_OPTIONS = 4;
	
	private static final Pattern PATCH_MARKER = Pattern.compile("P[0-9]*_(START|END)");
	private static final Pattern FLAT_MARKER = Pattern.compile("F[0-9]*_(START|END)");
	
	private static final String SWITCH_HELP = "--help";
	private static final String SWITCH_HELP2 = "-h";
	private static final String SWITCH_VERSION = "--version";
	private static final String SWITCH_BASE = "--base-wad";
	private static final String SWITCH_BASE2 = "-b";
	private static final String SWITCH_OUTPUT = "--output";
	private static final String SWITCH_OUTPUT2 = "-o";
	private static final String SWITCH_CREATE = "--create";
	private static final String SWITCH_CREATE2 = "-c";
	private static final String SWITCH_ADDITIVE = "--add";
	private static final String SWITCH_ADDITIVE2 = "-a";
	private static final String SWITCH_NULLTEX = "--null-texture";
	private static final String SWITCH_NOANIMATED = "--no-animated";
	private static final String SWITCH_NOSWITCH = "--no-switches";

	/**
	 * Context.
	 */
	public static class Options
	{
		private PrintStream out;
		private PrintStream err;
		private BufferedReader in;
		
		private boolean help;
		private boolean version;
		private boolean quiet;

		/** Path to base wad. */
		private String baseWad;
		/** Path to output wad. */
		private String outWad;
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
		private List<String> textureList; 
		/** List of flat names. */
		private List<String> flatList; 

		public Options(PrintStream out, PrintStream err, BufferedReader in)
		{
			this.out = out;
			this.err = err;
			this.in = in;
			
			this.quiet = false;
			this.baseWad = null;
			this.outWad = null;
			this.noAnimated = false;
			this.noSwitches = false;
			this.additive = null;
			this.nullComparator = new NullComparator(null);
			this.filePaths = new ArrayList<>();
			this.textureList = new ArrayList<>();
			this.flatList = new ArrayList<>();
		}
		
		void println(Object msg)
		{
			if (!quiet)
				out.println(msg);
		}
		
		void printf(String fmt, Object... args)
		{
			if (!quiet)
				out.printf(fmt, args);
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

		public void setBaseWad(String baseWad) 
		{
			this.baseWad = baseWad;
		}
		
		public void setOutWad(String outWad) 
		{
			this.outWad = outWad;
		}
		
		public void setNoAnimated(boolean noAnimated) 
		{
			this.noAnimated = noAnimated;
		}
		
		public void setNoSwitches(boolean noSwitches) 
		{
			this.noSwitches = noSwitches;
		}
		
		public void setAdditive(Boolean additive) 
		{
			this.additive = additive;
		}
		
		public void setNullTexture(String texture) 
		{
			this.nullComparator = new NullComparator(texture);
		}
		
		public void addFilePath(String path)
		{
			filePaths.add(path);
		}
	
		public void addTexture(String name)
		{
			textureList.add(name.toUpperCase());
		}

		public void addFlat(String name)
		{
			flatList.add(name.toUpperCase());
		}
	}

	public static class Context
	{
		/** Base Unit. */
		private WadUnit baseUnit;
		/** WAD priority queue. */
		private Deque<WadUnit> wadPriority;

		/** List of texture names (need this list because order matters). */
		private List<String> textureList; 
		/** List of flat names (need this list because order matters). */
		private List<String> flatList; 
		/** Set of texture names (dupe test). */
		private Set<String> textureSet; 
		/** Set of flat names (dupe test). */
		private Set<String> flatSet; 

		Context()
		{
			this.baseUnit = null;
			this.wadPriority = new LinkedList<WadUnit>();
			this.textureSet = new HashSet<>();
			this.flatSet = new HashSet<>();
			this.textureList = new ArrayList<>();
			this.flatList = new ArrayList<>();
		}

		// Scan WAD file.
		private boolean scanWAD(Options options, String path, boolean isBase)
		{
			options.printf("Scanning %s...\n", path);
			File f = new File(path);
			WadFile wf = openWadFile(options, f, false);
			if (wf == null)
				return false;
			
			WadUnit unit = new WadUnit(wf);
			
			try {
				if (!scanTexturesAndPNames(options, unit, wf))
					return false;
			} catch (IOException e) {
				options.printf("ERROR: \"%s\" could not be read.\n", f.getPath());
				return false;
			}
			
			options.println("    Scanning patch entries...");
			if (!scanNamespace(options, "P", "PP", PATCH_MARKER, unit, wf, unit.patchIndices))
				return false;
			if (!scanNamespace(options, "PP", "P", null, unit, wf, unit.patchIndices))
				return false;
			options.printf("        %d patches.\n", unit.patchIndices.size());
			options.println("    Scanning flat entries...");
			if (!scanNamespace(options, "F", "FF", FLAT_MARKER, unit, wf, unit.flatIndices))
				return false;
			if (!scanNamespace(options, "FF", "F", null, unit, wf, unit.flatIndices))
				return false;
			options.printf("        %d flats.\n", unit.flatIndices.size());
			options.println("    Scanning texture namespace entries...");
			if (!scanNamespace(options, "TX", null, null, unit, wf, unit.texNamespaceIndices))
				return false;
			options.printf("        %d namespace textures.\n", unit.texNamespaceIndices.size());
			
			for (Map.Entry<String, Integer> entry : unit.flatIndices.entrySet())
				unit.flatList.add(entry.getKey());
		
			for (Map.Entry<String, Integer> entry : unit.texNamespaceIndices.entrySet())
			{
				String s = entry.getKey();
				if (!unit.textureList.contains(s))
					unit.textureList.add(s);
			}
		
			for (TextureSet.Texture tex : unit.textureSet)
				if (!unit.textureList.contains(tex.getName()))
					unit.textureList.add(tex.getName());
		
			try {
				if (!scanAnimated(options, unit, wf))
					return false;
			} catch (IOException e) {
				options.printf("ERROR: \"%s\" could not be read: an ANIMATED or SWITCHES lump may be corrupt.\n", f.getPath());
				return false;
			}
			
			if (!isBase)
				wadPriority.add(unit);
			else
				baseUnit = unit;
			
			return true;
		}

		// Scan for TEXTUREx and PNAMES.
		private boolean scanTexturesAndPNames(Options options, WadUnit unit, WadFile wf) throws IOException
		{
			if (!wf.contains("TEXTURE1"))
				return true;
			options.println("    Scanning TEXTUREx/PNAMES...");
			
			
			PatchNames patchNames = null;
			CommonTextureList<?> textureList1 = null;
			CommonTextureList<?> textureList2 = null;
			byte[] textureData = null;
			
			try {
				textureData = wf.getData("TEXTURE1");
			} catch (WadException e) {
				options.printf("ERROR: %s: %s\n", wf.getFilePath(), e.getMessage());
				return false;
			} catch (IOException e) {
				options.printf("ERROR: %s: %s\n", wf.getFilePath(), e.getMessage());
				return false;
			}
		
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
					textureList2 = BinaryObject.create(StrifeTextureList.class, textureData);
				else
					textureList2 = BinaryObject.create(DoomTextureList.class, textureData);
				
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
		
			if (textureList2 != null)
				unit.textureSet = new TextureSet(patchNames, textureList1, textureList2);
			else
				unit.textureSet = new TextureSet(patchNames, textureList1);
			
			return true;
		}

		// Scan for ANIMATED. Add combinations of textures to animated mapping.
		private boolean scanAnimated(Options options, WadUnit unit, WadFile wf) throws IOException
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

		// Get animated texture sequence.
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

		// Get animated flat sequence.
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

		// Scans namespace entries.
		private boolean scanNamespace(Options options, String name, String equivName, Pattern ignorePattern, WadUnit unit, WadFile wf, HashMap<String, Integer> map)
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
						map.put(ename, i);
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

		private void addToLists(Set<String> set, List<String> list, String s)
		{
			if (set.contains(s))
				return;
			set.add(s);
			list.add(s);
		}

		private void readAndAddTextures(Options options, String textureName)
		{
			if (!NameUtils.isValidTextureName(textureName))
			{
				options.println("ERROR: Texture \""+textureName+"\" has an invalid name. Skipping.");
				return;
			}
			
			addToLists(textureSet, textureList, textureName);
		
			for (WadUnit unit : wadPriority)
			{
				if (!options.noAnimated)
				{
					if (unit.animatedTexture.containsKey(textureName))
						for (String s : unit.animatedTexture.get(textureName))
							addToLists(textureSet, textureList, s);
				}
			
				if (!options.noSwitches)
				{
					if (unit.switchMap.containsKey(textureName))
					{
						addToLists(textureSet, textureList, textureName);
						addToLists(textureSet, textureList, unit.switchMap.get(textureName));
					}
					else if (TextureTables.SWITCH_TABLE.containsKey(textureName))
					{
						addToLists(textureSet, textureList, textureName);
						addToLists(textureSet, textureList, TextureTables.SWITCH_TABLE.get(textureName));
					}
				}
			}
		}

		private void readAndAddFlats(Options options, String textureName)
		{
			addToLists(flatSet, flatList, textureName);
		
			if (!options.noAnimated)
			{
				for (WadUnit unit : wadPriority)
				{
					if (unit.animatedFlat.containsKey(textureName))
						for (String s : unit.animatedFlat.get(textureName))
							addToLists(flatSet, flatList, s);
				}
			}
		}

		/** Searches for the flat to extract. */
		private WadUnit searchForFlat(Deque<WadUnit> unitQueue, String flatName)
		{
			for (WadUnit unit : unitQueue)
			{
				if (unit.flatIndices.containsKey(flatName))
					return unit;
			}
			
			return null;
		}

		/** Searches for the texture to extract. */
		private WadUnit searchForTexture(Deque<WadUnit> unitQueue, String textureName)
		{
			for (WadUnit unit : unitQueue)
			{
				if (unit.textureSet.contains(textureName))
					return unit;
			}
			
			return null;
		}

		/** Searches for the texture to extract. */
		private WadUnit searchForNamespaceTexture(Deque<WadUnit> unitQueue, String textureName)
		{
			for (WadUnit unit : unitQueue)
			{
				if (unit.texNamespaceIndices.containsKey(textureName))
					return unit;
			}
			
			return null;
		}

		private boolean extractFlats(Options options, ExportSet exportSet)
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
								options.printf("        Extracting flat %s...\n", flat);
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

		private boolean extractTextures(Options options, ExportSet exportSet)
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
					
					TextureSet.Texture entry = unit.textureSet.getTextureByName(textureName);
					
					for (int i = 0; i < entry.getPatchCount(); i++)
					{
						TextureSet.Patch p = entry.getPatch(i);
						String pname = p.getName();
						
						// does a matching patch exist?
						if (unit.patchIndices.containsKey(pname))
						{
							foundPatches = true;
							Integer pidx = unit.patchIndices.get(pname);
							if (pidx != null && !exportSet.patchHash.contains(pname))
							{
								try {
									options.printf("        Extracting patch %s...\n", pname);
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
		
					// if we've found patches or the texture is new, better extract the texture.
					if (foundPatches || !exportSet.textureSet.contains(textureName))
					{
						options.printf("        Copying texture %s...\n", textureName);
						
						// check if potential overwrite.
						if (exportSet.textureSet.contains(textureName))
							exportSet.textureSet.removeTextureByName(textureName);
						
						TextureSet.Texture newtex = exportSet.textureSet.createTexture(textureName);
						newtex.setHeight(entry.getHeight());
						newtex.setWidth(entry.getWidth());
						for (TextureSet.Patch p : entry)
						{
							TextureSet.Patch newpatch = newtex.createPatch(p.getName());
							newpatch.setOriginX(p.getOriginX());
							newpatch.setOriginY(p.getOriginY());
						}
						
						exportSet.textureHash.add(textureName);
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
								options.printf("        Extracting namespace texture %s...\n", textureName);
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

		// Merges ANIMATED and SWITCHES from inputs.
		private boolean mergeAnimatedAndSwitches(Options options, ExportSet exportSet)
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
								exportSet.animatedData.addEntry(Animated.texture(entry.getLastName(), entry.getFirstName(), entry.getTicks(), entry.getAllowsDecals()));
						}
						else
						{
							if (exportSet.flatHash.contains(entry.getFirstName()))
								exportSet.animatedData.addEntry(Animated.flat(entry.getLastName(), entry.getFirstName(), entry.getTicks()));
							else if (baseUnit.flatIndices.containsKey(entry.getFirstName()))
								exportSet.animatedData.addEntry(Animated.flat(entry.getLastName(), entry.getFirstName(), entry.getTicks()));
						}
					}
				}
			}
			
			if (!options.noSwitches)
			{
				options.println("    Merging SWITCHES...");
				for (WadUnit unit : wadPriority)
				{
					// did we pull any animated textures? if so, copy the entries.
					for (Switches.Entry e : unit.switches)
					{
						if (exportSet.textureSet.contains(e.getOffName()))
							exportSet.switchesData.addEntry(e.getOffName(), e.getOnName(), e.getGame());
						else if (exportSet.textureSet.contains(e.getOnName()))
							exportSet.switchesData.addEntry(e.getOffName(), e.getOnName(), e.getGame());
					}
				}
			}
			
			return true;
		}

		private boolean dumpToOutputWad(Options options, ExportSet exportSet, WadFile wf) throws IOException
		{
			options.println("Sorting entries...");
			exportSet.textureSet.sort(options.nullComparator);
			Collections.sort(exportSet.patchData);
			Collections.sort(exportSet.flatData);
			Collections.sort(exportSet.textureData);
			
			options.println("Dumping entries...");
		
			List<CommonTextureList<?>> tlist = new ArrayList<>();
			PatchNames pnames;
			
			// if Strife-formatted source, export to Strife.
			if (baseUnit.strife)
			{
				pnames = new PatchNames();
				StrifeTextureList tex1 = new StrifeTextureList();
				StrifeTextureList tex2 = baseUnit.tex2exists ? new StrifeTextureList() : null;
				Set<String> tex1names = baseUnit.tex2exists ? baseUnit.tex1names : null;
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
				Set<String> tex1names = baseUnit.tex2exists ? baseUnit.tex1names : null;
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
					wf.replaceEntry(idx, exportSet.animatedData.toBytes());
				else
					wf.addData("ANIMATED", exportSet.animatedData.toBytes());
			}
		
			if (!options.noSwitches && exportSet.switchesData.getEntryCount() > 0)
			{
				idx = wf.indexOf("SWITCHES");
				if (idx >= 0)
					wf.replaceEntry(idx, exportSet.switchesData.toBytes());
				else
					wf.addData("SWITCHES", exportSet.switchesData.toBytes());
			}
			
			dumpListToOutputWad(exportSet.patchData, "PP", wf);
			dumpListToOutputWad(exportSet.flatData, "FF", wf);
			dumpListToOutputWad(exportSet.textureData, "TX", wf);
			
			return true;
		}

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

		/** Extracts the necessary stuff for output. */
		private boolean extractToOutputWad(Options options)
		{
			File outFile = new File(options.outWad);
			WadFile outWadFile = options.additive ? openWadFile(options, outFile, true) : newWadFile(options, outFile);
			if (outWadFile == null)
				return false;
		
			File baseFile = new File(options.baseWad);
			WadFile baseWadFile = openWadFile(options, baseFile, false);
			if (baseWadFile == null)
			{
				Common.close(outWadFile);
				return false;
			}
		
			ExportSet exportSet = new ExportSet();
			try {
				exportSet.textureSet = TextureUtils.importTextureSet(baseWadFile);
				extractTextures(options, exportSet);
				extractFlats(options, exportSet);
				mergeAnimatedAndSwitches(options, exportSet);
				dumpToOutputWad(options, exportSet, outWadFile);
			} catch (TextureException | IOException e) {
				options.printf("ERROR: %s: %s\n", baseWadFile.getFilePath(), e.getMessage());
				return false;
			} finally {
				Common.close(baseWadFile);
				Common.close(outWadFile);
			}
			
			return true;
		}

		// Attempts to make a new WAD file.
		private WadFile newWadFile(Options options, File f)
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

		// Attempts to open a WAD file.
		private WadFile openWadFile(Options options, File f, boolean create)
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
		 * Starts texture extraction.
		 * @param options
		 * @return the return code.
		 */
		public int doTextureExtraction(Options options) 
		{
			/* STEP 1 : Scan all incoming WADs so we know where crap is. */
			
			// scan base.
			if (!scanWAD(options, options.baseWad, true))
				return ERROR_BAD_FILE;
			
			// scan patches. 
			for (String f : options.filePaths)
				if (!scanWAD(options, f, false))
					return ERROR_BAD_FILE;
		
			/* STEP 2 : Compile list of what we want. */
		
			for (String t : options.textureList)
				readAndAddTextures(options, t);
			for (String f : options.flatList)
				readAndAddFlats(options, f);
			
			/* STEP 3 : Extract the junk and put it in the output wad. */
		
			if (options.nullComparator.nullName != null)
				options.println("Using "+ options.nullComparator.nullName.toUpperCase() + " as the null texture in TEXTURE1...");
			
			if (!extractToOutputWad(options))
				return ERROR_BAD_FILE;
			
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
		private Set<String> textureHash;
		private Set<String> patchHash;
		private Set<String> flatHash;
		
		private TextureSet textureSet;
		private List<EntryData> patchData;
		private List<EntryData> flatData;
		private List<EntryData> textureData;
		private Animated animatedData;
		private Switches switchesData;
		
		public ExportSet()
		{
			this.textureHash = new HashSet<>();
			this.flatHash = new HashSet<>();
			this.patchHash = new HashSet<>();
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
	 * Prints the splash.
	 * @param out the print stream to print to.
	 */
	private static void splash(PrintStream out)
	{
		out.println("WTEXport v" + VERSION + " by Matt Tropiano (using DoomStruct v" + DOOM_VERSION + ")");
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

	// Prints the usage message.
	private static void help(PrintStream out)
	{
		out.println("    --help                Prints help and exits.");
		out.println("    -h");
		out.println();
		out.println("    --version             Prints version, and exits.");
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
		out.println("                          file already  exists, it is replaced COMPLETELY");
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
		out.println("The utility WTEXSCAN already produces a list formatted this way.");
	}

	/**
	 * Reads command line arguments and sets options.
	 * @param options the program options. 
	 * @param args the argument args.
	 * @return true if parse successful, false if not.
	 * @throws IOException if a read error occurs.
	 */
	public static boolean scanOptions(Options options, String[] args) throws IOException
	{
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
					if (arg.equals(SWITCH_HELP) || arg.equals(SWITCH_HELP2))
						options.help = true;
					else if (arg.equals(SWITCH_VERSION))
						options.version = true;
					else if (arg.equals(SWITCH_NOANIMATED))
						options.setNoAnimated(true);
					else if (arg.equals(SWITCH_NOSWITCH))
						options.setNoSwitches(true);
					else if (arg.equals(SWITCH_CREATE) || arg.equals(SWITCH_CREATE2))
						options.setAdditive(false);
					else if (arg.equals(SWITCH_ADDITIVE) || arg.equals(SWITCH_ADDITIVE2))
						options.setAdditive(true);
					else if (arg.equals(SWITCH_BASE) || arg.equals(SWITCH_BASE2))
						state = STATE_BASE;
					else if (arg.equals(SWITCH_OUTPUT) || arg.equals(SWITCH_OUTPUT2))
						state = STATE_OUT;
					else if (arg.equals(SWITCH_NULLTEX))
						state = STATE_NULLTEX;
					else
						options.addFilePath(arg);
				}
				break;
			
				case STATE_BASE:
				{
					options.setBaseWad(arg);
					state = STATE_INIT;
				}
				break;
				
				case STATE_OUT:
				{
					options.setOutWad(arg);
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
		
		if (!options.help && !options.version && !options.filePaths.isEmpty() && !Common.isEmpty(options.outWad) && options.additive != null)
		{
			// Read list from Standard In		
			options.println("Input texture/flat list:");
			if (!readList(options))
				return false;
		}
		return true;
	}

	private static boolean readList(Options options) throws IOException
	{
		final String TEXTURES = ":textures";
		final String FLATS = ":flats";
		final String END = ":end";
		
		final int STATE_NONE = 0;
		final int STATE_TEXTURES = 1;
		final int STATE_FLATS = 2;
		
		String line = null;
		int state = STATE_NONE;
		boolean keepGoing = true;
		
		while (keepGoing && ((line = options.readLine()) != null))
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
					options.err.println("ERROR: Name before '-textures' or '-flats'.");
					options.in.close();
					return false;
				case STATE_TEXTURES:
					options.addTexture(line);
					break;
				case STATE_FLATS:
					options.addFlat(line);
					break;
			}
		}
		
		options.in.close();
		return true;
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
	
		if (options.filePaths == null || options.filePaths.isEmpty())
		{
			options.println("ERROR: No input WAD(s) specified.");
			usage(options.out);
			return ERROR_NO_FILES;
		}
	
		if (Common.isEmpty(options.baseWad))
		{
			options.println("ERROR: No base WAD specified.");
			usage(options.out);
			return ERROR_NO_FILES;
		}
	
		if (Common.isEmpty(options.outWad))
		{
			options.println("ERROR: No output WAD specified.");
			usage(options.out);
			return ERROR_NO_FILES;
		}
	
		if (options.additive == null)
		{
			options.println("ERROR: Must specify --create or --add for output.");
			usage(options.out);
			return ERROR_NO_FILES;
		}
	
		int out;
		if ((out = (new Context()).doTextureExtraction(options)) == ERROR_NONE)
			options.println("Done!");
		
		return out;
	}

	public static void main(String[] args)
	{
		try {
			Options options = new Options(System.out, System.err, Common.openTextStream(System.in));
			if (!scanOptions(options, args))
			{
				System.exit(ERROR_BAD_OPTIONS);
				return;
			}
			System.exit(call(options));
		} catch (IOException e) {
			// if we reach here, you got PROBLEMS, buddy.
			System.err.println("ERROR: Could not read from STDIN.");
			System.exit(ERROR_IO_ERROR);
		}
	}
	
}
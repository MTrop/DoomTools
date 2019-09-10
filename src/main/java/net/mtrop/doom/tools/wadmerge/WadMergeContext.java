package net.mtrop.doom.tools.wadmerge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

import net.mtrop.doom.Wad;
import net.mtrop.doom.WadBuffer;
import net.mtrop.doom.WadEntry;
import net.mtrop.doom.WadFile;
import net.mtrop.doom.exception.WadException;
import net.mtrop.doom.io.IOUtils;
import net.mtrop.doom.texture.Animated;
import net.mtrop.doom.texture.DoomTextureList;
import net.mtrop.doom.texture.PatchNames;
import net.mtrop.doom.texture.Switches;
import net.mtrop.doom.texture.TextureSet;
import net.mtrop.doom.texture.TextureSet.Patch;
import net.mtrop.doom.texture.TextureSet.Texture;
import net.mtrop.doom.util.MapUtils;
import net.mtrop.doom.util.NameUtils;

/**
 * The main context for WadMerge.
 * @author Matthew Tropiano
 */
public class WadMergeContext
{
	/** Simple date format. */
	private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = ThreadLocal.withInitial(
		()->new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ")
	);

	/** Map of open wads. */
	private HashMap<String, WadBuffer> currentWads;
	/** Log out print stream. */
	private PrintStream logout;
	/** If verbosity is enabled. */
	private boolean verbose;

	/**
	 * Creates a new context. No output.
	 */
	public WadMergeContext()
	{
		this(null, false);
	}
	
	/**
	 * Creates a new context.
	 * @param log the output print stream to use for logging (can be null).
	 * @param verbose if true, do verbose printing (only matters if log is not null).
	 */
	public WadMergeContext(PrintStream log, boolean verbose)
	{
		this.currentWads = new HashMap<String, WadBuffer>();
		this.logout = log;
		this.verbose = verbose;
	}
	
	public void verboseln(String seq)
	{
		if (verbose)
			logln(seq);
	}
	
	public void verbosef(String seq, Object... args)
	{
		if (verbose)
			logf(seq);
	}

	public void logln(String seq)
	{
		if (logout != null)
			logout.println(seq);
	}
	
	public void logf(String seq, Object... args)
	{
		if (logout != null)
			logout.printf(seq, args);
	}
	
	/**
	 * Returns the file's name, no extension.
	 * @param filename the file name.
	 * @param extensionSeparator the text or characters that separates file name from extension.
	 * @return the file's name without extension.
	 */
	public static String getFileNameWithoutExtension(String filename, String extensionSeparator)
	{
		int extindex = filename.lastIndexOf(extensionSeparator);
		if (extindex >= 0)
			return filename.substring(0, extindex);
		return "";
	}

	/**
	 * Returns the file's name, no extension.
	 * @param file the file.
	 * @return the file's name without extension.
	 */
	public static String getFileNameWithoutExtension(File file)
	{
		return getFileNameWithoutExtension(file.getName(), ".");
	}

	/**
	 * Returns the extension of a filename.
	 * @param filename the file name.
	 * @param extensionSeparator the text or characters that separates file name from extension.
	 * @return the file's extension, or an empty string for no extension.
	 */
	private static String getFileExtension(String filename, String extensionSeparator)
	{
		int extindex = filename.lastIndexOf(extensionSeparator);
		if (extindex >= 0)
			return filename.substring(extindex+1);
		return "";
	}

	/**
	 * Returns the extension of a file's name.
	 * Assumes the separator to be ".".
	 * @param file the file.
	 * @return the file's extension, or an empty string for no extension.
	 */
	private static String getFileExtension(File file)
	{
		return getFileExtension(file.getName(), ".");
	}

	/**
	 * Creates the necessary directories for a file path.
	 * @param file	the abstract file path.
	 * @return		true if the paths were made (or exists), false otherwise.
	 */
	public static boolean createPathForFile(File file)
	{
		return createPathForFile(file.getAbsolutePath());
	}

	/**
	 * Creates the necessary directories for a file path.
	 * @param path	the abstract path.
	 * @return		true if the paths were made (or exists), false otherwise.
	 */
	public static boolean createPathForFile(String path)
	{
		int sindx = -1;
		
		if ((sindx = Math.max(
				path.lastIndexOf(File.separator), 
				path.lastIndexOf("/"))) != -1)
		{
			return createPath(path.substring(0, sindx));
		}
		return true;
	}

	/**
	 * Creates the necessary directories for a file path.
	 * @param path	the abstract path.
	 * @return		true if the paths were made (or exists), false otherwise.
	 */
	public static boolean createPath(String path)
	{
		File dir = new File(path);
		if (dir.exists())
			return true;
		return dir.mkdirs();
	}

	/**
	 * Sets verbosity.
	 * @param verbose the new verbosity flag.
	 * @return OK.
	 */
	public Response setVerbose(boolean verbose)
	{
		if (!verbose)
			verboseln("Verbosity OFF.");
		this.verbose = verbose;
		verboseln("Verbosity ON.");
		return Response.OK;
	}
	
	/**
	 * Creates a blank Wad buffer.
	 * Symbol is case-insensitive.
	 * @param symbol the symbol to associate with the Wad.
	 * @return OK if the buffer does not exist and it was created, BAD_SYMBOL otherwise.
	 */
	public Response create(String symbol)
	{
		symbol = symbol.toLowerCase();
		if (currentWads.containsKey(symbol))
			return Response.BAD_SYMBOL;
		
		currentWads.put(symbol, new WadBuffer());
		verbosef("Created buffer `%s`.\n", symbol);
		return Response.OK;
	}

	/**
	 * Checks if a symbol refers to a valid buffer.
	 * Symbol is case-insensitive.
	 * @param symbol the symbol to check.
	 * @return OK if the buffer exists, BAD_SYMBOL otherwise.
	 */
	public Response isValid(String symbol)
	{
		symbol = symbol.toLowerCase();
		Response out = currentWads.containsKey(symbol) ? Response.OK : Response.BAD_SYMBOL;
		if (out == Response.OK)
			verbosef("Symbol `%s` is valid.\n", symbol);
		else
			verbosef("Symbol `%s` is invalid.\n", symbol);
		return out;
	}

	/**
	 * Clears an existing Wad buffer.
	 * Symbol is case-insensitive.
	 * @param symbol the symbol to clear.
	 * @return OK if the buffer exists and it was cleared, BAD_SYMBOL otherwise.
	 */
	public Response clear(String symbol)
	{
		symbol = symbol.toLowerCase();
		if (!currentWads.containsKey(symbol))
			return Response.BAD_SYMBOL;
		
		currentWads.put(symbol, new WadBuffer());
		verbosef("Cleared buffer `%s`.\n", symbol);
		return Response.OK;
	}

	/**
	 * Discards an existing Wad buffer.
	 * Symbol is case-insensitive.
	 * @param symbol the symbol to discard.
	 * @return OK if the buffer exists and it was discarded, BAD_SYMBOL otherwise.
	 */
	public Response discard(String symbol)
	{
		symbol = symbol.toLowerCase();
		if (!currentWads.containsKey(symbol))
			return Response.BAD_SYMBOL;
		
		currentWads.remove(symbol);
		verbosef("Discarded buffer `%s`.\n", symbol);
		return Response.OK;
	}

	/**
	 * Adds a marker to a Wad buffer.
	 * Symbol is case-insensitive. The entry is coerced to a valid name.
	 * @param symbol the symbol to use.
	 * @param name the entry name.
	 * @return OK if the buffer exists and was added to, false otherwise.
	 */
	public Response addMarker(String symbol, String name)
	{
		symbol = symbol.toLowerCase();
		WadBuffer buffer;
		if ((buffer = currentWads.get(symbol)) == null)
			return Response.BAD_SYMBOL;
		
		try {
			String marker = NameUtils.toValidEntryName(name);
			buffer.addMarker(marker);
			verbosef("Added marker `%s` to buffer `%s`.\n", marker, symbol);
		} catch (IOException e) {
			// Shouldn't happen.
			return Response.UNEXPECTED_ERROR;
		}
		return Response.OK;
	}

	/**
	 * Adds an entry to a Wad buffer that contains the current date.
	 * Symbol is case-insensitive. The entry is coerced to a valid name.
	 * @param symbol the symbol to use.
	 * @param name the entry name.
	 * @return OK if the buffer exists and was added to, BAD_SYMBOL otherwise.
	 */
	public Response addDateMarker(String symbol, String name)
	{
		symbol = symbol.toLowerCase();
		WadBuffer buffer;
		if ((buffer = currentWads.get(symbol)) == null)
			return Response.BAD_SYMBOL;
		
		try {
			String marker = NameUtils.toValidEntryName(name);
			buffer.addData(marker, DATE_FORMAT.get().format(new Date()).getBytes(Charset.forName("ASCII")));
			verbosef("Added date marker `%s` to buffer `%s`.\n", marker, symbol);
		} catch (IOException e) {
			// Shouldn't happen.
			return Response.UNEXPECTED_ERROR;
		}
		return Response.OK;
	}

	/**
	 * Merges a Wad buffer into another.
	 * The symbols are case-insensitive.
	 * @param destinationSymbol the destination buffer.
	 * @param sourceSymbol the source buffer.
	 * @return OK if both buffers exist and the merge worked, BAD_SYMBOL / BAD_SOURCE_SYMBOL otherwise.
	 */
	public Response mergeBuffer(String destinationSymbol, String sourceSymbol)
	{
		destinationSymbol = destinationSymbol.toLowerCase();
		WadBuffer bufferDest;
		if ((bufferDest = currentWads.get(destinationSymbol)) == null)
			return Response.BAD_SYMBOL;
		
		sourceSymbol = sourceSymbol.toLowerCase();
		WadBuffer bufferSource;
		if ((bufferSource = currentWads.get(sourceSymbol)) == null)
			return Response.BAD_SOURCE_SYMBOL;

		try 
		{
			for (WadEntry e : bufferSource)
			{
				bufferDest.addData(e.getName(), bufferSource.getData(e));
				verbosef("Added entry `%s` to buffer `%s` (from `%s`).\n", e.getName(), destinationSymbol, sourceSymbol);
			}
		} 
		catch (IOException e) 
		{
			// Shouldn't happen.
			return Response.UNEXPECTED_ERROR;
		}
		
		return Response.OK;
	}
	
	/**
	 * Merges the contents of a Wad into a buffer.
	 * Symbol is case-insensitive.
	 * @param symbol the buffer to merge into.
	 * @param wadFile the file to read from.
	 * @return OK if the file was found and contents were merged in, false otherwise. 
	 * @throws IOException if the file could not be read.
	 */
	public Response mergeWad(String symbol, File wadFile) throws IOException
	{
		if (!wadFile.exists() || wadFile.isDirectory())
			return Response.BAD_FILE;

		if (!Wad.isWAD(wadFile))
			return Response.BAD_WAD;

		symbol = symbol.toLowerCase();
		WadBuffer buffer;
		if ((buffer = currentWads.get(symbol)) == null)
			return Response.BAD_SYMBOL;
		
		try (WadFile wad = new WadFile(wadFile))
		{
			verbosef("Reading WAD `%s`...\n", wadFile.getPath());
			for (WadEntry e : wad)
			{
				buffer.addData(e.getName(), wad.getData(e));
				verbosef("Added entry `%s` to buffer `%s`.\n", e.getName(), symbol);
			}
			verbosef("Done reading `%s`.\n", wadFile.getPath());
		}
		
		return Response.OK;
	}

	// Merge map into buffer, with rename.
	private static Response mergeMap(WadBuffer buffer, String bufferName, String newHeader, Wad source, String header) throws IOException
	{
		WadEntry[] entries = MapUtils.getMapEntries(source, header);
		if (entries.length == 0)
			return Response.BAD_MAP;
		
		for (WadEntry e : entries)
		{
			String outname;
			if ((outname = e.getName()).equalsIgnoreCase(header))
				outname = NameUtils.toValidEntryName(newHeader);
			buffer.addData(outname, source.getData(e));
		}
		
		return Response.OK;
	}
	
	/**
	 * Merges a single map from a Wad file into a buffer.
	 * Symbol is case-insensitive, as well as entry. The new entry is coerced to a valid name.
	 * @param symbol the buffer to merge into.
	 * @param newHeader the new header name.
	 * @param wadFile the file to read from.
	 * @param header the map header.
	 * @return OK if the file was found and contents were merged in, BAD_SYMBOL otherwise. 
	 * @throws IOException if the file could not be read.
	 * @throws WadException if the file is not a Wad file.
	 */
	public Response mergeMap(String symbol, String newHeader, File wadFile, String header) throws IOException, WadException
	{
		if (!wadFile.exists() || wadFile.isDirectory())
			return Response.BAD_FILE;

		symbol = symbol.toLowerCase();
		WadBuffer buffer;
		if ((buffer = currentWads.get(symbol)) == null)
			return Response.BAD_SYMBOL;
		
		try (WadFile wad = new WadFile(wadFile))
		{
			Response out = mergeMap(buffer, symbol, newHeader, wad, header);
			verbosef("Added map `%s` to `%s` as `%s` (from `%s`).\n", header, symbol, newHeader, wadFile.getPath());
			return out;
		}		
	}
	
	/**
	 * Merges a single map from an existing buffer into a buffer.
	 * Symbol is case-insensitive, as well as entry. The new entry is coerced to a valid name.
	 * @param destinationSymbol the buffer to merge into.
	 * @param newHeader the new header name.
	 * @param sourceSymbol the buffer to read from.
	 * @param header the map header.
	 * @return OK if the file was found and contents were merged in, false otherwise. 
	 * @throws IOException if the file could not be read.
	 * @throws WadException if the file is not a Wad file.
	 */
	public Response mergeMap(String destinationSymbol, String newHeader, String sourceSymbol, String header) throws IOException, WadException
	{
		destinationSymbol = destinationSymbol.toLowerCase();
		WadBuffer bufferDest;
		if ((bufferDest = currentWads.get(destinationSymbol)) == null)
			return Response.BAD_SYMBOL;
		
		sourceSymbol = sourceSymbol.toLowerCase();
		WadBuffer bufferSource;
		if ((bufferSource = currentWads.get(sourceSymbol)) == null)
			return Response.BAD_SOURCE_SYMBOL;
		
		Response out = mergeMap(bufferDest, destinationSymbol, newHeader, bufferSource, header);
		verbosef("Added map `%s` to `%s` as `%s` (from `%s`).\n", header, destinationSymbol, newHeader, sourceSymbol);
		return out;
	}
		
	/**
	 * Merges a single file as an entry into a buffer.
	 * Symbol is case-insensitive. The entry is coerced to a valid name.
	 * @param symbol the buffer to merge into.
	 * @param inFile the file to read.
	 * @return OK if the file was found and contents were merged in, false otherwise. 
	 * @throws IOException if the file could not be read.
	 * @throws WadException if the file is not a Wad file.
	 */
	public Response mergeFile(String symbol, File inFile) throws IOException, WadException
	{
		if (!inFile.exists() || inFile.isDirectory())
			return Response.BAD_FILE;

		symbol = symbol.toLowerCase();
		WadBuffer buffer;
		if ((buffer = currentWads.get(symbol)) == null)
			return Response.BAD_SYMBOL;

		String entryName = NameUtils.toValidEntryName(getFileNameWithoutExtension(inFile));
		buffer.addData(entryName, IOUtils.getBinaryContents(new FileInputStream(inFile)));
		verbosef("Added `%s` to `%s` (from `%s`).\n", entryName, symbol, inFile.getPath());
		return Response.OK;
	}

	/**
	 * Iterates through a directory, adding each file's data into the buffer, 
	 * and if that file is a valid WAD file, it's entries and data are added.
	 * If it encounters a directory, it is skipped.
	 * Symbol is case-insensitive.
	 * @param symbol the buffer to write.
	 * @param inDirectory the directory to read from.
	 * @return OK if the file was written, or BAD_SYMBOL if the symbol is invalid, or BAD_DIRECTORY if the provided file is not a directory. 
	 * @throws IOException if the file could not be written.
	 */
	public Response mergeDirectory(String symbol, File inDirectory) throws IOException
	{
		if (!inDirectory.exists() || !inDirectory.isDirectory())
			return Response.BAD_DIRECTORY;

		for (File f : inDirectory.listFiles())
		{
			Response resp;
			if (f.isDirectory())
				continue;
			else if (getFileExtension(f).equalsIgnoreCase("wad") && Wad.isWAD(f))
			{
				if ((resp = mergeWad(symbol, f)) != Response.OK)
					return resp; 
			}
			else
			{
				if ((resp = mergeFile(symbol, f)) != Response.OK)
					return resp; 
			}
		}
		
		return Response.OK;
	}

	/**
	 * Iterates through a directory, adding each file's data into the buffer, 
	 * and if that file is a valid WAD file, it's entries and data are added.
	 * If it encounters a directory, a marker is added (directory name prepended with a backslash), 
	 * and {@link #mergeDirectory(String, File)} is called on it.
	 * Symbol is case-insensitive.
	 * @param symbol the buffer to write to.
	 * @param inDirectory the directory to read from.
	 * @return OK if the file was written, or BAD_SYMBOL if the symbol is invalid, or BAD_DIRECTORY if the provided file is not a directory. 
	 * @throws IOException if the file could not be written.
	 */
	public Response mergeTree(String symbol, File inDirectory) throws IOException
	{
		if (!inDirectory.exists() || !inDirectory.isDirectory())
			return Response.BAD_DIRECTORY;

		for (File f : inDirectory.listFiles())
		{
			Response resp;
			if (f.isDirectory())
			{
				verbosef("Scan directory `%s`...\n", f.getPath());
				if ((resp = addMarker(symbol, "\\" + f.getName())) != Response.OK)
					return resp; 
				if ((resp = mergeTree(symbol, f)) != Response.OK)
					return resp; 
				verbosef("Done scanning directory `%s`.\n", f.getPath());
			}
			else if (getFileExtension(f).equalsIgnoreCase("wad") && Wad.isWAD(f))
			{
				if ((resp = mergeWad(symbol, f)) != Response.OK)
					return resp; 
			}
			else
			{
				if ((resp = mergeFile(symbol, f)) != Response.OK)
					return resp; 
			}
		}
		
		return Response.OK;
	}

	/**
	 * Merges a DEUTEX texture file into new TEXTUREX/PNAMES entries in a buffer, 
	 * using the name of the texture lump is the name of the file.
	 * Will read in an existing PNAMES lump if it exists in the buffer.
	 * Symbol is case-insensitive.
	 * @param symbol the buffer to write to.
	 * @param textureFile the texture file to parse.
	 * @param textureEntryName the name of the texture entry name.
	 * @return OK if the file was found and contents were merged in, 
	 * 		or BAD_SYMBOL if the symbol is invalid, 
	 * 		or BAD_PARSE if the file is incorrect,
	 * 		or BAD_FILE if it does not exist or is a directory.
	 * @throws IOException if the file could not be read.
	 */
	public Response mergeDEUTEXTextureFile(String symbol, File textureFile, String textureEntryName) throws IOException
	{
		if (!textureFile.exists() || textureFile.isDirectory())
			return Response.BAD_FILE;

		symbol = symbol.toLowerCase();
		WadBuffer buffer;
		if ((buffer = currentWads.get(symbol)) == null)
			return Response.BAD_SYMBOL;

		PatchNames pout;
		if (buffer.contains("PNAMES"))
			pout = buffer.getDataAs("PNAMES", PatchNames.class);
		else
			pout = new PatchNames();
		Texture currentTexture = null;
		TextureSet textureSet = new TextureSet(pout, new DoomTextureList(128));
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(textureFile))))
		{
			String line;
			int linenum = 0;
			Pattern whitespacePattern = Pattern.compile("\\s+"); 
			while ((line = reader.readLine()) != null)
			{
				line = line.trim();
				linenum++;
				if (line.isEmpty() || line.startsWith(";"))
					continue;
				
				if (line.startsWith("*")) // is patch.
				{
					if (currentTexture == null)
					{
						logf("ERROR: %s, line %d: Patch line before texture line.\n", textureFile.getPath(), linenum);
						return Response.BAD_PARSE;
					}
					
					String elementName = "";
					try (Scanner scanner = new Scanner(line)) 
					{
						scanner.useDelimiter(whitespacePattern);
						elementName = "star";
						if (!scanner.next().equals("*"))
						{
							logf("ERROR: %s, line %d: Malformed patch: missing star prefix.\n", textureFile.getPath(), linenum);
							return Response.BAD_PARSE;
						}
						elementName = "name";
						Patch p = currentTexture.createPatch(NameUtils.toValidEntryName(scanner.next()));
						elementName = "origin X";
						p.setOriginX(scanner.nextInt());
						elementName = "origin Y";
						p.setOriginY(scanner.nextInt());
						verbosef("    Add patch `%s`.\n", p.getName());
					} 
					catch (NoSuchElementException e) 
					{
						logf("ERROR: %s, line %d: Malformed patch: missing %s.\n", textureFile.getPath(), linenum, elementName);
						return Response.BAD_PARSE;
					}
				}
				else // is new texture.
				{
					String elementName = "";
					try (Scanner scanner = new Scanner(line)) 
					{
						scanner.useDelimiter(whitespacePattern);
						elementName = "name";
						currentTexture = textureSet.createTexture(NameUtils.toValidTextureName(scanner.next()));
						elementName = "width";
						currentTexture.setWidth(scanner.nextInt());
						elementName = "height";
						currentTexture.setHeight(scanner.nextInt());
						verbosef("Add texture `%s`...\n", currentTexture.getName());
					} 
					catch (NoSuchElementException e) 
					{
						logf("ERROR: %s, line %d: Malformed patch: missing %s.\n", textureFile.getPath(), linenum, elementName);
						return Response.BAD_PARSE;
					}
				}
			}
		}
		
		DoomTextureList tout;
		textureSet.export(pout = new PatchNames(), tout = new DoomTextureList());
		if (buffer.contains("PNAMES"))
			buffer.deleteEntry(buffer.indexOf("PNAMES"));
		
		textureEntryName = NameUtils.toValidEntryName(textureEntryName);
		buffer.addData(textureEntryName, tout);
		verbosef("Added `%s` to `%s`.\n", textureEntryName, symbol);
		buffer.addData("PNAMES", pout);
		verbosef("Added `PNAMES` to `%s`.\n", symbol);

		return Response.OK;
	}
	
	/**
	 * Creates TEXTUREX/PNAMES entries in a buffer, using a directory of patches as the only textures,
	 * and imports all of the patch files between PP_START and PP_END markers.
	 * Symbol is case-insensitive.
	 * @param symbol the buffer to write to.
	 * @param textureDirectory the texture file to parse.
	 * @param textureEntryName the name of the texture entry name.
	 * @return OK if the file was found and contents were merged in, 
	 * 		or BAD_SYMBOL if the symbol is invalid, 
	 * 		or BAD_DIRECTORY if the provided file is not a directory.
	 * @throws IOException if the file could not be read.
	 */
	public Response mergeTextureDirectory(String symbol, File textureDirectory, String textureEntryName) throws IOException
	{
		if (!textureDirectory.exists() || !textureDirectory.isDirectory())
			return Response.BAD_DIRECTORY;

		symbol = symbol.toLowerCase();
		WadBuffer buffer;
		if ((buffer = currentWads.get(symbol)) == null)
			return Response.BAD_SYMBOL;

		Response resp;
		if ((resp = addMarker(symbol, "PP_START")) != Response.OK)
			return resp;
		
		TextureSet textureSet = new TextureSet(new PatchNames(), new DoomTextureList(128));
		for (File f : textureDirectory.listFiles())
		{
			if (f.isDirectory())
			{
				verbosef("Skipping directory `%s`...\n", f.getPath());
				continue;
			}
			else
			{
				if ((resp = mergeFile(symbol, f)) != Response.OK)
					return resp; 
				String textureName = NameUtils.toValidTextureName(getFileNameWithoutExtension(f));
				textureSet.createTexture(textureName).createPatch(textureName);
				verbosef("Add texture `%s`...\n", textureName);
			}
		}

		if ((resp = addMarker(symbol, "PP_END")) != Response.OK)
			return resp;

		PatchNames pout;
		DoomTextureList tout;
		textureSet.export(pout = new PatchNames(), tout = new DoomTextureList());

		textureEntryName = NameUtils.toValidEntryName(textureEntryName);
		buffer.addData(textureEntryName, tout);
		verbosef("Added `%s` to `%s`.\n", textureEntryName, symbol);
		buffer.addData("PNAMES", pout);
		verbosef("Added `PNAMES` to `%s`.\n", symbol);
		
		return Response.OK;
	}
	
	/**
	 * Creates ANIMATED and SWITCHES entries in a buffer using a table file read by SWANTBLS.
	 * Symbol is case-insensitive.
	 * @param symbol the buffer to write to.
	 * @param swantblsFile the texture file to parse.
	 * @return OK if the file was found and contents were merged in, 
	 * 		or BAD_SYMBOL if the symbol is invalid, 
	 * 		or BAD_DIRECTORY if the provided file is not a directory.
	 * @throws IOException if the file could not be read.
	 */
	public Response mergeSwitchAnimatedTables(String symbol, File swantblsFile) throws IOException
	{
		if (!swantblsFile.exists() || swantblsFile.isDirectory())
			return Response.BAD_FILE;

		symbol = symbol.toLowerCase();
		WadBuffer buffer;
		if ((buffer = currentWads.get(symbol)) == null)
			return Response.BAD_SYMBOL;

		Animated animated = new Animated();
		Switches switches = new Switches();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(swantblsFile))))
		{
			final int STATE_NONE = 0;
			final int STATE_SWITCHES = 1;
			final int STATE_FLATS = 2;
			final int STATE_TEXTURES = 3;
			Pattern whitespacePattern = Pattern.compile("\\s+"); 

			String line;
			int linenum = 0;
			int state = STATE_NONE;
			
			Switches.Game[] SWITCH_GAMES = Switches.Game.values();
			
			while ((line = reader.readLine()) != null)
			{
				line = line.trim();
				linenum++;
				if (line.isEmpty() || line.startsWith("#"))
					continue;
				
				if (line.startsWith("[") && line.endsWith("]"))
				{
					String type = line.substring(1, line.length() - 1);
					if (type.equals("SWITCHES"))
						state = STATE_SWITCHES;
					else if (type.equals("FLATS"))
						state = STATE_FLATS;
					else if (type.equals("TEXTURES"))
						state = STATE_TEXTURES;
					else
					{
						logf("ERROR: %s, line %d: Malformed patch: bad type header: %s. Expected SWITCHES, FLATS, or TEXTURES.\n", swantblsFile.getPath(), linenum, type);
						return Response.BAD_PARSE;
					}
				}
				else switch (state)
				{
					case STATE_NONE:
					{
						logf("ERROR: %s, line %d: No type header found. Expected [SWITCHES], [FLATS], or [TEXTURES] before entries.\n", swantblsFile.getPath(), linenum);
						return Response.BAD_PARSE;
					}
					
					case STATE_SWITCHES:
					{
						String elementName = "";
						try (Scanner scanner = new Scanner(line))
						{
							scanner.useDelimiter(whitespacePattern);
							
							elementName = "game";
							int gameId = scanner.nextInt();
							if (gameId < 1 || gameId >= SWITCH_GAMES.length)
							{
								logf("ERROR: %s, line %d: Bad switch game: %d. Expected 1, 2, 3.\n", swantblsFile.getPath(), linenum, gameId);
								return Response.BAD_PARSE;
							}
							
							elementName = "\"off\" texture";
							String off = NameUtils.toValidTextureName(scanner.next());
							
							elementName = "\"on\" texture";
							String on = NameUtils.toValidTextureName(scanner.next());

							switches.addEntry(off, on, SWITCH_GAMES[gameId]); 
						}
						catch (NoSuchElementException e) 
						{
							logf("ERROR: %s, line %d: Malformed switch: missing %s.\n", swantblsFile.getPath(), linenum, elementName);
							return Response.BAD_PARSE;
						}
					}
					break;
					
					case STATE_FLATS:
					{
						String elementName = "";
						try (Scanner scanner = new Scanner(line))
						{
							scanner.useDelimiter(whitespacePattern);
							
							elementName = "tics";
							int tics = scanner.nextInt();
							if (tics < 0)
							{
								logf("ERROR: %s, line %d: Bad animated flat. Tic duration is negative.\n", swantblsFile.getPath(), linenum);
								return Response.BAD_PARSE;
							}
							
							elementName = "ending texture";
							String lastName = NameUtils.toValidTextureName(scanner.next());
							
							elementName = "starting texture";
							String firstName = NameUtils.toValidTextureName(scanner.next());

							animated.addEntry(Animated.flat(lastName, firstName, tics)); 
						}
						catch (NoSuchElementException e) 
						{
							logf("ERROR: %s, line %d: Malformed flat: missing %s.\n", swantblsFile.getPath(), linenum, elementName);
							return Response.BAD_PARSE;
						}
					}
					break;
					
					case STATE_TEXTURES:
					{
						String elementName = "";
						try (Scanner scanner = new Scanner(line))
						{
							scanner.useDelimiter(whitespacePattern);
							
							elementName = "tics";
							int tics = scanner.nextInt();
							if (tics < 0)
							{
								logf("ERROR: %s, line %d: Bad animated texture. Tic duration is negative.\n", swantblsFile.getPath(), linenum);
								return Response.BAD_PARSE;
							}
							
							elementName = "ending texture";
							String lastName = NameUtils.toValidTextureName(scanner.next());
							
							elementName = "starting texture";
							String firstName = NameUtils.toValidTextureName(scanner.next());

							animated.addEntry(Animated.texture(lastName, firstName, tics)); 
						}
						catch (NoSuchElementException e) 
						{
							logf("ERROR: %s, line %d: Malformed texture: missing %s.\n", swantblsFile.getPath(), linenum, elementName);
							return Response.BAD_PARSE;
						}
					}
					break;
				}
			}
		}
		
		buffer.addData("ANIMATED", animated);
		verbosef("Added `ANIMATED` to `%s`.\n", symbol);
		buffer.addData("SWITCHES", switches);
		verbosef("Added `SWITCHES` to `%s`.\n", symbol);
		
		return Response.OK;
	}
	
	/**
	 * Loads the contents of a Wad file into a buffer.
	 * Symbol is case-insensitive.
	 * @param symbol the buffer to merge into.
	 * @param wadFile the file to read from.
	 * @return OK if the file was found and contents were merged in, or BAD_SYMBOL if the symbol is invalid. 
	 * @throws IOException if the file could not be read.
	 * @throws WadException if the file is not a Wad file.
	 */
	public Response load(String symbol, File wadFile) throws IOException, WadException
	{
		if (!wadFile.exists() || wadFile.isDirectory())
			return Response.BAD_FILE;

		if (!Wad.isWAD(wadFile))
			return Response.BAD_WAD;

		Response out;
		if ((out = create(symbol)) != Response.OK)
			return out;
		if ((out = mergeWad(symbol, wadFile)) != Response.OK)
			return out;
		return Response.OK;
	}
	
	/**
	 * Saves the contents of a Wad buffer into a file.
	 * Symbol is case-insensitive.
	 * @param symbol the buffer to write.
	 * @param outFile the file to read from.
	 * @return OK if the file was written, or BAD_SYMBOL if the symbol is invalid. 
	 * @throws IOException if the file could not be written.
	 */
	public Response save(String symbol, File outFile) throws IOException
	{
		symbol = symbol.toLowerCase();
		WadBuffer buffer;
		if ((buffer = currentWads.get(symbol)) == null)
			return Response.BAD_SYMBOL;

		createPathForFile(outFile);
		buffer.writeToFile(outFile);
		logf("Wrote file `%s`.\n", outFile.getPath());
		return Response.OK;
	}
	
	/**
	 * Saves the contents of a Wad buffer into a file, and discards the buffer at the symbol.
	 * Symbol is case-insensitive.
	 * @param symbol the buffer to write.
	 * @param outFile the file to read from.
	 * @return OK if the file was written, or BAD_SYMBOL if the symbol is invalid. 
	 * @throws IOException if the file could not be written.
	 */
	public Response finish(String symbol, File outFile) throws IOException
	{
		Response out;
		if ((out = save(symbol, outFile)) != Response.OK)
			return out;
		return discard(symbol);
	}
	
}

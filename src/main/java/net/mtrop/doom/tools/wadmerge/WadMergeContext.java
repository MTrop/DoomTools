/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.wadmerge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.TreeMap;

import net.mtrop.doom.Wad;
import net.mtrop.doom.Wad.Type;
import net.mtrop.doom.WadBuffer;
import net.mtrop.doom.WadEntry;
import net.mtrop.doom.WadFile;
import net.mtrop.doom.exception.WadException;
import net.mtrop.doom.texture.Animated;
import net.mtrop.doom.texture.CommonTextureList;
import net.mtrop.doom.texture.DoomTextureList;
import net.mtrop.doom.texture.PatchNames;
import net.mtrop.doom.texture.StrifeTextureList;
import net.mtrop.doom.texture.Switches;
import net.mtrop.doom.texture.TextureSet;
import net.mtrop.doom.texture.TextureSet.Texture;
import net.mtrop.doom.tools.common.Response;
import net.mtrop.doom.tools.common.Utility;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.common.ParseException;
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
	
	/** Comparator for MERGEDIR file sorting. */
	private static final Comparator<File> DIR_FILESORT = (a, b) -> 
	{
		if (a.isFile())
		{
			if (b.isDirectory())
				return -1;
			else
				return a.getPath().compareTo(b.getPath());
		}
		else
		{
			if (b.isFile())
				return 1;
			else
				return a.getPath().compareTo(b.getPath());
		}
	};

	private final int WIDTH_INDEX = 0;
	private final int HEIGHT_INDEX = 2;

	/** Map of open wads. */
	private TreeMap<String, Wad> currentWads;
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
		this.currentWads = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
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
			logf(seq, args);
	}

	public void logln()
	{
		logln("");
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
	 * @param iwad if true, created WAD is an IWAD.
	 * @return OK if a symbol was created, 
	 * 		or BAD_SYMBOL if the destination symbol already exists.
	 */
	public Response create(String symbol, boolean iwad)
	{
		if (currentWads.containsKey(symbol))
			return Response.BAD_SYMBOL;
		
		WadBuffer buffer = new WadBuffer();
		if (iwad)
			buffer.setType(Type.IWAD);
		currentWads.put(symbol, buffer);
		verbosef("Created buffer `%s`.\n", symbol);
		return Response.OK;
	}

	/**
	 * Creates a blank Wad file.
	 * Symbol is case-insensitive.
	 * @param symbol the symbol to associate with the Wad.
	 * @param wadFile the file name for the WAD to initialize.
	 * @param iwad if true, file is an IWAD, not PWAD.
	 * @return OK if creation successful and a symbol was created, 
	 * 		or BAD_SYMBOL if the destination symbol is invalid.
	 * @throws IOException if an error occurs attempting to create the file.
	 */
	public Response createFile(String symbol, File wadFile, boolean iwad) throws IOException
	{
		if (currentWads.containsKey(symbol))
			return Response.BAD_SYMBOL;
		
		WadFile wad = WadFile.createWadFile(wadFile);
		if (iwad)
			wad.setType(Type.IWAD);
		currentWads.put(symbol, wad);
		logf("Created WAD file `%s` (at `%s`).\n", symbol, wadFile.getPath());
		return Response.OK;
	}

	/**
	 * Checks if a symbol refers to a valid buffer.
	 * Symbol is case-insensitive.
	 * @param symbol the symbol to check.
	 * @return OK if valid, 
	 * 		or BAD_SYMBOL if the symbol is invalid.
	 */
	public Response isValid(String symbol)
	{
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
	 * @return OK if successful, 
	 * 		or BAD_SYMBOL if the symbol is invalid.
	 * @throws IOException if the Wad could not be closed.
	 */
	public Response clear(String symbol) throws IOException
	{
		if (!currentWads.containsKey(symbol))
			return Response.BAD_SYMBOL;
		
		Wad buffer = currentWads.remove(symbol);
		boolean iwad = buffer.isIWAD();
		verbosef("Cleared `%s`.\n", symbol);
		buffer.close();
		if (buffer instanceof WadBuffer)
			return create(symbol, iwad);
		else if (buffer instanceof WadFile)
			return createFile(symbol, new File(((WadFile)buffer).getFilePath()), iwad);
		else
			return Response.UNEXPECTED_ERROR;
	}

	/**
	 * Discards an existing Wad buffer.
	 * Symbol is case-insensitive.
	 * @param symbol the symbol to discard.
	 * @return OK if successful, 
	 * 		or BAD_SYMBOL if the symbol is invalid.
	 * @throws IOException if the Wad could not be closed.
	 */
	public Response discard(String symbol) throws IOException
	{
		if (!currentWads.containsKey(symbol))
			return Response.BAD_SYMBOL;

		currentWads.remove(symbol).close();
		verbosef("Discarded `%s`.\n", symbol);
		return Response.OK;
	}

	/**
	 * Loads the contents of a Wad file into a buffer.
	 * Symbol is case-insensitive.
	 * @param symbol the buffer to merge into.
	 * @param wadFile the file to read from.
	 * @return OK if successful, 
	 * 		or BAD_FILE if the file does not exist or is a directory, 
	 * 		or BAD_WAD if the file is not a WAD, 
	 * 		or BAD_SYMBOL if the destination symbol is invalid.
	 * @throws IOException if the file could not be read.
	 * @throws WadException if the file is not a Wad file.
	 */
	public Response load(String symbol, File wadFile) throws IOException, WadException
	{
		if (!wadFile.exists() || wadFile.isDirectory())
			return Response.BAD_FILE;
	
		if (!Wad.isWAD(wadFile))
			return Response.BAD_WAD;
	
		boolean iwad;
		try (WadFile wf = new WadFile(wadFile))
		{
			iwad = wf.isIWAD();
		}
		
		Response out;
		if ((out = create(symbol, iwad)) != Response.OK)
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
	 * @return OK if export successful, 
	 * 		or BAD_SYMBOL if the symbol is invalid.
	 * @throws IOException if the file could not be written.
	 */
	public Response save(String symbol, File outFile) throws IOException
	{
		Wad buffer;
		if ((buffer = currentWads.get(symbol)) == null)
			return Response.BAD_SYMBOL;
	
		Common.createPathForFile(outFile);
		
		if (buffer instanceof WadBuffer)
		{
			((WadBuffer)buffer).writeToFile(outFile);
			logf("Wrote file `%s`.\n", outFile.getPath());
		}
		else if (buffer instanceof WadFile)
		{
			File wadFile = new File(((WadFile)buffer).getFilePath());
			if (!wadFile.equals(outFile))
			{
				try (WadFile wf = WadFile.extract(outFile, buffer, 0, buffer.getEntryCount()))
				{
					wf.setType(((WadFile)buffer).getType());
				}
				logf("Wrote file `%s`.\n", outFile.getPath());
			}
			// Do nothing if same file.
			else
			{
				logf("Finished file `%s`.\n", outFile.getPath());
			}
		}
		else
			return Response.UNEXPECTED_ERROR;

		return Response.OK;
	}

	/**
	 * Saves the contents of a Wad buffer into a file, and discards the buffer at the symbol.
	 * Symbol is case-insensitive.
	 * @param symbol the buffer to write.
	 * @param outFile the file to read from.
	 * @return OK if export successful, 
	 * 		or BAD_SYMBOL if the symbol is invalid.
	 * @throws IOException if the file could not be written.
	 */
	public Response finish(String symbol, File outFile) throws IOException
	{
		Response out;
		if ((out = save(symbol, outFile)) != Response.OK)
			return out;
		return discard(symbol);
	}

	/**
	 * Adds a marker to a Wad buffer.
	 * Symbol is case-insensitive. The entry is coerced to a valid name.
	 * @param symbol the symbol to use.
	 * @param name the entry name.
	 * @return OK if add successful, 
	 * 		or BAD_SYMBOL if the destination symbol is invalid.
	 * @throws IOException if an I/O error occurs.
	 */
	public Response addMarker(String symbol, String name) throws IOException
	{
		Wad buffer;
		if ((buffer = currentWads.get(symbol)) == null)
			return Response.BAD_SYMBOL;
		
		String marker = NameUtils.toValidEntryName(name);
		buffer.addMarker(marker);
		verbosef("Added marker `%s` to buffer `%s`.\n", marker, symbol);
		return Response.OK;
	}

	/**
	 * Adds an entry to a Wad buffer that contains the current date.
	 * Symbol is case-insensitive. The entry is coerced to a valid name.
	 * @param symbol the symbol to use.
	 * @param name the entry name.
	 * @return OK if add successful, 
	 * 		or BAD_SYMBOL if the destination symbol is invalid.
	 * @throws IOException if an I/O error occurs.
	 */
	public Response addDateMarker(String symbol, String name) throws IOException
	{
		Wad buffer;
		if ((buffer = currentWads.get(symbol)) == null)
			return Response.BAD_SYMBOL;
		
		String marker = NameUtils.toValidEntryName(name);
		buffer.addData(marker, DATE_FORMAT.get().format(new Date()).getBytes(Charset.forName("ASCII")));
		verbosef("Added date marker `%s` to buffer `%s`.\n", marker, symbol);
		return Response.OK;
	}

	/**
	 * Merges a Wad buffer into another.
	 * The symbols are case-insensitive.
	 * @param destinationSymbol the destination buffer.
	 * @param sourceSymbol the source buffer.
	 * @return OK if merge successful, 
	 * 		or BAD_SYMBOL if the destination symbol is invalid,
	 * 		or BAD_SOURCE_SYMBOL if the source symbol is invalid.
	 * @throws IOException if an I/O error occurs.
	 */
	public Response merge(String destinationSymbol, String sourceSymbol) throws IOException
	{
		Wad bufferDest;
		if ((bufferDest = currentWads.get(destinationSymbol)) == null)
			return Response.BAD_SYMBOL;
		
		Wad bufferSource;
		if ((bufferSource = currentWads.get(sourceSymbol)) == null)
			return Response.BAD_SOURCE_SYMBOL;

		destinationSymbol = destinationSymbol.toLowerCase();
		sourceSymbol = sourceSymbol.toLowerCase();
		for (WadEntry e : bufferSource)
		{
			bufferDest.addData(e.getName(), bufferSource.getData(e));
			verbosef("Added entry `%s` to buffer `%s` (from `%s`).\n", e.getName(), destinationSymbol, sourceSymbol);
		}
		
		return Response.OK;
	}
	
	/**
	 * Merges the contents of a Wad file into a buffer.
	 * Symbol is case-insensitive.
	 * @param symbol the buffer to merge into.
	 * @param wadFile the file to read from.
	 * @return OK if merge successful, 
	 * 		or BAD_FILE if the file does not exist or is a directory, 
	 * 		or BAD_WAD if the file is not a WAD, 
	 * 		or BAD_SYMBOL if the destination symbol is invalid.
	 * @throws IOException if the file could not be read.
	 */
	public Response mergeWad(String symbol, File wadFile) throws IOException
	{
		if (!wadFile.exists() || wadFile.isDirectory())
			return Response.BAD_FILE;

		if (!Wad.isWAD(wadFile))
			return Response.BAD_WAD;

		Wad buffer;
		if ((buffer = currentWads.get(symbol)) == null)
			return Response.BAD_SYMBOL;
		
		try (WadFile wad = new WadFile(wadFile))
		{
			verbosef("Reading WAD `%s`...\n", wadFile.getPath());
			Response out = mergeBulkData(buffer, symbol, wad, wadFile.getPath(), wad.getAllEntries());
			verbosef("Done reading `%s`.\n", wadFile.getPath());
			return out;
		}		
	}

	/**
	 * Merges a Wad buffer into another, but just a source namespace from another WAD.
	 * The symbols are case-insensitive.
	 * @param destinationSymbol the destination buffer.
	 * @param wadFile the source WAD file.
	 * @param namespace the target namespace.
	 * @return OK if merge successful, 
	 * 		or BAD_FILE if the file does not exist or is a directory, 
	 * 		or BAD_WAD if the file is not a WAD, 
	 * 		or BAD_SYMBOL if the destination symbol is invalid, 
	 * 		or BAD_SOURCE_SYMBOL if the source symbol is invalid,
	 * 		or BAD_NAMESPACE if the namespace could not be found or is incomplete.
	 * 		or BAD_NAMESPACE_RANGE if the namespace entries are not in sequence.
	 * @throws IOException if the source could not be read.
	 */
	public Response mergeNamespace(String destinationSymbol, File wadFile, String namespace) throws IOException
	{
		if (!wadFile.exists() || wadFile.isDirectory())
			return Response.BAD_FILE;

		if (!Wad.isWAD(wadFile))
			return Response.BAD_WAD;

		Wad bufferDest;
		if ((bufferDest = currentWads.get(destinationSymbol)) == null)
			return Response.BAD_SYMBOL;
		
		try (WadFile wad = new WadFile(wadFile))
		{
			int startIndex;
			if ((startIndex = wad.indexOf(namespace + "_START")) < 0)
				return Response.BAD_NAMESPACE;

			int endIndex;
			if ((endIndex = wad.indexOf(namespace + "_END")) < 0)
				return Response.BAD_NAMESPACE;
			
			if (endIndex < startIndex)
				return Response.BAD_NAMESPACE_RANGE;

			int len = (endIndex - 1) - startIndex; 
			return mergeBulkData(bufferDest, destinationSymbol, wad, wadFile.getPath(), wad.mapEntries(startIndex + 1, len));
		}
	}

	/**
	 * Merges a Wad buffer into another, but just a source namespace from another WAD.
	 * The symbols are case-insensitive.
	 * @param destinationSymbol the destination buffer.
	 * @param sourceSymbol the source buffer.
	 * @param namespace the target namespace.
	 * @return OK if merge successful, 
	 * 		or BAD_SYMBOL if the destination symbol is invalid, 
	 * 		or BAD_SOURCE_SYMBOL if the source symbol is invalid,
	 * 		or BAD_NAMESPACE if the namespace could not be found or is incomplete.
	 * 		or BAD_NAMESPACE_RANGE if the namespace entries are not in sequence.
	 * @throws IOException if the source could not be read.
	 */
	public Response mergeNamespace(String destinationSymbol, String sourceSymbol, String namespace) throws IOException
	{
		Wad bufferDest;
		if ((bufferDest = currentWads.get(destinationSymbol)) == null)
			return Response.BAD_SYMBOL;
		
		Wad bufferSource;
		if ((bufferSource = currentWads.get(sourceSymbol)) == null)
			return Response.BAD_SOURCE_SYMBOL;

		int startIndex;
		if ((startIndex = bufferSource.indexOf(namespace + "_START")) < 0)
			return Response.BAD_NAMESPACE;

		int endIndex;
		if ((endIndex = bufferSource.indexOf(namespace + "_END")) < 0)
			return Response.BAD_NAMESPACE;
		
		if (endIndex < startIndex)
			return Response.BAD_NAMESPACE_RANGE;

		int len = (endIndex - 1) - startIndex; 
		return mergeBulkData(bufferDest, destinationSymbol, bufferSource, sourceSymbol, bufferSource.mapEntries(startIndex + 1, len));
	}

	/**
	 * Merges a single map from a Wad file into a buffer.
	 * Symbol is case-insensitive, as well as entry. The new entry is coerced to a valid name.
	 * @param symbol the buffer to merge into.
	 * @param newHeader the new header name.
	 * @param wadFile the file to read from.
	 * @param header the map header.
	 * @return OK if merge successful, 
	 * 		or BAD_SYMBOL if the destination symbol is invalid,
	 * 		or BAD_FILE if the file does not exist or is a directory,
	 * 		or BAD_WAD if the file is not a WAD, 
	 * 		or BAD_MAP if the map entries are malformed. 
	 * @throws IOException if the file could not be read.
	 * @throws WadException if the file is not a Wad file.
	 */
	public Response mergeMap(String symbol, String newHeader, File wadFile, String header) throws IOException, WadException
	{
		if (!wadFile.exists() || wadFile.isDirectory())
			return Response.BAD_FILE;

		if (!Wad.isWAD(wadFile))
			return Response.BAD_WAD;

		Wad buffer;
		if ((buffer = currentWads.get(symbol)) == null)
			return Response.BAD_SYMBOL;
		
		try (WadFile wad = new WadFile(wadFile))
		{
			Response out = mergeMap(buffer, symbol, newHeader, wad, wadFile.getPath(), header);
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
	 * @return OK if merge successful, 
	 * 		or BAD_SYMBOL if the destination symbol is invalid,
	 * 		or BAD_SOURCE_SYMBOL if the source symbol is invalid,
	 * 		or BAD_MAP if the map entries are malformed. 
	 * @throws IOException if the file could not be read.
	 */
	public Response mergeMap(String destinationSymbol, String newHeader, String sourceSymbol, String header) throws IOException
	{
		destinationSymbol = destinationSymbol.toLowerCase();
		Wad bufferDest;
		if ((bufferDest = currentWads.get(destinationSymbol)) == null)
			return Response.BAD_SYMBOL;
		
		sourceSymbol = sourceSymbol.toLowerCase();
		Wad bufferSource;
		if ((bufferSource = currentWads.get(sourceSymbol)) == null)
			return Response.BAD_SOURCE_SYMBOL;
		
		Response out = mergeMap(bufferDest, destinationSymbol, newHeader, bufferSource, sourceSymbol, header);
		verbosef("Added map `%s` to `%s` as `%s` (from `%s`).\n", header, destinationSymbol, newHeader, sourceSymbol);
		return out;
	}
		
	/**
	 * Merges a single file as an entry into a buffer.
	 * Symbol is case-insensitive. The entry is coerced to a valid name.
	 * @param symbol the buffer to merge into.
	 * @param inFile the file to read.
	 * @param entryName the name of the entry to write as (coerced to a valid name).
	 * @return OK if the was written, 
	 * 		or BAD_SYMBOL if the destination symbol is invalid, 
	 * 		or BAD_FILE if the provided file does not exist or is a directory.
	 * @throws IOException if the file could not be read.
	 */
	public Response mergeFile(String symbol, File inFile, String entryName) throws IOException
	{
		if (!inFile.exists() || inFile.isDirectory())
			return Response.BAD_FILE;

		Wad buffer;
		if ((buffer = currentWads.get(symbol)) == null)
			return Response.BAD_SYMBOL;

		return mergeFileData(buffer, symbol, inFile, entryName);
	}

	/**
	 * Iterates through a directory, adding each file's data into the buffer, 
	 * and if that file is a valid WAD file, it's entries and data are added.
	 * If it encounters a directory, a marker is added (directory name prepended with a backslash), 
	 * and {@link #mergeTree(String, File)} is called on it.
	 * Symbol is case-insensitive.
	 * @param symbol the buffer to write to.
	 * @param inDirectory the directory to read from.
	 * @param filter the file filter to use.
	 * @param omitMarkers if true, omit directory markers.
	 * @return OK if the was written, 
	 * 		or BAD_SYMBOL if the destination symbol is invalid, 
	 * 		or BAD_DIRECTORY if the provided file is not a directory.
	 * @throws IOException if the file could not be written.
	 */
	public Response mergeTree(String symbol, File inDirectory, FileFilter filter, boolean omitMarkers) throws IOException
	{
		if (!inDirectory.exists() || !inDirectory.isDirectory())
			return Response.BAD_DIRECTORY;

		Wad buffer;
		if ((buffer = currentWads.get(symbol)) == null)
			return Response.BAD_SYMBOL;

		File[] files;
		
		// Sort files first, directories last, alphabetical order.
		Arrays.sort(files = inDirectory.listFiles(), DIR_FILESORT);

		WadFile.Adder adder = null;
		try {
			for (File f : files)
			{
				Response resp;
				if (f.isDirectory())
				{
					if (adder != null)
					{
						adder.close();
						adder = null;
					}
					verbosef("Scan directory `%s`...\n", f.getPath());
					if (!omitMarkers && (resp = addMarker(symbol, "\\" + f.getName())) != Response.OK)
						return resp; 
					if ((resp = mergeTree(symbol, f, filter, omitMarkers)) != Response.OK)
						return resp; 
					verbosef("Done scanning directory `%s`.\n", f.getPath());
				}
				else if (filter.accept(f))
				{
					if (Common.getFileExtension(f).equalsIgnoreCase("wad") && Wad.isWAD(f))
					{
						if (adder != null)
						{
							adder.close();
							adder = null;
						}
						if ((resp = mergeWad(symbol, f)) != Response.OK)
							return resp; 
					}
					else if (buffer instanceof WadFile)
					{
						if (adder == null)
							adder = ((WadFile)buffer).createAdder();
						if ((resp = mergeFileData(adder, symbol, f, Common.getFileNameWithoutExtension(f))) != Response.OK)
							return resp; 
					}
					else
					{
						if ((resp = mergeFile(symbol, f, Common.getFileNameWithoutExtension(f))) != Response.OK)
							return resp; 
					}
				}
			}
		} finally {
			Common.close(adder);
		}
		
		return Response.OK;
	}

	/**
	 * Merges a DEUTEX texture file into TEXTUREX/PNAMES entries in a buffer
	 * Will read in an existing PNAMES lump and/or matching texture lump if it exists in the buffer,
	 * and append the new textures and patch names.
	 * Symbol is case-insensitive.
	 * @param symbol the buffer to write to.
	 * @param textureFile the texture file to parse.
	 * @param strife if true, will read and export in Strife format, false for Doom format. 
	 * @param textureEntryName the name of the texture entry name.
	 * @return OK if the file was found and contents were merged in, 
	 * 		or BAD_SYMBOL if the destination symbol is invalid, 
	 * 		or BAD_PARSE if the input file had a parse error,
	 * 		or BAD_FILE if the file does not exist or is a directory.
	 * @throws IOException if the file could not be read.
	 */
	@SuppressWarnings("unchecked")
	public Response mergeDEUTEXTextureFile(String symbol, File textureFile, boolean strife, String textureEntryName) throws IOException
	{
		if (!textureFile.exists() || textureFile.isDirectory())
			return Response.BAD_FILE;

		Wad buffer;
		if ((buffer = currentWads.get(symbol)) == null)
			return Response.BAD_SYMBOL;

		PatchNames pout;
		if (buffer.contains("PNAMES"))
			pout = buffer.getDataAs("PNAMES", PatchNames.class);
		else
			pout = new PatchNames();

		CommonTextureList<?> tout;
		if (buffer.contains(textureEntryName))
		{
			if (strife)
				tout = buffer.getDataAs(textureEntryName, StrifeTextureList.class);
			else
				tout = buffer.getDataAs(textureEntryName, DoomTextureList.class);
		}
		else
		{
			tout = strife ? new StrifeTextureList(128) : new DoomTextureList(128);
		}

		TextureSet textureSet;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(textureFile))))
		{
			textureSet = Utility.readDEUTEXFile(reader, pout, tout);
		} 
		catch (ParseException e) 
		{
			logln("ERROR: "+ textureFile + ", " + e.getMessage());
			return Response.BAD_PARSE;
		}
		
		if (strife)
			textureSet.export(pout, (CommonTextureList<StrifeTextureList.Texture>)(tout = new StrifeTextureList(128)));
		else
			textureSet.export(pout, (CommonTextureList<DoomTextureList.Texture>)(tout = new DoomTextureList(128)));

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
	 * 		or BAD_SYMBOL if the destination symbol is invalid, 
	 * 		or BAD_DIRECTORY if the provided file is not a directory.
	 * @throws IOException if the file could not be read.
	 */
	public Response mergeTextureDirectory(String symbol, File textureDirectory, String textureEntryName) throws IOException
	{
		if (!textureDirectory.exists() || !textureDirectory.isDirectory())
			return Response.BAD_DIRECTORY;

		Wad buffer;
		if ((buffer = currentWads.get(symbol)) == null)
			return Response.BAD_SYMBOL;

		Response resp;
		if ((resp = addMarker(symbol, "PP_START")) != Response.OK)
			return resp;
		
		TextureSet textureSet = new TextureSet(new PatchNames(), new DoomTextureList(128));
		WadFile.Adder adder = (buffer instanceof WadFile) ? ((WadFile)buffer).createAdder() : null;

		File[] files;
		
		// Sort files first, directories last, alphabetical order.
		Arrays.sort(files = textureDirectory.listFiles(), DIR_FILESORT);

		try {
			for (File f : files)
			{
				if (f.isDirectory())
				{
					verbosef("Skipping directory `%s`...\n", f.getPath());
					continue;
				}
				else
				{
					String namenoext = Common.getFileNameWithoutExtension(f);
					if (adder != null)
					{
						if ((resp = mergeFileData(adder, symbol, f, namenoext)) != Response.OK)
							return resp;
					}
					else
					{
						if ((resp = mergeFileData(buffer, symbol, f, namenoext)) != Response.OK)
							return resp;
					}
					String textureName = NameUtils.toValidTextureName(namenoext);
					Texture texture = textureSet.createTexture(textureName);
					texture.setHeight(getTextureDimensionFromFile(f, HEIGHT_INDEX));
					texture.setWidth(getTextureDimensionFromFile(f, WIDTH_INDEX));
					texture.createPatch(textureName);
					verbosef("Add texture `%s`...\n", textureName);
				}
			}
		} finally {
			Common.close(adder);
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
	
	// Assumes that the texture is in the Doom patch format (not a png)
	private int getTextureDimensionFromFile(File file, int byteIndex) throws IOException
	{
		RandomAccessFile raf = new RandomAccessFile(file, "r");

		byte[] dimensionBytes = new byte[2];

		raf.seek(byteIndex);
		raf.read(dimensionBytes);
		raf.close();

		ByteBuffer byteBuffer = ByteBuffer.wrap(dimensionBytes);
		byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
		return byteBuffer.getShort(); 
	}

	/**
	 * Creates ANIMATED and SWITCHES entries in a buffer using a table file read by SWANTBLS.
	 * If ANIMATED and SWITCHES exist, they are appended to.
	 * Symbol is case-insensitive.
	 * @param symbol the buffer to write to.
	 * @param swantblsFile the texture file to parse.
	 * @return OK if the file was found and contents were merged in, 
	 * 		or BAD_SYMBOL if the destination symbol is invalid, 
	 * 		or BAD_DIRECTORY if the provided file is not a directory.
	 * 		or BAD_PARSE if the input file had a parse error.
	 * @throws IOException if the file could not be read.
	 */
	public Response mergeSwitchAnimatedTables(String symbol, File swantblsFile) throws IOException
	{
		if (!swantblsFile.exists() || swantblsFile.isDirectory())
			return Response.BAD_FILE;

		Wad buffer;
		if ((buffer = currentWads.get(symbol)) == null)
			return Response.BAD_SYMBOL;

		Animated animated;
		if ((animated = buffer.getDataAs("ANIMATED", Animated.class)) == null)
			animated = new Animated();
		Switches switches;
		if ((switches = buffer.getDataAs("SWITCHES", Switches.class)) == null)
			switches = new Switches();
		
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(swantblsFile))))
		{
			Utility.readSwitchAnimatedTables(reader, animated, switches);
			buffer.addData("ANIMATED", animated);
			verbosef("Added `ANIMATED` to `%s`.\n", symbol);
			buffer.addData("SWITCHES", switches);
			verbosef("Added `SWITCHES` to `%s`.\n", symbol);
			return Response.OK;
		}
		catch (ParseException e)
		{
			logln("ERROR: "+ swantblsFile + ", " + e.getMessage());
			return Response.BAD_PARSE;
		}
	}

	// Merge map into buffer, with rename.
	private Response mergeMap(Wad targetBuffer, String bufferName, String newHeader, Wad source, String sourceName, String header) throws IOException
	{
		int count = MapUtils.getMapEntryCount(source, header);
		WadEntry[] entries = source.mapEntries(source.indexOf(header) + 1, count - 1);
		if (entries.length == 0)
			return Response.BAD_MAP;
		
		targetBuffer.addData(newHeader, source.getData(header));
		mergeBulkData(targetBuffer, bufferName, source, sourceName, entries);
		return Response.OK;
	}

	private Response mergeBulkData(Wad targetWad, String targetSymbol, Wad sourceWad, String sourceName, WadEntry[] entries) throws IOException
	{
		WadFile.Adder adder = (targetWad instanceof WadFile) ? ((WadFile)targetWad).createAdder() : null;

		try {
			for (WadEntry e : entries)
			{
				if (adder != null)
					adder.addData(e.getName(), sourceWad.getData(e));
				else
					targetWad.addData(e.getName(), sourceWad.getData(e));
				verbosef("Added `%s` to `%s` (from `%s`).\n", e.getName(), targetSymbol, sourceName);
			}
		} finally {
			Common.close(adder);
		}
		return Response.OK;
	}

	private Response mergeFileData(Wad targetWad, String targetSymbol, File inFile, String entryName) throws IOException
	{
		entryName = NameUtils.toValidEntryName(entryName);
		targetWad.addData(entryName, inFile);
		verbosef("Added `%s` to `%s` (from `%s`).\n", entryName, targetSymbol, inFile.getPath());
		return Response.OK;
	}

	private Response mergeFileData(WadFile.Adder targetAdder, String targetSymbol, File inFile, String entryName) throws IOException
	{
		entryName = NameUtils.toValidEntryName(entryName);
		targetAdder.addData(entryName, inFile);
		verbosef("Added `%s` to `%s` (from `%s`).\n", entryName, targetSymbol, inFile.getPath());
		return Response.OK;
	}
	
}

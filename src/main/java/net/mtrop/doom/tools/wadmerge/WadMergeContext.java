package net.mtrop.doom.tools.wadmerge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import net.mtrop.doom.Wad;
import net.mtrop.doom.WadBuffer;
import net.mtrop.doom.WadEntry;
import net.mtrop.doom.WadFile;
import net.mtrop.doom.exception.WadException;
import net.mtrop.doom.io.IOUtils;
import net.mtrop.doom.util.MapUtils;
import net.mtrop.doom.util.NameUtils;

/**
 * The main context for WadMerge.
 * @author Matthew Tropiano
 * TODO: Finish
 */
public class WadMergeContext
{
	/** Simple date format. */
	private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = ThreadLocal.withInitial(
		()->new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ")
	);

	public static enum Response
	{
		OK,
		BAD_SYMBOL,
		BAD_SOURCE_SYMBOL,
		BAD_MAP,
		BAD_WAD,
		BAD_FILE,
		BAD_DIRECTORY,
		UNEXPECTED_ERROR;
	}
	
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
	
	private void verboseln(String seq)
	{
		if (verbose)
			logln(seq);
	}
	
	private void verbosef(String seq, Object... args)
	{
		if (verbose)
			logf(seq);
	}

	private void logln(String seq)
	{
		if (logout != null)
			logout.println(seq);
	}
	
	private void logf(String seq, Object... args)
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
	private static String getFileNameWithoutExtension(String filename, String extensionSeparator)
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
	private static String getFileNameWithoutExtension(File file)
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
			buffer.addMarker(NameUtils.toValidEntryName(name));
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
			buffer.addData(NameUtils.toValidEntryName(name), DATE_FORMAT.get().format(new Date()).getBytes(Charset.forName("ASCII")));
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

		try {
			for (WadEntry e : bufferSource)
				bufferDest.addData(e.getName(), bufferSource.getData(e));
		} catch (IOException e) {
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
			for (WadEntry e : wad)
				buffer.addData(e.getName(), wad.getData(e));
		}
		
		return Response.OK;
	}

	// Merge map into buffer, with rename.
	private static Response mergeMap(WadBuffer buffer, String newHeader, Wad source, String header) throws IOException
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
			return mergeMap(buffer, newHeader, wad, header);
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
		
		return mergeMap(bufferDest, newHeader, bufferSource, header);
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
		return Response.OK;
	}

	/**
	 * Iterates through a directory, adding each file's data into the buffer, 
	 * and if that file is a valid WAD file, it's entries and data are added.
	 * If it encounters a directory, it is skipped.
	 * Symbol is case-insensitive.
	 * @param symbol the buffer to write.
	 * @param inDirectory the directory to read from.
	 * @return OK if the file was written, or BAD_SYMBOL if the symbol is valid, or BAD_DIRECTORY if the provided file is not a directory. 
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
	 * @param symbol the buffer to write.
	 * @param inDirectory the directory to read from.
	 * @return OK if the file was written, or BAD_SYMBOL if the symbol is valid, or BAD_DIRECTORY if the provided file is not a directory. 
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
				if ((resp = addMarker(symbol, "\\" + f.getName())) != Response.OK)
					return resp; 
				continue;
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
	 * Loads the contents of a Wad file into a buffer.
	 * Symbol is case-insensitive.
	 * @param symbol the buffer to merge into.
	 * @param wadFile the file to read from.
	 * @return OK if the file was found and contents were merged in, false otherwise. 
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
		return mergeWad(symbol, wadFile);
	}
	
	/**
	 * Saves the contents of a Wad buffer into a file.
	 * Symbol is case-insensitive.
	 * @param symbol the buffer to write.
	 * @param outFile the file to read from.
	 * @return OK if the file was written, or BAD_SYMBOL if the symbol is valid. 
	 * @throws IOException if the file could not be written.
	 */
	public Response save(String symbol, File outFile) throws IOException
	{
		symbol = symbol.toLowerCase();
		WadBuffer buffer;
		if ((buffer = currentWads.get(symbol)) == null)
			return Response.BAD_SYMBOL;

		buffer.writeToFile(outFile);
		return Response.OK;
	}
	
	/**
	 * Saves the contents of a Wad buffer into a file, and discards the buffer at the symbol.
	 * Symbol is case-insensitive.
	 * @param symbol the buffer to write.
	 * @param outFile the file to read from.
	 * @return OK if the file was written, or BAD_SYMBOL if the symbol is valid. 
	 * @throws IOException if the file could not be written.
	 */
	public Response finish(String symbol, File outFile) throws IOException
	{
		Response out;
		if ((out = save(symbol, outFile)) != Response.OK)
			return out;
		return discard(symbol);
	}
	
	/*

MERGETEXTUREFILE [symbol] [path]
    Reads file from [path], interprets it as a DEUTEX texture/patch assembly file, adds TEXTUREx/PNAMES.
    Error out on [path] I/O error or interpretation error.

MERGETEXTUREDIR [symbol] [path]
    Reads directory from [path].
        Calls `MARKER [symbol] pp_start`.
        For each file in DIR,
            Add [file name] to PNAMES, add [file name] to TEXTURE1 with only patch [file name].
        Calls `MARKER [symbol] pp_end`.
        Export TEXTURE1/PNAMES.
    Error out on [path] I/O error.

MERGESWANTBLS [symbol] [path]
    Reads file from [path], interprets it as a SWANTBLS file, creates two entries in [symbol]: ANIMATED and SWITCHES.
    Error out on [path] I/O error or interpretation error.

	 */
	
}

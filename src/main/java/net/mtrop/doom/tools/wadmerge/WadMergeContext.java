package net.mtrop.doom.tools.wadmerge;

import java.io.File;
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
		return Response.OK;
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
		return currentWads.containsKey(symbol) ? Response.OK : Response.BAD_SYMBOL;
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
		sourceSymbol = sourceSymbol.toLowerCase();
		WadBuffer bufferDest;
		if ((bufferDest = currentWads.get(destinationSymbol)) == null)
			return Response.BAD_SYMBOL;
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
	 */
	public Response mergeWad(String symbol, File wadFile) throws IOException, WadException
	{
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
	private Response mergeMap(WadBuffer buffer, String newHeader, Wad source, String header) throws IOException
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
	 * Symbol is case-insensitive, as well as entry.
	 * @param symbol the buffer to merge into.
	 * @param newHeader the new header name.
	 * @param wadFile the file to read from.
	 * @param header the map header.
	 * @return OK if the file was found and contents were merged in, BAD_SYMBOL otherwise. 
	 */
	public Response mergeMap(String symbol, String newHeader, File wadFile, String header) throws IOException, WadException
	{
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
	 * Symbol is case-insensitive, as well as entry.
	 * @param destinationSymbol the buffer to merge into.
	 * @param newHeader the new header name.
	 * @param sourceSymbol the buffer to read from.
	 * @param header the map header.
	 * @return OK if the file was found and contents were merged in, false otherwise. 
	 */
	public Response mergeMap(String destinationSymbol, String newHeader, String sourceSymbol, String header) throws IOException, WadException
	{
		destinationSymbol = destinationSymbol.toLowerCase();
		sourceSymbol = sourceSymbol.toLowerCase();
		WadBuffer bufferDest;
		if ((bufferDest = currentWads.get(destinationSymbol)) == null)
			return Response.BAD_SYMBOL;
		WadBuffer bufferSource;
		if ((bufferSource = currentWads.get(sourceSymbol)) == null)
			return Response.BAD_SOURCE_SYMBOL;
		
		return mergeMap(bufferDest, newHeader, bufferSource, header);
	}
		
	/*

MERGEFILE [symbol] [path]
    Reads file from [path], 
        If file is WAD type,
            MERGEWAD [symbol] [path]
        Else,
            Add as-is to [symbol].
            Name of entry is coerced from [path] file name.
    Error out on [path] I/O error.

MERGEDIR [symbol] [path]
    Reads directory from [path]. 
        For each file:
            MERGEFILE [symbol] [filepath].
    Error out on [path] I/O error or [filepath] I/O error.

MERGEDIRSET [symbol] [path]
    Reads directory from [path].
        For each FILE in [path], 
            If FILE is DIR,
                MARKER [symbol] \[dirname]
                MERGEDIRSET [symbol] [FILE]
            Else,
                MERGEFILE [symbol] [FILE]
    Error out on [path] I/O error.

MERGESWANTBLS [symbol] [path]
    Reads file from [path], interprets it as a SWANTBLS file, creates two entries in [symbol]: ANIMATED and SWITCHES.
    Error out on [path] I/O error or interpretation error.

MERGETEXTUREFILE [symbol] [path]
    Reads file from [path], interprets it as a texture/patch assembly file, creates TEXTUREx/PNAMES.
    Error out on [path] I/O error or interpretation error.

MERGETEXTUREDIR [symbol] [path]
    Reads directory from [path].
        Calls `MARKER [symbol] pp_start`.
        For each file in DIR,
            Add [file name] to PNAMES, add [file name] to TEXTURE1 with only patch [file name].
        Calls `MARKER [symbol] pp_end`.
        Export TEXTURE1/PNAMES.
    Error out on [path] I/O error.

LOAD [symbol] [path]
    Synonym for:
        CREATE [symbol]
        MERGEWAD [symbol] [path]

SAVE [symbol] [path]
    Saves WadBuffer [symbol] out to a WAD file [path].
    Error out on I/O error.

FINISH [symbol] [path]
    Synonym for:
        SAVE [symbol] [path]
        DISCARD [symbol]
	 */
	
}

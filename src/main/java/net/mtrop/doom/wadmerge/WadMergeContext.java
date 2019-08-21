package net.mtrop.doom.wadmerge;

import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import net.mtrop.doom.WadBuffer;
import net.mtrop.doom.WadEntry;
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
	
	/** Map of open wads. */
	private HashMap<String, WadBuffer> currentWads;
	
	/**
	 * Creates a new context.
	 */
	public WadMergeContext()
	{
		this.currentWads = new HashMap<String, WadBuffer>();
	}
	
	/**
	 * Creates a blank Wad buffer.
	 * Symbol is case-insensitive.
	 * @param symbol the symbol to associate with the Wad.
	 * @return true if the buffer does not exist and it was created, false otherwise.
	 */
	public boolean create(String symbol)
	{
		symbol = symbol.toLowerCase();
		if (currentWads.containsKey(symbol))
			return false;
		currentWads.put(symbol, new WadBuffer());
		return true;
	}
	
	/**
	 * Clears an existing Wad buffer.
	 * Symbol is case-insensitive.
	 * @param symbol the symbol to clear.
	 * @return true if the buffer exists and it was cleared, false otherwise.
	 */
	public boolean clear(String symbol)
	{
		symbol = symbol.toLowerCase();
		if (!currentWads.containsKey(symbol))
			return false;
		currentWads.put(symbol, new WadBuffer());
		return true;
	}

	/**
	 * Discards an existing Wad buffer.
	 * Symbol is case-insensitive.
	 * @param symbol the symbol to discard.
	 * @return true if the buffer exists and it was discarded, false otherwise.
	 */
	public boolean discard(String symbol)
	{
		symbol = symbol.toLowerCase();
		if (!currentWads.containsKey(symbol))
			return false;
		currentWads.remove(symbol);
		return true;
	}

	/**
	 * Checks if a symbol refers to a valid buffer.
	 * Symbol is case-insensitive.
	 * @param symbol the symbol to check.
	 * @return true if the buffer exists, false otherwise.
	 */
	public boolean isValid(String symbol)
	{
		symbol = symbol.toLowerCase();
		return currentWads.containsKey(symbol);
	}
	
	/**
	 * Adds a marker to a Wad buffer.
	 * Symbol is case-insensitive. The entry is coerced to a valid name.
	 * @param symbol the symbol to use.
	 * @param name the entry name.
	 * @return true if the buffer exists and was added to, false otherwise.
	 */
	public boolean addMarker(String symbol, String name)
	{
		symbol = symbol.toLowerCase();
		WadBuffer buffer;
		if ((buffer = currentWads.get(symbol)) == null)
			return false;
		try {
			buffer.addMarker(NameUtils.toValidEntryName(name));
		} catch (IOException e) {
			// Shouldn't happen.
		}
		return true;
	}

	/**
	 * Adds an entry to a Wad buffer that contains the current date.
	 * Symbol is case-insensitive. The entry is coerced to a valid name.
	 * @param symbol the symbol to use.
	 * @param name the entry name.
	 * @return true if the buffer exists and was added to, false otherwise.
	 */
	public boolean addDateMarker(String symbol, String name)
	{
		symbol = symbol.toLowerCase();
		WadBuffer buffer;
		if ((buffer = currentWads.get(symbol)) == null)
			return false;
		try {
			buffer.addData(NameUtils.toValidEntryName(name), DATE_FORMAT.get().format(new Date()).getBytes(Charset.forName("ASCII")));
		} catch (IOException e) {
			// Shouldn't happen.
		}
		return true;
	}

	/**
	 * Merges a Wad buffer into another.
	 * The symbols are case-insensitive.
	 * @param destinationSymbol the destination buffer.
	 * @param sourceSymbol the source buffer.
	 * @return true if both buffers exist and the merge worked, false otherwise.
	 */
	public boolean merge(String destinationSymbol, String sourceSymbol)
	{
		destinationSymbol = destinationSymbol.toLowerCase();
		sourceSymbol = sourceSymbol.toLowerCase();
		WadBuffer bufferDest;
		if ((bufferDest = currentWads.get(destinationSymbol)) == null)
			return false;
		WadBuffer bufferSource;
		if ((bufferSource = currentWads.get(sourceSymbol)) == null)
			return false;

		try {
			for (WadEntry e : bufferSource)
				bufferDest.addData(e.getName(), bufferSource.getData(e));
		} catch (IOException e) {
			// Shouldn't happen.
		}
		
		return true;
	}
	
}

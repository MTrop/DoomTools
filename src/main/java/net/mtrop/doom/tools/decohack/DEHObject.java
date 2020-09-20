package net.mtrop.doom.tools.decohack;

import java.io.IOException;
import java.io.Writer;

/**
 * Describes all DeHackEd objects and how to write them.
 * @author Matthew Tropiano
 */
public interface DEHObject
{
	/**
	 * Writes this object to a DeHackEd file stream.
	 * @param writer the writer to write to.
	 * @throws IOException if a write error occurs.
	 */
	public void writeObject(Writer writer) throws IOException;
	
}

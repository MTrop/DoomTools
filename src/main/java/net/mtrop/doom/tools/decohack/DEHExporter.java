package net.mtrop.doom.tools.decohack;

import java.io.IOException;
import java.io.Writer;

/**
 * Describes all DeHackEd contexts that can be exported.
 * @author Matthew Tropiano
 */
public interface DEHExporter
{
	/**
	 * Writes this patch to a DeHackEd file stream.
	 * @param writer the writer to write to.
	 * @param comment the comment line.
	 * @throws IOException if a write error occurs.
	 */
	void writePatch(Writer writer, String comment) throws IOException;
	
}

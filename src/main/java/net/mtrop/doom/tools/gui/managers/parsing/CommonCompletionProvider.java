package net.mtrop.doom.tools.gui.managers.parsing;

import java.io.IOException;
import java.io.StringWriter;

import org.fife.ui.autocomplete.DefaultCompletionProvider;

import net.mtrop.doom.tools.struct.HTMLWriter;
import net.mtrop.doom.tools.struct.HTMLWriter.Options;

/**
 * A common completion provider with useful functions.
 * @author Matthew Tropiano
 */
public abstract class CommonCompletionProvider extends DefaultCompletionProvider 
{
	protected CommonCompletionProvider()
	{
		super();
	}
	
	/**
	 * Creates an HTMLWriter with common settings, and writes stuff to the provided string writer.
	 * @param writeFunc the writing function to call with a new HTMLWriter.
	 * @return the String written.
	 */
	protected static String writeHTML(IOConsumer<HTMLWriter> writeFunc)
	{
		StringWriter out = new StringWriter(1024);
		try (HTMLWriter html = new HTMLWriter(out, Options.SLASHES_IN_SINGLE_TAGS)) 
		{
			html.push("html").push("body");
			writeFunc.accept(html);
			html.end();
		} 
		catch (IOException e)
		{
			// Do nothing - shouldn't be thrown.
		}
		return out.toString();
	}
	
	@FunctionalInterface
	protected interface IOConsumer<T>
	{
		/**
		 * Consumer execution method that eats any exceptions.
		 * @param t the input.
		 * @throws IOException 
		 */
		void accept(T t) throws IOException;
	}
	
}

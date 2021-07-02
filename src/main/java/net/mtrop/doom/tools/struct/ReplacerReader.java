package net.mtrop.doom.tools.struct;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A reader that replaces strings as characters get read in.
 * Tokens are defined that correspond to strings that become the values read
 * as replace tokens are encountered. All tokens must be bound by a start and end
 * sequence of characters. 
 * @author Matthew Tropiano
 */
public class ReplacerReader extends Reader
{
	/** The default string of characters that starts a replacer token. */
	public static final String DEFAULT_TOKEN_START = "%";
	/** The default string of characters that ends a replacer token. */
	public static final String DEFAULT_TOKEN_END = "%";
	
	/** The reuseable string buffer for token search. */
	private StringBuilder tokenBuffer;
	/** The map of token to character sequence. */
	private Map<String, Supplier<String>> tokenMap;
	/** The current string to read from, if not null. */
	private Deque<StringReader> replaceStringReader;
	/** The internal reader. */
	private Reader internalReader;
	
	/**
	 * Wraps a Reader for this new reader as a reading source.
	 * @param reader the Reader to wrap.
	 */
	public ReplacerReader(Reader reader) 
	{
		this.tokenBuffer = new StringBuilder(64);
		this.tokenMap = new HashMap<>();
		this.replaceStringReader = new LinkedList<>();
		this.internalReader = reader;
	}

	/**
	 * Wraps a String as a reading source.
	 * @param input the string to read from. 
	 */
	public ReplacerReader(String input) 
	{
		this(new StringReader(input));
	}
	
	/**
	 * Sets or redefines a replace token.
	 * Note that replacement strings can also contain tokens that can get recursively replaced.
	 * @param replaceToken the token that gets replaced. 
	 * @param result the result string to use for the replacement.
	 * @return this reader, for chaining.
	 */
	public ReplacerReader setReplaceToken(String replaceToken, String result)
	{
		return setReplaceToken(replaceToken, ()->result);
	}
	
	/**
	 * Sets or redefines a replace token. 
	 * Note that the provided replacement strings can also contain tokens that can get recursively replaced.
	 * @param replaceToken the token that gets replaced. 
	 * @param supplier the supplier function that provides the string for replacement.
	 * @return this reader, for chaining.
	 */
	public ReplacerReader setReplaceToken(String replaceToken, Supplier<String> supplier)
	{
		tokenMap.put(replaceToken, supplier);
		return this;
	}
	
	// Scans characters until a viable token or character is found.
	private int scanChar() throws IOException
	{
		// TODO: Write this.
		return readChar();
	}
	
	// Reads a single character from the internal reader or the replace buffer.
	private int readChar() throws IOException
	{
		if (!replaceStringReader.isEmpty())
		{
			int out = replaceStringReader.peek().read();
			if (out < 0)
			{
				replaceStringReader.peek().close();
				replaceStringReader.pop();
				return readChar();
			}
			else
			{
				return out;
			}
		}
		return internalReader.read();
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException
	{
		int i;
		boolean success = false;
		int end = off + len;
		for (i = off; i < end; i++)
		{
			int c = scanChar();
			if (c < 0)
				break;
			success = true;
			cbuf[i] = (char)c;
		}
		return success ? i - off : -1;
	}
	
	@Override
	public void close() throws IOException 
	{
		while (!replaceStringReader.isEmpty())
		{
			replaceStringReader.peek().close();
			replaceStringReader.pop();
		}
		replaceStringReader = null;
		internalReader.close();
	}

}

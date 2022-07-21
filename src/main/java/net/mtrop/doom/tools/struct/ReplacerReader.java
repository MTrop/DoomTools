/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A reader that replaces strings as characters get read in.
 * Tokens are defined that correspond to strings that become the values read
 * as replace tokens are encountered. All tokens must be bound by a start and end
 * sequence of characters. If a token is malformed, it is streamed out as-is.
 * @author Matthew Tropiano
 */
public class ReplacerReader extends Reader
{
	/** The default string of characters that starts a replacer token. */
	public static final String DEFAULT_TOKEN_START = "%";
	/** The default string of characters that ends a replacer token. */
	public static final String DEFAULT_TOKEN_END = "%";
	
	/** The internal reader. */
	private Reader internalReader;
	/** The token start delimiter. */
	private String tokenStart;
	/** The token end delimiter. */
	private String tokenEnd;

	/** The reuseable string buffer for token search. */
	private StringBuilder tokenBuffer;
	/** The map of token to character sequence. */
	private Map<String, Supplier<String>> tokenMap;
	/** The current string to read from, if not null. */
	private StringReader replaceStringReader;
	
	/**
	 * Wraps a Reader for this new reader as a reading source.
	 * @param reader the Reader to wrap.
	 * @param tokenStart the token start delimiter sequence.
	 * @param tokenEnd the token end delimiter sequence.
	 */
	public ReplacerReader(Reader reader, String tokenStart, String tokenEnd) 
	{
		this.internalReader = reader;
		this.tokenStart = tokenStart;
		this.tokenEnd = tokenEnd;
		
		this.tokenBuffer = new StringBuilder(64);
		this.tokenMap = new HashMap<>();
		this.replaceStringReader = null;
	}

	/**
	 * Wraps a String as a reading source.
	 * @param input the string to read from. 
	 * @param tokenStart the token start delimiter sequence.
	 * @param tokenEnd the token end delimiter sequence.
	 */
	public ReplacerReader(String input, String tokenStart, String tokenEnd) 
	{
		this(new StringReader(input), tokenStart, tokenEnd);
	}
	
	/**
	 * Wraps a Reader for this new reader as a reading source, with the default token start and end delimiters.
	 * @param reader the Reader to wrap.
	 * @see #DEFAULT_TOKEN_START
	 * @see #DEFAULT_TOKEN_END
	 */
	public ReplacerReader(Reader reader) 
	{
		this(reader, DEFAULT_TOKEN_START, DEFAULT_TOKEN_END);
	}

	/**
	 * Wraps a String as a reading source, with the default token start and end delimiters.
	 * @param input the string to read from. 
	 * @see #DEFAULT_TOKEN_START
	 * @see #DEFAULT_TOKEN_END
	 */
	public ReplacerReader(String input) 
	{
		this(new StringReader(input), DEFAULT_TOKEN_START, DEFAULT_TOKEN_END);
	}
	
	/**
	 * Sets or redefines a replace token.
	 * @param replaceToken the token that gets replaced. 
	 * @param result the result string to use for the replacement.
	 * @return this reader, for chaining.
	 */
	public ReplacerReader replace(String replaceToken, String result)
	{
		return replace(replaceToken, ()->result);
	}
	
	/**
	 * Sets or redefines a replace token. 
	 * @param replaceToken the token that gets replaced. 
	 * @param supplier the supplier function that provides the string for replacement.
	 * @return this reader, for chaining.
	 */
	public ReplacerReader replace(String replaceToken, Supplier<String> supplier)
	{
		tokenMap.put(replaceToken, supplier);
		return this;
	}
	
	/**
	 * Sets or redefines a replace token using a map of key-values.
	 * The map contents are copied into this one.
	 * @param tokenMapping the token mapping. 
	 * @return this reader, for chaining.
	 * @see #replace(String, String)
	 */
	public ReplacerReader replace(Map<String, String> tokenMapping)
	{
		for (Map.Entry<String, String> entry : tokenMapping.entrySet())
			replace(entry.getKey(), entry.getValue());
		return this;
	}
	
	// Clears a buffer.
	private static void clearBuffer(StringBuilder sb)
	{
		sb.delete(0, sb.length());
	}
	
	// Sets a string for the replacement reader.
	private void pushString(String s)
	{
		replaceStringReader = new StringReader(s);
	}
	
	// Sets a string for the replacement reader using the parsed token.
	private void pushReplacement(final StringBuilder sb)
	{
		String token = sb.substring(tokenStart.length(), sb.length() - tokenEnd.length());
		String out = tokenMap.getOrDefault(token, ()->sb.toString()).get(); // return self if not found.
		replaceStringReader = new StringReader(out);
	}
	
	// Scans characters until a viable token or character is found.
	private int scanChar() throws IOException
	{
		final int STATE_READ = 0;
		final int STATE_TOKEN_STARTING = 1;
		final int STATE_TOKEN = 2;
		final int STATE_TOKEN_ENDING = 3;
		int state = STATE_READ;
		int tokenOffset = 0;
		while (true)
		{
			int c = readChar();
			if (c < 0)
			{
				if (tokenBuffer.length() > 0)
				{
					pushString(tokenBuffer.toString());
					clearBuffer(tokenBuffer);
					return scanChar();
				}
				else
				{
					return -1;
				}
			}
			else if (replaceStringReader != null)
			{
				return c;
			}
			
			char t = (char)c;
			
			switch (state)
			{
				default:
				case STATE_READ:
				{
					if (t == tokenStart.charAt(tokenOffset))
					{
						tokenBuffer.append(t);
						if (++tokenOffset < tokenStart.length()) 
						{
							state = STATE_TOKEN_STARTING;
						}
						else
						{
							tokenOffset = 0;
							state = STATE_TOKEN;
						}
					}
					else
					{
						return c;
					}
				}
				break;

				case STATE_TOKEN_STARTING:
				{
					if (t == tokenStart.charAt(tokenOffset))
					{
						tokenBuffer.append(t);
						if (++tokenOffset < tokenStart.length()) 
						{
							// No change - keep state.
						}
						else
						{
							tokenOffset = 0;
							state = STATE_TOKEN;
						}
					}
					else // not start - dump to secondary stream and read.
					{
						tokenBuffer.append(t);
						pushString(tokenBuffer.toString());
						clearBuffer(tokenBuffer);
						return scanChar();
					}
				}
				break;

				case STATE_TOKEN:
				{
					if (t == tokenEnd.charAt(tokenOffset))
					{
						tokenBuffer.append(t);
						if (++tokenOffset < tokenEnd.length()) 
						{
							state = STATE_TOKEN_ENDING;
						}
						else
						{
							pushReplacement(tokenBuffer);
							clearBuffer(tokenBuffer);
							return scanChar();
						}
					}
					else
					{
						tokenBuffer.append(t);
					}
				}
				break;

				case STATE_TOKEN_ENDING:
				{
					if (t == tokenEnd.charAt(tokenOffset))
					{
						tokenBuffer.append(t);
						if (++tokenOffset < tokenEnd.length()) 
						{
							// No change - keep state.
						}
						else
						{
							pushReplacement(tokenBuffer);
							clearBuffer(tokenBuffer);
							return scanChar();
						}
					}
					else // not end - jump back to token.
					{
						tokenBuffer.append(t);
						tokenOffset = 0;
						state = STATE_TOKEN;
					}
				}
				break;
			}
		}
	}
	
	// Reads a single character from the internal reader or the replace buffer.
	private int readChar() throws IOException
	{
		if (replaceStringReader != null)
		{
			int out = replaceStringReader.read();
			if (out < 0)
			{
				replaceStringReader.close();
				replaceStringReader = null;
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
		if (replaceStringReader != null)
			replaceStringReader.close();
		replaceStringReader = null;
		internalReader.close();
	}

}

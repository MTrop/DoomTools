/*******************************************************************************
 * Copyright (c) 2021 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A simple scanner.
 * Can read escaped text and quoted strings, using whitespace as delimiters.
 * <p>
 * If created in a try-with-resources block, this can auto-close the 
 * underlying Reader once you are done scanning.
 * @author Matthew Tropiano
 */
public class TokenScanner implements AutoCloseable, Iterator<String>
{
	private static final ThreadLocal<StringBuilder> BUILDER = ThreadLocal.withInitial(()->new StringBuilder());
	
	/** Source reader. */
	private Reader reader;
	/** Next loaded token (from a {@link #hasNext()} call). */
	private String heldToken;
	
	/**
	 * Creates a new scanner that reads from a Reader. 
	 * @param reader the Reader.
	 */
	public TokenScanner(Reader reader)
	{
		this.reader = reader;
	}
	
	/**
	 * Creates a new scanner that reads from a String. 
	 * @param str the string to read from. 
	 */
	public TokenScanner(String str)
	{
		this(new StringReader(str));
	}
	
	/**
	 * Creates a new scanner that reads from an InputStream with provided encoding. 
	 * @param in the input stream.
	 * @param charset the encoding of the provided stream.
	 */
	public TokenScanner(InputStream in, Charset charset)
	{
		this(new InputStreamReader(in, charset));
	}
	
	/**
	 * Creates a new scanner that reads from an InputStream with default platform encoding. 
	 * @param in the input stream.
	 */
	public TokenScanner(InputStream in)
	{
		this(in, Charset.defaultCharset());
	}

	/**
	 * Closes the underlying Reader, throwing a RuntimeException on exception.
	 * @see AutoCloseable#close()
	 */
	@Override
	public void close()
	{
		try {
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException("I/O Error closing the Reader.", e);
		}
	}
	
	/**
	 * Reads a single character, throwing a {@link RuntimeException} if a problem occurs.
	 * @return the character as an integer, or -1 of end-of-stream.
	 * @throws RuntimeException if an I/O problem occurs.
	 * @see Reader#read()
	 */
	protected final int readChar()
	{
		try {
			return reader.read();
		} catch (IOException e) {
			throw new RuntimeException("I/O Error reading a character.", e);
		}
	}
	
	/**
	 * Returns true if this is a hex digit (0-9, A-F, a-f).
	 * @param c the character to test.
	 * @return true if so, false if not.
	 */
	protected final boolean isHexDigit(int c)
	{
		return (c >= 0x0030 && c <= 0x0039) || 
			(c >= 0x0041 && c <= 0x0046) || 
			(c >= 0x0061 && c <= 0x0066);
	}

	/**
	 * Returns the hex value of this character.
	 * @param c the character to test.
	 * @return the value, or -1 if no value.
	 */
	protected final int getHexValue(int c)
	{
		if (c >= 0x0030 && c <= 0x0039)
			return c - 0x0030;
		else if (c >= 0x0041 && c <= 0x0046)
			return (c - 0x0041) + 10;
		else if (c >= 0x0061 && c <= 0x0066)
			return (c - 0x0061) + 10;
		else
			return -1;
	}

	/**
	 * Attempts to read a hexadecimal digit.
	 * @param errmsg the error message to use if not end-of-stream.
	 * @return the hexadecimal value read.
	 * @throws RuntimeException if an I/O problem occurs from reading.
	 */
	protected final int readHexDigit(String errmsg)
	{
		int r;
		if (!isHexDigit(r = readChar()))
		{
			if (r == -1)
				throw new ParseException("Malformed string - unexpected stream end.");
			else
				throw new ParseException(errmsg + ((char)r));
		}
		return getHexValue(r);
	}

	/**
	 * Scans for the next token, but if no token is scanned,
	 * {@link NoSuchElementException} is thrown.
	 * @return the next token.
	 * @throws NoSuchElementException if no token scanned.
	 * @throws RuntimeException if an I/O problem occurs from reading.
	 */
	protected final String expectToken()
	{
		String token;
		if ((token = nextToken()) == null)
			throw new NoSuchElementException("No more tokens to scan. End-of-stream reached.");
		return token;
	}

	/**
	 * Reads characters until a non-whitespace character is encountered or end-of-stream is reached.
	 * @return the first non-whitespace character found, or -1 if end-of-stream reached. 
	 * @throws RuntimeException if an I/O problem occurs from reading.
	 * @see Character#isWhitespace(char)
	 */
	protected int skipWhitespace()
	{
		int r;
		while ((r = readChar()) >= 0 && Character.isWhitespace((char)r)) {}
		return r;
	}
	
	@Override
	public boolean hasNext()
	{
		return heldToken != null || (heldToken = nextToken()) != null;
	}
	
	@Override
	public String next()
	{
		return nextString();
	}

	/**
	 * Reads until a full string is read, and returns it.
	 * <p> 
	 * Tokens can just be a series of non-whitespace characters, or a series of characters
	 * bounded by double quotes. Double-quoted strings can have escaped characters in them:
	 * <table>
	 * 		<thead>
	 * 			<tr>
	 * 				<th>Escape sequence</th>
	 * 				<th>Output</th>
	 * 			</tr>
	 * 		</thead>
	 * 		<tbody>
	 * 			<tr><td>\0</td><td>NULL character (UTF-16 0x0000)</td></tr>
	 * 			<tr><td>\b</td><td>Backspace</td></tr>
	 * 			<tr><td>\t</td><td>Tab</td></tr>
	 * 			<tr><td>\n</td><td>Newline</td></tr>
	 * 			<tr><td>\f</td><td>Form feed</td></tr>
	 * 			<tr><td>\r</td><td>Carriage Return</td></tr>
	 * 			<tr><td>\"</td><td>Double quote</td></tr>
	 * 			<tr><td>\\</td><td>Backslash</td></tr>
	 * 			<tr><td>\&#117;<em>hhhh</em></td><td>UTF-16 character (where <em>hhhh</em> is 4 hex digits)</td></tr>
	 * 			<tr><td>\x<em>hh</em></td><td>ASCII character (where <em>hh</em> is 2 hex digits)</td></tr>
	 * 		</tbody>
	 * </table>
	 * @return the next string, or null if no more tokens.
	 * @throws RuntimeException if an I/O problem occurs from reading.
	 * @throws ParseException if a malformed string is read (bad escaping).
	 */
	public String nextToken()
	{
		if (heldToken != null)
		{
			String out = heldToken;
			heldToken = null;
			return out;
		}
		
		final int STATE_START = 0;
		final int STATE_STRING = 1;
		final int STATE_QUOTE_DOUBLE = 2;
		final int STATE_QUOTE_ESCAPE = 3;
		final int STATE_BREAK = 4;
		int state = STATE_START;
		
		StringBuilder sb = BUILDER.get();
		sb.delete(0, sb.length());
				
		int r;
		if ((r = skipWhitespace()) < 0)
			return null;
		
		do {
			char c = (char)r;
			
			switch (state)
			{
				case STATE_START:
				{
					if (c == '"')
						state = STATE_QUOTE_DOUBLE;
					else
					{
						sb.append(c);
						state = STATE_STRING;
					}
				}
				break;

				case STATE_STRING:
				{
					sb.append(c);
				}
				break;

				case STATE_QUOTE_DOUBLE:
				{
					if (c == '"')
						state = STATE_BREAK;
					else if (c == '\\')
						state = STATE_QUOTE_ESCAPE;
					else
						sb.append(c);
				}
				break;

				case STATE_QUOTE_ESCAPE:
				{
					if (c == '"')
						sb.append('"');
					else if (c == 'b')
						sb.append('\b');
					else if (c == 't')
						sb.append('\t');
					else if (c == 'n')
						sb.append('\n');
					else if (c == 'f')
						sb.append('\f');
					else if (c == 'r')
						sb.append('\r');
					else if (c == '\\')
						sb.append('\\');
					else if (c == 'x')
					{
						int v = 0;
						String errmsg = "Malformed string - bad character in ASCII byte escape: "; 
						v |= readHexDigit(errmsg) << 4;
						v |= readHexDigit(errmsg);
						sb.append((char)v);
					}
					else if (c == 'u')
					{
						int v = 0;
						String errmsg = "Malformed string - bad character in Unicode character escape: "; 
						v |= readHexDigit(errmsg) << 12;
						v |= readHexDigit(errmsg) << 8;
						v |= readHexDigit(errmsg) << 4;
						v |= readHexDigit(errmsg);
						sb.append((char)v);
					}
					else
					{
						throw new ParseException("Unrecognized or unsupported escape sequence.");
					}
					state = STATE_QUOTE_DOUBLE;
				}
				break;
			}
			
			if (state == STATE_BREAK)
				break;
			
			r = readChar();
			
			if (state == STATE_QUOTE_DOUBLE)
			{
				if (r < 0)
					throw new ParseException("Missing an ending quote.");
			}
			else
			{
				if (r < 0 || Character.isWhitespace((char)r))
					break;
			}
			
		} while (true);
		
		return sb.toString();
	}

	/**
	 * Scans for the next token and interprets it as a string.
	 * @return the next string.
	 * @throws NoSuchElementException if no more tokens.
	 * @throws RuntimeException if an I/O problem occurs from reading.
	 * @see #nextToken()
	 */
	public String nextString()
	{
		return expectToken();
	}
	
	/**
	 * Scans for the next token and interprets it as an integer that fits in a byte.
	 * @param radix the numerical base/radix for the expected number. 
	 * @return the next byte value.
	 * @throws NumberFormatException if the token can't be interpreted as a byte.
	 * @throws NoSuchElementException if no more tokens.
	 * @throws RuntimeException if an I/O problem occurs from reading.
	 * @see Byte#parseByte(String, int)
	 * @see #nextToken()
	 */
	public byte nextByte(int radix)
	{
		return Byte.parseByte(expectToken(), radix);
	}
	
	/**
	 * Scans for the next token and interprets it as an integer that fits in a byte.
	 * Expects a number in base-10. 
	 * @return the next byte value.
	 * @throws NumberFormatException if the token can't be interpreted as a byte.
	 * @throws NoSuchElementException if no more tokens.
	 * @throws RuntimeException if an I/O problem occurs from reading.
	 * @see Byte#parseByte(String, int)
	 * @see #nextToken()
	 */
	public byte nextByte()
	{
		return nextByte(10);
	}
	
	/**
	 * Scans for the next token and interprets it as an integer that fits in a short.
	 * @param radix the numerical base/radix for the expected number. 
	 * @return the next short value.
	 * @throws NumberFormatException if the token can't be interpreted as a short.
	 * @throws NoSuchElementException if no more tokens.
	 * @throws RuntimeException if an I/O problem occurs from reading.
	 * @see Short#parseShort(String, int)
	 * @see #nextToken()
	 */
	public short nextShort(int radix)
	{
		return Short.parseShort(expectToken(), radix);
	}
	
	/**
	 * Scans for the next token and interprets it as an integer that fits in a short.
	 * Expects a number in base-10. 
	 * @return the next short value.
	 * @throws NumberFormatException if the token can't be interpreted as a short.
	 * @throws NoSuchElementException if no more tokens.
	 * @throws RuntimeException if an I/O problem occurs from reading.
	 * @see Short#parseShort(String, int)
	 * @see #nextToken()
	 */
	public short nextShort()
	{
		return nextShort(10);
	}
	
	/**
	 * Scans for the next token and interprets it as an integer.
	 * @param radix the numerical base/radix for the expected number. 
	 * @return the next integer value.
	 * @throws NumberFormatException if the token can't be interpreted as a integer.
	 * @throws NoSuchElementException if no more tokens.
	 * @throws RuntimeException if an I/O problem occurs from reading.
	 * @see Integer#parseInt(String, int)
	 * @see #nextToken()
	 */
	public int nextInt(int radix)
	{
		return Integer.parseInt(expectToken(), radix);
	}
	
	/**
	 * Scans for the next token and interprets it as an integer.
	 * Expects a number in base-10. 
	 * @return the next integer value.
	 * @throws NumberFormatException if the token can't be interpreted as a integer.
	 * @throws NoSuchElementException if no more tokens.
	 * @throws RuntimeException if an I/O problem occurs from reading.
	 * @see Integer#parseInt(String, int)
	 * @see #nextToken()
	 */
	public int nextInt()
	{
		return nextInt(10);
	}
	
	/**
	 * Scans for the next token and interprets it as a long integer.
	 * @param radix the numerical base/radix for the expected number. 
	 * @return the next long value.
	 * @throws NumberFormatException if the token can't be interpreted as a long.
	 * @throws NoSuchElementException if no more tokens.
	 * @throws RuntimeException if an I/O problem occurs from reading.
	 * @see Integer#parseInt(String, int)
	 * @see #nextToken()
	 */
	public long nextLong(int radix)
	{
		return Long.parseLong(expectToken(), radix);
	}
	
	/**
	 * Scans for the next token and interprets it as a long integer.
	 * Expects a number in base-10. 
	 * @return the next long value.
	 * @throws NumberFormatException if the token can't be interpreted as a long.
	 * @throws NoSuchElementException if no more tokens.
	 * @throws RuntimeException if an I/O problem occurs from reading.
	 * @see Integer#parseInt(String, int)
	 * @see #nextToken()
	 */
	public long nextLong()
	{
		return nextLong(10);
	}
	
	/**
	 * Scans for the next token and interprets it as a BigInteger.
	 * @param radix the numerical base/radix for the expected number. 
	 * @return the next BigInteger value.
	 * @throws NumberFormatException if the token can't be interpreted as a BigInteger.
	 * @throws NoSuchElementException if no more tokens.
	 * @throws RuntimeException if an I/O problem occurs from reading.
	 * @see BigInteger
	 * @see #nextToken()
	 */
	public BigInteger nextBigInteger(int radix)
	{
		return new BigInteger(expectToken(), radix);
	}
	
	/**
	 * Scans for the next token and interprets it as a BigInteger.
	 * Expects a number in base-10. 
	 * @return the next BigInteger value.
	 * @throws NumberFormatException if the token can't be interpreted as a BigInteger.
	 * @throws NoSuchElementException if no more tokens.
	 * @throws RuntimeException if an I/O problem occurs from reading.
	 * @see BigInteger
	 * @see #nextToken()
	 */
	public BigInteger nextBigInteger()
	{
		return nextBigInteger(10);
	}
	
	/**
	 * Scans for the next token and interprets it as a float.
	 * @return the next float value.
	 * @throws NumberFormatException if the token can't be interpreted as a float.
	 * @throws NoSuchElementException if no more tokens.
	 * @throws RuntimeException if an I/O problem occurs from reading.
	 * @see Float#parseFloat(String)
	 * @see #nextToken()
	 */
	public float nextFloat()
	{
		return Float.parseFloat(expectToken());
	}
	
	/**
	 * Scans for the next token and interprets it as a double-precision float.
	 * @return the next double value.
	 * @throws NumberFormatException if the token can't be interpreted as a double.
	 * @throws NoSuchElementException if no more tokens.
	 * @throws RuntimeException if an I/O problem occurs from reading.
	 * @see Double#parseDouble(String)
	 * @see #nextToken()
	 */
	public double nextDouble()
	{
		return Double.parseDouble(expectToken());
	}
	
	/**
	 * Scans for the next token and interprets it as a BigDecimal.
	 * @return the next BigDecimal value.
	 * @throws NumberFormatException if the token can't be interpreted as a BigDecimal.
	 * @throws NoSuchElementException if no more tokens.
	 * @throws RuntimeException if an I/O problem occurs from reading.
	 * @see BigDecimal
	 * @see #nextToken()
	 */
	public BigDecimal nextBigDecimal()
	{
		return new BigDecimal(expectToken());
	}
	
	/**
	 * Exception thrown when a parse error occurs. 
	 */
	public static class ParseException extends RuntimeException 
	{
		private static final long serialVersionUID = 1959603462166085682L;

		private ParseException(String message)
		{
			super(message);
		}

		private ParseException(String message, Throwable cause)
		{
			super(message, cause);
		}
	}

}

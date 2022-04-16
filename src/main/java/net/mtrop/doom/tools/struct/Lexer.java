/*******************************************************************************
 * Copyright (c) 2019-2022 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Breaks up a stream of characters into lexicographical tokens.
 * Spaces, newlines, tabs, comments, and breaks in the stream are added if desired,
 * otherwise, they are stripped out. 
 * <p>
 * String delimiter characters take precedence over regular delimiters.
 * Raw String delimiter characters take precedence over regular string delimiters.
 * Delimiter characters take parsing priority over other characters.
 * Delimiter evaluation priority goes: Comment Delimiter, Delimiter.
 * Identifier evaluation priority goes: Keyword, CaseInsensitiveKeyword, Identifier.
 * <p>
 * Other implementations of this class may manipulate the stack as well (such as ones that do in-language stream inclusion).
 * <p>
 * If the system property <code>com.blackrook.base.Lexer.debug</code> is set to <code>true</code>, this does debugging output to {@link System#out}.
 * <p>
 * Lexer functions are NOT thread-safe.
 * @author Matthew Tropiano
 */
public class Lexer
{
	public static boolean DEBUG = parseBoolean(System.getProperty(Lexer.class.getName()+".debug"), false);
	
	/** Lexer end-of-stream char. */
	public static final char END_OF_LEXER = '\uffff';
	/** Lexer end-of-stream char. */
	public static final char END_OF_STREAM = '\ufffe';
	/** Lexer newline char. */
	public static final char NEWLINE = '\n';
	
	/** The current stream stack. */
	private ReaderStack readerStack;
	/** The lexer kernel to use. */
	private Kernel kernel;
	
	// ============ STATE =============
	
	/** Current token builder. */
	private StringBuilder tokenBuffer;

	/**
	 * Creates a new lexer with no streams.
	 * This will also assign this lexer a default name.
	 * @param kernel the lexer kernel to use for defining how to parse the input text.
	 */
	public Lexer(Kernel kernel)
	{
		this(kernel, null, (Reader)null);
	}
	
	/**
	 * Creates a new lexer around a String, that will be wrapped into a StringReader.
	 * This will also assign this lexer a default name.
	 * @param kernel the lexer kernel to use for defining how to parse the input text.
	 * @param in the string to read from.
	 */
	public Lexer(Kernel kernel, String in)
	{
		this(kernel, null, in);
	}
	
	/**
	 * Creates a new lexer around a String, that will be wrapped into a StringReader.
	 * @param kernel the lexer kernel to use for defining how to parse the input text.
	 * @param name the name of this lexer.
	 * @param in the reader to read from.
	 */
	public Lexer(Kernel kernel, String name, String in)
	{
		this(kernel, name, new StringReader(in));
	}
	
	/**
	 * Creates a new lexer around a reader.
	 * This will also assign this lexer a default name.
	 * @param kernel the kernel to use for this lexer.
	 * @param in the reader to read from.
	 */
	public Lexer(Kernel kernel, Reader in)
	{
		this(kernel, null, in);
	}
	
	/**
	 * Creates a new lexer around a reader.
	 * @param kernel the kernel to use for this lexer.
	 * @param name the name of this lexer.
	 * @param in the reader to read from. If null, does not push a stream.
	 */
	public Lexer(Kernel kernel, String name, Reader in)
	{
		this.kernel = kernel;
		readerStack = new ReaderStack();
		tokenBuffer = new StringBuilder();
		if (in != null)
			pushStream(name, in);
	}
	
	/**
	 * @return the lexer's current stream name.
	 */
	public String getCurrentStreamName()
	{
		if (readerStack.isEmpty())
			return "LEXER END";
		return readerStack.getCurrentStreamName();
	}

	/**
	 * Gets the lexer's current stream's line number.
	 * @return the lexer's current stream's line number, or -1 if at Lexer end.
	 */
	public int getCurrentLineNumber()
	{
		if (readerStack.isEmpty())
			return -1;
		return readerStack.getCurrentLineNumber();
	}

	/**
	 * Gets the current stream.
	 * @return the name of the current stream.
	 */
	public ReaderStack.Stream getCurrentStream()
	{
		return readerStack.peek();
	}

	/**
	 * Pushes a stream onto the encapsulated reader stack.
	 * @param name the name of the stream.
	 * @param in the reader reader.
	 */
	public void pushStream(String name, Reader in)
	{
		readerStack.push(name, in);
	}
	
	/**
	 * Gets the next token.
	 * If there are no tokens left to read, this will return null.
	 * This method is NOT thread-safe!
	 * @return the next token, or null if no more tokens to read.
	 * @throws IOException if a token cannot be read by the underlying Reader.
	 */
	public Token nextToken() throws IOException
	{
		int lineNumber = -1;
		int charIndex = 0;
		int state = Kernel.TYPE_UNKNOWN;
		boolean breakloop = false;
		Character stringEnd = null;
		String commentEnd = null;
		int commentEndIndex = 0;
		
		while (!breakloop)
		{
			char c = readChar();
			
			switch (state)
			{
				case Kernel.TYPE_END_OF_LEXER:
				{
					breakloop = true;
					break;
				}

				case Kernel.TYPE_UNKNOWN:
				{
					if (isLexerEnd(c))
					{
						state = Kernel.TYPE_END_OF_LEXER;
						breakloop = true;
					}
					else if (isStreamEnd(c))
					{
						if (kernel.willEmitStreamBreak())
						{
							state = Kernel.TYPE_END_OF_STREAM;
							charIndex = readerStack.getCurrentLineCharacterIndex();
							lineNumber = readerStack.getCurrentLineNumber();
							breakloop = true;
						}
						close(readerStack.pop());
					}
					else if (isNewline(c))
					{
						if (kernel.willEmitNewlines())
						{
							state = Kernel.TYPE_NEWLINE;
							charIndex = readerStack.getCurrentLineCharacterIndex();
							lineNumber = readerStack.getCurrentLineNumber();
							breakloop = true;
						}
					}
					else if (isSpace(c))
					{
						if (kernel.willEmitSpaces())
						{
							state = Kernel.TYPE_SPACE;
							charIndex = readerStack.getCurrentLineCharacterIndex();
							lineNumber = readerStack.getCurrentLineNumber();
							breakloop = true;
						}
					}
					else if (isTab(c))
					{
						if (kernel.willEmitTabs())
						{
							state = Kernel.TYPE_TAB;
							charIndex = readerStack.getCurrentLineCharacterIndex();
							lineNumber = readerStack.getCurrentLineNumber();
							breakloop = true;
						}
					}
					else if (isWhitespace(c))
					{
						// Eat leading whitespace.
					}
					else if (isPoint(c) && isDelimiterStart(c))
					{
						state = Kernel.TYPE_POINT;
						charIndex = readerStack.getCurrentLineCharacterIndex();
						lineNumber = readerStack.getCurrentLineNumber();
						saveChar(c);
					}
					else if (isPoint(c) && !isDelimiterStart(c))
					{
						state = Kernel.TYPE_FLOAT;
						charIndex = readerStack.getCurrentLineCharacterIndex();
						lineNumber = readerStack.getCurrentLineNumber();
						saveChar(c);
					}
					else if (isStringStart(c))
					{
						state = Kernel.TYPE_STRING;
						charIndex = readerStack.getCurrentLineCharacterIndex();
						lineNumber = readerStack.getCurrentLineNumber();
						stringEnd = getStringStartAndEnd(c);
					}
					else if (isRawStringStart(c))
					{
						state = Kernel.TYPE_RAWSTRING;
						charIndex = readerStack.getCurrentLineCharacterIndex();
						lineNumber = readerStack.getCurrentLineNumber();
						stringEnd = getRawStringStartAndEnd(c);
					}
					else if (isDelimiterStart(c))
					{
						state = Kernel.TYPE_DELIMITER;
						charIndex = readerStack.getCurrentLineCharacterIndex();
						lineNumber = readerStack.getCurrentLineNumber();
						saveChar(c);
					}
					else if (c == '0')
					{
						state = Kernel.TYPE_HEX_INTEGER0;
						charIndex = readerStack.getCurrentLineCharacterIndex();
						lineNumber = readerStack.getCurrentLineNumber();
						saveChar(c);
					}
					else if (isDigit(c))
					{
						state = Kernel.TYPE_NUMBER;
						charIndex = readerStack.getCurrentLineCharacterIndex();
						lineNumber = readerStack.getCurrentLineNumber();
						saveChar(c);
					}
					// anything else starts an identifier.
					else
					{
						state = Kernel.TYPE_IDENTIFIER;
						charIndex = readerStack.getCurrentLineCharacterIndex();
						lineNumber = readerStack.getCurrentLineNumber();
						saveChar(c);
					}
					break; // end Kernel.TYPE_START_OF_LEXER
				}
				
				case Kernel.TYPE_ILLEGAL:
				{
					if (isStreamEnd(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isNewline(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isSpace(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isTab(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isWhitespace(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isStringStart(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isRawStringStart(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isDelimiterStart(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else
					{
						saveChar(c);
					}

					break; // end Kernel.TYPE_ILLEGAL
				}

				case Kernel.TYPE_POINT: // decimal point is seen, but it is a delimiter.
				{
					if (isStreamEnd(c))
					{
						state = Kernel.TYPE_DELIMITER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isNewline(c))
					{
						state = Kernel.TYPE_DELIMITER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isSpace(c))
					{
						state = Kernel.TYPE_DELIMITER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isTab(c))
					{
						state = Kernel.TYPE_DELIMITER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isWhitespace(c))
					{
						state = Kernel.TYPE_DELIMITER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isStringStart(c))
					{
						state = Kernel.TYPE_DELIMITER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isRawStringStart(c))
					{
						state = Kernel.TYPE_DELIMITER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isDigit(c))
					{
						state = Kernel.TYPE_FLOAT;
						saveChar(c);
					}
					else
					{
						state = Kernel.TYPE_DELIMITER;
						if (kernel.getDelimTable().containsKey(getCurrentLexeme() + c))
							saveChar(c);
						else
						{
							setDelimBreak(c);
							breakloop = true;
						}
					}
					break; // end Kernel.TYPE_POINT
				}
					
				case Kernel.TYPE_FLOAT:
				{
					if (isStreamEnd(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isNewline(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isSpace(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isTab(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isWhitespace(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isExponent(c))
					{
						state = Kernel.TYPE_EXPONENT;
						saveChar(c);
					}
					else if (isStringStart(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isRawStringStart(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isDigit(c))
					{
						saveChar(c);
					}
					else if (isDelimiterStart(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else
					{
						state = Kernel.TYPE_ILLEGAL;
						saveChar(c);
					}
					break; // end Kernel.TYPE_FLOAT
				}
					
				case Kernel.TYPE_IDENTIFIER:
				{
					if (isStreamEnd(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isNewline(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isSpace(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isTab(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isWhitespace(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isStringStart(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isRawStringStart(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isDelimiterStart(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else
					{
						saveChar(c);
					}
					break; // end Kernel.TYPE_IDENTIFIER
				}
					
				case Kernel.TYPE_HEX_INTEGER0:
				{
					if (isStreamEnd(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isNewline(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isSpace(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isTab(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isWhitespace(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isPoint(c))
					{
						state = Kernel.TYPE_FLOAT;
						saveChar(c);
					}
					else if (isStringStart(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isRawStringStart(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isDelimiterStart(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (c == 'x' || c == 'X')
					{
						state = Kernel.TYPE_HEX_INTEGER1;
						saveChar(c);
					}
					else if (isLetter(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						saveChar(c);
					}
					else if (isDigit(c))
					{
						state = Kernel.TYPE_NUMBER;
						saveChar(c);
					}
					else
					{
						state = Kernel.TYPE_ILLEGAL;
						saveChar(c);
					}
					break; // end Kernel.TYPE_HEX_INTEGER0
				}

				case Kernel.TYPE_HEX_INTEGER1:
				{
					if (isStreamEnd(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isNewline(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isSpace(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isTab(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isWhitespace(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isPoint(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isStringStart(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isRawStringStart(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isDelimiterStart(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isHexDigit(c))
					{
						state = Kernel.TYPE_HEX_INTEGER;
						saveChar(c);
					}
					else if (isLetter(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						saveChar(c);
					}
					else
					{
						state = Kernel.TYPE_ILLEGAL;
						saveChar(c);
					}
					break; // end Kernel.TYPE_HEX_INTEGER1
				}

				case Kernel.TYPE_HEX_INTEGER:
				{
					if (isStreamEnd(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isNewline(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isSpace(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isTab(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isWhitespace(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isStringStart(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isRawStringStart(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isDelimiterStart(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isHexDigit(c))
					{
						saveChar(c);
					}
					else if (isLetter(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						saveChar(c);
					}
					else
					{
						state = Kernel.TYPE_ILLEGAL;
						saveChar(c);
					}
					break; // end Kernel.TYPE_HEX_INTEGER
				}

				case Kernel.TYPE_NUMBER:
				{
					if (isStreamEnd(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isNewline(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isSpace(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isTab(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isWhitespace(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isPoint(c))
					{
						state = Kernel.TYPE_FLOAT;
						saveChar(c);
					}
					else if (isExponent(c))
					{
						state = Kernel.TYPE_EXPONENT;
						saveChar(c);
					}
					else if (isStringStart(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isRawStringStart(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isDelimiterStart(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isLetter(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						saveChar(c);
					}
					else if (isDigit(c))
					{
						saveChar(c);
					}
					else
					{
						state = Kernel.TYPE_ILLEGAL;
						saveChar(c);
					}
					break; // end Kernel.TYPE_NUMBER
				}

				case Kernel.TYPE_EXPONENT:
				{
					if (isStreamEnd(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isNewline(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isSpace(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isTab(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isWhitespace(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isExponentSign(c))
					{
						state = Kernel.TYPE_EXPONENT_POWER;
						saveChar(c);
					}
					else if (isStringStart(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isRawStringStart(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isDelimiterStart(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isLetter(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						saveChar(c);
					}
					else if (isDigit(c))
					{
						state = Kernel.TYPE_EXPONENT_POWER;
						saveChar(c);
					}
					else
					{
						state = Kernel.TYPE_ILLEGAL;
						saveChar(c);
					}
					break; // end Kernel.TYPE_EXPONENT
				}
					
				case Kernel.TYPE_EXPONENT_POWER:
				{
					if (isStreamEnd(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isNewline(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isSpace(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isTab(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isWhitespace(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isStringStart(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isRawStringStart(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isDigit(c))
					{
						saveChar(c);
					}
					else if (isDelimiterStart(c))
					{
						state = Kernel.TYPE_NUMBER;
						setDelimBreak(c);
						breakloop = true;
					}
					else
					{
						state = Kernel.TYPE_ILLEGAL;
						saveChar(c);
					}
					break; // end Kernel.TYPE_EXPONENT_POWER
				}

				case Kernel.TYPE_STRING:
				{
					if (isStreamEnd(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (stringEnd == c)
					{
						breakloop = true;
					}
					else if (isNewline(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isStringEscape(c))
					{
						c = readChar();
						if (stringEnd == c)
							saveChar(c);
						else if (isStringEscape(c))
							saveChar(c);
						else switch (c)
						{
							case '0':
								saveChar('\0');
								break;
							case 'b':
								saveChar('\b');
								break;
							case 't':
								saveChar('\t');
								break;
							case 'n':
								saveChar('\n');
								break;
							case 'f':
								saveChar('\f');
								break;
							case 'r':
								saveChar('\r');
								break;
							case '/':
								saveChar('/');
								break;
							case 'u':
							{
								StringBuilder sb = new StringBuilder();
								for (int i = 0; i < 4; i++)
								{
									c = readChar();
									if (!isHexDigit(c))
									{
										state = Kernel.TYPE_ILLEGAL;
										setDelimBreak(c);
										breakloop = true;
									}
									else
										sb.append(c);
								}
								
								if (!breakloop)
								{
									saveChar((char)(Integer.parseInt(sb.toString(), 16) & 0x0ffff));
								}
							}
							break;
							
							case 'x':
							{
								StringBuilder sb = new StringBuilder();
								for (int i = 0; i < 2; i++)
								{
									c = readChar();
									if (!isHexDigit(c))
									{
										state = Kernel.TYPE_ILLEGAL;
										setDelimBreak(c);
										breakloop = true;
									}
									else
										sb.append(c);
								}
								
								if (!breakloop)
								{
									saveChar((char)(Integer.parseInt(sb.toString(), 16) & 0x0ff));
								}
							}
							break;
						}
						
					}
					else
					{
						saveChar(c);
					}
					break; // end Kernel.TYPE_STRING
				}

				case Kernel.TYPE_RAWSTRING:
				{
					if (isStreamEnd(c))
					{
						state = Kernel.TYPE_ILLEGAL;
						setDelimBreak(c);
						breakloop = true;
					}
					else if (stringEnd == c)
					{
						state = Kernel.TYPE_STRING;
						breakloop = true;
					}
					else
					{
						saveChar(c);
					}
					break; // end Kernel.TYPE_RAWSTRING
				}
				
				case Kernel.TYPE_DELIMITER:
				{
					String lookAheadDelimiter = getCurrentLexeme() + c;
					
					if (isStreamEnd(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (kernel.getCommentTable().containsKey(lookAheadDelimiter))
					{
						commentEnd = kernel.getCommentTable().get(lookAheadDelimiter);
						clearCurrentLexeme();
						state = Kernel.TYPE_COMMENT;
					}
					else if (kernel.getCommentLineSet().contains(lookAheadDelimiter))
					{
						clearCurrentLexeme();
						state = Kernel.TYPE_LINE_COMMENT;
					}
					else if (kernel.getDelimTable().containsKey(lookAheadDelimiter))
					{
						saveChar(c);
					}
					else if (isNewline(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isSpace(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isTab(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isWhitespace(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isStringStart(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else if (isRawStringStart(c))
					{
						setDelimBreak(c);
						breakloop = true;
					}
					else
					{
						setDelimBreak(c);
						breakloop = true;
					}
					break; // end Kernel.TYPE_DELIMITER
				}
					
				case Kernel.TYPE_COMMENT:
				{
					if (isStreamEnd(c))
					{
						if (!kernel.willEmitComments())
						{
							clearCurrentLexeme();
							state = Kernel.TYPE_UNKNOWN;
						}
						else
						{
							saveChar(c);
							breakloop = true;
						}
					}
					else if (commentEnd.charAt(0) == c)
					{
						if (commentEnd.length() == 1)
						{
							state = Kernel.TYPE_COMMENT;
						}
						else
						{
							state = Kernel.TYPE_ENDING_COMMENT;
							commentEndIndex = 1;
							saveChar(c);
						}
					}
					else
					{
						saveChar(c);
					}
					break; // end Kernel.TYPE_COMMENT
				}

				case Kernel.TYPE_ENDING_COMMENT:
				{
					if (isStreamEnd(c))
					{
						state = Kernel.TYPE_UNKNOWN;
						breakloop = true;
					}
					else if (commentEnd.charAt(commentEndIndex) == c)
					{
						if (commentEndIndex < commentEnd.length() - 1)
						{
							commentEndIndex++;
							saveChar(c);
						}
						else if (kernel.willEmitComments())
						{
							tokenBuffer.delete(tokenBuffer.length() - commentEnd.length(), tokenBuffer.length());
							state = Kernel.TYPE_COMMENT;
							breakloop = true;
						}
						else
						{
							clearCurrentLexeme();
							state = Kernel.TYPE_UNKNOWN;
						}
					}
					else if (commentEnd.charAt(0) == c)
					{
						commentEndIndex = 1;
						saveChar(c);
					}
					else
					{
						state = Kernel.TYPE_COMMENT;
						saveChar(c);
					}
					break; // end Kernel.TYPE_ENDING_COMMENT
				}
					
				case Kernel.TYPE_LINE_COMMENT:
				{
					if (isStreamEnd(c))
					{
						if (!kernel.willEmitComments())
						{
							clearCurrentLexeme();
							state = Kernel.TYPE_UNKNOWN;
						}
						else
						{
							breakloop = true;
						}
					}
					else if (isNewline(c))
					{
						if (!kernel.willEmitComments())
						{
							clearCurrentLexeme();
							state = Kernel.TYPE_UNKNOWN;
						}
						else
						{
							breakloop = true;
						}
					}
					else
					{
						saveChar(c);
					}
					break; // end Kernel.TYPE_LINE_COMMENT
				}
			}
		}

		// send token.
		int type = state;
		String lexeme = getCurrentLexeme();
		clearCurrentLexeme();
		
		Token out = null;
		if (state != Kernel.TYPE_END_OF_LEXER)
		{
			String streamName = readerStack.getCurrentStreamName();
			out = new Token(streamName, type, lexeme, lineNumber, charIndex);
			modifyType(out);
		}
		
		if (DEBUG)
			System.out.println(out);
		return out;
	}

	/**
	 * Called when the lexer wants to create a token,
	 * but the lexeme of the token may cause this token to be a different type.
	 * <p>
	 * By default, this handles space, tab, newline, delimiter, and identifier.
	 * <p>
	 * If this method is overridden, this should have 
	 * <pre>
	 * if (super.modifyType(token)) 
	 *	 return true;
	 * </pre>
	 * right at the beginning.
	 * 
	 * @param token the original token.
	 * @return true if the token's contents were changed, false if not.
	 */
	protected boolean modifyType(Token token)
	{
		switch (token.getType())
		{
			case Kernel.TYPE_SPACE:
			{
				token.setLexeme(" ");
				return true;
			}
			
			case Kernel.TYPE_TAB:
			{
				token.setLexeme("\t");
				return true;
			}
			
			case Kernel.TYPE_NEWLINE:
			{
				token.setLexeme("");
				return true;
			}
			
			case Kernel.TYPE_DELIMITER:
			{
				String lexeme = token.getLexeme();
				if (kernel.getDelimTable().containsKey(lexeme))
				{
					token.setType(kernel.getDelimTable().get(lexeme));
					return true;
				}
				break;
			}
			
			case Kernel.TYPE_IDENTIFIER:
			{
				String lexeme = token.getLexeme();
				if (kernel.getKeywordTable().containsKey(lexeme))
				{
					token.setType(kernel.getKeywordTable().get(lexeme));
					return true;
				}
				else
				{
					String lk = lexeme.toLowerCase();
					if (kernel.getCaseInsensitiveKeywordTable().containsKey(lk))
					{
						token.setType(kernel.getCaseInsensitiveKeywordTable().get(lk));
						return true;
					}
				}
				break;
			}
			
		}
		return false;
	}

	/**
	 * Reads a character from the stream.
	 * @return the character read, or {@link #END_OF_LEXER} if no more characters, or {@link #END_OF_STREAM} if end of current stream.
	 * @throws IOException if a character cannot be read.
	 */
	protected char readChar() throws IOException
	{
		if (readerStack.isEmpty())
			return END_OF_LEXER;
	
		int c = readerStack.readChar();

		if (c >= 0)
			return (char)c; 
		else
			return END_OF_STREAM; 
	}

	/**
	 * Sets if we are in a delimiter break.
	 * @param delimChar the delimiter character that starts the break.
	 */
	protected void setDelimBreak(char delimChar)
	{
		if (getCurrentStream() != null)
			getCurrentStream().pushChar(delimChar);
	}
	
	/**
	 * Saves a character for the next token.
	 * @param c the character to save into the current token.
	 */
	protected void saveChar(char c)
	{
		tokenBuffer.append(c);
	}
	
	/**
	 * Gets the end character for a string start character.
	 * @param c the start delimiter character.
	 * @return the corresponding end, or null if no character.
	 */
	protected Character getStringStartAndEnd(char c)
	{
		return isStringStart(c) ? getStringEnd(c) : null;
	}

	/**
	 * Gets the end character for a multi-line, "raw" string start character.
	 * @param c the start delimiter character.
	 * @return the corresponding end, or null if no character.
	 */
	protected Character getRawStringStartAndEnd(char c)
	{
		return isRawStringStart(c) ? getRawStringEnd(c) : null;
	}

	/**
	 * Gets the current token lexeme.
	 * @return the current contents of the token lexeme builder buffer. 
	 */
	protected String getCurrentLexeme()
	{
		return tokenBuffer.toString();
	}

	/**
	 * Clears the current token lexeme buffer.
	 */
	protected void clearCurrentLexeme()
	{
		tokenBuffer.delete(0, tokenBuffer.length());
	}

	/**
	 * Convenience method for <code>c == '_'</code>.
	 * @param c the character to test.
	 * @return true if so, false if not.
	 */
	protected boolean isUnderscore(char c)
	{
		return c == '_';
	}
	
	/**
	 * Convenience method for {@link Character#isLetter(char)}.
	 * @param c the character to test.
	 * @return true if so, false if not.
	 */
	protected boolean isLetter(char c)
	{
		return Character.isLetter(c);
	}
	
	/**
	 * Convenience method for {@link Character#isDigit(char)}.
	 * @param c the character to test.
	 * @return true if so, false if not.
	 */
	protected boolean isDigit(char c)
	{
		return Character.isDigit(c);
	}
	
	/**
	 * Returns true if this is a hex digit (0-9, A-F, a-f).
	 * @param c the character to test.
	 * @return true if so, false if not.
	 */
	protected boolean isHexDigit(char c)
	{
		return (c >= 0x0030 && c <= 0x0039) || 
			(c >= 0x0041 && c <= 0x0046) || 
			(c >= 0x0061 && c <= 0x0066);
	}
	
	/**
	 * Convenience method for {@link Character#isWhitespace(char)}.
	 * @param c the character to test.
	 * @return true if so, false if not.
	 */
	protected boolean isWhitespace(char c)
	{
		return Character.isWhitespace(c);
	}

	/**
	 * Checks if a character is a decimal point (depends on locale/kernel).
	 * @param c the character to test.
	 * @return true if so, false if not.
	 */
	protected boolean isPoint(char c)
	{
		return kernel.getDecimalSeparator() == c;
	}
	
	/**
	 * Checks if char is the exponent character in a number.
	 * @param c the character to test.
	 * @return true if so, false if not.
	 */
	protected boolean isExponent(char c)
	{
		return c == 'E' || c == 'e';
	}
	
	/**
	 * Checks if char is the exponent sign character in a number.
	 * @param c the character to test.
	 * @return true if so, false if not.
	 */
	protected boolean isExponentSign(char c)
	{
		return c == '+' || c == '-';
	}
	
	/**
	 * Checks if a char is a space.
	 * @param c the character to test.
	 * @return true if so, false if not.
	 */
	protected boolean isSpace(char c)
	{
		return c == ' ';
	}

	/**
	 * Checks if a char is a tab.
	 * @param c the character to test.
	 * @return true if so, false if not.
	 */
	protected boolean isTab(char c)
	{
		return c == '\t';
	}

	/**
	 * Checks if this is a character that is a String escape character.
	 * @param c the character to test.
	 * @return true if so, false if not.
	 */
	protected boolean isStringEscape(char c)
	{
		return c == '\\';
	}
	
	/**
	 * Checks if this is a character that starts a String.
	 * @param c the character to test.
	 * @return true if so, false if not.
	 */
	protected boolean isStringStart(char c)
	{
		return kernel.getStringDelimTable().containsKey(c);
	}
	
	/**
	 * Checks if this is a character that starts a multiline String.
	 * @param c the character to test.
	 * @return true if so, false if not.
	 */
	protected boolean isRawStringStart(char c)
	{
		return kernel.getRawStringDelimTable().containsKey(c);
	}
	
	/**
	 * Gets the character that ends a String, using the starting character.
	 * @param c the starting character.
	 * @return the corresponding end character, or the null character ('\0') if this does not end a string.
	 */
	protected char getStringEnd(char c)
	{
		return kernel.getStringDelimTable().get(c);
	}
	
	/**
	 * Gets the character that ends a raw String, using the starting character.
	 * @param c the starting character.
	 * @return the corresponding end character, or the null character ('\0') if this does not end a multi-line string.
	 */
	protected char getRawStringEnd(char c)
	{
		return kernel.getRawStringDelimTable().get(c);
	}
	
	/**
	 * Checks if this is a (or the start of a) delimiter character.
	 * @param c the character input.
	 * @return true if so, false if not.
	 */
	protected boolean isDelimiterStart(char c)
	{
		return kernel.getDelimStartTable().contains(c);
	}
	
	/**
	 * Checks if a char equals {@link #END_OF_STREAM}.
	 * @param c the character input.
	 * @return true if so, false if not.
	 */
	protected boolean isStreamEnd(char c)
	{
		return c == END_OF_STREAM;
	}

	/**
	 * Checks if a char equals {@link #END_OF_LEXER}.
	 * @param c the character input.
	 * @return true if so, false if not.
	 */
	protected boolean isLexerEnd(char c)
	{
		return c == END_OF_LEXER;
	}

	/**
	 * Checks if a char equals {@link #NEWLINE}.
	 * @param c the character input.
	 * @return true if so, false if not.
	 */
	protected boolean isNewline(char c)
	{
		return c == NEWLINE;
	}

	// Closes stuff.
	private static void close(AutoCloseable c)
	{
		if (c == null) return;
		try { c.close(); } catch (Exception e){}
	}

	// Check if a string is blank.
	private static boolean isStringEmpty(String obj)
	{
		if (obj == null)
			return true;
		else
			return obj.trim().length() == 0;
	}

	// Parse boolean.
	private static boolean parseBoolean(String s, boolean def)
	{
		if (isStringEmpty(s))
			return def;
		else if (!s.equalsIgnoreCase("true"))
			return false;
		else
			return true;
	}

	/**
	 * This holds a series of {@link Reader} streams such that the stream on top is the current active stream.
	 * This was separated out of {@link Lexer} to mix different Lexers on one stack (different lexers, same stream).
	 * It is the user's responsibility to {@link #pop()} streams off of the stack when they reach an end. 
	 * @author Matthew Tropiano
	 */
	public static class ReaderStack
	{
		/** Stream stack. */
		private LinkedList<Stream> innerStack;
	
		/**
		 * Creates a new empty ReaderStack. 
		 */
		public ReaderStack()
		{
			innerStack = new LinkedList<>();
		}
		
		/**
		 * Creates a new ReaderStack. 
		 * @param name the name to give the first reader.
		 * @param reader the reader itself (assumed open).
		 */
		public ReaderStack(String name, Reader reader)
		{
			this();
			push(name, reader);
		}
		
		/**
		 * Pushes another reader onto the stack.
		 * @param name the name to give this reader.
		 * @param reader the reader itself (assumed open).
		 */
		public final void push(String name, Reader reader)
		{
			innerStack.add(new Stream(name, reader));
		}
		
		/**
		 * Gets the reference to the topmost (current) stream.
		 * @return the topmost stream.
		 */
		public final Stream peek()
		{
			return innerStack.peekLast();
		}
		
		/**
		 * Pops a {@link Stream} off of the top of this stack.
		 * The stream is closed automatically.
		 * @return the removed {@link Stream}, or null if empty.
		 * @see #isEmpty()
		 */
		public final Stream pop()
		{
			Stream out = innerStack.pollLast();
			if (out != null)
				close(out);
			return out;
		}
		
		/**
		 * @return the current stream name, or null if empty.
		 * @see #isEmpty()
		 */
		public final String getCurrentStreamName()
		{
			if (isEmpty())
				return null;
			return peek().getStreamName();
		}
	
		/**
		 * @return the current line number, or -1 if empty.
		 * @see #isEmpty()
		 */
		public final int getCurrentLineNumber()
		{
			if (isEmpty())
				return -1;
			return peek().getLine();
		}
	
		/**
		 * @return the current line number, or -1 if empty.
		 * @see #isEmpty()
		 */
		public final int getCurrentLineCharacterIndex()
		{
			if (isEmpty())
				return -1;
			return peek().getCharIndex();
		}
	
		/**
		 * @return the amount of readers in the stack.
		 */
		public final int size()
		{
			return innerStack.size();
		}
	
		/**
		 * @return if there are no readers in the stack.
		 */
		public final boolean isEmpty()
		{
			return innerStack.isEmpty();
		}
	
		/**
		 * Reads the next character.
		 * @return the character read, or null if end of current stream.
		 * @throws IOException if a line cannot be read by the topmost Reader.
		 */
		public final int readChar() throws IOException
		{
			return peek().readChar();
		}
	
		/**
		 * Stream encapsulation of a single named Reader.
		 * Also holds current line, character number, line number.
		 */
		public class Stream implements AutoCloseable
		{
			/** Name of the stream. */
			private String streamName;
			/** The buffered reader. */
			private BufferedReader reader;
			/** Current line number. */
			private int line;
			/** Current character index. */
			private int charIndex;
	
			private int[] charStack;
			private int charStackPosition;
			
			/**
			 * Creates a new stream.
			 * @param name the stream name.
			 * @param in the reader used.
			 */
			private Stream(String name, Reader in)
			{
				this.streamName = name;
				this.reader = new BufferedReader(in);
				this.line = 1;
				this.charIndex = 0;
				this.charStackPosition = -1;
				this.charStack = new int[16];
			}
			
			private void pushChar(int breakChar)
			{
				charStack[++charStackPosition] = breakChar;
			}

			private String getStreamName()
			{
				return streamName;
			}
			
			private int getLine()
			{
				return line;
			}
			
			private int getCharIndex()
			{
				return charIndex;
			}
			
			private boolean isNewlineChar(int c)
			{
				return c == '\r' || c == '\n';
			}
			
			/**
			 * Reads the next char from the stream.
			 * Eats all manner of newline combos into '\n'.
			 * @return the line read.
			 * @throws IOException if a line cannot be read.
			 */
			private int readChar() throws IOException
			{
				int c;
				if (charStackPosition >= 0)
				{
					c = charStack[charStackPosition--];
					return c;
				}
				else
				{
					c = reader.read();
					boolean newline = false;
					while (isNewlineChar(c))
					{
						if (c == (int)'\n')
							line++;

						newline = true;
						c = reader.read();
						if (!isNewlineChar(c))
							pushChar(c);
					}
					if (newline)
						c = (int)NEWLINE;
				}
				
				if (c == (int)NEWLINE)
				{
					charIndex = 0;
				}
				return c;
			}
			
			@Override
			public void close() throws IOException
			{
				reader.close();
			}
		}
	}

	/**
	 * This is a info kernel that tells a {@link Lexer} how to interpret certain characters and identifiers.
	 * @author Matthew Tropiano
	 */
	public static class Kernel
	{
		/** Reserved token type: End of lexer. */
		public static final int TYPE_END_OF_LEXER = 			-1;
		/** Reserved token type: End of stream. */
		public static final int TYPE_END_OF_STREAM =			-2;
		/** Reserved token type: Unknown token. */
		public static final int TYPE_UNKNOWN = 					-3;
		/** Reserved token type: Number. */
		public static final int TYPE_NUMBER = 					-4;
		/** Reserved token type: Space. */
		public static final int TYPE_SPACE = 					-5;
		/** Reserved token type: Tab. */
		public static final int TYPE_TAB = 						-6;
		/** Reserved token type: New line character. */
		public static final int TYPE_NEWLINE =					-7;
		/** Reserved token type: Identifier. */
		public static final int TYPE_IDENTIFIER = 				-8;
		/** Reserved token type: Illegal token. */
		public static final int TYPE_ILLEGAL = 					-9;
		/** Reserved token type: Comment. */
		public static final int TYPE_COMMENT = 					-10;
		/** Reserved token type: Line Comment. */
		public static final int TYPE_LINE_COMMENT = 			-11;
		/** Reserved token type: String. */
		public static final int TYPE_STRING = 					-12;
		
		/** Reserved token type: Raw String (never returned). */
		private static final int TYPE_RAWSTRING = 				-16;
		/** Reserved token type: Delimiter (never returned). */
		private static final int TYPE_DELIMITER = 				-17;
		/** Reserved token type: Point state (never returned). */
		private static final int TYPE_POINT = 					-18;
		/** Reserved token type: Floating point state (never returned). */
		private static final int TYPE_FLOAT = 					-19;
		/** Reserved token type: Delimiter Comment (never returned). */
		private static final int TYPE_ENDING_COMMENT = 			-20;
		/** Reserved token type: hexadecimal integer (never returned). */
		private static final int TYPE_HEX_INTEGER0 = 			-21;
		/** Reserved token type: hexadecimal integer (never returned). */
		private static final int TYPE_HEX_INTEGER1 = 			-22;
		/** Reserved token type: hexadecimal integer (never returned). */
		private static final int TYPE_HEX_INTEGER = 			-23;
		/** Reserved token type: Exponent state (never returned). */
		private static final int TYPE_EXPONENT = 				-24;
		/** Reserved token type: Exponent power state (never returned). */
		private static final int TYPE_EXPONENT_POWER = 			-25;
	
		/**
		 * Table of single-character (or beginning character of) significant
		 * delimiters. Delimiters immediately break the current token if encountered.
		 */
		private Set<Character> delimStartTable;
		/** 
		 * Table of significant delimiters.
		 */
		private Map<String, Integer> delimTable;
		/** 
		 * Table of comment-starting delimiters.
		 */
		private Map<String, String> commentTable;
		/** 
		 * Table of line-comment delimiters.
		 */
		private Set<String> commentLineTable;
		/**
		 * Table of identifiers mapped to token ids (used for reserved keywords).
		 */
		private Map<String, Integer> keywordTable;
		/**
		 * Table of (case-insensitive) identifiers mapped to token ids (used for reserved keywords).
		 */
		private Map<String, Integer> caseInsensitiveKeywordTable;
		/** 
		 * Table of string delimiters, or delimiters that cue the start of
		 * a string of characters. Each is paired with an ending delimiter.
		 * These take precedence over regular delimiters on scanning.
		 */
		private Map<Character, Character> stringDelimTable;
		/** 
		 * Table of string delimiters, or delimiters that cue the start of
		 * a multi-line string of characters that turns newlines into spaces. 
		 * Each is paired with an ending delimiter.
		 * These take precedence over regular delimiters on scanning.
		 */
		private Map<Character, Character> rawStringDelimTable;
	
		/** Will this lexer add spaces as tokens? */
		private boolean emitSpaces;
		/** Will this lexer add tabs as tokens? */
		private boolean emitTabs;
		/** Will this lexer add newlines as tokens? */
		private boolean emitNewlines;
		/** Will this lexer add stream breaks as tokens? */
		private boolean emitStreamBreak;
		/** Will this lexer add comments as tokens? */
		private boolean emitComments;
		/** Decimal separator. */
		private char decimalSeparator;
		
		/**
		 * Creates a new, blank LexerKernel with default settings.
		 */
		public Kernel()
		{
			decimalSeparator = DecimalFormatSymbols.getInstance().getDecimalSeparator();
			delimStartTable = new HashSet<Character>();
			delimTable = new HashMap<String, Integer>();
			commentTable = new HashMap<String, String>(2);
			commentLineTable = new TreeSet<String>();
			stringDelimTable = new HashMap<Character, Character>();
			rawStringDelimTable = new HashMap<Character, Character>();
			keywordTable = new HashMap<String, Integer>();
			caseInsensitiveKeywordTable = new HashMap<String, Integer>();
			
			emitSpaces = false;
			emitTabs = false;
			emitNewlines = false;
			emitStreamBreak = false;
			emitComments = false;
		}
	
		/**
		 * Adds a delimiter to this lexer.
		 * @param delimiter		the delimiter lexeme.
		 * @param type			the type id.
		 * @throws IllegalArgumentException if type is &lt; 0 or delimiter is null or empty.
		 */
		public void addDelimiter(String delimiter, int type)
		{
			typeCheck(type);
			keyCheck(delimiter);
			if (!delimStartTable.contains(delimiter.charAt(0)))
				delimStartTable.add(delimiter.charAt(0));
			delimTable.put(delimiter, type);
		}
	
		private void typeCheck(int type)
		{
			if (type < 0)
				throw new IllegalArgumentException("Type cannot be less than 0.");
		}
		
		private void keyCheck(String name)
		{
			if (isStringEmpty(name))
				throw new IllegalArgumentException("String cannot be null nor empty.");
		}
	
		/**
		 * Adds a string delimiter to this lexer along with its ending character.
		 * @param delimiterStart	the starting delimiter.
		 * @param delimiterEnd		the ending delimiter.
		 */
		public void addStringDelimiter(char delimiterStart, char delimiterEnd)
		{
			stringDelimTable.put(delimiterStart, delimiterEnd);
		}
	
		/**
		 * Adds a raw string delimiter to this lexer along with its ending character.
		 * @param delimiterStart	the starting delimiter.
		 * @param delimiterEnd		the ending delimiter.
		 */
		public void addRawStringDelimiter(char delimiterStart, char delimiterEnd)
		{
			rawStringDelimTable.put(delimiterStart, delimiterEnd);
		}

		/**
		 * Adds a comment-starting (and ending) delimiter to this lexer.
		 * @param delimiterStart the comment starting delimiter. 
		 * @param delimiterEnd the comment ending sequence.
		 */
		public void addCommentDelimiter(String delimiterStart, String delimiterEnd)
		{
			keyCheck(delimiterStart);
			commentTable.put(delimiterStart, delimiterEnd);
			if (!delimStartTable.contains(delimiterStart.charAt(0)))
				delimStartTable.add(delimiterStart.charAt(0));
		}
	
		/**
		 * Adds a line comment delimiter to this lexer.
		 * @param delimiter		the delimiter lexeme.
		 * @throws IllegalArgumentException if type is &lt; 0 or delimiter is null or empty.
		 */
		public void addCommentLineDelimiter(String delimiter)
		{
			keyCheck(delimiter);
			commentLineTable.add(delimiter);
			if (!delimStartTable.contains(delimiter.charAt(0)))
				delimStartTable.add(delimiter.charAt(0));
		}
	
		/**
		 * Adds a keyword to the Lexer, case-sensitive. When this identifier is read in,
		 * its token type is specified type. 
		 * @param keyword	the keyword identifier.
		 * @param type		the type id.
		 */
		public void addKeyword(String keyword, int type)
		{
			typeCheck(type);
			keyCheck(keyword);
			keywordTable.put(keyword, type);
		}
	
		/**
		 * Adds a keyword to the Lexer, case-insensitive. 
		 * When this identifier is read in, its token type is specified type. 
		 * @param keyword	the keyword identifier.
		 * @param type		the type id.
		 */
		public void addCaseInsensitiveKeyword(String keyword, int type)
		{
			typeCheck(type);
			keyCheck(keyword);
			caseInsensitiveKeywordTable.put(keyword.toLowerCase(), type);
		}
	
		/** 
		 * Checks if this lexer emits space tokens.
		 * @return true if so, false if not. 
		 */
		public boolean willEmitSpaces()
		{
			return emitSpaces;
		}
	
		/** 
		 * Sets if this lexer emits space tokens? 
		 * @param includeSpaces true if so, false if not.
		 */
		public void setEmitSpaces(boolean includeSpaces)
		{
			this.emitSpaces = includeSpaces;
		}
	
		/** 
		 * Checks if this lexer emits tab tokens.
		 * @return true if so, false if not. 
		 */
		public boolean willEmitTabs()
		{
			return emitTabs;
		}
	
		/** 
		 * Sets if this lexer emits tab tokens.
		 * @param includeTabs true if so, false if not. 
		 */
		public void setEmitTabs(boolean includeTabs)
		{
			this.emitTabs = includeTabs;
		}
	
		/** 
		 * Checks if this lexer emits newline tokens.
		 * @return true if so, false if not.
		 */
		public boolean willEmitNewlines()
		{
			return emitNewlines;
		}
	
		/** 
		 * Sets if this lexer emits newline tokens.
		 * @param includeNewlines true if so, false if not. 
		 */
		public void setEmitNewlines(boolean includeNewlines)
		{
			this.emitNewlines = includeNewlines;
		}
	
		/** 
		 * Checks if this lexer emits stream break tokens.
		 * @return true if so, false if not.
		 */
		public boolean willEmitStreamBreak()
		{
			return emitStreamBreak;
		}
	
		/** 
		 * Sets if this lexer emits stream break tokens.
		 * @param emitStreamBreak true if so, false if not. 
		 */
		public void setEmitStreamBreak(boolean emitStreamBreak)
		{
			this.emitStreamBreak = emitStreamBreak;
		}
	
		/** 
		 * Checks if this lexer emits comment tokens.
		 * @return true if so, false if not.
		 */
		public boolean willEmitComments()
		{
			return emitComments;
		}
	
		/** 
		 * Sets if this lexer emits comment tokens.
		 * @param emitComments true if so, false if not. 
		 */
		public void setEmitComments(boolean emitComments)
		{
			this.emitComments = emitComments;
		}
	
		/**
		 * Sets the current decimal separator character.
		 * By default, this is the current locale's decimal separator character.
		 * @param c the character to set.
		 */
		public void setDecimalSeparator(char c)
		{
			decimalSeparator = c;
		}
	
		/**
		 * Gets the current decimal separator character.
		 * By default, this is the current locale's decimal separator character.
		 * @return the separator character.
		 */
		public char getDecimalSeparator()
		{
			return decimalSeparator;
		}
	
		private Set<Character> getDelimStartTable()
		{
			return delimStartTable;
		}
	
		private Map<String, Integer> getDelimTable()
		{
			return delimTable;
		}
	
		private Map<String, String> getCommentTable()
		{
			return commentTable;
		}
	
		private Set<String> getCommentLineSet()
		{
			return commentLineTable;
		}
	
		private Map<String, Integer> getKeywordTable()
		{
			return keywordTable;
		}
	
		private Map<String, Integer> getCaseInsensitiveKeywordTable()
		{
			return caseInsensitiveKeywordTable;
		}
	
		private Map<Character, Character> getStringDelimTable()
		{
			return stringDelimTable;
		}
	
		private Map<Character, Character> getRawStringDelimTable()
		{
			return rawStringDelimTable;
		}
	
	}

	/**
	 * Abstract parser class.
	 * This class aids in the creation of top-down (AKA recursive-descent) parsers.
	 */
	public static abstract class Parser
	{
		/** Lexer used by this parser. */
		private Lexer lexer;
		/** Current lexer token. */
		private Lexer.Token currentToken;
		
		/**
		 * Constructs the parser and binds a Lexer to it.
		 * @param lexer the lexer that this reads from.
		 */
		protected Parser(Lexer lexer)
		{
			this.lexer = lexer;
		}
		
		/**
		 * Gets the {@link Lexer} that this Parser uses.
		 * @return the underlying Lexer.
		 */
		public Lexer getLexer()
		{
			return lexer;
		}
		
		/**
		 * Gets the token read from the last {@link #nextToken()} call.
		 * @return the current token.
		 */
		protected Lexer.Token currentToken()
		{
			return currentToken;
		}

		/**
		 * @return the current token's lexeme, or null if no current token.
		 * @see Lexer.Token#getLexeme()
		 */
		protected String currentLexeme()
		{
			return currentToken != null ? currentToken.getLexeme() : null;
		}

		/**
		 * Matches the current token. If matched, this returns true and advances
		 * to the next token. Else, this returns false.
		 * @param tokenType the type to match.
		 * @return true if matched, false if not.
		 */
		protected boolean matchType(int tokenType)
		{
			if (currentType(tokenType))
			{
				nextToken();
				return true;
			}
			return false;
		}

		/**
		 * Attempts to match the type of the current token. If matched, this returns true.
		 * This DOES NOT ADVANCE to the next token.
		 * @param tokenTypes the list of types.
		 * @return true if one was matched, false if not.
		 */
		protected boolean currentType(int ... tokenTypes)
		{
			if (currentToken != null)
			{
				for (int i : tokenTypes)
					if (currentToken.getType() == i)
						return true;
			}
			return false;
		}

		/**
		 * Reads and sets the current token to the next token.
		 * If the current token is null, it is the end of the Lexer's stream.
		 * @throws Parser.Exception if the next token can't be read. 
		 */
		protected void nextToken()
		{
			try {
				currentToken = lexer.nextToken();
			} catch (IOException e) {
				throw new Parser.Exception(e.getMessage(), e);
			}
		}
		
		/**
		 * Changes the current token's type to a different type.
		 * Only useful after {@link #nextToken()} but before it is parsed by your parser.
		 * @param type the new type id.
		 */
		protected void changeTokenType(int type)
		{
			currentToken.type = type;
		}
		
		/**
		 * Returns a stock error line for when an error/warning or whatever occurs during parse.
		 * For convenience only - not called by any of the current methods.
		 * @param message the message to append.
		 * @return the error message.
		 */
		public String getTokenInfoLine(String message)
		{
			String error;
			if (currentToken == null)
			{
				error = "(STREAM END) "+message;
			}
			else
			{
				StringBuilder sb = new StringBuilder();
				if (lexer.getCurrentStreamName() != null)
					sb.append("(").append(lexer.getCurrentStreamName()).append(") ");
				sb.append("Line ").append(currentToken.getLineNumber()).append(", ");
				sb.append("Token ").append("\"").append(currentToken.getLexeme()).append("\": ");
				sb.append(message);
				error = sb.toString();
			}
			return error;
		}
		
		/**
		 * Thrown when a Parser has a problem.
		 */
		public static class Exception extends RuntimeException
		{
			private static final long serialVersionUID = 6712240658282073090L;

			public Exception()
			{
				super("Bad type requested.");
			}

			public Exception(String s)
			{
				super(s);
			}
			
			public Exception(String s, Throwable t)
			{
				super(s, t);
			}
		}
	}

	/**
	 * Lexer token object.
	 */
	public static class Token
	{
		private String streamName;
		private String lexeme;
		private int lineNumber;
		private int charIndex;
		private int type;
		
		public Token(String streamName, int type, String lexeme, int lineNumber, int charIndex)
		{
			this.streamName = streamName;
			this.lexeme = lexeme;
			this.lineNumber = lineNumber;
			this.charIndex = charIndex;
			this.type = type;
		}

		/**
		 * @return the name of the stream that this token came from.
		 */
		public String getStreamName()
		{
			return streamName;
		}

		/**
		 * @return the line number within the stream that this token appeared.
		 */
		public int getLineNumber()
		{
			return lineNumber;
		}

		/**
		 * @return the character index that this token starts on.
		 */
		public int getCharIndex()
		{
			return charIndex;
		}
		
		/** 
		 * @return this token's lexeme. 
		 */
		public String getLexeme()
		{
			return lexeme;
		}

		/**
		 * Sets this token's lexeme.
		 * @param lexeme the new lexeme.
		 */
		public void setLexeme(String lexeme)
		{
			this.lexeme = lexeme;
		}
		
		/** @return this token's type. */
		public int getType()
		{
			return type;
		}
		
		/** 
		 * Sets this token's type.
		 * @param type a type corresponding to a type in the lexer or lexer kernel.
		 */
		public void setType(int type)
		{
			this.type = type;
		}
		
		@Override
		public String toString()
		{
			StringBuilder sb = new StringBuilder();
			if (streamName != null)
				sb.append("(").append(streamName).append(") ");
			
			sb.append("Id: ").append(type);
			if (lineNumber >= 0);
				sb.append(", Line: ").append(lineNumber);
			
			if (lexeme != null)
				sb.append(", Lexeme: \"").append(lexeme).append('"');
			return sb.toString();
		}
		
	}
	
}

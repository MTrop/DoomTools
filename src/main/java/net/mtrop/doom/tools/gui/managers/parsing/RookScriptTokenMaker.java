/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.managers.parsing;

import java.util.Arrays;
import java.util.Set;

import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

import net.mtrop.doom.tools.struct.util.ObjectUtils;


/**
 * Token maker for DoomMake/WadScript/RookScript.
 * @author Matthew Tropiano 
 */
public class RookScriptTokenMaker extends CommonTokenMaker
{
	private static final char[] SEPARATORS = createAlphabet("[]{}()");
	private static final char[] DELIMITER_BREAK = createAlphabet("[]{}(),.!~+-*/%&|^=<>\"`:;?");
	private static final char[] HEXDIGITS = createAlphabet("0123456789abcdefABCDEF");

	private static final Set<String> OPERATORS = ObjectUtils.createSet(
		"+", "-", "!", "~", "*", "/", "%", "&", "&&", "|", "||", "^", "=", "==", "!=", "===", "!==",
		"<", ">", "<<", ">>", ">>>", "?", ":", "?:", "??", "+=", "-=", "*=", "/=", "%=", "&=", "|=",
		">>=", "<<=", ">>>=", ";", "::", "->", ",", "."
	);

	@Override
	protected void addKeywords(TokenMap targetMap)
	{
		// Rookscript Common.
		targetMap.put("if",       RESERVED_WORD);
		targetMap.put("else",     RESERVED_WORD);
		targetMap.put("return",   RESERVED_WORD);
		targetMap.put("while",    RESERVED_WORD);
		targetMap.put("for",      RESERVED_WORD);
		targetMap.put("each",     RESERVED_WORD);
		targetMap.put("entry",    RESERVED_WORD);
		targetMap.put("function", RESERVED_WORD);
		targetMap.put("break",    RESERVED_WORD);
		targetMap.put("continue", RESERVED_WORD);
		targetMap.put("check",    RESERVED_WORD);
		
		targetMap.put("null",     LITERAL_NUMBER_DECIMAL_INT);
		targetMap.put("true",     LITERAL_BOOLEAN);
		targetMap.put("false",    LITERAL_BOOLEAN);
		targetMap.put("infinity", LITERAL_NUMBER_FLOAT);
		targetMap.put("nan",      LITERAL_NUMBER_FLOAT);
	}
	
	protected static boolean isSeparator(char c)
	{
		return Arrays.binarySearch(SEPARATORS, c) >= 0;
	}
	
	protected static boolean isHexDigit(char c)
	{
		return Arrays.binarySearch(HEXDIGITS, c) >= 0;
	}
	
	protected static boolean isDelimiterBreak(char c)
	{
		return Arrays.binarySearch(DELIMITER_BREAK, c) >= 0;
	}
	
	protected static boolean isOperator(String token)
	{
		return OPERATORS.contains(token);
	}
	
	@Override
	public Token getTokenList(final Segment segment, final int initialTokenType, final int documentStartOffset)
	{
		resetTokenList();
		
		char[] segmentChars = segment.array;
		int segmentOffset = segment.offset;
		int segmentCount = segment.count;
		int segmentEnd = segmentOffset + segmentCount;
		int currentType = initialTokenType;

		int newDocumentStartOffset = documentStartOffset - segmentOffset;
		int currentTokenStart = segmentOffset;
		
		for (int i = segmentOffset; i < segmentEnd; i++)
		{
			char c = segmentChars[i];
			switch (currentType)
			{
				case NULL: // starting type
				{
					currentTokenStart = i;
					
					if (c == '#')
						currentType = PREPROCESSOR;
					else if (Character.isWhitespace(c))
						currentType = WHITESPACE;
					else if (c == '"')
						currentType = LITERAL_STRING_DOUBLE_QUOTE;
					else if (c == '`')
						currentType = LITERAL_BACKQUOTE;
					else if (c == '/')
						currentType = TYPE_MAYBE_COMMENT;
					else if (isSeparator(c))
						currentType = SEPARATOR;
					else if (isDelimiterBreak(c))
						currentType = TYPE_MAYBE_DELIMITER;
					else if (c == '0')
						currentType = TYPE_MAYBE_NUMERIC;
					else if (Character.isDigit(c))
						currentType = LITERAL_NUMBER_DECIMAL_INT;
					else
						currentType = IDENTIFIER;
				}
				break;
				
				case WHITESPACE:
				{
					if (Character.isWhitespace(c))
					{
						// Do nothing.
					}
					else
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
				}
				break;

				case PREPROCESSOR:
				{
					if (c == '\\')
					{
						currentType = TYPE_PREPROCESSOR_MULTILINE;
					}
					// Keep state.
				}
				break;

				case TYPE_PREPROCESSOR_MULTILINE:
				{
					// tolerate a little whitespace after a backslash... 
					if (Character.isWhitespace(c))
					{
						// ignore.
					}
					// ...but if we hit non-whitespace, back to single-line!
					else
					{
						currentType = PREPROCESSOR;
					}
				}
				break;

				case TYPE_MAYBE_NUMERIC: // saw just one number, and it was "0"
				{
					if (c == 'x' || c == 'X')
					{
						currentType = LITERAL_NUMBER_HEXADECIMAL;
					}
					else if (c == '.')
					{
						currentType = LITERAL_NUMBER_FLOAT;
					}
					else if (Character.isWhitespace(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, LITERAL_NUMBER_DECIMAL_INT, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
					else if (isDelimiterBreak(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, LITERAL_NUMBER_DECIMAL_INT, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
					else if (Character.isDigit(c))
					{
						currentType = LITERAL_NUMBER_DECIMAL_INT;
					}
					else
					{
						currentType = ERROR_IDENTIFIER;
					}
				}
				break;

				case SEPARATOR:
				{
					addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
					currentType = NULL;
					i--; // wait one char.
				}
				break;
				
				case TYPE_MAYBE_DELIMITER:
				{
					if (Character.isWhitespace(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
					else if (Character.isDigit(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
					else if (Character.isLetter(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
					else if (isSeparator(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
					else if (c == '"' || c == '`')
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
					else if (c == '/')
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
					else if (isDelimiterBreak(c))
					{
						// Do nothing.
					}
					else
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
				}
				break;

				case LITERAL_NUMBER_DECIMAL_INT:
				{
					if (c == 'e' || c == 'E')
					{
						currentType = TYPE_FLOAT_EXPONENT_SIGN;
					}
					else if (c == '.')
					{
						currentType = LITERAL_NUMBER_FLOAT;
					}
					else if (Character.isLetter(c))
					{
						currentType = ERROR_NUMBER_FORMAT;
					}
					else if (Character.isWhitespace(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
					else if (isDelimiterBreak(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
					else if (Character.isDigit(c))
					{
						// Do nothing and continue.
					}
					else
					{
						currentType = ERROR_NUMBER_FORMAT;
					}
				}
				break;
				
				case LITERAL_NUMBER_HEXADECIMAL:
				{
					if (isHexDigit(c))
					{
						// Do nothing and continue.
					}
					else if (Character.isLetter(c))
					{
						currentType = ERROR_NUMBER_FORMAT;
					}
					else if (Character.isWhitespace(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
					else if (isDelimiterBreak(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
					else
					{
						currentType = ERROR_NUMBER_FORMAT;
					}
				}
				break;

				case LITERAL_NUMBER_FLOAT:
				{
					if (c == 'e' || c == 'E')
					{
						currentType = TYPE_FLOAT_EXPONENT_SIGN;
					}
					else if (Character.isDigit(c))
					{
						// Do nothing and continue.
					}
					else if (Character.isLetter(c))
					{
						currentType = ERROR_NUMBER_FORMAT;
					}
					else if (Character.isWhitespace(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
					else if (isDelimiterBreak(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
					else
					{
						currentType = ERROR_NUMBER_FORMAT;
					}
				}
				break;

				case TYPE_FLOAT_EXPONENT_SIGN:
				{
					if (c == '+' || c == '-')
					{
						currentType = TYPE_FLOAT_EXPONENT_SIGN_0;
					}
					else if (Character.isDigit(c))
					{
						currentType = TYPE_FLOAT_EXPONENT;
					}
					else if (Character.isWhitespace(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
					else if (isDelimiterBreak(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
					else
					{
						currentType = ERROR_NUMBER_FORMAT;
					}
				}
				break;
				
				case TYPE_FLOAT_EXPONENT_SIGN_0:
				{
					if (Character.isDigit(c))
					{
						currentType = TYPE_FLOAT_EXPONENT;
					}
					else if (Character.isWhitespace(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
					else if (isDelimiterBreak(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
					else
					{
						currentType = ERROR_NUMBER_FORMAT;
					}
				}
				break;

				case TYPE_FLOAT_EXPONENT:
				{
					if (Character.isDigit(c))
					{
						// Do nothing and continue.
					}
					else if (Character.isLetter(c))
					{
						currentType = ERROR_NUMBER_FORMAT;
					}
					else if (Character.isWhitespace(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
					else if (isDelimiterBreak(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
					else
					{
						currentType = ERROR_NUMBER_FORMAT;
					}
				}
				break;

				case LITERAL_STRING_DOUBLE_QUOTE:
				{
					if (c == '\\')
					{
						currentType = TYPE_STRING_ESCAPE;
					}
					else if (c == '"')
					{
						addToken(segmentChars, currentTokenStart, i, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
					}
					// Do nothing and continue.
				}
				break;

				case TYPE_STRING_ESCAPE:
				{
					if (c == 'x' || c == 'X')
					{
						currentType = TYPE_STRING_ESCAPE_HEX;
					}
					else if (c == 'u' || c == 'U')
					{
						currentType = TYPE_STRING_ESCAPE_UNICODE;
					}
					else
					{
						currentType = LITERAL_STRING_DOUBLE_QUOTE;
					}
				}
				break;

				case TYPE_STRING_ESCAPE_HEX:
				{
					if (isHexDigit(c))
						currentType = TYPE_STRING_ESCAPE_HEX_0;
					else
						currentType = ERROR_STRING_DOUBLE;
				}
				break;

				case TYPE_STRING_ESCAPE_HEX_0:
				{
					if (isHexDigit(c))
						currentType = LITERAL_STRING_DOUBLE_QUOTE;
					else
						currentType = ERROR_STRING_DOUBLE;
				}
				break;

				case TYPE_STRING_ESCAPE_UNICODE:
				{
					if (isHexDigit(c))
						currentType = TYPE_STRING_ESCAPE_UNICODE_0;
					else
						currentType = ERROR_STRING_DOUBLE;
				}
				break;

				case TYPE_STRING_ESCAPE_UNICODE_0:
				{
					if (isHexDigit(c))
						currentType = TYPE_STRING_ESCAPE_UNICODE_1;
					else
						currentType = ERROR_STRING_DOUBLE;
				}
				break;

				case TYPE_STRING_ESCAPE_UNICODE_1:
				{
					if (isHexDigit(c))
						currentType = TYPE_STRING_ESCAPE_UNICODE_2;
					else
						currentType = ERROR_STRING_DOUBLE;
				}
				break;

				case TYPE_STRING_ESCAPE_UNICODE_2:
				{
					if (isHexDigit(c))
						currentType = LITERAL_STRING_DOUBLE_QUOTE;
					else
						currentType = ERROR_STRING_DOUBLE;
				}
				break;

				case LITERAL_BACKQUOTE:
				{
					if (c == '`')
					{
						addToken(segmentChars, currentTokenStart, i, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
					}
					// Do nothing and continue.
				}
				break;

				case TYPE_MAYBE_COMMENT:
				{
					if (c == '*')
					{
						currentType = TYPE_MAYBE_COMMENT_DOCUMENTATION;
					}
					else if (c == '/')
					{
						currentType = COMMENT_EOL;
					}
					else
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
				}
				break;
				
				case COMMENT_EOL:
				{
					// Eat characters.
				}
				break;
				
				case TYPE_MAYBE_COMMENT_DOCUMENTATION:
				{
					if (c == '*')
						currentType = TYPE_MAYBE_COMMENT_DOCUMENTATION_END;
					else
						currentType = COMMENT_MULTILINE;
				}
				break;
				
				case TYPE_MAYBE_COMMENT_DOCUMENTATION_END:
				{
					if (c == '/')
					{
						addToken(segmentChars, currentTokenStart, i, COMMENT_DOCUMENTATION, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
					}
					else if (c == '*')
					{
						// Do nothing.
					}
					else
					{
						currentType = COMMENT_DOCUMENTATION;
					}
				}
				break;
				
				case COMMENT_MULTILINE:
				{
					if (c == '*')
					{
						currentType = TYPE_MAYBE_COMMENT_MULTILINE_END;
					}
					// Eat characters.
				}
				break;
				
				case TYPE_MAYBE_COMMENT_MULTILINE_END:
				{
					if (c == '/')
					{
						addToken(segmentChars, currentTokenStart, i, COMMENT_MULTILINE, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
					}
					else if (c == '*')
					{
						// Do nothing.
					}
					else
					{
						currentType = COMMENT_MULTILINE;
					}
				}
				break;
				
				case COMMENT_DOCUMENTATION:
				{
					if (c == '*')
					{
						currentType = TYPE_MAYBE_COMMENT_DOCUMENTATION_END;
					}
					else
					{
						// Eat characters.
					}
				}
				break;
				
				case IDENTIFIER:
				{
					if (Character.isWhitespace(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
					else if (isDelimiterBreak(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
					else
					{
						// Do nothing and continue.
					}
				}
				break;
				
				case ERROR_NUMBER_FORMAT:
				{
					if (Character.isWhitespace(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false);
						currentType = NULL;
						i--; // wait one char.
					}
					else if (isDelimiterBreak(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false);
						currentType = NULL;
						i--; // wait one char.
					}
					// Eat character.
				}
				break;
				
				case ERROR_STRING_DOUBLE:
				{
					if (c == '"')
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false);
						currentType = NULL;
						i--; // wait one char.
					}
					else if (c == '\\')
					{
						currentType = ERROR_TYPE_STRING_ESCAPE;
					}
					// Eat character.
				}
				break;
				
				case ERROR_TYPE_STRING_ESCAPE:
				{
					currentType = ERROR_STRING_DOUBLE;
				}
				break;
				
				case ERROR_IDENTIFIER:
				{
					if (Character.isWhitespace(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false);
						currentType = NULL;
						i--; // wait one char.
					}
					else if (isDelimiterBreak(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false);
						currentType = NULL;
						i--; // wait one char.
					}
					// Eat character.
				}
				break;
			}
		}
		
		// Figure out terminals.
		switch (currentType)
		{
			case NULL: // Just terminate.
				addNullToken();
				break;
				
			case TYPE_PREPROCESSOR_MULTILINE:
				addToken(segmentChars, currentTokenStart, segmentEnd - 1, PREPROCESSOR, newDocumentStartOffset + currentTokenStart, false);
				// start PREPROCESSOR next line
				break;
			
			case LITERAL_BACKQUOTE:
				addToken(segmentChars, currentTokenStart, segmentEnd - 1, LITERAL_BACKQUOTE, newDocumentStartOffset + currentTokenStart, false);
				// start LITERAL_BACKQUOTE next line
				break;
			
			case COMMENT_DOCUMENTATION:
				addToken(segmentChars, currentTokenStart, segmentEnd - 1, COMMENT_DOCUMENTATION, newDocumentStartOffset + currentTokenStart, false);
				// start COMMENT_DOCUMENTATION next line
				break;
			
			case COMMENT_MULTILINE:
				addToken(segmentChars, currentTokenStart, segmentEnd - 1, COMMENT_MULTILINE, newDocumentStartOffset + currentTokenStart, false);
				// start COMMENT_MULTILINE next line
				break;
			
			case TYPE_MAYBE_COMMENT_DOCUMENTATION: // just "/*"
			{
				addToken(segmentChars, currentTokenStart, segmentEnd - 1, COMMENT_MULTILINE, newDocumentStartOffset + currentTokenStart, false);
				// start COMMENT_MULTILINE next line
				break;
			}
			
			case TYPE_MAYBE_COMMENT_DOCUMENTATION_END:
			{
				addToken(segmentChars, currentTokenStart, segmentEnd - 1, COMMENT_DOCUMENTATION, newDocumentStartOffset + currentTokenStart, false);
				// start COMMENT_DOCUMENTATION next line
				break;
			}
			
			case TYPE_MAYBE_COMMENT_MULTILINE_END:
			{
				addToken(segmentChars, currentTokenStart, segmentEnd - 1, COMMENT_MULTILINE, newDocumentStartOffset + currentTokenStart, false);
				// start COMMENT_MULTILINE next line
				break;
			}
			
			case TYPE_STRING_ESCAPE:
			case ERROR_TYPE_STRING_ESCAPE:
			case TYPE_STRING_ESCAPE_HEX:
			case TYPE_STRING_ESCAPE_HEX_0:
			case TYPE_STRING_ESCAPE_UNICODE:
			case TYPE_STRING_ESCAPE_UNICODE_0:
			case TYPE_STRING_ESCAPE_UNICODE_1:
			case TYPE_STRING_ESCAPE_UNICODE_2:
			case LITERAL_STRING_DOUBLE_QUOTE:
				addToken(segmentChars, currentTokenStart, segmentEnd - 1, ERROR_STRING_DOUBLE, newDocumentStartOffset + currentTokenStart, false);
				addNullToken();
				break;
			
			default:
				addToken(segmentChars, currentTokenStart, segmentEnd - 1, currentType, newDocumentStartOffset + currentTokenStart, false);
				addNullToken();
				break;
		}
		
		return firstToken;
	}

	@Override
	public void addToken(char[] segmentChars, int start, int end, int tokenType, int documentStartOffset, boolean hyperLink) 
	{
		switch (tokenType)
		{
			case IDENTIFIER:
			{
				int value;
				if ((value = wordsToHighlight.get(segmentChars, start, end)) != -1) 
					tokenType = value;
			}
			break;
			
			case TYPE_MAYBE_DELIMITER:
			{
				String str = new String(segmentChars, start, end + 1 - start);
				if (isOperator(str))
					tokenType = OPERATOR;
				else
					tokenType = SEPARATOR;
			}
			break;
			
			case TYPE_MAYBE_NUMERIC: // just 0
			{
				tokenType = LITERAL_NUMBER_DECIMAL_INT;
			}
			break;

			case TYPE_FLOAT_EXPONENT:
			{
				tokenType = LITERAL_NUMBER_FLOAT;
			}
			break;
			
			case TYPE_MAYBE_COMMENT: // just "/"
			{
				tokenType = OPERATOR;
			}
			break;
			
			case TYPE_FLOAT_EXPONENT_SIGN:  // incomplete float with exponent signifier
			case TYPE_FLOAT_EXPONENT_SIGN_0:  // incomplete float with sign char
			{
				tokenType = ERROR_NUMBER_FORMAT;
			}
			break;
			
		}
		
		if (!hyperLink && isHyperlinkableType(tokenType))
			addPossibleHyperlinkTokens(segmentChars, start, end, tokenType, documentStartOffset);
		else
			super.addToken(segmentChars, start, end, tokenType, documentStartOffset, hyperLink);
	}
	
	
}


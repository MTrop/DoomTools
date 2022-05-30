package net.mtrop.doom.tools.gui.managers.parsing;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;

import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

import net.mtrop.doom.tools.struct.util.ObjectUtils;


/**
 * Token maker for DECOHack.
 * @author Matthew Tropiano 
 */
public class DecoHackTokenMaker extends CommonTokenMaker
{
	private static final Function<String, char[]> ALPHABET = (str) -> {
		char[] chars = str.toCharArray();
		Arrays.sort(chars);
		return chars;
	};
	
	private static final char[] SEPARATORS = ALPHABET.apply("{}()");
	private static final char[] DELIMITER_BREAK = ALPHABET.apply("{}(),.+-|:\"`/");
	private static final char[] HEXDIGITS = ALPHABET.apply("0123456789abcdefABCDEF");

	private static final Set<String> OPERATORS = ObjectUtils.createSet("+", "-", "|");
	
	private static final int TYPE_START =                           Token.DEFAULT_NUM_TOKEN_TYPES;
	private static final int TYPE_MAYBE_NUMERIC =                   TYPE_START + 0;
	private static final int TYPE_MAYBE_DELIMITER =                 TYPE_START + 1;
	private static final int TYPE_PREPROCESSOR_MULTILINE =          TYPE_START + 2;
	private static final int TYPE_MAYBE_COMMENT =                   TYPE_START + 3;
	private static final int TYPE_MAYBE_COMMENT_MULTILINE_END =     TYPE_START + 4;
	private static final int TYPE_MAYBE_COMMENT_DOCUMENTATION_END = TYPE_START + 5;
	private static final int TYPE_MAYBE_COMMENT_DOCUMENTATION =     TYPE_START + 6;
	private static final int TYPE_STRING_ESCAPE =                   TYPE_START + 7;
	private static final int ERROR_TYPE_STRING_ESCAPE =             TYPE_START + 8;
	private static final int TYPE_STRING_ESCAPE_HEX =               TYPE_START + 9;
	private static final int TYPE_STRING_ESCAPE_HEX_0 =             TYPE_START + 10;


	@Override
	protected void addKeywords(TokenMap targetMap)
	{
		targetMap.put("using",      Token.RESERVED_WORD);

		targetMap.put("misc",       Token.RESERVED_WORD);
		targetMap.put("pars",       Token.RESERVED_WORD);
		targetMap.put("state",      Token.RESERVED_WORD);
		targetMap.put("sound",      Token.RESERVED_WORD);
		targetMap.put("strings",    Token.RESERVED_WORD);
		targetMap.put("weapon",     Token.RESERVED_WORD);
		targetMap.put("thing",      Token.RESERVED_WORD);

		targetMap.put("custom",     Token.RESERVED_WORD);
		targetMap.put("auto",       Token.RESERVED_WORD);
		targetMap.put("fill",       Token.RESERVED_WORD);
		targetMap.put("alias",      Token.RESERVED_WORD);

		targetMap.put("clear",      Token.RESERVED_WORD);
		targetMap.put("properties", Token.RESERVED_WORD);
		targetMap.put("sounds",     Token.RESERVED_WORD);
		targetMap.put("states",     Token.RESERVED_WORD);

		targetMap.put("goto",       Token.RESERVED_WORD);
		targetMap.put("loop",       Token.RESERVED_WORD);
		targetMap.put("wait",       Token.RESERVED_WORD);
		targetMap.put("stop",       Token.RESERVED_WORD);

		targetMap.put("free",       Token.RESERVED_WORD);
		targetMap.put("protect",    Token.RESERVED_WORD);
		targetMap.put("unprotect",  Token.RESERVED_WORD);

		targetMap.put("each",       Token.RESERVED_WORD);
		targetMap.put("in",         Token.RESERVED_WORD);
		targetMap.put("to",         Token.RESERVED_WORD);
		targetMap.put("from",       Token.RESERVED_WORD);
		targetMap.put("with",       Token.RESERVED_WORD);
		targetMap.put("swap",       Token.RESERVED_WORD);

		targetMap.put("null",       Token.LITERAL_NUMBER_DECIMAL_INT);
		targetMap.put("true",       Token.LITERAL_BOOLEAN);
		targetMap.put("false",      Token.LITERAL_BOOLEAN);
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
				case Token.NULL: // starting type
				{
					currentTokenStart = i;
					
					if (c == '#')
						currentType = Token.PREPROCESSOR;
					else if (Character.isWhitespace(c))
						currentType = Token.WHITESPACE;
					else if (c == '"')
						currentType = Token.LITERAL_STRING_DOUBLE_QUOTE;
					else if (c == '`')
						currentType = Token.LITERAL_BACKQUOTE;
					else if (c == '/')
						currentType = TYPE_MAYBE_COMMENT;
					else if (isSeparator(c))
						currentType = Token.SEPARATOR;
					else if (isDelimiterBreak(c))
						currentType = TYPE_MAYBE_DELIMITER;
					else if (c == '0')
						currentType = TYPE_MAYBE_NUMERIC;
					else if (Character.isDigit(c))
						currentType = Token.LITERAL_NUMBER_DECIMAL_INT;
					else
						currentType = Token.IDENTIFIER;
				}
				break;
				
				case Token.WHITESPACE:
				{
					if (Character.isWhitespace(c))
					{
						// Do nothing.
					}
					else
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
				}
				break;

				case Token.PREPROCESSOR:
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
						currentType = Token.PREPROCESSOR;
					}
				}
				break;

				case TYPE_MAYBE_NUMERIC: // saw just one number, and it was "0"
				{
					if (c == 'x' || c == 'X')
					{
						currentType = Token.LITERAL_NUMBER_HEXADECIMAL;
					}
					else if (c == '.')
					{
						currentType = Token.LITERAL_NUMBER_FLOAT;
					}
					else if (Character.isWhitespace(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else if (isDelimiterBreak(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, Token.LITERAL_NUMBER_DECIMAL_INT, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else if (Character.isDigit(c))
					{
						currentType = Token.LITERAL_NUMBER_DECIMAL_INT;
					}
					else
					{
						currentType = Token.ERROR_IDENTIFIER;
					}
				}
				break;

				case Token.SEPARATOR:
				{
					addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
					currentType = Token.NULL;
					i--; // wait one char.
				}
				break;
				
				case TYPE_MAYBE_DELIMITER:
				{
					if (Character.isWhitespace(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else if (Character.isDigit(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else if (Character.isLetter(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else if (isSeparator(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else if (isDelimiterBreak(c))
					{
						// Do nothing.
					}
					else
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
				}
				break;

				case Token.LITERAL_NUMBER_DECIMAL_INT:
				{
					if (c == '.')
					{
						currentType = Token.LITERAL_NUMBER_FLOAT;
					}
					else if (Character.isLetter(c))
					{
						currentType = Token.ERROR_NUMBER_FORMAT;
					}
					else if (Character.isWhitespace(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else if (isDelimiterBreak(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else if (Character.isDigit(c))
					{
						// Do nothing and continue.
					}
					else
					{
						currentType = Token.ERROR_NUMBER_FORMAT;
					}
				}
				break;
				
				case Token.LITERAL_NUMBER_HEXADECIMAL:
				{
					if (isHexDigit(c))
					{
						// Do nothing and continue.
					}
					else if (Character.isLetter(c))
					{
						currentType = Token.ERROR_NUMBER_FORMAT;
					}
					else if (Character.isWhitespace(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else if (isDelimiterBreak(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else
					{
						currentType = Token.ERROR_NUMBER_FORMAT;
					}
				}
				break;

				case Token.LITERAL_NUMBER_FLOAT:
				{
					if (Character.isDigit(c))
					{
						// Do nothing and continue.
					}
					else if (Character.isLetter(c))
					{
						currentType = Token.ERROR_NUMBER_FORMAT;
					}
					else if (Character.isWhitespace(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else if (isDelimiterBreak(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else
					{
						currentType = Token.ERROR_NUMBER_FORMAT;
					}
				}
				break;

				case Token.LITERAL_STRING_DOUBLE_QUOTE:
				{
					if (c == '\\')
					{
						currentType = TYPE_STRING_ESCAPE;
					}
					else if (c == '"')
					{
						addToken(segmentChars, currentTokenStart, i, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
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
					else
					{
						currentType = Token.LITERAL_STRING_DOUBLE_QUOTE;
					}
				}
				break;

				case TYPE_STRING_ESCAPE_HEX:
				{
					if (isHexDigit(c))
						currentType = TYPE_STRING_ESCAPE_HEX_0;
					else
						currentType = Token.ERROR_STRING_DOUBLE;
				}
				break;

				case TYPE_STRING_ESCAPE_HEX_0:
				{
					if (isHexDigit(c))
						currentType = Token.LITERAL_STRING_DOUBLE_QUOTE;
					else
						currentType = Token.ERROR_STRING_DOUBLE;
				}
				break;

				case Token.LITERAL_BACKQUOTE:
				{
					if (c == '`')
					{
						addToken(segmentChars, currentTokenStart, i, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
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
						currentType = Token.COMMENT_EOL;
					}
					else
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
				}
				break;
				
				case Token.COMMENT_EOL:
				{
					// Eat characters.
				}
				break;
				
				case TYPE_MAYBE_COMMENT_DOCUMENTATION:
				{
					if (c == '*')
						currentType = TYPE_MAYBE_COMMENT_DOCUMENTATION_END;
					else
						currentType = Token.COMMENT_MULTILINE;
				}
				break;
				
				case TYPE_MAYBE_COMMENT_DOCUMENTATION_END:
				{
					if (c == '/')
					{
						addToken(segmentChars, currentTokenStart, i, Token.COMMENT_DOCUMENTATION, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
					}
					else if (c == '*')
					{
						// Do nothing.
					}
					else
					{
						currentType = Token.COMMENT_DOCUMENTATION;
					}
				}
				break;
				
				case Token.COMMENT_MULTILINE:
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
						addToken(segmentChars, currentTokenStart, i, Token.COMMENT_MULTILINE, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
					}
					else if (c == '*')
					{
						// Do nothing.
					}
					else
					{
						currentType = Token.COMMENT_MULTILINE;
					}
				}
				break;
				
				case Token.COMMENT_DOCUMENTATION:
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
				
				case Token.IDENTIFIER:
				{
					if (Character.isWhitespace(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else if (isDelimiterBreak(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else
					{
						// Do nothing and continue.
					}
				}
				break;
				
				case Token.ERROR_NUMBER_FORMAT:
				{
					if (Character.isWhitespace(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false);
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else if (isDelimiterBreak(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false);
						currentType = Token.NULL;
						i--; // wait one char.
					}
					// Eat character.
				}
				break;
				
				case Token.ERROR_STRING_DOUBLE:
				{
					if (c == '"')
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false);
						currentType = Token.NULL;
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
					currentType = Token.ERROR_STRING_DOUBLE;
				}
				break;
				
				case Token.ERROR_IDENTIFIER:
				{
					if (Character.isWhitespace(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false);
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else if (isDelimiterBreak(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false);
						currentType = Token.NULL;
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
			case Token.NULL: // Just terminate.
				addNullToken();
				break;
				
			case TYPE_PREPROCESSOR_MULTILINE:
				addToken(segmentChars, currentTokenStart, segmentEnd - 1, Token.PREPROCESSOR, newDocumentStartOffset + currentTokenStart, false);
				// start PREPROCESSOR next line
				break;
			
			case Token.LITERAL_BACKQUOTE:
				addToken(segmentChars, currentTokenStart, segmentEnd - 1, Token.LITERAL_BACKQUOTE, newDocumentStartOffset + currentTokenStart, false);
				// start LITERAL_BACKQUOTE next line
				break;
			
			case Token.COMMENT_DOCUMENTATION:
				addToken(segmentChars, currentTokenStart, segmentEnd - 1, Token.COMMENT_DOCUMENTATION, newDocumentStartOffset + currentTokenStart, false);
				// start COMMENT_DOCUMENTATION next line
				break;
			
			case Token.COMMENT_MULTILINE:
				addToken(segmentChars, currentTokenStart, segmentEnd - 1, Token.COMMENT_MULTILINE, newDocumentStartOffset + currentTokenStart, false);
				// start COMMENT_MULTILINE next line
				break;
			
			case TYPE_MAYBE_COMMENT_DOCUMENTATION: // just "/*"
			{
				addToken(segmentChars, currentTokenStart, segmentEnd - 1, Token.COMMENT_MULTILINE, newDocumentStartOffset + currentTokenStart, false);
				// start COMMENT_MULTILINE next line
				break;
			}
			
			case TYPE_MAYBE_COMMENT_DOCUMENTATION_END:
			{
				addToken(segmentChars, currentTokenStart, segmentEnd - 1, Token.COMMENT_DOCUMENTATION, newDocumentStartOffset + currentTokenStart, false);
				// start COMMENT_DOCUMENTATION next line
				break;
			}
			
			case TYPE_MAYBE_COMMENT_MULTILINE_END:
			{
				addToken(segmentChars, currentTokenStart, segmentEnd - 1, Token.COMMENT_MULTILINE, newDocumentStartOffset + currentTokenStart, false);
				// start COMMENT_MULTILINE next line
				break;
			}
			
			case TYPE_STRING_ESCAPE:
			case ERROR_TYPE_STRING_ESCAPE:
			case TYPE_STRING_ESCAPE_HEX:
			case TYPE_STRING_ESCAPE_HEX_0:
			case Token.LITERAL_STRING_DOUBLE_QUOTE:
				addToken(segmentChars, currentTokenStart, segmentEnd - 1, Token.ERROR_STRING_DOUBLE, newDocumentStartOffset + currentTokenStart, false);
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
			case Token.IDENTIFIER:
			{
				int value;
				if ((value = wordsToHighlight.get(segmentChars, start, end)) != -1) 
					tokenType = value;
				else
				{
					String str = new String(segmentChars, start, end + 1 - start);
					if (str.length() >= 2 && str.substring(0, 2).equalsIgnoreCase("a_"))
						tokenType = Token.FUNCTION;
				}
			}
			break;
			
			case TYPE_MAYBE_DELIMITER:
			{
				String str = new String(segmentChars, start, end + 1 - start);
				if (isOperator(str))
					tokenType = Token.OPERATOR;
				else
					tokenType = Token.SEPARATOR;
			}
			break;
			
			case TYPE_MAYBE_NUMERIC: // just 0
			{
				tokenType = Token.LITERAL_NUMBER_DECIMAL_INT;
			}
			break;

			case TYPE_MAYBE_COMMENT: // just "/"
			{
				tokenType = Token.OPERATOR;
			}
			break;
			
		}
		
		if (!hyperLink && isHyperlinkableType(tokenType))
			addPossibleHyperlinkTokens(segmentChars, start, end, tokenType, documentStartOffset);
		else
			super.addToken(segmentChars, start, end, tokenType, documentStartOffset, hyperLink);
	}
	
}


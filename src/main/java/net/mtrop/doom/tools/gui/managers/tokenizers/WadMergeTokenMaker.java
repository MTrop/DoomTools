package net.mtrop.doom.tools.gui.managers.tokenizers;

import java.util.Arrays;
import java.util.function.Function;

import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

import net.mtrop.doom.tools.wadmerge.WadMergeCommand;


/**
 * Token maker for WadMerge.
 * @author Matthew Tropiano 
 */
public class WadMergeTokenMaker extends AbstractTokenMaker
{
	private static final Function<String, char[]> ALPHABET = (str) -> {
		char[] chars = str.toCharArray();
		Arrays.sort(chars);
		return chars;
	};
	
	private static final char[] HEXDIGITS = ALPHABET.apply("0123456789abcdefABCDEF");

	private static final int TYPE_START =                   Token.DEFAULT_NUM_TOKEN_TYPES;
	private static final int TYPE_STRING_ESCAPE =           TYPE_START + 0;
	private static final int ERROR_TYPE_STRING_ESCAPE =     TYPE_START + 1;
	private static final int TYPE_STRING_ESCAPE_HEX =       TYPE_START + 2;
	private static final int TYPE_STRING_ESCAPE_HEX_0 =     TYPE_START + 3;
	private static final int TYPE_STRING_ESCAPE_UNICODE =   TYPE_START + 4;
	private static final int TYPE_STRING_ESCAPE_UNICODE_0 = TYPE_START + 5;
	private static final int TYPE_STRING_ESCAPE_UNICODE_1 = TYPE_START + 6;
	private static final int TYPE_STRING_ESCAPE_UNICODE_2 = TYPE_START + 7;
	private static final int TYPE_IDENTIFIER_ARGUMENT =     TYPE_START + 8;
	private static final int TYPE_STRING_ARGUMENT =         TYPE_START + 9;

	protected static boolean isHexDigit(char c)
	{
		return Arrays.binarySearch(HEXDIGITS, c) >= 0;
	}
	
	@Override
	public TokenMap getWordsToHighlight() 
	{
		TokenMap map = new TokenMap(true);
		for (WadMergeCommand command : WadMergeCommand.values())
			map.put(command.name(), Token.RESERVED_WORD);
		return map;
	}

	@Override
	public Token getTokenList(final Segment segment, final int initialTokenType, final int startOffset)
	{
		resetTokenList();
		
		char[] chars = segment.array;
		int offset = segment.offset;
		int count = segment.count;
		int end = offset + count;
		int currentType = initialTokenType;

		int currentTokenStart = offset;
		int currentTokenDocumentStart = startOffset;

		for (int i = offset; i < end; i++)
		{
			char c = chars[i];
			switch (currentType)
			{
				case Token.NULL: // starting type
				{
					currentTokenStart = i;
					currentTokenDocumentStart = startOffset + (i - offset);
					
					if (c == '#')
						currentType = Token.COMMENT_EOL;
					else if (Character.isWhitespace(c))
						currentType = Token.WHITESPACE;
					else if (c == '"')
						currentType = Token.LITERAL_STRING_DOUBLE_QUOTE;
					else if (c == '$')
						currentType = TYPE_IDENTIFIER_ARGUMENT;
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
						addToken(chars, currentTokenStart, i - 1, currentType, currentTokenDocumentStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
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
						addToken(chars, currentTokenStart, i, currentType, currentTokenDocumentStart, false); 
						currentType = Token.NULL;
					}
					else if (c == '$')
					{
						addToken(chars, currentTokenStart, i - 1, currentType, currentTokenDocumentStart, false); 
						currentTokenStart = i;
						currentTokenDocumentStart = startOffset + (i - offset);
						currentType = TYPE_STRING_ARGUMENT;
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

				case TYPE_STRING_ESCAPE_UNICODE:
				{
					if (isHexDigit(c))
						currentType = TYPE_STRING_ESCAPE_UNICODE_0;
					else
						currentType = Token.ERROR_STRING_DOUBLE;
				}
				break;

				case TYPE_STRING_ESCAPE_UNICODE_0:
				{
					if (isHexDigit(c))
						currentType = TYPE_STRING_ESCAPE_UNICODE_1;
					else
						currentType = Token.ERROR_STRING_DOUBLE;
				}
				break;

				case TYPE_STRING_ESCAPE_UNICODE_1:
				{
					if (isHexDigit(c))
						currentType = TYPE_STRING_ESCAPE_UNICODE_2;
					else
						currentType = Token.ERROR_STRING_DOUBLE;
				}
				break;

				case TYPE_STRING_ESCAPE_UNICODE_2:
				{
					if (isHexDigit(c))
						currentType = Token.LITERAL_STRING_DOUBLE_QUOTE;
					else
						currentType = Token.ERROR_STRING_DOUBLE;
				}
				break;

				case Token.COMMENT_EOL:
				{
					// Eat characters.
				}
				break;
				
				case Token.IDENTIFIER:
				{
					if (Character.isWhitespace(c))
					{
						addToken(chars, currentTokenStart, i - 1, currentType, currentTokenDocumentStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else if (c == '$')
					{
						addToken(chars, currentTokenStart, i - 1, currentType, currentTokenDocumentStart, false); 
						currentTokenStart = i;
						currentTokenDocumentStart = startOffset + (i - offset);
						currentType = TYPE_IDENTIFIER_ARGUMENT;
					}
					else
					{
						// Do nothing and continue.
					}
				}
				break;
				
				case TYPE_IDENTIFIER_ARGUMENT:
				{
					if (Character.isWhitespace(c))
					{
						addToken(chars, currentTokenStart, i - 1, Token.VARIABLE, currentTokenDocumentStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else if (Character.isDigit(c))
					{
						// Do nothing.
					}
					else 
					{
						addToken(chars, currentTokenStart, i - 1, Token.VARIABLE, currentTokenDocumentStart, false);
						currentTokenStart = i;
						currentTokenDocumentStart = startOffset + (i - offset);
						currentType = Token.IDENTIFIER;
					}
				}
				break;
				
				case TYPE_STRING_ARGUMENT:
				{
					if (Character.isWhitespace(c))
					{
						addToken(chars, currentTokenStart, i - 1, Token.VARIABLE, currentTokenDocumentStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else if (Character.isDigit(c))
					{
						// Do nothing.
					}
					else
					{
						addToken(chars, currentTokenStart, i - 1, Token.VARIABLE, currentTokenDocumentStart, false);
						currentTokenStart = i;
						currentTokenDocumentStart = startOffset + (i - offset);
						currentType = Token.LITERAL_STRING_DOUBLE_QUOTE;
					}
				}
				break;
				
				case Token.ERROR_STRING_DOUBLE:
				{
					if (c == '"')
					{
						addToken(chars, currentTokenStart, i - 1, currentType, currentTokenDocumentStart, false);
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
						addToken(chars, currentTokenStart, i - 1, currentType, currentTokenDocumentStart, false);
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
				
			case Token.LITERAL_BACKQUOTE:
				addToken(chars, currentTokenStart, end - 1, Token.LITERAL_BACKQUOTE, currentTokenDocumentStart, false);
				// start LITERAL_BACKQUOTE next line
				break;
			
			case Token.COMMENT_DOCUMENTATION:
				addToken(chars, currentTokenStart, end - 1, Token.COMMENT_DOCUMENTATION, currentTokenDocumentStart, false);
				// start COMMENT_DOCUMENTATION next line
				break;
			
			case TYPE_STRING_ESCAPE:
			case ERROR_TYPE_STRING_ESCAPE:
			case TYPE_STRING_ESCAPE_HEX:
			case TYPE_STRING_ESCAPE_HEX_0:
			case TYPE_STRING_ESCAPE_UNICODE:
			case TYPE_STRING_ESCAPE_UNICODE_0:
			case TYPE_STRING_ESCAPE_UNICODE_1:
			case TYPE_STRING_ESCAPE_UNICODE_2:
			case Token.LITERAL_STRING_DOUBLE_QUOTE:
				addToken(chars, currentTokenStart, end - 1, Token.ERROR_STRING_DOUBLE, currentTokenDocumentStart, false);
				addNullToken();
				break;

			case TYPE_IDENTIFIER_ARGUMENT:
				addToken(chars, currentTokenStart, end - 1, Token.VARIABLE, currentTokenDocumentStart, false);
				addNullToken();
				break;

			case TYPE_STRING_ARGUMENT:
				addToken(chars, currentTokenStart, end - 1, Token.VARIABLE, currentTokenDocumentStart, false);
				addNullToken();
				break;

			default:
				addToken(chars, currentTokenStart, end - 1, currentType, currentTokenDocumentStart, false);
				addNullToken();
				break;
		}
		
		return firstToken;
	}

	@Override
	public void addToken(char[] segment, int start, int end, int tokenType, int startOffset, boolean hyperLink) 
	{
		switch (tokenType)
		{
			case Token.IDENTIFIER:
			{
				int value;
				if ((value = wordsToHighlight.get(segment, start, end)) != -1) 
					tokenType = value;
			}
			break;
			
			
		}
		
		super.addToken(segment, start, end, tokenType, startOffset, hyperLink);
	}
	
}


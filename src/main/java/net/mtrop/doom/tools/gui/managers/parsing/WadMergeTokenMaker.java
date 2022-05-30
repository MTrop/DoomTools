package net.mtrop.doom.tools.gui.managers.parsing;

import java.util.Arrays;

import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

import net.mtrop.doom.tools.wadmerge.WadMergeCommand;


/**
 * Token maker for WadMerge.
 * @author Matthew Tropiano 
 */
public class WadMergeTokenMaker extends CommonTokenMaker
{
	private static final char[] HEXDIGITS = createAlphabet("0123456789abcdefABCDEF");

	protected static boolean isHexDigit(char c)
	{
		return Arrays.binarySearch(HEXDIGITS, c) >= 0;
	}
	
	@Override
	protected void addKeywords(TokenMap targetMap) 
	{
		for (WadMergeCommand command : WadMergeCommand.values())
			targetMap.put(command.name(), Token.RESERVED_WORD);
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
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
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
						addToken(segmentChars, currentTokenStart, i, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
					}
					else if (c == '$')
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentTokenStart = i;
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
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else if (c == '$')
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentTokenStart = i;
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
						addToken(segmentChars, currentTokenStart, i - 1, Token.VARIABLE, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else if (Character.isDigit(c))
					{
						// Do nothing.
					}
					else 
					{
						addToken(segmentChars, currentTokenStart, i - 1, Token.VARIABLE, newDocumentStartOffset + currentTokenStart, false);
						currentTokenStart = i;
						currentType = Token.IDENTIFIER;
					}
				}
				break;
				
				case TYPE_STRING_ARGUMENT:
				{
					if (Character.isWhitespace(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, Token.VARIABLE, newDocumentStartOffset + currentTokenStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else if (Character.isDigit(c))
					{
						// Do nothing.
					}
					else
					{
						addToken(segmentChars, currentTokenStart, i - 1, Token.VARIABLE, newDocumentStartOffset + currentTokenStart, false);
						currentTokenStart = i;
						currentType = Token.LITERAL_STRING_DOUBLE_QUOTE;
					}
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
				addToken(segmentChars, currentTokenStart, segmentEnd - 1, Token.LITERAL_BACKQUOTE, newDocumentStartOffset + currentTokenStart, false);
				// start LITERAL_BACKQUOTE next line
				break;
			
			case Token.COMMENT_DOCUMENTATION:
				addToken(segmentChars, currentTokenStart, segmentEnd - 1, Token.COMMENT_DOCUMENTATION, newDocumentStartOffset + currentTokenStart, false);
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
				addToken(segmentChars, currentTokenStart, segmentEnd - 1, Token.ERROR_STRING_DOUBLE, newDocumentStartOffset + currentTokenStart, false);
				addNullToken();
				break;

			case TYPE_IDENTIFIER_ARGUMENT:
				addToken(segmentChars, currentTokenStart, segmentEnd - 1, Token.VARIABLE, newDocumentStartOffset + currentTokenStart, false);
				addNullToken();
				break;

			case TYPE_STRING_ARGUMENT:
				addToken(segmentChars, currentTokenStart, segmentEnd - 1, Token.VARIABLE, newDocumentStartOffset + currentTokenStart, false);
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
			}
			break;
		}
		
		if (!hyperLink && isHyperlinkableType(tokenType))
			addPossibleHyperlinkTokens(segmentChars, start, end, tokenType, documentStartOffset);
		else
			super.addToken(segmentChars, start, end, tokenType, documentStartOffset, hyperLink);
	}
	
}


/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.managers.parsing;

import java.util.Arrays;

import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;


/**
 * Token maker for DEUTex files.
 * @author Matthew Tropiano 
 */
public class DEUTEXTokenMaker extends CommonTokenMaker
{
	private static final char[] DECIMALDIGITS = createAlphabet("0123456789");
	private static final char[] PATCHENTRY = createAlphabet("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^+-_");
	private static final char[] TEXTUREENTRY = createAlphabet("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^+-_");

	protected static final int TYPE_IDENTIFIER_PATCH =   COMMON_NUM_TOKEN_TYPES;
	protected static final int TYPE_IDENTIFIER_TEXTURE = COMMON_NUM_TOKEN_TYPES + 1;
	protected static final int TYPE_PATCHLINE =          COMMON_NUM_TOKEN_TYPES + 2;

	
	protected static boolean isDecimalDigit(char c)
	{
		return Arrays.binarySearch(DECIMALDIGITS, c) >= 0;
	}
	
	protected static boolean isPatchChar(char c)
	{
		return Arrays.binarySearch(PATCHENTRY, c) >= 0;
	}
	
	protected static boolean isTextureChar(char c)
	{
		return Arrays.binarySearch(TEXTUREENTRY, c) >= 0;
	}
	
	@Override
	protected void addKeywords(TokenMap targetMap) 
	{
		// No keywords.
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

		boolean patchLine = false;
		
		for (int i = segmentOffset; i < segmentEnd; i++)
		{
			char c = segmentChars[i];
			switch (currentType)
			{
				case NULL: // starting type
				{
					currentTokenStart = i;
					
					if (c == ';')
						currentType = COMMENT_EOL;
					else if (c == '*')
					{
						currentType = TYPE_PATCHLINE;
						patchLine = true;
					}
					else if (Character.isWhitespace(c))
						currentType = WHITESPACE;
					else if (isDecimalDigit(c))
						currentType = LITERAL_NUMBER_DECIMAL_INT;
					else if (isPatchChar(c))
						currentType = patchLine ? TYPE_IDENTIFIER_PATCH : TYPE_IDENTIFIER_TEXTURE;
					else
						currentType = ERROR_IDENTIFIER;
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

				case COMMENT_EOL:
				{
					// Eat characters.
				}
				break;
				
				case TYPE_PATCHLINE:
				{
					if (Character.isWhitespace(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
					else
					{
						currentType = ERROR_IDENTIFIER;
					}
				}
				break;
				
				case LITERAL_NUMBER_DECIMAL_INT:
				{
					if (Character.isWhitespace(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false); 
						currentType = NULL;
						i--; // wait one char.
					}
					else if (isPatchChar(c))
					{
						currentType = patchLine ? TYPE_IDENTIFIER_PATCH : TYPE_IDENTIFIER_TEXTURE;
					}
					else if (!isDecimalDigit(c))
					{
						currentType = ERROR_NUMBER_FORMAT;
					}
				}
				break;
				
				case TYPE_IDENTIFIER_PATCH:
				{
					if (Character.isWhitespace(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false);
						currentType = NULL;
						i--; // wait one char.
					}
					else if (!isPatchChar(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false);
						currentType = NULL;
						i--; // wait one char.
					}
					// Eat character.
				}
				break;

				case TYPE_IDENTIFIER_TEXTURE:
				{
					if (Character.isWhitespace(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false);
						currentType = NULL;
						i--; // wait one char.
					}
					else if (!isTextureChar(c))
					{
						addToken(segmentChars, currentTokenStart, i - 1, currentType, newDocumentStartOffset + currentTokenStart, false);
						currentType = NULL;
						i--; // wait one char.
					}
					// Eat character.
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
					// Eat character.
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
			
			case TYPE_IDENTIFIER_PATCH:
			case TYPE_IDENTIFIER_TEXTURE:
			{
				tokenType = IDENTIFIER;
			}
			break;

			case TYPE_PATCHLINE:
			{
				tokenType = OPERATOR;
			}
			break;

		}
		
		if (!hyperLink && isHyperlinkableType(tokenType))
			addPossibleHyperlinkTokens(segmentChars, start, end, tokenType, documentStartOffset);
		else
			super.addToken(segmentChars, start, end, tokenType, documentStartOffset, hyperLink);
	}
	
}


/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
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
public class DefSwAniTokenMaker extends CommonTokenMaker
{
	private static final char[] DECIMALDIGITS = createAlphabet("0123456789");

	protected static boolean isDecimalDigit(char c)
	{
		return Arrays.binarySearch(DECIMALDIGITS, c) >= 0;
	}
	
	@Override
	protected void addKeywords(TokenMap targetMap) 
	{
		targetMap.put("[SWITCHES]", RESERVED_WORD);
		targetMap.put("[FLATS]", RESERVED_WORD);
		targetMap.put("[TEXTURES]", RESERVED_WORD);
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
						currentType = COMMENT_EOL;
					else if (Character.isWhitespace(c))
						currentType = WHITESPACE;
					else if (isDecimalDigit(c))
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

				case COMMENT_EOL:
				{
					// Eat characters.
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
					else if (!isDecimalDigit(c))
					{
						currentType = IDENTIFIER;
					}
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
		}
		
		if (!hyperLink && isHyperlinkableType(tokenType))
			addPossibleHyperlinkTokens(segmentChars, start, end, tokenType, documentStartOffset);
		else
			super.addToken(segmentChars, start, end, tokenType, documentStartOffset, hyperLink);
	}
	
}


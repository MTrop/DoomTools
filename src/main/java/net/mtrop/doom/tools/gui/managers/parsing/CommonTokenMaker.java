/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.managers.parsing;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.TokenMap;
import org.fife.ui.rsyntaxtextarea.TokenTypes;


/**
 * A Token Maker base with common useful functions.
 * @author Matthew Tropiano
 */
public abstract class CommonTokenMaker extends AbstractTokenMaker implements TokenTypes
{
	private static final Pattern QUOTED_PATH_PATTERN = Pattern.compile("\".+\"");
	private static final Pattern URL_PATTERN = Pattern.compile("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
	
	protected static final int TYPE_START =                           DEFAULT_NUM_TOKEN_TYPES;
	protected static final int TYPE_MAYBE_NUMERIC =                   TYPE_START + 0;
	protected static final int TYPE_MAYBE_DELIMITER =                 TYPE_START + 1;
	protected static final int TYPE_FLOAT_EXPONENT =                  TYPE_START + 2;
	protected static final int TYPE_FLOAT_EXPONENT_SIGN =             TYPE_START + 3;
	protected static final int TYPE_FLOAT_EXPONENT_SIGN_0 =           TYPE_START + 4;
	protected static final int TYPE_PREPROCESSOR_MULTILINE =          TYPE_START + 5;
	protected static final int TYPE_MAYBE_COMMENT =                   TYPE_START + 6;
	protected static final int TYPE_MAYBE_COMMENT_MULTILINE_END =     TYPE_START + 7;
	protected static final int TYPE_MAYBE_COMMENT_DOCUMENTATION_END = TYPE_START + 8;
	protected static final int TYPE_MAYBE_COMMENT_DOCUMENTATION =     TYPE_START + 9;
	protected static final int TYPE_STRING_ESCAPE =                   TYPE_START + 10;
	protected static final int ERROR_TYPE_STRING_ESCAPE =             TYPE_START + 11;
	protected static final int TYPE_STRING_ESCAPE_HEX =               TYPE_START + 12;
	protected static final int TYPE_STRING_ESCAPE_HEX_0 =             TYPE_START + 13;
	protected static final int TYPE_STRING_ESCAPE_UNICODE =           TYPE_START + 14;
	protected static final int TYPE_STRING_ESCAPE_UNICODE_0 =         TYPE_START + 15;
	protected static final int TYPE_STRING_ESCAPE_UNICODE_1 =         TYPE_START + 16;
	protected static final int TYPE_STRING_ESCAPE_UNICODE_2 =         TYPE_START + 17;
	protected static final int TYPE_IDENTIFIER_ARGUMENT =             TYPE_START + 18;
	protected static final int TYPE_STRING_ARGUMENT =                 TYPE_START + 19;
	protected static final int COMMON_NUM_TOKEN_TYPES =               TYPE_START + 20; // keep at end!

	@Override
	public TokenMap getWordsToHighlight() 
	{
		TokenMap map = new TokenMap(true);
		addKeywords(map);
		return map;
	}

	/**
	 * Adds this token maker's keywords to a case-insensitive TokenMap.
	 * @param targetMap the target map to add to.
	 */
	protected abstract void addKeywords(TokenMap targetMap);
	
	/**
	 * Creates a sorted char array from an alphabet string.
	 * @param str the input string.
	 * @return the char array.
	 */
	protected static char[] createAlphabet(String str)
	{
		char[] chars = str.toCharArray();
		Arrays.sort(chars);
		return chars;
	}
	
	/**
	 * Tests if a token type can be scanned for hyperlinks.
	 * @param tokenType the type.
	 * @return true if so, false if not.
	 */
	protected static boolean isHyperlinkableType(int tokenType)
	{
		switch (tokenType)
		{
			default:
				return false;
			case PREPROCESSOR:
			case COMMENT_EOL:
			case COMMENT_MULTILINE:
			case COMMENT_DOCUMENTATION:
			case LITERAL_STRING_DOUBLE_QUOTE:
			case LITERAL_BACKQUOTE:
				return true;
		}
	}

	/**
	 * Scans through a token segment splitting it into hyperlink tokens within it.
	 * @param segmentChars the segment characters.
	 * @param start the starting character in the characters of the existing token.
	 * @param end the ending character in the characters of the existing token (inclusive range).
	 * @param tokenType the token type that is being split.
	 * @param documentSegmentStartOffset the segment's start in the document.
	 */
	protected void addPossibleHyperlinkTokens(char[] segmentChars, int start, int end, int tokenType, int documentSegmentStartOffset)
	{
		String input = new String(segmentChars, start, end + 1 - start);
		Matcher matcher = tokenType == PREPROCESSOR ? QUOTED_PATH_PATTERN.matcher(input) : URL_PATTERN.matcher(input);
		int startOffset = tokenType == PREPROCESSOR ? 1 : 0;
		int endOffset = tokenType == PREPROCESSOR ? -1 : 0;
		
		int prevScanEnd = start;
		int currentScan = start;
		boolean matchedAtLeastOnce = false;
		int newDocumentStartOffset = documentSegmentStartOffset - start;

		while (matcher.find())
		{
			matchedAtLeastOnce = true;
			int linkMatchStart = matcher.start() + start;
			int linkMatchEnd = matcher.end() - 1 + start; // inclusive range.
			
			if (prevScanEnd < linkMatchStart)
			{
				super.addToken(segmentChars, currentScan, linkMatchStart - 1 + startOffset, tokenType, newDocumentStartOffset + currentScan, false);
				currentScan = linkMatchStart + startOffset;
			}

			super.addToken(segmentChars, linkMatchStart + startOffset, linkMatchEnd + endOffset, tokenType, newDocumentStartOffset + currentScan, true);
			currentScan = linkMatchEnd + 1 + endOffset;
			prevScanEnd = linkMatchEnd + 1 + endOffset;
		}
		
		if (matchedAtLeastOnce)
		{
			if (currentScan <= end)
				super.addToken(segmentChars, currentScan, end, tokenType, newDocumentStartOffset + currentScan, false);
		}
		else
		{
			super.addToken(segmentChars, start, end, tokenType, documentSegmentStartOffset, false);
		}
	}

}

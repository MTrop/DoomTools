package net.mtrop.doom.tools.gui.managers.tokenizers;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;

import javax.swing.text.Segment;

import org.fife.ui.rsyntaxtextarea.AbstractTokenMaker;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

import net.mtrop.doom.tools.struct.util.ObjectUtils;


/**
 * Token maker for DoomMake/WadScript/RookScript.
 * @author Matthew Tropiano 
 */
public class RookScriptTokenMaker extends AbstractTokenMaker
{
	private static final Function<String, char[]> ALPHABET = (str) -> {
		char[] chars = str.toCharArray();
		Arrays.sort(chars);
		return chars;
	};
	
	private static final char[] SEPARATORS = ALPHABET.apply("[]{}()");
	private static final char[] DELIMITER_BREAK = ALPHABET.apply("[]{}().,!~+-*/%&|^=<>\"`:;?");
	private static final char[] HEXDIGITS = ALPHABET.apply("0123456789abcdefABCDEF");

	private static final Set<String> OPERATORS = ObjectUtils.createSet(
		"+", "-", "!", "~", "*", "/", "%", "&", "&&", "|", "||", "^", "=", "==", "!=", "===", "!==",
		"<", ">", "<<", ">>", ">>>", "?", ":", "?:", "??", "+=", "-=", "*=", "/=", "%=", "&=", "|=",
		">>=", "<<=", ">>>=", ";", "::", "->"
	);
	
	private static final int TYPE_START =                  Token.DEFAULT_NUM_TOKEN_TYPES;
	private static final int TYPE_MAYBE_NUMERIC =          TYPE_START + 0;
	private static final int TYPE_MAYBE_DELIMITER =        TYPE_START + 1;
	private static final int TYPE_FLOAT_EXPONENT =         TYPE_START + 2;
	private static final int TYPE_FLOAT_EXPONENT_SIGN =    TYPE_START + 3;
	private static final int TYPE_PREPROCESSOR_MULTILINE = TYPE_START + 4;
	private static final int TYPE_MAYBE_COMMENT =          TYPE_START + 5;

	
	/**
	 * Adds the common RookScript keywords to a TokenMap.
	 * @param targetMap the target map to add to.
	 */
	protected static void addRookScriptKeywords(TokenMap targetMap)
	{
		// Rookscript Common.
		targetMap.put("if",       Token.RESERVED_WORD);
		targetMap.put("else",     Token.RESERVED_WORD);
		targetMap.put("return",   Token.RESERVED_WORD);
		targetMap.put("while",    Token.RESERVED_WORD);
		targetMap.put("for",      Token.RESERVED_WORD);
		targetMap.put("each",     Token.RESERVED_WORD);
		targetMap.put("entry",    Token.RESERVED_WORD);
		targetMap.put("function", Token.RESERVED_WORD);
		targetMap.put("break",    Token.RESERVED_WORD);
		targetMap.put("continue", Token.RESERVED_WORD);
		targetMap.put("check",    Token.RESERVED_WORD);
		
		targetMap.put("null",     Token.LITERAL_NUMBER_DECIMAL_INT);
		targetMap.put("true",     Token.LITERAL_BOOLEAN);
		targetMap.put("false",    Token.LITERAL_BOOLEAN);
		targetMap.put("infinity", Token.LITERAL_NUMBER_FLOAT);
		targetMap.put("nan",      Token.LITERAL_NUMBER_FLOAT);
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
	public TokenMap getWordsToHighlight() 
	{
		TokenMap map = new TokenMap(true);
		addRookScriptKeywords(map);
		return new TokenMap();
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
						currentType = Token.PREPROCESSOR;
					else if (Character.isWhitespace(c))
						currentType = Token.WHITESPACE;
					else if (c == '"')
						currentType = Token.LITERAL_STRING_DOUBLE_QUOTE;
					else if (c == '`')
						currentType = Token.LITERAL_BACKQUOTE;
					else if (c == '/')
						currentType = TYPE_MAYBE_COMMENT;
					else if (isDelimiterBreak(c))
					{
						currentType = TYPE_MAYBE_DELIMITER;
						i--; // wait one char.
					}
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
						addToken(chars, currentTokenStart, i - 1, currentType, currentTokenDocumentStart, false); 
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
					else if (isDelimiterBreak(c))
					{
						addToken(chars, currentTokenStart, i - 1, currentType, currentTokenDocumentStart, false); 
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

				case TYPE_MAYBE_DELIMITER:
				{
					if (Character.isWhitespace(c))
					{
						addToken(chars, currentTokenStart, i - 1, currentType, currentTokenDocumentStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else if (Character.isDigit(c))
					{
						addToken(chars, currentTokenStart, i - 1, currentType, currentTokenDocumentStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else if (Character.isLetter(c))
					{
						addToken(chars, currentTokenStart, i - 1, currentType, currentTokenDocumentStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else if (isSeparator(c))
					{
						addToken(chars, currentTokenStart, i - 1, currentType, currentTokenDocumentStart, false); 
						currentType = Token.NULL;
						i--; // wait one char.
					}
					else if (isDelimiterBreak(c))
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

				case Token.LITERAL_NUMBER_DECIMAL_INT:
				{
					// TODO: Finish this.
				}
				break;

				case Token.LITERAL_NUMBER_FLOAT:
				{
					// TODO: Finish this.
				}
				break;

				case TYPE_FLOAT_EXPONENT:
				{
					// TODO: Finish this.
				}
				break;

				case TYPE_FLOAT_EXPONENT_SIGN:
				{
					// TODO: Finish this.
				}
				break;

				case Token.LITERAL_STRING_DOUBLE_QUOTE:
				{
					// TODO: Finish this.
				}
				break;

				case Token.LITERAL_BACKQUOTE:
				{
					// TODO: Finish this.
				}
				break;

				case TYPE_MAYBE_COMMENT:
				{
					// TODO: Finish this.
				}
				break;
				
				case Token.COMMENT_EOL:
				{
					// TODO: Finish this.
				}
				break;
				
				case Token.COMMENT_MULTILINE:
				{
					// TODO: Finish this.
				}
				break;
				
				case Token.COMMENT_DOCUMENTATION:
				{
					// TODO: Finish this.
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
					else if (isDelimiterBreak(c))
					{
						addToken(chars, currentTokenStart, i - 1, currentType, currentTokenDocumentStart, false);
						currentType = Token.NULL;
						i--; // wait one char.
					}
					// Eat character.
				}
				break;
				
				// TODO: Finish this.
			}
		}
		
		// Figure out terminals.
		switch (currentType)
		{
			case Token.NULL: // Just terminate.
				addNullToken();
				break;
				
			case TYPE_PREPROCESSOR_MULTILINE:
				addToken(chars, currentTokenStart, end - 1, Token.PREPROCESSOR, currentTokenDocumentStart, false);
				// start PREPROCESSOR next line
				break;
			
			case Token.LITERAL_BACKQUOTE:
				addToken(chars, currentTokenStart, end - 1, Token.LITERAL_BACKQUOTE, currentTokenDocumentStart, false);
				// start LITERAL_BACKQUOTE next line
				break;
			
			case Token.COMMENT_DOCUMENTATION:
				addToken(chars, currentTokenStart, end - 1, Token.COMMENT_DOCUMENTATION, currentTokenDocumentStart, false);
				// start COMMENT_DOCUMENTATION next line
				break;
			
			case Token.COMMENT_MULTILINE:
				addToken(chars, currentTokenStart, end - 1, Token.COMMENT_MULTILINE, currentTokenDocumentStart, false);
				// start COMMENT_MULTILINE next line
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
			
			case TYPE_MAYBE_DELIMITER:
			{
				String str = new String(segment, start, end - start);
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
			
			case TYPE_FLOAT_EXPONENT_SIGN:  // incomplete float with exponent
			{
				tokenType = Token.ERROR_NUMBER_FORMAT;
			}
			break;
		}
		
		super.addToken(segment, start, end, tokenType, startOffset, hyperLink);
	}
	
}


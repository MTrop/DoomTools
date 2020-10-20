package net.mtrop.doom.tools.decohack.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import net.mtrop.doom.tools.decohack.DEHActionPointer;
import net.mtrop.doom.tools.decohack.DEHPatch;
import net.mtrop.doom.tools.decohack.DEHPatchBoom.EpisodeMap;
import net.mtrop.doom.tools.decohack.contexts.AbstractPatchBoomContext;
import net.mtrop.doom.tools.decohack.contexts.AbstractPatchContext;
import net.mtrop.doom.tools.decohack.contexts.AbstractPatchDoom19Context;
import net.mtrop.doom.tools.decohack.contexts.PatchBoomContext;
import net.mtrop.doom.tools.decohack.contexts.PatchDHEExtendedContext;
import net.mtrop.doom.tools.decohack.contexts.PatchDoom19Context;
import net.mtrop.doom.tools.decohack.contexts.PatchMBFContext;
import net.mtrop.doom.tools.decohack.contexts.PatchUltimateDoom19Context;
import net.mtrop.doom.tools.decohack.exception.DecoHackParseException;
import net.mtrop.doom.tools.struct.Lexer;
import net.mtrop.doom.tools.struct.PreprocessorLexer;

/**
 * The DecoHack parser.
 * @author Matthew Tropiano
 */
public final class DecoHackParser extends Lexer.Parser
{
	public static final String STREAMNAME_TEXT = "[Text String]";
	
	private static final Pattern MAPLUMP_EXMY = Pattern.compile("E[0-9]+M[0-9]+", Pattern.CASE_INSENSITIVE);
	private static final Pattern MAPLUMP_MAPXX = Pattern.compile("MAP[0-9][0-9]+", Pattern.CASE_INSENSITIVE);
	
	/**
	 * Reads a DECOHack script from a String of text.
	 * @param text the String to read from.
	 * @return an exportable patch.
	 * @throws DecoHackParseException if one or more parse errors happen.
	 * @throws IOException if the stream can't be read.
	 * @throws NullPointerException if text is null. 
	 */
	public static AbstractPatchContext<?> read(String text) throws IOException
	{
		return read(STREAMNAME_TEXT, new StringReader(text));
	}

	/**
	 * Reads a DECOHack script from a String of text.
	 * @param streamName a name to assign to the stream.
	 * @param text the String to read from.
	 * @return an exportable patch.
	 * @throws DecoHackParseException if one or more parse errors happen.
	 * @throws IOException if the stream can't be read.
	 * @throws NullPointerException if text is null. 
	 */
	public static AbstractPatchContext<?> read(String streamName, String text) throws IOException
	{
		return read(streamName, new StringReader(text));
	}

	/**
	 * Reads a DECOHack script from a starting text file.
	 * @param file the file to read from.
	 * @return an exportable patch.
	 * @throws DecoHackParseException if one or more parse errors happen.
	 * @throws IOException if the stream can't be read.
	 * @throws SecurityException if a read error happens due to OS permissioning.
	 * @throws NullPointerException if file is null. 
	 */
	public static AbstractPatchContext<?> read(File file) throws IOException
	{
		try (FileInputStream fis = new FileInputStream(file))
		{
			return read(file.getPath(), fis);
		}
	}

	/**
	 * Reads a DECOHack script.
	 * @param streamName the name of the stream.
	 * @param in the stream to read from.
	 * @return an exportable patch.
	 * @throws DecoHackParseException if one or more parse errors happen.
	 * @throws IOException if the stream can't be read.
	 * @throws SecurityException if a read error happens due to OS permissioning.
	 * @throws NullPointerException if in is null. 
	 */
	public static AbstractPatchContext<?> read(String streamName, InputStream in) throws IOException
	{
		return read(streamName, new InputStreamReader(in));
	}

	/**
	 * Reads a DECOHack script from a reader stream.
	 * @param streamName the name of the stream.
	 * @param reader the reader to read from.
	 * @return an exportable patch.
	 * @throws DecoHackParseException if one or more parse errors happen.
	 * @throws IOException if the stream can't be read.
	 * @throws SecurityException if a read error happens due to OS permissioning.
	 * @throws NullPointerException if reader is null. 
	 */
	public static AbstractPatchContext<?> read(String streamName, Reader reader) throws IOException
	{
		return (new DecoHackParser(streamName, reader)).parse();
	}

	// =======================================================================
	
	/**
	 * Parse "using" line (must be first).
	 */
	private AbstractPatchContext<?> parseUsing()
	{
		if (!matchIdentifierLexeme("using"))
		{
			addErrorMessage("Expected \"using\" clause to set the patch format.");
			return null;
		}
		
		if (matchIdentifierLexeme("doom19"))
			return new PatchDoom19Context();
		else if (matchIdentifierLexeme("udoom19"))
			return new PatchUltimateDoom19Context();
		else if (matchIdentifierLexeme("boom"))
			return new PatchBoomContext();
		else if (matchIdentifierLexeme("mbf"))
			return new PatchMBFContext();
		else if (matchIdentifierLexeme("extended"))
			return new PatchDHEExtendedContext();
		else
		{
			addErrorMessage("Expected valid patch format type (doom19, udoom19, boom, mbf, extended).");
			return null;
		}
	}

	/**
	 * Parse entries.
	 */
	private boolean parseEntry(AbstractPatchContext<?> context)
	{
		if (matchIdentifierLexeme("strings"))
		{
			if (!matchType(DecoHackKernel.TYPE_LBRACE))
			{
				addErrorMessage("Expected { after \"strings\".");
				return false;
			}
			
			if (context instanceof AbstractPatchDoom19Context)
			{
				if (!parseStringEntryList((AbstractPatchDoom19Context)context))
					return false;
			}
			else if (context instanceof AbstractPatchBoomContext)
			{
				if (!parseStringEntryList((AbstractPatchBoomContext)context))
					return false;
			}
			else
				throw new IllegalStateException("INTERNAL ERROR! - Context type on string section parse.");
			
			if (!matchType(DecoHackKernel.TYPE_RBRACE))
			{
				addErrorMessage("Expected } to close \"strings\" section.");
				return false;
			}
			
			return true;
		}
		else if (currentToken() != null)
		{
			addErrorMessage("Unknown section or command \"%s\".", currentToken().getLexeme());
			return false;
		}
		else
		{
			return true;
		}
	}

	private boolean parseStringEntryList(AbstractPatchDoom19Context context)
	{
		// TODO Auto-generated method stub
		addErrorMessage("INTERNAL - UNFINISHED!");
		return false;
	}

	private boolean parseStringEntryList(AbstractPatchBoomContext context) 
	{
		// TODO Auto-generated method stub
		addErrorMessage("INTERNAL - UNFINISHED!");
		return false;
	}

	// Matches an identifier with a specific lexeme.
	private boolean matchIdentifierLexeme(String lexeme)
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
			return false;
		if (!currentToken().getLexeme().equalsIgnoreCase(lexeme))
			return false;
		nextToken();
		return true;
	}

	// Matches an identifier or string that references a sprite name.
	// If match, advance token and return sprite index integer.
	// Else, return null.
	private Integer matchSpriteIndex(DEHPatch patch)
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER, DecoHackKernel.TYPE_STRING))
			return null;
		Integer out;
		if ((out = patch.getSpriteIndex(currentToken().getLexeme())) == null)
			return null;
		nextToken();
		return out;
	}
	
	// Matches an identifier that is a list of subframe indices.
	// If match, advance token and return true plus modified out list.
	// Else, return null.
	private boolean matchSubframes(List<Integer> outList)
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
			return false;

		List<Integer> out = new LinkedList<>();
		String lexeme = currentToken().getLexeme();
		for (int i = 0; i < lexeme.length(); i++)
		{
			char c = lexeme.charAt(i);
			if (c < 'A' || c > ']')
				throw new IllegalArgumentException("Subframe list contains an invalid character: " + c + " Expected A through ].");
			out.add(c - 'A');
		}
		return true;
	}
	
	// Matches an identifier or string that references a sound name.
	// If match, advance token and return sound index integer.
	// Else, return null.
	private Integer matchSoundIndex(DEHPatch patch)
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER, DecoHackKernel.TYPE_STRING))
			return null;
		Integer out;
		if ((out = patch.getSoundIndex(currentToken().getLexeme())) == null)
			return null;
		nextToken();
		return out;
	}
	
	// Matches an identifier or string that references a sound name.
	// If match, advance token and return sound index integer.
	// Else, return null.
	private DEHActionPointer matchActionPointer()
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
			return null;

		String lexeme = currentToken().getLexeme();
		DEHActionPointer out;
		if (lexeme.length() < 2 || !lexeme.substring(0, 2).startsWith("A_"))
			return null;
		if ((out = DEHActionPointer.getByMnemonic(lexeme.substring(2))) == null)
			return null;
		nextToken();
		return out;
	}
	
	// Matches an identifier or string that references a map lump pattern.
	// If match, advance token and return episode-map.
	// Else, return null.
	private EpisodeMap matchEpisodeMap()
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER, DecoHackKernel.TYPE_STRING))
			return null;
		
		String lexeme = currentToken().getLexeme();
		if (MAPLUMP_EXMY.matcher(lexeme).matches())
		{
			int midx = Math.max(lexeme.indexOf('m'), lexeme.indexOf('M'));
			EpisodeMap out = EpisodeMap.create(
				Integer.parseInt(lexeme.substring(1, midx)),
				Integer.parseInt(lexeme.substring(midx + 1, lexeme.length()))
			);
			nextToken();
			return out;
		}
		else if (MAPLUMP_MAPXX.matcher(lexeme).matches())
		{
			EpisodeMap out = EpisodeMap.create(
				0,
				Integer.parseInt(lexeme.substring(3, lexeme.length()))
			);
			nextToken();
			return out;
		}
		else
		{
			return null;
		}
	}
	
	// Matches a string.
	private String matchString()
	{
		if (!currentType(DecoHackKernel.TYPE_STRING))
			return null;
		String out = currentToken().getLexeme();
		nextToken();
		return out;
	}

	// Matches a positive integer.
	private Integer matchPositiveInteger()
	{
		if (!currentType(DecoHackKernel.TYPE_NUMBER))
			return null;
		
		String lexeme = currentToken().getLexeme();
		if (lexeme.startsWith("0X") || lexeme.startsWith("0x"))
		{
			long v = parseUnsignedHexLong(lexeme.substring(2));
			if (v > (long)Integer.MAX_VALUE || v < (long)Integer.MIN_VALUE)
				return null;
			nextToken();
			return (int)v;
		}
		else if (lexeme.contains("."))
		{
			return null;
		}
		else
		{
			long v = Long.parseLong(lexeme);
			if (v > (long)Integer.MAX_VALUE || v < (long)Integer.MIN_VALUE)
				return null;
			nextToken();
			return (int)v;
		}
	}

	// Matches an integer.
	private Integer matchInteger()
	{
		if (matchType(DecoHackKernel.TYPE_DASH))
		{
			Integer out;
			if ((out = matchPositiveInteger()) == null)
				return null;
			return -out;
		}
		return null;
	}

	// Matches a boolean.
	private Boolean matchBoolean()
	{
		if (matchType(DecoHackKernel.TYPE_TRUE))
			return true;
		if (matchType(DecoHackKernel.TYPE_FALSE))
			return false;
		return null;
	}

	private static final char[] HEXALPHABET = "0123456789abcdef".toCharArray();

	// parses an unsigned hex string.
	private long parseUnsignedHexLong(String hexString)
	{
		long out = 0L;
		for (int i = hexString.length() - 1, x = 0; i >= 0; i--, x++)
		{
			char c = Character.toLowerCase(hexString.charAt(i));
			long n = Arrays.binarySearch(HEXALPHABET, c);
			if (n < 0)
				throw new NumberFormatException(hexString + " could not be parsed.");
			out |= (n << (4 * x));
		}
		return out;
	}
	
	// =======================================================================

	/** List of errors. */
	private LinkedList<String> errors;

	// Return the exporter for the patch.
	private DecoHackParser(String streamName, Reader in)
	{
		super(new DecoHackLexer(streamName, in));
		this.errors = new LinkedList<>();
	}
	
	private void addErrorMessage(String message, Object... args)
	{
		errors.add(getTokenInfoLine(String.format(message, args)));
	}
	
	private String[] getErrorMessages()
	{
		String[] out = new String[errors.size()];
		errors.toArray(out);
		return out;
	}
	
	/**
	 * Starts parsing a script.
	 * @param context 
	 * @return the exporter for the script.
	 */
	public AbstractPatchContext<?> parse()
	{
		// prime first token.
		nextToken();
		
		boolean noError;
		AbstractPatchContext<?> context = null;
		
		try {
			context = parseUsing();
			// keep parsing entries.
			noError = context != null;
			while (currentToken() != null && noError)
				noError = parseEntry(context);
		} catch (DecoHackParseException e) {
			addErrorMessage(e.getMessage());
			noError = false;
		} catch (NumberFormatException e) {
			addErrorMessage(e.getMessage());
			noError = false;
		} catch (IllegalArgumentException e) {
			addErrorMessage(e.getMessage());
			noError = false;
		} catch (IllegalStateException e) {
			addErrorMessage(e.getMessage());
			noError = false;
		}
		
		if (!noError) // awkward, I know.
		{
			String[] errors = getErrorMessages();
			if (errors.length > 0)
			{
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < errors.length; i++)
				{
					sb.append(errors[i]);
					if (i < errors.length-1)
						sb.append('\n');
				}
				throw new DecoHackParseException(sb.toString());
			}
		}
		
		return context;
	}
	
	/**
	 * Lexer Kernel for DECOHack.
	 */
	private static class DecoHackKernel extends Lexer.Kernel
	{
		public static final int TYPE_COMMENT = 0;
		public static final int TYPE_LPAREN = 1;
		public static final int TYPE_RPAREN = 2;
		public static final int TYPE_COMMA = 5;
		public static final int TYPE_LBRACE = 7;
		public static final int TYPE_RBRACE = 8;
		public static final int TYPE_COLON = 10;
		public static final int TYPE_PERIOD = 11;
		public static final int TYPE_PLUS = 12;
		public static final int TYPE_DASH = 13;
		
		public static final int TYPE_TRUE = 101;
		public static final int TYPE_FALSE = 102;

		private DecoHackKernel()
		{
			setDecimalSeparator('.');

			addStringDelimiter('"', '"');
			addRawStringDelimiter('`', '`');
			
			addCommentStartDelimiter("/*", TYPE_COMMENT);
			addCommentLineDelimiter("//", TYPE_COMMENT);
			addCommentEndDelimiter("*/", TYPE_COMMENT);

			addDelimiter("(", TYPE_LPAREN);
			addDelimiter(")", TYPE_RPAREN);
			addDelimiter("{", TYPE_LBRACE);
			addDelimiter("}", TYPE_RBRACE);
			addDelimiter(",", TYPE_COMMA);
			addDelimiter(".", TYPE_PERIOD);
			addDelimiter(":", TYPE_COLON);

			addDelimiter("+", TYPE_PLUS);
			addDelimiter("-", TYPE_DASH);
			
			addCaseInsensitiveKeyword("true", TYPE_TRUE);
			addCaseInsensitiveKeyword("false", TYPE_FALSE);
		}
	}
	
	/**
	 * The lexer for a script reader context.
	 */
	private static class DecoHackLexer extends PreprocessorLexer
	{
		private static final Kernel KERNEL = new DecoHackKernel();

		private DecoHackLexer(String streamName, Reader in)
		{
			super(KERNEL, streamName, in);
			setIncluder(DEFAULT_INCLUDER);
		}
	}

}

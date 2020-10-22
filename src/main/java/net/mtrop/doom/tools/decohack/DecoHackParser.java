package net.mtrop.doom.tools.decohack;

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
import java.util.Map;
import java.util.regex.Pattern;

import net.mtrop.doom.tools.decohack.contexts.AbstractPatchBoomContext;
import net.mtrop.doom.tools.decohack.contexts.AbstractPatchContext;
import net.mtrop.doom.tools.decohack.contexts.AbstractPatchDoom19Context;
import net.mtrop.doom.tools.decohack.contexts.PatchBoomContext;
import net.mtrop.doom.tools.decohack.contexts.PatchDHEExtendedContext;
import net.mtrop.doom.tools.decohack.contexts.PatchDoom19Context;
import net.mtrop.doom.tools.decohack.contexts.PatchMBFContext;
import net.mtrop.doom.tools.decohack.contexts.PatchUltimateDoom19Context;
import net.mtrop.doom.tools.decohack.data.DEHActionPointer;
import net.mtrop.doom.tools.decohack.data.DEHAmmo;
import net.mtrop.doom.tools.decohack.data.DEHSound;
import net.mtrop.doom.tools.decohack.data.DEHState;
import net.mtrop.doom.tools.decohack.exception.DecoHackParseException;
import net.mtrop.doom.tools.decohack.patches.DEHPatch;
import net.mtrop.doom.tools.decohack.patches.DEHPatchBoom.EpisodeMap;
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
		if (!matchIdentifierLexemeIgnoreCase("using"))
		{
			addErrorMessage("Expected \"using\" clause to set the patch format.");
			return null;
		}
		
		if (matchIdentifierLexemeIgnoreCase("doom19"))
			return new PatchDoom19Context();
		else if (matchIdentifierLexemeIgnoreCase("udoom19"))
			return new PatchUltimateDoom19Context();
		else if (matchIdentifierLexemeIgnoreCase("boom"))
			return new PatchBoomContext();
		else if (matchIdentifierLexemeIgnoreCase("mbf"))
			return new PatchMBFContext();
		else if (matchIdentifierLexemeIgnoreCase("extended"))
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
		if (matchIdentifierLexemeIgnoreCase("strings"))
		{
			return parseStringBlock(context);
		}
		else if (matchIdentifierLexemeIgnoreCase("ammo"))
		{
			return parseAmmoBlock(context);
		}
		else if (matchIdentifierLexemeIgnoreCase("sound"))
		{
			return parseSoundBlock(context);
		}
		else if (matchIdentifierLexemeIgnoreCase("state"))
		{
			return parseStateBlock(context);
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

	// Parses a string block.
	private boolean parseStringBlock(AbstractPatchContext<?> context)
	{
		if (!matchType(DecoHackKernel.TYPE_LBRACE))
		{
			addErrorMessage("Expected '{' to start \"strings\" section.");
			return false;
		}
		
		if (context instanceof AbstractPatchDoom19Context)
		{
			if (!parseStringEntryList((AbstractPatchDoom19Context)context))
				return false;
			if (!matchType(DecoHackKernel.TYPE_RBRACE))
			{
				addErrorMessage("Expected '}' to close \"strings\" section, or string index to start string replacement entry.");
				return false;
			}
			return true;
		}
		else if (context instanceof AbstractPatchBoomContext)
		{
			if (!parseStringEntryList((AbstractPatchBoomContext)context))
				return false;
			if (!matchType(DecoHackKernel.TYPE_RBRACE))
			{
				addErrorMessage("Expected '}' to close \"strings\" section, or string key name to start string replacement entry.");
				return false;
			}
			return true;
		}
		else
		{
			throw new IllegalStateException("INTERNAL ERROR! - Context type on string section parse.");
		}
	}
	
	// Parses a string block (Doom 1.9 entries).
	private boolean parseStringEntryList(AbstractPatchDoom19Context context)
	{
		Integer stringIndex;
		while ((stringIndex = matchPositiveInteger()) != null)
		{
			String replacementString;
			if ((replacementString = matchString()) != null)
			{
				context.setString(stringIndex, replacementString);
			}
			else
			{
				addErrorMessage("Expected string after string index.");
				return false;
			}
		}
		return true;
	}

	// Parses a string block (Boom mnemonic entries).
	private boolean parseStringEntryList(AbstractPatchBoomContext context) 
	{
		String stringKey;
		while ((stringKey = matchIdentifier()) != null)
		{
			String replacementString;
			if ((replacementString = matchString()) != null)
			{
				context.setString(stringKey, replacementString);
			}
			else
			{
				addErrorMessage("Expected string after string key name.");
				return false;
			}
		}
		return true;
	}
	
	// Parses an ammo block.
	private boolean parseAmmoBlock(AbstractPatchContext<?> context)
	{
		Integer ammoIndex;
		if ((ammoIndex = matchPositiveInteger()) == null)
		{
			if ((ammoIndex = matchAmmoType()) == null)
			{
				addErrorMessage("Expected ammo type: an integer from 0 to %d or 'bullets', 'shells', 'cells', or 'rockets'.", context.getAmmoCount() - 1);
				return false;
			}
		}
		
		if (ammoIndex >= context.getAmmoCount())
		{
			addErrorMessage("Expected ammo type: an integer from 0 to %d or 'bullets', 'shells', 'cells', or 'rockets'.", context.getAmmoCount() - 1);
			return false;
		}
		
		DEHAmmo ammo = context.getAmmo(ammoIndex);
		
		String optionalName;
		if ((optionalName = matchString()) != null)
			ammo.setName(optionalName);
		
		if (!matchType(DecoHackKernel.TYPE_LBRACE))
		{
			addErrorMessage("Expected '{' after \"ammo\" header.");
			return false;
		}
		
		while (currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			if (matchIdentifierLexemeIgnoreCase("max"))
			{
				Integer value;
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"max\".");
					return false;
				}
				ammo.setMax(value);
			}
			else if (matchIdentifierLexemeIgnoreCase("pickup"))
			{
				Integer value;
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"pickup\".");
					return false;
				}
				ammo.setPickup(value);
			}
			else
			{
				addErrorMessage("Expected \"max\" or \"pickup\".");
				return false;
			}
		}
		
		if (!matchType(DecoHackKernel.TYPE_RBRACE))
		{
			addErrorMessage("Expected '}' after \"ammo\" section.");
			return false;
		}
		
		return true;
	}

	// Parses an sound block.
	private boolean parseSoundBlock(AbstractPatchContext<?> context)
	{
		Integer soundIndex;
		if ((soundIndex = matchSoundIndex(context)) == null)
		{
			addErrorMessage("Expected sound index or sound name after \"sound\".");
			return false;
		}

		DEHSound sound = context.getSound(soundIndex);
		
		if (!matchType(DecoHackKernel.TYPE_LBRACE))
		{
			addErrorMessage("Expected '{' after \"sound\" header.");
			return false;
		}

		while (currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			if (matchIdentifierLexemeIgnoreCase("priority"))
			{
				Integer value;
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"priority\".");
					return false;
				}
				sound.setPriority(value);
			}
			else if (matchIdentifierLexemeIgnoreCase("singular"))
			{
				Boolean value;
				if ((value = matchBoolean()) == null)
				{
					addErrorMessage("Expected boolean after \"singular\".");
					return false;
				}
				sound.setSingular(value);
			}
			else
			{
				addErrorMessage("Expected \"priority\" or \"singular\".");
				return false;
			}
		}
		
		if (!matchType(DecoHackKernel.TYPE_RBRACE))
		{
			addErrorMessage("Expected '}' after \"sound\" section.");
			return false;
		}
		
		return true;
	}
	
	// Parses a state block.
	private boolean parseStateBlock(AbstractPatchContext<?> context)
	{
		Integer index;
		// if single state...
		if ((index = matchPositiveInteger()) != null)
		{
			if (index >= context.getStateCount())
			{
				addErrorMessage("Invalid state index: %d. Max is %d.", index, context.getStateCount() - 1);
				return false;
			}
			
			if (!matchType(DecoHackKernel.TYPE_LBRACE))
			{
				addErrorMessage("Expected '{' after \"state\" header.");
				return false;
			}

			if (!parseState(context, true, index))
				return false;

			if (!matchType(DecoHackKernel.TYPE_RBRACE))
			{
				addErrorMessage("Expected '}' after \"state\" definition.");
				return false;
			}

			return true;
		}
		// if fill state...
		else if (matchIdentifierLexemeIgnoreCase("fill"))
		{
			if ((index = matchPositiveInteger()) != null)
			{
				// TODO: Finish this.
			}
			
			return true;
		}
		else
		{
			addErrorMessage("Expected state index or \"fill\" keyword after \"state\".");
			return false;
		}
		
	}
	
	// Parses a single state.
	private boolean parseState(AbstractPatchContext<?> context, boolean singleFrame, int index)
	{
		Integer nextStateIndex = null;
		if ((nextStateIndex = parseNextStateIndex(context, null, null, index)) != null)
			return true;
		
		if (currentIsSpriteIndex(context))
		{
			DEHState state = context.getState(index);
			
			if (!parseStateLine(state, context, singleFrame, index))
				return false;

			// Try to parse next state clause.
			nextStateIndex = parseNextStateIndex(context, null, null, index);

			if (nextStateIndex != null)
				state.setNextStateIndex(nextStateIndex);

			return true;
		}
		else
		{
			addErrorMessage("Expected valid sprite name or next state clause (goto, stop, wait).");
			return false;				
		}
	}

	// Parse a single state and if true is returned, the input state is altered.
	// TODO: Maybe parse into a "state" object?
	private boolean parseStateLine(DEHState state, AbstractPatchContext<?> context, boolean singleFrame, int index) 
	{
		Integer spriteIndex = null;
		LinkedList<Integer> frameList = new LinkedList<>();
		Integer duration = null;
		Boolean bright = null;
		DEHActionPointer action = null;
		
		if ((spriteIndex = matchSpriteIndex(context)) == null)
		{
			addErrorMessage("Expected valid sprite name.");
			return false;				
		}
		
		if (!matchFrameIndices(frameList))
		{
			addErrorMessage("Expected valid frame characters after sprite name.");
			return false;				
		}
		
		if (singleFrame && frameList.size() > 1)
		{
			addErrorMessage("Expected valid frame characters after sprite name.");
			return false;				
		}
		
		Integer frameIndex = frameList.pollFirst();
		
		if ((duration = matchInteger()) == null)
		{
			addErrorMessage("Expected valid state duration after frame.");
			return false;				
		}

		bright = matchBrightFlag();
		
		action = matchActionPointer();
		
		Integer pointerIndex = context.getStateActionPointerIndex(index);
		if (pointerIndex == null && action != null || pointerIndex != null && action == null)
		{
			if (action != null)
				addErrorMessage("Action function specified for state without a function!");
			else
				addErrorMessage("Action function not specified for state with a function!");
			return false;				
		}
		
		// Boom special case.
		if (context instanceof AbstractPatchBoomContext && pointerIndex != null && action == null)
			action = DEHActionPointer.NULL;
		
		// Set action pointer.
		if (pointerIndex != null)
			context.setActionPointer(index, action);
		
		// Set state stuff.
		state
			.setSpriteIndex(spriteIndex)
			.setFrameIndex(frameIndex)
			.setBright(bright)
			.setDuration(duration)
		;
		return true;
	}
	
	// Parses a next state line.
	private Integer parseNextStateIndex(AbstractPatchContext<?> context, Map<String, Integer> stateMap, Integer lastLabelledStateIndex, int currentStateIndex)
	{
		// Test for only next state clause.
		if (matchIdentifierLexemeIgnoreCase("stop"))
		{
			return -1;
		}
		else if (matchIdentifierLexemeIgnoreCase("wait"))
		{
			return currentStateIndex;
		}
		else if (matchIdentifierLexemeIgnoreCase("loop"))
		{
			if (lastLabelledStateIndex == null)
			{
				addErrorMessage("Can't use \"loop\" with no declared state labels.");
				return null;
			}
			return lastLabelledStateIndex;
		}
		else if (matchIdentifierLexemeIgnoreCase("goto"))
		{
			Integer nextFrame;
			String labelName;
			if ((labelName = matchIdentifier()) != null)
			{
				if (stateMap == null)
				{
					addErrorMessage("Name of label was unexpected after \"goto\". Only valid in thing or weapon.");
					return null;				
				}
				else if ((nextFrame = stateMap.get(labelName)) == null)
				{
					addErrorMessage("Label \"%s\" is invalid or not declared.");
					return null;				
				}
				
				// increment/decrement
				if (matchType(DecoHackKernel.TYPE_PLUS))
				{
					Integer amount;
					if ((amount = matchPositiveInteger()) == null)
					{
						addErrorMessage("Expected integer after label name in \"goto\".");
						return null;				
					}
					
					if (nextFrame + amount >= context.getStateCount())
					{
						addErrorMessage("Label \"%s\" plus %d would exceed amount of states.", labelName, amount);
						return null;				
					}
					
					return nextFrame + amount;
				}
				else if (matchType(DecoHackKernel.TYPE_DASH))
				{
					Integer amount;
					if ((amount = matchPositiveInteger()) == null)
					{
						addErrorMessage("Expected integer after label name in \"goto\".");
						return null;				
					}
					
					if (nextFrame - amount < 0)
					{
						addErrorMessage("Label \"%s\" minus %d would be less than 0.", labelName, amount);
						return null;				
					}
					
					return nextFrame - amount;
				}
				else
				{
					return nextFrame;
				}
			}
			else if ((nextFrame = matchPositiveInteger()) == null)
			{
				addErrorMessage("Expected integer after \"goto\".");
				return null;				
			}
			else if (nextFrame >= context.getStateCount())
			{
				addErrorMessage("Expected valid state index after \"goto\".");
				return null;				
			}
			else
			{
				return nextFrame;
			}
		}
		else
		{
			return null;
		}
	}
	
	// Matches an identifier.
	// If match, advance token and return lexeme.
	// Else, return null.
	private String matchIdentifier()
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
			return null;
		String out = currentToken().getLexeme();
		nextToken();
		return out;
	}

	// Matches an identifier with a specific lexeme.
	// If match, advance token and return true.
	// Else, return false.
	private boolean matchIdentifierLexemeIgnoreCase(String lexeme)
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
			return false;
		if (!currentToken().getLexeme().equalsIgnoreCase(lexeme))
			return false;
		nextToken();
		return true;
	}

	// Matches an ammo type identifier.
	private Integer matchAmmoType()
	{
		if (matchIdentifierLexemeIgnoreCase("bullets"))
			return 0;
		else if (matchIdentifierLexemeIgnoreCase("shells"))
			return 1;
		else if (matchIdentifierLexemeIgnoreCase("cells"))
			return 2;
		else if (matchIdentifierLexemeIgnoreCase("rockets"))
			return 3;
		else if (matchIdentifierLexemeIgnoreCase("infinite"))
			return 5;
		else
			return null;
	}

	// Tests for an identifier or string that references a sprite name.
	private boolean currentIsSpriteIndex(DEHPatch patch)
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER, DecoHackKernel.TYPE_STRING))
			return false;
		else
			return patch.getSpriteIndex(currentToken().getLexeme()) != null;
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
	private boolean matchFrameIndices(List<Integer> outList)
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
	
	// Matches an identifier that can be "bright".
	// If match, advance token and return true plus modified out list.
	// Else, return null.
	private boolean matchBrightFlag()
	{
		return matchIdentifierLexemeIgnoreCase("bright");
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
		if (out == DEHActionPointer.NULL)
			return null;
		nextToken();
		return out;
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

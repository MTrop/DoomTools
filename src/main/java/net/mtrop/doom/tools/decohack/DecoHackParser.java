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
import java.util.TreeMap;
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
import net.mtrop.doom.tools.decohack.data.DEHMiscellany;
import net.mtrop.doom.tools.decohack.data.DEHSound;
import net.mtrop.doom.tools.decohack.data.DEHState;
import net.mtrop.doom.tools.decohack.data.DEHThing;
import net.mtrop.doom.tools.decohack.data.DEHWeapon;
import net.mtrop.doom.tools.decohack.data.DEHWeapon.Ammo;
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

	private static final String KEYWORD_MISC = "misc";
	private static final String KEYWORD_MAX_ARMOR = "maxArmor";
	private static final String KEYWORD_MAX_HEALTH = "maxHealth";
	private static final String KEYWORD_BFG_CELLS_PER_SHOT = "bfgCellsPerShot";
	private static final String KEYWORD_IDKFA_ARMOR_CLASS = "idkfaArmorClass";
	private static final String KEYWORD_IDKFA_ARMOR = "idkfaArmor";
	private static final String KEYWORD_IDFA_ARMOR_CLASS = "idfaArmorClass";
	private static final String KEYWORD_IDFA_ARMOR = "idfaArmor";
	private static final String KEYWORD_GOD_MODE_HEALTH = "godModeHealth";
	private static final String KEYWORD_MAX_SOULSPHERE_HEALTH = "maxSoulsphereHealth";
	private static final String KEYWORD_SOULSPHERE_HEALTH = "soulsphereHealth";
	private static final String KEYWORD_BLUE_ARMOR_CLASS = "blueArmorClass";
	private static final String KEYWORD_GREEN_ARMOR_CLASS = "greenArmorClass";
	private static final String KEYWORD_INITIAL_HEALTH = "initialHealth";
	private static final String KEYWORD_INITIAL_BULLETS = "initialBullets";
	private static final String KEYWORD_MONSTER_INFIGHTING = "monsterInfighting";
	private static final String KEYWORD_PARS = "pars";
	private static final String KEYWORD_CLEAR = "clear";
	private static final String KEYWORD_STATE = "state";
	private static final String KEYWORD_FILL = "fill";
	private static final String KEYWORD_GOTO = "goto";
	private static final String KEYWORD_LOOP = "loop";
	private static final String KEYWORD_WAIT = "wait";
	private static final String KEYWORD_STOP = "stop";
	private static final String KEYWORD_FREE = "free";
	private static final String KEYWORD_PROTECT = "protect";
	private static final String KEYWORD_UNPROTECT = "unprotect";
	private static final String KEYWORD_TO = "to";
	private static final String KEYWORD_FROM = "from";
	private static final String KEYWORD_SOUND = "sound";
	private static final String KEYWORD_SOUNDS = "sounds";
	private static final String KEYWORD_SINGULAR = "singular";
	private static final String KEYWORD_PRIORITY = "priority";
	private static final String KEYWORD_AMMO = "ammo";
	private static final String KEYWORD_BULLETS = "bullets";
	private static final String KEYWORD_SHELLS = "shells";
	private static final String KEYWORD_CELLS = "cells";
	private static final String KEYWORD_ROCKETS = "rockets";
	private static final String KEYWORD_INFINITE = "infinite";
	private static final String KEYWORD_PICKUP = "pickup";
	private static final String KEYWORD_MAX = "max";
	private static final String KEYWORD_STRINGS = "strings";
	private static final String KEYWORD_STATES = "states";
	private static final String KEYWORD_WEAPON = "weapon";
	private static final String KEYWORD_AMMOTYPE = "ammotype";
	private static final String KEYWORD_WEAPONSTATE_READY = "ready";
	private static final String KEYWORD_WEAPONSTATE_SELECT = "select";
	private static final String KEYWORD_WEAPONSTATE_DESELECT = "deselect";
	private static final String KEYWORD_WEAPONSTATE_FIRE = "fire";
	private static final String KEYWORD_WEAPONSTATE_FLASH = "flash";
	private static final String KEYWORD_THING = "thing";
	private static final String KEYWORD_THINGSTATE_SPAWN = "spawn";
	private static final String KEYWORD_THINGSTATE_SEE = "see";
	private static final String KEYWORD_THINGSTATE_MELEE = "melee";
	private static final String KEYWORD_THINGSTATE_MISSILE = "missile";
	private static final String KEYWORD_THINGSTATE_PAIN = "pain";
	private static final String KEYWORD_THINGSTATE_DEATH = "death";
	private static final String KEYWORD_THINGSTATE_XDEATH = "xdeath";
	private static final String KEYWORD_THINGSTATE_RAISE = "raise";
	private static final String KEYWORD_USING = "using";
	private static final String KEYWORD_DOOM19 = "doom19";
	private static final String KEYWORD_UDOOM19 = "udoom19";
	private static final String KEYWORD_BOOM = "boom";
	private static final String KEYWORD_MBF = "mbf";
	private static final String KEYWORD_EXTENDED = "extended";

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
		if (!matchIdentifierLexemeIgnoreCase(KEYWORD_USING))
		{
			addErrorMessage("Expected \"using\" clause to set the patch format.");
			return null;
		}
		
		if (matchIdentifierLexemeIgnoreCase(KEYWORD_DOOM19))
			return new PatchDoom19Context();
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_UDOOM19))
			return new PatchUltimateDoom19Context();
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_BOOM))
			return new PatchBoomContext();
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_MBF))
			return new PatchMBFContext();
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_EXTENDED))
			return new PatchDHEExtendedContext();
		else
		{
			addErrorMessage("Expected valid patch format type (%s, %s, %s, %s, %s).", 
				KEYWORD_DOOM19, KEYWORD_UDOOM19, KEYWORD_BOOM, KEYWORD_MBF, KEYWORD_EXTENDED
			);
			return null;
		}
	}

	/**
	 * Parse entries.
	 */
	private boolean parseEntry(AbstractPatchContext<?> context)
	{
		if (matchIdentifierLexemeIgnoreCase(KEYWORD_STRINGS))
			return parseStringBlock(context);
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_AMMO))
			return parseAmmoBlock(context);
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_SOUND))
			return parseSoundBlock(context);
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_STATE))
			return parseStateBlock(context);
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_PARS))
			return parseParBlock(context);
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_THING))
			return parseThingBlock(context);
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_WEAPON))
			return parseWeaponBlock(context);
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_MISC))
			return parseMiscellaneousBlock(context);
		else if (currentToken() != null)
		{
			addErrorMessage("Unknown section or command \"%s\".", currentToken().getLexeme());
			return false;
		}
		else
			return true;
	}

	// Parses a string block.
	private boolean parseStringBlock(AbstractPatchContext<?> context)
	{
		if (!matchType(DecoHackKernel.TYPE_LBRACE))
		{
			addErrorMessage("Expected '{' to start \"%s\" section.", KEYWORD_STRINGS);
			return false;
		}
		
		if (context instanceof AbstractPatchDoom19Context)
		{
			if (!parseStringEntryList((AbstractPatchDoom19Context)context))
				return false;
			if (!matchType(DecoHackKernel.TYPE_RBRACE))
			{
				addErrorMessage("Expected '}' to close \"%s\" section, or string index to start string replacement entry.", KEYWORD_STRINGS);
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
				addErrorMessage("Expected '}' to close \"%s\" section, or string key name to start string replacement entry.", KEYWORD_STRINGS);
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
			if (stringIndex >= context.getStringCount())
			{
				addErrorMessage("String index out of range. Must be from 0 to " + (context.getStringCount() - 1));
				return false;
			}
			
			if (currentType(DecoHackKernel.TYPE_STRING))
			{
				context.setString(stringIndex, currentToken().getLexeme());
				nextToken();
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
			Ammo ammo;
			if ((ammo = matchAmmoType()) == null)
			{
				addErrorMessage("Expected ammo type: an integer from 0 to %d or 'bullets', 'shells', 'cells', or 'rockets'.", context.getAmmoCount() - 1);
				return false;
			}
			ammoIndex = ammo.ordinal();
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
			addErrorMessage("Expected '{' after \"%s\" header.", KEYWORD_AMMO);
			return false;
		}
		
		while (currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			if (matchIdentifierLexemeIgnoreCase(KEYWORD_MAX))
			{
				Integer value;
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_MAX);
					return false;
				}
				ammo.setMax(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_PICKUP))
			{
				Integer value;
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_PICKUP);
					return false;
				}
				ammo.setPickup(value);
			}
			else
			{
				addErrorMessage("Expected \"%s\" or \"%s\".", KEYWORD_MAX, KEYWORD_PICKUP);
				return false;
			}
		}
		
		if (!matchType(DecoHackKernel.TYPE_RBRACE))
		{
			addErrorMessage("Expected '}' after \"%s\" section.", KEYWORD_AMMO);
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
			addErrorMessage("Expected '{' after \"%s\" header.", KEYWORD_SOUND);
			return false;
		}

		while (currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			if (matchIdentifierLexemeIgnoreCase(KEYWORD_PRIORITY))
			{
				Integer value;
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_PRIORITY);
					return false;
				}
				sound.setPriority(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_SINGULAR))
			{
				Boolean value;
				if ((value = matchBoolean()) == null)
				{
					addErrorMessage("Expected boolean after \"%s\".", KEYWORD_SINGULAR);
					return false;
				}
				sound.setSingular(value);
			}
			else
			{
				addErrorMessage("Expected \"%s\" or \"%s\".", KEYWORD_PRIORITY, KEYWORD_SINGULAR);
				return false;
			}
		}
		
		if (!matchType(DecoHackKernel.TYPE_RBRACE))
		{
			addErrorMessage("Expected '}' after \"%s\" section.", KEYWORD_SOUND);
			return false;
		}
		
		return true;
	}

	// Parses a par block.
	private boolean parseParBlock(AbstractPatchContext<?> context)
	{
		if (!(context instanceof AbstractPatchBoomContext))
		{
			addErrorMessage("Par block not supported in non-Boom type patches.");
			return false;
		}
		
		if (!matchType(DecoHackKernel.TYPE_LBRACE))
		{
			addErrorMessage("Expected '{' after \"%s\" header.", KEYWORD_PARS);
			return false;
		}
		
		while (currentType(DecoHackKernel.TYPE_IDENTIFIER, DecoHackKernel.TYPE_STRING))
		{
			EpisodeMap map;
			if ((map = matchEpisodeMap()) == null)
			{
				addErrorMessage("Expected EXMY or MAPXX map entry.");
				return false;
			}
			
			Integer seconds;
			if ((seconds = matchPositiveInteger()) == null)
			{
				addErrorMessage("Expected seconds after map entry.");
				return false;
			}
			
			((AbstractPatchBoomContext)context).setParSeconds(map, seconds);
		}

		if (!matchType(DecoHackKernel.TYPE_RBRACE))
		{
			addErrorMessage("Expected '}' after \"%s\" section.", KEYWORD_PARS);
			return false;
		}
		
		return true;
	}
	
	// Parses a par block.
	private boolean parseMiscellaneousBlock(AbstractPatchContext<?> context)
	{
		if (!matchType(DecoHackKernel.TYPE_LBRACE))
		{
			addErrorMessage("Expected '{' after \"%s\" header.", KEYWORD_MISC);
			return false;
		}
		
		DEHMiscellany misc = context.getMiscellany();
		
		Boolean flag;
		Integer value;
		while (currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			if (matchIdentifierLexemeIgnoreCase(KEYWORD_MONSTER_INFIGHTING))
			{
				if ((flag = matchBoolean()) == null)
				{
					addErrorMessage("Expected boolean value after \"%s\".", KEYWORD_MONSTER_INFIGHTING);
					return false;
				}
				misc.setMonsterInfightingEnabled(flag);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_INITIAL_BULLETS))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_INITIAL_BULLETS);
					return false;
				}
				misc.setInitialBullets(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_INITIAL_HEALTH))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_INITIAL_HEALTH);
					return false;
				}
				misc.setInitialHealth(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_GREEN_ARMOR_CLASS))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_GREEN_ARMOR_CLASS);
					return false;
				}
				misc.setGreenArmorClass(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_BLUE_ARMOR_CLASS))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_BLUE_ARMOR_CLASS);
					return false;
				}
				misc.setBlueArmorClass(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_SOULSPHERE_HEALTH))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_SOULSPHERE_HEALTH);
					return false;
				}
				misc.setSoulsphereHealth(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_MAX_SOULSPHERE_HEALTH))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_MAX_SOULSPHERE_HEALTH);
					return false;
				}
				misc.setMaxSoulsphereHealth(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_GOD_MODE_HEALTH))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_GOD_MODE_HEALTH);
					return false;
				}
				misc.setGodModeHealth(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_IDFA_ARMOR))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_IDFA_ARMOR);
					return false;
				}
				misc.setIDFAArmor(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_IDFA_ARMOR_CLASS))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_IDFA_ARMOR_CLASS);
					return false;
				}
				misc.setIDFAArmorClass(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_IDKFA_ARMOR))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_IDKFA_ARMOR);
					return false;
				}
				misc.setIDKFAArmor(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_IDKFA_ARMOR_CLASS))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_IDKFA_ARMOR_CLASS);
					return false;
				}
				misc.setIDKFAArmorClass(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_BFG_CELLS_PER_SHOT))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_BFG_CELLS_PER_SHOT);
					return false;
				}
				misc.setBFGCellsPerShot(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_MAX_HEALTH))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_MAX_HEALTH);
					return false;
				}
				misc.setMaxHealth(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_MAX_ARMOR))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_MAX_ARMOR);
					return false;
				}
				misc.setMaxArmor(value);
			}
			else
			{
				addErrorMessage("Expected miscellaneous entry type.");
				return false;
			}
		}
		
		if (!matchType(DecoHackKernel.TYPE_RBRACE))
		{
			addErrorMessage("Expected '}' after \"%s\" section.", KEYWORD_MISC);
			return false;
		}

		return true;
	}
	
	// Parses a thing block.
	private boolean parseThingBlock(AbstractPatchContext<?> context)
	{
		Integer slot;
		if ((slot = matchPositiveInteger()) == null)
		{
			addErrorMessage("Expected positive integer after \"%s\" for the thing slot number.", KEYWORD_THING);
			return false;
		}
		
		if (slot == 0)
		{
			addErrorMessage("Invalid thing index: %d.", slot);
			return false;
		}

		if (slot >= context.getThingCount())
		{
			addErrorMessage("Invalid thing index: %d. Max is %d.", slot, context.getThingCount() - 1);
			return false;
		}
		
		// free states.
		if (matchIdentifierLexemeIgnoreCase(KEYWORD_FREE))
		{
			if (matchIdentifierLexemeIgnoreCase(KEYWORD_STATES))
			{
				context.freeThingStates(slot);
				return true;
			}

			if (!currentIsThingState())
			{
				addErrorMessage("Expected thing state name or \"%s\" after \"%s\".", KEYWORD_STATES, KEYWORD_FREE);
				return false;
			}

			DEHThing thing = context.getThing(slot);

			if (matchIdentifierLexemeIgnoreCase(KEYWORD_THINGSTATE_SPAWN))
				context.freeConnectedStates(thing.getSpawnFrameIndex());
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_THINGSTATE_SEE))
				context.freeConnectedStates(thing.getWalkFrameIndex());
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_THINGSTATE_MELEE))
				context.freeConnectedStates(thing.getMeleeFrameIndex());
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_THINGSTATE_MISSILE))
				context.freeConnectedStates(thing.getMissileFrameIndex());
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_THINGSTATE_PAIN))
				context.freeConnectedStates(thing.getPainFrameIndex());
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_THINGSTATE_DEATH))
				context.freeConnectedStates(thing.getDeathFrameIndex());
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_THINGSTATE_XDEATH))
				context.freeConnectedStates(thing.getExtremeDeathFrameIndex());
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_THINGSTATE_RAISE))
				context.freeConnectedStates(thing.getRaiseFrameIndex());
			else
			{
				addErrorMessage("INTERNAL ERROR - UNEXPECTED THINGSTATE NAME.");
				return false;
			}
			
			return true;
		}
		else
		{
			return parseThingBody(context, context.getThing(slot));
		}
	}

	// Parses a thing body.
	private boolean parseThingBody(AbstractPatchContext<?> context, DEHThing thing)
	{
		if (currentType(DecoHackKernel.TYPE_STRING))
		{
			thing.setName(matchString());
		}
		
		if (!matchType(DecoHackKernel.TYPE_LBRACE))
		{
			addErrorMessage("Expected '{' after \"%s\" declaration.", KEYWORD_THING);
			return false;
		}
		
		String name;
		Boolean flag;
		Integer value;
		while (currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			if (matchIdentifierLexemeIgnoreCase(KEYWORD_STATE))
			{
				if (!parseThingStateClause(context, thing))
					return false;
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_STATES))
			{
				if (!parseThingStateBody(context, thing))
					return false;
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_CLEAR))
			{
				if (matchIdentifierLexemeIgnoreCase(KEYWORD_STATES))
				{
					thing
						.setSpawnFrameIndex(0)
						.setWalkFrameIndex(0)
						.setPainFrameIndex(0)
						.setMeleeFrameIndex(0)
						.setMissileFrameIndex(0)
						.setDeathFrameIndex(0)
						.setExtremeDeathFrameIndex(0)
						.setRaiseFrameIndex(0)
					;
				}
				else if (matchIdentifierLexemeIgnoreCase(KEYWORD_SOUNDS))
				{
					thing
						.setSeeSoundPosition(0)
						.setAttackSoundPosition(0)
						.setPainSoundPosition(0)
						.setDeathSoundPosition(0)
						.setActiveSoundPosition(0)
					;
				}
				else
				{
					addErrorMessage("Expected '%s' or '%s', after '%s'.", KEYWORD_STATES, KEYWORD_SOUNDS, KEYWORD_CLEAR);
					return false;
				}
			}
			// TODO: Finish this (thing properties)
			else
			{
				addErrorMessage("Expected '%s', '%s', or state block start.", KEYWORD_AMMOTYPE, KEYWORD_STATE);
				return false;
			}
		}		

		if (!matchType(DecoHackKernel.TYPE_RBRACE))
		{
			addErrorMessage("Expected '}' after \"%s\" section.", KEYWORD_THING);
			return false;
		}
		
		return true;
	}
	
	// PArses a thing state clause.
	private boolean parseThingStateClause(AbstractPatchContext<?> context, DEHThing thing) 
	{
		Integer value;
		if (matchIdentifierLexemeIgnoreCase(KEYWORD_THINGSTATE_SPAWN))
		{
			if ((value = parseStateIndex(context)) != null)
				thing.setSpawnFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_THINGSTATE_SEE))
		{
			if ((value = parseStateIndex(context)) != null)
				thing.setWalkFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_THINGSTATE_MELEE))
		{
			if ((value = parseStateIndex(context)) != null)
				thing.setMeleeFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_THINGSTATE_MISSILE))
		{
			if ((value = parseStateIndex(context)) != null)
				thing.setMissileFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_THINGSTATE_PAIN))
		{
			if ((value = parseStateIndex(context)) != null)
				thing.setPainChance(value);
			else
				return false;
		}
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_THINGSTATE_DEATH))
		{
			if ((value = parseStateIndex(context)) != null)
				thing.setDeathFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_THINGSTATE_XDEATH))
		{
			if ((value = parseStateIndex(context)) != null)
				thing.setExtremeDeathFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_THINGSTATE_RAISE))
		{
			if ((value = parseStateIndex(context)) != null)
				thing.setRaiseFrameIndex(value);
			else
				return false;
		}
		else
		{
			addErrorMessage(
				"Expected a valid thing state name (%s, %s, %s, %s, %s, %s, %s, %s).",
				KEYWORD_THINGSTATE_SPAWN,
				KEYWORD_THINGSTATE_SEE,
				KEYWORD_THINGSTATE_MELEE,
				KEYWORD_THINGSTATE_MISSILE,
				KEYWORD_THINGSTATE_PAIN,
				KEYWORD_THINGSTATE_DEATH,
				KEYWORD_THINGSTATE_XDEATH,
				KEYWORD_THINGSTATE_RAISE
			);
			return false;
		}
		
		return true;
	}
	
	// Parses a thing state body.
	private boolean parseThingStateBody(AbstractPatchContext<?> context, final DEHThing thing)
	{
		if (!matchType(DecoHackKernel.TYPE_LBRACE))
		{
			addErrorMessage("Expected '{' after \"%s\" declaration.", KEYWORD_STATES);
			return false;
		}

		final Map<String, Integer> labelMap;
		setupThingStateLabels(thing, labelMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
		
		if (!parseActorStateSet(context, labelMap, (label, idx) -> {
			if (label.equalsIgnoreCase(KEYWORD_THINGSTATE_SPAWN))
				thing.setSpawnFrameIndex(idx);
			else if (label.equalsIgnoreCase(KEYWORD_THINGSTATE_SEE))
				thing.setWalkFrameIndex(idx);
			else if (label.equalsIgnoreCase(KEYWORD_THINGSTATE_MELEE))
				thing.setMeleeFrameIndex(idx);
			else if (label.equalsIgnoreCase(KEYWORD_THINGSTATE_MISSILE))
				thing.setMissileFrameIndex(idx);
			else if (label.equalsIgnoreCase(KEYWORD_THINGSTATE_PAIN))
				thing.setPainFrameIndex(idx);
			else if (label.equalsIgnoreCase(KEYWORD_THINGSTATE_DEATH))
				thing.setDeathFrameIndex(idx);
			else if (label.equalsIgnoreCase(KEYWORD_THINGSTATE_XDEATH))
				thing.setExtremeDeathFrameIndex(idx);
			else if (label.equalsIgnoreCase(KEYWORD_THINGSTATE_RAISE))
				thing.setRaiseFrameIndex(idx);
			
			labelMap.put(label, idx);
		})) return false;
		
		if (!matchType(DecoHackKernel.TYPE_RBRACE))
		{
			addErrorMessage("Expected '}' after \"%s\" section.", KEYWORD_STATES);
			return false;
		}
		
		return true;
	}
	
	// Parses a weapon block.
	private boolean parseWeaponBlock(AbstractPatchContext<?> context)
	{
		Integer slot;
		if ((slot = matchPositiveInteger()) == null)
		{
			addErrorMessage("Expected positive integer after \"%s\" for the weapon slot number.", KEYWORD_THING);
			return false;
		}
		
		if (slot >= context.getWeaponCount())
		{
			addErrorMessage("Invalid weapon index: %d. Max is %d.", slot, context.getWeaponCount() - 1);
			return false;
		}
		
		// free states.
		if (matchIdentifierLexemeIgnoreCase(KEYWORD_FREE))
		{
			if (matchIdentifierLexemeIgnoreCase(KEYWORD_STATES))
			{
				context.freeWeaponStates(slot);
				return true;
			}

			if (!currentIsWeaponState())
			{
				addErrorMessage("Expected weapon state name or \"%s\" after \"%s\".", KEYWORD_STATES, KEYWORD_FREE);
				return false;
			}

			DEHWeapon weapon = context.getWeapon(slot);

			if (matchIdentifierLexemeIgnoreCase(KEYWORD_WEAPONSTATE_SELECT))
				context.freeConnectedStates(weapon.getRaiseFrameIndex());
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_WEAPONSTATE_DESELECT))
				context.freeConnectedStates(weapon.getLowerFrameIndex());
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_WEAPONSTATE_READY))
				context.freeConnectedStates(weapon.getReadyFrameIndex());
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_WEAPONSTATE_FIRE))
				context.freeConnectedStates(weapon.getFireFrameIndex());
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_WEAPONSTATE_FLASH))
				context.freeConnectedStates(weapon.getFlashFrameIndex());
			else
			{
				addErrorMessage("INTERNAL ERROR - UNEXPECTED WEAPONSTATE NAME.");
				return false;
			}
			
			return true;
		}
		else
		{
			return parseWeaponBody(context, context.getWeapon(slot));
		}
	}

	// Parses a weapon body.
	private boolean parseWeaponBody(AbstractPatchContext<?> context, DEHWeapon weapon)
	{
		if (currentType(DecoHackKernel.TYPE_STRING))
		{
			weapon.setName(matchString());
		}
		
		if (!matchType(DecoHackKernel.TYPE_LBRACE))
		{
			addErrorMessage("Expected '{' after \"%s\" declaration.", KEYWORD_WEAPON);
			return false;
		}
		
		while (currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			if (matchIdentifierLexemeIgnoreCase(KEYWORD_STATE))
			{
				if (!parseWeaponStateClause(context, weapon))
					return false;
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_STATES))
			{
				if (!parseWeaponStateBody(context, weapon))
					return false;
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_CLEAR))
			{
				if (matchIdentifierLexemeIgnoreCase(KEYWORD_STATES))
				{
					weapon
						.setRaiseFrameIndex(0)
						.setLowerFrameIndex(0)
						.setReadyFrameIndex(0)
						.setFireFrameIndex(0)
						.setFlashFrameIndex(0)
					;
				}
				else
				{
					addErrorMessage("Expected '%s', after '%s'.", KEYWORD_STATES, KEYWORD_CLEAR);
					return false;
				}
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_AMMOTYPE))
			{
				Ammo ammo;
				Integer ammoIndex;
				if ((ammoIndex = matchPositiveInteger()) == null)
				{
					if ((ammo = matchAmmoType()) == null)
					{
						addErrorMessage("Expected ammo type: an integer from 0 to %d or 'bullets', 'shells', 'cells', 'rockets', or 'infinite'.", context.getAmmoCount() - 1);
						return false;
					}
				}
				else if (ammoIndex >= context.getAmmoCount())
				{
					addErrorMessage("Expected ammo type: an integer from 0 to %d or 'bullets', 'shells', 'cells', 'rockets', or 'infinite'.", context.getAmmoCount() - 1);
					return false;
				}
				else
				{
					ammo = Ammo.VALUES[ammoIndex];
				}

				weapon.setAmmoType(ammo);
			}
			else
			{
				addErrorMessage("Expected '%s', '%s', or state block start.", KEYWORD_AMMOTYPE, KEYWORD_STATE);
				return false;
			}
		}		

		if (!matchType(DecoHackKernel.TYPE_RBRACE))
		{
			addErrorMessage("Expected '}' after \"%s\" section.", KEYWORD_WEAPON);
			return false;
		}
		
		return true;
	}

	private boolean parseWeaponStateClause(AbstractPatchContext<?> context, DEHWeapon weapon) 
	{
		Integer value;
		if (matchIdentifierLexemeIgnoreCase(KEYWORD_WEAPONSTATE_READY))
		{
			if ((value = parseStateIndex(context)) != null)
				weapon.setReadyFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_WEAPONSTATE_SELECT))
		{
			if ((value = parseStateIndex(context)) != null)
				weapon.setRaiseFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_WEAPONSTATE_DESELECT))
		{
			if ((value = parseStateIndex(context)) != null)
				weapon.setLowerFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_WEAPONSTATE_FIRE))
		{
			if ((value = parseStateIndex(context)) != null)
				weapon.setFireFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_WEAPONSTATE_FLASH))
		{
			if ((value = parseStateIndex(context)) != null)
				weapon.setFlashFrameIndex(value);
			else
				return false;
		}
		else
		{
			addErrorMessage(
				"Expected a valid weapon state name (%s, %s, %s, %s, %s).",
				KEYWORD_WEAPONSTATE_READY,
				KEYWORD_WEAPONSTATE_SELECT,
				KEYWORD_WEAPONSTATE_DESELECT,
				KEYWORD_WEAPONSTATE_FIRE,
				KEYWORD_WEAPONSTATE_FLASH
			);
			return false;
		}
		
		return true;
	}
	
	// Parses a weapon state body.
	private boolean parseWeaponStateBody(AbstractPatchContext<?> context, final DEHWeapon weapon)
	{
		if (!matchType(DecoHackKernel.TYPE_LBRACE))
		{
			addErrorMessage("Expected '{' after \"%s\" declaration.", KEYWORD_STATES);
			return false;
		}

		final Map<String, Integer> labelMap;
		setupWeaponStateLabels(weapon, labelMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER));

		if (!parseActorStateSet(context, labelMap, (label, idx) -> {
			if (label.equalsIgnoreCase(KEYWORD_WEAPONSTATE_READY))
				weapon.setReadyFrameIndex(idx);
			else if (label.equalsIgnoreCase(KEYWORD_WEAPONSTATE_SELECT))
				weapon.setRaiseFrameIndex(idx);
			else if (label.equalsIgnoreCase(KEYWORD_WEAPONSTATE_DESELECT))
				weapon.setLowerFrameIndex(idx);
			else if (label.equalsIgnoreCase(KEYWORD_WEAPONSTATE_FIRE))
				weapon.setFireFrameIndex(idx);
			else if (label.equalsIgnoreCase(KEYWORD_WEAPONSTATE_FLASH))
				weapon.setFlashFrameIndex(idx);
			
			labelMap.put(label, idx);
		})) return false;
		
		if (!matchType(DecoHackKernel.TYPE_RBRACE))
		{
			addErrorMessage("Expected '}' after \"%s\" section.", KEYWORD_STATES);
			return false;
		}
		
		return true;
	}
	
	// Parses an actor's state body.
	private boolean parseActorStateSet(AbstractPatchContext<?> context, Map<String, Integer> labelMap, LabelApplier applier)
	{
		if (currentIsSpriteIndex(context))
		{
			addErrorMessage("Expected state label.");
			return false;
		}
		
		// state label.
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			addErrorMessage("Expected state label.");
			return false;
		}
		
		String label = null;
		ParsedState parsed = new ParsedState();
		StateFillCursor stateCursor = new StateFillCursor();
		
		while (currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			if (!currentIsSpriteIndex(context))
			{
				label = currentToken().getLexeme();
				nextToken();
				
				if (!matchType(DecoHackKernel.TYPE_COLON))
				{
					addErrorMessage("Expected ':' after state label.");
					return false;
				}
			}

			Integer startIndex;
		
			do {
				parsed.reset();
				if (!parseStateLine(context, parsed))
					return false;
				if ((startIndex = fillStates(context, parsed, stateCursor, false)) == null)
					return false;
				if (label != null)
				{
					applier.apply(label, startIndex);
					label = null;
				}
			} while (currentIsSpriteIndex(context));
			
			// Parse next state.
			if (currentIsNextStateKeyword())
			{
				Integer nextStateIndex = null;
				if ((nextStateIndex = parseNextStateIndex(context, labelMap, startIndex, stateCursor.lastIndexFilled)) == null)
				{
					addErrorMessage("Expected next state clause (%s, %s, %s, %s).", KEYWORD_STOP, KEYWORD_WAIT, KEYWORD_LOOP, KEYWORD_GOTO);
					return false;
				}
				stateCursor.lastStateFilled.setNextStateIndex(nextStateIndex);
				stateCursor.lastStateFilled = null;
			}
		}
		
		return true;
	}
	
	// Parses a mandatory state index.
	private Integer parseStateIndex(AbstractPatchContext<?> context)
	{
		Integer value;
		if ((value = matchPositiveInteger()) == null)
		{
			addErrorMessage("Expected state index number.");
			return null;
		}
		else if (value >= context.getStateCount())
		{
			addErrorMessage("Invalid state index: %d. Max is %d.", value, context.getStateCount() - 1);
			return null;
		}
		else
		{
			return value;
		}
	}
	
	// Parses a state block.
	private boolean parseStateBlock(AbstractPatchContext<?> context)
	{
		Integer index;
		// if single state...
		if (currentType(DecoHackKernel.TYPE_NUMBER))
		{
			if ((index = parseStateIndex(context)) == null)
				return false;
			
			if (!matchType(DecoHackKernel.TYPE_LBRACE))
			{
				addErrorMessage("Expected '{' after \"%s\" header.", KEYWORD_STATE);
				return false;
			}

			if (!parseStateBody(context, index))
				return false;

			if (!matchType(DecoHackKernel.TYPE_RBRACE))
			{
				addErrorMessage("Expected '}' after \"%s\" definition.", KEYWORD_STATE);
				return false;
			}

			return true;
		}
		// if fill state...
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_FILL))
		{
			if ((index = matchPositiveInteger()) == null)
			{
				addErrorMessage("Expected state index keyword after \"%s\".", KEYWORD_FILL);
				return false;
			}

			if (index >= context.getStateCount())
			{
				addErrorMessage("Invalid state index: %d. Max is %d.", index, context.getStateCount() - 1);
				return false;
			}
			
			if (!matchType(DecoHackKernel.TYPE_LBRACE))
			{
				addErrorMessage("Expected '{' after \"%s %s\" header.", KEYWORD_STATE, KEYWORD_FILL);
				return false;
			}

			if (!parseStateFillSequence(context, index))
				return false;

			if (!matchType(DecoHackKernel.TYPE_RBRACE))
			{
				addErrorMessage("Expected '}' after \"%s %s\" block.", KEYWORD_STATE, KEYWORD_FILL);
				return false;
			}
			
			return true;
		}
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_FREE))
		{
			return parseStateFreeLine(context);
		}
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_PROTECT))
		{
			return parseStateProtectLine(context, true);
		}
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_UNPROTECT))
		{
			return parseStateProtectLine(context, false);
		}
		else
		{
			addErrorMessage("Expected state index or \"%s\" keyword after \"%s\".", KEYWORD_FILL, KEYWORD_STATE);
			return false;
		}
	}

	// Parses a sequence of auto-fill states.
	private boolean parseStateFillSequence(AbstractPatchContext<?> context, int startIndex)
	{
		if (!context.isFreeState(startIndex))
		{
			addErrorMessage("Starting state index for state fill, %d, is not a free state.", startIndex);
			return false;
		}
		
		// First frame must match state, and the block must contain at least one state.
		if (!currentIsSpriteIndex(context))
		{
			addErrorMessage("Expected sprite name (for a state description).");
			return false;
		}

		Integer index = startIndex;
		ParsedState parsed = new ParsedState();
		StateFillCursor stateCursor = new StateFillCursor();
		stateCursor.lastIndexFilled = index;
		boolean first = true;
		
		do {
			parsed.reset();
			if (!parseStateLine(context, parsed))
				return false;
			if (fillStates(context, parsed, stateCursor, first) == null)
				return false;
			first = false;
		} while (currentIsSpriteIndex(context));
		
		// Parse end.
		Integer nextStateIndex = null;
		if ((nextStateIndex = parseNextStateIndex(context, null, startIndex, stateCursor.lastIndexFilled)) == null)
		{
			addErrorMessage("Expected next state clause (%s, %s, %s, %s).", KEYWORD_STOP, KEYWORD_WAIT, KEYWORD_LOOP, KEYWORD_GOTO);
			return false;
		}
		
		stateCursor.lastStateFilled.setNextStateIndex(nextStateIndex);
		return true;
	}
	
	// Parses a single state definition body.
	// Either consists of a next state index clause, a state and next index clause, or just a state.
	private boolean parseStateBody(AbstractPatchContext<?> context, int index)
	{
		Integer nextStateIndex = null;
		if ((nextStateIndex = parseNextStateIndex(context, null, null, index)) != null)
			return true;
		
		if (currentIsSpriteIndex(context))
		{
			ParsedState parsedState = new ParsedState();
			
			boolean isBoom = context instanceof AbstractPatchBoomContext;			
			Integer pointerIndex = context.getStateActionPointerIndex(index);
			if (!parseStateLine(context, parsedState, true, isBoom ? null : pointerIndex != null))
				return false;

			if (isBoom)
			{
				if (pointerIndex != null && parsedState.action == null)
					parsedState.action = DEHActionPointer.NULL;
			}
			else if ((pointerIndex == null && parsedState.action != null) || (pointerIndex != null && parsedState.action == null))
			{
				if (parsedState.action != null)
					addErrorMessage("Action function specified for state without a function!");
				else
					addErrorMessage("Action function not specified for state with a function!");
				return false;
			}

			if (pointerIndex != null)
				context.setActionPointer(pointerIndex, parsedState.action);
			
			// fill state.
			context.getState(index)
				.setSpriteIndex(parsedState.spriteIndex)
				.setFrameIndex(parsedState.frameList.get(0))
				.setDuration(parsedState.duration)
				.setBright(parsedState.bright)
				.setParameter0(parsedState.parameter0)
				.setParameter1(parsedState.parameter1)
			;

			// Try to parse next state clause.
			nextStateIndex = parseNextStateIndex(context, null, null, index);
			if (nextStateIndex != null)
				context.getState(index).setNextStateIndex(nextStateIndex);

			return true;
		}
		else
		{
			addErrorMessage("Expected valid sprite name or next state clause (goto, stop, wait).");
			return false;				
		}
	}

	// Parse a single state and if true is returned, the input state is altered.
	// requireAction is either true, false, or null. If null, no check is performed. 
	private boolean parseStateLine(AbstractPatchContext<?> context, ParsedState state)
	{
		return parseStateLine(context, state, false, null);
	}
	
	// Parse a single state and if true is returned, the input state is altered.
	// requireAction is either true, false, or null. If null, no check is performed. 
	private boolean parseStateLine(AbstractPatchContext<?> context, ParsedState state, boolean singleFrame, Boolean requireAction) 
	{
		if ((state.spriteIndex = matchSpriteIndex(context)) == null)
		{
			addErrorMessage("Expected valid sprite name.");
			return false;				
		}
		
		if (!matchFrameIndices(state.frameList))
		{
			addErrorMessage("Expected valid frame characters after sprite name.");
			return false;				
		}
		
		if (singleFrame && state.frameList.size() > 1)
		{
			addErrorMessage("Expected a single frame character after sprite name.");
			return false;				
		}
		
		if ((state.duration = matchInteger()) == null)
		{
			addErrorMessage("Expected valid state duration after frame.");
			return false;				
		}

		state.bright = matchBrightFlag();
		
		state.action = matchActionPointer();
		
		if (requireAction != null)
		{
			if (requireAction && state.action == null)
			{
				addErrorMessage("Expected an action pointer for this state.");
				return false;				
			}
			if (!requireAction && state.action != null)
			{
				addErrorMessage("Expected no action pointer for this state. State definition attempted to set one.");
				return false;				
			}
		}
		
		// Maybe parse parameters.
		state.parameter0 = 0;
		state.parameter1 = 0;
		if (state.action != null)
		{
			if (matchType(DecoHackKernel.TYPE_LPAREN))
			{
				state.parameter0 = 0;
				state.parameter1 = 0;

				// get first argument
				Integer p;
				if ((p = matchInteger()) != null)
				{
					state.parameter0 = p;
					if (!state.action.isMBF())
					{
						addErrorMessage("Action does not require parameters.");
						return false;				
					}
					
					if (matchType(DecoHackKernel.TYPE_COMMA))
					{
						if ((state.parameter1 = matchInteger()) == null)
						{
							addErrorMessage("Expected a second parameter after ','.");
							return false;				
						}
					}
				}
				
				if (!matchType(DecoHackKernel.TYPE_RPAREN))
				{
					addErrorMessage("Expected a ')' after action parameters.");
					return false;				
				}
			}
		}
		
		return true;
	}
	
	// Parses a next state line.
	private Integer parseNextStateIndex(AbstractPatchContext<?> context, Map<String, Integer> labelMap, Integer lastLabelledStateIndex, int currentStateIndex)
	{
		// Test for only next state clause.
		if (matchIdentifierLexemeIgnoreCase(KEYWORD_STOP))
		{
			return 0;
		}
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_WAIT))
		{
			return currentStateIndex;
		}
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_LOOP))
		{
			if (lastLabelledStateIndex == null)
			{
				addErrorMessage("Can't use \"%s\" with no declared state labels.", KEYWORD_LOOP);
				return null;
			}
			return lastLabelledStateIndex;
		}
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_GOTO))
		{
			Integer nextFrame;
			String labelName;
			if ((labelName = matchIdentifier()) != null)
			{
				if (labelMap == null)
				{
					addErrorMessage("Name of label was unexpected after \"%s\". Only valid in thing or weapon.", KEYWORD_GOTO);
					return null;				
				}
				else if ((nextFrame = labelMap.get(labelName)) == null)
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
						addErrorMessage("Expected integer after label name in \"%s\".", KEYWORD_GOTO);
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
						addErrorMessage("Expected integer after label name in \"%s\".", KEYWORD_GOTO);
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
				addErrorMessage("Expected integer after \"%s\".", KEYWORD_GOTO);
				return null;				
			}
			else if (nextFrame >= context.getStateCount())
			{
				addErrorMessage("Expected valid state index after \"%s\".", KEYWORD_GOTO);
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
	
	// Parses a state freeing command.
	private boolean parseStateFreeLine(AbstractPatchContext<?> context)
	{
		Integer min, max;
		if (matchIdentifierLexemeIgnoreCase(KEYWORD_FROM))
		{
			// free chain
			if ((min = matchPositiveInteger()) != null)
			{
				context.freeConnectedStates(min);
				return true;
			}
			else
			{
				addErrorMessage("Expected state index after \"%s\".", KEYWORD_FROM);
				return false;
			}
		}
		else if ((min = matchPositiveInteger()) != null)
		{
			if (min >= context.getStateCount())
			{
				addErrorMessage("Invalid state index: %d. Max is %d.", min, context.getStateCount() - 1);
				return false;
			}
			
			// free range
			if (matchIdentifierLexemeIgnoreCase(KEYWORD_TO))
			{
				if ((max = matchPositiveInteger()) != null)
				{
					if (max >= context.getStateCount())
					{
						addErrorMessage("Invalid state index: %d. Max is %d.", max, context.getStateCount() - 1);
						return false;
					}
					
					int a = Math.min(min, max);
					int b = Math.max(min, max);
					while (a <= b)
						context.setFreeState(a++, true);
					return true;
				}
				else
				{
					addErrorMessage("Expected state index after \"%s\".", KEYWORD_TO);
					return false;
				}
			}
			// free single
			else
			{
				context.setFreeState(min, true);
				return true;
			}
		}
		else
		{
			addErrorMessage("Expected \"%s\" or state index after \"%s\".", KEYWORD_FROM, KEYWORD_FREE);
			return false;
		}
	}
	
	// Parses a state protection line.
	private boolean parseStateProtectLine(AbstractPatchContext<?> context, boolean protectedState)
	{
		Integer min, max;
		if ((min = matchPositiveInteger()) != null)
		{
			if (min >= context.getStateCount())
			{
				addErrorMessage("Invalid state index: %d. Max is %d.", min, context.getStateCount() - 1);
				return false;
			}
			
			// protect range
			if (matchIdentifierLexemeIgnoreCase(KEYWORD_TO))
			{
				if ((max = matchPositiveInteger()) != null)
				{
					if (max >= context.getStateCount())
					{
						addErrorMessage("Invalid state index: %d. Max is %d.", max, context.getStateCount() - 1);
						return false;
					}
					
					int a = Math.min(min, max);
					int b = Math.max(min, max);
					while (a <= b)
						context.setProtectedState(a++, protectedState);
					return true;
				}
				else
				{
					addErrorMessage("Expected state index after \"%s\".", KEYWORD_TO);
					return false;
				}
			}
			// protect single state.
			else
			{
				context.setProtectedState(min, protectedState);
				return true;
			}
		}
		else
		{
			addErrorMessage("Expected state index after \"%s\".", protectedState ? KEYWORD_PROTECT : KEYWORD_UNPROTECT);
			return false;
		}
	}
	
	// Attempts to fill states from a starting index.
	// If forceFirst is true, the state index filled MUST be cursor.lastFilledIndex. 
	// Returns the FIRST INDEX FILLED or null if error.
	private Integer fillStates(AbstractPatchContext<?> context, ParsedState state, StateFillCursor cursor, boolean forceFirst)
	{
		Integer out = null;
		boolean isBoom = context instanceof AbstractPatchBoomContext;
		
		while (!state.frameList.isEmpty())
		{
			Integer currentIndex;
			if ((currentIndex = searchNextState(context, state, cursor)) == null)
				return null;
			if (out == null)
				out = currentIndex;
			
			if (!isBoom && forceFirst && currentIndex != cursor.lastIndexFilled)
			{
				addErrorMessage("Provided state definition would not fill state " + cursor.lastIndexFilled + ". " + (
					state.action != null 
						? "State " + cursor.lastIndexFilled + " cannot have an action pointer."
						: "State " + cursor.lastIndexFilled + " must have an action pointer."
				));
				return null;
			}
			
			if (cursor.lastStateFilled != null)
				cursor.lastStateFilled.setNextStateIndex(currentIndex);
	
			Integer pointerIndex = context.getStateActionPointerIndex(currentIndex);
			
			cursor.lastStateFilled = context.getState(currentIndex)
				.setSpriteIndex(state.spriteIndex)
				.setFrameIndex(state.frameList.pollFirst())
				.setDuration(state.duration)
				.setBright(state.bright)
				.setParameter0(state.parameter0)
				.setParameter1(state.parameter1)
			;
	
			if (isBoom && pointerIndex != null && state.action == null)
				state.action = DEHActionPointer.NULL;
			
			if (pointerIndex != null)
				context.setActionPointer(pointerIndex, state.action);
			
			context.setFreeState(currentIndex, false);
			cursor.lastIndexFilled = currentIndex;
			forceFirst = false;
		}
		
		return out;
	}
	
	private void setupWeaponStateLabels(DEHWeapon weapon, Map<String, Integer> labelMap)
	{
		int index;
		if ((index = weapon.getReadyFrameIndex()) > 0)
			labelMap.put(KEYWORD_WEAPONSTATE_READY, index);
		if ((index = weapon.getRaiseFrameIndex()) > 0)
			labelMap.put(KEYWORD_WEAPONSTATE_SELECT, index);
		if ((index = weapon.getLowerFrameIndex()) > 0)
			labelMap.put(KEYWORD_WEAPONSTATE_DESELECT, index);
		if ((index = weapon.getFireFrameIndex()) > 0)
			labelMap.put(KEYWORD_WEAPONSTATE_FIRE, index);
		if ((index = weapon.getFlashFrameIndex()) > 0)
			labelMap.put(KEYWORD_WEAPONSTATE_FLASH, index);
	}

	private void setupThingStateLabels(DEHThing thing, Map<String, Integer> labelMap)
	{
		int index;
		if ((index = thing.getSpawnFrameIndex()) > 0)
			labelMap.put(KEYWORD_THINGSTATE_SPAWN, index);
		if ((index = thing.getWalkFrameIndex()) > 0)
			labelMap.put(KEYWORD_THINGSTATE_SEE, index);
		if ((index = thing.getPainFrameIndex()) > 0)
			labelMap.put(KEYWORD_THINGSTATE_PAIN, index);
		if ((index = thing.getMeleeFrameIndex()) > 0)
			labelMap.put(KEYWORD_THINGSTATE_MELEE, index);
		if ((index = thing.getMissileFrameIndex()) > 0)
			labelMap.put(KEYWORD_THINGSTATE_MISSILE, index);
		if ((index = thing.getDeathFrameIndex()) > 0)
			labelMap.put(KEYWORD_THINGSTATE_DEATH, index);
		if ((index = thing.getExtremeDeathFrameIndex()) > 0)
			labelMap.put(KEYWORD_THINGSTATE_XDEATH, index);
		if ((index = thing.getRaiseFrameIndex()) > 0)
			labelMap.put(KEYWORD_THINGSTATE_RAISE, index);
	}

	private Integer searchNextState(AbstractPatchContext<?> context, ParsedState state, StateFillCursor cursor) 
	{
		boolean isBoom = context instanceof AbstractPatchBoomContext;
		
		Integer index = isBoom 
			? context.findNextFreeState(cursor.lastIndexFilled)
			: (state.action != null 
				? context.findNextFreeActionPointerState(cursor.lastIndexFilled)
				: context.findNextFreeNonActionPointerState(cursor.lastIndexFilled)
			);
		
		if (index == null) 
		{
			if (isBoom)
			{
				addErrorMessage("No more free states.");
			}
			else
			{
				if (state.action != null)
					addErrorMessage("No more free states with an action pointer.");
				else
					addErrorMessage("No more free states without an action pointer.");
			}
		}
		return index;
	}

	// Tests for an identifier that is a "next state" keyword.
	private boolean currentIsNextStateKeyword()
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
			return false;

		switch (currentToken().getLexeme().toLowerCase())
		{
			case KEYWORD_STOP:
			case KEYWORD_GOTO:
			case KEYWORD_LOOP:
			case KEYWORD_WAIT:
				return true; 
			default:
				return false;
		}
	}

	// Tests for an identifier or string that references a sprite name.
	private boolean currentIsSpriteIndex(DEHPatch patch)
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER, DecoHackKernel.TYPE_STRING))
			return false;
		else
			return patch.getSpriteIndex(currentToken().getLexeme()) != null;
	}

	// Tests for an identifier that references a thing state name.
	private boolean currentIsThingState()
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
			return false;
		
		switch (currentToken().getLexeme().toLowerCase())
		{
			case KEYWORD_THINGSTATE_SPAWN:
			case KEYWORD_THINGSTATE_SEE:
			case KEYWORD_THINGSTATE_MELEE:
			case KEYWORD_THINGSTATE_MISSILE:
			case KEYWORD_THINGSTATE_PAIN:
			case KEYWORD_THINGSTATE_DEATH:
			case KEYWORD_THINGSTATE_XDEATH:
			case KEYWORD_THINGSTATE_RAISE:
				return true; 
			default:
				return false;
		}
	}

	// Tests for an identifier that references a weapon state name.
	private boolean currentIsWeaponState()
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
			return false;
		
		switch (currentToken().getLexeme().toLowerCase())
		{
			case KEYWORD_WEAPONSTATE_READY:
			case KEYWORD_WEAPONSTATE_DESELECT:
			case KEYWORD_WEAPONSTATE_SELECT:
			case KEYWORD_WEAPONSTATE_FIRE:
			case KEYWORD_WEAPONSTATE_FLASH:
				return true; 
			default:
				return false;
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
	private Ammo matchAmmoType()
	{
		if (matchIdentifierLexemeIgnoreCase(KEYWORD_BULLETS))
			return Ammo.BULLETS;
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_SHELLS))
			return Ammo.SHELLS;
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_CELLS))
			return Ammo.CELLS;
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_ROCKETS))
			return Ammo.ROCKETS;
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_INFINITE))
			return Ammo.INFINITE;
		else
			return null;
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

		String lexeme = currentToken().getLexeme();
		for (int i = 0; i < lexeme.length(); i++)
		{
			char c = lexeme.charAt(i);
			if (c < 'A' || c > ']')
				throw new IllegalArgumentException("Subframe list contains an invalid character: " + c + " Expected A through ].");
			outList.add(c - 'A');
		}
		nextToken();
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
		return matchPositiveInteger();
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
	
	@FunctionalInterface
	private static interface LabelApplier
	{
		void apply(String label, int index);
	}
	
	private static class StateFillCursor
	{
		private DEHState lastStateFilled;
		private int lastIndexFilled;
	}
	
	private static class ParsedState
	{
		private Integer spriteIndex;
		private LinkedList<Integer> frameList;
		private Integer duration;
		private Boolean bright;
		private DEHActionPointer action;
		private Integer parameter0;
		private Integer parameter1;
		
		private ParsedState()
		{
			this.frameList = new LinkedList<>();
			reset();
		}
		
		void reset()
		{
			this.spriteIndex = null;
			this.frameList.clear();
			this.duration = null;
			this.bright = null;
			this.action = null;
			this.parameter0 = null;
			this.parameter1 = null;
		}
		
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

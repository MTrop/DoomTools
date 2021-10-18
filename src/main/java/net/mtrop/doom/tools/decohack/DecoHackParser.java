/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import net.mtrop.doom.tools.decohack.contexts.AbstractPatchBoomContext;
import net.mtrop.doom.tools.decohack.contexts.AbstractPatchContext;
import net.mtrop.doom.tools.decohack.contexts.AbstractPatchDoom19Context;
import net.mtrop.doom.tools.decohack.contexts.PatchBoomContext;
import net.mtrop.doom.tools.decohack.contexts.PatchDSDHackedContext;
import net.mtrop.doom.tools.decohack.contexts.PatchExtendedContext;
import net.mtrop.doom.tools.decohack.contexts.PatchDoom19Context;
import net.mtrop.doom.tools.decohack.contexts.PatchMBFContext;
import net.mtrop.doom.tools.decohack.contexts.PatchMBF21Context;
import net.mtrop.doom.tools.decohack.contexts.PatchUltimateDoom19Context;
import net.mtrop.doom.tools.decohack.data.DEHActor;
import net.mtrop.doom.tools.decohack.data.DEHAmmo;
import net.mtrop.doom.tools.decohack.data.DEHMiscellany;
import net.mtrop.doom.tools.decohack.data.DEHSound;
import net.mtrop.doom.tools.decohack.data.DEHState;
import net.mtrop.doom.tools.decohack.data.DEHThing;
import net.mtrop.doom.tools.decohack.data.DEHThingTarget;
import net.mtrop.doom.tools.decohack.data.DEHThingTemplate;
import net.mtrop.doom.tools.decohack.data.DEHWeapon;
import net.mtrop.doom.tools.decohack.data.DEHWeapon.Ammo;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointer;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerParam;
import net.mtrop.doom.tools.decohack.data.enums.DEHFeatureLevel;
import net.mtrop.doom.tools.decohack.data.enums.DEHStateFlag;
import net.mtrop.doom.tools.decohack.data.enums.DEHThingFlag;
import net.mtrop.doom.tools.decohack.data.enums.DEHThingMBF21Flag;
import net.mtrop.doom.tools.decohack.data.enums.DEHWeaponMBF21Flag;
import net.mtrop.doom.tools.decohack.data.DEHWeaponTarget;
import net.mtrop.doom.tools.decohack.data.DEHWeaponTemplate;
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
	private static final String KEYWORD_MEGASPHERE_HEALTH = "megasphereHealth";
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
	private static final String KEYWORD_PROPERTIES = "properties";
	private static final String KEYWORD_SPRITENAME = "spritename";
	private static final String KEYWORD_FRAME = "frame";
	private static final String KEYWORD_DURATION = "duration";
	private static final String KEYWORD_NEXTSTATE = "nextstate";
	private static final String KEYWORD_POINTER = "pointer";

	private static final String KEYWORD_AUTO = "auto";
	private static final String KEYWORD_EACH = "each";
	private static final String KEYWORD_IN = "in";
	private static final String KEYWORD_TO = "to";
	private static final String KEYWORD_FROM = "from";
	
	private static final String KEYWORD_SOUND = "sound";
	private static final String KEYWORD_SOUNDS = "sounds";
	private static final String KEYWORD_SINGULAR = "singular";
	private static final String KEYWORD_PRIORITY = "priority";

	private static final String KEYWORD_AMMO = "ammo";
	private static final String KEYWORD_PICKUP = "pickup";
	private static final String KEYWORD_MAX = "max";
	
	private static final String KEYWORD_STRINGS = "strings";
	
	private static final String KEYWORD_STATES = "states";
	
	private static final String KEYWORD_WEAPON = "weapon";
	private static final String KEYWORD_AMMOTYPE = "ammotype";
	private static final String KEYWORD_AMMOPERSHOT = "ammopershot";
	private static final String KEYWORD_WEAPONSTATE_READY = DEHWeapon.STATE_LABEL_READY;
	private static final String KEYWORD_WEAPONSTATE_SELECT = DEHWeapon.STATE_LABEL_SELECT;
	private static final String KEYWORD_WEAPONSTATE_DESELECT = DEHWeapon.STATE_LABEL_DESELECT;
	private static final String KEYWORD_WEAPONSTATE_FIRE = DEHWeapon.STATE_LABEL_FIRE;
	private static final String KEYWORD_WEAPONSTATE_FLASH = DEHWeapon.STATE_LABEL_FLASH;
	
	private static final String KEYWORD_THING = "thing";
	private static final String KEYWORD_THINGSTATE_SPAWN = DEHThing.STATE_LABEL_SPAWN;
	private static final String KEYWORD_THINGSTATE_SEE = DEHThing.STATE_LABEL_SEE;
	private static final String KEYWORD_THINGSTATE_MELEE = DEHThing.STATE_LABEL_MELEE;
	private static final String KEYWORD_THINGSTATE_MISSILE = DEHThing.STATE_LABEL_MISSILE;
	private static final String KEYWORD_THINGSTATE_PAIN = DEHThing.STATE_LABEL_PAIN;
	private static final String KEYWORD_THINGSTATE_DEATH = DEHThing.STATE_LABEL_DEATH;
	private static final String KEYWORD_THINGSTATE_XDEATH = DEHThing.STATE_LABEL_XDEATH;
	private static final String KEYWORD_THINGSTATE_RAISE = DEHThing.STATE_LABEL_RAISE;
	private static final String KEYWORD_EDNUM = "ednum";
	private static final String KEYWORD_FLAGS = "flags";
	private static final String KEYWORD_MASS = "mass";
	private static final String KEYWORD_MELEERANGE = "meleerange";
	private static final String KEYWORD_PAINCHANCE = "painchance";
	private static final String KEYWORD_REACTIONTIME = "reactiontime";
	private static final String KEYWORD_HEALTH = "health";
	private static final String KEYWORD_DAMAGE = "damage";
	private static final String KEYWORD_HEIGHT = "height";
	private static final String KEYWORD_RADIUS = "radius";
	private static final String KEYWORD_SPEED = "speed";
	private static final String KEYWORD_FASTSPEED = "fastspeed";
	private static final String KEYWORD_DROPITEM = "dropitem";
	private static final String KEYWORD_INFIGHTINGGROUP = "infightinggroup";
	private static final String KEYWORD_PROJECTILEGROUP = "projectilegroup";
	private static final String KEYWORD_SPLASHGROUP = "splashgroup";
	private static final String KEYWORD_SEESOUND = "seesound";
	private static final String KEYWORD_ATTACKSOUND = "attacksound";
	private static final String KEYWORD_PAINSOUND = "painsound";
	private static final String KEYWORD_DEATHSOUND = "deathsound";
	private static final String KEYWORD_ACTIVESOUND = "activesound";
	private static final String KEYWORD_RIPSOUND = "ripsound";

	private static final String KEYWORD_OFFSET = "offset";
	private static final String KEYWORD_STATE_BRIGHT = "bright";
	private static final String KEYWORD_STATE_NOTBRIGHT = "notbright";
	private static final String KEYWORD_STATE_FAST = "fast";
	private static final String KEYWORD_STATE_NOTFAST = "notfast";
	
	private static final String KEYWORD_WITH = "with";
	private static final String KEYWORD_SWAP = "swap";

	private static final String KEYWORD_USING = "using";
	private static final String KEYWORD_DOOM19 = "doom19";
	private static final String KEYWORD_UDOOM19 = "udoom19";
	private static final String KEYWORD_BOOM = "boom";
	private static final String KEYWORD_MBF = "mbf";
	private static final String KEYWORD_EXTENDED = "extended";
	private static final String KEYWORD_MBF21 = "mbf21";
	private static final String KEYWORD_DSDHACKED = "dsdhacked";
	
	private static final String KEYWORD_NULL = "null";

	private static final Pattern MAPLUMP_EXMY = Pattern.compile("E[0-9]+M[0-9]+", Pattern.CASE_INSENSITIVE);
	private static final Pattern MAPLUMP_MAPXX = Pattern.compile("MAP[0-9][0-9]+", Pattern.CASE_INSENSITIVE);

	private static final FutureLabels EMPTY_LABELS = null;
	private static final int PLACEHOLDER_LABEL = 1234567890;

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

	@Override
	protected void nextToken() 
	{
		do {
			super.nextToken();
			
			/*
			 * Some line comments are DECORATE Actor editor keys.
			 * DECOHack should use these to set on things for export later, however,
			 * any use context that is not inside a "thing" is useless, but should still
			 * be treated like a comment in other situations. Passive parsing of these
			 * key comments seems to be the "best of both worlds" approach.
			 */
			if (currentType(DecoHackKernel.TYPE_LINE_COMMENT) && currentToken().getLexeme().startsWith("$"))
			{
				// Lazily split this thing.
				String content = currentToken().getLexeme().substring(1).trim();
				int splitIndex = content.indexOf(' ');
				if (splitIndex > 0)
				{
					String value = content.substring(splitIndex).trim();
					if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"')
						value = value.substring(1, value.length() - 1);
					editorKeys.put(content.substring(0, splitIndex), value);
				}
				else
					editorKeys.put(content, "");
			}
			
		} while (currentType(DecoHackKernel.TYPE_COMMENT, DecoHackKernel.TYPE_LINE_COMMENT));
	}
	
	// =======================================================================

	/**
	 * Parse "using" line (must be first).
	 */
	private AbstractPatchContext<?> parseUsing()
	{
		if (!matchIdentifierIgnoreCase(KEYWORD_USING))
		{
			addErrorMessage("Expected \"using\" clause to set the patch format.");
			return null;
		}
		
		if (matchIdentifierIgnoreCase(KEYWORD_DOOM19))
			return new PatchDoom19Context();
		else if (matchIdentifierIgnoreCase(KEYWORD_UDOOM19))
			return new PatchUltimateDoom19Context();
		else if (matchIdentifierIgnoreCase(KEYWORD_BOOM))
			return new PatchBoomContext();
		else if (matchIdentifierIgnoreCase(KEYWORD_MBF))
			return new PatchMBFContext();
		else if (matchIdentifierIgnoreCase(KEYWORD_EXTENDED))
			return new PatchExtendedContext();
		else if (matchIdentifierIgnoreCase(KEYWORD_MBF21))
			return new PatchMBF21Context();
		else if (matchIdentifierIgnoreCase(KEYWORD_DSDHACKED))
			return new PatchDSDHackedContext();
		else
		{
			addErrorMessage("Expected valid patch format type (%s, %s, %s, %s, %s, %s, %s).", 
				KEYWORD_DOOM19, KEYWORD_UDOOM19, KEYWORD_BOOM, KEYWORD_MBF, KEYWORD_EXTENDED, KEYWORD_MBF21, KEYWORD_DSDHACKED
			);
			return null;
		}
	}

	/**
	 * Parse entries.
	 */
	private boolean parseEntry(AbstractPatchContext<?> context)
	{
		if (matchIdentifierIgnoreCase(KEYWORD_STRINGS))
			return parseStringBlock(context);
		else if (matchIdentifierIgnoreCase(KEYWORD_AMMO))
			return parseAmmoBlock(context);
		else if (matchIdentifierIgnoreCase(KEYWORD_SOUND))
			return parseSoundBlock(context);
		else if (matchIdentifierIgnoreCase(KEYWORD_STATE))
			return parseStateBlock(context);
		else if (matchIdentifierIgnoreCase(KEYWORD_PARS))
			return parseParBlock(context);
		else if (matchIdentifierIgnoreCase(KEYWORD_THING))
			return parseThingBlock(context);
		else if (matchIdentifierIgnoreCase(KEYWORD_WEAPON))
			return parseWeaponBlock(context);
		else if (matchIdentifierIgnoreCase(KEYWORD_MISC))
			return parseMiscellaneousBlock(context);
		else if (matchIdentifierIgnoreCase(KEYWORD_EACH))
		{
			if (matchIdentifierIgnoreCase(KEYWORD_THING))
				return parseThingEachBlock(context);
			else if (matchIdentifierIgnoreCase(KEYWORD_WEAPON))
				return parseWeaponEachBlock(context);
			else
			{
				addErrorMessage("Expected \"%s\" or \"%s\" after \"%s\".", KEYWORD_THING, KEYWORD_WEAPON, KEYWORD_EACH);
				return false;
			}
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_AUTO))
		{
			if (matchIdentifierIgnoreCase(KEYWORD_THING))
				return parseThingAutoBlock(context);
			else
			{
				addErrorMessage("Expected \"%s\" after \"%s\".", KEYWORD_THING, KEYWORD_AUTO);
				return false;
			}
		}
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
		
		if (context.supports(DEHFeatureLevel.BOOM))
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
		else if (context.supports(DEHFeatureLevel.DOOM19))
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
			if (!context.isValidStringKey(stringKey))
			{
				addErrorMessage("String name \"" + stringKey + "\" is not a valid string name.");
				return false;
			}
			
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
		DEHAmmo ammo;
		Integer ammoIndex;
		if ((ammoIndex = matchPositiveInteger()) == null)
		{
			addErrorMessage("Expected ammo type: an integer from 0 to %d.", context.getAmmoCount() - 1);
			return false;
		}		
		else if (ammoIndex >= context.getAmmoCount())
		{
			addErrorMessage("Expected ammo type: an integer from 0 to %d.", context.getAmmoCount() - 1);
			return false;
		}
		else if ((ammo = context.getAmmo(ammoIndex)) == null)
		{
			addErrorMessage("Expected ammo type: an integer from 0 to %d.", context.getAmmoCount() - 1);
			return false;
		}
		
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
			if (matchIdentifierIgnoreCase(KEYWORD_MAX))
			{
				Integer value;
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_MAX);
					return false;
				}
				ammo.setMax(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_PICKUP))
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
		DEHSound sound;
		Integer soundIndex;
		if ((soundIndex = matchSoundIndexName(context)) != null)
		{
			if ((sound = context.getSound(soundIndex)) == null)
			{
				addErrorMessage("Expected valid sound name after \"%s\".", KEYWORD_SOUND);
				return false;
			}
		}
		else
		{
			addErrorMessage("Expected sound name after \"%s\".", KEYWORD_SOUND);
			return false;
		}

		if (!matchType(DecoHackKernel.TYPE_LBRACE))
		{
			addErrorMessage("Expected '{' after \"%s\" header.", KEYWORD_SOUND);
			return false;
		}

		while (currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			if (matchIdentifierIgnoreCase(KEYWORD_PRIORITY))
			{
				Integer value;
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_PRIORITY);
					return false;
				}
				sound.setPriority(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_SINGULAR))
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
		if (!context.supports(DEHFeatureLevel.BOOM))
		{
			addErrorMessage("Par block not supported in non-Boom-feature-level patches.");
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
			
			if (matchType(DecoHackKernel.TYPE_COLON))
			{
				int minutes = seconds;
				if ((seconds = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected seconds after ':'.");
					return false;
				}
				seconds = (minutes * 60) + seconds;
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
			if (matchIdentifierIgnoreCase(KEYWORD_MONSTER_INFIGHTING))
			{
				if ((flag = matchBoolean()) == null)
				{
					addErrorMessage("Expected boolean value after \"%s\".", KEYWORD_MONSTER_INFIGHTING);
					return false;
				}
				misc.setMonsterInfightingEnabled(flag);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_INITIAL_BULLETS))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_INITIAL_BULLETS);
					return false;
				}
				misc.setInitialBullets(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_INITIAL_HEALTH))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_INITIAL_HEALTH);
					return false;
				}
				misc.setInitialHealth(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_GREEN_ARMOR_CLASS))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_GREEN_ARMOR_CLASS);
					return false;
				}
				misc.setGreenArmorClass(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_BLUE_ARMOR_CLASS))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_BLUE_ARMOR_CLASS);
					return false;
				}
				misc.setBlueArmorClass(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_SOULSPHERE_HEALTH))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_SOULSPHERE_HEALTH);
					return false;
				}
				misc.setSoulsphereHealth(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_MAX_SOULSPHERE_HEALTH))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_MAX_SOULSPHERE_HEALTH);
					return false;
				}
				misc.setMaxSoulsphereHealth(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_MEGASPHERE_HEALTH))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_MEGASPHERE_HEALTH);
					return false;
				}
				misc.setMegasphereHealth(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_GOD_MODE_HEALTH))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_GOD_MODE_HEALTH);
					return false;
				}
				misc.setGodModeHealth(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_IDFA_ARMOR))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_IDFA_ARMOR);
					return false;
				}
				misc.setIDFAArmor(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_IDFA_ARMOR_CLASS))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_IDFA_ARMOR_CLASS);
					return false;
				}
				misc.setIDFAArmorClass(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_IDKFA_ARMOR))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_IDKFA_ARMOR);
					return false;
				}
				misc.setIDKFAArmor(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_IDKFA_ARMOR_CLASS))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_IDKFA_ARMOR_CLASS);
					return false;
				}
				misc.setIDKFAArmorClass(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_BFG_CELLS_PER_SHOT))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_BFG_CELLS_PER_SHOT);
					return false;
				}
				misc.setBFGCellsPerShot(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_MAX_HEALTH))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", KEYWORD_MAX_HEALTH);
					return false;
				}
				misc.setMaxHealth(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_MAX_ARMOR))
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
				addErrorMessage("Expected valid miscellaneous entry type.");
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
	
			if (context.isProtectedState(index))
			{
				addErrorMessage("State index %d is a protected state.", index);
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
		else if (matchIdentifierIgnoreCase(KEYWORD_FILL))
		{
			if ((index = parseStateIndex(context)) == null)
				return false;
	
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
		else if (matchIdentifierIgnoreCase(KEYWORD_FREE))
		{
			return parseStateFreeLine(context);
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_PROTECT))
		{
			return parseStateProtectLine(context, true);
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_UNPROTECT))
		{
			return parseStateProtectLine(context, false);
		}
		else
		{
			addErrorMessage("Expected state index or \"%s\" keyword after \"%s\".", KEYWORD_FILL, KEYWORD_STATE);
			return false;
		}
	}

	// Parses a state freeing command.
	private boolean parseStateFreeLine(AbstractPatchContext<?> context)
	{
		Integer min, max;
		if (matchIdentifierIgnoreCase(KEYWORD_FROM))
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
			if (matchIdentifierIgnoreCase(KEYWORD_TO))
			{
				if ((max = matchPositiveInteger()) != null)
				{
					if (max >= context.getStateCount())
					{
						addErrorMessage("Invalid state index: %d. Max is %d.", max, context.getStateCount() - 1);
						return false;
					}
					
					context.setFreeState(min, max, true);
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
			if (matchIdentifierIgnoreCase(KEYWORD_TO))
			{
				if ((max = matchPositiveInteger()) != null)
				{
					if (max >= context.getStateCount())
					{
						addErrorMessage("Invalid state index: %d. Max is %d.", max, context.getStateCount() - 1);
						return false;
					}
					
					context.setProtectedState(min, max, protectedState);
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

	// Parses a thing block.
	private boolean parseThingBlock(AbstractPatchContext<?> context)
	{
		Integer slot;
		if ((slot = matchThingIndex(context)) == null)
			return false;

		// thing swap
		if (matchIdentifierIgnoreCase(KEYWORD_SWAP))
		{
			if (!matchIdentifierIgnoreCase(KEYWORD_WITH))
			{
				addErrorMessage("Expected \"%s\" after \"%s\".", KEYWORD_WITH, KEYWORD_SWAP);
				return false;
			}

			Integer other;
			if ((other = matchThingIndex(context)) == null)
				return false;

			DEHThing temp = new DEHThing();
			temp.copyFrom(context.getThing(other));
			context.getThing(other).copyFrom(context.getThing(slot));
			context.getThing(slot).copyFrom(temp);
			
			// Affected things are no longer "free"
			context.setFreeThing(slot, false);
			context.setFreeThing(other, false);
			
			return true;
		}
		// free things or thing states.
		else if (matchIdentifierIgnoreCase(KEYWORD_FREE))
		{
			if (matchIdentifierIgnoreCase(KEYWORD_STATES))
			{
				context.freeThingStates(slot);
				return true;
			}
			
			Integer min;
			if ((min = matchPositiveInteger()) != null)
			{
				if ((min = verifyThingIndex(context, min)) == null)
					return false;
				
				if (!matchIdentifierIgnoreCase(KEYWORD_TO))
				{
					context.setFreeThing(min, true);
					return true;
				}
				
				Integer max;
				if ((max = matchThingIndex(context)) == null)
					return false;
				
				context.setFreeThing(min, max, true);
				return true;
			}

			if (!currentIsThingState())
			{
				addErrorMessage("Expected thing state name or \"%s\" or a thing index after \"%s\".", KEYWORD_STATES, KEYWORD_FREE);
				return false;
			}

			DEHThing thing = context.getThing(slot);

			if (matchIdentifierIgnoreCase(KEYWORD_THINGSTATE_SPAWN))
				context.freeConnectedStates(thing.getSpawnFrameIndex());
			else if (matchIdentifierIgnoreCase(KEYWORD_THINGSTATE_SEE))
				context.freeConnectedStates(thing.getWalkFrameIndex());
			else if (matchIdentifierIgnoreCase(KEYWORD_THINGSTATE_MELEE))
				context.freeConnectedStates(thing.getMeleeFrameIndex());
			else if (matchIdentifierIgnoreCase(KEYWORD_THINGSTATE_MISSILE))
				context.freeConnectedStates(thing.getMissileFrameIndex());
			else if (matchIdentifierIgnoreCase(KEYWORD_THINGSTATE_PAIN))
				context.freeConnectedStates(thing.getPainFrameIndex());
			else if (matchIdentifierIgnoreCase(KEYWORD_THINGSTATE_DEATH))
				context.freeConnectedStates(thing.getDeathFrameIndex());
			else if (matchIdentifierIgnoreCase(KEYWORD_THINGSTATE_XDEATH))
				context.freeConnectedStates(thing.getExtremeDeathFrameIndex());
			else if (matchIdentifierIgnoreCase(KEYWORD_THINGSTATE_RAISE))
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
			DEHThing thing = context.getThing(slot);
			
			if (matchType(DecoHackKernel.TYPE_COLON))
			{
				if (!matchIdentifierIgnoreCase(KEYWORD_THING))
				{
					addErrorMessage("Expected \"%s\" after ':'.", KEYWORD_THING);
					return false;
				}
				
				Integer sourceSlot;
				if ((sourceSlot = matchThingIndex(context)) == null)
					return false;
				
				thing.copyFrom(context.getThing(sourceSlot));
			}
			
			if (currentType(DecoHackKernel.TYPE_STRING))
			{
				thing.setName(matchString());
			}
			
			context.setFreeThing(slot, false);
			return parseThingBody(context, thing);
		}
	}

	// Parses an "each thing" block.
	private boolean parseThingEachBlock(final AbstractPatchContext<?> context)
	{
		if (matchIdentifierIgnoreCase(KEYWORD_IN))
		{
			return parseThingEachInBlock(context);
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_FROM))
		{
			return parseThingEachFromBlock(context);
		}
		else
		{
			addErrorMessage("Expected '%s' or '%s' after \"%s %s\" declaration.", KEYWORD_IN, KEYWORD_FROM, KEYWORD_EACH, KEYWORD_THING);
			return false;
		}
	}
	
	// Parses an "each thing in" block.
	private boolean parseThingEachInBlock(final AbstractPatchContext<?> context)
	{
		List<Integer> thingList = new LinkedList<>();
		
		if (!matchType(DecoHackKernel.TYPE_LPAREN))
		{
			addErrorMessage("Expected '(' after \"%s\".", KEYWORD_IN);
			return false;
		}

		while (currentType(DecoHackKernel.TYPE_NUMBER))
		{
			Integer id;
			if ((id = matchThingIndex(context)) == null)
				return false;

			thingList.add(id);
			
			if (!matchType(DecoHackKernel.TYPE_COMMA))
				break;
		}
		
		if (!matchType(DecoHackKernel.TYPE_RPAREN))
		{
			addErrorMessage("Expected ')' after the list of things.");
			return false;
		}

		if (thingList.isEmpty())
		{
			addErrorMessage("Expected at least one thing in a list of things.");
			return false;
		}
		
		DEHThingTemplate template = new DEHThingTemplate();
		if (!parseThingBody(context, template))
			return false;
		
		for (Integer id : thingList)
		{
			DEHThing thing;
			if ((thing = context.getThing(id)) == null)
			{
				addErrorMessage("INTERNAL ERROR. Thing id %d invalid, but passed parsing.", id);
				return false;
			}
			
			template.applyTo(thing);
			context.setFreeThing(id, false);
		}

		return true;
	}
	
	// Parses an "each thing from" block.
	private boolean parseThingEachFromBlock(final AbstractPatchContext<?> context)
	{
		Integer min;
		if ((min = matchThingIndex(context)) == null)
			return false;

		if (!matchIdentifierIgnoreCase(KEYWORD_TO))
		{
			addErrorMessage("Expected \"%s\" after starting thing index.", KEYWORD_TO);
			return false;
		}

		Integer max;
		if ((max = matchThingIndex(context)) == null)
			return false;
		
		DEHThingTemplate template = new DEHThingTemplate();
		if (!parseThingBody(context, template))
			return false;
		
		int a = Math.min(min, max);
		int b = Math.max(min, max);
		while (a <= b)
		{
			int id = a++;
			
			DEHThing thing;
			if ((thing = context.getThing(id)) == null)
			{
				addErrorMessage("INTERNAL ERROR. Thing id %d invalid, but passed parsing.", id);
				return false;
			}
			
			template.applyTo(thing);
			context.setFreeThing(id, false);
		}
		
		return true;
	}
	
	// Parses an "auto thing" block.
	private boolean parseThingAutoBlock(final AbstractPatchContext<?> context)
	{
		// TODO: Finish this.
		addErrorMessage("UNFINISHED");
		return false;
	}
	
	// Parses a thing body.
	private boolean parseThingBody(AbstractPatchContext<?> context, DEHThingTarget<?> thing)
	{
		editorKeys.clear();
		
		if (!matchType(DecoHackKernel.TYPE_LBRACE))
		{
			addErrorMessage("Expected '{' after \"%s\" declaration.", KEYWORD_THING);
			return false;
		}
		
		Integer value;
		while (currentType(DecoHackKernel.TYPE_IDENTIFIER, DecoHackKernel.TYPE_PLUS, DecoHackKernel.TYPE_DASH))
		{
			if (matchType(DecoHackKernel.TYPE_PLUS))
			{
				if ((value = matchThingFlagMnemonic()) != null)
				{
					thing.addFlag(value);
				}
				else if ((value = matchThingMBF21FlagMnemonic(context)) != null)
				{
					thing.addMBF21Flag(value);
				}
				else if (matchIdentifierIgnoreCase(KEYWORD_MBF21))
				{
					if ((value = matchPositiveInteger()) != null)
					{
						thing.addMBF21Flag(value);
					}
					else
					{
						addErrorMessage("Expected integer after \"+ mbf21\".");
						return false;
					}
				}
				else if ((value = matchPositiveInteger()) != null)
				{
					thing.addFlag(value);
				}
				else
				{
					addErrorMessage("Expected integer after \"+\".");
					return false;
				}
			}
			else if (matchType(DecoHackKernel.TYPE_DASH))
			{
				if ((value = matchThingFlagMnemonic()) != null)
				{
					thing.removeFlag(value);
				}
				else if ((value = matchThingMBF21FlagMnemonic(context)) != null)
				{
					thing.removeMBF21Flag(value);
				}
				else if (matchIdentifierIgnoreCase(KEYWORD_MBF21))
				{
					if ((value = matchPositiveInteger()) != null)
					{
						thing.removeMBF21Flag(value);
					}
					else
					{
						addErrorMessage("Expected integer after \"+ mbf21\".");
						return false;
					}
				}
				else if ((value = matchPositiveInteger()) != null)
				{
					thing.removeFlag(value);
				}
				else
				{
					addErrorMessage("Expected integer after \"+\".");
					return false;
				}
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_STATE))
			{
				if (!parseThingStateClause(context, thing))
					return false;
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_STATES))
			{
				if (!parseThingStateBody(context, thing))
					return false;
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_CLEAR))
			{
				if (matchIdentifierIgnoreCase(KEYWORD_STATE))
				{
					String labelName;
					if ((labelName = matchIdentifier()) != null)
					{
						if (thing.getLabel(labelName) == 0)
						{
							addErrorMessage("Label \"%s\" is invalid or not declared at this moment. Expected one of: %s", labelName, Arrays.toString(thing.getLabels()));
							return false;
						}
						else
						{
							thing.setLabel(labelName, 0);
						}
					}
					else
					{
						addErrorMessage("Expected state label after '%s': %s", KEYWORD_STATE, Arrays.toString(thing.getLabels()));
						return false;
					}
				}
				else if (matchIdentifierIgnoreCase(KEYWORD_PROPERTIES))
				{
					thing.clearProperties();
				}
				else if (matchIdentifierIgnoreCase(KEYWORD_FLAGS))
				{
					thing.clearFlags();
				}
				else if (matchIdentifierIgnoreCase(KEYWORD_STATES))
				{
					thing.clearLabels();
				}
				else if (matchIdentifierIgnoreCase(KEYWORD_SOUNDS))
				{
					thing.clearSounds();
				}
				else
				{
					addErrorMessage("Expected '%s', '%s', or '%s' after '%s'.", KEYWORD_STATE, KEYWORD_STATES, KEYWORD_SOUNDS, KEYWORD_CLEAR);
					return false;
				}
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_EDNUM))
			{
				if ((value = matchInteger()) == null)
				{
					addErrorMessage("Expected integer after \"%s\".", KEYWORD_EDNUM);
					return false;
				}
				
				// bad or reserved ednums.
				if (value < -1 || value == 0 || value == 1 || value == 2 || value == 3 || value == 4 || value == 11)
				{
					addErrorMessage("The editor number %d is either invalid or reserved.", value);
					return false;
				}
				
				thing.setEditorNumber(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_HEALTH))
			{
				if ((value = matchInteger()) == null)
				{
					addErrorMessage("Expected integer after \"%s\".", KEYWORD_HEALTH);
					return false;
				}
				thing.setHealth(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_SPEED))
			{
				if ((value = matchInteger()) == null)
				{
					addErrorMessage("Expected integer after \"%s\".", KEYWORD_SPEED);
					return false;
				}
				thing.setSpeed(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_RADIUS))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_RADIUS);
					return false;
				}
				thing.setRadius(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_HEIGHT))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_HEIGHT);
					return false;
				}
				thing.setHeight(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_DAMAGE))
			{
				if ((value = matchInteger()) == null)
				{
					addErrorMessage("Expected integer after \"%s\".", KEYWORD_DAMAGE);
					return false;
				}
				thing.setDamage(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_REACTIONTIME))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_REACTIONTIME);
					return false;
				}
				thing.setReactionTime(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_PAINCHANCE))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_PAINCHANCE);
					return false;
				}
				thing.setPainChance(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_MASS))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_MASS);
					return false;
				}
				thing.setMass(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_FLAGS))
			{
				if ((value = (Integer)matchNumericExpression(context, true)) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_FLAGS);
					return false;
				}
				thing.setFlags(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_SEESOUND))
			{
				if ((value = matchSoundIndexName(context)) == null)
				{
					addErrorMessage("Expected sound name after \"%s\".", KEYWORD_SEESOUND);
					return false;
				}
				thing.setSeeSoundPosition(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_ATTACKSOUND))
			{
				if ((value = matchSoundIndexName(context)) == null)
				{
					addErrorMessage("Expected sound name after \"%s\".", KEYWORD_ATTACKSOUND);
					return false;
				}
				thing.setAttackSoundPosition(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_PAINSOUND))
			{
				if ((value = matchSoundIndexName(context)) == null)
				{
					addErrorMessage("Expected sound name after \"%s\".", KEYWORD_PAINSOUND);
					return false;
				}
				thing.setPainSoundPosition(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_DEATHSOUND))
			{
				if ((value = matchSoundIndexName(context)) == null)
				{
					addErrorMessage("Expected sound name after \"%s\".", KEYWORD_DEATHSOUND);
					return false;
				}
				thing.setDeathSoundPosition(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_ACTIVESOUND))
			{
				if ((value = matchSoundIndexName(context)) == null)
				{
					addErrorMessage("Expected sound name after \"%s\".", KEYWORD_ACTIVESOUND);
					return false;
				}
				thing.setActiveSoundPosition(value);
			}
			// ================= EXTENDED
			else if (matchIdentifierIgnoreCase(KEYWORD_DROPITEM))
			{
				if (!context.supports(DEHFeatureLevel.EXTENDED))
				{
					addErrorMessage("The \"%s\" property is not available. Not an EXTENDED patch.", KEYWORD_DROPITEM);
					return false;
				}
				
				if ((value = matchThingIndex(context)) == null)
				{
					addErrorMessage("Expected thing index after \"%s\".", KEYWORD_DROPITEM);
					return false;
				}
				thing.setDroppedItem(value);
			}
			// ================= MBF21
			else if (matchIdentifierIgnoreCase(KEYWORD_FASTSPEED))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("The \"%s\" property is not available. Not an MBF21 patch.", KEYWORD_FASTSPEED);
					return false;
				}
				
				if ((value = matchInteger()) == null)
				{
					addErrorMessage("Expected integer after \"%s\".", KEYWORD_FASTSPEED);
					return false;
				}
				thing.setFastSpeed(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_MELEERANGE))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("The \"%s\" property is not available. Not an MBF21 patch.", KEYWORD_MELEERANGE);
					return false;
				}
				
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_MELEERANGE);
					return false;
				}
				thing.setMeleeRange(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_RIPSOUND))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("The \"%s\" property is not available. Not an MBF21 patch.", KEYWORD_RIPSOUND);
					return false;
				}
				
				if ((value = matchSoundIndexName(context)) == null)
				{
					addErrorMessage("Expected sound name after \"%s\".", KEYWORD_RIPSOUND);
					return false;
				}
				thing.setRipSoundPosition(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_INFIGHTINGGROUP))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("The \"%s\" property is not available. Not an MBF21 patch.", KEYWORD_INFIGHTINGGROUP);
					return false;
				}
				
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_INFIGHTINGGROUP);
					return false;
				}
				thing.setInfightingGroup(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_PROJECTILEGROUP))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("The \"%s\" property is not available. Not an MBF21 patch.", KEYWORD_PROJECTILEGROUP);
					return false;
				}
				
				if ((value = matchInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_PROJECTILEGROUP);
					return false;
				}
				thing.setProjectileGroup(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_SPLASHGROUP))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("The \"%s\" property is not available. Not an MBF21 patch.", KEYWORD_SPLASHGROUP);
					return false;
				}
				
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_SPLASHGROUP);
					return false;
				}
				thing.setSplashGroup(value);
			}
			else
			{
				addErrorMessage("Expected '%s', '%s', or state block start.", KEYWORD_AMMOTYPE, KEYWORD_STATE);
				return false;
			}
		} // while

		if (!matchType(DecoHackKernel.TYPE_RBRACE))
		{
			addErrorMessage("Expected '}' after \"%s\" section.", KEYWORD_THING);
			return false;
		}
		
		// apply editor keys
		for (Map.Entry<String, String> entry : editorKeys.entrySet())
			thing.setEditorKey(entry.getKey(), entry.getValue());
		
		return true;
	}
	
	// Parses a thing state clause.
	private boolean parseThingStateClause(AbstractPatchContext<?> context, DEHThingTarget<?> thing) 
	{
		Integer value;
		if (matchIdentifierIgnoreCase(KEYWORD_THINGSTATE_SPAWN))
		{
			if ((value = parseStateIndex(context)) != null)
				thing.setSpawnFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_THINGSTATE_SEE))
		{
			if ((value = parseStateIndex(context)) != null)
				thing.setWalkFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_THINGSTATE_MELEE))
		{
			if ((value = parseStateIndex(context)) != null)
				thing.setMeleeFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_THINGSTATE_MISSILE))
		{
			if ((value = parseStateIndex(context)) != null)
				thing.setMissileFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_THINGSTATE_PAIN))
		{
			if ((value = parseStateIndex(context)) != null)
				thing.setPainChance(value);
			else
				return false;
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_THINGSTATE_DEATH))
		{
			if ((value = parseStateIndex(context)) != null)
				thing.setDeathFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_THINGSTATE_XDEATH))
		{
			if ((value = parseStateIndex(context)) != null)
				thing.setExtremeDeathFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_THINGSTATE_RAISE))
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
	private boolean parseThingStateBody(AbstractPatchContext<?> context, DEHThingTarget<?> thing)
	{
		if (!matchType(DecoHackKernel.TYPE_LBRACE))
		{
			addErrorMessage("Expected '{' after \"%s\" declaration.", KEYWORD_STATES);
			return false;
		}

		if (!parseActorStateSet(context, thing)) 
			return false;
		
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
		if ((slot = matchWeaponIndex(context)) == null)
			return false;
		
		// weapon swap
		if (matchIdentifierIgnoreCase(KEYWORD_SWAP))
		{
			if (!matchIdentifierIgnoreCase(KEYWORD_WITH))
			{
				addErrorMessage("Expected \"%s\" after \"%s\".", KEYWORD_WITH, KEYWORD_SWAP);
				return false;
			}

			Integer other;
			if ((other = matchThingIndex(context)) == null)
				return false;

			DEHWeapon temp = new DEHWeapon();
			temp.copyFrom(context.getWeapon(other));
			context.getWeapon(other).copyFrom(context.getWeapon(slot));
			context.getWeapon(slot).copyFrom(temp);
			return true;
		}
		// free states.
		else if (matchIdentifierIgnoreCase(KEYWORD_FREE))
		{
			if (matchIdentifierIgnoreCase(KEYWORD_STATES))
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

			if (matchIdentifierIgnoreCase(KEYWORD_WEAPONSTATE_SELECT))
				context.freeConnectedStates(weapon.getRaiseFrameIndex());
			else if (matchIdentifierIgnoreCase(KEYWORD_WEAPONSTATE_DESELECT))
				context.freeConnectedStates(weapon.getLowerFrameIndex());
			else if (matchIdentifierIgnoreCase(KEYWORD_WEAPONSTATE_READY))
				context.freeConnectedStates(weapon.getReadyFrameIndex());
			else if (matchIdentifierIgnoreCase(KEYWORD_WEAPONSTATE_FIRE))
				context.freeConnectedStates(weapon.getFireFrameIndex());
			else if (matchIdentifierIgnoreCase(KEYWORD_WEAPONSTATE_FLASH))
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
			DEHWeapon weapon = context.getWeapon(slot);
			
			if (matchType(DecoHackKernel.TYPE_COLON))
			{
				if (!matchIdentifierIgnoreCase(KEYWORD_WEAPON))
				{
					addErrorMessage("Expected \"%s\" after ':'.", KEYWORD_WEAPON);
					return false;
				}
				
				Integer sourceSlot;
				if ((sourceSlot = matchWeaponIndex(context)) == null)
					return false;

				weapon.copyFrom(context.getWeapon(sourceSlot));
			}
			
			if (currentType(DecoHackKernel.TYPE_STRING))
			{
				weapon.setName(matchString());
			}
			
			return parseWeaponBody(context, weapon);
		}
	}

	// Parses an "each weapon" block.
	private boolean parseWeaponEachBlock(final AbstractPatchContext<?> context)
	{
		if (matchIdentifierIgnoreCase(KEYWORD_IN))
		{
			return parseWeaponEachInBlock(context);
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_FROM))
		{
			return parseWeaponEachFromBlock(context);
		}
		else
		{
			addErrorMessage("Expected '%s' or '%s' after \"%s %s\" declaration.", KEYWORD_IN, KEYWORD_FROM, KEYWORD_EACH, KEYWORD_THING);
			return false;
		}
	}
	
	// Parses an "each weapon in" block.
	private boolean parseWeaponEachInBlock(final AbstractPatchContext<?> context)
	{
		List<Integer> weaponList = new LinkedList<>();
		
		if (!matchType(DecoHackKernel.TYPE_LPAREN))
		{
			addErrorMessage("Expected '(' after \"%s\".", KEYWORD_IN);
			return false;
		}

		while (currentType(DecoHackKernel.TYPE_NUMBER))
		{
			Integer id;
			if ((id = matchWeaponIndex(context)) == null)
				return false;

			weaponList.add(id);
			
			if (!matchType(DecoHackKernel.TYPE_COMMA))
				break;
		}
		
		if (!matchType(DecoHackKernel.TYPE_RPAREN))
		{
			addErrorMessage("Expected ')' after the list of weapons.");
			return false;
		}

		if (weaponList.isEmpty())
		{
			addErrorMessage("Expected at least one weapon in a list of weapons.");
			return false;
		}
		
		DEHWeaponTemplate template = new DEHWeaponTemplate();
		if (!parseWeaponBody(context, template))
			return false;
		
		for (Integer id : weaponList)
		{
			DEHWeapon weapon;
			if ((weapon = context.getWeapon(id)) == null)
			{
				addErrorMessage("INTERNAL ERROR. Weapon id %d invalid, but passed parsing.", id);
				return false;
			}
			
			template.applyTo(weapon);
		}

		return true;
	}
	
	// Parses an "each weapon from" block.
	private boolean parseWeaponEachFromBlock(final AbstractPatchContext<?> context)
	{
		Integer min;
		if ((min = matchWeaponIndex(context)) == null)
			return false;

		if (!matchIdentifierIgnoreCase(KEYWORD_TO))
		{
			addErrorMessage("Expected \"%s\" after starting weapon index.", KEYWORD_TO);
			return false;
		}

		Integer max;
		if ((max = matchWeaponIndex(context)) == null)
			return false;
		
		DEHWeaponTemplate template = new DEHWeaponTemplate();
		if (!parseWeaponBody(context, template))
			return false;
		
		int a = Math.min(min, max);
		int b = Math.max(min, max);
		while (a <= b)
		{
			int id = a++;
			
			DEHWeapon weapon;
			if ((weapon = context.getWeapon(id)) == null)
			{
				addErrorMessage("INTERNAL ERROR. Weapon id %d invalid, but passed parsing.", id);
				return false;
			}
			
			template.applyTo(weapon);
		}
		
		return true;
	}
	
	// Parses a weapon body.
	private boolean parseWeaponBody(AbstractPatchContext<?> context, DEHWeaponTarget<?> weapon)
	{
		if (!matchType(DecoHackKernel.TYPE_LBRACE))
		{
			addErrorMessage("Expected '{' after \"%s\" declaration.", KEYWORD_WEAPON);
			return false;
		}
		
		while (currentType(DecoHackKernel.TYPE_IDENTIFIER, DecoHackKernel.TYPE_PLUS, DecoHackKernel.TYPE_DASH))
		{
			if (matchType(DecoHackKernel.TYPE_PLUS))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("Weapon flags are not available. Not an MBF21 patch.");
					return false;
				}

				Integer flags;
				if ((flags = matchWeaponMBF21FlagMnemonic(context)) == null && (flags = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer after \"+\".");
					return false;
				}
				weapon.addMBF21Flag(flags);
			}
			else if (matchType(DecoHackKernel.TYPE_DASH))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("Weapon flags are not available. Not an MBF21 patch.");
					return false;
				}

				Integer flags;
				if ((flags = matchWeaponMBF21FlagMnemonic(context)) == null && (flags = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer after \"-\".");
					return false;
				}
				weapon.removeMBF21Flag(flags);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_STATE))
			{
				if (!parseWeaponStateClause(context, weapon))
					return false;
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_STATES))
			{
				if (!parseWeaponStateBody(context, weapon))
					return false;
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_CLEAR))
			{
				if (matchIdentifierIgnoreCase(KEYWORD_STATE))
				{
					String labelName;
					if ((labelName = matchIdentifier()) != null)
					{
						if (weapon.getLabel(labelName) == 0)
						{
							addErrorMessage("Label \"%s\" is invalid or not declared at this moment. Expected one of: %s", labelName, Arrays.toString(weapon.getLabels()));
							return false;
						}
						else
						{
							weapon.setLabel(labelName, 0);
						}
					}
					else
					{
						addErrorMessage("Expected state label after '%s': %s", KEYWORD_STATE, Arrays.toString(weapon.getLabels()));
						return false;
					}
				}
				else if (matchIdentifierIgnoreCase(KEYWORD_PROPERTIES))
				{
					weapon.clearProperties();
				}
				else if (matchIdentifierIgnoreCase(KEYWORD_STATES))
				{
					weapon.clearLabels();
				}
				else if (matchIdentifierIgnoreCase(KEYWORD_FLAGS))
				{
					if (!context.supports(DEHFeatureLevel.MBF21))
					{
						addErrorMessage("Can't clear flags. Not an MBF21 patch.");
						return false;
					}
					weapon.clearFlags();
				}
				else
				{
					addErrorMessage("Expected '%s' or '%s' after '%s'.", KEYWORD_STATE, KEYWORD_STATES, KEYWORD_CLEAR);
					return false;
				}
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_AMMOTYPE))
			{
				Ammo ammo;
				Integer ammoIndex;
				if ((ammoIndex = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected ammo type: an integer from 0 to %d, or 5.", context.getAmmoCount() - 1);
					return false;
				}
				else if (ammoIndex < 0 || ammoIndex > 5  || ammoIndex == 4)
				{
					addErrorMessage("Expected ammo type: an integer from 0 to %d, or 5.", context.getAmmoCount() - 1);
					return false;
				}
				else
				{
					ammo = Ammo.VALUES[ammoIndex];
				}

				weapon.setAmmoType(ammo);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_AMMOPERSHOT))
			{
				Integer ammoPerShot;
				if ((ammoPerShot = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected ammo per shot: a positive integer.");
					return false;
				}
				else
				{
					weapon.setAmmoPerShot(ammoPerShot);
				}
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_FLAGS))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("The \"%s\" property is not available. Not an MBF21 patch.", KEYWORD_FLAGS);
					return false;
				}
				
				if (!matchIdentifierIgnoreCase(KEYWORD_MBF21))
				{
					addErrorMessage("Expected \"%s\" after \"%s\".", KEYWORD_MBF21, KEYWORD_FLAGS);
					return false;
				}
				
				Integer flags;
				if ((flags = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_FLAGS);
					return false;
				}
				else
				{
					weapon.setMBF21Flags(flags);
				}
			}
			else
			{
				addErrorMessage("Expected '%s', '%s', '%s', or state block start.", KEYWORD_AMMOTYPE, KEYWORD_AMMOPERSHOT, KEYWORD_STATE);
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

	private boolean parseWeaponStateClause(AbstractPatchContext<?> context, DEHWeaponTarget<?> weapon) 
	{
		Integer value;
		if (matchIdentifierIgnoreCase(KEYWORD_WEAPONSTATE_READY))
		{
			if ((value = parseStateIndex(context)) != null)
				weapon.setReadyFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_WEAPONSTATE_SELECT))
		{
			if ((value = parseStateIndex(context)) != null)
				weapon.setRaiseFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_WEAPONSTATE_DESELECT))
		{
			if ((value = parseStateIndex(context)) != null)
				weapon.setLowerFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_WEAPONSTATE_FIRE))
		{
			if ((value = parseStateIndex(context)) != null)
				weapon.setFireFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_WEAPONSTATE_FLASH))
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
	private boolean parseWeaponStateBody(AbstractPatchContext<?> context, DEHWeaponTarget<?> weapon)
	{
		if (!matchType(DecoHackKernel.TYPE_LBRACE))
		{
			addErrorMessage("Expected '{' after \"%s\" declaration.", KEYWORD_STATES);
			return false;
		}

		if (!parseActorStateSet(context, weapon)) 
			return false;
		
		if (!matchType(DecoHackKernel.TYPE_RBRACE))
		{
			addErrorMessage("Expected '}' after \"%s\" section.", KEYWORD_STATES);
			return false;
		}
		
		return true;
	}

	/* ***************************************************************************************** */
	/*                                START STATE BODY PARSER                                    */
	/* ***************************************************************************************** */

	// Parses an actor's state body.
	private boolean parseActorStateSet(AbstractPatchContext<?> context, DEHActor<?> actor)
	{
		// state label.
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			addErrorMessage("Expected state label.");
			return false;
		}
		
		FutureLabels futureLabels = new FutureLabels();
		LinkedList<String> labelList = new LinkedList<>();
		ParsedState parsed = new ParsedState();
		StateFillCursor stateCursor = new StateFillCursor();
		
		while (currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			labelList.add(currentToken().getLexeme());
			nextToken();
			
			if (!matchType(DecoHackKernel.TYPE_COLON))
			{
				addErrorMessage("Expected ':' after state label.");
				return false;
			}

			Integer startIndex;
			Integer loopIndex = null;
			if (currentIsSpriteIndex(context))
			{
				do {
					parsed.reset();
					if (!parseStateLine(context, actor, parsed))
						return false;
					if ((startIndex = fillStates(context, futureLabels, parsed, stateCursor, false)) == null)
						return false;
					if (loopIndex == null)
						loopIndex = startIndex;
					while (!labelList.isEmpty())
						futureLabels.backfill(context, actor, labelList.pollFirst(), startIndex);
				} while (currentIsSpriteIndex(context));
			}
			
			// Parse next state.
			if (currentIsNextStateKeyword())
			{
				if (stateCursor.lastStateFilled == null)
				{
					if (matchIdentifierIgnoreCase(KEYWORD_GOTO))
					{
						Object index;
						if ((index = parseStateIndex(context, actor)) == null)
							return false;
						else if (index instanceof String)
						{
							while (!labelList.isEmpty())
								futureLabels.addAlias(labelList.pollFirst(), (String)index);
						}
						else while (!labelList.isEmpty())
						{
							futureLabels.backfill(context, actor, labelList.pollFirst(), (Integer)index);
						}
					}
					else if (matchIdentifierIgnoreCase(KEYWORD_STOP))
					{
						while (!labelList.isEmpty())
							actor.setLabel(labelList.pollFirst(), 0);
					}
					else
					{
						addErrorMessage("Expected a state definition after label, or a \"%s\" clause, or \"%s\".", KEYWORD_GOTO, KEYWORD_STOP);
						return false;
					}
				}
				// A previous state was filled.
				else
				{
					Object nextStateIndex = null;
					if ((nextStateIndex = parseNextStateIndex(context, actor, loopIndex, stateCursor.lastIndexFilled)) == null)
					{
						addErrorMessage("Expected next state clause (%s, %s, %s, %s).", KEYWORD_STOP, KEYWORD_WAIT, KEYWORD_LOOP, KEYWORD_GOTO);
						return false;
					}
					else if (nextStateIndex instanceof Integer)
					{
						stateCursor.lastStateFilled.setNextStateIndex((Integer)nextStateIndex);
					}
					else // String
					{
						futureLabels.addStateField(stateCursor.lastIndexFilled, FieldType.NEXTSTATE, (String)nextStateIndex);
					}
					stateCursor.lastStateFilled = null;
				}
			}
		}
		
		// Handle all actor labels un-backfilled. Error out if any left.
		if (futureLabels.hasLabels())
		{
			List<String> unknownLabels = new LinkedList<>();
			for (String label : futureLabels.getLabels())
			{
				if (actor.hasLabel(label))
					futureLabels.backfill(context, actor, label, actor.getLabel(label));
				else
					unknownLabels.add(label);
			}
			
			if (!unknownLabels.isEmpty())
			{
				addErrorMessage("Labels on this actor were referenced and not defined: %s", Arrays.toString(unknownLabels.toArray(new String[unknownLabels.size()])));
				return false;
			}
		}
		
		return true;
	}
	
	// Parses a sequence of auto-fill states.
	private boolean parseStateFillSequence(AbstractPatchContext<?> context, int startIndex)
	{
		if (!context.isFreeState(startIndex))
		{
			addErrorMessage("Starting state index for state fill, %d, is not a free state.", startIndex);
			return false;
		}
		if (context.isProtectedState(startIndex))
		{
			addErrorMessage("Starting state index for state fill, %d, is a protected state.", startIndex);
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
			if (!parseStateLine(context, null, parsed))
				return false;
			if (fillStates(context, EMPTY_LABELS, parsed, stateCursor, first) == null)
				return false;
			first = false;
		} while (currentIsSpriteIndex(context));
		
		// Parse end.
		Integer nextStateIndex = null;
		if ((nextStateIndex = parseNextStateIndex(context, startIndex, stateCursor.lastIndexFilled)) == null)
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
		DEHState state = context.getState(index);
	
		Integer nextStateIndex = null;
		if ((nextStateIndex = parseNextStateIndex(context, null, index)) != null)
		{
			state.setNextStateIndex(nextStateIndex);
			context.setFreeState(index, false);
			return true;
		}
		
		boolean notModified = true;
		
		if (currentIsSpriteIndex(context))
		{
			ParsedState parsedState = new ParsedState();
	
			boolean isBoom = context.supports(DEHFeatureLevel.BOOM);			
			Integer pointerIndex = context.getStateActionPointerIndex(index);
			if (!parseStateLine(context, null, parsedState, true, isBoom ? null : pointerIndex != null))
				return false;
	
			ParsedAction parsedAction = parsedState.parsedActions.get(0);
			
			if (isBoom)
			{
				if (pointerIndex != null && parsedAction.pointer == null)
					parsedAction.pointer = DEHActionPointer.NULL;
			}
			else if ((pointerIndex == null && parsedAction.pointer != null) || (pointerIndex != null && parsedAction.pointer == null))
			{
				if (parsedAction.pointer != null)
					addErrorMessage("Action function specified for state without a function!");
				else
					addErrorMessage("Action function not specified for state with a function!");
				return false;
			}
	
			if (pointerIndex != null)
				context.setActionPointer(pointerIndex, parsedAction.pointer);
			
			// fill state.
			state
				.setSpriteIndex(parsedState.spriteIndex)
				.setFrameIndex(parsedState.frameList.pollFirst())
				.setDuration(parsedState.duration)
				.setBright(parsedState.bright)
				.setMisc1(parsedAction.misc1)
				.setMisc2(parsedAction.misc2)
				.setArgs(parsedAction.args)
			;
			if (parsedState.mbf21Flags != null && state.getMBF21Flags() != parsedState.mbf21Flags)
				state.setMBF21Flags(parsedState.mbf21Flags);
	
			// Try to parse next state clause.
			nextStateIndex = parseNextStateIndex(context, null, index);
			if (nextStateIndex != null)
				state.setNextStateIndex(nextStateIndex);
			
			context.setFreeState(index, false);
			return true;
		}
		else while (currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			if (matchIdentifierIgnoreCase(KEYWORD_SPRITENAME))
			{
				Integer value;
				if ((value = matchSpriteIndexName(context)) == null)
				{
					addErrorMessage("Expected valid sprite name after \"%s\".", KEYWORD_SPRITENAME);
					return false;				
				}
				
				state.setSpriteIndex(value);
				notModified = false;
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_FRAME))
			{
				Deque<Integer> value;
				if ((value = matchFrameIndices()) == null)
				{
					addErrorMessage("Expected valid frame characters after \"%s\".", KEYWORD_FRAME);
					return false;				
				}
				
				if (value.size() > 1)
				{
					addErrorMessage("Expected a single frame character after \"%s\".", KEYWORD_FRAME);
					return false;				
				}
				
				state.setFrameIndex(value.pollFirst());
				notModified = false;
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_DURATION))
			{
				Integer value;
				if ((value = matchInteger()) == null)
				{
					addErrorMessage("Expected integer after \"%s\".", KEYWORD_DURATION);
					return false;				
				}
				
				state.setDuration(value);
				notModified = false;
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_NEXTSTATE))
			{
				Integer value;
				if ((value = parseStateIndex(context)) == null)
				{
					addErrorMessage("Expected valid state index clause after \"%s\".", KEYWORD_NEXTSTATE);
					return false;				
				}
				
				state.setNextStateIndex(value);
				notModified = false;
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_POINTER))
			{
				boolean isBoom = context.supports(DEHFeatureLevel.BOOM);			
				Integer pointerIndex = context.getStateActionPointerIndex(index);
				ParsedAction action = new ParsedAction();
				Boolean requireAction = isBoom ? null : pointerIndex != null;
	
				if (matchIdentifierIgnoreCase(KEYWORD_NULL))
				{
					if (requireAction != null && requireAction)
					{
						addErrorMessage("Expected an action pointer for this state.");
						return false;
					}
					else
					{
						action.pointer = DEHActionPointer.NULL;
					}
				}
				else if (!parseActionClause(context, null, action, requireAction))
				{
					return false;
				}
	
				if (isBoom && pointerIndex != null && action.pointer == null)
					action.pointer = DEHActionPointer.NULL;
				
				if (pointerIndex != null)
					context.setActionPointer(pointerIndex, action.pointer);
	
				state
					.setMisc1(action.misc1)
					.setMisc2(action.misc2)
					.setArgs(action.args)
				;
				
				notModified = false;
			}
			else if (currentIdentifierIgnoreCase(KEYWORD_OFFSET))
			{
				ParsedAction action = new ParsedAction();
				if (!parseOffsetClause(action))
					return false;
				
				state
					.setMisc1(action.misc1)
					.setMisc2(action.misc2)
				;
				
				notModified = false;
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_STATE_BRIGHT))
			{
				state.setBright(true);
				notModified = false;
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_STATE_NOTBRIGHT))
			{
				state.setBright(false);
				notModified = false;
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_STATE_FAST))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("MBF21 state flags (e.g. \"%s\") are not available. Not an MBF21 patch.", KEYWORD_STATE_FAST);
					return false;
				}
				
				state.setMBF21Flags(state.getMBF21Flags() | DEHStateFlag.SKILL5FAST.getValue());
				notModified = false;
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_STATE_NOTFAST))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("MBF21 state flags (e.g. \"%s\") are not available. Not an MBF21 patch.", KEYWORD_STATE_NOTFAST);
					return false;
				}
	
				state.setMBF21Flags(state.getMBF21Flags() & ~DEHStateFlag.SKILL5FAST.getValue());
				notModified = false;
			}
			else
			{
				break;
			}
		}
		
		if (notModified)
		{
			addErrorMessage("Expected valid sprite name, property, or next state clause (goto, stop, wait).");
			return false;
		}
		
		context.setFreeState(index, false);
		return true;
	}

	// Parse a single state and if true is returned, the input state is altered.
	// requireAction is either true, false, or null. If null, no check is performed. 
	private boolean parseStateLine(AbstractPatchContext<?> context, DEHActor<?> actor, ParsedState state)
	{
		return parseStateLine(context, actor, state, false, null);
	}

	// Parse a single state and if true is returned, the input state is altered.
	// requireAction is either true, false, or null. If null, no check is performed. 
	private boolean parseStateLine(AbstractPatchContext<?> context, DEHActor<?> actor, ParsedState state, boolean singleFrame, Boolean requireAction) 
	{
		if ((state.spriteIndex = matchSpriteIndexName(context)) == null)
		{
			addErrorMessage("Expected valid sprite name.");
			return false;				
		}
		
		if ((state.frameList = matchFrameIndices()) == null)
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
	
		if (!matchStateFlags(context, state))
			return false;
		
		ParsedAction action;
		if (matchType(DecoHackKernel.TYPE_LBRACE))
		{
			if (singleFrame || (requireAction != null))
			{
				addErrorMessage("You cannot specify many action pointers for a single frame.");
				return false;
			}
			
			while (currentType(DecoHackKernel.TYPE_IDENTIFIER))
			{
				state.parsedActions.add(action = new ParsedAction());
				if (!parseActionClause(context, actor, action, null))
					return false;				
			}
			
			if (!matchType(DecoHackKernel.TYPE_RBRACE))
			{
				addErrorMessage("Expected a '}' to close an action pointer list.");
				return false;
			}
			else if (state.parsedActions.isEmpty())
			{
				// create dummy action.
				state.parsedActions.add(action = new ParsedAction());
			}
		}
		else
		{
			state.parsedActions.add(action = new ParsedAction());
			if (!parseOffsetClause(action))
				return false;
			if (!parseActionClause(context, actor, action, requireAction))
				return false;
		}
		
		return true;
	}

	// Parses a mandatory state index.
	private Integer parseStateIndex(AbstractPatchContext<?> context)
	{
		return (Integer)parseStateIndex(context, null);
	}

	// Parses a mandatory state index.
	private Object parseStateIndex(AbstractPatchContext<?> context, DEHActor<?> actor)
	{
		if (matchIdentifierIgnoreCase(KEYWORD_THING))
			return parseThingStateIndex(context);
		else if (matchIdentifierIgnoreCase(KEYWORD_WEAPON))
			return parseWeaponStateIndex(context);
		else if (currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			if (actor == null)
			{
				addErrorMessage("Label '%s' was unexpected. State declaration not for a thing or weapon.", currentToken().getLexeme());
				return null;
			}
			return matchIdentifier();
		}
		else
			return matchStateIndex(context);
	}

	// Parses a weapon state index.
	private Integer parseWeaponStateIndex(AbstractPatchContext<?> context)
	{
		String labelName;
		Integer index;
		if ((index = matchWeaponIndex(context)) == null)
			return null;
		
		DEHWeapon weapon = context.getWeapon(index);
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			addErrorMessage("Expected weapon label name.");
			return null;
		}
		
		labelName = currentToken().getLexeme();

		Integer stateIndex;
		if ((stateIndex = weapon.getLabel(labelName)) == 0)
		{
			String[] labels = weapon.getLabels();
			StringBuilder sb;
			if (labels.length == 0)
			{
				sb = new StringBuilder("Expected a valid thing state label for weapon ");
				sb.append(index);
				sb.append(", but it has no state labels. It may be stateless or undefined at this point.");
			}
			else
			{
				sb = new StringBuilder("Expected a valid thing state label for weapon ");
				sb.append(index).append(": ");
				sb.append(Arrays.toString(labels));
				sb.append(".");
			}
			addErrorMessage(sb.toString());
			return null;
		}

		nextToken();

		return stateIndex;
	}

	// Parses a thing state index.
	private Integer parseThingStateIndex(AbstractPatchContext<?> context) 
	{
		String labelName;
		Integer index;
		if ((index = matchThingIndex(context)) == null)
			return null;
		
		DEHThing thing = context.getThing(index);
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			addErrorMessage("Expected thing label name.");
			return null;
		}
		
		labelName = currentToken().getLexeme();

		Integer stateIndex;
		if ((stateIndex = thing.getLabel(labelName)) == 0)
		{
			String[] labels = thing.getLabels();
			StringBuilder sb;
			if (labels.length == 0)
			{
				sb = new StringBuilder("Expected a valid thing state label for thing ");
				sb.append(index);
				sb.append(", but it has no state labels. It may be stateless or undefined at this point.");
			}
			else
			{
				sb = new StringBuilder("Expected a valid thing state label for thing ");
				sb.append(index).append(": ");
				sb.append(Arrays.toString(labels));
				sb.append(".");
			}
			addErrorMessage(sb.toString());
			return null;
		}
		
		nextToken();
		
		return stateIndex;
	}
	
	// Parses a sound index.
	private Integer parseSoundIndex(AbstractPatchContext<?> context)
	{
		Integer value;
		if ((value = matchSoundIndexName(context)) == null)
		{
			addErrorMessage("Expected a valid sound name after '%s'.", KEYWORD_SOUND);
			return null;
		}
		return value;
	}
	
	// Parses and Offset clause.
	private boolean parseOffsetClause(ParsedAction parsedAction)
	{
		if (matchIdentifierIgnoreCase(KEYWORD_OFFSET))
		{
			parsedAction.offset = true;
			
			if (matchType(DecoHackKernel.TYPE_LPAREN))
			{
				// get first argument
				Integer p;
				if ((p = matchInteger()) == null)
				{
					addErrorMessage("Expected integer for X offset value.");
					return false;
				}
				parsedAction.misc1 = p;

				if (!matchType(DecoHackKernel.TYPE_COMMA))
				{
					addErrorMessage("Expected a ',' after X offset; both X and Y offsets must be defined.");
					return false;
				}

				if ((p = matchInteger()) == null)
				{
					addErrorMessage("Expected integer for Y offset value.");
					return false;
				}
				parsedAction.misc2 = p;

				if (!matchType(DecoHackKernel.TYPE_RPAREN))
				{
					addErrorMessage("Expected a ')' after offsets.");
					return false;
				}
			}
			else
			{
				addErrorMessage("Expected a '(' after \"offset\".");
				return false;
			}
		}
		
		return true;
	}

	// Parse an action into a parsed action.
	private boolean parseActionClause(AbstractPatchContext<?> context, DEHActor<?> actor, ParsedAction action, Boolean requireAction) 
	{
		// Maybe parse action
		DEHActionPointer pointer = matchActionPointerName();
		action.pointer = pointer;
		
		if (requireAction != null)
		{
			if (requireAction && pointer == null)
			{
				addErrorMessage("Expected an action pointer for this state.");
				return false;				
			}
			if (!requireAction && pointer != null)
			{
				addErrorMessage("Expected no action pointer for this state. State definition attempted to set one.");
				return false;				
			}
		}

		if (pointer != null)
		{
			if (!context.supports(pointer.getType()))
			{
				addErrorMessage(pointer.getType().name() + " action pointer used: " + pointer.getMnemonic() + ". Patch does not support this action type.");
				return false;
			}
			
			if (actor instanceof DEHThing)
			{
				if (pointer.isWeapon())
				{
					addErrorMessage("Action pointer " + pointer.getMnemonic() + " is a weapon action. Thing action expected.");
					return false;
				}
			}
			else if (actor instanceof DEHWeapon)
			{
				if (!pointer.isWeapon())
				{
					addErrorMessage("Action pointer " + pointer.getMnemonic() + " is a thing action. Weapon action expected.");
					return false;
				}				
			}
			// else, state body.

			// MBF args (misc1/misc2)
			if (!pointer.useArgs())
			{
				if (matchType(DecoHackKernel.TYPE_LPAREN))
				{
					// no arguments
					if (matchType(DecoHackKernel.TYPE_RPAREN))
						return true;

					if (action.offset)
					{
						addErrorMessage("Cannot use 'offset' directive on a state with an MBF action function parameter.");
						return false;
					}

					// get first argument
					Object p;
					if ((p = parseActionPointerParameterValue(context, actor)) == null)
						return false;
					else if (p instanceof Integer)
					{
						if (!checkActionParamValue(pointer, 0, (Integer)p))
							return false;
						action.misc1 = (Integer)p;
					}
					else
					{
						action.labelFields.add(new FieldSet(FieldType.MISC1, (String)p));
						action.misc1 = PLACEHOLDER_LABEL;
					}


					if (matchType(DecoHackKernel.TYPE_COMMA))
					{
						if ((p = parseActionPointerParameterValue(context, actor)) == null)
							return false;
						else if (p instanceof Integer)
						{
							if (!checkActionParamValue(pointer, 1, (Integer)p))
								return false;
							action.misc2 = (Integer)p;
						}
						else
						{
							action.labelFields.add(new FieldSet(FieldType.MISC2, (String)p));
							action.misc2 = PLACEHOLDER_LABEL;
						}
					}

					if (!matchType(DecoHackKernel.TYPE_RPAREN))
					{
						addErrorMessage("Expected a ')' after action parameters.");
						return false;
					}
				}
			}

			// MBF21 args
			else
			{
				if (matchType(DecoHackKernel.TYPE_LPAREN))
				{
					// no arguments
					if (matchType(DecoHackKernel.TYPE_RPAREN))
					{
						return true;
					}

					boolean done = false;
					while (!done)
					{
						// get argument
						int argIndex = action.args.size();
						Object p;
						if ((p = parseActionPointerParameterValue(context, actor)) == null)
							return false;
						else if (p instanceof Integer)
						{
							if (!checkActionParamValue(pointer, argIndex, (Integer)p))
								return false;
							action.args.add((Integer)p);
						}
						else
						{
							action.labelFields.add(new FieldSet(FieldType.getArg(argIndex), (String)p));
							action.args.add(PLACEHOLDER_LABEL);
						}

						if (matchType(DecoHackKernel.TYPE_RPAREN))
						{
							done = true;
						}
						else if (!matchType(DecoHackKernel.TYPE_COMMA))
						{
							addErrorMessage("Expected a ')' after action parameters.");
							return false;
						}
					}
				}
			}
		}
		
		return true;
	}

	// Parses a pointer argument value.
	private Object parseActionPointerParameterValue(AbstractPatchContext<?> context, DEHActor<?> actor)
	{
		Object value;
		if (matchIdentifierIgnoreCase(KEYWORD_THING))
			return parseThingStateIndex(context);
		else if (matchIdentifierIgnoreCase(KEYWORD_WEAPON))
			return parseWeaponStateIndex(context);
		else if (matchIdentifierIgnoreCase(KEYWORD_SOUND))
			return parseSoundIndex(context);
		else if (matchIdentifierIgnoreCase(KEYWORD_FLAGS))
			return matchNumericExpression(context, true);
		else if ((value = matchNumericExpression(context, false)) != null)
		{
			if (value instanceof String)
			{
				String labelName = (String)value;
				if (actor != null)
				{
					if (actor.hasLabel(labelName))
						return actor.getLabel(labelName);
					else
						return labelName;
				}
				else
				{
					addErrorMessage("Expected valid parameter value.");
					return null;
				}				
			}
			else
			{
				return (Integer)value;
			}
		}
		else
		{
			addErrorMessage("Expected parameter.");
			return null;
		}
	}

	// Parses a next state line.
	private Integer parseNextStateIndex(AbstractPatchContext<?> context, Integer lastLabelledStateIndex, int currentStateIndex)
	{
		return (Integer)parseNextStateIndex(context, null, lastLabelledStateIndex, currentStateIndex);
	}
	
	// Parses a next state line.
	private Object parseNextStateIndex(AbstractPatchContext<?> context, DEHActor<?> actor, Integer lastLabelledStateIndex, int currentStateIndex)
	{
		// Test for only next state clause.
		if (matchIdentifierIgnoreCase(KEYWORD_STOP))
		{
			return 0;
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_WAIT))
		{
			return currentStateIndex;
		}
		else if (currentIdentifierIgnoreCase(KEYWORD_LOOP))
		{
			if (lastLabelledStateIndex == null)
			{
				addErrorMessage("Can't use \"%s\" with no declared state labels.", KEYWORD_LOOP);
				return null;
			}
			nextToken();
			return lastLabelledStateIndex;
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_GOTO))
		{
			return parseStateIndex(context, actor);
		}
		else
		{
			return null;
		}
	}
	
	// Attempts to fill states from a starting index.
	// If forceFirst is true, the state index filled MUST be cursor.lastFilledIndex. 
	// Returns the FIRST INDEX FILLED or null if error.
	private Integer fillStates(AbstractPatchContext<?> context, FutureLabels labels, ParsedState state, StateFillCursor cursor, boolean forceFirst)
	{
		Integer out = null;
		boolean isBoom = context.supports(DEHFeatureLevel.BOOM);
		
		while (!state.frameList.isEmpty())
		{
			Integer frame = state.frameList.pollFirst();
			
			for (int i = 0; i < state.parsedActions.size(); i++)
			{
				ParsedAction parsedAction = state.parsedActions.get(i);
				
				Integer currentIndex;
				if ((currentIndex = searchNextState(context, parsedAction, cursor)) == null)
					return null;
				if (out == null)
					out = currentIndex;
				
				if (!isBoom && forceFirst && currentIndex != cursor.lastIndexFilled)
				{
					addErrorMessage("Provided state definition would not fill state " + cursor.lastIndexFilled + ". " + (
						parsedAction.pointer != null 
							? "State " + cursor.lastIndexFilled + " cannot have an action pointer."
							: "State " + cursor.lastIndexFilled + " must have an action pointer."
					));
					return null;
				}
				
				if (cursor.lastStateFilled != null)
					cursor.lastStateFilled.setNextStateIndex(currentIndex);
		
				DEHState fillState = context.getState(currentIndex);
				
				if (labels != null)
					parsedAction.dumpLabels(currentIndex, labels);
				
				fillState
					.setSpriteIndex(state.spriteIndex)
					.setFrameIndex(frame)
					.setDuration(i == state.parsedActions.size() - 1 ? state.duration : 0) // only write duration to last state.
					.setBright(state.bright)
					.setMisc1(parsedAction.misc1)
					.setMisc2(parsedAction.misc2)
					.setArgs(parsedAction.args)
				;
				cursor.lastStateFilled = fillState;
		
				Integer pointerIndex = context.getStateActionPointerIndex(currentIndex);
				
				if (isBoom && pointerIndex != null && parsedAction.pointer == null)
					parsedAction.pointer = DEHActionPointer.NULL;
				
				if (pointerIndex != null)
					context.setActionPointer(pointerIndex, parsedAction.pointer);
				
				context.setFreeState(currentIndex, false);
				cursor.lastIndexFilled = currentIndex;
				forceFirst = false;
			}
		}
		
		if (out == null)
		{
			addErrorMessage("INTERNAL ERROR: Nothing to fill.");
			return null;
		}
		
		return out;
	}
	
	// Searches for the next suitable free state for a parsed action.
	// Return null if none found.
	private Integer searchNextState(AbstractPatchContext<?> context, ParsedAction parsed, StateFillCursor cursor) 
	{
		boolean isBoom = context.supports(DEHFeatureLevel.BOOM);
		
		Integer index = isBoom 
			? context.findNextFreeState(cursor.lastIndexFilled)
			: (parsed.pointer != null 
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
				if (parsed.pointer != null)
					addErrorMessage("No more free states with an action pointer.");
				else
					addErrorMessage("No more free states without an action pointer.");
			}
		}
		return index;
	}
	
	/* ***************************************************************************************** */
	/*                                 END STATE BODY PARSER                                     */
	/* ***************************************************************************************** */
	
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

	// Checks if the current token is an identifier with a specific lexeme.
	private boolean currentIdentifierIgnoreCase(String lexeme)
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
			return false;
		if (!currentToken().getLexeme().equalsIgnoreCase(lexeme))
			return false;
		return true;
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

	// Matches a valid state index number.
	// If match, advance token and return integer.
	// Else, return null.
	private Integer matchStateIndex(AbstractPatchContext<?> context)
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
		else if (context.getState(value) == null)
		{
			addErrorMessage("Invalid state index: %d. Max is %d.", value, context.getStateCount() - 1);
			return null;
		}
		else
		{
			return value;
		}
	}
	
	// Matches a valid thing index number.
	// If match, advance token and return integer.
	// Else, return null.
	private Integer matchThingIndex(AbstractPatchContext<?> context)
	{
		Integer slot;
		if ((slot = matchPositiveInteger()) == null)
		{
			addErrorMessage("Expected positive integer for the thing slot number.");
			return null;
		}
		
		return verifyThingIndex(context, slot);
	}
	
	// Verifies a valid thing index number.
	private Integer verifyThingIndex(AbstractPatchContext<?> context, int slot)
	{
		if (slot == 0)
		{
			addErrorMessage("Invalid thing index: %d.", slot);
			return null;
		}

		if (slot >= context.getThingCount())
		{
			addErrorMessage("Invalid thing index: %d. Max is %d.", slot, context.getThingCount() - 1);
			return null;
		}

		if (context.getThing(slot) == null)
		{
			addErrorMessage("Invalid thing index: %d. Max is %d.", slot, context.getThingCount() - 1);
			return null;
		}

		return slot;
	}

	// Matches a valid weapon index number.
	// If match, advance token and return integer.
	// Else, return null.
	private Integer matchWeaponIndex(AbstractPatchContext<?> context) 
	{
		Integer slot;
		if ((slot = matchPositiveInteger()) == null)
		{
			addErrorMessage("Expected positive integer after \"%s\" for the weapon slot number.", KEYWORD_WEAPON);
			return null;
		}

		return verifyWeaponIndex(context, slot);
	}

	// Verifies a valid weapon index number.
	private Integer verifyWeaponIndex(AbstractPatchContext<?> context, int slot)
	{
		if (slot >= context.getWeaponCount())
		{
			addErrorMessage("Invalid weapon index: %d. Max is %d.", slot, context.getWeaponCount() - 1);
			return null;
		}
		
		if (context.getWeapon(slot) == null)
		{
			addErrorMessage("Invalid weapon index: %d. Max is %d.", slot, context.getWeaponCount() - 1);
			return null;
		}

		return slot;
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
	private boolean matchIdentifierIgnoreCase(String lexeme)
	{
		if (!currentIdentifierIgnoreCase(lexeme))
			return false;
		nextToken();
		return true;
	}

	// Matches an identifier or string that references a sprite name.
	// If match, advance token and return sprite index integer.
	// Else, return null.
	private Integer matchSpriteIndexName(DEHPatch patch)
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
	private Deque<Integer> matchFrameIndices()
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
			return null;

		Deque<Integer> frameList = new LinkedList<>();
		String lexeme = currentToken().getLexeme();
		for (int i = 0; i < lexeme.length(); i++)
		{
			char c = lexeme.charAt(i);
			if (c < 'A' || c > ']')
			{
				addErrorMessage("Subframe list contains an invalid character: " + c + " Expected A through ].");
				return null;
			}
			frameList.add(c - 'A');
		}
		nextToken();
		return frameList;
	}
	
	// Matches a series of state flags (including "bright").
	// If match, advance token and return true plus modified out list.
	// Else, return null.
	private boolean matchStateFlags(AbstractPatchContext<?> context, ParsedState state)
	{
		while (currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			if (matchIdentifierIgnoreCase(KEYWORD_STATE_BRIGHT))
			{
				state.bright = true;
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_STATE_FAST))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("MBF21 state flags (e.g. \"%s\") are not available. Not an MBF21 patch.", KEYWORD_STATE_FAST);
					return false;
				}
				state.mbf21Flags = state.mbf21Flags != null ? state.mbf21Flags : 0;
				state.mbf21Flags |= DEHStateFlag.SKILL5FAST.getValue();
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_STATE_NOTFAST))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("MBF21 state flags (e.g. \"%s\") are not available. Not an MBF21 patch.", KEYWORD_STATE_NOTFAST);
					return false;
				}
				state.mbf21Flags = state.mbf21Flags != null ? state.mbf21Flags : 0;
				state.mbf21Flags &= ~DEHStateFlag.SKILL5FAST.getValue();
			}
			else
			{
				break;
			}
		}
		
		return true;
	}
	
	// Matches an identifier that references a thing flag mnemonic.
	// If match, advance token and return bitflags.
	// Else, return null.
	private Integer matchThingFlagMnemonic()
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
			return null;

		Integer out = null;
		DEHThingFlag flag;
		if ((flag = DEHThingFlag.getByMnemonic(currentToken().getLexeme())) != null)
		{
			out = flag.getValue();
			nextToken();
		}
		
		return out;
	}
	
	// Matches an identifier that references an MBF thing flag mnemonic.
	// If match, advance token and return bitflags.
	// Else, return null.
	private Integer matchThingMBF21FlagMnemonic(AbstractPatchContext<?> context)
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
			return null;

		if (!context.supports(DEHFeatureLevel.MBF21))
		{
			addErrorMessage("MBF21 thing flags are not available. Not an MBF21 patch.");
			return null;
		}

		Integer out = null;
		DEHThingMBF21Flag flag;
		if ((flag = DEHThingMBF21Flag.getByMnemonic(currentToken().getLexeme())) != null)
		{
			out = flag.getValue();
			nextToken();
		}
		
		return out;
	}
	
	// Matches an identifier that references a weapon flag mnemonic. 
	// If match, advance token and return bitflags. 
	// Else, return null.
	private Integer matchWeaponMBF21FlagMnemonic(AbstractPatchContext<?> context)
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
			return null;
		
		if (!context.supports(DEHFeatureLevel.MBF21))
		{
			addErrorMessage("MBF21 weapon flags are not available. Not an MBF21 patch.");
			return null;
		}

		Integer out = null;
		DEHWeaponMBF21Flag flag;
		if ((flag = DEHWeaponMBF21Flag.getByMnemonic(currentToken().getLexeme())) != null)
		{
			out = flag.getValue();
			nextToken();
		}
		
		return out;
	}

	// Matches an identifier or string that references an action pointer name.
	// If match, advance token and return action pointer.
	// Else, return null.
	private DEHActionPointer matchActionPointerName()
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
			return null;
	
		String lexeme = currentToken().getLexeme();
		DEHActionPointer out;
		if (lexeme.length() < 2 || !lexeme.substring(0, 2).toUpperCase().startsWith("A_"))
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
	private Integer matchSoundIndexName(DEHPatch patch)
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER, DecoHackKernel.TYPE_STRING))
			return null;
		Integer out;
		if (currentToken().getLexeme().length() == 0)
		{
			nextToken();
			return 0;
		}
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

	// Matches and parses a numeric expression.
	// Figure out a better way to force flag interpretation.
	private Object matchNumericExpression(AbstractPatchContext<?> context, boolean forceFlags)
	{
		Integer value;
		Integer out = null;
		
		// force label.
		if (!forceFlags && currentType(DecoHackKernel.TYPE_STRING))
		{
			String labelName = currentToken().getLexeme();
			nextToken();
			return labelName;
		}
		
		if (forceFlags && currentType(DecoHackKernel.TYPE_STRING))
		{
			addErrorMessage("Unexpected label. Not in a thing/weapon block.");
			return null;
		}
		
		while (currentType(DecoHackKernel.TYPE_DASH, DecoHackKernel.TYPE_NUMBER, DecoHackKernel.TYPE_IDENTIFIER))
		{
			if (out == null)
				out = 0;
			
			if (currentType(DecoHackKernel.TYPE_DASH, DecoHackKernel.TYPE_NUMBER))
			{
				if ((value = matchNumeric()) == null)
				{
					addErrorMessage("Expected numeric value.");
					return null;
				}
				out |= value;
			}
			else if ((value = matchThingFlagMnemonic()) != null)
			{
				out |= value;
			}
			else if ((value = matchThingMBF21FlagMnemonic(context)) != null)
			{
				out |= value;
			}
			else if ((value = matchWeaponMBF21FlagMnemonic(context)) != null)
			{
				out |= value;
			}
			else if (forceFlags)
			{
				addErrorMessage("Expected valid flag mnemonic.");
				return null;
			}
			else // expression not started. Maybe label.
			{
				return matchIdentifier(); 
			}
			
			if (!matchType(DecoHackKernel.TYPE_PIPE))
				break;
			
			forceFlags = true;
		}
		
		return out;
	}

	// Matches a numeric value, and returns an integer or a fixed-point value. 
	private Integer matchNumeric()
	{
		if (matchType(DecoHackKernel.TYPE_DASH))
		{
			Integer out;
			if ((out = matchPositiveNumeric()) == null)
				return null;
			return -out;
		}
		return matchPositiveNumeric();
	}
	
	// Matches a positive numeric value, and returns an integer or a fixed-point value. 
	private Integer matchPositiveNumeric()
	{
		if (!currentType(DecoHackKernel.TYPE_NUMBER))
			return null;
	
		String lexeme = currentToken().getLexeme();
		if (lexeme.startsWith("0X") || lexeme.startsWith("0x"))
		{
			try {
				long v = parseUnsignedHexLong(lexeme.substring(2));
				if (v > (long)Integer.MAX_VALUE || v < (long)Integer.MIN_VALUE)
					return null;
				nextToken();
				return (int)v;
			} catch (NumberFormatException e) {
				return null;
			}
		}
		else if (lexeme.contains("."))
		{
			try {
				int out = (int)(Double.parseDouble(lexeme) * (1 << 16L));
				nextToken();
				return out;
			} catch (NumberFormatException e) {
				return null;
			}
		}
		else
		{
			try {
				long v = Long.parseLong(lexeme);
				if (v > (long)Integer.MAX_VALUE || v < (long)Integer.MIN_VALUE)
					return null;
				nextToken();
				return (int)v;
			} catch (NumberFormatException e) {
				return null;
			}
		}
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

	// checks if a value can be assigned to a given action
	// pointer parameter; if not, prints an error message.
	private boolean checkActionParamValue(DEHActionPointer action, int index, int value)
	{
		if (action == null || index < 0)
		{
			addErrorMessage("Error in checkActionParamValue: invalid action or index. This is a bug with DECOHack!");
			return false;
		}

		DEHActionPointerParam[] params = action.getParams();
		if (index >= params.length)
		{
			addErrorMessage("Too many args for action %s: this action expects a maximum of %d args.", action.getMnemonic(), index);
			return false;
		}

		if (!params[index].isValueValid(value))
		{
			addErrorMessage("Invalid value '%d' for %s arg %d: value must be between %d and %d.", value, action.getMnemonic(), index, params[index].getValueMin(), params[index].getValueMax());
			return false;
		}

		return true;
	}
	
	// =======================================================================

	/** List of errors. */
	private LinkedList<String> errors;
	/** Editor directives. */
	private Map<String, String> editorKeys;

	// Return the exporter for the patch.
	private DecoHackParser(String streamName, Reader in)
	{
		super(new DecoHackLexer(streamName, in));
		this.errors = new LinkedList<>();
		this.editorKeys = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
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
	
	// State field type.
	private enum FieldType
	{
		NEXTSTATE,
		MISC1,
		MISC2,
		ARG0,
		ARG1,
		ARG2,
		ARG3,
		ARG4,
		ARG5,
		ARG6,
		ARG7,
		ARG8,
		ARG9;
		
		private static final FieldType[] VALUES = values();
		
		public static FieldType getArg(int i)
		{
			return VALUES[i + 3];
		}
	}
	
	// Field set.
	private static class FieldSet
	{
		private FieldType type;
		private String label; 
		
		private FieldSet(FieldType type, String label)
		{
			this.type = type;
			this.label = label;
		}
	}
	
	private static class FutureLabels
	{
		/** Future label map. */
		private Map<String, Set<Integer>> futureLabelMap;
		/** Future label alias. */
		private Map<String, Set<String>> futureLabelAlias;
		/** Actual state index to field use. */
		private Map<Integer, List<FieldSet>> stateFieldMap;
		
		private FutureLabels()
		{
			this.futureLabelMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			this.futureLabelAlias = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			this.stateFieldMap = new TreeMap<>();
		}
		
		/**
		 * Adds a field to backfill on a state.
		 * @param index the index of the state.
		 * @param type the field type.
		 * @param label the label for that place.
		 */
		public void addStateField(int index, FieldType type, String label)
		{
			List<FieldSet> fields;
			if ((fields = stateFieldMap.get(index)) == null)
				stateFieldMap.put(index, fields = new LinkedList<>());
			fields.add(new FieldSet(type, label));
	
			Set<Integer> indices;
			if ((indices = futureLabelMap.get(label)) == null)
				futureLabelMap.put(label, indices = new TreeSet<>());
			indices.add(index);
		}
		
		/**
		 * Adds a label destination alias for another label.
		 * @param label the label for that place.
		 * @param destinationAlias the destination label.
		 */
		public void addAlias(String label, String destinationAlias)
		{
			Set<String> labels;
			if ((labels = futureLabelAlias.get(destinationAlias)) == null)
				futureLabelAlias.put(destinationAlias, labels = new TreeSet<>());
			labels.add(label);
		}

		/**
		 * Resolves a single a label and backfills it.
		 * If the label is not recorded, nothing happens.
		 * @param context the context to use.
		 * @param actor the actor to set labels on.
		 * @param label the label to backfill.
		 * @param realIndex the real index of the label.
		 */
		public void backfill(AbstractPatchContext<?> context, DEHActor<?> actor, String label, int realIndex)
		{
			actor.setLabel(label, realIndex);

			if (!futureLabelMap.containsKey(label))
				return;
			
			for (Integer index : futureLabelMap.get(label))
			{
				DEHState state = context.getState(index);
				Iterator<FieldSet> fieldIterator = stateFieldMap.get(index).iterator();
				while (fieldIterator.hasNext())
				{
					FieldSet field = fieldIterator.next();
					if (!field.label.equalsIgnoreCase(label))
						continue;
					switch (field.type)
					{
						default:
							throw new IllegalArgumentException("INTERNAL ERROR: Unsupported field type."); 
						case NEXTSTATE:
							state.setNextStateIndex(realIndex);
							break;
						case MISC1:
							state.setMisc1(realIndex);
							break;
						case MISC2:
							state.setMisc2(realIndex);
							break;
						case ARG0:
							state.getArgs()[0] = realIndex;
							break;
						case ARG1:
							state.getArgs()[1] = realIndex;
							break;
						case ARG2:
							state.getArgs()[2] = realIndex;
							break;
						case ARG3:
							state.getArgs()[3] = realIndex;
							break;
						case ARG4:
							state.getArgs()[4] = realIndex;
							break;
						case ARG5:
							state.getArgs()[5] = realIndex;
							break;
						case ARG6:
							state.getArgs()[6] = realIndex;
							break;
						case ARG7:
							state.getArgs()[7] = realIndex;
							break;
						case ARG8:
							state.getArgs()[8] = realIndex;
							break;
						case ARG9:
							state.getArgs()[9] = realIndex;
							break;
					}
					fieldIterator.remove();
				}
			}
			
			if (futureLabelAlias.containsKey(label)) for (String destLabel : futureLabelAlias.get(label))
			{
				actor.setLabel(destLabel, realIndex);
			}

			futureLabelAlias.remove(label);
			futureLabelMap.remove(label);
		}
		
		/**
		 * @return true if there are unresolved labels left over.
		 */
		public boolean hasLabels()
		{
			return !futureLabelMap.isEmpty();
		}
		
		/**
		 * @return the list of labels still unresolved.
		 */
		public String[] getLabels()
		{
			Set<String> keys = futureLabelMap.keySet();
			return keys.toArray(new String[keys.size()]);
		}
		
	}

	private static class ParsedAction
	{
		private boolean offset;
		private DEHActionPointer pointer;
		private int misc1;
		private int misc2;
		private List<Integer> args;
		private Deque<FieldSet> labelFields;

		private ParsedAction()
		{
			this.args = new LinkedList<>();
			this.labelFields = new LinkedList<>();
			reset();
		}
		
		void reset()
		{
			this.offset = false;
			this.pointer = null;
			this.misc1 = 0;
			this.misc2 = 0;
			this.args.clear();
			this.labelFields.clear();
		}
		
		void dumpLabels(int stateIndex, FutureLabels labels)
		{
			while (!labelFields.isEmpty())
			{
				FieldSet fs = labelFields.pollFirst();
				labels.addStateField(stateIndex, fs.type, fs.label);
			}
		}
		
	}
	
	private static class ParsedState
	{
		private Integer spriteIndex;
		private Deque<Integer> frameList;
		private Integer duration;
		private boolean bright;
		private Integer mbf21Flags;
		private List<ParsedAction> parsedActions;
		
		private ParsedState()
		{
			this.frameList = new LinkedList<>();
			this.parsedActions = new ArrayList<>();
			reset();
		}
		
		void reset()
		{
			this.spriteIndex = null;
			this.duration = null;
			this.bright = false;
			this.mbf21Flags = null;
			this.frameList.clear();
			this.parsedActions.clear();
		}
		
	}
	
	/**
	 * Lexer Kernel for DECOHack.
	 */
	private static class DecoHackKernel extends Lexer.Kernel
	{
		public static final int TYPE_LPAREN = 1;
		public static final int TYPE_RPAREN = 2;
		public static final int TYPE_COMMA = 5;
		public static final int TYPE_LBRACE = 7;
		public static final int TYPE_RBRACE = 8;
		public static final int TYPE_COLON = 10;
		public static final int TYPE_PERIOD = 11;
		public static final int TYPE_PLUS = 12;
		public static final int TYPE_DASH = 13;
		public static final int TYPE_PIPE = 14;

		public static final int TYPE_TRUE = 101;
		public static final int TYPE_FALSE = 102;

		private DecoHackKernel()
		{
			setDecimalSeparator('.');

			addStringDelimiter('"', '"');
			addRawStringDelimiter('`', '`');
			
			addCommentDelimiter("/*", "*/");
			addCommentLineDelimiter("//");

			addDelimiter("(", TYPE_LPAREN);
			addDelimiter(")", TYPE_RPAREN);
			addDelimiter("{", TYPE_LBRACE);
			addDelimiter("}", TYPE_RBRACE);
			addDelimiter(",", TYPE_COMMA);
			addDelimiter(".", TYPE_PERIOD);
			addDelimiter(":", TYPE_COLON);

			addDelimiter("+", TYPE_PLUS);
			addDelimiter("-", TYPE_DASH);
			addDelimiter("|", TYPE_PIPE);
			
			addCaseInsensitiveKeyword("true", TYPE_TRUE);
			addCaseInsensitiveKeyword("false", TYPE_FALSE);
			
			setEmitComments(true); // for directives.
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
			setIncluder(new PreprocessorLexer.DefaultIncluder() 
			{
				private final Map<String, String> SPECIAL_INCLUDES = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER)
				{
					private static final long serialVersionUID = -7828739256854493701L;
					{
						put("<doom19>", "classpath:decohack/doom19.dh");
						put("<udoom19>", "classpath:decohack/udoom19.dh");
						put("<boom>", "classpath:decohack/boom.dh");
						put("<mbf>", "classpath:decohack/mbf.dh");
						put("<extended>", "classpath:decohack/extended.dh");
						put("<mbf21>", "classpath:decohack/mbf21.dh");
						put("<dsdhacked>", "classpath:decohack/dsdhacked.dh");
						put("<friendly>", "classpath:decohack/constants/friendly_things.dh");
					}
				};
				
				@Override
				public String getIncludeResourcePath(String streamName, String path) throws IOException 
				{
					String foundPath;
					if ((foundPath = SPECIAL_INCLUDES.get(path)) != null)
					{
						return super.getIncludeResourcePath(streamName, foundPath);
					}
					else
					{
						return super.getIncludeResourcePath(streamName, path);
					}
				}
			});
		}
	}

}

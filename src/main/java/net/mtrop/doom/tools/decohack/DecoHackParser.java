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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import net.mtrop.doom.tools.decohack.contexts.AbstractPatchBoomContext;
import net.mtrop.doom.tools.decohack.contexts.AbstractPatchContext;
import net.mtrop.doom.tools.decohack.contexts.AbstractPatchDoom19Context;
import net.mtrop.doom.tools.decohack.contexts.PatchBoomContext;
import net.mtrop.doom.tools.decohack.contexts.PatchDHEExtendedContext;
import net.mtrop.doom.tools.decohack.contexts.PatchDoom19Context;
import net.mtrop.doom.tools.decohack.contexts.PatchMBFContext;
import net.mtrop.doom.tools.decohack.contexts.PatchMBF21Context;
import net.mtrop.doom.tools.decohack.contexts.PatchUltimateDoom19Context;
import net.mtrop.doom.tools.decohack.data.DEHActionPointer;
import net.mtrop.doom.tools.decohack.data.DEHActionPointerParam;
import net.mtrop.doom.tools.decohack.data.DEHActor;
import net.mtrop.doom.tools.decohack.data.DEHAmmo;
import net.mtrop.doom.tools.decohack.data.DEHMiscellany;
import net.mtrop.doom.tools.decohack.data.DEHSound;
import net.mtrop.doom.tools.decohack.data.DEHState;
import net.mtrop.doom.tools.decohack.data.DEHThing;
import net.mtrop.doom.tools.decohack.data.DEHWeapon;
import net.mtrop.doom.tools.decohack.data.DEHWeapon.Ammo;
import net.mtrop.doom.tools.decohack.data.DEHWeaponFlag;
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

	private static final int FLAG_SPECIAL =       0x00000001;
	private static final int FLAG_SOLID =         0x00000002;
	private static final int FLAG_SHOOTABLE =     0x00000004;
	private static final int FLAG_NOSECTOR =      0x00000008;
	private static final int FLAG_NOBLOCKMAP =    0x00000010;
	private static final int FLAG_AMBUSH =        0x00000020;
	private static final int FLAG_JUSTHIT =       0x00000040;
	private static final int FLAG_JUSTATTACKED =  0x00000080;
	private static final int FLAG_SPAWNCEILING =  0x00000100;
	private static final int FLAG_NOGRAVITY =     0x00000200;
	private static final int FLAG_DROPOFF =       0x00000400;
	private static final int FLAG_PICKUP =        0x00000800;
	private static final int FLAG_NOCLIP =        0x00001000;
	private static final int FLAG_SLIDE =         0x00002000;
	private static final int FLAG_FLOAT =         0x00004000;
	private static final int FLAG_TELEPORT =      0x00008000;
	private static final int FLAG_MISSILE =       0x00010000;
	private static final int FLAG_DROPPED =       0x00020000;
	private static final int FLAG_SHADOW =        0x00040000;
	private static final int FLAG_NOBLOOD =       0x00080000;
	private static final int FLAG_CORPSE =        0x00100000;
	private static final int FLAG_INFLOAT =       0x00200000;
	private static final int FLAG_COUNTKILL =     0x00400000;
	private static final int FLAG_COUNTITEM =     0x00800000;
	private static final int FLAG_SKULLFLY =      0x01000000;
	private static final int FLAG_NOTDEATHMATCH = 0x02000000;
	private static final int FLAG_TRANSLATION =   0x04000000;
	private static final int FLAG_TRANSLATION2 =  0x08000000;
	private static final int FLAG_TOUCHY =        0x10000000;
	private static final int FLAG_BOUNCES =       0x20000000;
	private static final int FLAG_FRIEND =        0x40000000;
	private static final int FLAG_TRANSLUCENT =   0x80000000;

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
	private static final String KEYWORD_PAINCHANCE = "painchance";
	private static final String KEYWORD_REACTIONTIME = "reactiontime";
	private static final String KEYWORD_DAMAGE = "damage";
	private static final String KEYWORD_HEIGHT = "height";
	private static final String KEYWORD_RADIUS = "radius";
	private static final String KEYWORD_SPEED = "speed";
	private static final String KEYWORD_FASTSPEED = "fastspeed";
	private static final String KEYWORD_HEALTH = "health";
	private static final String KEYWORD_SEESOUND = "seesound";
	private static final String KEYWORD_ATTACKSOUND = "attacksound";
	private static final String KEYWORD_PAINSOUND = "painsound";
	private static final String KEYWORD_DEATHSOUND = "deathsound";
	private static final String KEYWORD_ACTIVESOUND = "activesound";
	private static final String KEYWORD_RIPSOUND = "ripsound";

	private static final String KEYWORD_OFFSET = "offset";
	private static final String KEYWORD_BRIGHT = "bright";
	
	private static final String KEYWORD_WITH = "with";
	private static final String KEYWORD_SWAP = "swap";

	private static final String KEYWORD_USING = "using";
	private static final String KEYWORD_DOOM19 = "doom19";
	private static final String KEYWORD_UDOOM19 = "udoom19";
	private static final String KEYWORD_BOOM = "boom";
	private static final String KEYWORD_MBF = "mbf";
	private static final String KEYWORD_EXTENDED = "extended";
	private static final String KEYWORD_MBF21 = "mbf21";

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
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_MBF21))
			return new PatchMBF21Context();
		else
		{
			addErrorMessage("Expected valid patch format type (%s, %s, %s, %s, %s, %s).", 
				KEYWORD_DOOM19, KEYWORD_UDOOM19, KEYWORD_BOOM, KEYWORD_MBF, KEYWORD_EXTENDED, KEYWORD_MBF21
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
		DEHSound sound;
		Integer soundIndex;
		if ((soundIndex = matchSoundIndexName(context)) == null)
		{
			addErrorMessage("Expected sound index or sound name after \"%s\".", KEYWORD_SOUND);
			return false;
		}
		else if (soundIndex < 0)
		{
			addErrorMessage("Expected valid sound index or sound name after \"%s\".", KEYWORD_SOUND);
			return false;
		}
		else if ((sound = context.getSound(soundIndex)) == null)
		{
			addErrorMessage("Expected valid sound index or sound name after \"%s\".", KEYWORD_SOUND);
			return false;
		}
		
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
		if ((slot = matchThingIndex(context)) == null)
			return false;

		// thing swap
		if (matchIdentifierLexemeIgnoreCase(KEYWORD_SWAP))
		{
			if (!matchIdentifierLexemeIgnoreCase(KEYWORD_WITH))
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
			return true;
		}
		// free states.
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_FREE))
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
		if (matchType(DecoHackKernel.TYPE_COLON))
		{
			if (!matchIdentifierLexemeIgnoreCase(KEYWORD_THING))
			{
				addErrorMessage("Expected \"%s\" after ':'.", KEYWORD_THING);
				return false;
			}
			
			Integer slot;
			if ((slot = matchThingIndex(context)) == null)
				return false;
			
			thing.copyFrom(context.getThing(slot));
		}
		
		if (currentType(DecoHackKernel.TYPE_STRING))
		{
			thing.setName(matchString());
		}
		
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
				if ((value = matchFlagMnemonic()) == null && (value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer after \"+\".");
					return false;
				}
				thing.setFlags(thing.getFlags() | value);
			}
			else if (matchType(DecoHackKernel.TYPE_DASH))
			{
				if ((value = matchFlagMnemonic()) == null && (value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer after \"-\".");
					return false;
				}
				thing.setFlags(thing.getFlags() & (~value));
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_STATE))
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
				if (matchIdentifierLexemeIgnoreCase(KEYWORD_STATE))
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
				else if (matchIdentifierLexemeIgnoreCase(KEYWORD_STATES))
				{
					thing.clearLabels();
				}
				else if (matchIdentifierLexemeIgnoreCase(KEYWORD_SOUNDS))
				{
					thing
						.setSeeSoundPosition(0)
						.setAttackSoundPosition(0)
						.setPainSoundPosition(0)
						.setDeathSoundPosition(0)
						.setActiveSoundPosition(0)
						.setRipSoundPosition(0)
					;
				}
				else
				{
					addErrorMessage("Expected '%s', '%s', or '%s' after '%s'.", KEYWORD_STATE, KEYWORD_STATES, KEYWORD_SOUNDS, KEYWORD_CLEAR);
					return false;
				}
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_EDNUM))
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
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_HEALTH))
			{
				if ((value = matchInteger()) == null)
				{
					addErrorMessage("Expected integer after \"%s\".", KEYWORD_HEALTH);
					return false;
				}
				thing.setHealth(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_SPEED))
			{
				if ((value = matchInteger()) == null)
				{
					addErrorMessage("Expected integer after \"%s\".", KEYWORD_SPEED);
					return false;
				}
				thing.setSpeed(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_FASTSPEED))
			{
				if ((value = matchInteger()) == null)
				{
					addErrorMessage("Expected integer after \"%s\".", KEYWORD_FASTSPEED);
					return false;
				}
				thing.setFastSpeed(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_RADIUS))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_RADIUS);
					return false;
				}
				thing.setRadius(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_HEIGHT))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_HEIGHT);
					return false;
				}
				thing.setHeight(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_DAMAGE))
			{
				if ((value = matchInteger()) == null)
				{
					addErrorMessage("Expected integer after \"%s\".", KEYWORD_DAMAGE);
					return false;
				}
				thing.setDamage(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_REACTIONTIME))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_REACTIONTIME);
					return false;
				}
				thing.setReactionTime(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_PAINCHANCE))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_PAINCHANCE);
					return false;
				}
				thing.setPainChance(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_MASS))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_MASS);
					return false;
				}
				thing.setMass(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_FLAGS))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_FLAGS);
					return false;
				}
				thing.setFlags(value);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_SEESOUND))
			{
				if ((value = matchSoundIndexName(context)) == null)
				{
					addErrorMessage("Expected sound name after \"%s\".", KEYWORD_SEESOUND);
					return false;
				}
				thing.setSeeSoundPosition(value + 1);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_ATTACKSOUND))
			{
				if ((value = matchSoundIndexName(context)) == null)
				{
					addErrorMessage("Expected sound name after \"%s\".", KEYWORD_ATTACKSOUND);
					return false;
				}
				thing.setAttackSoundPosition(value + 1);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_PAINSOUND))
			{
				if ((value = matchSoundIndexName(context)) == null)
				{
					addErrorMessage("Expected sound name after \"%s\".", KEYWORD_PAINSOUND);
					return false;
				}
				thing.setPainSoundPosition(value + 1);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_DEATHSOUND))
			{
				if ((value = matchSoundIndexName(context)) == null)
				{
					addErrorMessage("Expected sound name after \"%s\".", KEYWORD_DEATHSOUND);
					return false;
				}
				thing.setDeathSoundPosition(value + 1);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_ACTIVESOUND))
			{
				if ((value = matchSoundIndexName(context)) == null)
				{
					addErrorMessage("Expected sound name after \"%s\".", KEYWORD_ACTIVESOUND);
					return false;
				}
				thing.setActiveSoundPosition(value + 1);
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_RIPSOUND))
			{
				if ((value = matchSoundIndexName(context)) == null)
				{
					addErrorMessage("Expected sound name after \"%s\".", KEYWORD_RIPSOUND);
					return false;
				}
				thing.setRipSoundPosition(value + 1);
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
		
		return true;
	}
	
	// Parses a thing state clause.
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
		if (matchIdentifierLexemeIgnoreCase(KEYWORD_SWAP))
		{
			if (!matchIdentifierLexemeIgnoreCase(KEYWORD_WITH))
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
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_FREE))
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
		if (matchType(DecoHackKernel.TYPE_COLON))
		{
			if (!matchIdentifierLexemeIgnoreCase(KEYWORD_WEAPON))
			{
				addErrorMessage("Expected \"%s\" after ':'.", KEYWORD_WEAPON);
				return false;
			}
			
			Integer slot;
			if ((slot = matchWeaponIndex(context)) == null)
				return false;

			weapon.copyFrom(context.getWeapon(slot));
		}
		
		if (currentType(DecoHackKernel.TYPE_STRING))
		{
			weapon.setName(matchString());
		}
		
		if (!matchType(DecoHackKernel.TYPE_LBRACE))
		{
			addErrorMessage("Expected '{' after \"%s\" declaration.", KEYWORD_WEAPON);
			return false;
		}
		
		while (currentType(DecoHackKernel.TYPE_IDENTIFIER, DecoHackKernel.TYPE_PLUS, DecoHackKernel.TYPE_DASH))
		{
			if (matchType(DecoHackKernel.TYPE_PLUS))
			{
				Integer flags;
				if ((flags = matchWeaponFlagMnemonic()) == null && (flags = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer after \"+\".");
					return false;
				}
				weapon.setFlags(weapon.getFlags() | flags);
			}
			else if (matchType(DecoHackKernel.TYPE_DASH))
			{
				Integer flags;
				if ((flags = matchWeaponFlagMnemonic()) == null && (flags = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer after \"-\".");
					return false;
				}
				weapon.setFlags(weapon.getFlags() & (~flags));
			}
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
				if (matchIdentifierLexemeIgnoreCase(KEYWORD_STATE))
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
				else if (matchIdentifierLexemeIgnoreCase(KEYWORD_STATES))
				{
					weapon.clearLabels();
				}
				else
				{
					addErrorMessage("Expected '%s' or '%s' after '%s'.", KEYWORD_STATE, KEYWORD_STATES, KEYWORD_CLEAR);
					return false;
				}
			}
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_AMMOTYPE))
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
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_AMMOPERSHOT))
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
			else if (matchIdentifierLexemeIgnoreCase(KEYWORD_FLAGS))
			{
				Integer flags;
				if ((flags = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_FLAGS);
					return false;
				}
				else
				{
					weapon.setFlags(flags);
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

		if (!parseActorStateSet(context, weapon)) 
			return false;
		
		if (!matchType(DecoHackKernel.TYPE_RBRACE))
		{
			addErrorMessage("Expected '}' after \"%s\" section.", KEYWORD_STATES);
			return false;
		}
		
		return true;
	}
	
	// Parses an actor's state body.
	private boolean parseActorStateSet(AbstractPatchContext<?> context, DEHActor actor)
	{
		// state label.
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			addErrorMessage("Expected state label.");
			return false;
		}
		
		LinkedList<String> label = new LinkedList<>();
		ParsedState parsed = new ParsedState();
		StateFillCursor stateCursor = new StateFillCursor();
		
		while (currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			label.add(currentToken().getLexeme());
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
					if ((startIndex = fillStates(context, parsed, stateCursor, false)) == null)
						return false;
					if (loopIndex == null)
						loopIndex = startIndex;
					while (!label.isEmpty())
						actor.setLabel(label.pollFirst(), startIndex);
				} while (currentIsSpriteIndex(context));
			}
			
			// Parse next state.
			if (currentIsNextStateKeyword())
			{
				Integer nextStateIndex = null;
				if ((nextStateIndex = parseNextStateIndex(context, actor, loopIndex, stateCursor.lastIndexFilled)) == null)
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
		return parseStateIndex(context, null);
	}

	// Parses a mandatory state index.
	private Integer parseStateIndex(AbstractPatchContext<?> context, DEHActor actor)
	{
		String labelName;
		if (matchIdentifierLexemeIgnoreCase(KEYWORD_THING))
			return parseThingStateIndex(context);
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_WEAPON))
			return parseWeaponStateIndex(context);
		else if ((labelName = matchIdentifier()) != null)
			return parseActorStateLabelIndex(actor, labelName);
		else
			return matchStateIndex(context);
	}

	// Parses a label state index.
	private Integer parseActorStateLabelIndex(DEHActor actor, String labelName) 
	{
		Integer value;
		if (actor == null)
		{
			addErrorMessage("Name of label was unexpected after \"%s\". Only valid in thing or weapon.", KEYWORD_GOTO);
			return null;				
		}
		else if ((value = actor.getLabel(labelName)) == 0)
		{
			StringBuilder sb = new StringBuilder("Expected a valid state label for this object: ");
			sb.append(Arrays.toString(actor.getLabels()));
			sb.append(".");
			addErrorMessage(sb.toString());

			addErrorMessage("Label \"%s\" is invalid or not declared at this moment: " + sb.toString(), labelName);
			return null;				
		}
		
		return value;
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
		if (context.isProtectedState(index))
		{
			addErrorMessage("State index %d is a protected state.", index);
			return false;
		}
		
		Integer nextStateIndex = null;
		if ((nextStateIndex = parseNextStateIndex(context, null, null, index)) != null)
		{
			context.getState(index).setNextStateIndex(nextStateIndex);
			return true;
		}
		
		if (currentIsSpriteIndex(context))
		{
			ParsedState parsedState = new ParsedState();
			
			boolean isBoom = context instanceof AbstractPatchBoomContext;			
			Integer pointerIndex = context.getStateActionPointerIndex(index);
			if (!parseStateLine(context, null, parsedState, true, isBoom ? null : pointerIndex != null))
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
				.setMisc1(parsedState.misc1)
				.setMisc2(parsedState.misc2)
				.setArgs(parsedState.args)
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
	private boolean parseStateLine(AbstractPatchContext<?> context, DEHActor actor, ParsedState state)
	{
		return parseStateLine(context, actor, state, false, null);
	}
	
	// Parse a single state and if true is returned, the input state is altered.
	// requireAction is either true, false, or null. If null, no check is performed. 
	private boolean parseStateLine(AbstractPatchContext<?> context, DEHActor actor, ParsedState state, boolean singleFrame, Boolean requireAction) 
	{
		if ((state.spriteIndex = matchSpriteIndexName(context)) == null)
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

		// Maybe parse offsets
		state.misc1 = 0;
		state.misc2 = 0;
		state.args.clear();

		boolean useoffsets = matchOffsetDirective();
		if (useoffsets)
		{
			if (matchType(DecoHackKernel.TYPE_LPAREN))
			{
				// no arguments
				if (!matchType(DecoHackKernel.TYPE_RPAREN))
				{
					// get first argument
					Integer p;
					if ((p = matchInteger()) == null)
					{
						addErrorMessage("Expected integer for X offset value.");
						return false;
					}
					state.misc1 = p;

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
					state.misc2 = p;

					if (!matchType(DecoHackKernel.TYPE_RPAREN))
					{
						addErrorMessage("Expected a ')' after offsets.");
						return false;
					}
				}
			}
			else
			{
				addErrorMessage("Expected a '(' after \"offset\".");
				return false;
			}
		}

		// Maybe parse action
		state.action = matchActionPointerName();
		
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

		if (state.action != null)
		{
			if (!context.isActionPointerTypeSupported(state.action.getType()))
			{
				addErrorMessage(state.action.getType().name() + " action pointer used: " + state.action.getMnemonic() +". Patch does not support this action type.");
				return false;
			}

			// MBF args (misc1/misc2)
			if (!state.action.useArgs())
			{
				if (matchType(DecoHackKernel.TYPE_LPAREN))
				{
					// no arguments
					if (matchType(DecoHackKernel.TYPE_RPAREN))
					{
						return true;
					}

					if (useoffsets)
					{
						addErrorMessage("Cannot use 'offset' directive on a state with an MBF action function parameter.");
						return false;
					}

					// get first argument
					Integer p;
					if ((p = parseActionPointerParameterValue(context, actor)) == null)
						return false;

					if (!checkActionParamValue(state.action, 0, p))
						return false;

					state.misc1 = p;

					if (matchType(DecoHackKernel.TYPE_COMMA))
					{
						if ((p = parseActionPointerParameterValue(context, actor)) == null)
							return false;

						if (!checkActionParamValue(state.action, 1, p))
							return false;

						state.misc2 = p;
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
						int argIndex = state.args.size();
						Integer p;
						if ((p = parseActionPointerParameterValue(context, actor)) == null)
							return false;

						if (!checkActionParamValue(state.action, argIndex, p))
							return false;

						state.args.add(p);

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
	private Integer parseActionPointerParameterValue(AbstractPatchContext<?> context, DEHActor actor)
	{
		Integer value;
		String labelName;
		if (matchIdentifierLexemeIgnoreCase(KEYWORD_THING))
			return parseThingStateIndex(context);
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_WEAPON))
			return parseWeaponStateIndex(context);
		else if (matchIdentifierLexemeIgnoreCase(KEYWORD_SOUND))
			return parseSoundIndex(context);
		else if ((labelName = matchIdentifier()) != null)
			return parseActorStateLabelIndex(actor, labelName);
		else if ((value = matchNumeric()) != null)
			return value;
		else
		{
			addErrorMessage("Expected parameter.");
			return null;
		}
	}

	// Parses a next state line.
	private Integer parseNextStateIndex(AbstractPatchContext<?> context, DEHActor actor, Integer lastLabelledStateIndex, int currentStateIndex)
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
			return parseStateIndex(context, actor);
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
				.setMisc1(state.misc1)
				.setMisc2(state.misc2)
				.setArgs(state.args)
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

	// Checks if the current token is an identifier with a specific lexeme.
	private boolean currentIdentifierLexemeIgnoreCase(String lexeme)
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
	private boolean matchIdentifierLexemeIgnoreCase(String lexeme)
	{
		if (!currentIdentifierLexemeIgnoreCase(lexeme))
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
		return matchIdentifierLexemeIgnoreCase(KEYWORD_BRIGHT);
	}
	
	// Matches an identifier that can be "offset".
	// If match, advance token and return true plus modified out list.
	// Else, return null.
	private boolean matchOffsetDirective()
	{
		return matchIdentifierLexemeIgnoreCase(KEYWORD_OFFSET);
	}
	
	// Matches an identifier that references a flag mnemonic.
	// If match, advance token and return bitflags.
	// Else, return null.
	private Integer matchFlagMnemonic()
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
			return null;
		
		Integer out;
		switch (currentToken().getLexeme().toLowerCase())
		{
			case "special":
				out = FLAG_SPECIAL;
				break;
			case "solid":
				out = FLAG_SOLID;
				break;
			case "shootable":
				out = FLAG_SHOOTABLE;
				break;
			case "nosector":
				out = FLAG_NOSECTOR;
				break;
			case "noblockmap":
				out = FLAG_NOBLOCKMAP;
				break;
			case "ambush":
				out = FLAG_AMBUSH;
				break;
			case "justhit":
				out = FLAG_JUSTHIT;
				break;
			case "justattacked":
				out = FLAG_JUSTATTACKED;
				break;
			case "spawnceiling":
				out = FLAG_SPAWNCEILING;
				break;
			case "nogravity":
				out = FLAG_NOGRAVITY;
				break;
			case "dropoff":
				out = FLAG_DROPOFF;
				break;
			case "pickup":
				out = FLAG_PICKUP;
				break;
			case "noclip":
				out = FLAG_NOCLIP;
				break;
			case "slide":
				out = FLAG_SLIDE;
				break;
			case "float":
				out = FLAG_FLOAT;
				break;
			case "teleport":
				out = FLAG_TELEPORT;
				break;
			case "missile":
				out = FLAG_MISSILE;
				break;
			case "dropped":
				out = FLAG_DROPPED;
				break;
			case "shadow":
				out = FLAG_SHADOW;
				break;
			case "noblood":
				out = FLAG_NOBLOOD;
				break;
			case "corpse":
				out = FLAG_CORPSE;
				break;
			case "infloat":
				out = FLAG_INFLOAT;
				break;
			case "countkill":
				out = FLAG_COUNTKILL;
				break;
			case "countitem":
				out = FLAG_COUNTITEM;
				break;
			case "skullfly":
				out = FLAG_SKULLFLY;
				break;
			case "notdmatch":
				out = FLAG_NOTDEATHMATCH;
				break;
			case "translation":
				out = FLAG_TRANSLATION;
				break;
			case "translation2":
			case "unused1":
				out = FLAG_TRANSLATION2;
				break;
			case "unused2":
			case "touchy":
				out = FLAG_TOUCHY;
				break;
			case "unused3":
			case "bounces":
				out = FLAG_BOUNCES;
				break;
			case "unused4":
			case "friend":
			case "friendly":
				out = FLAG_FRIEND;
				break;
			case "translucent":
				out = FLAG_TRANSLUCENT;
				break;
			default:
				out = null;
				break;
		}
		
		if (out != null)
			nextToken();
		return out;
	}
	
	// Matches an identifier that references a weapon
	// flag mnemonic. If match, advance token and
	// return bitflags. Else, return null.
	private Integer matchWeaponFlagMnemonic()
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
			return null;
		
		Integer out = null;
		String mnemonic = currentToken().getLexeme().toLowerCase();

		for (int i = 0; i < DEHWeaponFlag.VALUES.length; i++)
		{
			if(DEHWeaponFlag.VALUES[i].getMnemonic().equals(mnemonic))
			{
				out = DEHWeaponFlag.VALUES[i].getValue();
				break;
			}
		}
		
		if (out != null)
			nextToken();
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
			return -1;
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
		private Integer misc1;
		private Integer misc2;
		private List<Integer> args;
		
		private ParsedState()
		{
			this.frameList = new LinkedList<>();
			this.args = new LinkedList<>();
			reset();
		}
		
		void reset()
		{
			this.spriteIndex = null;
			this.frameList.clear();
			this.duration = null;
			this.bright = null;
			this.action = null;
			this.misc1 = null;
			this.misc2 = null;
			this.args.clear();
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

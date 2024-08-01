/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
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
import java.nio.charset.Charset;
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

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.decohack.contexts.AbstractPatchContext;
import net.mtrop.doom.tools.decohack.contexts.PatchBoomContext;
import net.mtrop.doom.tools.decohack.contexts.PatchDSDHackedContext;
import net.mtrop.doom.tools.decohack.contexts.PatchDoom19Context;
import net.mtrop.doom.tools.decohack.data.DEHActionPointer;
import net.mtrop.doom.tools.decohack.data.DEHActionPointerEntry;
import net.mtrop.doom.tools.decohack.data.DEHActor;
import net.mtrop.doom.tools.decohack.data.DEHAmmo;
import net.mtrop.doom.tools.decohack.data.DEHMiscellany;
import net.mtrop.doom.tools.decohack.data.DEHProperty;
import net.mtrop.doom.tools.decohack.data.DEHSound;
import net.mtrop.doom.tools.decohack.data.DEHState;
import net.mtrop.doom.tools.decohack.data.DEHThing;
import net.mtrop.doom.tools.decohack.data.DEHThingTarget;
import net.mtrop.doom.tools.decohack.data.DEHThingTemplate;
import net.mtrop.doom.tools.decohack.data.DEHWeapon;
import net.mtrop.doom.tools.decohack.data.DEHWeapon.Ammo;
import net.mtrop.doom.tools.decohack.data.enums.DEHValueType;
import net.mtrop.doom.tools.decohack.data.enums.DEHFeatureLevel;
import net.mtrop.doom.tools.decohack.data.enums.DEHFlag;
import net.mtrop.doom.tools.decohack.data.enums.DEHStateMBF21Flag;
import net.mtrop.doom.tools.decohack.data.enums.DEHThingFlag;
import net.mtrop.doom.tools.decohack.data.enums.DEHThingMBF21Flag;
import net.mtrop.doom.tools.decohack.data.enums.DEHWeaponMBF21Flag;
import net.mtrop.doom.tools.decohack.data.enums.DEHValueType.Type;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerType;
import net.mtrop.doom.tools.decohack.data.DEHWeaponTarget;
import net.mtrop.doom.tools.decohack.data.DEHWeaponTemplate;
import net.mtrop.doom.tools.decohack.patches.DEHPatch;
import net.mtrop.doom.tools.decohack.patches.DEHPatchBoom.EpisodeMap;
import net.mtrop.doom.tools.struct.Lexer;
import net.mtrop.doom.tools.struct.PreprocessorLexer;
import net.mtrop.doom.tools.struct.util.ArrayUtils;
import net.mtrop.doom.tools.struct.util.EnumUtils;

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
	private static final String KEYWORD_MONSTER_INFIGHTING2 = "monstersFightOwnSpecies";
	
	private static final String KEYWORD_PARS = "pars";
	
	private static final String KEYWORD_SET = "set";
	private static final String KEYWORD_NEXT = "next";
	private static final String KEYWORD_INDEX = "index";
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
	private static final String KEYWORD_PROPERTY = "property";

	private static final String KEYWORD_ALIAS = "alias";
	private static final String KEYWORD_AUTO = "auto";
	private static final String KEYWORD_EACH = "each";
	private static final String KEYWORD_IN = "in";
	private static final String KEYWORD_TO = "to";
	private static final String KEYWORD_FROM = "from";
	
	private static final String KEYWORD_SOUND = "sound";
	private static final String KEYWORD_SOUNDS = "sounds";
	private static final String KEYWORD_SINGULAR = "singular";
	private static final String KEYWORD_PRIORITY = "priority";

	private static final String KEYWORD_SPRITE = "sprite";

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
	private static final String KEYWORD_MBF21 = "mbf21";

	private static final String KEYWORD_CUSTOM = "custom";
	
	private static final String KEYWORD_NULL = "null";

	private static final Pattern MAPLUMP_EXMY = Pattern.compile("E[0-9]+M[0-9]+", Pattern.CASE_INSENSITIVE);
	private static final Pattern MAPLUMP_MAPXX = Pattern.compile("MAP[0-9][0-9]+", Pattern.CASE_INSENSITIVE);

	private static final FutureLabels EMPTY_LABELS = null;
	private static final int PLACEHOLDER_LABEL = -1234567890;

	/**
	 * The parser result from compiling a DECOHack.
	 */
	public static final class Result
	{
		private AbstractPatchContext<?> context;
		private String[] warnings;
		private String[] errors;
		
		public AbstractPatchContext<?> getContext() 
		{
			return context;
		}
		
		public String[] getWarnings() 
		{
			return warnings;
		}
		
		public String[] getErrors() 
		{
			return errors;
		}
	}
	
	/**
	 * Reads a DECOHack script.
	 * @param streamName the name of the stream.
	 * @param in the stream to read from.
	 * @param inputCharset the input charset encoding for the stream.
	 * @return the result of the parse.
	 * @throws IOException if the stream can't be read.
	 * @throws SecurityException if a read error happens due to OS permissioning.
	 * @throws NullPointerException if in is null. 
	 */
	public static Result read(String streamName, InputStream in, Charset inputCharset) throws IOException
	{
		DecoHackParser parser = new DecoHackParser(streamName, in, inputCharset);
		Result out = new Result();
		out.context = parser.parse();
		out.warnings = parser.getWarningMessages();
		out.errors = parser.getErrorMessages();
		return out;
	}

	/**
	 * Reads a DECOHack script from a starting text file.
	 * @param files the files to read from (as though each file is included, in order).
	 * @param inputCharset the input charset for all files.
	 * @return the result of the parse.
	 * @throws IOException if the stream can't be read.
	 * @throws SecurityException if a read error happens due to OS permissioning.
	 * @throws NullPointerException if file is null. 
	 */
	public static Result read(Iterable<File> files, Charset inputCharset) throws IOException
	{
		DecoHackParser parser = new DecoHackParser(null, null, inputCharset);
		Lexer lexer = parser.getLexer();
		
		// Lexer streams are a stack, so add files backwards for the correct order.
		Deque<File> backwards = new LinkedList<>();
		for (File file : files)
			backwards.push(file);
		while (!backwards.isEmpty())
		{
			File file = backwards.pollFirst();
			lexer.pushStream(file.getPath(), new InputStreamReader(new FileInputStream(file), inputCharset));
		}

		Result out = new Result();
		out.context = parser.parse();
		out.warnings = parser.getWarningMessages();
		out.errors = parser.getErrorMessages();
		return out;
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
			if (currentType(DecoHackKernel.TYPE_LINE_COMMENT) && currentLexeme().startsWith("$"))
			{
				// Lazily split this thing.
				String content = currentLexeme().substring(1).trim();
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
			addErrorMessage("Expected \"using\" clause to set the patch format (or better yet, use a built-in #include!).");
			return null;
		}
		
		DecoHackPatchType patchType = null;
		String patchName;
		if ((patchName = matchIdentifier()) != null)
			patchType = DecoHackPatchType.getByKeyword(patchName);
		
		if (patchType == null)
		{
			StringBuilder sb = new StringBuilder();
			DecoHackPatchType[] values = DecoHackPatchType.values();
			for (int i = 0; i < values.length; i++) 
			{
				sb.append(values[i].getKeyword());
				if (i < values.length - 1)
					sb.append(", ");
			}
			
			addErrorMessage("Expected valid patch format type (%s).", sb.toString());
			return null;
		}
		
		return Common.create(patchType.getPatchClass());
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
		else if (matchIdentifierIgnoreCase(KEYWORD_CUSTOM))
			return parseCustomClause(context);
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
		else if (matchIdentifierIgnoreCase(KEYWORD_ALIAS))
		{
			if (matchIdentifierIgnoreCase(KEYWORD_THING))
				return parseThingAliasLine(context);
			else if (matchIdentifierIgnoreCase(KEYWORD_WEAPON))
				return parseWeaponAliasLine(context);
			else
			{
				addErrorMessage("Expected \"%s\" or \"%s\" after \"%s\".", KEYWORD_THING, KEYWORD_WEAPON, KEYWORD_ALIAS);
				return false;
			}
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_SET))
		{
			return parseSetClause(context);
		}
		else if (currentToken() != null)
		{
			addErrorMessage("Unknown section or command \"%s\".", currentLexeme());
			return false;
		}
		else
			return true;
	}

	// Parses a "set" clause.
	private boolean parseSetClause(AbstractPatchContext<?> context) 
	{
		if (matchIdentifierIgnoreCase(KEYWORD_NEXT))
		{
			if (matchIdentifierIgnoreCase(KEYWORD_SPRITE))
			{
				if (!matchIdentifierIgnoreCase(KEYWORD_INDEX))
				{
					addErrorMessage("Expected \"%s\" after \"%s\".", KEYWORD_INDEX, KEYWORD_SPRITE);
					return false;
				}
				
				Integer idx;
				if ((idx = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_INDEX, KEYWORD_SPRITE);
					return false;
				}
				
				// are we able to set the index?
				if (!(context instanceof PatchDSDHackedContext))
				{
					addErrorMessage("Index can only be set if patch type is DSDHACKED or later.");
					return false;
				}
				
				try {
					((PatchDSDHackedContext)context).setNextSpriteIndex(idx);
				} catch (IllegalArgumentException e) {
					addErrorMessage(e.getLocalizedMessage());
					return false;
				}
				
				return true;
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_SOUND))
			{
				if (!matchIdentifierIgnoreCase(KEYWORD_INDEX))
				{
					addErrorMessage("Expected \"%s\" after \"%s\".", KEYWORD_INDEX, KEYWORD_SOUND);
					return false;
				}
				
				Integer idx;
				if ((idx = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_INDEX, KEYWORD_SOUND);
					return false;
				}
				
				// are we able to set the index?
				if (!(context instanceof PatchDSDHackedContext))
				{
					addErrorMessage("Index can only be set if patch type is DSDHACKED or later.");
					return false;
				}
				
				try {
					((PatchDSDHackedContext)context).setNextSoundIndex(idx);
				} catch (IllegalArgumentException e) {
					addErrorMessage(e.getLocalizedMessage());
					return false;
				}
				
				return true;
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_AUTO))
			{
				if (matchIdentifierIgnoreCase(KEYWORD_THING))
				{
					if (!matchIdentifierIgnoreCase(KEYWORD_INDEX))
					{
						addErrorMessage("Expected \"%s\" after \"%s\".", KEYWORD_INDEX, KEYWORD_THING);
						return false;
					}

					Integer idx;
					if ((idx = matchPositiveInteger()) == null)
					{
						addErrorMessage("Expected positive integer after \"%s\".", KEYWORD_INDEX, KEYWORD_SOUND);
						return false;
					}
					
					lastAutoThingIndex = idx;
					return true;
				}
				else
				{
					addErrorMessage("Expected \"%s\" after \"%s\".", KEYWORD_THING, KEYWORD_AUTO);
					return false;
				}
			}
			else
			{
				addErrorMessage("Expected \"%s\", \"%s\", or \"%s\" after \"%s\".", KEYWORD_SPRITE, KEYWORD_SOUND, KEYWORD_AUTO, KEYWORD_NEXT);
				return false;
			}
		}
		else
		{
			addErrorMessage("Expected \"%s\" after \"%s\".", KEYWORD_NEXT, KEYWORD_SET);
			return false;
		}
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
			if (!parseStringEntryList((PatchBoomContext)context))
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
			if (!parseStringEntryList((PatchDoom19Context)context))
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
	private boolean parseStringEntryList(PatchDoom19Context context)
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
				context.setString(stringIndex, currentLexeme());
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
	private boolean parseStringEntryList(PatchBoomContext context) 
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
			else if (currentIsCustomProperty(context, DEHAmmo.class))
			{
				String propertyName = matchIdentifier(); 
				DEHProperty property = context.getCustomPropertyByKeyword(DEHAmmo.class, propertyName);
				
				Object value;
				if ((value = matchNumericExpression(context, null, property.getType().getTypeCheck())) == null)
					return false;

				if (!checkCustomPropertyValue(property, propertyName, value))
					return false;
				
				ammo.setCustomPropertyValue(property, String.valueOf(value));
			}
			else
			{
				addErrorMessage("Expected \"%s\" or \"%s\" or a custom property.", KEYWORD_MAX, KEYWORD_PICKUP);
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
			else if (currentIsCustomProperty(context, DEHSound.class))
			{
				String propertyName = matchIdentifier(); 
				DEHProperty property = context.getCustomPropertyByKeyword(DEHSound.class, propertyName);
				
				Object value;
				if ((value = matchNumericExpression(context, null, property.getType().getTypeCheck())) == null)
					return false;

				if (!checkCustomPropertyValue(property, propertyName, value))
					return false;
				
				sound.setCustomPropertyValue(property, String.valueOf(value));
			}
			else
			{
				addErrorMessage("Expected \"%s\" or \"%s\" or a custom property.", KEYWORD_PRIORITY, KEYWORD_SINGULAR);
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
			
			((PatchBoomContext)context).setParSeconds(map, seconds);
		}

		if (!matchType(DecoHackKernel.TYPE_RBRACE))
		{
			addErrorMessage("Expected '}' after \"%s\" section.", KEYWORD_PARS);
			return false;
		}
		
		return true;
	}
	
	// Parses a miscellany block.
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
			else if (matchIdentifierIgnoreCase(KEYWORD_MONSTER_INFIGHTING2))
			{
				if ((flag = matchBoolean()) == null)
				{
					addErrorMessage("Expected boolean value after \"%s\".", KEYWORD_MONSTER_INFIGHTING2);
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
			else if (currentIsCustomProperty(context, DEHMiscellany.class))
			{
				String propertyName = matchIdentifier(); 
				DEHProperty property = context.getCustomPropertyByKeyword(DEHMiscellany.class, propertyName);
				
				Object val;
				if ((val = matchNumericExpression(context, null, property.getType().getTypeCheck())) == null)
					return false;

				if (!checkCustomPropertyValue(property, propertyName, val))
					return false;
				
				misc.setCustomPropertyValue(property, String.valueOf(val));
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

	// Parses a custom element clause.
	private boolean parseCustomClause(AbstractPatchContext<?> context)
	{
		if (matchIdentifierIgnoreCase(KEYWORD_THING))
		{
			if (matchIdentifierIgnoreCase(KEYWORD_POINTER))
			{
				if (context.getSupportedActionPointerType() == DEHActionPointerType.DOOM19)
				{
					addErrorMessage("Patch type must be Boom or better for custom pointers.");
					return false;
				}

				return parseCustomPointerClause(context, false);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_PROPERTY))
			{
				return parseCustomPropertyClause(context, DEHThing.class);
			}
			else
			{
				addErrorMessage("Expected \"%s\" or \"%s\" after \"%s\".", KEYWORD_POINTER, KEYWORD_PROPERTY, KEYWORD_THING);
				return false;
			}
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_WEAPON))
		{
			if (matchIdentifierIgnoreCase(KEYWORD_POINTER))
			{
				if (context.getSupportedActionPointerType() == DEHActionPointerType.DOOM19)
				{
					addErrorMessage("Patch type must be Boom or better for custom pointers.");
					return false;
				}

				return parseCustomPointerClause(context, true);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_PROPERTY))
			{
				return parseCustomPropertyClause(context, DEHWeapon.class);
			}
			else
			{
				addErrorMessage("Expected \"%s\" or \"%s\" after \"%s\".", KEYWORD_POINTER, KEYWORD_PROPERTY, KEYWORD_WEAPON);
				return false;
			}
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_AMMO))
		{
			if (matchIdentifierIgnoreCase(KEYWORD_PROPERTY))
			{
				return parseCustomPropertyClause(context, DEHAmmo.class);
			}
			else
			{
				addErrorMessage("Expected \"%s\" after \"%s\".", KEYWORD_PROPERTY, KEYWORD_AMMO);
				return false;
			}
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_STATE))
		{
			if (matchIdentifierIgnoreCase(KEYWORD_PROPERTY))
			{
				return parseCustomPropertyClause(context, DEHState.class);
			}
			else
			{
				addErrorMessage("Expected \"%s\" after \"%s\".", KEYWORD_PROPERTY, KEYWORD_STATE);
				return false;
			}
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_SOUND))
		{
			if (matchIdentifierIgnoreCase(KEYWORD_PROPERTY))
			{
				return parseCustomPropertyClause(context, DEHSound.class);
			}
			else
			{
				addErrorMessage("Expected \"%s\" after \"%s\".", KEYWORD_PROPERTY, KEYWORD_SOUND);
				return false;
			}
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_MISC))
		{
			if (matchIdentifierIgnoreCase(KEYWORD_PROPERTY))
			{
				return parseCustomPropertyClause(context, DEHMiscellany.class);
			}
			else
			{
				addErrorMessage("Expected \"%s\" after \"%s\".", KEYWORD_PROPERTY, KEYWORD_MISC);
				return false;
			}
		}
		else
		{
			addErrorMessage("Expected an object type after \"%s\": %s", KEYWORD_CUSTOM, Arrays.toString(ArrayUtils.arrayOf(KEYWORD_MISC, KEYWORD_STATE, KEYWORD_SOUND, KEYWORD_AMMO, KEYWORD_WEAPON, KEYWORD_THING)));
			return false;
		}
	}

	// Parses a custom pointer clause.
	private boolean parseCustomPropertyClause(AbstractPatchContext<?> context, Class<?> objectClass)
	{
		String keyword;
		if ((keyword = matchIdentifier()) == null)
		{
			addErrorMessage("Expected identifier for custom property name.");
			return false;
		}

		if (context.getCustomPropertyByKeyword(objectClass, keyword) != null)
		{
			addErrorMessage("Custom property \"%s\" was already defined for this object type.", keyword);
			return false;
		}

		if (!currentType(DecoHackKernel.TYPE_STRING))
		{
			addErrorMessage("Expected DeHackEd label name after type.");
			return false;
		}
		String dehackedLabel = matchString();
		
		String propertyTypeName;
		if ((propertyTypeName = matchIdentifier()) == null)
		{
			addErrorMessage("Expected identifier for parameter type after \"%s\".", keyword);
			return false;
		}
		
		DEHValueType paramType;
		if ((paramType = DEHValueType.getByName(propertyTypeName)) == null)
		{
			addErrorMessage("Expected valid parameter type: %s", Arrays.toString(DEHValueType.values()));
			return false;
		}

		context.addCustomProperty(objectClass, new DEHProperty(keyword, dehackedLabel, paramType));
		return true;
	}
	
	// Parses a custom pointer clause.
	private boolean parseCustomPointerClause(AbstractPatchContext<?> context, boolean weapon)
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			addErrorMessage("Expected argument type: [boom, mbf, mbf21].");
			return false;
		}
		
		DEHActionPointerType type;
		String typeName = currentLexeme();
		if ((type = DEHActionPointerType.getByName(typeName)) == null)
		{
			addErrorMessage("Expected \"boom\", \"mbf\", or \"mbf21\" for the parameter use type.");
			return false;
		}
		nextToken();

		String pointerName;
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			addErrorMessage("Expected action pointer name (i.e. \"A_PointerName\").");
			return false;
		}

		pointerName = currentLexeme();
		if (!"A_".equalsIgnoreCase(pointerName.substring(0, 2)))
		{
			addErrorMessage("Action pointer name must start with \"A_\".");
			return false;
		}
		nextToken();
		
		// Action mnemonic.
		String pointerMnemonic = pointerName.substring(2);
		
		if (context.getActionPointerByMnemonic(pointerMnemonic) != null)
		{
			addErrorMessage("Action pointer \"%s\" is already defined.", pointerName);
			return false;
		}
		
		// Parameters
		List<DEHValueType> params = new LinkedList<>();
		if (matchType(DecoHackKernel.TYPE_LPAREN))
		{
			String parameterTypeName;
			if (currentType(DecoHackKernel.TYPE_IDENTIFIER))
			{
				do {
					if ((parameterTypeName = matchIdentifier()) == null)
					{
						addErrorMessage("Expected identifier for parameter type after \",\".");
						return false;
					}
					
					DEHValueType paramType;
					if ((paramType = DEHValueType.getByName(parameterTypeName)) == null)
					{
						addErrorMessage("Expected valid parameter type: %s", Arrays.toString(DEHValueType.values()));
						return false;
					}
					if (params.size() >= type.getMaxCustomParams())
					{
						addErrorMessage("Action pointer definition cannot exceed %d parameters for type: %s.", type.getMaxCustomParams(), type.name());
						return false;
					}
					
					params.add(paramType);
					
				} while (matchType(DecoHackKernel.TYPE_COMMA));
			}
			
			if (!matchType(DecoHackKernel.TYPE_RPAREN))
			{
				addErrorMessage("Expected ',' to continue parameter types or ')' to end parameter type list.");
				return false;
			}
		}
		
		DEHValueType[] parameters = params.toArray(new DEHValueType[params.size()]);
		context.addActionPointer(new DEHActionPointerEntry(weapon, type, pointerMnemonic, parameters));
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
				String lexeme = currentLexeme();
				// be a little smart
				if (lexeme != null && lexeme.length() >= 2 && "A_".equalsIgnoreCase(currentLexeme().substring(0, 2)))
				{
					addErrorMessage("Unknown or unsupported action pointer \"%s\".", lexeme);
					return false;
				}
				else
				{
					addErrorMessage("Expected '}' after \"%s\" definition.", KEYWORD_STATE);
					return false;
				}
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
		if ((min = parseStateIndex(context)) != null)
		{
			// protect range
			if (matchIdentifierIgnoreCase(KEYWORD_TO))
			{
				if ((max = parseStateIndex(context)) != null)
				{
					context.setProtectedState(min, max, protectedState);
					return true;
				}
				else
				{
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
			return false;
		}
	}

	// Parses a thing alias line.
	private boolean parseThingAliasLine(AbstractPatchContext<?> context)
	{
		String thingName;
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			addErrorMessage("Expected thing name after \"%s\".", KEYWORD_THING);
			return false;			
		}
		
		if ((thingName = matchIdentifier()) == null)
		{
			addErrorMessage("INTERNAL ERROR: EXPECTED IDENTIFIER FOR THING.");
			return false;
		}
		
		Integer slot;
		if ((slot = context.getThingAlias(thingName)) != null)
		{
			addErrorMessage("Expected valid thing identifier for alias: \"%s\" is already in use!", thingName);
			return false;
		}
		
		if ((slot = matchPositiveInteger()) == null)
		{
			addErrorMessage("Expected a positive integer for the thing slot number after \"%s\".", thingName);
			return false;
		}
		else if ((slot = verifyThingIndex(context, slot)) == null)
		{
			return false;
		}
		
		context.setThingAlias(thingName, slot);
		return true;
	}
	
	// Parses a thing block.
	private boolean parseThingBlock(AbstractPatchContext<?> context)
	{
		// free things?
		if (matchIdentifierIgnoreCase(KEYWORD_FREE))
		{
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
			else
			{
				addErrorMessage("Expected thing index after \"%s\".", KEYWORD_FREE);
				return false;
			}
		}
		
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
		// free thing states.
		else if (matchIdentifierIgnoreCase(KEYWORD_FREE))
		{
			if (matchIdentifierIgnoreCase(KEYWORD_STATES))
			{
				context.freeThingStates(slot);
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
			return parseThingDefinitionBlock(context, slot);
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

		while (currentType(DecoHackKernel.TYPE_NUMBER, DecoHackKernel.TYPE_IDENTIFIER))
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
		if ((min = matchPositiveInteger()) == null)
		{
			addErrorMessage("Expected starting thing index.");
			return false;
		}
		else if ((min = verifyThingIndex(context, min)) == null)
		{
			return false;
		}

		if (!matchIdentifierIgnoreCase(KEYWORD_TO))
		{
			addErrorMessage("Expected \"%s\" after starting thing index.", KEYWORD_TO);
			return false;
		}

		Integer max;
		if ((max = matchPositiveInteger()) == null)
		{
			addErrorMessage("Expected ending thing index after \"%s\".", KEYWORD_TO);
			return false;
		}
		else if ((max = verifyThingIndex(context, max)) == null)
		{
			return false;
		}
		
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
		String thingName;
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			addErrorMessage("Expected thing name after \"%s\".", KEYWORD_THING);
			return false;			
		}
		
		if ((thingName = matchIdentifier()) == null)
		{
			addErrorMessage("INTERNAL ERROR: EXPECTED IDENTIFIER FOR THING.");
			return false;
		}
		
		Integer slot;
		if ((slot = context.getThingAlias(thingName)) != null)
		{
			addErrorMessage("Expected valid thing identifier for new auto-thing: \"%s\" is already in use!", thingName);
			return false;
		}
		
		if ((slot = context.findNextFreeThing(lastAutoThingIndex)) == null)
		{
			addErrorMessage("No more free things for a new auto-thing.");
			return false;
		}
		
		// Save hint.
		lastAutoThingIndex = slot;

		// set thing.
		context.setThingAlias(thingName, slot);
		
		return parseThingDefinitionBlock(context, slot);
	}
	
	// Parses the thing copy clauses, marks the thing as not free, and parses the body.
	private boolean parseThingDefinitionBlock(AbstractPatchContext<?> context, int slot)
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
						addErrorMessage("Expected integer after \"- mbf21\".");
						return false;
					}
				}
				else if ((value = matchPositiveInteger()) != null)
				{
					thing.removeFlag(value);
				}
				else
				{
					addErrorMessage("Expected integer after \"-\".");
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
				if ((value = matchInteger()) == null)
				{
					addErrorMessage("Expected integer after \"%s\".", KEYWORD_MASS);
					return false;
				}				

				// zero-mass check
				if (value == 0 && thing.hasFlag(DEHThingFlag.SHOOTABLE.getValue()))
				{
					addWarningMessage("Thing is SHOOTABLE and mass was set to 0. This may crash certain ports!");
					return false;
				}
				
				thing.setMass(value);
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_FLAGS))
			{
				if ((value = (Integer)matchNumericExpression(context, thing, Type.FLAGS)) == null)
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
				
				if ((value = matchThingIndex(context, true)) == null)
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
			else if (currentIsCustomProperty(context, DEHThing.class))
			{
				String propertyName = matchIdentifier(); 
				DEHProperty property = context.getCustomPropertyByKeyword(DEHThing.class, propertyName);
				
				Object val;
				if ((val = matchNumericExpression(context, thing, property.getType().getTypeCheck())) == null)
					return false;

				if (!checkCustomPropertyValue(property, propertyName, val))
					return false;
				
				thing.setCustomPropertyValue(property, String.valueOf(val));
			}
			else
			{
				addErrorMessage("Expected Thing property, \"%s\" directive, or state block start.", KEYWORD_CLEAR);
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
	
	// Parses a weapon alias line.
	private boolean parseWeaponAliasLine(AbstractPatchContext<?> context)
	{
		String weaponName;
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			addErrorMessage("Expected weapon name after \"%s\".", KEYWORD_THING);
			return false;			
		}
		
		if ((weaponName = matchIdentifier()) == null)
		{
			addErrorMessage("INTERNAL ERROR: EXPECTED IDENTIFIER FOR WEAPON.");
			return false;
		}
		
		Integer slot;
		if ((slot = context.getWeaponAlias(weaponName)) != null)
		{
			addErrorMessage("Expected valid weapon identifier for alias: \"%s\" is already in use!", weaponName);
			return false;
		}
		
		if ((slot = matchPositiveInteger()) == null)
		{
			addErrorMessage("Expected a positive integer for the weapon slot number after \"%s\".", weaponName);
			return false;
		}
		else if ((slot = verifyWeaponIndex(context, slot)) == null)
		{
			return false;
		}
		
		context.setWeaponAlias(weaponName, slot);
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
			if ((other = matchWeaponIndex(context)) == null)
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
			return parseWeaponDefinitionBlock(context, slot);
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
		if ((min = matchPositiveInteger()) == null)
		{
			addErrorMessage("Expected starting weapon index.");
			return false;
		}
		else if ((min = verifyWeaponIndex(context, min)) == null)
		{
			return false;
		}

		if (!matchIdentifierIgnoreCase(KEYWORD_TO))
		{
			addErrorMessage("Expected \"%s\" after starting weapon index.", KEYWORD_TO);
			return false;
		}

		Integer max;
		if ((max = matchPositiveInteger()) == null)
		{
			addErrorMessage("Expected ending weapon index after \"%s\".", KEYWORD_TO);
			return false;
		}
		else if ((max = verifyWeaponIndex(context, max)) == null)
		{
			return false;
		}
		
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
	
	// Parses the weapon copy clauses, marks the weapon as not free, and parses the body.
	private boolean parseWeaponDefinitionBlock(AbstractPatchContext<?> context, int slot)
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
					ammo = Ammo.VALUES.get(ammoIndex);
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
			else if (currentIsCustomProperty(context, DEHWeapon.class))
			{
				String propertyName = matchIdentifier(); 
				DEHProperty property = context.getCustomPropertyByKeyword(DEHWeapon.class, propertyName);
				
				Object val;
				if ((val = matchNumericExpression(context, weapon, property.getType().getTypeCheck())) == null)
					return false;

				if (!checkCustomPropertyValue(property, propertyName, val))
					return false;
				
				weapon.setCustomPropertyValue(property, String.valueOf(val));
			}
			else
			{
				addErrorMessage("Expected Weapon property, \"%s\" directive, or state block start.", KEYWORD_CLEAR);
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
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER, DecoHackKernel.TYPE_STRING))
		{
			addErrorMessage("Expected state label.");
			return false;
		}
		
		FutureLabels futureLabels = new FutureLabels();
		LinkedList<String> labelList = new LinkedList<>();
		ParsedState parsed = new ParsedState();
		StateFillCursor stateCursor = new StateFillCursor();
		
		Integer loopIndex = null;
		boolean startedWithLabel = false;
		while (currentType(DecoHackKernel.TYPE_IDENTIFIER, DecoHackKernel.TYPE_STRING))
		{
			String label = null;
			
			// accumulate labels
			do {
				if (label != null)
				{
					loopIndex = null;
					labelList.add(label);
				}
				
				label = currentLexeme();
				nextToken();
				
			} while (matchType(DecoHackKernel.TYPE_COLON) && currentType(DecoHackKernel.TYPE_IDENTIFIER, DecoHackKernel.TYPE_STRING));
			
			if (!startedWithLabel && labelList.isEmpty())
			{
				addErrorMessage("Expected ':' after \"%s\" - a state label is required, here.", label);
				return false;
			}
			else
			{
				startedWithLabel = true;
			}

			Integer startIndex;
			Integer spriteIndex;
			
			parsed.reset();
			
			if ((spriteIndex = context.getSpriteIndex(label)) == null)
			{
				addErrorMessage("Expected valid sprite name. \"%s\" is not a valid name.", label);
				return false;
			}
			
			if (!parseStateLineData(spriteIndex, context, actor, parsed, false, null))
				return false;
			if ((startIndex = fillStates(context, futureLabels, parsed, stateCursor, false)) == null)
				return false;
			if (loopIndex == null)
				loopIndex = startIndex;
			while (!labelList.isEmpty())
				futureLabels.backfill(context, actor, labelList.pollFirst(), startIndex);
			
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
				
				state.setMBF21Flags(state.getMBF21Flags() | DEHStateMBF21Flag.SKILL5FAST.getValue());
				notModified = false;
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_STATE_NOTFAST))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("MBF21 state flags (e.g. \"%s\") are not available. Not an MBF21 patch.", KEYWORD_STATE_NOTFAST);
					return false;
				}
	
				state.setMBF21Flags(state.getMBF21Flags() & ~DEHStateMBF21Flag.SKILL5FAST.getValue());
				notModified = false;
			}
			else if (currentIsCustomProperty(context, DEHState.class))
			{
				String propertyName = matchIdentifier(); 
				DEHProperty property = context.getCustomPropertyByKeyword(DEHState.class, propertyName);
				
				Object val;
				if ((val = matchNumericExpression(context, null, property.getType().getTypeCheck())) == null)
					return false;

				if (!checkCustomPropertyValue(property, propertyName, val))
					return false;
				
				state.setCustomPropertyValue(property, String.valueOf(val));
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
		Integer spriteIndex;
		if ((spriteIndex = matchSpriteIndexName(context)) == null)
		{
			addErrorMessage("Expected valid sprite name. \"%s\" is not a valid name.", currentLexeme());
			return false;
		}
		
		return parseStateLineData(spriteIndex, context, actor, state, singleFrame, requireAction);
	}

	// Parses the rest of the state line. If true is returned, the input state is altered.
	// requireAction is either true, false, or null. If null, no check is performed. 
	private boolean parseStateLineData(int spriteIndex, AbstractPatchContext<?> context, DEHActor<?> actor, ParsedState state, boolean singleFrame, Boolean requireAction)
	{
		state.spriteIndex = spriteIndex;
		
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
				if (action.pointer == null)
					break;
			}
			
			if (!matchType(DecoHackKernel.TYPE_RBRACE))
			{
				addErrorMessage("Expected a '}' to close a list of valid action pointers.");
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
		{
			return parseThingStateIndex(context);
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_WEAPON))
		{
			return parseWeaponStateIndex(context);
		}
		else if (currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			if (actor == null)
			{
				addErrorMessage("Label '%s' was unexpected. State declaration not for a thing or weapon.", currentLexeme());
				return null;
			}
			return matchIdentifier();
		}
		else if (currentType(DecoHackKernel.TYPE_STRING))
		{
			if (actor == null)
			{
				addErrorMessage("Label '%s' was unexpected. State declaration not for a thing or weapon.", currentLexeme());
				return null;
			}
			return matchString();
		}
		else
		{
			return matchStateIndex(context);
		}
	}

	// Parses a thing or thing state index.
	private Integer parseThingOrThingStateIndex(AbstractPatchContext<?> context) 
	{
		Integer index;
		if ((index = matchThingIndex(context)) == null)
			return null;
		
		if (currentType(DecoHackKernel.TYPE_IDENTIFIER))
			return parseThingLabel(context, index);
		
		return index;
	}
	
	// Parses a thing state index.
	private Integer parseThingStateIndex(AbstractPatchContext<?> context) 
	{
		Integer index;
		if ((index = matchThingIndex(context)) == null)
			return null;
		
		return parseThingLabel(context, index);
	}

	// Parses a thing's label.
	private Integer parseThingLabel(AbstractPatchContext<?> context, Integer index) 
	{
		String labelName;
		DEHThing thing = context.getThing(index);
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			addErrorMessage("Expected thing label name.");
			return null;
		}
		
		labelName = currentLexeme();

		Integer stateIndex;
		if ((stateIndex = thing.getLabel(labelName)) == 0)
		{
			String[] labels = thing.getLabels();
			StringBuilder sb;
			if (labels.length == 0)
			{
				sb = new StringBuilder("Expected a valid thing state label for thing ");
				sb.append(index).append("(").append(thing.getName()).append(") ");
				sb.append(", but it has no state labels. It may be stateless or undefined at this point.");
			}
			else
			{
				sb = new StringBuilder("Expected a valid thing state label for thing ");
				sb.append(index).append("(").append(thing.getName()).append("): ");
				sb.append(Arrays.toString(labels));
				sb.append(".");
			}
			addErrorMessage(sb.toString());
			return null;
		}
		
		nextToken();
		
		return stateIndex;
	}
	
	// Parses a weapon state index.
	private Integer parseWeaponOrWeaponStateIndex(AbstractPatchContext<?> context)
	{
		String labelName;
		Integer index;
		if ((index = matchWeaponIndex(context)) == null)
			return null;
		
		DEHWeapon weapon = context.getWeapon(index);
		
		if (currentType(DecoHackKernel.TYPE_IDENTIFIER))
			return parseWeaponLabel(context, index);

		labelName = currentLexeme();
	
		Integer stateIndex;
		if ((stateIndex = weapon.getLabel(labelName)) == 0)
		{
			String[] labels = weapon.getLabels();
			StringBuilder sb;
			if (labels.length == 0)
			{
				sb = new StringBuilder("Expected a valid thing state label for weapon ");
				sb.append(index).append("(").append(weapon.getName()).append(") ");
				sb.append(", but it has no state labels. It may be stateless or undefined at this point.");
			}
			else
			{
				sb = new StringBuilder("Expected a valid thing state label for weapon ");
				sb.append(index).append("(").append(weapon.getName()).append("): ");
				sb.append(Arrays.toString(labels));
				sb.append(".");
			}
			addErrorMessage(sb.toString());
			return null;
		}
	
		nextToken();
	
		return stateIndex;
	}

	// Parses a weapon state index.
	private Integer parseWeaponStateIndex(AbstractPatchContext<?> context) 
	{
		Integer index;
		if ((index = matchWeaponIndex(context)) == null)
			return null;
		
		return parseWeaponLabel(context, index);
	}

	// Parses a weapon's label.
	private Integer parseWeaponLabel(AbstractPatchContext<?> context, Integer index) 
	{
		String labelName;
		DEHWeapon weapon = context.getWeapon(index);
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			addErrorMessage("Expected weapon label name.");
			return null;
		}
		
		labelName = currentLexeme();

		Integer stateIndex;
		if ((stateIndex = weapon.getLabel(labelName)) == 0)
		{
			String[] labels = weapon.getLabels();
			StringBuilder sb;
			if (labels.length == 0)
			{
				sb = new StringBuilder("Expected a valid weapon state label for weapon ");
				sb.append(index).append("(").append(weapon.getName()).append(") ");
				sb.append(", but it has no state labels. It may be stateless or undefined at this point.");
			}
			else
			{
				sb = new StringBuilder("Expected a valid weapon state label for weapon ");
				sb.append(index).append("(").append(weapon.getName()).append("): ");
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
			addErrorMessage("Expected a valid sound name.");
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
		DEHActionPointer pointer = matchActionPointerName(context);
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
			// Sanity check - shouldn't even be true here, anymore.
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
			if (!pointer.getType().getUseArgs())
			{
				if (matchType(DecoHackKernel.TYPE_LPAREN))
				{
					if (action.offset && pointer.getParams().length > 0)
					{
						addErrorMessage("Cannot use the 'offset' directive on a state with an MBF action function that takes parameters.");
						return false;
					}

					// no arguments
					if (matchType(DecoHackKernel.TYPE_RPAREN))
						return true;

					DEHValueType paramType;
					if ((paramType = pointer.getParam(0)) == null)
					{
						addErrorMessage("Too many args for action %s: this action expects a maximum of %d args.", action.pointer.getMnemonic(), action.pointer.getParams().length);
						return false;
					}
					
					// get first argument
					Object p;
					if ((p = parseParameterValue(paramType, context, actor)) == null)
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
						if ((paramType = pointer.getParam(1)) == null)
						{
							addErrorMessage("Too many args for action %s: this action expects a maximum of %d args.", action.pointer.getMnemonic(), action.pointer.getParams().length);
							return false;
						}
						
						if ((p = parseParameterValue(paramType, context, actor)) == null)
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
						addErrorMessage("Expected a ')' after action parameters. Are you adding too many parameters?");
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
						
						DEHValueType paramType;
						if ((paramType = pointer.getParam(argIndex)) == null)
						{
							addErrorMessage("Too many args for action %s: this action expects a maximum of %d args.", action.pointer.getMnemonic(), action.pointer.getParams().length);
							return false;
						}
						
						Object p;
						if ((p = parseParameterValue(paramType, context, actor)) == null)
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
							addErrorMessage("Expected a ')' after action parameters. Are you adding too many parameters?");
							return false;
						}
					}
				}
			}
		}
		
		return true;
	}

	// Parses a parameter value.
	private Object parseParameterValue(DEHValueType paramType, AbstractPatchContext<?> context, DEHActor<?> actor)
	{
		// Force value interpretation.
		if (matchIdentifierIgnoreCase(KEYWORD_THING))
		{
			if (paramType == DEHValueType.THING || paramType == DEHValueType.THINGMISSILE)
				addWarningMessage("The use of a \"thing\" clause as a parameter in an action pointer is unneccesary. You can just use an index or a thing alias.");
			
			Integer thingIndex;
			if ((thingIndex = parseThingOrThingStateIndex(context)) == null)
				return null;
			
			// Verify missile type.
			if (paramType == DEHValueType.THINGMISSILE)
			{
				DEHThing thing = context.getThing(thingIndex);
				if ((thing.getFlags() & DEHFlag.flags(DEHThingFlag.MISSILE)) == 0)
					addWarningMessage("This action pointer requires a Thing that is flagged with MISSILE.");
			}
			
			return thingIndex;
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_WEAPON))
		{
			if (paramType == DEHValueType.WEAPON)
				addWarningMessage("The use of a \"weapon\" clause as a parameter in an action pointer is unneccesary. You can just use an index or a weapon alias.");
			return parseWeaponOrWeaponStateIndex(context);
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_SOUND))
		{
			if (paramType == DEHValueType.SOUND)
				addWarningMessage("The use of a \"sound\" clause as a parameter in an action pointer is unneccesary. You can just use the sound name.");
			return parseSoundIndex(context);
		}
		else if (matchIdentifierIgnoreCase(KEYWORD_FLAGS))
		{
			if (paramType == DEHValueType.FLAGS)
				addWarningMessage("The use of a \"flags\" clause as a parameter in an action pointer is unneccesary. You can just write flags as-is.");
			return matchNumericExpression(context, actor, Type.FLAGS);
		}
		// Guess it.
		else 
			return matchNumericExpression(context, actor, paramType.getTypeCheck());
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
	private Integer fillStates(AbstractPatchContext<?> context, FutureLabels labels, ParsedState parsedState, StateFillCursor cursor, boolean forceFirst)
	{
		Integer out = null;
		boolean isBoom = context.supports(DEHFeatureLevel.BOOM);
		
		while (!parsedState.frameList.isEmpty())
		{
			Integer frame = parsedState.frameList.pollFirst();
			
			for (int i = 0; i < parsedState.parsedActions.size(); i++)
			{
				ParsedAction parsedAction = parsedState.parsedActions.get(i);
				
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
					parsedAction.applyLabels(currentIndex, labels);
				
				fillState
					.setSpriteIndex(parsedState.spriteIndex)
					.setFrameIndex(frame)
					.setDuration(i == parsedState.parsedActions.size() - 1 ? parsedState.duration : 0) // only write duration to last state.
					.setBright(parsedState.bright)
					.setMisc1(parsedAction.misc1)
					.setMisc2(parsedAction.misc2)
					.setArgs(parsedAction.args)
				;
				
				if (parsedState.mbf21Flags != null && fillState.getMBF21Flags() != parsedState.mbf21Flags)
					fillState.setMBF21Flags(parsedState.mbf21Flags);

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

		switch (currentLexeme().toLowerCase())
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
		else if (currentIsNextStateKeyword()) // Exclude control keywords if identifier token type.
			return false;
		else
		{
			String sprite = currentLexeme();
			if (sprite.length() != 4)
				return false;
			return patch.getSpriteIndex(sprite) != null;
		}
	}

	// Checks if the current token is an identifier with a specific lexeme.
	private boolean currentIdentifierIgnoreCase(String lexeme)
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
			return false;
		if (!currentLexeme().equalsIgnoreCase(lexeme))
			return false;
		return true;
	}

	// Tests for an identifier that references a thing state name.
	private boolean currentIsThingState()
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
			return false;
		
		switch (currentLexeme().toLowerCase())
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
		
		switch (currentLexeme().toLowerCase())
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

	// Checks if the current identifier is a custom property.
	private boolean currentIsCustomProperty(AbstractPatchContext<?> context, Class<?> objectType)
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
			return false;
		if (context.getCustomPropertyByKeyword(objectType, currentLexeme()) == null)
			return false;
		
		return true;
	}

	// Checks the property value.
	private boolean checkCustomPropertyValue(DEHProperty property, String propertyName, Object value) 
	{
		if (value instanceof Integer)
		{
			DEHValueType param = property.getType();
			if (param.isValueCheckable() && !param.isValueValid((Integer)value))
			{
				addErrorMessage("Invalid value '%d' for property '%s': value must be between %d and %d.", value, propertyName, param.getValueMin(), param.getValueMax());
				return false;
			}
		}
		else if (value instanceof String)
		{
			if (property.getType() != DEHValueType.STRING)
			{
				addErrorMessage("Invalid value '%d' for property '%s': value must be a string.", value, propertyName);
				return false;
			}
		}
		
		return true;
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

	// Matches a valid nonzero thing index number.
	// If match, advance token and return integer.
	// Else, return null.
	private Integer matchThingIndex(AbstractPatchContext<?> context)
	{
		return matchThingIndex(context, false);
	}
	
	// Matches a valid thing index number.
	// If match, advance token and return integer.
	// Else, return null.
	private Integer matchThingIndex(AbstractPatchContext<?> context, boolean allowZero)
	{
		Integer slot;
		String autoThingName;
		
		if ((autoThingName = matchIdentifier()) != null)
		{
			if ((slot = context.getThingAlias(autoThingName)) == null)
			{
				addErrorMessage("Expected valid thing alias: \"%s\" is not a valid alias.", autoThingName);
				return null;
			}
			else
			{
				return slot;
			}
		}
		else if ((slot = matchPositiveInteger()) != null)
		{
			if (allowZero && slot == 0)
				return 0;
			
			return verifyThingIndex(context, slot);
		}
		else
		{
			addErrorMessage("Expected positive integer or alias for the thing slot.");
			return null;
		}		
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
		String autoWeaponName;
		
		if ((autoWeaponName = matchIdentifier()) != null)
		{
			if ((slot = context.getWeaponAlias(autoWeaponName)) == null)
			{
				addErrorMessage("Expected valid weapon identifier: \"%s\" is not a valid alias.", autoWeaponName);
				return null;
			}
			else
			{
				return slot;
			}
		}
		else if ((slot = matchPositiveInteger()) == null)
		{
			addErrorMessage("Expected positive integer or alias for the weapon slot.");
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
		String out = currentLexeme();
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
		if ((out = patch.getSpriteIndex(currentLexeme())) == null)
			return null;
		nextToken();
		return out;
	}
	
	// Matches an identifier that is interpreted to be a list of subframe indices.
	// If match, advance token and returns the list of indices.
	// Else, return null.
	private Deque<Integer> matchFrameIndices()
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
			return null;

		Deque<Integer> frameList = new LinkedList<>();
		String lexeme = currentLexeme();
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
				state.mbf21Flags |= DEHStateMBF21Flag.SKILL5FAST.getValue();
			}
			else if (matchIdentifierIgnoreCase(KEYWORD_STATE_NOTFAST))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("MBF21 state flags (e.g. \"%s\") are not available. Not an MBF21 patch.", KEYWORD_STATE_NOTFAST);
					return false;
				}
				state.mbf21Flags = state.mbf21Flags != null ? state.mbf21Flags : 0;
				state.mbf21Flags &= ~DEHStateMBF21Flag.SKILL5FAST.getValue();
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
		DEHFlag flag;
		if ((flag = DEHThingFlag.getByMnemonic(currentLexeme())) != null)
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

		Integer out = null;
		DEHThingMBF21Flag flag;
		if ((flag = DEHThingMBF21Flag.getByMnemonic(currentLexeme())) != null)
		{
			out = flag.getValue();

			if (!context.supports(DEHFeatureLevel.MBF21))
			{
				addErrorMessage("MBF21 thing flags are not available. Not an MBF21 patch.");
				return null;
			}

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
		
		Integer out = null;
		DEHWeaponMBF21Flag flag;
		if ((flag = DEHWeaponMBF21Flag.getByMnemonic(currentLexeme())) != null)
		{
			out = flag.getValue();

			if (!context.supports(DEHFeatureLevel.MBF21))
			{
				addErrorMessage("MBF21 weapon flags are not available. Not an MBF21 patch.");
				return null;
			}

			nextToken();
		}
		
		return out;
	}

	// Matches an identifier or string that references an action pointer name.
	// If match, advance token and return action pointer.
	// Else, return null.
	private DEHActionPointer matchActionPointerName(AbstractPatchContext<?> context)
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
			return null;
	
		String lexeme = currentLexeme();
		DEHActionPointer out;
		if (lexeme.length() < 2 || !lexeme.substring(0, 2).toUpperCase().startsWith("A_"))
			return null;
		if ((out = context.getActionPointerByMnemonic(lexeme.substring(2))) == null)
		{
			addErrorMessage("Action pointer \"%s\" is not known or invalid.", lexeme);
			return null;
		}
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
		
		String name = currentLexeme();
		
		Integer out;
		if (name.length() == 0)
		{
			nextToken();
			return 0;
		}
		if (name.length() > 6)
		{
			addErrorMessage("Sound name \"%s\" is invalid - sounds cannot exceed 6 characters.", name);
			return null;
		}
		if ((out = patch.getSoundIndex(name)) == null)
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
		
		String lexeme = currentLexeme();
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
			EpisodeMap out = EpisodeMap.create(0, Integer.parseInt(lexeme.substring(3, lexeme.length())));
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
		String out = currentLexeme();
		nextToken();
		return out;
	}

	// Matches and parses a numeric expression.
	// STATE type can return a String. Everything else is an Integer, or null.
	private Object matchNumericExpression(AbstractPatchContext<?> context, DEHActor<?> actor, Type typeCheck)
	{
		Integer out = null;
		
		switch (typeCheck)
		{
			default:
			{
				addErrorMessage("INTERNAL ERROR: Unrecognized parameter type.");
				return null;
			}
			
			case INTEGER:
			{
				if ((out = matchInteger()) == null)
				{
					addErrorMessage("Expected integer value.");
					return null;
				}
				return out;
			}
			
			case FIXED:
			{
				if ((out = matchFixed()) == null)
				{
					addErrorMessage("Expected fixed-point value.");
					return null;
				}
				return out;
			}
			
			case FLAGS:
			{
				Integer value;
				
				// Check for accidental flag mixing.
				int flagInclusionMask = 0;
				final int FIM_THING_DOOM19 = 0x01;
				final int FIM_THING_MBF21 = 0x02;
				final int FIM_WEAPON_MBF21 = 0x04;
				
				while (currentType(DecoHackKernel.TYPE_DASH, DecoHackKernel.TYPE_NUMBER, DecoHackKernel.TYPE_IDENTIFIER))
				{
					if (out == null)
						out = 0;
					
					if (currentType(DecoHackKernel.TYPE_DASH, DecoHackKernel.TYPE_NUMBER))
					{
						if ((value = matchInteger()) == null)
						{
							addErrorMessage("Expected integer value.");
							return null;
						}
						out |= value;
					}
					else if ((value = matchThingFlagMnemonic()) != null)
					{
						out |= value;
						
						flagInclusionMask |= FIM_THING_DOOM19;
						if ((flagInclusionMask & ~FIM_THING_DOOM19) != 0)
						{
							addErrorMessage("Attempted to mix different flag mnemonic types!");
							return null;
						}
					}
					else if ((value = matchThingMBF21FlagMnemonic(context)) != null)
					{
						out |= value;

						flagInclusionMask |= FIM_THING_MBF21;
						if ((flagInclusionMask & ~FIM_THING_MBF21) != 0)
						{
							addErrorMessage("Attempted to mix different flag mnemonic types!");
							return null;
						}
					}
					else if ((value = matchWeaponMBF21FlagMnemonic(context)) != null)
					{
						out |= value;
						
						flagInclusionMask |= FIM_WEAPON_MBF21;
						if ((flagInclusionMask & ~FIM_WEAPON_MBF21) != 0)
						{
							addErrorMessage("Attempted to mix different flag mnemonic types!");
							return null;
						}
					}
					else // expression not started. Maybe label.
					{
						addErrorMessage("Expected valid flag mnemonic or integer.");
						return null;
					}
					
					if (!currentType(DecoHackKernel.TYPE_PIPE, DecoHackKernel.TYPE_PLUS))
						break;
					else
						nextToken();
				}
				
				return out;
			}
			
			case STATE:
			{
				Object value;
				if ((value = parseStateIndex(context, actor)) == null)
				{
					addErrorMessage("Expected valid state: positive integer, or thing/weapon/local state label.");
					return null;
				}
				
				return value;
			}
			
			case THING:
			{
				Object value;
				if ((value = matchThingIndex(context)) == null)
				{
					addErrorMessage("Expected valid thing index: positive integer, or thing alias.");
					return null;
				}
				
				return value;
			}

			case THINGMISSILE:
			{
				Object value;
				if ((value = matchThingIndex(context)) == null)
				{
					addErrorMessage("Expected valid thing index: positive integer, or thing alias.");
					return null;
				}
				
				Integer index = (Integer)value;
				
				// Verify missile type.
				DEHThing thing = context.getThing(index);
				if ((thing.getFlags() & DEHFlag.flags(DEHThingFlag.MISSILE)) == 0)
					addWarningMessage("This action pointer requires a Thing that is flagged with MISSILE.");
				
				return value;
			}
			
			case WEAPON:
			{
				Object value;
				if ((value = matchWeaponIndex(context)) == null)
				{
					addErrorMessage("Expected valid weapon index: positive integer, or weapon alias.");
					return null;
				}
				
				return value;
			}
			
			case SOUND:
			{
				Object value;
				if ((value = parseSoundIndex(context)) == null)
					return null;
				
				return value;
			}

			case STRING:
			{
				Object value;
				if ((value = matchString()) == null)
				{
					addErrorMessage("Expected string.");
					return null;
				}
				
				return value;
			}
		}
	}

	// Matches a positive integer.
	private Integer matchPositiveInteger()
	{
		if (!currentType(DecoHackKernel.TYPE_NUMBER))
			return null;
		
		String lexeme = currentLexeme();
		// Always take hex numbers as raw.
		if (lexeme.startsWith("0X") || lexeme.startsWith("0x"))
		{
			long v = parseUnsignedHexLong(lexeme.substring(2));
			if (v > (long)Integer.MAX_VALUE || v < (long)Integer.MIN_VALUE)
				return null;
			nextToken();
			return (int)v;
		}
		// Fixed - coerce to whole number.
		else if (lexeme.contains("."))
		{
			addWarningMessage("Found fixed-point, but will be converted to integer.");
			try {
				int out = (int)(Double.parseDouble(lexeme));
				nextToken();
				return out;
			} catch (NumberFormatException e) {
				return null;
			}
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

	// Matches a positive fixed-point number.
	private Integer matchPositiveFixed()
	{
		if (!currentType(DecoHackKernel.TYPE_NUMBER))
			return null;
		
		String lexeme = currentLexeme();
		// Always take hex numbers as raw.
		if (lexeme.startsWith("0X") || lexeme.startsWith("0x"))
		{
			addWarningMessage("Expected fixed-point - hex numbers are interpreted as-is.");
			long v = parseUnsignedHexLong(lexeme.substring(2));
			if (v > (long)Integer.MAX_VALUE || v < (long)Integer.MIN_VALUE)
				return null;
			nextToken();
			return ((int)v);
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
		// Whole number - coerce to fixed.
		else
		{
			addWarningMessage("Found integer, but will be converted to fixed-point.");
			long v = Long.parseLong(lexeme);
			if (v > (long)Integer.MAX_VALUE || v < (long)Integer.MIN_VALUE)
				return null;
			nextToken();
			return ((int)v) << 16;
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

	// Matches a fixed-point value.
	private Integer matchFixed()
	{
		if (matchType(DecoHackKernel.TYPE_DASH))
		{
			Integer out;
			if ((out = matchPositiveFixed()) == null)
				return null;
			return -out;
		}
		return matchPositiveFixed();
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

		DEHValueType param = action.getParam(index);
		if (param.isValueCheckable() && !param.isValueValid(value))
		{
			addErrorMessage("Invalid value '%d' for %s arg %d: value must be between %d and %d.", value, action.getMnemonic(), index, param.getValueMin(), param.getValueMax());
			return false;
		}
		
		return true;
	}
	
	// =======================================================================

	/** List of errors. */
	private LinkedList<String> errors;
	/** List of warnings. */
	private LinkedList<String> warnings;
	/** Editor directives. */
	private Map<String, String> editorKeys;
	/** Last auto thing index (for slightly better search continuation). */
	private int lastAutoThingIndex;

	// Return the exporter for the patch.
	private DecoHackParser(String streamName, InputStream in, Charset inputCharset)
	{
		super(new DecoHackLexer(streamName, in != null ? new InputStreamReader(in, inputCharset) : null, inputCharset));
		this.warnings = new LinkedList<>();
		this.errors = new LinkedList<>();
		this.editorKeys = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		this.lastAutoThingIndex = 0;
	}
	
	private void addWarningMessage(String message, Object... args)
	{
		warnings.add(getTokenInfoLine(String.format(message, args)));
	}
	
	private String[] getWarningMessages()
	{
		String[] out = new String[warnings.size()];
		warnings.toArray(out);
		return out;
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
		
		return noError ? context : null;
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
	public enum FieldType
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
		
		private static final Map<Integer, FieldType> VALUES = EnumUtils.createOrdinalMap(FieldType.class);
		
		public static FieldType getArg(int i)
		{
			return VALUES.get(i + 3);
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
		
		void applyLabels(int stateIndex, FutureLabels labels)
		{
			for (FieldSet fs : labelFields)
			{
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

		private DecoHackLexer(String streamName, Reader reader, final Charset encoding)
		{
			super(KERNEL, streamName, reader);
			setIncluder(new PreprocessorLexer.Includer() 
			{
				private final Map<String, String> SPECIAL_INCLUDES = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER)
				{
					private static final long serialVersionUID = -7828739256854493701L;
					{
						put("<doom19>", "classpath:decohack/doom19.dh");
						put("<udoom19>", "classpath:decohack/udoom19.dh");
						put("<doomunity>", "classpath:decohack/doomunity.dh");
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
						return DEFAULT_INCLUDER.getIncludeResourcePath(streamName, foundPath);
					}
					else
					{
						return DEFAULT_INCLUDER.getIncludeResourcePath(streamName, path);
					}
				}

				@Override
				public InputStream getIncludeResource(String path) throws IOException 
				{
					return DEFAULT_INCLUDER.getIncludeResource(path);
				}
				
				@Override
				public Charset getEncodingForIncludedResource(String path) 
				{
					return encoding;
				}
			});
		}
	}

}

/*******************************************************************************
 * Copyright (c) 2020-2025 Matt Tropiano
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
import net.mtrop.doom.tools.decohack.data.enums.DEHValueType;
import net.mtrop.doom.tools.decohack.data.enums.DEHFeatureLevel;
import net.mtrop.doom.tools.decohack.data.enums.DEHFlag;
import net.mtrop.doom.tools.decohack.data.enums.DEHStateMBF21Flag;
import net.mtrop.doom.tools.decohack.data.enums.DEHThingFlag;
import net.mtrop.doom.tools.decohack.data.enums.DEHThingID24Flag;
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

	private interface Keyword
	{
		String MISC = "misc";
		String MAX_ARMOR = "maxArmor";
		String MAX_HEALTH = "maxHealth";
		String BFG_CELLS_PER_SHOT = "bfgCellsPerShot";
		String IDKFA_ARMOR_CLASS = "idkfaArmorClass";
		String IDKFA_ARMOR = "idkfaArmor";
		String IDFA_ARMOR_CLASS = "idfaArmorClass";
		String IDFA_ARMOR = "idfaArmor";
		String GOD_MODE_HEALTH = "godModeHealth";
		String MAX_SOULSPHERE_HEALTH = "maxSoulsphereHealth";
		String MEGASPHERE_HEALTH = "megasphereHealth";
		String SOULSPHERE_HEALTH = "soulsphereHealth";
		String BLUE_ARMOR_CLASS = "blueArmorClass";
		String GREEN_ARMOR_CLASS = "greenArmorClass";
		String INITIAL_HEALTH = "initialHealth";
		String INITIAL_BULLETS = "initialBullets";
		String MONSTER_INFIGHTING = "monsterInfighting";
		String MONSTER_INFIGHTING2 = "monstersFightOwnSpecies";
		
		String PARS = "pars";
		
		String SET = "set";
		String SAFETY = "safety";
		String ON = "on";
		String OFF = "off";
		String NEXT = "next";
		String INDEX = "index";
		String CLEAR = "clear";
		String STATE = "state";
		String FILL = "fill";
		String GOTO = "goto";
		String LOOP = "loop";
		String WAIT = "wait";
		String STOP = "stop";
		String FREE = "free";
		String PROTECT = "protect";
		String UNPROTECT = "unprotect";
		String PROPERTIES = "properties";
		String SPRITENAME = "spritename";
		String FRAME = "frame";
		String DURATION = "duration";
		String NEXTSTATE = "nextstate";
		String POINTER = "pointer";
		String PROPERTY = "property";

		String ALIAS = "alias";
		String AUTO = "auto";
		String EACH = "each";
		String IN = "in";
		String TO = "to";
		String FROM = "from";
		
		String SOUND = "sound";
		String SOUNDS = "sounds";
		String SINGULAR = "singular";
		String PRIORITY = "priority";

		String SPRITE = "sprite";

		String AMMO = "ammo";
		String PICKUP = "pickup";
		String MAX = "max";
		String INITIALAMMO = "initialAmmo";
		String MAXUPGRADEDAMMO = "maxUpgradedAmmo";
		String BOXAMMO = "boxAmmo";
		String BACKPACKAMMO = "backpackAmmo";
		String WEAPONAMMO = "weaponAmmo";
		String DROPPEDAMMO = "droppedAmmo";
		String DROPPEDBOXAMMO = "droppedBoxAmmo";
		String DROPPEDBACKPACKAMMO = "droppedBackpackAmmo";
		String DROPPEDWEAPONAMMO = "droppedWeaponAmmo";
		String DEATHMATCHWEAPONAMMO = "deathmatchWeaponAmmo";
		String SKILL1MULTIPLIER = "skill1Multiplier";
		String SKILL2MULTIPLIER = "skill2Multiplier";
		String SKILL3MULTIPLIER = "skill3Multiplier";
		String SKILL4MULTIPLIER = "skill4Multiplier";
		String SKILL5MULTIPLIER = "skill5Multiplier";
		
		String STRINGS = "strings";
		
		String STATES = "states";
		
		String WEAPON = "weapon";
		String AMMOTYPE = "ammotype";
		String WEAPONSTATE_READY = DEHWeapon.STATE_LABEL_READY;
		String WEAPONSTATE_SELECT = DEHWeapon.STATE_LABEL_SELECT;
		String WEAPONSTATE_DESELECT = DEHWeapon.STATE_LABEL_DESELECT;
		String WEAPONSTATE_FIRE = DEHWeapon.STATE_LABEL_FIRE;
		String WEAPONSTATE_FLASH = DEHWeapon.STATE_LABEL_FLASH;
		String AMMOPERSHOT = "ammopershot";
		String SLOT = "slot";
		String SLOTPRIORITY = "slotPriority";
		String SWITCHPRIORITY = "switchPriority";
		String INITIALOWNED = "initialOwned";
		String INITIALRAISED = "initialRaised";
		String CAROUSELICON = "carouselIcon";
		String ALLOWSWITCHWITHOWNEDWEAPON = "allowSwitchWithOwnedWeapon";
		String NOSWITCHWITHOWNEDWEAPON = "noSwitchWithOwnedWeapon";
		String ALLOWSWITCHWITHOWNEDITEM = "allowSwitchWithOwnedItem";
		String NOSWITCHWITHOWNEDITEM = "noSwitchWithOwnedItem";
		
		String THING = "thing";
		String THINGSTATE_SPAWN = DEHThing.STATE_LABEL_SPAWN;
		String THINGSTATE_SEE = DEHThing.STATE_LABEL_SEE;
		String THINGSTATE_MELEE = DEHThing.STATE_LABEL_MELEE;
		String THINGSTATE_MISSILE = DEHThing.STATE_LABEL_MISSILE;
		String THINGSTATE_PAIN = DEHThing.STATE_LABEL_PAIN;
		String THINGSTATE_DEATH = DEHThing.STATE_LABEL_DEATH;
		String THINGSTATE_XDEATH = DEHThing.STATE_LABEL_XDEATH;
		String THINGSTATE_RAISE = DEHThing.STATE_LABEL_RAISE;
		String EDNUM = "ednum";
		String FLAGS = "flags";
		String MASS = "mass";
		String MELEERANGE = "meleerange";
		String PAINCHANCE = "painchance";
		String REACTIONTIME = "reactiontime";
		String HEALTH = "health";
		String DAMAGE = "damage";
		String HEIGHT = "height";
		String RADIUS = "radius";
		String SPEED = "speed";
		String FASTSPEED = "fastspeed";
		String DROPITEM = "dropitem";
		String INFIGHTINGGROUP = "infightinggroup";
		String PROJECTILEGROUP = "projectilegroup";
		String SPLASHGROUP = "splashgroup";
		String SEESOUND = "seesound";
		String ATTACKSOUND = "attacksound";
		String PAINSOUND = "painsound";
		String DEATHSOUND = "deathsound";
		String ACTIVESOUND = "activesound";
		String RIPSOUND = "ripsound";
		String MINRESPAWNTICS = "minRespawnTics";
		String RESPAWNDICE = "respawnDice";
		String PICKUPAMMOTYPE = "pickupAmmoType";
		String PICKUPAMMOCATEGORY = "pickupAmmoCategory";
		String PICKUPWEAPONTYPE = "pickupWeaponType";
		String PICKUPITEMTYPE = "pickupItemType";
		String PICKUPBONUSCOUNT = "pickupBonusCount";
		String PICKUPMESSAGE = "pickupMessage";
		String TRANSLATION = "translation";
		String PICKUPSOUND = "pickupSound";

		String OFFSET = "offset";
		String STATE_BRIGHT = "bright";
		String STATE_NOTBRIGHT = "notbright";
		String STATE_FAST = "fast";
		String STATE_NOTFAST = "notfast";
		String TRANMAP = "tranmap";
		
		String WITH = "with";
		String SWAP = "swap";

		String USING = "using";
		String MBF21 = "mbf21";
		String ID24 = "id24";

		String CUSTOM = "custom";
		
	}

	private static final String VALUE_NULL = "null";
	
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
		if (!matchIdentifierIgnoreCase(Keyword.USING))
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
		if (matchIdentifierIgnoreCase(Keyword.STRINGS))
			return parseStringBlock(context);
		else if (matchIdentifierIgnoreCase(Keyword.AMMO))
			return parseAmmoBlock(context);
		else if (matchIdentifierIgnoreCase(Keyword.SOUND))
			return parseSoundBlock(context);
		else if (matchIdentifierIgnoreCase(Keyword.STATE))
			return parseStateBlock(context);
		else if (matchIdentifierIgnoreCase(Keyword.PARS))
			return parseParBlock(context);
		else if (matchIdentifierIgnoreCase(Keyword.THING))
			return parseThingBlock(context);
		else if (matchIdentifierIgnoreCase(Keyword.WEAPON))
			return parseWeaponBlock(context);
		else if (matchIdentifierIgnoreCase(Keyword.MISC))
			return parseMiscellaneousBlock(context);
		else if (matchIdentifierIgnoreCase(Keyword.CUSTOM))
			return parseCustomClause(context);
		else if (matchIdentifierIgnoreCase(Keyword.EACH))
		{
			if (matchIdentifierIgnoreCase(Keyword.THING))
				return parseThingEachBlock(context);
			else if (matchIdentifierIgnoreCase(Keyword.WEAPON))
				return parseWeaponEachBlock(context);
			else
			{
				addErrorMessage("Expected \"%s\" or \"%s\" after \"%s\".", Keyword.THING, Keyword.WEAPON, Keyword.EACH);
				return false;
			}
		}
		else if (matchIdentifierIgnoreCase(Keyword.AUTO))
		{
			if (matchIdentifierIgnoreCase(Keyword.THING))
				return parseThingAutoBlock(context);
			else if (matchIdentifierIgnoreCase(Keyword.WEAPON))
				return parseWeaponAutoBlock(context);
			else if (matchIdentifierIgnoreCase(Keyword.AMMO))
				return parseAmmoAutoBlock(context);
			else
			{
				addErrorMessage("Expected \"%s\", \"%s\", or \"%s\" after \"%s\".", Keyword.THING, Keyword.WEAPON, Keyword.AMMO, Keyword.AUTO);
				return false;
			}
		}
		else if (matchIdentifierIgnoreCase(Keyword.ALIAS))
		{
			if (matchIdentifierIgnoreCase(Keyword.THING))
				return parseThingAliasLine(context);
			else if (matchIdentifierIgnoreCase(Keyword.WEAPON))
				return parseWeaponAliasLine(context);
			else if (matchIdentifierIgnoreCase(Keyword.AMMO))
				return parseAmmoAliasLine(context);
			else
			{
				addErrorMessage("Expected \"%s\" or \"%s\" or \"%s\" after \"%s\".", Keyword.THING, Keyword.WEAPON, Keyword.AMMO, Keyword.ALIAS);
				return false;
			}
		}
		else if (matchIdentifierIgnoreCase(Keyword.SET))
		{
			return parseSetClause(context);
		}
		else if (currentIdentifierIgnoreCase(Keyword.USING))
		{
			addErrorMessage("Keyword \"%s\" already seen. Did you use an #include that declared it already?", Keyword.USING);
			return false;
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
		if (matchIdentifierIgnoreCase(Keyword.STATE))
		{
			if (!matchIdentifierIgnoreCase(Keyword.FREE))
			{
				addErrorMessage("Expected \"%s\" after \"%s\".", Keyword.FREE, Keyword.STATE);
				return false;
			}
			
			if (!matchIdentifierIgnoreCase(Keyword.SAFETY))
			{
				addErrorMessage("Expected \"%s\" after \"%s\".", Keyword.SAFETY, Keyword.FREE);
				return false;
			}
			
			if (matchIdentifierIgnoreCase(Keyword.ON))
			{
				context.setStateSafetySwitch(true);
				return true;
			}
			else if (matchIdentifierIgnoreCase(Keyword.OFF))
			{
				context.setStateSafetySwitch(false);
				return true;
			}
			else
			{
				addErrorMessage("Expected \"%s\" or \"%s\" after \"%s\".", Keyword.ON, Keyword.OFF, Keyword.SAFETY);
				return false;
			}
		}
		else if (matchIdentifierIgnoreCase(Keyword.NEXT))
		{
			if (matchIdentifierIgnoreCase(Keyword.SPRITE))
			{
				if (!matchIdentifierIgnoreCase(Keyword.INDEX))
				{
					addErrorMessage("Expected \"%s\" after \"%s\".", Keyword.INDEX, Keyword.SPRITE);
					return false;
				}
				
				Integer idx;
				if ((idx = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.INDEX, Keyword.SPRITE);
					return false;
				}
				
				// are we able to set the index?
				if (!context.supports(DEHFeatureLevel.DSDHACKED))
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
			else if (matchIdentifierIgnoreCase(Keyword.SOUND))
			{
				if (!matchIdentifierIgnoreCase(Keyword.INDEX))
				{
					addErrorMessage("Expected \"%s\" after \"%s\".", Keyword.INDEX, Keyword.SOUND);
					return false;
				}
				
				Integer idx;
				if ((idx = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.INDEX, Keyword.SOUND);
					return false;
				}
				
				// are we able to set the index?
				if (!context.supports(DEHFeatureLevel.DSDHACKED))
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
			else if (matchIdentifierIgnoreCase(Keyword.AUTO))
			{
				if (matchIdentifierIgnoreCase(Keyword.THING))
				{
					if (!matchIdentifierIgnoreCase(Keyword.INDEX))
					{
						addErrorMessage("Expected \"%s\" after \"%s\".", Keyword.INDEX, Keyword.THING);
						return false;
					}

					Integer idx;
					if ((idx = matchPositiveInteger()) == null)
					{
						addErrorMessage("Expected positive integer after \"%s\".", Keyword.INDEX);
						return false;
					}
					
					lastAutoThingIndex = idx;
				}
				else if (matchIdentifierIgnoreCase(Keyword.WEAPON))
				{
					if (!matchIdentifierIgnoreCase(Keyword.INDEX))
					{
						addErrorMessage("Expected \"%s\" after \"%s\".", Keyword.INDEX, Keyword.WEAPON);
						return false;
					}

					Integer idx;
					if ((idx = matchPositiveInteger()) == null)
					{
						addErrorMessage("Expected positive integer after \"%s\".", Keyword.INDEX);
						return false;
					}
					
					lastAutoWeaponIndex = idx;
				}
				else if (matchIdentifierIgnoreCase(Keyword.AMMO))
				{
					if (!matchIdentifierIgnoreCase(Keyword.INDEX))
					{
						addErrorMessage("Expected \"%s\" after \"%s\".", Keyword.INDEX, Keyword.AMMO);
						return false;
					}

					Integer idx;
					if ((idx = matchPositiveInteger()) == null)
					{
						addErrorMessage("Expected positive integer after \"%s\".", Keyword.INDEX);
						return false;
					}
					
					lastAutoAmmoIndex = idx;
				}
				else
				{
					addErrorMessage("Expected \"%s\", \"%s\", \"%s\" after \"%s\".", Keyword.THING, Keyword.WEAPON, Keyword.AMMO, Keyword.AUTO);
					return false;
				}

				return true;
			}
			else
			{
				addErrorMessage("Expected \"%s\", \"%s\", or \"%s\" after \"%s\".", Keyword.SPRITE, Keyword.SOUND, Keyword.AUTO, Keyword.NEXT);
				return false;
			}
		}
		else
		{
			addErrorMessage("Expected \"%s\" or \"%s\" after \"%s\".", Keyword.NEXT, Keyword.STATE, Keyword.SET);
			return false;
		}
	}

	// Parses a string block.
	private boolean parseStringBlock(AbstractPatchContext<?> context)
	{
		if (!matchType(DecoHackKernel.TYPE_LBRACE))
		{
			addErrorMessage("Expected '{' to start \"%s\" section.", Keyword.STRINGS);
			return false;
		}
		
		if (context.supports(DEHFeatureLevel.BOOM))
		{
			if (!parseStringEntryList((PatchBoomContext)context))
				return false;
			if (!matchType(DecoHackKernel.TYPE_RBRACE))
			{
				addErrorMessage("Expected '}' to close \"%s\" section, or string key name to start string replacement entry.", Keyword.STRINGS);
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
				addErrorMessage("Expected '}' to close \"%s\" section, or string index to start string replacement entry.", Keyword.STRINGS);
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
	
	// Parses an ammo type alias line.
	private boolean parseAmmoAliasLine(AbstractPatchContext<?> context)
	{
		String ammoName;
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			addErrorMessage("Expected ammo name after \"%s\".", Keyword.AMMO);
			return false;			
		}
		
		if ((ammoName = matchIdentifier()) == null)
		{
			addErrorMessage("INTERNAL ERROR: EXPECTED IDENTIFIER FOR AMMO.");
			return false;
		}
		
		Integer slot;
		if ((slot = context.getAmmoAlias(ammoName)) != null)
		{
			addErrorMessage("Expected valid ammo identifier for alias: \"%s\" is already in use!", ammoName);
			return false;
		}
		
		if ((slot = context.supports(DEHFeatureLevel.ID24) ? matchInteger() : matchPositiveInteger()) == null)
		{
			addErrorMessage("Expected a valid integer for the ammo slot number after \"%s\".", ammoName);
			return false;
		}
		else if ((slot = verifyAmmoIndex(context, slot)) == null)
		{
			return false;
		}
		
		context.setAmmoAlias(ammoName, slot);
		return true;
	}
	
	// Parses an "auto ammo" block.
	private boolean parseAmmoAutoBlock(final AbstractPatchContext<?> context)
	{
		String ammoName;
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			addErrorMessage("Expected ammo name after \"%s\".", Keyword.AMMO);
			return false;			
		}
		
		if ((ammoName = matchIdentifier()) == null)
		{
			addErrorMessage("INTERNAL ERROR: EXPECTED IDENTIFIER FOR AMMO.");
			return false;
		}
		
		Integer slot;
		if ((slot = context.getAmmoAlias(ammoName)) != null)
		{
			addErrorMessage("Expected valid ammo identifier for new auto-ammo: \"%s\" is already in use!", ammoName);
			return false;
		}
		
		if ((slot = context.findNextFreeAmmo(lastAutoAmmoIndex)) == null)
		{
			addErrorMessage("No more free ammo types for a new auto-ammo.");
			return false;
		}
		
		// Save hint.
		lastAutoAmmoIndex = slot;

		// set thing.
		context.setAmmoAlias(ammoName, slot);
		
		String optionalName;
		if ((optionalName = matchString()) != null)
			context.getAmmo(slot).setName(optionalName);

		return parseAmmoBodyBlock(context, slot);
	}
	
	// Parses an ammo block.
	private boolean parseAmmoBlock(AbstractPatchContext<?> context)
	{
		// free things?
		if (matchIdentifierIgnoreCase(Keyword.FREE))
		{
			Integer min;
			if ((min = matchPositiveInteger()) != null)
			{
				if ((min = verifyAmmoIndex(context, min)) == null)
					return false;
				
				if (!matchIdentifierIgnoreCase(Keyword.TO))
				{
					context.setFreeAmmo(min, true);
					return true;
				}
				
				Integer max;
				if ((max = matchAmmoIndex(context)) == null)
					return false;
				
				context.setFreeAmmo(min, max, true);
				return true;
			}
			else
			{
				addErrorMessage("Expected ammo index after \"%s\".", Keyword.FREE);
				return false;
			}
		}
		
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
		
		return parseAmmoBodyBlock(context, ammoIndex);
	}
	
	// Parses an ammo body block.
	private boolean parseAmmoBodyBlock(AbstractPatchContext<?> context, int slot)
	{
		DEHAmmo ammo = context.getAmmo(slot);

		if (!matchType(DecoHackKernel.TYPE_LBRACE))
		{
			addErrorMessage("Expected '{' after \"%s\" header.", Keyword.AMMO);
			return false;
		}
		
		while (currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			if (matchIdentifierIgnoreCase(Keyword.MAX))
			{
				Integer value;
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.MAX);
					return false;
				}
				ammo.setMax(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.PICKUP))
			{
				Integer value;
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.PICKUP);
					return false;
				}
				ammo.setPickup(value);
			}
			// ================ ID24
			else if (matchIdentifierIgnoreCase(Keyword.INITIALAMMO))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.INITIALAMMO);
					return false;
				}
				
				Integer value;
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.INITIALAMMO);
					return false;
				}
				ammo.setInitialAmmo(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.MAXUPGRADEDAMMO))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.MAXUPGRADEDAMMO);
					return false;
				}
				
				Integer value;
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.MAXUPGRADEDAMMO);
					return false;
				}
				ammo.setMaxUpgradedAmmo(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.BOXAMMO))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.BOXAMMO);
					return false;
				}
				
				Integer value;
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.BOXAMMO);
					return false;
				}
				ammo.setBoxAmmo(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.BACKPACKAMMO))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.BACKPACKAMMO);
					return false;
				}
				
				Integer value;
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.BACKPACKAMMO);
					return false;
				}
				ammo.setBackpackAmmo(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.WEAPONAMMO))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.WEAPONAMMO);
					return false;
				}
				
				Integer value;
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.WEAPONAMMO);
					return false;
				}
				ammo.setWeaponAmmo(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.DROPPEDAMMO))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.DROPPEDAMMO);
					return false;
				}
				
				Integer value;
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.DROPPEDAMMO);
					return false;
				}
				ammo.setDroppedAmmo(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.DROPPEDBOXAMMO))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.DROPPEDBOXAMMO);
					return false;
				}
				
				Integer value;
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.DROPPEDBOXAMMO);
					return false;
				}
				ammo.setDroppedBoxAmmo(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.DROPPEDBACKPACKAMMO))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.DROPPEDBACKPACKAMMO);
					return false;
				}
				
				Integer value;
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.DROPPEDBACKPACKAMMO);
					return false;
				}
				ammo.setDroppedBackpackAmmo(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.DROPPEDWEAPONAMMO))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.DROPPEDWEAPONAMMO);
					return false;
				}
				
				Integer value;
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.DROPPEDWEAPONAMMO);
					return false;
				}
				ammo.setDroppedWeaponAmmo(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.DEATHMATCHWEAPONAMMO))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.DEATHMATCHWEAPONAMMO);
					return false;
				}
				
				Integer value;
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.DEATHMATCHWEAPONAMMO);
					return false;
				}
				ammo.setDeathmatchWeaponAmmo(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.SKILL1MULTIPLIER))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.SKILL1MULTIPLIER);
					return false;
				}
				
				Integer value;
				if ((value = matchPositiveFixed(true)) == null)
				{
					addErrorMessage("Expected positive fixed-point number after \"%s\".", Keyword.SKILL1MULTIPLIER);
					return false;
				}
				ammo.setSkill1Multiplier(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.SKILL2MULTIPLIER))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.SKILL2MULTIPLIER);
					return false;
				}
				
				Integer value;
				if ((value = matchPositiveFixed(true)) == null)
				{
					addErrorMessage("Expected positive fixed-point number after \"%s\".", Keyword.SKILL2MULTIPLIER);
					return false;
				}
				ammo.setSkill2Multiplier(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.SKILL3MULTIPLIER))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.SKILL3MULTIPLIER);
					return false;
				}
				
				Integer value;
				if ((value = matchPositiveFixed(true)) == null)
				{
					addErrorMessage("Expected positive fixed-point number after \"%s\".", Keyword.SKILL3MULTIPLIER);
					return false;
				}
				ammo.setSkill3Multiplier(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.SKILL4MULTIPLIER))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.SKILL4MULTIPLIER);
					return false;
				}
				
				Integer value;
				if ((value = matchPositiveFixed(true)) == null)
				{
					addErrorMessage("Expected positive fixed-point number after \"%s\".", Keyword.SKILL4MULTIPLIER);
					return false;
				}
				ammo.setSkill4Multiplier(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.SKILL5MULTIPLIER))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.SKILL5MULTIPLIER);
					return false;
				}
				
				Integer value;
				if ((value = matchPositiveFixed(true)) == null)
				{
					addErrorMessage("Expected positive fixed-point number after \"%s\".", Keyword.SKILL5MULTIPLIER);
					return false;
				}
				ammo.setSkill5Multiplier(value);
			}
			// Custom Properties
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
				addErrorMessage("Expected \"%s\" or \"%s\" or a custom property.", Keyword.MAX, Keyword.PICKUP);
				return false;
			}
		}
		
		if (!matchType(DecoHackKernel.TYPE_RBRACE))
		{
			addErrorMessage("Expected '}' after \"%s\" section.", Keyword.AMMO);
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
				addErrorMessage("Expected valid sound name after \"%s\".", Keyword.SOUND);
				return false;
			}
		}
		else
		{
			addErrorMessage("Expected sound name after \"%s\".", Keyword.SOUND);
			return false;
		}

		if (!matchType(DecoHackKernel.TYPE_LBRACE))
		{
			addErrorMessage("Expected '{' after \"%s\" header.", Keyword.SOUND);
			return false;
		}

		while (currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			if (matchIdentifierIgnoreCase(Keyword.PRIORITY))
			{
				Integer value;
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.PRIORITY);
					return false;
				}
				sound.setPriority(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.SINGULAR))
			{
				Boolean value;
				if ((value = matchBoolean()) == null)
				{
					addErrorMessage("Expected boolean after \"%s\".", Keyword.SINGULAR);
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
				addErrorMessage("Expected \"%s\" or \"%s\" or a custom property.", Keyword.PRIORITY, Keyword.SINGULAR);
				return false;
			}
		}
		
		if (!matchType(DecoHackKernel.TYPE_RBRACE))
		{
			addErrorMessage("Expected '}' after \"%s\" section.", Keyword.SOUND);
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
			addErrorMessage("Expected '{' after \"%s\" header.", Keyword.PARS);
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
			addErrorMessage("Expected '}' after \"%s\" section.", Keyword.PARS);
			return false;
		}
		
		return true;
	}
	
	// Parses a miscellany block.
	private boolean parseMiscellaneousBlock(AbstractPatchContext<?> context)
	{
		if (!matchType(DecoHackKernel.TYPE_LBRACE))
		{
			addErrorMessage("Expected '{' after \"%s\" header.", Keyword.MISC);
			return false;
		}
		
		DEHMiscellany misc = context.getMiscellany();
		
		Boolean flag;
		Integer value;
		while (currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			if (matchIdentifierIgnoreCase(Keyword.MONSTER_INFIGHTING))
			{
				if ((flag = matchBoolean()) == null)
				{
					addErrorMessage("Expected boolean value after \"%s\".", Keyword.MONSTER_INFIGHTING);
					return false;
				}
				misc.setMonsterInfightingEnabled(flag);
			}
			else if (matchIdentifierIgnoreCase(Keyword.MONSTER_INFIGHTING2))
			{
				if ((flag = matchBoolean()) == null)
				{
					addErrorMessage("Expected boolean value after \"%s\".", Keyword.MONSTER_INFIGHTING2);
					return false;
				}
				misc.setMonsterInfightingEnabled(flag);
			}
			else if (matchIdentifierIgnoreCase(Keyword.INITIAL_BULLETS))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", Keyword.INITIAL_BULLETS);
					return false;
				}
				misc.setInitialBullets(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.INITIAL_HEALTH))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", Keyword.INITIAL_HEALTH);
					return false;
				}
				misc.setInitialHealth(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.GREEN_ARMOR_CLASS))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", Keyword.GREEN_ARMOR_CLASS);
					return false;
				}
				misc.setGreenArmorClass(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.BLUE_ARMOR_CLASS))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", Keyword.BLUE_ARMOR_CLASS);
					return false;
				}
				misc.setBlueArmorClass(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.SOULSPHERE_HEALTH))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", Keyword.SOULSPHERE_HEALTH);
					return false;
				}
				misc.setSoulsphereHealth(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.MAX_SOULSPHERE_HEALTH))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", Keyword.MAX_SOULSPHERE_HEALTH);
					return false;
				}
				misc.setMaxSoulsphereHealth(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.MEGASPHERE_HEALTH))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", Keyword.MEGASPHERE_HEALTH);
					return false;
				}
				misc.setMegasphereHealth(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.GOD_MODE_HEALTH))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", Keyword.GOD_MODE_HEALTH);
					return false;
				}
				misc.setGodModeHealth(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.IDFA_ARMOR))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", Keyword.IDFA_ARMOR);
					return false;
				}
				misc.setIDFAArmor(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.IDFA_ARMOR_CLASS))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", Keyword.IDFA_ARMOR_CLASS);
					return false;
				}
				misc.setIDFAArmorClass(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.IDKFA_ARMOR))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", Keyword.IDKFA_ARMOR);
					return false;
				}
				misc.setIDKFAArmor(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.IDKFA_ARMOR_CLASS))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", Keyword.IDKFA_ARMOR_CLASS);
					return false;
				}
				misc.setIDKFAArmorClass(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.BFG_CELLS_PER_SHOT))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", Keyword.BFG_CELLS_PER_SHOT);
					return false;
				}
				misc.setBFGCellsPerShot(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.MAX_HEALTH))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", Keyword.MAX_HEALTH);
					return false;
				}
				misc.setMaxHealth(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.MAX_ARMOR))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected integer value after \"%s\".", Keyword.MAX_ARMOR);
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
			addErrorMessage("Expected '}' after \"%s\" section.", Keyword.MISC);
			return false;
		}

		return true;
	}

	// Parses a custom element clause.
	private boolean parseCustomClause(AbstractPatchContext<?> context)
	{
		if (matchIdentifierIgnoreCase(Keyword.THING))
		{
			if (matchIdentifierIgnoreCase(Keyword.POINTER))
			{
				if (context.getSupportedActionPointerType() == DEHActionPointerType.DOOM19)
				{
					addErrorMessage("Patch type must be Boom or better for custom pointers.");
					return false;
				}

				return parseCustomPointerClause(context, false);
			}
			else if (matchIdentifierIgnoreCase(Keyword.PROPERTY))
			{
				return parseCustomPropertyClause(context, DEHThing.class);
			}
			else
			{
				addErrorMessage("Expected \"%s\" or \"%s\" after \"%s\".", Keyword.POINTER, Keyword.PROPERTY, Keyword.THING);
				return false;
			}
		}
		else if (matchIdentifierIgnoreCase(Keyword.WEAPON))
		{
			if (matchIdentifierIgnoreCase(Keyword.POINTER))
			{
				if (context.getSupportedActionPointerType() == DEHActionPointerType.DOOM19)
				{
					addErrorMessage("Patch type must be Boom or better for custom pointers.");
					return false;
				}

				return parseCustomPointerClause(context, true);
			}
			else if (matchIdentifierIgnoreCase(Keyword.PROPERTY))
			{
				return parseCustomPropertyClause(context, DEHWeapon.class);
			}
			else
			{
				addErrorMessage("Expected \"%s\" or \"%s\" after \"%s\".", Keyword.POINTER, Keyword.PROPERTY, Keyword.WEAPON);
				return false;
			}
		}
		else if (matchIdentifierIgnoreCase(Keyword.AMMO))
		{
			if (matchIdentifierIgnoreCase(Keyword.PROPERTY))
			{
				return parseCustomPropertyClause(context, DEHAmmo.class);
			}
			else
			{
				addErrorMessage("Expected \"%s\" after \"%s\".", Keyword.PROPERTY, Keyword.AMMO);
				return false;
			}
		}
		else if (matchIdentifierIgnoreCase(Keyword.STATE))
		{
			if (matchIdentifierIgnoreCase(Keyword.PROPERTY))
			{
				return parseCustomPropertyClause(context, DEHState.class);
			}
			else
			{
				addErrorMessage("Expected \"%s\" after \"%s\".", Keyword.PROPERTY, Keyword.STATE);
				return false;
			}
		}
		else if (matchIdentifierIgnoreCase(Keyword.SOUND))
		{
			if (matchIdentifierIgnoreCase(Keyword.PROPERTY))
			{
				return parseCustomPropertyClause(context, DEHSound.class);
			}
			else
			{
				addErrorMessage("Expected \"%s\" after \"%s\".", Keyword.PROPERTY, Keyword.SOUND);
				return false;
			}
		}
		else if (matchIdentifierIgnoreCase(Keyword.MISC))
		{
			if (matchIdentifierIgnoreCase(Keyword.PROPERTY))
			{
				return parseCustomPropertyClause(context, DEHMiscellany.class);
			}
			else
			{
				addErrorMessage("Expected \"%s\" after \"%s\".", Keyword.PROPERTY, Keyword.MISC);
				return false;
			}
		}
		else
		{
			addErrorMessage("Expected an object type after \"%s\": %s", Keyword.CUSTOM, Arrays.toString(ArrayUtils.arrayOf(Keyword.MISC, Keyword.STATE, Keyword.SOUND, Keyword.AMMO, Keyword.WEAPON, Keyword.THING)));
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
				addErrorMessage("Expected '{' after \"%s\" header.", Keyword.STATE);
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
					addErrorMessage("Expected '}' after \"%s\" definition.", Keyword.STATE);
					return false;
				}
			}
	
			return true;
		}
		// if fill state...
		else if (matchIdentifierIgnoreCase(Keyword.FILL))
		{
			if ((index = parseStateIndex(context)) == null)
				return false;
	
			if (!matchType(DecoHackKernel.TYPE_LBRACE))
			{
				addErrorMessage("Expected '{' after \"%s %s\" header.", Keyword.STATE, Keyword.FILL);
				return false;
			}
	
			if (!parseStateFillSequence(context, index))
				return false;
	
			if (!matchType(DecoHackKernel.TYPE_RBRACE))
			{
				addErrorMessage("Expected '}' after \"%s %s\" block.", Keyword.STATE, Keyword.FILL);
				return false;
			}
			
			return true;
		}
		else if (matchIdentifierIgnoreCase(Keyword.FREE))
		{
			return parseStateFreeLine(context);
		}
		else if (matchIdentifierIgnoreCase(Keyword.PROTECT))
		{
			return parseStateProtectLine(context, true);
		}
		else if (matchIdentifierIgnoreCase(Keyword.UNPROTECT))
		{
			return parseStateProtectLine(context, false);
		}
		else
		{
			addErrorMessage("Expected state index or \"%s\" keyword after \"%s\".", Keyword.FILL, Keyword.STATE);
			return false;
		}
	}

	// Parses a state freeing command.
	private boolean parseStateFreeLine(AbstractPatchContext<?> context)
	{
		Integer min, max;
		if (matchIdentifierIgnoreCase(Keyword.FROM))
		{
			// free chain
			if ((min = matchPositiveInteger()) != null)
			{
				context.freeConnectedStates(min);
				return true;
			}
			else
			{
				addErrorMessage("Expected state index after \"%s\".", Keyword.FROM);
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
			if (matchIdentifierIgnoreCase(Keyword.TO))
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
					addErrorMessage("Expected state index after \"%s\".", Keyword.TO);
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
			addErrorMessage("Expected \"%s\" or state index after \"%s\".", Keyword.FROM, Keyword.FREE);
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
			if (matchIdentifierIgnoreCase(Keyword.TO))
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
			addErrorMessage("Expected thing name after \"%s\".", Keyword.THING);
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
		
		if ((slot = context.supports(DEHFeatureLevel.ID24) ? matchInteger() : matchPositiveInteger()) == null)
		{
			addErrorMessage("Expected a valid integer for the thing slot number after \"%s\".", thingName);
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
		if (matchIdentifierIgnoreCase(Keyword.FREE))
		{
			Integer min;
			if ((min = matchPositiveInteger()) != null)
			{
				if ((min = verifyThingIndex(context, min)) == null)
					return false;
				
				if (!matchIdentifierIgnoreCase(Keyword.TO))
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
				addErrorMessage("Expected thing index after \"%s\".", Keyword.FREE);
				return false;
			}
		}
		
		Integer slot;
		if ((slot = matchThingIndex(context)) == null)
			return false;

		// thing swap
		if (matchIdentifierIgnoreCase(Keyword.SWAP))
		{
			if (!matchIdentifierIgnoreCase(Keyword.WITH))
			{
				addErrorMessage("Expected \"%s\" after \"%s\".", Keyword.WITH, Keyword.SWAP);
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
		else if (matchIdentifierIgnoreCase(Keyword.FREE))
		{
			if (matchIdentifierIgnoreCase(Keyword.STATES))
			{
				context.freeThingStates(slot);
				return true;
			}
			
			if (!currentIsThingState())
			{
				addErrorMessage("Expected thing state name or \"%s\" or a thing index after \"%s\".", Keyword.STATES, Keyword.FREE);
				return false;
			}

			DEHThing thing = context.getThing(slot);

			if (matchIdentifierIgnoreCase(Keyword.THINGSTATE_SPAWN))
				context.freeConnectedStates(thing.getSpawnFrameIndex());
			else if (matchIdentifierIgnoreCase(Keyword.THINGSTATE_SEE))
				context.freeConnectedStates(thing.getWalkFrameIndex());
			else if (matchIdentifierIgnoreCase(Keyword.THINGSTATE_MELEE))
				context.freeConnectedStates(thing.getMeleeFrameIndex());
			else if (matchIdentifierIgnoreCase(Keyword.THINGSTATE_MISSILE))
				context.freeConnectedStates(thing.getMissileFrameIndex());
			else if (matchIdentifierIgnoreCase(Keyword.THINGSTATE_PAIN))
				context.freeConnectedStates(thing.getPainFrameIndex());
			else if (matchIdentifierIgnoreCase(Keyword.THINGSTATE_DEATH))
				context.freeConnectedStates(thing.getDeathFrameIndex());
			else if (matchIdentifierIgnoreCase(Keyword.THINGSTATE_XDEATH))
				context.freeConnectedStates(thing.getExtremeDeathFrameIndex());
			else if (matchIdentifierIgnoreCase(Keyword.THINGSTATE_RAISE))
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
		if (matchIdentifierIgnoreCase(Keyword.IN))
		{
			return parseThingEachInBlock(context);
		}
		else if (matchIdentifierIgnoreCase(Keyword.FROM))
		{
			return parseThingEachFromBlock(context);
		}
		else
		{
			addErrorMessage("Expected '%s' or '%s' after \"%s %s\" declaration.", Keyword.IN, Keyword.FROM, Keyword.EACH, Keyword.THING);
			return false;
		}
	}
	
	// Parses an "each thing in" block.
	private boolean parseThingEachInBlock(final AbstractPatchContext<?> context)
	{
		List<Integer> thingList = new LinkedList<>();
		
		if (!matchType(DecoHackKernel.TYPE_LPAREN))
		{
			addErrorMessage("Expected '(' after \"%s\".", Keyword.IN);
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

		if (!matchIdentifierIgnoreCase(Keyword.TO))
		{
			addErrorMessage("Expected \"%s\" after starting thing index.", Keyword.TO);
			return false;
		}

		Integer max;
		if ((max = matchPositiveInteger()) == null)
		{
			addErrorMessage("Expected ending thing index after \"%s\".", Keyword.TO);
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
			addErrorMessage("Expected thing name after \"%s\".", Keyword.THING);
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
			if (!matchIdentifierIgnoreCase(Keyword.THING))
			{
				addErrorMessage("Expected \"%s\" after ':'.", Keyword.THING);
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
			addErrorMessage("Expected '{' after \"%s\" declaration.", Keyword.THING);
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
				else if ((value = matchThingID24FlagMnemonic(context)) != null)
				{
					thing.addID24Flag(value);
				}
				else if (matchIdentifierIgnoreCase(Keyword.MBF21))
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
				else if (matchIdentifierIgnoreCase(Keyword.ID24))
				{
					if ((value = matchPositiveInteger()) != null)
					{
						thing.addID24Flag(value);
					}
					else
					{
						addErrorMessage("Expected integer after \"+ id24\".");
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
				else if ((value = matchThingID24FlagMnemonic(context)) != null)
				{
					thing.removeID24Flag(value);
				}
				else if (matchIdentifierIgnoreCase(Keyword.MBF21))
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
				else if (matchIdentifierIgnoreCase(Keyword.ID24))
				{
					if ((value = matchPositiveInteger()) != null)
					{
						thing.removeID24Flag(value);
					}
					else
					{
						addErrorMessage("Expected integer after \"- id24\".");
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
			else if (matchIdentifierIgnoreCase(Keyword.STATE))
			{
				if (!parseThingStateClause(context, thing))
					return false;
			}
			else if (matchIdentifierIgnoreCase(Keyword.STATES))
			{
				if (!parseThingStateBody(context, thing))
					return false;
			}
			else if (matchIdentifierIgnoreCase(Keyword.CLEAR))
			{
				if (matchIdentifierIgnoreCase(Keyword.STATE))
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
						addErrorMessage("Expected state label after '%s': %s", Keyword.STATE, Arrays.toString(thing.getLabels()));
						return false;
					}
				}
				else if (matchIdentifierIgnoreCase(Keyword.PROPERTIES))
				{
					thing.clearProperties();
				}
				else if (matchIdentifierIgnoreCase(Keyword.FLAGS))
				{
					thing.clearFlags();
				}
				else if (matchIdentifierIgnoreCase(Keyword.STATES))
				{
					thing.clearLabels();
				}
				else if (matchIdentifierIgnoreCase(Keyword.SOUNDS))
				{
					thing.clearSounds();
				}
				else
				{
					addErrorMessage("Expected '%s', '%s', or '%s' after '%s'.", Keyword.STATE, Keyword.STATES, Keyword.SOUNDS, Keyword.CLEAR);
					return false;
				}
			}
			else if (matchIdentifierIgnoreCase(Keyword.EDNUM))
			{
				if ((value = matchInteger()) == null)
				{
					addErrorMessage("Expected integer after \"%s\".", Keyword.EDNUM);
					return false;
				}
				
				if (value < -1 && !context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("A negative editor number is only valid for ID24 patches or better.");
					return false;
				}
				
				// bad or reserved ednums.
				if (value == 0 || value == 1 || value == 2 || value == 3 || value == 4 || value == 11)
				{
					addErrorMessage("The editor number %d is either invalid or reserved.", value);
					return false;
				}
				
				thing.setEditorNumber(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.HEALTH))
			{
				if ((value = matchInteger()) == null)
				{
					addErrorMessage("Expected integer after \"%s\".", Keyword.HEALTH);
					return false;
				}
				thing.setHealth(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.SPEED))
			{
				if (thing.hasFlag(DEHThingFlag.MISSILE.getValue()))
				{
					if ((value = matchFixed(false)) == null)
					{
						addErrorMessage("Expected integer or fixed-point value after \"%s\".", Keyword.SPEED);
						return false;
					}
					thing.setFixedSpeed(value);
				}
				else
				{
					if ((value = matchInteger()) == null)
					{
						addErrorMessage("Expected integer after \"%s\".", Keyword.SPEED);
						return false;
					}
					thing.setSpeed(value);
				}
			}
			else if (matchIdentifierIgnoreCase(Keyword.RADIUS))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.RADIUS);
					return false;
				}
				thing.setRadius(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.HEIGHT))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.HEIGHT);
					return false;
				}
				thing.setHeight(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.DAMAGE))
			{
				if ((value = matchInteger()) == null)
				{
					addErrorMessage("Expected integer after \"%s\".", Keyword.DAMAGE);
					return false;
				}
				thing.setDamage(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.REACTIONTIME))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.REACTIONTIME);
					return false;
				}
				thing.setReactionTime(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.PAINCHANCE))
			{
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.PAINCHANCE);
					return false;
				}
				thing.setPainChance(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.MASS))
			{
				if ((value = matchInteger()) == null)
				{
					addErrorMessage("Expected integer after \"%s\".", Keyword.MASS);
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
			else if (matchIdentifierIgnoreCase(Keyword.FLAGS))
			{
				if ((value = (Integer)matchNumericExpression(context, thing, Type.FLAGS)) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.FLAGS);
					return false;
				}
				thing.setFlags(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.SEESOUND))
			{
				if ((value = matchSoundIndexName(context)) == null)
				{
					addErrorMessage("Expected sound name after \"%s\".", Keyword.SEESOUND);
					return false;
				}
				thing.setSeeSoundPosition(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.ATTACKSOUND))
			{
				if ((value = matchSoundIndexName(context)) == null)
				{
					addErrorMessage("Expected sound name after \"%s\".", Keyword.ATTACKSOUND);
					return false;
				}
				thing.setAttackSoundPosition(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.PAINSOUND))
			{
				if ((value = matchSoundIndexName(context)) == null)
				{
					addErrorMessage("Expected sound name after \"%s\".", Keyword.PAINSOUND);
					return false;
				}
				thing.setPainSoundPosition(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.DEATHSOUND))
			{
				if ((value = matchSoundIndexName(context)) == null)
				{
					addErrorMessage("Expected sound name after \"%s\".", Keyword.DEATHSOUND);
					return false;
				}
				thing.setDeathSoundPosition(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.ACTIVESOUND))
			{
				if ((value = matchSoundIndexName(context)) == null)
				{
					addErrorMessage("Expected sound name after \"%s\".", Keyword.ACTIVESOUND);
					return false;
				}
				thing.setActiveSoundPosition(value);
			}
			// ================= EXTENDED
			else if (matchIdentifierIgnoreCase(Keyword.DROPITEM))
			{
				if (!context.supports(DEHFeatureLevel.EXTENDED))
				{
					addErrorMessage("The \"%s\" property is not available. Not an EXTENDED patch.", Keyword.DROPITEM);
					return false;
				}
				
				if ((value = matchThingIndex(context, true)) == null)
				{
					addErrorMessage("Expected thing index after \"%s\".", Keyword.DROPITEM);
					return false;
				}
				thing.setDroppedItem(value);
			}
			// ================= MBF21
			else if (matchIdentifierIgnoreCase(Keyword.FASTSPEED))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("The \"%s\" property is not available. Not an MBF21 patch.", Keyword.FASTSPEED);
					return false;
				}
				
				if ((value = matchInteger()) == null)
				{
					addErrorMessage("Expected integer after \"%s\".", Keyword.FASTSPEED);
					return false;
				}
				thing.setFastSpeed(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.MELEERANGE))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("The \"%s\" property is not available. Not an MBF21 patch.", Keyword.MELEERANGE);
					return false;
				}
				
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.MELEERANGE);
					return false;
				}
				thing.setMeleeRange(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.RIPSOUND))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("The \"%s\" property is not available. Not an MBF21 patch.", Keyword.RIPSOUND);
					return false;
				}
				
				if ((value = matchSoundIndexName(context)) == null)
				{
					addErrorMessage("Expected sound name after \"%s\".", Keyword.RIPSOUND);
					return false;
				}
				thing.setRipSoundPosition(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.INFIGHTINGGROUP))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("The \"%s\" property is not available. Not an MBF21 patch.", Keyword.INFIGHTINGGROUP);
					return false;
				}
				
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.INFIGHTINGGROUP);
					return false;
				}
				thing.setInfightingGroup(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.PROJECTILEGROUP))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("The \"%s\" property is not available. Not an MBF21 patch.", Keyword.PROJECTILEGROUP);
					return false;
				}
				
				if ((value = matchInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.PROJECTILEGROUP);
					return false;
				}
				thing.setProjectileGroup(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.SPLASHGROUP))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("The \"%s\" property is not available. Not an MBF21 patch.", Keyword.SPLASHGROUP);
					return false;
				}
				
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.SPLASHGROUP);
					return false;
				}
				thing.setSplashGroup(value);
			}
			// ================= ID24
			else if (matchIdentifierIgnoreCase(Keyword.MINRESPAWNTICS))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.MINRESPAWNTICS);
					return false;
				}
				
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.MINRESPAWNTICS);
					return false;
				}
				thing.setMinRespawnTics(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.RESPAWNDICE))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.RESPAWNDICE);
					return false;
				}
				
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.RESPAWNDICE);
					return false;
				}
				thing.setRespawnDice(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.PICKUPAMMOTYPE))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.PICKUPAMMOTYPE);
					return false;
				}
				
				if ((value = matchAmmoIndex(context)) == null)
				{
					return false;
				}
				thing.setPickupAmmoType(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.PICKUPAMMOCATEGORY))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.PICKUPAMMOCATEGORY);
					return false;
				}
				
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.PICKUPAMMOCATEGORY);
					return false;
				}
				thing.setPickupAmmoCategory(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.PICKUPWEAPONTYPE))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.PICKUPWEAPONTYPE);
					return false;
				}
				
				if ((value = matchWeaponIndex(context)) == null)
				{
					return false;
				}
				thing.setPickupWeaponType(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.PICKUPITEMTYPE))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.PICKUPITEMTYPE);
					return false;
				}
				
				if ((value = matchPickupItemType()) == null)
				{
					return false;
				}
				thing.setPickupItemType(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.PICKUPBONUSCOUNT))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.PICKUPBONUSCOUNT);
					return false;
				}
				
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.PICKUPBONUSCOUNT);
					return false;
				}
				thing.setPickupBonusCount(value);
			}
			else if (matchIdentifierIgnoreCase(Keyword.PICKUPMESSAGE))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.PICKUPMESSAGE);
					return false;
				}
				
				String str;
				if ((str = matchString()) == null)
				{
					addErrorMessage("Expected string after \"%s\".", Keyword.PICKUPMESSAGE);
					return false;
				}
				thing.setPickupMessageMnemonic(str);
			}
			else if (matchIdentifierIgnoreCase(Keyword.TRANSLATION))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.TRANSLATION);
					return false;
				}
				
				String str;
				if ((str = matchString()) == null)
				{
					addErrorMessage("Expected string after \"%s\".", Keyword.TRANSLATION);
					return false;
				}
				thing.setTranslation(str);
			}
			else if (matchIdentifierIgnoreCase(Keyword.PICKUPSOUND))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.PICKUPSOUND);
					return false;
				}
				
				if ((value = matchSoundIndexName(context)) == null)
				{
					addErrorMessage("Expected valid sound after \"%s\".", Keyword.PICKUPSOUND);
					return false;
				}
				thing.setPickupSoundPosition(value);
			}
			// Custom Properties
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
				addErrorMessage("Expected Thing property, \"%s\" directive, or state block start.", Keyword.CLEAR);
				return false;
			}
		} // while

		if (!matchType(DecoHackKernel.TYPE_RBRACE))
		{
			addErrorMessage("Expected '}' after \"%s\" section.", Keyword.THING);
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
		if (matchIdentifierIgnoreCase(Keyword.THINGSTATE_SPAWN))
		{
			if ((value = parseStateIndex(context)) != null)
				thing.setSpawnFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierIgnoreCase(Keyword.THINGSTATE_SEE))
		{
			if ((value = parseStateIndex(context)) != null)
				thing.setWalkFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierIgnoreCase(Keyword.THINGSTATE_MELEE))
		{
			if ((value = parseStateIndex(context)) != null)
				thing.setMeleeFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierIgnoreCase(Keyword.THINGSTATE_MISSILE))
		{
			if ((value = parseStateIndex(context)) != null)
				thing.setMissileFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierIgnoreCase(Keyword.THINGSTATE_PAIN))
		{
			if ((value = parseStateIndex(context)) != null)
				thing.setPainFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierIgnoreCase(Keyword.THINGSTATE_DEATH))
		{
			if ((value = parseStateIndex(context)) != null)
				thing.setDeathFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierIgnoreCase(Keyword.THINGSTATE_XDEATH))
		{
			if ((value = parseStateIndex(context)) != null)
				thing.setExtremeDeathFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierIgnoreCase(Keyword.THINGSTATE_RAISE))
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
				Keyword.THINGSTATE_SPAWN,
				Keyword.THINGSTATE_SEE,
				Keyword.THINGSTATE_MELEE,
				Keyword.THINGSTATE_MISSILE,
				Keyword.THINGSTATE_PAIN,
				Keyword.THINGSTATE_DEATH,
				Keyword.THINGSTATE_XDEATH,
				Keyword.THINGSTATE_RAISE
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
			addErrorMessage("Expected '{' after \"%s\" declaration.", Keyword.STATES);
			return false;
		}

		if (!parseActorStateSet(context, thing)) 
			return false;
		
		if (!matchType(DecoHackKernel.TYPE_RBRACE))
		{
			addErrorMessage("Expected '}' after \"%s\" section.", Keyword.STATES);
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
			addErrorMessage("Expected weapon name after \"%s\".", Keyword.WEAPON);
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
		
		if ((slot = context.supports(DEHFeatureLevel.ID24) ? matchInteger() : matchPositiveInteger()) == null)
		{
			addErrorMessage("Expected a valid integer for the weapon slot number after \"%s\".", weaponName);
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
		// free things?
		if (matchIdentifierIgnoreCase(Keyword.FREE))
		{
			Integer min;
			if ((min = matchPositiveInteger()) != null)
			{
				if ((min = verifyWeaponIndex(context, min)) == null)
					return false;
				
				if (!matchIdentifierIgnoreCase(Keyword.TO))
				{
					context.setFreeWeapon(min, true);
					return true;
				}
				
				Integer max;
				if ((max = matchWeaponIndex(context)) == null)
					return false;
				
				context.setFreeWeapon(min, max, true);
				return true;
			}
			else
			{
				addErrorMessage("Expected weapon index after \"%s\".", Keyword.FREE);
				return false;
			}
		}
		
		Integer slot;
		if ((slot = matchWeaponIndex(context)) == null)
			return false;
		
		// weapon swap
		if (matchIdentifierIgnoreCase(Keyword.SWAP))
		{
			if (!matchIdentifierIgnoreCase(Keyword.WITH))
			{
				addErrorMessage("Expected \"%s\" after \"%s\".", Keyword.WITH, Keyword.SWAP);
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
		else if (matchIdentifierIgnoreCase(Keyword.FREE))
		{
			if (matchIdentifierIgnoreCase(Keyword.STATES))
			{
				context.freeWeaponStates(slot);
				return true;
			}

			if (!currentIsWeaponState())
			{
				addErrorMessage("Expected weapon state name or \"%s\" after \"%s\".", Keyword.STATES, Keyword.FREE);
				return false;
			}

			DEHWeapon weapon = context.getWeapon(slot);

			if (matchIdentifierIgnoreCase(Keyword.WEAPONSTATE_SELECT))
				context.freeConnectedStates(weapon.getRaiseFrameIndex());
			else if (matchIdentifierIgnoreCase(Keyword.WEAPONSTATE_DESELECT))
				context.freeConnectedStates(weapon.getLowerFrameIndex());
			else if (matchIdentifierIgnoreCase(Keyword.WEAPONSTATE_READY))
				context.freeConnectedStates(weapon.getReadyFrameIndex());
			else if (matchIdentifierIgnoreCase(Keyword.WEAPONSTATE_FIRE))
				context.freeConnectedStates(weapon.getFireFrameIndex());
			else if (matchIdentifierIgnoreCase(Keyword.WEAPONSTATE_FLASH))
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
		if (matchIdentifierIgnoreCase(Keyword.IN))
		{
			return parseWeaponEachInBlock(context);
		}
		else if (matchIdentifierIgnoreCase(Keyword.FROM))
		{
			return parseWeaponEachFromBlock(context);
		}
		else
		{
			addErrorMessage("Expected '%s' or '%s' after \"%s %s\" declaration.", Keyword.IN, Keyword.FROM, Keyword.EACH, Keyword.THING);
			return false;
		}
	}
	
	// Parses an "each weapon in" block.
	private boolean parseWeaponEachInBlock(final AbstractPatchContext<?> context)
	{
		List<Integer> weaponList = new LinkedList<>();
		
		if (!matchType(DecoHackKernel.TYPE_LPAREN))
		{
			addErrorMessage("Expected '(' after \"%s\".", Keyword.IN);
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

		if (!matchIdentifierIgnoreCase(Keyword.TO))
		{
			addErrorMessage("Expected \"%s\" after starting weapon index.", Keyword.TO);
			return false;
		}

		Integer max;
		if ((max = matchPositiveInteger()) == null)
		{
			addErrorMessage("Expected ending weapon index after \"%s\".", Keyword.TO);
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
	
	// Parses an "auto weapon" block.
	private boolean parseWeaponAutoBlock(final AbstractPatchContext<?> context)
	{
		String weaponName;
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
		{
			addErrorMessage("Expected weapon name after \"%s\".", Keyword.WEAPON);
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
			addErrorMessage("Expected valid weapon identifier for new auto-weapon: \"%s\" is already in use!", weaponName);
			return false;
		}
		
		if ((slot = context.findNextFreeWeapon(lastAutoWeaponIndex)) == null)
		{
			addErrorMessage("No more free weapons for a new auto-weapon.");
			return false;
		}
		
		// Save hint.
		lastAutoWeaponIndex = slot;

		// set thing.
		context.setWeaponAlias(weaponName, slot);
		
		return parseWeaponDefinitionBlock(context, slot);
	}
	
	// Parses the weapon copy clauses, marks the weapon as not free, and parses the body.
	private boolean parseWeaponDefinitionBlock(AbstractPatchContext<?> context, int slot)
	{
		DEHWeapon weapon = context.getWeapon(slot);
		
		if (matchType(DecoHackKernel.TYPE_COLON))
		{
			if (!matchIdentifierIgnoreCase(Keyword.WEAPON))
			{
				addErrorMessage("Expected \"%s\" after ':'.", Keyword.WEAPON);
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
			addErrorMessage("Expected '{' after \"%s\" declaration.", Keyword.WEAPON);
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
			else if (matchIdentifierIgnoreCase(Keyword.STATE))
			{
				if (!parseWeaponStateClause(context, weapon))
					return false;
			}
			else if (matchIdentifierIgnoreCase(Keyword.STATES))
			{
				if (!parseWeaponStateBody(context, weapon))
					return false;
			}
			else if (matchIdentifierIgnoreCase(Keyword.CLEAR))
			{
				if (matchIdentifierIgnoreCase(Keyword.STATE))
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
						addErrorMessage("Expected state label after '%s': %s", Keyword.STATE, Arrays.toString(weapon.getLabels()));
						return false;
					}
				}
				else if (matchIdentifierIgnoreCase(Keyword.PROPERTIES))
				{
					weapon.clearProperties();
				}
				else if (matchIdentifierIgnoreCase(Keyword.STATES))
				{
					weapon.clearLabels();
				}
				else if (matchIdentifierIgnoreCase(Keyword.FLAGS))
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
					addErrorMessage("Expected '%s' or '%s' after '%s'.", Keyword.STATE, Keyword.STATES, Keyword.CLEAR);
					return false;
				}
			}
			else if (matchIdentifierIgnoreCase(Keyword.AMMOTYPE))
			{
				Integer ammoIndex;
				if ((ammoIndex = matchAmmoIndex(context)) == null)
				{
					return false;
				}
				
				weapon.setAmmoType(ammoIndex);
			}
			else if (matchIdentifierIgnoreCase(Keyword.AMMOPERSHOT))
			{
				Integer ammoPerShot;
				if ((ammoPerShot = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected a positive integer after \"%s\".", Keyword.AMMOPERSHOT);
					return false;
				}
				else
				{
					weapon.setAmmoPerShot(ammoPerShot);
				}
			}
			// ================= MBF21
			else if (matchIdentifierIgnoreCase(Keyword.FLAGS))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("The \"%s\" property is not available. Not an MBF21 patch.", Keyword.FLAGS);
					return false;
				}
				
				if (!matchIdentifierIgnoreCase(Keyword.MBF21))
				{
					addErrorMessage("Expected \"%s\" after \"%s\".", Keyword.MBF21, Keyword.FLAGS);
					return false;
				}
				
				Integer flags;
				if ((flags = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected positive integer after \"%s\".", Keyword.FLAGS);
					return false;
				}
				else
				{
					weapon.setMBF21Flags(flags);
				}
			}
			// ================= ID24
			else if (matchIdentifierIgnoreCase(Keyword.SLOT))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.SLOT);
					return false;
				}
				
				Integer value;
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected a positive integer after \"%s\".", Keyword.SLOT);
					return false;
				}
				else
				{
					weapon.setSlot(value);
				}
			}
			else if (matchIdentifierIgnoreCase(Keyword.SLOTPRIORITY))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.SLOTPRIORITY);
					return false;
				}
				
				Integer value;
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected a positive integer after \"%s\".", Keyword.SLOTPRIORITY);
					return false;
				}
				else
				{
					weapon.setSlotPriority(value);
				}
			}
			else if (matchIdentifierIgnoreCase(Keyword.SWITCHPRIORITY))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.SWITCHPRIORITY);
					return false;
				}
				
				Integer value;
				if ((value = matchPositiveInteger()) == null)
				{
					addErrorMessage("Expected a positive integer after \"%s\".", Keyword.SWITCHPRIORITY);
					return false;
				}
				else
				{
					weapon.setSwitchPriority(value);
				}
			}
			else if (matchIdentifierIgnoreCase(Keyword.INITIALOWNED))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.INITIALOWNED);
					return false;
				}
				
				Boolean value;
				if ((value = matchBoolean()) == null)
				{
					addErrorMessage("Expected a boolean value (true, false) after \"%s\".", Keyword.INITIALOWNED);
					return false;
				}
				else
				{
					weapon.setInitialOwned(value);
				}
			}
			else if (matchIdentifierIgnoreCase(Keyword.INITIALRAISED))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.INITIALRAISED);
					return false;
				}
				
				Boolean value;
				if ((value = matchBoolean()) == null)
				{
					addErrorMessage("Expected a boolean value (true, false) after \"%s\".", Keyword.INITIALRAISED);
					return false;
				}
				else
				{
					weapon.setInitialRaised(value);
				}
			}
			else if (matchIdentifierIgnoreCase(Keyword.CAROUSELICON))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.CAROUSELICON);
					return false;
				}
				
				String value;
				if ((value = matchString()) == null)
				{
					addErrorMessage("Expected a string after \"%s\".", Keyword.CAROUSELICON);
					return false;
				}
				else
				{
					weapon.setCarouselIcon(value);
				}
			}
			else if (matchIdentifierIgnoreCase(Keyword.ALLOWSWITCHWITHOWNEDWEAPON))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.ALLOWSWITCHWITHOWNEDWEAPON);
					return false;
				}
				
				Integer value;
				if ((value = matchWeaponIndex(context)) == null)
				{
					return false;
				}
				else
				{
					weapon.setAllowSwitchWithOwnedWeapon(value);
				}
			}
			else if (matchIdentifierIgnoreCase(Keyword.NOSWITCHWITHOWNEDWEAPON))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.NOSWITCHWITHOWNEDWEAPON);
					return false;
				}
				
				Integer value;
				if ((value = matchWeaponIndex(context)) == null)
				{
					return false;
				}
				else
				{
					weapon.setNoSwitchWithOwnedWeapon(value);
				}
			}
			else if (matchIdentifierIgnoreCase(Keyword.ALLOWSWITCHWITHOWNEDITEM))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.ALLOWSWITCHWITHOWNEDITEM);
					return false;
				}
				
				Integer value;
				if ((value = matchPickupItemType()) == null)
				{
					return false;
				}
				else
				{
					weapon.setAllowSwitchWithOwnedItem(value);
				}
			}
			else if (matchIdentifierIgnoreCase(Keyword.NOSWITCHWITHOWNEDITEM))
			{
				if (!context.supports(DEHFeatureLevel.ID24))
				{
					addErrorMessage("The \"%s\" property is not available. Not an ID24 patch.", Keyword.NOSWITCHWITHOWNEDITEM);
					return false;
				}
				
				Integer value;
				if ((value = matchPickupItemType()) == null)
				{
					return false;
				}
				else
				{
					weapon.setNoSwitchWithOwnedItem(value);
				}
			}
			// Custom Properties
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
				addErrorMessage("Expected Weapon property, \"%s\" directive, or state block start.", Keyword.CLEAR);
				return false;
			}
		}		

		if (!matchType(DecoHackKernel.TYPE_RBRACE))
		{
			addErrorMessage("Expected '}' after \"%s\" section.", Keyword.WEAPON);
			return false;
		}
		
		return true;
	}

	private boolean parseWeaponStateClause(AbstractPatchContext<?> context, DEHWeaponTarget<?> weapon) 
	{
		Integer value;
		if (matchIdentifierIgnoreCase(Keyword.WEAPONSTATE_READY))
		{
			if ((value = parseStateIndex(context)) != null)
				weapon.setReadyFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierIgnoreCase(Keyword.WEAPONSTATE_SELECT))
		{
			if ((value = parseStateIndex(context)) != null)
				weapon.setRaiseFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierIgnoreCase(Keyword.WEAPONSTATE_DESELECT))
		{
			if ((value = parseStateIndex(context)) != null)
				weapon.setLowerFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierIgnoreCase(Keyword.WEAPONSTATE_FIRE))
		{
			if ((value = parseStateIndex(context)) != null)
				weapon.setFireFrameIndex(value);
			else
				return false;
		}
		else if (matchIdentifierIgnoreCase(Keyword.WEAPONSTATE_FLASH))
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
				Keyword.WEAPONSTATE_READY,
				Keyword.WEAPONSTATE_SELECT,
				Keyword.WEAPONSTATE_DESELECT,
				Keyword.WEAPONSTATE_FIRE,
				Keyword.WEAPONSTATE_FLASH
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
			addErrorMessage("Expected '{' after \"%s\" declaration.", Keyword.STATES);
			return false;
		}

		if (!parseActorStateSet(context, weapon)) 
			return false;
		
		if (!matchType(DecoHackKernel.TYPE_RBRACE))
		{
			addErrorMessage("Expected '}' after \"%s\" section.", Keyword.STATES);
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
					if (matchIdentifierIgnoreCase(Keyword.GOTO))
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
					else if (matchIdentifierIgnoreCase(Keyword.STOP))
					{
						while (!labelList.isEmpty())
							actor.setLabel(labelList.pollFirst(), 0);
					}
					else
					{
						addErrorMessage("Expected a state definition after label, or a \"%s\" clause, or \"%s\".", Keyword.GOTO, Keyword.STOP);
						return false;
					}
				}
				// A previous state was filled.
				else
				{
					Object nextStateIndex = null;
					if ((nextStateIndex = parseNextStateIndex(context, actor, loopIndex, stateCursor.lastIndexFilled)) == null)
					{
						addErrorMessage("Expected next state clause (%s, %s, %s, %s).", Keyword.STOP, Keyword.WAIT, Keyword.LOOP, Keyword.GOTO);
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
			addErrorMessage("Expected next state clause (%s, %s, %s, %s).", Keyword.STOP, Keyword.WAIT, Keyword.LOOP, Keyword.GOTO);
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
			if (matchIdentifierIgnoreCase(Keyword.SPRITENAME))
			{
				Integer value;
				if ((value = matchSpriteIndexName(context)) == null)
				{
					addErrorMessage("Expected valid sprite name after \"%s\".", Keyword.SPRITENAME);
					return false;				
				}
				
				state.setSpriteIndex(value);
				notModified = false;
			}
			else if (matchIdentifierIgnoreCase(Keyword.FRAME))
			{
				Deque<Integer> value;
				if ((value = matchFrameIndices()) == null)
				{
					addErrorMessage("Expected valid frame characters after \"%s\".", Keyword.FRAME);
					return false;				
				}
				
				if (value.size() > 1)
				{
					addErrorMessage("Expected a single frame character after \"%s\".", Keyword.FRAME);
					return false;				
				}
				
				state.setFrameIndex(value.pollFirst());
				notModified = false;
			}
			else if (matchIdentifierIgnoreCase(Keyword.DURATION))
			{
				Integer value;
				if ((value = matchInteger()) == null)
				{
					addErrorMessage("Expected integer after \"%s\".", Keyword.DURATION);
					return false;				
				}
				
				state.setDuration(value);
				notModified = false;
			}
			else if (matchIdentifierIgnoreCase(Keyword.NEXTSTATE))
			{
				Integer value;
				if ((value = parseStateIndex(context)) == null)
				{
					addErrorMessage("Expected valid state index clause after \"%s\".", Keyword.NEXTSTATE);
					return false;				
				}
				
				state.setNextStateIndex(value);
				notModified = false;
			}
			else if (matchIdentifierIgnoreCase(Keyword.POINTER))
			{
				boolean isBoom = context.supports(DEHFeatureLevel.BOOM);			
				Integer pointerIndex = context.getStateActionPointerIndex(index);
				ParsedAction action = new ParsedAction();
				Boolean requireAction = isBoom ? null : pointerIndex != null;
	
				if (matchIdentifierIgnoreCase(VALUE_NULL))
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
			else if (currentIdentifierIgnoreCase(Keyword.OFFSET))
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
			else if (currentIdentifierIgnoreCase(Keyword.TRANMAP))
			{
				ParsedAction action = new ParsedAction();
				if (!parseTranmapClause(action))
					return false;
				
				state.setTranmap(action.tranmap);
				notModified = false;
			}
			else if (matchIdentifierIgnoreCase(Keyword.STATE_BRIGHT))
			{
				state.setBright(true);
				notModified = false;
			}
			else if (matchIdentifierIgnoreCase(Keyword.STATE_NOTBRIGHT))
			{
				state.setBright(false);
				notModified = false;
			}
			else if (matchIdentifierIgnoreCase(Keyword.STATE_FAST))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("MBF21 state flags (e.g. \"%s\") are not available. Not an MBF21 patch.", Keyword.STATE_FAST);
					return false;
				}
				
				state.setMBF21Flags(state.getMBF21Flags() | DEHStateMBF21Flag.SKILL5FAST.getValue());
				notModified = false;
			}
			else if (matchIdentifierIgnoreCase(Keyword.STATE_NOTFAST))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("MBF21 state flags (e.g. \"%s\") are not available. Not an MBF21 patch.", Keyword.STATE_NOTFAST);
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
			if (!parseTranmapClause(action))
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
		if (matchIdentifierIgnoreCase(Keyword.THING))
		{
			return parseThingStateIndex(context);
		}
		else if (matchIdentifierIgnoreCase(Keyword.WEAPON))
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
	
	// Parses an Offset clause.
	private boolean parseOffsetClause(ParsedAction parsedAction)
	{
		if (matchIdentifierIgnoreCase(Keyword.OFFSET))
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

	// Parses a Tranmap clause.
	private boolean parseTranmapClause(ParsedAction parsedAction)
	{
		if (matchIdentifierIgnoreCase(Keyword.TRANMAP))
		{
			if (matchType(DecoHackKernel.TYPE_LPAREN))
			{
				String name;
				if ((name = matchString()) == null)
				{
					addErrorMessage("Expected a string for tranmap name.");
					return false;
				}
				
				parsedAction.tranmap = name;
				
				if (!matchType(DecoHackKernel.TYPE_RPAREN))
				{
					addErrorMessage("Expected a ')' after tranmap name.");
					return false;
				}
			}
			else
			{
				addErrorMessage("Expected a '(' after \"tranmap\".");
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
		if (matchIdentifierIgnoreCase(Keyword.THING))
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
		else if (matchIdentifierIgnoreCase(Keyword.WEAPON))
		{
			if (paramType == DEHValueType.WEAPON)
				addWarningMessage("The use of a \"weapon\" clause as a parameter in an action pointer is unneccesary. You can just use an index or a weapon alias.");
			return parseWeaponOrWeaponStateIndex(context);
		}
		else if (matchIdentifierIgnoreCase(Keyword.SOUND))
		{
			if (paramType == DEHValueType.SOUND)
				addWarningMessage("The use of a \"sound\" clause as a parameter in an action pointer is unneccesary. You can just use the sound name.");
			return parseSoundIndex(context);
		}
		else if (matchIdentifierIgnoreCase(Keyword.FLAGS))
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
		if (matchIdentifierIgnoreCase(Keyword.STOP))
		{
			return 0;
		}
		else if (matchIdentifierIgnoreCase(Keyword.WAIT))
		{
			return currentStateIndex;
		}
		else if (currentIdentifierIgnoreCase(Keyword.LOOP))
		{
			if (lastLabelledStateIndex == null)
			{
				addErrorMessage("Can't use \"%s\" with no declared state labels.", Keyword.LOOP);
				return null;
			}
			nextToken();
			return lastLabelledStateIndex;
		}
		else if (matchIdentifierIgnoreCase(Keyword.GOTO))
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
					.setTranmap(parsedAction.tranmap)
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
			case Keyword.STOP:
			case Keyword.GOTO:
			case Keyword.LOOP:
			case Keyword.WAIT:
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
			case Keyword.THINGSTATE_SPAWN:
			case Keyword.THINGSTATE_SEE:
			case Keyword.THINGSTATE_MELEE:
			case Keyword.THINGSTATE_MISSILE:
			case Keyword.THINGSTATE_PAIN:
			case Keyword.THINGSTATE_DEATH:
			case Keyword.THINGSTATE_XDEATH:
			case Keyword.THINGSTATE_RAISE:
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
			case Keyword.WEAPONSTATE_READY:
			case Keyword.WEAPONSTATE_DESELECT:
			case Keyword.WEAPONSTATE_SELECT:
			case Keyword.WEAPONSTATE_FIRE:
			case Keyword.WEAPONSTATE_FLASH:
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
		else if ((slot = context.supports(DEHFeatureLevel.ID24) ? matchInteger() : matchPositiveInteger()) != null)
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
		else if ((slot = context.supports(DEHFeatureLevel.ID24) ? matchInteger() : matchPositiveInteger()) == null)
		{
			addErrorMessage("Expected valid integer or alias for the weapon index.");
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

	// Mqtches a valid ammo type.
	private Integer matchAmmoIndex(AbstractPatchContext<?> context)
	{
		Integer ammoType;
		String autoAmmoName;
		
		if ((autoAmmoName = matchIdentifier()) != null)
		{
			if ((ammoType = context.getAmmoAlias(autoAmmoName)) == null)
			{
				addErrorMessage("Expected valid ammo identifier: \"%s\" is not a valid alias.", autoAmmoName);
				return null;
			}
			else
			{
				return ammoType;
			}
		}
		
		if ((ammoType = matchInteger()) == null)
		{
			addErrorMessage("Expected integer for ammo type.");
			return null;
		}
		else if (ammoType == 4)
		{
			addErrorMessage("Ammo type %d is reserved.", ammoType);
			return null;
		}
		
		if (context.getAmmo(ammoType) == null)
		{
			addErrorMessage("Expected valid ammo type index.");
			return null;
		}
		
		return verifyAmmoIndex(context, ammoType);
	}

	// Verifies a valid ammo index number.
	private Integer verifyAmmoIndex(AbstractPatchContext<?> context, int slot)
	{
		if (slot == -1)
		{
			addErrorMessage("Invalid ammo index: %d.", slot);
			return null;
		}

		if (slot == 4)
		{
			addErrorMessage("Invalid ammo index: %d.", slot);
			return null;
		}

		if (slot >= context.getAmmoCount())
		{
			addErrorMessage("Invalid ammo index: %d. Max is %d.", slot, context.getAmmoCount() - 1);
			return null;
		}

		if (context.getAmmo(slot) == null)
		{
			addErrorMessage("Invalid ammo index: %d. Max is %d.", slot, context.getAmmoCount() - 1);
			return null;
		}

		return slot;
	}

	// Mqtches a valid pickup item type.
	private Integer matchPickupItemType()
	{
		Integer itemType;
		if ((itemType = matchInteger()) == null)
		{
			addErrorMessage("Expected integer for item type.");
			return null;
		}
		
		if (itemType < -1 || itemType > 21)
		{
			addErrorMessage("Expected valid item type index (%d to %d).", -1, 21);
			return null;
		}
		
		return itemType;
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
			if (matchIdentifierIgnoreCase(Keyword.STATE_BRIGHT))
			{
				state.bright = true;
			}
			else if (matchIdentifierIgnoreCase(Keyword.STATE_FAST))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("MBF21 state flags (e.g. \"%s\") are not available. Not an MBF21 patch.", Keyword.STATE_FAST);
					return false;
				}
				state.mbf21Flags = state.mbf21Flags != null ? state.mbf21Flags : 0;
				state.mbf21Flags |= DEHStateMBF21Flag.SKILL5FAST.getValue();
			}
			else if (matchIdentifierIgnoreCase(Keyword.STATE_NOTFAST))
			{
				if (!context.supports(DEHFeatureLevel.MBF21))
				{
					addErrorMessage("MBF21 state flags (e.g. \"%s\") are not available. Not an MBF21 patch.", Keyword.STATE_NOTFAST);
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
	
	// Matches an identifier that references an MBF thing flag mnemonic.
	// If match, advance token and return bitflags.
	// Else, return null.
	private Integer matchThingID24FlagMnemonic(AbstractPatchContext<?> context)
	{
		if (!currentType(DecoHackKernel.TYPE_IDENTIFIER))
			return null;

		Integer out = null;
		DEHThingID24Flag flag;
		if ((flag = DEHThingID24Flag.getByMnemonic(currentLexeme())) != null)
		{
			out = flag.getValue();

			if (!context.supports(DEHFeatureLevel.ID24))
			{
				addErrorMessage("ID24 thing flags are not available. Not an ID24 patch.");
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
				if ((out = matchFixed(true)) == null)
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
				final int FIM_THING_ID24 = 0x08;
				
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
					else if ((value = matchThingID24FlagMnemonic(context)) != null)
					{
						out |= value;

						flagInclusionMask |= FIM_THING_ID24;
						if ((flagInclusionMask & ~FIM_THING_ID24) != 0)
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
	private Integer matchPositiveFixed(boolean typeCheck)
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
			if (typeCheck)
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
	private Integer matchFixed(boolean typeCheck)
	{
		if (matchType(DecoHackKernel.TYPE_DASH))
		{
			Integer out;
			if ((out = matchPositiveFixed(typeCheck)) == null)
				return null;
			return -out;
		}
		return matchPositiveFixed(typeCheck);
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
	/** Last auto weapon index (for slightly better search continuation). */
	private int lastAutoWeaponIndex;
	/** Last auto ammo index (for slightly better search continuation). */
	private int lastAutoAmmoIndex;

	// Return the exporter for the patch.
	private DecoHackParser(String streamName, InputStream in, Charset inputCharset)
	{
		super(new DecoHackLexer(streamName, in != null ? new InputStreamReader(in, inputCharset) : null, inputCharset));
		this.warnings = new LinkedList<>();
		this.errors = new LinkedList<>();
		this.editorKeys = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		this.lastAutoThingIndex = 0;
		this.lastAutoWeaponIndex = 0;
		this.lastAutoAmmoIndex = 0;
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
		private String tranmap;
		
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
			this.tranmap = null;
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
						put("<id24>", "classpath:decohack/id24.dh");
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

/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.patches;

import static net.mtrop.doom.tools.decohack.patches.Constants.*;
import static net.mtrop.doom.tools.decohack.patches.ConstantsBoom.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import net.mtrop.doom.tools.decohack.data.DEHActionPointer;
import net.mtrop.doom.tools.decohack.data.DEHAmmo;
import net.mtrop.doom.tools.decohack.data.DEHMiscellany;
import net.mtrop.doom.tools.decohack.data.DEHSound;
import net.mtrop.doom.tools.decohack.data.DEHState;
import net.mtrop.doom.tools.decohack.data.DEHThing;
import net.mtrop.doom.tools.decohack.data.DEHWeapon;
import net.mtrop.doom.tools.struct.util.ArrayUtils;

/**
 * Patch implementation for Doom 1.9.
 * @author Matthew Tropiano
 */
public class PatchBoom implements DEHPatchBoom
{
	protected static final String[] SOUNDSTRINGS = 
	{
		null, // None
		"PISTOL",
		"SHOTGN",
		"SGCOCK",
		"DSHTGN",
		"DBOPN",
		"DBCLS",
		"DBLOAD",
		"PLASMA",
		"BFG",
		"SAWUP",
		"SAWIDL",
		"SAWFUL",
		"SAWHIT",
		"RLAUNC",
		"RXPLOD",
		"FIRSHT",
		"FIRXPL",
		"PSTART",
		"PSTOP",
		"DOROPN",
		"DORCLS",
		"STNMOV",
		"SWTCHN",
		"SWTCHX",
		"PLPAIN",
		"DMPAIN",
		"POPAIN",
		"VIPAIN",
		"MNPAIN",
		"PEPAIN",
		"SLOP",
		"ITEMUP",
		"WPNUP",
		"OOF",
		"TELEPT",
		"POSIT1",
		"POSIT2",
		"POSIT3",
		"BGSIT1",
		"BGSIT2",
		"SGTSIT",
		"CACSIT",
		"BRSSIT",
		"CYBSIT",
		"SPISIT",
		"BSPSIT",
		"KNTSIT",
		"VILSIT",
		"MANSIT",
		"PESIT",
		"SKLATK",
		"SGTATK",
		"SKEPCH",
		"VILATK",
		"CLAW",
		"SKESWG",
		"PLDETH",
		"PDIEHI",
		"PODTH1",
		"PODTH2",
		"PODTH3",
		"BGDTH1",
		"BGDTH2",
		"SGTDTH",
		"CACDTH",
		"SKLDTH",
		"BRSDTH",
		"CYBDTH",
		"SPIDTH",
		"BSPDTH",
		"VILDTH",
		"KNTDTH",
		"PEDTH",
		"SKEDTH",
		"POSACT",
		"BGACT",
		"DMACT",
		"BSPACT",
		"BSPWLK",
		"VILACT",
		"NOWAY",
		"BAREXP",
		"PUNCH",
		"HOOF",
		"METAL",
		"CHGUN",
		"TINK",
		"BDOPN",
		"BDCLS",
		"ITMBK",
		"FLAME",
		"FLAMST",
		"GETPOW",
		"BOSPIT",
		"BOSCUB",
		"BOSSIT",
		"BOSPN",
		"BOSDTH",
		"MANATK",
		"MANDTH",
		"SSSIT",
		"SSDTH",
		"KEENPN",
		"KEENDT",
		"SKEACT",
		"SKESIT",
		"SKEATK",
		"RADIO",
	};
		
	protected static final String[] SPRITESTRINGS = 
	{
		"TROO",
		"SHTG",
		"PUNG",
		"PISG",
		"PISF",
		"SHTF",
		"SHT2",
		"CHGG",
		"CHGF",
		"MISG",
		"MISF",
		"SAWG",
		"PLSG",
		"PLSF",
		"BFGG",
		"BFGF",
		"BLUD",
		"PUFF",
		"BAL1",
		"BAL2",
		"PLSS",
		"PLSE",
		"MISL",
		"BFS1",
		"BFE1",
		"BFE2",
		"TFOG",
		"IFOG",
		"PLAY",
		"POSS",
		"SPOS",
		"VILE",
		"FIRE",
		"FATB",
		"FBXP",
		"SKEL",
		"MANF",
		"FATT",
		"CPOS",
		"SARG",
		"HEAD",
		"BAL7",
		"BOSS",
		"BOS2",
		"SKUL",
		"SPID",
		"BSPI",
		"APLS",
		"APBX",
		"CYBR",
		"PAIN",
		"SSWV",
		"KEEN",
		"BBRN",
		"BOSF",
		"ARM1",
		"ARM2",
		"BAR1",
		"BEXP",
		"FCAN",
		"BON1",
		"BON2",
		"BKEY",
		"RKEY",
		"YKEY",
		"BSKU",
		"RSKU",
		"YSKU",
		"STIM",
		"MEDI",
		"SOUL",
		"PINV",
		"PSTR",
		"PINS",
		"MEGA",
		"SUIT",
		"PMAP",
		"PVIS",
		"CLIP",
		"AMMO",
		"ROCK",
		"BROK",
		"CELL",
		"CELP",
		"SHEL",
		"SBOX",
		"BPAK",
		"BFUG",
		"MGUN",
		"CSAW",
		"LAUN",
		"PLAS",
		"SHOT",
		"SGN2",
		"COLU",
		"SMT2",
		"GOR1",
		"POL2",
		"POL5",
		"POL4",
		"POL3",
		"POL1",
		"POL6",
		"GOR2",
		"GOR3",
		"GOR4",
		"GOR5",
		"SMIT",
		"COL1",
		"COL2",
		"COL3",
		"COL4",
		"CAND",
		"CBRA",
		"COL6",
		"TRE1",
		"TRE2",
		"ELEC",
		"CEYE",
		"FSKU",
		"COL5",
		"TBLU",
		"TGRN",
		"TRED",
		"SMBT",
		"SMGT",
		"SMRT",
		"HDB1",
		"HDB2",
		"HDB3",
		"HDB4",
		"HDB5",
		"HDB6",
		"POB1",
		"POB2",
		"BRS1",
		"TLMP",
		"TLP2",
		"TNT1",
	};
	
	protected static final int SPRITE_INDEX_TNT1 = 138;

	private static final Map<String, Integer> MAP_SOUNDINDEX = new HashMap<String, Integer>()
	{
		private static final long serialVersionUID = -4513058612574767102L;
		{
			for (int i = 1; i < SOUNDSTRINGS.length; i++)
				put(SOUNDSTRINGS[i], i);
		}
	};
	
	private static final Map<String, Integer> MAP_SPRITEINDEX = new HashMap<String, Integer>()
	{
		private static final long serialVersionUID = -91431875042148768L;
		{
			for (int i = 0; i < SPRITESTRINGS.length; i++)
				put(SPRITESTRINGS[i], i);
		}
	};
	
	private static final Map<EpisodeMap, Integer> MAP_PARS = new HashMap<EpisodeMap, Integer>()
	{
		private static final long serialVersionUID = -7194135744791190357L;
		{
			put(EpisodeMap.create(1, 1), 30);
			put(EpisodeMap.create(1, 2), 75);
			put(EpisodeMap.create(1, 3), 120);
			put(EpisodeMap.create(1, 4), 90);
			put(EpisodeMap.create(1, 5), 165);
			put(EpisodeMap.create(1, 6), 180);
			put(EpisodeMap.create(1, 7), 180);
			put(EpisodeMap.create(1, 8), 165);
			put(EpisodeMap.create(1, 9), 165);
			put(EpisodeMap.create(2, 1), 90);
			put(EpisodeMap.create(2, 2), 90);
			put(EpisodeMap.create(2, 3), 90);
			put(EpisodeMap.create(2, 4), 120);
			put(EpisodeMap.create(2, 5), 90);
			put(EpisodeMap.create(2, 6), 360);
			put(EpisodeMap.create(2, 7), 240);
			put(EpisodeMap.create(2, 8), 135);
			put(EpisodeMap.create(2, 9), 170);
			put(EpisodeMap.create(3, 1), 90);
			put(EpisodeMap.create(3, 2), 45);
			put(EpisodeMap.create(3, 3), 90);
			put(EpisodeMap.create(3, 4), 150);
			put(EpisodeMap.create(3, 5), 90);
			put(EpisodeMap.create(3, 6), 90);
			put(EpisodeMap.create(3, 7), 165);
			put(EpisodeMap.create(3, 8), 105);
			put(EpisodeMap.create(3, 9), 135);
			put(EpisodeMap.create(4, 1), 165);
			put(EpisodeMap.create(4, 2), 255);
			put(EpisodeMap.create(4, 3), 135);
			put(EpisodeMap.create(4, 4), 150);
			put(EpisodeMap.create(4, 5), 180);
			put(EpisodeMap.create(4, 6), 390);
			put(EpisodeMap.create(4, 7), 135);
			put(EpisodeMap.create(4, 8), 360);
			put(EpisodeMap.create(4, 9), 180);
			put(EpisodeMap.create(0, 1), 30);
			put(EpisodeMap.create(0, 2), 90);
			put(EpisodeMap.create(0, 3), 120);
			put(EpisodeMap.create(0, 4), 120);
			put(EpisodeMap.create(0, 5), 90);
			put(EpisodeMap.create(0, 6), 150);
			put(EpisodeMap.create(0, 7), 120);
			put(EpisodeMap.create(0, 8), 120);
			put(EpisodeMap.create(0, 9), 270);
			put(EpisodeMap.create(0, 10), 90);
			put(EpisodeMap.create(0, 11), 210);
			put(EpisodeMap.create(0, 12), 150);
			put(EpisodeMap.create(0, 13), 150);
			put(EpisodeMap.create(0, 14), 150);
			put(EpisodeMap.create(0, 15), 210);
			put(EpisodeMap.create(0, 16), 150);
			put(EpisodeMap.create(0, 17), 420);
			put(EpisodeMap.create(0, 18), 150);
			put(EpisodeMap.create(0, 19), 210);
			put(EpisodeMap.create(0, 20), 150);
			put(EpisodeMap.create(0, 21), 240);
			put(EpisodeMap.create(0, 22), 150);
			put(EpisodeMap.create(0, 23), 180);
			put(EpisodeMap.create(0, 24), 150);
			put(EpisodeMap.create(0, 25), 150);
			put(EpisodeMap.create(0, 26), 300);
			put(EpisodeMap.create(0, 27), 350);
			put(EpisodeMap.create(0, 28), 420);
			put(EpisodeMap.create(0, 29), 300);
			put(EpisodeMap.create(0, 30), 180);
			put(EpisodeMap.create(0, 31), 120);
			put(EpisodeMap.create(0, 32), 30);
		}
	};
		
	private static final Pattern STRING_MNEMONIC = Pattern.compile("[A-Za-z0-9_]+");
	
	public static class State
	{
		private DEHState state;
		private DEHActionPointer pointer;
		
		private State()
		{
			this.state = null;
			this.pointer = null;
		}
		
		public static State create(DEHState state, DEHActionPointer pointer)
		{
			State out = new State();
			out.state = state;
			out.pointer = pointer;
			return out;
		}
		
		public DEHState getState() 
		{
			return state;
		}
		
		public DEHActionPointer getPointer() 
		{
			return pointer;
		}
		
	}
	
	// ======================================================================
	
	@Override
	public DEHMiscellany getMiscellany() 
	{
		return DEHMISCBOOM;
	}

	@Override
	public int getAmmoCount() 
	{
		return DEHAMMO.length;
	}

	@Override
	public DEHAmmo getAmmo(int index) 
	{
		return ArrayUtils.arrayElement(DEHAMMO, index);
	}

	@Override
	public String getString(String key)
	{
		return isValidStringKey(key) ? "" : null;
	}

	@Override
	public boolean isValidStringKey(String key)
	{
		return STRING_MNEMONIC.matcher(key).matches();
	}

	@Override
	public Integer getSoundIndex(String name)
	{
		return MAP_SOUNDINDEX.get(name.toUpperCase());
	}

	@Override
	public Integer getSpriteIndex(String name)
	{
		return MAP_SPRITEINDEX.get(name.toUpperCase());
	}

	@Override
	public int getSoundCount() 
	{
		return DEHSOUND.length;
	}

	@Override
	public DEHSound getSound(int index)
	{
		return ArrayUtils.arrayElement(DEHSOUND, index);
	}

	@Override
	public int getThingCount() 
	{
		return DEHTHING.length + DEHTHINGBOOM.length;
	}

	@Override
	public DEHThing getThing(int index)
	{
		if (index >= DEHTHING.length)
			return ArrayUtils.arrayElement(DEHTHINGBOOM, index - DEHTHING.length);
		else
			return ArrayUtils.arrayElement(DEHTHING, index);
	}

	@Override
	public int getWeaponCount()
	{
		return DEHWEAPON.length;
	}

	@Override
	public DEHWeapon getWeapon(int index)
	{
		return ArrayUtils.arrayElement(DEHWEAPON, index);
	}

	@Override
	public int getStateCount()
	{
		return DEHSTATE.length;
	}

	protected PatchBoom.State getBoomState(int index)
	{
		return ArrayUtils.arrayElement(DEHSTATE, index);
	}

	@Override
	public DEHState getState(int index) 
	{
		State state = getBoomState(index);
		return state != null ? state.getState() : null;
	}

	@Override
	public Integer getStateActionPointerIndex(int stateIndex) 
	{
		return stateIndex;
	}

	@Override
	public int getActionPointerCount() 
	{
		return getStateCount();
	}

	@Override
	public DEHActionPointer getActionPointer(int index)
	{
		State state = getBoomState(index);
		return state != null ? state.getPointer() : null;
	}

	@Override
	public Set<EpisodeMap> getParEntries()
	{
		return MAP_PARS.keySet();
	}

	@Override
	public Integer getParSeconds(EpisodeMap episodeMap) 
	{
		return MAP_PARS.get(episodeMap);
	}

}

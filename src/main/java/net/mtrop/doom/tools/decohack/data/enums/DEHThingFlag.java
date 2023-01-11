/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data.enums;

import java.util.Map;
import java.util.TreeMap;

/**
 * Doom lower integer flags.
 * @see DEHThingDoom19Flag
 * @see DEHThingBoomFlag
 * @see DEHThingMBFFlag
 * @author Matthew Tropiano
 */
public interface DEHThingFlag
{
	DEHThingDoom19Flag SPECIAL = DEHThingDoom19Flag.SPECIAL;
	DEHThingDoom19Flag SOLID = DEHThingDoom19Flag.SOLID;
	DEHThingDoom19Flag SHOOTABLE = DEHThingDoom19Flag.SHOOTABLE;
	DEHThingDoom19Flag NOSECTOR = DEHThingDoom19Flag.NOSECTOR;
	DEHThingDoom19Flag NOBLOCKMAP = DEHThingDoom19Flag.NOBLOCKMAP;
	DEHThingDoom19Flag AMBUSH = DEHThingDoom19Flag.AMBUSH;
	DEHThingDoom19Flag JUSTHIT = DEHThingDoom19Flag.JUSTHIT;
	DEHThingDoom19Flag JUSTATTACKED = DEHThingDoom19Flag.JUSTATTACKED;
	DEHThingDoom19Flag SPAWNCEILING = DEHThingDoom19Flag.SPAWNCEILING;
	DEHThingDoom19Flag NOGRAVITY = DEHThingDoom19Flag.NOGRAVITY;
	DEHThingDoom19Flag DROPOFF = DEHThingDoom19Flag.DROPOFF;
	DEHThingDoom19Flag PICKUP = DEHThingDoom19Flag.PICKUP;
	DEHThingDoom19Flag NOCLIP = DEHThingDoom19Flag.NOCLIP;
	DEHThingDoom19Flag SLIDE = DEHThingDoom19Flag.SLIDE;
	DEHThingDoom19Flag FLOAT = DEHThingDoom19Flag.FLOAT;
	DEHThingDoom19Flag TELEPORT = DEHThingDoom19Flag.TELEPORT;
	DEHThingDoom19Flag MISSILE = DEHThingDoom19Flag.MISSILE;
	DEHThingDoom19Flag DROPPED = DEHThingDoom19Flag.DROPPED;
	DEHThingDoom19Flag SHADOW = DEHThingDoom19Flag.SHADOW;
	DEHThingDoom19Flag NOBLOOD = DEHThingDoom19Flag.NOBLOOD;
	DEHThingDoom19Flag CORPSE = DEHThingDoom19Flag.CORPSE;
	DEHThingDoom19Flag INFLOAT = DEHThingDoom19Flag.INFLOAT;
	DEHThingDoom19Flag COUNTKILL = DEHThingDoom19Flag.COUNTKILL;
	DEHThingDoom19Flag COUNTITEM = DEHThingDoom19Flag.COUNTITEM;
	DEHThingDoom19Flag SKULLFLY = DEHThingDoom19Flag.SKULLFLY;
	DEHThingDoom19Flag NOTDEATHMATCH = DEHThingDoom19Flag.NOTDEATHMATCH;
	DEHThingDoom19Flag TRANSLATION = DEHThingDoom19Flag.TRANSLATION;
	DEHThingDoom19Flag TRANSLATION2 = DEHThingDoom19Flag.TRANSLATION2;
	DEHThingDoom19Flag UNUSED1 = DEHThingDoom19Flag.UNUSED1;
	DEHThingDoom19Flag UNUSED2 = DEHThingDoom19Flag.UNUSED2;
	DEHThingDoom19Flag UNUSED3 = DEHThingDoom19Flag.UNUSED3;
	DEHThingDoom19Flag UNUSED4 = DEHThingDoom19Flag.UNUSED4;
	DEHThingMBFFlag    TOUCHY = DEHThingMBFFlag.TOUCHY;
	DEHThingMBFFlag    BOUNCES = DEHThingMBFFlag.BOUNCES;
	DEHThingMBFFlag    FRIEND = DEHThingMBFFlag.FRIEND;
	DEHThingMBFFlag    FRIENDLY = DEHThingMBFFlag.FRIENDLY;
	DEHThingBoomFlag   TRANSLUCENT = DEHThingBoomFlag.TRANSLUCENT;
	
	static final Map<String, DEHFlag> MNEMONIC_MAP = new TreeMap<String, DEHFlag>(String.CASE_INSENSITIVE_ORDER) 
	{
		private static final long serialVersionUID = 5554097245644939005L;
		{
			for (DEHThingDoom19Flag flag : DEHThingDoom19Flag.values())
				put(flag.name(), flag);
			for (DEHThingMBFFlag flag : DEHThingMBFFlag.values())
				put(flag.name(), flag);
			for (DEHThingBoomFlag flag : DEHThingBoomFlag.values())
				put(flag.name(), flag);
		}
	};
	
	/**
	 * Gets a DEHFlag by its mnemonic.
	 * @param mnemonic the mnemonic string.
	 * @return the corresponding flag or null if no corresponding flag.
	 */
	static DEHFlag getByMnemonic(String mnemonic)
	{
		return MNEMONIC_MAP.get(mnemonic);
	}
	
}

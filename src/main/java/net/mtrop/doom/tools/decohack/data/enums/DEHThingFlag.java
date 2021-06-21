/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano & Xaser Acheron
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data.enums;

import java.util.Map;
import java.util.TreeMap;

/**
 * Doom Thing flags
 * @author Matthew Tropiano
 */
public enum DEHThingFlag implements DEHFlag
{
	SPECIAL       (0x00000001),
	SOLID         (0x00000002),
	SHOOTABLE     (0x00000004),
	NOSECTOR      (0x00000008),
	NOBLOCKMAP    (0x00000010),
	AMBUSH        (0x00000020),
	JUSTHIT       (0x00000040),
	JUSTATTACKED  (0x00000080),
	SPAWNCEILING  (0x00000100),
	NOGRAVITY     (0x00000200),
	DROPOFF       (0x00000400),
	PICKUP        (0x00000800),
	NOCLIP        (0x00001000),
	SLIDE         (0x00002000),
	FLOAT         (0x00004000),
	TELEPORT      (0x00008000),
	MISSILE       (0x00010000),
	DROPPED       (0x00020000),
	SHADOW        (0x00040000),
	NOBLOOD       (0x00080000),
	CORPSE        (0x00100000),
	INFLOAT       (0x00200000),
	COUNTKILL     (0x00400000),
	COUNTITEM     (0x00800000),
	SKULLFLY      (0x01000000),
	NOTDEATHMATCH (0x02000000),
	TRANSLATION   (0x04000000),
	TRANSLATION2  (0x08000000),
	TOUCHY        (0x10000000),
	BOUNCES       (0x20000000),
	FRIEND        (0x40000000),
	TRANSLUCENT   (0x80000000),
	;

	public static final DEHThingFlag[] VALUES = values();

	private static final Map<String, DEHThingFlag> MNEMONIC_MAP = new TreeMap<String, DEHThingFlag>(String.CASE_INSENSITIVE_ORDER)
	{
		private static final long serialVersionUID = 6270128398455692128L;
		{
			for (DEHThingFlag val : DEHThingFlag.values())
				put(val.name(), val);
		}
	};

	public static DEHThingFlag getByMnemonic(String mnemonic)
	{
		return MNEMONIC_MAP.get(mnemonic);
	}

	private int value;

	private DEHThingFlag(int value)
	{
		this.value = value;
	}

	public int getValue()
	{
		return value;
	}
}

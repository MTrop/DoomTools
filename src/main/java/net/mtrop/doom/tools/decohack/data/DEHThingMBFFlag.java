/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano & Xaser Acheron
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

import java.util.Map;
import java.util.TreeMap;

/**
 * MBF21 Thing Flags.
 * @author Matthew Tropiano 
 */
public enum DEHThingMBFFlag implements DEHFlag
{
	LOGRAV         (0x00000001),
	SHORTMRANGE    (0x00000002),
	DMGIGNORED     (0x00000004),
	NORADIUSDMG    (0x00000008),
	FORCERADIUSDMG (0x00000010),
	HIGHERMPROB    (0x00000020),
	RANGEHALF      (0x00000040),
	NOTHRESHOLD    (0x00000080),
	LONGMELEE      (0x00000100),
	BOSS           (0x00000200),
	MAP07BOSS1     (0x00000400),
	MAP07BOSS2     (0x00000800),
	E1M8BOSS       (0x00001000),
	E2M8BOSS       (0x00002000),
	E3M8BOSS       (0x00004000),
	E4M6BOSS       (0x00008000),
	E4M8BOSS       (0x00010000),
	RIP            (0x00020000),
	FULLVOLSOUNDS  (0x00040000),
	;

	public static final DEHThingMBFFlag[] VALUES = values();

	private static final Map<String, DEHThingMBFFlag> MNEMONIC_MAP = new TreeMap<String, DEHThingMBFFlag>(String.CASE_INSENSITIVE_ORDER)
	{
		private static final long serialVersionUID = -3232636284673026047L;
		{
			for (DEHThingMBFFlag val : DEHThingMBFFlag.values())
				put(val.name(), val);
		}
	};

	public static DEHThingMBFFlag getByMnemonic(String mnemonic)
	{
		return MNEMONIC_MAP.get(mnemonic);
	}
	
	private int value;

	private DEHThingMBFFlag(int value)
	{
		this.value = value;
	}

	public int getValue()
	{
		return value;
	}
}

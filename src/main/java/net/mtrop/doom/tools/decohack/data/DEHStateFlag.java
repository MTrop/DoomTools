/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano & Xaser Acheron
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

import java.util.Map;
import java.util.TreeMap;

/**
 * Doom state flags.
 * This is supported for MBF21 and higher.
 * @author Matthew Tropiano
 */
public enum DEHStateFlag implements DEHFlag
{
	SKILL5FAST (0x00000001),
	;

	public static final DEHStateFlag[] VALUES = values();

	private static final Map<String, DEHStateFlag> MNEMONIC_MAP = new TreeMap<String, DEHStateFlag>(String.CASE_INSENSITIVE_ORDER)
	{
		private static final long serialVersionUID = -935311506875376904L;
		{
			for (DEHStateFlag val : DEHStateFlag.values())
				put(val.name(), val);
		}
	};

	public static DEHStateFlag getByMnemonic(String mnemonic)
	{
		return MNEMONIC_MAP.get(mnemonic);
	}

	private int value;

	private DEHStateFlag(int value)
	{
		this.value = value;
	}

	public int getValue()
	{
		return value;
	}
}

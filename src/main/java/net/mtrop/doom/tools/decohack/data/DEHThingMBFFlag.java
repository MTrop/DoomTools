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
public enum DEHThingMBFFlag
{
	// TODO: Finish this.
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

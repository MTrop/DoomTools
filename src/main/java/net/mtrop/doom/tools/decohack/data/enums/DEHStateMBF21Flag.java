/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano & Xaser Acheron
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data.enums;

import java.util.Map;

import net.mtrop.doom.tools.struct.util.EnumUtils;

/**
 * Doom state flags.
 * This is supported for MBF21 and higher.
 * @author Matthew Tropiano
 */
public enum DEHStateMBF21Flag implements DEHFlag
{
	SKILL5FAST (0x00000001),
	;

	private static final Map<String, DEHStateMBF21Flag> MNEMONIC_MAP = EnumUtils.createCaseInsensitiveNameMap(DEHStateMBF21Flag.class);

	public static DEHStateMBF21Flag getByMnemonic(String mnemonic)
	{
		return MNEMONIC_MAP.get(mnemonic);
	}

	private int value;

	private DEHStateMBF21Flag(int value)
	{
		this.value = value;
	}

	public int getValue()
	{
		return value;
	}
	
}

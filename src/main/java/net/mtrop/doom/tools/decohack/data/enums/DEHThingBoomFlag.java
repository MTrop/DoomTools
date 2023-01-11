/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data.enums;

import java.util.Map;

import net.mtrop.doom.tools.struct.util.EnumUtils;

/**
 * Boom Engine-specific Thing flags
 * @author Matthew Tropiano
 */
public enum DEHThingBoomFlag implements DEHFlag
{
	TRANSLUCENT (0x80000000, "Thing is rendered via the translucent colormap.");

	private static final Map<String, DEHThingBoomFlag> MNEMONIC_MAP = EnumUtils.createCaseInsensitiveNameMap(DEHThingBoomFlag.class);

	public static DEHThingBoomFlag getByMnemonic(String mnemonic)
	{
		return MNEMONIC_MAP.get(mnemonic);
	}

	private final int value;
	private final String usage;

	private DEHThingBoomFlag(int value, String usage)
	{
		this.value = value;
		this.usage = usage;
	}

	public int getValue()
	{
		return value;
	}
	
	@Override
	public String getUsage() 
	{
		return usage;
	}
	
}

/*******************************************************************************
 * Copyright (c) 2020-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data.enums;

import java.util.Map;

import net.mtrop.doom.tools.struct.util.EnumUtils;

/**
 * ID24 Thing Flags.
 * @author Matthew Tropiano
 */
public enum DEHThingID24Flag implements DEHFlag
{
	NORESPAWN          (0x00000001, "Thing does not respawn."),
	SPECIALSTAYSSINGLE (0x00000002, "Thing stays on pickup in single player."),
	SPECIALSTAYSCOOP   (0x00000004, "Thing stays on pickup in co-op."),
	SPECIALSTAYSDM     (0x00000008, "Thing stays on pickup in deathmatch."),
	;

	private static final Map<String, DEHThingID24Flag> MNEMONIC_MAP = EnumUtils.createCaseInsensitiveNameMap(DEHThingID24Flag.class);

	public static DEHThingID24Flag getByMnemonic(String mnemonic)
	{
		return MNEMONIC_MAP.get(mnemonic);
	}
	
	private int value;
	private String usage;

	private DEHThingID24Flag(int value, String usage)
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

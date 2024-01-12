/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data.enums;

/**
 * Boom Engine-specific Thing flags
 * @author Matthew Tropiano
 * @author Xaser Acheron
 */
public enum DEHThingMBFFlag implements DEHFlag
{
	TOUCHY        (0x10000000, "Thing dies if it collides with a solid object."),
	BOUNCES       (0x20000000, "Thing bounces if it collides with a floor or wall. Plays SIGHT sound on bounce."),
	FRIEND        (0x40000000, "Thing is friendly to players. Affects A_Chase logic and targeting."),
	FRIENDLY      (0x40000000, "Thing is friendly to players. Affects A_Chase logic and targeting."),
	;

	private final int value;
	private final String usage;

	private DEHThingMBFFlag(int value, String usage)
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

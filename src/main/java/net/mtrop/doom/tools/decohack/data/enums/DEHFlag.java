/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano & Xaser Acheron
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data.enums;

/**
 * Doom flag type.
 * @author Matthew Tropiano
 */
public interface DEHFlag
{
	/**
	 * @return the bit value of this flag.
	 */
	int getValue();
	
	/**
	 * @return the usage info for this flag.
	 */
	default String getUsage()
	{
		return "";
	}
	
	static int flags(DEHFlag ... flags)
	{
		int out = 0;
		for (DEHFlag flag : flags)
			out |= flag.getValue();
		return out;
	}
	
}

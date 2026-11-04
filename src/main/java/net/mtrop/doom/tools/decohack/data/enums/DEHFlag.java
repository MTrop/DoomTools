/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
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
	 * @return this flag's name/mnemonic.
	 */
	String name();
	
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

}

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
	 * @return this flag's name/mnemonic. Cannot be null.
	 */
	String name();
	
	/**
	 * @return the bit value of this flag, or null if purely mnemonic.
	 */
	Integer getValue();
	
	/**
	 * @return true if this is a pure mnemonic, false if it has a value.
	 */
	default boolean isPureMnemonic()
	{
		return getValue() == null;
	}
	
	/**
	 * @return the usage info for this flag.
	 */
	default String getUsage()
	{
		return "";
	}

}

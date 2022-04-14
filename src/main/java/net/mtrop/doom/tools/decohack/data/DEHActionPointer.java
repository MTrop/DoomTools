/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerParamType;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerType;

/**
 * Enumeration of action pointers for frames.
 * NOTE: KEEP THIS ORDER SORTED IN THIS WAY! It is used as breaking categories for the pointer dumper!
 * @author Matthew Tropiano
 */
public interface DEHActionPointer
{
	/**
	 * Gets the originating frame for an action pointer.
	 * This will return <code>-1</code> for pointers not in the original Doom.
	 * @return the original frame, or -1.
	 */
	int getFrame();
	
	/**
	 * @return true if this is a weapon action pointer.
	 */
	boolean isWeapon();
	
	/**
	 * @return the mnemonic string for this pointer.
	 */
	String getMnemonic();
	
	/**
	 * @return the feature set category for this pointer.
	 */
	DEHActionPointerType getType();
	
	/**
	 * @return the parameter list.
	 */
	DEHActionPointerParamType[] getParams();

	/**
	 * Gets a specific param using the param index.
	 * @param index the parameter index.
	 * @return the corresponding parameter, or <code>null</code> if no parameter.
	 */
	DEHActionPointerParamType getParam(int index);

}

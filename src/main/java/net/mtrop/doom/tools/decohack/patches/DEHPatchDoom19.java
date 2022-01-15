/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.patches;

/**
 * Common DeHackEd Patch interface for specific Doom v1.9 executable patches.
 * @author Matthew Tropiano
 */
public interface DEHPatchDoom19 extends DEHPatch
{
	/**
	 * @return the amount of strings.
	 */
	int getStringCount();

	/**
	 * @return if string length should be enforced.
	 */
	Boolean enforceStringLength();
	
	/**
	 * Gets a string.
	 * @param index the index.
	 * @return the corresponding string, or null if invalid index.
	 * @see #getStringCount() 
	 */
	String getString(int index);
	
	/**
	 * Gets the original frame number for an action pointer. 
	 * @param index the action pointer index.
	 * @return the original frame index, or null if invalid index.
	 */
	Integer getActionPointerFrame(int index);
	
}

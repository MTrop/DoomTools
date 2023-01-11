/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data.enums;

/**
 * DeHackEd Feature Level.
 * Note: DO NOT CHANGE THE ORDERING of the enums! They are ordered in ascending feature set!
 * @author Matthew Tropiano 
 */
public enum DEHFeatureLevel
{
	DOOM19,
	BOOM,
	MBF,
	EXTENDED,
	MBF21,
	DSDHACKED,
	;
	
	/**
	 * Checks if the provided feature level is supported by this one.
	 * @param level the provided level.
	 * @return true if so, false if not.
	 */
	public boolean supports(DEHFeatureLevel level)
	{
		return level.ordinal() <= ordinal(); 
	}
	
}

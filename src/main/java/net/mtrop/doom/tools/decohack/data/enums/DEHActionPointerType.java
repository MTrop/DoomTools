/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano & Xaser Acheron
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data.enums;

/**
 * Enumeration of action pointer types.
 * Note: DO NOT CHANGE THE ORDERING of the enums! They are ordered in ascending feature set!
 * @author Xaser Acheron
 * @author Matthew Tropiano 
 */
public enum DEHActionPointerType
{
	DOOM19(false),
	MBF(false),
	MBF21(true);

	public static final DEHActionPointerType[] VALUES = values();

	private boolean useArgs;

	private DEHActionPointerType(boolean useArgs)
	{
		this.useArgs = useArgs;
	}

	public boolean getUseArgs()
	{
		return useArgs;
	}
	
	/**
	 * Checks if the provided pointer type is supported by this one.
	 * @param type the provided type.
	 * @return true if so, false if not.
	 */
	public boolean supports(DEHActionPointerType type)
	{
		return type.ordinal() <= ordinal(); 
	}
	

}

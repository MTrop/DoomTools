/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano & Xaser Acheron
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data.enums;

import java.util.Map;

import net.mtrop.doom.tools.struct.util.EnumUtils;

/**
 * Enumeration of action pointer types.
 * Note: DO NOT CHANGE THE ORDERING of the enums! They are ordered in ascending feature set!
 * @author Xaser Acheron
 * @author Matthew Tropiano 
 */
public enum DEHActionPointerType
{
	DOOM19 (false, 0),
	BOOM   (false, 0), // redundant for DOOM19, necessary for custom pointers
	MBF    (false, 2),
	MBF21  (true,  9);

	private int maxCustomParams;
	private boolean useArgs;

	private static final Map<String, DEHActionPointerType> NAME_MAP = EnumUtils.createCaseInsensitiveNameMap(DEHActionPointerType.class);
	
	public static DEHActionPointerType getByName(String mnemonic)
	{
		return NAME_MAP.get(mnemonic);
	}
	
	private DEHActionPointerType(boolean useArgs, int maxCustomParams)
	{
		this.useArgs = useArgs;
		this.maxCustomParams = maxCustomParams;
	}

	/**
	 * @return true if this uses the "args" fields for values, false for the "misc" fields.
	 */
	public boolean getUseArgs()
	{
		return useArgs;
	}
	
	/**
	 * Gets the maximum amount of definable parameter types.
	 * If used in a custom type, this is the max amount of params that a user is allowed to define.
	 * @return the max amount of params. 
	 */
	public int getMaxCustomParams() 
	{
		return maxCustomParams;
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

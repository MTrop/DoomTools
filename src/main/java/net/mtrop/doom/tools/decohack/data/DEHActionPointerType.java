/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano & Xaser Acheron
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

/**
 * Enumeration of action pointer types.
 * @author Xaser Acheron
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
}

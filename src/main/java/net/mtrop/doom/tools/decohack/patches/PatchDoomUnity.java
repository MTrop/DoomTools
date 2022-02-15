/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.patches;

/**
 * Patch implementation for Doom Unity Port.
 * Same as {@link PatchUDoom19} without string length enforcement.
 * @author Xaser Acheron
 */
public class PatchDoomUnity extends PatchUDoom19
{
	@Override
	public boolean enforceStringLength()
	{
		return false;
	}

}

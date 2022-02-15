/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.contexts;

import net.mtrop.doom.tools.decohack.contexts.PatchUltimateDoom19Context;
import net.mtrop.doom.tools.decohack.patches.DEHPatchDoom19;
import net.mtrop.doom.tools.decohack.patches.PatchDoomUnity;

/**
 * Patch context for Doom Unity Port.
 * Same as {@link PatchUltimateDoom19Context} without string length enforcement.
 * @author Xaser Acheron
 */
public class PatchDoomUnityContext extends PatchUltimateDoom19Context
{
	private static final DEHPatchDoom19 DOOMUNITYPATCH = new PatchDoomUnity();
	
	@Override
	public DEHPatchDoom19 getSourcePatch()
	{
		return DOOMUNITYPATCH;
	}

	@Override
	public boolean enforceStringLength()
	{
		return false;
	}

}

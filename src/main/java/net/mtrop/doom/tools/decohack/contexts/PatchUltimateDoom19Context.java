/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.contexts;

import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerType;
import net.mtrop.doom.tools.decohack.data.enums.DEHFeatureLevel;
import net.mtrop.doom.tools.decohack.patches.DEHPatchDoom19;
import net.mtrop.doom.tools.decohack.patches.PatchUDoom19;

/**
 * Patch context for Ultimate Doom 1.9.
 * Biggest difference to {@link PatchDoom19Context} is the string table.
 * @author Matthew Tropiano
 */
public class PatchUltimateDoom19Context extends PatchDoom19Context
{
	private static final DEHPatchDoom19 UDOOM19PATCH = new PatchUDoom19();
	
	@Override
	public DEHPatchDoom19 getSourcePatch()
	{
		return UDOOM19PATCH;
	}

	@Override
	public DEHActionPointerType getSupportedActionPointerType() 
	{
		return DEHActionPointerType.DOOM19;
	}
	
	@Override
	public DEHFeatureLevel getSupportedFeatureLevel() 
	{
		return DEHFeatureLevel.DOOM19;
	}

	/**
	 * @return the string offset for sound names, or null if not supported.
	 */
	@Override
	public Integer getSoundStringIndex()
	{
		return PatchUDoom19.STRING_INDEX_SOUNDS;
	}

	/**
	 * @return the string offset for sprite names, or null if not supported.
	 */
	@Override
	public Integer getSpriteStringIndex()
	{
		return PatchUDoom19.STRING_INDEX_SPRITES;
	}

}

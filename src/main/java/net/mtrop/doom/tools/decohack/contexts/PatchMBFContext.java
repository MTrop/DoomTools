/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.contexts;

import net.mtrop.doom.tools.decohack.data.DEHActionPointerType;
import net.mtrop.doom.tools.decohack.data.DEHFeatureLevel;
import net.mtrop.doom.tools.decohack.patches.DEHPatchBoom;
import net.mtrop.doom.tools.decohack.patches.PatchMBF;

/**
 * Patch context for MBF (Marine's Best Friend).
 * @author Matthew Tropiano
 */
public class PatchMBFContext extends AbstractPatchBoomContext
{
	private static final DEHPatchBoom MBFPATCH = new PatchMBF();
	
	@Override
	public DEHPatchBoom getSourcePatch()
	{
		return MBFPATCH;
	}

	@Override
	public boolean isActionPointerTypeSupported(DEHActionPointerType type)
	{
		return type == DEHActionPointerType.DOOM19 || type == DEHActionPointerType.MBF;
	}

	@Override
	public DEHFeatureLevel getFeatureLevel() 
	{
		return DEHFeatureLevel.MBF;
	}

}

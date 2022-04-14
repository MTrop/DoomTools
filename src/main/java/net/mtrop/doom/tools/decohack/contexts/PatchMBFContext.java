/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.contexts;

import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerMBF;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerType;
import net.mtrop.doom.tools.decohack.data.enums.DEHFeatureLevel;
import net.mtrop.doom.tools.decohack.patches.DEHPatchBoom;
import net.mtrop.doom.tools.decohack.patches.PatchMBF;
import net.mtrop.doom.tools.struct.util.EnumUtils;

/**
 * Patch context for MBF (Marine's Best Friend).
 * @author Matthew Tropiano
 */
public class PatchMBFContext extends PatchBoomContext
{
	private static final DEHPatchBoom MBFPATCH = new PatchMBF();
	
	public PatchMBFContext() 
	{
		super();
		EnumUtils.addToNameMap(this.pointerMnemonicMap, DEHActionPointerMBF.class);
	}
	
	@Override
	public DEHPatchBoom getSourcePatch()
	{
		return MBFPATCH;
	}

	@Override
	public DEHActionPointerType getSupportedActionPointerType() 
	{
		return DEHActionPointerType.MBF;
	}
	
	@Override
	public DEHFeatureLevel getSupportedFeatureLevel() 
	{
		return DEHFeatureLevel.MBF;
	}

}

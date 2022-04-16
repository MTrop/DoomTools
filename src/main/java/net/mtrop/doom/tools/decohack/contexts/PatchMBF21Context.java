/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano & Xaser Acheron
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.contexts;

import net.mtrop.doom.tools.decohack.data.DEHActionPointer;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerMBF21;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerType;
import net.mtrop.doom.tools.decohack.data.enums.DEHFeatureLevel;
import net.mtrop.doom.tools.decohack.patches.DEHPatchBoom;
import net.mtrop.doom.tools.decohack.patches.PatchMBF21;

/**
 * Patch context for MBF21.
 * @author Xaser Acheron
 * @author Matthew Tropiano
 */
public class PatchMBF21Context extends PatchExtendedContext
{
	private static final DEHPatchBoom MBF21PATCH = new PatchMBF21();
	
	@Override
	public DEHPatchBoom getSourcePatch()
	{
		return MBF21PATCH;
	}

	@Override
	public DEHActionPointer getActionPointerByMnemonic(String mnemonic) 
	{
		DEHActionPointer out = super.getActionPointerByMnemonic(mnemonic);
		if (out == null)
			out = DEHActionPointerMBF21.getActionPointerByMnemonic(mnemonic);
		return out;
	}

	@Override
	public DEHActionPointerType getSupportedActionPointerType() 
	{
		return DEHActionPointerType.MBF21;
	}
	
	@Override
	public DEHFeatureLevel getSupportedFeatureLevel() 
	{
		return DEHFeatureLevel.MBF21;
	}

}

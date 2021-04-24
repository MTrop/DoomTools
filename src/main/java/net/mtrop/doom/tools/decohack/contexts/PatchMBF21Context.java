/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano & Xaser Acheron
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.contexts;

import net.mtrop.doom.tools.decohack.data.DEHActionPointerType;
import net.mtrop.doom.tools.decohack.patches.DEHPatchBoom;
import net.mtrop.doom.tools.decohack.patches.PatchMBF21;

/**
 * Patch context for MBF21.
 * @author Xaser Acheron
 */
public class PatchMBF21Context extends AbstractPatchBoomContext
{
	private static final DEHPatchBoom MBF21PATCH = new PatchMBF21();
	
	@Override
	public DEHPatchBoom getSourcePatch()
	{
		return MBF21PATCH;
	}

	@Override
	public boolean isActionPointerTypeSupported(DEHActionPointerType type)
	{
		return type == DEHActionPointerType.DOOM19 || type == DEHActionPointerType.MBF || type == DEHActionPointerType.MBF21;
	}

}

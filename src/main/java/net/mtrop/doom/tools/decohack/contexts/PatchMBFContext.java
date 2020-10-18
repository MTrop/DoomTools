package net.mtrop.doom.tools.decohack.contexts;

import net.mtrop.doom.tools.decohack.DEHPatchBoom;
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

}

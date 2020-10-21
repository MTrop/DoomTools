package net.mtrop.doom.tools.decohack.contexts;

import net.mtrop.doom.tools.decohack.patches.DEHPatchBoom;
import net.mtrop.doom.tools.decohack.patches.PatchDHEExtended;

/**
 * Patch context for MBF (Marine's Best Friend).
 * @author Matthew Tropiano
 */
public class PatchDHEExtendedContext extends AbstractPatchBoomContext
{
	private static final DEHPatchBoom DHEEXTENDEDPATCH = new PatchDHEExtended();
	
	@Override
	public DEHPatchBoom getSourcePatch()
	{
		return DHEEXTENDEDPATCH;
	}

}

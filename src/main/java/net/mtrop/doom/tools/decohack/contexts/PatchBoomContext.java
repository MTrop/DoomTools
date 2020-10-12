package net.mtrop.doom.tools.decohack.contexts;

import net.mtrop.doom.tools.decohack.DEHPatchBoom;
import net.mtrop.doom.tools.decohack.patches.BoomPatch;

/**
 * Patch context for Boom.
 * @author Matthew Tropiano
 */
public class PatchBoomContext extends AbstractPatchBoomContext
{
	private static final DEHPatchBoom BOOMPATCH = new BoomPatch();
	
	@Override
	public DEHPatchBoom getSourcePatch()
	{
		return BOOMPATCH;
	}

}

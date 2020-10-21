package net.mtrop.doom.tools.decohack.contexts;

import net.mtrop.doom.tools.decohack.patches.DEHPatchDoom19;
import net.mtrop.doom.tools.decohack.patches.PatchDoom19;

/**
 * Patch context for Doom 1.9.
 * @author Matthew Tropiano
 */
public class PatchDoom19Context extends AbstractPatchDoom19Context
{
	private static final DEHPatchDoom19 DOOM19PATCH = new PatchDoom19();
	
	@Override
	public DEHPatchDoom19 getSourcePatch()
	{
		return DOOM19PATCH;
	}

	/**
	 * @return the string offset for sound names, or null if not supported.
	 */
	@Override
	public Integer getSoundStringIndex()
	{
		return PatchDoom19.STRING_INDEX_SOUNDS;
	}

	/**
	 * @return the string offset for sprite names, or null if not supported.
	 */
	@Override
	public Integer getSpriteStringIndex()
	{
		return PatchDoom19.STRING_INDEX_SPRITES;
	}

}

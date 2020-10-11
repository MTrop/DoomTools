package net.mtrop.doom.tools.decohack.contexts;

import net.mtrop.doom.tools.decohack.DEHPatchDoom19;
import net.mtrop.doom.tools.decohack.patches.Doom19Patch;

/**
 * Patch context for Doom 1.9.
 * @author Matthew Tropiano
 */
public class PatchDoom19Context extends AbstractPatchDoom19Context
{
	private static final DEHPatchDoom19 DOOM19PATCH = new Doom19Patch();
	
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
		return Doom19Patch.STRING_INDEX_SOUNDS;
	}

	/**
	 * @return the string offset for sprite names, or null if not supported.
	 */
	@Override
	public Integer getSpriteStringIndex()
	{
		return Doom19Patch.STRING_INDEX_SPRITES;
	}

}

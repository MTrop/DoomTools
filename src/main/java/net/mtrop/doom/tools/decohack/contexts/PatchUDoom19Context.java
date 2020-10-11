package net.mtrop.doom.tools.decohack.contexts;

import net.mtrop.doom.tools.decohack.DEHPatchDoom19;
import net.mtrop.doom.tools.decohack.patches.UDoom19Patch;

/**
 * Patch context for Ultimate Doom 1.9.
 * Biggest difference to {@link PatchDoom19Context} is the string table.
 * @author Matthew Tropiano
 */
public class PatchUDoom19Context extends AbstractPatchDoom19Context
{
	private static final DEHPatchDoom19 UDOOM19PATCH = new UDoom19Patch();
	
	@Override
	public DEHPatchDoom19 getSourcePatch()
	{
		return UDOOM19PATCH;
	}
	
	/**
	 * @return the string offset for sound names, or null if not supported.
	 */
	@Override
	public Integer getSoundStringIndex()
	{
		return UDoom19Patch.STRING_INDEX_SOUNDS;
	}

	/**
	 * @return the string offset for sprite names, or null if not supported.
	 */
	@Override
	public Integer getSpriteStringIndex()
	{
		return UDoom19Patch.STRING_INDEX_SPRITES;
	}

}

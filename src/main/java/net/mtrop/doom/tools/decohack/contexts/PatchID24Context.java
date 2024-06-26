package net.mtrop.doom.tools.decohack.contexts;

import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerType;
import net.mtrop.doom.tools.decohack.data.enums.DEHFeatureLevel;
import net.mtrop.doom.tools.decohack.patches.DEHPatchBoom;
import net.mtrop.doom.tools.decohack.patches.PatchID24;

/**
 * Patch context for ID24
 * @author Matthew Tropiano
 */
public class PatchID24Context extends PatchDSDHackedContext
{
	private static final DEHPatchBoom ID24PATCH = new PatchID24();

	@Override
	public int getVersion() 
	{
		return 2024;
	}
	
	@Override
	public DEHPatchBoom getSourcePatch()
	{
		return ID24PATCH;
	}

	@Override
	public DEHActionPointerType getSupportedActionPointerType() 
	{
		return DEHActionPointerType.MBF21;
	}
	
	@Override
	public DEHFeatureLevel getSupportedFeatureLevel() 
	{
		return DEHFeatureLevel.ID24;
	}

}

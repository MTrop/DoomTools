package net.mtrop.doom.tools.decohack.patches;

import static net.mtrop.doom.tools.decohack.patches.ConstantsID24.*;

import net.mtrop.doom.tools.decohack.data.DEHAmmo;
import net.mtrop.doom.tools.decohack.data.DEHSound;
import net.mtrop.doom.tools.decohack.data.DEHThing;
import net.mtrop.doom.tools.decohack.data.DEHWeapon;

/**
 * Patch implementation for ID24, extending DSDHacked.
 * @author Matthew Tropiano
 */
public class PatchID24 extends PatchDSDHacked
{

	@Override
	public DEHAmmo getAmmo(int index) 
	{
		DEHAmmo out = DEHAMMOID24.get(index);
		if (out == null)
			return super.getAmmo(index);
		else
			return out;
	}

	@Override
	public Integer getSoundIndex(String name)
	{
		Integer idx = DEHSOUNDNAMESID24.get(name.toUpperCase());
		if (idx == null)
			return super.getSoundIndex(name);
		else
			return idx;
	}

	@Override
	public Integer getSpriteIndex(String name)
	{
		Integer idx = DEHSPRITENAMESID24.get(name.toUpperCase());
		if (idx == null)
			return super.getSpriteIndex(name);
		else
			return idx;
	}

	@Override
	public DEHSound getSound(int index)
	{
		DEHSound out = DEHSOUNDID24.get(index);
		if (out == null)
			return super.getSound(index);
		else
			return out;
	}

	@Override
	public DEHThing getThing(int index)
	{
		DEHThing out = DEHTHINGID24.get(index);
		if (out == null)
			return super.getThing(index);
		else
			return out;
	}

	@Override
	public DEHWeapon getWeapon(int index)
	{
		DEHWeapon out = DEHWEAPONID24.get(index);
		if (out == null)
			return super.getWeapon(index);
		else
			return out;
	}

	@Override
	protected PatchBoom.State getBoomState(int index)
	{
		PatchBoom.State out = DEHSTATEID24.get(index);
		if (out == null)
			return super.getBoomState(index);
		else
			return out;
	}
	
}

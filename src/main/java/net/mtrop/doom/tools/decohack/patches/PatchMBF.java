package net.mtrop.doom.tools.decohack.patches;

import static net.mtrop.doom.tools.decohack.patches.Constants.*;
import static net.mtrop.doom.tools.decohack.patches.ConstantsBoom.*;
import static net.mtrop.doom.tools.decohack.patches.ConstantsMBF.*;

import java.util.HashMap;
import java.util.Map;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.decohack.data.DEHSound;
import net.mtrop.doom.tools.decohack.data.DEHThing;

/**
 * Patch implementation for MBF.
 * @author Matthew Tropiano
 */
public class PatchMBF extends PatchBoom
{
	protected static final String[] SOUNDSTRINGSMBF = 
	{
		"DGSIT",
		"DGATK",
		"DGACT",
		"DGDTH",
		"DGPAIN",
	};
		
	protected static final String[] SPRITESTRINGSMBF = 
	{
		"DOGS",
		"PLS1",
		"PLS2",
		"BON3",
		"BON4",
	};

	private static final Map<String, Integer> MAP_MBFSOUNDINDEX = new HashMap<String, Integer>()
	{
		private static final long serialVersionUID = -4513058612574767102L;
		{
			for (int i = 0; i < SOUNDSTRINGSMBF.length; i++)
				put(SOUNDSTRINGSMBF[i], i + SOUNDSTRINGS.length);
		}
	};
	
	private static final Map<String, Integer> MAP_MBFSPRITEINDEX = new HashMap<String, Integer>()
	{
		private static final long serialVersionUID = -91431875042148768L;
		{
			for (int i = 0; i < SPRITESTRINGSMBF.length; i++)
				put(SPRITESTRINGSMBF[i], i + SPRITESTRINGS.length);
		}
	};
	
	// ======================================================================
	
	@Override
	public Integer getSoundIndex(String name)
	{
		return MAP_MBFSOUNDINDEX.getOrDefault(name.toUpperCase(), super.getSoundIndex(name));
	}

	@Override
	public Integer getSpriteIndex(String name)
	{
		return MAP_MBFSPRITEINDEX.getOrDefault(name.toUpperCase(), super.getSpriteIndex(name));
	}

	@Override
	public int getSoundCount() 
	{
		return DEHSOUND.length + DEHSOUNDMBF.length;
	}

	@Override
	public DEHSound getSound(int index)
	{
		if (index >= DEHSOUND.length)
			return Common.arrayElement(DEHSOUNDMBF, index - DEHSOUND.length);
		else
			return super.getSound(index);
	}

	@Override
	public int getThingCount() 
	{
		return DEHTHING.length + DEHTHINGBOOM.length + DEHTHINGMBF.length;
	}

	@Override
	public DEHThing getThing(int index)
	{
		int boomlen = DEHTHING.length + DEHTHINGBOOM.length;
		if (index >= boomlen)
			return Common.arrayElement(DEHTHINGMBF, index - boomlen);
		else
			return super.getThing(index);
	}

	@Override
	public int getStateCount()
	{
		return DEHSTATE.length + DEHSTATEMBF.length;
	}

	protected PatchBoom.State getBoomState(int index)
	{
		if (index >= DEHSTATE.length)
			return Common.arrayElement(DEHSTATEMBF, index - DEHSTATE.length);			
		else
			return super.getBoomState(index);
	}

	@Override
	public Integer getStateActionPointerIndex(int stateIndex) 
	{
		return stateIndex;
	}

	@Override
	public int getActionPointerCount() 
	{
		return getStateCount();
	}

}

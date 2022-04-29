/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.patches;

import static net.mtrop.doom.tools.decohack.patches.Constants.*;
import static net.mtrop.doom.tools.decohack.patches.ConstantsBoom.*;
import static net.mtrop.doom.tools.decohack.patches.ConstantsMBF.*;

import java.util.HashMap;
import java.util.Map;

import net.mtrop.doom.tools.decohack.data.DEHSound;
import net.mtrop.doom.tools.decohack.data.DEHThing;
import net.mtrop.doom.tools.struct.util.ArrayUtils;

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
			return ArrayUtils.arrayElement(DEHSOUNDMBF, index - DEHSOUND.length);
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
			return ArrayUtils.arrayElement(DEHTHINGMBF, index - boomlen);
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
			return ArrayUtils.arrayElement(DEHSTATEMBF, index - DEHSTATE.length);			
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

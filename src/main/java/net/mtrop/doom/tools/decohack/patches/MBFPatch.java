package net.mtrop.doom.tools.decohack.patches;

import net.mtrop.doom.tools.decohack.DEHActionPointer;
import net.mtrop.doom.tools.decohack.DEHSound;
import net.mtrop.doom.tools.decohack.DEHState;
import net.mtrop.doom.tools.decohack.DEHThing;

import static net.mtrop.doom.tools.decohack.patches.PatchConstants.*;
import static net.mtrop.doom.tools.decohack.patches.PatchConstantsBoom.*;
import static net.mtrop.doom.tools.decohack.patches.PatchConstantsMBF.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Patch implementation for MBF.
 * @author Matthew Tropiano
 */
public class MBFPatch extends BoomPatch
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

	private static final Map<String, Integer> MAP_SOUNDINDEX = new HashMap<String, Integer>()
	{
		private static final long serialVersionUID = -4513058612574767102L;
		{
			int len = SOUNDSTRINGS.length + SOUNDSTRINGSMBF.length;
			for (int i = 0; i < len; i++)
			{
				if (i < SOUNDSTRINGS.length)
					put(SOUNDSTRINGS[i], i);
				else
					put(SOUNDSTRINGSMBF[i - SOUNDSTRINGS.length], i);
			}
		}
	};
	
	private static final Map<String, Integer> MAP_SPRITEINDEX = new HashMap<String, Integer>()
	{
		private static final long serialVersionUID = -91431875042148768L;
		{
			int len = SPRITESTRINGS.length + SPRITESTRINGSMBF.length;
			for (int i = 0; i < len; i++)
			{
				if (i < SPRITESTRINGS.length)
					put(SPRITESTRINGS[i], i);
				else
					put(SPRITESTRINGSMBF[i - SPRITESTRINGS.length], i);
			}
		}
	};
	
	// ======================================================================
	
	@Override
	public Integer getSoundIndex(String name)
	{
		return MAP_SOUNDINDEX.get(name.toUpperCase());
	}

	@Override
	public Integer getSpriteIndex(String name)
	{
		return MAP_SPRITEINDEX.get(name.toUpperCase());
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
			return DEHSOUNDMBF[index - DEHSOUND.length];
		else
			return DEHSOUND[index];
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
			return DEHTHINGMBF[index - boomlen];
		else if (index >= DEHTHING.length)
			return DEHTHINGBOOM[index - DEHTHING.length];
		else
			return DEHTHING[index];
	}

	@Override
	public int getStateCount()
	{
		return DEHSTATE.length + DEHSTATEMBF.length;
	}

	protected BoomState getBoomState(int index)
	{
		if (index >= DEHSTATE.length)
			return DEHSTATEMBF[index - DEHSTATE.length];			
		else
			return DEHSTATE[index];
	}
	
	@Override
	public DEHState getState(int index) 
	{
		return getBoomState(index).getState();
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

	@Override
	public DEHActionPointer getActionPointer(int index)
	{
		return getBoomState(index).getPointer();
	}

}

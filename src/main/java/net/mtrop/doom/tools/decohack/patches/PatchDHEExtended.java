package net.mtrop.doom.tools.decohack.patches;

import net.mtrop.doom.tools.decohack.DEHActionPointer;
import net.mtrop.doom.tools.decohack.DEHState;
import net.mtrop.doom.tools.decohack.DEHThing;

import static net.mtrop.doom.tools.decohack.patches.Constants.*;
import static net.mtrop.doom.tools.decohack.patches.ConstantsBoom.*;
import static net.mtrop.doom.tools.decohack.patches.ConstantsMBF.*;
import static net.mtrop.doom.tools.decohack.patches.ConstantsBoomExtended.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Patch implementation for Extended DeHackEd.
 * @author Matthew Tropiano
 */
public class PatchDHEExtended extends PatchMBF
{
	protected static final String[] SPRITESTRINGSEXTENDED = 
	{
		"BLD2",
		"SP00",
		"SP01",
		"SP02",
		"SP03",
		"SP04",
		"SP05",
		"SP06",
		"SP07",
		"SP08",
		"SP09",
		"SP10",
		"SP11",
		"SP12",
		"SP13",
		"SP14",
		"SP15",
		"SP16",
		"SP17",
		"SP18",
		"SP19",
		"SP20",
		"SP21",
		"SP22",
		"SP23",
		"SP24",
		"SP25",
		"SP26",
		"SP27",
		"SP28",
		"SP29",
		"SP30",
		"SP31",
		"SP32",
		"SP33",
		"SP34",
		"SP35",
		"SP36",
		"SP37",
		"SP38",
		"SP39",
		"SP40",
		"SP41",
		"SP42",
		"SP43",
		"SP44",
		"SP45",
		"SP46",
		"SP47",
		"SP48",
		"SP49",
		"SP50",
		"SP51",
		"SP52",
		"SP53",
		"SP54",
		"SP55",
		"SP56",
		"SP57",
		"SP58",
		"SP59",
		"SP60",
		"SP61",
		"SP62",
		"SP63",
		"SP64",
		"SP65",
		"SP66",
		"SP67",
		"SP68",
		"SP69",
		"SP70",
		"SP71",
		"SP72",
		"SP73",
		"SP74",
		"SP75",
		"SP76",
		"SP77",
		"SP78",
		"SP79",
		"SP80",
		"SP81",
		"SP82",
		"SP83",
		"SP84",
		"SP85",
		"SP86",
		"SP87",
		"SP88",
		"SP89",
		"SP90",
		"SP91",
		"SP92",
		"SP93",
		"SP94",
		"SP95",
		"SP96",
		"SP97",
		"SP98",
		"SP99",
	};

	private static final Map<String, Integer> MAP_SPRITEINDEX = new HashMap<String, Integer>()
	{
		private static final long serialVersionUID = -5790149102738250549L;
		{
			int mbflen = SPRITESTRINGS.length + SPRITESTRINGSMBF.length;
			int len = SPRITESTRINGS.length + SPRITESTRINGSMBF.length + SPRITESTRINGSEXTENDED.length;
			for (int i = 0; i < len; i++)
			{
				if (i >= mbflen)
					put(SPRITESTRINGSEXTENDED[i - mbflen], i);
				else if (i >= SPRITESTRINGS.length)
					put(SPRITESTRINGSMBF[i - SPRITESTRINGS.length], i);
				else
					put(SPRITESTRINGS[i], i);
			}
		}
	};
	
	// ======================================================================
	
	@Override
	public Integer getSpriteIndex(String name)
	{
		return MAP_SPRITEINDEX.get(name.toUpperCase());
	}

	@Override
	public int getThingCount() 
	{
		return DEHTHING.length + DEHTHINGBOOM.length + DEHTHINGMBF.length + DEHTHINGEXTENDED.length;
	}

	@Override
	public DEHThing getThing(int index)
	{
		int mbflen = DEHTHING.length + DEHTHINGBOOM.length + DEHTHINGMBF.length;
		int boomlen = DEHTHING.length + DEHTHINGBOOM.length;

		if (index >= mbflen)
			return DEHTHINGEXTENDED[index - mbflen];
		else if (index >= boomlen)
			return DEHTHINGMBF[index - boomlen];
		else if (index >= DEHTHING.length)
			return DEHTHINGBOOM[index - DEHTHING.length];
		else
			return DEHTHING[index];
	}

	@Override
	public int getStateCount()
	{
		return 4000;
	}

	protected PatchBoom.State getBoomState(int index)
	{
		int len = DEHSTATE.length + DEHSTATEMBF.length + DEHSTATEEXTENDED.length;
		int mbflen = DEHSTATE.length + DEHSTATEMBF.length;
		
		if (index >= getStateCount())
			throw new ArrayIndexOutOfBoundsException(index);
		else if (index >= len && index < getStateCount())
			return PatchBoom.State.create(DEHState.create(138, 0, false, index, -1), DEHActionPointer.NULL);			
		else if (index >= mbflen)
			return DEHSTATEEXTENDED[index - mbflen];			
		else if (index >= DEHSTATE.length)
			return DEHSTATEMBF[index - DEHSTATE.length];			
		else
			return DEHSTATE[index];
	}
	
}

/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.patches;

import static net.mtrop.doom.tools.decohack.patches.Constants.*;
import static net.mtrop.doom.tools.decohack.patches.ConstantsBoom.*;
import static net.mtrop.doom.tools.decohack.patches.ConstantsMBF.*;
import static net.mtrop.doom.tools.decohack.patches.ConstantsExtended.*;

import java.util.HashMap;
import java.util.Map;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.decohack.data.DEHSound;
import net.mtrop.doom.tools.decohack.data.DEHState;
import net.mtrop.doom.tools.decohack.data.DEHThing;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointer;

/**
 * Patch implementation for Extended DeHackEd.
 * @author Matthew Tropiano
 */
public class PatchExtended extends PatchMBF
{
	private static final int SOUND_INDEX_EXTENDED_START = 500;

	protected static final String[] SOUNDSTRINGSEXTENDED = 
	{
		"FRE000",
		"FRE001",
		"FRE002",
		"FRE003",
		"FRE004",
		"FRE005",
		"FRE006",
		"FRE007",
		"FRE008",
		"FRE009",
		"FRE010",
		"FRE011",
		"FRE012",
		"FRE013",
		"FRE014",
		"FRE015",
		"FRE016",
		"FRE017",
		"FRE018",
		"FRE019",
		"FRE020",
		"FRE021",
		"FRE022",
		"FRE023",
		"FRE024",
		"FRE025",
		"FRE026",
		"FRE027",
		"FRE028",
		"FRE029",
		"FRE030",
		"FRE031",
		"FRE032",
		"FRE033",
		"FRE034",
		"FRE035",
		"FRE036",
		"FRE037",
		"FRE038",
		"FRE039",
		"FRE040",
		"FRE041",
		"FRE042",
		"FRE043",
		"FRE044",
		"FRE045",
		"FRE046",
		"FRE047",
		"FRE048",
		"FRE049",
		"FRE050",
		"FRE051",
		"FRE052",
		"FRE053",
		"FRE054",
		"FRE055",
		"FRE056",
		"FRE057",
		"FRE058",
		"FRE059",
		"FRE060",
		"FRE061",
		"FRE062",
		"FRE063",
		"FRE064",
		"FRE065",
		"FRE066",
		"FRE067",
		"FRE068",
		"FRE069",
		"FRE070",
		"FRE071",
		"FRE072",
		"FRE073",
		"FRE074",
		"FRE075",
		"FRE076",
		"FRE077",
		"FRE078",
		"FRE079",
		"FRE080",
		"FRE081",
		"FRE082",
		"FRE083",
		"FRE084",
		"FRE085",
		"FRE086",
		"FRE087",
		"FRE088",
		"FRE089",
		"FRE090",
		"FRE091",
		"FRE092",
		"FRE093",
		"FRE094",
		"FRE095",
		"FRE096",
		"FRE097",
		"FRE098",
		"FRE099",
		"FRE100",
		"FRE101",
		"FRE102",
		"FRE103",
		"FRE104",
		"FRE105",
		"FRE106",
		"FRE107",
		"FRE108",
		"FRE109",
		"FRE110",
		"FRE111",
		"FRE112",
		"FRE113",
		"FRE114",
		"FRE115",
		"FRE116",
		"FRE117",
		"FRE118",
		"FRE119",
		"FRE120",
		"FRE121",
		"FRE122",
		"FRE123",
		"FRE124",
		"FRE125",
		"FRE126",
		"FRE127",
		"FRE128",
		"FRE129",
		"FRE130",
		"FRE131",
		"FRE132",
		"FRE133",
		"FRE134",
		"FRE135",
		"FRE136",
		"FRE137",
		"FRE138",
		"FRE139",
		"FRE140",
		"FRE141",
		"FRE142",
		"FRE143",
		"FRE144",
		"FRE145",
		"FRE146",
		"FRE147",
		"FRE148",
		"FRE149",
		"FRE150",
		"FRE151",
		"FRE152",
		"FRE153",
		"FRE154",
		"FRE155",
		"FRE156",
		"FRE157",
		"FRE158",
		"FRE159",
		"FRE160",
		"FRE161",
		"FRE162",
		"FRE163",
		"FRE164",
		"FRE165",
		"FRE166",
		"FRE167",
		"FRE168",
		"FRE169",
		"FRE170",
		"FRE171",
		"FRE172",
		"FRE173",
		"FRE174",
		"FRE175",
		"FRE176",
		"FRE177",
		"FRE178",
		"FRE179",
		"FRE180",
		"FRE181",
		"FRE182",
		"FRE183",
		"FRE184",
		"FRE185",
		"FRE186",
		"FRE187",
		"FRE188",
		"FRE189",
		"FRE190",
		"FRE191",
		"FRE192",
		"FRE193",
		"FRE194",
		"FRE195",
		"FRE196",
		"FRE197",
		"FRE198",
		"FRE199",
	};

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

	private static final Map<String, Integer> MAP_SOUNDINDEX = new HashMap<String, Integer>()
	{
		private static final long serialVersionUID = -4513058612574767103L;
		{
			// the extended sound range expliticly starts at 500, skipping several
			// indices, so we need to make the output index relative to that.
			int extstart = SOUND_INDEX_EXTENDED_START;
			int mbflen = SOUNDSTRINGS.length + SOUNDSTRINGSMBF.length;
			int len = SOUND_INDEX_EXTENDED_START + SOUNDSTRINGSEXTENDED.length;
			for (int i = 1; i < len; i++)
			{
				if (i >= extstart)
					put(SOUNDSTRINGSEXTENDED[i - extstart], i); // offset 1 for sfx_None
				else if (i >= mbflen)
					continue; // blank until extended
				else if (i >= SOUNDSTRINGS.length)
					put(SOUNDSTRINGSMBF[i - SOUNDSTRINGS.length], i); // offset 1 for sfx_None
				else
					put(SOUNDSTRINGS[i].toUpperCase(), i);
			}
		}
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
	
	private static final Map<Integer, State> MAP_POSTEXTENDEDSTATECACHE = new HashMap<Integer, State>();
	
	private static State getPostExtendedState(int index)
	{
		State state;
		if ((state = MAP_POSTEXTENDEDSTATECACHE.get(index)) == null)
			MAP_POSTEXTENDEDSTATECACHE.put(index, state = PatchBoom.State.create(DEHState.create(138, 0, false, index, -1), DEHActionPointer.NULL));
		return state;
	}
	
	// ======================================================================

	@Override
	public Integer getSoundIndex(String name)
	{
		return MAP_SOUNDINDEX.getOrDefault(name.toUpperCase(), super.getSoundIndex(name));
	}

	@Override
	public Integer getSpriteIndex(String name)
	{
		return MAP_SPRITEINDEX.getOrDefault(name.toUpperCase(), super.getSpriteIndex(name));
	}

	@Override
	public int getSoundCount()
	{
		return SOUND_INDEX_EXTENDED_START + SOUNDSTRINGSEXTENDED.length;
	}

	@Override
	public DEHSound getSound(int index)
	{
		if (index >= SOUND_INDEX_EXTENDED_START)
			return Common.arrayElement(DEHSOUNDEXTENDED, index - SOUND_INDEX_EXTENDED_START);
		else
			return super.getSound(index);
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
		if (index >= mbflen)
			return Common.arrayElement(DEHTHINGEXTENDED, index - mbflen);
		else
			return super.getThing(index);
	}

	@Override
	public int getStateCount()
	{
		return 4000;
	}

	protected PatchBoom.State getBoomState(int index)
	{
		int mbflen = DEHSTATE.length + DEHSTATEMBF.length;
		int extlen = mbflen + DEHSTATEEXTENDED.length;
		
		if (index >= getStateCount())
			return null;
		else if (index >= extlen)
			return getPostExtendedState(index);
		else if (index >= mbflen)
			return Common.arrayElement(DEHSTATEEXTENDED, index - mbflen);
		else
			return super.getBoomState(index);
	}
	
}

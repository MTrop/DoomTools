package net.mtrop.doom.tools.decohack.patches;

import static net.mtrop.doom.tools.decohack.patches.Constants.*;
import static net.mtrop.doom.tools.decohack.patches.ConstantsBoom.*;
import static net.mtrop.doom.tools.decohack.patches.ConstantsMBF.*;
import static net.mtrop.doom.tools.decohack.patches.ConstantsBoomExtended.*;

import java.util.HashMap;
import java.util.Map;

import net.mtrop.doom.tools.decohack.data.DEHActionPointer;
import net.mtrop.doom.tools.decohack.data.DEHState;
import net.mtrop.doom.tools.decohack.data.DEHThing;

/**
 * Patch implementation for Extended DeHackEd.
 * @author Matthew Tropiano
 */
public class PatchDHEExtended extends PatchMBF
{
	protected static final String[] SOUNDSTRINGSEXTENDED = 
	{
		"fre000",
		"fre001",
		"fre002",
		"fre003",
		"fre004",
		"fre005",
		"fre006",
		"fre007",
		"fre008",
		"fre009",
		"fre010",
		"fre011",
		"fre012",
		"fre013",
		"fre014",
		"fre015",
		"fre016",
		"fre017",
		"fre018",
		"fre019",
		"fre020",
		"fre021",
		"fre022",
		"fre023",
		"fre024",
		"fre025",
		"fre026",
		"fre027",
		"fre028",
		"fre029",
		"fre030",
		"fre031",
		"fre032",
		"fre033",
		"fre034",
		"fre035",
		"fre036",
		"fre037",
		"fre038",
		"fre039",
		"fre040",
		"fre041",
		"fre042",
		"fre043",
		"fre044",
		"fre045",
		"fre046",
		"fre047",
		"fre048",
		"fre049",
		"fre050",
		"fre051",
		"fre052",
		"fre053",
		"fre054",
		"fre055",
		"fre056",
		"fre057",
		"fre058",
		"fre059",
		"fre060",
		"fre061",
		"fre062",
		"fre063",
		"fre064",
		"fre065",
		"fre066",
		"fre067",
		"fre068",
		"fre069",
		"fre070",
		"fre071",
		"fre072",
		"fre073",
		"fre074",
		"fre075",
		"fre076",
		"fre077",
		"fre078",
		"fre079",
		"fre080",
		"fre081",
		"fre082",
		"fre083",
		"fre084",
		"fre085",
		"fre086",
		"fre087",
		"fre088",
		"fre089",
		"fre090",
		"fre091",
		"fre092",
		"fre093",
		"fre094",
		"fre095",
		"fre096",
		"fre097",
		"fre098",
		"fre099",
		"fre100",
		"fre101",
		"fre102",
		"fre103",
		"fre104",
		"fre105",
		"fre106",
		"fre107",
		"fre108",
		"fre109",
		"fre110",
		"fre111",
		"fre112",
		"fre113",
		"fre114",
		"fre115",
		"fre116",
		"fre117",
		"fre118",
		"fre119",
		"fre120",
		"fre121",
		"fre122",
		"fre123",
		"fre124",
		"fre125",
		"fre126",
		"fre127",
		"fre128",
		"fre129",
		"fre130",
		"fre131",
		"fre132",
		"fre133",
		"fre134",
		"fre135",
		"fre136",
		"fre137",
		"fre138",
		"fre139",
		"fre140",
		"fre141",
		"fre142",
		"fre143",
		"fre144",
		"fre145",
		"fre146",
		"fre147",
		"fre148",
		"fre149",
		"fre150",
		"fre151",
		"fre152",
		"fre153",
		"fre154",
		"fre155",
		"fre156",
		"fre157",
		"fre158",
		"fre159",
		"fre160",
		"fre161",
		"fre162",
		"fre163",
		"fre164",
		"fre165",
		"fre166",
		"fre167",
		"fre168",
		"fre169",
		"fre170",
		"fre171",
		"fre172",
		"fre173",
		"fre174",
		"fre175",
		"fre176",
		"fre177",
		"fre178",
		"fre179",
		"fre180",
		"fre181",
		"fre182",
		"fre183",
		"fre184",
		"fre185",
		"fre186",
		"fre187",
		"fre188",
		"fre189",
		"fre190",
		"fre191",
		"fre192",
		"fre193",
		"fre194",
		"fre195",
		"fre196",
		"fre197",
		"fre198",
		"fre199",
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
			int extstart = 500;
			int len = SOUNDSTRINGS.length + SOUNDSTRINGSEXTENDED.length;
			for (int i = 0; i < len; i++)
			{
				if (i >= SOUNDSTRINGS.length)
					put(SOUNDSTRINGSEXTENDED[i - SOUNDSTRINGS.length].toUpperCase(), i - SOUNDSTRINGS.length + extstart - 1);
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

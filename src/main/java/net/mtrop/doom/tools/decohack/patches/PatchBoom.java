package net.mtrop.doom.tools.decohack.patches;

import static net.mtrop.doom.tools.decohack.patches.Constants.*;
import static net.mtrop.doom.tools.decohack.patches.ConstantsBoom.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.mtrop.doom.tools.decohack.data.DEHActionPointer;
import net.mtrop.doom.tools.decohack.data.DEHAmmo;
import net.mtrop.doom.tools.decohack.data.DEHMiscellany;
import net.mtrop.doom.tools.decohack.data.DEHSound;
import net.mtrop.doom.tools.decohack.data.DEHState;
import net.mtrop.doom.tools.decohack.data.DEHThing;
import net.mtrop.doom.tools.decohack.data.DEHWeapon;

/**
 * Patch implementation for Doom 1.9.
 * @author Matthew Tropiano
 */
public class PatchBoom implements DEHPatchBoom
{
	protected static final String[] SOUNDSTRINGS = 
	{
		"pistol",
		"shotgn",
		"sgcock",
		"dshtgn",
		"dbopn",
		"dbcls",
		"dbload",
		"plasma",
		"bfg",
		"sawup",
		"sawidl",
		"sawful",
		"sawhit",
		"rlaunc",
		"rxplod",
		"firsht",
		"firxpl",
		"pstart",
		"pstop",
		"doropn",
		"dorcls",
		"stnmov",
		"swtchn",
		"swtchx",
		"plpain",
		"dmpain",
		"popain",
		"vipain",
		"mnpain",
		"pepain",
		"slop",
		"itemup",
		"wpnup",
		"oof",
		"telept",
		"posit1",
		"posit2",
		"posit3",
		"bgsit1",
		"bgsit2",
		"sgtsit",
		"cacsit",
		"brssit",
		"cybsit",
		"spisit",
		"bspsit",
		"kntsit",
		"vilsit",
		"mansit",
		"pesit",
		"sklatk",
		"sgtatk",
		"skepch",
		"vilatk",
		"claw",
		"skeswg",
		"pldeth",
		"pdiehi",
		"podth1",
		"podth2",
		"podth3",
		"bgdth1",
		"bgdth2",
		"sgtdth",
		"cacdth",
		"skldth",
		"brsdth",
		"cybdth",
		"spidth",
		"bspdth",
		"vildth",
		"kntdth",
		"pedth",
		"skedth",
		"posact",
		"bgact",
		"dmact",
		"bspact",
		"bspwlk",
		"vilact",
		"noway",
		"barexp",
		"punch",
		"hoof",
		"metal",
		"chgun",
		"tink",
		"bdopn",
		"bdcls",
		"itmbk",
		"flame",
		"flamst",
		"getpow",
		"bospit",
		"boscub",
		"bossit",
		"bospn",
		"bosdth",
		"manatk",
		"mandth",
		"sssit",
		"ssdth",
		"keenpn",
		"keendt",
		"skeact",
		"skesit",
		"skeatk",
		"radio",
	};
		
	protected static final String[] SPRITESTRINGS = 
	{
		"TROO",
		"SHTG",
		"PUNG",
		"PISG",
		"PISF",
		"SHTF",
		"SHT2",
		"CHGG",
		"CHGF",
		"MISG",
		"MISF",
		"SAWG",
		"PLSG",
		"PLSF",
		"BFGG",
		"BFGF",
		"BLUD",
		"PUFF",
		"BAL1",
		"BAL2",
		"PLSS",
		"PLSE",
		"MISL",
		"BFS1",
		"BFE1",
		"BFE2",
		"TFOG",
		"IFOG",
		"PLAY",
		"POSS",
		"SPOS",
		"VILE",
		"FIRE",
		"FATB",
		"FBXP",
		"SKEL",
		"MANF",
		"FATT",
		"CPOS",
		"SARG",
		"HEAD",
		"BAL7",
		"BOSS",
		"BOS2",
		"SKUL",
		"SPID",
		"BSPI",
		"APLS",
		"APBX",
		"CYBR",
		"PAIN",
		"SSWV",
		"KEEN",
		"BBRN",
		"BOSF",
		"ARM1",
		"ARM2",
		"BAR1",
		"BEXP",
		"FCAN",
		"BON1",
		"BON2",
		"BKEY",
		"RKEY",
		"YKEY",
		"BSKU",
		"RSKU",
		"YSKU",
		"STIM",
		"MEDI",
		"SOUL",
		"PINV",
		"PSTR",
		"PINS",
		"MEGA",
		"SUIT",
		"PMAP",
		"PVIS",
		"CLIP",
		"AMMO",
		"ROCK",
		"BROK",
		"CELL",
		"CELP",
		"SHEL",
		"SBOX",
		"BPAK",
		"BFUG",
		"MGUN",
		"CSAW",
		"LAUN",
		"PLAS",
		"SHOT",
		"SGN2",
		"COLU",
		"SMT2",
		"GOR1",
		"POL2",
		"POL5",
		"POL4",
		"POL3",
		"POL1",
		"POL6",
		"GOR2",
		"GOR3",
		"GOR4",
		"GOR5",
		"SMIT",
		"COL1",
		"COL2",
		"COL3",
		"COL4",
		"CAND",
		"CBRA",
		"COL6",
		"TRE1",
		"TRE2",
		"ELEC",
		"CEYE",
		"FSKU",
		"COL5",
		"TBLU",
		"TGRN",
		"TRED",
		"SMBT",
		"SMGT",
		"SMRT",
		"HDB1",
		"HDB2",
		"HDB3",
		"HDB4",
		"HDB5",
		"HDB6",
		"POB1",
		"POB2",
		"BRS1",
		"TLMP",
		"TLP2",
		"TNT1",
	};

	private static final Map<String, Integer> MAP_SOUNDINDEX = new HashMap<String, Integer>()
	{
		private static final long serialVersionUID = -4513058612574767102L;
		{
			for (int i = 0; i < SOUNDSTRINGS.length; i++)
				put(SOUNDSTRINGS[i], i);
		}
	};
	
	private static final Map<String, Integer> MAP_SPRITEINDEX = new HashMap<String, Integer>()
	{
		private static final long serialVersionUID = -91431875042148768L;
		{
			for (int i = 0; i < SPRITESTRINGS.length; i++)
				put(SPRITESTRINGS[i], i);
		}
	};
	
	private static final Map<EpisodeMap, Integer> MAP_PARS = new HashMap<EpisodeMap, Integer>()
	{
		private static final long serialVersionUID = -7194135744791190357L;
		{
			put(EpisodeMap.create(1, 1), 30);
			put(EpisodeMap.create(1, 2), 75);
			put(EpisodeMap.create(1, 3), 120);
			put(EpisodeMap.create(1, 4), 90);
			put(EpisodeMap.create(1, 5), 165);
			put(EpisodeMap.create(1, 6), 180);
			put(EpisodeMap.create(1, 7), 180);
			put(EpisodeMap.create(1, 8), 165);
			put(EpisodeMap.create(1, 9), 165);
			put(EpisodeMap.create(2, 1), 90);
			put(EpisodeMap.create(2, 2), 90);
			put(EpisodeMap.create(2, 3), 90);
			put(EpisodeMap.create(2, 4), 120);
			put(EpisodeMap.create(2, 5), 90);
			put(EpisodeMap.create(2, 6), 360);
			put(EpisodeMap.create(2, 7), 240);
			put(EpisodeMap.create(2, 8), 135);
			put(EpisodeMap.create(2, 9), 170);
			put(EpisodeMap.create(3, 1), 90);
			put(EpisodeMap.create(3, 2), 45);
			put(EpisodeMap.create(3, 3), 90);
			put(EpisodeMap.create(3, 4), 150);
			put(EpisodeMap.create(3, 5), 90);
			put(EpisodeMap.create(3, 6), 90);
			put(EpisodeMap.create(3, 7), 165);
			put(EpisodeMap.create(3, 8), 105);
			put(EpisodeMap.create(3, 9), 135);
			put(EpisodeMap.create(4, 1), 165);
			put(EpisodeMap.create(4, 2), 255);
			put(EpisodeMap.create(4, 3), 135);
			put(EpisodeMap.create(4, 4), 150);
			put(EpisodeMap.create(4, 5), 180);
			put(EpisodeMap.create(4, 6), 390);
			put(EpisodeMap.create(4, 7), 135);
			put(EpisodeMap.create(4, 8), 360);
			put(EpisodeMap.create(4, 9), 180);
			put(EpisodeMap.create(0, 1), 30);
			put(EpisodeMap.create(0, 2), 90);
			put(EpisodeMap.create(0, 3), 120);
			put(EpisodeMap.create(0, 4), 120);
			put(EpisodeMap.create(0, 5), 90);
			put(EpisodeMap.create(0, 6), 150);
			put(EpisodeMap.create(0, 7), 120);
			put(EpisodeMap.create(0, 8), 120);
			put(EpisodeMap.create(0, 9), 270);
			put(EpisodeMap.create(0, 10), 90);
			put(EpisodeMap.create(0, 11), 210);
			put(EpisodeMap.create(0, 12), 150);
			put(EpisodeMap.create(0, 13), 150);
			put(EpisodeMap.create(0, 14), 150);
			put(EpisodeMap.create(0, 15), 210);
			put(EpisodeMap.create(0, 16), 150);
			put(EpisodeMap.create(0, 17), 420);
			put(EpisodeMap.create(0, 18), 150);
			put(EpisodeMap.create(0, 19), 210);
			put(EpisodeMap.create(0, 20), 150);
			put(EpisodeMap.create(0, 21), 240);
			put(EpisodeMap.create(0, 22), 150);
			put(EpisodeMap.create(0, 23), 180);
			put(EpisodeMap.create(0, 24), 150);
			put(EpisodeMap.create(0, 25), 150);
			put(EpisodeMap.create(0, 26), 300);
			put(EpisodeMap.create(0, 27), 350);
			put(EpisodeMap.create(0, 28), 420);
			put(EpisodeMap.create(0, 29), 300);
			put(EpisodeMap.create(0, 30), 180);
			put(EpisodeMap.create(0, 31), 120);
			put(EpisodeMap.create(0, 32), 30);
		}
	};
	
	private static final Map<String, String> MAP_STRINGS = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER)
	{
		private static final long serialVersionUID = -419155824507194333L;
		{
			put("D_DEVSTR", "Development mode ON.");
			put("D_CDROM", "CD-ROM Version: default.cfg from c:\\doomdata");
			put("QUITMSG", "are you sure you want to\nquit this great game?");
			put("LOADNET", "you can't do load while in a net game!");
			put("QLOADNET", "you can't quickload during a netgame!");
			put("QSAVESPOT", "you haven't picked a quicksave slot yet!");
			put("SAVEDEAD", "you can't save if you aren't playing!");
			put("QSPROMPT", "quicksave over your game named\n\n'%s'?");
			put("QLPROMPT", "do you want to quickload the game named\n\n'%s'?");
			put("NEWGAME", "you can't start a new game\nwhile in a network game.\n\npress a key.");
			put("NIGHTMARE", "are you sure? this skill level\nisn't even remotely fair.\n\npress y or n.");
			put("SWSTRING", "this is the shareware version of doom.\n\nyou need to order the entire trilogy.\n\npress a key.");
			put("MSGOFF", "Messages OFF");
			put("MSGON", "Messages ON");
			put("NETEND", "you can't end a netgame!");
			put("ENDGAME", "are you sure you want to end the game?");
			put("DETAILHI", "High detail");
			put("DETAILLO", "Low detail");
			put("GAMMALVL0", "Gamma correction OFF");
			put("GAMMALVL1", "Gamma correction level 1");
			put("GAMMALVL2", "Gamma correction level 2");
			put("GAMMALVL3", "Gamma correction level 3");
			put("GAMMALVL4", "Gamma correction level 4");
			put("EMPTYSTRING", "empty slot");
			put("GGSAVED", "game saved.");
			put("SAVEGAMENAME", "BOOMSAV");
			put("GOTARMOR", "Picked up the armor.");
			put("GOTMEGA", "Picked up the MegaArmor!");
			put("GOTHTHBONUS", "Picked up a health bonus.");
			put("GOTARMBONUS", "Picked up an armor bonus.");
			put("GOTSTIM", "Picked up a stimpack.");
			put("GOTMEDINEED", "Picked up a medikit that you REALLY need!");
			put("GOTMEDIKIT", "Picked up a medikit.");
			put("GOTSUPER", "Supercharge!");
			put("GOTBLUECARD", "Picked up a blue keycard.");
			put("GOTYELWCARD", "Picked up a yellow keycard.");
			put("GOTREDCARD", "Picked up a red keycard.");
			put("GOTBLUESKUL", "Picked up a blue skull key.");
			put("GOTYELWSKUL", "Picked up a yellow skull key.");
			put("GOTREDSKUL", "Picked up a red skull key.");
			put("GOTINVUL", "Invulnerability!");
			put("GOTBERSERK", "Berserk!");
			put("GOTINVIS", "Partial Invisibility");
			put("GOTSUIT", "Radiation Shielding Suit");
			put("GOTMAP", "Computer Area Map");
			put("GOTVISOR", "Light Amplification Visor");
			put("GOTMSPHERE", "MegaSphere!");
			put("GOTCLIP", "Picked up a clip.");
			put("GOTCLIPBOX", "Picked up a box of bullets.");
			put("GOTROCKET", "Picked up a rocket.");
			put("GOTROCKBOX", "Picked up a box of rockets.");
			put("GOTCELL", "Picked up an energy cell.");
			put("GOTCELLBOX", "Picked up an energy cell pack.");
			put("GOTSHELLS", "Picked up 4 shotgun shells.");
			put("GOTSHELLBOX", "Picked up a box of shotgun shells.");
			put("GOTBACKPACK", "Picked up a backpack full of ammo!");
			put("GOTBFG9000", "You got the BFG9000!  Oh, yes.");
			put("GOTCHAINGUN", "You got the chaingun!");
			put("GOTCHAINSAW", "A chainsaw!  Find some meat!");
			put("GOTLAUNCHER", "You got the rocket launcher!");
			put("GOTPLASMA", "You got the plasma gun!");
			put("GOTSHOTGUN", "You got the shotgun!");
			put("GOTSHOTGUN2", "You got the super shotgun!");
			put("PD_BLUEO", "You need a blue key to activate this object");
			put("PD_REDO", "You need a red key to activate this object");
			put("PD_YELLOWO", "You need a yellow key to activate this object");
			put("PD_BLUEK", "You need a blue key to open this door");
			put("PD_REDK", "You need a red key to open this door");
			put("PD_YELLOWK", "You need a yellow key to open this door");
			put("PD_BLUEC", "You need a blue card to open this door");
			put("PD_REDC", "You need a red card to open this door");
			put("PD_YELLOWC", "You need a yellow card to open this door");
			put("PD_BLUES", "You need a blue skull to open this door");
			put("PD_REDS", "You need a red skull to open this door");
			put("PD_YELLOWS", "You need a yellow skull to open this door");
			put("PD_ANY", "Any key will open this door");
			put("PD_ALL3", "You need all three keys to open this door");
			put("PD_ALL6", "You need all six keys to open this door");
			put("HUSTR_MSGU", "[Message unsent]");
			put("HUSTR_MESSAGESENT", "[Message Sent]");
			put("HUSTR_CHATMACRO1", "I'm ready to kick butt!");
			put("HUSTR_CHATMACRO2", "I'm OK.");
			put("HUSTR_CHATMACRO3", "I'm not looking too good!");
			put("HUSTR_CHATMACRO4", "Help!");
			put("HUSTR_CHATMACRO5", "You suck!");
			put("HUSTR_CHATMACRO6", "Next time, scumbag...");
			put("HUSTR_CHATMACRO7", "Come here!");
			put("HUSTR_CHATMACRO8", "I'll take care of it.");
			put("HUSTR_CHATMACRO9", "Yes");
			put("HUSTR_CHATMACRO0", "No");
			put("HUSTR_TALKTOSELF1", "You mumble to yourself");
			put("HUSTR_TALKTOSELF2", "Who's there?");
			put("HUSTR_TALKTOSELF3", "You scare yourself");
			put("HUSTR_TALKTOSELF4", "You start to rave");
			put("HUSTR_TALKTOSELF5", "You've lost it...");
			put("HUSTR_PLRGREEN", "Green: ");
			put("HUSTR_PLRINDIGO", "Indigo: ");
			put("HUSTR_PLRBROWN", "Brown: ");
			put("HUSTR_PLRRED", "Red: ");
			put("HUSTR_E1M1", "E1M1: Hangar");
			put("HUSTR_E1M2", "E1M2: Nuclear Plant");
			put("HUSTR_E1M3", "E1M3: Toxin Refinery");
			put("HUSTR_E1M4", "E1M4: Command Control");
			put("HUSTR_E1M5", "E1M5: Phobos Lab");
			put("HUSTR_E1M6", "E1M6: Central Processing");
			put("HUSTR_E1M7", "E1M7: Computer Station");
			put("HUSTR_E1M8", "E1M8: Phobos Anomaly");
			put("HUSTR_E1M9", "E1M9: Military Base");
			put("HUSTR_E2M1", "E2M1: Deimos Anomaly");
			put("HUSTR_E2M2", "E2M2: Containment Area");
			put("HUSTR_E2M3", "E2M3: Refinery");
			put("HUSTR_E2M4", "E2M4: Deimos Lab");
			put("HUSTR_E2M5", "E2M5: Command Center");
			put("HUSTR_E2M6", "E2M6: Halls of the Damned");
			put("HUSTR_E2M7", "E2M7: Spawning Vats");
			put("HUSTR_E2M8", "E2M8: Tower of Babel");
			put("HUSTR_E2M9", "E2M9: Fortress of Mystery");
			put("HUSTR_E3M1", "E3M1: Hell Keep");
			put("HUSTR_E3M2", "E3M2: Slough of Despair");
			put("HUSTR_E3M3", "E3M3: Pandemonium");
			put("HUSTR_E3M4", "E3M4: House of Pain");
			put("HUSTR_E3M5", "E3M5: Unholy Cathedral");
			put("HUSTR_E3M6", "E3M6: Mt. Erebus");
			put("HUSTR_E3M7", "E3M7: Limbo");
			put("HUSTR_E3M8", "E3M8: Dis");
			put("HUSTR_E3M9", "E3M9: Warrens");
			put("HUSTR_E4M1", "E4M1: Hell Beneath");
			put("HUSTR_E4M2", "E4M2: Perfect Hatred");
			put("HUSTR_E4M3", "E4M3: Sever The Wicked");
			put("HUSTR_E4M4", "E4M4: Unruly Evil");
			put("HUSTR_E4M5", "E4M5: They Will Repent");
			put("HUSTR_E4M6", "E4M6: Against Thee Wickedly");
			put("HUSTR_E4M7", "E4M7: And Hell Followed");
			put("HUSTR_E4M8", "E4M8: Unto The Cruel");
			put("HUSTR_E4M9", "E4M9: Fear");
			put("HUSTR_1", "level 1: entryway");
			put("HUSTR_2", "level 2: underhalls");
			put("HUSTR_3", "level 3: the gantlet");
			put("HUSTR_4", "level 4: the focus");
			put("HUSTR_5", "level 5: the waste tunnels");
			put("HUSTR_6", "level 6: the crusher");
			put("HUSTR_7", "level 7: dead simple");
			put("HUSTR_8", "level 8: tricks and traps");
			put("HUSTR_9", "level 9: the pit");
			put("HUSTR_10", "level 10: refueling base");
			put("HUSTR_11", "level 11: 'o' of destruction!");
			put("HUSTR_12", "level 12: the factory");
			put("HUSTR_13", "level 13: downtown");
			put("HUSTR_14", "level 14: the inmost dens");
			put("HUSTR_15", "level 15: industrial zone");
			put("HUSTR_16", "level 16: suburbs");
			put("HUSTR_17", "level 17: tenements");
			put("HUSTR_18", "level 18: the courtyard");
			put("HUSTR_19", "level 19: the citadel");
			put("HUSTR_20", "level 20: gotcha!");
			put("HUSTR_21", "level 21: nirvana");
			put("HUSTR_22", "level 22: the catacombs");
			put("HUSTR_23", "level 23: barrels o' fun");
			put("HUSTR_24", "level 24: the chasm");
			put("HUSTR_25", "level 25: bloodfalls");
			put("HUSTR_26", "level 26: the abandoned mines");
			put("HUSTR_27", "level 27: monster condo");
			put("HUSTR_28", "level 28: the spirit world");
			put("HUSTR_29", "level 29: the living end");
			put("HUSTR_30", "level 30: icon of sin");
			put("HUSTR_31", "level 31: wolfenstein");
			put("HUSTR_32", "level 32: grosse");
			put("PHUSTR_1", "level 1: congo");
			put("PHUSTR_2", "level 2: well of souls");
			put("PHUSTR_3", "level 3: aztec");
			put("PHUSTR_4", "level 4: caged");
			put("PHUSTR_5", "level 5: ghost town");
			put("PHUSTR_6", "level 6: baron's lair");
			put("PHUSTR_7", "level 7: caughtyard");
			put("PHUSTR_8", "level 8: realm");
			put("PHUSTR_9", "level 9: abattoire");
			put("PHUSTR_10", "level 10: onslaught");
			put("PHUSTR_11", "level 11: hunted");
			put("PHUSTR_12", "level 12: speed");
			put("PHUSTR_13", "level 13: the crypt");
			put("PHUSTR_14", "level 14: genesis");
			put("PHUSTR_15", "level 15: the twilight");
			put("PHUSTR_16", "level 16: the omen");
			put("PHUSTR_17", "level 17: compound");
			put("PHUSTR_18", "level 18: neurosphere");
			put("PHUSTR_19", "level 19: nme");
			put("PHUSTR_20", "level 20: the death domain");
			put("PHUSTR_21", "level 21: slayer");
			put("PHUSTR_22", "level 22: impossible mission");
			put("PHUSTR_23", "level 23: tombstone");
			put("PHUSTR_24", "level 24: the final frontier");
			put("PHUSTR_25", "level 25: the temple of darkness");
			put("PHUSTR_26", "level 26: bunker");
			put("PHUSTR_27", "level 27: anti-christ");
			put("PHUSTR_28", "level 28: the sewers");
			put("PHUSTR_29", "level 29: odyssey of noises");
			put("PHUSTR_30", "level 30: the gateway of hell");
			put("PHUSTR_31", "level 31: cyberden");
			put("PHUSTR_32", "level 32: go 2 it");
			put("THUSTR_1", "level 1: system control");
			put("THUSTR_2", "level 2: human bbq");
			put("THUSTR_3", "level 3: power control");
			put("THUSTR_4", "level 4: wormhole");
			put("THUSTR_5", "level 5: hanger");
			put("THUSTR_6", "level 6: open season");
			put("THUSTR_7", "level 7: prison");
			put("THUSTR_8", "level 8: metal");
			put("THUSTR_9", "level 9: stronghold");
			put("THUSTR_10", "level 10: redemption");
			put("THUSTR_11", "level 11: storage facility");
			put("THUSTR_12", "level 12: crater");
			put("THUSTR_13", "level 13: nukage processing");
			put("THUSTR_14", "level 14: steel works");
			put("THUSTR_15", "level 15: dead zone");
			put("THUSTR_16", "level 16: deepest reaches");
			put("THUSTR_17", "level 17: processing area");
			put("THUSTR_18", "level 18: mill");
			put("THUSTR_19", "level 19: shipping/respawning");
			put("THUSTR_20", "level 20: central processing");
			put("THUSTR_21", "level 21: administration center");
			put("THUSTR_22", "level 22: habitat");
			put("THUSTR_23", "level 23: lunar mining project");
			put("THUSTR_24", "level 24: quarry");
			put("THUSTR_25", "level 25: baron's den");
			put("THUSTR_26", "level 26: ballistyx");
			put("THUSTR_27", "level 27: mount pain");
			put("THUSTR_28", "level 28: heck");
			put("THUSTR_29", "level 29: river styx");
			put("THUSTR_30", "level 30: last call");
			put("THUSTR_31", "level 31: pharaoh");
			put("THUSTR_32", "level 32: caribbean");
			put("AMSTR_FOLLOWON", "Follow Mode ON");
			put("AMSTR_FOLLOWOFF", "Follow Mode OFF");
			put("AMSTR_GRIDON", "Grid ON");
			put("AMSTR_GRIDOFF", "Grid OFF");
			put("AMSTR_MARKEDSPOT", "Marked Spot");
			put("AMSTR_MARKSCLEARED", "All Marks Cleared");
			put("STSTR_MUS", "Music Change");
			put("STSTR_NOMUS", "IMPOSSIBLE SELECTION");
			put("STSTR_DQDON", "Degreelessness Mode On");
			put("STSTR_DQDOFF", "Degreelessness Mode Off");
			put("STSTR_KFAADDED", "Very Happy Ammo Added");
			put("STSTR_FAADDED", "Ammo (no keys) Added");
			put("STSTR_NCON", "No Clipping Mode ON");
			put("STSTR_NCOFF", "No Clipping Mode OFF");
			put("STSTR_BEHOLD", "inVuln, Str, Inviso, Rad, Allmap, or Lite-amp");
			put("STSTR_BEHOLDX", "Power-up Toggled");
			put("STSTR_CHOPPERS", "... doesn't suck - GM");
			put("STSTR_CLEV", "Changing Level...");
			put("STSTR_COMPON", "Compatibility Mode On");
			put("STSTR_COMPOFF", "Compatibility Mode Off");
			put("E1TEXT", "Once you beat the big badasses and\nclean out the moon base you're supposed\nto win, aren't you? Aren't you? Where's\nyour fat reward and ticket home? What\nthe hell is this? It's not supposed to\nend this way!\n\nIt stinks like rotten meat, but looks\nlike the lost Deimos base.  Looks like\nyou're stuck on The Shores of Hell.\nThe only way out is through.\n\nTo continue the DOOM experience, play\nThe Shores of Hell and its amazing\nsequel, Inferno!\n");
			put("E2TEXT", "You've done it! The hideous cyber-\ndemon lord that ruled the lost Deimos\nmoon base has been slain and you\nare triumphant! But ... where are\nyou? You clamber to the edge of the\nmoon and look down to see the awful\ntruth.\n\nDeimos floats above Hell itself!\nYou've never heard of anyone escaping\nfrom Hell, but you'll make the bastards\nsorry they ever heard of you! Quickly,\nyou rappel down to  the surface of\nHell.\n\nNow, it's on to the final chapter of\nDOOM! -- Inferno.");
			put("E3TEXT", "The loathsome spiderdemon that\nmasterminded the invasion of the moon\nbases and caused so much death has had\nits ass kicked for all time.\n\nA hidden doorway opens and you enter.\nYou've proven too tough for Hell to\ncontain, and now Hell at last plays\nfair -- for you emerge from the door\nto see the green fields of Earth!\nHome at last.\n\nYou wonder what's been happening on\nEarth while you were battling evil\nunleashed. It's good that no Hell-\nspawn could have come through that\ndoor with you ...");
			put("E4TEXT", "the spider mastermind must have sent forth\nits legions of hellspawn before your\nfinal confrontation with that terrible\nbeast from hell.  but you stepped forward\nand brought forth eternal damnation and\nsuffering upon the horde as a true hero\nwould in the face of something so evil.\n\nbesides, someone was gonna pay for what\nhappened to daisy, your pet rabbit.\n\nbut now, you see spread before you more\npotential pain and gibbitude as a nation\nof demons run amok among our cities.\n\nnext stop, hell on earth!");
			put("C1TEXT", "YOU HAVE ENTERED DEEPLY INTO THE INFESTED\nSTARPORT. BUT SOMETHING IS WRONG. THE\nMONSTERS HAVE BROUGHT THEIR OWN REALITY\nWITH THEM, AND THE STARPORT'S TECHNOLOGY\nIS BEING SUBVERTED BY THEIR PRESENCE.\n\nAHEAD, YOU SEE AN OUTPOST OF HELL, A\nFORTIFIED ZONE. IF YOU CAN GET PAST IT,\nYOU CAN PENETRATE INTO THE HAUNTED HEART\nOF THE STARBASE AND FIND THE CONTROLLING\nSWITCH WHICH HOLDS EARTH'S POPULATION\nHOSTAGE.");
			put("C2TEXT", "YOU HAVE WON! YOUR VICTORY HAS ENABLED\nHUMANKIND TO EVACUATE EARTH AND ESCAPE\nTHE NIGHTMARE.  NOW YOU ARE THE ONLY\nHUMAN LEFT ON THE FACE OF THE PLANET.\nCANNIBAL MUTATIONS, CARNIVOROUS ALIENS,\nAND EVIL SPIRITS ARE YOUR ONLY NEIGHBORS.\nYOU SIT BACK AND WAIT FOR DEATH, CONTENT\nTHAT YOU HAVE SAVED YOUR SPECIES.\n\nBUT THEN, EARTH CONTROL BEAMS DOWN A\nMESSAGE FROM SPACE: \"SENSORS HAVE LOCATED\nTHE SOURCE OF THE ALIEN INVASION. IF YOU\nGO THERE, YOU MAY BE ABLE TO BLOCK THEIR\nENTRY.  THE ALIEN BASE IS IN THE HEART OF\nYOUR OWN HOME CITY, NOT FAR FROM THE\nSTARPORT.\" SLOWLY AND PAINFULLY YOU GET\nUP AND RETURN TO THE FRAY.");
			put("C3TEXT", "YOU ARE AT THE CORRUPT HEART OF THE CITY,\nSURROUNDED BY THE CORPSES OF YOUR ENEMIES.\nYOU SEE NO WAY TO DESTROY THE CREATURES'\nENTRYWAY ON THIS SIDE, SO YOU CLENCH YOUR\nTEETH AND PLUNGE THROUGH IT.\n\nTHERE MUST BE A WAY TO CLOSE IT ON THE\nOTHER SIDE. WHAT DO YOU CARE IF YOU'VE\nGOT TO GO THROUGH HELL TO GET TO IT?");
			put("C4TEXT", "THE HORRENDOUS VISAGE OF THE BIGGEST\nDEMON YOU'VE EVER SEEN CRUMBLES BEFORE\nYOU, AFTER YOU PUMP YOUR ROCKETS INTO\nHIS EXPOSED BRAIN. THE MONSTER SHRIVELS\nUP AND DIES, ITS THRASHING LIMBS\nDEVASTATING UNTOLD MILES OF HELL'S\nSURFACE.\n\nYOU'VE DONE IT. THE INVASION IS OVER.\nEARTH IS SAVED. HELL IS A WRECK. YOU\nWONDER WHERE BAD FOLKS WILL GO WHEN THEY\nDIE, NOW. WIPING THE SWEAT FROM YOUR\nFOREHEAD YOU BEGIN THE LONG TREK BACK\nHOME. REBUILDING EARTH OUGHT TO BE A\nLOT MORE FUN THAN RUINING IT WAS.\n");
			put("C5TEXT", "CONGRATULATIONS, YOU'VE FOUND THE SECRET\nLEVEL! LOOKS LIKE IT'S BEEN BUILT BY\nHUMANS, RATHER THAN DEMONS. YOU WONDER\nWHO THE INMATES OF THIS CORNER OF HELL\nWILL BE.");
			put("C6TEXT", "CONGRATULATIONS, YOU'VE FOUND THE\nSUPER SECRET LEVEL!  YOU'D BETTER\nBLAZE THROUGH THIS ONE!\n");
			put("P1TEXT", "You gloat over the steaming carcass of the\nGuardian.  With its death, you've wrested\nthe Accelerator from the stinking claws\nof Hell.  You relax and glance around the\nroom.  Damn!  There was supposed to be at\nleast one working prototype, but you can't\nsee it. The demons must have taken it.\n\nYou must find the prototype, or all your\nstruggles will have been wasted. Keep\nmoving, keep fighting, keep killing.\nOh yes, keep living, too.");
			put("P2TEXT", "Even the deadly Arch-Vile labyrinth could\nnot stop you, and you've gotten to the\nprototype Accelerator which is soon\nefficiently and permanently deactivated.\n\nYou're good at that kind of thing.");
			put("P3TEXT", "You've bashed and battered your way into\nthe heart of the devil-hive.  Time for a\nSearch-and-Destroy mission, aimed at the\nGatekeeper, whose foul offspring is\ncascading to Earth.  Yeah, he's bad. But\nyou know who's worse!\n\nGrinning evilly, you check your gear, and\nget ready to give the bastard a little Hell\nof your own making!");
			put("P4TEXT", "The Gatekeeper's evil face is splattered\nall over the place.  As its tattered corpse\ncollapses, an inverted Gate forms and\nsucks down the shards of the last\nprototype Accelerator, not to mention the\nfew remaining demons.  You're done. Hell\nhas gone back to pounding bad dead folks \ninstead of good live ones.  Remember to\ntell your grandkids to put a rocket\nlauncher in your coffin. If you go to Hell\nwhen you die, you'll need it for some\nfinal cleaning-up ...");
			put("P5TEXT", "You've found the second-hardest level we\ngot. Hope you have a saved game a level or\ntwo previous.  If not, be prepared to die\naplenty. For master marines only.");
			put("P6TEXT", "Betcha wondered just what WAS the hardest\nlevel we had ready for ya?  Now you know.\nNo one gets out alive.");
			put("T1TEXT", "You've fought your way out of the infested\nexperimental labs.   It seems that UAC has\nonce again gulped it down.  With their\nhigh turnover, it must be hard for poor\nold UAC to buy corporate health insurance\nnowadays..\n\nAhead lies the military complex, now\nswarming with diseased horrors hot to get\ntheir teeth into you. With luck, the\ncomplex still has some warlike ordnance\nlaying around.");
			put("T2TEXT", "You hear the grinding of heavy machinery\nahead.  You sure hope they're not stamping\nout new hellspawn, but you're ready to\nream out a whole herd if you have to.\nThey might be planning a blood feast, but\nyou feel about as mean as two thousand\nmaniacs packed into one mad killer.\n\nYou don't plan to go down easy.");
			put("T3TEXT", "The vista opening ahead looks real damn\nfamiliar. Smells familiar, too -- like\nfried excrement. You didn't like this\nplace before, and you sure as hell ain't\nplanning to like it now. The more you\nbrood on it, the madder you get.\nHefting your gun, an evil grin trickles\nonto your face. Time to take some names.");
			put("T4TEXT", "Suddenly, all is silent, from one horizon\nto the other. The agonizing echo of Hell\nfades away, the nightmare sky turns to\nblue, the heaps of monster corpses start \nto evaporate along with the evil stench \nthat filled the air. Jeeze, maybe you've\ndone it. Have you really won?\n\nSomething rumbles in the distance.\nA blue light begins to glow inside the\nruined skull of the demon-spitter.");
			put("T5TEXT", "What now? Looks totally different. Kind\nof like King Tut's condo. Well,\nwhatever's here can't be any worse\nthan usual. Can it?  Or maybe it's best\nto let sleeping gods lie..");
			put("T6TEXT", "Time for a vacation. You've burst the\nbowels of hell and by golly you're ready\nfor a break. You mutter to yourself,\nMaybe someone else can kick Hell's ass\nnext time around. Ahead lies a quiet town,\nwith peaceful flowing water, quaint\nbuildings, and presumably no Hellspawn.\n\nAs you step off the transport, you hear\nthe stomp of a cyberdemon's iron shoe.");
			put("CC_ZOMBIE", "ZOMBIEMAN");
			put("CC_SHOTGUN", "SHOTGUN GUY");
			put("CC_HEAVY", "HEAVY WEAPON DUDE");
			put("CC_IMP", "IMP");
			put("CC_DEMON", "DEMON");
			put("CC_LOST", "LOST SOUL");
			put("CC_CACO", "CACODEMON");
			put("CC_HELL", "HELL KNIGHT");
			put("CC_BARON", "BARON OF HELL");
			put("CC_ARACH", "ARACHNOTRON");
			put("CC_PAIN", "PAIN ELEMENTAL");
			put("CC_REVEN", "REVENANT");
			put("CC_MANCU", "MANCUBUS");
			put("CC_ARCH", "ARCH-VILE");
			put("CC_SPIDER", "THE SPIDER MASTERMIND");
			put("CC_CYBER", "THE CYBERDEMON");
			put("CC_HERO", "OUR HERO");
			put("BGFLATE1", "FLOOR4_8");
			put("BGFLATE2", "SFLR6_1");
			put("BGFLATE3", "MFLR8_4");
			put("BGFLATE4", "MFLR8_3");
			put("BGFLAT06", "SLIME16");
			put("BGFLAT11", "RROCK14");
			put("BGFLAT20", "RROCK07");
			put("BGFLAT30", "RROCK17");
			put("BGFLAT15", "RROCK13");
			put("BGFLAT31", "RROCK19");
			put("BGCASTCALL", "BOSSBACK");
			put("STARTUP1", "");
			put("STARTUP2", "");
			put("STARTUP3", "");
			put("STARTUP4", "");
			put("STARTUP5", "");	
		}
	};
	
	public static class State
	{
		private DEHState state;
		private DEHActionPointer pointer;
		
		private State()
		{
			this.state = null;
			this.pointer = null;
		}
		
		public static State create(DEHState state, DEHActionPointer pointer)
		{
			State out = new State();
			out.state = state;
			out.pointer = pointer;
			return out;
		}
		
		public DEHState getState() 
		{
			return state;
		}
		
		public DEHActionPointer getPointer() 
		{
			return pointer;
		}
		
	}

	// ======================================================================
	
	@Override
	public DEHMiscellany getMiscellany() 
	{
		return DEHMISC;
	}

	@Override
	public int getAmmoCount() 
	{
		return DEHAMMO.length;
	}

	@Override
	public DEHAmmo getAmmo(int index) 
	{
		return DEHAMMO[index];
	}

	@Override
	public String getString(String key)
	{
		return MAP_STRINGS.get(key);
	}

	@Override
	public Set<String> getStringKeys()
	{
		return MAP_STRINGS.keySet();
	}

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
		return DEHSOUND.length;
	}

	@Override
	public DEHSound getSound(int index)
	{
		return DEHSOUND[index];
	}

	@Override
	public int getThingCount() 
	{
		return DEHTHING.length + DEHTHINGBOOM.length;
	}

	@Override
	public DEHThing getThing(int index)
	{
		if (index >= DEHTHING.length)
			return DEHTHINGBOOM[index - DEHTHING.length];
		else
			return DEHTHING[index];
	}

	@Override
	public int getWeaponCount()
	{
		return DEHWEAPON.length;
	}

	@Override
	public DEHWeapon getWeapon(int index)
	{
		return DEHWEAPON[index];
	}

	@Override
	public int getStateCount()
	{
		return DEHSTATE.length;
	}

	@Override
	public DEHState getState(int index) 
	{
		return DEHSTATE[index].getState();
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
		return DEHSTATE[index].getPointer();
	}

	@Override
	public Set<EpisodeMap> getParEntries()
	{
		return MAP_PARS.keySet();
	}

	@Override
	public Integer getParSeconds(EpisodeMap episodeMap) 
	{
		return MAP_PARS.get(episodeMap);
	}

}

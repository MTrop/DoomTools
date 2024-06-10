/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.patches;

import static net.mtrop.doom.tools.decohack.patches.Constants.*;
import static net.mtrop.doom.tools.decohack.patches.ConstantsBoom.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.mtrop.doom.tools.decohack.data.DEHActionPointer;
import net.mtrop.doom.tools.decohack.data.DEHAmmo;
import net.mtrop.doom.tools.decohack.data.DEHMiscellany;
import net.mtrop.doom.tools.decohack.data.DEHSound;
import net.mtrop.doom.tools.decohack.data.DEHState;
import net.mtrop.doom.tools.decohack.data.DEHThing;
import net.mtrop.doom.tools.decohack.data.DEHWeapon;
import net.mtrop.doom.tools.struct.util.ArrayUtils;

/**
 * Patch implementation for Doom 1.9.
 * @author Matthew Tropiano
 */
public class PatchBoom implements DEHPatchBoom
{
	protected static final String[] SOUNDSTRINGS = 
	{
		null, // None
		"PISTOL",
		"SHOTGN",
		"SGCOCK",
		"DSHTGN",
		"DBOPN",
		"DBCLS",
		"DBLOAD",
		"PLASMA",
		"BFG",
		"SAWUP",
		"SAWIDL",
		"SAWFUL",
		"SAWHIT",
		"RLAUNC",
		"RXPLOD",
		"FIRSHT",
		"FIRXPL",
		"PSTART",
		"PSTOP",
		"DOROPN",
		"DORCLS",
		"STNMOV",
		"SWTCHN",
		"SWTCHX",
		"PLPAIN",
		"DMPAIN",
		"POPAIN",
		"VIPAIN",
		"MNPAIN",
		"PEPAIN",
		"SLOP",
		"ITEMUP",
		"WPNUP",
		"OOF",
		"TELEPT",
		"POSIT1",
		"POSIT2",
		"POSIT3",
		"BGSIT1",
		"BGSIT2",
		"SGTSIT",
		"CACSIT",
		"BRSSIT",
		"CYBSIT",
		"SPISIT",
		"BSPSIT",
		"KNTSIT",
		"VILSIT",
		"MANSIT",
		"PESIT",
		"SKLATK",
		"SGTATK",
		"SKEPCH",
		"VILATK",
		"CLAW",
		"SKESWG",
		"PLDETH",
		"PDIEHI",
		"PODTH1",
		"PODTH2",
		"PODTH3",
		"BGDTH1",
		"BGDTH2",
		"SGTDTH",
		"CACDTH",
		"SKLDTH",
		"BRSDTH",
		"CYBDTH",
		"SPIDTH",
		"BSPDTH",
		"VILDTH",
		"KNTDTH",
		"PEDTH",
		"SKEDTH",
		"POSACT",
		"BGACT",
		"DMACT",
		"BSPACT",
		"BSPWLK",
		"VILACT",
		"NOWAY",
		"BAREXP",
		"PUNCH",
		"HOOF",
		"METAL",
		"CHGUN",
		"TINK",
		"BDOPN",
		"BDCLS",
		"ITMBK",
		"FLAME",
		"FLAMST",
		"GETPOW",
		"BOSPIT",
		"BOSCUB",
		"BOSSIT",
		"BOSPN",
		"BOSDTH",
		"MANATK",
		"MANDTH",
		"SSSIT",
		"SSDTH",
		"KEENPN",
		"KEENDT",
		"SKEACT",
		"SKESIT",
		"SKEATK",
		"RADIO",
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
	
	protected static final int SPRITE_INDEX_TNT1 = 138;

	private static final Map<String, Integer> MAP_SOUNDINDEX = new HashMap<String, Integer>()
	{
		private static final long serialVersionUID = -4513058612574767102L;
		{
			for (int i = 1; i < SOUNDSTRINGS.length; i++)
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
	
	private static final Set<String> SET_STRINGS = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER)
	{
		private static final long serialVersionUID = -419155824507194333L;
		{
			add("AMSTR_FOLLOWOFF");
			add("AMSTR_FOLLOWON");
			add("AMSTR_GRIDOFF");
			add("AMSTR_GRIDON");
			add("AMSTR_MARKEDSPOT");
			add("AMSTR_MARKSCLEARED");
			add("BGCASTCALL");
			add("BGFLAT06");
			add("BGFLAT11");
			add("BGFLAT15");
			add("BGFLAT20");
			add("BGFLAT30");
			add("BGFLAT31");
			add("BGFLATE1");
			add("BGFLATE2");
			add("BGFLATE3");
			add("BGFLATE4");
			add("C1TEXT");
			add("C2TEXT");
			add("C3TEXT");
			add("C4TEXT");
			add("C5TEXT");
			add("C6TEXT");
			add("CC_ARACH");
			add("CC_ARCH");
			add("CC_BARON");
			add("CC_CACO");
			add("CC_CYBER");
			add("CC_DEMON");
			add("CC_HEAVY");
			add("CC_HELL");
			add("CC_HERO");
			add("CC_IMP");
			add("CC_LOST");
			add("CC_MANCU");
			add("CC_PAIN");
			add("CC_REVEN");
			add("CC_SHOTGUN");
			add("CC_SPIDER");
			add("CC_ZOMBIE");
			add("D_CDROM");
			add("D_DEVSTR");
			add("DETAILHI");
			add("DETAILLO");
			add("E1TEXT");
			add("E2TEXT");
			add("E3TEXT");
			add("E4TEXT");
			add("EMPTYSTRING");
			add("ENDGAME");
			add("GAMMALVL0");
			add("GAMMALVL1");
			add("GAMMALVL2");
			add("GAMMALVL3");
			add("GAMMALVL4");
			add("GGSAVED");
			add("GOTARMBONUS");
			add("GOTARMOR");
			add("GOTBACKPACK");
			add("GOTBERSERK");
			add("GOTBFG9000");
			add("GOTBLUECARD");
			add("GOTBLUESKUL");
			add("GOTCELL");
			add("GOTCELLBOX");
			add("GOTCHAINGUN");
			add("GOTCHAINSAW");
			add("GOTCLIP");
			add("GOTCLIPBOX");
			add("GOTHTHBONUS");
			add("GOTINVIS");
			add("GOTINVUL");
			add("GOTLAUNCHER");
			add("GOTMAP");
			add("GOTMEDIKIT");
			add("GOTMEDINEED");
			add("GOTMEGA");
			add("GOTMSPHERE");
			add("GOTPLASMA");
			add("GOTREDCARD");
			add("GOTREDSKULL");
			add("GOTROCKBOX");
			add("GOTROCKET");
			add("GOTSHELLBOX");
			add("GOTSHELLS");
			add("GOTSHOTGUN");
			add("GOTSHOTGUN2");
			add("GOTSTIM");
			add("GOTSUIT");
			add("GOTSUPER");
			add("GOTVISOR");
			add("GOTYELWCARD");
			add("GOTYELWSKUL");
			add("HUSTR_1");
			add("HUSTR_10");
			add("HUSTR_11");
			add("HUSTR_12");
			add("HUSTR_13");
			add("HUSTR_14");
			add("HUSTR_15");
			add("HUSTR_16");
			add("HUSTR_17");
			add("HUSTR_18");
			add("HUSTR_19");
			add("HUSTR_2");
			add("HUSTR_20");
			add("HUSTR_21");
			add("HUSTR_22");
			add("HUSTR_23");
			add("HUSTR_24");
			add("HUSTR_25");
			add("HUSTR_26");
			add("HUSTR_27");
			add("HUSTR_28");
			add("HUSTR_29");
			add("HUSTR_3");
			add("HUSTR_30");
			add("HUSTR_31");
			add("HUSTR_32");
			add("HUSTR_4");
			add("HUSTR_5");
			add("HUSTR_6");
			add("HUSTR_7");
			add("HUSTR_8");
			add("HUSTR_9");
			add("HUSTR_CHATMACRO0");
			add("HUSTR_CHATMACRO1");
			add("HUSTR_CHATMACRO2");
			add("HUSTR_CHATMACRO3");
			add("HUSTR_CHATMACRO4");
			add("HUSTR_CHATMACRO5");
			add("HUSTR_CHATMACRO6");
			add("HUSTR_CHATMACRO7");
			add("HUSTR_CHATMACRO8");
			add("HUSTR_CHATMACRO9");
			add("HUSTR_E1M1");
			add("HUSTR_E1M2");
			add("HUSTR_E1M3");
			add("HUSTR_E1M4");
			add("HUSTR_E1M5");
			add("HUSTR_E1M6");
			add("HUSTR_E1M7");
			add("HUSTR_E1M8");
			add("HUSTR_E1M9");
			add("HUSTR_E2M1");
			add("HUSTR_E2M2");
			add("HUSTR_E2M3");
			add("HUSTR_E2M4");
			add("HUSTR_E2M5");
			add("HUSTR_E2M6");
			add("HUSTR_E2M7");
			add("HUSTR_E2M8");
			add("HUSTR_E2M9");
			add("HUSTR_E3M1");
			add("HUSTR_E3M2");
			add("HUSTR_E3M3");
			add("HUSTR_E3M4");
			add("HUSTR_E3M5");
			add("HUSTR_E3M6");
			add("HUSTR_E3M7");
			add("HUSTR_E3M8");
			add("HUSTR_E3M9");
			add("HUSTR_E4M1");
			add("HUSTR_E4M2");
			add("HUSTR_E4M3");
			add("HUSTR_E4M4");
			add("HUSTR_E4M5");
			add("HUSTR_E4M6");
			add("HUSTR_E4M7");
			add("HUSTR_E4M8");
			add("HUSTR_E4M9");
			add("HUSTR_MESSAGESENT");
			add("HUSTR_MSGU");
			add("HUSTR_PLRBROWN");
			add("HUSTR_PLRGREEN");
			add("HUSTR_PLRINDIGO");
			add("HUSTR_PLRRED");
			add("HUSTR_TALKTOSELF1");
			add("HUSTR_TALKTOSELF2");
			add("HUSTR_TALKTOSELF3");
			add("HUSTR_TALKTOSELF4");
			add("HUSTR_TALKTOSELF5");
			add("LOADNET");
			add("MSGOFF");
			add("MSGON");
			add("NETEND");
			add("NEWGAME");
			add("NIGHTMARE");
			add("P1TEXT");
			add("P2TEXT");
			add("P3TEXT");
			add("P4TEXT");
			add("P5TEXT");
			add("P6TEXT");
			add("PD_ALL3");
			add("PD_ALL6");
			add("PD_ANY");
			add("PD_BLUEC");
			add("PD_BLUEK");
			add("PD_BLUEO");
			add("PD_BLUES");
			add("PD_REDC");
			add("PD_REDK");
			add("PD_REDO");
			add("PD_REDS");
			add("PD_YELLOWC");
			add("PD_YELLOWK");
			add("PD_YELLOWO");
			add("PD_YELLOWS");
			add("PHUSTR_1");
			add("PHUSTR_10");
			add("PHUSTR_11");
			add("PHUSTR_12");
			add("PHUSTR_13");
			add("PHUSTR_14");
			add("PHUSTR_15");
			add("PHUSTR_16");
			add("PHUSTR_17");
			add("PHUSTR_18");
			add("PHUSTR_19");
			add("PHUSTR_2");
			add("PHUSTR_20");
			add("PHUSTR_21");
			add("PHUSTR_22");
			add("PHUSTR_23");
			add("PHUSTR_24");
			add("PHUSTR_25");
			add("PHUSTR_26");
			add("PHUSTR_27");
			add("PHUSTR_28");
			add("PHUSTR_29");
			add("PHUSTR_3");
			add("PHUSTR_30");
			add("PHUSTR_31");
			add("PHUSTR_32");
			add("PHUSTR_4");
			add("PHUSTR_5");
			add("PHUSTR_6");
			add("PHUSTR_7");
			add("PHUSTR_8");
			add("PHUSTR_9");
			add("QLOADNET");
			add("QLPROMPT");
			add("QSAVESPOT");
			add("QSPROMPT");
			add("QUITMSG");
			add("SAVEDEAD");
			add("SAVEGAMENAME");
			add("STARTUP1");
			add("STARTUP2");
			add("STARTUP3");
			add("STARTUP4");
			add("STARTUP5");
			add("STSTR_BEHOLD");
			add("STSTR_BEHOLDX");
			add("STSTR_CHOPPERS");
			add("STSTR_CLEV");
			add("STSTR_COMPOFF");
			add("STSTR_COMPON");
			add("STSTR_DQDOFF");
			add("STSTR_DQDON");
			add("STSTR_FAADDED");
			add("STSTR_KFAADDED");
			add("STSTR_MUS");
			add("STSTR_NCOFF");
			add("STSTR_NCON");
			add("STSTR_NOMUS");
			add("SWSTRING");
			add("T1TEXT");
			add("T2TEXT");
			add("T3TEXT");
			add("T4TEXT");
			add("T5TEXT");
			add("T6TEXT");
			add("THUSTR_1");
			add("THUSTR_10");
			add("THUSTR_11");
			add("THUSTR_12");
			add("THUSTR_13");
			add("THUSTR_14");
			add("THUSTR_15");
			add("THUSTR_16");
			add("THUSTR_17");
			add("THUSTR_18");
			add("THUSTR_19");
			add("THUSTR_2");
			add("THUSTR_20");
			add("THUSTR_21");
			add("THUSTR_22");
			add("THUSTR_23");
			add("THUSTR_24");
			add("THUSTR_25");
			add("THUSTR_26");
			add("THUSTR_27");
			add("THUSTR_28");
			add("THUSTR_29");
			add("THUSTR_3");
			add("THUSTR_30");
			add("THUSTR_31");
			add("THUSTR_32");
			add("THUSTR_4");
			add("THUSTR_5");
			add("THUSTR_6");
			add("THUSTR_7");
			add("THUSTR_8");
			add("THUSTR_9");
			add("QUITMSG1");
			add("QUITMSG2");
			add("QUITMSG3");
			add("QUITMSG4");
			add("QUITMSG5");
			add("QUITMSG6");
			add("QUITMSG7");
			add("QUITMSG8");
			add("QUITMSG9");
			add("QUITMSG10");
			add("QUITMSG11");
			add("QUITMSG12");
			add("QUITMSG13");
			add("QUITMSG14");
			add("QUITMSG15");
			add("QUITMSG16");
			add("QUITMSG17");
			add("QUITMSG18");
			add("QUITMSG19");
			add("QUITMSG20");
			add("QUITMSG21");
			add("QUITMSG22");
			add("QUITMSG23");
			add("QUITMSG24");
			add("QUITMSG25");
			add("QUITMSG26");
			add("QUITMSG27");
			add("QUITMSG28");
			add("QUITMSG29");
			add("TXT_FRAGLIMIT");
			add("TXT_TIMELIMIT");
			add("SPREEKILLSELF");
			add("SPREEOVER");
			add("SPREE5");
			add("SPREE10");
			add("SPREE15");
			add("SPREE20");
			add("SPREE25");
			add("MULTI2");
			add("MULTI3");
			add("MULTI4");
			add("MULTI5");
			add("AM_MONSTERS");
			add("AM_SECRETS");
			add("AM_ITEMS");
			add("OB_SUICIDE");
			add("OB_FALLING");
			add("OB_CRUSH");
			add("OB_EXIT");
			add("OB_WATER");
			add("OB_SLIME");
			add("OB_LAVA");
			add("OB_BARREL");
			add("OB_SPLASH");
			add("OB_R_SPLASH");
			add("OB_ROCKET");
			add("OB_KILLEDSELF");
			add("OB_VOODOO");
			add("OB_MPTELEFRAG");
			add("OB_MONTELEFRAG");
			add("OB_DEFAULT");
			add("OB_MPDEFAULT");
			add("OB_FRIENDLY1");
			add("OB_FRIENDLY2");
			add("OB_FRIENDLY3");
			add("OB_FRIENDLY4");
			add("OB_STEALTHBABY");
			add("OB_STEALTHVILE");
			add("OB_STEALTHBARON");
			add("OB_STEALTHCACO");
			add("OB_STEALTHCHAINGUY");
			add("OB_STEALTHDEMON");
			add("OB_STEALTHKNIGHT");
			add("OB_STEALTHIMP");
			add("OB_STEALTHFATSO");
			add("OB_STEALTHUNDEAD");
			add("OB_STEALTHSHOTGUNGUY");
			add("OB_STEALTHZOMBIE");
			add("OB_UNDEADHIT");
			add("OB_IMPHIT");
			add("OB_CACOHIT");
			add("OB_DEMONHIT");
			add("OB_SPECTREHIT");
			add("OB_BARONHIT");
			add("OB_KNIGHTHIT");
			add("OB_ZOMBIE");
			add("OB_SHOTGUY");
			add("OB_VILE");
			add("OB_UNDEAD");
			add("OB_FATSO");
			add("OB_CHAINGUY");
			add("OB_SKULL");
			add("OB_IMP");
			add("OB_CACO");
			add("OB_BARON");
			add("OB_KNIGHT");
			add("OB_SPIDER");
			add("OB_BABY");
			add("OB_CYBORG");
			add("OB_WOLFSS");
			add("OB_DOG");
			add("OB_MPFIST");
			add("OB_MPCHAINSAW");
			add("OB_MPPISTOL");
			add("OB_MPSHOTGUN");
			add("OB_MPSSHOTGUN");
			add("OB_MPCHAINGUN");
			add("OB_MPROCKET");
			add("OB_MPR_SPLASH");
			add("OB_MPPLASMARIFLE");
			add("OB_MPBFG_BOOM");
			add("OB_MPBFG_SPLASH");
			add("OB_RAILGUN");
			add("OB_MPBFG_MBF");
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
		return ArrayUtils.arrayElement(DEHAMMO, index);
	}

	@Override
	public String getString(String key)
	{
		return SET_STRINGS.contains(key) ? "" : null;
	}

	@Override
	public Set<String> getStringKeys()
	{
		return SET_STRINGS;
	}

	@Override
	public boolean isValidStringKey(String key)
	{
		return SET_STRINGS.contains(key);
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
		return ArrayUtils.arrayElement(DEHSOUND, index);
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
			return ArrayUtils.arrayElement(DEHTHINGBOOM, index - DEHTHING.length);
		else
			return ArrayUtils.arrayElement(DEHTHING, index);
	}

	@Override
	public int getWeaponCount()
	{
		return DEHWEAPON.length;
	}

	@Override
	public DEHWeapon getWeapon(int index)
	{
		return ArrayUtils.arrayElement(DEHWEAPON, index);
	}

	@Override
	public int getStateCount()
	{
		return DEHSTATE.length;
	}

	protected PatchBoom.State getBoomState(int index)
	{
		return ArrayUtils.arrayElement(DEHSTATE, index);
	}

	@Override
	public DEHState getState(int index) 
	{
		State state = getBoomState(index);
		return state != null ? state.getState() : null;
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
		State state = getBoomState(index);
		return state != null ? state.getPointer() : null;
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

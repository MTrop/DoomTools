/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

import java.util.Map;
import java.util.TreeMap;

import net.mtrop.doom.tools.decohack.data.DEHActionPointerType;

/**
 * Enumeration of action pointers for frames.
 * @author Matthew Tropiano
 */
public enum DEHActionPointer
{
	NULL            (0,   "NULL"),
	LIGHT0          (1,   "Light0"),
	WEAPONREADY     (2,   "WeaponReady"),
	LOWER           (3,   "Lower"),
	RAISE           (4,   "Raise"),
	PUNCH           (6,   "Punch"),
	REFIRE          (9,   "ReFire"),
	FIREPISTOL      (14,  "FirePistol"),
	LIGHT1          (17,  "Light1"),
	FIRESHOTGUN     (22,  "FireShotgun"),
	LIGHT2          (31,  "Light2"),
	FIRESHOTGUN2    (36,  "FireShotgun2"),
	CHECKRELOAD     (38,  "CheckReload"),
	OPENSHOTGUN2    (39,  "OpenShotgun2"),
	LOADSHOTGUN2    (41,  "LoadShotgun2"),
	CLOSESHOTGUN2   (43,  "CloseShotgun2"),
	FIRECGUN        (52,  "FireCGun"),
	GUNFLASH        (60,  "GunFlash"),
	FIREMISSILE     (61,  "FireMissile"),
	SAW             (71,  "Saw"),
	FIREPLASMA      (77,  "FirePlasma"),
	BFGSOUND        (84,  "BFGsound"),
	FIREBFG         (86,  "FireBFG"),
	BFGSPRAY        (119, "BFGSpray"),
	EXPLODE         (127, "Explode"),
	PAIN            (157, "Pain"),
	PLAYERSCREAM    (159, "PlayerScream"),
	FALL            (160, "Fall"),
	XSCREAM         (166, "XScream"),
	LOOK            (174, "Look"),
	CHASE           (176, "Chase"),
	FACETARGET      (184, "FaceTarget"),
	POSATTACK       (185, "PosAttack"),
	SCREAM          (190, "Scream"),
	VILECHASE       (243, "VileChase"),
	VILESTART       (255, "VileStart"),
	VILETARGET      (257, "VileTarget"),
	VILEATTACK      (264, "VileAttack"),
	STARTFIRE       (281, "StartFire"),
	FIRE            (282, "Fire"),
	FIRECRACKLE     (285, "FireCrackle"),
	TRACER          (316, "Tracer"),
	SKELWHOOSH      (336, "SkelWhoosh"),
	SKELFIST        (338, "SkelFist"),
	SKELMISSILE     (341, "SkelMissile"),
	FATRAISE        (376, "FatRaise"),
	FATATTACK1      (377, "FatAttack1"),
	FATATTACK2      (380, "FatAttack2"),
	FATATTACK3      (383, "FatAttack3"),
	BOSSDEATH       (397, "BossDeath"),
	CPOSATTACK      (417, "CPosAttack"),
	CPOSREFIRE      (419, "CPosRefire"),
	TROOPATTACK     (454, "TroopAttack"),
	SARGATTACK      (487, "SargAttack"),
	HEADATTACK      (506, "HeadAttack"),
	BRUISATTACK     (539, "BruisAttack"),
	SKULLATTACK     (590, "SkullAttack"),
	METAL           (603, "Metal"),
	SPOSATTACK      (616, "SPosAttack"),
	SPIDREFIRE      (618, "SpidRefire"),
	BABYMETAL       (635, "BabyMetal"),
	BSPIATTACK      (648, "BspiAttack"),
	HOOF            (676, "Hoof"),
	CYBERATTACK     (685, "CyberAttack"),
	PAINATTACK      (711, "PainAttack"),
	PAINDIE         (718, "PainDie"),
	KEENDIE         (774, "KeenDie"),
	BRAINPAIN       (779, "BrainPain"),
	BRAINSCREAM     (780, "BrainScream"),
	BRAINDIE        (783, "BrainDie"),
	BRAINAWAKE      (785, "BrainAwake"),
	BRAINSPIT       (786, "BrainSpit"),
	SPAWNSOUND      (787, "SpawnSound"),
	SPAWNFLY        (788, "SpawnFly"),
	BRAINEXPLODE    (801, "BrainExplode"),
	
	// MBF Action Pointers
	
	DETONATE        (-1, "Detonate", DEHActionPointerType.MBF),
	MUSHROOM        (-1, "Mushroom", DEHActionPointerType.MBF, (-360 << 16) + 1, (360 << 16) - 1, Integer.MIN_VALUE, Integer.MAX_VALUE), // fixed point on both
	SPAWN           (-1, "Spawn", DEHActionPointerType.MBF, 0, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE),
	TURN            (-1, "Turn", DEHActionPointerType.MBF, -359, 359),
	FACE            (-1, "Face", DEHActionPointerType.MBF, 0, 359),
	SCRATCH         (-1, "Scratch", DEHActionPointerType.MBF, -32767, 32767, 0, Integer.MAX_VALUE),
	PLAYSOUND       (-1, "PlaySound", DEHActionPointerType.MBF, 0, Integer.MAX_VALUE, 0, 1),
	RANDOMJUMP      (-1, "RandomJump", DEHActionPointerType.MBF, 0, Integer.MAX_VALUE, 0, 255),
	LINEEFFECT      (-1, "LineEffect", DEHActionPointerType.MBF, -32767, 32767, -32767, 32767),
	DIE             (-1, "Die", DEHActionPointerType.MBF),
	FIREOLDBFG      (-1, "FireOldBFG", DEHActionPointerType.MBF),
	BETASKULLATTACK (-1, "BetaSkullAttack", DEHActionPointerType.MBF),
	STOP            (-1, "Stop", DEHActionPointerType.MBF),
	;
	
	/** Originating frame (for DEH 3.0 format 19). */
	private int frame;
	/** Mnemonic name for BEX/DECORATE. */
	private String mnemonic;
	/** Action pointer type. */
	private DEHActionPointerType type;
	
	private int param0min;
	private int param0max;
	private int param1min;
	private int param1max;

	private static Map<String, DEHActionPointer> MNEMONIC_MAP = null;

	public static DEHActionPointer getByMnemonic(String mnemonic)
	{
		if (MNEMONIC_MAP == null)
		{
			MNEMONIC_MAP = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			for (DEHActionPointer ap : values())
				MNEMONIC_MAP.put(ap.getMnemonic(), ap);
		}
		return MNEMONIC_MAP.get(mnemonic);
	}
	
	private DEHActionPointer(int frame, String mnemonic)
	{
		this(frame, mnemonic, DEHActionPointerType.DOOM19, 0, 0, 0, 0);
	}

	private DEHActionPointer(int frame, String mnemonic, DEHActionPointerType type)
	{
		this(frame, mnemonic, type, 0, 0, 0, 0);
	}

	private DEHActionPointer(int frame, String mnemonic, DEHActionPointerType type, int param0min, int param0max)
	{
		this(frame, mnemonic, type, param0min, param0max, 0, 0);
	}

	private DEHActionPointer(int frame, String mnemonic, DEHActionPointerType type, int param0min, int param0max, int param1min, int param1max)
	{
		this.frame = frame;
		this.mnemonic = mnemonic;
		this.type = type;
		this.param0min = param0min;
		this.param0max = param0max;
		this.param1min = param1min;
		this.param1max = param1max;
	}

	public int getFrame() 
	{
		return frame;
	}
	
	public String getMnemonic() 
	{
		return mnemonic;
	}
	
	public DEHActionPointerType getType() 
	{
		return type;
	}
	
	public int getParam0min()
	{
		return param0min;
	}
	
	public int getParam0max() 
	{
		return param0max;
	}
	
	public int getParam1min()
	{
		return param1min;
	}
	
	public int getParam1max() 
	{
		return param1max;
	}
	
}

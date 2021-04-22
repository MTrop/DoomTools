/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

import java.util.Map;
import java.util.TreeMap;

import net.mtrop.doom.tools.decohack.data.DEHActionPointerParam;
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
	MUSHROOM        (-1, "Mushroom", DEHActionPointerType.MBF, DEHActionPointerParam.ANGLE_FIXED, DEHActionPointerParam.INT), // fixed point on both
	SPAWN           (-1, "Spawn", DEHActionPointerType.MBF, DEHActionPointerParam.UINT, DEHActionPointerParam.INT),
	TURN            (-1, "Turn", DEHActionPointerType.MBF, DEHActionPointerParam.ANGLE_INT),
	FACE            (-1, "Face", DEHActionPointerType.MBF, DEHActionPointerParam.ANGLE_UINT),
	SCRATCH         (-1, "Scratch", DEHActionPointerType.MBF, DEHActionPointerParam.SHORT, DEHActionPointerParam.UINT),
	PLAYSOUND       (-1, "PlaySound", DEHActionPointerType.MBF, DEHActionPointerParam.UINT, DEHActionPointerParam.BOOL),
	RANDOMJUMP      (-1, "RandomJump", DEHActionPointerType.MBF, DEHActionPointerParam.UINT, DEHActionPointerParam.BYTE),
	LINEEFFECT      (-1, "LineEffect", DEHActionPointerType.MBF, DEHActionPointerParam.SHORT, DEHActionPointerParam.SHORT),
	DIE             (-1, "Die", DEHActionPointerType.MBF),
	FIREOLDBFG      (-1, "FireOldBFG", DEHActionPointerType.MBF),
	BETASKULLATTACK (-1, "BetaSkullAttack", DEHActionPointerType.MBF),
	STOP            (-1, "Stop", DEHActionPointerType.MBF),

	// MBF21 Action Pointers

	SPAWNFACING         (-1, "SpawnFacing", DEHActionPointerType.MBF21, DEHActionPointerParam.UINT, DEHActionPointerParam.INT),
	MONSTERPROJECTILE   (-1, "MonsterProjectile", DEHActionPointerType.MBF21, DEHActionPointerParam.UINT, DEHActionPointerParam.ANGLE_FIXED),
	MONSTERBULLETATTACK (-1, "MonsterBulletAttack", DEHActionPointerType.MBF21, DEHActionPointerParam.SHORT, DEHActionPointerParam.ANGLE_FIXED),
	RADIUSDAMAGE        (-1, "RadiusDamage", DEHActionPointerType.MBF21, DEHActionPointerParam.UINT, DEHActionPointerParam.UINT),
	WEAPONPROJECTILE    (-1, "WeaponProjectile", DEHActionPointerType.MBF21, DEHActionPointerParam.UINT, DEHActionPointerParam.ANGLE_FIXED),
	WEAPONBULLETATTACK  (-1, "WeaponBulletAttack", DEHActionPointerType.MBF21, DEHActionPointerParam.SHORT, DEHActionPointerParam.ANGLE_FIXED),
	WEAPONSOUND         (-1, "WeaponSound", DEHActionPointerType.MBF21, DEHActionPointerParam.UINT, DEHActionPointerParam.BOOL),
	WEAPONJUMP          (-1, "WeaponJump", DEHActionPointerType.MBF21, DEHActionPointerParam.UINT, DEHActionPointerParam.BYTE),
	CONSUMEAMMO         (-1, "ConsumeAmmo", DEHActionPointerType.MBF21, DEHActionPointerParam.SHORT),
	CHECKAMMO           (-1, "CheckAmmo", DEHActionPointerType.MBF21, DEHActionPointerParam.UINT, DEHActionPointerParam.SHORT),
	REFIRETO            (-1, "RefireTo", DEHActionPointerType.MBF21, DEHActionPointerParam.UINT, DEHActionPointerParam.BOOL),
	GUNFLASHTO          (-1, "GunFlashTo", DEHActionPointerType.MBF21, DEHActionPointerParam.UINT, DEHActionPointerParam.BOOL),
	;
	
	/** Originating frame (for DEH 3.0 format 19). */
	private int frame;
	/** Mnemonic name for BEX/DECORATE. */
	private String mnemonic;
	/** Action pointer type. */
	private DEHActionPointerType type;
	
	private DEHActionPointerParam param0;
	private DEHActionPointerParam param1;

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
		this(frame, mnemonic, DEHActionPointerType.DOOM19, DEHActionPointerParam.NONE, DEHActionPointerParam.NONE);
	}

	private DEHActionPointer(int frame, String mnemonic, DEHActionPointerType type)
	{
		this(frame, mnemonic, type, DEHActionPointerParam.NONE, DEHActionPointerParam.NONE);
	}

	private DEHActionPointer(int frame, String mnemonic, DEHActionPointerType type, DEHActionPointerParam param0)
	{
		this(frame, mnemonic, type, param0, DEHActionPointerParam.NONE);
	}

	private DEHActionPointer(int frame, String mnemonic, DEHActionPointerType type, DEHActionPointerParam param0, DEHActionPointerParam param1)
	{
		this.frame = frame;
		this.mnemonic = mnemonic;
		this.type = type;
		this.param0 = param0;
		this.param1 = param1;
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
	
	public DEHActionPointerParam getParam0()
	{
		return param0;
	}

	public DEHActionPointerParam getParam1()
	{
		return param1;
	}
	
}

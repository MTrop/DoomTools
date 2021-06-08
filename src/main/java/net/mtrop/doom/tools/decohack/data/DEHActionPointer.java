/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

import java.util.Map;
import java.util.TreeMap;

/**
 * Enumeration of action pointers for frames.
 * TODO: Separate Weapon and Thing pointers - check for valid use on parser.
 * @author Matthew Tropiano
 */
public enum DEHActionPointer
{
	NULL            (0,   "NULL"),
	
	// Doom Weapon Action Pointers
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

	// Doom Thing Action Pointers
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
	
	// MBF Thing Action Pointers
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

	// MBF21 Thing Action Pointers
	SPAWNOBJECT         (-1, "SpawnObject", DEHActionPointerType.MBF21, DEHActionPointerParam.UINT, DEHActionPointerParam.ANGLE_FIXED, DEHActionPointerParam.INT, DEHActionPointerParam.INT, DEHActionPointerParam.INT, DEHActionPointerParam.INT, DEHActionPointerParam.INT, DEHActionPointerParam.INT),
	MONSTERPROJECTILE   (-1, "MonsterProjectile", DEHActionPointerType.MBF21, DEHActionPointerParam.UINT, DEHActionPointerParam.ANGLE_FIXED, DEHActionPointerParam.ANGLE_FIXED, DEHActionPointerParam.INT, DEHActionPointerParam.INT),
	MONSTERBULLETATTACK (-1, "MonsterBulletAttack", DEHActionPointerType.MBF21, DEHActionPointerParam.ANGLE_FIXED, DEHActionPointerParam.ANGLE_FIXED, DEHActionPointerParam.UINT, DEHActionPointerParam.SHORT, DEHActionPointerParam.UINT),
	MONSTERMELEEATTACK  (-1, "MonsterMeleeAttack", DEHActionPointerType.MBF21, DEHActionPointerParam.SHORT, DEHActionPointerParam.UINT, DEHActionPointerParam.UINT, DEHActionPointerParam.INT),
	RADIUSDAMAGE        (-1, "RadiusDamage", DEHActionPointerType.MBF21, DEHActionPointerParam.UINT, DEHActionPointerParam.UINT),
	NOISEALERT          (-1, "NoiseAlert", DEHActionPointerType.MBF21),
	HEALCHASE           (-1, "HealChase", DEHActionPointerType.MBF21, DEHActionPointerParam.UINT, DEHActionPointerParam.UINT),
	SEEKTRACER          (-1, "SeekTracer", DEHActionPointerType.MBF21, DEHActionPointerParam.UINT, DEHActionPointerParam.UINT),
	FINDTRACER          (-1, "FindTracer", DEHActionPointerType.MBF21, DEHActionPointerParam.ANGLE_FIXED, DEHActionPointerParam.UINT),
	CLEARTRACER         (-1, "ClearTracer", DEHActionPointerType.MBF21),
	JUMPIFHEALTHBELOW   (-1, "JumpIfHealthBelow", DEHActionPointerType.MBF21, DEHActionPointerParam.UINT, DEHActionPointerParam.UINT),
	JUMPIFTARGETINSIGHT (-1, "JumpIfTargetInSight", DEHActionPointerType.MBF21, DEHActionPointerParam.UINT, DEHActionPointerParam.ANGLE_FIXED),
	JUMPIFTARGETCLOSER  (-1, "JumpIfTargetCloser", DEHActionPointerType.MBF21, DEHActionPointerParam.UINT, DEHActionPointerParam.UINT),
	JUMPIFTRACERINSIGHT (-1, "JumpIfTracerInSight", DEHActionPointerType.MBF21, DEHActionPointerParam.UINT, DEHActionPointerParam.ANGLE_FIXED),
	JUMPIFTRACERCLOSER  (-1, "JumpIfTracerCloser", DEHActionPointerType.MBF21, DEHActionPointerParam.UINT, DEHActionPointerParam.UINT),
	JUMPIFFLAGSSET      (-1, "JumpIfFlagsSet", DEHActionPointerType.MBF21, DEHActionPointerParam.UINT, DEHActionPointerParam.UINT, DEHActionPointerParam.UINT),
	ADDFLAGS            (-1, "AddFlags", DEHActionPointerType.MBF21, DEHActionPointerParam.UINT, DEHActionPointerParam.UINT),
	REMOVEFLAGS         (-1, "RemoveFlags", DEHActionPointerType.MBF21, DEHActionPointerParam.UINT, DEHActionPointerParam.UINT),

	// MBF21 Weapon Action Pointers
	WEAPONPROJECTILE    (-1, "WeaponProjectile", DEHActionPointerType.MBF21, DEHActionPointerParam.UINT, DEHActionPointerParam.ANGLE_FIXED, DEHActionPointerParam.ANGLE_FIXED, DEHActionPointerParam.INT, DEHActionPointerParam.INT),
	WEAPONBULLETATTACK  (-1, "WeaponBulletAttack", DEHActionPointerType.MBF21, DEHActionPointerParam.ANGLE_FIXED, DEHActionPointerParam.ANGLE_FIXED, DEHActionPointerParam.UINT, DEHActionPointerParam.SHORT, DEHActionPointerParam.UINT),
	WEAPONMELEEATTACK   (-1, "WeaponMeleeAttack", DEHActionPointerType.MBF21, DEHActionPointerParam.SHORT, DEHActionPointerParam.UINT, DEHActionPointerParam.UINT, DEHActionPointerParam.UINT, DEHActionPointerParam.INT),
	WEAPONSOUND         (-1, "WeaponSound", DEHActionPointerType.MBF21, DEHActionPointerParam.UINT, DEHActionPointerParam.BOOL),
	WEAPONALERT         (-1, "WeaponAlert", DEHActionPointerType.MBF21),
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
	
	private DEHActionPointerParam[] params;

	private static final Map<String, DEHActionPointer> MNEMONIC_MAP = new TreeMap<String, DEHActionPointer>(String.CASE_INSENSITIVE_ORDER)
	{
		private static final long serialVersionUID = -7754048704695925418L;
		{
			for (DEHActionPointer val : DEHActionPointer.values())
				put(val.name(), val);
		}
	};

	public static DEHActionPointer getByMnemonic(String mnemonic)
	{
		return MNEMONIC_MAP.get(mnemonic);
	}
	
	private DEHActionPointer(int frame, String mnemonic)
	{
		this(frame, mnemonic, DEHActionPointerType.DOOM19, new DEHActionPointerParam[0]);
	}

	private DEHActionPointer(int frame, String mnemonic, DEHActionPointerType type)
	{
		this(frame, mnemonic, type, new DEHActionPointerParam[0]);
	}

	private DEHActionPointer(int frame, String mnemonic, DEHActionPointerType type, DEHActionPointerParam ... params)
	{
		this.frame = frame;
		this.mnemonic = mnemonic;
		this.type = type;
		this.params = params;
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
	
	public DEHActionPointerParam[] getParams()
	{
		return params;
	}

	public boolean useArgs()
	{
		return type != null && type.getUseArgs();
	}
	
}

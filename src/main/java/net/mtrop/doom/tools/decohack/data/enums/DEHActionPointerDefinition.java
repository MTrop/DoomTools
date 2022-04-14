/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data.enums;

import java.util.Map;

import net.mtrop.doom.tools.decohack.data.DEHActionPointer;
import net.mtrop.doom.tools.struct.util.EnumUtils;

/**
 * Enumeration of action pointers for frames.
 * NOTE: KEEP THIS ORDER SORTED IN THIS WAY! It is used as breaking categories for the pointer dumper!
 * @author Matthew Tropiano
 * @author Xaser Acheron
 */
public enum DEHActionPointerDefinition implements DEHActionPointer
{
	NULL                (0,   "NULL"),
	
	// Doom Weapon Action Pointers
	LIGHT0              (1,   true,  "Light0"),
	WEAPONREADY         (2,   true,  "WeaponReady"),
	LOWER               (3,   true,  "Lower"),
	RAISE               (4,   true,  "Raise"),
	PUNCH               (6,   true,  "Punch"),
	REFIRE              (9,   true,  "ReFire"),
	FIREPISTOL          (14,  true,  "FirePistol"),
	LIGHT1              (17,  true,  "Light1"),
	FIRESHOTGUN         (22,  true,  "FireShotgun"),
	LIGHT2              (31,  true,  "Light2"),
	FIRESHOTGUN2        (36,  true,  "FireShotgun2"),
	CHECKRELOAD         (38,  true,  "CheckReload"),
	OPENSHOTGUN2        (39,  true,  "OpenShotgun2"),
	LOADSHOTGUN2        (41,  true,  "LoadShotgun2"),
	CLOSESHOTGUN2       (43,  true,  "CloseShotgun2"),
	FIRECGUN            (52,  true,  "FireCGun"),
	GUNFLASH            (60,  true,  "GunFlash"),
	FIREMISSILE         (61,  true,  "FireMissile"),
	SAW                 (71,  true,  "Saw"),
	FIREPLASMA          (77,  true,  "FirePlasma"),
	BFGSOUND            (84,  true,  "BFGsound"),
	FIREBFG             (86,  true,  "FireBFG"),

	// Doom Thing Action Pointers
	BFGSPRAY            (119, false, "BFGSpray"),
	EXPLODE             (127, false, "Explode"),
	PAIN                (157, false, "Pain"),
	PLAYERSCREAM        (159, false, "PlayerScream"),
	FALL                (160, false, "Fall"),
	XSCREAM             (166, false, "XScream"),
	LOOK                (174, false, "Look"),
	CHASE               (176, false, "Chase"),
	FACETARGET          (184, false, "FaceTarget"),
	POSATTACK           (185, false, "PosAttack"),
	SCREAM              (190, false, "Scream"),
	VILECHASE           (243, false, "VileChase"),
	VILESTART           (255, false, "VileStart"),
	VILETARGET          (257, false, "VileTarget"),
	VILEATTACK          (264, false, "VileAttack"),
	STARTFIRE           (281, false, "StartFire"),
	FIRE                (282, false, "Fire"),
	FIRECRACKLE         (285, false, "FireCrackle"),
	TRACER              (316, false, "Tracer"),
	SKELWHOOSH          (336, false, "SkelWhoosh"),
	SKELFIST            (338, false, "SkelFist"),
	SKELMISSILE         (341, false, "SkelMissile"),
	FATRAISE            (376, false, "FatRaise"),
	FATATTACK1          (377, false, "FatAttack1"),
	FATATTACK2          (380, false, "FatAttack2"),
	FATATTACK3          (383, false, "FatAttack3"),
	BOSSDEATH           (397, false, "BossDeath"),
	CPOSATTACK          (417, false, "CPosAttack"),
	CPOSREFIRE          (419, false, "CPosRefire"),
	TROOPATTACK         (454, false, "TroopAttack"),
	SARGATTACK          (487, false, "SargAttack"),
	HEADATTACK          (506, false, "HeadAttack"),
	BRUISATTACK         (539, false, "BruisAttack"),
	SKULLATTACK         (590, false, "SkullAttack"),
	METAL               (603, false, "Metal"),
	SPOSATTACK          (616, false, "SPosAttack"),
	SPIDREFIRE          (618, false, "SpidRefire"),
	BABYMETAL           (635, false, "BabyMetal"),
	BSPIATTACK          (648, false, "BspiAttack"),
	HOOF                (676, false, "Hoof"),
	CYBERATTACK         (685, false, "CyberAttack"),
	PAINATTACK          (711, false, "PainAttack"),
	PAINDIE             (718, false, "PainDie"),
	KEENDIE             (774, false, "KeenDie"),
	BRAINPAIN           (779, false, "BrainPain"),
	BRAINSCREAM         (780, false, "BrainScream"),
	BRAINDIE            (783, false, "BrainDie"),
	BRAINAWAKE          (785, false, "BrainAwake"),
	BRAINSPIT           (786, false, "BrainSpit"),
	SPAWNSOUND          (787, false, "SpawnSound"),
	SPAWNFLY            (788, false, "SpawnFly"),
	BRAINEXPLODE        (801, false, "BrainExplode"),
	
	// MBF Thing Action Pointers
	DETONATE            (-1,  false, DEHActionPointerType.MBF, "Detonate"),
	MUSHROOM            (-1,  false, DEHActionPointerType.MBF, "Mushroom", DEHActionPointerParamType.ANGLE_FIXED, DEHActionPointerParamType.FIXED),
	SPAWN               (-1,  false, DEHActionPointerType.MBF, "Spawn", DEHActionPointerParamType.THING, DEHActionPointerParamType.FIXED),
	TURN                (-1,  false, DEHActionPointerType.MBF, "Turn", DEHActionPointerParamType.ANGLE_INT),
	FACE                (-1,  false, DEHActionPointerType.MBF, "Face", DEHActionPointerParamType.ANGLE_UINT),
	SCRATCH             (-1,  false, DEHActionPointerType.MBF, "Scratch", DEHActionPointerParamType.SHORT, DEHActionPointerParamType.SOUND),
	PLAYSOUND           (-1,  false, DEHActionPointerType.MBF, "PlaySound", DEHActionPointerParamType.SOUND, DEHActionPointerParamType.BOOL),
	RANDOMJUMP          (-1,  false, DEHActionPointerType.MBF, "RandomJump", DEHActionPointerParamType.STATE, DEHActionPointerParamType.UINT),
	LINEEFFECT          (-1,  false, DEHActionPointerType.MBF, "LineEffect", DEHActionPointerParamType.SHORT, DEHActionPointerParamType.SHORT),
	DIE                 (-1,  false, DEHActionPointerType.MBF, "Die"),
	FIREOLDBFG          (-1,  false, DEHActionPointerType.MBF, "FireOldBFG"),
	BETASKULLATTACK     (-1,  false, DEHActionPointerType.MBF, "BetaSkullAttack"),
	STOP                (-1,  false, DEHActionPointerType.MBF, "Stop"),

	// MBF21 Thing Action Pointers
	SPAWNOBJECT         (-1,  false, DEHActionPointerType.MBF21, "SpawnObject", DEHActionPointerParamType.THING, DEHActionPointerParamType.ANGLE_FIXED, DEHActionPointerParamType.FIXED, DEHActionPointerParamType.FIXED, DEHActionPointerParamType.FIXED, DEHActionPointerParamType.FIXED, DEHActionPointerParamType.FIXED, DEHActionPointerParamType.FIXED),
	MONSTERPROJECTILE   (-1,  false, DEHActionPointerType.MBF21, "MonsterProjectile", DEHActionPointerParamType.THING, DEHActionPointerParamType.ANGLE_FIXED, DEHActionPointerParamType.ANGLE_FIXED, DEHActionPointerParamType.FIXED, DEHActionPointerParamType.FIXED),
	MONSTERBULLETATTACK (-1,  false, DEHActionPointerType.MBF21, "MonsterBulletAttack", DEHActionPointerParamType.ANGLE_FIXED, DEHActionPointerParamType.ANGLE_FIXED, DEHActionPointerParamType.UINT, DEHActionPointerParamType.USHORT, DEHActionPointerParamType.UINT),
	MONSTERMELEEATTACK  (-1,  false, DEHActionPointerType.MBF21, "MonsterMeleeAttack", DEHActionPointerParamType.USHORT, DEHActionPointerParamType.UINT, DEHActionPointerParamType.SOUND, DEHActionPointerParamType.INT),
	RADIUSDAMAGE        (-1,  false, DEHActionPointerType.MBF21, "RadiusDamage", DEHActionPointerParamType.UINT, DEHActionPointerParamType.UINT),
	NOISEALERT          (-1,  false, DEHActionPointerType.MBF21, "NoiseAlert"),
	HEALCHASE           (-1,  false, DEHActionPointerType.MBF21, "HealChase", DEHActionPointerParamType.STATE, DEHActionPointerParamType.SOUND),
	SEEKTRACER          (-1,  false, DEHActionPointerType.MBF21, "SeekTracer", DEHActionPointerParamType.ANGLE_FIXED, DEHActionPointerParamType.ANGLE_FIXED),
	FINDTRACER          (-1,  false, DEHActionPointerType.MBF21, "FindTracer", DEHActionPointerParamType.ANGLE_FIXED, DEHActionPointerParamType.UINT),
	CLEARTRACER         (-1,  false, DEHActionPointerType.MBF21, "ClearTracer"),
	JUMPIFHEALTHBELOW   (-1,  false, DEHActionPointerType.MBF21, "JumpIfHealthBelow", DEHActionPointerParamType.STATE, DEHActionPointerParamType.INT),
	JUMPIFTARGETINSIGHT (-1,  false, DEHActionPointerType.MBF21, "JumpIfTargetInSight", DEHActionPointerParamType.STATE, DEHActionPointerParamType.ANGLE_FIXED),
	JUMPIFTARGETCLOSER  (-1,  false, DEHActionPointerType.MBF21, "JumpIfTargetCloser", DEHActionPointerParamType.STATE, DEHActionPointerParamType.FIXED),
	JUMPIFTRACERINSIGHT (-1,  false, DEHActionPointerType.MBF21, "JumpIfTracerInSight", DEHActionPointerParamType.STATE, DEHActionPointerParamType.ANGLE_FIXED),
	JUMPIFTRACERCLOSER  (-1,  false, DEHActionPointerType.MBF21, "JumpIfTracerCloser", DEHActionPointerParamType.STATE, DEHActionPointerParamType.FIXED),
	JUMPIFFLAGSSET      (-1,  false, DEHActionPointerType.MBF21, "JumpIfFlagsSet", DEHActionPointerParamType.STATE, DEHActionPointerParamType.FLAGS, DEHActionPointerParamType.FLAGS),
	ADDFLAGS            (-1,  false, DEHActionPointerType.MBF21, "AddFlags", DEHActionPointerParamType.FLAGS, DEHActionPointerParamType.FLAGS),
	REMOVEFLAGS         (-1,  false, DEHActionPointerType.MBF21, "RemoveFlags", DEHActionPointerParamType.FLAGS, DEHActionPointerParamType.FLAGS),

	// MBF21 Weapon Action Pointers
	WEAPONPROJECTILE    (-1,  true,  DEHActionPointerType.MBF21, "WeaponProjectile", DEHActionPointerParamType.THING, DEHActionPointerParamType.ANGLE_FIXED, DEHActionPointerParamType.ANGLE_FIXED, DEHActionPointerParamType.FIXED, DEHActionPointerParamType.FIXED),
	WEAPONBULLETATTACK  (-1,  true,  DEHActionPointerType.MBF21, "WeaponBulletAttack", DEHActionPointerParamType.ANGLE_FIXED, DEHActionPointerParamType.ANGLE_FIXED, DEHActionPointerParamType.UINT, DEHActionPointerParamType.USHORT, DEHActionPointerParamType.UINT),
	WEAPONMELEEATTACK   (-1,  true,  DEHActionPointerType.MBF21, "WeaponMeleeAttack", DEHActionPointerParamType.USHORT, DEHActionPointerParamType.UINT, DEHActionPointerParamType.FIXED, DEHActionPointerParamType.SOUND, DEHActionPointerParamType.FIXED),
	WEAPONSOUND         (-1,  true,  DEHActionPointerType.MBF21, "WeaponSound", DEHActionPointerParamType.SOUND, DEHActionPointerParamType.BOOL),
	WEAPONALERT         (-1,  true,  DEHActionPointerType.MBF21, "WeaponAlert"),
	WEAPONJUMP          (-1,  true,  DEHActionPointerType.MBF21, "WeaponJump", DEHActionPointerParamType.STATE, DEHActionPointerParamType.UINT),
	CONSUMEAMMO         (-1,  true,  DEHActionPointerType.MBF21, "ConsumeAmmo", DEHActionPointerParamType.SHORT),
	CHECKAMMO           (-1,  true,  DEHActionPointerType.MBF21, "CheckAmmo", DEHActionPointerParamType.STATE, DEHActionPointerParamType.USHORT),
	REFIRETO            (-1,  true,  DEHActionPointerType.MBF21, "RefireTo", DEHActionPointerParamType.STATE, DEHActionPointerParamType.BOOL),
	GUNFLASHTO          (-1,  true,  DEHActionPointerType.MBF21, "GunFlashTo", DEHActionPointerParamType.STATE, DEHActionPointerParamType.BOOL),
	;
	
	/** Originating frame (for DEH 3.0 format 19). */
	private int frame;
	/** Is weapon pointer. */
	private boolean weapon;
	/** Mnemonic name for BEX/DECORATE. */
	private String mnemonic;
	/** Action pointer type. */
	private DEHActionPointerType type;
	/** Action pointer parameters. */
	private DEHActionPointerParamType[] params;

	private static final Map<String, DEHActionPointerDefinition> MNEMONIC_MAP = EnumUtils.createCaseInsensitiveNameMap(DEHActionPointerDefinition.class);
	
	private DEHActionPointerDefinition(int frame, String mnemonic)
	{
		this(frame, false, DEHActionPointerType.DOOM19, mnemonic, new DEHActionPointerParamType[0]);
	}

	private DEHActionPointerDefinition(int frame, boolean weapon, String mnemonic)
	{
		this(frame, weapon, DEHActionPointerType.DOOM19, mnemonic, new DEHActionPointerParamType[0]);
	}

	private DEHActionPointerDefinition(int frame, boolean weapon, DEHActionPointerType type, String mnemonic)
	{
		this(frame, weapon, type, mnemonic, new DEHActionPointerParamType[0]);
	}

	private DEHActionPointerDefinition(int frame, boolean weapon, DEHActionPointerType type, String mnemonic, DEHActionPointerParamType ... params)
	{
		this.frame = frame;
		this.weapon = weapon;
		this.mnemonic = mnemonic;
		this.type = type;
		this.params = params;
	}

	public static DEHActionPointer getActionPointerByMnemonic(String mnemonic)
	{
		return MNEMONIC_MAP.get(mnemonic);
	}
	
	@Override
	public int getFrame() 
	{
		return frame;
	}
	
	@Override
	public boolean isWeapon()
	{
		return weapon;
	}
	
	@Override
	public String getMnemonic() 
	{
		return mnemonic;
	}
	
	@Override
	public DEHActionPointerType getType() 
	{
		return type;
	}
	
	@Override
	public DEHActionPointerParamType[] getParams()
	{
		return params;
	}

	@Override
	public DEHActionPointerParamType getParam(int index)
	{
		return index < 0 || index >= params.length ? null : params[index];
	}

}

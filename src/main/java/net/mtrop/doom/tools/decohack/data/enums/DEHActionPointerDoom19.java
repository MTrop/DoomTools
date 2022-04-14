/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data.enums;

import net.mtrop.doom.tools.decohack.data.DEHActionPointer;

/**
 * Enumeration of action pointers for frames.
 * NOTE: KEEP THIS ORDER SORTED IN THIS WAY! It is used as breaking categories for the pointer dumper!
 * @author Matthew Tropiano
 * @author Xaser Acheron
 */
public enum DEHActionPointerDoom19 implements DEHActionPointer
{
	NULL          (0,   "NULL"),
	
	// Doom Weapon Action Pointers
	LIGHT0        (1,   true,  "Light0"),
	WEAPONREADY   (2,   true,  "WeaponReady"),
	LOWER         (3,   true,  "Lower"),
	RAISE         (4,   true,  "Raise"),
	PUNCH         (6,   true,  "Punch"),
	REFIRE        (9,   true,  "ReFire"),
	FIREPISTOL    (14,  true,  "FirePistol"),
	LIGHT1        (17,  true,  "Light1"),
	FIRESHOTGUN   (22,  true,  "FireShotgun"),
	LIGHT2        (31,  true,  "Light2"),
	FIRESHOTGUN2  (36,  true,  "FireShotgun2"),
	CHECKRELOAD   (38,  true,  "CheckReload"),
	OPENSHOTGUN2  (39,  true,  "OpenShotgun2"),
	LOADSHOTGUN2  (41,  true,  "LoadShotgun2"),
	CLOSESHOTGUN2 (43,  true,  "CloseShotgun2"),
	FIRECGUN      (52,  true,  "FireCGun"),
	GUNFLASH      (60,  true,  "GunFlash"),
	FIREMISSILE   (61,  true,  "FireMissile"),
	SAW           (71,  true,  "Saw"),
	FIREPLASMA    (77,  true,  "FirePlasma"),
	BFGSOUND      (84,  true,  "BFGsound"),
	FIREBFG       (86,  true,  "FireBFG"),

	// Doom Thing Action Pointers
	BFGSPRAY      (119, false, "BFGSpray"),
	EXPLODE       (127, false, "Explode"),
	PAIN          (157, false, "Pain"),
	PLAYERSCREAM  (159, false, "PlayerScream"),
	FALL          (160, false, "Fall"),
	XSCREAM       (166, false, "XScream"),
	LOOK          (174, false, "Look"),
	CHASE         (176, false, "Chase"),
	FACETARGET    (184, false, "FaceTarget"),
	POSATTACK     (185, false, "PosAttack"),
	SCREAM        (190, false, "Scream"),
	VILECHASE     (243, false, "VileChase"),
	VILESTART     (255, false, "VileStart"),
	VILETARGET    (257, false, "VileTarget"),
	VILEATTACK    (264, false, "VileAttack"),
	STARTFIRE     (281, false, "StartFire"),
	FIRE          (282, false, "Fire"),
	FIRECRACKLE   (285, false, "FireCrackle"),
	TRACER        (316, false, "Tracer"),
	SKELWHOOSH    (336, false, "SkelWhoosh"),
	SKELFIST      (338, false, "SkelFist"),
	SKELMISSILE   (341, false, "SkelMissile"),
	FATRAISE      (376, false, "FatRaise"),
	FATATTACK1    (377, false, "FatAttack1"),
	FATATTACK2    (380, false, "FatAttack2"),
	FATATTACK3    (383, false, "FatAttack3"),
	BOSSDEATH     (397, false, "BossDeath"),
	CPOSATTACK    (417, false, "CPosAttack"),
	CPOSREFIRE    (419, false, "CPosRefire"),
	TROOPATTACK   (454, false, "TroopAttack"),
	SARGATTACK    (487, false, "SargAttack"),
	HEADATTACK    (506, false, "HeadAttack"),
	BRUISATTACK   (539, false, "BruisAttack"),
	SKULLATTACK   (590, false, "SkullAttack"),
	METAL         (603, false, "Metal"),
	SPOSATTACK    (616, false, "SPosAttack"),
	SPIDREFIRE    (618, false, "SpidRefire"),
	BABYMETAL     (635, false, "BabyMetal"),
	BSPIATTACK    (648, false, "BspiAttack"),
	HOOF          (676, false, "Hoof"),
	CYBERATTACK   (685, false, "CyberAttack"),
	PAINATTACK    (711, false, "PainAttack"),
	PAINDIE       (718, false, "PainDie"),
	KEENDIE       (774, false, "KeenDie"),
	BRAINPAIN     (779, false, "BrainPain"),
	BRAINSCREAM   (780, false, "BrainScream"),
	BRAINDIE      (783, false, "BrainDie"),
	BRAINAWAKE    (785, false, "BrainAwake"),
	BRAINSPIT     (786, false, "BrainSpit"),
	SPAWNSOUND    (787, false, "SpawnSound"),
	SPAWNFLY      (788, false, "SpawnFly"),
	BRAINEXPLODE  (801, false, "BrainExplode");
	
	/** Originating frame (for DEH 3.0 format 19). */
	private int frame;
	/** Is weapon pointer. */
	private boolean weapon;
	/** Mnemonic name for BEX/DECORATE. */
	private String mnemonic;

	private static final DEHActionPointerParamType[] NO_PARAMS = new DEHActionPointerParamType[0];
	
	private DEHActionPointerDoom19(int frame, String mnemonic)
	{
		this(frame, false, mnemonic);
	}

	private DEHActionPointerDoom19(int frame, boolean weapon, String mnemonic)
	{
		this.frame = frame;
		this.weapon = weapon;
		this.mnemonic = mnemonic;
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
		return DEHActionPointerType.DOOM19;
	}
	
	@Override
	public DEHActionPointerParamType[] getParams()
	{
		return NO_PARAMS;
	}

	@Override
	public DEHActionPointerParamType getParam(int index)
	{
		return null;
	}

}

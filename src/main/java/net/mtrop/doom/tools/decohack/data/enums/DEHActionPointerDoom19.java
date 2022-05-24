/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data.enums;

import java.util.Map;

import net.mtrop.doom.tools.decohack.data.DEHActionPointer;
import net.mtrop.doom.tools.struct.util.EnumUtils;

import static net.mtrop.doom.tools.decohack.data.DEHActionPointer.*;

/**
 * Enumeration of action pointers for frames.
 * NOTE: KEEP THIS ORDER SORTED IN THIS WAY! It is used as breaking categories for the pointer dumper!
 * @author Matthew Tropiano
 * @author Xaser Acheron
 */
public enum DEHActionPointerDoom19 implements DEHActionPointer
{
	// TODO: Finish docs!!
	
	NULL          (0,   "NULL"),
	
	// ========== Doom Weapon Action Pointers ==========
	
	LIGHT0        (1,   true,  "Light0", usage(
		"Sets the player's extra lighting level to 0.",
		"Used to reset lighting after weapon fire. See A_Light1 and A_Light2."
	)),
	
	WEAPONREADY   (2,   true,  "WeaponReady", usage(
		"Listens for the calling player's weapon fire input flag for firing a weapon. If so, weapon enters the FIRE state.",
		"Also sets the calling actor to S_PLAY (state 149) if the actor is in the S_PLAY_ATK1 or S_PLAY_ATK2 states (154 or 155).",
		"Also if the current weapon is WP_CHAINSAW (slot 7) and it is called on state S_SAW (67), it will play sound \"SAWIDL\".",
		"Also puts this weapon into the LOWER state if the player is NOT dead and has a pending weapon to switch to.",
		"Also sets the weapon graphic offsets for bobbing animation by player speed."
	)),
	
	LOWER         (3,   true,  "Lower", usage(
		"Lowers the weapon by a certain offset amount (LOWERSPEED) until it is off the screen. If it is, the pending weapon is switched to and enters its RAISE state.",
		"Can be called multiple times in one gametic to lower a weapon more quickly."
	)),
	
	RAISE         (4,   true,  "Raise", usage(
		"Raises the weapon by a certain offset amount (RAISESPEED) until it reaches a certain offset (WEAPONTOP). Once it does, it enters the READY state.",
		"Can be called multiple times in one gametic to raise a weapon more quickly."
	)),
	
	PUNCH         (6,   true,  "Punch", usage(
		"Performs a punch hitscan attack.",
		"If it is in range of a shootable actor, it plays the \"PUNCH\" sound, deals 1d10 x2 damage to that actor, multiplied by 10 if berserk.",
		"If the attack deals damage, it will turn the player towards what it punched.",
		"The range of the attack is 64 map units."
	)),
	
	REFIRE        (9,   true,  "ReFire", usage(
		"Checks if the player's weapon fire input flag is set, and if possible, the weapon is fired again.",
		"On refire, the player's \"refire\" count is incremented, the ammo amount is checked/deducted, and the weapon enters the FIRE state if all conditions are good.",
		"However, if the player is dead, is switching away from a weapon, or the weapon fire input flag is not set, the \"refire\" count on the player is set to 0, and ammo checks occur.",
		"NOTE: The \"refire\" count influences things like certain hitscan attack accuracies and which firing state to use on the chaingun (see A_FireCGun)."
	)),
	
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
	private Usage usage;
	/** Originating frame (for DEH 3.0 format 19). */
	private int frame;
	/** Is weapon pointer. */
	private boolean weapon;
	/** Mnemonic name for BEX/DECORATE. */
	private String mnemonic;

	private static final DEHActionPointerParamType[] NO_PARAMS = new DEHActionPointerParamType[0];
	
	private DEHActionPointerDoom19(int frame, String mnemonic)
	{
		this(frame, false, mnemonic, BLANK_USAGE);
	}

	private DEHActionPointerDoom19(int frame, String mnemonic, Usage usage)
	{
		this(frame, false, mnemonic, usage);
	}

	private DEHActionPointerDoom19(int frame, boolean weapon, String mnemonic)
	{
		this(frame, weapon, mnemonic, BLANK_USAGE);
	}

	private DEHActionPointerDoom19(int frame, boolean weapon, String mnemonic, Usage usage)
	{
		this.frame = frame;
		this.weapon = weapon;
		this.mnemonic = mnemonic;
		this.usage = usage;
	}

	private static final Map<String, DEHActionPointerDoom19> MNEMONIC_MAP = EnumUtils.createCaseInsensitiveNameMap(DEHActionPointerDoom19.class);
	
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

	@Override
	public Usage getUsage() 
	{
		return usage;
	}
	
}

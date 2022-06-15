/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data.enums;

import net.mtrop.doom.tools.decohack.data.DEHActionPointer;
import net.mtrop.doom.tools.struct.util.EnumUtils;

import static net.mtrop.doom.tools.decohack.data.DEHActionPointer.params;
import static net.mtrop.doom.tools.decohack.data.DEHActionPointer.usage;
import static net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerParamType.*;

import java.util.Map;

/**
 * Enumeration of action pointers for frames.
 * NOTE: KEEP THIS ORDER SORTED IN THIS WAY! It is used as breaking categories for the pointer dumper!
 * @author Matthew Tropiano
 * @author Xaser Acheron
 */
public enum DEHActionPointerMBF implements DEHActionPointer
{
	// MBF Thing Action Pointers
	DETONATE        (false, "Detonate", params(), usage(
		"Performs a radius damage attack according to the actor's damage property.",
		"Like A_Explode, except both the radius and potential damage are taken from the calling actor's damage property.",
		"For example, if the actor's Damage is 100, this has a radius of 100 and a max damage of 100 at the center."
	)),
	
	MUSHROOM        (false, "Mushroom", params(ANGLEFIXED, FIXED), usage(
		"Calls A_Explode and then creates a mushroom-like explosion of Mancubus fireballs (MT_FATSHOT, slot 10) from the calling actor.",
		"The Damage property of the calling actor controls how many fireballs are created.",
		"\nNOTE: Since this function uses the MISC fields for this state, the Offset directive cannot be used."
		).parameter("vangle", ANGLEFIXED, "The vertical angle of the launch (default 0.0)."
		).parameter("speed", FIXED, "The projectile speed (default 0.0)."
	)),
	
	SPAWN           (false, "Spawn", params(THING, SHORT), usage(
		"Spawns an object in the same position as the calling actor.",
		"Note that if both the caller and the spawned are SOLID, they can get stuck on each other.",
		"\nNOTE: Since this function uses the MISC fields for this state, the Offset directive cannot be used."
		).parameter("thingId", THING, "The slot id/alias of the Thing to spawn (thing slot) (default 0)."
		).parameter("zpos", SHORT, "The Z-position of the spawned object relative to the caller (default 0)."
	)),
	
	TURN            (false, "Turn", params(ANGLEINT), usage(
		"Adjusts the calling actor's angle by the desired angle.",
		"\nNOTE: Since this function uses the MISC fields for this state, the Offset directive cannot be used."
		).parameter("angle", ANGLEINT, "The angle amount in degrees (default 0)."
	)),
	
	FACE            (false, "Face", params(ANGLEUINT), usage(
		"Sets the calling actor's angle to an absolute value.",
		"\nNOTE: Since this function uses the MISC fields for this state, the Offset directive cannot be used."
		).parameter("angle", ANGLEUINT, "The absolute angle in degrees (default 0)."
	)),
	
	SCRATCH         (false, "Scratch", params(SHORT, SOUND), usage(
		"Performs a melee attack from the calling actor, dealing a set amount of damage if it connects.",
		"\nNOTE: Since this function uses the MISC fields for this state, the Offset directive cannot be used."
		).parameter("damage", SHORT, "The amount of damage on hit (default 0)."
		).parameter("sound", SOUND, "The sound to play if the damage occurs (default \"\").", "The sound is played from the calling actor."
	)),
	
	PLAYSOUND       (false, "PlaySound", params(SOUND, BOOL), usage(
		"Plays the desired sound from the calling actor.",
		"\nNOTE: Since this function uses the MISC fields for this state, the Offset directive cannot be used."
		).parameter("sound", SOUND, "The sound to play (default \"\")."
		).parameter("fullvolume", BOOL, "If true (non-zero), the sound is played at full volume, otherwise (zero), it is played from the caller (default 0)."
	)),
	
	RANDOMJUMP      (false, "RandomJump", params(STATE, UINT), usage(
		"Jumps to the desired state if a random check succeeds.",
		"If the check does not succeed, this will continue on instead of performing the jump.",
		"It is recommended to NOT use this on a state with a duration less than 0 for it to work properly.",
		"The RandomJump call may use a separate random seed, depending on implementation.",
		"\nNOTE: Since this function uses the MISC fields for this state, the Offset directive cannot be used."
		).parameter("state", STATE, "The state or state label to jump to.", "It is recommended to use a state label."
		).parameter("chance", UINT, "A probability value from 0 to 256, 0 being \"never\" while 256 is \"always.\""
	)),
	
	LINEEFFECT      (false, "LineEffect", params(SHORT, SHORT), usage(
		"Performs a specific line special event as though the calling actor activated one.",
		"\nNOTE: Since this function uses the MISC fields for this state, the Offset directive cannot be used."
		).parameter("special", SHORT, "The line special to activate.", "This value is the same as the line special numbers in map editors."
		).parameter("tag", SHORT, "The tag of the sector to affect."
	)),
	
	DIE             (false, "Die", params(), usage(
		"Kills the calling actor, as though it were dealt lethal damage.",
		"Specifically, this damages the caller by its current health, with no source to the damage.",
		"Since it is damaged by its remaining health, this will not put the caller in its \"extreme\" death state."
	)),
	
	BETASKULLATTACK (false, "BetaSkullAttack", params(), usage(
		"Performs the \"beta\" version attack of the Lost Soul.",
		"Plays the calling actor's ATTACK sound (if defined), calls A_FaceTarget, and damages its target 1d8 x the caller's Damage property.",
		"This does not cause monster infighting, and does not damage the same \"species\" of the calling actor."
	)),
	
	STOP            (false, "Stop", params(), usage(
		"Arrests the momentum of the calling actor.",
		"Sets the X, Y, and Z momentum of the calling actor to 0."
	)),

	// MBF Weapon Action Pointers

	FIREOLDBFG      (true,  "FireOldBFG", params(), usage(
		"Fires a \"Beta BFG\" shot from the calling player.",
		"This does a bunch of stuff - fires MT_PLASMA1 (slot 141) and an MT_PLASMA2 (slot 142) shot, " + 
			"subtracts 1 of the weapon's ammo, and sets player extralight to 2 (equivalent to calling A_Light2)."
	));
	
	
	private static final Map<String, DEHActionPointerMBF> MNEMONIC_MAP = EnumUtils.createCaseInsensitiveNameMap(DEHActionPointerMBF.class);

	/** Function usage. */
	private Usage usage;
	/** Mnemonic name for BEX/DECORATE. */
	private String mnemonic;
	/** Is Weapon state. */
	private boolean weapon;
	/** Action pointer parameters. */
	private DEHActionPointerParamType[] params;

	private DEHActionPointerMBF(boolean weapon, String mnemonic, DEHActionPointerParamType[] params, Usage usage)
	{
		this.usage = usage;
		this.mnemonic = mnemonic;
		this.weapon = weapon;
		this.params = params;
	}

	public static DEHActionPointer getActionPointerByMnemonic(String mnemonic)
	{
		return MNEMONIC_MAP.get(mnemonic);
	}
	
	@Override
	public int getFrame() 
	{
		return -1;
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
		return DEHActionPointerType.MBF;
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

	@Override
	public Usage getUsage() 
	{
		return usage;
	}
	
}

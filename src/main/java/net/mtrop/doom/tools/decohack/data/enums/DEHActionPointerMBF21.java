/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data.enums;

import java.util.Map;

import net.mtrop.doom.tools.decohack.data.DEHActionPointer;
import net.mtrop.doom.tools.struct.util.EnumUtils;

import static net.mtrop.doom.tools.decohack.data.DEHActionPointer.params;
import static net.mtrop.doom.tools.decohack.data.DEHActionPointer.usage;
import static net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerParamType.*;


/**
 * Enumeration of action pointers for frames.
 * NOTE: KEEP THIS ORDER SORTED IN THIS WAY! It is used as breaking categories for the pointer dumper!
 * @author Matthew Tropiano
 * @author Xaser Acheron
 */
public enum DEHActionPointerMBF21 implements DEHActionPointer
{
	// MBF21 Thing Action Pointers
	
	SPAWNOBJECT         (false, "SpawnObject", params(THING, ANGLEFIXED, FIXED, FIXED, FIXED, FIXED, FIXED, FIXED), usage(
		"Spawns a new actor relative to the position of the calling actor.",
		"If the spawned actor is a projectile, its \"target\" and \"tracer\" references are copied from the calling actor.",
		"If the spawned actor is NOT a projectile, its \"target\" is the calling actor and its \"tracer\" is set to the caller's target."
		).parameter("thingId", THING, "The thing to spawn."
		).parameter("angle", ANGLEFIXED, "The angle of the spawned actor, relative to the calling actor's angle."
		).parameter("xoffset", FIXED, "The X-position relative to the calling actor's position (forward/back)."
		).parameter("yoffset", FIXED, "The Y-position relative to the calling actor's position (left/right)."
		).parameter("zoffset", FIXED, "The Z-position relative to the calling actor's position (up/down)."
		).parameter("xvel", FIXED, "The initial X-velocity of the spawned actor (forward/back)."
		).parameter("yvel", FIXED, "The initial Y-velocity of the spawned actor (left/right)."
		).parameter("zvel", FIXED, "The initial Z-velocity of the spawned actor (up/down)."
	)),
	
	MONSTERPROJECTILE   (false, "MonsterProjectile", params(THING, ANGLEFIXED, ANGLEFIXED, FIXED, FIXED), usage(
		"Fires a projectile from the calling actor.",
		"The spawned projectile also has its \"tracer\" pointer set to the calling actor's target to enable tracing behavior."
		).parameter("thingId", THING, "The thing to spawn as the new projectile (must be flagged as a projectile)."
		).parameter("angle", ANGLEFIXED, "The angle of the spawned actor, relative to the calling actor's angle."
		).parameter("pitch", ANGLEFIXED, "The pitch of the spawned actor, relative to the calling actor's pitch."
		).parameter("hoffset", FIXED, "The horizontal position relative to the calling actor."
		).parameter("voffset", FIXED, "The vertical position relative to the calling actor's default projectile fire height."
	)),
	
	MONSTERBULLETATTACK (false, "MonsterBulletAttack", params(ANGLEFIXED, ANGLEFIXED, UINT, USHORT, UINT), usage(
		"Fires a multi-hitscan attack from the calling actor.",
		"The damage formula is: (damagebase * random(1, damagedice)).",
		"The damage parameter defaults are identical to Doom's monster bullet attack damage values."
		).parameter("hspread", ANGLEFIXED, "The horizontal spread in degrees (default 0.0)."
		).parameter("vspread", ANGLEFIXED, "The vertical spread in degrees (default 0.0)."
		).parameter("numBullets", UINT, "The number of hitscans to fire (default 1)."
		).parameter("damageBase", USHORT, "The attack base damage (default 3)."
		).parameter("damageDice", UINT, "The attack damage random multiplier (default 5)."
	)),
	
	MONSTERMELEEATTACK  (false, "MonsterMeleeAttack", params(USHORT, UINT, SOUND, FIXED), usage(
		"Performs a generic melee attack from the calling actor.",
		"The damage formula is: (damagebase * random(1, damagedice))."
		).parameter("damageBase", USHORT, "The attack base damage (default 3)."
		).parameter("damageDice", UINT, "The attack damage random multiplier (default 8)."
		).parameter("sound", SOUND, "The sound to play if the damage occurs (default \"\").", "The sound is played from the calling actor."
		).parameter("range", FIXED, "The distance check for the attack connecting.", "If not provided, the actor's default melee range is used."
	)),
	
	RADIUSDAMAGE        (false, "RadiusDamage", params(UINT, UINT), usage(
		"Performs a generic radius damage attack from the calling actor.",
		"The damage is at its greatest at the actor's centerpoint."
		).parameter("damage", UINT, "The maximum damage."
		).parameter("radius", UINT, "The attack radius in map units."
	)),
	
	NOISEALERT          (false, "NoiseAlert", params(), usage(
		"Alerts monsters within sound-travel distance of the calling actor's target."
	)),
	
	HEALCHASE           (false, "HealChase", params(STATE, SOUND), usage(
		"Does exactly what A_Chase does, with a few differences.",
		"If a dead actor (CORPSE flag is set) with a defined RAISE state is in range, the dead actor is revived, and the calling actor will jump to the provided state and play the provided sound.",
		"The revived actor has its health reset, its flags restored, and its target cleared, and it enters its RAISE state.",
		"See A_VileChase for the inspiration. A_HealChase(S_VILE_HEAL1, slop) is the equivalent to A_VileChase."
		).parameter("state", STATE, "The state or state label to jump to when this actor encounters a corpse.", "It is recommended to use a state label."
		).parameter("sound", SOUND, "The sound to play from the calling actor on revive."
	)),
	
	SEEKTRACER          (false, "SeekTracer", params(ANGLEFIXED, ANGLEFIXED), usage(
		"Missile seeker function.",
		"When called, this function will adjust the facing angle and velocity of this projectile towards its \"tracer\" reference.",
		"The calling actor must have its \"tracer\" reference set, via A_MonsterProjectile or A_SpawnObject or A_FindTracer or potentially A_WeaponProjectile.",
		"Note that when using this function, keep in mind that seek \"strength\" also depends on how often this function is called.", 
		"Calling A_SeekTracer every tic will result in a much more aggressive seek than calling it every 4 tics, even if the args are the same.",
		"The inspiration for this function is based on Heretic's seeker missile logic, rather than Doom's, so there are some slight differences in behavior compared to A_Tracer (better z-axis seeking, for instance)."
		).parameter("threshold", ANGLEFIXED, "If the angle to \"tracer\" target is lower than this, missile will \"snap\" directly to face the target."
		).parameter("maxTurnAngle", ANGLEFIXED, "The maximum angle a missile will turn towards the target if the angle is above the threshold."
	)),
	
	FINDTRACER          (false, "FindTracer", params(ANGLEFIXED, UINT), usage(
		"Searches for a valid actor to set as a \"tracer\" reference, but ONLY if the calling actor doesn't already have one.",
		"This is best used on projectiles shot from a player's weapon.",
		"Actors that are friendly to the calling actor are not included in the search.",
		"See A_ClearTracer to clear this actor's \"tracer\" reference, if needed."
		).parameter("fov", ANGLEFIXED, "The field-of-view, relative to calling actor's angle, to search for targets.", "If zero, the search will occur in all directions."
		).parameter("rangeBlocks", UINT, "The distance to search, in map blocks (128 units) (default 10).", "Do NOT set this to a high value - the larger the search, the more expensive the call."
	)),
	
	CLEARTRACER         (false, "ClearTracer", params(), usage(
		"Clears the calling actor's \"tracer\" reference.",
		"May be necessary if you want A_FindTracer to seek a new target."
	)),
	
	JUMPIFHEALTHBELOW   (false, "JumpIfHealthBelow", params(STATE, INT), usage(
		"Jumps to the provided state if the calling actor's health is below the specified threshold.",
		"The calling actor's health must be STRICTLY less than the provided value."
		).parameter("state", STATE, "The state or state label to jump to.", "It is recommended to use a state label."
		).parameter("health", INT, "The health value."
	)),
	
	JUMPIFTARGETINSIGHT (false, "JumpIfTargetInSight", params(STATE, ANGLEFIXED), usage(
		"Jumps to the provided state if the calling actor's current target is in its line-of-sight."
		).parameter("state", STATE, "The state or state label to jump to.", "It is recommended to use a state label."
		).parameter("fov", ANGLEFIXED, "The field-of-view, relative to calling actor's angle, to search for its current target.", "If zero, the search will occur in all directions."
	)),
	
	JUMPIFTARGETCLOSER  (false, "JumpIfTargetCloser", params(STATE, FIXED), usage(
		"Jumps to the provided state if the calling actor's current target is closer than the specified distance.",
		"The target's distance must be STRICTLY less than the provided value."
		).parameter("state", STATE, "The state or state label to jump to.", "It is recommended to use a state label."
		).parameter("distance", FIXED, "The distance in map units."
	)),
	
	JUMPIFTRACERINSIGHT (false, "JumpIfTracerInSight", params(STATE, ANGLEFIXED), usage(
		"Jumps to the provided state if the calling actor's \"tracer\" reference is in its line-of-sight."
		).parameter("state", STATE, "The state or state label to jump to.", "It is recommended to use a state label."
		).parameter("fov", ANGLEFIXED, "The field-of-view, relative to calling actor's angle, to search for its current tracer reference.", "If zero, the search will occur in all directions."
	)),
	
	JUMPIFTRACERCLOSER  (false, "JumpIfTracerCloser", params(STATE, FIXED), usage(
		"Jumps to the provided state if the calling actor's \"tracer\" reference is closer than the specified distance.",
		"The tracer's distance must be STRICTLY less than the provided value."
		).parameter("state", STATE, "The state or state label to jump to.", "It is recommended to use a state label."
		).parameter("distance", FIXED, "The distance in map units."
	)),
	
	JUMPIFFLAGSSET      (false, "JumpIfFlagsSet", params(STATE, FLAGS, FLAGS), usage(
		"Jumps to the provided state if ALL of the calling actor's thing flags are set to the provided values.",
		"This is an AND check, not an equality check - all of the provided BITS must be set."
		).parameter("state", STATE, "The state or state label to jump to.", "It is recommended to use a state label."
		).parameter("flags", FLAGS, "The Doom thing flag bits."
		).parameter("mbf21flags", FLAGS, "The MBF21 thing flag bits."
	)),
	
	ADDFLAGS            (false, "AddFlags", params(FLAGS, FLAGS), usage(
		"Sets the provided actor flag bits on the calling actor."
		).parameter("flags", FLAGS, "The Doom thing flag bits."
		).parameter("mbf21flags", FLAGS, "The MBF21 thing flag bits."
	)),
	
	REMOVEFLAGS         (false, "RemoveFlags", params(FLAGS, FLAGS), usage(
		"Clears the provided actor flag bits on the calling actor."
		).parameter("flags", FLAGS, "The Doom thing flag bits."
		).parameter("mbf21flags", FLAGS, "The MBF21 thing flag bits."
	)),

	// MBF21 Weapon Action Pointers
	
	WEAPONPROJECTILE    (true,  "WeaponProjectile", params(THING, ANGLEFIXED, ANGLEFIXED, FIXED, FIXED), usage(
		"Fires a projectile from the calling player.",
		"Unlike most of DOOM19's attack codepointers, this function will NOT consume ammo, trigger the weapon's FLASH state, nor play a sound.",
		"The projectile spawned will have its \"tracer\" reference set to the actor found by Doom's autoaim, if any."
		).parameter("thingId", THING, "The thing to spawn as the new projectile (must be flagged as a projectile)."
		).parameter("angle", ANGLEFIXED, "The angle of the spawned actor, relative to the calling player's angle."
		).parameter("pitch", ANGLEFIXED, "The pitch of the spawned actor, relative to the calling player's pitch."
		).parameter("hoffset", FIXED, "The horizontal position relative to the calling player."
		).parameter("voffset", FIXED, "The vertical position relative to the calling player's default projectile fire height."
	)),
	
	WEAPONBULLETATTACK  (true,  "WeaponBulletAttack", params(ANGLEFIXED, ANGLEFIXED, UINT, USHORT, UINT), usage(
		"Fires a multi-hitscan attack from the calling player.",
		"The damage formula is: (damageBase * random(1, damageDice)).",
		"The damage parameter defaults are identical to Doom's monster bullet attack damage values.",
		"Unlike most of DOOM19's attack codepointers, this function will NOT consume ammo, trigger the weapon's FLASH state, nor play a sound.",
		"\nNOTE: The documentation is not incorrect - Doom actually reverses the base damage and dice for player attacks, compared to enemy hitscans."
		).parameter("hspread", ANGLEFIXED, "The horizontal spread in degrees (default 0.0)."
		).parameter("vspread", ANGLEFIXED, "The vertical spread in degrees (default 0.0)."
		).parameter("numBullets", UINT, "The number of hitscans to fire (default 1)."
		).parameter("damageBase", USHORT, "The attack base damage (default 5)."
		).parameter("damageDice", UINT, "The attack damage random multiplier (default 3)."
	)),
	
	WEAPONMELEEATTACK   (true,  "WeaponMeleeAttack", params(USHORT, UINT, FIXED, SOUND, FIXED), usage(
		"Performs a generic melee attack from the calling player.",
		"The damage formula is: (damagebase * random(1, damagedice)).",
		"If the calling player is Berserk, the damage is additionally multiplied by the berserk multiplier."
		).parameter("damageBase", USHORT, "The attack base damage (default 2)."
		).parameter("damageDice", UINT, "The attack damage random multiplier (default 10)."
		).parameter("berserkFactor", FIXED, "The berserk attack damage multiplier (default 1.0)."
		).parameter("sound", SOUND, "The sound to play if the damage occurs (default \"\").", "The sound is played from the calling player."
		).parameter("range", FIXED, "The distance check for the attack connecting.", "If not provided, the player's default melee range is used."
	)),
	
	WEAPONSOUND         (true,  "WeaponSound", params(SOUND, BOOL), usage(
		"Plays the desired sound from the calling player.",
		"NOTE: This does not affect alerting enemies!"
		).parameter("sound", SOUND, "The sound to play (default \"\")."
		).parameter("fullvolume", BOOL, "If true (non-zero), the sound is played at full volume across the map, otherwise (zero), it is played from the caller (default 0)."
	)),
	
	WEAPONALERT         (true,  "WeaponAlert", params(), usage(
		"Alerts monsters within sound-travel distance of the calling player.",
		"Overrides the SILENT weapon flag."
	)),
	
	WEAPONJUMP          (true,  "WeaponJump", params(STATE, UINT), usage(
		"Jumps to the desired state if a random check succeeds.",
		"If the check does not succeed, this will continue on instead of performing the jump.",
		"It is recommended to NOT use this on a state with a duration less than 0 for it to work properly.",
		"The WeaponJump call may use a separate random seed, depending on implementation."
		).parameter("state", STATE, "The state or state label to jump to.", "It is recommended to use a state label."
		).parameter("chance", UINT, "A probability value from 0 to 256, 0 being \"never\" while 256 is \"always.\""
	)),
	
	CONSUMEAMMO         (true,  "ConsumeAmmo", params(SHORT), usage(
		"Subtracts ammo from the currently-selected weapon's ammo pool on the calling player.",
		"If the amount is 0, the current weapon's AmmoPerShot property is used.",
		"If the amount is negative, the ammo is added instead."
		).parameter("state", STATE, "The state or state label to jump to.", "It is recommended to use a state label."
		).parameter("amount", UINT, "The amount of ammo to subtract."
	)),
	
	CHECKAMMO           (true,  "CheckAmmo", params(STATE, USHORT), usage(
		"Jumps to the desired state if the calling player's ammo is STRICTLY less than the provided value.",
		"If the check does not succeed, this will continue on instead of performing the jump."
		).parameter("state", STATE, "The state or state label to jump to.", "It is recommended to use a state label."
		).parameter("amount", USHORT, "The ammo value."
	)),
	
	REFIRETO            (true,  "RefireTo", params(STATE, BOOL), usage(
		"Jumps to the desired state if the calling player is holding FIRE and there is emough ammo for a re-fire.",
		"If the check does not succeed, this will continue on instead of performing the jump."
		).parameter("state", STATE, "The state or state label to jump to.", "It is recommended to use a state label."
		).parameter("noAmmoCheck", BOOL, "If true (non-zero), the ammo check is skipped."
	)),
	
	GUNFLASHTO          (true,  "GunFlashTo", params(STATE, BOOL), usage(
		"Starts the desired state as a gun-flash overlay for the calling player."
		).parameter("state", STATE, "The state or state label to jump to.", "It is recommended to use a state label."
		).parameter("noThirdPerson", BOOL, "If true (non-zero), the calling player's actor state is NOT set to S_PLAY_ATK2 (155)."
	));
	
	/** Function usage. */
	private Usage usage;
	/** Is weapon pointer. */
	private boolean weapon;
	/** Mnemonic name for BEX/DECORATE. */
	private String mnemonic;
	/** Action pointer parameters. */
	private DEHActionPointerParamType[] params;

	private DEHActionPointerMBF21(boolean weapon, String mnemonic, DEHActionPointerParamType[] params, Usage usage)
	{
		this.usage = usage;
		this.weapon = weapon;
		this.mnemonic = mnemonic;
		this.params = params;
	}

	private static final Map<String, DEHActionPointerMBF21> MNEMONIC_MAP = EnumUtils.createCaseInsensitiveNameMap(DEHActionPointerMBF21.class);
	
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
		return DEHActionPointerType.MBF21;
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

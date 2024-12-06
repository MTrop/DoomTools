/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data.enums;

import java.util.Map;

import net.mtrop.doom.tools.struct.util.EnumUtils;

/**
 * Doom Thing flags
 * @author Matthew Tropiano
 * @author Xaser Acheron
 */
public enum DEHThingDoom19Flag implements DEHFlag
{
	SPECIAL       (0x00000001, "Thing has special inventory/pickup behavior, decided by sprite name."),
	SOLID         (0x00000002, "Thing is solid."),
	SHOOTABLE     (0x00000004, "Thing is shootable."),
	NOSECTOR      (0x00000008, "Thing belongs to no sector lookups (and is NOT rendered)."),
	NOBLOCKMAP    (0x00000010, "Thing is not maintained by the blockmap (most missiles or objects that don't collide with players)."),
	AMBUSH        (0x00000020, "Thing is in AMBUSH mode (heard a sound, A_Look looks in all directions)."),
	JUSTHIT       (0x00000040, "Thing was just damaged by something. Affects A_Chase reaction time logic."),
	JUSTATTACKED  (0x00000080, "Thing has just performed an attack. Affects A_Chase logic."),
	SPAWNCEILING  (0x00000100, "Thing hangs from the ceiling."),
	NOGRAVITY     (0x00000200, "Thing is not susceptible to gravity."),
	DROPOFF       (0x00000400, "Thing can travel over cliffs taller than 24 units."),
	PICKUP        (0x00000800, "Thing can pick up items (collision logic)."),
	NOCLIP        (0x00001000, "Thing does not collide with SOLID objects or lines."),
	SLIDE         (0x00002000, "Thing slides along walls (should never be set - used in player logic)."),
	FLOAT         (0x00004000, "Thing is a flying thing. Affects A_Chase logic."),
	TELEPORT      (0x00008000, "Thing is currently teleporting (should never be set - does not trip lines during move logic)."),
	MISSILE       (0x00010000, "Thing is a projectile. Affects collision and actor update logic."),
	DROPPED       (0x00020000, "Thing was dropped via another actor's death. Reduces ammo on pickup and does not respawn."),
	SHADOW        (0x00040000, "Thing is rendered in Spectre fuzz."),
	NOBLOOD       (0x00080000, "Thing does not bleed when shot (makes bullet puff)."),
	CORPSE        (0x00100000, "Thing is a corpse (set on death)."),
	INFLOAT       (0x00200000, "Thing is floating towards target actor's height (should never be set - part of A_Chase logic)."),
	COUNTKILL     (0x00400000, "Thing counts towards kill count."),
	COUNTITEM     (0x00800000, "Thing counts towards item count."),
	SKULLFLY      (0x01000000, "Thing is currently in a lost soul charge attack (should never be set)."),
	NOTDEATHMATCH (0x02000000, "Thing never appears in DeathMatch game modes."),
	TRANSLATION   (0x04000000, "Thing palette translation bit 0. If set, palette range 112-127 (green) is now 96-111 (gray). If set with TRANSLATION2, it is 32-47 (red)."),
	TRANSLATION1  (0x04000000, "Thing palette translation bit 0. If set, palette range 112-127 (green) is now 96-111 (gray). If set with TRANSLATION2, it is 32-47 (red)."),
	UNUSED1       (0x08000000, "Thing palette translation bit 1. If set, palette range 112-127 (green) is now 64-79 (brown). If set with TRANSLATION, it is 32-47 (red)."),
	TRANSLATION2  (0x08000000, "Thing palette translation bit 1. If set, palette range 112-127 (green) is now 64-79 (brown). If set with TRANSLATION, it is 32-47 (red)."),
	UNUSED2       (0x10000000, "Unused bit (in Doom19)."),
	UNUSED3       (0x20000000, "Unused bit (in Doom19)."),
	UNUSED4       (0x40000000, "Unused bit (in Doom19)."),
	;

	private static final Map<String, DEHThingDoom19Flag> MNEMONIC_MAP = EnumUtils.createCaseInsensitiveNameMap(DEHThingDoom19Flag.class);

	public static DEHThingDoom19Flag getByMnemonic(String mnemonic)
	{
		return MNEMONIC_MAP.get(mnemonic);
	}

	private final int value;
	private final String usage;

	private DEHThingDoom19Flag(int value, String usage)
	{
		this.value = value;
		this.usage = usage;
	}

	public int getValue()
	{
		return value;
	}
	
	@Override
	public String getUsage() 
	{
		return usage;
	}
	
}

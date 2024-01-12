/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data.enums;

import java.util.Map;

import net.mtrop.doom.tools.struct.util.EnumUtils;

/**
 * MBF21 Weapon flags.
 * @author Xaser Acheron
 * @author Matthew Tropiano 
 */
public enum DEHWeaponMBF21Flag implements DEHFlag
{
	NOTHRUST       (0x00000001, "Weapon's shots do not thrust things around."),
	SILENT         (0x00000002, "Weapon does not alert monsters on shot (must use A_WeaponAlert to alert)."),
	NOAUTOFIRE     (0x00000004, "Weapon will not autofire if FIRE is held on switch to it."),
	FLEEMELEE      (0x00000008, "Weapon is considered \"melee\" and enemies know it."),
	AUTOSWITCHFROM (0x00000010, "Weapon is auto-switched away if a different weapon's ammo is picked up."),
	NOAUTOSWITCHTO (0x00000020, "Weapon is never auto-switched to."),
	;

	private static final Map<String, DEHWeaponMBF21Flag> MNEMONIC_MAP = EnumUtils.createCaseInsensitiveNameMap(DEHWeaponMBF21Flag.class);

	public static DEHWeaponMBF21Flag getByMnemonic(String mnemonic)
	{
		return MNEMONIC_MAP.get(mnemonic);
	}
	
	private int value;
	private String usage;

	private DEHWeaponMBF21Flag(int value, String usage)
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

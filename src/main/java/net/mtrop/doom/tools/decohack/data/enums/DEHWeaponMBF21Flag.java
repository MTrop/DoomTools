/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano & Xaser Acheron
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
	NOTHRUST       (0x00000001),
	SILENT         (0x00000002),
	NOAUTOFIRE     (0x00000004),
	FLEEMELEE      (0x00000008),
	AUTOSWITCHFROM (0x00000010),
	NOAUTOSWITCHTO (0x00000020),
	;

	private static final Map<String, DEHWeaponMBF21Flag> MNEMONIC_MAP = EnumUtils.createCaseInsensitiveNameMap(DEHWeaponMBF21Flag.class);

	public static DEHWeaponMBF21Flag getByMnemonic(String mnemonic)
	{
		return MNEMONIC_MAP.get(mnemonic);
	}
	
	private int value;

	private DEHWeaponMBF21Flag(int value)
	{
		this.value = value;
	}

	public int getValue()
	{
		return value;
	}
}

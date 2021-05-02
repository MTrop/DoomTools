/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano & Xaser Acheron
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

/**
 * MBF21 Weapon flags.
 * @author Xaser Acheron
 */
public enum DEHWeaponFlag
{
	NOTHRUST      (0x00000001, "nothrust"),
	SILENT        (0x00000002, "silent"),
	NOAUTOFIRE    (0x00000004, "noautofire"),
	FLEEMELEE     (0x00000008, "fleemelee"),
	AUTOSWITCHFROM(0x00000010, "autoswitchfrom"),
	NOAUTOSWITCHTO(0x00000020, "noautoswitchto"),
	;

	public static final DEHWeaponFlag[] VALUES = values();

	private int value;
	private String mnemonic;

	private DEHWeaponFlag(int value, String mnemonic)
	{
		this.value = value;
		this.mnemonic = mnemonic;
	}

	public int getValue()
	{
		return value;
	}

	public String getMnemonic()
	{
		return mnemonic;
	}
}

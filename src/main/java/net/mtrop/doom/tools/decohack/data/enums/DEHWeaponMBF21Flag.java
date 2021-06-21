/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano & Xaser Acheron
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data.enums;

import java.util.Map;
import java.util.TreeMap;

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

	public static final DEHWeaponMBF21Flag[] VALUES = values();

	private static final Map<String, DEHWeaponMBF21Flag> MNEMONIC_MAP = new TreeMap<String, DEHWeaponMBF21Flag>(String.CASE_INSENSITIVE_ORDER)
	{
		private static final long serialVersionUID = -2813044917550646916L;
		{
			for (DEHWeaponMBF21Flag val : DEHWeaponMBF21Flag.values())
				put(val.name(), val);
		}
	};

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

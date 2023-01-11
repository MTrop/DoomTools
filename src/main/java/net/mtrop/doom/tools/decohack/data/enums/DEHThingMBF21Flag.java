/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data.enums;

import java.util.Map;

import net.mtrop.doom.tools.struct.util.EnumUtils;

/**
 * MBF21 Thing Flags.
 * @author Matthew Tropiano
 * @author Xaser Acheron
 */
public enum DEHThingMBF21Flag implements DEHFlag
{
	LOGRAV         (0x00000001, "Thing has less gravity applied (1/8th)."),
	SHORTMRANGE    (0x00000002, "Thing has shortened missile range (Archvile special)."),
	DMGIGNORED     (0x00000004, "Thing's attacks are ignored by others and don't cause infighting (Archvile special)."),
	NORADIUSDMG    (0x00000008, "Thing is immune to splash damage."),
	FORCERADIUSDMG (0x00000010, "Thing causes splash damage that cannot be ignored by things with NORADIUSDMG set."),
	HIGHERMPROB    (0x00000020, "Thing has raised missile attack probability (Cyberdemon special)."),
	RANGEHALF      (0x00000040, "Thing has raised missile attack probability at half-distance."),
	NOTHRESHOLD    (0x00000080, "Thing has no targeting threshold (\"quick to retaliate\" in ZDoom)."),
	LONGMELEE      (0x00000100, "Thing has longer melee range (Revenant special)."),
	BOSS           (0x00000200, "Thing has NORADIUSDMG + FULLVOLSOUNDS."),
	MAP07BOSS1     (0x00000400, "Thing uses MAP07 MT_FATSO (Mancubus) special logic if it calls A_BossDeath."),
	MAP07BOSS2     (0x00000800, "Thing uses MAP07 MT_BABY (Arachnotron) special logic if it calls A_BossDeath."),
	E1M8BOSS       (0x00001000, "Thing uses E1M8 MT_BRUISER (Baron) special logic if it calls A_BossDeath."),
	E2M8BOSS       (0x00002000, "Thing uses E2M8 MT_CYBORG (Cyberdemon) special logic if it calls A_BossDeath."),
	E3M8BOSS       (0x00004000, "Thing uses E2M8 MT_SPIDER (Spider Mastermind) special logic if it calls A_BossDeath."),
	E4M6BOSS       (0x00008000, "Thing uses E4M6 MT_CYBORG (Cyberdemon) special logic if it calls A_BossDeath."),
	E4M8BOSS       (0x00010000, "Thing uses E4M8 MT_SPIDER (Spider Mastermind) special logic if it calls A_BossDeath."),
	RIP            (0x00020000, "Thing can collide with multiple actors (does not die on impact with them)."),
	FULLVOLSOUNDS  (0x00040000, "Thing plays its SIGHT and DEATH sounds at full volume (no attenuation)."),
	;

	private static final Map<String, DEHThingMBF21Flag> MNEMONIC_MAP = EnumUtils.createCaseInsensitiveNameMap(DEHThingMBF21Flag.class);

	public static DEHThingMBF21Flag getByMnemonic(String mnemonic)
	{
		return MNEMONIC_MAP.get(mnemonic);
	}
	
	private int value;
	private String usage;

	private DEHThingMBF21Flag(int value, String usage)
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

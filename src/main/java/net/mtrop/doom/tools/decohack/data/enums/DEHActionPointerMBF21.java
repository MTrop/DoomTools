/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data.enums;

import net.mtrop.doom.tools.decohack.data.DEHActionPointer;
import net.mtrop.doom.tools.struct.util.EnumUtils;

import static net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerParamType.*;

import java.util.Map;

/**
 * Enumeration of action pointers for frames.
 * NOTE: KEEP THIS ORDER SORTED IN THIS WAY! It is used as breaking categories for the pointer dumper!
 * @author Matthew Tropiano
 * @author Xaser Acheron
 */
public enum DEHActionPointerMBF21 implements DEHActionPointer
{
	// MBF21 Thing Action Pointers
	SPAWNOBJECT         (false, "SpawnObject",         THING, ANGLEFIXED, FIXED, FIXED, FIXED, FIXED, FIXED, FIXED),
	MONSTERPROJECTILE   (false, "MonsterProjectile",   THING, ANGLEFIXED, ANGLEFIXED, FIXED, FIXED),
	MONSTERBULLETATTACK (false, "MonsterBulletAttack", ANGLEFIXED, ANGLEFIXED, UINT, USHORT, UINT),
	MONSTERMELEEATTACK  (false, "MonsterMeleeAttack",  USHORT, UINT, SOUND, INT),
	RADIUSDAMAGE        (false, "RadiusDamage",        UINT, UINT),
	NOISEALERT          (false, "NoiseAlert"),
	HEALCHASE           (false, "HealChase",           STATE, SOUND),
	SEEKTRACER          (false, "SeekTracer",          ANGLEFIXED, ANGLEFIXED),
	FINDTRACER          (false, "FindTracer",          ANGLEFIXED, UINT),
	CLEARTRACER         (false, "ClearTracer"),
	JUMPIFHEALTHBELOW   (false, "JumpIfHealthBelow",   STATE, INT),
	JUMPIFTARGETINSIGHT (false, "JumpIfTargetInSight", STATE, ANGLEFIXED),
	JUMPIFTARGETCLOSER  (false, "JumpIfTargetCloser",  STATE, FIXED),
	JUMPIFTRACERINSIGHT (false, "JumpIfTracerInSight", STATE, ANGLEFIXED),
	JUMPIFTRACERCLOSER  (false, "JumpIfTracerCloser",  STATE, FIXED),
	JUMPIFFLAGSSET      (false, "JumpIfFlagsSet",      STATE, FLAGS, FLAGS),
	ADDFLAGS            (false, "AddFlags",            FLAGS, FLAGS),
	REMOVEFLAGS         (false, "RemoveFlags",         FLAGS, FLAGS),

	// MBF21 Weapon Action Pointers
	WEAPONPROJECTILE    (true,  "WeaponProjectile",    THING, ANGLEFIXED, ANGLEFIXED, FIXED, FIXED),
	WEAPONBULLETATTACK  (true,  "WeaponBulletAttack",  ANGLEFIXED, ANGLEFIXED, UINT, USHORT, UINT),
	WEAPONMELEEATTACK   (true,  "WeaponMeleeAttack",   USHORT, UINT, FIXED, SOUND, FIXED),
	WEAPONSOUND         (true,  "WeaponSound",         SOUND, BOOL),
	WEAPONALERT         (true,  "WeaponAlert"),
	WEAPONJUMP          (true,  "WeaponJump",          STATE, UINT),
	CONSUMEAMMO         (true,  "ConsumeAmmo",         SHORT),
	CHECKAMMO           (true,  "CheckAmmo",           STATE, USHORT),
	REFIRETO            (true,  "RefireTo",            STATE, BOOL),
	GUNFLASHTO          (true,  "GunFlashTo",          STATE, BOOL);
	
	/** Is weapon pointer. */
	private boolean weapon;
	/** Mnemonic name for BEX/DECORATE. */
	private String mnemonic;
	/** Action pointer parameters. */
	private DEHActionPointerParamType[] params;

	private DEHActionPointerMBF21(boolean weapon, String mnemonic, DEHActionPointerParamType ... params)
	{
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

}

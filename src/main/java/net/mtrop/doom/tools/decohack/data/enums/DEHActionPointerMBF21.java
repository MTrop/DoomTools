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
	// TODO: Finish docs!!

	// MBF21 Thing Action Pointers
	SPAWNOBJECT         (false, "SpawnObject",         params(THING, ANGLEFIXED, FIXED, FIXED, FIXED, FIXED, FIXED, FIXED), usage()),
	MONSTERPROJECTILE   (false, "MonsterProjectile",   params(THING, ANGLEFIXED, ANGLEFIXED, FIXED, FIXED), usage()),
	MONSTERBULLETATTACK (false, "MonsterBulletAttack", params(ANGLEFIXED, ANGLEFIXED, UINT, USHORT, UINT), usage()),
	MONSTERMELEEATTACK  (false, "MonsterMeleeAttack",  params(USHORT, UINT, SOUND, INT), usage()),
	RADIUSDAMAGE        (false, "RadiusDamage",        params(UINT, UINT), usage()),
	NOISEALERT          (false, "NoiseAlert",          params(), usage()),
	HEALCHASE           (false, "HealChase",           params(STATE, SOUND), usage()),
	SEEKTRACER          (false, "SeekTracer",          params(ANGLEFIXED, ANGLEFIXED), usage()),
	FINDTRACER          (false, "FindTracer",          params(ANGLEFIXED, UINT), usage()),
	CLEARTRACER         (false, "ClearTracer",         params(), usage()),
	JUMPIFHEALTHBELOW   (false, "JumpIfHealthBelow",   params(STATE, INT), usage()),
	JUMPIFTARGETINSIGHT (false, "JumpIfTargetInSight", params(STATE, ANGLEFIXED), usage()),
	JUMPIFTARGETCLOSER  (false, "JumpIfTargetCloser",  params(STATE, FIXED), usage()),
	JUMPIFTRACERINSIGHT (false, "JumpIfTracerInSight", params(STATE, ANGLEFIXED), usage()),
	JUMPIFTRACERCLOSER  (false, "JumpIfTracerCloser",  params(STATE, FIXED), usage()),
	JUMPIFFLAGSSET      (false, "JumpIfFlagsSet",      params(STATE, FLAGS, FLAGS), usage()),
	ADDFLAGS            (false, "AddFlags",            params(FLAGS, FLAGS), usage()),
	REMOVEFLAGS         (false, "RemoveFlags",         params(FLAGS, FLAGS), usage()),

	// MBF21 Weapon Action Pointers
	WEAPONPROJECTILE    (true,  "WeaponProjectile",    params(THING, ANGLEFIXED, ANGLEFIXED, FIXED, FIXED), usage()),
	WEAPONBULLETATTACK  (true,  "WeaponBulletAttack",  params(ANGLEFIXED, ANGLEFIXED, UINT, USHORT, UINT), usage()),
	WEAPONMELEEATTACK   (true,  "WeaponMeleeAttack",   params(USHORT, UINT, FIXED, SOUND, FIXED), usage()),
	WEAPONSOUND         (true,  "WeaponSound",         params(SOUND, BOOL), usage()),
	WEAPONALERT         (true,  "WeaponAlert",         params(), usage()),
	WEAPONJUMP          (true,  "WeaponJump",          params(STATE, UINT), usage()),
	CONSUMEAMMO         (true,  "ConsumeAmmo",         params(SHORT), usage()),
	CHECKAMMO           (true,  "CheckAmmo",           params(STATE, USHORT), usage()),
	REFIRETO            (true,  "RefireTo",            params(STATE, BOOL), usage()),
	GUNFLASHTO          (true,  "GunFlashTo",          params(STATE, BOOL), usage());
	
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

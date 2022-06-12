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
	// TODO: Finish docs!!

	// MBF Thing Action Pointers
	DETONATE        ("Detonate", usage(
		"[TODO] ADD ME."
	)),
	
	MUSHROOM        ("Mushroom",   params(ANGLEFIXED, FIXED), usage(
		"[TODO] ADD ME."
	)),
	
	SPAWN           ("Spawn",      params(THING, FIXED), usage(
		"[TODO] ADD ME."
	)),
	
	TURN            ("Turn",       params(ANGLEINT), usage(
		"[TODO] ADD ME."
	)),
	
	FACE            ("Face",       params(ANGLEUINT), usage(
		"[TODO] ADD ME."
	)),
	
	SCRATCH         ("Scratch",    params(SHORT, SOUND), usage(
		"[TODO] ADD ME."
	)),
	
	PLAYSOUND       ("PlaySound",  params(SOUND, BOOL), usage(
		"[TODO] ADD ME."
	)),
	
	RANDOMJUMP      ("RandomJump", params(STATE, UINT), usage(
		"[TODO] ADD ME."
	)),
	
	LINEEFFECT      ("LineEffect", params(SHORT, SHORT), usage(
		"[TODO] ADD ME."
	)),
	
	DIE             ("Die", usage(
		"[TODO] ADD ME."
	)),
	
	FIREOLDBFG      ("FireOldBFG", usage(
		"[TODO] ADD ME."
	)),
	
	BETASKULLATTACK ("BetaSkullAttack", usage(
		"[TODO] ADD ME."
	)),
	
	STOP            ("Stop", usage(
		"[TODO] ADD ME."
	));
	
	private static final Map<String, DEHActionPointerMBF> MNEMONIC_MAP = EnumUtils.createCaseInsensitiveNameMap(DEHActionPointerMBF.class);

	/** Function usage. */
	private Usage usage;
	/** Mnemonic name for BEX/DECORATE. */
	private String mnemonic;
	/** Action pointer parameters. */
	private DEHActionPointerParamType[] params;

	private DEHActionPointerMBF(String mnemonic, Usage usage)
	{
		this.usage = usage;
		this.mnemonic = mnemonic;
		this.params = params();
	}

	private DEHActionPointerMBF(String mnemonic, DEHActionPointerParamType[] params, Usage usage)
	{
		this.usage = usage;
		this.mnemonic = mnemonic;
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
		return false;
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

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
public enum DEHActionPointerMBF implements DEHActionPointer
{
	// MBF Thing Action Pointers
	DETONATE        ("Detonate"),
	MUSHROOM        ("Mushroom",   ANGLEFIXED, FIXED),
	SPAWN           ("Spawn",      THING, FIXED),
	TURN            ("Turn",       ANGLEINT),
	FACE            ("Face",       ANGLEUINT),
	SCRATCH         ("Scratch",    SHORT, SOUND),
	PLAYSOUND       ("PlaySound",  SOUND, BOOL),
	RANDOMJUMP      ("RandomJump", STATE, UINT),
	LINEEFFECT      ("LineEffect", SHORT, SHORT),
	DIE             ("Die"),
	FIREOLDBFG      ("FireOldBFG"),
	BETASKULLATTACK ("BetaSkullAttack"),
	STOP            ("Stop");
	
	/** Mnemonic name for BEX/DECORATE. */
	private String mnemonic;
	/** Action pointer parameters. */
	private DEHActionPointerParamType[] params;

	private DEHActionPointerMBF(String mnemonic, DEHActionPointerParamType ... params)
	{
		this.mnemonic = mnemonic;
		this.params = params;
	}

	private static final Map<String, DEHActionPointerDoom19> MNEMONIC_MAP = EnumUtils.createCaseInsensitiveNameMap(DEHActionPointerDoom19.class);
	
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

}

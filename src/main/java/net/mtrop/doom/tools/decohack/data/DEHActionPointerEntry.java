/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerParamType;
import net.mtrop.doom.tools.decohack.data.enums.DEHActionPointerType;

/**
 * Enumeration of action pointers for frames.
 * @author Matthew Tropiano
 */
public class DEHActionPointerEntry implements DEHActionPointer
{
	/** Is weapon pointer. */
	private boolean weapon;
	/** Mnemonic name for BEX/DECORATE. */
	private String mnemonic;
	/** Action pointer type. */
	private DEHActionPointerType type;
	/** Action pointer parameters. */
	private DEHActionPointerParamType[] params;

	public DEHActionPointerEntry(boolean weapon, DEHActionPointerType type, String mnemonic, DEHActionPointerParamType ... params)
	{
		this.weapon = weapon;
		this.mnemonic = mnemonic;
		this.type = type;
		this.params = params;
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
		return type;
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

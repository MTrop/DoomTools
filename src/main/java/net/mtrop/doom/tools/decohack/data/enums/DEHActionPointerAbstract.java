/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data.enums;

/**
 * Enumeration of action pointers for frames.
 * NOTE: KEEP THIS ORDER SORTED IN THIS WAY! It is used as breaking categories for the pointer dumper!
 * @author Matthew Tropiano
 */
public abstract class DEHActionPointerAbstract implements DEHActionPointer
{
	/** Originating frame (for DEH 3.0 format 19). */
	private int frame;
	/** Is weapon pointer. */
	private boolean weapon;
	/** Mnemonic name for BEX/DECORATE. */
	private String mnemonic;
	/** Action pointer type. */
	private DEHActionPointerType type;
	/** Action pointer parameters. */
	private DEHActionPointerParam[] params;

	protected DEHActionPointerAbstract(int frame, String mnemonic)
	{
		this(frame, false, DEHActionPointerType.DOOM19, mnemonic, new DEHActionPointerParam[0]);
	}

	protected DEHActionPointerAbstract(int frame, boolean weapon, String mnemonic)
	{
		this(frame, weapon, DEHActionPointerType.DOOM19, mnemonic, new DEHActionPointerParam[0]);
	}

	protected DEHActionPointerAbstract(int frame, boolean weapon, DEHActionPointerType type, String mnemonic)
	{
		this(frame, weapon, type, mnemonic, new DEHActionPointerParam[0]);
	}

	protected DEHActionPointerAbstract(int frame, boolean weapon, DEHActionPointerType type, String mnemonic, DEHActionPointerParam ... params)
	{
		this.frame = frame;
		this.weapon = weapon;
		this.mnemonic = mnemonic;
		this.type = type;
		this.params = params;
	}

	@Override
	public int getFrame() 
	{
		return frame;
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
	public DEHActionPointerParam[] getParams()
	{
		return params;
	}

	@Override
	public DEHActionPointerParam getParam(int index)
	{
		return index < 0 || index >= params.length ? null : params[index];
	}

}

/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data.enums;

import java.util.Map;

import net.mtrop.doom.tools.struct.util.EnumUtils;

/**
 * Enumeration of action pointer/property parameter types.
 * @author Xaser Acheron
 * @author Matthew Tropiano
 */
public enum DEHValueType
{
	BOOL          (Type.INTEGER,       0, 1),
	UBYTE         (Type.INTEGER,       0, 255),
	BYTE          (Type.INTEGER,       -128, 127),
	SHORT         (Type.INTEGER,       -32768, 32767),
	USHORT        (Type.INTEGER,       0, 65535),
	INT           (Type.INTEGER,       Integer.MIN_VALUE, Integer.MAX_VALUE),
	UINT          (Type.INTEGER,       0, Integer.MAX_VALUE),
	ANGLEINT      (Type.INTEGER,       -359, 359),
	ANGLEUINT     (Type.INTEGER,       0, 359),
	ANGLEFIXED    (Type.FIXED,         (-360 << 16) + 1, (360 << 16) - 1),
	FIXED         (Type.FIXED,         Integer.MIN_VALUE, Integer.MAX_VALUE),
	STATE         (Type.STATE,         0, Integer.MAX_VALUE),
	THING         (Type.THING,         0, Integer.MAX_VALUE),
	THINGMISSILE  (Type.THINGMISSILE,  0, Integer.MAX_VALUE),
	WEAPON        (Type.WEAPON,        0, Integer.MAX_VALUE),
	SOUND         (Type.SOUND,         0, Integer.MAX_VALUE),
	FLAGS         (Type.FLAGS,         Integer.MIN_VALUE, Integer.MAX_VALUE),

	// For custom properties ONLY.
	STRING        (Type.STRING),
	;

	public enum Type
	{
		INTEGER,
		FIXED,
		FLAGS,
		STATE,
		THING,
		THINGMISSILE,
		WEAPON,
		SOUND,
		STRING
	}
	
	private Type typeCheck;
	private int valueMin;
	private int valueMax;

	private static final Map<String, DEHValueType> NAME_MAP = EnumUtils.createCaseInsensitiveNameMap(DEHValueType.class);
	
	public static DEHValueType getByName(String mnemonic)
	{
		return NAME_MAP.get(mnemonic);
	}
	
	private DEHValueType(Type typeCheck)
	{
		this.typeCheck = typeCheck;
		this.valueMin = 0;
		this.valueMax = 0;
	}

	private DEHValueType(Type typeCheck, int valueMin, int valueMax)
	{
		this.typeCheck = typeCheck;
		this.valueMin = valueMin;
		this.valueMax = valueMax;
	}

	public Type getTypeCheck() 
	{
		return typeCheck;
	}
	
	public int getValueMin()
	{
		return valueMin;
	}

	public int getValueMax()
	{
		return valueMax;
	}

	public boolean isValueCheckable()
	{
		return typeCheck != Type.STRING;
	}
	
	public boolean isValueValid(int value)
	{
		return value >= valueMin && value <= valueMax;
	}
}

/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano & Xaser Acheron
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data.enums;

/**
 * Enumeration of action pointer parameter types.
 * @author Xaser Acheron
 */
public enum DEHActionPointerParam
{
	NONE        (null,            false, 0, 0),
	BOOL        (null,            false, 0, 1),
	BYTE        (null,            false, 0, 255),
	SHORT       (null,            false, -32767, 32767),
	INT         (null,            false, Integer.MIN_VALUE, Integer.MAX_VALUE),
	UINT        (null,            false, 0, Integer.MAX_VALUE),
	ANGLE_INT   (null,            false, -359, 359),
	ANGLE_UINT  (null,            false, 0, 359),
	ANGLE_FIXED (null,            true,  (-360 << 16) + 1, (360 << 16) - 1),
	FIXED       (null,            true,  Integer.MIN_VALUE, Integer.MAX_VALUE),
	STATE       (TypeGuess.STATE, false, 0, Integer.MAX_VALUE),
	THING       (TypeGuess.THING, false, 0, Integer.MAX_VALUE),
	SOUND       (TypeGuess.SOUND, false, 0, Integer.MAX_VALUE),
	FLAGS       (TypeGuess.FLAGS, false, Integer.MIN_VALUE, Integer.MAX_VALUE),
	;

	public enum TypeGuess
	{
		STATE,
		THING,
		SOUND,
		FLAGS
	}
	
	private TypeGuess typeGuess;
	private boolean fixed;
	private int valueMin;
	private int valueMax;

	private DEHActionPointerParam(int valueMin, int valueMax)
	{
		this(null, false, valueMin, valueMax);
	}

	private DEHActionPointerParam(TypeGuess typeGuess, boolean fixed, int valueMin, int valueMax)
	{
		this.typeGuess = typeGuess;
		this.fixed = fixed;
		this.valueMin = valueMin;
		this.valueMax = valueMax;
	}

	public TypeGuess getTypeGuess() 
	{
		return typeGuess;
	}
	
	public boolean isFixed() 
	{
		return fixed;
	}

	public int getValueMin()
	{
		return valueMin;
	}

	public int getValueMax()
	{
		return valueMax;
	}

	public boolean isValueValid(int value)
	{
		return value >= valueMin && value <= valueMax;
	}
}

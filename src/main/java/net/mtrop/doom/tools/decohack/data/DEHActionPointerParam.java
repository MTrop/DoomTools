/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano & Xaser Acheron
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.decohack.data;

/**
 * Enumeration of action pointer parameter types.
 * @author Xaser Acheron
 */
public enum DEHActionPointerParam
{
	NONE(0, 0),
	BOOL(0, 1),
	BYTE(0, 255),
	SHORT(-32767, 32767),
	INT(Integer.MIN_VALUE, Integer.MAX_VALUE),
	UINT(0, Integer.MAX_VALUE),
	ANGLE_INT(-359, 359),
	ANGLE_UINT(0, 359),
	ANGLE_FIXED((-360 << 16) + 1, (360 << 16) - 1),
	;

	private int valueMin;
	private int valueMax;

	private DEHActionPointerParam(int valueMin, int valueMax)
	{
		this.valueMin = valueMin;
		this.valueMax = valueMax;
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

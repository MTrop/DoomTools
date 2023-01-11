/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.common;

import net.mtrop.doom.tools.struct.util.FileUtils;

public final class CommonTest 
{
	public static void main(String[] args) 
	{
		System.out.println(FileUtils.createTempFile());
	}

}

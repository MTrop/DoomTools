/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct;

public final class ProcessCallableTest 
{
	public static void main(String[] args) throws Exception 
	{
		ProcessCallable sort = ProcessCallable.shell("sort").inheritOut();
		ProcessCallable dir = ProcessCallable.shell("dir", "/b").pipeOutTo(sort);
		System.out.println(dir.call());
	}
}

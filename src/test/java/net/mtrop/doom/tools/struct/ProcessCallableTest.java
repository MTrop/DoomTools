/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import net.mtrop.doom.tools.struct.ProcessCallable.BufferedRing;

public final class ProcessCallableTest 
{
	public static void main(String[] args) throws Exception 
	{
		ProcessCallable sort = ProcessCallable.shell("sort").inheritOut();
		ProcessCallable dir = ProcessCallable.shell("dir", "/b").pipeOutTo(sort);
		System.out.println(dir.call());

		/*
		BufferedRing ring = new BufferedRing(16);
		OutputStream out = ring.getOutputStream();
		InputStream in = ring.getInputStream();
		
		PrintWriter pw = new PrintWriter(out, true);
		pw.println("Hello World, this is Matt.");
		pw.println("This is a test.");
		pw.close();

		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		System.out.println(br.readLine());
		System.out.println(br.readLine());
		System.out.println(br.readLine());
		*/
	}
}

/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;

public final class TestReplacerReader 
{
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException 
	{
		ReplacerReader reader = new ReplacerReader("This is a {{string}} {{date}d}}}.", "{{", "}}")
			.replace("string", "date")
			.replace("date}d", ()->new Date().toString())
		;
		StringWriter writer = new StringWriter();
		
		int c;
		while ((c = reader.read()) >= 0)
			writer.append((char)c);
		
		reader.close();
		System.out.println(writer.toString());
	}
}

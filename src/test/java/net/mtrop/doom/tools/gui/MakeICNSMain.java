/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.mtrop.doom.struct.io.SerialWriter;
import net.mtrop.doom.tools.struct.util.IOUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

/**
 * I can't believe I have to write this garbage.
 * Screw you, literally every image program out there.
 * @author Matthew Tropiano
 */
public final class MakeICNSMain 
{
	private static final Charset ASCII = Charset.forName("ASCII");
	
	private static final byte[] MAGIC_HEADER = "icns".getBytes(ASCII);

	// PNG-friendly formats
	private static final byte[] TYPE_16  = "icp4".getBytes(ASCII);
	private static final byte[] TYPE_32  = "icp5".getBytes(ASCII);
	private static final byte[] TYPE_48  = "icp6".getBytes(ASCII);
	private static final byte[] TYPE_128 = "ic07".getBytes(ASCII);
	
	private static void writeHeader(OutputStream out, long lengthUnsigned) throws IOException
	{
		SerialWriter sw = new SerialWriter(SerialWriter.BIG_ENDIAN);
		out.write(MAGIC_HEADER);
		sw.writeUnsignedInteger(out, lengthUnsigned);
	}

	private static void writeIconChunk(OutputStream out, byte[] type, File pngFile) throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream((int)pngFile.length());
		
		try (FileInputStream fis = new FileInputStream(pngFile))
		{
			IOUtils.relay(fis, bos);
		}
		
		SerialWriter sw = new SerialWriter(SerialWriter.BIG_ENDIAN);
		out.write(type);
		sw.writeUnsignedInteger(out, bos.size() + 8L);
		out.write(bos.toByteArray());
	}
	
	public static void main(String[] args) throws IOException
	{
		ByteArrayOutputStream headerOut = new ByteArrayOutputStream();
		ByteArrayOutputStream iconsOut = new ByteArrayOutputStream();

		final List<Map.Entry<File, byte[]>> images = Arrays.asList(
			ObjectUtils.keyValue(new File("src/main/resources/gui/images/doomtools-logo-16.png"),  TYPE_16),
			ObjectUtils.keyValue(new File("src/main/resources/gui/images/doomtools-logo-32.png"),  TYPE_32),
			ObjectUtils.keyValue(new File("src/main/resources/gui/images/doomtools-logo-48.png"),  TYPE_48),
			ObjectUtils.keyValue(new File("src/main/resources/gui/images/doomtools-logo-128.png"), TYPE_128)
		);
		
		for (Map.Entry<File, byte[]> entry : images)
		{
			writeIconChunk(iconsOut, entry.getValue(), entry.getKey());
		}
		
		writeHeader(headerOut, iconsOut.size());
		
		try (FileOutputStream fos = new FileOutputStream(new File("images/doomtools-logo.icns")))
		{
			fos.write(headerOut.toByteArray());
			fos.write(iconsOut.toByteArray());
		}
		
		System.out.println("DONE");
	}
}

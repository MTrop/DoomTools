/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.gui.swing.panels;

import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringWriter;

import javax.swing.JTextArea;

import net.mtrop.doom.tools.struct.util.IOUtils;

/**
 * Text output panel.
 * This panel also provides two streams for writing to the text panel like a console.
 * Both streams are synchronized such that output does not step on each other.
 * @author Matthew Tropiano
 */
public class DoomToolsTextOutputPanel extends JTextArea
{
	private static final long serialVersionUID = -1405465151452714437L;

	private static final Font DEFAULT_FONT = new Font("Monospaced", Font.PLAIN, 12);
	
	private Object printMutex;
	
	/**
	 * Creates a new output panel.
	 */
	public DoomToolsTextOutputPanel()
	{
		super(25, 84);
		this.printMutex = new Object();
		setFont(DEFAULT_FONT);
		setEditable(false);
	}

	/**
	 * Writes a character to this panel.
	 * @param c the character.
	 */
	public void writeChar(char c)
	{
		synchronized (printMutex) 
		{
			append(String.valueOf(c));
			setCaretPosition(getDocument().getLength());
		}
	}
	
	/**
	 * Writes a string to this panel.
	 * @param str the string.
	 */
	public void writeString(String str)
	{
		synchronized (printMutex) 
		{
			append(str);
			setCaretPosition(getDocument().getLength());
		}
	}
	
	/**
	 * @return an output stream to use for printing to the text area.
	 */
	public OutputStream getOutputStream()
	{
		return new Printer();
	}
	
	/**
	 * @return an error stream to use for printing to the text area.
	 */
	public OutputStream getErrorStream()
	{
		return new Printer();
	}
	
	/**
	 * @return a print stream to use for printing to the text area.
	 */
	public PrintStream getPrintStream()
	{
		return new PrintStream(new Printer()); // do not enable flush - Printer auto-flushes.
	}
	
	/**
	 * @return a print stream to use for printing to the text area (error stream).
	 */
	public PrintStream getErrorPrintStream()
	{
		return new PrintStream(new Printer()); // do not enable flush - Printer auto-flushes.
	}
	
	private class Printer extends OutputStream
	{
		private ByteArrayOutputStream buffer;
		private StringWriter charBuffer;
		
		public Printer()
		{
			this.buffer = new ByteArrayOutputStream(512);
			this.charBuffer = new StringWriter(512);
		}
		
		@Override
		public void close() throws IOException
		{
			flush();
		}
		
		@Override
		public void flush() throws IOException 
		{
			if (buffer.size() == 0)
				return;
			
			// TODO: Creates far too much garbage on flush. Write better in-place solution.
			try (Reader reader = new InputStreamReader(new ByteArrayInputStream(buffer.toByteArray())))
			{
				charBuffer.getBuffer().delete(0, charBuffer.getBuffer().length());
				IOUtils.relay(reader, charBuffer);
			}
			writeString(charBuffer.toString());
			buffer.reset();
		}
		
		@Override
		public void write(int b) throws IOException 
		{
			buffer.write(b);
			if (b == '\n')
				flush();
		}
		
	}
	
}

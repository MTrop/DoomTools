/*******************************************************************************
 * Copyright (c) 2025 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * A simple CSV writer.
 * A table of comma-separated cells.
 */
public class CSVWriter implements AutoCloseable 
{
	/** The underlying writer. */
	private Writer writer;

	/**
	 * Creates a new CSV writer using an open writer.
	 * @param writer the writer to write to.
	 */
	public CSVWriter(Writer writer)
	{
		this.writer = writer; 
	}
	
	/**
	 * Creates a new CSV writer using an output stream and encoding.
	 * @param out the output stream.
	 * @param cs the character encoding.
	 */
	public CSVWriter(OutputStream out, Charset cs)
	{
		this(new BufferedWriter(new OutputStreamWriter(out, cs))); 
	}
	
	/**
	 * Creates a new CSV writer using an output stream and default encoding.
	 * @param out the output stream.
	 */
	public CSVWriter(OutputStream out)
	{
		this(out, Charset.defaultCharset()); 
	}
	
	/**
	 * Creates a new CSV writer using an output file and encoding.
	 * @param file the output file.
	 * @param append if true, append to an existing file.
	 * @param cs the character encoding.
	 * @throws FileNotFoundException if appending to a file, and the file could not be found, or the file is a directory.
	 * @throws SecurityException if the file could not be created due to an OS constraint.
	 */
	public CSVWriter(File file, boolean append, Charset cs) throws FileNotFoundException
	{
		this(new FileOutputStream(file, append), cs); 
	}
	
	/**
	 * Creates a new CSV writer using a new output file and encoding.
	 * @param file the output file.
	 * @param cs the character encoding.
	 * @throws FileNotFoundException if attempting to write to a directory.
	 * @throws SecurityException if the file could not be created due to an OS constraint.
	 */
	public CSVWriter(File file, Charset cs) throws FileNotFoundException
	{
		this(file, false, cs); 
	}
	
	/**
	 * Creates a new CSV writer using an output file and encoding.
	 * @param file the output file.
	 * @param append if true, append to an existing file.
	 * @throws FileNotFoundException if appending to a file, and the file could not be found, or the file is a directory.
	 * @throws SecurityException if the file could not be created due to an OS constraint.
	 */
	public CSVWriter(File file, boolean append) throws FileNotFoundException
	{
		this(file, append, Charset.defaultCharset()); 
	}
	
	/**
	 * Creates a new CSV writer using a new output file and default encoding.
	 * @param file the output file.
	 * @throws FileNotFoundException if attempting to write to a directory.
	 * @throws SecurityException if the file could not be created due to an OS constraint.
	 */
	public CSVWriter(File file) throws FileNotFoundException
	{
		this(file, false); 
	}
	
	/**
	 * Creates a new CSV writer using an output file and encoding.
	 * @param filePath the output file path.
	 * @param append if true, append to an existing file.
	 * @param cs the character encoding.
	 * @throws FileNotFoundException if appending to a file, and the file could not be found, or the file is a directory.
	 * @throws SecurityException if the file could not be created due to an OS constraint.
	 */
	public CSVWriter(String filePath, boolean append, Charset cs) throws FileNotFoundException
	{
		this(new File(filePath), append, cs);  
	}
	
	/**
	 * Creates a new CSV writer using a new output file and encoding.
	 * @param filePath the output file path.
	 * @param cs the character encoding.
	 * @throws FileNotFoundException if attempting to write to a directory.
	 * @throws SecurityException if the file could not be created due to an OS constraint.
	 */
	public CSVWriter(String filePath, Charset cs) throws FileNotFoundException
	{
		this(filePath, false, cs);  
	}
	
	/**
	 * Creates a new CSV writer using an output file and encoding.
	 * @param filePath the output file path.
	 * @param append if true, append to an existing file.
	 * @throws FileNotFoundException if appending to a file, and the file could not be found, or the file is a directory.
	 * @throws SecurityException if the file could not be created due to an OS constraint.
	 */
	public CSVWriter(String filePath, boolean append) throws FileNotFoundException
	{
		this(filePath, append, Charset.defaultCharset());  
	}
	
	/**
	 * Creates a new CSV writer using a new output file and default encoding.
	 * @param filePath the output file path.
	 * @throws FileNotFoundException if attempting to write to a directory.
	 * @throws SecurityException if the file could not be created due to an OS constraint.
	 */
	public CSVWriter(String filePath) throws FileNotFoundException
	{
		this(filePath, false); 
	}
	
	/**
	 * Writes a line to the writer.
	 * @param cells the the cells to write, in order from first to last.
	 * @throws IOException if an error occurred during write. 
	 */
	public void writeLine(Iterable<CharSequence> cells) throws IOException 
	{
		boolean first = false;
		for (CharSequence cs : cells)
		{
			if (first)
				writer.append(',');
			writer.append(escapeCell(cs));
			first = true;
		}
		writer.append('\n');
	}
	
	/**
	 * Writes a line to the writer.
	 * @param cells the the cells to write, in order from first to last.
	 * @throws IOException if an error occurred during write. 
	 */
	public void writeLine(CharSequence ... cells) throws IOException
	{
		writeLine(Arrays.asList(cells));
	}
	
	/**
	 * Writes a line to the writer.
	 * @param cells the the cells to write, in order from first to last.
	 * @throws IOException if an error occurred during write. 
	 */
	public void writeLine(String ... cells) throws IOException
	{
		writeLine(Arrays.asList(cells));
	}
	
	/**
	 * Potentially escapes a single cell for output into the CSV writer.
	 * @param input the string input.
	 * @return the resultant string.
	 */
	public static String escapeCell(CharSequence input)
	{
		String in = input.toString();
		boolean quoteWrap = in.indexOf(',') >= 0 || in.indexOf('"') >= 0;
		if (quoteWrap)
			in = in.replace("\"", "\"\"");
		return quoteWrap ? "\"" + in + "\"" : in;
	}
	
	@Override
	public void close() throws Exception 
	{
		writer.flush();
		writer.close();
	}

}

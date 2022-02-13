/*******************************************************************************
 * Copyright (c) 2019-2021 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Simple IO utility functions.
 * @author Matthew Tropiano
 */
public final class IOUtils
{
	/** The relay buffer size, used by relay(). */
	private static int RELAY_BUFFER_SIZE = 8192;
	/** The input wrapper used by getLine(). */
	private static BufferedReader SYSTEM_IN_READER;
	/** A null outputstream. */
	private static final OutputStream OUTPUTSTREAM_NULL = new OutputStream() 
	{
		@Override
		public void write(int b) throws IOException
		{
			// Do nothing.
		}
		
		@Override
		public void write(byte[] b) throws IOException
		{
			// Do nothing.
		}
	
		@Override
		public void write(byte[] b, int off, int len) throws IOException 
		{
			// Do nothing.
		}
	};
	
	/** A null inputstream. */
	private static final InputStream INPUTSTREAM_NULL = new InputStream() 
	{
		@Override
		public int read() throws IOException
		{
			return -1;
		}
	};
	
	/** A null file. */
	private static final File NULL_FILE = new File(System.getProperty("os.name").contains("Windows") ? "NUL" : "/dev/null");

	private IOUtils() {}
	
	/**
	 * Convenience method for
	 * <code>new BufferedReader(new InputStreamReader(in))</code>
	 * @param in the stream to read.
	 * @return an open buffered reader for the provided stream.
	 * @throws IOException if an error occurred opening the stream for reading.
	 * @throws SecurityException if you do not have permission for opening the stream.
	 */
	public static BufferedReader openTextStream(InputStream in) throws IOException
	{
		return new BufferedReader(new InputStreamReader(in));
	}

	/**
	 * Convenience method for
	 * <code>new BufferedReader(new InputStreamReader(new FileInputStream(file)))</code>
	 * @param file the file to open.
	 * @return an open buffered reader for the provided file.
	 * @throws IOException if an error occurred opening the file for reading.
	 * @throws SecurityException if you do not have permission for opening the file.
	 */
	public static BufferedReader openTextFile(File file) throws IOException
	{
		return openTextStream(new FileInputStream(file));
	}

	/**
	 * Convenience method for
	 * <code>new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))))</code>
	 * @param filePath the path of the file to open.
	 * @return an open buffered reader for the provided path.
	 * @throws IOException if an error occurred opening the file for reading.
	 * @throws SecurityException if you do not have permission for opening the file.
	 */
	public static BufferedReader openTextFile(String filePath) throws IOException
	{
		return openTextFile(new File(filePath));
	}

	/**
	 * Convenience method for
	 * <code>new BufferedReader(new InputStreamReader(System.in))</code>
	 * @return an open buffered reader for {@link System#in}.
	 * @throws IOException if an error occurred opening Standard IN.
	 * @throws SecurityException if you do not have permission for opening Standard IN.
	 */
	public static BufferedReader openSystemIn() throws IOException
	{
		return openTextStream(System.in);
	}

	/**
	 * Opens an {@link InputStream} to a resource using the current thread's {@link ClassLoader}.
	 * @param pathString the resource pathname.
	 * @return an open {@link InputStream} for reading the resource or null if not found.
	 * @see ClassLoader#getResourceAsStream(String)
	 */
	public static InputStream openResource(String pathString)
	{
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(pathString);
	}

	/**
	 * Opens an {@link InputStream} to a resource using a provided ClassLoader.
	 * @param classLoader the provided {@link ClassLoader} to use.
	 * @param pathString the resource pathname.
	 * @return an open {@link InputStream} for reading the resource or null if not found.
	 * @see ClassLoader#getResourceAsStream(String)
	 */
	public static InputStream openResource(ClassLoader classLoader, String pathString)
	{
		return classLoader.getResourceAsStream(pathString);
	}

	/**
	 * Retrieves the ASCII contents of a file.
	 * @param f		the file to use.
	 * @return		a contiguous string (including newline characters) of the file's contents.
	 * @throws FileNotFoundException	if the file cannot be found.
	 * @throws IOException				if the read cannot be done.
	 */
	public static String getASCIIContents(File f) throws IOException
	{
		FileInputStream fis = new FileInputStream(f);
		String out = getTextualContents(fis, "ASCII");
		fis.close();
		return out;
	}

	/**
	 * Retrieves the textual contents of a file in the system's current encoding.
	 * @param f	the file to use.
	 * @return		a contiguous string (including newline characters) of the file's contents.
	 * @throws IOException	if the read cannot be done.
	 */
	public static String getTextualContents(File f) throws IOException
	{
		FileInputStream fis = new FileInputStream(f);
		String out = getTextualContents(fis);
		fis.close();
		return out;
	}

	/**
	 * Retrieves the textual contents of a stream in the system's current encoding.
	 * @param in	the input stream to use.
	 * @return		a contiguous string (including newline characters) of the stream's contents.
	 * @throws IOException	if the read cannot be done.
	 */
	public static String getTextualContents(InputStream in) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		while ((line = br.readLine()) != null)
		{
			sb.append(line);
			sb.append('\n');
		}
		br.close();
		return sb.toString();
	}

	/**
	 * Retrieves the textual contents of a stream.
	 * @param in		the input stream to use.
	 * @param encoding	name of the encoding type.
	 * @return		a contiguous string (including newline characters) of the stream's contents.
	 * @throws IOException				if the read cannot be done.
	 */
	public static String getTextualContents(InputStream in, String encoding) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(in, encoding));
		String line;
		while ((line = br.readLine()) != null)
		{
			sb.append(line);
			sb.append('\n');
		}
		br.close();
		return sb.toString();
	}

	/**
	 * Retrieves the binary contents of a file.
	 * @param f		the file to use.
	 * @return		an array of the bytes that make up the file.
	 * @throws FileNotFoundException	if the file cannot be found.
	 * @throws IOException				if the read cannot be done.
	 */
	public static byte[] getBinaryContents(File f) throws IOException
	{
		FileInputStream fis = new FileInputStream(f);
		byte[] b = getBinaryContents(fis, (int)f.length());
		fis.close();
		return b;
	}

	/**
	 * Retrieves the binary contents of a stream.
	 * @param in	the input stream to use.
	 * @param len	the amount of bytes to read.
	 * @return		an array of len bytes that make up the stream.
	 * @throws IOException				if the read cannot be done.
	 */
	public static byte[] getBinaryContents(InputStream in, int len) throws IOException
	{
		byte[] b = new byte[len];
		in.read(b);
		return b;
	}

	/**
	 * Retrieves the binary contents of a stream until it hits the end of the stream.
	 * @param in	the input stream to use.
	 * @return		an array of len bytes that make up the data in the stream.
	 * @throws IOException	if the read cannot be done.
	 */
	public static byte[] getBinaryContents(InputStream in) throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		relay(in, bos);
		return bos.toByteArray();
	}

	/**
	 * Reads from an input stream, reading in a consistent set of data
	 * and writing it to the output stream. The read/write is buffered
	 * so that it does not bog down the OS's other I/O requests.
	 * This method finishes when the end of the source stream is reached.
	 * Note that this may block if the input stream is a type of stream
	 * that will block if the input stream blocks for additional input.
	 * This method is thread-safe.
	 * @param in the input stream to grab data from.
	 * @param out the output stream to write the data to.
	 * @return the total amount of bytes relayed.
	 * @throws IOException if a read or write error occurs.
	 */
	public static int relay(InputStream in, OutputStream out) throws IOException
	{
		return relay(in, out, RELAY_BUFFER_SIZE, -1);
	}

	/**
	 * Reads from an input stream, reading in a consistent set of data
	 * and writing it to the output stream. The read/write is buffered
	 * so that it does not bog down the OS's other I/O requests.
	 * This method finishes when the end of the source stream is reached.
	 * Note that this may block if the input stream is a type of stream
	 * that will block if the input stream blocks for additional input.
	 * This method is thread-safe.
	 * @param in the input stream to grab data from.
	 * @param out the output stream to write the data to.
	 * @param bufferSize the buffer size for the I/O. Must be &gt; 0.
	 * @return the total amount of bytes relayed.
	 * @throws IOException if a read or write error occurs.
	 */
	public static int relay(InputStream in, OutputStream out, int bufferSize) throws IOException
	{
		return relay(in, out, bufferSize, -1);
	}

	/**
	 * Reads from an input stream, reading in a consistent set of data
	 * and writing it to the output stream. The read/write is buffered
	 * so that it does not bog down the OS's other I/O requests.
	 * This method finishes when the end of the source stream is reached.
	 * Note that this may block if the input stream is a type of stream
	 * that will block if the input stream blocks for additional input.
	 * This method is thread-safe.
	 * @param in the input stream to grab data from.
	 * @param out the output stream to write the data to.
	 * @param bufferSize the buffer size for the I/O. Must be &gt; 0.
	 * @param maxLength the maximum amount of bytes to relay, or a value &lt; 0 for no max.
	 * @return the total amount of bytes relayed.
	 * @throws IOException if a read or write error occurs.
	 */
	public static int relay(InputStream in, OutputStream out, int bufferSize, int maxLength) throws IOException
	{
		int total = 0;
		int buf = 0;
			
		byte[] RELAY_BUFFER = new byte[bufferSize];
		
		while ((buf = in.read(RELAY_BUFFER, 0, Math.min(maxLength < 0 ? Integer.MAX_VALUE : maxLength, bufferSize))) > 0)
		{
			out.write(RELAY_BUFFER, 0, buf);
			total += buf;
			if (maxLength >= 0)
				maxLength -= buf;
		}
		return total;
	}

	/**
	 * Sets the size of the buffer in bytes for {@link #relay(InputStream, OutputStream)}.
	 * Although you may not encounter this problem, it would be unwise to set this during a call to relay().
	 * Size cannot be 0 or less.
	 * @param size the size of the relay buffer. 
	 */
	public static void setRelayBufferSize(int size)
	{
		if (size <= 0)
			throw new IllegalArgumentException("size is 0 or less.");
		RELAY_BUFFER_SIZE = size;
	}

	/**
	 * @return the size of the relay buffer for {@link #relay(InputStream, OutputStream)} in bytes.
	 */
	public static int getRelayBufferSize()
	{
		return RELAY_BUFFER_SIZE;
	}

	/**
	 * Reads a line from standard in; throws a RuntimeException
	 * if something absolutely serious happens. Should be used
	 * just for convenience.
	 * @return a single line read from Standard In.
	 * @see #openSystemIn()
	 * @see BufferedReader#readLine()
	 */
	public static String getLine()
	{
		String out = null;
		try {
			if (SYSTEM_IN_READER == null)
				SYSTEM_IN_READER = openSystemIn();
			out = SYSTEM_IN_READER.readLine();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return out;
	}

	/**
	 * @return a null output stream, where all writes are accepted and not used.
	 */
	public static PrintStream getNullPrintStream()
	{
		return new PrintStream(OUTPUTSTREAM_NULL);
	}

	/**
	 * @return a null output stream, where all writes are accepted and not used.
	 */
	public static OutputStream getNullOutputStream()
	{
		return OUTPUTSTREAM_NULL;
	}

	/**
	 * @return a null input stream, where all reads result in an end-of-stream.
	 */
	public static InputStream getNullInputStream()
	{
		return INPUTSTREAM_NULL;
	}

	/**
	 * @return a handle to the null file for this platform.
	 */
	public static File getNullFile()
	{
		return NULL_FILE;
	}

	/**
	 * Attempts to close a {@link Closeable} object.
	 * If the object is null, this does nothing.
	 * @param c the reference to the closeable object.
	 */
	public static void close(Closeable c)
	{
		if (c == null) return;
		try { c.close(); } catch (IOException e){}
	}

	/**
	 * Attempts to close an {@link AutoCloseable} object.
	 * If the object is null, this does nothing.
	 * @param c the reference to the AutoCloseable object.
	 */
	public static void close(AutoCloseable c)
	{
		if (c == null) return;
		try { c.close(); } catch (Exception e){}
	}
	
}

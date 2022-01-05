/*******************************************************************************
 * Copyright (c) 2019-2021 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct.util;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

/**
 * Simple IO utility functions.
 * @author Matthew Tropiano
 */
public final class IOUtils
{
	/** The relay buffer size, used by relay(). */
	private static int RELAY_BUFFER_SIZE = 8192;
	
	private IOUtils() {}
	
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

	/**
	 * BufferedReader to Writer stream thread.
	 * Transfers characters buffered at one line at a time until the source stream is closed.
	 * <p> The thread terminates if the reader is closed. The target writer is not closed.
	 */
	public static class LineReaderToWriterThread extends Thread
	{
		protected BufferedReader sourceReader;
		protected PrintWriter targetPrintWriter;
		protected PrintStream targetPrintStream;
		
		private Throwable exception;
		private long totalCharacters;
		
		private LineReaderToWriterThread(BufferedReader reader)
		{
			super("LineReaderToWriterThread");
			this.sourceReader = reader;
			this.exception = null;
			this.totalCharacters = 0L;
		}
		
		/**
		 * Creates a new thread that, when started, transfers characters one line at a time until the source stream is closed.
		 * The user must call {@link #start()} on this thread - it is not active after creation.
		 * @param reader the Reader to read from (wrapped as a BufferedReader).
		 * @param writer the Writer to write to.
		 */
		public LineReaderToWriterThread(Reader reader, Writer writer)
		{
			this(new BufferedReader(reader), new PrintWriter(writer));
		}

		/**
		 * Creates a new thread that, when started, transfers characters one line at a time until the source stream is closed.
		 * The user must call {@link #start()} on this thread - it is not active after creation.
		 * @param reader the BufferedReader to read from.
		 * @param writer the Writer to write to.
		 */
		public LineReaderToWriterThread(BufferedReader reader, PrintWriter writer)
		{
			this(reader);
			this.targetPrintWriter = writer;
		}

		/**
		 * Creates a new thread that, when started, transfers characters one line at a time until the source stream is closed.
		 * The user must call {@link #start()} on this thread - it is not active after creation.
		 * @param reader the BufferedReader to read from.
		 * @param stream the PrintStream to write to.
		 */
		public LineReaderToWriterThread(BufferedReader reader, PrintStream stream)
		{
			this(reader);
			this.targetPrintStream = stream;
		}

		@Override
		public final void run() 
		{
			String line;
			try {
				while ((line = sourceReader.readLine()) != null)
				{
					if (targetPrintWriter != null)
					{
						targetPrintWriter.append(line).append('\n').flush();
					}
					else
					{
						targetPrintStream.println(line);
						targetPrintStream.flush();
					}
					totalCharacters += line.length() + 1;
				}
			} catch (Throwable e) {
				exception = e;
			} finally {
				afterClose();
			}
		}
		
		/**
		 * Called after the source stream hits EOF or closes,
		 * but before the Thread terminates.
		 * <p> Does nothing by default.
		 */
		public void afterClose()
		{
			// Do nothing by default.
		}
		
		/**
		 * @return the exception that occurred, if any.
		 */
		public Throwable getException() 
		{
			return exception;
		}
		
		/**
		 * @return the total amount of characters moved.
		 */
		public long getTotalCharacters() 
		{
			return totalCharacters;
		}
		
	}
	
	/**
	 * Reader to Writer stream thread.
	 * Transfers characters until the source stream is closed.
	 * <p> The thread terminates if the reader is closed. The target writer is not closed.
	 */
	public static class ReaderToWriterThread extends Thread
	{
		protected Reader sourceReader;
		protected Writer targetWriter;
		
		private Throwable exception;
		private long totalCharacters;
		
		/**
		 * Creates a new thread that, when started, transfers characters until the source stream is closed.
		 * The user must call {@link #start()} on this thread - it is not active after creation.
		 * @param reader the Reader to read from.
		 * @param writer the Writer to write to.
		 */
		public ReaderToWriterThread(Reader reader, Writer writer)
		{
			super("ReaderToWriterThread");
			this.sourceReader = reader;
			this.targetWriter = writer;
			this.exception = null;
			this.totalCharacters = 0L;
		}

		@Override
		public final void run() 
		{
			int buf;
			char[] buffer = new char[8192]; 
			try {
				while ((buf = sourceReader.read(buffer)) > 0)
				{
					targetWriter.write(buffer, 0, buf);
					targetWriter.flush();
					totalCharacters += buf;
				}
			} catch (Throwable e) {
				exception = e;
			} finally {
				afterClose();
			}
		}
		
		/**
		 * Called after the source stream hits EOF or closes,
		 * but before the Thread terminates.
		 * <p> Does nothing by default.
		 */
		public void afterClose()
		{
			// Do nothing by default.
		}
		
		/**
		 * @return the exception that occurred, if any.
		 */
		public Throwable getException() 
		{
			return exception;
		}
		
		/**
		 * @return the total amount of characters moved.
		 */
		public long getTotalCharacters() 
		{
			return totalCharacters;
		}
		
	}
	
	/**
	 * Input to Output stream thread.
	 * Transfers until the source stream is closed.
	 * <p> The thread terminates if the input stream is closed. The target output stream is not closed.
	 */
	public static class InputToOutputStreamThread extends Thread
	{
		protected InputStream sourceStream;
		protected OutputStream targetStream;
		
		private Throwable exception;
		private long totalBytes;
		
		/**
		 * Creates a new thread that, when started, transfers bytes until the source stream is closed.
		 * The user must call {@link #start()} on this thread - it is not active after creation.
		 * @param sourceStream the InputStream to read from. 
		 * @param targetStream the OutputStream to write to.
		 */
		public InputToOutputStreamThread(InputStream sourceStream, OutputStream targetStream)
		{
			super("InputToOutputStreamThread");
			this.sourceStream = sourceStream;
			this.targetStream = targetStream;
			this.exception = null;
			this.totalBytes = 0L;
		}
		
		@Override
		public final void run() 
		{
			int buf;
			byte[] buffer = new byte[8192]; 
			try {
				while ((buf = sourceStream.read(buffer)) > 0)
				{
					targetStream.write(buffer, 0, buf);
					targetStream.flush();
					totalBytes += buf;
				}
			} catch (Throwable e) {
				exception = e;
			} finally {
				afterClose();
			}
		}
		
		/**
		 * Called after the source stream hits EOF or closes,
		 * but before the Thread terminates.
		 * <p> Does nothing by default.
		 */
		public void afterClose()
		{
			// Do nothing by default.
		}
		
		/**
		 * @return the exception that occurred, if any.
		 */
		public Throwable getException() 
		{
			return exception;
		}
		
		/**
		 * @return the total amount of bytes moved.
		 */
		public long getTotalBytes() 
		{
			return totalBytes;
		}
		
	}
	
}

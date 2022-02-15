/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.mtrop.doom.tools.struct.ProcessCallable;
import net.mtrop.doom.tools.struct.ReplacerReader;
import net.mtrop.doom.tools.struct.util.OSUtils;

/**
 * Common shared functions.
 * @author Matthew Tropiano
 */
public final class Common
{
	/** DoomTools Config folder base. */
    public static final String SETTINGS_PATH = OSUtils.getApplicationSettingsPath() + "/DoomTools/";

	/** Version number map. */
	private static final Map<String, String> VERSION_MAP = new HashMap<>();
	
    /**
	 * Gets the embedded version string for a tool name.
	 * If there is no embedded version, this returns "SNAPSHOT".
	 * @param name the name of the tool. 
	 * @return the version string or "SNAPSHOT"
	 */
	public static String getVersionString(String name)
	{
		if (VERSION_MAP.containsKey(name))
			return VERSION_MAP.get(name);
		
		String out = null;
		try (InputStream in = openResource("net/mtrop/doom/tools/" + name + ".version")) {
			if (in != null)
				VERSION_MAP.put(name, out = getTextualContents(in, "UTF-8").trim());
		} catch (IOException e) {
			/* Do nothing. */
		}
		
		return out != null ? out : "SNAPSHOT";
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
	 * Opens an {@link InputStream} to a resource using the current thread's {@link ClassLoader}.
	 * @param pathString the resource pathname.
	 * @return an open {@link InputStream} for reading the resource or null if not found.
	 * @see ClassLoader#getResourceAsStream(String)
	 */
	public static Reader openResourceReader(String pathString)
	{
		InputStream in = openResource(pathString);
		return in != null ? new InputStreamReader(in) : null;
	}

	/**
	 * Retrieves the textual contents of a stream.
	 * @param in the input stream to use.
	 * @param encoding name of the encoding type.
	 * @return a contiguous string (including newline characters) of the stream's contents.
	 * @throws IOException if the read cannot be done.
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
	 * Returns the file's name, no extension.
	 * @param filename the file name.
	 * @param extensionSeparator the text or characters that separates file name from extension.
	 * @return the file's name without extension.
	 */
	public static String getFileNameWithoutExtension(String filename, String extensionSeparator)
	{
		int extindex = filename.lastIndexOf(extensionSeparator);
		if (extindex >= 0)
			return filename.substring(0, extindex);
		return "";
	}

	/**
	 * Returns the file's name, no extension.
	 * @param filename the file name.
	 * @return the file's name without extension.
	 */
	public static String getFileNameWithoutExtension(String filename)
	{
		return getFileNameWithoutExtension(filename, ".");
	}

	/**
	 * Returns the file's name, no extension.
	 * @param file the file.
	 * @return the file's name without extension.
	 */
	public static String getFileNameWithoutExtension(File file)
	{
		return getFileNameWithoutExtension(file.getName(), ".");
	}

	/**
	 * Returns the extension of a filename.
	 * @param filename the file name.
	 * @param extensionSeparator the text or characters that separates file name from extension.
	 * @return the file's extension, or an empty string for no extension.
	 */
	public static String getFileExtension(String filename, String extensionSeparator)
	{
		int extindex = filename.lastIndexOf(extensionSeparator);
		if (extindex >= 0)
			return filename.substring(extindex+1);
		return "";
	}

	/**
	 * Returns the extension of a file's name.
	 * Assumes the separator to be ".".
	 * @param filename the file name.
	 * @return the file's extension, or an empty string for no extension.
	 */
	public static String getFileExtension(String filename)
	{
		return getFileExtension(filename, ".");
	}

	/**
	 * Returns the extension of a file's name.
	 * Assumes the separator to be ".".
	 * @param file the file.
	 * @return the file's extension, or an empty string for no extension.
	 */
	public static String getFileExtension(File file)
	{
		return getFileExtension(file.getName(), ".");
	}

	/**
	 * Gets a list of subdirectories from a top directory.
	 * @param startDirectory the starting directory.
	 * @param includeTop if true, the output includes the starting directory.
	 * @param dirFilter additional directory filter.
	 * @return an array of subdirectory paths under the top directory.
	 */
	public static File[] getSubdirectories(File startDirectory, boolean includeTop, FileFilter dirFilter)
	{
		if (!startDirectory.isDirectory())
			return null;
		
		List<File> dirs = new LinkedList<>();
		Deque<File> dirQueue = new LinkedList<>();
		dirQueue.add(startDirectory);
		
		if (includeTop)
			dirs.add(startDirectory);
		
		while (!dirQueue.isEmpty())
		{
			File dir = dirQueue.pollFirst();
			File[] files = dir.listFiles((f) -> f.isDirectory());
			for (int i = 0; i < files.length; i++)
			{
				if (dirFilter.accept(files[i]))
				{
					dirQueue.add(files[i]);
					dirs.add(files[i]);
				}
			}
		}
		
		return dirs.toArray(new File[dirs.size()]);
	}
	
	/**
	 * Joins an array of strings into one string, with a separator between them.
	 * @param separator the separator to insert between strings. Can be empty or null.
	 * @param strings the strings to join.
	 * @return a string of all joined strings and separators.
	 */
	public static String joinStrings(String separator, String... strings)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < strings.length; i++)
		{
			sb.append(strings[i]);
			if (i < strings.length - 1)
				sb.append(separator);
		}
		return sb.toString();
	}

	/**
	 * Creates the necessary directories for a file path.
	 * @param file	the abstract file path.
	 * @return		true if the paths were made (or exists), false otherwise.
	 */
	public static boolean createPathForFile(File file)
	{
		return createPathForFile(file.getAbsolutePath());
	}

	/**
	 * Creates the necessary directories for a file path.
	 * @param path	the abstract path.
	 * @return		true if the paths were made (or exists), false otherwise.
	 */
	public static boolean createPathForFile(String path)
	{
		int sindx = -1;
		
		if ((sindx = Math.max(
				path.lastIndexOf(File.separator), 
				path.lastIndexOf("/"))) != -1)
		{
			return createPath(path.substring(0, sindx));
		}
		return true;
	}

	/**
	 * Creates the necessary directories for a file path.
	 * @param path	the abstract path.
	 * @return		true if the paths were made (or exists), false otherwise.
	 */
	public static boolean createPath(String path)
	{
		File dir = new File(path);
		if (dir.exists())
			return true;
		return dir.mkdirs();
	}

	/**
	 * Cleans a directory.
	 * @param directory the directory.
	 * @param deleteTop if true, delete the directory too.
	 * @return true if everything was deleted successfully, false otherwise.
	 */
	public static boolean cleanDirectory(File directory, boolean deleteTop)
	{
		boolean out = true;
		for (File f : directory.listFiles())
		{
			if (f.isDirectory())
				out = out && cleanDirectory(f, true);
			else
				out = out && f.delete();
		}
		if (deleteTop)
			out = out && directory.delete();
		return out;
	}
	
	/**
	 * Compares two file paths for equality.
	 * If the OS is Windows, the paths are compared case-insensitively.
	 * @param a the first file.
	 * @param b the second file.
	 * @return true if the two files have the same absolute path, false otherwise.
	 */
	public static boolean filePathEquals(File a, File b)
	{
		if (a == null)
			return b == null;
		else if (b == null)
			return false;
		else if (OSUtils.isWindows())
			return a.getAbsolutePath().equalsIgnoreCase(b.getAbsolutePath());
		else
			return a.getAbsolutePath().equals(b.getAbsolutePath());
	}
	
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
	 * Checks if a value is "empty."
	 * The following is considered "empty":
	 * <ul>
	 * <li><i>Null</i> references.
	 * <li>{@link Boolean} objects that are false.
	 * <li>{@link Character} objects that are the null character ('\0', '\u0000').
	 * <li>{@link Number} objects that are zero.
	 * <li>{@link String} objects that are the empty string, or are {@link String#trim()}'ed down to the empty string.
	 * <li>{@link Collection} objects where {@link Collection#isEmpty()} returns true.
	 * </ul> 
	 * @param obj the object to check.
	 * @return true if the provided object is considered "empty", false otherwise.
	 */
	public static boolean isEmpty(Object obj)
	{
		if (obj == null)
			return true;
		else if (obj instanceof Boolean)
			return !((Boolean)obj);
		else if (obj instanceof Character)
			return ((Character)obj) == '\0';
		else if (obj instanceof Number)
			return ((Number)obj).doubleValue() == 0.0;
		else if (obj instanceof String)
			return ((String)obj).trim().length() == 0;
		else if (obj instanceof Collection<?>)
			return ((Collection<?>)obj).isEmpty();
		else
			return false;
	}

	/**
	 * Gets an element in an array or if out of bounds, null.
	 * @param <T> the object type in the array.
	 * @param arr the array.
	 * @param index the index into the array
	 * @return the element at the array index, or null.
	 */
	public static <T> T arrayElement(T[] arr, int index)
	{
		return arrayElement(arr, index, null);
	}

	/**
	 * Gets an element in an array or if out of bounds, <code>def</code>.
	 * @param <T> the object type in the array.
	 * @param arr the array.
	 * @param index the index into the array
	 * @param def the default value.
	 * @return the element at the array index, or null.
	 */
	public static <T> T arrayElement(T[] arr, int index, T def)
	{
		return index < 0 || index >= arr.length ? def : arr[index];
	}

	/**
	 * Creates a new instance of a class from a class type.
	 * This essentially calls {@link Class#getDeclaredConstructor(Class...)} with no arguments 
	 * and {@link Class#newInstance()}, but wraps the call in a try/catch block that only throws an exception if something goes wrong.
	 * @param <T> the return object type.
	 * @param clazz the class type to instantiate.
	 * @return a new instance of an object.
	 * @throws RuntimeException if instantiation cannot happen, either due to
	 * a non-existent constructor or a non-visible constructor.
	 */
	public static <T> T create(Class<T> clazz)
	{
		Object out = null;
		try {
			out = clazz.getDeclaredConstructor().newInstance();
		} catch (SecurityException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
		
		return clazz.cast(out);
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
	 * Makes a new String with escape sequences in it.
	 * @param s	the original string.
	 * @return the new one with escape sequences in it.
	 */
	public static String withEscChars(String s)
	{
		StringBuilder out = new StringBuilder();
		for (int i = 0; i < s.length(); i++)
			switch (s.charAt(i))
			{
				case '\0':
					out.append("\\0");
					break;
				case '\b':
					out.append("\\b");
					break;
				case '\t':
					out.append("\\t");
					break;
				case '\n':
					out.append("\\n");
					break;
				case '\f':
					out.append("\\f");
					break;
				case '\r':
					out.append("\\r");
					break;
				case '\\':
					out.append("\\\\");
					break;
				case '"':
					if (i != 0 && i != s.length() - 1)
						out.append("\\\"");						
					else
						out.append("\"");
					break;
				default:
					out.append(s.charAt(i));
					break;
			}
		return out.toString();
	}

	/**
	 * Prints a message out to a PrintStream, word-wrapped
	 * to a set column width (in characters). The width cannot be
	 * 1 or less or this does nothing. This will also turn any whitespace
	 * character it encounters into a single space, regardless of specialty.
	 * @param out the print stream to use. 
	 * @param startColumn the starting column.
	 * @param indent the indent line.
	 * @param width the width in characters.
	 * @param message the output message.
	 * @return the ending column for subsequent calls.
	 */
	public static int printWrapped(PrintStream out, int startColumn, int indent, int width, CharSequence message)
	{
		if (width <= 1) return startColumn;
		
		StringBuilder token = new StringBuilder();
		StringBuilder line = new StringBuilder();
		int ln = startColumn;
		int tok = 0;
		for (int j = 0; j < indent; j++)
			line.append(' ');
		for (int i = 0; i < message.length(); i++)
		{
			char c = message.charAt(i);
			if (c == '\n')
			{
				line.append(token);
				ln += token.length();
				token.delete(0, token.length());
				tok = 0;
				out.println(line.toString());
				line.delete(0, line.length());
				ln = 0;
				for (int j = 0; j < indent; j++)
					line.append(' ');
			}
			else if (Character.isWhitespace(c))
			{
				line.append(token);
				ln += token.length();
				if (ln < width-1)
				{
					line.append(' ');
					ln++;
				}
				token.delete(0, token.length());
				tok = 0;
			}
			else if (c == '-')
			{
				line.append(token);
				ln += token.length();
				line.append('-');
				ln++;
				token.delete(0, token.length());
				tok = 0;
			}
			else if (ln + token.length() + 1 > width-1)
			{
				out.println(line.toString());
				line.delete(0, line.length());
				ln = 0;
				for (int j = 0; j < indent; j++)
					line.append(' ');
				token.append(c);
				tok++;
			}
			else
			{
				token.append(c);
				tok++;
			}
		}
		
		String linestr = line.toString();
		if (line.length() > 0)
			out.print(linestr);
		if (token.length() > 0)
			out.print(token.toString());
		
		return ln + tok;
	}

	/**
	 * Creates a key-value entry.
	 * @param key the key.
	 * @param value the value.
	 * @return a new entry.
	 */
	public static <K, V> Map.Entry<K, V> keyValue(K key, V value)
	{
		KeyValue<K, V> out = new KeyValue<>();
		out.key = key;
		out.value = value;
		return out;
	}
	
	/**
	 * Creates a new immutable map.
	 * @param <K> the key type.
	 * @param <V> the value type.
	 * @param entries the entries to add.
	 * @return the new map.
	 */
	@SafeVarargs
	public static <K, V> Map<K, V> map(Map.Entry<K, V> ... entries)
	{
		Map<K, V> out = new HashMap<>();
		for (Map.Entry<K, V> e : entries)
			out.put(e.getKey(), e.getValue());
		return Collections.unmodifiableMap(out);
	}
	
	/**
	 * Replaces a series of keys in an input character sequence.
	 * <p>Each keyword is wrapped in <code>${}</code> and a map is provided that maps
	 * keyword to object to replace with (the {@link String#toString()}). You can output a
	 * <code>$</code> by doubling it up in the input character sequence. If a replace key 
	 * is not found in the provided map, the whole expression is not replaced.
	 * @param source the source characters to parse.
	 * @param replacerMap the list of replacers.
	 * @return the resultant string.
	 */
	public static String replace(CharSequence source, Map<String, ?> replacerMap)
	{
		StringBuilder sb = new StringBuilder();
		StringBuilder token = new StringBuilder();
		final int STATE_TEXT = 0;
		final int STATE_REPLACER_START = 1;
		final int STATE_REPLACER_TOKEN = 2;
		
		int state = STATE_TEXT;
		for (int i = 0; i < source.length(); i++)
		{
			char c = source.charAt(i);
			switch (state)
			{
				case STATE_TEXT:
				{
					if (c == '$')
						state = STATE_REPLACER_START;
					else
						sb.append(c);
				}
				break;
				
				case STATE_REPLACER_START:
				{
					if (c == '$')
					{
						state = STATE_TEXT;
						sb.append('$');
					}
					else if (c == '{')
					{
						state = STATE_REPLACER_TOKEN;
					}
					else
					{
						sb.append(c);
					}
				}
				break;
				
				case STATE_REPLACER_TOKEN:
				{
					if (c == '}')
					{
						state = STATE_TEXT;
						String key = token.toString();
						token.delete(0, token.length());
						Object value = replacerMap.get(key);
						if (value != null)
							sb.append(String.valueOf(value));
						else
							sb.append("${").append(key).append('}');
					}
					else
					{
						token.append(c);
					}
				}
				break;
			}
		}
			
		if (state == STATE_REPLACER_START)
			sb.append('$');
		else if (state == STATE_REPLACER_TOKEN)
			sb.append("${").append(token.toString());
			
		return sb.toString();
	}
	
	/**
	 * Exports a shell script to a directory.
	 * @param resourceName the source resource to read from.
	 * @param mainClass the main class to invoke.
	 * @param options the JVM options.
	 * @param jarName the JAR file name to invoke, if embedded JAR script.
	 * @param target the target file to write to.
	 * @throws IOException if the file could not be written.
	 * @throws SecurityException if file could not be created due to permissioning.
	 */
	public static void copyShellScript(String resourceName, Class<?> mainClass, String options, String jarName, File target) throws IOException
	{
		try (
			ReplacerReader reader = new ReplacerReader(Common.openResourceReader(resourceName), "{{", "}}");
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(target))
		){
			reader
				.replace("JAVA_OPTIONS", options)
				.replace("MAIN_CLASSNAME", mainClass.getCanonicalName())
				.replace("JAR_NAME", jarName)
			;
			
			int b;
			char[] cbuf = new char[8192];
			while ((b = reader.read(cbuf)) >= 0)
			{
				writer.write(cbuf, 0, b);
				writer.flush();
			}
		}
	}
	
	/**
	 * Creates a Java process callable using some common Java options.
	 * @param mainClass the class.
	 * @return the new process callable.
	 */
	public static ProcessCallable spawnJava(Class<?> mainClass)
	{
		return ProcessCallable.java(mainClass, "-Xms64M", "-Xmx768M");
	}
	
	/**
	 * A single replacer for the text replacers.
	 */
	private static class KeyValue<K, V> implements Map.Entry<K, V>
	{
		private K key;
		private V value;

		@Override
		public K getKey()
		{
			return key;
		}
		
		@Override
		public V getValue()
		{
			return value;
		}
		
		@Override
		public V setValue(V value)
		{
			V old = this.value;
			this.value = value;
			return old;
		}
	}

	/**
	 * BufferedReader to Writer stream thread.
	 * Transfers characters buffered at one line at a time until the source stream is closed.
	 * <p> The thread terminates if the reader is closed. The target writer is not closed.
	 */
	public static class LineReaderToWriterThread extends Thread
	{
		protected BufferedReader sourceReader;
		protected Writer targetWriter;
		
		private Throwable exception;
		private long totalCharacters;
		
		/**
		 * Creates a new thread that, when started, transfers characters one line at a time until the source stream is closed.
		 * The user must call {@link #start()} on this thread - it is not active after creation.
		 * @param reader the Reader to read from (wrapped as a BufferedReader).
		 * @param writer the Writer to write to.
		 */
		public LineReaderToWriterThread(Reader reader, Writer writer)
		{
			this(new BufferedReader(reader), writer);
		}

		/**
		 * Creates a new thread that, when started, transfers characters one line at a time until the source stream is closed.
		 * The user must call {@link #start()} on this thread - it is not active after creation.
		 * @param reader the BufferedReader to read from.
		 * @param writer the Writer to write to.
		 */
		public LineReaderToWriterThread(BufferedReader reader, Writer writer)
		{
			super("LineReaderToWriterThread");
			this.sourceReader = reader;
			this.targetWriter = writer;
			this.exception = null;
			this.totalCharacters = 0L;
		}

		@Override
		public final void run() 
		{
			String line;
			try {
				while ((line = sourceReader.readLine()) != null)
				{
					targetWriter.append(line).append('\n').flush();
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

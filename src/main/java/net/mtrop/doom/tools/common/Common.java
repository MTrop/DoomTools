/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.mtrop.doom.tools.struct.ProcessCallable;
import net.mtrop.doom.tools.struct.ReplacerReader;
import net.mtrop.doom.tools.struct.util.IOUtils;

/**
 * Common shared functions.
 * @author Matthew Tropiano
 */
public final class Common
{
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
		try (InputStream in = IOUtils.openResource("net/mtrop/doom/tools/" + name + ".version")) {
			if (in != null)
				VERSION_MAP.put(name, out = IOUtils.getTextualContents(in, "UTF-8").trim());
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
	public static Reader openResourceReader(String pathString)
	{
		InputStream in = IOUtils.openResource(pathString);
		return in != null ? new InputStreamReader(in) : null;
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
	 * @param javaexe the executable name.
	 * @param target the target file to write to.
	 * @throws IOException if the file could not be written.
	 * @throws SecurityException if file could not be created due to permissioning.
	 */
	public static void copyShellScript(String resourceName, Class<?> mainClass, String options, String jarName, String javaexe, File target) throws IOException
	{
		try (
			ReplacerReader reader = new ReplacerReader(Common.openResourceReader(resourceName), "{{", "}}");
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(target))
		){
			reader
				.replace("JAVA_OPTIONS", options)
				.replace("JAVA_EXENAME", javaexe)
				.replace("MAIN_CLASSNAME", mainClass.getCanonicalName())
				.replace("JAR_NAME", jarName)
			;
			
			IOUtils.relay(reader, writer);
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
	
}

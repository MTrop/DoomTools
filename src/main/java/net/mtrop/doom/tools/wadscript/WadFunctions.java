/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.wadscript;

import com.blackrook.rookscript.ScriptInstance;
import com.blackrook.rookscript.ScriptIteratorType;
import com.blackrook.rookscript.ScriptValue;
import com.blackrook.rookscript.ScriptValue.BufferType;
import com.blackrook.rookscript.ScriptValue.Type;
import com.blackrook.rookscript.lang.ScriptFunctionType;
import com.blackrook.rookscript.lang.ScriptFunctionUsage;
import com.blackrook.rookscript.resolvers.ScriptFunctionResolver;
import com.blackrook.rookscript.resolvers.hostfunction.EnumFunctionResolver;

import net.mtrop.doom.Wad;
import net.mtrop.doom.WadBuffer;
import net.mtrop.doom.WadEntry;
import net.mtrop.doom.WadFile;
import net.mtrop.doom.exception.WadException;
import net.mtrop.doom.object.BinaryObject;
import net.mtrop.doom.object.TextObject;
import net.mtrop.doom.struct.io.IOUtils;
import net.mtrop.doom.util.NameUtils;

import static com.blackrook.rookscript.lang.ScriptFunctionUsage.type;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Script functions for WAD.
 * @author Matthew Tropiano
 */
public enum WadFunctions implements ScriptFunctionType
{
	ISWAD(1)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Checks if a file exists and is a Wad file."
				)
				.parameter("file", 
					type(Type.STRING, "Path to WAD file."),
					type(Type.OBJECTREF, "File", "Path to WAD file."),
					type(Type.OBJECTREF, "Wad", "Returns true.")
				)
				.returns(
					type(Type.BOOLEAN, "True if so, false if not."),
					type(Type.ERROR, "Security", "If the OS denied permission to read the file."),
					type(Type.ERROR, "IOError", "If there was an error reading the file.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try
			{
				scriptInstance.popStackValue(temp);
				if (temp.isObjectType(Wad.class))
				{
					returnValue.set(true);
					return true;
				}
				
				File file;
				if (temp.isObjectType(File.class))
					file = temp.asObjectType(File.class);
				else if (temp.isNull())
					file = null;
				else
					file = new File(temp.asString());
				
				if (file == null)
				{
					returnValue.set(false);
					return true;
				}
				
				try {
					returnValue.set(Wad.isWAD(file));
				} catch (SecurityException e) {
					returnValue.setError("Security", e.getMessage(), e.getLocalizedMessage());
				} catch (IOException e) {
					returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
				}
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},

	WADFILE(1)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Opens a WAD File. Registered as an open resource."
				)
				.parameter("file", 
					type(Type.STRING, "Path to WAD file."),
					type(Type.OBJECTREF, "File", "Path to WAD file.")
				)
				.returns(
					type(Type.OBJECTREF, "Wad", "An open Wad."),
					type(Type.ERROR, "BadParameter", "If [file] is null."),
					type(Type.ERROR, "BadFile", "If [file] could not be found."),
					type(Type.ERROR, "BadWad", "If [file] is not a WAD file."),
					type(Type.ERROR, "Security", "If the OS denied permission to read the file."),
					type(Type.ERROR, "IOError", "If there was an error reading the file.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try
			{
				File file = popFile(scriptInstance, temp);
				if (file == null)
				{
					returnValue.setError("BadParameter", "No file provided.");
					return true;
				}
				
				try {
					Wad wad = new WadFile(file);
					scriptInstance.registerCloseable((WadFile)wad);
					returnValue.set(wad);
				} catch (SecurityException e) {
					returnValue.setError("Security", e.getMessage(), e.getLocalizedMessage());
				} catch (WadException e) {
					returnValue.setError("BadWad", e.getMessage(), e.getLocalizedMessage());
				} catch (FileNotFoundException e) {
					returnValue.setError("BadFile", e.getMessage(), e.getLocalizedMessage());
				} catch (IOException e) {
					returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
				}
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},

	WADFILECREATE(1)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Creates a new, empty, open WAD file. Overwrites existing files! Registered as an open resource."
				)
				.parameter("file", 
					type(Type.STRING, "Path to WAD file."),
					type(Type.OBJECTREF, "File", "Path to WAD file.")
				)
				.returns(
					type(Type.OBJECTREF, "Wad", "A newly created WAD file."),
					type(Type.ERROR, "BadParameter", "If [file] is null."),
					type(Type.ERROR, "Security", "If the OS denied permission to create the file."),
					type(Type.ERROR, "IOError", "If there was an error creating the file.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try
			{
				File file = popFile(scriptInstance, temp);
				if (file == null)
				{
					returnValue.setError("BadParameter", "No file provided.");
					return true;
				}
				
				try {
					Wad wad = WadFile.createWadFile(file);
					scriptInstance.registerCloseable((WadFile)wad);
					returnValue.set(wad);
				} catch (SecurityException e) {
					returnValue.setError("Security", e.getMessage(), e.getLocalizedMessage());
				} catch (IOException e) {
					returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
				}
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},

	WADBUFFER(1)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Opens a WAD into an in-memory WAD buffer (not a resource - does not require closing)."
				)
				.parameter("file", 
					type(Type.NULL, "Make empty buffer."),
					type(Type.STRING, "Path to WAD file."),
					type(Type.OBJECTREF, "File", "Path to WAD file."),
					type(Type.OBJECTREF, "InputStream", "An open input stream.")
				)
				.returns(
					type(Type.OBJECTREF, "Wad", "A loaded buffer."),
					type(Type.ERROR, "BadFile", "If [file] could not be found."),
					type(Type.ERROR, "BadWad", "If [file] is not a WAD file."),
					type(Type.ERROR, "Security", "If the OS denied permission to read the file."),
					type(Type.ERROR, "IOError", "If there was an error reading the file.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try
			{
				scriptInstance.popStackValue(temp);
				if (temp.isNull())
				{
					Wad wad = new WadBuffer();
					returnValue.set(wad);
					return true;
				}

				try {
					if (temp.isObjectRef(InputStream.class))
					{
						Wad wad = new WadBuffer(temp.asObjectType(InputStream.class));
						returnValue.set(wad);
					}
					else if (temp.isObjectRef(File.class))
					{
						Wad wad = new WadBuffer(temp.asObjectType(File.class));
						returnValue.set(wad);
					}
					else
					{
						Wad wad = new WadBuffer(new File(temp.asString()));
						returnValue.set(wad);
					}
				} catch (SecurityException e) {
					returnValue.setError("Security", e.getMessage(), e.getLocalizedMessage());
				} catch (WadException e) {
					returnValue.setError("BadWad", e.getMessage(), e.getLocalizedMessage());
				} catch (FileNotFoundException e) {
					returnValue.setError("BadFile", e.getMessage(), e.getLocalizedMessage());
				} catch (IOException e) {
					returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
				}
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},

	/** @since 1.1.0  */
	WADSETTYPE(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Sets a Wad's major type."
				)
				.parameter("wad", 
					type(Type.OBJECTREF, "Wad", "The WAD to change.")
				)
				.parameter("type", 
					type(Type.STRING, "The new WAD type - \"iwad\" or \"pwad\" .")
				)
				.returns(
					type(Type.OBJECTREF, "Wad", "[wad], on success."),
					type(Type.ERROR, "BadType", "If [type] is not \"iwad\" or \"pwad\"."),
					type(Type.ERROR, "BadParameter", "If [wad] is not a WAD."),
					type(Type.ERROR, "IOError", "If a write error occurs.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try
			{
				scriptInstance.popStackValue(temp);
				String type = temp.isNull() ? null : temp.asString();
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(Wad.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a Wad.");
					return true;
				}

				try
				{
					Wad wad = temp.asObjectType(Wad.class);
					if (net.mtrop.doom.Wad.Type.IWAD.name().equalsIgnoreCase(type))
					{
						if (wad instanceof WadFile)
							((WadFile)wad).setType(net.mtrop.doom.Wad.Type.IWAD);
						else if (wad instanceof WadBuffer)
							((WadBuffer)wad).setType(net.mtrop.doom.Wad.Type.IWAD);
						returnValue.set(wad);
					}
					else if (net.mtrop.doom.Wad.Type.PWAD.name().equalsIgnoreCase(type))
					{
						if (wad instanceof WadFile)
							((WadFile)wad).setType(net.mtrop.doom.Wad.Type.PWAD);
						else if (wad instanceof WadBuffer)
							((WadBuffer)wad).setType(net.mtrop.doom.Wad.Type.PWAD);
						returnValue.set(wad);
					}
					else
					{
						returnValue.setError("BadType", "Type is not \"iwad\" or \"pwad\".");
						return true;
					}
				} 
				catch (IOException e) 
				{
					returnValue.setError("IOError", e.getLocalizedMessage());
					return true;
				}
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	WADINFO(1)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Fetches a Wad's info."
				)
				.parameter("wad", 
					type(Type.OBJECTREF, "Wad", "The open WAD to inspect.")
				)
				.returns(
					type(Type.MAP, "{type:STRING, entrycount:INTEGER, listoffset:INTEGER, contentlength:INTEGER, filepath:STRING}", "The fetched WAD information as a map."),
					type(Type.ERROR, "BadParameter", "If [wad] is not a Wad file.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue wadValue = CACHEVALUE1.get();
			try
			{
				scriptInstance.popStackValue(wadValue);
				if (!wadValue.isObjectRef(Wad.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a Wad.");
					return true;
				}

				Wad wad = wadValue.asObjectType(Wad.class);
				returnValue.setEmptyMap(5);
				returnValue.mapSet("contentlength", 12);
				returnValue.mapSet("entrycount", wad.getEntryCount());
				returnValue.mapSet("filepath", (wad instanceof WadFile) ? ((WadFile)wad).getFilePath() : null);
				returnValue.mapSet("listoffset", wad.getContentLength() + 12);
				returnValue.mapSet("type", wad.isIWAD() ? Wad.Type.IWAD.name() : Wad.Type.PWAD.name());
				return true;
			}
			finally
			{
				wadValue.setNull();
			}
		}
	},
	
	WADENTRY(3)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Fetches a Wad's entry info."
				)
				.parameter("wad", 
					type(Type.OBJECTREF, "Wad", "The open WAD to use.")
				)
				.parameter("search", 
					type(Type.NULL, "Fetch nothing."),
					type(Type.INTEGER, "The entry index (0-based)."),
					type(Type.STRING, "The entry name (first found). Also use [startFromSearch].")
				)
				.parameter("startFromSearch", 
					type(Type.NULL, "Start from entry 0."),
					type(Type.INTEGER, "Start from entry index (0-based)."),
					type(Type.STRING, "Start from entry name (first found).")
				)
				.returns(
					type(Type.NULL, "If not found."),
					type(Type.MAP, "{name:STRING, offset:INTEGER, size:INTEGER}", "The fetched entry information as a map."),
					type(Type.ERROR, "BadParameter", "If [wad] is not a Wad file.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue startSearch = CACHEVALUE1.get();
			ScriptValue search = CACHEVALUE2.get();
			ScriptValue wadValue = CACHEVALUE3.get();
			try
			{
				scriptInstance.popStackValue(startSearch);
				scriptInstance.popStackValue(search);
				scriptInstance.popStackValue(wadValue);
				
				if (!wadValue.isObjectRef(Wad.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a Wad.");
					return true;
				}
				
				WadEntry entry;
				Wad wad = wadValue.asObjectType(Wad.class);
				
				if (search.isNull())
					entry = null;
				else if (search.isNumeric())
					entry = wad.getEntry(search.asInt());
				else if (search.isString() && startSearch.isNull())
					entry = wad.getEntry(search.asString());
				else if (search.isString() && startSearch.isNumeric())
					entry = wad.getEntry(search.asString(), startSearch.asInt());
				else if (search.isString() && startSearch.isString())
					entry = wad.getEntry(search.asString(), startSearch.asString());
				else
					entry = null;
				
				if (entry != null)
					setEntry(returnValue, entry);
				
				return true;
			}
			finally
			{
				wadValue.setNull();
				search.setNull();
				startSearch.setNull();
			}
		}
	},

	WADENTRYINDEX(3)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Fetches the index of an entry in the Wad."
				)
				.parameter("wad", 
					type(Type.OBJECTREF, "Wad", "The open WAD to use.")
				)
				.parameter("search", 
					type(Type.NULL, "Fetch nothing."),
					type(Type.STRING, "The entry name (first found). Also use [startFromSearch].")
				)
				.parameter("startFromSearch", 
					type(Type.NULL, "Start from entry 0."),
					type(Type.INTEGER, "Start from entry index (0-based)."),
					type(Type.STRING, "Start from entry name (first found).")
				)
				.returns(
					type(Type.NULL, "If not found."),
					type(Type.INTEGER, "The index of the found entry."),
					type(Type.ERROR, "BadParameter", "If [wad] is not a Wad file.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue startSearch = CACHEVALUE1.get();
			ScriptValue search = CACHEVALUE2.get();
			ScriptValue wadValue = CACHEVALUE3.get();
			try
			{
				scriptInstance.popStackValue(startSearch);
				scriptInstance.popStackValue(search);
				scriptInstance.popStackValue(wadValue);
				
				if (!wadValue.isObjectRef(Wad.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a Wad.");
					return true;
				}
				
				Integer foundIndex;
				Wad wad = wadValue.asObjectType(Wad.class);
				
				if (search.isNull())
					foundIndex = null;
				else if (search.isString() && startSearch.isNull())
					foundIndex = wad.indexOf(search.asString());
				else if (search.isString() && startSearch.isNumeric())
					foundIndex = wad.indexOf(search.asString(), startSearch.asInt());
				else if (search.isString() && startSearch.isString())
					foundIndex = wad.indexOf(search.asString(), startSearch.asString());
				else
					foundIndex = null;

				if (foundIndex != null && foundIndex >= 0)
					returnValue.set(foundIndex);
				else
					returnValue.setNull();
				
				return true;
			}
			finally
			{
				wadValue.setNull();
				search.setNull();
				startSearch.setNull();
			}
		}
	},

	WADENTRIES(3)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Retrieves a contiguous set of entries from a WAD, starting from a desired index."
				)
				.parameter("wad", 
					type(Type.OBJECTREF, "Wad", "The open WAD to use.")
				)
				.parameter("start", 
					type(Type.NULL, "Use 0."),
					type(Type.INTEGER, "The starting entry index.")
				)
				.parameter("length", 
					type(Type.NULL, "Use [wad's entry count] - [start]."),
					type(Type.INTEGER, "The maximum amount of entries to return.")
				)
				.returns(
					type(Type.LIST, "[MAP:{name:STRING, offset:INTEGER, size:INTEGER}, ...]", "The entry info returned."),
					type(Type.ERROR, "BadParameter", "If [wad] is not a Wad file.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try
			{
				scriptInstance.popStackValue(temp);
				Integer length = temp.isNull() ? null : temp.asInt();
				scriptInstance.popStackValue(temp);
				int start = temp.asInt();
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(Wad.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a Wad.");
					return true;
				}
				
				final Wad wad = temp.asObjectType(Wad.class);

				start = Math.min(Math.max(start, 0), wad.getEntryCount());
				if (length == null)
					length = wad.getEntryCount() - start;
				else
					length = Math.max(length, 0);

				WadEntry[] entries = wad.mapEntries(start, length);
				returnValue.setEmptyList(entries.length);
				for (int i = 0; i < entries.length; i++)
				{
					setEntry(temp, entries[i]);
					returnValue.listAdd(temp);
				}
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	WADITERATE(1)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Creates an iterator that iterates through all of the entries in a WAD. " +
					"The value that this produces can be used in an each(...) loop. The keys are entry indices, and " +
					"values are maps of entry info (a la WADENTRY). If you need to scan through a Wad with many " +
					"entries, this may be a less memory-intense way to do it."
				)
				.parameter("wad", 
					type(Type.OBJECTREF, "Wad", "The open WAD to iterate through.")
				)
				.returns(
					type(Type.OBJECTREF, "Iterator", "An iterator for each entry - Key: index:INTEGER, value: MAP{name:STRING, offset:INTEGER, size:INTEGER}."),
					type(Type.ERROR, "BadParameter", "If [wad] is not a Wad file.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue wadValue = CACHEVALUE1.get();
			try
			{
				scriptInstance.popStackValue(wadValue);
				if (!wadValue.isObjectRef(Wad.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a Wad.");
					return true;
				}

				final Wad wad = wadValue.asObjectType(Wad.class);
				returnValue.set(new WadEntryIterator(wad));
				return true;
			}
			finally
			{
				wadValue.setNull();
			}
		}
	},
	
	WADDATA(3)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Gets WAD data using an entry descriptor, returning it as buffers of data."
				)
				.parameter("wad", 
					type(Type.OBJECTREF, "Wad", "The open WAD to use.")
				)
				.parameter("entry", 
					type(Type.NULL, "Fetch nothing."),
					type(Type.INTEGER, "The entry index."),
					type(Type.STRING, "The entry name (first found). Also use [startFromSearch]."),
					type(Type.MAP, "{..., offset:INTEGER, size:INTEGER}", "The entry descriptor.")
				)
				.parameter("startFromSearch", 
					type(Type.NULL, "Start from entry 0."),
					type(Type.INTEGER, "Start from entry index (0-based)."),
					type(Type.STRING, "Start from entry name (first found).")
				)
				.returns(
					type(Type.NULL, "If not found."),
					type(Type.BUFFER, "The entry data."),
					type(Type.ERROR, "BadParameter", "If [wad] is not a Wad file."),
					type(Type.ERROR, "BadEntry", "If [entry] is a map and \"offset\" or \"size\" are missing, or not an accepted value type."),
					type(Type.ERROR, "IOError", "If a read error occurs.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue entry = CACHEVALUE2.get();
			try
			{
				scriptInstance.popStackValue(temp);
				int startFromSearch = temp.isNull() ? 0 : temp.asInt();
				scriptInstance.popStackValue(entry);
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(Wad.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a Wad.");
					return true;
				}

				final Wad wad = temp.asObjectType(Wad.class);
				
				if (entry.isNull())
				{
					returnValue.setNull();
					return true;
				}
				else if (entry.isNumeric())
				{
					int index = entry.asInt();
					if (index < 0 || index >= wad.getEntryCount())
					{
						returnValue.setNull();
						return true;
					}
					
					WadEntry we = wad.getEntry(index);
					if (we != null)
					{
						setWADData(returnValue, wad, we);
						return true;
					}
					else
					{
						returnValue.setNull();
						return true;
					}
				}
				else if (entry.isString())
				{
					String entryName = entry.asString();
					WadEntry we = wad.getEntry(entryName, startFromSearch);
					if (we != null)
					{
						setWADData(returnValue, wad, we);
						return true;
					}
					else
					{
						returnValue.setNull();
						return true;
					}
				}
				else if (entry.isMap())
				{
					int offset, size;
					if (!entry.mapGet("offset", temp))
					{
						returnValue.setError("BadEntry", "\"Offset\" is missing from the provided map.");
						return true;
					}
					else
						offset = temp.asInt();

					if (!entry.mapGet("size", temp))
					{
						returnValue.setError("BadEntry", "\"Size\" is missing from the provided map.");
						return true;
					}
					else
						size = temp.asInt();

					try {
						WadEntry we = WadEntry.create("TEMP", offset, size);
						setWADData(returnValue, wad, we);
						return true;
					} catch (IllegalArgumentException e) {
						returnValue.setError("BadEntry", e.getMessage());
						return true;
					}
				}
				else
				{
					returnValue.setError("BadEntry", "Bad entry type provided.");
					return true;
				}
			}
			finally
			{
				temp.setNull();
				entry.setNull();
			}
		}
	},
	
	WADDATASTREAM(3)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Gets an input stream for WAD data using an entry descriptor."
				)
				.parameter("wad", 
					type(Type.OBJECTREF, "Wad", "The open WAD to use.")
				)
				.parameter("entry", 
					type(Type.NULL, "Fetch nothing."),
					type(Type.INTEGER, "The entry index."),
					type(Type.STRING, "The entry name (first found). Also use [startFromSearch]."),
					type(Type.MAP, "{..., offset:INTEGER, size:INTEGER}", "The entry descriptor.")
				)
				.parameter("startFromSearch", 
					type(Type.NULL, "Start from entry 0."),
					type(Type.INTEGER, "Start from entry index (0-based)."),
					type(Type.STRING, "Start from entry name (first found).")
				)
				.returns(
					type(Type.NULL, "If not found."),
					type(Type.OBJECTREF, "DataInputStream", "The entry data as an open input stream."),
					type(Type.ERROR, "BadParameter", "If [wad] is not a Wad file."),
					type(Type.ERROR, "BadEntry", "If [entry] is a map and \"offset\" or \"size\" are missing, or not an accepted value type."),
					type(Type.ERROR, "IOError", "If a read error occurs.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue entry = CACHEVALUE2.get();
			try
			{
				scriptInstance.popStackValue(temp);
				int startFromSearch = temp.isNull() ? 0 : temp.asInt();
				scriptInstance.popStackValue(entry);
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(Wad.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a Wad.");
					return true;
				}

				final Wad wad = temp.asObjectType(Wad.class);
				
				if (entry.isNull())
				{
					returnValue.setNull();
					return true;
				}
				else if (entry.isNumeric())
				{
					int index = entry.asInt();
					if (index < 0 || index >= wad.getEntryCount())
					{
						returnValue.setNull();
						return true;
					}
					
					WadEntry we = wad.getEntry(index);
					if (we != null)
					{
						setWADDataStream(returnValue, wad, we);
						return true;
					}
					else
					{
						returnValue.setNull();
						return true;
					}
				}
				else if (entry.isString())
				{
					String entryName = entry.asString();
					WadEntry we = wad.getEntry(entryName, startFromSearch);
					if (we != null)
					{
						setWADDataStream(returnValue, wad, we);
						return true;
					}
					else
					{
						returnValue.setNull();
						return true;
					}
				}
				else if (entry.isMap())
				{
					int offset, size;
					if (!entry.mapGet("offset", temp))
					{
						returnValue.setError("BadEntry", "\"Offset\" is missing from the provided map.");
						return true;
					}
					else
						offset = temp.asInt();

					if (!entry.mapGet("size", temp))
					{
						returnValue.setError("BadEntry", "\"Size\" is missing from the provided map.");
						return true;
					}
					else
						size = temp.asInt();

					try {
						WadEntry we = WadEntry.create("temp", offset, size);
						setWADDataStream(returnValue, wad, we);
						return true;
					} catch (IllegalArgumentException e) {
						returnValue.setError("BadEntry", e.getMessage());
						return true;
					}
				}
				else
				{
					returnValue.setError("BadEntry", "Bad entry type provided.");
					return true;
				}
			}
			finally
			{
				temp.setNull();
				entry.setNull();
			}
		}
	},
	
	WADADD(4)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Adds an entry to a Wad."
				)
				.parameter("wad", 
					type(Type.OBJECTREF, "Wad", "The open WAD to use.")
				)
				.parameter("name", 
					type(Type.NULL, "Use \"-\"."),
					type(Type.STRING, "The new entry name (name is coerced into a valid name).")
				)
				.parameter("data", 
					type(Type.NULL, "Synonymous with empty buffer - no data. Writes a marker."),
					type(Type.STRING, "The data to add (as UTF-8)."),
					type(Type.BUFFER, "The data to add (read from current cursor position to the end)."),
					type(Type.OBJECTREF, "File", "The file contents to add."),
					type(Type.OBJECTREF, "InputStream", "The data to add."),
					type(Type.OBJECTREF, "BinaryObject", "The data to add."),
					type(Type.OBJECTREF, "TextObject", "The data to add.")
				)
				.parameter("index",
					type(Type.NULL, "Add to the end."),
					type(Type.INTEGER, "Insert at index.")
				)
				.returns(
					type(Type.OBJECTREF, "Wad", "[wad], if successful."),
					type(Type.ERROR, "BadParameter", "If [wad] is not a Wad file."),
					type(Type.ERROR, "BadData", "If [data] is not an accepted value type."),
					type(Type.ERROR, "BadIndex", "If an [index] was provided and it is less than 0 or greater than the current entry count."),
					type(Type.ERROR, "IOError", "If a read or write error occurs.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue data = CACHEVALUE2.get();
			try
			{
				scriptInstance.popStackValue(temp);
				Integer index = temp.isNull() ? null : temp.asInt();
				scriptInstance.popStackValue(data);
				scriptInstance.popStackValue(temp);
				String name = temp.isNull() ? "-" : temp.asString();
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(Wad.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a Wad.");
					return true;
				}

				final Wad wad = temp.asObjectType(Wad.class);
				name = NameUtils.toValidEntryName(name);
				
				if (data.isNull())
				{
					addWADData(returnValue, wad, name, Wad.NO_DATA, index);
					return true;
				}
				else if (data.isString())
				{
					addWADData(returnValue, wad, name, data.asString().getBytes(UTF_8), index);
					return true;
				}
				else if (data.isBuffer())
				{
					BufferType buffer = data.asObjectType(BufferType.class);
					addWADData(returnValue, wad, name, buffer.getInputStream(), index);
					return true;
				}
				else if (data.isObjectRef(File.class))
				{
					File file = data.asObjectType(File.class);
					addWADData(returnValue, wad, name, file, index);
					return true;
				}
				else if (data.isObjectRef(InputStream.class))
				{
					InputStream in = data.asObjectType(InputStream.class);
					addWADData(returnValue, wad, name, in, index);
					return true;
				}
				else if (data.isObjectRef(BinaryObject.class))
				{
					try (InputStream in = new ByteArrayInputStream(data.asObjectType(BinaryObject.class).toBytes()))
					{
						addWADData(returnValue, wad, name, in, index);
					}
					catch (IOException e)
					{
						returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
					}
					return true;
				}
				else if (data.isObjectRef(TextObject.class))
				{
					try (InputStream in = new ByteArrayInputStream(data.asObjectType(TextObject.class).toText().getBytes(UTF_8)))
					{
						addWADData(returnValue, wad, name, in, index);
					}
					catch (IOException e)
					{
						returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
					}
					return true;
				}
				else
				{
					returnValue.setError("BadData", "Data is not an accepted type.");
					return true;
				}
			}
			finally
			{
				temp.setNull();
				data.setNull();
			}
		}
	},
	
	WADREMOVE(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Removes a WAD's entry by index. Does not remove content."
				)
				.parameter("wad", 
					type(Type.OBJECTREF, "Wad", "The open WAD to use.")
				)
				.parameter("index",
					type(Type.INTEGER, "The entry index to remove.")
				)
				.returns(
					type(Type.OBJECTREF, "Wad", "[wad], if successful."),
					type(Type.ERROR, "BadParameter", "If [wad] is not a Wad file."),
					type(Type.ERROR, "BadIndex", "If the index is less than 0 or greater than or equal to the current entry count."),
					type(Type.ERROR, "IOError", "If a write error occurs.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try
			{
				scriptInstance.popStackValue(temp);
				Integer index = temp.isNull() ? null : temp.asInt();
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(Wad.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a Wad.");
					return true;
				}
				if (index == null)
				{
					returnValue.setError("BadIndex", "Index not provided.");
					return true;
				}

				final Wad wad = temp.asObjectType(Wad.class);

				try {
					wad.removeEntry(index);
					returnValue.set(wad);
				} catch (IndexOutOfBoundsException e) {
					returnValue.setError("BadIndex", "Index " + index + " is out of acceptable range.");
				} catch (IOException e) {
					returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
				}

				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	WADDELETE(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Deletes a WAD's entry by index. Removes content!"
				)
				.parameter("wad", 
					type(Type.OBJECTREF, "Wad", "The open WAD to use.")
				)
				.parameter("index",
					type(Type.INTEGER, "The entry index to delete.")
				)
				.returns(
					type(Type.OBJECTREF, "Wad", "[wad], if successful."),
					type(Type.ERROR, "BadParameter", "If [wad] is not a Wad file."),
					type(Type.ERROR, "BadIndex", "If the index is less than 0 or greater than or equal to the current entry count."),
					type(Type.ERROR, "IOError", "If a write error occurs.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try
			{
				scriptInstance.popStackValue(temp);
				Integer index = temp.isNull() ? null : temp.asInt();
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(Wad.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a Wad.");
					return true;
				}
				if (index == null)
				{
					returnValue.setError("BadIndex", "Index not provided.");
					return true;
				}

				final Wad wad = temp.asObjectType(Wad.class);

				try {
					wad.deleteEntry(index);
					returnValue.set(wad);
				} catch (IndexOutOfBoundsException e) {
					returnValue.setError("BadIndex", "Index " + index + " is out of acceptable range.");
				} catch (IOException e) {
					returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
				}

				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	WADIMPORT(4)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Imports data into one Wad from another."
				)
				.parameter("wad", 
					type(Type.OBJECTREF, "Wad", "The open WAD to import into.")
				)
				.parameter("srcWad", 
					type(Type.OBJECTREF, "Wad", "The open source Wad to import from.")
				)
				.parameter("srcEntries",
					type(Type.NULL, "Assume all entries from the source."),
					type(Type.LIST, "[MAP:{name:STRING, offset:INTEGER, size:INTEGER}, ...]", "The list of entries to import from the source, in the order provided.")
				)
				.parameter("index",
					type(Type.NULL, "Add to the end."),
					type(Type.INTEGER, "Insert at index.")
				)
				.returns(
					type(Type.OBJECTREF, "Wad", "[wad], if successful."),
					type(Type.ERROR, "BadParameter", "If [wad] or [srcWad] are not Wad files."),
					type(Type.ERROR, "BadEntry", "If one of the entries in the entry list is malformed."),
					type(Type.ERROR, "BadIndex", "If the index is less than 0 or greater than or equal to the current entry count."),
					type(Type.ERROR, "IOError", "If a write error occurs.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue dest = CACHEVALUE1.get();
			ScriptValue src = CACHEVALUE2.get();
			ScriptValue entries = CACHEVALUE3.get();
			ScriptValue temp = CACHEVALUE4.get();
			try
			{
				scriptInstance.popStackValue(temp);
				Integer index = temp.isNull() ? null : temp.asInt();
				scriptInstance.popStackValue(entries);
				scriptInstance.popStackValue(src);
				scriptInstance.popStackValue(dest);
				
				if (!dest.isObjectRef(Wad.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a Wad.");
					return true;
				}
				if (!src.isObjectRef(Wad.class))
				{
					returnValue.setError("BadParameter", "Second parameter is not a Wad.");
					return true;
				}
				
				final Wad srcWad = src.asObjectType(Wad.class);
				final Wad destWad = dest.asObjectType(Wad.class);
				
				if (index == null)
					index = destWad.getEntryCount();
				
				if (entries.isNull())
				{
					try {
						destWad.addFrom(srcWad, srcWad.getAllEntries());
						returnValue.set(destWad);
					} catch (IOException e) {
						returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
					} 
					return true;
				}
				else if (!entries.isList())
				{
					temp.set(entries);
					entries.setEmptyList(1);
					entries.listSetByIndex(0, temp);
				}

				WadFile.Adder adder = null;
				if (WadFile.class.isAssignableFrom(destWad.getClass()))
					adder = ((WadFile)destWad).createAdder();
				try 
				{
					for (int i = 0; i < entries.length(); i++)
					{
						entries.listGetByIndex(i, src);
						WadEntry entry = getEntry(src, temp);
						if (entry == null)
						{
							returnValue.setError("BadEntry", "List index " + i + " describes a bad entry - must be a map, and check name, offset, size.");
							return true;
						}
						
						try (InputStream in = srcWad.getInputStream(entry))
						{
							if (adder != null)
								adder.addData(entry.getName(), in);
							else
								destWad.addData(entry.getName(), in);
						}
					}
					returnValue.set(destWad);
				} catch (IndexOutOfBoundsException e) {
					returnValue.setError("BadIndex", "Index " + index + " is out of acceptable range.");
				} catch (IOException e) {
					returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
				} finally {
					IOUtils.close(adder);
				}
				
				return true;
			}
			finally
			{
				temp.setNull();
				dest.setNull();
				src.setNull();
				entries.setNull();
			}
		}
	},
	
	;
	
	private final int parameterCount;
	private Usage usage;
	private WadFunctions(int parameterCount)
	{
		this.parameterCount = parameterCount;
		this.usage = null;
	}
	
	/**
	 * @return a function resolver that handles all of the functions in this enum.
	 */
	public static final ScriptFunctionResolver createResolver()
	{
		return new EnumFunctionResolver(WadFunctions.values());
	}

	@Override
	public int getParameterCount()
	{
		return parameterCount;
	}

	@Override
	public Usage getUsage()
	{
		if (usage == null)
			usage = usage();
		return usage;
	}
	
	@Override
	public abstract boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue);

	protected abstract Usage usage();

	/**
	 * Pops a variable off the stack and, using a temp variable, extracts a File/String.
	 * @param scriptInstance the script instance.
	 * @param temp the temporary script value.
	 * @return a File object.
	 */
	private static File popFile(ScriptInstance scriptInstance, ScriptValue temp) 
	{
		scriptInstance.popStackValue(temp);
		if (temp.isNull())
			return null;
		else if (temp.isObjectRef(File.class))
			return temp.asObjectType(File.class);
		else
			return new File(temp.asString());
	}
	
	/**
	 * Sets a script value to an entry map.
	 * @param value the script value.
	 * @param entry the entry to use.
	 */
	private static void setEntry(ScriptValue value, WadEntry entry) 
	{
		value.setEmptyMap(3);
		value.mapSet("name", entry.getName());
		value.mapSet("offset", entry.getOffset());
		value.mapSet("size", entry.getSize());
	}
	
	/**
	 * Gets an entry from a script value.
	 * @param value the script value.
	 * @param temp a temp value.
	 * @return a WadEntry, or null if information is missing.
	 * @throws IllegalArgumentException if entry is malformed.
	 */
	private static WadEntry getEntry(ScriptValue value, ScriptValue temp) 
	{
		if (!value.isMap())
			return null;
		
		int offset, size;
		String name;
		
		if (!value.mapGet("offset", temp))
			return null;
		offset = temp.asInt();

		if (!value.mapGet("size", temp))
			return null;
		size = temp.asInt();

		if (!value.mapGet("name", temp))
			return null;
		name = NameUtils.toValidEntryName(temp.asString());

		return WadEntry.create(name, offset, size);
	}
	
	/**
	 * Gets byte data from a Wad and sets it on a value.
	 * @param value the script value.
	 * @param wad the Wad to read from.
	 * @param entry the entry.
	 */
	private static void setWADData(ScriptValue value, final Wad wad, WadEntry entry)
	{
		try {
			byte[] b = wad.getData(entry);
			value.setEmptyBuffer(b.length);
			value.asObjectType(BufferType.class).readBytes(0, b, 0, b.length);
		} catch (IOException e) {
			value.setError("IOError", e.getMessage(), e.getLocalizedMessage());
		}
	}

	/**
	 * Opens a stream to data from a Wad and sets it on a value.
	 * @param value the script value.
	 * @param wad the Wad to read from.
	 * @param entry the entry.
	 */
	private static void setWADDataStream(ScriptValue value, final Wad wad, WadEntry entry)
	{
		try {
			value.set(new DataInputStream(wad.getInputStream(entry)));
		} catch (IOException e) {
			value.setError("IOError", e.getMessage(), e.getLocalizedMessage());
		}
	}

	/**
	 * Adds data to an open Wad.
	 * @param value the return value (wad or error)
	 * @param wad the open Wad.
	 * @param name the entry name.
	 * @param data the raw data.
	 * @param index the optional index.
	 */
	private static void addWADData(ScriptValue value, final Wad wad, String name, byte[] data, Integer index)
	{
		try {
			wad.addDataAt(index != null ? index : wad.getEntryCount(), name, data);
			value.set(wad);
		} catch (IndexOutOfBoundsException e) {
			value.setError("BadIndex", "Index " + index + " is out of acceptable range.");
		} catch (IOException e) {
			value.setError("IOError", e.getMessage(), e.getLocalizedMessage());
		}
	}

	/**
	 * Adds data to an open Wad.
	 * @param value the return value (wad or error).
	 * @param wad the open Wad.
	 * @param name the entry name.
	 * @param file the file to add.
	 * @param index the optional index.
	 */
	private static void addWADData(ScriptValue value, final Wad wad, String name, File file, Integer index)
	{
		try {
			wad.addDataAt(index != null ? index : wad.getEntryCount(), name, file);
			value.set(wad);
		} catch (IndexOutOfBoundsException e) {
			value.setError("BadIndex", "Index " + index + " is out of acceptable range.");
		} catch (IOException e) {
			value.setError("IOError", e.getMessage(), e.getLocalizedMessage());
		}
	}

	/**
	 * Adds data to an open Wad.
	 * @param value the return value (wad or error).
	 * @param wad the open Wad.
	 * @param name the entry name.
	 * @param in the stream to read.
	 * @param index the optional index.
	 */
	private static void addWADData(ScriptValue value, final Wad wad, String name, InputStream in, Integer index)
	{
		try {
			wad.addDataAt(index != null ? index : wad.getEntryCount(), name, in);
			value.set(wad);
		} catch (IndexOutOfBoundsException e) {
			value.setError("BadIndex", "Index " + index + " is out of acceptable range.");
		} catch (IOException e) {
			value.setError("IOError", e.getMessage(), e.getLocalizedMessage());
		}
	}

	private static class WadEntryIterator implements ScriptIteratorType
	{
		private IteratorPair pair;
		private Wad wad;
		private int cur;

		protected WadEntryIterator(Wad wad) 
		{
			this.pair = new IteratorPair();
			this.wad = wad;
			this.cur = 0;
		}
		
		@Override
		public boolean hasNext()
		{
			return cur < wad.getEntryCount();
		}

		@Override
		public IteratorPair next() 
		{
			pair.getKey().set(cur);
			setEntry(pair.getValue(), wad.getEntry(cur));
			cur++;
			return pair;
		}
	}

	
	private static final Charset UTF_8 = Charset.forName("UTF-8");
	
	// Threadlocal "stack" values.
	private static final ThreadLocal<ScriptValue> CACHEVALUE1 = ThreadLocal.withInitial(()->ScriptValue.create(null));
	private static final ThreadLocal<ScriptValue> CACHEVALUE2 = ThreadLocal.withInitial(()->ScriptValue.create(null));
	private static final ThreadLocal<ScriptValue> CACHEVALUE3 = ThreadLocal.withInitial(()->ScriptValue.create(null));
	private static final ThreadLocal<ScriptValue> CACHEVALUE4 = ThreadLocal.withInitial(()->ScriptValue.create(null));

}

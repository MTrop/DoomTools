/*******************************************************************************
 * Copyright (c) 2017-2020 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package net.mtrop.doom.tools.scripting;

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
import net.mtrop.doom.util.MapUtils;

import static com.blackrook.rookscript.lang.ScriptFunctionUsage.type;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

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

	WADBUFFER(1)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Opens a WAD into an in-memory WAD buffer."
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

				if (foundIndex != null)
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

	WADENTRIES(1)
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
					type(Type.INTEGER, "The starting entry index.")
				)
				.parameter("length", 
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
				int length = temp.asInt();
				scriptInstance.popStackValue(temp);
				int start = temp.asInt();
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(Wad.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a Wad.");
					return true;
				}
				
				start = Math.max(start, 0);
				length = Math.max(length, 0);

				final Wad wad = temp.asObjectType(Wad.class);
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
	
	WADITERATOR(1)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Creates an iterator that iterates through all of the entries in a WAD."
				)
				.parameter("wad", 
					type(Type.OBJECTREF, "Wad", "The open WAD to use.")
				)
				.returns(
					type(Type.OBJECTREF, "ScriptIteratorType", "An iterator for each entry - Key: index:INTEGER, value: MAP{name:STRING, offset:INTEGER, size:INTEGER}."),
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
				returnValue.set(new ScriptIteratorType() 
				{
					private final IteratorPair pair = new IteratorPair();
					private final Iterator<WadEntry> iter = wad.iterator();
					private int cur = 0;
					
					@Override
					public IteratorPair next() 
					{
						pair.set(cur++, null);
						setEntry(pair.getValue(), iter.next());
						return pair;
					}
					
					@Override
					public boolean hasNext()
					{
						return iter.hasNext();
					}
				});
				return true;
			}
			finally
			{
				wadValue.setNull();
			}
		}
	},
	
	WADMAPHEADERS(1)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Fetches all map headers in a WAD."
				)
				.parameter("wad", 
					type(Type.OBJECTREF, "Wad", "The open WAD to use.")
				)
				.returns(
					type(Type.LIST, "[STRING, ...]", "The header names of all found maps. Can be empty."),
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
				String[] headers = MapUtils.getAllMapHeaders(wad);
				returnValue.setEmptyList(headers.length);
				for (int i = 0; i < headers.length; i++)
					returnValue.listAdd(headers[i]);
				return true;
			}
			finally
			{
				wadValue.setNull();
			}
		}
	},
	
	WADMAPENTRIES(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Fetches all map headers in a WAD."
				)
				.parameter("wad", 
					type(Type.OBJECTREF, "Wad", "The open WAD to use.")
				)
				.parameter("header", 
					type(Type.STRING, "The map header entry name.")
				)
				.returns(
					type(Type.LIST, "[MAP:{name:STRING, offset:INTEGER, size:INTEGER}, ...]", "The entries that make up the map, including the header, or an empty list if it couldn't be found."),
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
				String header = temp.asString();
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(Wad.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a Wad.");
					return true;
				}

				final Wad wad = temp.asObjectType(Wad.class);
				WadEntry[] entries = MapUtils.getMapEntries(wad, header);
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
	
	WADMAPENTRYCOUNT(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Returns the amount of entries that make up a map."
				)
				.parameter("wad", 
					type(Type.OBJECTREF, "Wad", "The open WAD to use.")
				)
				.parameter("header", 
					type(Type.STRING, "The map header entry name.")
				)
				.returns(
					type(Type.INTEGER, "The amount of entries from the header entry that comprises the whole map (including the header)."),
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
				String header = temp.asString();
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(Wad.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a Wad.");
					return true;
				}

				final Wad wad = temp.asObjectType(Wad.class);
				returnValue.set(MapUtils.getMapEntryCount(wad, header));
				return true;
			}
			finally
			{
				temp.setNull();
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
					type(Type.LIST, "[BUFFER, ...]", "The entry data of more than one provided entry, if [entry] was a list."),
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
						WadEntry we = WadEntry.create("temp", offset, size);
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
	
	WADDATAASSTREAM(3)
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
					type(Type.OBJECTREF, "", "The entry data."),
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
	
	// TODO: Finish this.
	
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
	protected File popFile(ScriptInstance scriptInstance, ScriptValue temp) 
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
	protected void setEntry(ScriptValue value, WadEntry entry) 
	{
		value.setEmptyMap(3);
		value.mapSet("name", entry.getName());
		value.mapSet("offset", entry.getOffset());
		value.mapSet("size", entry.getSize());
	}
	
	/**
	 * Gets byte data from a Wad and sets it on a value.
	 * @param value the script value.
	 * @param wad the Wad to read from.
	 * @param entry the entry.
	 */
	protected void setWADData(ScriptValue value, final Wad wad, WadEntry entry)
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
	protected void setWADDataStream(ScriptValue value, final Wad wad, WadEntry entry)
	{
		try {
			value.set(new DataInputStream(wad.getInputStream(entry)));
		} catch (IOException e) {
			value.setError("IOError", e.getMessage(), e.getLocalizedMessage());
		}
	}

	// Threadlocal "stack" values.
	private static final ThreadLocal<ScriptValue> CACHEVALUE1 = ThreadLocal.withInitial(()->ScriptValue.create(null));
	private static final ThreadLocal<ScriptValue> CACHEVALUE2 = ThreadLocal.withInitial(()->ScriptValue.create(null));
	private static final ThreadLocal<ScriptValue> CACHEVALUE3 = ThreadLocal.withInitial(()->ScriptValue.create(null));

}

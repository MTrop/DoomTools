/*******************************************************************************
 * Copyright (c) 2017-2020 Black Rook Software
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 ******************************************************************************/
package net.mtrop.doom.tools.scripting;

import com.blackrook.rookscript.ScriptInstance;
import com.blackrook.rookscript.ScriptValue;
import com.blackrook.rookscript.ScriptValue.Type;
import com.blackrook.rookscript.functions.ZipFunctions;
import com.blackrook.rookscript.lang.ScriptFunctionType;
import com.blackrook.rookscript.lang.ScriptFunctionUsage;
import com.blackrook.rookscript.resolvers.ScriptFunctionResolver;
import com.blackrook.rookscript.resolvers.hostfunction.EnumFunctionResolver;

import net.mtrop.doom.DoomPK3;
import net.mtrop.doom.Wad;
import net.mtrop.doom.WadBuffer;
import net.mtrop.doom.exception.WadException;

import static com.blackrook.rookscript.lang.ScriptFunctionUsage.type;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Script functions for WAD.
 * @author Matthew Tropiano
 */
public enum PK3Functions implements ScriptFunctionType
{
	PK3OPEN(1)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Opens a Doom PK3. The ZF* functions can work with this - a Doom PK3 is a Zip file! " +
					"The file, if opened, is registered as a resource, and will be closed when the script terminates."
				)
				.parameter("path", 
					type(Type.STRING, "Path to PK3 file. Relative paths are relative to working directory."),
					type(Type.OBJECTREF, "File", "Path to PK3 file. Relative paths are relative to working directory.")
				)
				.returns(
					type(Type.OBJECTREF, "DoomPK3", "An open PK3 file."),
					type(Type.ERROR, "Security", "If the OS denied opening the file for the required permissions."),
					type(Type.ERROR, "IOError", "If [path] is null or the file is not a PK3 file, or it does could not be opened/found for some reason.")
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
				try {
					if (file == null)
						returnValue.setError("IOError", "A file was not provided.");
					else
					{
						DoomPK3 pk3 = new DoomPK3(file);
						scriptInstance.registerCloseable(pk3);
						returnValue.set(pk3);
					}
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
	
	PK3ENTRY(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Returns a list of all of the entries in an open PK3 file. This is a rebrand of ZFENTRY - same function."
				)
				.parameter("zip", 
					type(Type.OBJECTREF, "ZipFile", "The open zip/PK3 file.")
				)
				.parameter("entry", 
					type(Type.STRING, "The entry name.")
				)
				.returns(
					type(Type.NULL, "If an entry by that name could not be found."),
					type(Type.MAP, "{name:STRING, dir:BOOLEAN, size:INTEGER, time:INTEGER, comment:STRING, compressedsize:INTEGER, crc:INTEGER, creationtime:INTEGER, lastaccesstime:INTEGER, lastmodifiedtime:INTEGER}", "A map of entry info."),
					type(Type.ERROR, "BadParameter", "If an open zip file was not provided, or [entry] is null."),
					type(Type.ERROR, "IOError", "If a read error occurs, or the zip is not open.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			return ZipFunctions.ZFENTRY.execute(scriptInstance, returnValue);
		}
	},
	
	PK3ENTRIES(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Returns a list of all entries that start with a type of key. The name is treated case-insensitively."
				)
				.parameter("pk3", 
					type(Type.OBJECTREF, "DoomPK3", "An open PK3 file.")
				)
				.parameter("prefix", 
					type(Type.STRING, "The starting prefix for the entries.")
				)
				.returns(
					type(Type.LIST, "[MAP:{name:STRING, dir:BOOLEAN, size:INTEGER, time:INTEGER, comment:STRING, compressedsize:INTEGER, crc:INTEGER, creationtime:INTEGER, lastaccesstime:INTEGER, lastmodifiedtime:INTEGER}, ...]", "A list of maps containg Zip entry info."),
					type(Type.ERROR, "BadParameter", "If an open PK3 file was not provided, or [entry] is null."),
					type(Type.ERROR, "IOError", "If a read error occurs, or the PK3 is not open.")
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
				String prefix = temp.asString();
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectType(ZipFile.class))
				{
					returnValue.setError("BadParameter", "First parameter is not an open Zip/PK3 file.");
					return true;
				}

				DoomPK3 pk3 = temp.asObjectType(DoomPK3.class);
				List<String> entries = pk3.getEntriesStartingWith(prefix);
				if (entries.isEmpty())
				{
					returnValue.setEmptyList();
					return true;
				}
				else
				{
					for (int i = 0; i < entries.size(); i++)
					{
						setEntryInfo(pk3.getEntry(entries.get(i)), temp);
						returnValue.listAdd(temp);
					}
				}
				
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	PK3EOPEN(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Opens a data input stream for reading from a zip file entry (and registers this resource as an open resource). " +
					"This is a rebrand of ZFENTRY - same function."
				)
				.parameter("zip", 
					type(Type.OBJECTREF, "ZipFile", "The open zip/PK3 file.")
				)
				.parameter("entry", 
					type(Type.STRING, "The entry name."),
					type(Type.MAP, "{... name:STRING ...}", "A map of zip entry info containing the name of the entry.")
				)
				.returns(
					type(Type.OBJECTREF, "DataInputStream", "An open data input stream to read from."),
					type(Type.ERROR, "BadParameter", "If an open zip file was not provided, or [entry] is null or [entry].name is null."),
					type(Type.ERROR, "BadEntry", "If [entry] could not be found in the zip."),
					type(Type.ERROR, "IOError", "If a read error occurs, or the zip is not open.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			return ZipFunctions.ZFEOPEN.execute(scriptInstance, returnValue);
		}
	},

	PK3WAD(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Reads a PK3 entry as though it were a WAD file and returns an in-memory Wad buffer (not a resource - does not require closing)."
				)
				.parameter("zip", 
					type(Type.OBJECTREF, "ZipFile", "The open zip/PK3 file.")
				)
				.parameter("entry", 
					type(Type.STRING, "The entry name."),
					type(Type.MAP, "{... name:STRING ...}", "A map of zip entry info containing the name of the entry.")
				)
				.returns(
					type(Type.OBJECTREF, "Wad", "An open data input stream to read from."),
					type(Type.ERROR, "BadParameter", "If an open zip file was not provided, or [entry] is null or [entry].name is null."),
					type(Type.ERROR, "BadEntry", "If [entry] could not be found in the zip."),
					type(Type.ERROR, "BadWad", "If [entry] is not a WAD file."),
					type(Type.ERROR, "IOError", "If a read error occurs, or the zip is not open.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue temp2 = CACHEVALUE2.get();
			try 
			{
				scriptInstance.popStackValue(temp);
				String name;
				if (temp.isNull())
					name = null;
				else if (temp.isMap())
				{
					temp.mapGet("name", temp2);
					name = temp2.isNull() ? null : temp2.asString();
				}
				else
					name = temp.asString();
				
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectType(ZipFile.class))
				{
					returnValue.setError("BadParameter", "First parameter is not an open zip file.");
					return true;
				}
				if (name == null)
				{
					returnValue.setError("BadParameter", "No entry name provided.");
					return true;
				}

				ZipFile zf = temp.asObjectType(ZipFile.class);
				
				ZipEntry entry;
				try {
					entry = zf.getEntry(name);
					if (entry == null)
					{
						returnValue.setError("BadEntry", "Entry named \"" + name + "\" could not be found.");
					}
					else
					{
						try (InputStream in = zf.getInputStream(entry))
						{
							returnValue.set((Wad)(new WadBuffer(in)));
						}
					}
				} catch (WadException e) {
					returnValue.setError("BadWad", e.getMessage(), e.getLocalizedMessage());
				} catch (IllegalStateException | IOException e) {
					returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
				}
				return true;
			}
			finally
			{
				temp.setNull();
				temp2.setNull();
			}
		}
	},

	;
	
	private final int parameterCount;
	private Usage usage;
	private PK3Functions(int parameterCount)
	{
		this.parameterCount = parameterCount;
		this.usage = null;
	}
	
	/**
	 * @return a function resolver that handles all of the functions in this enum.
	 */
	public static final ScriptFunctionResolver createResolver()
	{
		return new EnumFunctionResolver(PK3Functions.values());
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
	 * Sets a script value to a map with zip entry data.
	 * @param entry the zip entry.
	 * @param out the value to change.
	 */
	private static void setEntryInfo(ZipEntry entry, ScriptValue out) 
	{
		out.setEmptyMap(8);
		
		if (entry.getComment() != null)
			out.mapSet("comment", entry.getComment());
		if (entry.getCompressedSize() >= 0)
			out.mapSet("compressedsize", entry.getCompressedSize());
		if (entry.getCrc() >= 0)
			out.mapSet("crc", entry.getCrc());
		if (entry.getCreationTime() != null)
			out.mapSet("creationtime", entry.getCreationTime().toMillis());
		
		out.mapSet("dir", entry.isDirectory());

		if (entry.getLastAccessTime() != null)
			out.mapSet("lastaccesstime", entry.getLastAccessTime().toMillis());
		if (entry.getLastModifiedTime() != null)
			out.mapSet("lastmodifiedtime", entry.getLastModifiedTime().toMillis());
		
		out.mapSet("name", entry.getName());
		
		if (entry.getSize() >= 0)
			out.mapSet("size", entry.getSize());
		if (entry.getTime() >= 0)
			out.mapSet("time", entry.getTime());
	}
	
	// Threadlocal "stack" values.
	private static final ThreadLocal<ScriptValue> CACHEVALUE1 = ThreadLocal.withInitial(()->ScriptValue.create(null));
	private static final ThreadLocal<ScriptValue> CACHEVALUE2 = ThreadLocal.withInitial(()->ScriptValue.create(null));

}

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
import com.blackrook.rookscript.lang.ScriptFunctionType;
import com.blackrook.rookscript.lang.ScriptFunctionUsage;
import com.blackrook.rookscript.resolvers.ScriptFunctionResolver;
import com.blackrook.rookscript.resolvers.hostfunction.EnumFunctionResolver;

import net.mtrop.doom.Wad;
import net.mtrop.doom.WadBuffer;
import net.mtrop.doom.WadFile;
import net.mtrop.doom.exception.WadException;

import static com.blackrook.rookscript.lang.ScriptFunctionUsage.type;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

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
	
	// Threadlocal "stack" values.
	private static final ThreadLocal<ScriptValue> CACHEVALUE1 = ThreadLocal.withInitial(()->ScriptValue.create(null));

}

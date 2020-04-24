package net.mtrop.doom.tools.scripting;

import com.blackrook.rookscript.ScriptInstance;
import com.blackrook.rookscript.ScriptValue;
import com.blackrook.rookscript.ScriptValue.Type;
import com.blackrook.rookscript.lang.ScriptFunctionType;
import com.blackrook.rookscript.lang.ScriptFunctionUsage;
import com.blackrook.rookscript.resolvers.ScriptFunctionResolver;
import com.blackrook.rookscript.resolvers.hostfunction.EnumFunctionResolver;

import net.mtrop.doom.Wad;
import net.mtrop.doom.exception.TextureException;
import net.mtrop.doom.object.BinaryObject;
import net.mtrop.doom.struct.io.IOUtils;
import net.mtrop.doom.texture.Animated;
import net.mtrop.doom.texture.CommonTextureList;
import net.mtrop.doom.texture.DoomTextureList;
import net.mtrop.doom.texture.PatchNames;
import net.mtrop.doom.texture.StrifeTextureList;
import net.mtrop.doom.texture.Switches;
import net.mtrop.doom.texture.TextureSet;
import net.mtrop.doom.tools.common.ParseException;
import net.mtrop.doom.tools.common.Utility;
import net.mtrop.doom.util.NameUtils;
import net.mtrop.doom.util.TextureUtils;

import static com.blackrook.rookscript.lang.ScriptFunctionUsage.type;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * Script functions for thicker utility functions.
 * @author Matthew Tropiano
 */
public enum UtilityFunctions implements ScriptFunctionType
{
	IMPORTDEUTEX(5)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Imports a DEUTEX-style texture file, adding a TEXTUREX and PNAMES entry to a Wad."
				)
				.parameter("wad", 
					type(Type.OBJECTREF, "Wad", "An open Wad file.")
				)
				.parameter("input", 
					type(Type.OBJECTREF, "File", "Path to texture definition file."),
					type(Type.OBJECTREF, "InputStream", "Input stream for reading texture definition info (assumes UTF-8 encoding)."),
					type(Type.OBJECTREF, "Reader", "The reader to read the texture definition info from.")
				)
				.parameter("entryName", 
					type(Type.NULL, "Use \"TEXTURE1\"."),
					type(Type.STRING, "The target entry name.")
				)
				.parameter("append", 
					type(Type.BOOLEAN, "If true, search for the existing entry and add to it (and PNAMES).")
				)
				.parameter("strife", 
					type(Type.BOOLEAN, "If true, use Strife texture set format on write (if append is true, use existing format).")
				)
				.returns(
					type(Type.OBJECTREF, "Wad", "[wad]."),
					type(Type.ERROR, "BadParameter", "If [wad] is not a Wad, or [input] is not a valid input type."),
					type(Type.ERROR, "BadFile", "If [input] is a file and it can't be found."),
					type(Type.ERROR, "Parse", "If the texture data cannot be parsed."),
					type(Type.ERROR, "Security", "If [input] is a file and the OS is preventing the read."),
					type(Type.ERROR, "IOError", "If a read or write error occurs.")
				)
			;
		}
		
		@Override
		@SuppressWarnings("unchecked")
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue input = CACHEVALUE2.get();
			try 
			{
				scriptInstance.popStackValue(temp);
				boolean strife = temp.asBoolean();
				scriptInstance.popStackValue(temp);
				boolean append = temp.asBoolean();
				scriptInstance.popStackValue(temp);
				String entryName = temp.isNull() ? "TEXTURE1" : NameUtils.toValidEntryName(temp.asString());
				scriptInstance.popStackValue(input);
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(Wad.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a Wad.");
					return true;
				}

				BufferedReader reader;
				boolean close = false;
				if (input.isObjectRef(File.class))
				{
					try {
						reader = new BufferedReader(new InputStreamReader(new FileInputStream(input.asObjectType(File.class)), UTF_8));
						close = true;
					} catch (FileNotFoundException e) {
						returnValue.setError("BadFile", e.getMessage(), e.getLocalizedMessage());
						return true;
					} catch (SecurityException e) {
						returnValue.setError("Security", e.getMessage(), e.getLocalizedMessage());
						return true;
					}
				}
				else if (input.isObjectRef(InputStream.class))
					reader = new BufferedReader(new InputStreamReader(input.asObjectType(InputStream.class), UTF_8));
				else if (input.isObjectRef(BufferedReader.class))
					reader = input.asObjectType(BufferedReader.class);
				else if (input.isObjectRef(Reader.class))
					reader = new BufferedReader(input.asObjectType(Reader.class));
				else
				{
					returnValue.setError("BadParameter", "Second parameter is not a valid input.");
					return true;
				}
				
				Wad wad = temp.asObjectType(Wad.class);
				PatchNames patchNames;
				CommonTextureList<?> textures;
				TextureSet textureSet;
				int textureEntryIndex = -1;
				int patchEntryIndex = -1;
				
				try {
					
					if (!append || (textureEntryIndex = wad.indexOf(entryName)) == -1)
						textures = strife ? new StrifeTextureList(256) : new DoomTextureList(256);
					else
					{
						byte[] data = wad.getData(textureEntryIndex);
						textures = TextureUtils.isStrifeTextureData(data) 
							? BinaryObject.create(StrifeTextureList.class, data) 
							: BinaryObject.create(DoomTextureList.class, data);
					}

					if (!append || (patchEntryIndex = wad.indexOf("PNAMES")) == -1)
						patchNames = new PatchNames();
					else
						patchNames = wad.getDataAs(patchEntryIndex, PatchNames.class);
					
					textureSet = Utility.readDEUTEXFile(reader, patchNames, textures);
					
				} catch (ParseException e) {
					returnValue.setError("Parse", e.getMessage());
					return true;
				} catch (IOException e) {
					returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
					return true;
				} finally {
					if (close)
						IOUtils.close(reader);
				}
				
				if (strife)
					textureSet.export(patchNames = new PatchNames(), (CommonTextureList<StrifeTextureList.Texture>)(textures = new StrifeTextureList(256)));
				else
					textureSet.export(patchNames = new PatchNames(), (CommonTextureList<DoomTextureList.Texture>)(textures = new DoomTextureList(256)));

				try {
					
					if (patchEntryIndex >= 0)
						wad.replaceEntry(patchEntryIndex, patchNames);
					else
						wad.addData("PNAMES", patchNames);
					
					if (textureEntryIndex >= 0)
						wad.replaceEntry(textureEntryIndex, textures);
					else
						wad.addData(entryName, textures);
					
				} catch (IOException e) {
					returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
					return true;
				}
				
				returnValue.set(temp);
				return true;
			}
			finally
			{
				temp.setNull();
				input.setNull();
			}
		}
	},
	
	EXPORTDEUTEX(4)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Exports a DEUTEX-style texture file from a TEXTUREx entry (and corresponding PNAMES entry)."
				)
				.parameter("wad", 
					type(Type.OBJECTREF, "Wad", "An open Wad file.")
				)
				.parameter("output", 
					type(Type.OBJECTREF, "File", "The file to write the texture definition info to (encoding is UTF-8, file is overwritten)."),
					type(Type.OBJECTREF, "OutputStream", "The output stream to write the texture definition info to (encoding is UTF-8)."),
					type(Type.OBJECTREF, "Writer", "The Writer to write the texture definition info to.")
				)
				.parameter("entryName", 
					type(Type.STRING, "The texture entry name (PNAMES is automatically picked up).")
				)
				.parameter("header", 
					type(Type.NULL, "Use default."),
					type(Type.STRING, "The header line to write first.")
				)
				.returns(
					type(Type.OBJECTREF, "Wad", "[wad]."),
					type(Type.ERROR, "BadParameter", "If [wad] is not a Wad, or [output] is not a valid output type."),
					type(Type.ERROR, "BadFile", "If [output] is a file and is a directory."),
					type(Type.ERROR, "BadData", "If the texture entry does not exist or the entry \"PNAMES\" does not exist, or the entries have bad data."),
					type(Type.ERROR, "Security", "If [output] is a file and the OS is preventing the read."),
					type(Type.ERROR, "IOError", "If a read or write error occurs.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue output = CACHEVALUE2.get();
			try 
			{
				scriptInstance.popStackValue(temp);
				String header = temp.isNull() ? null : temp.asString();
				scriptInstance.popStackValue(temp);
				String entryName = temp.asString();
				scriptInstance.popStackValue(output);
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(Wad.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a Wad.");
					return true;
				}

				Wad wad = temp.asObjectType(Wad.class);
				PrintWriter writer = null;
				boolean close = false;
				
				try {
					
					if (output.isObjectRef(File.class))
					{
						writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output.asObjectType(File.class)), UTF_8), true);
						close = true;
					}
					else if (output.isObjectRef(OutputStream.class))
						writer = new PrintWriter(new OutputStreamWriter(output.asObjectType(OutputStream.class), UTF_8), true);
					else if (output.isObjectRef(PrintWriter.class))
						writer = output.asObjectType(PrintWriter.class);
					else if (output.isObjectRef(Writer.class))
						writer = new PrintWriter(output.asObjectType(Writer.class), true);
					else
					{
						returnValue.setError("BadParameter", "Second parameter is not a valid Writer.");
						return true;
					}
					
					byte[] data = wad.getData(entryName);
					if (data == null)
					{
						returnValue.setError("BadData", "Wad is missing " + entryName + ".");
						return true;
					}
					CommonTextureList<?> textures = TextureUtils.isStrifeTextureData(data) 
						? BinaryObject.create(StrifeTextureList.class, data) 
						: BinaryObject.create(DoomTextureList.class, data);

					PatchNames patchNames = wad.getDataAs("PNAMES", PatchNames.class);
					if (patchNames == null)
					{
						returnValue.setError("BadData", "Wad is missing PNAMES.");
						return true;
					}
					
					TextureSet textureSet = new TextureSet(patchNames, textures);
					
					Utility.writeDEUTEXFile(
						textureSet, 
						header == null 
							? WADSCRIPT_DEUTEX_OUTPUT_HEADER 
							: "; " + header.replaceAll("\\n+", "\n; "), 
						writer
					);
					
				} catch (TextureException e) {
					returnValue.setError("BadData", e.getMessage(), e.getLocalizedMessage());
					return true;
				} catch (SecurityException e) {
					returnValue.setError("Security", e.getMessage(), e.getLocalizedMessage());
					return true;
				} catch (IOException e) {
					returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
					return true;
				} finally {
					if (close)
						IOUtils.close(writer);
				}
				
				returnValue.set(temp);
				return true;
			}
			finally
			{
				temp.setNull();
				output.setNull();
			}
		}
	},
	
	IMPORTSWANTBLS(3)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Imports a SWANTBLS-style (SWitch and ANimated TaBLeS) file, adding a Boom Engine SWITCHES and ANIMATED entry to a Wad."
				)
				.parameter("wad", 
					type(Type.OBJECTREF, "Wad", "An open Wad file.")
				)
				.parameter("input", 
					type(Type.OBJECTREF, "File", "Path to table file."),
					type(Type.OBJECTREF, "InputStream", "Input stream for reading Switch/Animated info (assumes UTF-8 encoding)."),
					type(Type.OBJECTREF, "Reader", "The reader to read the Switch/Animated info from.")
				)
				.parameter("append", 
					type(Type.BOOLEAN, "If true, search for the existing entries and add to them (SWITCHES and ANIMATED).")
				)
				.returns(
					type(Type.OBJECTREF, "Wad", "[wad]."),
					type(Type.ERROR, "BadParameter", "If [wad] is not a Wad, or [input] is not a valid input type."),
					type(Type.ERROR, "BadFile", "If [input] is a file and it can't be found."),
					type(Type.ERROR, "Parse", "If the table data cannot be parsed."),
					type(Type.ERROR, "Security", "If [input] is a file and the OS is preventing the read."),
					type(Type.ERROR, "IOError", "If a read or write error occurs.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue input = CACHEVALUE2.get();
			try 
			{
				scriptInstance.popStackValue(temp);
				boolean append = temp.asBoolean();
				scriptInstance.popStackValue(input);
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(Wad.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a Wad.");
					return true;
				}

				BufferedReader reader;
				boolean close = false;
				if (input.isObjectRef(File.class))
				{
					try {
						reader = new BufferedReader(new InputStreamReader(new FileInputStream(input.asObjectType(File.class)), UTF_8));
						close = true;
					} catch (FileNotFoundException e) {
						returnValue.setError("BadFile", e.getMessage(), e.getLocalizedMessage());
						return true;
					} catch (SecurityException e) {
						returnValue.setError("Security", e.getMessage(), e.getLocalizedMessage());
						return true;
					}
				}
				else if (input.isObjectRef(InputStream.class))
					reader = new BufferedReader(new InputStreamReader(input.asObjectType(InputStream.class), UTF_8));
				else if (input.isObjectRef(BufferedReader.class))
					reader = input.asObjectType(BufferedReader.class);
				else if (input.isObjectRef(Reader.class))
					reader = new BufferedReader(input.asObjectType(Reader.class));
				else
				{
					returnValue.setError("BadParameter", "Second parameter is not a valid input.");
					return true;
				}
				
				Wad wad = temp.asObjectType(Wad.class);
				int switchesEntryIndex = -1;
				int animatedEntryIndex = -1;
				Animated animated;
				Switches switches;
				
				try {
					
					if (!append || (switchesEntryIndex = wad.indexOf("SWITCHES")) == -1)
						switches = new Switches();
					else
						switches = wad.getDataAs(switchesEntryIndex, Switches.class);

					if (!append || (animatedEntryIndex = wad.indexOf("ANIMATED")) == -1)
						animated = new Animated();
					else
						animated = wad.getDataAs(animatedEntryIndex, Animated.class);
					
					Utility.readSwitchAnimatedTables(reader, animated, switches);
					
				} catch (ParseException e) {
					returnValue.setError("Parse", e.getMessage());
					return true;
				} catch (IOException e) {
					returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
					return true;
				} finally {
					if (close)
						IOUtils.close(reader);
				}
				
				try {
					
					if (switchesEntryIndex >= 0)
						wad.replaceEntry(switchesEntryIndex, switches);
					else
						wad.addData("SWITCHES", switches);

					if (animatedEntryIndex >= 0)
						wad.replaceEntry(animatedEntryIndex, animated);
					else
						wad.addData("ANIMATED", animated);

				} catch (IOException e) {
					returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
					return true;
				}
				
				returnValue.set(temp);
				return true;
			}
			finally
			{
				temp.setNull();
				input.setNull();
			}
		}
	},
	
	EXPORTSWANTBLS(3)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Exports a SWANTBLS-style (SWitch and ANimated TaBLeS) file, from Boom Engine SWITCHES and ANIMATED entries."
				)
				.parameter("wad", 
					type(Type.OBJECTREF, "Wad", "An open Wad file.")
				)
				.parameter("output", 
					type(Type.OBJECTREF, "File", "The file to write the info to (encoding is UTF-8, file is overwritten)."),
					type(Type.OBJECTREF, "OutputStream", "The output stream to write the info to (encoding is UTF-8)."),
					type(Type.OBJECTREF, "Writer", "The Writer to write the info to.")
				)
				.parameter("header", 
					type(Type.NULL, "Use default."),
					type(Type.STRING, "The header line to write first.")
				)
				.returns(
					type(Type.OBJECTREF, "Wad", "[wad]."),
					type(Type.ERROR, "BadParameter", "If [wad] is not a Wad, or [output] is not a valid output type."),
					type(Type.ERROR, "BadFile", "If [output] is a file and is a directory."),
					type(Type.ERROR, "Security", "If [output] is a file and the OS is preventing the read."),
					type(Type.ERROR, "IOError", "If a read or write error occurs.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue output = CACHEVALUE2.get();
			try 
			{
				scriptInstance.popStackValue(temp);
				String header = temp.isNull() ? null : temp.asString();
				scriptInstance.popStackValue(output);
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(Wad.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a Wad.");
					return true;
				}

				Wad wad = temp.asObjectType(Wad.class);
				PrintWriter writer = null;
				boolean close = false;
				
				try {
					
					if (output.isObjectRef(File.class))
					{
						writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(output.asObjectType(File.class)), UTF_8), true);
						close = true;
					}
					else if (output.isObjectRef(OutputStream.class))
						writer = new PrintWriter(new OutputStreamWriter(output.asObjectType(OutputStream.class), UTF_8), true);
					else if (output.isObjectRef(PrintWriter.class))
						writer = output.asObjectType(PrintWriter.class);
					else if (output.isObjectRef(Writer.class))
						writer = new PrintWriter(output.asObjectType(Writer.class), true);
					else
					{
						returnValue.setError("BadParameter", "Second parameter is not a valid Writer.");
						return true;
					}
					
					Animated animated;
					Switches switches;
					if ((animated = wad.getDataAs("ANIMATED", Animated.class)) == null)
						animated = new Animated();
					if ((switches = wad.getDataAs("SWITCHES", Switches.class)) == null)
						switches = new Switches();
					
					Utility.writeSwitchAnimatedTables(
						switches, animated, 
						header == null 
							? WADSCRIPT_SWANTBLS_OUTPUT_HEADER 
							: "# " + header.replaceAll("\\n+", "\n# "), 
						writer
					);
					
				} catch (SecurityException e) {
					returnValue.setError("Security", e.getMessage(), e.getLocalizedMessage());
					return true;
				} catch (IOException e) {
					returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
					return true;
				} finally {
					if (close)
						IOUtils.close(writer);
				}
				
				returnValue.set(temp);
				return true;
			}
			finally
			{
				temp.setNull();
				output.setNull();
			}
		}
	},
	
	;
	
	private final int parameterCount;
	private Usage usage;
	private UtilityFunctions(int parameterCount)
	{
		this.parameterCount = parameterCount;
		this.usage = null;
	}
	
	/**
	 * @return a function resolver that handles all of the functions in this enum.
	 */
	public static final ScriptFunctionResolver createResolver()
	{
		return new EnumFunctionResolver(UtilityFunctions.values());
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

	private static final Charset UTF_8 = Charset.forName("UTF-8");

	private static final String WADSCRIPT_DEUTEX_OUTPUT_HEADER = (new StringBuilder())
		.append("; File generated by WadScript").append('\n')
		.append("; This is also compatible with DEUTEX!")
	.toString();

	private static final String WADSCRIPT_SWANTBLS_OUTPUT_HEADER = "# File generated by WadScript";

	// Threadlocal "stack" values.
	private static final ThreadLocal<ScriptValue> CACHEVALUE1 = ThreadLocal.withInitial(()->ScriptValue.create(null));
	private static final ThreadLocal<ScriptValue> CACHEVALUE2 = ThreadLocal.withInitial(()->ScriptValue.create(null));

}

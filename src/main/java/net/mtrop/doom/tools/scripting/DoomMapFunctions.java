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
import com.blackrook.rookscript.ScriptIteratorType.IteratorPair;
import com.blackrook.rookscript.ScriptValue;
import com.blackrook.rookscript.ScriptValue.Type;
import com.blackrook.rookscript.lang.ScriptFunctionType;
import com.blackrook.rookscript.lang.ScriptFunctionUsage;
import com.blackrook.rookscript.resolvers.ScriptFunctionResolver;
import com.blackrook.rookscript.resolvers.hostfunction.EnumFunctionResolver;

import net.mtrop.doom.Wad;
import net.mtrop.doom.map.data.DoomLinedef;
import net.mtrop.doom.map.data.DoomSector;
import net.mtrop.doom.map.data.DoomSidedef;
import net.mtrop.doom.map.data.DoomThing;
import net.mtrop.doom.map.data.DoomVertex;
import net.mtrop.doom.map.data.HexenLinedef;
import net.mtrop.doom.map.data.HexenThing;
import net.mtrop.doom.map.data.flags.BoomLinedefFlags;
import net.mtrop.doom.map.data.flags.BoomThingFlags;
import net.mtrop.doom.map.data.flags.DoomLinedefFlags;
import net.mtrop.doom.map.data.flags.DoomThingFlags;
import net.mtrop.doom.map.data.flags.HexenLinedefFlags;
import net.mtrop.doom.map.data.flags.HexenThingFlags;
import net.mtrop.doom.map.data.flags.MBFThingFlags;
import net.mtrop.doom.map.data.flags.StrifeLinedefFlags;
import net.mtrop.doom.map.data.flags.StrifeThingFlags;
import net.mtrop.doom.map.data.flags.ZDoomLinedefFlags;
import net.mtrop.doom.map.data.flags.ZDoomThingFlags;
import net.mtrop.doom.map.udmf.UDMFObject;
import net.mtrop.doom.map.udmf.attributes.UDMFCommonSectorAttributes;
import net.mtrop.doom.map.udmf.attributes.UDMFCommonSidedefAttributes;
import net.mtrop.doom.map.udmf.attributes.UDMFCommonThingAttributes;
import net.mtrop.doom.map.udmf.attributes.UDMFCommonVertexAttributes;
import net.mtrop.doom.map.udmf.attributes.UDMFDoomLinedefAttributes;
import net.mtrop.doom.map.udmf.attributes.UDMFHexenLinedefAttributes;
import net.mtrop.doom.map.udmf.attributes.UDMFHexenThingAttributes;
import net.mtrop.doom.map.udmf.attributes.UDMFMBFThingAttributes;
import net.mtrop.doom.map.udmf.attributes.UDMFStrifeLinedefAttributes;
import net.mtrop.doom.map.udmf.attributes.UDMFStrifeThingAttributes;
import net.mtrop.doom.map.udmf.attributes.UDMFZDoomLinedefAttributes;
import net.mtrop.doom.util.NameUtils;

import static com.blackrook.rookscript.lang.ScriptFunctionUsage.type;

import java.util.Map;
import java.util.zip.ZipEntry;

/**
 * Script functions for WAD.
 * @author Matthew Tropiano
 */
public enum DoomMapFunctions implements ScriptFunctionType
{
	WADGETMAP(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Loads a Doom Map into memory for inspection. The map header in the Wad can be " +
					"a Doom or Hexen or UDMF format."
				)
				.parameter("wad", 
					type(Type.OBJECTREF, "Wad", "An open Wad.")
				)
				.parameter("header", 
					type(Type.INTEGER, "The entry index of the map's header."),
					type(Type.STRING, "The name of the map entry to read.")
				)
				.returns(
					type(Type.OBJECTREF, "MapView", "An open map."),
					type(Type.ERROR, "BadParameter", "If [wad] is not a valid open Wad file."),
					type(Type.ERROR, "BadMap", "If a map could not be read."),
					type(Type.ERROR, "IOError", "If [wad] could not be read or the map data could not be read.")
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
				scriptInstance.popStackValue(entry);
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(Wad.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a Wad.");
					return true;
				}

				if (entry.isNull())
				{
					returnValue.setNull();
					return true;
				}
				else if (entry.isNumeric())
				{
					int index = entry.asInt();
					
					// TODO: Finish this.
					return true;
				}
				else
				{
					String name = entry.asString();
					
					// TODO: Finish this.
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
	private DoomMapFunctions(int parameterCount)
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
	 * Sets a script value to a map with zip entry data.
	 * @param entry the zip entry.
	 * @param out the value to change.
	 */
	protected void setEntryInfo(ZipEntry entry, ScriptValue out) 
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
	
	protected void udmfToMap(UDMFObject object, ScriptValue out)
	{
		out.setEmptyMap(16);
		for (Map.Entry<String, Object> entry : object)
			out.mapSet(entry.getKey(), entry.getValue());
	}

	protected boolean mapToUDMF(ScriptValue out, UDMFObject object)
	{
		if (!out.isMap())
			return false;
		
		ScriptIteratorType mapIt = out.iterator();
		while (mapIt.hasNext())
		{
			IteratorPair pair = mapIt.next();
			String key = pair.getKey().asString();
			ScriptValue value = pair.getValue();
			if (value.isNull())
				continue;
			else if (value.isBoolean())
				object.setBoolean(key, value.asBoolean());
			else if (value.isInteger())
				object.setInteger(key, value.asInt());
			else if (value.isFloat())
				object.setFloat(key, value.asFloat());
			else
				object.setString(key, String.valueOf(value));
		}
		
		out.setEmptyMap(16);
		for (Map.Entry<String, Object> entry : object)
			out.mapSet(entry.getKey(), entry.getValue());
		return true;
	}

	protected void vertexToMap(DoomVertex vertex, ScriptValue out)
	{
		out.setEmptyMap(2);
		out.mapSet(UDMFCommonVertexAttributes.ATTRIB_POSITION_X, vertex.getX());
		out.mapSet(UDMFCommonVertexAttributes.ATTRIB_POSITION_Y, vertex.getY());
	}

	protected boolean mapToVertex(ScriptValue in, DoomVertex vertex)
	{
		if (!in.isMap())
			return false;
		
		ScriptValue temp = CACHETEMP.get();
		try {
			if (in.mapGet(UDMFCommonVertexAttributes.ATTRIB_POSITION_X, temp))
				vertex.setX(temp.asInt());
			if (in.mapGet(UDMFCommonVertexAttributes.ATTRIB_POSITION_Y, temp))
				vertex.setY(temp.asInt());
		} finally {
			temp.setNull();
		}
		return true;
	}

	protected void sidedefToMap(DoomSidedef sidedef, ScriptValue out)
	{
		out.setEmptyMap(6);
		out.mapSet(UDMFCommonSidedefAttributes.ATTRIB_OFFSET_X, sidedef.getOffsetX());
		out.mapSet(UDMFCommonSidedefAttributes.ATTRIB_OFFSET_Y, sidedef.getOffsetY());
		out.mapSet(UDMFCommonSidedefAttributes.ATTRIB_TEXTURE_TOP, sidedef.getTextureTop());
		out.mapSet(UDMFCommonSidedefAttributes.ATTRIB_TEXTURE_BOTTOM, sidedef.getTextureBottom());
		out.mapSet(UDMFCommonSidedefAttributes.ATTRIB_TEXTURE_MIDDLE, sidedef.getTextureMiddle());
		out.mapSet(UDMFCommonSidedefAttributes.ATTRIB_SECTOR_INDEX, sidedef.getSectorIndex());
	}
	
	protected boolean mapToSidedef(ScriptValue in, DoomSidedef sidedef)
	{
		if (!in.isMap())
			return false;
		
		ScriptValue temp = CACHETEMP.get();
		try {
			if (in.mapGet(UDMFCommonSidedefAttributes.ATTRIB_OFFSET_X, temp))
				sidedef.setOffsetX(temp.asInt());
			if (in.mapGet(UDMFCommonSidedefAttributes.ATTRIB_OFFSET_Y, temp))
				sidedef.setOffsetY(temp.asInt());
			if (in.mapGet(UDMFCommonSidedefAttributes.ATTRIB_TEXTURE_TOP, temp))
				sidedef.setTextureTop(NameUtils.toValidTextureName(temp.asString()));
			if (in.mapGet(UDMFCommonSidedefAttributes.ATTRIB_TEXTURE_BOTTOM, temp))
				sidedef.setTextureBottom(NameUtils.toValidTextureName(temp.asString()));
			if (in.mapGet(UDMFCommonSidedefAttributes.ATTRIB_TEXTURE_MIDDLE, temp))
				sidedef.setTextureMiddle(NameUtils.toValidTextureName(temp.asString()));
			if (in.mapGet(UDMFCommonSidedefAttributes.ATTRIB_SECTOR_INDEX, temp))
				sidedef.setSectorIndex(temp.asInt());
		} finally {
			temp.setNull();
		}
		return true;
	}

	protected void sectorToMap(DoomSector sector, ScriptValue out)
	{
		out.setEmptyMap(8);
		out.mapSet(UDMFCommonSectorAttributes.ATTRIB_HEIGHT_FLOOR, sector.getFloorHeight());
		out.mapSet(UDMFCommonSectorAttributes.ATTRIB_HEIGHT_CEILING, sector.getCeilingHeight());
		out.mapSet(UDMFCommonSectorAttributes.ATTRIB_TEXTURE_FLOOR, sector.getFloorTexture());
		out.mapSet(UDMFCommonSectorAttributes.ATTRIB_TEXTURE_CEILING, sector.getCeilingTexture());
		out.mapSet(UDMFCommonSectorAttributes.ATTRIB_LIGHT_LEVEL, sector.getLightLevel());
		out.mapSet(UDMFCommonSectorAttributes.ATTRIB_SPECIAL, sector.getSpecial());
		out.mapSet(UDMFCommonSectorAttributes.ATTRIB_TAG, sector.getTag());
	}

	protected boolean mapToSector(ScriptValue in, DoomSector sector)
	{
		if (!in.isMap())
			return false;
	
		ScriptValue temp = CACHETEMP.get();
		try {
			if (in.mapGet(UDMFCommonSectorAttributes.ATTRIB_HEIGHT_FLOOR, temp))
				sector.setFloorHeight(temp.asInt());
			if (in.mapGet(UDMFCommonSectorAttributes.ATTRIB_HEIGHT_CEILING, temp))
				sector.setCeilingHeight(temp.asInt());
			if (in.mapGet(UDMFCommonSectorAttributes.ATTRIB_TEXTURE_FLOOR, temp))
				sector.setFloorTexture(NameUtils.toValidTextureName(temp.asString()));
			if (in.mapGet(UDMFCommonSectorAttributes.ATTRIB_TEXTURE_CEILING, temp))
				sector.setCeilingTexture(NameUtils.toValidTextureName(temp.asString()));
			if (in.mapGet(UDMFCommonSectorAttributes.ATTRIB_LIGHT_LEVEL, temp))
				sector.setLightLevel(temp.asInt());
			if (in.mapGet(UDMFCommonSectorAttributes.ATTRIB_SPECIAL, temp))
				sector.setSpecial(temp.asInt());
			if (in.mapGet(UDMFCommonSectorAttributes.ATTRIB_TAG, temp))
				sector.setTag(temp.asInt());
		} finally {
			temp.setNull();
		}
		return true;
	}
	
	protected void thingToMap(DoomThing thing, boolean strife, ScriptValue out)
	{
		out.setEmptyMap(20);
		out.mapSet(UDMFCommonThingAttributes.ATTRIB_POSITION_X, thing.getX());
		out.mapSet(UDMFCommonThingAttributes.ATTRIB_POSITION_Y, thing.getY());
		out.mapSet(UDMFCommonThingAttributes.ATTRIB_ANGLE, thing.getAngle());
		out.mapSet(UDMFCommonThingAttributes.ATTRIB_TYPE, thing.getType());
		
		boolean easy = thing.isFlagSet(DoomThingFlags.EASY);
		boolean medium = thing.isFlagSet(DoomThingFlags.MEDIUM);
		boolean hard = thing.isFlagSet(DoomThingFlags.HARD);

		out.mapSet(UDMFCommonThingAttributes.ATTRIB_FLAG_SKILL1, easy);
		out.mapSet(UDMFCommonThingAttributes.ATTRIB_FLAG_SKILL2, easy);
		out.mapSet("easy", easy);

		out.mapSet(UDMFCommonThingAttributes.ATTRIB_FLAG_SKILL3, medium);
		out.mapSet("medium", medium);

		out.mapSet(UDMFCommonThingAttributes.ATTRIB_FLAG_SKILL4, hard);
		out.mapSet(UDMFCommonThingAttributes.ATTRIB_FLAG_SKILL5, hard);
		out.mapSet("hard", hard);

		if (strife)
		{
			out.mapSet(UDMFStrifeThingAttributes.ATTRIB_FLAG_AMBUSH, thing.isFlagSet(StrifeThingFlags.AMBUSH));
			out.mapSet(UDMFStrifeThingAttributes.ATTRIB_FLAG_SINGLE_PLAYER, !thing.isFlagSet(StrifeThingFlags.MULTIPLAYER));
			out.mapSet(UDMFStrifeThingAttributes.ATTRIB_FLAG_COOPERATIVE, true);
			out.mapSet(UDMFStrifeThingAttributes.ATTRIB_FLAG_DEATHMATCH, true);
			out.mapSet(UDMFStrifeThingAttributes.ATTRIB_FLAG_STANDING, thing.isFlagSet(StrifeThingFlags.STANDING));
			out.mapSet(UDMFStrifeThingAttributes.ATTRIB_FLAG_ALLY, thing.isFlagSet(StrifeThingFlags.ALLY));
			out.mapSet(UDMFStrifeThingAttributes.ATTRIB_FLAG_TRANSLUCENT, thing.isFlagSet(StrifeThingFlags.TRANSLUCENT_25));
			out.mapSet(UDMFStrifeThingAttributes.ATTRIB_FLAG_INVISIBLE, thing.isFlagSet(StrifeThingFlags.INVISIBLE));
		}
		else
		{
			out.mapSet(UDMFCommonThingAttributes.ATTRIB_FLAG_AMBUSH, thing.isFlagSet(DoomThingFlags.AMBUSH));
			out.mapSet(UDMFCommonThingAttributes.ATTRIB_FLAG_SINGLE_PLAYER, !thing.isFlagSet(DoomThingFlags.NOT_SINGLEPLAYER));
			out.mapSet(UDMFCommonThingAttributes.ATTRIB_FLAG_COOPERATIVE, !thing.isFlagSet(BoomThingFlags.NOT_COOPERATIVE));
			out.mapSet(UDMFCommonThingAttributes.ATTRIB_FLAG_DEATHMATCH, !thing.isFlagSet(BoomThingFlags.NOT_DEATHMATCH));
			out.mapSet(UDMFMBFThingAttributes.ATTRIB_FLAG_FRIENDLY, thing.isFlagSet(MBFThingFlags.FRIENDLY));
		}
	}
	
	protected boolean mapToThing(ScriptValue in, DoomThing thing, boolean strife)
	{
		if (!in.isMap())
			return false;
	
		ScriptValue temp = CACHETEMP.get();
		try {
			if (in.mapGet(UDMFCommonThingAttributes.ATTRIB_POSITION_X, temp))
				thing.setX(temp.asInt());
			if (in.mapGet(UDMFCommonThingAttributes.ATTRIB_POSITION_Y, temp))
				thing.setY(temp.asInt());
			if (in.mapGet(UDMFCommonThingAttributes.ATTRIB_ANGLE, temp))
				thing.setAngle(temp.asInt());
			if (in.mapGet(UDMFCommonThingAttributes.ATTRIB_TYPE, temp))
				thing.setType(temp.asInt());

			thing.setFlags(0);
			
			if (in.mapGet("easy", temp))
				thing.setFlag(DoomThingFlags.EASY, temp.asBoolean());
			if (in.mapGet("medium", temp))
				thing.setFlag(DoomThingFlags.MEDIUM, temp.asBoolean());
			if (in.mapGet("hard", temp))
				thing.setFlag(DoomThingFlags.HARD, temp.asBoolean());

			if (strife)
			{
				if (in.mapGet(UDMFStrifeThingAttributes.ATTRIB_FLAG_AMBUSH, temp))
					thing.setFlag(StrifeThingFlags.AMBUSH, temp.asBoolean());
				if (in.mapGet(UDMFStrifeThingAttributes.ATTRIB_FLAG_SINGLE_PLAYER, temp))
					thing.setFlag(StrifeThingFlags.MULTIPLAYER, temp.asBoolean());
				if (in.mapGet(UDMFStrifeThingAttributes.ATTRIB_FLAG_STANDING, temp))
					thing.setFlag(StrifeThingFlags.STANDING, temp.asBoolean());
				if (in.mapGet(UDMFStrifeThingAttributes.ATTRIB_FLAG_ALLY, temp))
					thing.setFlag(StrifeThingFlags.ALLY, temp.asBoolean());
				if (in.mapGet(UDMFStrifeThingAttributes.ATTRIB_FLAG_TRANSLUCENT, temp))
					thing.setFlag(StrifeThingFlags.TRANSLUCENT_25, temp.asBoolean());
				if (in.mapGet(UDMFStrifeThingAttributes.ATTRIB_FLAG_INVISIBLE, temp))
					thing.setFlag(StrifeThingFlags.INVISIBLE, temp.asBoolean());
			}
			else
			{
				if (in.mapGet(UDMFCommonThingAttributes.ATTRIB_FLAG_AMBUSH, temp))
					thing.setFlag(DoomThingFlags.AMBUSH, temp.asBoolean());
				if (in.mapGet(UDMFCommonThingAttributes.ATTRIB_FLAG_SINGLE_PLAYER, temp))
					thing.setFlag(DoomThingFlags.NOT_SINGLEPLAYER, !temp.asBoolean());
				if (in.mapGet(UDMFCommonThingAttributes.ATTRIB_FLAG_COOPERATIVE, temp))
					thing.setFlag(BoomThingFlags.NOT_COOPERATIVE, !temp.asBoolean());
				if (in.mapGet(UDMFCommonThingAttributes.ATTRIB_FLAG_DEATHMATCH, temp))
					thing.setFlag(BoomThingFlags.NOT_DEATHMATCH, !temp.asBoolean());
				if (in.mapGet(UDMFCommonThingAttributes.ATTRIB_FLAG_FRIENDLY, temp))
					thing.setFlag(MBFThingFlags.FRIENDLY, temp.asBoolean());
			}
		} finally {
			temp.setNull();
		}
		return true;
	}
	
	protected void thingToMap(HexenThing thing, ScriptValue out)
	{
		out.setEmptyMap(20);
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_POSITION_X, thing.getX());
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_POSITION_Y, thing.getY());
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_ANGLE, thing.getAngle());
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_TYPE, thing.getType());
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_HEIGHT, thing.getZ());
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_ID, thing.getId());
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_SPECIAL, thing.getSpecial());

		out.mapSet(UDMFHexenThingAttributes.ATTRIB_ARG0, thing.getArgument(0));
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_ARG1, thing.getArgument(1));
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_ARG2, thing.getArgument(2));
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_ARG3, thing.getArgument(3));
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_ARG4, thing.getArgument(4));
		
		boolean easy = thing.isFlagSet(HexenThingFlags.EASY);
		boolean medium = thing.isFlagSet(HexenThingFlags.MEDIUM);
		boolean hard = thing.isFlagSet(HexenThingFlags.HARD);

		out.mapSet(UDMFHexenThingAttributes.ATTRIB_FLAG_SKILL1, easy);
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_FLAG_SKILL2, easy);
		out.mapSet("easy", easy);

		out.mapSet(UDMFHexenThingAttributes.ATTRIB_FLAG_SKILL3, medium);
		out.mapSet("medium", medium);

		out.mapSet(UDMFHexenThingAttributes.ATTRIB_FLAG_SKILL4, hard);
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_FLAG_SKILL5, hard);
		out.mapSet("hard", hard);

		out.mapSet(UDMFHexenThingAttributes.ATTRIB_FLAG_AMBUSH, thing.isFlagSet(HexenThingFlags.AMBUSH));
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_FLAG_SINGLE_PLAYER, thing.isFlagSet(HexenThingFlags.SINGLEPLAYER));
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_FLAG_COOPERATIVE, thing.isFlagSet(HexenThingFlags.COOPERATIVE));
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_FLAG_DEATHMATCH, thing.isFlagSet(HexenThingFlags.DEATHMATCH));
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_FLAG_CLASS1, thing.isFlagSet(HexenThingFlags.FIGHTER));
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_FLAG_CLASS2, thing.isFlagSet(HexenThingFlags.CLERIC));
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_FLAG_CLASS3, thing.isFlagSet(HexenThingFlags.MAGE));
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_FLAG_FRIENDLY, thing.isFlagSet(ZDoomThingFlags.FRIENDLY));
	}
	
	protected boolean mapToThing(ScriptValue in, HexenThing thing)
	{
		if (!in.isMap())
			return false;
	
		ScriptValue temp = CACHETEMP.get();
		try {
			if (in.mapGet(UDMFHexenThingAttributes.ATTRIB_POSITION_X, temp))
				thing.setX(temp.asInt());
			if (in.mapGet(UDMFHexenThingAttributes.ATTRIB_POSITION_Y, temp))
				thing.setY(temp.asInt());
			if (in.mapGet(UDMFHexenThingAttributes.ATTRIB_ANGLE, temp))
				thing.setAngle(temp.asInt());
			if (in.mapGet(UDMFHexenThingAttributes.ATTRIB_TYPE, temp))
				thing.setType(temp.asInt());
			if (in.mapGet(UDMFHexenThingAttributes.ATTRIB_HEIGHT, temp))
				thing.setZ(temp.asInt());
			if (in.mapGet(UDMFHexenThingAttributes.ATTRIB_ID, temp))
				thing.setId(temp.asInt());
			if (in.mapGet(UDMFHexenThingAttributes.ATTRIB_SPECIAL, temp))
				thing.setSpecial(temp.asInt());

			int arg0 = thing.getArgument(0);
			int arg1 = thing.getArgument(1);
			int arg2 = thing.getArgument(2);
			int arg3 = thing.getArgument(3);
			int arg4 = thing.getArgument(4);
			
			if (in.mapGet(UDMFHexenThingAttributes.ATTRIB_ARG0, temp))
				arg0 = temp.asInt();
			if (in.mapGet(UDMFHexenThingAttributes.ATTRIB_ARG1, temp))
				arg1 = temp.asInt();
			if (in.mapGet(UDMFHexenThingAttributes.ATTRIB_ARG2, temp))
				arg2 = temp.asInt();
			if (in.mapGet(UDMFHexenThingAttributes.ATTRIB_ARG3, temp))
				arg3 = temp.asInt();
			if (in.mapGet(UDMFHexenThingAttributes.ATTRIB_ARG4, temp))
				arg4 = temp.asInt();
			thing.setArguments(arg0, arg1, arg2, arg3, arg4);

			if (in.mapGet("easy", temp))
				thing.setFlag(HexenThingFlags.EASY, temp.asBoolean());
			if (in.mapGet("medium", temp))
				thing.setFlag(HexenThingFlags.MEDIUM, temp.asBoolean());
			if (in.mapGet("hard", temp))
				thing.setFlag(HexenThingFlags.HARD, temp.asBoolean());

			if (in.mapGet(UDMFHexenThingAttributes.ATTRIB_FLAG_AMBUSH, temp))
				thing.setFlag(HexenThingFlags.AMBUSH, temp.asBoolean());
			if (in.mapGet(UDMFHexenThingAttributes.ATTRIB_FLAG_SINGLE_PLAYER, temp))
				thing.setFlag(HexenThingFlags.SINGLEPLAYER, temp.asBoolean());
			if (in.mapGet(UDMFHexenThingAttributes.ATTRIB_FLAG_COOPERATIVE, temp))
				thing.setFlag(HexenThingFlags.COOPERATIVE, temp.asBoolean());
			if (in.mapGet(UDMFHexenThingAttributes.ATTRIB_FLAG_DEATHMATCH, temp))
				thing.setFlag(HexenThingFlags.DEATHMATCH, temp.asBoolean());
			if (in.mapGet(UDMFHexenThingAttributes.ATTRIB_FLAG_CLASS1, temp))
				thing.setFlag(HexenThingFlags.FIGHTER, temp.asBoolean());
			if (in.mapGet(UDMFHexenThingAttributes.ATTRIB_FLAG_CLASS2, temp))
				thing.setFlag(HexenThingFlags.CLERIC, temp.asBoolean());
			if (in.mapGet(UDMFHexenThingAttributes.ATTRIB_FLAG_CLASS3, temp))
				thing.setFlag(HexenThingFlags.MAGE, temp.asBoolean());
			if (in.mapGet(UDMFHexenThingAttributes.ATTRIB_FLAG_FRIENDLY, temp))
				thing.setFlag(ZDoomThingFlags.FRIENDLY, temp.asBoolean());
		} finally {
			temp.setNull();
		}
		return true;
	}
	
	protected void linedefToMap(DoomLinedef linedef, ScriptValue out)
	{
		out.setEmptyMap(20);
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_VERTEX_START, linedef.getVertexStartIndex());
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_VERTEX_END, linedef.getVertexEndIndex());
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_SPECIAL, linedef.getSpecial());
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_SIDEDEF_FRONT, linedef.getSidedefFrontIndex());
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_SIDEDEF_BACK, linedef.getSidedefBackIndex());
	
		// Common to Both Doom/Hexen
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_ID, linedef.getTag());
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_ARG0, linedef.getTag());
		
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_BLOCKING, linedef.isFlagSet(DoomLinedefFlags.IMPASSABLE));
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_TWO_SIDED, linedef.isFlagSet(DoomLinedefFlags.TWO_SIDED));
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_UNPEG_LOWER, linedef.isFlagSet(DoomLinedefFlags.UNPEG_LOWER));
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_UNPEG_TOP, linedef.isFlagSet(DoomLinedefFlags.UNPEG_UPPER));
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_BLOCK_MONSTERS, linedef.isFlagSet(DoomLinedefFlags.BLOCK_MONSTERS));
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_BLOCK_SOUND, linedef.isFlagSet(DoomLinedefFlags.BLOCK_SOUND));
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_DONT_DRAW, linedef.isFlagSet(DoomLinedefFlags.NOT_DRAWN));
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_MAPPED, linedef.isFlagSet(DoomLinedefFlags.MAPPED));
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_SECRET, linedef.isFlagSet(DoomLinedefFlags.SECRET));
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_PASSTHRU, linedef.isFlagSet(BoomLinedefFlags.PASSTHRU));
	
		// Strife Extensions
		out.mapSet(UDMFStrifeLinedefAttributes.ATTRIB_FLAG_BLOCK_FLOAT, linedef.isFlagSet(StrifeLinedefFlags.BLOCK_FLOATERS));
		out.mapSet(UDMFStrifeLinedefAttributes.ATTRIB_FLAG_JUMPOVER, linedef.isFlagSet(StrifeLinedefFlags.RAILING));
		out.mapSet(UDMFStrifeLinedefAttributes.ATTRIB_FLAG_TRANSLUCENT, linedef.isFlagSet(StrifeLinedefFlags.TRANSLUCENT));
	}

	protected boolean mapToLinedef(ScriptValue in, DoomLinedef linedef)
	{
		if (!in.isMap())
			return false;
	
		ScriptValue temp = CACHETEMP.get();
		try {
			if (in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_VERTEX_START, temp))
				linedef.setVertexStartIndex(temp.asInt()); 
			if (in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_VERTEX_END, temp))
				linedef.setVertexEndIndex(temp.asInt()); 
			if (in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_SPECIAL, temp))
				linedef.setSpecial(temp.asInt()); 
			if (in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_SIDEDEF_FRONT, temp))
				linedef.setSidedefFrontIndex(temp.asInt()); 
			if (in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_SIDEDEF_BACK, temp))
				linedef.setSidedefBackIndex(temp.asInt()); 

			if (in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_ID, temp))
				linedef.setTag(temp.asInt());
			if (in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_ARG0, temp))
				linedef.setTag(temp.asInt()); 

			if (in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_BLOCKING, temp))
				linedef.setFlag(DoomLinedefFlags.IMPASSABLE, temp.asBoolean());
			if (in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_TWO_SIDED, temp))
				linedef.setFlag(DoomLinedefFlags.TWO_SIDED, temp.asBoolean());
			if (in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_UNPEG_LOWER, temp))
				linedef.setFlag(DoomLinedefFlags.UNPEG_LOWER, temp.asBoolean());
			if (in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_UNPEG_TOP, temp))
				linedef.setFlag(DoomLinedefFlags.UNPEG_UPPER, temp.asBoolean());
			if (in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_BLOCK_MONSTERS, temp))
				linedef.setFlag(DoomLinedefFlags.BLOCK_MONSTERS, temp.asBoolean());
			if (in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_BLOCK_SOUND, temp))
				linedef.setFlag(DoomLinedefFlags.BLOCK_SOUND, temp.asBoolean());
			if (in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_DONT_DRAW, temp))
				linedef.setFlag(DoomLinedefFlags.NOT_DRAWN, temp.asBoolean());
			if (in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_MAPPED, temp))
				linedef.setFlag(DoomLinedefFlags.MAPPED, temp.asBoolean());
			if (in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_SECRET, temp))
				linedef.setFlag(DoomLinedefFlags.SECRET, temp.asBoolean());
			if (in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_PASSTHRU, temp))
				linedef.setFlag(BoomLinedefFlags.PASSTHRU, temp.asBoolean());
			
			if (in.mapGet(UDMFStrifeLinedefAttributes.ATTRIB_FLAG_BLOCK_FLOAT, temp))
				linedef.setFlag(StrifeLinedefFlags.BLOCK_FLOATERS, temp.asBoolean());
			if (in.mapGet(UDMFStrifeLinedefAttributes.ATTRIB_FLAG_JUMPOVER, temp))
				linedef.setFlag(StrifeLinedefFlags.RAILING, temp.asBoolean());
			if (in.mapGet(UDMFStrifeLinedefAttributes.ATTRIB_FLAG_TRANSLUCENT, temp))
				linedef.setFlag(StrifeLinedefFlags.TRANSLUCENT, temp.asBoolean());
		} finally {
			temp.setNull();
		}
		return true;
	}
	
	protected void linedefToMap(HexenLinedef linedef, ScriptValue out)
	{
		out.setEmptyMap(24);
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_VERTEX_START, linedef.getVertexStartIndex());
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_VERTEX_END, linedef.getVertexEndIndex());
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_SPECIAL, linedef.getSpecial());
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_SIDEDEF_FRONT, linedef.getSidedefFrontIndex());
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_SIDEDEF_BACK, linedef.getSidedefBackIndex());
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_BLOCKING, linedef.isFlagSet(HexenLinedefFlags.IMPASSABLE));
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_TWO_SIDED, linedef.isFlagSet(HexenLinedefFlags.TWO_SIDED));
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_UNPEG_LOWER, linedef.isFlagSet(HexenLinedefFlags.UNPEG_LOWER));
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_UNPEG_TOP, linedef.isFlagSet(HexenLinedefFlags.UNPEG_UPPER));
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_BLOCK_MONSTERS, linedef.isFlagSet(HexenLinedefFlags.BLOCK_MONSTERS));
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_BLOCK_SOUND, linedef.isFlagSet(HexenLinedefFlags.BLOCK_SOUND));
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_DONT_DRAW, linedef.isFlagSet(HexenLinedefFlags.NOT_DRAWN));
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_MAPPED, linedef.isFlagSet(HexenLinedefFlags.MAPPED));
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_SECRET, linedef.isFlagSet(HexenLinedefFlags.SECRET));
	
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_ARG0, linedef.getArgument(0));
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_ARG1, linedef.getArgument(1));
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_ARG2, linedef.getArgument(2));
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_ARG3, linedef.getArgument(3));
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_ARG4, linedef.getArgument(4));
	
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_REPEATABLE, linedef.isFlagSet(HexenLinedefFlags.REPEATABLE));
		out.mapSet(UDMFZDoomLinedefAttributes.ATTRIB_FLAG_BLOCK_PLAYERS, linedef.isFlagSet(ZDoomLinedefFlags.BLOCK_PLAYERS));
		out.mapSet(UDMFZDoomLinedefAttributes.ATTRIB_FLAG_BLOCK_EVERYTHING, linedef.isFlagSet(ZDoomLinedefFlags.BLOCK_EVERYTHING));
		
		switch (linedef.getActivationType())
		{
			case HexenLinedef.ACTIVATION_PLAYER_CROSSES:
				out.mapSet(UDMFZDoomLinedefAttributes.ATTRIB_ACTIVATE_PLAYER_CROSS, true);
				break;
			case HexenLinedef.ACTIVATION_PLAYER_USES:
				out.mapSet(UDMFZDoomLinedefAttributes.ATTRIB_ACTIVATE_PLAYER_USE, true);
				break;
			case HexenLinedef.ACTIVATION_MONSTER_CROSSES:
				out.mapSet(UDMFZDoomLinedefAttributes.ATTRIB_ACTIVATE_MONSTER_CROSS, true);
				break;
			case HexenLinedef.ACTIVATION_PROJECTILE_HITS:
				out.mapSet(UDMFZDoomLinedefAttributes.ATTRIB_ACTIVATE_IMPACT, true);
				break;
			case HexenLinedef.ACTIVATION_PLAYER_BUMPS:
				out.mapSet(UDMFZDoomLinedefAttributes.ATTRIB_ACTIVATE_PLAYER_PUSH, true);
				break;
			case HexenLinedef.ACTIVATION_PROJECTILE_CROSSES:
				out.mapSet(UDMFZDoomLinedefAttributes.ATTRIB_ACTIVATE_PROJECTILE_CROSS, true);
				break;
			case HexenLinedef.ACTIVATION_PLAYER_USES_PASSTHRU:
				out.mapSet(UDMFZDoomLinedefAttributes.ATTRIB_ACTIVATE_PLAYER_USE, true);
				out.mapSet(UDMFZDoomLinedefAttributes.ATTRIB_FLAG_PASSTHRU, true);
				break;
			default:
				break;
		}
	}
	
	protected boolean mapToLinedef(ScriptValue in, HexenLinedef linedef)
	{
		if (!in.isMap())
			return false;
	
		ScriptValue temp = CACHETEMP.get();
		try {
			// TODO: Finish this.
		} finally {
			temp.setNull();
		}
		return true;
	}
	
	// Threadlocal "stack" values.
	private static final ThreadLocal<ScriptValue> CACHEVALUE1 = ThreadLocal.withInitial(()->ScriptValue.create(null));
	private static final ThreadLocal<ScriptValue> CACHEVALUE2 = ThreadLocal.withInitial(()->ScriptValue.create(null));
	private static final ThreadLocal<ScriptValue> CACHETEMP = ThreadLocal.withInitial(()->ScriptValue.create(null));

}

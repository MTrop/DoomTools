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
import net.mtrop.doom.exception.MapException;
import net.mtrop.doom.map.DoomMap;
import net.mtrop.doom.map.HexenMap;
import net.mtrop.doom.map.MapFormat;
import net.mtrop.doom.map.MapObjectConstants;
import net.mtrop.doom.map.MapView;
import net.mtrop.doom.map.UDMFMap;
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
import net.mtrop.doom.map.udmf.attributes.UDMFDoomLinedefAttributes;
import net.mtrop.doom.map.udmf.attributes.UDMFDoomSectorAttributes;
import net.mtrop.doom.map.udmf.attributes.UDMFDoomSidedefAttributes;
import net.mtrop.doom.map.udmf.attributes.UDMFDoomThingAttributes;
import net.mtrop.doom.map.udmf.attributes.UDMFDoomVertexAttributes;
import net.mtrop.doom.map.udmf.attributes.UDMFHexenLinedefAttributes;
import net.mtrop.doom.map.udmf.attributes.UDMFHexenThingAttributes;
import net.mtrop.doom.map.udmf.attributes.UDMFMBFThingAttributes;
import net.mtrop.doom.map.udmf.attributes.UDMFStrifeLinedefAttributes;
import net.mtrop.doom.map.udmf.attributes.UDMFStrifeThingAttributes;
import net.mtrop.doom.map.udmf.attributes.UDMFZDoomLinedefAttributes;
import net.mtrop.doom.util.MapUtils;
import net.mtrop.doom.util.NameUtils;

import static com.blackrook.rookscript.lang.ScriptFunctionUsage.type;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Script functions for WAD.
 * @author Matthew Tropiano
 */
public enum DoomMapFunctions implements ScriptFunctionType
{
	MAPVIEW(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Loads a Doom Map into memory for inspection as a MapView. The map in the Wad can be " +
					"in Doom or Hexen or UDMF format."
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
					type(Type.ERROR, "BadMap", "If a map could not be read from the data."),
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
					Wad wad = temp.asObjectType(Wad.class);
					int index = entry.asInt();
					try
					{
						MapFormat format = MapUtils.getMapFormat(wad, index);
						if (format == null)
						{
							returnValue.setNull();
							return true;
						}

						switch (format)
						{
							default:
								break;
							case DOOM:
								returnValue.set(MapUtils.createDoomMap(wad, index));
								break;
							case HEXEN:
								returnValue.set(MapUtils.createHexenMap(wad, index));
								break;
							case UDMF:
								returnValue.set(MapUtils.createUDMFMap(wad, index));
								break;
						}
					} 
					catch (MapException e) 
					{
						returnValue.setError("BadMap", "Map information is malformed.");
					} 
					catch (IOException e)
					{
						returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
					}
					return true;
				}
				else
				{
					Wad wad = temp.asObjectType(Wad.class);
					String name = entry.asString();
					try
					{
						MapFormat format = MapUtils.getMapFormat(wad, name);
						if (format == null)
						{
							returnValue.setNull();
							return true;
						}

						switch (format)
						{
							default:
								break;
							case DOOM:
								returnValue.set(MapUtils.createDoomMap(wad, name));
								break;
							case HEXEN:
								returnValue.set(MapUtils.createHexenMap(wad, name));
								break;
							case UDMF:
								returnValue.set(MapUtils.createUDMFMap(wad, name));
								break;
						}
					} 
					catch (MapException e) 
					{
						returnValue.setError("BadMap", "Map information is malformed.");
					} 
					catch (IOException e)
					{
						returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
					}
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
	
	MAPINFO(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Loads a Doom Map into memory for inspection as a MapView. The map in the Wad can be " +
					"in Doom or Hexen or UDMF format."
				)
				.parameter("wad", 
					type(Type.OBJECTREF, "Wad", "An open Wad.")
				)
				.parameter("header", 
					type(Type.INTEGER, "The entry index of the map's header."),
					type(Type.STRING, "The name of the map entry to read.")
				)
				.returns(
					type(Type.STRING, "The map type."),
					type(Type.ERROR, "BadParameter", "If [wad] is not a valid open Wad file.")
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
					Wad wad = temp.asObjectType(Wad.class);
					int index = entry.asInt();
					MapFormat format = MapUtils.getMapFormat(wad, index);
					if (format == null)
						returnValue.setNull();
					else
						returnValue.set(format.name().toLowerCase());
					return true;
				}
				else
				{
					Wad wad = temp.asObjectType(Wad.class);
					String name = entry.asString();
					MapFormat format = MapUtils.getMapFormat(wad, name);
					if (format == null)
						returnValue.setNull();
					else
						returnValue.set(format.name().toLowerCase());
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
	
	MAPVIEWINFO(1)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Returns a map of info about a MapView."
				)
				.parameter("mapview", 
					type(Type.OBJECTREF, "MapView", "The map view to use.")
				)
				.returns(
					type(Type.MAP, "{type:STRING, thingcount:INTEGER, vertexcount:INTEGER, linedefcount:INTEGER, sidedefcount:INTEGER, sectorcount:INTEGER}", "Information on the provided map."),
					type(Type.ERROR, "BadParameter", "If [mapview] is not a valid MapView.")
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
				if (!temp.isObjectRef(MapView.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a MapView.");
					return true;
				}

				MapView<?,?,?,?,?> mapView = temp.asObjectType(MapView.class);
				
				returnValue.setEmptyMap();
				if (mapView instanceof DoomMap)
					returnValue.mapSet("type", MapFormat.DOOM.name().toLowerCase());
				else if (mapView instanceof HexenMap)
					returnValue.mapSet("type", MapFormat.HEXEN.name().toLowerCase());
				else if (mapView instanceof UDMFMap)
					returnValue.mapSet("type", MapFormat.UDMF.name().toLowerCase());
				else
				{
					returnValue.setNull();
					return true;
				}
				
				returnValue.mapSet("linedefcount", mapView.getLinedefCount());
				returnValue.mapSet("sectorcount", mapView.getSectorCount());
				returnValue.mapSet("sidedefcount", mapView.getSidedefCount());
				returnValue.mapSet("thingcount", mapView.getThingCount());
				returnValue.mapSet("vertexcount", mapView.getVertexCount());
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	THING(3)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Fetches a thing from a MapView and returns it as a map."
				)
				.parameter("mapview", 
					type(Type.OBJECTREF, "MapView", "The map view to use.")
				)
				.parameter("index", 
					type(Type.INTEGER, "The index of the thing to retrieve.")
				)
				.parameter("strife", 
					type(Type.BOOLEAN, "If true, interpret this thing as a Strife thing (different flags).")
				)
				.returns(
					type(Type.NULL, "If [index] is less than 0 or greater than or equal to the amount of things in the MapView."),
					type(Type.MAP, "A map with Thing data."),
					type(Type.ERROR, "BadParameter", "If [mapview] is not a valid MapView.")
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
				boolean strife = temp.asBoolean();
				scriptInstance.popStackValue(temp);
				int index = temp.asInt();
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(MapView.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a MapView.");
					return true;
				}

				Object thing = temp.asObjectType(MapView.class).getThing(index);
				if (thing instanceof DoomThing)
					thingToMap((DoomThing)thing, strife, returnValue);
				else
					mapElementToMap(thing, returnValue);
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	THINGS(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Creates an iterator that iterates through each thing in the provided MapView. " +
					"The value that this produces can be used in an each(...) loop. The key is the index (starts at 0), and " +
					"values are maps (see THING())."
				)
				.parameter("mapview", 
					type(Type.OBJECTREF, "MapView", "The map view to use.")
				)
				.parameter("strife", 
					type(Type.BOOLEAN, "If true, interpret each thing as a Strife thing (different flags).")
				)
				.returns(
					type(Type.OBJECTREF, "ScriptIteratorType", "The iterator returned."),
					type(Type.ERROR, "BadParameter", "If [mapview] is not a valid MapView.")
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
				boolean strife = temp.asBoolean();
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(MapView.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a MapView.");
					return true;
				}

				returnValue.set(new ThingIterator(temp.asObjectType(MapView.class), strife));
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	VERTEX(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Fetches a vertex from a MapView and returns it as a map."
				)
				.parameter("mapview", 
					type(Type.OBJECTREF, "MapView", "The map view to use.")
				)
				.parameter("index", 
					type(Type.INTEGER, "The index of the vertex to retrieve.")
				)
				.returns(
					type(Type.NULL, "If [index] is less than 0 or greater than or equal to the amount of vertices in the MapView."),
					type(Type.MAP, "A map with Vertex data."),
					type(Type.ERROR, "BadParameter", "If [mapview] is not a valid MapView.")
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
				int index = temp.asInt();
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(MapView.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a MapView.");
					return true;
				}

				mapElementToMap(temp.asObjectType(MapView.class).getVertex(index), returnValue);
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	VERTICES(1)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Creates an iterator that iterates through each vertex in the provided MapView. " +
					"The value that this produces can be used in an each(...) loop. The key is the index (starts at 0), and " +
					"values are maps (see VERTEX())."
				)
				.parameter("mapview", 
					type(Type.OBJECTREF, "MapView", "The map view to use.")
				)
				.returns(
					type(Type.OBJECTREF, "ScriptIteratorType", "The iterator returned."),
					type(Type.ERROR, "BadParameter", "If [mapview] is not a valid MapView.")
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
				if (!temp.isObjectRef(MapView.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a MapView.");
					return true;
				}

				returnValue.set(new VertexIterator(temp.asObjectType(MapView.class)));
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	LINEDEF(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Fetches a linedef from a MapView and returns it as a map."
				)
				.parameter("mapview", 
					type(Type.OBJECTREF, "MapView", "The map view to use.")
				)
				.parameter("index", 
					type(Type.INTEGER, "The index of the linedef to retrieve.")
				)
				.returns(
					type(Type.NULL, "If [index] is less than 0 or greater than or equal to the amount of linedef in the MapView."),
					type(Type.MAP, "A map with Linedef data."),
					type(Type.ERROR, "BadParameter", "If [mapview] is not a valid MapView.")
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
				int index = temp.asInt();
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(MapView.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a MapView.");
					return true;
				}

				mapElementToMap(temp.asObjectType(MapView.class).getLinedef(index), returnValue);
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	LINEDEFS(1)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Creates an iterator that iterates through each linedef in the provided MapView. " +
					"The value that this produces can be used in an each(...) loop. The key is the index (starts at 0), and " +
					"values are maps (see LINEDEF())."
				)
				.parameter("mapview", 
					type(Type.OBJECTREF, "MapView", "The map view to use.")
				)
				.returns(
					type(Type.OBJECTREF, "ScriptIteratorType", "The iterator returned."),
					type(Type.ERROR, "BadParameter", "If [mapview] is not a valid MapView.")
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
				if (!temp.isObjectRef(MapView.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a MapView.");
					return true;
				}

				returnValue.set(new LinedefIterator(temp.asObjectType(MapView.class)));
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	SIDEDEF(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Fetches a sidedef from a MapView and returns it as a map."
				)
				.parameter("mapview", 
					type(Type.OBJECTREF, "MapView", "The map view to use.")
				)
				.parameter("index", 
					type(Type.INTEGER, "The index of the sidedef to retrieve.")
				)
				.returns(
					type(Type.NULL, "If [index] is less than 0 or greater than or equal to the amount of sidedef in the MapView."),
					type(Type.MAP, "A map with Sidedef data."),
					type(Type.ERROR, "BadParameter", "If [mapview] is not a valid MapView.")
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
				int index = temp.asInt();
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(MapView.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a MapView.");
					return true;
				}

				mapElementToMap(temp.asObjectType(MapView.class).getSidedef(index), returnValue);
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	SIDEDEFS(1)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Creates an iterator that iterates through each sidedef in the provided MapView. " +
					"The value that this produces can be used in an each(...) loop. The key is the index (starts at 0), and " +
					"values are maps (see SIDEDEF())."
				)
				.parameter("mapview", 
					type(Type.OBJECTREF, "MapView", "The map view to use.")
				)
				.returns(
					type(Type.OBJECTREF, "ScriptIteratorType", "The iterator returned."),
					type(Type.ERROR, "BadParameter", "If [mapview] is not a valid MapView.")
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
				if (!temp.isObjectRef(MapView.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a MapView.");
					return true;
				}

				returnValue.set(new SidedefIterator(temp.asObjectType(MapView.class)));
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	SECTOR(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Fetches a sector from a MapView and returns it as a map."
				)
				.parameter("mapview", 
					type(Type.OBJECTREF, "MapView", "The map view to use.")
				)
				.parameter("index", 
					type(Type.INTEGER, "The index of the sector to retrieve.")
				)
				.returns(
					type(Type.NULL, "If [index] is less than 0 or greater than or equal to the amount of sector in the MapView."),
					type(Type.MAP, "A map with Sector data."),
					type(Type.ERROR, "BadParameter", "If [mapview] is not a valid MapView.")
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
				int index = temp.asInt();
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(MapView.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a MapView.");
					return true;
				}

				mapElementToMap(temp.asObjectType(MapView.class).getSector(index), returnValue);
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	SECTORS(1)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Creates an iterator that iterates through each sector in the provided MapView. " +
					"The value that this produces can be used in an each(...) loop. The key is the index (starts at 0), and " +
					"values are maps (see SECTOR())."
				)
				.parameter("mapview", 
					type(Type.OBJECTREF, "MapView", "The map view to use.")
				)
				.returns(
					type(Type.OBJECTREF, "ScriptIteratorType", "The iterator returned."),
					type(Type.ERROR, "BadParameter", "If [mapview] is not a valid MapView.")
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
				if (!temp.isObjectRef(MapView.class))
				{
					returnValue.setError("BadParameter", "First parameter is not a MapView.");
					return true;
				}

				returnValue.set(new SectorIterator(temp.asObjectType(MapView.class)));
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	READTHING(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Reads a single Doom-formatted Thing from an input stream of some kind ("+DoomThing.LENGTH+" bytes) and " +
					"returns the information as a map (like THING())."
				)
				.parameter("input", 
					type(Type.OBJECTREF, "InputStream", "A readable input stream (also DataInputStream).")
				)
				.parameter("strife", 
					type(Type.BOOLEAN, "If true, interpret the thing as a Strife thing (different flags).")
				)
				.returns(
					type(Type.MAP, "A map with Thing data."),
					type(Type.ERROR, "BadParameter", "If [input] is not a valid input stream."),
					type(Type.ERROR, "IOError", "If a read error occurs.")
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
				boolean strife = temp.asBoolean();
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(InputStream.class))
				{
					returnValue.setError("BadParameter", "First parameter is not an InputStream.");
					return true;
				}

				DoomThing obj = CACHEDOOMTHING.get();
				
				try {
					obj.readBytes(temp.asObjectType(InputStream.class));
				} catch (IOException e) {
					returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
					return true;
				}
				
				thingToMap(obj, strife, returnValue);
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	WRITETHING(3)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Writes a single Doom-formatted Thing to an output stream of some kind ("+DoomThing.LENGTH+" bytes)." +
					"Input map requires the fields that would be returned by READTHING()."
				)
				.parameter("output", 
					type(Type.OBJECTREF, "OutputStream", "A readable output stream (also DataOutputStream).")
				)
				.parameter("data", 
					type(Type.MAP, "The map of thing data/fields. If any field is missing or null, a default value is used.")
				)
				.parameter("strife", 
					type(Type.BOOLEAN, "If true, interpret the thing map as a Strife thing (different flags).")
				)
				.returns(
					type(Type.OBJECTREF, "OutputStream", "[output]."),
					type(Type.ERROR, "BadParameter", "If [output] is not a valid output stream, or [data] is not a map."),
					type(Type.ERROR, "BadData", "If the [data] has a field that does not fit a required range or format."),
					type(Type.ERROR, "IOError", "If a write error occurs.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue map = CACHEVALUE2.get();
			try 
			{
				scriptInstance.popStackValue(temp);
				boolean strife = temp.asBoolean();
				scriptInstance.popStackValue(map);
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(OutputStream.class))
				{
					returnValue.setError("BadParameter", "First parameter is not an OutputStream.");
					return true;
				}
				if (!map.isMap())
				{
					returnValue.setError("BadParameter", "Second parameter is not a map.");
					return true;
				}

				DoomThing obj = CACHEDOOMTHING.get();
				
				try {
					mapToThing(map, obj, strife);
					obj.writeBytes(temp.asObjectType(OutputStream.class));
				} catch (IllegalArgumentException e) {
					returnValue.setError("BadData", e.getMessage(), e.getLocalizedMessage());
					return true;
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
				map.setNull();
			}
		}
	},
	
	READHEXENTHING(1)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Reads a single Hexen/ZDoom-formatted Thing from an input stream of some kind ("+HexenThing.LENGTH+" bytes) and " +
					"returns the information as a map (like THING())."
				)
				.parameter("input", 
					type(Type.OBJECTREF, "InputStream", "A readable input stream (also DataInputStream).")
				)
				.returns(
					type(Type.MAP, "A map with Thing data."),
					type(Type.ERROR, "BadParameter", "If [input] is not a valid input stream."),
					type(Type.ERROR, "IOError", "If a read error occurs.")
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
				if (!temp.isObjectRef(InputStream.class))
				{
					returnValue.setError("BadParameter", "First parameter is not an InputStream.");
					return true;
				}

				HexenThing obj = CACHEHEXENTHING.get();
				
				try {
					obj.readBytes(temp.asObjectType(InputStream.class));
				} catch (IOException e) {
					returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
					return true;
				}
				
				thingToMap(obj, returnValue);
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	WRITEHEXENTHING(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Writes a single Hexen/ZDoom-formatted Thing to an output stream of some kind ("+DoomThing.LENGTH+" bytes)." +
					"Input map requires the fields that would be returned by READHEXENTHING()."
				)
				.parameter("output", 
					type(Type.OBJECTREF, "OutputStream", "A readable output stream (also DataOutputStream).")
				)
				.parameter("data", 
					type(Type.MAP, "The map of thing data/fields. If any field is missing or null, a default value is used.")
				)
				.returns(
					type(Type.OBJECTREF, "OutputStream", "[output]."),
					type(Type.ERROR, "BadParameter", "If [output] is not a valid output stream, or [data] is not a map."),
					type(Type.ERROR, "BadData", "If the [data] has a field that does not fit a required range or format."),
					type(Type.ERROR, "IOError", "If a write error occurs.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue map = CACHEVALUE2.get();
			try 
			{
				scriptInstance.popStackValue(map);
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(OutputStream.class))
				{
					returnValue.setError("BadParameter", "First parameter is not an OutputStream.");
					return true;
				}
				if (!map.isMap())
				{
					returnValue.setError("BadParameter", "Second parameter is not a map.");
					return true;
				}

				HexenThing obj = CACHEHEXENTHING.get();
				
				try {
					mapToThing(map, obj);
					obj.writeBytes(temp.asObjectType(OutputStream.class));
				} catch (IllegalArgumentException e) {
					returnValue.setError("BadData", e.getMessage(), e.getLocalizedMessage());
					return true;
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
				map.setNull();
			}
		}
	},
	
	READVERTEX(1)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Reads a single Vertex from an input stream of some kind ("+DoomVertex.LENGTH+" bytes) and " +
					"returns the information as a map (like VERTEX())."
				)
				.parameter("input", 
					type(Type.OBJECTREF, "InputStream", "A readable input stream (also DataInputStream).")
				)
				.returns(
					type(Type.MAP, "A map with Vertex data."),
					type(Type.ERROR, "BadParameter", "If [input] is not a valid input stream."),
					type(Type.ERROR, "IOError", "If a read error occurs.")
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
				if (!temp.isObjectRef(InputStream.class))
				{
					returnValue.setError("BadParameter", "First parameter is not an InputStream.");
					return true;
				}

				DoomVertex obj = CACHEDOOMVERTEX.get();
				
				try {
					obj.readBytes(temp.asObjectType(InputStream.class));
				} catch (IOException e) {
					returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
					return true;
				}
				
				vertexToMap(obj, returnValue);
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	WRITEVERTEX(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Writes a single Vertex to an output stream of some kind ("+DoomVertex.LENGTH+" bytes)." +
					"Input map requires the fields that would be returned by READVERTEX()."
				)
				.parameter("output", 
					type(Type.OBJECTREF, "OutputStream", "A readable output stream (also DataOutputStream).")
				)
				.parameter("data", 
					type(Type.MAP, "The map of thing data/fields. If any field is missing or null, a default value is used.")
				)
				.returns(
					type(Type.OBJECTREF, "OutputStream", "[output]."),
					type(Type.ERROR, "BadParameter", "If [output] is not a valid output stream, or [data] is not a map."),
					type(Type.ERROR, "BadData", "If the [data] has a field that does not fit a required range or format."),
					type(Type.ERROR, "IOError", "If a write error occurs.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue map = CACHEVALUE2.get();
			try 
			{
				scriptInstance.popStackValue(map);
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(OutputStream.class))
				{
					returnValue.setError("BadParameter", "First parameter is not an OutputStream.");
					return true;
				}
				if (!map.isMap())
				{
					returnValue.setError("BadParameter", "Second parameter is not a map.");
					return true;
				}

				DoomVertex obj = CACHEDOOMVERTEX.get();
				
				try {
					mapToVertex(map, obj);
					obj.writeBytes(temp.asObjectType(OutputStream.class));
				} catch (IllegalArgumentException e) {
					returnValue.setError("BadData", e.getMessage(), e.getLocalizedMessage());
					return true;
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
				map.setNull();
			}
		}
	},

	READLINEDEF(1)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Reads a single Doom-formatted Linedef from an input stream of some kind ("+DoomLinedef.LENGTH+" bytes) and " +
					"returns the information as a map (like LINEDEF())."
				)
				.parameter("input", 
					type(Type.OBJECTREF, "InputStream", "A readable input stream (also DataInputStream).")
				)
				.returns(
					type(Type.MAP, "A map with Linedef data."),
					type(Type.ERROR, "BadParameter", "If [input] is not a valid input stream."),
					type(Type.ERROR, "IOError", "If a read error occurs.")
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
				if (!temp.isObjectRef(InputStream.class))
				{
					returnValue.setError("BadParameter", "First parameter is not an InputStream.");
					return true;
				}

				DoomLinedef obj = CACHEDOOMLINEDEF.get();
				
				try {
					obj.readBytes(temp.asObjectType(InputStream.class));
				} catch (IOException e) {
					returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
					return true;
				}
				
				linedefToMap(obj, returnValue);
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	WRITELINEDEF(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Writes a single Doom-formatted Linedef to an output stream of some kind ("+DoomLinedef.LENGTH+" bytes)." +
					"Input map requires the fields that would be returned by READLINEDEF()."
				)
				.parameter("output", 
					type(Type.OBJECTREF, "OutputStream", "A readable output stream (also DataOutputStream).")
				)
				.parameter("data", 
					type(Type.MAP, "The map of thing data/fields. If any field is missing or null, a default value is used.")
				)
				.returns(
					type(Type.OBJECTREF, "OutputStream", "[output]."),
					type(Type.ERROR, "BadParameter", "If [output] is not a valid output stream, or [data] is not a map."),
					type(Type.ERROR, "BadData", "If the [data] has a field that does not fit a required range or format."),
					type(Type.ERROR, "IOError", "If a write error occurs.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue map = CACHEVALUE2.get();
			try 
			{
				scriptInstance.popStackValue(map);
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(OutputStream.class))
				{
					returnValue.setError("BadParameter", "First parameter is not an OutputStream.");
					return true;
				}
				if (!map.isMap())
				{
					returnValue.setError("BadParameter", "Second parameter is not a map.");
					return true;
				}

				DoomLinedef obj = CACHEDOOMLINEDEF.get();
				
				try {
					mapToLinedef(map, obj);
					obj.writeBytes(temp.asObjectType(OutputStream.class));
				} catch (IllegalArgumentException e) {
					returnValue.setError("BadData", e.getMessage(), e.getLocalizedMessage());
					return true;
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
				map.setNull();
			}
		}
	},

	READHEXENLINEDEF(1)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Reads a single Hexen/ZDoom-formatted Linedef from an input stream of some kind ("+HexenLinedef.LENGTH+" bytes) and " +
					"returns the information as a map (like LINEDEF())."
				)
				.parameter("input", 
					type(Type.OBJECTREF, "InputStream", "A readable input stream (also DataInputStream).")
				)
				.returns(
					type(Type.MAP, "A map with Linedef data."),
					type(Type.ERROR, "BadParameter", "If [input] is not a valid input stream."),
					type(Type.ERROR, "IOError", "If a read error occurs.")
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
				if (!temp.isObjectRef(InputStream.class))
				{
					returnValue.setError("BadParameter", "First parameter is not an InputStream.");
					return true;
				}

				HexenLinedef obj = CACHEHEXENLINEDEF.get();
				
				try {
					obj.readBytes(temp.asObjectType(InputStream.class));
				} catch (IOException e) {
					returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
					return true;
				}
				
				linedefToMap(obj, returnValue);
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	WRITEHEXENLINEDEF(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Writes a single Hexen/ZDoom-formatted Linedef to an output stream of some kind ("+HexenLinedef.LENGTH+" bytes)." +
					"Input map requires the fields that would be returned by READHEXENLINEDEF()."
				)
				.parameter("output", 
					type(Type.OBJECTREF, "OutputStream", "A readable output stream (also DataOutputStream).")
				)
				.parameter("data", 
					type(Type.MAP, "The map of thing data/fields. If any field is missing or null, a default value is used.")
				)
				.returns(
					type(Type.OBJECTREF, "OutputStream", "[output]."),
					type(Type.ERROR, "BadParameter", "If [output] is not a valid output stream, or [data] is not a map."),
					type(Type.ERROR, "BadData", "If the [data] has a field that does not fit a required range or format."),
					type(Type.ERROR, "IOError", "If a write error occurs.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue map = CACHEVALUE2.get();
			try 
			{
				scriptInstance.popStackValue(map);
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(OutputStream.class))
				{
					returnValue.setError("BadParameter", "First parameter is not an OutputStream.");
					return true;
				}
				if (!map.isMap())
				{
					returnValue.setError("BadParameter", "Second parameter is not a map.");
					return true;
				}

				HexenLinedef obj = CACHEHEXENLINEDEF.get();
				
				try {
					mapToLinedef(map, obj);
					obj.writeBytes(temp.asObjectType(OutputStream.class));
				} catch (IllegalArgumentException e) {
					returnValue.setError("BadData", e.getMessage(), e.getLocalizedMessage());
					return true;
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
				map.setNull();
			}
		}
	},

	READSIDEDEF(1)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Reads a single Sidedef from an input stream of some kind ("+DoomSidedef.LENGTH+" bytes) and " +
					"returns the information as a map (like SIDEDEF())."
				)
				.parameter("input", 
					type(Type.OBJECTREF, "InputStream", "A readable input stream (also DataInputStream).")
				)
				.returns(
					type(Type.MAP, "A map with Sidedef data."),
					type(Type.ERROR, "BadParameter", "If [input] is not a valid input stream."),
					type(Type.ERROR, "IOError", "If a read error occurs.")
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
				if (!temp.isObjectRef(InputStream.class))
				{
					returnValue.setError("BadParameter", "First parameter is not an InputStream.");
					return true;
				}

				DoomSidedef obj = CACHEDOOMSIDEDEF.get();
				
				try {
					obj.readBytes(temp.asObjectType(InputStream.class));
				} catch (IOException e) {
					returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
					return true;
				}
				
				sidedefToMap(obj, returnValue);
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	WRITESIDEDEF(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Writes a single Sidedef to an output stream of some kind ("+DoomSidedef.LENGTH+" bytes)." +
					"Input map requires the fields that would be returned by READSIDEDEF()."
				)
				.parameter("output", 
					type(Type.OBJECTREF, "OutputStream", "A readable output stream (also DataOutputStream).")
				)
				.parameter("data", 
					type(Type.MAP, "The map of thing data/fields. If any field is missing or null, a default value is used.")
				)
				.returns(
					type(Type.OBJECTREF, "OutputStream", "[output]."),
					type(Type.ERROR, "BadParameter", "If [output] is not a valid output stream, or [data] is not a map."),
					type(Type.ERROR, "BadData", "If the [data] has a field that does not fit a required range or format."),
					type(Type.ERROR, "IOError", "If a write error occurs.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue map = CACHEVALUE2.get();
			try 
			{
				scriptInstance.popStackValue(map);
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(OutputStream.class))
				{
					returnValue.setError("BadParameter", "First parameter is not an OutputStream.");
					return true;
				}
				if (!map.isMap())
				{
					returnValue.setError("BadParameter", "Second parameter is not a map.");
					return true;
				}

				DoomSidedef obj = CACHEDOOMSIDEDEF.get();
				
				try {
					mapToSidedef(map, obj);
					obj.writeBytes(temp.asObjectType(OutputStream.class));
				} catch (IllegalArgumentException e) {
					returnValue.setError("BadData", e.getMessage(), e.getLocalizedMessage());
					return true;
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
				map.setNull();
			}
		}
	},

	READSECTOR(1)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Reads a single Sector from an input stream of some kind ("+DoomSector.LENGTH+" bytes) and " +
					"returns the information as a map (like SECTOR())."
				)
				.parameter("input", 
					type(Type.OBJECTREF, "InputStream", "A readable input stream (also DataInputStream).")
				)
				.returns(
					type(Type.MAP, "A map with Sector data."),
					type(Type.ERROR, "BadParameter", "If [input] is not a valid input stream."),
					type(Type.ERROR, "IOError", "If a read error occurs.")
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
				if (!temp.isObjectRef(InputStream.class))
				{
					returnValue.setError("BadParameter", "First parameter is not an InputStream.");
					return true;
				}

				DoomSector obj = CACHEDOOMSECTOR.get();
				
				try {
					obj.readBytes(temp.asObjectType(InputStream.class));
				} catch (IOException e) {
					returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
					return true;
				}
				
				sectorToMap(obj, returnValue);
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	WRITESECTOR(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Writes a single Sector to an output stream of some kind ("+DoomSector.LENGTH+" bytes)." +
					"Input map requires the fields that would be returned by READSECTOR()."
				)
				.parameter("output", 
					type(Type.OBJECTREF, "OutputStream", "A readable output stream (also DataOutputStream).")
				)
				.parameter("data", 
					type(Type.MAP, "The map of thing data/fields. If any field is missing or null, a default value is used.")
				)
				.returns(
					type(Type.OBJECTREF, "OutputStream", "[output]."),
					type(Type.ERROR, "BadParameter", "If [output] is not a valid output stream, or [data] is not a map."),
					type(Type.ERROR, "BadData", "If the [data] has a field that does not fit a required range or format."),
					type(Type.ERROR, "IOError", "If a write error occurs.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue map = CACHEVALUE2.get();
			try 
			{
				scriptInstance.popStackValue(map);
				scriptInstance.popStackValue(temp);
				if (!temp.isObjectRef(OutputStream.class))
				{
					returnValue.setError("BadParameter", "First parameter is not an OutputStream.");
					return true;
				}
				if (!map.isMap())
				{
					returnValue.setError("BadParameter", "Second parameter is not a map.");
					return true;
				}

				DoomSector obj = CACHEDOOMSECTOR.get();
				
				try {
					mapToSector(map, obj);
					obj.writeBytes(temp.asObjectType(OutputStream.class));
				} catch (IllegalArgumentException e) {
					returnValue.setError("BadData", e.getMessage(), e.getLocalizedMessage());
					return true;
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
				map.setNull();
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
		return new EnumFunctionResolver(DoomMapFunctions.values());
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

	private static void mapElementToMap(Object object, ScriptValue out)
	{
		if (object == null)
			out.setNull();
		else if (object instanceof UDMFObject)
			udmfToMap((UDMFObject)object, out);
		else if (object instanceof DoomVertex)
			vertexToMap((DoomVertex)object, out);
		else if (object instanceof DoomSector)
			sectorToMap((DoomSector)object, out);
		else if (object instanceof DoomSidedef)
			sidedefToMap((DoomSidedef)object, out);
		else if (object instanceof DoomLinedef)
			linedefToMap((DoomLinedef)object, out);
		else if (object instanceof HexenLinedef)
			linedefToMap((HexenLinedef)object, out);
		else if (object instanceof DoomThing)
			thingToMap((DoomThing)object, false, out);
		else if (object instanceof HexenThing)
			thingToMap((HexenThing)object, out);
		else
			out.setEmptyMap();
	}
	
	private static void udmfToMap(UDMFObject object, ScriptValue out)
	{
		out.setEmptyMap(16);
		for (Map.Entry<String, Object> entry : object)
			out.mapSet(entry.getKey(), entry.getValue());
	}

	private static boolean mapToUDMF(ScriptValue out, UDMFObject object)
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

	private static void vertexToMap(DoomVertex vertex, ScriptValue out)
	{
		out.setEmptyMap(2);
		out.mapSet(UDMFDoomVertexAttributes.ATTRIB_POSITION_X, vertex.getX());
		out.mapSet(UDMFDoomVertexAttributes.ATTRIB_POSITION_Y, vertex.getY());
	}

	private static boolean mapToVertex(ScriptValue in, DoomVertex vertex)
	{
		if (!in.isMap())
			return false;
		
		ScriptValue temp = CACHETEMP.get();
		try {
			in.mapGet(UDMFDoomVertexAttributes.ATTRIB_POSITION_X, temp);
			vertex.setX(temp.asInt());
			in.mapGet(UDMFDoomVertexAttributes.ATTRIB_POSITION_Y, temp);
			vertex.setY(temp.asInt());
		} finally {
			temp.setNull();
		}
		return true;
	}

	private static void sidedefToMap(DoomSidedef sidedef, ScriptValue out)
	{
		out.setEmptyMap(6);
		out.mapSet(UDMFDoomSidedefAttributes.ATTRIB_OFFSET_X, sidedef.getOffsetX());
		out.mapSet(UDMFDoomSidedefAttributes.ATTRIB_OFFSET_Y, sidedef.getOffsetY());
		out.mapSet(UDMFDoomSidedefAttributes.ATTRIB_TEXTURE_TOP, sidedef.getTextureTop());
		out.mapSet(UDMFDoomSidedefAttributes.ATTRIB_TEXTURE_BOTTOM, sidedef.getTextureBottom());
		out.mapSet(UDMFDoomSidedefAttributes.ATTRIB_TEXTURE_MIDDLE, sidedef.getTextureMiddle());
		out.mapSet(UDMFDoomSidedefAttributes.ATTRIB_SECTOR_INDEX, sidedef.getSectorIndex());
	}
	
	private static boolean mapToSidedef(ScriptValue in, DoomSidedef sidedef)
	{
		if (!in.isMap())
			return false;
		
		ScriptValue temp = CACHETEMP.get();
		try {
			in.mapGet(UDMFDoomSidedefAttributes.ATTRIB_OFFSET_X, temp);
			sidedef.setOffsetX(temp.asInt());
			in.mapGet(UDMFDoomSidedefAttributes.ATTRIB_OFFSET_Y, temp);
			sidedef.setOffsetY(temp.asInt());
			in.mapGet(UDMFDoomSidedefAttributes.ATTRIB_TEXTURE_TOP, temp);
			sidedef.setTextureTop(NameUtils.toValidTextureName(temp.asString()));
			in.mapGet(UDMFDoomSidedefAttributes.ATTRIB_TEXTURE_BOTTOM, temp);
			sidedef.setTextureBottom(NameUtils.toValidTextureName(temp.asString()));
			in.mapGet(UDMFDoomSidedefAttributes.ATTRIB_TEXTURE_MIDDLE, temp);
			sidedef.setTextureMiddle(NameUtils.toValidTextureName(temp.asString()));
			in.mapGet(UDMFDoomSidedefAttributes.ATTRIB_SECTOR_INDEX, temp);
			sidedef.setSectorIndex(temp.isNull() ? MapObjectConstants.NULL_REFERENCE : temp.asInt());
		} finally {
			temp.setNull();
		}
		return true;
	}

	private static void sectorToMap(DoomSector sector, ScriptValue out)
	{
		out.setEmptyMap(8);
		out.mapSet(UDMFDoomSectorAttributes.ATTRIB_HEIGHT_FLOOR, sector.getHeightFloor());
		out.mapSet(UDMFDoomSectorAttributes.ATTRIB_HEIGHT_CEILING, sector.getHeightCeiling());
		out.mapSet(UDMFDoomSectorAttributes.ATTRIB_TEXTURE_FLOOR, sector.getTextureFloor());
		out.mapSet(UDMFDoomSectorAttributes.ATTRIB_TEXTURE_CEILING, sector.getTextureCeiling());
		out.mapSet(UDMFDoomSectorAttributes.ATTRIB_LIGHT_LEVEL, sector.getLightLevel());
		out.mapSet(UDMFDoomSectorAttributes.ATTRIB_SPECIAL, sector.getSpecial());
		out.mapSet(UDMFDoomSectorAttributes.ATTRIB_ID, sector.getTag());
	}

	private static boolean mapToSector(ScriptValue in, DoomSector sector)
	{
		if (!in.isMap())
			return false;
	
		ScriptValue temp = CACHETEMP.get();
		try {
			in.mapGet(UDMFDoomSectorAttributes.ATTRIB_HEIGHT_FLOOR, temp);
			sector.setHeightFloor(temp.asInt());
			in.mapGet(UDMFDoomSectorAttributes.ATTRIB_HEIGHT_CEILING, temp);
			sector.setHeightCeiling(temp.asInt());
			if (!in.mapGet(UDMFDoomSectorAttributes.ATTRIB_TEXTURE_FLOOR, temp))
				throw new IllegalArgumentException("Floor texture name was blank.");
			sector.setTextureFloor(NameUtils.toValidTextureName(temp.asString()));
			if (!in.mapGet(UDMFDoomSectorAttributes.ATTRIB_TEXTURE_CEILING, temp))
				throw new IllegalArgumentException("Ceiling texture name was blank.");
			sector.setTextureCeiling(NameUtils.toValidTextureName(temp.asString()));
			in.mapGet(UDMFDoomSectorAttributes.ATTRIB_LIGHT_LEVEL, temp);
			sector.setLightLevel(temp.asInt());
			in.mapGet(UDMFDoomSectorAttributes.ATTRIB_SPECIAL, temp);
			sector.setSpecial(temp.asInt());
			in.mapGet(UDMFDoomSectorAttributes.ATTRIB_ID, temp);
			sector.setTag(temp.asInt());
		} finally {
			temp.setNull();
		}
		return true;
	}
	
	private static void linedefToMap(DoomLinedef linedef, ScriptValue out)
	{
		out.setEmptyMap(20);
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_VERTEX_START, linedef.getVertexStartIndex());
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_VERTEX_END, linedef.getVertexEndIndex());
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_SPECIAL, linedef.getSpecial());
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_SIDEDEF_FRONT, linedef.getSidedefFrontIndex());
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_SIDEDEF_BACK, linedef.getSidedefBackIndex());
	
		// Common to Both Doom/Hexen
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_ID, linedef.getTag());
		
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_BLOCKING, linedef.isFlagSet(DoomLinedefFlags.IMPASSABLE));
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_TWO_SIDED, linedef.isFlagSet(DoomLinedefFlags.TWO_SIDED));
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_UNPEG_BOTTOM, linedef.isFlagSet(DoomLinedefFlags.UNPEG_BOTTOM));
		out.mapSet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_UNPEG_TOP, linedef.isFlagSet(DoomLinedefFlags.UNPEG_TOP));
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

	private static boolean mapToLinedef(ScriptValue in, DoomLinedef linedef)
	{
		if (!in.isMap())
			return false;
	
		ScriptValue temp = CACHETEMP.get();
		try {
			in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_VERTEX_START, temp);
			linedef.setVertexStartIndex(temp.asInt()); 
			in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_VERTEX_END, temp);
			linedef.setVertexEndIndex(temp.asInt()); 
			in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_SPECIAL, temp);
			linedef.setSpecial(temp.asInt()); 
			in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_SIDEDEF_FRONT, temp);
			linedef.setSidedefFrontIndex(temp.isNull() ? MapObjectConstants.NULL_REFERENCE : temp.asInt()); 
			in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_SIDEDEF_BACK, temp);
			linedef.setSidedefBackIndex(temp.isNull() ? MapObjectConstants.NULL_REFERENCE : temp.asInt()); 
	
			in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_ID, temp);
			linedef.setTag(temp.asInt());
	
			in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_BLOCKING, temp);
			linedef.setFlag(DoomLinedefFlags.IMPASSABLE, temp.asBoolean());
			in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_TWO_SIDED, temp);
			linedef.setFlag(DoomLinedefFlags.TWO_SIDED, temp.asBoolean());
			in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_UNPEG_BOTTOM, temp);
			linedef.setFlag(DoomLinedefFlags.UNPEG_BOTTOM, temp.asBoolean());
			in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_UNPEG_TOP, temp);
			linedef.setFlag(DoomLinedefFlags.UNPEG_TOP, temp.asBoolean());
			in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_BLOCK_MONSTERS, temp);
			linedef.setFlag(DoomLinedefFlags.BLOCK_MONSTERS, temp.asBoolean());
			in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_BLOCK_SOUND, temp);
			linedef.setFlag(DoomLinedefFlags.BLOCK_SOUND, temp.asBoolean());
			in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_DONT_DRAW, temp);
			linedef.setFlag(DoomLinedefFlags.NOT_DRAWN, temp.asBoolean());
			in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_MAPPED, temp);
			linedef.setFlag(DoomLinedefFlags.MAPPED, temp.asBoolean());
			in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_SECRET, temp);
			linedef.setFlag(DoomLinedefFlags.SECRET, temp.asBoolean());
			in.mapGet(UDMFDoomLinedefAttributes.ATTRIB_FLAG_PASSTHRU, temp);
			linedef.setFlag(BoomLinedefFlags.PASSTHRU, temp.asBoolean());
			
			in.mapGet(UDMFStrifeLinedefAttributes.ATTRIB_FLAG_BLOCK_FLOAT, temp);
			linedef.setFlag(StrifeLinedefFlags.BLOCK_FLOATERS, temp.asBoolean());
			in.mapGet(UDMFStrifeLinedefAttributes.ATTRIB_FLAG_JUMPOVER, temp);
			linedef.setFlag(StrifeLinedefFlags.RAILING, temp.asBoolean());
			in.mapGet(UDMFStrifeLinedefAttributes.ATTRIB_FLAG_TRANSLUCENT, temp);
			linedef.setFlag(StrifeLinedefFlags.TRANSLUCENT, temp.asBoolean());
		} finally {
			temp.setNull();
		}
		return true;
	}

	private static void linedefToMap(HexenLinedef linedef, ScriptValue out)
	{
		out.setEmptyMap(24);
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_VERTEX_START, linedef.getVertexStartIndex());
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_VERTEX_END, linedef.getVertexEndIndex());
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_SPECIAL, linedef.getSpecial());
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_SIDEDEF_FRONT, linedef.getSidedefFrontIndex());
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_SIDEDEF_BACK, linedef.getSidedefBackIndex());
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_BLOCKING, linedef.isFlagSet(HexenLinedefFlags.IMPASSABLE));
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_TWO_SIDED, linedef.isFlagSet(HexenLinedefFlags.TWO_SIDED));
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_UNPEG_BOTTOM, linedef.isFlagSet(HexenLinedefFlags.UNPEG_BOTTOM));
		out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_UNPEG_TOP, linedef.isFlagSet(HexenLinedefFlags.UNPEG_TOP));
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
		out.mapSet("monsteractivate", linedef.isFlagSet(ZDoomLinedefFlags.ACTIVATED_BY_MONSTERS));
		
		switch (linedef.getActivationType())
		{
			case HexenLinedef.ACTIVATION_PLAYER_CROSSES:
				out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_ACTIVATE_PLAYER_CROSS, true);
				break;
			case HexenLinedef.ACTIVATION_PLAYER_USES:
				out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_ACTIVATE_PLAYER_USE, true);
				break;
			case HexenLinedef.ACTIVATION_MONSTER_CROSSES:
				out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_ACTIVATE_MONSTER_CROSS, true);
				break;
			case HexenLinedef.ACTIVATION_PROJECTILE_HITS:
				out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_ACTIVATE_IMPACT, true);
				break;
			case HexenLinedef.ACTIVATION_PLAYER_BUMPS:
				out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_ACTIVATE_PLAYER_PUSH, true);
				break;
			case HexenLinedef.ACTIVATION_PROJECTILE_CROSSES:
				out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_ACTIVATE_PROJECTILE_CROSS, true);
				break;
			case HexenLinedef.ACTIVATION_PLAYER_USES_PASSTHRU:
				out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_ACTIVATE_PLAYER_USE, true);
				out.mapSet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_PASSTHRU, true);
				break;
			default:
				break;
		}
	}

	private static boolean mapToLinedef(ScriptValue in, HexenLinedef linedef)
	{
		if (!in.isMap())
			return false;
	
		ScriptValue temp = CACHETEMP.get();
		try {
			in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_VERTEX_START, temp);
			linedef.setVertexStartIndex(temp.asInt()); 
			in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_VERTEX_END, temp);
			linedef.setVertexEndIndex(temp.asInt()); 
			in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_SPECIAL, temp);
			linedef.setSpecial(temp.asInt()); 
			in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_SIDEDEF_FRONT, temp);
			linedef.setSidedefFrontIndex(temp.isNull() ? MapObjectConstants.NULL_REFERENCE : temp.asInt()); 
			in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_SIDEDEF_BACK, temp);
			linedef.setSidedefBackIndex(temp.isNull() ? MapObjectConstants.NULL_REFERENCE : temp.asInt()); 
	
			int arg0 = linedef.getArgument(0);
			int arg1 = linedef.getArgument(1);
			int arg2 = linedef.getArgument(2);
			int arg3 = linedef.getArgument(3);
			int arg4 = linedef.getArgument(4);

			in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_ARG0, temp);
				arg0 = temp.asInt();
			in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_ARG1, temp);
				arg1 = temp.asInt();
			in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_ARG2, temp);
				arg2 = temp.asInt();
			in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_ARG3, temp);
				arg3 = temp.asInt();
			in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_ARG4, temp);
				arg4 = temp.asInt();
			linedef.setArguments(arg0, arg1, arg2, arg3, arg4);
	
			in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_BLOCKING, temp);
			linedef.setFlag(HexenLinedefFlags.IMPASSABLE, temp.asBoolean());
			in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_TWO_SIDED, temp);
			linedef.setFlag(HexenLinedefFlags.TWO_SIDED, temp.asBoolean());
			in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_UNPEG_BOTTOM, temp);
			linedef.setFlag(HexenLinedefFlags.UNPEG_BOTTOM, temp.asBoolean());
			in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_UNPEG_TOP, temp);
			linedef.setFlag(HexenLinedefFlags.UNPEG_TOP, temp.asBoolean());
			in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_BLOCK_MONSTERS, temp);
			linedef.setFlag(HexenLinedefFlags.BLOCK_MONSTERS, temp.asBoolean());
			in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_BLOCK_SOUND, temp);
			linedef.setFlag(HexenLinedefFlags.BLOCK_SOUND, temp.asBoolean());
			in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_DONT_DRAW, temp);
			linedef.setFlag(HexenLinedefFlags.NOT_DRAWN, temp.asBoolean());
			in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_MAPPED, temp);
			linedef.setFlag(HexenLinedefFlags.MAPPED, temp.asBoolean());
			in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_SECRET, temp);
			linedef.setFlag(HexenLinedefFlags.SECRET, temp.asBoolean());
			in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_REPEATABLE, temp);
			linedef.setFlag(HexenLinedefFlags.REPEATABLE, temp.asBoolean());
			in.mapGet(UDMFZDoomLinedefAttributes.ATTRIB_FLAG_BLOCK_PLAYERS, temp);
			linedef.setFlag(ZDoomLinedefFlags.BLOCK_PLAYERS, temp.asBoolean());
			in.mapGet(UDMFZDoomLinedefAttributes.ATTRIB_FLAG_BLOCK_EVERYTHING, temp);
			linedef.setFlag(ZDoomLinedefFlags.BLOCK_EVERYTHING, temp.asBoolean());
			in.mapGet("monsteractivate", temp);
			linedef.setFlag(ZDoomLinedefFlags.ACTIVATED_BY_MONSTERS, temp.asBoolean());
			
			if (in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_ACTIVATE_PLAYER_CROSS, temp) && temp.asBoolean())
				linedef.setActivationType(HexenLinedef.ACTIVATION_PLAYER_CROSSES);
			else if (in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_ACTIVATE_PLAYER_USE, temp) && temp.asBoolean())
			{
				if (in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_FLAG_PASSTHRU, temp) && temp.asBoolean())
					linedef.setActivationType(HexenLinedef.ACTIVATION_PLAYER_USES_PASSTHRU);
				else
					linedef.setActivationType(HexenLinedef.ACTIVATION_PLAYER_USES);
			}
			else if (in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_ACTIVATE_MONSTER_CROSS, temp) && temp.asBoolean())
				linedef.setActivationType(HexenLinedef.ACTIVATION_MONSTER_CROSSES);
			else if (in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_ACTIVATE_IMPACT, temp) && temp.asBoolean())
				linedef.setActivationType(HexenLinedef.ACTIVATION_PROJECTILE_HITS);
			else if (in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_ACTIVATE_PLAYER_PUSH, temp) && temp.asBoolean())
				linedef.setActivationType(HexenLinedef.ACTIVATION_PLAYER_BUMPS);
			else if (in.mapGet(UDMFHexenLinedefAttributes.ATTRIB_ACTIVATE_PROJECTILE_CROSS, temp) && temp.asBoolean())
				linedef.setActivationType(HexenLinedef.ACTIVATION_PROJECTILE_CROSSES);
			else 
				linedef.setActivationType(0);
		} finally {
			temp.setNull();
		}
		return true;
	}

	private static void thingToMap(DoomThing thing, boolean strife, ScriptValue out)
	{
		out.setEmptyMap(20);
		out.mapSet(UDMFDoomThingAttributes.ATTRIB_POSITION_X, thing.getX());
		out.mapSet(UDMFDoomThingAttributes.ATTRIB_POSITION_Y, thing.getY());
		out.mapSet(UDMFDoomThingAttributes.ATTRIB_ANGLE, thing.getAngle());
		out.mapSet(UDMFDoomThingAttributes.ATTRIB_TYPE, thing.getType());
		
		boolean easy = thing.isFlagSet(DoomThingFlags.EASY);
		boolean medium = thing.isFlagSet(DoomThingFlags.MEDIUM);
		boolean hard = thing.isFlagSet(DoomThingFlags.HARD);
	
		out.mapSet(UDMFDoomThingAttributes.ATTRIB_FLAG_SKILL1, easy);
		out.mapSet(UDMFDoomThingAttributes.ATTRIB_FLAG_SKILL2, easy);
		out.mapSet("easy", easy);
	
		out.mapSet(UDMFDoomThingAttributes.ATTRIB_FLAG_SKILL3, medium);
		out.mapSet("medium", medium);
	
		out.mapSet(UDMFDoomThingAttributes.ATTRIB_FLAG_SKILL4, hard);
		out.mapSet(UDMFDoomThingAttributes.ATTRIB_FLAG_SKILL5, hard);
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
			out.mapSet(UDMFDoomThingAttributes.ATTRIB_FLAG_AMBUSH, thing.isFlagSet(DoomThingFlags.AMBUSH));
			out.mapSet(UDMFDoomThingAttributes.ATTRIB_FLAG_SINGLE_PLAYER, !thing.isFlagSet(DoomThingFlags.NOT_SINGLEPLAYER));
			out.mapSet(UDMFDoomThingAttributes.ATTRIB_FLAG_COOPERATIVE, !thing.isFlagSet(BoomThingFlags.NOT_COOPERATIVE));
			out.mapSet(UDMFDoomThingAttributes.ATTRIB_FLAG_DEATHMATCH, !thing.isFlagSet(BoomThingFlags.NOT_DEATHMATCH));
			out.mapSet(UDMFMBFThingAttributes.ATTRIB_FLAG_FRIENDLY, thing.isFlagSet(MBFThingFlags.FRIENDLY));
		}
	}

	private static boolean mapToThing(ScriptValue in, DoomThing thing, boolean strife)
	{
		if (!in.isMap())
			return false;
	
		ScriptValue temp = CACHETEMP.get();
		try {
			in.mapGet(UDMFDoomThingAttributes.ATTRIB_POSITION_X, temp);
			thing.setX(temp.asInt());
			in.mapGet(UDMFDoomThingAttributes.ATTRIB_POSITION_Y, temp);
			thing.setY(temp.asInt());
			in.mapGet(UDMFDoomThingAttributes.ATTRIB_ANGLE, temp);
			thing.setAngle(temp.asInt());
			in.mapGet(UDMFDoomThingAttributes.ATTRIB_TYPE, temp);
			thing.setType(temp.asInt());

			thing.setFlags(0);
			
			in.mapGet("easy", temp);
			thing.setFlag(DoomThingFlags.EASY, temp.asBoolean());
			in.mapGet("medium", temp);
			thing.setFlag(DoomThingFlags.MEDIUM, temp.asBoolean());
			in.mapGet("hard", temp);
			thing.setFlag(DoomThingFlags.HARD, temp.asBoolean());

			if (strife)
			{
				in.mapGet(UDMFStrifeThingAttributes.ATTRIB_FLAG_AMBUSH, temp);
				thing.setFlag(StrifeThingFlags.AMBUSH, temp.asBoolean());
				in.mapGet(UDMFStrifeThingAttributes.ATTRIB_FLAG_SINGLE_PLAYER, temp);
				thing.setFlag(StrifeThingFlags.MULTIPLAYER, temp.asBoolean());
				in.mapGet(UDMFStrifeThingAttributes.ATTRIB_FLAG_STANDING, temp);
				thing.setFlag(StrifeThingFlags.STANDING, temp.asBoolean());
				in.mapGet(UDMFStrifeThingAttributes.ATTRIB_FLAG_ALLY, temp);
				thing.setFlag(StrifeThingFlags.ALLY, temp.asBoolean());
				in.mapGet(UDMFStrifeThingAttributes.ATTRIB_FLAG_TRANSLUCENT, temp);
				thing.setFlag(StrifeThingFlags.TRANSLUCENT_25, temp.asBoolean());
				in.mapGet(UDMFStrifeThingAttributes.ATTRIB_FLAG_INVISIBLE, temp);
				thing.setFlag(StrifeThingFlags.INVISIBLE, temp.asBoolean());
			}
			else
			{
				in.mapGet(UDMFDoomThingAttributes.ATTRIB_FLAG_AMBUSH, temp);
				thing.setFlag(DoomThingFlags.AMBUSH, temp.asBoolean());
				in.mapGet(UDMFDoomThingAttributes.ATTRIB_FLAG_SINGLE_PLAYER, temp);
				thing.setFlag(DoomThingFlags.NOT_SINGLEPLAYER, !temp.asBoolean());
				in.mapGet(UDMFDoomThingAttributes.ATTRIB_FLAG_COOPERATIVE, temp);
				thing.setFlag(BoomThingFlags.NOT_COOPERATIVE, !temp.asBoolean());
				in.mapGet(UDMFDoomThingAttributes.ATTRIB_FLAG_DEATHMATCH, temp);
				thing.setFlag(BoomThingFlags.NOT_DEATHMATCH, !temp.asBoolean());
				in.mapGet(UDMFMBFThingAttributes.ATTRIB_FLAG_FRIENDLY, temp);
				thing.setFlag(MBFThingFlags.FRIENDLY, temp.asBoolean());
			}
		} finally {
			temp.setNull();
		}
		return true;
	}
	
	private static void thingToMap(HexenThing thing, ScriptValue out)
	{
		out.setEmptyMap(20);
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_POSITION_X, thing.getX());
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_POSITION_Y, thing.getY());
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_ANGLE, thing.getAngle());
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_TYPE, thing.getType());
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_HEIGHT, thing.getHeight());
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
		out.mapSet("dormant", thing.isFlagSet(HexenThingFlags.DORMANT));
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_FLAG_SINGLE_PLAYER, thing.isFlagSet(HexenThingFlags.SINGLEPLAYER));
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_FLAG_COOPERATIVE, thing.isFlagSet(HexenThingFlags.COOPERATIVE));
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_FLAG_DEATHMATCH, thing.isFlagSet(HexenThingFlags.DEATHMATCH));
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_FLAG_CLASS1, thing.isFlagSet(HexenThingFlags.FIGHTER));
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_FLAG_CLASS2, thing.isFlagSet(HexenThingFlags.CLERIC));
		out.mapSet(UDMFHexenThingAttributes.ATTRIB_FLAG_CLASS3, thing.isFlagSet(HexenThingFlags.MAGE));
		out.mapSet(UDMFMBFThingAttributes.ATTRIB_FLAG_FRIENDLY, thing.isFlagSet(ZDoomThingFlags.FRIENDLY));
	}
	
	private static boolean mapToThing(ScriptValue in, HexenThing thing)
	{
		if (!in.isMap())
			return false;
	
		ScriptValue temp = CACHETEMP.get();
		try {
			in.mapGet(UDMFHexenThingAttributes.ATTRIB_POSITION_X, temp);
				thing.setX(temp.asInt());
			in.mapGet(UDMFHexenThingAttributes.ATTRIB_POSITION_Y, temp);
				thing.setY(temp.asInt());
			in.mapGet(UDMFHexenThingAttributes.ATTRIB_ANGLE, temp);
				thing.setAngle(temp.asInt());
			in.mapGet(UDMFHexenThingAttributes.ATTRIB_TYPE, temp);
				thing.setType(temp.asInt());
			in.mapGet(UDMFHexenThingAttributes.ATTRIB_HEIGHT, temp);
				thing.setHeight(temp.asInt());
			in.mapGet(UDMFHexenThingAttributes.ATTRIB_ID, temp);
				thing.setId(temp.asInt());
			in.mapGet(UDMFHexenThingAttributes.ATTRIB_SPECIAL, temp);
				thing.setSpecial(temp.asInt());

			int arg0 = thing.getArgument(0);
			int arg1 = thing.getArgument(1);
			int arg2 = thing.getArgument(2);
			int arg3 = thing.getArgument(3);
			int arg4 = thing.getArgument(4);
			
			in.mapGet(UDMFHexenThingAttributes.ATTRIB_ARG0, temp);
				arg0 = temp.asInt();
			in.mapGet(UDMFHexenThingAttributes.ATTRIB_ARG1, temp);
				arg1 = temp.asInt();
			in.mapGet(UDMFHexenThingAttributes.ATTRIB_ARG2, temp);
				arg2 = temp.asInt();
			in.mapGet(UDMFHexenThingAttributes.ATTRIB_ARG3, temp);
				arg3 = temp.asInt();
			in.mapGet(UDMFHexenThingAttributes.ATTRIB_ARG4, temp);
				arg4 = temp.asInt();
			thing.setArguments(arg0, arg1, arg2, arg3, arg4);

			in.mapGet("easy", temp);
				thing.setFlag(HexenThingFlags.EASY, temp.asBoolean());
			in.mapGet("medium", temp);
				thing.setFlag(HexenThingFlags.MEDIUM, temp.asBoolean());
			in.mapGet("hard", temp);
				thing.setFlag(HexenThingFlags.HARD, temp.asBoolean());

			in.mapGet(UDMFHexenThingAttributes.ATTRIB_FLAG_AMBUSH, temp);
				thing.setFlag(HexenThingFlags.AMBUSH, temp.asBoolean());
			in.mapGet("dormant", temp);
				thing.setFlag(HexenThingFlags.DORMANT, temp.asBoolean());
			in.mapGet(UDMFHexenThingAttributes.ATTRIB_FLAG_SINGLE_PLAYER, temp);
				thing.setFlag(HexenThingFlags.SINGLEPLAYER, temp.asBoolean());
			in.mapGet(UDMFHexenThingAttributes.ATTRIB_FLAG_COOPERATIVE, temp);
				thing.setFlag(HexenThingFlags.COOPERATIVE, temp.asBoolean());
			in.mapGet(UDMFHexenThingAttributes.ATTRIB_FLAG_DEATHMATCH, temp);
				thing.setFlag(HexenThingFlags.DEATHMATCH, temp.asBoolean());
			in.mapGet(UDMFHexenThingAttributes.ATTRIB_FLAG_CLASS1, temp);
				thing.setFlag(HexenThingFlags.FIGHTER, temp.asBoolean());
			in.mapGet(UDMFHexenThingAttributes.ATTRIB_FLAG_CLASS2, temp);
				thing.setFlag(HexenThingFlags.CLERIC, temp.asBoolean());
			in.mapGet(UDMFHexenThingAttributes.ATTRIB_FLAG_CLASS3, temp);
				thing.setFlag(HexenThingFlags.MAGE, temp.asBoolean());
			in.mapGet(UDMFMBFThingAttributes.ATTRIB_FLAG_FRIENDLY, temp);
				thing.setFlag(ZDoomThingFlags.FRIENDLY, temp.asBoolean());
		} finally {
			temp.setNull();
		}
		return true;
	}
	
	private static abstract class MapViewObjectIterator implements ScriptIteratorType
	{
		protected MapView<?,?,?,?,?> mapView;
		protected IteratorPair pair;
		protected int count;
		protected int cur;

		protected MapViewObjectIterator(MapView<?,?,?,?,?> mapView, int count) 
		{
			this.mapView = mapView;
			this.pair = new IteratorPair();
			this.count = count;
			this.cur = 0;
		}
		
		@Override
		public boolean hasNext()
		{
			return cur < count;
		}

		@Override
		public IteratorPair next() 
		{
			pair.getKey().set(cur);
			mapElementToMap(nextObject(cur), pair.getValue());
			cur++;
			return pair;
		}
		
		protected abstract Object nextObject(int seq);
		
	}
	
	private static class ThingIterator extends MapViewObjectIterator
	{
		private boolean strife;

		private ThingIterator(MapView<?,?,?,?,?> mapView, boolean strife)
		{
			super(mapView, mapView.getThingCount());
			this.strife = strife;
		}

		@Override
		public IteratorPair next() 
		{
			pair.getKey().set(cur);
			Object thing = mapView.getThing(cur);
			if (thing instanceof DoomThing)
				thingToMap((DoomThing)thing, strife, pair.getValue());
			else
				mapElementToMap(thing, pair.getValue());
			cur++;
			return pair;
		}

		@Override
		protected Object nextObject(int seq)
		{
			return mapView.getThing(seq);
		}
	}
	
	private static class VertexIterator extends MapViewObjectIterator
	{
		private VertexIterator(MapView<?,?,?,?,?> mapView)
		{
			super(mapView, mapView.getVertexCount());
		}
	
		@Override
		protected Object nextObject(int seq)
		{
			return mapView.getVertex(seq);
		}
	}

	private static class LinedefIterator extends MapViewObjectIterator
	{
		private LinedefIterator(MapView<?,?,?,?,?> mapView)
		{
			super(mapView, mapView.getLinedefCount());
		}

		@Override
		protected Object nextObject(int seq)
		{
			return mapView.getLinedef(seq);
		}
	}
	
	private static class SidedefIterator extends MapViewObjectIterator
	{
		private SidedefIterator(MapView<?,?,?,?,?> mapView)
		{
			super(mapView, mapView.getSidedefCount());
		}

		@Override
		protected Object nextObject(int seq)
		{
			return mapView.getSidedef(seq);
		}
	}
	
	private static class SectorIterator extends MapViewObjectIterator
	{
		private SectorIterator(MapView<?,?,?,?,?> mapView)
		{
			super(mapView, mapView.getSectorCount());
		}

		@Override
		protected Object nextObject(int seq)
		{
			return mapView.getSector(seq);
		}
	}
	
	// Threadlocal "stack" values.
	private static final ThreadLocal<ScriptValue> CACHEVALUE1 = ThreadLocal.withInitial(()->ScriptValue.create(null));
	private static final ThreadLocal<ScriptValue> CACHEVALUE2 = ThreadLocal.withInitial(()->ScriptValue.create(null));

	private static final ThreadLocal<DoomThing> CACHEDOOMTHING = ThreadLocal.withInitial(()->new DoomThing());
	private static final ThreadLocal<HexenThing> CACHEHEXENTHING = ThreadLocal.withInitial(()->new HexenThing());
	private static final ThreadLocal<DoomLinedef> CACHEDOOMLINEDEF = ThreadLocal.withInitial(()->new DoomLinedef());
	private static final ThreadLocal<HexenLinedef> CACHEHEXENLINEDEF = ThreadLocal.withInitial(()->new HexenLinedef());
	private static final ThreadLocal<DoomSidedef> CACHEDOOMSIDEDEF = ThreadLocal.withInitial(()->new DoomSidedef());
	private static final ThreadLocal<DoomVertex> CACHEDOOMVERTEX = ThreadLocal.withInitial(()->new DoomVertex());
	private static final ThreadLocal<DoomSector> CACHEDOOMSECTOR = ThreadLocal.withInitial(()->new DoomSector());
	
	private static final ThreadLocal<ScriptValue> CACHETEMP = ThreadLocal.withInitial(()->ScriptValue.create(null));

}

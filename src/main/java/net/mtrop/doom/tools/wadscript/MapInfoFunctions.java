package net.mtrop.doom.tools.wadscript;

import static com.blackrook.rookscript.lang.ScriptFunctionUsage.type;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import com.blackrook.rookscript.ScriptInstance;
import com.blackrook.rookscript.ScriptIteratorType;
import com.blackrook.rookscript.ScriptValue;
import com.blackrook.rookscript.ScriptValue.BufferType;
import com.blackrook.rookscript.ScriptValue.Type;
import com.blackrook.rookscript.lang.ScriptFunctionType;
import com.blackrook.rookscript.lang.ScriptFunctionUsage;
import com.blackrook.rookscript.resolvers.ScriptFunctionResolver;
import com.blackrook.rookscript.resolvers.hostfunction.EnumFunctionResolver;

import net.mtrop.doom.object.TextObject;
import net.mtrop.doom.object.TextObject.ParseException;
import net.mtrop.doom.text.EternityMapInfo;
import net.mtrop.doom.text.HexenMapInfo;
import net.mtrop.doom.text.UniversalMapInfo;
import net.mtrop.doom.text.ZDoomMapInfo;
import net.mtrop.doom.text.data.MapInfoData;
import net.mtrop.doom.text.data.MapInfoData.Value;
import net.mtrop.doom.tools.struct.util.IOUtils;

/**
 * MapInfo parsing/iterating functions.
 */
public enum MapInfoFunctions implements ScriptFunctionType 
{
	MAPINFOTYPE(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Attempts to figure out what MAPINFO type is provided based on the provided data."
				)
				.parameter("data", 
					type(Type.STRING, "The string data to use."),
					type(Type.BUFFER, "The buffer of data to use."),
					type(Type.OBJECTREF, "File", "The file to use."),
					type(Type.OBJECTREF, "Reader", "The reader to use."),
					type(Type.OBJECTREF, "InputStream", "The stream of data to use.")
				)
				.parameter("encoding", 
					type(Type.NULL, "Use native encoding."),
					type(Type.STRING, "If buffer, file, or stream, sets the character encoding type.")
				)
				.returns(
					type(Type.NULL, "If [data] is null."),
					type(Type.STRING, "One of the following: \"mapinfo\", \"zmapinfo\", \"emapinfo\", \"umapinfo\" or null if no match."),
					type(Type.ERROR, "Parse", "If [data] could not be parsed."),
					type(Type.ERROR, "BadFile", "If [data] is a File, and it is not found."),
					type(Type.ERROR, "BadEncoding", "If the provided [encoding] is invalid."),
					type(Type.ERROR, "IOError", "If the data could not be read.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try
			{
				String dataString;
				Charset encoding;
				
				scriptInstance.popStackValue(temp);
				String encodingName = temp.isNull() ? null : temp.asString();
				scriptInstance.popStackValue(temp);
				
				encoding = getEncoding(encodingName, returnValue);
				if (returnValue.isError())
					return true;
				
				dataString = getDataString(temp, encoding, returnValue);
				if (returnValue.isError())
					return true;

				if (dataString != null)
				{
					if (UMAPINFO_PATTERN.matcher(dataString).find())
						returnValue.set("umapinfo");
					else if (EMAPINFO_PATTERN.matcher(dataString).find())
						returnValue.set("emapinfo");
					else if (ZMAPINFO_PATTERN.matcher(dataString).find())
						returnValue.set("zmapinfo");
					else if (MAPINFO_PATTERN.matcher(dataString).find())
						returnValue.set("mapinfo");
					else
						returnValue.setNull();
				}
				
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}

	},

	MAPINFOITERATE(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Parses and iterates through Hexen MAPINFO data - returns a value that is iterable through an each(...) loop. " +
					"The value returned each iteration is a list of tokens pertinent to a complete line of MAPINFO data, or an error if a parse error happens."
				)
				.parameter("data", 
					type(Type.STRING, "The string data to use."),
					type(Type.BUFFER, "The buffer of data to use."),
					type(Type.OBJECTREF, "File", "The file to use."),
					type(Type.OBJECTREF, "Reader", "The reader to use."),
					type(Type.OBJECTREF, "InputStream", "The stream of data to use.")
				)
				.parameter("encoding", 
					type(Type.NULL, "Use native encoding."),
					type(Type.STRING, "If buffer, file, or stream, sets the character encoding type.")
				)
				.returns(
					type(Type.NULL, "If [data] is null."),
					type(Type.OBJECTREF, "ScriptIteratorType", "The info iterator."),
					type(Type.ERROR, "Parse", "If [data] could not be parsed."),
					type(Type.ERROR, "BadFile", "If [data] is a File, and it is not found."),
					type(Type.ERROR, "BadEncoding", "If the provided [encoding] is invalid."),
					type(Type.ERROR, "IOError", "If the data could not be read.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try
			{
				String dataString;
				Charset encoding;
				
				scriptInstance.popStackValue(temp);
				String encodingName = temp.isNull() ? null : temp.asString();
				scriptInstance.popStackValue(temp);
				
				encoding = getEncoding(encodingName, returnValue);
				if (returnValue.isError())
					return true;
				
				dataString = getDataString(temp, encoding, returnValue);
				if (returnValue.isError())
					return true;

				try {
					returnValue.set(new HexenMapInfoIterator(TextObject.create(HexenMapInfo.class, dataString)));
				} catch (ParseException e) {
					returnValue.setError("Parse", e.getMessage(), e.getLocalizedMessage());
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
	
	ZMAPINFOITERATE(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Parses and iterates through ZDoom MAPINFO data (ZMAPINFO) - returns a value that is iterable through an each(...) loop. " +
					"Each key is a ZMAPINFO keyword and each value is a list of corresponding values, optionally terminated in a map of child values."
				)
				.parameter("data", 
					type(Type.STRING, "The string data to use."),
					type(Type.BUFFER, "The buffer of data to use."),
					type(Type.OBJECTREF, "File", "The file to use."),
					type(Type.OBJECTREF, "Reader", "The reader to use."),
					type(Type.OBJECTREF, "InputStream", "The stream of data to use.")
				)
				.parameter("encoding", 
					type(Type.NULL, "Use native encoding."),
					type(Type.STRING, "If buffer, file, or stream, sets the character encoding type.")
				)
				.returns(
					type(Type.NULL, "If [data] is null."),
					type(Type.OBJECTREF, "ScriptIteratorType", "The info iterator."),
					type(Type.ERROR, "Parse", "If [data] could not be parsed."),
					type(Type.ERROR, "BadFile", "If [data] is a File, and it is not found."),
					type(Type.ERROR, "BadEncoding", "If the provided [encoding] is invalid."),
					type(Type.ERROR, "IOError", "If the data could not be read.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try
			{
				String dataString;
				Charset encoding;
				
				scriptInstance.popStackValue(temp);
				String encodingName = temp.isNull() ? null : temp.asString();
				scriptInstance.popStackValue(temp);
				
				encoding = getEncoding(encodingName, returnValue);
				if (returnValue.isError())
					return true;
				
				dataString = getDataString(temp, encoding, returnValue);
				if (returnValue.isError())
					return true;

				try {
					returnValue.set(new ZDoomMapInfoIterator(TextObject.create(ZDoomMapInfo.class, dataString)));
				} catch (ParseException e) {
					returnValue.setError("Parse", e.getMessage(), e.getLocalizedMessage());
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
	
	EMAPINFOITERATE(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Parses and iterates through Eternity Engine MAPINFO data (EMAPINFO) - returns a value that is iterable through an each(...) loop. " +
					"Each key is a EMAPINFO map header name and each value is a map of values, " +
					"with each key of that being an entry name and each value as a list of values associated with that key."
				)
				.parameter("data", 
					type(Type.STRING, "The string data to use."),
					type(Type.BUFFER, "The buffer of data to use."),
					type(Type.OBJECTREF, "File", "The file to use."),
					type(Type.OBJECTREF, "Reader", "The reader to use."),
					type(Type.OBJECTREF, "InputStream", "The stream of data to use.")
				)
				.parameter("encoding", 
					type(Type.NULL, "Use native encoding."),
					type(Type.STRING, "If buffer, file, or stream, sets the character encoding type.")
				)
				.returns(
					type(Type.NULL, "If [data] is null."),
					type(Type.OBJECTREF, "ScriptIteratorType", "The info iterator."),
					type(Type.ERROR, "Parse", "If [data] could not be parsed."),
					type(Type.ERROR, "BadFile", "If [data] is a File, and it is not found."),
					type(Type.ERROR, "BadEncoding", "If the provided [encoding] is invalid."),
					type(Type.ERROR, "IOError", "If the data could not be read.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try
			{
				String dataString;
				Charset encoding;
				
				scriptInstance.popStackValue(temp);
				String encodingName = temp.isNull() ? null : temp.asString();
				scriptInstance.popStackValue(temp);
				
				encoding = getEncoding(encodingName, returnValue);
				if (returnValue.isError())
					return true;
				
				dataString = getDataString(temp, encoding, returnValue);
				if (returnValue.isError())
					return true;

				try {
					returnValue.set(new EternityMapInfoIterator(TextObject.create(EternityMapInfo.class, dataString)));
				} catch (ParseException e) {
					returnValue.setError("Parse", e.getMessage(), e.getLocalizedMessage());
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
	
	UMAPINFOITERATE(2)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Parses and iterates through Universal MAPINFO data (UMAPINFO) - returns a value that is iterable through an each(...) loop. " +
					"Each key is a UMAPINFO map header name and each value is a map of values, " +
					"with each key of that being an entry name and each value as a list of values associated with that key."
				)
				.parameter("data", 
					type(Type.STRING, "The string data to use."),
					type(Type.BUFFER, "The buffer of data to use."),
					type(Type.OBJECTREF, "File", "The file to use."),
					type(Type.OBJECTREF, "Reader", "The reader to use."),
					type(Type.OBJECTREF, "InputStream", "The stream of data to use.")
				)
				.parameter("encoding", 
					type(Type.NULL, "Use native encoding."),
					type(Type.STRING, "If buffer, file, or stream, sets the character encoding type.")
				)
				.returns(
					type(Type.NULL, "If [data] is null."),
					type(Type.OBJECTREF, "ScriptIteratorType", "The info iterator."),
					type(Type.ERROR, "Parse", "If [data] could not be parsed."),
					type(Type.ERROR, "BadFile", "If [data] is a File, and it is not found."),
					type(Type.ERROR, "BadEncoding", "If the provided [encoding] is invalid."),
					type(Type.ERROR, "IOError", "If the data could not be read.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try
			{
				String dataString;
				Charset encoding;
				
				scriptInstance.popStackValue(temp);
				String encodingName = temp.isNull() ? null : temp.asString();
				scriptInstance.popStackValue(temp);
				
				encoding = getEncoding(encodingName, returnValue);
				if (returnValue.isError())
					return true;
				
				dataString = getDataString(temp, encoding, returnValue);
				if (returnValue.isError())
					return true;

				try {
					returnValue.set(new UniversalMapInfoIterator(TextObject.create(UniversalMapInfo.class, dataString)));
				} catch (ParseException e) {
					returnValue.setError("Parse", e.getMessage(), e.getLocalizedMessage());
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
	
	;
	
	private final int parameterCount;
	private Usage usage;
	private MapInfoFunctions(int parameterCount)
	{
		this.parameterCount = parameterCount;
		this.usage = null;
	}
	
	/**
	 * @return a function resolver that handles all of the functions in this enum.
	 */
	public static final ScriptFunctionResolver createResolver()
	{
		return new EnumFunctionResolver(MapInfoFunctions.values());
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
	 * Gets an encoding from an encoding name.
	 * @param encodingName the name of the encoding. Can be null.
	 * @param returnValue the return value - set on error.
	 * @return the corresponding charset.
	 */
	private static Charset getEncoding(String encodingName, ScriptValue returnValue) 
	{
		if (encodingName == null)
			return Charset.defaultCharset();
		else try {
			return Charset.forName(encodingName);
		} catch (IllegalCharsetNameException | UnsupportedCharsetException e) {
			returnValue.setError("BadEncoding", e.getMessage(), e.getLocalizedMessage());
			return null;
		}
	}

	/**
	 * Gets the data string to read for MAPINFO.
	 * @param data the incoming data type.
	 * @param encoding the encoding to use, if any.
	 * @param returnValue the return value - set to error on error.
	 * @return the string to read.
	 */
	private static String getDataString(ScriptValue data, Charset encoding, ScriptValue returnValue) 
	{
		if (data.isNull())
		{
			returnValue.setNull();
			return null;
		}
		else if (data.isBuffer())
		{
			BufferType buf = data.asObjectType(BufferType.class);
			Reader reader = new InputStreamReader(buf.getInputStream(), encoding);
			StringWriter sw = new StringWriter();
			try {
				IOUtils.relay(reader, sw);
			} catch (IOException e) {
				returnValue.setError("IOError", "Could not read data from Buffer.");
				return null;
			}
			return data.asString();
		}
		else if (data.isObjectRef(File.class))
		{
			;
			StringWriter sw = new StringWriter();
			try (Reader reader = new InputStreamReader(new FileInputStream(data.asObjectType(File.class)))) 
			{
				IOUtils.relay(reader, sw);
			} 
			catch (FileNotFoundException e) 
			{
				returnValue.setError("BadFile", e.getMessage(), e.getLocalizedMessage());
				return null;
			}
			catch (IOException e) 
			{
				returnValue.setError("IOError", "Could not read data from Reader.");
				return null;
			}
			return sw.toString();
		}
		else if (data.isObjectRef(Reader.class))
		{
			Reader reader = data.asObjectType(Reader.class);
			StringWriter sw = new StringWriter();
			try {
				IOUtils.relay(reader, sw);
			} catch (IOException e) {
				returnValue.setError("IOError", "Could not read data from Reader.");
				return null;
			}
			return sw.toString();
		}
		else if (data.isObjectRef(InputStream.class))
		{
			Reader reader = new InputStreamReader(data.asObjectType(InputStream.class), encoding);
			StringWriter sw = new StringWriter();
			try {
				IOUtils.relay(reader, sw);
			} catch (IOException e) {
				returnValue.setError("IOError", "Could not read data from InputStream.");
				return null;
			}
			return sw.toString();
		}
		else
		{
			return data.asString();
		}
	}

	private static void fillValues(MapInfoData data, ScriptValue list)
	{
		Value[] dataValues = data.getValues();
		for (int i = 0; i < dataValues.length; i++)
			list.listAdd(dataValues[i]);
		
		if (data.hasChildren())
		{
			ScriptValue temp = ScriptValue.createEmptyMap();
			fillChildren(data, temp);
			list.listAdd(temp);
		}
	}
	
	private static void fillChildren(MapInfoData data, ScriptValue map)
	{
		ScriptValue temp = ScriptValue.create(null);
		for (int i = 0; i < data.getChildCount(); i++)
		{
			MapInfoData child = data.getChildAt(i);
			temp.setEmptyList(child.getValues().length);
			fillValues(child, temp);
			map.mapSet(child.getName(), temp);
		}
	}
	
	private static class HexenMapInfoIterator implements ScriptIteratorType
	{
		private HexenMapInfo mapInfo;
		private String[] next;
		private String exception;
		private IteratorPair pair;

		private HexenMapInfoIterator(HexenMapInfo mapInfo)
		{
			this.mapInfo = mapInfo;
			this.next = null;
			this.exception = null;
			this.pair = new IteratorPair();
			nextTokens();
		}
		
		private void nextTokens()
		{
			try {
				next = mapInfo.nextTokens();
			} catch (ParseException e) {
				exception = e.getLocalizedMessage();
			}
		}
		
		@Override
		public boolean hasNext()
		{
			return next != null || exception != null;
		}

		@Override
		public IteratorPair next() 
		{
			if (exception != null)
			{
				pair.getValue().setError("Parse", exception);
			}
			else
			{
				pair.getValue().setEmptyList(next.length);
				for (int i = 0; i < next.length; i++)
				{
					pair.getValue().listAdd(next[i]);
				}
			}
			
			pair.getKey().setNull();
			nextTokens();
			return pair;
		}

	}
	
	private static class ZDoomMapInfoIterator implements ScriptIteratorType
	{
		private Iterator<MapInfoData> iterator;
		private IteratorPair pair;
		
		private ZDoomMapInfoIterator(ZDoomMapInfo mapInfo)
		{
			this.iterator = mapInfo.iterator();
			this.pair = new IteratorPair();
		}
		
		@Override
		public boolean hasNext() 
		{
			return iterator.hasNext();
		}
		
		@Override
		public IteratorPair next() 
		{
			MapInfoData data = iterator.next();

			pair.getKey().set(data.getName());
			pair.getValue().setEmptyList(data.getValues().length + (data.hasChildren() ? 1 : 0));
			fillValues(data, pair.getValue());
			
			return pair;
		}
		
	}
	
	private static class EternityMapInfoIterator implements ScriptIteratorType
	{
		private Iterator<Map.Entry<String, MapInfoData[]>> iterator;
		private IteratorPair pair;
		
		private EternityMapInfoIterator(EternityMapInfo mapInfo)
		{
			this.iterator = mapInfo.iterator();
			this.pair = new IteratorPair();
		}
		
		@Override
		public boolean hasNext() 
		{
			return iterator.hasNext();
		}
		
		@Override
		public IteratorPair next() 
		{
			Map.Entry<String, MapInfoData[]> entry = iterator.next();

			pair.getKey().set(entry.getKey());
			
			MapInfoData[] values = entry.getValue();
			pair.getValue().setEmptyMap(values.length);
			
			for (MapInfoData data : values)
			{
				ScriptValue sv = ScriptValue.createEmptyList();
				fillValues(data, sv);
				pair.getValue().mapSet(data.getName(), sv);
			}
			
			return pair;
		}
		
	}
	
	private static class UniversalMapInfoIterator implements ScriptIteratorType
	{
		private Iterator<Map.Entry<String, MapInfoData[]>> iterator;
		private IteratorPair pair;
		
		private UniversalMapInfoIterator(UniversalMapInfo mapInfo)
		{
			this.iterator = mapInfo.iterator();
			this.pair = new IteratorPair();
		}
		
		@Override
		public boolean hasNext() 
		{
			return iterator.hasNext();
		}
		
		@Override
		public IteratorPair next() 
		{
			Map.Entry<String, MapInfoData[]> entry = iterator.next();

			pair.getKey().set(entry.getKey());
			
			MapInfoData[] values = entry.getValue();
			pair.getValue().setEmptyMap(values.length);
			
			for (MapInfoData data : values)
			{
				ScriptValue sv = ScriptValue.createEmptyList();
				fillValues(data, sv);
				pair.getValue().mapSet(data.getName(), sv);
			}
			
			return pair;
		}
		
	}
	
	/** Regex pattern for UMAPINFO. */
	private static final Pattern UMAPINFO_PATTERN = Pattern.compile("(skytexture\\s+=)", Pattern.CASE_INSENSITIVE);
	/** Regex pattern for EMAPINFO. */
	private static final Pattern EMAPINFO_PATTERN = Pattern.compile("(skyname\\s+=?|sky2name\\s+=?)", Pattern.CASE_INSENSITIVE);
	/** Regex pattern for ZMAPINFO. */
	private static final Pattern ZMAPINFO_PATTERN = Pattern.compile("(sky1\\s+=|sky2\\s+=)", Pattern.CASE_INSENSITIVE);
	/** Regex pattern for MAPINFO. */
	private static final Pattern MAPINFO_PATTERN = Pattern.compile("(sky1\\s+|sky2\\s+)", Pattern.CASE_INSENSITIVE);
	
	// Threadlocal "stack" values.
	private static final ThreadLocal<ScriptValue> CACHEVALUE1 = ThreadLocal.withInitial(()->ScriptValue.create(null));
	
}

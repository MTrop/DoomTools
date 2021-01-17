package net.mtrop.doom.tools.doommake;

import static com.blackrook.rookscript.lang.ScriptFunctionUsage.type;

import java.io.InputStream;
import java.io.PrintStream;

import com.blackrook.rookscript.ScriptInstance;
import com.blackrook.rookscript.ScriptValue;
import com.blackrook.rookscript.ScriptValue.Type;
import com.blackrook.rookscript.lang.ScriptFunctionType;
import com.blackrook.rookscript.lang.ScriptFunctionUsage;
import com.blackrook.rookscript.resolvers.ScriptFunctionResolver;
import com.blackrook.rookscript.resolvers.hostfunction.EnumFunctionResolver;

import net.mtrop.doom.tools.DecoHackMain;
import net.mtrop.doom.tools.DoomMakeMain;
import net.mtrop.doom.tools.DoomToolsMain;
import net.mtrop.doom.tools.WADTexMain;
import net.mtrop.doom.tools.WSwAnTablesMain;
import net.mtrop.doom.tools.WTExportMain;
import net.mtrop.doom.tools.WTexScanMain;
import net.mtrop.doom.tools.WadMergeMain;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.exception.OptionParseException;

/**
 * Script functions for invoking the tools directly.
 * @author Matthew Tropiano
 */
public enum ToolInvocationFunctions implements ScriptFunctionType
{
	DOOMTOOLS(1)
	{
		@Override
		protected Usage usage() 
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Calls the DoomTools tool. Inherits STDOUT/STDERR of this script unless overridden (see options)."
				)
				.parameter("options", 
					type(Type.MAP, 
						"{stdout:OBJECTREF(Outputstream)}", 
						"Map of options."
					)
				)
				.returns(
					type(Type.INTEGER, "The normal return of this tool's process."),
					type(Type.ERROR, "BadOptions", "If the options map could not be applied."),
					type(Type.ERROR, "Security", "If the OS is preventing the read.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try 
			{
				PrintStream stdout = scriptInstance.getEnvironment().getStandardOut();
				PrintStream stderr = scriptInstance.getEnvironment().getStandardErr();
				DoomToolsMain.Options options = DoomToolsMain.options(stdout, stderr);
				scriptInstance.popStackValue(temp);
				if (!temp.isNull())
				{
					if (!temp.isMap())
					{
						returnValue.setError("BadOptions", "Options parameter needs to be a Map type.");
						return true;
					}
					else if (!temp.mapApply(options))
					{
						returnValue.setError("BadOptions", "Options Map could not be applied.");
						return true;
					}
				}
				returnValue.set(DoomToolsMain.call(options));
				return true;
			} catch (OptionParseException e) {
				returnValue.setError("BadOptions", "Option argument parse failed: " + e.getLocalizedMessage());
				return true;
			} catch (ClassCastException e) {
				returnValue.setError("BadOptions", "Options Map could not be applied: " + e.getLocalizedMessage());
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},

	DOOMMAKE(1)
	{
		@Override
		protected Usage usage() 
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Calls the DoomMake tool. Inherits STDOUT/STDERR/STDIN of this script unless overridden (see options). " +
					"It is important to note that the instance of DoomMake that runs has no inherant link to this instance."
				)
				.parameter("options", 
					type(Type.MAP, 
						"{" + Common.joinStrings(", ",
							"stdout:OBJECTREF(Outputstream)",
							"stderr:OBJECTREF(Outputstream)",
							"stdin:OBJECTREF(InputStream)",
							"targetName:STRING",
							"propertiesFile:OBJECTREF(File)",
							"scriptFile:OBJECTREF(File)",
							"args:LIST[STRING, ...]",
							"runawayLimit:INTEGER",
							"activationDepth:INTEGER",
							"stackDepth:INTEGER",
							"help:BOOLEAN",
							"version:BOOLEAN"
						) + "}",
						"Map of options."
					)
				)
				.returns(
					type(Type.INTEGER, "The normal return of this tool's process."),
					type(Type.ERROR, "BadOptions", "If the options map could not be applied."),
					type(Type.ERROR, "Security", "If the OS is preventing the read.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try 
			{
				PrintStream stdout = scriptInstance.getEnvironment().getStandardOut();
				PrintStream stderr = scriptInstance.getEnvironment().getStandardErr();
				InputStream stdin = scriptInstance.getEnvironment().getStandardIn();
				DoomMakeMain.Options options = DoomMakeMain.options(stdout, stderr, stdin);
				scriptInstance.popStackValue(temp);
				if (!temp.isNull())
				{
					if (!temp.isMap())
					{
						returnValue.setError("BadOptions", "Options parameter needs to be a Map type.");
						return true;
					}
					else if (!temp.mapApply(options))
					{
						returnValue.setError("BadOptions", "Options Map could not be applied.");
						return true;
					}
				}
				returnValue.set(DoomMakeMain.call(options));
				return true;
			} catch (OptionParseException e) {
				returnValue.setError("BadOptions", "Option argument parse failed: " + e.getLocalizedMessage());
				return true;
			} catch (ClassCastException e) {
				returnValue.setError("BadOptions", "Options Map could not be applied: " + e.getLocalizedMessage());
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},

	DECOHACK(1)
	{
		@Override
		protected Usage usage() 
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Calls the DecoHack tool. Inherits STDOUT/STDERR of this script unless overridden (see options)."
				)
				.parameter("options", 
					type(Type.MAP, "Map of options.")
				)
				.returns(
					type(Type.INTEGER, "The normal return of this tool's process."),
					type(Type.ERROR, "BadOptions", "If the options map could not be applied."),
					type(Type.ERROR, "Security", "If the OS is preventing the read.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try 
			{
				PrintStream stdout = scriptInstance.getEnvironment().getStandardOut();
				PrintStream stderr = scriptInstance.getEnvironment().getStandardErr();
				DecoHackMain.Options options = DecoHackMain.options(stdout, stderr);
				scriptInstance.popStackValue(temp);
				if (!temp.isNull())
				{
					if (!temp.isMap())
					{
						returnValue.setError("BadOptions", "Options parameter needs to be a Map type.");
						return true;
					}
					else if (!temp.mapApply(options))
					{
						returnValue.setError("BadOptions", "Options Map could not be applied.");
						return true;
					}
				}
				returnValue.set(DecoHackMain.call(options));
				return true;
			} catch (OptionParseException e) {
				returnValue.setError("BadOptions", "Option argument parse failed: " + e.getLocalizedMessage());
				return true;
			} catch (ClassCastException e) {
				returnValue.setError("BadOptions", "Options Map could not be applied: " + e.getLocalizedMessage());
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},

	DMXCONVERT(1)
	{
		@Override
		protected Usage usage() 
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Calls the DMXConvert tool. Inherits STDOUT/STDERR of this script unless overridden (see options)."
				)
				.parameter("options", 
					type(Type.MAP, "Map of options.")
				)
				.returns(
					type(Type.INTEGER, "The normal return of this tool's process."),
					type(Type.ERROR, "BadOptions", "If the options map could not be applied."),
					type(Type.ERROR, "Security", "If the OS is preventing the read.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try 
			{
				PrintStream stdout = scriptInstance.getEnvironment().getStandardOut();
				PrintStream stderr = scriptInstance.getEnvironment().getStandardErr();
				DecoHackMain.Options options = DecoHackMain.options(stdout, stderr);
				scriptInstance.popStackValue(temp);
				if (!temp.isNull())
				{
					if (!temp.isMap())
					{
						returnValue.setError("BadOptions", "Options parameter needs to be a Map type.");
						return true;
					}
					else if (!temp.mapApply(options))
					{
						returnValue.setError("BadOptions", "Options Map could not be applied.");
						return true;
					}
				}
				returnValue.set(DecoHackMain.call(options));
				return true;
			} catch (OptionParseException e) {
				returnValue.setError("BadOptions", "Option argument parse failed: " + e.getLocalizedMessage());
				return true;
			} catch (ClassCastException e) {
				returnValue.setError("BadOptions", "Options Map could not be applied: " + e.getLocalizedMessage());
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},

	WADMERGE(1)
	{
		@Override
		protected Usage usage() 
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Calls the WadMerge tool. Inherits STDOUT/STDERR/STDIN of this script unless overridden (see options)."
				)
				.parameter("options", 
					type(Type.MAP, "Map of options.")
				)
				.returns(
					type(Type.INTEGER, "The normal return of this tool's process."),
					type(Type.ERROR, "BadOptions", "If the options map could not be applied."),
					type(Type.ERROR, "Security", "If the OS is preventing the read.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try 
			{
				PrintStream stdout = scriptInstance.getEnvironment().getStandardOut();
				PrintStream stderr = scriptInstance.getEnvironment().getStandardErr();
				InputStream stdin = scriptInstance.getEnvironment().getStandardIn();
				WadMergeMain.Options options = WadMergeMain.options(stdout, stderr, stdin);
				scriptInstance.popStackValue(temp);
				if (!temp.isNull())
				{
					if (!temp.isMap())
					{
						returnValue.setError("BadOptions", "Options parameter needs to be a Map type.");
						return true;
					}
					else if (!temp.mapApply(options))
					{
						returnValue.setError("BadOptions", "Options Map could not be applied.");
						return true;
					}
				}
				returnValue.set(WadMergeMain.call(options));
				return true;
			} catch (OptionParseException e) {
				returnValue.setError("BadOptions", "Option argument parse failed: " + e.getLocalizedMessage());
				return true;
			} catch (ClassCastException e) {
				returnValue.setError("BadOptions", "Options Map could not be applied: " + e.getLocalizedMessage());
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},

	WADSCRIPT(1)
	{
		@Override
		protected Usage usage() 
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Calls the WadScript tool. Inherits STDOUT/STDERR/STDIN of this script unless overridden (see options). " +
					"It is important to note that the instance of WadScript that runs has no inherant link to this instance."
				)
				.parameter("options", 
					type(Type.MAP, "Map of options.")
				)
				.returns(
					type(Type.INTEGER, "The normal return of this tool's process."),
					type(Type.ERROR, "BadOptions", "If the options map could not be applied."),
					type(Type.ERROR, "Security", "If the OS is preventing the read.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try 
			{
				PrintStream stdout = scriptInstance.getEnvironment().getStandardOut();
				PrintStream stderr = scriptInstance.getEnvironment().getStandardErr();
				InputStream stdin = scriptInstance.getEnvironment().getStandardIn();
				WadMergeMain.Options options = WadMergeMain.options(stdout, stderr, stdin);
				scriptInstance.popStackValue(temp);
				if (!temp.isNull())
				{
					if (!temp.isMap())
					{
						returnValue.setError("BadOptions", "Options parameter needs to be a Map type.");
						return true;
					}
					else if (!temp.mapApply(options))
					{
						returnValue.setError("BadOptions", "Options Map could not be applied.");
						return true;
					}
				}
				returnValue.set(WadMergeMain.call(options));
				return true;
			} catch (OptionParseException e) {
				returnValue.setError("BadOptions", "Option argument parse failed: " + e.getLocalizedMessage());
				return true;
			} catch (ClassCastException e) {
				returnValue.setError("BadOptions", "Options Map could not be applied: " + e.getLocalizedMessage());
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},

	WADTEX(1)
	{
		@Override
		protected Usage usage() 
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Calls the WADTex tool. Inherits STDOUT/STDERR of this script unless overridden (see options)."
				)
				.parameter("options", 
					type(Type.MAP, "Map of options.")
				)
				.returns(
					type(Type.INTEGER, "The normal return of this tool's process."),
					type(Type.ERROR, "BadOptions", "If the options map could not be applied."),
					type(Type.ERROR, "Security", "If the OS is preventing the read.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try 
			{
				PrintStream stdout = scriptInstance.getEnvironment().getStandardOut();
				PrintStream stderr = scriptInstance.getEnvironment().getStandardErr();
				WADTexMain.Options options = WADTexMain.options(stdout, stderr);
				scriptInstance.popStackValue(temp);
				if (!temp.isNull())
				{
					if (!temp.isMap())
					{
						returnValue.setError("BadOptions", "Options parameter needs to be a Map type.");
						return true;
					}
					else if (!temp.mapApply(options))
					{
						returnValue.setError("BadOptions", "Options Map could not be applied.");
						return true;
					}
				}
				returnValue.set(WADTexMain.call(options));
				return true;
			} catch (ClassCastException e) {
				returnValue.setError("BadOptions", "Options Map could not be applied: " + e.getLocalizedMessage());
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},

	WSWANTBL(1)
	{
		@Override
		protected Usage usage() 
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Calls the WSwAnTbl tool. Inherits STDOUT/STDERR of this script unless overridden (see options)."
				)
				.parameter("options", 
					type(Type.MAP, "Map of options.")
				)
				.returns(
					type(Type.INTEGER, "The normal return of this tool's process."),
					type(Type.ERROR, "BadOptions", "If the options map could not be applied."),
					type(Type.ERROR, "Security", "If the OS is preventing the read.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try 
			{
				PrintStream stdout = scriptInstance.getEnvironment().getStandardOut();
				PrintStream stderr = scriptInstance.getEnvironment().getStandardErr();
				WSwAnTablesMain.Options options = WSwAnTablesMain.options(stdout, stderr);
				scriptInstance.popStackValue(temp);
				if (!temp.isNull())
				{
					if (!temp.isMap())
					{
						returnValue.setError("BadOptions", "Options parameter needs to be a Map type.");
						return true;
					}
					else if (!temp.mapApply(options))
					{
						returnValue.setError("BadOptions", "Options Map could not be applied.");
						return true;
					}
				}
				returnValue.set(WSwAnTablesMain.call(options));
				return true;
			} catch (ClassCastException e) {
				returnValue.setError("BadOptions", "Options Map could not be applied: " + e.getLocalizedMessage());
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},

	WTEXPORT(1)
	{
		@Override
		protected Usage usage() 
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Calls the WTEXport tool. Inherits STDOUT/STDERR/STDIN of this script unless overridden (see options)."
				)
				.parameter("options", 
					type(Type.MAP, "Map of options.")
				)
				.returns(
					type(Type.INTEGER, "The normal return of this tool's process."),
					type(Type.ERROR, "BadOptions", "If the options map could not be applied."),
					type(Type.ERROR, "Security", "If the OS is preventing the read.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try 
			{
				PrintStream stdout = scriptInstance.getEnvironment().getStandardOut();
				PrintStream stderr = scriptInstance.getEnvironment().getStandardErr();
				InputStream stdin = scriptInstance.getEnvironment().getStandardIn();
				WTExportMain.Options options = WTExportMain.options(stdout, stderr, stdin);
				scriptInstance.popStackValue(temp);
				if (!temp.isNull())
				{
					if (!temp.isMap())
					{
						returnValue.setError("BadOptions", "Options parameter needs to be a Map type.");
						return true;
					}
					else if (!temp.mapApply(options))
					{
						returnValue.setError("BadOptions", "Options Map could not be applied.");
						return true;
					}
				}
				returnValue.set(WTExportMain.call(options));
				return true;
			} catch (OptionParseException e) {
				returnValue.setError("BadOptions", "Option argument parse failed: " + e.getLocalizedMessage());
				return true;
			} catch (ClassCastException e) {
				returnValue.setError("BadOptions", "Options Map could not be applied: " + e.getLocalizedMessage());
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},

	WTEXSCAN(1)
	{
		@Override
		protected Usage usage() 
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Calls the WTexScan tool. Inherits STDOUT/STDERR of this script unless overridden (see options)."
				)
				.parameter("options", 
					type(Type.MAP, "Map of options.")
				)
				.returns(
					type(Type.INTEGER, "The normal return of this tool's process."),
					type(Type.ERROR, "BadOptions", "If the options map could not be applied."),
					type(Type.ERROR, "Security", "If the OS is preventing the read.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try 
			{
				PrintStream stdout = scriptInstance.getEnvironment().getStandardOut();
				PrintStream stderr = scriptInstance.getEnvironment().getStandardErr();
				WTexScanMain.Options options = WTexScanMain.options(stdout, stderr);
				scriptInstance.popStackValue(temp);
				if (!temp.isNull())
				{
					if (!temp.isMap())
					{
						returnValue.setError("BadOptions", "Options parameter needs to be a Map type.");
						return true;
					}
					else if (!temp.mapApply(options))
					{
						returnValue.setError("BadOptions", "Options Map could not be applied.");
						return true;
					}
				}
				returnValue.set(WTexScanMain.call(options));
				return true;
			} catch (OptionParseException e) {
				returnValue.setError("BadOptions", "Option argument parse failed: " + e.getLocalizedMessage());
				return true;
			} catch (ClassCastException e) {
				returnValue.setError("BadOptions", "Options Map could not be applied: " + e.getLocalizedMessage());
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
	private ToolInvocationFunctions(int parameterCount)
	{
		this.parameterCount = parameterCount;
		this.usage = null;
	}
	
	/**
	 * @return a function resolver that handles all of the functions in this enum.
	 */
	public static final ScriptFunctionResolver createResolver()
	{
		return new EnumFunctionResolver(ToolInvocationFunctions.values());
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
	
	protected abstract Usage usage();

	@Override
	public abstract boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue);

	// Threadlocal "stack" values.
	private static final ThreadLocal<ScriptValue> CACHEVALUE1 = ThreadLocal.withInitial(()->ScriptValue.create(null));

}

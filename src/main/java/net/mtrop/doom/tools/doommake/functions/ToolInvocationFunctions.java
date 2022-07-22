/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.doommake.functions;

import static com.blackrook.rookscript.lang.ScriptFunctionUsage.type;

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import com.blackrook.rookscript.ScriptInstance;
import com.blackrook.rookscript.ScriptIteratorType;
import com.blackrook.rookscript.ScriptValue;
import com.blackrook.rookscript.ScriptValue.Type;
import com.blackrook.rookscript.lang.ScriptFunctionType;
import com.blackrook.rookscript.lang.ScriptFunctionUsage;
import com.blackrook.rookscript.resolvers.ScriptFunctionResolver;
import com.blackrook.rookscript.resolvers.hostfunction.EnumFunctionResolver;

import net.mtrop.doom.tools.DMXConvertMain;
import net.mtrop.doom.tools.DecoHackMain;
import net.mtrop.doom.tools.DoomImageConvertMain;
import net.mtrop.doom.tools.DoomMakeMain;
import net.mtrop.doom.tools.DoomToolsMain;
import net.mtrop.doom.tools.WADTexMain;
import net.mtrop.doom.tools.WSwAnTablesMain;
import net.mtrop.doom.tools.WTExportMain;
import net.mtrop.doom.tools.WTexScanMain;
import net.mtrop.doom.tools.WadMergeMain;
import net.mtrop.doom.tools.WadScriptMain;
import net.mtrop.doom.tools.exception.OptionParseException;
import net.mtrop.doom.tools.struct.util.StringUtils;

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
						"{" + StringUtils.joinStrings(", ",
							"stdout:OBJECTREF(OutputStream)",
							"stderr:OBJECTREF(OutputStream)",
							"openWebsite:BOOLEAN",
							"openDocs:BOOLEAN"
						) + "}",
						"Map of options."
					)
				)
				.returns(
					type(Type.INTEGER, "The normal return of this tool's process."),
					type(Type.ERROR, "BadOptions", "If the options map could not be applied.")
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
					"It is important to note that the instance of DoomMake that runs has no inherent link to this instance."
				)
				.parameter("options", 
					type(Type.MAP, 
						"{" + StringUtils.joinStrings(", ",
							"stdout:OBJECTREF(OutputStream)",
							"stderr:OBJECTREF(OutputStream)",
							"stdin:OBJECTREF(InputStream)",
							"targetName:STRING",
							"propertiesFile:OBJECTREF(File)",
							"scriptFile:OBJECTREF(File)",
							"runawayLimit:INTEGER",
							"activationDepth:INTEGER",
							"stackDepth:INTEGER",
							"args:LIST[STRING, ...]"
						) + "}",
						"Map of options."
					)
				)
				.returns(
					type(Type.INTEGER, "The normal return of this tool's process."),
					type(Type.ERROR, "BadOptions", "If the options map could not be applied.")
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
					"Calls the DecoHack tool. Inherits STDOUT/STDERR/STDIN of this script unless overridden (see options). " +
					"If [useStdin] is true, then it will read from the [stdin] stream, either set or inherited. " +
					"Also, do not use both [infile] and [infiles] as options. Use one or the other."
				)
				.parameter("options", 
					type(Type.MAP, 
						"{" + StringUtils.joinStrings(", ",
							"stdout:OBJECTREF(OutputStream)",
							"stderr:OBJECTREF(OutputStream)",
							"stdin:OBJECTREF(InputStream)",
							"useStdin:BOOLEAN",
							"inFile:OBJECTREF(File)",
							"inFiles:LIST[OBJECTREF(File), ...]",
							"inCharsetName:STRING",
							"outFile:OBJECTREF(File)",
							"outSourceFile:OBJECTREF(File)",
							"outCharsetName:STRING",
							"outputBudget:BOOLEAN"
						) + "}",
						"Map of options."
					)
				)
				.returns(
					type(Type.INTEGER, "The normal return of this tool's process."),
					type(Type.ERROR, "BadOptions", "If the options map could not be applied.")
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
				DecoHackMain.Options options = DecoHackMain.options(stdout, stderr, stdin);
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

	DIMGCONVERT(1)
	{
		@Override
		protected Usage usage() 
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Calls the DImgConv tool. Inherits STDOUT/STDERR of this script unless overridden (see options)."
				)
				.parameter("options", 
					type(Type.MAP, 
						"{" + StringUtils.joinStrings(", ",
							"stdout:OBJECTREF(OutputStream)",
							"stderr:OBJECTREF(OutputStream)",
							"sourcePath:OBJECTREF(File)",
							"outputPath:OBJECTREF(File)",
							"recursive:BOOLEAN",
							"paletteSourcePath:OBJECTREF(File)",
							"modeType:STRING (one of 'palettes', 'colormaps', 'graphics', 'flats')",
							"metaInfoFilename:STRING",
							"verbose:BOOLEAN"
						) + "}",
						"Map of options."
					)
				)
				.returns(
					type(Type.INTEGER, "The normal return of this tool's process."),
					type(Type.ERROR, "BadOptions", "If the options map could not be applied.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue files = CACHEVALUE2.get();
			try 
			{
				PrintStream stdout = scriptInstance.getEnvironment().getStandardOut();
				PrintStream stderr = scriptInstance.getEnvironment().getStandardErr();
				DoomImageConvertMain.Options options = DoomImageConvertMain.options(stdout, stderr);
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
				returnValue.set(DoomImageConvertMain.call(options));
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
				files.setNull();
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
					type(Type.MAP, 
						"{" + StringUtils.joinStrings(", ",
							"stdout:OBJECTREF(OutputStream)",
							"stderr:OBJECTREF(OutputStream)",
							"files:LIST[STRING, ...]",
							"outputdirectory:OBJECTREF(File)",
							"ffmpegpath:OBJECTREF(File)",
							"onlyffmpeg:BOOLEAN",
							"onlyjspi:BOOLEAN"
						) + "}",
						"Map of options."
					)
				)
				.returns(
					type(Type.INTEGER, "The normal return of this tool's process."),
					type(Type.ERROR, "BadOptions", "If the options map could not be applied.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue files = CACHEVALUE2.get();
			try 
			{
				PrintStream stdout = scriptInstance.getEnvironment().getStandardOut();
				PrintStream stderr = scriptInstance.getEnvironment().getStandardErr();
				DMXConvertMain.Options options = DMXConvertMain.options(stdout, stderr);
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
					
					temp.mapGet("files", files);
					if (!files.isNull() && files.isList())
					{
						for (ScriptIteratorType.IteratorPair pair : files)
						{
							ScriptValue value = pair.getValue();
							if (value.isString())
								options.addInputFile(new File(value.asString()));
							else if (value.isObjectType(File.class))
								options.addInputFile(value.asObjectType(File.class));
						}
					}
				}
				returnValue.set(DMXConvertMain.call(options));
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
				files.setNull();
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
					type(Type.MAP, 
						"{" + StringUtils.joinStrings(", ",
							"stdout:OBJECTREF(OutputStream)",
							"stderr:OBJECTREF(OutputStream)",
							"stdin:OBJECTREF(InputStream)",
							"inputfile:OBJECTREF(File)",
							"inputCharsetName:STRING",
							"args:LIST[STRING, ...]",
							"usestdin:BOOLEAN",
							"verbose:BOOLEAN"
						) + "}",
						"Map of options."
					)
				)
				.returns(
					type(Type.INTEGER, "The normal return of this tool's process."),
					type(Type.ERROR, "BadOptions", "If the options map could not be applied.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue args = CACHEVALUE2.get();
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

				temp.mapGet("args", args);
				if (!args.isNull() && args.isList())
				{
					for (ScriptIteratorType.IteratorPair pair : args)
					{
						ScriptValue value = pair.getValue();
						options.addArg(value.asString());
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
				args.setNull();
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
					"It is important to note that the instance of WadScript that runs has no inherent link to this instance."
				)
				.parameter("options", 
					type(Type.MAP, 
						"{" + StringUtils.joinStrings(", ",
							"stdout:OBJECTREF(OutputStream)",
							"stderr:OBJECTREF(OutputStream)",
							"stdin:OBJECTREF(InputStream)",
							"scriptFile:OBJECTREF(File)",
							"scriptCharsetName:STRING",
							"entryPointName:STRING",
							"runawayLimit:INTEGER",
							"activationDepth:INTEGER",
							"stackDepth:INTEGER",
							"args:LIST[STRING, ...]"
						) + "}",
						"Map of options."
					)
				)
				.returns(
					type(Type.INTEGER, "The normal return of this tool's process."),
					type(Type.ERROR, "BadOptions", "If the options map could not be applied.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue args = CACHEVALUE2.get();
			try 
			{
				PrintStream stdout = scriptInstance.getEnvironment().getStandardOut();
				PrintStream stderr = scriptInstance.getEnvironment().getStandardErr();
				InputStream stdin = scriptInstance.getEnvironment().getStandardIn();
				WadScriptMain.Options options = WadScriptMain.options(stdout, stderr, stdin);
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

				temp.mapGet("args", args);
				if (!args.isNull() && args.isList())
				{
					for (ScriptIteratorType.IteratorPair pair : args)
					{
						ScriptValue value = pair.getValue();
						options.addArg(value.asString());
					}
				}

				returnValue.set(WadScriptMain.call(options));
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
				args.setNull();
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
					type(Type.MAP, 
						"{" + StringUtils.joinStrings(", ",
							"stdout:OBJECTREF(OutputStream)",
							"stderr:OBJECTREF(OutputStream)",
							"sourceFile:OBJECTREF(File)",
							"wadFile:OBJECTREF(File)",
							"additive:BOOLEAN",
							"exportMode:BOOLEAN",
							"entryName:STRING",
							"strife:BOOLEAN",
							"verbose:BOOLEAN"
						) + "}",
						"Map of options."
					)
				)
				.returns(
					type(Type.INTEGER, "The normal return of this tool's process."),
					type(Type.ERROR, "BadOptions", "If the options map could not be applied.")
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
					type(Type.MAP, 
						"{" + StringUtils.joinStrings(", ",
							"stdout:OBJECTREF(OutputStream)",
							"stderr:OBJECTREF(OutputStream)",
							"sourceFile:OBJECTREF(File)",
							"wadFile:OBJECTREF(File)",
							"exportMode:BOOLEAN",
							"importSource:BOOLEAN",
							"verbose:BOOLEAN"
						) + "}",
						"Map of options."
					)
				)
				.returns(
					type(Type.INTEGER, "The normal return of this tool's process."),
					type(Type.ERROR, "BadOptions", "If the options map could not be applied.")
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
					type(Type.MAP, 
						"{" + StringUtils.joinStrings(", ",
							"stdout:OBJECTREF(OutputStream)",
							"stderr:OBJECTREF(OutputStream)",
							"stdin:OBJECTREF(InputStream)",
							"texturewads:LIST[STRING, ...]",
							"basewad:OBJECTREF(File)",
							"outwad:OBJECTREF(File)",
							"additive:BOOLEAN",
							"nulltexture:STRING",
							"noanimated:BOOLEAN",
							"noswitches:BOOLEAN"
						) + "}",
						"Map of options."
					)
				)
				.returns(
					type(Type.INTEGER, "The normal return of this tool's process."),
					type(Type.ERROR, "BadOptions", "If the options map could not be applied.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue args = CACHEVALUE2.get();
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
				temp.mapGet("texturewads", args);
				if (!args.isNull() && args.isList())
				{
					for (ScriptIteratorType.IteratorPair pair : args)
					{
						ScriptValue value = pair.getValue();
						if (value.isObjectRef(File.class))
							options.addFilePath(value.asObjectType(File.class).getPath());
						else
							options.addFilePath(value.asString());
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
				args.setNull();
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
					type(Type.MAP, 
						"{" + StringUtils.joinStrings(", ",
							"stdout:OBJECTREF(OutputStream)",
							"stderr:OBJECTREF(OutputStream)",
							"wadfiles:LIST[STRING, ...]",
							"mapsToScan:LIST[STRING, ...]",
							"quiet:BOOLEAN",
							"outputtextures:BOOLEAN",
							"outputflats:BOOLEAN",
							"skipskies:BOOLEAN"
						) + "}",
						"Map of options."
					)
				)
				.returns(
					type(Type.INTEGER, "The normal return of this tool's process."),
					type(Type.ERROR, "BadOptions", "If the options map could not be applied.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue args = CACHEVALUE2.get();
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
				temp.mapGet("wadfiles", args);
				if (!args.isNull() && args.isList())
				{
					for (ScriptIteratorType.IteratorPair pair : args)
					{
						ScriptValue value = pair.getValue();
						if (value.isObjectRef(File.class))
							options.addWadFile(value.asObjectType(File.class));
						else
							options.addWadFile(new File(value.asString()));
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
				args.setNull();
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
	private static final ThreadLocal<ScriptValue> CACHEVALUE2 = ThreadLocal.withInitial(()->ScriptValue.create(null));

}

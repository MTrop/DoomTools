/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.exception.OptionParseException;

/**
 * Main class for Utility.
 * @author Matthew Tropiano
 */
public final class DoomToolsMain 
{
	private static final String VERSION = Common.getVersionString("doomtools");

	private static final String DOOM_VERSION = Common.getVersionString("doom");
	private static final String ROOKSCRIPT_VERSION = Common.getVersionString("rookscript");

	private static final String DECOHACK_VERSION = Common.getVersionString("decohack");
	private static final String DMXCONV_VERSION = Common.getVersionString("dmxconv");
	private static final String WADMERGE_VERSION = Common.getVersionString("wadmerge");
	private static final String WADSCRIPT_VERSION = Common.getVersionString("wadscript");
	private static final String WADTEX_VERSION = Common.getVersionString("wadtex");
	private static final String WSWANTBLS_VERSION = Common.getVersionString("wswantbls");
	private static final String WTEXPORT_VERSION = Common.getVersionString("wtexport");
	private static final String WTEXSCAN_VERSION = Common.getVersionString("wtexscan");

	private static final int ERROR_NONE = 0;
		
	/**
	 * Program options.
	 */
	public static class Options
	{
		private PrintStream stdout;
		
		private Options()
		{
			this.stdout = null;
		}
		
		public Options setStdout(OutputStream out) 
		{
			this.stdout = new PrintStream(out, true);;
			return this;
		}
		
	}
	
	/**
	 * Program context.
	 */
	private static class Context
	{
		private Options options;
	
		private Context(Options options)
		{
			this.options = options;
		}
		
		public int call()
		{
			options.stdout.println("DoomTools v" + VERSION);
			options.stdout.println();
			options.stdout.println("Using DoomStruct v" + DOOM_VERSION);
			options.stdout.println("Using Rookscript v" + ROOKSCRIPT_VERSION);
			options.stdout.println();
			options.stdout.println("Contains DECOHack v" + DECOHACK_VERSION);
			options.stdout.println("Contains DMXConv v" + DMXCONV_VERSION);
			options.stdout.println("Contains WadMerge v" + WADMERGE_VERSION);
			options.stdout.println("Contains WadScript v" + WADSCRIPT_VERSION);
			options.stdout.println("Contains WADTex v" + WADTEX_VERSION);
			options.stdout.println("Contains WSwAnTBLs v" + WSWANTBLS_VERSION);
			options.stdout.println("Contains WTExport v" + WTEXPORT_VERSION);
			options.stdout.println("Contains WTexScan v" + WTEXSCAN_VERSION);
			return ERROR_NONE;
		}
	}
	
	/**
	 * Reads command line arguments and sets options.
	 * @param out the standard output print stream.
	 * @param err the standard error print stream. 
	 * @param args the argument args.
	 * @return the parsed options.
	 * @throws OptionParseException if a parse exception occurs.
	 */
	public static Options options(PrintStream out, PrintStream err, String ... args) throws OptionParseException
	{
		Options options = new Options();
		options.stdout = out;
		return options;
	}
	
	/**
	 * Calls the utility using a set of options.
	 * @param options the options to call with.
	 * @return the error code.
	 */
	public static int call(Options options)
	{
		return (new Context(options)).call();
	}
	
	public static void main(String[] args) throws IOException
	{
		try {
			System.exit(call(options(System.out, System.err, args)));
		} catch (OptionParseException e) {
			System.err.println(e.getMessage());
			System.exit(ERROR_NONE);
		}
	}
	
}

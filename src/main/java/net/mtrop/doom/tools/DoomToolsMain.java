/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.exception.OptionParseException;

/**
 * Main class for DoomTools.
 * @author Matthew Tropiano
 */
public final class DoomToolsMain 
{
	private static final String VERSION = Common.getVersionString("doomtools");

	private static final String DOOM_VERSION = Common.getVersionString("doom");
	private static final String ROOKSCRIPT_VERSION = Common.getVersionString("rookscript");
	private static final String ROOKSCRIPT_DESKTOP_VERSION = Common.getVersionString("rookscript-desktop");

	private static final String DECOHACK_VERSION = Common.getVersionString("decohack");
	private static final String DIMGCONV_VERSION = Common.getVersionString("dimgconv");
	private static final String DMXCONV_VERSION = Common.getVersionString("dmxconv");
	private static final String DOOMMAKE_VERSION = Common.getVersionString("doommake");
	private static final String WADMERGE_VERSION = Common.getVersionString("wadmerge");
	private static final String WADSCRIPT_VERSION = Common.getVersionString("wadscript");
	private static final String WADTEX_VERSION = Common.getVersionString("wadtex");
	private static final String WSWANTBLS_VERSION = Common.getVersionString("wswantbls");
	private static final String WTEXPORT_VERSION = Common.getVersionString("wtexport");
	private static final String WTEXSCAN_VERSION = Common.getVersionString("wtexscan");

	private static final String DOOMTOOLS_WEBSITE = "https://mtrop.github.io/DoomTools/";
	
	private static final int ERROR_NONE = 0;
	private static final int ERROR_DESKTOP_ERROR = 1;
	
	private static final String SWITCH_HELP = "--help";
	private static final String SWITCH_HELP2 = "-h";
	private static final String SWITCH_WEBSITE = "--website";

	
	/**
	 * Program options.
	 */
	public static class Options
	{
		private PrintStream stdout;
		private PrintStream stderr;
		
		private boolean help;
		private boolean openWebsite;
		
		private Options()
		{
			this.stdout = null;
			this.help = false;
			this.openWebsite = false;
		}
		
		public Options setStdout(OutputStream out) 
		{
			this.stdout = new PrintStream(out, true);
			return this;
		}
		
		public Options setStderr(OutputStream err) 
		{
			this.stderr = new PrintStream(err, true);
			return this;
		}
		
		public Options setOpenWebsite(boolean value)
		{
			this.openWebsite = value;
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
			if (options.help)
			{
				options.stdout.println("DoomTools v" + VERSION);
				options.stdout.println();
				help(options.stdout);
				return ERROR_NONE;
			}
			else if (options.openWebsite)
			{
				if (!Desktop.isDesktopSupported())
				{
					options.stderr.println("ERROR: No desktop support. Cannot open website.");
					return ERROR_DESKTOP_ERROR;
				}

				if (!Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
				{
					options.stderr.println("ERROR: No default browser support. Cannot open website.");
					return ERROR_DESKTOP_ERROR;
				}				
				
				try {
					options.stdout.println("Opening the DoomTools website...");
					Desktop.getDesktop().browse(new URI(DOOMTOOLS_WEBSITE));
				} catch (URISyntaxException e) {
					options.stderr.println("ERROR: INTERNAL ERROR: " + e.getLocalizedMessage());
					return ERROR_DESKTOP_ERROR;
				} catch (IOException e) {
					options.stderr.println("ERROR: Cannot launch browser. Cannot open website.");
					return ERROR_DESKTOP_ERROR;
				} catch (SecurityException e) {
					options.stderr.println("ERROR: Cannot launch browser: OS is preventing browser access. Cannot open website.");
					return ERROR_DESKTOP_ERROR;
				}

				return ERROR_NONE;
			}
			else
			{
				options.stdout.println("DoomTools v" + VERSION);
				options.stdout.println("Run with `--help` for more options.");
				options.stdout.println();
				options.stdout.println("Using DoomStruct v" + DOOM_VERSION);
				options.stdout.println("Using Rookscript v" + ROOKSCRIPT_VERSION);
				options.stdout.println("Using Rookscript-Desktop v" + ROOKSCRIPT_DESKTOP_VERSION);
				options.stdout.println();
				options.stdout.println("Contains DECOHack v" + DECOHACK_VERSION);
				options.stdout.println("Contains DImgConv v" + DIMGCONV_VERSION);
				options.stdout.println("Contains DMXConv v" + DMXCONV_VERSION);
				options.stdout.println("Contains DoomMake v" + DOOMMAKE_VERSION);
				options.stdout.println("Contains WadMerge v" + WADMERGE_VERSION);
				options.stdout.println("Contains WadScript v" + WADSCRIPT_VERSION);
				options.stdout.println("Contains WADTex v" + WADTEX_VERSION);
				options.stdout.println("Contains WSwAnTBLs v" + WSWANTBLS_VERSION);
				options.stdout.println("Contains WTExport v" + WTEXPORT_VERSION);
				options.stdout.println("Contains WTexScan v" + WTEXSCAN_VERSION);
				return ERROR_NONE;
			}
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
		options.stderr = err;
		
		final int STATE_START = 0;
		int state = STATE_START;

		int i = 0;
		while (i < args.length)
		{
			String arg = args[i];
			switch (state)
			{
				case STATE_START:
				{
					if (arg.equalsIgnoreCase(SWITCH_HELP) || arg.equalsIgnoreCase(SWITCH_HELP2))
						options.help = true;
					else if (arg.equalsIgnoreCase(SWITCH_WEBSITE))
						options.openWebsite = true;
				}
				break;
			}
			i++;
		}
		
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

	/**
	 * Prints the help.
	 * @param out the print stream to print to.
	 */
	private static void help(PrintStream out)
	{
		out.println("    --help        Prints help and exits.");
		out.println("    -h");
		out.println();
		out.println("    --website     Opens DoomTools's main website.");
	}
	
}

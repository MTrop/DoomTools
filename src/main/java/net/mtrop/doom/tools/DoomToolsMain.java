/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools;

import java.awt.Desktop;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.Callable;

import com.blackrook.rookscript.tools.ScriptExecutor;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.doomtools.DoomToolsUpdater;
import net.mtrop.doom.tools.exception.OptionParseException;
import net.mtrop.doom.tools.gui.DoomToolsGUIMain;
import net.mtrop.doom.tools.gui.DoomToolsConstants.Paths;
import net.mtrop.doom.tools.struct.util.OSUtils;

/**
 * Main class for DoomTools.
 * @author Matthew Tropiano
 */
public final class DoomToolsMain 
{
	public static final String DOOMTOOLS_WEBSITE = "https://mtrop.github.io/DoomTools/";
	public static final String DOOMTOOLS_REPO_WEBSITE = "https://github.com/MTrop/DoomTools";

	public static final int ERROR_NONE = 0;
	public static final int ERROR_DESKTOP_ERROR = 1;
	public static final int ERROR_SECURITY = 2;
	public static final int ERROR_NOWHERE = 3;
	public static final int ERROR_TIMEOUT = 4;
	public static final int ERROR_SITE_ERROR = 5;
	public static final int ERROR_IOERROR = 6;
	public static final int ERROR_GUI_ALREADY_RUNNING = 7;
	public static final int ERROR_TASK_CANCELLED = 8;
	public static final int ERROR_UNKNOWN = -1;

	private static final String SHELL_OPTIONS = "-Xms64M -Xmx768M";
	
	private static final Map<String, Class<?>> SHELL_DATA = Common.map(
		Common.keyValue("doomtools",  DoomToolsMain.class),
		Common.keyValue("wadmerge",   WadMergeMain.class),
		Common.keyValue("wswantbl",   WSwAnTablesMain.class),
		Common.keyValue("wadtex",     WADTexMain.class),
		Common.keyValue("wtexscan",   WTexScanMain.class),
		Common.keyValue("wtexport",   WTExportMain.class),
		Common.keyValue("wadscript",  WadScriptMain.class),
		Common.keyValue("decohack",   DecoHackMain.class),
		Common.keyValue("dmxconv",    DMXConvertMain.class),
		Common.keyValue("dimgconv",   DoomImageConvertMain.class),
		Common.keyValue("doommake",   DoomMakeMain.class),
		Common.keyValue("rookscript", ScriptExecutor.class)
	);
		
	private static final FileFilter JAR_FILES = (f) -> {
		return Common.getFileExtension(f.getName()).equalsIgnoreCase("jar");
	};

	private static final String SWITCH_HELP = "--help";
	private static final String SWITCH_HELP2 = "-h";
	private static final String SWITCH_WEBSITE = "--website";
	private static final String SWITCH_DOCS = "--docs";
	private static final String SWITCH_WHERE = "--where";
	private static final String SWITCH_SETTINGS = "--settings";
	private static final String SWITCH_UPDATE = "--update";
	private static final String SWITCH_UPDATE_CLEANUP = "--update-cleanup";
	private static final String SWITCH_UPDATE_SHELL = "--update-shell";
	private static final String SWITCH_GUI = "--gui";
	
	/**
	 * Program options.
	 */
	public static class Options
	{
		private PrintStream stdout;
		private PrintStream stderr;
		
		private boolean help;
		private boolean update;
		private boolean updateCleanup;
		private boolean updateShell;
		private boolean openWebsite;
		private boolean openDocs;
		private boolean where;
		private boolean openSettings;
		private boolean gui;
		
		private Options()
		{
			this.stdout = null;
			this.help = false;
			this.update = false;
			this.updateCleanup = false;
			this.updateShell = false;
			this.openWebsite = false;
			this.where = false;
			this.openSettings = false;
			this.gui = false;
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
		
		public Options setOpenDocs(boolean value)
		{
			this.openDocs = value;
			return this;
		}
		
	}
	
	/**
	 * Program context.
	 */
	private static class Context implements Callable<Integer>
	{
		private Options options;
	
		private Context(Options options)
		{
			this.options = options;
		}
		
		private static File[] getSortedJarList(File dir)
		{
			File[] jars = dir.listFiles(JAR_FILES);
			Arrays.sort(jars, (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName()));
			return jars;
		}
		
		private static String getProgressBar(long current, Long max)
		{
			final int MAXPIPS = 40;
			if (max != null && max != 0)
			{
				StringBuilder sb = new StringBuilder("[");
				
				int i;
				long pips = current / (max / MAXPIPS);
				for (i = 0; i < pips; i++)
					sb.append('.');
				for (; i < MAXPIPS; i++)
					sb.append(' ');
				sb.append("] %d KB / %d KB");
				return String.format(sb.toString(), current / 1024, max / 1024);
			}
			else
			{
				return String.format(" ... %d KB", current / 1024);
			}
		}

		public int doUpdateShell()
		{
			final String path; 
			try {
				path = Environment.getDoomToolsPath();
			} catch (SecurityException e) {
				options.stderr.println("ERROR: Could not fetch value of ENVVAR.");
				return ERROR_SECURITY;
			}
			if (Common.isEmpty(path))
			{
				options.stderr.println("ERROR: DOOMTOOLS_PATH ENVVAR not set. Not invoked via shell?");
				return ERROR_NOWHERE;
			}
			
			final String shellSourceFile = OSUtils.isWindows() ? "app-name.cmd" : "app-name.sh";
			final String shellExtension = OSUtils.isWindows() ? ".cmd" : "";
			
			// Export shell scripts.
			for (Map.Entry<String, Class<?>> entry : SHELL_DATA.entrySet())
			{
				File outputFilePath = new File(path + "/" + entry.getKey() + shellExtension);
				try {
					Common.copyShellScript("shell/jar/" + shellSourceFile, entry.getValue(), SHELL_OPTIONS, "", "java", outputFilePath);
					if (!OSUtils.isWindows())
						outputFilePath.setExecutable(true, false);
					options.stdout.println("Created `" + outputFilePath.getPath() + "`.");
				} catch (IOException e) {
					options.stderr.println("ERROR: Could not create `" + outputFilePath.getPath() + "`.");
					return ERROR_IOERROR;
				} catch (SecurityException e) {
					options.stderr.println("ERROR: Could not create `" + outputFilePath.getPath() + "`. Access denied by OS.");
					return ERROR_SECURITY;
				}
			}
			
			options.stdout.println("Done!");
			return ERROR_NONE;
		}
		
		public int doUpdateCleanup()
		{
			final String path; 
			try {
				path = Environment.getDoomToolsPath();
			} catch (SecurityException e) {
				options.stderr.println("ERROR: Could not fetch value of ENVVAR.");
				return ERROR_SECURITY;
			}
			if (Common.isEmpty(path))
			{
				options.stderr.println("ERROR: DOOMTOOLS_PATH ENVVAR not set. Not invoked via shell?");
				return ERROR_NOWHERE;
			}

			options.stdout.println("Cleaning up older versions...");
			
			File[] jars = getSortedJarList(new File(path + "/jar"));
			for (int i = 0; i < jars.length - 1; i++)
			{
				if (!jars[i].delete())
				{
					options.stderr.println("ERROR: Could not delete " + jars[i].getName());
					return ERROR_IOERROR;
				}
			}

			options.stdout.println("Done!");
			return ERROR_NONE;
		}
		
		public int doUpdate()
		{
			final String path; 
			try {
				path = Environment.getDoomToolsPath();
			} catch (SecurityException e) {
				options.stderr.println("ERROR: Could not fetch value of ENVVAR.");
				return ERROR_SECURITY;
			}
			if (Common.isEmpty(path))
			{
				options.stderr.println("ERROR: DOOMTOOLS_PATH ENVVAR not set. Not invoked via shell?");
				return ERROR_NOWHERE;
			}

			// Listener 
			DoomToolsUpdater.Listener listener = new DoomToolsUpdater.Listener() 
			{
				@Override
				public void onMessage(String message) 
				{
					options.stdout.println(message);
				}

				@Override
				public void onError(String message) 
				{
					options.stderr.println("ERROR: " + message);
				}

				@Override
				public void onDownloadStart() 
				{
					// Do nothing.
				}

				@Override
				public void onDownloadTransfer(long current, Long max) 
				{
					options.stdout.print("\r" + getProgressBar(current, max));
				}

				@Override
				public void onDownloadFinish() 
				{
					// Do nothing.
				}

				@Override
				public void onUpToDate() 
				{
					options.stdout.println("DoomTools is up-to-date!");
				}

				@Override
				public void onUpdateSuccessful() 
				{
					options.stdout.println("DoomTools is up-to-date!");
				}

				@Override
				public boolean shouldContinue(String versionString)
				{
					options.stdout.println("New version found:  " + versionString);
					return true; // just continue.
				}

				@Override
				public void onUpdateAbort() 
				{
					options.stdout.println("Update aborted!");
				}
				
			};
			
			try {
				return (new DoomToolsUpdater(new File(path), listener)).call();
			} catch (Exception e) {
				options.stderr.println("ERROR: Uncaught error during update: " + e.getClass().getSimpleName());
				return ERROR_UNKNOWN;
			}
		}
		
		@Override
		public Integer call()
		{
			if (options.help)
			{
				options.stdout.println("DoomTools v" + Version.DOOMTOOLS);
				options.stdout.println();
				help(options.stdout);
				return ERROR_NONE;
			}
			else if (options.gui)
			{
				if (DoomToolsGUIMain.isAlreadyRunning())
				{
					options.stderr.println("DoomTools is already running.");
					return ERROR_GUI_ALREADY_RUNNING;
				}
				else
				{
					try {
						DoomToolsGUIMain.startGUIAppProcess();
					} catch (IOException e) {
						options.stderr.println("ERROR: Could not start DoomTools GUI process!");
						return ERROR_IOERROR;
					}
					return ERROR_NONE;
				}
			}
			else if (options.updateShell)
			{
				return doUpdateShell();
			}
			else if (options.updateCleanup)
			{
				return doUpdateCleanup();
			}
			else if (options.update)
			{
				return doUpdate();
			}
			else if (options.where)
			{
				String path; 
				try {
					path = Environment.getDoomToolsPath();
				} catch (SecurityException e) {
					options.stderr.println("ERROR: Could not fetch value of ENVVAR.");
					return ERROR_SECURITY;
				}
				
				if (Common.isEmpty(path))
				{
					options.stderr.println("ERROR: DOOMTOOLS_PATH ENVVAR not set. Not invoked via shell?");
					return ERROR_NOWHERE;
				}
				else
				{
					options.stdout.println(path);
					return ERROR_NONE;
				}
			}
			else if (options.openDocs)
			{
				String path; 
				try {
					path = Environment.getDoomToolsPath();
				} catch (SecurityException e) {
					options.stderr.println("ERROR: Could not fetch value of ENVVAR.");
					return ERROR_SECURITY;
				}
				
				int desktopError;
				if ((desktopError = checkDesktopAction(Desktop.Action.OPEN, "documentation folder")) != ERROR_NONE)
					return desktopError;
				
				try {
					options.stdout.println("Opening the DoomTools documentation folder...");
					Desktop.getDesktop().open(new File(path + File.separator + "docs"));
				} catch (IOException e) {
					options.stderr.println("ERROR: Cannot open documentation folder. I/O Error.");
					return ERROR_DESKTOP_ERROR;
				} catch (SecurityException e) {
					options.stderr.println("ERROR: Cannot open documentation folder. OS is preventing folder access.");
					return ERROR_DESKTOP_ERROR;
				}

				return ERROR_NONE;
			}
			else if (options.openSettings)
			{
				int desktopError;
				if ((desktopError = checkDesktopAction(Desktop.Action.OPEN, "settings folder")) != ERROR_NONE)
					return desktopError;

				options.stdout.println("Opening the DoomTools settings folder...");
				File settingsDir = new File(Paths.APPDATA_PATH);
				if (!settingsDir.exists())
				{
					options.stdout.println("Creating the missing DoomTools settings folder...");
					if( !settingsDir.mkdirs())
					{
						options.stderr.println("ERROR: Cannot open settings folder. Not created nor found.");
						return ERROR_DESKTOP_ERROR;
					}
				}

				try {
					Desktop.getDesktop().open(settingsDir);
				} catch (IOException e) {
					options.stderr.println("ERROR: Cannot open settings folder. I/O Error.");
					return ERROR_DESKTOP_ERROR;
				} catch (SecurityException e) {
					options.stderr.println("ERROR: Cannot open settings folder. OS is preventing folder access.");
					return ERROR_DESKTOP_ERROR;
				}

				return ERROR_NONE;
			}
			else if (options.openWebsite)
			{
				int desktopError;
				if ((desktopError = checkDesktopAction(Desktop.Action.BROWSE, "website")) != ERROR_NONE)
					return desktopError;
				
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
				options.stdout.println("DoomTools v" + Version.DOOMTOOLS);
				options.stdout.println("Run with `--help` for more options.");
				options.stdout.println();
				options.stdout.println("Using DoomStruct v" + Version.DOOMSTRUCT);
				options.stdout.println("Using Black Rook JSON v" + Version.JSON);
				options.stdout.println("Using Rookscript v" + Version.ROOKSCRIPT);
				options.stdout.println("Using Rookscript-Desktop v" + Version.ROOKSCRIPT_DESKTOP);
				options.stdout.println();
				options.stdout.println("Contains DECOHack v" + Version.DECOHACK);
				options.stdout.println("Contains DImgConv v" + Version.DIMGCONV);
				options.stdout.println("Contains DMXConv v" + Version.DMXCONV);
				options.stdout.println("Contains DoomMake v" + Version.DOOMMAKE);
				options.stdout.println("Contains WadMerge v" + Version.WADMERGE);
				options.stdout.println("Contains WadScript v" + Version.WADSCRIPT);
				options.stdout.println("Contains WADTex v" + Version.WADTEX);
				options.stdout.println("Contains WSwAnTBLs v" + Version.WSWANTBLS);
				options.stdout.println("Contains WTExport v" + Version.WTEXPORT);
				options.stdout.println("Contains WTexScan v" + Version.WTEXSCAN);
				return ERROR_NONE;
			}
		}

		private int checkDesktopAction(Desktop.Action action, String name) 
		{
			if (!Desktop.isDesktopSupported())
			{
				options.stderr.println("ERROR: No desktop support. Cannot open " + name + ".");
				return ERROR_DESKTOP_ERROR;
			}

			if (!Desktop.getDesktop().isSupported(action))
			{
				options.stderr.println("ERROR: No support for desktop " + action.name() + ". Cannot open " + name + ".");
				return ERROR_DESKTOP_ERROR;
			}
			
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
					else if (arg.equalsIgnoreCase(SWITCH_GUI))
						options.gui = true;
					else if (arg.equalsIgnoreCase(SWITCH_WEBSITE))
						options.openWebsite = true;
					else if (arg.equalsIgnoreCase(SWITCH_SETTINGS))
						options.openSettings = true;
					else if (arg.equalsIgnoreCase(SWITCH_DOCS))
						options.openDocs = true;
					else if (arg.equalsIgnoreCase(SWITCH_WHERE))
						options.where = true;
					else if (arg.equalsIgnoreCase(SWITCH_UPDATE))
						options.update = true;
					else if (arg.equalsIgnoreCase(SWITCH_UPDATE_CLEANUP))
						options.updateCleanup = true;
					else if (arg.equalsIgnoreCase(SWITCH_UPDATE_SHELL))
						options.updateShell = true;
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
		try {
			return (int)(asCallable(options).call());
		} catch (Exception e) {
			e.printStackTrace(options.stderr);
			return ERROR_UNKNOWN;
		}
	}
	
	/**
	 * Creates a {@link Callable} for this utility.
	 * @param options the options to use.
	 * @return a Callable that returns the process error.
	 */
	public static Callable<Integer> asCallable(Options options)
	{
		return new Context(options);
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
		out.println("    --help               Prints help and exits.");
		out.println("    -h");
		out.println();
		out.println("    --docs               Opens DoomTools's documentation folder.");
		out.println();
		out.println("    --settings           Opens the folder where DoomTools stores global");
		out.println("                             (user-level) settings.");
		out.println();
		out.println("    --website            Opens DoomTools's main website.");
		out.println();
		out.println("    --where              Displays where DoomTools lives (ENVVAR test).");
		out.println();
		out.println("    --update             Attempts to update DoomTools (may require permission");
		out.println("                             elevation on some operating systems).");
		out.println();
		out.println("    --update-cleanup     Deletes all previous versions downloaded via update");
		out.println("                             except for the latest (may require permission");
		out.println("                             elevation on some operating systems).");
		out.println();
		out.println("    --update-shell       Updates the shell commands that invoke the tools.");
		out.println("                             If you are missing one, run DoomTools with this");
		out.println("                             switch.");
		out.println();
		out.println("    --gui                Starts the DoomTools GUI.");
	}
	
}

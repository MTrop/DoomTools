/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools;

import java.awt.Desktop;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.blackrook.json.JSONObject;
import com.blackrook.json.JSONReader;
import com.blackrook.rookscript.tools.ScriptExecutor;

import net.mtrop.doom.struct.io.IOUtils;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.exception.OptionParseException;
import net.mtrop.doom.tools.struct.HTTPUtils;
import net.mtrop.doom.tools.struct.HTTPUtils.HTTPReader;
import net.mtrop.doom.tools.struct.HTTPUtils.HTTPResponse;

/**
 * Main class for DoomTools.
 * @author Matthew Tropiano
 */
public final class DoomToolsMain 
{
	private static final String VERSION = Common.getVersionString("doomtools");

	private static final String JSON_VERSION = Common.getVersionString("json");
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

	private static final String ENVVAR_DOOMTOOLS_PATH = "DOOMTOOLS_PATH";

	private static final String DOOMTOOLS_WEBSITE = "https://mtrop.github.io/DoomTools/";

	private static final String UPDATE_GITHUB_API = "https://api.github.com/";
	private static final String UPDATE_REPO_NAME = "DoomTools";
	private static final String UPDATE_REPO_OWNER = "MTrop";

	private static final String SHELL_OPTIONS = "-Xms64M -Xmx784M";
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

	private static final HTTPReader<JSONObject> JSON_READER = (response) -> {
		String json = HTTPReader.STRING_CONTENT_READER.onHTTPResponse(response);
		return JSONReader.readJSON(json);
	};

	private static final int ERROR_NONE = 0;
	private static final int ERROR_DESKTOP_ERROR = 1;
	private static final int ERROR_SECURITY = 2;
	private static final int ERROR_NOWHERE = 3;
	private static final int ERROR_TIMEOUT = 4;
	private static final int ERROR_SITE_ERROR = 5;
	private static final int ERROR_IOERROR = 6;
	
	private static final String SWITCH_HELP = "--help";
	private static final String SWITCH_HELP2 = "-h";
	private static final String SWITCH_WEBSITE = "--website";
	private static final String SWITCH_WHERE = "--where";
	private static final String SWITCH_UPDATE = "--update";
	private static final String SWITCH_UPDATE_CLEANUP = "--update-cleanup";
	private static final String SWITCH_UPDATE_SHELL = "--update-shell";
	
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
		private boolean where;
		
		private Options()
		{
			this.stdout = null;
			this.help = false;
			this.update = false;
			this.updateCleanup = false;
			this.updateShell = false;
			this.openWebsite = false;
			this.where = false;
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
		
		private File[] getSortedJarList(File dir)
		{
			File[] jars = dir.listFiles(JAR_FILES);
			Arrays.sort(jars, (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName()));
			return jars;
		}
		
		private File getLatestJarFile(File dir)
		{
			File[] jars = getSortedJarList(dir);
			return jars.length == 0 ? null : jars[jars.length - 1];
		}
		
		private JSONObject getJSONResponse(String url) throws IOException
		{
			return HTTPUtils.httpGet(url, JSON_READER);
		}
		
		private String getProgressBar(long current, Long max)
		{
			final int MAXPIPS = 40;
			if (max != null)
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
				path = System.getenv(ENVVAR_DOOMTOOLS_PATH);
			} catch (SecurityException e) {
				options.stderr.println("ERROR: Could not fetch value of ENVVAR " + ENVVAR_DOOMTOOLS_PATH);
				return ERROR_SECURITY;
			}
			if (Common.isEmpty(path))
			{
				options.stderr.println("ERROR: DOOMTOOLS_PATH ENVVAR not set. Not invoked via shell?");
				return ERROR_NOWHERE;
			}
			
			final String shellSourceFile = Common.IS_WINDOWS ? "app-name.cmd" : "app-name.sh";
			final String shellExtension = Common.IS_WINDOWS ? ".cmd" : "";
			
			// Export shell scripts.
			for (Map.Entry<String, Class<?>> entry : SHELL_DATA.entrySet())
			{
				File outputFilePath = new File(path + "/" + entry.getKey() + shellExtension);
				try {
					Common.copyShellScript("shell/jar/" + shellSourceFile, entry.getValue(), SHELL_OPTIONS, "", outputFilePath);
					if (!Common.IS_WINDOWS)
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
				path = System.getenv(ENVVAR_DOOMTOOLS_PATH);
			} catch (SecurityException e) {
				options.stderr.println("ERROR: Could not fetch value of ENVVAR " + ENVVAR_DOOMTOOLS_PATH);
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
				path = System.getenv(ENVVAR_DOOMTOOLS_PATH);
			} catch (SecurityException e) {
				options.stderr.println("ERROR: Could not fetch value of ENVVAR " + ENVVAR_DOOMTOOLS_PATH);
				return ERROR_SECURITY;
			}
			if (Common.isEmpty(path))
			{
				options.stderr.println("ERROR: DOOMTOOLS_PATH ENVVAR not set. Not invoked via shell?");
				return ERROR_NOWHERE;
			}

			String currentVersion = getLatestJarFile(new File(path + "/jar")).getName().substring(10, 30); // grab date from current JAR.
			options.stdout.println("Current version is: " + currentVersion);
			options.stdout.println("Contacting update site...");

			JSONObject assetJSON = null;

			try {
				JSONObject json;
				json = getJSONResponse(UPDATE_GITHUB_API);
				
				String repoURL;
				if ((repoURL = json.get("repository_url").getString()) == null)
				{
					options.stderr.println("ERROR: Unexpected content from update site. Update failed.");
					return ERROR_SITE_ERROR;
				}
				
				// Get latest release assets list.
				json = getJSONResponse(repoURL.replace("{owner}", UPDATE_REPO_OWNER).replace("{repo}", UPDATE_REPO_NAME) + "/releases")
					.get(0).get("assets")
				;

				if (json == null || !json.isArray())
				{
					options.stderr.println("ERROR: Unexpected content from update site. Update failed.");
					return ERROR_SITE_ERROR;
				}
				
				// Get archive from latest release.
				for (int i = 0; i < json.length(); i++)
				{
					JSONObject element = json.get(i);
					if ("application/zip".equals(element.get("content_type").getString()))
					{
						assetJSON = element;
						break;
					}
				}
				
			} catch (SocketTimeoutException e) {
				options.stderr.println("ERROR: Timeout occurred contacting update site.");
				return ERROR_TIMEOUT;
			} catch (IOException e) {
				options.stderr.println("ERROR: Read error contacting update site: " + e.getLocalizedMessage());
				return ERROR_IOERROR;
			}
			
			if (assetJSON == null)
			{
				options.stderr.println("ERROR: Release found, but no suitable archive.");
				return ERROR_SITE_ERROR;
			}
			
			String releaseVersion = assetJSON.get("name").getString().substring(14, 34); // should grab date from asset name (CMD version). 
			
			if (currentVersion.compareTo(releaseVersion) >= 0)
			{
				options.stdout.println("DoomTools is up-to-date!");
				return ERROR_NONE;
			}

			options.stdout.println("New version found:  " + releaseVersion);
			
			try (HTTPResponse response = HTTPUtils.httpGet(assetJSON.get("browser_download_url").getString()))
			{
				File tempFile = Files.createTempFile("doomtools-tmp-", ".zip").toFile(); 
				try 
				{
					final AtomicLong currentBytes = new AtomicLong(0L);

					options.stdout.print("\r" + getProgressBar(currentBytes.get(), response.getLength()));
					
					final AtomicLong lastDate = new AtomicLong(System.currentTimeMillis());
					try (FileOutputStream fos = new FileOutputStream(tempFile))
					{
						response.relayContent(fos, (cur, max) -> 
						{
							long next = System.currentTimeMillis();
							currentBytes.set(cur);
							if (next > lastDate.get() + 250L)
							{
								options.stdout.print("\r" + getProgressBar(currentBytes.get(), max));
								lastDate.set(next);
							}
						});
					}
					
					options.stdout.println("\r" + getProgressBar(currentBytes.get(), response.getLength()));
					options.stdout.println("Extracting....");
					
					boolean extracted = false;
					try (ZipFile zf = new ZipFile(tempFile))
					{
						@SuppressWarnings("unchecked")
						Enumeration<ZipEntry> zipenum = (Enumeration<ZipEntry>)zf.entries();
						while (zipenum.hasMoreElements())
						{
							ZipEntry entry = zipenum.nextElement();
							if (entry.getName().endsWith(".jar"))
							{
								File targetJarFile = new File(path + "/" + entry.getName());
								try (InputStream zin = zf.getInputStream(entry); FileOutputStream fos = new FileOutputStream(targetJarFile))
								{
									IOUtils.relay(zin, fos);
								}
								extracted = true;
							}
						}
					}
					
					if (!extracted)
					{
						options.stderr.println("ERROR: No JAR entry in archive!");
						return ERROR_IOERROR;
					}
					else
					{
						options.stdout.println("Finished updating!");
					}
				} 
				finally 
				{
					tempFile.delete();
				}
			} 
			catch (SocketTimeoutException e) 
			{
				options.stderr.println("ERROR: Timeout occurred downloading update!");
				return ERROR_TIMEOUT;
			} 
			catch (IOException e) 
			{
				options.stderr.println("ERROR: Read error downloading update: " + e.getLocalizedMessage());
				return ERROR_IOERROR;
			}
			
			options.stdout.println("DoomTools is up-to-date!");
			return ERROR_NONE;
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
					path = System.getenv(ENVVAR_DOOMTOOLS_PATH);
				} catch (SecurityException e) {
					options.stderr.println("ERROR: Could not fetch value of ENVVAR " + ENVVAR_DOOMTOOLS_PATH);
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
				options.stdout.println("Using Black Rook JSON v" + JSON_VERSION);
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
		out.println("    --help               Prints help and exits.");
		out.println("    -h");
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
	}
	
}

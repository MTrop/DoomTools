/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;

import net.mtrop.doom.tools.doomfetch.DogSoftDriver;
import net.mtrop.doom.tools.doomfetch.DoomShackDriver;
import net.mtrop.doom.tools.doomfetch.FetchDriver;
import net.mtrop.doom.tools.doomfetch.FetchDriver.Response;
import net.mtrop.doom.tools.doomfetch.IdGamesDriver;
import net.mtrop.doom.tools.doomfetch.TSPGAustralDriver;
import net.mtrop.doom.tools.doomfetch.TSPGEuroborosDriver;
import net.mtrop.doom.tools.doomfetch.TSPGPainkillerDriver;
import net.mtrop.doom.tools.exception.OptionParseException;
import net.mtrop.doom.tools.struct.TokenScanner;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.HTTPUtils.HTTPResponse;
import net.mtrop.doom.tools.struct.util.IOUtils;
import net.mtrop.doom.tools.struct.util.OSUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;

/**
 * Main class for DoomFetch.
 * @author Matthew Tropiano
 */
public final class DoomFetchMain 
{
	private static final String SPLASH_VERSION = "DoomFetch v" + Version.DOOMFETCH + " by Matt Tropiano";

	private static final int ERROR_NONE = 0;
	private static final int ERROR_BAD_OPTIONS = 1;
	private static final int ERROR_IOERROR = 2;
	private static final int ERROR_NOTFOUND = 3;
	private static final int ERROR_UNKNOWN = 4;

	public static final String SWITCH_HELP = "--help";
	public static final String SWITCH_HELP2 = "-h";
	public static final String SWITCH_VERSION = "--version";
	public static final String SWITCH_CHANGELOG = "--changelog";

	public static final String SWITCH_LOCKFILE = "--lockfile";
	public static final String SWITCH_TARGET = "--target";
	public static final String SWITCH_UPDATE = "--update";
	public static final String SWITCH_NOLOCK = "--nolock";

	public static final String DEFAULT_LOCK_FILENAME = "doomfetch.lock";

	// LinkedHashMap to establish priority.
	public static final Map<String, BiFunction<PrintStream, PrintStream, FetchDriver>> DRIVER_LIST = new LinkedHashMap<String, BiFunction<PrintStream, PrintStream, FetchDriver>>() 
	{
		private static final long serialVersionUID = 3458742412979808872L;
		{
			put("idgames", (out, err) -> new IdGamesDriver(out, err));
			put("doomshack", (out, err) -> new DoomShackDriver(out, err));
			put("dogsoft", (out, err) -> new DogSoftDriver(out, err));
			put("tspgpk", (out, err) -> new TSPGPainkillerDriver(out, err));
			put("tspgeuro", (out, err) -> new TSPGEuroborosDriver(out, err));
			put("austral", (out, err) -> new TSPGAustralDriver(out, err));
		}
	};
	
	/**
	 * In-memory representation of the lock file.
	 */
	private static class LockFile
	{
		private static class EntryData
		{
			private String driver;
			private String etag; // not implemented
			private String date; // not implemented

			private EntryData(String driver, String etag, String date) 
			{
				super();
				this.driver = driver;
				this.etag = etag;
				this.date = date;
			}
		}
		
		/** Entry table. */
		private Map<String, EntryData> entries;

		public LockFile()
		{
			this.entries = new TreeMap<>();
		}
		
		/**
		 * Adds an entry to this lock.
		 * @param name the name of the entry.
		 */
		public void add(String name)
		{
			add(name, "", "", "");
		}

		/**
		 * Adds an entry to this lock.
		 * @param name the name of the entry.
		 * @param driver the driver that downloaded it.
		 */
		public void add(String name, String driver)
		{
			add(name, driver, "", "");
		}

		/**
		 * Adds an entry to this lock.
		 * @param name the name of the entry.
		 * @param driver the driver that downloaded it.
		 * @param etag the hash or cache tag for the file.
		 * @param date the cache date of the file fetched.
		 */
		public void add(String name, String driver, String etag, String date)
		{
			entries.put(name, new EntryData(driver, etag, date));
		}
		
		/**
		 * Attempts to load a lock file from a provided file path.
		 * This lock object contents are replaced.
		 * @param lockFile the file path.
		 * @throws IOException if an I/O or parsing error occurs.
		 */
		public void fromFile(File lockFile) throws IOException
		{
			entries.clear();
			addFile(lockFile);
		}
		
		/**
		 * Attempts to load a lock file from a provided file path.
		 * Contents are added to this one.
		 * @param lockFile the file path.
		 * @throws IOException if an I/O or parsing error occurs.
		 */
		public void addFile(File lockFile) throws IOException
		{
			try (BufferedReader br = IOUtils.openTextFile(lockFile, StandardCharsets.UTF_8))
			{
				String line;
				while ((line = br.readLine()) != null)
				{
					String name = "";
					String driver = "";
					String etag = "";
					String date = "";
					
					try (TokenScanner scanner = new TokenScanner(line))
					{
						if (!scanner.hasNext())
							continue;
						
						name = scanner.next();
						if (!scanner.hasNext())
						{
							add(name);
							continue;
						}
	
						driver = scanner.next();
						if (!scanner.hasNext())
						{
							add(name, driver);
							continue;
						}
	
						etag = scanner.next();
						if (!scanner.hasNext())
						{
							add(name, driver, etag, date);
							continue;
						}
	
						date = scanner.next();
						add(name, driver, etag, date);
					}
				}
			}
		}
		
		/**
		 * Attempts to save a lock file to a provided file path.
		 * @param lockFile the file path.
		 * @throws IOException if a write error occurs.
		 */
		public void toFile(File lockFile) throws IOException
		{
			try (PrintWriter pw = new PrintWriter(lockFile, "UTF-8"))
			{
				for (Map.Entry<String, EntryData> entry : entries.entrySet())
				{
					String name =   entry.getKey();
					String driver = entry.getValue().driver;
					String etag =   entry.getValue().etag;
					String date =   entry.getValue().date;
					
					if (ObjectUtils.isEmpty(etag) && ObjectUtils.isEmpty(date))
					{
						pw.println(name + " " + driver);
					}
					else if (ObjectUtils.isEmpty(date))
					{
						pw.println(name + " " + driver + " " + etag);
					}
					else if (ObjectUtils.isEmpty(etag))
					{
						pw.println(name + " " + driver + " \"\" " + date);
					}
					else
					{
						pw.println(name + " " + driver + " " + etag + " " + date);
					}
				}
			}
		}
		
		/**
		 * Gets the entry set of this lockfile.
		 * @return the entry set.
		 */
		public Set<Map.Entry<String, EntryData>> entries()
		{
			return entries.entrySet();
		}
		
		/**
		 * @return true if no entries, false if has entries.
		 */
		public boolean isEmpty()
		{
			return entries.isEmpty();
		}
		
	}
	
	/**
	 * Program options.
	 */
	public static class Options
	{
		private PrintStream stdout;
		private PrintStream stderr;
		
		private boolean help;
		private boolean version;
		private boolean changelog;
		
		private File lockFile;
		private File targetDirectory;
		private boolean update;
		private boolean nolock;
		
		private String driver;
		private String name;
		
		public Options()
		{
			this.stdout = null;
			this.stderr = null;
			
			this.help = false;
			this.version = false;
			this.changelog = false;
			
			this.lockFile = new File(DEFAULT_LOCK_FILENAME);
			this.targetDirectory = new File(".");
			this.update = false;
			this.nolock = false;
			
			this.driver = null;
			this.name = null;
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

		public Options setLockFile(File lockFile) 
		{
			this.lockFile = lockFile;
			return this;
		}
		
		public Options setTargetDirectory(File targetDirectory) 
		{
			this.targetDirectory = targetDirectory;
			return this;
		}
		
		public Options setUpdate(boolean update) 
		{
			this.update = update;
			return this;
		}
		
		public Options setNoLock(boolean nolock) 
		{
			this.nolock = nolock;
			return this;
		}
		
		public Options setDriver(String driver) 
		{
			this.driver = driver;
			return this;
		}
		
		public Options setName(String name) 
		{
			this.name = name;
			return this;
		}
		
		public String getName() 
		{
			return name;
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
		
		// Returns true if the file was fetched successfully.
		private boolean fetchFile(LockFile lockFile, String name)
		{
			boolean success = false;
			for (Map.Entry<String, ?> entry : DRIVER_LIST.entrySet())
			{
				success = fetchFile(lockFile, entry.getKey(), name); 
				if (success)
				{
					lockFile.add(name, entry.getKey());
					break;
				}
			}
			return success;
		}

		// Returns true if the file was fetched (or found locally) successfully.
		private boolean fetchFile(LockFile lockFile, String driver, String name)
		{
			if (!options.update && searchForTargetFile(options.targetDirectory, name))
			{
				options.stdout.println("[Skipping] File found in target directory: " + name);
				return true;
			}
			
			BiFunction<PrintStream, PrintStream, FetchDriver> driverFunc = DRIVER_LIST.get(driver);
			if (driver == null)
				options.stderr.println("ERROR: No such driver: " + driver);
		
			FetchDriver fetcher = driverFunc.apply(options.stdout, options.stderr);
			
			Response response = null;
			try
			{
				response = fetcher.getStreamFor(name);
				if (response == null)
					return false;
				
				File targetFile = new File(options.targetDirectory.getPath() + File.separator + response.getFilename());
				if (!FileUtils.createPathForFile(targetFile))
				{
					options.stderr.println("ERROR: Could not create target directory for file.");
					return false;
				}
				
				try (HTTPResponse httpResponse = response.getHTTPResponse(); FileOutputStream fos = new FileOutputStream(targetFile))
				{
					if (!httpResponse.isSuccess())
					{
						options.stderr.println("ERROR: Received " + httpResponse.getStatusCode() + " (" + httpResponse.getStatusMessage() + ") from source.");
						return false;
					}
					
					options.stdout.println("Downloading " + response.getFilename() + "...");
					final AtomicLong currentBytes = new AtomicLong(0L);
					final AtomicLong lastDate = new AtomicLong(System.currentTimeMillis());
					httpResponse.relayContent(fos, (cur, max) -> 
					{
						long next = System.currentTimeMillis();
						currentBytes.set(cur);
						if (next > lastDate.get() + 250L)
						{
							printProgress(cur, max, options.stdout);
							lastDate.set(next);
						}
					});
					printProgress(currentBytes.get(), currentBytes.get(), options.stdout);
					options.stdout.println("\nDone.");
				}
			}
			catch (IOException e)
			{
				options.stderr.println("ERROR: Can't read from source: " + driver);
				return false;
			}
			finally
			{
				IOUtils.close(response);
			}
			
			lockFile.add(name, driver);
			return true;
		}

		// Returns true if a file that has a target name is found
		private static boolean searchForTargetFile(File targetDirectoryPath, String name)
		{
			if (!targetDirectoryPath.exists())
				return false;
			
			for (File f : targetDirectoryPath.listFiles())
			{
				String fname = FileUtils.getFileNameWithoutExtension(f);
				if (OSUtils.isWindows() && fname.equalsIgnoreCase(name))
					return true;
				else if (fname.equals(name))
					return true;
			}
			
			return false;
		}
		
		// Prints transfer progress.
		private static void printProgress(long current, Long max, PrintStream out)
		{
			if (max != null)
				out.print("\r" + (current / 1024) + " KB / " + (max / 1024) + " KB ");
			else
				out.print("\r" + (current / 1024) + " KB ");
		}
		
		@Override
		public Integer call()
		{
			if (options.help)
			{
				splash(options.stdout);
				usage(options.stdout);
				options.stdout.println();
				help(options.stdout);
				return ERROR_NONE;
			}
			
			if (options.version)
			{
				splash(options.stdout);
				return ERROR_NONE;
			}

			if (options.changelog)
			{
				changelog(options.stdout, "doomfetch");
				return ERROR_NONE;
			}
			
			if (options.lockFile == null)
				options.lockFile = new File(DEFAULT_LOCK_FILENAME);
			
			LockFile lock = new LockFile();
			
			if (options.lockFile.exists())
			{
				try {
					lock.fromFile(options.lockFile);
				} catch (IOException e) {
					options.stderr.println("ERROR: Could not read lockfile: " + options.lockFile.getPath());
					return ERROR_IOERROR;
				}
			}
			
			if (lock.isEmpty() && ObjectUtils.isEmpty(options.name))
			{
				options.stdout.println("No work to do.");
				return ERROR_NONE;
			}
			
			boolean success = true;
			boolean atleastone = false;
			
			// Provided name.
			if (!ObjectUtils.isEmpty(options.name))
			{
				// Provided driver.
				if (!ObjectUtils.isEmpty(options.driver))
				{
					success = fetchFile(lock, options.driver, options.name);
					if (success)
						atleastone = true;
				}
				else
				{
					success = fetchFile(lock, options.name); 
					if (success)
						atleastone = true;
				}
			}
			// No name. Pull from Lock file.
			else for (Map.Entry<String, LockFile.EntryData> entry : lock.entries())
			{
				boolean out;
				String driver = entry.getValue().driver;
				if (ObjectUtils.isEmpty(driver))
					out = fetchFile(lock, entry.getKey()) && success;
				else
					out = fetchFile(lock, driver, entry.getKey()) && success;
				success = out;
				if (success)
					atleastone = true;
			}
			
			if (!success)
			{
				if (atleastone)
					options.stdout.println("Some files not fetched.");
				else
					options.stdout.println("No files found/fetched.");
				return ERROR_NOTFOUND;
			}
			
			try {
				if (!options.nolock)
					lock.toFile(options.lockFile);
			} catch (IOException e) {
				options.stderr.println("ERROR: Could not write to lock file.");
				return ERROR_IOERROR;
			}
			
			options.stdout.println("All files fetched.");
			return ERROR_NONE;
		}
	}
	
	/**
	 * Reads command line arguments and sets options.
	 * @param out the standard output print stream.
	 * @param err the standard error print stream. 
	 * @param in the standard in stream. 
	 * @param args the argument args.
	 * @return the parsed options.
	 * @throws OptionParseException if a parse exception occurs.
	 */
	public static Options options(PrintStream out, PrintStream err, InputStream in, String ... args) throws OptionParseException
	{
		Options options = new Options();
		options.stdout = out;
		options.stderr = err;

		final int STATE_START = 0;
		final int STATE_LOCKFILE = 1;
		final int STATE_TARGET = 2;
		int state = STATE_START;

		for (int i = 0; i < args.length; i++)
		{
			String arg = args[i];
			
			switch (state)
			{
				case STATE_START:
				{
					if (arg.equals(SWITCH_HELP) || arg.equals(SWITCH_HELP2))
						options.help = true;
					else if (arg.equals(SWITCH_VERSION))
						options.version = true;
					else if (arg.equalsIgnoreCase(SWITCH_CHANGELOG))
						options.changelog = true;
					else if (arg.equals(SWITCH_UPDATE))
						options.setUpdate(true);
					else if (arg.equals(SWITCH_NOLOCK))
						options.setNoLock(true);
					else if (arg.equals(SWITCH_LOCKFILE))
						state = STATE_LOCKFILE;
					else if (arg.equals(SWITCH_TARGET))
						state = STATE_TARGET;
					else if (options.name != null)
					{
						options.driver = options.name;
						options.name = arg;
					}
					else
					{
						options.name = arg;
					}
				}
				break;
				
				case STATE_LOCKFILE:
				{
					options.lockFile = new File(arg);
					state = STATE_START;
				}
				break;	
				
				case STATE_TARGET:
				{
					options.targetDirectory = new File(arg);
					state = STATE_START;
				}
				break;
			}
		}

		if (state == STATE_LOCKFILE)
			throw new OptionParseException("ERROR: Expected path to lock file.");
		if (state == STATE_TARGET)
			throw new OptionParseException("ERROR: Expected path to target directory.");

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
	
	public static void main(String[] args)
	{
		try {
			System.exit(call(options(System.out, System.err, System.in, args)));
		} catch (OptionParseException e) {
			System.err.println(e.getMessage());
			System.exit(ERROR_BAD_OPTIONS);
		}
	}

	/**
	 * Prints the splash.
	 * @param out the print stream to print to.
	 */
	private static void splash(PrintStream out)
	{
		out.println(SPLASH_VERSION);
	}
	
	/**
	 * Prints the usage.
	 * @param out the print stream to print to.
	 */
	private static void usage(PrintStream out)
	{
		out.println("Usage: doomfetch [--help | -h | --version]");
		out.println("                 [driver] [filename] [options]");
		out.println("                 [filename] [options]");
		out.println("                 [options]");
	}
	
	/**
	 * Prints the help.
	 * @param out the print stream to print to.
	 */
	private static void help(PrintStream out)
	{
		out.println("    --help              Prints help and exits.");
		out.println("    -h");
		out.println();
		out.println("    --version           Prints version, and exits.");
		out.println();
		out.println("[driver]:");
		out.println("    (OPTIONAL) The specific driver to use for searching for a file.");
		out.println();
		out.println("[filename]:");
		out.println("    (OPTIONAL) If specified, the name of the file to fetch (no extension).");
		out.println("    If NOT specified, attempt to fetch the files in the lock file.");
		out.println("    Successfully fetched files are placed in the lock file.");
		out.println();
		out.println("[options]:");
		out.println();
		out.println("    --lockfile [path]   Changes the lockfile to use. [path] is the path");
		out.println("                        to the lock file.");
		out.println("                            Default: " + DEFAULT_LOCK_FILENAME);
		out.println();
		out.println("    --target [path]     The target path for the downloaded files. If a");
		out.println("                        file is determined to exist in the target path, of the");
		out.println("                        same name, it is skipped unless --update is specified.");
		out.println("                            Default: .");
		out.println();
		out.println("    --update            Tells DoomFetch to update even if a file is present.");
		out.println();
		out.println("    --nolock            Tells DoomFetch to not update the lock file.");
		out.println();
		out.println("Available drivers:");
		for (Map.Entry<String, ?> entry : DRIVER_LIST.entrySet())
			out.println("    " + entry.getKey());
	}

	/**
	 * Prints the changelog.
	 * @param out the print stream to print to.
	 */
	private static void changelog(PrintStream out, String name)
	{
		String line;
		int i = 0;
		try (BufferedReader br = IOUtils.openTextStream(IOUtils.openResource("docs/changelogs/CHANGELOG-" + name + ".md")))
		{
			while ((line = br.readLine()) != null)
			{
				if (i >= 3) // eat the first three lines
					out.println(line);
				i++;
			}
		} 
		catch (IOException e) 
		{
			out.println("****** ERROR: Cannot read CHANGELOG ******");
		}
	}
	
}

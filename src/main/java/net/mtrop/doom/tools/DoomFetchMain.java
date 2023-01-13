package net.mtrop.doom.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;

import com.formdev.flatlaf.util.StringUtils;

import net.mtrop.doom.tools.doomfetch.FetchDriver;
import net.mtrop.doom.tools.doomfetch.IdGamesDriver;
import net.mtrop.doom.tools.exception.OptionParseException;
import net.mtrop.doom.tools.struct.TokenScanner;
import net.mtrop.doom.tools.struct.util.IOUtils;

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

	public static final String SWITCH_LOCKFILE = "--lockfile";
	public static final String SWITCH_TARGET = "--target";
	public static final String SWITCH_UPDATE = "--update";

	public static final String DEFAULT_LOCK_FILENAME = "doomfetch.lock";
	
	public static final Map<String, BiFunction<PrintStream, PrintStream, FetchDriver>> DRIVER_LIST = new TreeMap<String, BiFunction<PrintStream, PrintStream, FetchDriver>>() 
	{
		private static final long serialVersionUID = 3458742412979808872L;
		{
			put("idgames", (out, err) -> new IdGamesDriver(out, err));
		}
	};
	
	/**
	 * In-memory representation of the lock file.
	 */
	private static class LockFile
	{
		private static class EntryData
		{
			private String etag; // not implemented
			private String date; // not implemented

			private EntryData(String etag, String date) 
			{
				super();
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
			add(name, "", "");
		}

		/**
		 * Adds an entry to this lock.
		 * @param name the name of the entry.
		 * @param etag the hash or cache tag for the file.
		 * @param date the cache date of the file fetched.
		 */
		public void add(String name, String etag, String date)
		{
			entries.put(name, new EntryData(etag, date));
		}
		
		/**
		 * Fetches an entry.
		 * @param name the name of the entry.
		 * @return the entry if exists, null if not.
		 */
		public EntryData get(String name)
		{
			return entries.get(name);
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
					String etag = "";
					String date = "";
					
					try (TokenScanner scanner = new TokenScanner(line))
					{
						if (!scanner.hasNext())
							continue;
						
						name = scanner.next();
						
						if (!scanner.hasNext())
							add(name);
	
						etag = scanner.next();
						if (!scanner.hasNext())
							add(name, etag, date);
	
						date = scanner.next();
						add(name, etag, date);
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
					String name = entry.getKey();
					String etag = entry.getValue().etag;
					String date = entry.getValue().date;
					
					if (StringUtils.isEmpty(etag) && StringUtils.isEmpty(date))
					{
						pw.println(name);
					}
					else if (StringUtils.isEmpty(date))
					{
						pw.println(name + " " + etag);
					}
					else if (StringUtils.isEmpty(etag))
					{
						pw.println(name + " \"\" " + date);
					}
					else
					{
						pw.println(name + " " + etag + " " + date);
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
		
		private File lockFile;
		private File targetDirectory;
		private boolean update;
		
		private String driver;
		private String name;
		
		public Options()
		{
			this.stdout = null;
			this.stderr = null;
			
			this.help = false;
			this.version = false;
			
			this.lockFile = new File(DEFAULT_LOCK_FILENAME);
			this.targetDirectory = new File(".");
			this.update = false;
			
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

		public Options setHelp(boolean help) 
		{
			this.help = help;
			return this;
		}
		
		public Options setVersion(boolean version) 
		{
			this.version = version;
			return this;
		}
		
		public void setLockFile(File lockFile) 
		{
			this.lockFile = lockFile;
		}
		
		public void setTargetDirectory(File targetDirectory) 
		{
			this.targetDirectory = targetDirectory;
		}
		
		public void setUpdate(boolean update) 
		{
			this.update = update;
		}
		
		public void setDriver(String driver) 
		{
			this.driver = driver;
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
		
		private boolean fetchFile(LockFile lockFile, String name)
		{
			boolean success = false;
			for (Map.Entry<String, ?> entry : DRIVER_LIST.entrySet())
			{
				success = fetchFile(lockFile, entry.getKey(), options.name); 
				if (success)
					break;
			}
			return success;
		}
		
		private boolean fetchFile(LockFile lockFile, String driver, String name)
		{
			// TODO: Finish this.
			return false;
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
			
			if (lock.isEmpty() && StringUtils.isEmpty(options.name))
			{
				options.stderr.println("No work to do.");
				splash(options.stdout);
				usage(options.stdout);
				options.stdout.println();
				help(options.stdout);
				return ERROR_NONE;
			}
			
			boolean success = true;
			
			// Provided name.
			if (!StringUtils.isEmpty(options.name))
			{
				// Provided driver.
				if (!StringUtils.isEmpty(options.driver))
				{
					success = fetchFile(lock, options.driver, options.name);
				}
				else
				{
					success = fetchFile(lock, options.name); 
				}
			}
			// No name. Pull from Lock file.
			else for (Map.Entry<String, ?> entry : lock.entries())
			{
				boolean out = fetchFile(lock, options.name) && success; 
			}
			
			// TODO: Finish.

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

		int i = 0;
		while (i < args.length)
		{
			String arg = args[i];
			if (arg.equals(SWITCH_HELP) || arg.equals(SWITCH_HELP2))
				options.setHelp(true);
			else if (arg.equals(SWITCH_VERSION))
				options.setVersion(true);
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
	
	public static void main(String[] args)
	{
		if (args.length == 0)
		{
			splash(System.out);
			usage(System.out);
			System.exit(-1);
			return;
		}
		
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
		out.println("Available drivers:");
		for (Map.Entry<String, ?> entry : DRIVER_LIST.entrySet())
			out.println("    " + entry.getKey());
	}

}

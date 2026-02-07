/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import net.mtrop.doom.sound.DMXSound;
import net.mtrop.doom.tools.exception.OptionParseException;
import net.mtrop.doom.tools.gui.DoomToolsGUIMain;
import net.mtrop.doom.tools.gui.DoomToolsGUIMain.ApplicationNames;
import net.mtrop.doom.tools.struct.ProcessCallable;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.IOUtils;

/**
 * Main class for Utility.
 * @author Matthew Tropiano
 */
public final class DMXConvertMain 
{
	private static final String SPLASH_VERSION = "DMXConv v" + Version.DMXCONV + " by Matt Tropiano (using DoomStruct v" + Version.DOOMSTRUCT + ")";

	private static final int ERROR_NONE = 0;
	private static final int ERROR_BAD_OPTIONS = 1;
	private static final int ERROR_NO_FILES = 2;
	private static final int ERROR_CONVERSION_SKIPPED = 3;
	private static final int ERROR_NO_FFMPEG = 4;
	private static final int ERROR_IOERROR = 5;
	private static final int ERROR_UNKNOWN = -1;

	public static final String SWITCH_CHANGELOG = "--changelog";
	public static final String SWITCH_GUI = "--gui";
	public static final String SWITCH_HELP = "--help";
	public static final String SWITCH_HELP2 = "-h";
	public static final String SWITCH_VERSION = "--version";
	public static final String SWITCH_TRYFFMPEG = "--try-ffmpeg";

	public static final String SWITCH_FFMPEG_ONLY = "--ffmpeg-only";
	public static final String SWITCH_JSPI_ONLY = "--jspi-only";
	public static final String SWITCH_FFMPEG_PATH = "--ffmpeg";
	public static final String SWITCH_OUTPUTDIR = "--output-dir";
	public static final String SWITCH_OUTPUTDIR2 = "-o";
	public static final String SWITCH_RECURSIVE = "--recursive";
	public static final String SWITCH_RECURSIVE2 = "-r";

	/**
	 * Program options.
	 */
	public static class Options
	{
		private PrintStream stdout;
		private PrintStream stderr;
		private boolean help;
		private boolean version;
		private boolean tryFFMpeg;
		private boolean changelog;
		private boolean gui;
		
		private List<File> sourceFiles;
		private boolean onlyFFMpeg;
		private boolean onlyJSPI;
		private File ffmpegPath;
		private File outputDirectory;
		private boolean recursive;
		
		private Options()
		{
			this.stdout = null;
			this.stderr = null;
			this.help = false;
			this.version = false;
			this.gui = false;
			this.changelog = false;
			
			this.sourceFiles = new LinkedList<>();
			this.onlyFFMpeg = false;
			this.onlyJSPI = false;
			this.ffmpegPath = null;
			this.outputDirectory = null;
			this.recursive = false;
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

		public Options setOnlyFFMpeg(boolean onlyFFMpeg) 
		{
			this.onlyFFMpeg = onlyFFMpeg;
			return this;
		}
		
		public Options setOnlyJSPI(boolean onlyJSPI) 
		{
			this.onlyJSPI = onlyJSPI;
			return this;
		}
		
		public Options setFFMpegPath(File ffmpegPath) 
		{
			this.ffmpegPath = ffmpegPath;
			return this;
		}
		
		public Options setOutputDirectory(File outputDirectory) 
		{
			this.outputDirectory = outputDirectory;
			return this;
		}
		
		public Options setRecursive(boolean recursive) 
		{
			this.recursive = recursive;
			return this;
		}
		
		public Options addInputFile(File file)
		{
			this.sourceFiles.add(file);
			return this;
		}
		
	}
	
	/**
	 * Program context.
	 */
	private static class Context implements Callable<Integer>
	{
		private static final File NULL_FILE = new File(System.getProperty("os.name").startsWith("Windows") ? "NUL" : "/dev/null");
		
		private Options options;
	
		private Context(Options options)
		{
			this.options = options;
		}
		
		@Override
		public Integer call()
		{
			if (options.gui)
			{
				try {
					DoomToolsGUIMain.startGUIAppProcess(ApplicationNames.DMXCONVERT);
				} catch (IOException e) {
					options.stderr.println("ERROR: Could not start DMXConv GUI!");
					return ERROR_IOERROR;
				}
				return ERROR_NONE;
			}

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
				changelog(options.stdout, "dmxconv");
				return ERROR_NONE;
			}
			
			if (options.tryFFMpeg)
			{
				if (detectFFmpeg(null))
				{
					options.stdout.println("SUCCESS. FFmpeg found!");
					return ERROR_NONE;
				}
				else
				{
					options.stdout.println("FFmpeg not found on the PATH.");
					options.stdout.println("Either it is not named \"ffmpeg\", or the executable was not found on the");
					options.stdout.println("current PATH.");
					return ERROR_NO_FFMPEG;
				}
			}
			
			if (options.sourceFiles.isEmpty())
			{
				options.stderr.println("ERROR: No files specified for conversion.");
				return ERROR_NO_FILES;
			}
			
			boolean useFFmpeg = true;
			if (!options.onlyJSPI)
			{
				useFFmpeg = detectFFmpeg(options.ffmpegPath);
				if (!useFFmpeg)
					options.stdout.println("NOTE: FFmpeg not found - will not use for conversion.");
			}
			
			boolean searchSPI = !options.onlyFFMpeg;
			boolean searchFFmpeg = !options.onlyJSPI && useFFmpeg;
			AtomicInteger filesFound = new AtomicInteger(0);
			int convertedCount = 0;
			
			for (File f : options.sourceFiles)
			{
				if (f.isDirectory())
					convertedCount += convertDirectory(f, f, searchSPI, searchFFmpeg, filesFound, options.recursive);
				else 
				{
					filesFound.incrementAndGet();
					if (convertFile(f.getParentFile(), f, searchSPI, searchFFmpeg))
						convertedCount++;
				}
			}

			options.stdout.printf("%d of %d file(s) converted.\n", convertedCount, filesFound.get());
			return convertedCount == filesFound.get() ? ERROR_NONE : ERROR_CONVERSION_SKIPPED;
		}
		
		// Converts a directory.
		private int convertDirectory(File base, File dir, boolean searchSPI, boolean searchFFmpeg, AtomicInteger filesFound, boolean recurse)
		{
			int convertedCount = 0;
			
			File[] dirFiles = dir.listFiles();
			if (dirFiles == null)
			{
				options.stderr.println("ERROR: Could not read directory: " + dir.getPath() +". Skipping...");
				return 0;
			}
			
			for (File f : dirFiles)
			{
				if (f.isDirectory())
				{ 
					if (recurse)
						convertedCount += convertDirectory(base, f, searchSPI, searchFFmpeg, filesFound, true);
					//else, skip
				}
				else
				{
					filesFound.incrementAndGet();
					if (convertFile(base, f, searchSPI, searchFFmpeg))
					{
						convertedCount++;
					}
				}
			}
			return convertedCount;
		}

		// Converts a single file.
		private boolean convertFile(File base, File f, boolean searchSPI, boolean searchFFmpeg)
		{
			AudioInputStream ais = null;
			if (searchSPI)
			{
				try {
					ais = openSPIAudioStreamForFile(f);
				} catch (IOException e) {
					options.stderr.printf("ERROR: Could not read %s.\n", f.getPath());
				}
			}

			if (ais == null && searchFFmpeg)
			{
				try {
					ais = openFFmpegAudioStreamForFile(options.ffmpegPath, f);
				} catch (IOException e) {
					options.stderr.printf("I/O ERROR: FFmpeg: %s\n", e.getLocalizedMessage());
					options.stderr.printf("ERROR: Could not read %s.\n", f.getPath());
					IOUtils.close(ais);
				}
			}
			
			if (ais == null)
			{
				options.stderr.printf("ERROR: Could not find decoder for %s. Skipping...\n", f.getPath());
				return false;
			}
			
			File outputFile;
			
			if (options.outputDirectory != null)
			{
				String treeName = f.getPath().substring(base.getPath().length());
				String outName = FileUtils.getFileNameWithoutExtension(treeName) + ".dmx";
				outputFile = new File(options.outputDirectory + outName);
			}
			else
			{
				String outName = FileUtils.getFileNameWithoutExtension(f) + ".dmx";
				String parent = f.getParent();
				if (parent != null)
					outputFile = new File(parent + File.separator + outName);
				else
					outputFile = new File("." + File.separator + outName);
			}
			
			if (!FileUtils.createPathForFile(outputFile))
			{
				options.stderr.printf("ERROR: Could not create path for %s. Skipping...\n", outputFile);
				return false;
			}
				
			try (AudioInputStream decoded = getDecoderStream(ais))
			{
				AudioFormat format = decoded.getFormat();
				byte[] sample = new byte[1];
				
				DMXSound dmx = new DMXSound((int)format.getSampleRate());
				
				while (decoded.read(sample) > 0)
					dmx.addSample((double)((sample[0] & 0x0ff) - 128) / 128.0);
				
				try (FileOutputStream fos = new FileOutputStream(outputFile))
				{
					dmx.writeBytes(fos);
					options.stdout.printf("Wrote %s.\n", outputFile.getPath());
					return true;
				} 
				catch (IOException e) 
				{
					options.stderr.printf("ERROR: Could not write %s.\n", outputFile.getPath());
				}
				catch (SecurityException e) 
				{
					options.stderr.printf("ERROR: Could not write %s (ACCESS DENIED).\n", outputFile.getPath());
				}
			} 
			catch (IOException e) 
			{
				options.stderr.printf("ERROR: Could not open decoder for %s.\n", f.getPath());
			}

			return false;
		}
		
		// Wraps an audio stream into a decoder. 
		private AudioInputStream getDecoderStream(AudioInputStream inputStream) throws IOException
		{
			AudioFormat format = inputStream.getFormat();
			return AudioSystem.getAudioInputStream(new AudioFormat(
				AudioFormat.Encoding.PCM_UNSIGNED,
				format.getSampleRate(), 
				8, // bits per sample
				1, // one channel
				1, // one byte per frame
				format.getSampleRate(),
				false // LE
			), inputStream);
		}

		// Opens an audio stream 
		private AudioInputStream openSPIAudioStreamForFile(File input) throws IOException
		{
			try {
				return AudioSystem.getAudioInputStream(input);
			} catch (UnsupportedAudioFileException e) {
				return null;
			}
		}
		
		// Opens an audio stream via FFmpeg
		// Throws SecurityException or UnsupportedAudioFileException
		private AudioInputStream openFFmpegAudioStreamForFile(File ffmpegPath, File input) throws IOException
		{
			String exe = ffmpegPath != null ? ffmpegPath.getAbsolutePath() : "ffmpeg";

			options.stdout.println("Calling FFmpeg...");
			
			Process proc = (new ProcessBuilder())
				.command(
					exe, "-i", input.getPath(), "-f", "wav", "-acodec", "pcm_s16le", "-ac", "2", "-"
				)
				.redirectError(Redirect.appendTo(NULL_FILE))
				.redirectOutput(Redirect.PIPE)
			.start();
			try {
				return AudioSystem.getAudioInputStream(new BufferedInputStream(proc.getInputStream()));
			} catch (UnsupportedAudioFileException e) {
				options.stderr.printf("UNSUPPORTED: Java via FFmpeg: %s\n", e.getLocalizedMessage());
				return null;
			}
		}
		
		private boolean detectFFmpeg(File ffmpegPath)
		{
			try {
				ProcessCallable.create(ffmpegPath != null ? ffmpegPath.getAbsolutePath() : "ffmpeg").call();
				return true;
			} catch (Exception e) {
				return false;
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
		final int STATE_FFMPEG = 1;
		final int STATE_OUTPUTDIR = 2;
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
					else if (arg.equals(SWITCH_TRYFFMPEG))
						options.tryFFMpeg = true;
					else if (arg.equalsIgnoreCase(SWITCH_GUI))
						options.gui = true;
					else if (arg.equalsIgnoreCase(SWITCH_CHANGELOG))
						options.changelog = true;
					else if (arg.equals(SWITCH_FFMPEG_ONLY))
						options.setOnlyFFMpeg(true);
					else if (arg.equals(SWITCH_JSPI_ONLY))
						options.setOnlyJSPI(true);
					else if (arg.equals(SWITCH_RECURSIVE) || arg.equals(SWITCH_RECURSIVE2))
						options.setRecursive(true);
					else if (arg.equals(SWITCH_FFMPEG_PATH))
						state = STATE_FFMPEG;
					else if (arg.equals(SWITCH_OUTPUTDIR) || arg.equals(SWITCH_OUTPUTDIR2))
						state = STATE_OUTPUTDIR;
					else
						options.sourceFiles.add(new File(arg));
				}
				break;
				
				case STATE_FFMPEG:
				{
					options.ffmpegPath = new File(arg);
					state = STATE_START;
				}
				break;

				case STATE_OUTPUTDIR:
				{
					options.outputDirectory = new File(arg);
					state = STATE_START;
				}
				break;
			}
		}

		if (state == STATE_FFMPEG)
			throw new OptionParseException("ERROR: Expected path to FFMpeg.");
		if (state == STATE_OUTPUTDIR)
			throw new OptionParseException("ERROR: Expected path to output directory.");

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
		if (args.length == 0)
		{
			splash(System.out);
			usage(System.out);
			System.exit(-1);
			return;
		}
	
		try {
			System.exit(call(options(System.out, System.err, args)));
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
		out.println("Usage: dmxconv [--help | -h | --version | --try-ffmpeg]");
		out.println("               [files] [switches]");
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
		out.println("    --changelog         Prints the changelog, and exits.");
		out.println();
		out.println("    --gui               Starts the GUI version of this program.");
		out.println();
		out.println("    --try-ffmpeg        Quick test to attempt to find FFmpeg via its default");
		out.println("                        name (\"ffmpeg\") on the current PATH, and exits.");
		out.println();
		out.println("[files]:");
		out.println("    <filenames>         The input sound files/directories (wildcard expansion");
		out.println("                        will work!).");
		out.println();
		out.println("[switches]:");
		out.println("    --output-dir [dir]  Sets the output directory path. If not set, each");
		out.println("    -o [dir]            output file is placed in the source file's directory.");
		out.println("                        If the output directory is not found, it is created.");
		out.println();
		out.println("    --ffmpeg [file]     Sets the path to FFmpeg. If not provided, it is");
		out.println("                        searched for on the PATH.");
		out.println();
		out.println("    --ffmpeg-only       If set, DMXConv does not attempt to read the incoming");
		out.println("                        sound files using Java SPI and the classpath, only");
		out.println("                        FFmpeg.");
		out.println();
		out.println("    --jspi-only         If set, DMXConv does not attempt to read the incoming");
		out.println("                        sound files using FFmpeg, only Java SPI and the");
		out.println("                        classpath.");
		out.println();
		out.println("    --recursive         If directories are found, then DMXConv will recurse");
		out.println("    -r                  through them looking for files.");
		out.println();
	}

}

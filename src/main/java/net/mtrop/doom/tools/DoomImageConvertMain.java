/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

import net.mtrop.doom.Wad;
import net.mtrop.doom.WadFile;
import net.mtrop.doom.graphics.Colormap;
import net.mtrop.doom.graphics.Flat;
import net.mtrop.doom.graphics.PNGPicture;
import net.mtrop.doom.graphics.Palette;
import net.mtrop.doom.graphics.Picture;
import net.mtrop.doom.object.BinaryObject;
import net.mtrop.doom.tools.exception.OptionParseException;
import net.mtrop.doom.tools.exception.UtilityException;
import net.mtrop.doom.tools.gui.DoomToolsGUIMain;
import net.mtrop.doom.tools.gui.DoomToolsGUIMain.ApplicationNames;
import net.mtrop.doom.tools.struct.TokenScanner;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.IOUtils;
import net.mtrop.doom.util.NameUtils;

/**
 * Main class for Utility.
 * @author Matthew Tropiano
 */
public final class DoomImageConvertMain 
{
	private static final String SPLASH_VERSION = "DImgConv v" + Version.DIMGCONV + " by Matt Tropiano (using DoomStruct v" + Version.DOOMSTRUCT + ")";

	private static final int ERROR_NONE = 0;
	private static final int ERROR_BAD_OPTIONS = 1;
	private static final int ERROR_NO_SOURCEFILE = 2;
	private static final int ERROR_NO_DESTINATION = 3;
	private static final int ERROR_NO_PALETTE = 4;
	private static final int ERROR_BAD_OUTPUT = 5;
	private static final int ERROR_IOERROR = 6;
	private static final int ERROR_PARSE = 7;
	private static final int ERROR_UNKNOWN = -1;

	public static final String SWITCH_CHANGELOG = "--changelog";
	public static final String SWITCH_GUI = "--gui";
	public static final String SWITCH_HELP = "--help";
	public static final String SWITCH_HELP2 = "-h";
	public static final String SWITCH_VERSION = "--version";
	public static final String SWITCH_VERBOSE = "--verbose";
	public static final String SWITCH_VERBOSE2 = "-v";

	public static final String SWITCH_RECURSIVE = "--recursive";
	public static final String SWITCH_RECURSIVE2 = "-r";

	public static final String SWITCH_MODE_PALETTES = "--mode-palettes";
	public static final String SWITCH_MODE_PALETTES2 = "-mp";

	public static final String SWITCH_MODE_COLORMAPS = "--mode-colormaps";
	public static final String SWITCH_MODE_COLORMAPS2 = "-mc";

	public static final String SWITCH_MODE_FLATS = "--mode-flats";
	public static final String SWITCH_MODE_FLATS2 = "-mf";

	public static final String SWITCH_OUTPUT = "--output";
	public static final String SWITCH_OUTPUT2 = "-o";

	public static final String SWITCH_METAINFOFILE = "--infofile";
	public static final String SWITCH_METAINFOFILE2 = "-i";

	public static final String SWITCH_PALETTE = "--palette";
	public static final String SWITCH_PALETTE2 = "-p";

	public enum Mode
	{
		PALETTE,
		COLORMAP,
		GRAPHIC,
		FLAT;
	}

	public static class MetaInfo
	{
		private Mode mode;
		private Integer x;
		private Integer y;
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
		private boolean verbose;
		private boolean changelog;
		private boolean gui;
		
		// Palette source for conversion.
		private File paletteSourcePath;
		
		// Source path.
		private File sourcePath;
		private boolean recursive;
		
		// Destination path.
		private File outputPath;

		// Filename for meta info.
		private String metaInfoFilename;
		
		private MetaInfo metaInfoFallback;
		
		public Options()
		{
			this.stdout = null;
			this.stderr = null;
			this.help = false;
			this.version = false;
			this.verbose = false;
			this.gui = false;
			this.changelog = false;

			this.sourcePath = null;
			this.recursive = false;
			this.outputPath = new File(".");
			this.paletteSourcePath = null;
			this.metaInfoFilename = "dimgconv.txt";
			this.metaInfoFallback = new MetaInfo();
			this.metaInfoFallback.mode = Mode.GRAPHIC;
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
		
		public Options setVerbose(boolean verbose) 
		{
			this.verbose = verbose;
			return this;
		}

		public Options setPaletteSourcePath(File paletteSourcePath) 
		{
			this.paletteSourcePath = paletteSourcePath;
			return this;
		}
		
		public Options setSourcePath(File sourcePath) 
		{
			this.sourcePath = sourcePath;
			return this;
		}
		
		public Options setRecursive(boolean recursive) 
		{
			this.recursive = recursive;
			return this;
		}
		
		public Options setOutputPath(File outputPath) 
		{
			this.outputPath = outputPath;
			return this;
		}
		
		public Options setModeType(String mode)
		{
			if ("palettes".equalsIgnoreCase(mode))
				setMode(Mode.PALETTE);
			else if ("colormaps".equalsIgnoreCase(mode))
				setMode(Mode.COLORMAP);
			else if ("graphics".equalsIgnoreCase(mode))
				setMode(Mode.GRAPHIC);
			else if ("flats".equalsIgnoreCase(mode))
				setMode(Mode.FLAT);
			return this;
		}
		
		public Options setMode(Mode mode)
		{
			this.metaInfoFallback.mode = mode;
			return this;
		}
		
		public Options setMetaInfoFilename(String metaInfoFilename)
		{
			this.metaInfoFilename = metaInfoFilename;
			return this;
		}
		
		public void verboseln(String message) 
		{
			if (verbose)
				stdout.println(message);
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
		
		@Override
		public Integer call()
		{
			if (options.gui)
			{
				try {
					DoomToolsGUIMain.startGUIAppProcess(ApplicationNames.DIMGCONVERT);
				} catch (IOException e) {
					options.stderr.println("ERROR: Could not start DImgConv GUI!");
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
				changelog(options.stdout, "dimgconv");
				return ERROR_NONE;
			}
			
			// Check source.
			if (options.sourcePath == null)
			{
				options.stderr.println("ERROR: No source path specified.");
				return ERROR_NO_SOURCEFILE;
			}
			
			if (!options.sourcePath.exists())
			{
				options.stderr.println("ERROR: Source path not found.");
				return ERROR_NO_SOURCEFILE;
			}
			
			// Check output.
			if (options.outputPath == null)
			{
				options.stderr.println("ERROR: Destination path not specified (use the `--output` switch).");
				return ERROR_NO_DESTINATION;
			}
			
			// Check palette.
			if (options.paletteSourcePath == null) 
			{
				if(options.metaInfoFallback.mode != Mode.PALETTE)
				{
					options.stderr.println("ERROR: No palette specified, and mode is not Palette (use the `--palette` switch to specify, or `--mode-palettes` to create one).");
					return ERROR_NO_PALETTE;
				}
			}
			else if (!options.paletteSourcePath.exists())
			{
				options.stderr.println("ERROR: The palette file specified does not exist: " + options.paletteSourcePath);
				return ERROR_NO_PALETTE;
			}
			else if (options.paletteSourcePath.isDirectory())
			{
				options.stderr.println("ERROR: The palette file specified is a directory: " + options.paletteSourcePath);
				return ERROR_NO_PALETTE;
			}

			Palette palette = null;
			
			// Grab palette, if any.
			if (options.paletteSourcePath != null)
			{
				try 
				{
					if (Wad.isWAD(options.paletteSourcePath))
					{
						try (WadFile wf = new WadFile(options.paletteSourcePath))
						{
							palette = wf.getDataAs("PLAYPAL", Palette.class);
							options.verboseln("Loaded PLAYPAL from " + options.paletteSourcePath.getPath());
						}
					}
					else
					{
						try (FileInputStream fis = new FileInputStream(options.paletteSourcePath))
						{
							palette = BinaryObject.read(Palette.class, fis);
							options.verboseln("Loaded palette from " + options.paletteSourcePath.getPath());
						}
					}
				} 
				catch (IOException e) 
				{
					options.stderr.println("ERROR: I/O Error reading palette: " + e.getLocalizedMessage());
					return ERROR_IOERROR;
				}
			}
			
			// Figure out if output is directory or WAD.
			WadFile outputWad = null;
			File outputDir = null;
			File outputFile = null;
			
			// If it doesn't exist...
			if (!options.outputPath.exists())
			{
				// Is WAD?
				if (FileUtils.getFileExtension(options.outputPath).equalsIgnoreCase("wad"))
				{
					if (!FileUtils.createPathForFile(options.outputPath))
					{
						options.stderr.println("ERROR: Could not create path for " + options.outputPath.getPath());
						return ERROR_BAD_OUTPUT;
					}
					
					try {
						outputWad = WadFile.createWadFile(options.outputPath);
						options.verboseln("Created " + options.outputPath.getPath());
					} catch (IOException e) {
						options.stderr.println("ERROR: Could not create WAD file: " + options.outputPath.getPath() + ": " + e.getLocalizedMessage());
						return ERROR_BAD_OUTPUT;
					} catch (SecurityException e) {
						options.stderr.println("ERROR: Could not create WAD file: " + options.outputPath.getPath() + ": Access denied.");
						return ERROR_BAD_OUTPUT;
					}
				}
				// Is directory (no extension).
				else if (FileUtils.getFileExtension(options.outputPath).length() == 0)
				{
					if (!FileUtils.createPath(options.outputPath.getPath()))
					{
						options.stderr.println("ERROR: Could not create path for " + options.outputPath.getPath());
						return ERROR_BAD_OUTPUT;
					}
					outputDir = options.outputPath;
					options.verboseln("Output directory is " + options.outputPath.getPath());
				}
				// Is file, and source is file.
				else if (!options.sourcePath.isDirectory())
				{
					if (!FileUtils.createPathForFile(options.outputPath))
					{
						options.stderr.println("ERROR: Could not create path for " + options.outputPath.getPath());
						return ERROR_BAD_OUTPUT;
					}
					outputFile = options.outputPath;
					options.verboseln("Output file is " + options.outputPath.getPath());
				}
				// Else, error.
				else
				{
					options.stderr.println("ERROR: Target is a non-WAD file, but source is potentially multiple: " + options.sourcePath.getPath());
					return ERROR_BAD_OUTPUT;
				}
			}
			// Exists and is directory.
			else if (options.outputPath.isDirectory())
			{
				outputDir = options.outputPath;
				options.verboseln("Output directory is " + options.outputPath.getPath());
			}
			// Exists and is a file.
			else
			{
				try 
				{
					if (Wad.isWAD(options.outputPath))
					{
						outputWad = new WadFile(options.outputPath);
						options.verboseln("Output WAD is " + options.outputPath.getPath());
					}
					else if (!options.sourcePath.isDirectory())
					{
						outputFile = options.outputPath;
						options.verboseln("Output file is " + options.outputPath.getPath());
					}
					else
					{
						options.stderr.println("ERROR: Target is a non-WAD file, but source is potentially multiple: " + options.sourcePath.getPath());
						return ERROR_BAD_OUTPUT;
					}
				} 
				catch (IOException e) 
				{
					options.stderr.println("ERROR: Could not open " + options.outputPath.getPath() + " for inspection: " + e.getLocalizedMessage());
					return ERROR_BAD_OUTPUT;
				}
			}
			
			if (options.sourcePath.isDirectory())
			{
				if (outputFile != null)
				{
					options.stderr.println("!INTERNAL ERROR!: Bad Output target!");
					return ERROR_BAD_OUTPUT;
				}
				else if (outputDir != null)
				{
					try
					{
						final File dest = outputDir;
						processDir(options.sourcePath, options.sourcePath, options.recursive, palette, options.metaInfoFallback, 
							(input, pal, info, path) -> readFile(input, pal, info, new File(dest.getPath() + FileUtils.getFileNameWithoutExtension(path) + ".lmp"))
						);
					}
					catch (IOException e)
					{
						options.stderr.println("ERROR: I/O error on file write: " + e.getLocalizedMessage());
						return ERROR_IOERROR;
					} 
					catch (SecurityException e) 
					{
						options.stderr.println("ERROR: OS threw security error: " + e.getLocalizedMessage());
						return ERROR_IOERROR;
					} 
					catch (UtilityException e) 
					{
						options.stderr.println("ERROR: " + e.getLocalizedMessage());
						return ERROR_PARSE;
					}
				}
				else if (outputWad != null)
				{
					try (final WadFile.Adder adder = outputWad.createAdder())
					{
						processDir(options.sourcePath, options.sourcePath, options.recursive, palette, options.metaInfoFallback, 
							(input, pal, info, path)->readFile(input, pal, info, adder)
						);
					}
					catch (IOException e)
					{
						options.stderr.println("ERROR: I/O error on WAD write: " + e.getLocalizedMessage());
						return ERROR_IOERROR;
					} 
					catch (SecurityException e) 
					{
						options.stderr.println("ERROR: OS threw security error: " + e.getLocalizedMessage());
						return ERROR_IOERROR;
					} 
					catch (UtilityException e) 
					{
						options.stderr.println("ERROR: " + e.getLocalizedMessage());
						return ERROR_PARSE;
					}
				}
				else
				{
					options.stderr.println("!INTERNAL ERROR!: Output target not selected!");
					return ERROR_BAD_OUTPUT;
				}
			}
			else // source is file.
			{
				if (outputFile != null)
				{
					try
					{
						int err;
						if ((err = readFile(options.sourcePath, palette, options.metaInfoFallback, outputFile)) != ERROR_NONE)
							return err;
					}
					catch (IOException e)
					{
						options.stderr.println("ERROR: I/O error on WAD write: " + e.getLocalizedMessage());
						return ERROR_IOERROR;
					} 
					catch (SecurityException e) 
					{
						options.stderr.println("ERROR: OS threw security error: " + e.getLocalizedMessage());
						return ERROR_IOERROR;
					} 
				}
				else if (outputDir != null)
				{
					try
					{
						File file = new File(outputDir.getPath() + File.separator + FileUtils.getFileNameWithoutExtension(options.sourcePath) + ".lmp");
						int err;
						if ((err = readFile(options.sourcePath, palette, options.metaInfoFallback, file)) != ERROR_NONE)
							return err;
					}
					catch (IOException e)
					{
						options.stderr.println("ERROR: I/O error on WAD write: " + e.getLocalizedMessage());
						return ERROR_IOERROR;
					} 
					catch (SecurityException e) 
					{
						options.stderr.println("ERROR: OS threw security error: " + e.getLocalizedMessage());
						return ERROR_IOERROR;
					} 
				}
				else if (outputWad != null)
				{
					try (final WadFile.Adder adder = outputWad.createAdder())
					{
						readFile(options.sourcePath, palette, options.metaInfoFallback, adder);
					}
					catch (IOException e)
					{
						options.stderr.println("ERROR: I/O error on WAD write: " + e.getLocalizedMessage());
						return ERROR_IOERROR;
					} 
					catch (SecurityException e) 
					{
						options.stderr.println("ERROR: OS threw security error: " + e.getLocalizedMessage());
						return ERROR_IOERROR;
					} 
				}
				else
				{
					options.stderr.println("!INTERNAL ERROR!: Output target not selected!");
					return ERROR_BAD_OUTPUT;
				}
			}
			
			options.stdout.println("Done.");
			return ERROR_NONE;
		}

		@FunctionalInterface
		private interface FileAdder
		{
			int addFile(File input, Palette palette, MetaInfo info, String path) throws IOException;
		}
		
		private int processDir(File base, File srcDir, boolean recursive, Palette palette, MetaInfo fallback, FileAdder adder) throws IOException, SecurityException, UtilityException
		{
			options.verboseln("Scanning directory " + srcDir.getPath() + "...");
			File metaFile = new File(srcDir.getPath() + File.separator + options.metaInfoFilename);
			Map<String, MetaInfo> metaMap;
			if (metaFile.exists())
				metaMap = readMetaInfoFile(metaFile);
			else
				metaMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			
			int err;
			for (File f : srcDir.listFiles())
			{
				String treeName = f.getPath().substring(base.getPath().length());
				if (f.isDirectory())
				{
					if (!recursive)
						continue;
					else
						processDir(base, f, recursive, palette, fallback, adder);
				}
				else if (!f.getName().equals(options.metaInfoFilename))
				{
					String fileName = FileUtils.getFileNameWithoutExtension(f);
					MetaInfo info = metaMap.getOrDefault(fileName, metaMap.get("*"));
					info = info == null ? fallback : info;
					if ((err = adder.addFile(f, palette, info, treeName)) != ERROR_NONE)
						return err;
				}
			}
			
			return ERROR_NONE;
		}
				
		private int readFile(File input, Palette palette, MetaInfo info, File output) throws IOException, SecurityException
		{
			if (!FileUtils.createPathForFile(output))
			{
				options.stderr.println("ERROR: Path creation error on file write: " + output.getPath());
				return ERROR_IOERROR;
			}

			switch (info.mode)
			{
				case PALETTE:
				{
					Palette[] palettes = readPalette(input);
					try (FileOutputStream fos = new FileOutputStream(output))
					{
						for (Palette p : palettes)
							p.writeBytes(fos);
					}
				}
				break;
				
				case COLORMAP:
				{
					if (palette == null)
					{
						options.stderr.println("ERROR: Attempt to convert COLORMAP " + input.getPath() + " without a provided palette!");
						return ERROR_NO_PALETTE;
					}
					Colormap[] colormaps = readColormaps(palette, input);
					try (FileOutputStream fos = new FileOutputStream(output))
					{
						for (Colormap c : colormaps)
							c.writeBytes(fos);
					}
				}
				break;

				case FLAT:
				{
					if (palette == null)
					{
						options.stderr.println("ERROR: Attempt to convert FLAT " + input.getPath() + " without a provided palette!");
						return ERROR_NO_PALETTE;
					}
					Flat flat = readFlat(palette, input);
					try (FileOutputStream fos = new FileOutputStream(output))
					{
						flat.writeBytes(fos);
					}
				}
				break;
				
				default:
				case GRAPHIC:
				{
					if (palette == null)
					{
						options.stderr.println("ERROR: Attempt to convert GRAPHIC " + input.getPath() + " without a provided palette!");
						return ERROR_NO_PALETTE;
					}
					Picture picture = readPictureFile(input, palette, info);
					try (FileOutputStream fos = new FileOutputStream(output))
					{
						picture.writeBytes(fos);
					}
				}
				break;
			}
			options.verboseln("Wrote " + output.getPath() + ".");
			return ERROR_NONE;
		}

		private int readFile(File input, Palette palette, MetaInfo info, WadFile.Adder output) throws IOException
		{
			String entryName = NameUtils.toValidEntryName(FileUtils.getFileNameWithoutExtension(input));
			switch (info.mode)
			{
				case PALETTE:
				{
					output.addData(entryName, readPalette(input));
				}
				break;
				
				case COLORMAP:
				{
					if (palette == null)
					{
						options.stderr.println("ERROR: Attempt to convert COLORMAP " + input.getPath() + " without a provided palette!");
						return ERROR_NO_PALETTE;
					}
					output.addData(entryName, readColormaps(palette, input));
				}
				break;
	
				case FLAT:
				{
					if (palette == null)
					{
						options.stderr.println("ERROR: Attempt to convert FLAT " + input.getPath() + " without a provided palette!");
						return ERROR_NO_PALETTE;
					}
					output.addData(entryName, readFlat(palette, input));
				}
				break;
				
				default:
				case GRAPHIC:
				{
					if (palette == null)
					{
						options.stderr.println("ERROR: Attempt to convert GRAPHIC " + input.getPath() + " without a provided palette!");
						return ERROR_NO_PALETTE;
					}
					Picture picture = readPictureFile(input, palette, info);
					output.addData(entryName, picture);
				}
				break;
			}
			options.verboseln("Added " + input.getPath() + " to WAD as " + entryName);
			return ERROR_NONE;
		}
		
		private Picture readPictureFile(File input, Palette palette, MetaInfo info) throws IOException, FileNotFoundException
		{
			Picture picture;
			if (FileUtils.getFileExtension(input).equalsIgnoreCase("png"))
			{
				options.verboseln("Reading " + input.getPath() + " as PNG graphic...");
				PNGPicture png = new PNGPicture();
				try (FileInputStream fis = new FileInputStream(input))
				{
					png.readBytes(fis);
				}
				picture = new Picture(png.getWidth(), png.getHeight());
				for (int x = 0; x < png.getWidth(); x++)
				{
					for (int y = 0; y < png.getHeight(); y++)
					{
						int argb = png.getImage().getRGB(x, y);
						// must be absolutely opaque.
						if ((argb & 0xff000000) != 0xff000000)
							picture.setPixel(x, y, Picture.PIXEL_TRANSLUCENT);
						else
							picture.setPixel(x, y, palette.getNearestColorIndex(argb, true));
					}
				}
				picture.setOffsetX(png.getOffsetX());
				picture.setOffsetY(png.getOffsetY());
			}
			else
			{
				picture = readPicture(palette, input);
			}
			
			if (info.x != null)
				picture.setOffsetX(info.x);
			if (info.y != null)
				picture.setOffsetY(info.y);
			
			return picture;
		}
		
		private Palette[] readPalette(File f) throws IOException
		{
			options.verboseln("Reading " + f.getPath() + " as palette...");
			BufferedImage image = ImageIO.read(f);
			Palette[] out = new Palette[image.getHeight()];
			int maxWidth = Math.min(Math.max(image.getWidth(), 0), 256);
			for (int i = 0; i < out.length; i++)
				out[i] = new Palette();
			
			for (int y = 0; y < out.length; y++)
				for (int x = 0; x < maxWidth; x++)
					out[y].setColor(x, image.getRGB(x, y));
			return out;
		}
		
		private Colormap[] readColormaps(Palette pal, File f) throws IOException
		{
			options.verboseln("Reading " + f.getPath() + " as colormap...");
			BufferedImage image = ImageIO.read(f);
			Colormap[] out = new Colormap[image.getHeight()];
			int maxWidth = Math.min(Math.max(image.getWidth(), 0), 256);
			for (int i = 0; i < out.length; i++)
				out[i] = new Colormap();

			for (int y = 0; y < out.length; y++)
				for (int x = 0; x < maxWidth; x++)
					out[y].setPaletteIndex(x, pal.getNearestColorIndex(image.getRGB(x, y)));
			return out;
		}
		
		private Picture readPicture(Palette pal, File f) throws IOException
		{
			options.verboseln("Reading " + f.getPath() + " as graphic...");
			BufferedImage image = ImageIO.read(f);
			Picture out = new Picture(image.getWidth(), image.getHeight());
			for (int x = 0; x < image.getWidth(); x++)
				for (int y = 0; y < image.getHeight(); y++)
				{
					int argb = image.getRGB(x, y);
					// must be absolutely opaque.
					if ((argb & 0xff000000) != 0xff000000)
						out.setPixel(x, y, Picture.PIXEL_TRANSLUCENT);
					else
						out.setPixel(x, y, pal.getNearestColorIndex(argb, true));
				}
			return out;
		}
		
		private Flat readFlat(Palette pal, File f) throws IOException
		{
			options.verboseln("Reading " + f.getPath() + " as flat...");
			BufferedImage image = ImageIO.read(f);
			Flat out = new Flat(image.getWidth(), image.getHeight());
			for (int x = 0; x < image.getWidth(); x++)
				for (int y = 0; y < image.getHeight(); y++)
				{
					int argb = image.getRGB(x, y);
					// must be absolutely opaque.
					if ((argb & 0xff000000) != 0xff000000)
						out.setPixel(x, y, 0);
					else
						out.setPixel(x, y, pal.getNearestColorIndex(argb));
				}
			return out;
		}
		
		private Map<String, MetaInfo> readMetaInfoFile(File f) throws IOException, UtilityException
		{
			options.verboseln("Reading metadata file from " + f.getPath() + "...");
			Map<String, MetaInfo> out = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			int i = 0;
			try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f))))
			{
				String line;
				while ((line = br.readLine()) != null)
				{
					i++;
					line = line.trim();
					if (line.length() == 0)
						continue;
					if (line.startsWith("#"))
						continue;
					try (TokenScanner scanner = new TokenScanner(line))
					{
						MetaInfo info = new MetaInfo();
						String name = scanner.nextString();
						if (!scanner.hasNext())
							throw new UtilityException("(" + f.getPath() + ") Line " + i + ": Expected mode after entry name.");

						String modeString = scanner.nextString();
						if (Mode.PALETTE.name().equalsIgnoreCase(modeString))
							info.mode = Mode.PALETTE;
						else if (Mode.COLORMAP.name().equalsIgnoreCase(modeString))
							info.mode = Mode.COLORMAP;
						else if (Mode.GRAPHIC.name().equalsIgnoreCase(modeString))
							info.mode = Mode.GRAPHIC;
						else if (Mode.FLAT.name().equalsIgnoreCase(modeString))
							info.mode = Mode.FLAT;
						else
							throw new UtilityException("(" + f.getPath() + ") Line " + i + ": Expected valid mode after entry name: " + Arrays.toString(Mode.values()));
						
						if (info.mode == Mode.GRAPHIC)
						{
							if (scanner.hasNext())
								info.x = scanner.nextInt();
							if (scanner.hasNext())
								info.y = scanner.nextInt();
						}
						
						out.put(name, info);
					}
				}
			} 
			return out;
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
		final int STATE_OUTPUT = 1;
		final int STATE_METAFILENAME = 2;
		final int STATE_PALETTE = 3;
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
					else if (arg.equalsIgnoreCase(SWITCH_VERSION))
						options.version = true;
					else if (arg.equalsIgnoreCase(SWITCH_GUI))
						options.gui = true;
					else if (arg.equalsIgnoreCase(SWITCH_CHANGELOG))
						options.changelog = true;
					else if (arg.equalsIgnoreCase(SWITCH_VERBOSE) || arg.equalsIgnoreCase(SWITCH_VERBOSE2))
						options.setVerbose(true);
					else if (arg.equalsIgnoreCase(SWITCH_RECURSIVE) || arg.equalsIgnoreCase(SWITCH_RECURSIVE2))
						options.setRecursive(true);
					else if (arg.equalsIgnoreCase(SWITCH_MODE_PALETTES) || arg.equalsIgnoreCase(SWITCH_MODE_PALETTES2))
						options.setMode(Mode.PALETTE);
					else if (arg.equalsIgnoreCase(SWITCH_MODE_COLORMAPS) || arg.equalsIgnoreCase(SWITCH_MODE_COLORMAPS2))
						options.setMode(Mode.COLORMAP);
					else if (arg.equalsIgnoreCase(SWITCH_MODE_FLATS) || arg.equalsIgnoreCase(SWITCH_MODE_FLATS2))
						options.setMode(Mode.FLAT);
					else if (arg.equalsIgnoreCase(SWITCH_OUTPUT) || arg.equalsIgnoreCase(SWITCH_OUTPUT2))
						state = STATE_OUTPUT;
					else if (arg.equalsIgnoreCase(SWITCH_METAINFOFILE) || arg.equalsIgnoreCase(SWITCH_METAINFOFILE2))
						state = STATE_METAFILENAME;
					else if (arg.equalsIgnoreCase(SWITCH_PALETTE) || arg.equalsIgnoreCase(SWITCH_PALETTE2))
						state = STATE_PALETTE;
					else if (options.sourcePath == null)
						options.sourcePath = new File(arg);
					else
						throw new OptionParseException("ERROR: Source already declared.");
				}
				break;

				case STATE_OUTPUT:
				{
					options.outputPath = new File(arg);
					state = STATE_START;
				}
				break;

				case STATE_METAFILENAME:
				{
					options.metaInfoFilename = arg;
					state = STATE_START;
				}
				break;

				case STATE_PALETTE:
				{
					options.paletteSourcePath = new File(arg);
					state = STATE_START;
				}
				break;
			}
			i++;
		}
		
		if (state == STATE_OUTPUT)
			throw new OptionParseException("ERROR: Expected path to output directory (or WAD file).");
		if (state == STATE_METAFILENAME)
			throw new OptionParseException("ERROR: Expected name of metainfo filename.");
		if (state == STATE_PALETTE)
			throw new OptionParseException("ERROR: Expected path to palette file.");
		
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
		out.println("Usage: dimgconv [--help | -h | --version]");
		out.println("                [source] [switches]");
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
		out.println("[source]:");
		out.println("    The source file or directory to read from.");
		out.println();
		out.println("[switches]:");
		out.println("    --output [path]     Sets the output path for converted files. Can be");
		out.println("    -o [path]           a single file, a directory, or a WAD file to add");
		out.println("                        entries to.");
		out.println();
		out.println("    --palette [path]    Sets the path to the source of target palette to use");
		out.println("    -p [path]           for conversion (Doom format). Can be a file or a");
		out.println("                        WAD. If WAD, it looks for the \"PLAYPAL\" lump.");
		out.println();
		out.println("    --recursive         If the [source] is a directory, this scans directories");
		out.println("    -r                  recursively.");
		out.println();
		out.println("    --mode-palettes     Assumes that the incoming file or files are, by");
		out.println("    -mp                 default, palettes, unless overridden by a metainfo");
		out.println("                        file.");
		out.println();
		out.println("    --mode-colormaps    Assumes that the incoming file or files are, by");
		out.println("    -mc                 default, colormaps, unless overridden by a metainfo");
		out.println("                        file.");
		out.println();
		out.println("    --mode-flats        Assumes that the incoming file or files are, by");
		out.println("    -mf                 default, flats, unless overridden by a metainfo");
		out.println("                        file.");
		out.println();
		out.println("    --infofile [name]   Sets the name of the metainfo file found in each");
		out.println("    -i [name]           directory that specifies the mode for specific");
		out.println("                        files. Default is \"dimgconv.txt\"");
		out.println();
		out.println("    --verbose           Prints verbose output.");
		out.println("    -v");
		out.println();
		out.println();
		out.println("Palettes");
		out.println("--------");
		out.println();
		out.println("Palettes are expected to be images where the x-axis describes the colors in a");
		out.println("palette and the y-axis is the palette index.");
		out.println();
		out.println();
		out.println("Colormaps");
		out.println("---------");
		out.println();
		out.println("Colormaps are expected to be images where the x-axis describes the colors in a");
		out.println("colomap to match against a palette to create the resulting map data and the");
		out.println("y-axis is the colormap index.");
		out.println();
		out.println("Stuff like TRANMAPs and TINTTABs are also COLORMAPs.");
		out.println();
		out.println();
		out.println("Graphics");
		out.println("--------");
		out.println();
		out.println("Graphics, if they are PNGs, can contain the custom 'grAb' chunks for default");
		out.println("offsets.");
		out.println();
		out.println();
		out.println("The MetaInfo Files");
		out.println("------------------");
		out.println();
		out.println("The meta info files consist of plain text lines of the following:");
		out.println();
		out.println("    [name] [mode] [x-offset] [y-offset]");
		out.println();
		out.println("[name]:");
		out.println("    The name of the file in this directory (no extension). Can be \"*\" for the");
		out.println("    default fallback.");
		out.println();
		out.println("[mode]:");
		out.println("    One of four modes: \"palette\", \"colormap\", \"graphic\" or \"flat\".");
		out.println();
		out.println("[x-offset]:");
		out.println("    (Optional) If mode is \"graphic\", specify an x-offset to set.");
		out.println();
		out.println("[y-offset]:");
		out.println("    (Optional) If mode is \"graphic\", specify a y-offset to set.");
		out.println();
		out.println("Lines that are blank and lines that begin with a \"#\" are ignored.");
		out.println();
		out.println("Example line for \"everything in this directory is a flat\":");
		out.println();
		out.println("    * flat");
		out.println();
		out.println("Example lines for \"GRAYTALL is a graphic but everything else is a flat\":");
		out.println();
		out.println("    graytall graphic");
		out.println("    * flat");
		out.println();
		out.println("Graphics with specific offsets:");
		out.println();
		out.println("    m_skull graphic 0 12");
		out.println("    m_doom graphic 30 -24");
	}

}

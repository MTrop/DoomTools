package net.mtrop.doom.tools;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import net.mtrop.doom.Wad;
import net.mtrop.doom.WadFile;
import net.mtrop.doom.graphics.Colormap;
import net.mtrop.doom.graphics.Flat;
import net.mtrop.doom.graphics.Palette;
import net.mtrop.doom.graphics.Picture;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.exception.OptionParseException;
import net.mtrop.doom.tools.exception.UtilityException;
import net.mtrop.doom.tools.struct.TokenScanner;
import net.mtrop.doom.util.NameUtils;

/**
 * Main class for Utility.
 * @author Matthew Tropiano
 */
public final class DoomImageConvertMain 
{
	private static final String DOOM_VERSION = Common.getVersionString("doom");
	private static final String VERSION = Common.getVersionString("dimgconv");
	private static final String SPLASH_VERSION = "DImgConv v" + VERSION + " by Matt Tropiano (using DoomStruct v" + DOOM_VERSION + ")";

	private static final int ERROR_NONE = 0;
	private static final int ERROR_BAD_OPTIONS = 1;
	private static final int ERROR_NO_SOURCEFILE = 2;

	private static final String SWITCH_HELP = "--help";
	private static final String SWITCH_HELP2 = "-h";
	private static final String SWITCH_VERSION = "--version";

	private static final String SWITCH_RECURSIVE = "--recursive";
	private static final String SWITCH_RECURSIVE2 = "-r";

	private static final String SWITCH_OUTPUT = "--output";
	private static final String SWITCH_OUTPUT2 = "-o";

	private static final String SWITCH_METAINFOFILE = "--infofile";
	private static final String SWITCH_METAINFOFILE2 = "-i";

	private static final String SWITCH_PALETTE = "--palette";
	private static final String SWITCH_PALETTE2 = "-p";

	private static final String SWITCH_MODE_PALETTES = "--mode-palettes";
	private static final String SWITCH_MODE_PALETTES2 = "-mp";

	private static final String SWITCH_MODE_COLORMAPS = "--mode-colormaps";
	private static final String SWITCH_MODE_COLORMAPS2 = "-mc";

	private static final String SWITCH_MODE_FLATS = "--mode-flats";
	private static final String SWITCH_MODE_FLATS2 = "-mf";

	private static final Colormap COLORMAP_IDENTITY = Colormap.createIdentityMap();
	
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
		
		// Palette source for conversion.
		private File paletteSourcePath;
		
		// Source path.
		private File sourcePath;
		private boolean recursive;
		
		// Destination path.
		private File outputPath;

		// Filename for meta info.
		private String metaInfoFilename;
		
		public Options()
		{
			this.stdout = null;
			this.stderr = null;
			this.help = false;
			this.version = false;
			this.verbose = false;
			
			this.sourcePath = null;
			this.recursive = false;
			this.outputPath = new File(".");
			this.paletteSourcePath = null;
			this.metaInfoFilename = "dimgconv.txt";
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
		
		public Options setMetaInfoFilename(String metaInfoFilename)
		{
			this.metaInfoFilename = metaInfoFilename;
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
		
			if (options.sourcePath == null)
			{
				options.stderr.println("ERROR: No source path specified.");
				return ERROR_NO_SOURCEFILE;
			}
			
			if (options.sourcePath.isDirectory())
			{
				// TODO: Finish this.
			}
			else // is file.
			{
				// TODO: Finish this.
			}
			
			// TODO: Finish.

			return ERROR_NONE;
		}

		private void processDir(File base, File srcDir, boolean recursive, Palette palette, MetaInfo fallback, File destDir) throws IOException, SecurityException, UtilityException
		{
			File metaFile = new File(srcDir.getPath() + File.separator + options.metaInfoFilename);
			Map<String, MetaInfo> metaMap;
			if (metaFile.exists())
				metaMap = readMetaInfoFile(metaFile);
			else
				metaMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			
			for (File f : srcDir.listFiles())
			{
				String treeName = f.getPath().substring(base.getPath().length());
				if (f.isDirectory())
				{
					if (!recursive)
						continue;
					else
						processDir(base, f, recursive, palette, fallback, destDir);
				}
				else
				{
					String fileName = Common.getFileNameWithoutExtension(f);
					MetaInfo info = metaMap.getOrDefault(fileName, metaMap.get("*"));
					File outputFile = new File(destDir.getPath() + treeName);
					info = info == null ? fallback : info;
					readFile(f, palette, info, outputFile);
				}
			}
		}
				
		private static void readFile(File input, Palette palette, MetaInfo info, File output) throws IOException, SecurityException
		{
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
					Flat flat = readFlat(palette, COLORMAP_IDENTITY, input);
					try (FileOutputStream fos = new FileOutputStream(output))
					{
						flat.writeBytes(fos);
					}
				}
				break;
				
				default:
				case GRAPHIC:
				{
					Picture picture = readGraphic(palette, COLORMAP_IDENTITY, input);
					picture.setOffsetX(info.x != null ? info.x : 0);
					picture.setOffsetY(info.y != null ? info.y : 0);
					try (FileOutputStream fos = new FileOutputStream(output))
					{
						picture.writeBytes(fos);
					}
				}
				break;
			}
		}
		
		private static void readFile(File input, Palette palette, MetaInfo info, Wad output) throws IOException
		{
			switch (info.mode)
			{
				case PALETTE:
					output.addData(NameUtils.toValidEntryName(Common.getFileNameWithoutExtension(input)), readPalette(input));
					break;
				
				case COLORMAP:
					output.addData(NameUtils.toValidEntryName(Common.getFileNameWithoutExtension(input)), readColormaps(palette, input));
					break;
	
				case FLAT:
					output.addData(NameUtils.toValidEntryName(Common.getFileNameWithoutExtension(input)), readFlat(palette, COLORMAP_IDENTITY, input));
					break;
				
				default:
				case GRAPHIC:
					Picture picture = readGraphic(palette, COLORMAP_IDENTITY, input);
					picture.setOffsetX(info.x != null ? info.x : 0);
					picture.setOffsetY(info.y != null ? info.y : 0);
					output.addData(NameUtils.toValidEntryName(Common.getFileNameWithoutExtension(input)), picture);
					break;
			}
		}
		
		private static void readFile(File input, Palette palette, MetaInfo info, WadFile.Adder output) throws IOException
		{
			switch (info.mode)
			{
				case PALETTE:
					output.addData(NameUtils.toValidEntryName(Common.getFileNameWithoutExtension(input)), readPalette(input));
					break;
				
				case COLORMAP:
					output.addData(NameUtils.toValidEntryName(Common.getFileNameWithoutExtension(input)), readColormaps(palette, input));
					break;
	
				case FLAT:
					output.addData(NameUtils.toValidEntryName(Common.getFileNameWithoutExtension(input)), readFlat(palette, COLORMAP_IDENTITY, input));
					break;
				
				default:
				case GRAPHIC:
					Picture picture = readGraphic(palette, COLORMAP_IDENTITY, input);
					picture.setOffsetX(info.x != null ? info.x : 0);
					picture.setOffsetY(info.y != null ? info.y : 0);
					output.addData(NameUtils.toValidEntryName(Common.getFileNameWithoutExtension(input)), picture);
					break;
			}
		}
		
		private static Palette[] readPalette(File f) throws IOException
		{
			BufferedImage image = ImageIO.read(f);
			Palette[] out = new Palette[image.getHeight()];
			for (int y = 0; y < out.length; y++)
				for (int x = 0; x < image.getWidth(); x++)
					out[y].setColor(x, image.getRGB(x, y));
			return out;
		}
		
		private static Colormap[] readColormaps(Palette pal, File f) throws IOException
		{
			BufferedImage image = ImageIO.read(f);
			Colormap[] out = new Colormap[image.getHeight()];
			for (int y = 0; y < out.length; y++)
				for (int x = 0; x < image.getWidth(); x++)
					out[y].setPaletteIndex(x, pal.getNearestColorIndex(image.getRGB(x, y)));
			return out;
		}
		
		private static Picture readGraphic(Palette pal, Colormap cm, File f) throws IOException
		{
			BufferedImage image = ImageIO.read(f);
			Picture out = new Picture(image.getWidth(), image.getHeight());
			for (int x = 0; x < image.getWidth(); x++)
				for (int y = 0; y < image.getHeight(); y++)
				{
					int argb = image.getRGB(x, y);
					if ((argb & 0xff000000) != 0xff000000)
						continue;
					out.setPixel(x, y, cm.getPaletteIndex(pal.getNearestColorIndex(argb)));
				}
			return out;
		}
		
		private static Flat readFlat(Palette pal, Colormap cm, File f) throws IOException
		{
			BufferedImage image = ImageIO.read(f);
			Flat out = new Flat(image.getWidth(), image.getHeight());
			for (int x = 0; x < image.getWidth(); x++)
				for (int y = 0; y < image.getHeight(); y++)
					out.setPixel(x, y, cm.getPaletteIndex(pal.getNearestColorIndex(image.getRGB(x, y))));
			return out;
		}
		
		private static Map<String, MetaInfo> readMetaInfoFile(File f) throws IOException, UtilityException
		{
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
	
		int i = 0;
		while (i < args.length)
		{
			String arg = args[i];
			if (arg.equals(SWITCH_HELP) || arg.equals(SWITCH_HELP2))
				options.help = true;
			else if (arg.equals(SWITCH_VERSION))
				options.version = true;
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
		out.println("                [files] [switches]");
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
	}

}

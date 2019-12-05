package net.mtrop.doom.tools.wadmerge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

import net.mtrop.doom.exception.WadException;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.common.Response;

/**
 * The Wad Merge commands.
 * @author Matthew Tropiano
 */
public enum WadMergeCommand
{
	/*
	CALL
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("CALL [script]");
			out.println("    Calls another merge script. The working directory will be the parent");
			out.println("    directory of the script file.");
			out.println("    [script]: The file name of the script.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			String oldWorkingDirectory = System.getProperty("user.dir");
			
			String streamName;
			BufferedReader reader = null;
			String fileName = scanner.next();
			File inputFile = new File(fileName);
			
			System.setProperty("user.dir", inputFile.getParent());
			
			try
			{
				if (!inputFile.exists() || inputFile.isDirectory())
					return Response.BAD_FILE;

				streamName = inputFile.getPath();
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
				if (!callScript(streamName, reader, context))
					return Response.BAD_PARSE;
			}
			catch (FileNotFoundException e)
			{
				context.logf("ERROR: File %s not found.\n", inputFile.getPath());
				return Response.BAD_FILE;
			}
			catch (SecurityException e)
			{
				context.logf("ERROR: File %s not readable (access denied).\n", inputFile.getPath());
				return Response.BAD_FILE;
			}
			catch (IOException e)
			{
				context.logf("ERROR: File %s not readable.\n", inputFile.getPath());
				return Response.BAD_FILE;
			}
			finally
			{
				IOUtils.close(reader);
				System.setProperty("user.dir", oldWorkingDirectory);
			}
			
			return Response.OK;
		}
	},
	*/
	
	END
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("END"); 
			out.println("    Ends script read.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			// Kills the script.
			return null;
		}
	},
	
	CREATE
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("CREATE [symbol]"); 
			out.println("    Creates a new buffer, errors out if the symbol exists."); 
			out.println("    [symbol]: The symbol for the new buffer.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			return context.create(scanner.next());
		}
	},
	
	CLEAR
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("CLEAR [symbol]"); 
			out.println("    Clears an existing buffer, errors out if the symbol does not exist."); 
			out.println("    [symbol]: The symbol for the existing buffer to clear.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			return context.clear(scanner.next());
		}
	},
	
	DISCARD
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("DISCARD [symbol]"); 
			out.println("    Discards an existing buffer."); 
			out.println("    [symbol]: The symbol for the existing buffer to discard.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			return context.discard(scanner.next());
		}
	},
	
	VALID
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("VALID [symbol]"); 
			out.println("    Asserts that a symbol is a valid buffer."); 
			out.println("    [symbol]: The symbol to test.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			return context.isValid(scanner.next());
		}
	},
	
	MARKER
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("MARKER [symbol] [name]"); 
			out.println("    Adds an empty entry to [symbol] called [name]."); 
			out.println("    [symbol]: The symbol to add to.");
			out.println("    [name]:   The name of the marker.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			return context.addMarker(scanner.next(), scanner.next());
		}
	},
	
	DATEMARKER
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("DATEMARKER [symbol] [name]"); 
			out.println("    Adds an entry to [symbol] called [name] with the current date."); 
			out.println("    [symbol]: The symbol to add to.");
			out.println("    [name]:   The name of the marker.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			return context.addDateMarker(scanner.next(), scanner.next());
		}
	},
	
	MERGE
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("MERGE [dest-symbol] [src-symbol]");
			out.println("    Adds all entries from [src-symbol] into [dest-symbol].");
			out.println("    [dest-symbol]: Destination buffer symbol.");
			out.println("    [src-symbol]:  Source buffer symbol.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			return context.mergeBuffer(scanner.next(), scanner.next());
		}
	},
	
	MERGEWAD
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("MERGEWAD [symbol] [path]"); 
			out.println("    Reads WAD entries from [path] into buffer [symbol].");
			out.println("    [symbol]: The buffer to add to.");
			out.println("    [path]:   The WAD contents to add.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			String symbol = scanner.next();
			String wadFile = scanner.next();
			try {
				return context.mergeWad(symbol, new File(wadFile));
			} catch (FileNotFoundException e) {
				context.logf("ERROR: File %s not found.\n", wadFile);
				return Response.BAD_FILE;
			} catch (SecurityException e) {
				context.logf("ERROR: File %s not readable (access denied).\n", wadFile);
				return Response.BAD_FILE;
			} catch (WadException e) {
				context.logf("ERROR: File %s is not a WAD.\n", wadFile);
				return Response.BAD_FILE;
			} catch (IOException e) {
				context.logf("ERROR: File %s not readable.\n", wadFile);
				return Response.BAD_FILE;
			}
		}
	},
	
	MERGEFILE
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("MERGEFILE [symbol] [path] [opt:entryname]"); 
			out.println("    Reads file from [path] into [symbol].");
			out.println("    [symbol]:    The buffer to add to.");
			out.println("    [path]:      The file to add.");
			out.println("    [entryname]: (Optional) If specified, this is the entry name to use");
			out.println("                 to import as.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			String symbol = scanner.next();
			String file = scanner.next();
			String entryName = null;
			if (scanner.hasNext())
				entryName = scanner.next();
			
			try {
				File f = new File(file);
				return context.mergeFile(symbol, f, entryName == null ? Common.getFileNameWithoutExtension(f) : entryName);
			} catch (FileNotFoundException e) {
				context.logf("ERROR: File %s not found.\n", file);
				return Response.BAD_FILE;
			} catch (SecurityException e) {
				context.logf("ERROR: File %s not readable (access denied).\n", file);
				return Response.BAD_FILE;
			} catch (IOException e) {
				context.logf("ERROR: File %s not readable.\n", file);
				return Response.BAD_FILE;
			}
		}
	},
	
	MERGEMAP
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("MERGEMAP [dest-symbol] [targetmap] [src-symbol] [opt:sourcemap]"); 
			out.println("    Reads file from [path] into [symbol].");
			out.println("    [dest-symbol]: The buffer to add to.");
			out.println("    [targetmap]:   The map to add (map header).");
			out.println("    [src-symbol]:  The buffer to read from.");
			out.println("    [sourcemap]:   (Optional) If specified, the map to read from the WAD,");
			out.println("                   and the target is the new header name.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			String destSymbol = scanner.next();
			String map1 = scanner.next();
			String srcSymbol = scanner.next();
			String map2 = null;
			if (scanner.hasNext())
				map2 = scanner.next();
			else
				map2 = map1;
			try {
				return context.mergeMap(destSymbol, map2, srcSymbol, map1);
			} catch (IOException e) {
				context.logf("ERROR: File %s not readable.\n", srcSymbol);
				return Response.BAD_FILE;
			}
		}
	},
	
	MERGEMAPFILE
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("MERGEMAPFILE [symbol] [targetmap] [path] [opt:sourcemap]"); 
			out.println("    Reads file from [path] into [symbol].");
			out.println("    [symbol]:    The buffer to add to.");
			out.println("    [targetmap]: The map to add (target header).");
			out.println("    [path]:      The source WAD file to read from.");
			out.println("    [sourcemap]: (Optional) If specified, the map to read from the WAD,");
			out.println("                 and the target is the new header name.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			String symbol = scanner.next();
			String map1 = scanner.next();
			String wadFile = scanner.next();
			String map2 = null;
			if (scanner.hasNext())
				map2 = scanner.next();
			else
				map2 = map1;
			try {
				return context.mergeMap(symbol, map1, new File(wadFile), map2);
			} catch (FileNotFoundException e) {
				context.logf("ERROR: File %s not found.\n", wadFile);
				return Response.BAD_FILE;
			} catch (SecurityException e) {
				context.logf("ERROR: File %s not readable (access denied).\n", wadFile);
				return Response.BAD_FILE;
			} catch (WadException e) {
				context.logf("ERROR: File %s is not a WAD.\n", wadFile);
				return Response.BAD_FILE;
			} catch (IOException e) {
				context.logf("ERROR: File %s not readable.\n", wadFile);
				return Response.BAD_FILE;
			}
		}
	},
	
	MERGEDIR
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("MERGEDIR [symbol] [path]");
		    out.println("    For each FILE in [path],"); 
		    out.println("        If FILE is DIR,");
		    out.println("            MARKER [symbol] \\[FILE]");
		    out.println("            MERGEDIR [symbol] [FILE]");
		    out.println("        Else,");
		    out.println("            MERGEFILE [symbol] [FILE]");
			out.println("    [symbol]: The buffer to add to.");
			out.println("    [path]:   The source directory to scan.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			String symbol = scanner.next();
			String directory = scanner.next();
			try {
				return context.mergeTree(symbol, new File(directory));
			} catch (FileNotFoundException e) {
				context.logf("ERROR: File %s not found.\n", directory);
				return Response.BAD_FILE;
			} catch (SecurityException e) {
				context.logf("ERROR: File %s not readable (access denied).\n", directory);
				return Response.BAD_FILE;
			} catch (IOException e) {
				context.logf("ERROR: File %s not readable.\n", directory);
				return Response.BAD_FILE;
			}
		}
	},
	
	MERGESWANTBLS
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("MERGESWANTBLS [symbol] [path]");
			out.println("    Reads file from [path], interprets it as a SWANTBLS file, creates two");
			out.println("    entries in [symbol]: ANIMATED and SWITCHES.");
			out.println("    [symbol]: The buffer to add to.");
			out.println("    [path]:   The file to read.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			String symbol = scanner.next();
			String file = scanner.next();
			try {
				return context.mergeSwitchAnimatedTables(symbol, new File(file));
			} catch (FileNotFoundException e) {
				context.logf("ERROR: File %s not found.\n", file);
				return Response.BAD_FILE;
			} catch (SecurityException e) {
				context.logf("ERROR: File %s not readable (access denied).\n", file);
				return Response.BAD_FILE;
			} catch (IOException e) {
				context.logf("ERROR: File %s not readable.\n", file);
				return Response.BAD_FILE;
			}
		}
	},
	
	MERGEDEUTEXFILE
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("MERGEDEUTEXFILE [symbol] [path] [opt:entry]");
			out.println("    Reads file from [path], interprets it as a DEUTeX texture/patch assembly");
			out.println("    file, creates TEXTUREx/PNAMES. The name of the file is the name of the");
			out.println("    texture lump.");
			out.println("    [symbol]: The buffer to add to.");
			out.println("    [path]:   The file to read.");
			out.println("    [entry]:  (Optional) If specified, the name of the entry to write.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			String symbol = scanner.next();
			String file = scanner.next();
			String textureEntryName = null;
			if (scanner.hasNext())
				textureEntryName = scanner.next();
			else
				textureEntryName = Common.getFileNameWithoutExtension(file, ".");
			
			try {
				return context.mergeDEUTEXTextureFile(symbol, new File(file), false, textureEntryName);
			} catch (FileNotFoundException e) {
				context.logf("ERROR: File %s not found.\n", file);
				return Response.BAD_FILE;
			} catch (SecurityException e) {
				context.logf("ERROR: File %s not readable (access denied).\n", file);
				return Response.BAD_FILE;
			} catch (IOException e) {
				context.logf("ERROR: File %s not readable.\n", file);
				return Response.BAD_FILE;
			}
		}
	},
	
	MERGETEXTUREDIR
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("MERGETEXTUREDIR [symbol] [path] [entry]");
		    out.println("    Reads directory from [path].");
		    out.println("    Calls `MARKER [symbol] pp_start`.");
		    out.println("    For each file in [path],");
		    out.println("        Add file name to PNAMES, add [file] to TEXTURE1 with only patch [file].");
		    out.println("    Calls `MARKER [symbol] pp_end`.");
		    out.println("    Export [entry]/PNAMES.");
			out.println("    [symbol]: The buffer to add to.");
			out.println("    [path]:   The file to read.");
			out.println("    [entry]:  The name of the texture entry to write.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			String symbol = scanner.next();
			String file = scanner.next();
			String textureEntryName = scanner.next();
			
			try {
				return context.mergeTextureDirectory(symbol, new File(file), textureEntryName);
			} catch (FileNotFoundException e) {
				context.logf("ERROR: File %s not found.\n", file);
				return Response.BAD_FILE;
			} catch (SecurityException e) {
				context.logf("ERROR: File %s not readable (access denied).\n", file);
				return Response.BAD_FILE;
			} catch (IOException e) {
				context.logf("ERROR: File %s not readable.\n", file);
				return Response.BAD_FILE;
			}
		}
	},
	
	SAVE
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("SAVE [symbol] [file]");
			out.println("    Exports the content of a buffer to a WAD file. Directories are created for");
			out.println("    the file, if they don't exist.");
			out.println("    [symbol]: The buffer to export.");
			out.println("    [file]:   The file to output.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			String symbol = scanner.next();
			String file = scanner.next();
			
			try {
				return context.save(symbol, new File(file));
			} catch (FileNotFoundException e) {
				context.logf("ERROR: File %s not found.\n", file);
				return Response.BAD_FILE;
			} catch (SecurityException e) {
				context.logf("ERROR: File %s not readable (access denied).\n", file);
				return Response.BAD_FILE;
			} catch (WadException e) {
				context.logf("ERROR: File %s is not a WAD.\n", file);
				return Response.BAD_FILE;
			} catch (IOException e) {
				context.logf("ERROR: File %s not readable.\n", file);
				return Response.BAD_FILE;
			}
		}
	},
	
	LOAD
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("LOAD [symbol] [file]");
			out.println("    Creates a new buffer by loading an existing WAD file into memory.");
			out.println("    The symbol must not already exist.");
			out.println("    [symbol]: The buffer to create.");
			out.println("    [file]:   The file to read.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			String symbol = scanner.next();
			String file = scanner.next();
			try {
				return context.load(symbol, new File(file));
			} catch (SecurityException e) {
				context.logf("ERROR: File %s not readable (access denied).\n", file);
				return Response.BAD_FILE;
			} catch (IOException e) {
				context.logf("ERROR: File %s not writeable.\n", file);
				return Response.BAD_FILE;
			}
		}
	},
	
	FINISH
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("FINISH [symbol] [file]");
			out.println("    Exports the content of a buffer to a WAD file, and disacrds the buffer.");
			out.println("     Directories are created for the file, if they don't exist.");
			out.println("    [symbol]: The buffer to export.");
			out.println("    [file]:   The file to output.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			String symbol = scanner.next();
			String file = scanner.next();
			
			try {
				return context.finish(symbol, new File(file));
			} catch (FileNotFoundException e) {
				context.logf("ERROR: File %s not found.\n", file);
				return Response.BAD_FILE;
			} catch (SecurityException e) {
				context.logf("ERROR: File %s not readable (access denied).\n", file);
				return Response.BAD_FILE;
			} catch (WadException e) {
				context.logf("ERROR: File %s is not a WAD.\n", file);
				return Response.BAD_FILE;
			} catch (IOException e) {
				context.logf("ERROR: File %s not readable.\n", file);
				return Response.BAD_FILE;
			}
		}
	},
	
	;
	
	/**
	 * Prints help.
	 * @param out the output stream for messages.
	 */
	public abstract void help(PrintStream out);
	
	/**
	 * Executes the commands.
	 * @param context the WAD Merge Context.
	 * @param scanner the scanner for arguments.
	 * @return a Response enum.
	 */
	public abstract Response execute(WadMergeContext context, Scanner scanner);
	
	/**
	 * Executes the provided script.
	 * @param streamName stream name.
	 * @param reader the reader to read the script from.
	 * @param context the WAD merge context.
	 * @return true if no errors, false otherwise.
	 * @throws IOException
	 */
	public static boolean callScript(String streamName, BufferedReader reader, WadMergeContext context) throws IOException
	{
		String line;
		int linenum = 0;
		Pattern whitespacePattern = Pattern.compile("\\s+"); 
		
		while ((line = reader.readLine()) != null)
		{
			linenum++;
			line = line.trim();
			if (line.isEmpty() || line.startsWith("#"))
				continue;
			WadMergeCommand mergeCommand = null;
			try (Scanner scanner = new Scanner(line)) 
			{
				scanner.useDelimiter(whitespacePattern);
				String command = scanner.next().toUpperCase();
				try 
				{
					mergeCommand = Enum.valueOf(WadMergeCommand.class, command);
					Response out = mergeCommand.execute(context, scanner);
					if (out == null)
						return true;
					if (out != Response.OK)
					{
						context.logf("ERROR: %s, line %d: Command %s returned %s.\n", streamName, linenum, command, out.name());
						return false;
					}
				}
				catch (NoSuchElementException e)
				{
					context.logf("ERROR: %s, line %d: Command %s is missing an argument.\n", streamName, linenum, command);
					context.logf("    %s ...?\n", line);
					context.logln("");
					mergeCommand.help(System.out);
					return false;
				}
				catch (NumberFormatException e)
				{
					context.logf("ERROR: %s, line %d: Command %s is missing a numeric argument.\n", streamName, linenum, command);
					context.logf("    %s ...?\n", line);
					context.logln("");
					mergeCommand.help(System.out);
					return false;
				}
				catch (IllegalArgumentException e)
				{
					context.logf("ERROR: %s, line %d: Bad command: %s.\n", streamName, linenum, command);
					return false;
				}
			}
			catch (NoSuchElementException e) 
			{
				context.logf("ERROR: %s, line %d: Missing command.\n", streamName, linenum);
				return false;
			}
		}
		
		return true;
	}
	
}

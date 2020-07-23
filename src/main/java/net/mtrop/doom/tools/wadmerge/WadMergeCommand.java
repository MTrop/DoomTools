/*******************************************************************************
 * Copyright (c) 2020 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
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
			out.println("    Creates a new in-memory buffer, errors out if the symbol exists."); 
			out.println("    Buffers are best used for speed, but large merges will consume"); 
			out.println("    lots of memory during merge."); 
			out.println("    [symbol]: The symbol for the new buffer.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			return context.create(scanner.next());
		}
	},
	
	CREATEFILE
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("CREATEFILE [symbol] [path]"); 
			out.println("    Creates a new WAD file (on disk - not in memory), errors out if ");
			out.println("    the symbol exists or the new file could not be created."); 
			out.println("    WARNING: If the file already exists, it is OVERWRITTEN!"); 
			out.println("    Files are best used for memory efficiency, but large merges will"); 
			out.println("    incur lots of overhead as the output file grows."); 
			out.println("    See: CREATE for the in-memory version."); 
			out.println("    [symbol]: The symbol for the new buffer.");
			out.println("    [path]: The file to create.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			String symbol = scanner.next();
			String path = scanner.next();
			try {
				return context.createFile(symbol, new File(path));
			} catch (IOException e) {
				context.logf("ERROR: File %s could not be created.\n", path);
				return Response.BAD_FILE;
			} catch (SecurityException e) {
				context.logf("ERROR: File %s could not be created. Access denied.\n", path);
				return Response.BAD_FILE;
			}
		}
	},
	
	CLEAR
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("CLEAR [symbol]"); 
			out.println("    Clears an existing buffer, errors out if the symbol does not exist."); 
			out.println("    If the symbol is a file, it is deleted and rebuilt."); 
			out.println("    [symbol]: The symbol for the existing buffer to clear.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			String symbol = scanner.next();
			try {
				return context.clear(symbol);
			} catch (IOException e) {
				context.logf("ERROR: Symbol %s could not be cleared (file not closed).\n", symbol);
				return Response.BAD_WAD;
			}
		}
	},
	
	DISCARD
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("DISCARD [symbol]"); 
			out.println("    Discards an existing buffer, errors out if the symbol does not exist."); 
			out.println("    If the symbol is a file, it is closed."); 
			out.println("    [symbol]: The symbol for the existing buffer to discard.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			String symbol = scanner.next();
			try {
				return context.discard(symbol);
			} catch (IOException e) {
				context.logf("ERROR: Symbol %s could not be discarded (file not closed).\n", symbol);
				return Response.BAD_WAD;
			}
		}
	},
	
	SAVE
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("SAVE [symbol] [file]");
			out.println("    Exports the content of a symbol to a WAD file. Directories are created for");
			out.println("    the file, if they don't exist. If the symbol is a WAD file (not buffer)");
			out.println("    and the destination is the same file, nothing happens.");
			out.println("    WARNING: If the target file already exists, it is OVERWRITTEN!"); 
			out.println("    [symbol]: The symbol to export.");
			out.println("    [file]:   The file to create and export to.");
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
			out.println("    Creates a new in-memory buffer by loading an existing WAD file");
			out.println("    into memory. The symbol must not already exist.");
			out.println("    [symbol]: The buffer to create.");
			out.println("    [file]:   The WAD file to read.");
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
			out.println("    Exports the content of a symbol to a WAD file. Directories are created for");
			out.println("    the file, if they don't exist. If the symbol is a WAD file (not buffer) and");
			out.println("    the destination is the same file, nothing happens. The symbol is discarded.");
			out.println("    WARNING: If the target file already exists, it is OVERWRITTEN!"); 
			out.println("    [symbol]: The symbol to export.");
			out.println("    [file]:   The file to create and export to.");
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
			out.println("    [symbol]: The symbol to add to.");
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
			out.println("    [symbol]:    The symbol to add to.");
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
			out.println("    Reads a single map from the source, appending it to the destination.");
			out.println("    [dest-symbol]: The symbol to add to.");
			out.println("    [targetmap]:   The map to add (map header).");
			out.println("    [src-symbol]:  The buffer to read from.");
			out.println("    [sourcemap]:   (Optional) If specified, this is map to read");
			out.println("                   from the source symbol, and the target is the");
			out.println("                   new header name. If not, [targetmap] is read.");
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
			out.println("    Reads a single map from the source WAD, appending it to the destination.");
			out.println("    [symbol]:    The symbol to add to.");
			out.println("    [targetmap]: The map to add (target header).");
			out.println("    [path]:      The source WAD file to read from.");
			out.println("    [sourcemap]: (Optional) If specified, this is map to read");
			out.println("                 from the source WAD, and the target is the");
			out.println("                 new header name. If not, [targetmap] is read.");
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
			out.println("    [symbol]: The symbol to add to.");
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
			out.println("    [symbol]: The symbol to add to.");
			out.println("    [path]:   The file to read.");
			out.println("    [entry]:  (Optional) If specified, the name of the entry to write.");
		}
		
		@Override
		public Response execute(WadMergeContext context, Scanner scanner)
		{
			String symbol = scanner.next();
			File file = new File(scanner.next());
			String textureEntryName = null;
			if (scanner.hasNext())
				textureEntryName = scanner.next();
			else
				textureEntryName = Common.getFileNameWithoutExtension(file);
			
			try {
				return context.mergeDEUTEXTextureFile(symbol, file, false, textureEntryName);
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
			out.println("    [symbol]: The symbol to add to.");
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
				catch (Exception e)
				{
					context.logf("ERROR: %s, line %d: Bad command call: %s. Internal error.\n", streamName, linenum, command);
					context.logf("    Caused by: %s: %s\n", e.getClass().getSimpleName(), e.getLocalizedMessage());
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

/*******************************************************************************
 * Copyright (c) 2020-2021 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.wadmerge;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import net.mtrop.doom.exception.WadException;
import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.common.Response;
import net.mtrop.doom.tools.struct.TokenScanner;
import net.mtrop.doom.tools.struct.TokenScanner.ParseException;

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
		public Response execute(WadMergeContext context, TokenScanner scanner)
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
			out.println("    ................................");
			out.println("    Returns: OK if a symbol was created.");
			out.println("             BAD_SYMBOL if the destination symbol already exists.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			return context.create(scanner.nextString());
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
			out.println("    ................................");
			out.println("    Returns: OK if creation successful and a symbol was created.");
			out.println("             BAD_SYMBOL if the destination symbol is invalid.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String symbol = scanner.nextString();
			String path = scanner.nextString();
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
			out.println("    ................................");
			out.println("    Returns: OK if successful.");
			out.println("             BAD_SYMBOL if the symbol is invalid.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String symbol = scanner.nextString();
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
			out.println("    ................................");
			out.println("    Returns: OK if successful.");
			out.println("             BAD_SYMBOL if the symbol is invalid.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String symbol = scanner.nextString();
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
			out.println("    ................................");
			out.println("    Returns: OK if export successful.");
			out.println("             BAD_SYMBOL if the symbol is invalid.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String symbol = scanner.nextString();
			String file = scanner.nextString();
			
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
				context.logf("ERROR: I/O error on merge: %s\n", e.getLocalizedMessage());
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
			out.println("    ................................");
			out.println("    Returns: OK if successful.");
			out.println("             BAD_FILE if the file does not exist or is a directory.");
			out.println("             BAD_WAD if the file is not a WAD.");
			out.println("             BAD_SYMBOL if the destination symbol is invalid.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String symbol = scanner.nextString();
			String file = scanner.nextString();
			try {
				return context.load(symbol, new File(file));
			} catch (SecurityException e) {
				context.logf("ERROR: File %s not readable (access denied).\n", file);
				return Response.BAD_FILE;
			} catch (IOException e) {
				context.logf("ERROR: I/O error on merge: %s\n", e.getLocalizedMessage());
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
			out.println("    ................................");
			out.println("    Returns: OK if export successful.");
			out.println("             BAD_SYMBOL if the symbol is invalid.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String symbol = scanner.nextString();
			String file = scanner.nextString();
			
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
				context.logf("ERROR: I/O error on merge: %s\n", e.getLocalizedMessage());
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
			out.println("    ................................");
			out.println("    Returns: OK if valid.");
			out.println("             BAD_SYMBOL if the symbol is invalid.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			return context.isValid(scanner.nextString());
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
			out.println("    ................................");
			out.println("    Returns: OK if add successful."); 
			out.println("             BAD_SYMBOL if the destination symbol is invalid.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String symbol = scanner.nextString();
			try {
				return context.addMarker(symbol, scanner.nextString());
			} catch (IOException e) {
				context.logf("ERROR: I/O error on merge: %s\n", e.getLocalizedMessage());
				return Response.BAD_FILE;
			}
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
			out.println("    ................................");
			out.println("    Returns: OK if add successful."); 
			out.println("             BAD_SYMBOL if the destination symbol is invalid.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String symbol = scanner.nextString();
			try {
				return context.addDateMarker(symbol, scanner.nextString());
			} catch (IOException e) {
				context.logf("ERROR: I/O error on merge: %s\n", e.getLocalizedMessage());
				return Response.BAD_FILE;
			}
		}
	},
	
	MERGE
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("MERGE [dest-symbol] [src-symbol]");
			out.println("    Adds all entries from [src-symbol] into [dest-symbol].");
			out.println("    [dest-symbol]: Destination symbol.");
			out.println("    [src-symbol]:  Source symbol.");
			out.println("    ................................");
			out.println("    Returns: OK if merge successful,");
			out.println("             BAD_SYMBOL if the destination symbol is invalid,");
			out.println("             BAD_SOURCE_SYMBOL if the source symbol is invalid.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String symbol = scanner.nextString();
			try {
				return context.merge(symbol, scanner.nextString());
			} catch (IOException e) {
				context.logf("ERROR: I/O error on merge: %s\n", e.getLocalizedMessage());
				return Response.BAD_FILE;
			}
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
			out.println("    ................................");
			out.println("    Returns: OK if merge successful,");
			out.println("             BAD_FILE if the file does not exist or is a directory,"); 
			out.println("             BAD_WAD if the file is not a WAD,");
			out.println("             BAD_SYMBOL if the destination symbol is invalid.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String symbol = scanner.nextString();
			String wadFile = scanner.nextString();
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
				context.logf("ERROR: I/O error on merge: %s\n", e.getLocalizedMessage());
				return Response.BAD_FILE;
			}
		}
	},
	
	MERGENAMESPACE
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("MERGENAMESPACE [dest-symbol] [src-symbol] [namespace]"); 
			out.println("    Adds all entries from [src-symbol] into [dest-symbol] that");
			out.println("    lie between [namespace]_START and  [namespace]_END, excluding");
			out.println("    the START/END namespace markers.");
			out.println("    [dest-symbol]: Destination symbol.");
			out.println("    [src-symbol]:  Source symbol.");
			out.println("    [namespace]:   Namespace name (e.g. FF, PP, TX).");
			out.println("    ................................");
			out.println("    Returns: OK if merge successful,");
			out.println("             BAD_SYMBOL if the destination symbol is invalid,");
			out.println("             BAD_SOURCE_SYMBOL if the source symbol is invalid,");
			out.println("             BAD_NAMESPACE if the namespace could not be found or is incomplete.");
			out.println("             BAD_NAMESPACE_RANGE if the namespace entries are not in sequence.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			try {
				return context.mergeNamespace(scanner.nextString(), scanner.nextString(), scanner.nextString());
			} catch (IOException e) {
				context.logf("ERROR: I/O error on merge: %s\n", e.getLocalizedMessage());
				return Response.BAD_FILE;
			}
		}
	},
	
	MERGENAMESPACEFILE
	{
		@Override
		public void help(PrintStream out)
		{
			out.println("MERGENAMESPACEFILE [symbol] [path] [namespace]"); 
			out.println("    Reads WAD entries from [path] into buffer [symbol] that");
			out.println("    lie between [namespace]_START and  [namespace]_END, excluding");
			out.println("    the START/END namespace markers.");
			out.println("    [symbol]:    The symbol to add to.");
			out.println("    [path]:      The WAD contents to add.");
			out.println("    [namespace]: Namespace name (e.g. FF, PP, TX).");
			out.println("    ................................");
			out.println("    Returns: OK if merge successful,");
			out.println("             BAD_FILE if the file does not exist or is a directory,"); 
			out.println("             BAD_WAD if the file is not a WAD,");
			out.println("             BAD_SYMBOL if the destination symbol is invalid,");
			out.println("             BAD_SOURCE_SYMBOL if the source symbol is invalid,");
			out.println("             BAD_NAMESPACE if the namespace could not be found or is incomplete.");
			out.println("             BAD_NAMESPACE_RANGE if the namespace entries are not in sequence.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String symbol = scanner.nextString();
			File wadFile = new File(scanner.nextString());
			try {
				return context.mergeNamespace(symbol, wadFile, scanner.nextString());
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
				context.logf("ERROR: I/O error on merge: %s\n", e.getLocalizedMessage());
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
			out.println("    ................................");
			out.println("    Returns: OK if merge successful,");
			out.println("             BAD_SYMBOL if the destination symbol is invalid,"); 
			out.println("             BAD_FILE if the provided file does not exist or is a directory.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String symbol = scanner.nextString();
			String file = scanner.nextString();
			String entryName = null;
			if (scanner.hasNext())
				entryName = scanner.nextString();
			
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
				context.logf("ERROR: I/O error on merge: %s\n", e.getLocalizedMessage());
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
			out.println("    ................................");
			out.println("    Returns: OK if merge successful,");
			out.println("             BAD_SYMBOL if the destination symbol is invalid,");
			out.println("             BAD_SOURCE_SYMBOL if the source symbol is invalid,");
			out.println("             BAD_MAP if the map entries are malformed.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String destSymbol = scanner.nextString();
			String map1 = scanner.nextString();
			String srcSymbol = scanner.nextString();
			String map2 = null;
			if (scanner.hasNext())
				map2 = scanner.nextString();
			else
				map2 = map1;
			try {
				return context.mergeMap(destSymbol, map2, srcSymbol, map1);
			} catch (IOException e) {
				context.logf("ERROR: I/O error on merge: %s\n", e.getLocalizedMessage());
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
			out.println("    ................................");
			out.println("    Returns: OK if merge successful,");
			out.println("             BAD_SYMBOL if the destination symbol is invalid,");
			out.println("             BAD_FILE if the file does not exist or is a directory,");
			out.println("             BAD_WAD if the file is not a WAD,");
			out.println("             BAD_MAP if the map entries are malformed.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String symbol = scanner.nextString();
			String map1 = scanner.nextString();
			String wadFile = scanner.nextString();
			String map2 = null;
			if (scanner.hasNext())
				map2 = scanner.nextString();
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
				context.logf("ERROR: I/O error on merge: %s\n", e.getLocalizedMessage());
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
			out.println("    Adds a directory and its subdirectories recursively (files first, then");
			out.println("    directory contents, per directory).");
		    out.println("    For each FILE in [path],"); 
		    out.println("        If FILE is a directory,");
		    out.println("            MARKER [symbol] \\[FILE]");
		    out.println("            MERGEDIR [symbol] [FILE]");
		    out.println("        Else if file is a WAD,");
		    out.println("            MERGEWAD [symbol] [FILE]");
		    out.println("        Else,");
		    out.println("            MERGEFILE [symbol] [FILE]");
			out.println("    [symbol]: The buffer to add to.");
			out.println("    [path]:   The source directory to scan.");
			out.println("    ................................");
			out.println("    Returns: OK if merge successful,");
			out.println("             BAD_SYMBOL if the destination symbol is invalid,"); 
			out.println("             BAD_DIRECTORY if the provided file is not a directory.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String symbol = scanner.nextString();
			String directory = scanner.nextString();
			try {
				return context.mergeTree(symbol, new File(directory));
			} catch (FileNotFoundException e) {
				context.logf("ERROR: File %s not found.\n", directory);
				return Response.BAD_FILE;
			} catch (SecurityException e) {
				context.logf("ERROR: File %s not readable (access denied).\n", directory);
				return Response.BAD_FILE;
			} catch (IOException e) {
				context.logf("ERROR: I/O error on merge: %s\n", e.getLocalizedMessage());
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
			out.println("    ................................");
			out.println("    Returns: OK if the file was found and contents were merged in,"); 
			out.println("             BAD_SYMBOL if the destination symbol is invalid,");
			out.println("             BAD_DIRECTORY if the provided file is not a directory,");
			out.println("             BAD_PARSE if the input file had a parse error.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String symbol = scanner.nextString();
			String file = scanner.nextString();
			try {
				return context.mergeSwitchAnimatedTables(symbol, new File(file));
			} catch (FileNotFoundException e) {
				context.logf("ERROR: File %s not found.\n", file);
				return Response.BAD_FILE;
			} catch (SecurityException e) {
				context.logf("ERROR: File %s not readable (access denied).\n", file);
				return Response.BAD_FILE;
			} catch (IOException e) {
				context.logf("ERROR: I/O error on merge: %s\n", e.getLocalizedMessage());
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
			out.println("    ................................");
			out.println("    Returns: OK if the file was found and contents were merged in,"); 
			out.println("             BAD_SYMBOL if the destination symbol is invalid,");
			out.println("             BAD_PARSE if the input file had a parse error,");
			out.println("             BAD_FILE if the file does not exist or is a directory.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String symbol = scanner.nextString();
			File file = new File(scanner.nextString());
			String textureEntryName = null;
			if (scanner.hasNext())
				textureEntryName = scanner.nextString();
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
				context.logf("ERROR: I/O error on merge: %s\n", e.getLocalizedMessage());
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
			out.println("    ................................");
			out.println("    Returns: OK if the file was found and contents were merged in,");
			out.println("             BAD_SYMBOL if the destination symbol is invalid,");
			out.println("             BAD_DIRECTORY if the provided file is not a directory.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String symbol = scanner.nextString();
			String file = scanner.nextString();
			String textureEntryName = scanner.nextString();
			
			try {
				return context.mergeTextureDirectory(symbol, new File(file), textureEntryName);
			} catch (FileNotFoundException e) {
				context.logf("ERROR: File %s not found.\n", file);
				return Response.BAD_FILE;
			} catch (SecurityException e) {
				context.logf("ERROR: File %s not readable (access denied).\n", file);
				return Response.BAD_FILE;
			} catch (IOException e) {
				context.logf("ERROR: I/O error on merge: %s\n", e.getLocalizedMessage());
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
	public abstract Response execute(WadMergeContext context, TokenScanner scanner);
	
	/**
	 * Executes the provided script.
	 * @param streamName stream name.
	 * @param reader the reader to read the script from.
	 * @param context the WAD merge context.
	 * @return true if no errors, false otherwise.
	 * @throws IOException if the script can't be read.
	 */
	public static boolean callScript(String streamName, BufferedReader reader, WadMergeContext context) throws IOException
	{
		String line;
		int linenum = 0;
		
		while ((line = reader.readLine()) != null)
		{
			linenum++;
			line = line.trim();
			if (line.isEmpty() || line.startsWith("#"))
				continue;
			WadMergeCommand mergeCommand = null;
			try (TokenScanner scanner = new TokenScanner(line)) 
			{
				String command = scanner.nextString();
				try 
				{
					mergeCommand = WadMergeCommand.VALUES.get(command);
					if (mergeCommand == null)
					{
						context.logf("ERROR: %s, line %d: Unknown command: \"%s\".\n", streamName, linenum, command);
						return false;
					}
					
					Response out = mergeCommand.execute(context, scanner);
					if (out == null)
						return true;
					if (out != Response.OK)
					{
						context.logf("ERROR: %s, line %d: Command %s returned %s.\n", streamName, linenum, command, out.name());
						return false;
					}
				}
				catch (ParseException e)
				{
					context.logf("ERROR: %s, line %d: An argument in command %s could not be parsed: %s\n", streamName, linenum, command, e.getLocalizedMessage());
					context.logf("    %s\n", line);
					return false;
				}
				catch (NoSuchElementException e)
				{
					context.logf("ERROR: %s, line %d: Command %s is missing an argument.\n", streamName, linenum, command);
					context.logf("    %s ...?\n", line);
					context.logln();
					mergeCommand.help(System.out);
					return false;
				}
				catch (NumberFormatException e)
				{
					context.logf("ERROR: %s, line %d: Command %s requires a numeric argument: %s\n", streamName, linenum, command, e.getLocalizedMessage());
					context.logf("    %s\n", line);
					context.logln();
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
	
	/** Value map for command name to command. */
	public static final Map<String, WadMergeCommand> VALUES = new TreeMap<String, WadMergeCommand>(String.CASE_INSENSITIVE_ORDER)
	{
		private static final long serialVersionUID = -9083149204025118660L;
		{
			for (WadMergeCommand command : WadMergeCommand.values())
			{
				put(command.name(), command);
			}
		}
	};
	
}

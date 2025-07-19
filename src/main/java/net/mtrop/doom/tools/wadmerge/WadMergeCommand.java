/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
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

import net.mtrop.doom.exception.WadException;
import net.mtrop.doom.tools.common.Response;
import net.mtrop.doom.tools.struct.ArgumentScanner;
import net.mtrop.doom.tools.struct.TokenScanner;
import net.mtrop.doom.tools.struct.TokenScanner.ParseException;
import net.mtrop.doom.tools.struct.util.EnumUtils;
import net.mtrop.doom.tools.struct.util.FileUtils;

/**
 * The Wad Merge commands.
 * @author Matthew Tropiano
 */
public enum WadMergeCommand
{
	END
	{
		@Override
		public String usage() 
		{
			return "END";
		}
		
		@Override
		public void help(PrintStream out)
		{
			out.println(usage()); 
			out.println("    Ends script read.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			// Kills the script.
			return null;
		}
	},
	
	ECHO
	{
		@Override
		public String usage()
		{
			return "ECHO [...]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage()); 
			out.println("    Prints tokens to output."); 
			out.println("    [...]: The tokens to print.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			StringBuilder sb = new StringBuilder();
			while (scanner.hasNext())
			{
				if (sb.length() > 0)
					sb.append(' ');
				sb.append(scanner.nextString());
			}
			context.logln(sb.toString());
			return Response.OK;
		}
	},
	
	CREATE
	{
		@Override
		public String usage()
		{
			return "CREATE [symbol] [opt:iwad]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage()); 
			out.println("    Creates a new in-memory buffer, errors out if the symbol exists."); 
			out.println("    Buffers are best used for speed, but large merges will consume"); 
			out.println("    lots of memory during merge."); 
			out.println("    [symbol]: The symbol for the new buffer.");
			out.println("    [iwad]:   (Optional) If \"iwad\", the created WAD buffer is an IWAD.");
			out.println("    ................................");
			out.println("    Returns: OK if a symbol was created.");
			out.println("             BAD_SYMBOL if the destination symbol already exists.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String symbol = scanner.nextString();
			boolean iwad = false;
			if (scanner.hasNext())
				iwad = scanner.nextString().equalsIgnoreCase("iwad");
			return context.create(symbol, iwad, 8 * 1024 * 1024, 0);
		}
	},
	
	CREATEBUFFER
	{
		@Override
		public String usage()
		{
			return "CREATEBUFFER [symbol] [capacity] [opt:increment] [opt:iwad]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage()); 
			out.println("    Creates a new in-memory buffer, errors out if the symbol exists."); 
			out.println("    Buffers are best used for speed, but large merges will consume"); 
			out.println("    lots of memory during merge. This differs from CREATE such that");
			out.println("    you can control the initial capacity or incremental capacity of");
			out.println("    the new buffer."); 
			out.println("    [symbol]:    The symbol for the new buffer.");
			out.println("    [capacity]:  The buffer capacity in bytes.");
			out.println("    [increment]: (Optional) The buffer increment (if more bytes are needed)");
			out.println("                 in bytes. 0 or less doubles the buffer size.");
			out.println("    [iwad]:      (Optional) If \"iwad\", the created WAD buffer is an IWAD.");
			out.println("    ................................");
			out.println("    Returns: OK if a symbol was created.");
			out.println("             BAD_SYMBOL if the destination symbol already exists.");
			out.println("             BAD_SIZE if the capacity or increment is an illegal value.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String symbol = scanner.nextString();
			int capacity = scanner.nextInt();

			boolean iwad = false;
			int capacityIncrement = 0; // double
			
			if (scanner.hasNext())
				capacityIncrement = scanner.nextInt();
			if (scanner.hasNext())
				iwad = scanner.nextString().equalsIgnoreCase("iwad");
			return context.create(symbol, iwad, capacity, capacityIncrement);
		}
	},
	
	CREATEFILE
	{
		@Override
		public String usage()
		{
			return "CREATEFILE [symbol] [path] [opt:iwad]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage()); 
			out.println("    Creates a new WAD file (on disk - not in memory), errors out if ");
			out.println("    the symbol exists or the new file could not be created."); 
			out.println("    WARNING: If the file already exists, it is OVERWRITTEN!"); 
			out.println("    Files are best used for memory efficiency, but large merges will"); 
			out.println("    incur lots of overhead as the output file grows."); 
			out.println("    See: CREATE for the in-memory version."); 
			out.println("    [symbol]: The symbol for the new buffer.");
			out.println("    [path]:   The file to create.");
			out.println("    [iwad]:   (Optional) If \"iwad\", the created WAD file is an IWAD.");
			out.println("    ................................");
			out.println("    Returns: OK if creation successful and a symbol was created.");
			out.println("             BAD_SYMBOL if the destination symbol is invalid.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String symbol = scanner.nextString();
			String path = scanner.nextString();
			boolean iwad = false;
			if (scanner.hasNext())
				iwad = scanner.nextString().equalsIgnoreCase("iwad");
			try {
				return context.createFile(symbol, new File(path), iwad);
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
		public String usage() 
		{
			return "CLEAR [symbol]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage()); 
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
		public String usage() 
		{
			return "DISCARD [symbol]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage()); 
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
		public String usage()
		{
			return "SAVE [symbol] [file]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage());
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
		public String usage() 
		{
			return "LOAD [symbol] [file]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage());
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
		public String usage()
		{
			return "FINISH [symbol] [file]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage());
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
		public String usage()
		{
			return "VALID [symbol]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage()); 
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
		public String usage() 
		{
			return "MARKER [symbol] [name]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage()); 
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
		public String usage()
		{
			return "DATEMARKER [symbol] [name]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage()); 
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
		public String usage()
		{
			return "MERGE [dest-symbol] [src-symbol]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage());
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
		public String usage() 
		{
			return "MERGEWAD [symbol] [path]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage()); 
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
		public String usage() 
		{
			return "MERGENAMESPACE [dest-symbol] [src-symbol] [namespace] [opt:amend]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage()); 
			out.println("    Adds all entries from [src-symbol] into [dest-symbol] that");
			out.println("    lie between [namespace]_START and  [namespace]_END, excluding");
			out.println("    the START/END namespace markers.");
			out.println("    [dest-symbol]: Destination symbol.");
			out.println("    [src-symbol]:  Source symbol.");
			out.println("    [namespace]:   Namespace name (e.g. FF, PP, TX).");
			out.println("    [amend]:       (Optional) If \"amend\", find the namespace in the");
			out.println("                   destination and append to its end. If the namespace does not");
			out.println("                   exist in the destination, it is created.");
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
			String destinationSymbol = scanner.nextString(); 
			String sourceSymbol = scanner.nextString();
			String namespace = scanner.nextString();
			boolean amendNamespace = false;
			if (scanner.hasNext())
				amendNamespace = scanner.nextBoolean("amend");
			try {
				return context.mergeNamespace(destinationSymbol, sourceSymbol, namespace, amendNamespace);
			} catch (IOException e) {
				context.logf("ERROR: I/O error on merge: %s\n", e.getLocalizedMessage());
				return Response.BAD_FILE;
			}
		}
	},
	
	MERGENAMESPACEFILE
	{
		@Override
		public String usage()
		{
			return "MERGENAMESPACEFILE [symbol] [path] [namespace] [opt:amend]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage()); 
			out.println("    Reads WAD entries from [path] into buffer [symbol] that");
			out.println("    lie between [namespace]_START and  [namespace]_END, excluding");
			out.println("    the START/END namespace markers.");
			out.println("    [symbol]:    The symbol to add to.");
			out.println("    [path]:      The WAD contents to add.");
			out.println("    [namespace]: Namespace name (e.g. FF, PP, TX).");
			out.println("    [amend]:     (Optional) If \"amend\", find the namespace in the");
			out.println("                 destination and append to its end. If the namespace does not");
			out.println("                 exist in the destination, it is created.");
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
			String namespace = scanner.nextString();
			boolean amendNamespace = false;
			if (scanner.hasNext())
				amendNamespace = scanner.nextBoolean("amend");
			try {
				return context.mergeNamespace(symbol, wadFile, namespace, amendNamespace);
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
		public String usage()
		{
			return "MERGEFILE [symbol] [path] [opt:entryname]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage()); 
			out.println("    Reads file from [path] into [symbol].");
			out.println("    NOTE: Specifying a target entry name will override any replacement");
			out.println("    behavior set by FILECHARSUB.");
			out.println("    [symbol]:    The symbol to add to.");
			out.println("    [path]:      The file to add.");
			out.println("    [entryname]: (Optional) If specified, this is the name to use as");
			out.println("                 the imported entry.");
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
				return context.mergeFile(symbol, f, entryName == null ? context.subCharString(FileUtils.getFileNameWithoutExtension(f)) : entryName);
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
		public String usage() 
		{
			return "MERGEMAP [dest-symbol] [targetmap] [src-symbol] [opt:sourcemap]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage()); 
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
		public String usage()
		{
			return "MERGEMAPFILE [symbol] [targetmap] [path] [opt:sourcemap]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage()); 
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
		public String usage() 
		{
			return "MERGEDIR [symbol] [path] [opt:nomarkers]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage());
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
			out.println("    [symbol]:    The buffer to add to.");
			out.println("    [path]:      The source directory to scan.");
			out.println("    [nomarkers]: (Optional) If \"nomarkers\", omit the directory markers.");
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
			boolean omitMarkers = false;
			if (scanner.hasNext())
				omitMarkers = scanner.nextBoolean("nomarkers");
			try {
				return context.mergeTree(symbol, new File(directory), (file)->true, omitMarkers);
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
	
	MERGEWADDIR
	{
		@Override
		public String usage() 
		{
			return "MERGEWADDIR [symbol] [path]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage());
			out.println("    Adds a directory and its subdirectories recursively (files first, then");
			out.println("    directory contents, per directory), but only the WAD files (by type and");
			out.println("    extension).");
			out.println("    For each FILE in [path],"); 
			out.println("        If FILE is a directory,");
			out.println("            MERGEDIR [symbol] [FILE]");
			out.println("        Else if file is a WAD,");
			out.println("            MERGEWAD [symbol] [FILE]");
			out.println("        Else,");
			out.println("            Skip file.");
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
				return context.mergeTree(symbol, new File(directory), (file)->file.getName().toLowerCase().endsWith(".wad"), true);
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
	
	MERGEENTRY
	{
		@Override
		public String usage() 
		{
			return "MERGEENTRY [dest-symbol] [entry] [src-symbol] [opt:src-entry]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage());
			out.println("    Adds an entry from [src-symbol] to [dest-symbol].");
			out.println("    The entry copied is first entry found by the provided name.");
			out.println("    [dest-symbol]: The destination buffer.");
			out.println("    [entry]:       The entry to add.");
			out.println("    [src-symbol]:  The source buffer.");
			out.println("    [src-entry]:   (Optional) If specified, [src-entry] is the entry to read");
			out.println("                   from [src-symbol], and [entry] is the new name for the");
			out.println("                   entry.");
			out.println("    ................................");
			out.println("    Returns: OK if merge successful,");
			out.println("             BAD_SYMBOL if the destination symbol is invalid,"); 
			out.println("             BAD_SOURCE_SYMBOL if the source symbol is invalid,");
			out.println("             BAD_ENTRY if the entry could not be found.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String destSymbol = scanner.nextString();
			String entry = scanner.nextString();
			String srcSymbol = scanner.nextString();
			
			String srcEntry;
			if (scanner.hasNext())
				srcEntry = scanner.nextString();
			else
				srcEntry = entry;

			try {
				return context.mergeEntry(destSymbol, entry, srcSymbol, srcEntry);
			} catch (IOException e) {
				context.logf("ERROR: I/O error on merge: %s\n", e.getLocalizedMessage());
				return Response.BAD_FILE;
			}
		}
	},
	
	MERGEENTRYFILE
	{
		@Override
		public String usage() 
		{
			return "MERGEENTRYFILE [symbol] [entry] [path] [opt:src-entry]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage());
			out.println("    Adds an entry from a source WAD to [symbol].");
			out.println("    The entry copied is first entry found by the provided name.");
			out.println("    [symbol]:    The destination buffer.");
			out.println("    [entry]:     The entry to add.");
			out.println("    [path]:      The source buffer.");
			out.println("    [src-entry]: (Optional) If specified, [src-entry] is the entry to read");
			out.println("                 from [src-symbol], and [entry] is the new name for the");
			out.println("                 entry.");
			out.println("    ................................");
			out.println("    Returns: OK if merge successful,");
			out.println("             BAD_SYMBOL if the destination symbol is invalid,"); 
			out.println("             BAD_SOURCE_SYMBOL if the source symbol is invalid,");
			out.println("             BAD_ENTRY if the entry could not be found,");
			out.println("             BAD_FILE if the file does not exist or is a directory,");
			out.println("             BAD_WAD if the file is not a WAD.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String symbol = scanner.nextString();
			String entry = scanner.nextString();
			String path = scanner.nextString();
			
			String srcEntry;
			if (scanner.hasNext())
				srcEntry = scanner.nextString();
			else
				srcEntry = entry;

			try {
				return context.mergeEntry(symbol, entry, new File(path), srcEntry);
			} catch (IOException e) {
				context.logf("ERROR: I/O error on merge: %s\n", e.getLocalizedMessage());
				return Response.BAD_FILE;
			}
		}
	},
	
	MERGESWANTBLS
	{
		@Override
		public String usage() 
		{
			return "MERGESWANTBLS [symbol] [path]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage());
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
		public String usage()
		{
			return "MERGEDEUTEXFILE [symbol] [path] [opt:entry] [opt:strife]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage());
			out.println("    Reads file from [path], interprets it as a DEUTeX texture/patch assembly");
			out.println("    file, creates/amends TEXTUREx/PNAMES. The name of the file is the name of");
			out.println("    the texture lump.");
			out.println("    [symbol]: The symbol to add to.");
			out.println("    [path]:   The file to read.");
			out.println("    [entry]:  (Optional) If specified, the name of the entry to write.");
			out.println("    [strife]: (Optional) If \"strife\", the texture entry is read and/or");
			out.println("              written as a Strife-formatted texture set.");
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
			boolean strife = false;
			if (scanner.hasNext())
			{
				textureEntryName = scanner.nextString();
				if (scanner.hasNext())
					strife = scanner.nextBoolean("strife");
			}
			else
			{
				textureEntryName = FileUtils.getFileNameWithoutExtension(file);
			}

			try {
				return context.mergeDEUTEXTextureFile(symbol, file, strife, textureEntryName);
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
		public String usage()
		{
			return "MERGETEXTUREDIR [symbol] [path] [entry] [opt:strife]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage());
			out.println("    Imports a directory's files as Doom Patches (or PNGs) and adds them");
			out.println("    to either a new or already-existing P or PP namespace and texture set entry");
			out.println("    (plus PNAMES).");
			out.println("    [symbol]: The symbol to add to.");
			out.println("    [path]:   The directory to read.");
			out.println("    [entry]:  The name of the texture entry to write/append to.");
			out.println("    [strife]: (Optional) If \"strife\", the texture entry is read and/or");
			out.println("              written as a Strife-formatted texture set.");
			out.println("    ................................");
			out.println("    Returns: OK if the file was found and contents were merged in,");
			out.println("             BAD_FILE if a file could not be read,");
			out.println("             BAD_SYMBOL if the destination symbol is invalid,");
			out.println("             BAD_DIRECTORY if the provided file is not a directory.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String symbol = scanner.nextString();
			String file = scanner.nextString();
			String textureEntryName = scanner.nextString();

			boolean strife = false;
			if (scanner.hasNext())
				strife = scanner.nextBoolean("strife");
			
			try {
				return context.mergeTextureDirectory(symbol, new File(file), strife, textureEntryName);
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
	
	FILECHARSUB
	{
		@Override
		public String usage()
		{
			return "FILECHARSUB [char] [replacement]";
		}

		@Override
		public void help(PrintStream out)
		{
			out.println(usage());
			out.println("    Sets a character replacement mapping (from this command onward) for");
			out.println("    auto-merged lumps that come from file names. The intended use for this is");
			out.println("    for renaming files to lump names that may have characters that can't be");
			out.println("    used in file names in your filesystem.");
			out.println("    For example: \"VILE^1.lmp\" will import as \"VILE\\1\" if the following");
			out.println("    was set:");
			out.println("        FILECHARSUB ^ \\");
			out.println("    [char]:        The character to replace (first character is used).");
			out.println("    [replacement]: The replacement character (first character is used).");
			out.println("    ................................");
			out.println("    Returns: OK.");
		}
		
		@Override
		public Response execute(WadMergeContext context, TokenScanner scanner)
		{
			String src = scanner.nextString();
			String dest = scanner.nextString();
			context.addCharSubstitution(src.charAt(0), dest.charAt(0));
			return Response.OK;
		}
	},
	
	;
	
	/**
	 * Prints help.
	 * @param out the output stream for messages.
	 */
	public abstract void help(PrintStream out);
	
	/**
	 * @return the usage line.
	 */
	public abstract String usage();
	
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
	 * @param arguments the WadMerge arguments.
	 * @return true if no errors, false otherwise.
	 * @throws IOException if the script can't be read.
	 */
	public static boolean callScript(String streamName, BufferedReader reader, WadMergeContext context, String[] arguments) throws IOException
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
			try (TokenScanner scanner = new ArgumentScanner(arguments, line)) 
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
	public static final Map<String, WadMergeCommand> VALUES = EnumUtils.createCaseInsensitiveNameMap(WadMergeCommand.class);
	
}

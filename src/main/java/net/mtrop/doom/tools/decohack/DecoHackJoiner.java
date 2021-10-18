package net.mtrop.doom.tools.decohack;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import net.mtrop.doom.tools.struct.TokenScanner;

/**
 * The joiner that exports a full DECOHack tree.
 * @author Matthew Tropiano
 */
public final class DecoHackJoiner 
{
	private static final boolean IS_WINDOWS;
	private static final String INCLUDE_LINE = "#include";

	static
	{
		IS_WINDOWS = System.getProperty("os.name").contains("Windows");
	}

	/**
	 * Reads the source from a file.
	 * @param sourceFile the source file.
	 * @param charset the file charset.
	 * @param writer the output writer.
	 * @throws IOException if a read or write error occurs.
	 */
	public static void joinSourceFrom(File sourceFile, Charset charset, PrintWriter writer) throws IOException
	{
		try (InputStream input = new FileInputStream(sourceFile))
		{
			joinSourceFrom(input, sourceFile.getPath(), charset, writer);
		}
	}
	
	/**
	 * Reads the source from an input stream.
	 * The input stream is not closed.
	 * @param input the input stream.
	 * @param path the stream path.
	 * @param charset the stream charset.
	 * @param writer the output writer.
	 * @throws IOException if a read or write error occurs.
	 */
	public static void joinSourceFrom(InputStream input, String path, Charset charset, PrintWriter writer) throws IOException
	{
		includeStream(path, charset, new BufferedReader(new InputStreamReader(input, charset)), writer);
	}
	
	/**
	 * Includes a stream and outputs it through a writer.
	 * @param streamName the name of the stream.
	 * @param reader the input reader.
	 * @param writer the output writer.
	 * @throws IOException if a read or write error occurs.
	 */
	private static void includeStream(String streamName, Charset charset, BufferedReader reader, PrintWriter writer) throws IOException
	{
		String line;
		while ((line = reader.readLine()) != null)
		{
			String trimmedLine = line.trim();
			if (trimmedLine.startsWith(INCLUDE_LINE))
			{
				InputStream in = null;
				String nextPath;
				try (TokenScanner scanner = new TokenScanner(trimmedLine.substring(INCLUDE_LINE.length())))
				{
					String path = scanner.nextToken();
					nextPath = getNextPath(streamName, path);
					File f = new File(nextPath);
					if (f.exists())
						in = new FileInputStream(f.getPath());
				}
				
				if (in != null)
				{
					try (BufferedReader nextReader = openReader(in, charset))
					{
						includeStream(nextPath, charset, nextReader, writer);
					}
				}
				else
				{
					writer.println(line);
				}
			}
			else
			{
				writer.println(line);
			}
		}
		writer.println();
	}

	/**
	 * Resolves a stream path.
	 * @param sourceFile the source file.
	 * @param charset the stream charset.
	 * @return an open BufferedReader.
	 * @throws IOException if a read or write error occurs.
	 */
	private static BufferedReader openReader(InputStream in, Charset charset) throws IOException
	{
		return new BufferedReader(new InputStreamReader(in, charset));
	}

	private static String getNextPath(String streamName, String path)
	{
		if (IS_WINDOWS && streamName.contains("\\")) // check for Windows paths.
			streamName = streamName.replace('\\', '/');
		return getParentPath(streamName) + path;
	}

	private static String getParentPath(String streamName)
	{
		int lidx = -1; 
		if ((lidx = streamName.lastIndexOf('/')) >= 0)
			return streamName.substring(0, lidx + 1);
		return "";
	}
	
}

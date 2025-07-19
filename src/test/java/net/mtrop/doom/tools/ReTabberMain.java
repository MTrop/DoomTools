package net.mtrop.doom.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.Charset;

public final class ReTabberMain
{
	public static final String NEWLINE = System.getProperty("os.name").contains("Windows") ? "\r\n" : "\n";
	public static final int SPACE_AMOUNT = 4;
	
	public static void main(String[] args)
	{
		respaceDirectory(new File("src/main/java/" + ReTabberMain.class.getPackage().getName().replace('.', '/')));
	}
	
	private static void respaceDirectory(File directory)
	{
		for (File javaFile : directory.listFiles((f) -> f.isDirectory() || getFileExtension(f.getName(), ".").equalsIgnoreCase("java")))
		{
			if (javaFile.isDirectory())
			{
				respaceDirectory(javaFile);
			}
			else
			{
				rebuildFile(respaceFile(javaFile), javaFile);
				System.out.println("Re-tabbed " + javaFile.getPath() + "...");
			}
		}
	}
	
	private static String respaceFile(File f)
	{
		StringBuilder sb = new StringBuilder();
		
		try (BufferedReader br = openTextFile(f))
		{
			String line;
			while ((line = br.readLine()) != null)
			{
				sb.append(respaceLine(line)).append(NEWLINE);
			}
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		return sb.toString();
	}

	private static String respaceLine(String line)
	{
		char c;
		int i = 0;
		int spaces = 0;
		char[] lineChars = line.toCharArray();
		StringBuilder sb = new StringBuilder();
		
		while (i < lineChars.length)
		{
			c = lineChars[i];
			
			if (Character.isWhitespace(c))
			{
				if (c == '\t')
					spaces = retabAmount(spaces, SPACE_AMOUNT);
				else
					spaces++;
			}
			else
			{
				break;
			}
			i++;
		}
		
		while (spaces >= SPACE_AMOUNT)
		{
			sb.append('\t');
			spaces -= SPACE_AMOUNT;
		}

		while (spaces-- > 0)
			sb.append(' ');

		while (i < lineChars.length)
			sb.append(lineChars[i++]);
		
		return sb.toString();
	}
	
	private static void rebuildFile(String s, File out)
	{
		try (BufferedReader br = new BufferedReader(new StringReader(s)))
		{
			try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(out))))
			{
				String line;
				while ((line = br.readLine()) != null)
				{
					bw.append(line).append(NEWLINE);
				}
			}
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	private static int retabAmount(int count, int tabAmount)
	{
		return (count + tabAmount) / tabAmount * tabAmount;
	}
	
	/**
	 * Returns the extension of a filename.
	 * @param filename the file name.
	 * @param extensionSeparator the text or characters that separates file name from extension.
	 * @return the file's extension, or an empty string for no extension.
	 */
	public static String getFileExtension(String filename, String extensionSeparator)
	{
		int extindex = filename.lastIndexOf(extensionSeparator);
		if (extindex >= 0)
			return filename.substring(extindex+1);
		return "";
	}

	/**
	 * Convenience method for
	 * <code>new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.defaultCharset()))</code>
	 * @param file the file to open.
	 * @return an open buffered reader for the provided file.
	 * @throws IOException if an error occurred opening the file for reading.
	 * @throws SecurityException if you do not have permission for opening the file.
	 */
	public static BufferedReader openTextFile(File file) throws IOException
	{
		return openTextStream(new FileInputStream(file), Charset.defaultCharset());
	}

	/**
	 * Convenience method for
	 * <code>new BufferedReader(new InputStreamReader(in, encoding))</code>
	 * @param in the stream to read.
	 * @param encoding the text encoding to use.
	 * @return an open buffered reader for the provided stream.
	 * @throws IOException if an error occurred opening the stream for reading.
	 * @throws SecurityException if you do not have permission for opening the stream.
	 */
	public static BufferedReader openTextStream(InputStream in, Charset encoding) throws IOException
	{
		return new BufferedReader(new InputStreamReader(in, encoding));
	}

}

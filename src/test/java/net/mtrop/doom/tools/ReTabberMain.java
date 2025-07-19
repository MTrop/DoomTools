package net.mtrop.doom.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;

import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.IOUtils;
import net.mtrop.doom.tools.struct.util.OSUtils;

public final class ReTabberMain
{
	public static final String NEWLINE = OSUtils.isWindows() ? "\r\n" : "\n";
	public static final int SPACE_AMOUNT = 4;
	
	public static void main(String[] args)
	{
		respaceDirectory(new File("src/main/java/" + ReTabberMain.class.getPackage().getName().replace('.', '/')));
	}
	
	private static void respaceDirectory(File directory)
	{
		for (File javaFile : directory.listFiles((f) -> f.isDirectory() || FileUtils.getFileExtension(f).equalsIgnoreCase("java")))
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
		
		try (BufferedReader br = IOUtils.openTextFile(f))
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
					sb.append('\t');
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
	
}

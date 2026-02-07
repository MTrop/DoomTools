/*******************************************************************************
 * Copyright (c) 2020-2026 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.struct.util;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/**
 * Simple utility functions around files.
 * @author Matthew Tropiano
 */
public final class FileUtils
{
	private FileUtils() {}

	/**
	 * The null file stream.
	 */
	public static final File NULL_FILE = new File(System.getProperty("os.name").startsWith("Windows") ? "NUL" : "/dev/null");
	
	private static final String TEMP_FILE_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

	private static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"));
	
	private static final Comparator<File> FILELIST_COMPARATOR;
	private static final Comparator<File> FILE_COMPARATOR;

	static
	{
		final Comparator<File> fileNameComparator = System.getProperty("os.name").contains("Windows")
				? (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getPath(), b.getPath())
				: (a, b) -> a.getPath().compareTo(b.getPath())
		;
		
		FILELIST_COMPARATOR = (a, b) -> (
			a.isDirectory() 
				? (b.isDirectory() ? fileNameComparator.compare(a, b) : -1)
				: (b.isDirectory() ? 1 : fileNameComparator.compare(a, b))
		);
		
		FILE_COMPARATOR = (a, b) -> fileNameComparator.compare(a, b);
	}
	

	/**
	 * Creates a blank file or updates its last modified date.
	 * @param filePath	the abstract path to use.
	 * @return true if the file was made/updated, false otherwise.
	 * @throws IOException if creating/modifying the file violates something.
	 */
	public static boolean touch(String filePath) throws IOException
	{
		return touch(new File(filePath));
	}

	/**
	 * Creates a blank file or updates its last modified date.
	 * @param filePath	the abstract path to use.
	 * @return true if the file was made/updated, false otherwise.
	 * @throws IOException if creating/modifying the file violates something.
	 */
	public static boolean touch(File filePath) throws IOException
	{
		RandomAccessFile file = new RandomAccessFile(filePath, "rw");
		file.setLength(file.length()); // should make a mock write without altering anything.
		file.close();
		return true;
	}

	/**
	 * Securely deletes a file. Overwrites its data with zeroes and its filename
	 * a bunch of times before it finally deletes it. Places an exclusive
	 * lock on the file as it is getting deleted. The length of time taken
	 * depends the file's length and the efficiency of the medium that contains it.
	 * <p>
	 * <b>DISCLAIMER:</b> Black Rook Software makes NO CLAIMS of COMPLETE SECURITY
	 * concerning deletions of files using this method. This method is provided AS-IS.
	 * It is reasonably secure insofar as deleting file content and headers as Java
	 * may allow. This does not alter dates of files.
	 * @param file the file to delete.
	 * @throws FileNotFoundException if the file does not exist.
	 * @throws IOException if the delete cannot be completed.
	 */
	public static void secureDelete(File file) throws IOException
	{
		secureDelete(file, 1);
	}

	/**
	 * Securely deletes a file. Overwrites its data and its filename
	 * a bunch of times before it finally deletes it. Places an exclusive
	 * lock on the file as it is getting deleted. The length of time taken
	 * depends the file's length and the efficiency of the medium that contains it. 
	 * <p>
	 * <b>DISCLAIMER:</b> Black Rook Software makes NO CLAIMS of COMPLETE SECURITY
	 * concerning deletions of files using this method. This method is provided AS-IS.
	 * It is reasonably secure insofar as deleting file content and headers as Java
	 * may allow. This does not alter dates of files directly.
	 * @param file the file to delete.
	 * @param passes the amount of passes for overwriting this file.
	 * The last pass is always zero-filled. If less than 1, it is 1.
	 * @return true if the file was deleted, false otherwise.
	 * @throws FileNotFoundException if the file does not exist.
	 * @throws IOException if the delete cannot be completed.
	 */
	public static boolean secureDelete(File file, int passes) throws IOException
	{
		passes = passes < 1 ? 1 : passes;
		boolean bitval = passes == 1 ? false : (passes % 2) == 0;
	
		// Overwrite.
		try (RandomAccessFile raf = new RandomAccessFile(file, "rws"); FileLock lock = raf.getChannel().lock()) 
		{
			byte[] buffer = new byte[65536];
			while (passes-- > 0)
			{
				Arrays.fill(buffer, (byte)(bitval ? 0xFF : 0x00));
				long end = raf.length();
				raf.seek(0L);
				long n = 0L;
				while (n < end)
				{
					raf.write(buffer, 0, Math.min(buffer.length, (int)(end - n)));
					n = raf.getFilePointer();
				}
			}
		}
		
		// Overwrite filename.
		String newName = null;
		char[] namebuf = new char[file.getName().length()];
		String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ9876543210";
		for (int i = 0; i < alphabet.length(); i++)
		{
			Arrays.fill(namebuf, alphabet.charAt(i));
			newName = new String(namebuf);
			File ren = new File(file.getParent() + File.separator + newName);
			file.renameTo(ren);
			file = ren;
		}
		
		return file.delete();
	}

	/**
	 * Creates a file filled with random data with the specified length.
	 * If <code>file</code> refers to an existing file, it will be OVERWRITTEN.
	 * @param file the file to write.
	 * @param random the random number generator to use.
	 * @param length the length of the file in bytes.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void createJunkFile(File file, Random random, int length) throws IOException
	{
		byte[] buffer = new byte[65536];
		int n = 0;
		
		FileOutputStream fos = new FileOutputStream(file);
		while (n < length)
		{
			random.nextBytes(buffer);
			int len = Math.min(buffer.length, length - n);
			fos.write(buffer, 0, len);
			fos.flush();
			n += len;
		}
		fos.close();
	}

	/**
	 * Creates a temporary file for whatever purpose.
	 * The returned object can be autoclosed via try-with-resources or some other method, which will
	 * attempt to delete it from storage.
	 * @return the file created.
	 */
	public static TempFile createTempFile()
	{
		Random random = new Random();
		char[] out = new char[32];
		for (int i = 0; i < out.length; i++)
			out[i] = TEMP_FILE_ALPHABET.charAt(random.nextInt(TEMP_FILE_ALPHABET.length()));
		return new TempFile(canonizeFile(TEMP_DIR) + File.separator + (new String(out)) + ".tmp");
	}

	/**
	 * Creates the necessary directories for a file path.
	 * @param file	the abstract file path.
	 * @return		true if the paths were made (or exists), false otherwise.
	 */
	public static boolean createPathForFile(File file)
	{
		return createPathForFile(file.getAbsolutePath());
	}

	/**
	 * Creates the necessary directories for a file path.
	 * @param path	the abstract path.
	 * @return		true if the paths were made (or exists), false otherwise.
	 */
	public static boolean createPathForFile(String path)
	{
		int sindx = -1;
		
		if ((sindx = Math.max(
				path.lastIndexOf(File.separator), 
				path.lastIndexOf("/"))) != -1)
		{
			return createPath(path.substring(0, sindx));
		}
		return true;
	}

	/**
	 * Creates the necessary directories for a file path.
	 * @param path	the abstract path.
	 * @return		true if the paths were made (or exists), false otherwise.
	 */
	public static boolean createPath(String path)
	{
		File dir = new File(path);
		if (dir.exists())
			return true;
		return dir.mkdirs();
	}

	/**
	 * Gets the relative path to a file path.
	 * @param sourcePath source file path.
	 * @param targetPath target file path to create the path to.
	 * @return a path string to the target path relative to the source path.
	 * @throws IOException if the canonical file paths cannot be resolved for either file.
	 */
	public static String getRelativePath(String sourcePath, String targetPath) throws IOException
	{
		return getRelativePath(new File(sourcePath), new File(targetPath));
	}

	/**
	 * Gets the relative path to a file path.
	 * @param source source file.
	 * @param target target file to create the path to.
	 * @return a path string to the target file relative to the source file.
	 * @throws IOException if the canonical file paths cannot be resolved for either file.
	 */
	public static String getRelativePath(File source, File target) throws IOException
	{
		LinkedList<File> sourcePath = new LinkedList<File>();
		LinkedList<File> targetPath = new LinkedList<File>();
	
		source = source.getCanonicalFile();
		sourcePath.push(source);
		while (source.getParentFile() != null)
		{
			source = source.getParentFile();
			sourcePath.push(source);
		}
	
		target = new File(target.getCanonicalPath());
		targetPath.push(target);
		while (target.getParentFile() != null)
		{
			target = target.getParentFile();
			targetPath.push(target);
		}
		
		if (OSUtils.isWindows())
		{
			String sroot = sourcePath.peek().getPath();
			String troot = targetPath.peek().getPath();
			if (!sroot.equals(troot))
				return targetPath.peekLast().getCanonicalPath();
		}
		
		while (!sourcePath.isEmpty() && !targetPath.isEmpty() && 
				sourcePath.peek().equals(targetPath.peek()))
		{
			sourcePath.pop();
			targetPath.pop();
		}
		
		StringBuilder sb = new StringBuilder();
		if (sourcePath.isEmpty())
		{
			sb.append(".");
			sb.append(File.separator);
		}
		else while (!sourcePath.isEmpty())
		{
			sb.append("..");
			sb.append(File.separator);
			sourcePath.pop();
		}
		
		while (!targetPath.isEmpty())
		{
			sb.append(targetPath.pop().getName());
			if (!targetPath.isEmpty())
				sb.append(File.separator);
		}
	
		return sb.toString();
	}

	/**
	 * Returns the provided file path into a canonical File path.
	 * If a call to {@link File#getCanonicalFile()} fails, it will call {@link File#getAbsoluteFile()} instead.
	 * @param source the source file.
	 * @return the resultant File with a full path.
	 */
	public static File canonizeFile(File source)
	{
		try {
			return source.getCanonicalFile();
		} catch (IOException e) {
			return source.getAbsoluteFile();
		}
	}

	/**
	 * Returns the file's name, no extension.
	 * @param file the file.
	 * @param extensionSeparator the text or characters that separates file name from extension.
	 * @return the file's name without extension.
	 */
	public static String getFileNameWithoutExtension(File file, String extensionSeparator)
	{
		return getFileNameWithoutExtension(file.getName(), extensionSeparator);
	}

	/**
	 * Returns the file's name, no extension.
	 * @param filename the file name.
	 * @param extensionSeparator the text or characters that separates file name from extension.
	 * @return the file's name without extension.
	 */
	public static String getFileNameWithoutExtension(String filename, String extensionSeparator)
	{
		int extindex = filename.lastIndexOf(extensionSeparator);
		if (extindex >= 0)
			return filename.substring(0, extindex);
		return filename;
	}

	/**
	 * Returns the file's name, no extension.
	 * @param file the file.
	 * @return the file's name without extension.
	 */
	public static String getFileNameWithoutExtension(File file)
	{
		return getFileNameWithoutExtension(file.getName(), ".");
	}

	/**
	 * Returns the file's name, no extension.
	 * @param filename the file name.
	 * @return the file's name without extension.
	 */
	public static String getFileNameWithoutExtension(String filename)
	{
		return getFileNameWithoutExtension(filename, ".");
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
	 * Returns the extension of a file's name.
	 * @param file the file.
	 * @param extensionSeparator the text or characters that separates file name from extension.
	 * @return the file's extension, or an empty string for no extension.
	 */
	public static String getFileExtension(File file, String extensionSeparator)
	{
		return getFileExtension(file.getName(), extensionSeparator);
	}

	/**
	 * Returns the extension of a filename.
	 * Assumes the separator to be ".".
	 * @param filename the file name.
	 * @return the file's extension, or an empty string for no extension.
	 */
	public static String getFileExtension(String filename)
	{
		return getFileExtension(filename, ".");
	}

	/**
	 * Returns the extension of a file's name.
	 * Assumes the separator to be ".".
	 * @param file the file.
	 * @return the file's extension, or an empty string for no extension.
	 */
	public static String getFileExtension(File file)
	{
		return getFileExtension(file.getName(), ".");
	}
	
	/**
	 * Appends an extension to a file.
	 * @param file the input file.
	 * @param extensionSeparator the text or characters that separates file name from extension.
	 * @param extension the extension to add.
	 * @return the new file path.
	 */
	public static File addExtension(File file, String extensionSeparator, String extension)
	{
		String parent = file.getParent();
		return new File((parent != null ? file.getParent() + File.separator : "") + file.getName() + extensionSeparator + extension);
	}

	/**
	 * Appends an extension to a file.
	 * Assumes the separator to be ".".
	 * @param file the input file.
	 * @param extension the extension to add.
	 * @return the new file path.
	 */
	public static File addExtension(File file, String extension)
	{
		return addExtension(file, ".", extension);
	}

	/**
	 * Appends an extension to a file, if it is missing an extension.
	 * @param file the input file.
	 * @param extensionSeparator the text or characters that separates file name from extension.
	 * @param extension the extension to add.
	 * @return the new file path.
	 */
	public static File addMissingExtension(File file, String extensionSeparator, String extension)
	{
		String ext = getFileExtension(file, extensionSeparator);
		if (ext.length() == 0)
			return new File(file.getPath() + extensionSeparator + extension);
		return file;
	}

	/**
	 * Appends an extension to a file.
	 * Assumes the separator to be ".".
	 * @param file the input file.
	 * @param extension the extension to add.
	 * @return the new file path.
	 */
	public static File addMissingExtension(File file, String extension)
	{
		return addMissingExtension(file, ".", extension);
	}

	/**
	 * Appends an extension to a file, if it already has an extension, replacing its final extension.
	 * @param file the input file.
	 * @param extensionSeparator the text or characters that separates file name from extension.
	 * @param extension the extension to add.
	 * @return the new file path.
	 */
	public static File changeExtension(File file, String extensionSeparator, String extension)
	{
		String ext = getFileExtension(file, extensionSeparator);
		if (ext.length() >= 0)
		{
			String fileName = getFileNameWithoutExtension(file);
			String parent = file.getParent();
			return new File((parent != null ? file.getParent() + File.separator : "") + fileName + extensionSeparator + extension);
		}
		return file;
	}

	/**
	 * Appends an extension to a file, if it already has an extension, replacing its final extension.
	 * @param file the input file.
	 * @param extension the extension to add.
	 * @return the new file path.
	 */
	public static File changeExtension(File file, String extension)
	{
		return changeExtension(file, ".", extension);
	}

	/**
	 * Creates a list of URLs from a list of files or directories.
	 * Directories in the list of files are exploded down to actual
	 * files, so that no directories remain. Because of this,
	 * the output list of URLs may be longer than the input file list!
	 * @param files	the list of files to convert.
	 * @return an array of URLs that point to individual files.
	 */
	public static URL[] getURLsForFiles(File ... files)
	{
		File[] flist = explodeFiles(files);
		URL[] urls = new URL[flist.length];
		for (int i = 0; i < flist.length; i++)
		{
			try {
				urls[i] = flist[i].toURI().toURL();
			} catch (MalformedURLException e) {
				RuntimeException re = new RuntimeException("A malformed URL was created somehow.");
				re.initCause(e);
				throw re;
			}
		}
		return urls;
	}

	/**
	 * Checks if a file's name matches a DOS/UNIX-style wildcard pattern (uses system case and path separator policy).
	 * This checks the pattern against the file's filename, path, and absolute path 
	 * (<code>target.getName()</code>, <code>target.getPath()</code>, <code>target.getAbsolutePath()</code>).
	 * <p>
	 * A star (*) in a pattern matches zero or more characters except a slash (back or forward).
	 * A question mark (?) matches a single character, but not a slash (back or forward).
	 * </p>
	 * @param pattern the pattern to use.
	 * @param target the target file to check.
	 * @return true if the pattern matches, false otherwise.
	 */
	public static boolean matchWildcardPattern(String pattern, File target)
	{
		return matchWildcardPattern(pattern, target, OSUtils.isWindows() ? true : false);
	}

	/**
	 * Checks if a file's name matches a DOS/UNIX-style wildcard pattern.
	 * Treats slashes and backslashes as equal.
	 * This checks the pattern against the file's filename, path, and absolute path 
	 * (<code>target.getName()</code>, <code>target.getPath()</code>, <code>target.getAbsolutePath()</code>).
	 * <p>
	 * A star (*) in a pattern matches zero or more characters except a slash (back or forward).
	 * A question mark (?) matches a single character, but not a slash (back or forward).
	 * </p>
	 * @param pattern the pattern to use.
	 * @param target the target file to check.
	 * @param caseInsensitive if true, this will not use letter case to evaluate.
	 * @return true if the pattern matches, false otherwise.
	 */
	public static boolean matchWildcardPattern(String pattern, File target, boolean caseInsensitive)
	{
		boolean slashAgnostic = File.separatorChar == '\\';
		return 
			matchWildcardPattern(pattern, target.getName(), caseInsensitive, slashAgnostic)
			|| matchWildcardPattern(pattern, target.getPath(), caseInsensitive, slashAgnostic)
			|| matchWildcardPattern(pattern, target.getAbsolutePath(), caseInsensitive, slashAgnostic);
	}

	/**
	 * Checks if a string matches a DOS/UNIX-style wildcard pattern.
	 * <p>
	 * A star (*) in a pattern matches zero or more characters except a slash (back or forward).
	 * A question mark (?) matches a single character, but not a slash (back or forward).
	 * </p>
	 * @param pattern the pattern to use.
	 * @param target the target string to check.
	 * @param caseInsensitive if true, this will not use letter case to evaluate.
	 * @return true if the pattern matches, false otherwise.
	 */
	public static boolean matchWildcardPattern(String pattern, String target, boolean caseInsensitive)
	{
		return matchWildcardPattern(pattern, target, caseInsensitive, false);
	}

	/**
	 * Checks if a string matches a DOS/UNIX-style wildcard pattern.
	 * <p>
	 * A star (*) in a pattern matches zero or more characters except a slash (back or forward).
	 * A question mark (?) matches a single character, but not a slash (back or forward).
	 * </p>
	 * @param pattern the pattern to use.
	 * @param target the target string to check.
	 * @param caseInsensitive if true, this will not use letter case to evaluate.
	 * @param slashAgnostic if true, treats slashes and backslashes as equal.
	 * @return true if the pattern matches, false otherwise.
	 */
	public static boolean matchWildcardPattern(String pattern, String target, boolean caseInsensitive, boolean slashAgnostic)
	{
		if (pattern.length() == 0 && target.length() == 0)
			return true;
		
		final char ANY_ALL_CHAR = '*';
		final char ANY_ONE_CHAR = '?';
	
		int pi = 0;
		int ti = 0;
		int plen = pattern.length();
		int tlen = target.length();
		
		while (pi < plen && ti < tlen)
		{
			char p = pattern.charAt(pi);
			char t = target.charAt(ti);
			if (p != ANY_ALL_CHAR)
			{
				if (p == ANY_ONE_CHAR)
				{
					if (t == '/' || t == '\\')
						return false;
					else
					{
						pi++; 
						ti++;						
					}
				}
				else if (p == t)
				{
					pi++; 
					ti++;
				}
				else if (caseInsensitive && Character.toLowerCase(p) == Character.toLowerCase(t))
				{
					pi++; 
					ti++;
				}
				else if (slashAgnostic && (p == '/' || p == '\\') && (t == '/' || t == '\\'))
				{
					pi++; 
					ti++;
				}
				else
					return false;
			}
			else
			{
				char nextChar = pi+1 < plen ? pattern.charAt(pi+1) : '\0';
				if (nextChar == ANY_ALL_CHAR)
					pi++;
				else if (nextChar != '\0')
				{
					// does not match a slash.
					if (t == '/' || t == '\\')
						pi++;
					else if (nextChar == t)
						pi++;
					else if (caseInsensitive && Character.toLowerCase(nextChar) == Character.toLowerCase(t))
						pi++;
					else if (slashAgnostic && (p == '/' || p == '\\') && (t == '/' || t == '\\'))
						pi++; 
					else
						ti++;
				}
				// does not match a slash.
				else if (t == '/' || t == '\\')
					pi++;
				else
					ti++;
			}
		}
		
		if (pi == plen - 1)
			return pattern.charAt(pi) == ANY_ALL_CHAR && ti == tlen;
		return pi == plen && ti == tlen;
	}

	/**
	 * Returns a list of files that match a wildcard path.
	 * @param path the file path, relative or absolute that
	 * 		contains wildcards in the file name portion.
	 * @return a list of matching files. Can return an array of zero length if nothing matches.
	 */
	public static File[] getFilesByWildcardPath(String path)
	{
		return getFilesByWildcardPath(path, false);
	}

	/**
	 * Returns a list of files that match a wildcard path.
	 * @param path the file path, relative or absolute that
	 * 		contains wildcards in the file name portion.
	 * @param hidden if true, include hidden files.
	 * @return a list of matching files. Can return an array of zero length if nothing matches. Can return null on a bad directory read.
	 */
	public static File[] getFilesByWildcardPath(String path, boolean hidden)
	{
		Queue<File> out = new LinkedList<File>();
		
		boolean slashAgnostic = OSUtils.isWindows();
		boolean caseInsensitive = OSUtils.isWindows();
		
		String parent = null;
		String name = null;
		File pathFile = new File(path);
		
		if (pathFile.exists() && pathFile.isDirectory())
		{
			parent = path;
			name = "*";
		}
		else
		{
			int sidx = Math.max(path.lastIndexOf(File.separator), path.lastIndexOf("/"));
			parent = sidx >= 0 ? path.substring(0, sidx) : ".";
			name = sidx == path.length() - 1 ? "*" : path.substring(sidx + 1, path.length());
		}
		
		File dir = new File(parent);
		
		if (!(dir.exists() && dir.isDirectory()))
			return new File[0];
		
		File[] dirFiles = dir.listFiles();
		if (dirFiles == null)
			return null;
		
		for (File f : dirFiles)
		{
			if (!hidden && f.isHidden())
				continue;
			
			if (matchWildcardPattern(name, f.getName(), caseInsensitive, slashAgnostic))
				out.add(f);
		}
		
		File[] files = new File[out.size()];
		out.toArray(files);
		return files;
	}

	/**
	 * Explodes a list of files into a larger list of files,
	 * such that all of the files in the resultant list are not
	 * directories, by traversing directory paths.
	 *
	 * The returned list is not guaranteed to be in any order
	 * related to the input list, and will contain files that are
	 * in the input list if they are not directories.
	 *
	 * @param files	the list of files to expand.
	 * @return	a list of all files found in the subdirectory search, or null on a bad directory read.
	 * @throws	NullPointerException if files is null.
	 */
	public static File[] explodeFiles(File ... files)
	{
		Queue<File> fileQueue = new LinkedList<File>();
		List<File> fileList = new ArrayList<File>();
	
		for (File f : files)
			fileQueue.add(f);
	
		while (!fileQueue.isEmpty())
		{
			File dequeuedFile = fileQueue.poll();
			if (dequeuedFile.isDirectory())
			{
				File[] dirFiles = dequeuedFile.listFiles();
				if (dirFiles == null)
					return null;
				
				for (File f : dirFiles)
				{
					if (f.isDirectory())
						fileQueue.add(f);
					else
						fileList.add(f);
				}
			}
			else
			{
				fileList.add(dequeuedFile);
			}
		}
	
		File[] out = new File[fileList.size()];
		fileList.toArray(out);
		return out;
	}
	
	/**
	 * Expands a list of files into a larger list of files,
	 * such that all of the files in the resultant list are not
	 * directories, by traversing directory paths, but unlike
	 * {@link #explodeFiles(File...)}, this does NOT RECURSE!
	 *
	 * The returned list is not guaranteed to be in any order
	 * related to the input list, and will contain files that are
	 * in the input list if they are not directories.
	 *
	 * @param files	the list of files to expand.
	 * @return	a list of all files found in the subdirectory search, or null on a bad directory read.
	 * @throws	NullPointerException if files is null.
	 */
	public static File[] expandFiles(File ... files)
	{
		Queue<File> fileQueue = new LinkedList<File>();
		List<File> fileList = new ArrayList<File>();
	
		for (File f : files)
			fileQueue.add(f);
	
		while (!fileQueue.isEmpty())
		{
			File dequeuedFile = fileQueue.poll();
			if (!dequeuedFile.isDirectory())
			{
				File[] dirFiles = dequeuedFile.listFiles();
				if (dirFiles == null)
					return null;
				
				for (File f : dirFiles)
					if (!f.isDirectory())
						fileList.add(f);
			}
			else
			{
				fileList.add(dequeuedFile);
			}
		}
	
		File[] out = new File[fileList.size()];
		fileList.toArray(out);
		return out;
	}

	/**
	 * Scans a directory for a file recursively until it finds the desired file (by name).
	 * @param dir the directory to search.
	 * @param name the name of the file.
	 * @param noExt if true, do not use the file's extension, just name.
	 * @param caseSensitive if true, search case-insensitively.
	 * @return the found file, or null if not found or the directory could not be read.
	 */
	public static File searchDirectory(File dir, String name, boolean noExt, boolean caseSensitive)
	{
		File[] dirFiles = dir.listFiles();
		if (dirFiles == null)
			return null;
		
		for (File file : dirFiles)
		{
			if (file.isDirectory())
			{
				File found;
				if ((found = searchDirectory(file, name, noExt, caseSensitive)) != null)
					return found;
			}
			else
			{
				if (noExt)
				{
					String filename = FileUtils.getFileNameWithoutExtension(file);
					if (caseSensitive && filename.equals(name))
					{
						return file;
					}
					else if (!caseSensitive && filename.equalsIgnoreCase(name))
					{
						return file;
					}
				}
				else
				{
					String filename = file.getName();
					if (caseSensitive && filename.equals(name))
					{
						return file;
					}
					else if (!caseSensitive && filename.equalsIgnoreCase(name))
					{
						return file;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Deletes all files under a directory.
	 * If the provided file is not a directory, it is still deleted.
	 * This does not search recursively for more files.
	 * @param directory the directory.
	 * @return the files deleted.
	 */
	public static File[] deleteDirectory(File directory)
	{
		return deleteDirectory(directory, false, (file) -> true);
	}
	
	/**
	 * Deletes all files under a directory.
	 * If the provided file is not a directory, it is still deleted.
	 * @param directory the directory.
	 * @param recurse if true, recurse through directories and delete those.
	 * @return the files deleted.
	 */
	public static File[] deleteDirectory(File directory, boolean recurse)
	{
		return deleteDirectory(directory, recurse, (file) -> true);
	}
	
	/**
	 * Deletes all files under a directory.
	 * If the provided file is not a directory, it is still deleted.
	 * @param directory the directory.
	 * @param recurse if true, recurse through directories and delete those.
	 * @param filter an optional file filter for what to delete.
	 * @return the files deleted.
	 */
	public static File[] deleteDirectory(File directory, boolean recurse, FileFilter filter)
	{
		List<File> aggregator = new LinkedList<File>();
		deleteDirectoryRecurse(aggregator, directory, recurse, filter);
		return aggregator.toArray(new File[aggregator.size()]);
	}
	
	/**
	 * Deletes all files under a directory.
	 * If the provided file is not a directory, it is still deleted.
	 * @param aggregator the aggregate list for all deleted files.
	 * @param directory the directory.
	 * @param recurse if true, recurse through directories and delete those.
	 * @param filter an optional file filter for what to delete.
	 */
	public static void deleteDirectoryRecurse(List<File> aggregator, File directory, boolean recurse, FileFilter filter)
	{
		File[] files = directory.listFiles(filter);
		if (files == null)
		{
			if (directory.delete())
				aggregator.add(directory);
			return;
		}
		
		for (int i = 0; i < files.length; i++) 
		{
			File file = files[i];
			if (file.isDirectory() && recurse)
			{
				deleteDirectoryRecurse(aggregator, file, recurse, filter);
			}
			else
			{
				if (file.delete())
					aggregator.add(file);
			}
		}
		
		if (directory.delete())
			aggregator.add(directory);
	}

	
	/**
	 * Gets a list of subdirectories from a top directory.
	 * @param startDirectory the starting directory.
	 * @param includeTop if true, the output includes the starting directory.
	 * @return an array of subdirectory paths under the top directory.
	 */
	public static File[] getSubdirectories(File startDirectory, boolean includeTop)
	{
		return getSubdirectories(startDirectory, includeTop, (unused) -> true);
	}

	/**
	 * Gets a list of subdirectories from a top directory.
	 * @param startDirectory the starting directory.
	 * @param includeTop if true, the output includes the starting directory.
	 * @param dirFilter additional directory filter.
	 * @return an array of subdirectory paths under the top directory.
	 */
	public static File[] getSubdirectories(File startDirectory, boolean includeTop, FileFilter dirFilter)
	{
		if (!startDirectory.isDirectory())
			return null;
		
		List<File> dirs = new LinkedList<>();
		Deque<File> dirQueue = new LinkedList<>();
		dirQueue.add(startDirectory);
		
		if (includeTop)
			dirs.add(startDirectory);
		
		while (!dirQueue.isEmpty())
		{
			File dir = dirQueue.pollFirst();
			File[] files = dir.listFiles((f) -> f.isDirectory());
			for (int i = 0; i < files.length; i++)
			{
				if (dirFilter.accept(files[i]))
				{
					dirQueue.add(files[i]);
					dirs.add(files[i]);
				}
			}
		}
		
		return dirs.toArray(new File[dirs.size()]);
	}

	/**
	 * Compares two file paths for equality.
	 * If the OS is Windows, the paths are compared case-insensitively.
	 * @param a the first file.
	 * @param b the second file.
	 * @return true if the two files have the same absolute path, false otherwise.
	 */
	public static boolean filePathEquals(File a, File b)
	{
		if (a == null)
			return b == null;
		else if (b == null)
			return false;
		else if (OSUtils.isWindows())
			return a.getAbsolutePath().equalsIgnoreCase(b.getAbsolutePath());
		else
			return a.getAbsolutePath().equals(b.getAbsolutePath());
	}
	
	/**
	 * Gets a file comparator that sorts files by name, lexicographically.
	 * Name sort is case-insensitive on operating systems with case-insensitive filesystems. 
	 * @return the comparator.
	 */
	public static Comparator<File> getFileComparator()
	{
		return FILE_COMPARATOR;
	}

	/**
	 * Gets a file comparator that sorts directories before files, lexicographically.
	 * Name sort is case-insensitive on operating systems with case-insensitive filesystems. 
	 * @return the comparator.
	 */
	public static Comparator<File> getFileListComparator()
	{
		return FILELIST_COMPARATOR;
	}
	
	/**
	 * Attempts to rename a file, waiting to do so since the file's handle may not be relinquished.
	 * Guaranteed to at least wait the amount of the timeout in milliseconds.
	 * @param oldName the file name.
	 * @param newName the file's new name.
	 * @param timeout the timeout in milliseconds.
	 * @return true if the file was renamed, false if not.
	 */
	public static boolean renameTimeout(File oldName, File newName, final int timeout)
	{
		int t = 0;
		while (!oldName.renameTo(newName) && t <= timeout)
		{
			t++;
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// Do nothing.
			}
		}
		
		return t < timeout;
	}

	/**
	 * Attempts to match a file's magic number (initial bytes).
	 * @param f the file to test.
	 * @param magicNumber the magic number bytes to test for.
	 * @return true if matched, false if not.
	 * @throws IOException if a read error occurs.
	 */
	public static boolean matchMagicNumber(File f, byte[] magicNumber) throws IOException
	{
		try (RandomAccessFile raf = new RandomAccessFile(f, "r"))
		{
			byte[] buf = new byte[magicNumber.length];
			int len = raf.read(buf);
			if (len != magicNumber.length)
				return false;
			return Arrays.equals(magicNumber, buf);
		}
	}
	
	/**
	 * A path to a temporary file that is deleted on close.
	 */
	public static final class TempFile extends File implements AutoCloseable
	{
		private static final long serialVersionUID = -4447892228054673063L;

		private TempFile(String path) 
		{
			super(path);
		}
		
		@Override
		public void close()
		{
			if (exists())
				delete();
		}
	}
	
}

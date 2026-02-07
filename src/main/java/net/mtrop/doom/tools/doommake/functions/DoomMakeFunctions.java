/*******************************************************************************
 * Copyright (c) 2020-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.doommake.functions;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.blackrook.rookscript.ScriptInstance;
import com.blackrook.rookscript.ScriptIteratorType.IteratorPair;
import com.blackrook.rookscript.ScriptValue;
import com.blackrook.rookscript.ScriptValue.BufferType;
import com.blackrook.rookscript.ScriptValue.Type;
import com.blackrook.rookscript.lang.ScriptFunctionType;
import com.blackrook.rookscript.lang.ScriptFunctionUsage;
import com.blackrook.rookscript.resolvers.ScriptFunctionResolver;
import com.blackrook.rookscript.resolvers.hostfunction.EnumFunctionResolver;
import com.blackrook.rookscript.struct.PatternUtils;

import net.mtrop.doom.tools.struct.ReplacerReader;
import net.mtrop.doom.tools.struct.util.FileUtils;
import net.mtrop.doom.tools.struct.util.IOUtils;
import net.mtrop.doom.tools.struct.util.ObjectUtils;
import net.mtrop.doom.tools.common.Common;

import static com.blackrook.rookscript.lang.ScriptFunctionUsage.type;


/**
 * Script functions specific to DoomMake.
 * @author Matthew Tropiano
 */
public enum DoomMakeFunctions implements ScriptFunctionType
{
	CLEANDIR(2)
	{
		@Override
		protected Usage usage() 
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Recursively deletes all files and directories inside a target directory."
				)
				.parameter("path", 
					type(Type.STRING, "Path to directory."),
					type(Type.OBJECTREF, "File", "Path to directory.")
				)
				.parameter("deleteTop", 
					type(Type.BOOLEAN, "If true, also delete the provided directory as well.")
				)
				.returns(
					type(Type.NULL, "If [path] is null."),
					type(Type.BOOLEAN, "True if the directory existed and was cleaned, false otherwise."),
					type(Type.ERROR, "Security", "If the OS is preventing the read.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try 
			{
				scriptInstance.popStackValue(temp);
				boolean deleteTop = temp.asBoolean();
				
				File file = popFile(scriptInstance, temp);
				
				try {
					if (file == null)
						returnValue.setNull();
					else if (!file.exists())
						returnValue.set(false);
					else
						returnValue.set(Common.cleanDirectory(file, deleteTop));
				} catch (SecurityException e) {
					returnValue.setError("Security", e.getMessage(), e.getLocalizedMessage());
				}
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},

	COPYFILE(3)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Copies a file from one path to another, optionally creating directories for it. " +
					"If the destination file exists, it is overwritten."
				)
				.parameter("srcFile", 
					type(Type.STRING, "Path to source file."),
					type(Type.OBJECTREF, "File", "Path to source file.")
				)
				.parameter("destFile", 
					type(Type.STRING, "Path to destination file."),
					type(Type.OBJECTREF, "File", "Path to destination file.")
				)
				.parameter("createDirs", 
					type(Type.BOOLEAN, "If true, create the directories for the destination, if not made.")
				)
				.returns(
					type(Type.NULL, "If either file is null."),
					type(Type.OBJECTREF, "File", "The created destination file."),
					type(Type.ERROR, "BadFile", "If the source file does not exist."),
					type(Type.ERROR, "IOError", "If a read or write error occurs."),
					type(Type.ERROR, "Security", "If the OS is preventing the read or write.")
				)
			;
		}

		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try 
			{
				scriptInstance.popStackValue(temp);
				boolean createDirs = temp.asBoolean();
				
				File destFile = popFile(scriptInstance, temp);
				File srcFile = popFile(scriptInstance, temp);

				if (srcFile == null || destFile == null)
				{
					returnValue.setNull();
					return true;
				}
				
				copyFile(srcFile, destFile, createDirs, returnValue);
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	COPYDIR(4)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Copies a series of files from one directory to another, replicating the tree in the destination. " +
					"If a destination file exists, it is overwritten."
				)
				.parameter("srcDir",
					type(Type.STRING, "Path to source directory (base path)."),
					type(Type.OBJECTREF, "File", "Path to source directory (base path).")
				)
				.parameter("destDir", 
					type(Type.STRING, "Path to destination directory."),
					type(Type.OBJECTREF, "File", "Path to destination directory.")
				)
				.parameter("recursive",
					type(Type.BOOLEAN, "If true, scan recursively.")
				)
				.parameter("regex",
					type(Type.NULL, "Include everything."),
					type(Type.STRING, "The pattern to match each file path against for inclusion. If matched, include.")
				)
				.returns(
					type(Type.NULL, "If either file is null."),
					type(Type.LIST, "[OBJECTREF:File, ...]", "The list of copied/created files (destination)."),
					type(Type.ERROR, "BadFile", "If the source or destination directory does not exist."),
					type(Type.ERROR, "BadPattern", "If the input RegEx pattern is malformed."),
					type(Type.ERROR, "IOError", "If a read or write error occurs."),
					type(Type.ERROR, "Security", "If the OS is preventing the read or write.")
				)
			;
		}

		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try 
			{
				scriptInstance.popStackValue(temp);
				String regex = temp.isNull() ? null : temp.asString();
				scriptInstance.popStackValue(temp);
				boolean recursive = temp.asBoolean();
				File destDir = popFile(scriptInstance, temp);
				File srcDir = popFile(scriptInstance, temp);

				if (srcDir == null || destDir == null)
				{
					returnValue.setNull();
					return true;
				}

				if (!srcDir.isDirectory())
				{
					returnValue.setError("BadFile", "Source directory does not exist: " + srcDir.getPath());
					return true;
				}
				if (!destDir.isDirectory())
				{
					returnValue.setError("BadFile", "Destination directory does not exist: " + destDir.getPath());
					return true;
				}
				
				FileFilter filter = ((f) -> true);
				if (regex != null)
				{
					try {
						final Pattern p = PatternUtils.get(regex);
						filter = ((f) -> p.matcher(f.getPath()).matches());
					} catch (PatternSyntaxException e) {
						returnValue.setError("BadPattern", e.getMessage(), e.getLocalizedMessage());
						return true;
					}
				}
				
				returnValue.setEmptyList(128);
				copyDir(srcDir, srcDir, destDir, recursive, filter, returnValue);
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},
	
	COPYWITHREPLACE(6)
	{

		@Override
		protected Usage usage() 
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Copies a text-based file, replacing the contents of the file with replace key " +
					"delimited tokens with other text data. If the destination file exists, it is overwritten."
				)
				.parameter("srcFile", 
					type(Type.STRING, "Path to source file."),
					type(Type.OBJECTREF, "File", "Path to source file.")
				)
				.parameter("destFile", 
					type(Type.STRING, "Path to destination file."),
					type(Type.OBJECTREF, "File", "Path to destination file.")
				)
				.parameter("keys",
					type(Type.MAP, "String:String", "The mapping of key to text value, string to string.")
				)
				.parameter("createDirs", 
					type(Type.BOOLEAN, "If true, create the directories for the destination, if not made.")
				)
				.parameter("delimStart",
					type(Type.NULL, "Use default: \"%\""),
					type(Type.STRING, "The token delimiter start string.")
				)
				.parameter("delimEnd",
					type(Type.NULL, "Use default: \"%\""),
					type(Type.STRING, "The token delimiter ending string.")
				)
				.returns(
					type(Type.NULL, "If either file is null."),
					type(Type.OBJECTREF, "File", "The created destination file."),
					type(Type.ERROR, "BadFile", "If the source file does not exist."),
					type(Type.ERROR, "IOError", "If a read or write error occurs."),
					type(Type.ERROR, "Security", "If the OS is preventing the read or write.")
				)
			;
		}

		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue keys = CACHEVALUE2.get();
			try 
			{
				scriptInstance.popStackValue(temp);
				String delimEnd = temp.isNull() ? "%" : temp.asString();
				scriptInstance.popStackValue(temp);
				String delimStart = temp.isNull() ? "%" : temp.asString();
				scriptInstance.popStackValue(temp);
				boolean createDirs = temp.asBoolean();
				scriptInstance.popStackValue(keys);
				File destFile = popFile(scriptInstance, temp);
				File srcFile = popFile(scriptInstance, temp);
				
				if (createDirs && !FileUtils.createPathForFile(destFile))
				{
					returnValue.setError("IOError", "Could not create directories for target file: " + destFile.getPath());
					return true;
				}
				
				String projectCharset = System.getProperty("doommake.project.encoding");
				if (ObjectUtils.isEmpty(projectCharset))
					projectCharset = Charset.defaultCharset().displayName();
				
				try (ReplacerReader reader = new ReplacerReader(new InputStreamReader(new FileInputStream(srcFile), projectCharset), delimStart, delimEnd))
				{
					if (keys.isMap()) for (IteratorPair p : keys)
					{
						reader.replace(p.getKey().asString(), p.getValue().asString());
					}
					
					try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(destFile), projectCharset))
					{
						IOUtils.relay(reader, writer);
					}
				} 
				catch (UnsupportedEncodingException e) 
				{
					returnValue.setError("IOError", "Project encoding is not supported: " + projectCharset);
					return true;
				} 
				catch (FileNotFoundException e) 
				{
					returnValue.setError("BadFile", "Source file could not be found: " + srcFile.getPath());
					return true;
				} 
				catch (IOException e) 
				{
					returnValue.setError("IOError", "Error occurred: " + e.getLocalizedMessage());
					return true;
				}
				catch (SecurityException e) 
				{
					returnValue.setError("Security", "Security error occurred: " + e.getLocalizedMessage());
					return true;
				}
				
				return true;
			}
			finally
			{
				temp.setNull();
				keys.setNull();
			}
		}
		
	},
	
	ZIPFILES(4)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Compresses a series of files into an archive, NOT preserving directory trees. " +
					"If the destination file exists, it is overwritten."
				)
				.parameter("zipfile",
					type(Type.STRING, "Path to target zip file."),
					type(Type.OBJECTREF, "File", "Path to target zip file.")
				)
				.parameter("files", 
					type(Type.LIST, "[STRING, ...]", "Paths to files to add."),
					type(Type.LIST, "[OBJECTREF:File, ...]", "Paths to files to add.")
				)
				.parameter("append",
					type(Type.NULL, "Default: False."),
					type(Type.BOOLEAN, "True to append to an existing Zip file, false to overwrite.")
				)
				.parameter("compressed",
					type(Type.NULL, "Default: True."),
					type(Type.BOOLEAN, "True to compress, false to not compress.")
				)
				.returns(
					type(Type.NULL, "If [zipfile] is null."),
					type(Type.STRING, "The path to the created file, if [zipfile] is a STRING."),
					type(Type.OBJECTREF, "File", "The path to the created file, if [zipfile] is an OBJECTREF:File."),
					type(Type.ERROR, "BadFile", "If a source file cannot be opened."),
					type(Type.ERROR, "IOError", "If a read or write error occurs."),
					type(Type.ERROR, "Security", "If the OS is preventing the read or write.")
				)
			;
		}

		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue files = CACHEVALUE2.get();
			try 
			{
				scriptInstance.popStackValue(temp);
				boolean compressed = temp.isNull() ? true : temp.asBoolean();
				scriptInstance.popStackValue(temp);
				boolean append = temp.asBoolean();
				scriptInstance.popStackValue(files);
				File zipFile = popFile(scriptInstance, temp);
				boolean wasString = temp.isString();
				
				if (zipFile == null)
				{
					returnValue.setNull();
					return true;
				}
				else if (zipFile.isDirectory())
				{
					returnValue.setError("BadZip", "Target file is a directory.");
					return true;
				}

				// attempt append
				if (append && zipFile.exists())
				{
					try (ZipOutputStream zos = reopenZipFile(zipFile, returnValue))
					{
						if (returnValue.isError())
							return true;
						
						zipFileList(zos, zipFile, temp, files, compressed, wasString, returnValue);
					} 
					catch (IOException e) 
					{
						returnValue.setError("IOError", "Could not close zip stream.");
					}
				}
				// not append
				else
				{
					try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile)))
					{
						zipFileList(zos, zipFile, temp, files, compressed, wasString, returnValue);
					} 
					catch (FileNotFoundException e) 
					{
						returnValue.setError("BadZip", "Target file is a directory.");
					} 
					catch (IOException e) 
					{
						returnValue.setError("IOError", "Could not close zip stream.");
					}
				}

				if (!returnValue.isError())
				{
					if (wasString)
						returnValue.set(zipFile.getPath());
					else
						returnValue.set(zipFile);
				}
				return true;
			}
			finally
			{
				temp.setNull();
				files.setNull();
			}
		}

		private void zipFileList(ZipOutputStream zos, File zipFile, ScriptValue temp, ScriptValue files, boolean compressed, boolean wasString, ScriptValue returnValue)
		{
			if (files.isList()) for (int i = 0; i < files.length(); i++)
			{
				files.listGetByIndex(i, temp);
				File file = getFile(temp);
				if (file == null)
					returnValue.setError("BadFile", "Encountered a null file.");
				else
					zipFile(zos, file, file.getName(), compressed, returnValue);
				
				if (returnValue.isError())
					break;
			}
		}
	},
	
	ZIPDIR(6)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Compresses a series of files into an archive from a directory, preserving directory trees. " +
					"Always recurses directory structure. If the destination file exists, it is overwritten, unless [append] is true."
				)
				.parameter("zipfile",
					type(Type.STRING, "Path to source directory (base path)."),
					type(Type.OBJECTREF, "File", "Path to source directory (base path).")
				)
				.parameter("directory", 
					type(Type.OBJECTREF, "File", "Path to source directory.")
				)
				.parameter("prefix",
					type(Type.NULL, "No prefix."),
					type(Type.STRING, "The string to prefix all new entries with.")
				)
				.parameter("append",
					type(Type.NULL, "Default: False."),
					type(Type.BOOLEAN, "True to append to an existing Zip file, false to overwrite.")
				)
				.parameter("regex",
					type(Type.NULL, "Include everything."),
					type(Type.STRING, "The pattern to match each file path against for inclusion. If matched, include.")
				)
				.parameter("compressed",
					type(Type.NULL, "Default: True."),
					type(Type.BOOLEAN, "True to compress, false to not compress.")
				)
				.returns(
					type(Type.NULL, "If [zipfile] is null."),
					type(Type.STRING, "The path to the created file, if [zipfile] is a STRING."),
					type(Type.OBJECTREF, "File", "The path to the created file, if [zipfile] is an OBJECTREF:File."),
					type(Type.ERROR, "BadFile", "If a source file cannot be opened."),
					type(Type.ERROR, "IOError", "If a read or write error occurs."),
					type(Type.ERROR, "Security", "If the OS is preventing the read or write.")
				)
			;
		}

		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue files = CACHEVALUE2.get();
			try 
			{
				scriptInstance.popStackValue(temp);
				boolean compressed = temp.isNull() ? true : temp.asBoolean();
				scriptInstance.popStackValue(temp);
				String regex = temp.isNull() ? null : temp.asString();
				scriptInstance.popStackValue(temp);
				boolean append = temp.asBoolean();
				scriptInstance.popStackValue(temp);
				String prefix = temp.isNull() ? "" : temp.asString();
				File dir = popFile(scriptInstance, temp);
				File zipFile = popFile(scriptInstance, temp);
				boolean wasString = temp.isString();
				
				if (zipFile == null)
				{
					returnValue.setNull();
					return true;
				}
				else if (zipFile.isDirectory())
				{
					returnValue.setError("BadZip", "Target file is a directory.");
					return true;
				}
				
				FileFilter filter = ((f) -> true);
				if (regex != null)
				{
					try {
						final Pattern p = PatternUtils.get(regex);
						filter = ((f) -> p.matcher(f.getPath()).matches());
					} catch (PatternSyntaxException e) {
						returnValue.setError("BadPattern", e.getMessage(), e.getLocalizedMessage());
						return true;
					}
				}
				
				// attempt append
				if (append && zipFile.exists())
				{
					try (ZipOutputStream zos = reopenZipFile(zipFile, returnValue))
					{
						if (returnValue.isError())
							return true;

						zipDir(zos, dir, dir, prefix, compressed, filter, returnValue);
					} 
					catch (IOException e) 
					{
						returnValue.setError("IOError", "Could not close zip stream.");
					}
				}
				// not append
				else
				{
					try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile)))
					{
						if (returnValue.isError())
							return true;

						zipDir(zos, dir, dir, prefix, compressed, filter, returnValue);
					} 
					catch (FileNotFoundException e) 
					{
						returnValue.setError("BadZip", "Target file could not be opened.");
					} 
					catch (IOException e) 
					{
						returnValue.setError("IOError", "Could not close zip stream.");
					}
				}
				
				if (!returnValue.isError())
				{
					if (wasString)
						returnValue.set(zipFile.getPath());
					else
						returnValue.set(zipFile);
				}
				return true;
			}
			finally
			{
				temp.setNull();
				files.setNull();
			}
		}
	},
	
	FETCH(4)
	{
		@Override
		protected Usage usage() 
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Fetches a file from a URL and writes it to a destination file."
				)
				.parameter("url", 
					type(Type.STRING, "URL path."),
					type(Type.OBJECTREF, "URL", "URL path.")
				)
				.parameter("destFile", 
					type(Type.STRING, "Path to destination file."),
					type(Type.OBJECTREF, "File", "Path to destination file.")
				)
				.parameter("createDirs", 
					type(Type.BOOLEAN, "If true, create the directories for the destination, if not made.")
				)
				.parameter("timeoutMillis", 
					type(Type.NULL, "Use 5000 ms."),
					type(Type.INTEGER, "Timeout in milliseconds.")
				)
				.returns(
					type(Type.NULL, "If the URL or the destination file is null."),
					type(Type.OBJECTREF, "File", "The downloaded file."),
					type(Type.ERROR, "BadURL", "If the provided URL is malformed."),
					type(Type.ERROR, "Timeout", "If the connection timed out."),
					type(Type.ERROR, "IOError", "If the connection could not be opened."),
					type(Type.ERROR, "Security", "If the OS is preventing the read or write.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try 
			{
				scriptInstance.popStackValue(temp);
				int timeoutMs = temp.isNull() ? 5000 : temp.asInt();
				scriptInstance.popStackValue(temp);
				boolean createDirs = temp.asBoolean();
				File destFile = popFile(scriptInstance, temp);
				URL url;
				try {
					url = popURL(scriptInstance, temp);
				} catch (MalformedURLException e) {
					returnValue.setError("BadURL", e.getMessage(), e.getLocalizedMessage());
					return true;
				}

				if (url == null || destFile == null)
				{
					returnValue.setNull();
					return true;
				}
				
				URLConnection urlConn;
				try {
					urlConn = url.openConnection();
					urlConn.setReadTimeout(timeoutMs);
					try (InputStream in = urlConn.getInputStream()) {
						writeFile(in, destFile, createDirs, returnValue);
					} catch (SocketTimeoutException e) {
						returnValue.setError("Timeout", e.getMessage(), e.getLocalizedMessage());
					} 
				} catch (IOException e) {
					returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
				}

				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
	},

	UNZIP(3)
	{
		@Override
		protected Usage usage() 
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Unzips a Zip archive to a target directory, preserving directory structure."
				)
				.parameter("zipFile", 
					type(Type.STRING, "Zip file path."),
					type(Type.OBJECTREF, "File", "Zip file path.")
				)
				.parameter("destDir", 
					type(Type.STRING, "Path to destination directory."),
					type(Type.OBJECTREF, "File", "Path to destination directory.")
				)
				.parameter("entries", 
					type(Type.NULL, "All entries."),
					type(Type.LIST, "[STRING, ...]", "If provided, the list of entries to unzip.")
				)
				.returns(
					type(Type.NULL, "If the zip file or the destination directory is null."),
					type(Type.LIST, "[OBJECTREF:File, ...]", "The list of created files."),
					type(Type.ERROR, "BadFile", "If the provided destination is not a directory."),
					type(Type.ERROR, "BadZip", "If the provided file is not a zip file."),
					type(Type.ERROR, "IOError", "If a read or write error occurs."),
					type(Type.ERROR, "Security", "If the OS is preventing the read or write.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue entries = CACHEVALUE2.get();
			try 
			{
				scriptInstance.popStackValue(entries);
				File destDir = popFile(scriptInstance, temp);
				File zipFile = popFile(scriptInstance, temp);

				if (zipFile == null || destDir == null)
				{
					returnValue.setNull();
					return true;
				}
				else if (zipFile.isDirectory())
				{
					returnValue.setError("BadZip", "Target file is a directory.");
					return true;
				}
				else if (!destDir.isDirectory())
				{
					returnValue.setError("BadFile", "Destination directory does not exist.");
					return true;
				}

				Set<String> entrySet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
				if (entries.isList()) for (int i = 0; i < entries.length(); i++)
				{
					entries.listGetByIndex(i, temp);
					entrySet.add(temp.asString());
				}

				returnValue.setEmptyList(64);
				try (ZipFile zf = new ZipFile(zipFile))
				{
					ZipEntry entry;
					for (Enumeration<? extends ZipEntry> en = zf.entries(); en.hasMoreElements();)
					{
						entry = en.nextElement();
						
						if (entrySet.isEmpty() || entrySet.contains(entry.getName()))
						{
							try (InputStream in = zf.getInputStream(entry))
							{
								writeFile(in, new File(destDir.getPath() + File.separator + entry.getName()), true, temp);
								returnValue.listAdd(temp);
							}
							catch (IOException e)
							{
								returnValue.setError("IOError", "Couldn't read from zip file: " + zipFile.getPath());
								return true;
							}
						}
					}
				} 
				catch (ZipException e) 
				{
					returnValue.setError("BadZip", "Could not open zip: " + e.getLocalizedMessage());
				}
				catch (IOException e) 
				{
					returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
				}
				
				return true;
			}
			finally
			{
				temp.setNull();
				entries.setNull();
			}
		}
	},

	HASHDIR(3)
	{
		@Override
		protected Usage usage() 
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Hashes file information in a directory. " +
					"No data content is hashed, just file paths, length, and modified date."
				)
				.parameter("path", 
					type(Type.STRING, "Directory path."),
					type(Type.OBJECTREF, "File", "Directory path.")
				)
				.parameter("recursive",
					type(Type.BOOLEAN, "If true, scan recursively.")
				)
				.parameter("algorithm",
					type(Type.NULL, "Use \"SHA-1\"."),
					type(Type.STRING, "The name of the hashing algorithm to use.")
				)
				.returns(
					type(Type.NULL, "If the provided directory is null."),
					type(Type.BUFFER, "A buffer containing the resultant hash digest."),
					type(Type.ERROR, "BadPath", "If the provided path is not a directory."),
					type(Type.ERROR, "IOError", "If the directory could not be read."),
					type(Type.ERROR, "Security", "If the OS is preventing file inspection.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			try 
			{
				scriptInstance.popStackValue(temp);
				String algo = temp.isNull() ? "SHA-1" : temp.asString();
				scriptInstance.popStackValue(temp);
				boolean recursive = temp.asBoolean();
				File pathDir = popFile(scriptInstance, temp);
				
				if (pathDir == null)
				{
					returnValue.setNull();
					return true;
				}
				else if (!pathDir.exists())
				{
					returnValue.setError("BadPath", "Provided path does not exist.");
					return true;
				}
				else if (!pathDir.isDirectory())
				{
					returnValue.setError("BadPath", "Provided path is not a directory.");
					return true;
				}

				MessageDigest digest;
				try {
					digest = MessageDigest.getInstance(algo);
					digestDirectory(digest, recursive, pathDir, returnValue);
				} catch (NoSuchAlgorithmException e) {
					returnValue.setError("BadAlgorithm", "Hash algorithm is not available: " + algo);
					return true;
				}

				if (returnValue.isError())
					return true;

				byte[] hash = digest.digest();
				returnValue.setEmptyBuffer(hash.length);
				returnValue.asObjectType(BufferType.class).readBytes(0, hash, 0, hash.length);

				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
		
	},
	
	HASHFILES(2)
	{
		@Override
		protected Usage usage() 
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Hashes file information. " +
					"No data content is hashed, just file paths, length, and modified date. " +
					"Files that are not found are skipped."
				)
				.parameter("paths", 
					type(Type.LIST, "[STRING, ...]", "The list of file paths."),
					type(Type.LIST, "[OBJECTREF:File, ...]", "The list of file paths.")
				)
				.parameter("algorithm",
					type(Type.NULL, "Use \"SHA-1\"."),
					type(Type.STRING, "The name of the hashing algorithm to use.")
				)
				.returns(
					type(Type.NULL, "If the provided directory is null."),
					type(Type.BUFFER, "A buffer containing the resultant hash digest."),
					type(Type.ERROR, "BadFile", "If a source file is null."),
					type(Type.ERROR, "Security", "If the OS is preventing file inspection.")
				)
			;
		}
		
		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue)
		{
			ScriptValue temp = CACHEVALUE1.get();
			ScriptValue files = CACHEVALUE2.get();
			try 
			{
				scriptInstance.popStackValue(temp);
				String algo = temp.isNull() ? "SHA-1" : temp.asString();
				scriptInstance.popStackValue(files);
				
				MessageDigest digest;
				try {
					digest = MessageDigest.getInstance(algo);
					
					if (files.isList()) for (int i = 0; i < files.length(); i++)
					{
						files.listGetByIndex(i, temp);
						File f = getFile(temp);
						if (f == null)
						{
							returnValue.setError("BadFile", "Encountered a null file.");
							return true;
						}
						else if (f.exists())
						{
							digestFileInfo(digest, f, returnValue);
							if (returnValue.isError())
								return true;
						}
					}

				} catch (NoSuchAlgorithmException e) {
					returnValue.setError("BadAlgorithm", "Hash algorithm is not available: " + algo);
					return true;
				}

				if (returnValue.isError())
					return true;

				byte[] hash = digest.digest();
				returnValue.setEmptyBuffer(hash.length);
				returnValue.asObjectType(BufferType.class).readBytes(0, hash, 0, hash.length);

				return true;
			}
			finally
			{
				temp.setNull();
				files.setNull();
			}
		}
		
	},
	
	SEARCHDIR(4)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Searches a directory tree (recursively) for a file by name and returns that file."
				)
				.parameter("path", 
					type(Type.STRING, "Directory path."),
					type(Type.OBJECTREF, "File", "Directory path.")
				)
				.parameter("target", 
					type(Type.STRING, "The file's name.")
				)
				.parameter("noExtension",
					type(Type.BOOLEAN, "If true, just the file's name is searched for, and not its extension.")
				)
				.parameter("caseSensitive",
					type(Type.BOOLEAN, "If true, search for matching case, false for case-insensitive search.")
				)
				.returns(
					type(Type.NULL, "If the file could not be found."),
					type(Type.OBJECTREF, "File", "The file that was found."),
					type(Type.ERROR, "BadPath", "If the provided path is not a directory."),
					type(Type.ERROR, "BadName", "If the provided name is empty."),
					type(Type.ERROR, "Security", "If the OS is preventing the search.")
				)
			;
		}

		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue) 
		{
			ScriptValue temp = CACHEVALUE1.get();
			try 
			{
				scriptInstance.popStackValue(temp);
				boolean caseSensitive = temp.asBoolean();
				scriptInstance.popStackValue(temp);
				boolean noExt = temp.asBoolean();
				scriptInstance.popStackValue(temp);
				String target = temp.isNull() ? null : temp.asString();
				File pathDir = popFile(scriptInstance, temp);
				
				if (ObjectUtils.isEmpty(target))
				{
					returnValue.setError("BadName", "Provided name is empty.");
					return true;
				}

				if (ObjectUtils.isEmpty(pathDir))
				{
					returnValue.setError("BadPath", "Provided path is empty.");
					return true;
				}
				else if (!pathDir.exists())
				{
					returnValue.setError("BadPath", "Provided path does not exist.");
					return true;
				}
				else if (!pathDir.isDirectory())
				{
					returnValue.setError("BadPath", "Provided path is not a directory.");
					return true;
				}

				try {
					returnValue.setNull();
					scanDir(pathDir, target, noExt, caseSensitive, returnValue);
				} catch (SecurityException e) {
					returnValue.setError("Security", e.getMessage(), e.getLocalizedMessage());
				}
				
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
		
	},
	
	TOUCHFILE(1)
	{
		@Override
		protected Usage usage()
		{
			return ScriptFunctionUsage.create()
				.instructions(
					"Creates or updates a file's modified timestamp."
				)
				.parameter("path", 
					type(Type.STRING, "The file path."),
					type(Type.OBJECTREF, "File", "The file path.")
				)
				.returns(
					type(Type.OBJECTREF, "File", "The file that was provided."),
					type(Type.ERROR, "BadPath", "If the file name is empty, or a directory."),
					type(Type.ERROR, "IOError", "If the file could not be created."),
					type(Type.ERROR, "Security", "If the OS is preventing the search.")
				)
			;
		}

		@Override
		public boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue) 
		{
			ScriptValue temp = CACHEVALUE1.get();
			try 
			{
				File path = popFile(scriptInstance, temp);
				
				if (ObjectUtils.isEmpty(path))
				{
					returnValue.setError("BadPath", "Provided path is empty.");
					return true;
				}
				else if (path.isDirectory())
				{
					returnValue.setError("BadPath", "Provided path is a directory.");
					return true;
				}

				try {
					FileUtils.touch(path);
					returnValue.set(path);
				} catch (IOException e) {
					returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
				} catch (SecurityException e) {
					returnValue.setError("Security", e.getMessage(), e.getLocalizedMessage());
				}
				
				return true;
			}
			finally
			{
				temp.setNull();
			}
		}
		
	},
	
	;
	
	private final int parameterCount;
	private Usage usage;
	private DoomMakeFunctions(int parameterCount)
	{
		this.parameterCount = parameterCount;
		this.usage = null;
	}
	
	private static final Charset UTF8 = Charset.forName("UTF-8");
	
	/**
	 * @return a function resolver that handles all of the functions in this enum.
	 */
	public static final ScriptFunctionResolver createResolver()
	{
		return new EnumFunctionResolver(DoomMakeFunctions.values());
	}

	@Override
	public int getParameterCount()
	{
		return parameterCount;
	}

	@Override
	public Usage getUsage()
	{
		if (usage == null)
			usage = usage();
		return usage;
	}
	
	protected abstract Usage usage();

	@Override
	public abstract boolean execute(ScriptInstance scriptInstance, ScriptValue returnValue);

	/**
	 * Pops a variable off the stack and, using a temp variable, extracts a File/String.
	 * @param scriptInstance the script instance.
	 * @param temp the temporary script value.
	 * @return a File object.
	 */
	private static File popFile(ScriptInstance scriptInstance, ScriptValue temp) 
	{
		scriptInstance.popStackValue(temp);
		return getFile(temp);
	}

	/**
	 * Pops a variable off the stack and, using a temp variable, extracts a URL.
	 * @param scriptInstance the script instance.
	 * @param temp the temporary script value.
	 * @return a URL object.
	 */
	private static URL popURL(ScriptInstance scriptInstance, ScriptValue temp) throws MalformedURLException 
	{
		scriptInstance.popStackValue(temp);
		return getURL(temp);
	}

	// Get file.
	private static File getFile(ScriptValue temp) 
	{
		if (temp.isNull())
			return null;
		else if (temp.isObjectRef(File.class))
			return temp.asObjectType(File.class);
		else
			return new File(temp.asString());
	}
	
	// Get URL.
	private static URL getURL(ScriptValue temp) throws MalformedURLException 
	{
		if (temp.isNull())
			return null;
		else if (temp.isObjectRef(URL.class))
			return temp.asObjectType(URL.class);
		else
			return new URL(temp.asString());
	}
	
	// Scans a directory for a file (recursively).
	// If found, returns true and sets returnValue. 
	// If not, returns false.
	private static boolean scanDir(File dir, String name, boolean noExt, boolean caseSensitive, ScriptValue returnValue)
	{
		File[] dirFiles = dir.listFiles();
		if (dirFiles == null)
		{
			returnValue.setError("IOError", "Directory " + dir.getPath() + " could not be read.");
			return false;
		}
		
		for (File file : dirFiles)
		{
			if (file.isDirectory())
			{
				if (scanDir(file, name, noExt, caseSensitive, returnValue))
					return true;
			}
			else
			{
				if (noExt)
				{
					String filename = FileUtils.getFileNameWithoutExtension(file);
					if (caseSensitive && filename.equals(name))
					{
						returnValue.set(new File(file.getPath()));
						return true;
					}
					else if (!caseSensitive && filename.equalsIgnoreCase(name))
					{
						returnValue.set(new File(file.getPath()));
						return true;
					}
				}
				else
				{
					String filename = file.getName();
					if (caseSensitive && filename.equals(name))
					{
						returnValue.set(new File(file.getPath()));
						return true;
					}
					else if (!caseSensitive && filename.equalsIgnoreCase(name))
					{
						returnValue.set(new File(file.getPath()));
						return true;
					}
				}
			}
		}
		return false;
	}
	
	// Return value is file list.
	private static void copyDir(File base, File srcDir, File destDir, boolean recursive, FileFilter filter, ScriptValue returnValue)
	{
		ScriptValue fileValue = ScriptValue.create(null);
		
		File[] dirFiles = srcDir.listFiles();
		if (dirFiles == null)
		{
			returnValue.setError("IOError", "Directory " + srcDir.getPath() + " could not be read.");
			return;
		}
		
		for (File f : srcDir.listFiles())
		{
			String treeName = f.getPath().substring(base.getPath().length());
			if (f.isDirectory() && recursive)
			{
				copyDir(base, f, destDir, recursive, filter, returnValue);
				if (returnValue.isError())
					break;
			}
			else if (filter.accept(f))
			{
				copyFile(f, new File(destDir.getPath() + treeName), true, fileValue);
				if (fileValue.isError())
				{
					returnValue.set(fileValue);
					break;
				}
				returnValue.listAdd(fileValue);
			}
		}
	}
	
	// Return value is file.
	private static void copyFile(File srcFile, File destFile, boolean createDirs, ScriptValue returnValue) 
	{
		try (FileInputStream fis = new FileInputStream(srcFile)) 
		{
			writeFile(fis, destFile, createDirs, returnValue);
		} 
		catch (FileNotFoundException e) 
		{
			returnValue.setError("BadFile", e.getMessage(), e.getLocalizedMessage());
		}
		catch (IOException e) 
		{
			returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
		}
		catch (SecurityException e) 
		{
			returnValue.setError("Security", e.getMessage(), e.getLocalizedMessage());
		}
	}

	// Return value is file.
	private static void writeFile(InputStream in, File destFile, boolean createDirs, ScriptValue returnValue) 
	{
		if (createDirs && !FileUtils.createPathForFile(destFile))
		{
			returnValue.setError("IOError", "Could not create directories for target file: " + destFile.getPath());
			return;
		}
		try (FileOutputStream fos = new FileOutputStream(destFile)) 
		{
			IOUtils.relay(in, fos);
			returnValue.set(destFile);
		} 
		catch (FileNotFoundException e) 
		{
			returnValue.setError("BadFile", e.getMessage(), e.getLocalizedMessage());
		}
		catch (IOException e) 
		{
			returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
		}
		catch (SecurityException e) 
		{
			returnValue.setError("Security", e.getMessage(), e.getLocalizedMessage());
		}
	}

	private static ZipOutputStream reopenZipFile(File zipFile, ScriptValue returnValue)
	{
		File oldZipFile = new File(zipFile.getPath() + "._tmp");
		
		if (!zipFile.exists())
		{
			returnValue.setError("IOError", "Cannot append: Zip file does not exist.");
			return null;
		}
		
		if (!FileUtils.renameTimeout(zipFile, oldZipFile, 1000))
		{
			returnValue.setError("IOError", "Could not rename zip for reopen.");
			return null;
		}
		
		// Need to keep ZipOutputStream open if successful, and close in the "finally" if error.
		ZipOutputStream zout = null;
		try
		{
			zout = new ZipOutputStream(new FileOutputStream(zipFile));
			try (ZipFile zf = new ZipFile(oldZipFile))
			{
				ZipEntry entry;
				for (Enumeration<? extends ZipEntry> en = zf.entries(); en.hasMoreElements();)
				{
					entry = en.nextElement();
					zout.putNextEntry(entry);
					try (InputStream in = zf.getInputStream(entry))
					{
						IOUtils.relay(in, zout);
					}
					catch (IOException e)
					{
						returnValue.setError("IOError", "Could not read from reopened zip.");
						return null;
					}
					zout.closeEntry();
				}
			} 
			catch (ZipException e) 
			{
				returnValue.setError("BadZip", "Could not reopen zip: " + e.getLocalizedMessage());
				return null;
			}
			catch (IOException e) 
			{
				returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
				return null;
			}	
		} 
		catch (FileNotFoundException e) 
		{
			returnValue.setError("BadFile", e.getMessage(), e.getLocalizedMessage());
			return null;
		}
		finally
		{
			if (returnValue.isError())
			{
				IOUtils.close(zout);
				FileUtils.renameTimeout(oldZipFile, zipFile, 1000);
				oldZipFile.delete();
			}
			else
			{
				oldZipFile.delete();
			}
		}
		
		return zout;
	}
	
	private static void zipDir(ZipOutputStream zos, File base, File srcDir, String prefix, boolean compressed, FileFilter filter, ScriptValue returnValue)
	{
		if (!srcDir.exists())
		{
			returnValue.setError("BadDir", "Directory " + srcDir.getPath() + " does not exist.");
			return;
		}
		
		File[] dirFiles = srcDir.listFiles();
		if (dirFiles == null)
		{
			returnValue.setError("IOError", "Directory " + srcDir.getPath() + " could not be read.");
			return;
		}
		
		for (File f : dirFiles)
		{
			String treeName = prefix + "/" + f.getPath().substring(base.getPath().length() + 1);
			if (treeName.startsWith("/"))
				treeName = treeName.substring(1);
			
			if (f.isDirectory())
				zipDir(zos, base, f, prefix, compressed, filter, returnValue);
			else if (filter.accept(f))
				zipFile(zos, f, treeName, compressed, returnValue);
			
			if (returnValue.isError())
				break;
		}
	}
	
	private static void zipFile(ZipOutputStream zos, File srcFile, String entryName, boolean compressed, ScriptValue returnValue) 
	{
		ZipEntry entry = new ZipEntry(entryName);
		try (FileInputStream fis = new FileInputStream(srcFile))
		{
			zos.putNextEntry(entry);
			IOUtils.relay(fis, zos);
			zos.closeEntry();
		} 
		catch (FileNotFoundException e) 
		{
			returnValue.setError("BadFile", e.getMessage(), e.getLocalizedMessage());
		}
		catch (IOException e) 
		{
			returnValue.setError("IOError", e.getMessage(), e.getLocalizedMessage());
		}
		catch (SecurityException e) 
		{
			returnValue.setError("Security", e.getMessage(), e.getLocalizedMessage());
		}
	}

	private static void digestDirectory(MessageDigest digest, boolean recursive, File directory, ScriptValue returnValue) 
	{
		File[] dirFiles = directory.listFiles();
		if (dirFiles == null)
		{
			returnValue.setError("IOError", "Directory " + directory.getPath() + " could not be read.");
			return;
		}
		
		for (File f : directory.listFiles())
		{
			if (f.isDirectory() && recursive)
			{
				digestDirectory(digest, recursive, f, returnValue);
				if (returnValue.isError())
					return;
			}
			else 
			{
				digestFileInfo(digest, f, returnValue);
				if (returnValue.isError())
					return;
			}
		}
	}
	
	private static void digestFileInfo(MessageDigest digest, File file, ScriptValue returnValue) 
	{
		try
		{
			long len = file.length();
			long date = file.lastModified();
			digest.update(file.getPath().getBytes(UTF8));
			digest.update((byte)((len >> 0)  & 0x0ff));
			digest.update((byte)((len >> 8)  & 0x0ff));
			digest.update((byte)((len >> 16) & 0x0ff));
			digest.update((byte)((len >> 24) & 0x0ff));
			digest.update((byte)((len >> 32) & 0x0ff));
			digest.update((byte)((len >> 40) & 0x0ff));
			digest.update((byte)((len >> 48) & 0x0ff));
			digest.update((byte)((len >> 56) & 0x0ff));
			digest.update((byte)((date >> 0)  & 0x0ff));
			digest.update((byte)((date >> 8)  & 0x0ff));
			digest.update((byte)((date >> 16) & 0x0ff));
			digest.update((byte)((date >> 24) & 0x0ff));
			digest.update((byte)((date >> 32) & 0x0ff));
			digest.update((byte)((date >> 40) & 0x0ff));
			digest.update((byte)((date >> 48) & 0x0ff));
			digest.update((byte)((date >> 56) & 0x0ff));
		} 
		catch (SecurityException e) 
		{
			returnValue.setError("Security", e.getMessage(), e.getLocalizedMessage());
		}
	}

	// Threadlocal "stack" values.
	private static final ThreadLocal<ScriptValue> CACHEVALUE1 = ThreadLocal.withInitial(()->ScriptValue.create(null));
	private static final ThreadLocal<ScriptValue> CACHEVALUE2 = ThreadLocal.withInitial(()->ScriptValue.create(null));

}

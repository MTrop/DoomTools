package net.mtrop.doom.tools.doommake;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.struct.ReplacerReader;
import net.mtrop.doom.tools.struct.util.IOUtils;

/**
 * A project module definition for DoomMake. 
 * @author Matthew Tropiano
 */
public final class ProjectModuleDescriptor
{
	private static final String[] BLANK_RESOURCES = new String[0];

	/** Module file entries. */
	private List<Entry> entries;
	
	/** Entries for the addition of release script lines. */
	private List<Entry> releaseScriptEntries;
	
	/**
	 * A module entry.
	 */
	public static class Entry
	{
		private boolean appending;
		private boolean binary;
		private String outputPath;
		private boolean pathsAreContentLines;
		private String[] resourcePaths;
		
		private Entry(boolean appending, boolean binary, String outputPath, boolean pathsAreContentLines, String[] resourcePaths) 
		{
			super();
			this.appending = appending;
			this.binary = binary;
			this.outputPath = outputPath;
			this.pathsAreContentLines = pathsAreContentLines;
			this.resourcePaths = resourcePaths;
		}
		
		private boolean isDirectory()
		{
			return resourcePaths == null;
		}
	}
	
	/**
	 * Creates an entry for a module directory.
	 * @param directoryPath the output directory.
	 * @return the new entry.
	 */
	public static Entry dir(String directoryPath)
	{
		return new Entry(false, false, directoryPath, false, null);
	}
	
	/**
	 * Creates an entry for a blank module file.
	 * @param filePath the output file.
	 * @return the new entry.
	 */
	public static Entry file(String filePath)
	{
		return new Entry(false, false, filePath, false, BLANK_RESOURCES);
	}

	/**
	 * Creates an entry for a module file.
	 * @param filePath the output file.
	 * @param resources the series of resources to read and combine into a file.
	 * @return the new entry.
	 */
	public static Entry file(String filePath, String... resources)
	{
		return new Entry(false, false, filePath, false, resources);
	}
	
	/**
	 * Creates an entry for a module file (binary, not put through the replacer).
	 * @param filePath the output file.
	 * @param resources the series of resources to read and combine into a file.
	 * @return the new entry.
	 */
	public static Entry fileData(String filePath, String... resources)
	{
		return new Entry(false, true, filePath, false, resources);
	}
	
	/**
	 * Creates an entry for a module file.
	 * @param filePath the output file.
	 * @param lines the lines to add to the file.
	 * @return the new entry.
	 */
	public static Entry fileContent(String filePath, String... lines)
	{
		return new Entry(false, false, filePath, true, lines);
	}
	
	/**
	 * Creates an entry for a module file that appends to existing files.
	 * @param filePath the output file.
	 * @param resources the series of resources to read and combine into a file.
	 * @return the new entry.
	 */
	public static Entry fileAppend(String filePath, String... resources)
	{
		return new Entry(true, false, filePath, false, resources);
	}
	
	/**
	 * Creates an entry for a module file that appends to existing files.
	 * @param filePath the output file.
	 * @param lines the lines to add to the file.
	 * @return the new entry.
	 */
	public static Entry fileContentAppend(String filePath, String... lines)
	{
		return new Entry(true, false, filePath, true, lines);
	}
	
	/**
	 * Creates a new module.
	 * @param entries the module entries.
	 * @return the new module.
	 */
	public static ProjectModuleDescriptor descriptor(Entry... entries)
	{
		return new ProjectModuleDescriptor(entries);
	}

	/**
	 * Creates this module in a target directory.
	 * @param entries the list of module entries.
	 * @param directory the directory.
	 * @param replacerMap the parameter map for replace tokens.
	 * @throws IOException if a problem happens while creating the module.
	 */
	public static void createIn(List<Entry> entries, File directory, Map<String, String> replacerMap) throws IOException
	{
		if (directory.exists())
		{
			if (!directory.isDirectory())
				throw new IOException("Target is not a directory: " + directory.getPath());
		}
		
		for (Entry e : entries)
		{
			String targetPath = directory.getPath() + File.separator + e.outputPath;
			if (e.isDirectory())
			{
				if (!Common.createPath(targetPath))
					throw new IOException("Could not create necessary directory: " + targetPath);
			}
			else
			{
				if (!Common.createPathForFile(targetPath))
					throw new IOException("Could not create necessary directory for file: " + targetPath);
				if (e.pathsAreContentLines)
				{
					try (PrintStream ps = new PrintStream(new FileOutputStream(targetPath, e.appending)))
					{
						for (String resourcePath : e.resourcePaths)
						{
							ps.println(resourcePath);
						}						
					}					
				}
				else
				{
					try (FileOutputStream fos = new FileOutputStream(targetPath, e.appending))
					{
						for (String resourcePath : e.resourcePaths)
						{
							if (e.binary)
							{
								try (
									InputStream fin = Common.openResource(resourcePath); 
								){
									IOUtils.relay(fin, fos);
								}
							}
							else
							{
								try (
									ReplacerReader reader = new ReplacerReader(Common.openResourceReader(resourcePath), "{{", "}}"); 
									OutputStreamWriter writer = new OutputStreamWriter(fos)
								){
									reader.replace(replacerMap);
									IOUtils.relay(reader, writer, 8192);
								}
							}
						}
					}
				}
			}
		}
	}
	
	// New project module.
	private ProjectModuleDescriptor(Entry... entries)
	{
		this.entries = new LinkedList<>();
		addEntries(entries);
	}
	
	// Add module entries to this one.
	private void addEntries(Entry... entries)
	{
		for (Entry entry : entries)
			this.entries.add(entry);
	}
	
	public ProjectModuleDescriptor releaseScriptLineEntry(Entry entry)
	{
		this.releaseScriptEntries.add(entry);
		return this;
	}
	
	/**
	 * Creates this module in a target directory.
	 * @param directory the directory.
	 * @param replacerMap the parameter map for replace tokens.
	 * @throws IOException if a problem happens while creating the module.
	 */
	public void createIn(File directory, Map<String, String> replacerMap) throws IOException
	{
		createIn(entries, directory, replacerMap);
	}

}

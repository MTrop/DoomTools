package net.mtrop.doom.tools.doommake;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import net.mtrop.doom.struct.io.IOUtils;
import net.mtrop.doom.tools.common.Common;

/**
 * A project module definition for DoomMake. 
 * @author Matthew Tropiano
 */
public final class ProjectModule 
{
	private static final String[] BLANK_RESOURCES = new String[0];
	
	/** Module file entries. */
	private List<Entry> entries;
	
	/**
	 * A module entry.
	 */
	public static class Entry
	{
		private boolean appending;
		private String outputPath;
		private String[] resourcePaths;
		
		private Entry(boolean appending, String outputPath, String[] resourcePaths) 
		{
			super();
			this.appending = appending;
			this.outputPath = outputPath;
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
		return new Entry(false, directoryPath, null);
	}
	
	/**
	 * Creates an entry for a blank module file.
	 * @param filePath the output file.
	 * @return the new entry.
	 */
	public static Entry file(String filePath)
	{
		return new Entry(false, filePath, BLANK_RESOURCES);
	}

	/**
	 * Creates an entry for a module file.
	 * @param filePath the output file.
	 * @param resources the series of resources to read and combine into a file.
	 * @return the new entry.
	 */
	public static Entry file(String filePath, String... resources)
	{
		return new Entry(false, filePath, resources);
	}
	
	/**
	 * Creates an entry for a module file that appends to existing files.
	 * @param filePath the output file.
	 * @param resources the series of resources to read and combine into a file.
	 * @return the new entry.
	 */
	public static Entry fileAppend(String filePath, String... resources)
	{
		return new Entry(true, filePath, resources);
	}
	
	/**
	 * Creates a new module.
	 * @param entries the module entries.
	 * @return the new module.
	 */
	public static ProjectModule create(Entry... entries)
	{
		return new ProjectModule(entries);
	}

	// New project module.
	private ProjectModule(Entry... entries)
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
	
	// Add module entries to this one.
	private void addEntries(Iterable<Entry> entries)
	{
		for (Entry entry : entries)
			this.entries.add(entry);
	}
	
	/**
	 * Adds a module's entries to this one.
	 * @param module the module to combine with this one.
	 * @return this module.
	 */
	public ProjectModule add(ProjectModule module)
	{
		addEntries(module.entries);
		return this;
	}
	
	/**
	 * Creates this module in a target directory.
	 * @param directory the directory.
	 * @throws IOException if a problem happens while creating the module.
	 */
	public void createIn(File directory) throws IOException
	{
		if (directory.exists())
		{
			if (!directory.isDirectory())
				throw new IOException("Target is not a directory: " + directory.getPath());
			if (directory.listFiles().length == 0)
				throw new IOException("Target is not an empty directory: " + directory.getPath());
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
				try (FileOutputStream fos = new FileOutputStream(targetPath, e.appending))
				{
					for (String resourcePath : e.resourcePaths)
					{
						try (InputStream in = Common.openResource(resourcePath))
						{
							if (in == null)
								throw new IOException("INTERNAL ERROR: Could not find resource: " + resourcePath);
							IOUtils.relay(in, fos);
						}
					}
				}
			}
		}
	}
	
}

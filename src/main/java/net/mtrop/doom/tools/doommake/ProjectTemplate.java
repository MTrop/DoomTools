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
 * A project template definition for DoomMake. 
 * @author Matthew Tropiano
 */
public final class ProjectTemplate 
{
	private static final String[] BLANK_RESOURCES = new String[0];
	
	/** Template description. */
	private String description;
	/** Template file entries. */
	private List<Entry> entries;
	
	/**
	 * A template entry.
	 */
	public static class Entry
	{
		private String outputPath;
		private String[] resourcePaths;
		
		private Entry(String outputPath, String[] resourcePaths) 
		{
			super();
			this.outputPath = outputPath;
			this.resourcePaths = resourcePaths;
		}
		
		private boolean isDirectory()
		{
			return resourcePaths == null;
		}
	}
	
	/**
	 * Creates an entry for a template directory.
	 * @param directoryPath the output directory.
	 * @return the new entry.
	 */
	public static Entry directory(String directoryPath)
	{
		return new Entry(directoryPath, null);
	}
	
	/**
	 * Creates an entry for a blank template file.
	 * @param filePath the output file.
	 * @return the new entry.
	 */
	public static Entry file(String filePath)
	{
		return new Entry(filePath, BLANK_RESOURCES);
	}

	/**
	 * Creates an entry for a template file.
	 * @param filePath the output file.
	 * @param resources the series of resources to read and combine into a file.
	 * @return the new entry.
	 */
	public static Entry file(String filePath, String... resources)
	{
		return new Entry(filePath, resources);
	}
	
	/**
	 * Creates a new template.
	 * @param description the template description.
	 * @param entries the template entries.
	 * @return the new template.
	 */
	public static ProjectTemplate create(String description, Entry... entries)
	{
		return new ProjectTemplate(description, entries);
	}

	/**
	 * Creates a new template.
	 * @param entries the template entries.
	 * @return the new template.
	 */
	public static ProjectTemplate create(Entry... entries)
	{
		return create(null, entries);
	}

	// New project template.
	private ProjectTemplate(String description, Entry... entries)
	{
		this.description = description;
		this.entries = new LinkedList<>();
		addEntries(entries);
	}
	
	// Add template entries to this one.
	private void addEntries(Entry... entries)
	{
		for (Entry entry : entries)
			this.entries.add(entry);
	}
	
	// Add template entries to this one.
	private void addEntries(Iterable<Entry> entries)
	{
		for (Entry entry : entries)
			this.entries.add(entry);
	}
	
	/**
	 * Adds a template's entries to this one.
	 * @param template the template to combine with this one.
	 * @return this template.
	 */
	public ProjectTemplate add(ProjectTemplate template)
	{
		addEntries(template.entries);
		return this;
	}
	
	/**
	 * @return this template's description.
	 */
	public String getDescription()
	{
		return description;
	}
	
	/**
	 * Creates this template in a target directory.
	 * @param directory the directory.
	 * @throws IOException if a problem happens while creating the template.
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
				try (FileOutputStream fos = new FileOutputStream(targetPath))
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

package net.mtrop.doom.tools.doommake;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * A project template definition for DoomMake.
 * @author Matthew Tropiano
 */
public final class ProjectTemplate implements Comparable<ProjectTemplate>, Iterable<String>
{
	/** Category. */
	private String category;
	/** Name. */
	private String name;
	/** Descriptor. */
	private String description;
	/** Module file entries. */
	private Set<String> moduleNames;
	
	/**
	 * Creates a new template.
	 * @param name the name of the template.
	 * @param category the template category.
	 * @param description the description.
	 * @param modules the template module names.
	 * @return the new template.
	 */
	public static ProjectTemplate template(String name, String category, String description, String... modules)
	{
		return new ProjectTemplate(name, category, description, modules);
	}

	// New project module.
	private ProjectTemplate(String name, String category, String description, String... modules)
	{
		this.category = category;
		this.name = name;
		this.description = description;
		this.moduleNames = new TreeSet<>();
		addModules(modules);
	}
	
	// Add module entries to this one.
	private void addModules(String... modules)
	{
		for (String module : modules)
			this.moduleNames.add(module);
	}
	
	/**
	 * @return the category name.
	 */
	public String getCategory()
	{
		return category;
	}
	
	/**
	 * @return the name.
	 */
	public String getName() 
	{
		return name;
	}
	
	/**
	 * @return the description.
	 */
	public String getDescription()
	{
		return description;
	}

	@Override
	public int compareTo(ProjectTemplate template) 
	{
		return name.compareTo(template.name);
	}

	@Override
	public Iterator<String> iterator()
	{
		return moduleNames.iterator();
	}
	
}

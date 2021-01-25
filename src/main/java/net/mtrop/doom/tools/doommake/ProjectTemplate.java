package net.mtrop.doom.tools.doommake;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A template descriptor.
 * @author Matthew Tropiano
 */
public class ProjectTemplate implements Comparable<ProjectTemplate>
{
	/** Descriptor name. */
	private String name;
	/** Descriptor description. */
	private String description;
	/** Order added. */
	private List<ProjectModule> moduleList;

	/** Quick lookup for added modules so that included templates are not re-added. */
	private Set<ProjectModule> moduleSet;

	private ProjectTemplate(String name, String description)
	{
		this.name = Objects.requireNonNull(name);
		this.description = Objects.requireNonNull(description);
		this.moduleList = new LinkedList<>();
		this.moduleSet = new HashSet<>();
	}
	
	/**
	 * Creates a new {@link ProjectTemplate}.
	 * @param name the name.
	 * @param description the description.
	 * @return a new descriptor.
	 */
	public static ProjectTemplate build(String name, String description)
	{
		return new ProjectTemplate(name, description);
	}
	
	public String getName() 
	{
		return name;
	}
	
	public String getDescription() 
	{
		return description;
	}
	
	/**
	 * Adds a template to this descriptor.
	 * It cannot be added more than once, and are exported/applied in the order added.
	 * @param template the template to add.
	 * @return this descriptor.
	 */
	public ProjectTemplate add(ProjectModule template)
	{
		if (!moduleSet.contains(template))
			moduleList.add(template);
		return this;
	}

	/**
	 * Creates the templates in this descriptor in a target directory.
	 * @param directory the directory.
	 * @throws IOException if a problem happens while creating the template.
	 */
	public void createIn(File directory) throws IOException
	{
		for (ProjectModule template : moduleList)
			template.createIn(directory);
	}

	@Override
	public int compareTo(ProjectTemplate o)
	{
		return name.compareTo(o.name);
	}
	
}

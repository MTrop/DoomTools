package net.mtrop.doom.tools.doommake;

import java.util.Arrays;
import java.util.List;

/**
 * A set of module data.
 * @author Matthew Tropiano
 */
public class ProjectModule implements Comparable<ProjectModule>
{
	/** Sort bias. */
	private int sort;

	/** Descriptor. */
	private ProjectModuleDescriptor descriptor;
	/** The release template fragments. */
	private ProjectModuleDescriptor releaseScript;
	/** The release template fragments. */
	private ProjectModuleDescriptor releaseScriptMerge;
	/** The release WadMerge line. */
	private List<String> releaseWadMergeLines;
	/** The post-release add-on template fragments. */
	private ProjectModuleDescriptor postRelease;
	/** The replacer list for each module. */
	private List<ProjectTokenReplacer> replacers;
	/** TODOs for included modules. */
	private List<String> todos;
	
	private ProjectModule(int sort)
	{
		this.sort = sort;
		
		this.descriptor = null;
		this.releaseScript = null;
		this.releaseScriptMerge = null;
		this.releaseWadMergeLines = null;
		this.postRelease = null;
		this.replacers = null;
		this.todos = null;
	}
	
	/**
	 * Creates a new project module.
	 * @param sort the sort value.
	 * @param name the module name.
	 * @param descriptor the main descriptor for files and directories.
	 * @return a new module.
	 */
	public static ProjectModule module(int sort)
	{
		return new ProjectModule(sort);
	}
	
	public ProjectModule base(ProjectModuleDescriptor descriptor)
	{
		this.descriptor = descriptor;
		return this;
	}
	
	public ProjectModule releaseScript(ProjectModuleDescriptor releaseScript)
	{
		this.releaseScript = releaseScript;
		return this;
	}
	
	public ProjectModule releaseScriptMerge(ProjectModuleDescriptor releaseScriptMerge)
	{
		this.releaseScriptMerge = releaseScriptMerge;
		return this;
	}
	
	public ProjectModule releaseWadMergeLines(String... releaseWadMergeLines)
	{
		this.releaseWadMergeLines = Arrays.asList(releaseWadMergeLines);
		return this;
	}
	
	public ProjectModule postRelease(ProjectModuleDescriptor postRelease)
	{
		this.postRelease = postRelease;
		return this;
	}
	
	public ProjectModule replacers(ProjectTokenReplacer... replacers)
	{
		this.replacers = Arrays.asList(replacers);
		return this;
	}
	
	public ProjectModule todos(String... todos)
	{
		this.todos = Arrays.asList(todos);
		return this;
	}

	public ProjectModuleDescriptor getDescriptor() 
	{
		return descriptor;
	}

	public ProjectModuleDescriptor getReleaseScript() 
	{
		return releaseScript;
	}

	public ProjectModuleDescriptor getReleaseScriptMerge() 
	{
		return releaseScriptMerge;
	}

	public List<String> getReleaseWadMergeLines() 
	{
		return releaseWadMergeLines;
	}

	public ProjectModuleDescriptor getPostRelease() 
	{
		return postRelease;
	}

	public List<ProjectTokenReplacer> getReplacers() 
	{
		return replacers;
	}

	public List<String> getTodos()
	{
		return todos;
	}

	@Override
	public int compareTo(ProjectModule pm)
	{
		return sort - pm.sort;
	}
	
}

package net.mtrop.doom.tools.doommake;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.mtrop.doom.tools.exception.UtilityException;
import net.mtrop.doom.tools.struct.util.IOUtils;

/**
 * Class used for generating project structures.
 * Mostly for moving this out of the main class.
 * @author Matthew Tropiano
 */
public abstract class ProjectGenerator
{
	/**
	 * @return the set of valid category names.
	 */
	public abstract Set<String> getCategoryNames();
	
	/**
	 * @param categoryName the category name.
	 * @return the set of template names by template category.
	 */
	public abstract Set<ProjectTemplate> getTemplatesByCategory(String categoryName);
	
	/**
	 * @return the set of valid template names.
	 */
	public abstract Set<String> getTemplateNames();
	
	/**
	 * Gets a project template by name.
	 * @param name the template name.
	 * @return the corresponding template, or null.
	 */
	public abstract ProjectTemplate getTemplate(String name);
	
	/**
	 * @return the set of valid module names.
	 */
	public abstract Set<String> getModuleNames();
	
	/**
	 * Gets a project module by name.
	 * @param name the module name.
	 * @return the corresponding module, or null.
	 */
	public abstract ProjectModule getModule(String name);
	
	/**
	 * Creates the project from all of the provided modules.
	 * @param selected the selected modules.
	 * @param replacerMap the replacer map for the text replacers. 
	 * @param targetDirectory the target directory for the project.
	 * @throws IOException if an I/O error happens.
	 */
	public abstract void createProject(SortedSet<ProjectModule> selected, Map<String, String> replacerMap, File targetDirectory) throws IOException;
	
	@SafeVarargs
	protected static <T> List<T> list(T ... items)
	{
		List<T> out =  new LinkedList<>();
		for (T t : items)
			out.add(t);
		return out;
	}

	@SafeVarargs
	protected static <T> Set<T> set(T ... items)
	{
		Set<T> out =  new HashSet<>();
		for (T t : items)
			out.add(t);
		return out;
	}

	/**
	 * Gets the set of selected modules from a list of templates.
	 * The modules (returned in a sorted order) are the modules to use for creating a project layout.
	 * @param templateNameList the list of template names.
	 * @return the set of selected modules from all of the templates.
	 * @throws UtilityException if one of the template names would cause a problem (no such name, repeat category). 
	 */
	public SortedSet<ProjectModule> getSelectedModules(Collection<String> templateNameList) throws UtilityException
	{
		// Get Modules.
		SortedSet<ProjectModule> selected = new TreeSet<>();
		Set<String> selectedCategories = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		
		// Get Templates.
		for (String name : templateNameList)
		{
			ProjectTemplate found;
			if ((found = getTemplate(name)) == null)
				throw new UtilityException("No such project template: " + name + "\nRun DoomMake with `--list-templates` for a list of available templates.");
			if (selectedCategories.contains(found.getCategory()))
				throw new UtilityException("Already included a template from category: " + found.getCategory() + ".");

			selectedCategories.add(found.getCategory());
			
			for (String modname : found)
			{
				ProjectModule modfound;
				if ((modfound = getModule(modname)) == null)
					throw new UtilityException("INTERNAL: No such project module: " + modname + "!!");
				selected.add(modfound);
			}
		}
		
		return selected;
	}

	// Returns modules.
	public static List<String> getTODOs(SortedSet<ProjectModule> selected)
	{
		List<String> out = new LinkedList<>();
		for (ProjectModule module : selected)
		{
			List<String> todos = module.getTodos();
			if (todos != null)
				out.addAll(todos);
		}
		return out;
	}

	/**
	 * Get all replacers to use from the set of selected modules.
	 * @param selected the selected modules.
	 * @return the list of replacers.
	 */
	public static List<ProjectTokenReplacer> getReplacers(SortedSet<ProjectModule> selected)
	{
		List<ProjectTokenReplacer> out = new LinkedList<>();
		
		for (ProjectModule module : selected)
		{
			List<ProjectTokenReplacer> replacers = module.getReplacers();
			if (replacers != null) for (ProjectTokenReplacer replacer : replacers)
			{
				if (!out.contains(replacer))
					out.add(replacer);
			}
		}
		
		return out;
	}

	/**
	 * Runs through the list of replacers, prompting the user for each replacer.
	 * @param replacerList the list of replacers.
	 * @param stdout the stream for prompt output.
	 * @param stdin the stream for reading input.
	 * @return the map of replacer tokens.
	 * @throws IOException if a stream can't be read or written to.
	 */
	public static Map<String, String> consoleReplacer(List<ProjectTokenReplacer> replacerList, PrintStream stdout, InputStream stdin) throws IOException
	{
		Map<String, String> out = new HashMap<>();
		try (BufferedReader br = IOUtils.openTextStream(stdin))
		{
			for (ProjectTokenReplacer replacer : replacerList)
				insertReplacer(replacer, stdout, br, out);
		}
		return out;
	}

	/**
	 * Prompts for a replacer value and if valid, puts it in the <code>outputMap</code>.
	 * @param replacer the replacer to use.
	 * @param out the ouptut stream for the prompt.
	 * @param reader the reader for reading input.
	 * @param outputMap the output map to add replacer token values to.
	 * @throws IOException if a stream can't be read or written to.
	 */
	public static void insertReplacer(ProjectTokenReplacer replacer, PrintStream out, BufferedReader reader, Map<String, String> outputMap) throws IOException
	{
		String value;
		String key = replacer.getToken();
		while (true)
		{
			out.print(replacer.getPrompt() + " ");
			if ((value = reader.readLine()) != null)
			{
				value = replacer.getSanitizer().apply(value.trim());
				
				String error;
				if ((error = replacer.getValidator().apply(value)) != null)
				{
					out.println("ERROR: " + error);
				}
				else if (value.length() == 0)
				{
					outputMap.put(key, replacer.getDefaultValue());
					return; 
				}
				else
				{
					outputMap.put(key, value);
					return; 
				}
			}
			else
			{
				break;
			}
		}
	}
	
}

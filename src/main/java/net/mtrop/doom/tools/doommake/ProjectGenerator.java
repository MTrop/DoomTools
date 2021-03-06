package net.mtrop.doom.tools.doommake;

import static net.mtrop.doom.tools.doommake.ProjectTemplate.template;

import static net.mtrop.doom.tools.doommake.ProjectModule.module;
import static net.mtrop.doom.tools.doommake.ProjectModule.dir;
import static net.mtrop.doom.tools.doommake.ProjectModule.file;
import static net.mtrop.doom.tools.doommake.ProjectModule.fileAppend;
import static net.mtrop.doom.tools.doommake.ProjectModule.fileContentAppend;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import net.mtrop.doom.tools.common.Common;
import net.mtrop.doom.tools.exception.UtilityException;

/**
 * Class used for generating project structures.
 * Mostly for moving this out of the main class.
 * @author Matthew Tropiano
 */
public final class ProjectGenerator
{
	private static final String CATEGORY_REPOSITORY = "Repositories";
	private static final String CATEGORY_PATCHES = "Patches";
	private static final String CATEGORY_ASSETS = "Assets";
	private static final String CATEGORY_EXECUTION = "Execution";

	private static final String TEMPLATE_GIT = "git";
	private static final String TEMPLATE_MERCURIAL = "hg";
	private static final String TEMPLATE_BASE = "base";
	private static final String TEMPLATE_MAPS = "maps";
	private static final String TEMPLATE_ASSETS = "assets";
	private static final String TEMPLATE_TEXTURES = "textures";
	private static final String TEMPLATE_ASSETS_TEXTURES = "assets-textures";
	private static final String TEMPLATE_MAPS_ASSETS = "maps-assets";
	private static final String TEMPLATE_MAPS_TEXTURES = "maps-textures";
	private static final String TEMPLATE_MAPS_TEXTUREWADS = "maps-texturewads";
	private static final String TEMPLATE_MAPS_ASSETS_TEXTURES = "maps-assets-textures";
	private static final String TEMPLATE_MAPS_ASSETS_TEXTUREWADS = "maps-assets-texturewads";
	private static final String TEMPLATE_DECOHACK = "decohack";
	private static final String TEMPLATE_RUN = "run";

	private static final String MODULE_GIT = "git";
	private static final String MODULE_MERCURIAL = "hg";
	private static final String MODULE_INIT = "init";
	private static final String MODULE_BASE = "bare";
	private static final String MODULE_DECOHACK = "decohack";
	private static final String MODULE_MAPS = "maps";
	private static final String MODULE_MAPTEX = "maptex";
	private static final String MODULE_ASSETS = "assets";
	private static final String MODULE_TEXTURES = "textures";
	private static final String MODULE_TEXTURES_MAPS = "textures-maps";
	private static final String MODULE_TEXTUREWADS = "texturewads";
	private static final String MODULE_RUN = "run";

	/** The main templates. */
	private static final SortedMap<String, ProjectTemplate> TEMPLATES;
	/** The main modules. */
	private static final Map<String, ProjectModule> MODULES;
	/** The release template fragments. */
	private static final Map<String, ProjectModule> RELEASE_SCRIPT;
	/** The release template fragments. */
	private static final Map<String, ProjectModule> RELEASE_SCRIPT_MERGE;
	/** The release WadMerge line. */
	private static final Map<String, String> RELEASE_WADMERGE_LINE;
	/** The post-release add-on template fragments. */
	private static final Map<String, ProjectModule> POST_RELEASE;
	/** The replacer list for each module. */
	private static final Map<String, List<ProjectTokenReplacer>> REPLACER_LISTS;
	/** TODOs for included modules. */
	private static final Map<String, List<String>> POST_CREATE_TODOS;

	/** Project name replacer. */
	private static final ProjectTokenReplacer REPLACER_PROJECT_NAME = ProjectTokenReplacer.create(
		"PROJECT_NAME", "What is your project's WAD name (blank for \"PROJECT\")?", "PROJECT"
	);
	
	/** IWAD path replacer. */
	private static final ProjectTokenReplacer REPLACER_PROJECT_IWAD = ProjectTokenReplacer.create(
		"PROJECT_IWAD", "Path to project IWAD (blank to skip)?", "", (path) -> {
			return path.replace("\\", "/");
		}, (path) -> {
			if (path.trim().length() == 0)
				return null;
			if (!new File(path).exists())
				return "IWAD path not found!";
			return null;
		}
	);

	/** DECOHack base type replacer. */
	private static final ProjectTokenReplacer REPLACER_PROJECT_DECOHACK = ProjectTokenReplacer.create(
		"DECOHACK_BASE", "Patch type for DECOHack (doom19, udoom19, boom, mbf, extended, mbf21) (blank for \"doom19\")?", "doom19", (type) -> {
			if (type.trim().length() == 0)
				return null;
			if (!set("doom19", "udoom19", "boom", "mbf", "extended", "mbf21").contains(type))
				return "Bad type name.";
			return null;
		}
	);

	/** EXE path replacer. */
	private static final ProjectTokenReplacer REPLACER_PROJECT_RUN_EXE_PATH = ProjectTokenReplacer.create(
		"PROJECT_EXE_PATH", "EXE path for testing (blank to skip)?", "", (path) -> {
			return path.replace("\\", "/");
		}, (path) -> {
			if (path.trim().length() == 0)
				return null;
			if (!new File(path).exists())
				return "EXE path not found!";
			return null;
		}
	);

	/** EXE working directory replacer. */
	private static final ProjectTokenReplacer REPLACER_PROJECT_RUN_EXE_WORKDIR = ProjectTokenReplacer.create(
		"PROJECT_EXE_WORKDIR", "EXE working directory (blank to use EXE dir)?", "", (path) -> {
			return path.replace("\\", "/");
		}, (path) -> {
			if (path.trim().length() == 0)
				return null;
			if (!new File(path).isDirectory())
				return "Path not found, or is not a directory.";
			return null;
		}
	);

	/** EXE IWAD switch replacer. */
	private static final ProjectTokenReplacer REPLACER_PROJECT_RUN_SWITCH_IWAD = ProjectTokenReplacer.create(
		"PROJECT_EXE_SWITCH_IWAD", "EXE IWAD switch (blank to use \"-iwad\")?", ""
	);

	/** EXE file switch replacer. */
	private static final ProjectTokenReplacer REPLACER_PROJECT_RUN_SWITCH_FILE = ProjectTokenReplacer.create(
		"PROJECT_EXE_SWITCH_FILE", "EXE file switch (blank to use \"-file\")?", ""
	);

	/** EXE DEH switch replacer. */
	private static final ProjectTokenReplacer REPLACER_PROJECT_RUN_SWITCH_DEH = ProjectTokenReplacer.create(
		"PROJECT_EXE_SWITCH_DEH", "EXE DEH file switch (blank to use \"-deh\")?", ""
	);


	static
	{
		TEMPLATES = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		MODULES = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		RELEASE_SCRIPT = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		RELEASE_SCRIPT_MERGE = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		RELEASE_WADMERGE_LINE = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		POST_RELEASE = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		REPLACER_LISTS = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		POST_CREATE_TODOS = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		// ................................................................

		// Adds files for Git repository support.
		MODULES.put(MODULE_GIT,
			module(200, MODULE_GIT,
				file(".gitignore", 
					"doommake/git/gitignore.txt"),
				file(".gitattributes", 
					"doommake/git/gitattributes.txt")
			)
		);
		POST_CREATE_TODOS.put(MODULE_GIT, list(
			"Open `.gitignore` and verify what you want ignored in the project."
		));

		// ................................................................

		// Adds files for Mercurial repository support.
		MODULES.put(MODULE_MERCURIAL,
			module(200, MODULE_MERCURIAL,
				file(".hgignore", 
					"doommake/hg/hgignore.txt")
			)
		);
		POST_CREATE_TODOS.put(MODULE_MERCURIAL, list(
			"Open `.hgignore` and verify what you want ignored in the project."
		));

		// ................................................................

		MODULES.put(MODULE_INIT, 
			module(0, MODULE_INIT,
				dir("build"),
				dir("dist"),
				file("src/wadinfo.txt", 
					"doommake/common/wadinfo.txt"),
				file("src/credits.txt", 
					"doommake/common/credits.txt"),
				file("doommake.script",
					"doommake/doommake.script"),
				file("scripts/doommake-init.script",
					"doommake/doommake-init.script"),
				file("scripts/doommake-lib.script",
					"doommake/doommake-lib.script"),
				file("doommake.properties",
					"doommake/doommake.properties"),
				file("doommake.project.properties",
					"doommake/doommake.project.properties"),
				file("README.md",
					"doommake/README.md")
			)
		);
		POST_RELEASE.put(MODULE_INIT,
			module(
				fileAppend("doommake.script",
					"doommake/doommake-target.script"
				)
			)
		);
		REPLACER_LISTS.put(MODULE_INIT, list(
			REPLACER_PROJECT_NAME, 
			REPLACER_PROJECT_IWAD
		));
		POST_CREATE_TODOS.put(MODULE_INIT, list(
			"Modify README.md and describe your project."
		));
		
		// ................................................................

		MODULES.put(MODULE_BASE, 
			module(100, MODULE_BASE)
		);
		RELEASE_SCRIPT.put(MODULE_BASE,
			module(
				fileContentAppend("doommake.script",
					"\t// Add other resource constructors here."
				)
			)
		);
		RELEASE_WADMERGE_LINE.put(MODULE_BASE,
			"# Add resource merging here."
		);
		POST_CREATE_TODOS.put(MODULE_BASE, list(
			"Add resources to merge to the WadMerge script."
			,"Add targets to the doommake.script script."
		));

		// ................................................................

		// A module that compiles a DeHackEd patch.
		MODULES.put(MODULE_DECOHACK,
			module(1, MODULE_DECOHACK,
				file("src/decohack/main.dh",
					"doommake/decohack/main.dh"),
				fileAppend("doommake.properties",
					"doommake/decohack/doommake.properties"),
				fileAppend("doommake.script", 
					"doommake/decohack/doommake.script"),
				fileAppend("README.md",
					"doommake/decohack/README.md")
			)
		);
		RELEASE_SCRIPT.put(MODULE_DECOHACK,
			module(
				fileContentAppend("doommake.script",
					"\tdoPatch(false);"
				)
			)
		);
		RELEASE_SCRIPT_MERGE.put(MODULE_DECOHACK,
			module(
				fileContentAppend("doommake.script",
					"\t\t,getPatchFile()"
				)
			)
		);
		RELEASE_WADMERGE_LINE.put(MODULE_DECOHACK,
			"mergefile  out $0/$"
		);
		POST_RELEASE.put(MODULE_DECOHACK,
			module(
				fileAppend("doommake.script",
					"doommake/decohack/doommake-target.script"
				)
			)
		);
		REPLACER_LISTS.put(MODULE_DECOHACK, list(
			REPLACER_PROJECT_DECOHACK
		));
		POST_CREATE_TODOS.put(MODULE_DECOHACK, list(
			"Modify `src/decohack/main.dh` to your liking."
		));

		// ................................................................

		// A module that builds a set of maps.
		MODULES.put(MODULE_MAPS,
			module(2, MODULE_MAPS,
				dir("src/maps"),
				file("scripts/merge-maps.txt",
					"doommake/common/maps/wadmerge.txt"),
				fileAppend("doommake.properties",
					"doommake/common/maps/doommake.properties"),
				fileAppend("doommake.script", 
					"doommake/common/maps/doommake.script"),
				fileAppend("README.md",
					"doommake/common/maps/README.md")
			)
		);
		RELEASE_SCRIPT.put(MODULE_MAPS,
			module(
				fileContentAppend("doommake.script",
					"\tdoMaps();"
				)
			)
		);
		RELEASE_SCRIPT_MERGE.put(MODULE_MAPS,
			module(
				fileContentAppend("doommake.script",
					"\t\t,getMapsWad()"
				)
			)
		);
		RELEASE_WADMERGE_LINE.put(MODULE_MAPS,
			"mergewad   out $0/$"
		);
		POST_RELEASE.put(MODULE_MAPS,
			module(
				fileAppend("doommake.script",
					"doommake/common/maps/doommake-target.script"
				)
			)
		);
		POST_CREATE_TODOS.put(MODULE_MAPS, list(
			"Add maps to `src/maps`."
		));

		// ................................................................

		// A module that builds maps and non-texture assets together.
		MODULES.put(MODULE_ASSETS,
			module(3, MODULE_ASSETS,
				dir("src/assets/_global"),
				dir("src/assets/graphics"),
				dir("src/assets/music"),
				dir("src/assets/sounds"),
				dir("src/assets/sprites"),
				file("scripts/merge-assets.txt",
					"doommake/common/assets/wadmerge.txt"),
				fileAppend("doommake.properties", 
					"doommake/common/assets/doommake.properties"),
				fileAppend("doommake.script",
					"doommake/common/assets/doommake.script"),
				fileAppend("README.md",
					"doommake/common/assets/README.md")
			)
		);
		RELEASE_SCRIPT.put(MODULE_ASSETS,
			module(
				fileContentAppend("doommake.script",
					"\tdoAssets();"
				)
			)
		);
		RELEASE_SCRIPT_MERGE.put(MODULE_ASSETS,
			module(
				fileContentAppend("doommake.script",
					"\t\t,getAssetsWAD()"
				)
			)
		);
		RELEASE_WADMERGE_LINE.put(MODULE_ASSETS,
			"mergewad   out $0/$"
		);
		POST_RELEASE.put(MODULE_ASSETS,
			module(
				fileAppend("doommake.script",
					"doommake/common/assets/doommake-target.script"
				)
			)
		);
		POST_CREATE_TODOS.put(MODULE_ASSETS, list(
			"Add assets to `src/assets` into the appropriate folders."
		));

		// ................................................................

		// A module that adds MapTex stuff.
		MODULES.put(MODULE_MAPTEX,
			module(4, MODULE_MAPTEX,
				fileAppend("doommake.properties",
					"doommake/common/maptex/doommake.properties"),
				fileAppend("doommake.script", 
					"doommake/common/maptex/doommake.script")
			)
		);

		// ................................................................

		// A module that builds texture WADs.
		// If this is used, do NOT use the "texturewads" module.
		MODULES.put(MODULE_TEXTURES,
			module(5, MODULE_TEXTURES,
				dir("src/textures/flats"),
				dir("src/textures/patches"),
				file("scripts/merge-textures.txt",
					"doommake/common/textures/wadmerge.txt"),
				file("src/textures/texture1.txt", 
					"doommake/common/textures/texture1.txt"),
				file("src/textures/texture2.txt", 
					"doommake/common/textures/texture2.txt"),
				fileAppend("doommake.properties", 
					"doommake/common/textures/doommake.properties"),
				fileAppend("doommake.script", 
					"doommake/common/textures/doommake.script"),
				fileAppend("README.md",
					"doommake/common/textures/README.md")
			)
		);
		RELEASE_SCRIPT.put(MODULE_TEXTURES,
			module(
				fileContentAppend("doommake.script",
					"\tdoTextures();"
				)
			)
		);
		POST_RELEASE.put(MODULE_TEXTURES,
			module(
				fileAppend("doommake.script",
					"doommake/common/textures/doommake-target.script"
				)
			)
		);
		POST_CREATE_TODOS.put(MODULE_TEXTURES, list(
			"Add flats to `src/textures/flats`."
			,"Add patches to `src/textures/patches`."
			,"Edit `src/textures/texture1.txt` or `src/textures/texture2.txt`."
			,"...OR delete those files and type `doommake rebuildtextures` to build them from IWAD."
		));
		
		// ................................................................

		// A module that uses textures from a set of provided texture WADs.
		// If this is used, do NOT use the "textures" module.
		MODULES.put(MODULE_TEXTUREWADS,
			module(5, MODULE_TEXTUREWADS,
				dir("src/wads/textures"),
				fileAppend("doommake.script",
					"doommake/common/texwad/doommake.script"),
				fileAppend("README.md",
					"doommake/common/texwad/README.md")
			)
		);
		RELEASE_SCRIPT.put(MODULE_TEXTUREWADS,
			module(
				fileContentAppend("doommake.script",
					"\tdoMapTextures();"
				)
			)
		);
		RELEASE_SCRIPT_MERGE.put(MODULE_TEXTUREWADS,
			module(
				fileContentAppend("doommake.script",
					"\t\t,getMapTexWad()"
				)
			)
		);
		RELEASE_WADMERGE_LINE.put(MODULE_TEXTUREWADS,
			"mergewad   out $0/$"
		);
		POST_RELEASE.put(MODULE_TEXTUREWADS,
			module(
				fileAppend("doommake.script",
					"doommake/common/texwad/doommake-target.script"
				)
			)
		);
		POST_CREATE_TODOS.put(MODULE_TEXTUREWADS, list(
			"Add texture WADs to `src/wads/textures`."
		));

		// ................................................................

		// A module that adds map texture exports for texture WAD stuff.
		MODULES.put(MODULE_TEXTURES_MAPS,
			module(6, MODULE_TEXTURES_MAPS,
				fileAppend("doommake.script", 
					"doommake/common/textures/maps/doommake.script")
			)
		);
		RELEASE_SCRIPT.put(MODULE_TEXTURES_MAPS,
			module(
				fileContentAppend("doommake.script",
					"\tdoMapTextures();"
				)
			)
		);
		RELEASE_SCRIPT_MERGE.put(MODULE_TEXTURES_MAPS,
			module(
				fileContentAppend("doommake.script",
					"\t\t,getMapTexWad()"
				)
			)
		);
		RELEASE_WADMERGE_LINE.put(MODULE_TEXTURES_MAPS,
			"mergewad   out $0/$"
		);
		POST_RELEASE.put(MODULE_TEXTURES_MAPS,
			module(
				fileAppend("doommake.script",
					"doommake/common/textures/maps/doommake-target.script"
				)
			)
		);
		
		// ................................................................

		// A module that allows running this project.
		// Stub for validity.
		MODULES.put(MODULE_RUN,
			module(7, MODULE_RUN,
				fileAppend("doommake.properties",
					"doommake/run/doommake.properties"),
				fileAppend("doommake.script",
					"doommake/run/doommake.script"),
				fileAppend("README.md",
					"doommake/run/README.md")
			)
		);
		POST_RELEASE.put(MODULE_RUN,
			module(
				fileAppend("doommake.script",
					"doommake/run/doommake-target.script"
				)
			)
		);
		REPLACER_LISTS.put(MODULE_RUN, list(
			REPLACER_PROJECT_RUN_EXE_PATH, 
			REPLACER_PROJECT_RUN_EXE_WORKDIR, 
			REPLACER_PROJECT_RUN_SWITCH_IWAD, 
			REPLACER_PROJECT_RUN_SWITCH_FILE, 
			REPLACER_PROJECT_RUN_SWITCH_DEH
		));
		POST_CREATE_TODOS.put(MODULE_RUN, list(
			"Open `doommake.script`, search for `entry run`, and add build folder files to run."
		));


		// ................................................................

		TEMPLATES.put(TEMPLATE_GIT, template(
			TEMPLATE_GIT, CATEGORY_REPOSITORY, "Adds Git repository ignores/attributes to the project.",
			MODULE_GIT
		));
		
		TEMPLATES.put(TEMPLATE_MERCURIAL, template(
			TEMPLATE_MERCURIAL, CATEGORY_REPOSITORY, "Adds Mercurial repository ignores to the project.",
			MODULE_MERCURIAL
		));
		
		TEMPLATES.put(TEMPLATE_DECOHACK, template(
			TEMPLATE_DECOHACK, CATEGORY_PATCHES, "Adds a DECOHack stub for a DeHackEd patch.",
			MODULE_INIT, MODULE_DECOHACK
		));

		TEMPLATES.put(TEMPLATE_RUN, template(
			TEMPLATE_RUN, CATEGORY_EXECUTION, "Adds the ability to run this project.",
			MODULE_INIT, MODULE_RUN
		));
			
		TEMPLATES.put(TEMPLATE_BASE, template(
			TEMPLATE_BASE, CATEGORY_ASSETS, "An empty base project.",
			MODULE_INIT, MODULE_BASE
		));
		
		TEMPLATES.put(TEMPLATE_MAPS, template(
			TEMPLATE_MAPS, CATEGORY_ASSETS, "A project for merging just maps together.",
			MODULE_INIT, MODULE_MAPS
		));
		
		TEMPLATES.put(TEMPLATE_MAPS_ASSETS, template(
			TEMPLATE_MAPS_ASSETS, CATEGORY_ASSETS, "A project for merging maps and assets together.",
			MODULE_INIT, MODULE_MAPS, MODULE_ASSETS
		));

		TEMPLATES.put(TEMPLATE_MAPS_ASSETS_TEXTURES, template(
			TEMPLATE_MAPS_ASSETS_TEXTURES, CATEGORY_ASSETS, "A project for merging maps, assets, and textures together.",
			MODULE_INIT, MODULE_MAPS, MODULE_ASSETS, MODULE_MAPTEX, MODULE_TEXTURES, MODULE_TEXTURES_MAPS
		));

		TEMPLATES.put(TEMPLATE_MAPS_ASSETS_TEXTUREWADS, template(
			TEMPLATE_MAPS_ASSETS_TEXTUREWADS, CATEGORY_ASSETS, "A project for merging maps, assets, and used textures from texture WADs together.",
			MODULE_INIT, MODULE_MAPS, MODULE_ASSETS, MODULE_MAPTEX, MODULE_TEXTUREWADS
		));

		TEMPLATES.put(TEMPLATE_MAPS_TEXTURES, template(
			TEMPLATE_MAPS_TEXTURES, CATEGORY_ASSETS, "A project for merging maps and textures together.",
			MODULE_INIT, MODULE_MAPS, MODULE_MAPTEX, MODULE_TEXTURES, MODULE_TEXTURES_MAPS
		));

		TEMPLATES.put(TEMPLATE_MAPS_TEXTUREWADS, template(
			TEMPLATE_MAPS_TEXTUREWADS, CATEGORY_ASSETS, "A project for merging maps and used textures from texture WADs together.",
			MODULE_INIT, MODULE_MAPS, MODULE_MAPTEX, MODULE_TEXTUREWADS
		));

		TEMPLATES.put(TEMPLATE_ASSETS, template(
			TEMPLATE_ASSETS, CATEGORY_ASSETS, "A project for merging just non-texture assets together.",
			MODULE_INIT, MODULE_ASSETS
		));
		
		TEMPLATES.put(TEMPLATE_ASSETS_TEXTURES, template(
			TEMPLATE_ASSETS_TEXTURES, CATEGORY_ASSETS, "A project for merging assets together, including a from-scratch texture set.",
			MODULE_INIT, MODULE_ASSETS, MODULE_TEXTURES
		));
		
		TEMPLATES.put(TEMPLATE_TEXTURES, template(
			TEMPLATE_TEXTURES, CATEGORY_ASSETS, "A project for merging a texture WAD together.",
			MODULE_INIT, MODULE_TEXTURES
		));

	}
	
	@SafeVarargs
	public static <T> List<T> list(T ... items)
	{
		List<T> out =  new LinkedList<>();
		for (T t : items)
			out.add(t);
		return out;
	}

	@SafeVarargs
	public static <T> Set<T> set(T ... items)
	{
		Set<T> out =  new HashSet<>();
		for (T t : items)
			out.add(t);
		return out;
	}

	public static Set<Map.Entry<String, ProjectTemplate>> getTemplates()
	{
		return TEMPLATES.entrySet();
	}

	// Returns modules.
	public static Set<Map.Entry<String, ProjectModule>> getModules()
	{
		return MODULES.entrySet();
	}


	public static SortedSet<ProjectModule> getSelectedProjects(List<String> templateNameList) throws UtilityException
	{
		// Get Modules.
		SortedSet<ProjectModule> selected = new TreeSet<>();
		Set<String> selectedCategories = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		
		// Get Templates.
		for (String name : templateNameList)
		{
			ProjectTemplate found;
			if ((found = TEMPLATES.get(name)) == null)
				throw new UtilityException("No such project template: " + name + "\nRun DoomMake with `--list-templates` for a list of available templates.");
			if (selectedCategories.contains(found.getCategory()))
				throw new UtilityException("Already included a template from category: " + found.getCategory() + ".");

			selectedCategories.add(found.getCategory());
			
			for (String modname : found)
			{
				ProjectModule modfound;
				if ((modfound = MODULES.get(modname)) == null)
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
			List<String> todos = POST_CREATE_TODOS.get(module.getName());
			if (todos != null)
				out.addAll(todos);
		}
		return out;
	}

	/**
	 * Get all replacers to use. 
	 * @param selected the selected modules.
	 * @return the list of replacers.
	 */
	public static List<ProjectTokenReplacer> getReplacers(SortedSet<ProjectModule> selected)
	{
		List<ProjectTokenReplacer> out = new LinkedList<>();
		
		for (ProjectModule module : selected)
		{
			List<ProjectTokenReplacer> replacers = REPLACER_LISTS.get(module.getName());
			if (replacers != null) for (ProjectTokenReplacer replacer : replacers)
			{
				if (!out.contains(replacer))
					out.add(replacer);
			}
		}
		
		return out;
	}

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
	
	// 
	public static Map<String, String> consoleReplacer(List<ProjectTokenReplacer> replacerList, PrintStream stdout, InputStream stdin) throws IOException
	{
		Map<String, String> out = new HashMap<>();
		try (BufferedReader br = Common.openTextStream(stdin))
		{
			for (ProjectTokenReplacer replacer : replacerList)
				insertReplacer(replacer, stdout, br, out);
		}
		return out;
	}
	
	/**
	 * Creates a project.
	 * @param selected the selected set project modules.
	 * @param targetDirectory the target directory.
	 * @param replacerMap the replacer token map.
	 * @throws IOException
	 */
	public static void createProject(SortedSet<ProjectModule> selected, Map<String, String> replacerMap, File targetDirectory) throws IOException
	{
		boolean includedInit = false;

		// Modules.
		for (ProjectModule module : selected)
		{
			module.createIn(targetDirectory, replacerMap);
			if (module.getName().equals(MODULE_INIT))
				includedInit = true;
		}

		if (includedInit)
		{
			// Add release script header.
			module(
				fileAppend("doommake.script",
					"doommake/projects/doommake-header.script")
			).createIn(targetDirectory, replacerMap);

			// Project Modules.
			for (ProjectModule module : selected)
			{
				ProjectModule found;
				if ((found = RELEASE_SCRIPT.get(module.getName())) != null)
					found.createIn(targetDirectory, replacerMap);
			}

			// WadMerge Properties Start
			module(
				fileContentAppend("doommake.script",
					"}\n",
					"/**",
					" * Merges all components into the project file and creates the distributable.",
					" */",
					"check function doRelease() {",
					"\n\twadmerge(file(MERGESCRIPT_RELEASE), [",
	        		"\t\tgetBuildDirectory()",
	        		"\t\t,getProjectWad()"
	        	)
			).createIn(targetDirectory, replacerMap);
			
			// Add merge script.
			module(
				file("scripts/merge-release.txt",
					"doommake/projects/wadmerge-header.txt")
			).createIn(targetDirectory, replacerMap);

			// Project Modules.
			int x = 2;
			for (ProjectModule module : selected)
			{
				ProjectModule found;
				if ((found = RELEASE_SCRIPT_MERGE.get(module.getName())) != null)
				{
					found.createIn(targetDirectory, replacerMap);
					
					String line;
					if ((line = RELEASE_WADMERGE_LINE.get(module.getName())) != null)
					{
						module(
							fileContentAppend("scripts/merge-release.txt", line + (x++))
						).createIn(targetDirectory, replacerMap);
					}
				}
			}

			// WadMerge Properties End
			module(
				fileContentAppend("doommake.script", 
					"\t]);"
	        	)
			).createIn(targetDirectory, replacerMap);
			
			// Add release script footer.
			module(
				fileAppend("doommake.script",
					"doommake/projects/doommake-footer.script")
			).createIn(targetDirectory, replacerMap);
			
			// Add merge script ending.
			module(
				fileContentAppend("scripts/merge-release.txt",
					"\nfinish out $0/$1",
					"end")
			).createIn(targetDirectory, replacerMap);
			
			// Finish README
			module(
				fileAppend("README.md",
					"doommake/projects/README.md")
			).createIn(targetDirectory, replacerMap);
			
			// ===============================================================
			
			for (ProjectModule module : selected)
			{
				ProjectModule found;
				if ((found = POST_RELEASE.get(module.getName())) != null)
					found.createIn(targetDirectory, replacerMap);
			}

			// Add release targets.
			module(
				fileAppend("doommake.script",
					"doommake/projects/doommake-target.script")
			).createIn(targetDirectory, replacerMap);
			
		}
		
	}
	
}

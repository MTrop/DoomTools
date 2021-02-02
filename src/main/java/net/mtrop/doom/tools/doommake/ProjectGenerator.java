package net.mtrop.doom.tools.doommake;

import static net.mtrop.doom.tools.doommake.ProjectTemplate.template;

import static net.mtrop.doom.tools.doommake.ProjectModule.module;
import static net.mtrop.doom.tools.doommake.ProjectModule.dir;
import static net.mtrop.doom.tools.doommake.ProjectModule.file;
import static net.mtrop.doom.tools.doommake.ProjectModule.fileAppend;
import static net.mtrop.doom.tools.doommake.ProjectModule.fileContentAppend;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

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

	private static final String TEMPLATE_GIT = "git";
	private static final String TEMPLATE_BARE = "bare";
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

	private static final String MODULE_GIT = "git";
	private static final String MODULE_BASE = "base";
	private static final String MODULE_BARE = "bare";
	private static final String MODULE_DECOHACK = "decohack";
	private static final String MODULE_MAPS = "maps";
	private static final String MODULE_MAPTEX = "maptex";
	private static final String MODULE_ASSETS = "assets";
	private static final String MODULE_TEXTURES = "textures";
	private static final String MODULE_TEXTURES_MAPS = "textures-maps";
	private static final String MODULE_TEXTUREWADS = "texturewads";

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

	static
	{
		TEMPLATES = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		MODULES = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		RELEASE_SCRIPT = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		RELEASE_SCRIPT_MERGE = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		RELEASE_WADMERGE_LINE = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		// ................................................................

		// Adds files for Git repository support.
		MODULES.put(MODULE_GIT,
			module(-1, MODULE_GIT,
				file(".gitignore", 
					"doommake/git/gitignore.txt"),
				file(".gitattributes", 
					"doommake/git/gitattributes.txt")
			)
		);

		// ................................................................

		MODULES.put(MODULE_BASE, 
			module(0, MODULE_BASE,
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
		
		// ................................................................

		MODULES.put(MODULE_BARE, 
			module(100, MODULE_BARE)
		);
		RELEASE_SCRIPT.put(MODULE_BARE,
			module(
				fileContentAppend("doommake.script",
					"\t// Add other resource constructors here."
				)
			)
		);
		RELEASE_WADMERGE_LINE.put(MODULE_BARE,
			"# Add resource merging here."
		);

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
		
		// ................................................................

		TEMPLATES.put(TEMPLATE_GIT, template(
			TEMPLATE_GIT, CATEGORY_REPOSITORY, "Adds Git repository support to the project.",
			MODULE_GIT
		));
		
		TEMPLATES.put(TEMPLATE_DECOHACK, template(
			TEMPLATE_DECOHACK, CATEGORY_PATCHES, "Adds a DECOHack stub for a DeHackEd patch.",
			MODULE_BASE, MODULE_DECOHACK
		));

		TEMPLATES.put(TEMPLATE_BARE, template(
			TEMPLATE_BARE, CATEGORY_ASSETS, "An empty base project.",
			MODULE_BASE, MODULE_BARE
		));
		
		TEMPLATES.put(TEMPLATE_MAPS, template(
			TEMPLATE_MAPS, CATEGORY_ASSETS, "A project for merging just maps together.",
			MODULE_BASE, MODULE_MAPS
		));
		
		TEMPLATES.put(TEMPLATE_MAPS_ASSETS, template(
			TEMPLATE_MAPS_ASSETS, CATEGORY_ASSETS, "A project for merging maps and assets together.",
			MODULE_BASE, MODULE_MAPS, MODULE_ASSETS
		));

		TEMPLATES.put(TEMPLATE_MAPS_ASSETS_TEXTURES, template(
			TEMPLATE_MAPS_ASSETS_TEXTURES, CATEGORY_ASSETS, "A project for merging maps, assets, and textures together.",
			MODULE_BASE, MODULE_MAPS, MODULE_ASSETS, MODULE_MAPTEX, MODULE_TEXTURES, MODULE_TEXTURES_MAPS
		));

		TEMPLATES.put(TEMPLATE_MAPS_ASSETS_TEXTUREWADS, template(
			TEMPLATE_MAPS_ASSETS_TEXTUREWADS, CATEGORY_ASSETS, "A project for merging maps, assets, and used textures from texture WADs together.",
			MODULE_BASE, MODULE_MAPS, MODULE_ASSETS, MODULE_MAPTEX, MODULE_TEXTUREWADS
		));

		TEMPLATES.put(TEMPLATE_MAPS_TEXTURES, template(
			TEMPLATE_MAPS_TEXTURES, CATEGORY_ASSETS, "A project for merging maps and textures together.",
			MODULE_BASE, MODULE_MAPS, MODULE_MAPTEX, MODULE_TEXTURES, MODULE_TEXTURES_MAPS
		));

		TEMPLATES.put(TEMPLATE_MAPS_TEXTUREWADS, template(
			TEMPLATE_MAPS_TEXTUREWADS, CATEGORY_ASSETS, "A project for merging maps and used textures from texture WADs together.",
			MODULE_BASE, MODULE_MAPS, MODULE_MAPTEX, MODULE_TEXTUREWADS
		));

		TEMPLATES.put(TEMPLATE_ASSETS, template(
			TEMPLATE_ASSETS, CATEGORY_ASSETS, "A project for merging just non-texture assets together.",
			MODULE_BASE, MODULE_ASSETS
		));
		
		TEMPLATES.put(TEMPLATE_ASSETS_TEXTURES, template(
			TEMPLATE_ASSETS_TEXTURES, CATEGORY_ASSETS, "A project for merging assets together, including a from-scratch texture set.",
			MODULE_BASE, MODULE_ASSETS, MODULE_TEXTURES
		));
		
		TEMPLATES.put(TEMPLATE_TEXTURES, template(
			TEMPLATE_TEXTURES, CATEGORY_ASSETS, "A project for merging a texture WAD together.",
			MODULE_BASE, MODULE_TEXTURES
		));

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

	// Creates a project.
	public static void createProject(List<String> templateNameList, File targetDirectory) throws UtilityException, IOException
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
		
		// Modules.
		for (ProjectModule module : selected)
			module.createIn(targetDirectory);

		// Add release script header.
		module(
			fileAppend("doommake.script",
				"doommake/projects/doommake-header.script")
		).createIn(targetDirectory);

		// Project Modules.
		for (ProjectModule module : selected)
		{
			ProjectModule found;
			if ((found = RELEASE_SCRIPT.get(module.getName())) != null)
				found.createIn(targetDirectory);
		}

		// WadMerge Properties Start
		module(
			fileContentAppend("doommake.script", 
				"\n\twadmerge(file(MERGESCRIPT_RELEASE), [",
        		"\t\tgetBuildDirectory()",
        		"\t\t,getProjectWad()"
        	)
		).createIn(targetDirectory);
		
		// Add merge script.
		module(
			file("scripts/merge-release.txt",
				"doommake/projects/wadmerge-header.txt")
		).createIn(targetDirectory);

		// Project Modules.
		int x = 2;
		for (ProjectModule module : selected)
		{
			ProjectModule found;
			if ((found = RELEASE_SCRIPT_MERGE.get(module.getName())) != null)
			{
				found.createIn(targetDirectory);
				
				String line;
				if ((line = RELEASE_WADMERGE_LINE.get(module.getName())) != null)
				{
					module(
						fileContentAppend("scripts/merge-release.txt", line + (x++))
					).createIn(targetDirectory);
				}
			}
		}

		// WadMerge Properties End
		module(
			fileContentAppend("doommake.script", 
				"\t]);"
        	)
		).createIn(targetDirectory);
		
		// Add release script footer.
		module(
			fileAppend("doommake.script",
				"doommake/projects/doommake-footer.script")
		).createIn(targetDirectory);
		
		// Add merge script ending.
		module(
			fileContentAppend("scripts/merge-release.txt",
				"\nfinish out $0/$1",
				"end")
		).createIn(targetDirectory);
		
		// Finish README
		module(
			fileAppend("README.md",
				"doommake/projects/README.md")
		).createIn(targetDirectory);
	}
	
}

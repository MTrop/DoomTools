package net.mtrop.doom.tools.doommake.generators;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import net.mtrop.doom.tools.doommake.ProjectGenerator;
import net.mtrop.doom.tools.doommake.ProjectModule;
import net.mtrop.doom.tools.doommake.ProjectModuleDescriptor;
import net.mtrop.doom.tools.doommake.ProjectTemplate;
import net.mtrop.doom.tools.doommake.ProjectTokenReplacer;

import static net.mtrop.doom.tools.doommake.ProjectTemplate.template;

import static net.mtrop.doom.tools.doommake.ProjectModule.module;

import static net.mtrop.doom.tools.doommake.ProjectModuleDescriptor.descriptor;
import static net.mtrop.doom.tools.doommake.ProjectModuleDescriptor.dir;
import static net.mtrop.doom.tools.doommake.ProjectModuleDescriptor.file;
import static net.mtrop.doom.tools.doommake.ProjectModuleDescriptor.fileData;
import static net.mtrop.doom.tools.doommake.ProjectModuleDescriptor.fileAppend;
import static net.mtrop.doom.tools.doommake.ProjectModuleDescriptor.fileContentAppend;


/**
 * Class used for generating project structures.
 * Mostly for moving this out of the main class.
 * @author Matthew Tropiano
 */
public class WADProjectGenerator extends ProjectGenerator
{
	private static final String CATEGORY_ASSETS = "Assets";
	private static final String CATEGORY_MAPS = "Maps";
	private static final String CATEGORY_TEXTURES = "Textures";
	private static final String CATEGORY_REPOSITORY = "Repositories";
	private static final String CATEGORY_PATCHES = "Patches";
	private static final String CATEGORY_EXECUTION = "Execution";

	private static final String TEMPLATE_GIT = "git";
	private static final String TEMPLATE_MERCURIAL = "hg";
	private static final String TEMPLATE_ASSETS = "assets";
	private static final String TEMPLATE_MAPS = "maps";
	private static final String TEMPLATE_TEXTURES = "textures";
	private static final String TEMPLATE_TEXTURES_BOOM = "texturesboom";
	private static final String TEMPLATE_TEXTUREWADS = "texturewads";
	private static final String TEMPLATE_DECOHACK = "decohack";
	private static final String TEMPLATE_PATCH = "patch";
	private static final String TEMPLATE_RUN = "run";

	private static final String MODULE_GIT = "git";
	private static final String MODULE_MERCURIAL = "hg";
	private static final String MODULE_INIT = "init";
	private static final String MODULE_BASE = "bare";
	private static final String MODULE_DECOHACK = "decohack";
	private static final String MODULE_PATCH = "patch";
	private static final String MODULE_MAPS = "maps";
	private static final String MODULE_ASSETS = "assets";
	private static final String MODULE_ASSETS_CONVERT = "assets-convert";
	private static final String MODULE_TEXTURES = "textures";
	private static final String MODULE_TEXTURES_VANILLA = "textures-vanilla";
	private static final String MODULE_TEXTURES_BOOM = "textures-boom";
	private static final String MODULE_TEXTURES_CONVERT = "textures-convert";
	private static final String MODULE_TEXTUREWADS = "texturewads";
	private static final String MODULE_RUN = "run";

	/** The main templates. */
	private static final SortedMap<String, ProjectTemplate> TEMPLATES;
	/** The main modules. */
	private static final Map<String, ProjectModule> MODULES;

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
		"DECOHACK_BASE", "DECOHack Patch type (doom19, udoom19, boom, mbf, extended, mbf21, dsdhacked)\n(blank for \"doom19\")?", "doom19", (type) -> {
			if (type.trim().length() == 0)
				return null;
			if (!set("doom19", "udoom19", "boom", "mbf", "extended", "mbf21", "dsdhacked").contains(type))
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

		// ................................................................

		MODULES.put(MODULE_GIT, module(200)
			.base(descriptor(
				file(".gitignore", 
					"doommake/git/gitignore.txt"),
				file(".gitattributes", 
					"doommake/git/gitattributes.txt")
			))
			.todos("Open `.gitignore` and verify what you want ignored in the project.")
		);

		// ................................................................

		// Adds files for Mercurial repository support.
		MODULES.put(MODULE_MERCURIAL, module(200)
			.base(descriptor(
				file(".hgignore", 
					"doommake/hg/hgignore.txt")
			))
			.todos("Open `.hgignore` and verify what you want ignored in the project.")
		);

		// ................................................................

		MODULES.put(MODULE_INIT, module(0)
			.base(descriptor(
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
			))
			.postRelease(descriptor(
				fileAppend("doommake.script",
					"doommake/doommake-target.script"
				)
			))
			.replacers(
				REPLACER_PROJECT_NAME, 
				REPLACER_PROJECT_IWAD
			)
			.todos(
				"Modify `README.md` and describe your project."
				,"Modify `src/wadinfo.txt`, especially the license section. It will become your text file."
				,"Add extended credits to `src/credits.txt`."
			)
		);
		
		// ................................................................

		MODULES.put(MODULE_BASE, module(100)
			.base(descriptor())
			.releaseScript(descriptor(
				fileContentAppend("doommake.script",
					"\t// Add other resource constructors here."
				)
			))
			.releaseWadMergeLines(
				"# Add resource merging here."
			)
			.todos(
				"Add resources to merge to the WadMerge script."
				,"Add targets to the doommake.script script."
			)
		);

		// ................................................................

		// A module that compiles a DeHackEd patch.
		MODULES.put(MODULE_DECOHACK, module(1)
			.base(descriptor(
				file("src/decohack/main.dh",
					"doommake/decohack/main.dh"),
				fileAppend("doommake.properties",
					"doommake/decohack/doommake.properties"),
				fileAppend("doommake.script", 
					"doommake/decohack/doommake.script"),
				fileAppend("README.md",
					"doommake/decohack/README.md")
			))
			.releaseScript(descriptor(
				fileContentAppend("doommake.script",
					"\tdoPatch(false);"
				)
			))
			.releaseScriptMerge(descriptor(
				fileContentAppend("doommake.script",
					"\t\t,getPatchFile()"
					,"\t\t,getPatchSourceOutputFile()"
				)
			))
			.releaseWadMergeLines(
				"mergefile  out $0/$"
				,"mergefile  out $0/$"
			)
			.postRelease(descriptor(
				fileAppend("doommake.script",
					"doommake/decohack/doommake-target.script"
				)
			))
			.replacers(
				REPLACER_PROJECT_DECOHACK
			)
			.todos(
				"Modify `src/decohack/main.dh` to your liking."
			)
		);

		// ................................................................

		// A module that copies a DeHackEd patch.
		MODULES.put(MODULE_PATCH, module(1)
			.base(descriptor(
				file("src/patch/dehacked.deh",
					"doommake/patch/dehacked.deh"),
				fileAppend("doommake.properties",
					"doommake/patch/doommake.properties"),
				fileAppend("doommake.project.properties",
					"doommake/patch/doommake.project.properties"),
				fileAppend("doommake.script", 
					"doommake/patch/doommake.script"),
				fileAppend("README.md",
					"doommake/patch/README.md")
			))
			.releaseScript(descriptor(
				fileContentAppend("doommake.script",
					"\tdoPatch();"
				)
			))
			.releaseScriptMerge(descriptor(
				fileContentAppend("doommake.script",
					"\t\t,getPatchFile()"
				)
			))
			.releaseWadMergeLines(
				"mergefile  out $0/$"
			)
			.postRelease(descriptor(
				fileAppend("doommake.script",
					"doommake/patch/doommake-target.script"
				)
			))
			.todos(
				"Create and save your DeHackEd patch file in the `src/patch` directory."
			)
		);

		// ................................................................

		// A module that converts assets.
		MODULES.put(MODULE_ASSETS_CONVERT, module(4)
			.base(descriptor(
				dir("src/convert/graphics"),
				dir("src/convert/sounds"),
				dir("src/convert/sprites"),
				fileAppend("doommake.script",
					"doommake/common/assets/convert/doommake.script"),
				fileAppend("README.md",
					"doommake/common/assets/convert/README.md")
			))
			.releaseScript(descriptor(
				fileContentAppend("doommake.script",
					"\tdoConvertSounds();"
					,"\tdoConvertGraphics();"
					,"\tdoConvertSprites();"
				)
			))
			.postRelease(descriptor(
				fileAppend("doommake.script",
					"doommake/common/assets/convert/doommake-target.script"
				)
			))
			.todos(
				"Add BMP, GIF, or PNG files to `src/convert/graphics`."
				,"Add BMP, GIF, or PNG files to `src/convert/sprites`."
				,"Add sound files to `src/convert/sounds`."
			)
		);

		// ................................................................

		// A module that builds maps and non-texture assets together.
		MODULES.put(MODULE_ASSETS, module(5)
			.base(descriptor(
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
			))
			.releaseScript(descriptor(
				fileContentAppend("doommake.script",
					"\tdoAssets();"
				)
			))
			.releaseScriptMerge(descriptor(
				fileContentAppend("doommake.script",
					"\t\t,getAssetsWAD()"
				)
			))
			.releaseWadMergeLines(
				"mergewad   out $0/$"
			)
			.postRelease(descriptor(
				fileAppend("doommake.script",
					"doommake/common/assets/doommake-target.script"
				)
			))
			.todos(
				"Add assets to `src/assets` into the appropriate folders."
			)
		);

		// ................................................................

		// A module that builds texture WADs.
		// If this is used, do NOT use the "texturewads" module.
		MODULES.put(MODULE_TEXTURES_CONVERT, module(9)
			.base(descriptor(
				dir("src/convert/flats"),
				dir("src/convert/patches"),
				dir("src/convert/texture1"),
				dir("src/convert/texture2"),
				fileAppend("doommake.script", 
					"doommake/common/textures/convert/doommake.script"),
				fileAppend("README.md",
					"doommake/common/textures/convert/README.md")
			))
			.releaseScript(descriptor(
				fileContentAppend("doommake.script",
					"\tdoConvertFlats();"
					,"\tdoConvertPatches();"
					,"\tdoConvertTextures();"
				)
			))
			.postRelease(descriptor(
				fileAppend("doommake.script",
					"doommake/common/textures/convert/doommake-target.script"
				)
			))
			.todos(
				"Add BMP, GIF, or PNG files to `src/convert/flats`."
				,"Add BMP, GIF, or PNG files to `src/convert/patches`."
				,"Add BMP, GIF, or PNG files to `src/convert/texture1`."
				,"Add BMP, GIF, or PNG files to `src/convert/texture2`."
			)
		);
		
		// ................................................................

		// A module that builds texture WADs.
		// If this is used, do NOT use the "texturewads" module.
		MODULES.put(MODULE_TEXTURES, module(10)
			.base(descriptor(
				dir("src/textures/flats"),
				dir("src/textures/patches"),
				dir("src/textures/texture1"),
				dir("src/textures/texture2"),
				file("src/textures/texture1.txt", 
					"doommake/common/textures/texture1.txt"),
				file("src/textures/texture2.txt", 
					"doommake/common/textures/texture2.txt"),
				fileData("src/textures/animflats.wad", 
					"doommake/common/textures/animflats.wad"),
				fileAppend("doommake.properties", 
					"doommake/common/textures/doommake.properties"),
				fileAppend("doommake.script", 
					"doommake/common/textures/doommake.script"),
				fileAppend("README.md",
					"doommake/common/textures/README.md")
			))
			.releaseScript(descriptor(
				fileContentAppend("doommake.script",
					"\tdoTextures();"
				)
			))
			.postRelease(descriptor(
				fileAppend("doommake.script",
					"doommake/common/textures/doommake-target.script"
				)
			))
			.todos(
				"Add flats to `src/textures/flats`."
				,"Add patches to `src/textures/patches`."
				,"Add patches to `src/textures/texture1`."
				,"Add patches to `src/textures/texture2`."
				,"Edit `src/textures/animflats.wad` for flats that need to be in a specific order."
				,"Edit `src/textures/texture1.txt` or `src/textures/texture2.txt`."
				,"...OR delete those files and type `doommake rebuildtextures` to build them from IWAD."
			)
		);
		
		// ................................................................

		// A module that builds vanilla texture WADs.
		MODULES.put(MODULE_TEXTURES_VANILLA, module(11)
			.base(descriptor(
				file("scripts/merge-textures.txt",
					"doommake/common/textures/vanilla/wadmerge.txt")
			))
		);
		
		// ................................................................

		// A module that builds Boom texture WADs.
		MODULES.put(MODULE_TEXTURES_BOOM, module(11)
			.base(descriptor(
				file("src/textures/defswani.txt",
					"doommake/common/textures/boom/defswani.txt"),
				file("scripts/merge-textures.txt",
					"doommake/common/textures/boom/wadmerge.txt")
			))
			.todos(
				"Edit `src/textures/defswani.txt` for defining ANIMATED and SWITCHES."
			)
		);
		
		// ................................................................

		// A module that uses textures from a set of provided texture WADs.
		// If this is used, do NOT use the "textures" module.
		MODULES.put(MODULE_TEXTUREWADS, module(15)
			.base(descriptor(
				dir("src/wads/textures")
			))
			.releaseScriptMerge(descriptor(
				fileContentAppend("doommake.script",
					"\t\t,getMapTexWad()"
				)
			))
			.releaseWadMergeLines(
				"mergewad   out $0/$"
			)
			.todos(
				"Add texture WADs to `src/wads/textures`."
			)
		);

		// ................................................................

		// A module that builds a set of maps.
		MODULES.put(MODULE_MAPS, module(18)
			.base(descriptor(
				dir("src/maps"),
				file("scripts/merge-maps.txt",
					"doommake/common/maps/wadmerge.txt"),
				fileAppend("doommake.properties",
					"doommake/common/maps/doommake.properties"),
				fileAppend("doommake.script", 
					"doommake/common/maps/doommake.script"),
				fileAppend("README.md",
					"doommake/common/maps/README.md")
			))
			.releaseScript(descriptor(
				fileContentAppend("doommake.script",
					"\tdoMaps();"
					,"\tdoMapTextures();"
				)
			))
			.releaseScriptMerge(descriptor(
				fileContentAppend("doommake.script",
					"\t\t,getMapsWad()"
					,"\t\t,getMapTexWad()"
				)
			))
			.releaseWadMergeLines(
				"mergewad   out $0/$"
				,"mergewad   out $0/$"
			)
			.postRelease(descriptor(
				fileAppend("doommake.script",
					"doommake/common/maps/doommake-target.script"
				)
			))
			.todos(
				"Add maps to `src/maps`."
			)
		);

		// ................................................................

		// A module that allows running this project.
		// Stub for validity.
		MODULES.put(MODULE_RUN, module(20)
			.base(descriptor(
				fileAppend("doommake.properties",
					"doommake/run/doommake.properties"),
				fileAppend("doommake.script",
					"doommake/run/doommake.script"),
				fileAppend("README.md",
					"doommake/run/README.md")
			))
			.postRelease(descriptor(
				fileAppend("doommake.script",
					"doommake/run/doommake-target.script"
				)
			))
			.replacers(
				REPLACER_PROJECT_RUN_EXE_PATH, 
				REPLACER_PROJECT_RUN_EXE_WORKDIR, 
				REPLACER_PROJECT_RUN_SWITCH_IWAD, 
				REPLACER_PROJECT_RUN_SWITCH_FILE, 
				REPLACER_PROJECT_RUN_SWITCH_DEH
			)
			.todos(
				"Open `doommake.script`, search for `entry run`, and double-check the files added for the run script."
			)
		);

		// ................................................................

		TEMPLATES.put(TEMPLATE_BASE, template(
			TEMPLATE_BASE, CATEGORY_ASSETS, "An empty base project.",
			MODULE_INIT, MODULE_BASE
		));
		
		TEMPLATES.put(TEMPLATE_GIT, template(
			TEMPLATE_GIT, CATEGORY_REPOSITORY, "Adds Git repository ignores/attributes to the project.",
			MODULE_INIT, MODULE_GIT
		));
		
		TEMPLATES.put(TEMPLATE_MERCURIAL, template(
			TEMPLATE_MERCURIAL, CATEGORY_REPOSITORY, "Adds Mercurial repository ignores to the project.",
			MODULE_INIT, MODULE_MERCURIAL
		));
		
		TEMPLATES.put(TEMPLATE_DECOHACK, template(
			TEMPLATE_DECOHACK, CATEGORY_PATCHES, "Adds a DECOHack stub for a DeHackEd patch.",
			MODULE_INIT, MODULE_DECOHACK
		));

		TEMPLATES.put(TEMPLATE_PATCH, template(
			TEMPLATE_PATCH, CATEGORY_PATCHES, "Adds a source folder for importing a DeHackEd patch (for external programs like WhackEd).",
			MODULE_INIT, MODULE_PATCH
		));

		TEMPLATES.put(TEMPLATE_RUN, template(
			TEMPLATE_RUN, CATEGORY_EXECUTION, "Adds the ability to run this project from DoomMake.",
			MODULE_INIT, MODULE_RUN
		));
			
		TEMPLATES.put(TEMPLATE_ASSETS, template(
			TEMPLATE_ASSETS, CATEGORY_ASSETS, "Adds non-texture assets.",
			MODULE_INIT, MODULE_ASSETS_CONVERT, MODULE_ASSETS
		));
		
		TEMPLATES.put(TEMPLATE_TEXTURES, template(
			TEMPLATE_TEXTURES, CATEGORY_TEXTURES, "Adds the ability to merge a texture WAD together as the project's texture pool.",
			MODULE_INIT, MODULE_TEXTURES_CONVERT, MODULE_TEXTURES, MODULE_TEXTURES_VANILLA
		));

		TEMPLATES.put(TEMPLATE_TEXTURES_BOOM, template(
			TEMPLATE_TEXTURES_BOOM, CATEGORY_TEXTURES, "Adds the ability to merge a texture WAD together (with Boom additions) as the project's texture pool.",
			MODULE_INIT, MODULE_TEXTURES_CONVERT, MODULE_TEXTURES, MODULE_TEXTURES_BOOM
		));

		TEMPLATES.put(TEMPLATE_TEXTUREWADS, template(
			TEMPLATE_TEXTUREWADS, CATEGORY_TEXTURES, "Adds the ability to add texture WADs as the project's texture pool.",
			MODULE_INIT, MODULE_TEXTUREWADS
		));

		TEMPLATES.put(TEMPLATE_MAPS, template(
			TEMPLATE_MAPS, CATEGORY_MAPS, "Adds the ability to merge maps together.",
			MODULE_INIT, MODULE_MAPS
		));
		
	}
	
	@Override
	public Set<String> getTemplateNames() 
	{
		return TEMPLATES.keySet();
	}

	@Override
	public ProjectTemplate getTemplate(String name)
	{
		return TEMPLATES.get(name);
	}

	@Override
	public Set<String> getModuleNames() 
	{
		return MODULES.keySet();
	}

	@Override
	public ProjectModule getModule(String name) 
	{
		return MODULES.get(name);
	}

	@Override
	public void createProject(SortedSet<ProjectModule> selected, Map<String, String> replacerMap, File targetDirectory) throws IOException 
	{
		// Modules.
		for (ProjectModule module : selected)
			module.getDescriptor().createIn(targetDirectory, replacerMap);

		// Add release script header.
		descriptor(
			fileAppend("doommake.script",
				"doommake/projects/doommake-header.script")
		).createIn(targetDirectory, replacerMap);

		// Project Modules.
		for (ProjectModule module : selected)
		{
			ProjectModuleDescriptor found;
			if ((found = module.getReleaseScript()) != null)
				found.createIn(targetDirectory, replacerMap);
		}

		// WadMerge Properties Start
		descriptor(
			fileContentAppend("doommake.script",
				"}\n",
				"/**",
				" * Merges all components into the project file and creates the distributable.",
				" */",
				"check function doRelease() {\n",
				"\toutFile = getBuildDirectory() + \"/\" + getProjectWAD();\n",
				"\tif (checkFileExistenceAndBuildStatuses(outFile, [\"dehacked\", \"maps\", \"assets\", \"maptextures\"])) {",
				"\t\tprintln(\"[Skipped] No pertinent project data built.\");",
				"\t\treturn;",
				"\t}\n",
				"\twadmerge(file(MERGESCRIPT_RELEASE), [",
        		"\t\tgetBuildDirectory()",
        		"\t\t,getSourceDirectory()",
        		"\t\t,getProjectWad()"
        	)
		).createIn(targetDirectory, replacerMap);
		
		// Add merge script.
		descriptor(
			file("scripts/merge-release.txt",
				"doommake/projects/wadmerge-header.txt")
		).createIn(targetDirectory, replacerMap);

		// Project Modules.
		int x = 3;
		for (ProjectModule module : selected)
		{
			ProjectModuleDescriptor found;
			if ((found = module.getReleaseScriptMerge()) != null)
			{
				found.createIn(targetDirectory, replacerMap);
				
				List<String> lines;
				if ((lines = module.getReleaseWadMergeLines()) != null)
				{
					for (String line : lines)
					{
						descriptor(
							fileContentAppend("scripts/merge-release.txt", line + (x++))
						).createIn(targetDirectory, replacerMap);
					}
				}
			}
		}

		// WadMerge Properties End
		descriptor(
			fileContentAppend("doommake.script", 
				"\t]);"
        	)
		).createIn(targetDirectory, replacerMap);
		
		// Add release script footer.
		descriptor(
			fileAppend("doommake.script",
				"doommake/projects/doommake-footer.script")
		).createIn(targetDirectory, replacerMap);
		
		// Add merge script ending.
		descriptor(
			fileContentAppend("scripts/merge-release.txt",
				"\nfinish out $0/$2",
				"end")
		).createIn(targetDirectory, replacerMap);
		
		// Finish README
		descriptor(
			fileAppend("README.md",
				"doommake/projects/README.md")
		).createIn(targetDirectory, replacerMap);
		
		// ===============================================================
		
		for (ProjectModule module : selected)
		{
			ProjectModuleDescriptor found;
			if ((found = module.getPostRelease()) != null)
				found.createIn(targetDirectory, replacerMap);
		}

		// Add release targets.
		descriptor(
			fileAppend("doommake.script",
				"doommake/projects/doommake-target.script")
		).createIn(targetDirectory, replacerMap);
		
	}
	
}

/*******************************************************************************
 * Copyright (c) 2020-2023 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doom.tools.doommake.generators;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import net.mtrop.doom.tools.doommake.ProjectGenerator;
import net.mtrop.doom.tools.doommake.ProjectModule;
import net.mtrop.doom.tools.doommake.ProjectModuleDescriptor;
import net.mtrop.doom.tools.doommake.ProjectTemplate;
import net.mtrop.doom.tools.doommake.ProjectTokenReplacer;
import net.mtrop.doom.tools.doommake.ProjectTokenReplacer.GUIHint;
import net.mtrop.doom.tools.exception.UtilityException;

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
public class TextureProjectGenerator extends ProjectGenerator
{
	public static final String CATEGORY_ASSETS = "Assets";
	public static final String CATEGORY_TEXTURES = "Textures";
	public static final String CATEGORY_REPOSITORY = "Repositories";

	public static final String TEMPLATE_BASE = "base";
	public static final String TEMPLATE_GIT = "git";
	public static final String TEMPLATE_MERCURIAL = "hg";
	public static final String TEMPLATE_TEXTURES = "textures";
	public static final String TEMPLATE_TEXTURES_BOOM = "texturesboom";

	private static final String MODULE_GIT = "git";
	private static final String MODULE_MERCURIAL = "hg";
	private static final String MODULE_INIT = "init";
	private static final String MODULE_BASE = "bare";
	private static final String MODULE_TEXTURES = "textures";
	private static final String MODULE_TEXTURES_VANILLA = "textures-vanilla";
	private static final String MODULE_TEXTURES_BOOM = "textures-boom";
	private static final String MODULE_TEXTURES_CONVERT = "textures-convert";
	private static final String MODULE_RELEASE = "release";

	/** The main categories. */
	private static final SortedMap<String, Set<ProjectTemplate>> CATEGORIES;
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
		"PROJECT_IWAD", "Path to project IWAD (blank to skip)?", "", GUIHint.FILE, (path) -> {
			return path.replace("\\", "/");
		}, (path) -> {
			if (path.trim().length() == 0)
				return null;
			if (!new File(path).exists())
				return "IWAD path not found!";
			return null;
		}
	);


	static
	{
		CATEGORIES = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
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
				file("PROPS.txt",
					"doommake/props.txt"),
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

		// A module that converts texture data.
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
				fileAppend("PROPS.txt",
					"doommake/common/textures/props.txt"),
				fileAppend("doommake.script", 
					"doommake/common/textures/doommake.script"),
				fileAppend("doommake.script", 
					"doommake/textures/doommake.script"),
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
				),
				fileAppend("doommake.script",
					"doommake/textures/doommake-target.script"
				)
			))
			.todos(
				"Add flats to `src/textures/flats`."
				,"Add patches to `src/textures/patches`."
				,"Add patches to `src/textures/texture1` and `src/textures/texture2`."
				,"Edit `src/textures/animflats.wad` for flats that need to be in a specific order."
				,"Edit `src/textures/texture1.txt` or `src/textures/texture2.txt`."
				,"...OR delete those files and type `doommake rebuildtextures` to build them from IWAD."
				,"Optionally: Type `doommake rebuildpalette` to extract the palette to use for conversion."
			)
		);
		
		// ................................................................

		// A module that builds vanilla texture WADs.
		MODULES.put(MODULE_TEXTURES_VANILLA, module(11)
			.base(descriptor(
				file("scripts/merge-textures.txt",
					"doommake/common/textures/vanilla/wadmerge.txt")
			))
			.releaseScriptMerge(descriptor(
				fileContentAppend("doommake.script",
					",getTextureWad()"
				)
			))
			.releaseWadMergeLines(
				"mergewad   out $0/$"
			)
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
			.releaseScriptMerge(descriptor(
				fileContentAppend("doommake.script",
					",getTextureWad()"
				)
			))
			.releaseWadMergeLines(
				"mergewad   out $0/$"
			)
			.todos(
				"Edit `src/textures/defswani.txt` for defining ANIMATED and SWITCHES."
			)
		);
		
		// ................................................................

		// A module that appends the release merging bits.
		MODULES.put(MODULE_RELEASE, module(11)
			.releaseScriptMerge(descriptor(
				fileContentAppend("doommake.script",
					"doommake/common/textures/doommake-target.script"
				)
			))
		);
		
		
		// ................................................................

		// Hidden base template.
		TEMPLATES.put(TEMPLATE_BASE, template(
			TEMPLATE_BASE, CATEGORY_ASSETS, "An empty base project.", true,
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
		
		TEMPLATES.put(TEMPLATE_TEXTURES, template(
			TEMPLATE_TEXTURES, CATEGORY_TEXTURES, "Adds the ability to merge a texture WAD together as the project's texture pool.",
			MODULE_INIT, MODULE_TEXTURES_CONVERT, MODULE_TEXTURES, MODULE_TEXTURES_VANILLA
		));

		TEMPLATES.put(TEMPLATE_TEXTURES_BOOM, template(
			TEMPLATE_TEXTURES_BOOM, CATEGORY_TEXTURES, "Adds the ability to merge a texture WAD together (with Boom additions) as the project's texture pool.",
			MODULE_INIT, MODULE_TEXTURES_CONVERT, MODULE_TEXTURES, MODULE_TEXTURES_BOOM
		));

		for (Map.Entry<String, ProjectTemplate> templateEntry : TEMPLATES.entrySet())
		{
			ProjectTemplate template = templateEntry.getValue();
			Set<ProjectTemplate> templateSet;
			if ((templateSet = CATEGORIES.get(template.getCategory())) == null)
				CATEGORIES.put(template.getCategory(), templateSet = new TreeSet<>());
			templateSet.add(template);
		}
	}
	
	@Override
	public Set<String> getCategoryNames() 
	{
		return CATEGORIES.keySet();
	}

	@Override
	public Set<ProjectTemplate> getTemplatesByCategory(String name) 
	{
		return CATEGORIES.get(name);
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
	public SortedSet<ProjectModule> getSelectedModules(Collection<String> templateNameList) throws UtilityException 
	{
		// Ensure that the base project is selected.
		SortedSet<ProjectModule> out = super.getSelectedModules(templateNameList);
		if (out.isEmpty())
			out = getSelectedModules(Arrays.asList(TEMPLATE_BASE));
		return out;
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
				"\tif (checkFileExistenceAndBuildStatuses(outFile, [\"textures\"])) {",
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

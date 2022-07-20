DoomMake
--------

### Changed for 0.18.0

* `Added` A "Studio" mode only accessible as Desktop App: `doommake-gui doommake-studio`


### Changed for 0.17.0

* `Added` The `TOOL::WADTEX(...)` function was changed to support entry name override.
* `Added` The `TOOL::WSWANTBL(...)` function was changed to support source import.
* `Added` The `TOOL::WADMERGE(...)` function was changed to support alternate input charset encoding.
* `Added` The `TOOL::WADSCRIPT(...)` function was changed to support alternate input charset encoding.
* `Added` The `TOOL::DECOHACK(...)` function was changed to support alternate input charset encoding.
* `Added` Support for project-wide text encoding setting.
* `Changed` Default DECOHack source code output file was changed from `dehsrc.dh` to `decohack.dh`. More to the point.


### Changed for 0.16.0

* `Fixed` (GUI) GUI still checked for DoomMake on PATH in order to run. This was unnecessary. (Issue #69)
* `Added` The `TOOL::DECOHACK(...)` function was changed to support reading DECOHack source from STDIN.


### Changed for 0.15.0

* `Added` DoomMake Agent for listening for project changes. See help for more info!
* `Added` DoomMake GUI for individual projects and creating new ones.
* `Added` A `justrun` target for new projects using the `run` template to execute the project without building it.


### Changed for 0.14.0

* `Added` A switch for printing the targets for a project.
* `Changed` The conversion function in graphics conversions now looks for (and prioritizes) loose palette assets for source palettes (in asset global folder) (new projects).
* `Changed` Some WadMerge scripts for some templates to make use of the `FILECHARSUB` command for asset directory imports (new projects).


### Changed for 0.13.1

* `Fixed` The `texturewads` template added script lines that duped the extracted textures on export.


### Changed for 0.13.0

* `Added` `infiles` as a valid option field for `TOOL::DECOHACK`.
* `Changed` Improved HTML documentation output.
* `Changed` Single-patch texture generation added to the `textures` and `texturesboom` templates (new projects). (Enhancement #58)


### Changed for 0.12.0

* `Added` Some texture template types for Vanilla and Boom.
* `Changed` Template ordering and setup for other texture templates (new projects).
* `Changed` Some tweaks to default `.gitattributes` on Git template (new projects).
* `Changed` The project "TODO" list is now a Markdown file.
* `Changed` (Internal) Project Generator organization changes. (Issue #36)


### Changed for 0.11.0

* `Fixed` Some generated script documentation.
* `Changed` Attempting to reduce map textures without any texture WADs errors out (new projects).
* `Changed` Assets are now a separate template category.


### Changed for 0.10.0

* `Added` A patch source module for patches made in "not DECOHack".
* `Changed` `doClean()` will clean the build directory and distribution directories (new projects).
* `Changed` More informational output (new projects).


### Changed for 0.9.0

* `Added` `dsdhacked` as a valid DECOHack patch type.


### Changed for 0.8.0

* `Added` The ability to embed DoomMake into a project (`--embed` switch).


### Changed for 0.7.0

* `Added` The options for `TOOL::DOOMTOOLS`.
* `Added` Added including the DECOHack source in the result to DECOHack projects.
* `Added` A switch for dumping documentation as HTML. (Enhancement #45)
* `Fixed` Some tweaks to auto-detecting changes to projects.
* `Fixed` "Textures" template: Converting patches used the wrong default mode.
* `Changed` The options for `TOOL::DECOHACK` to include the source export.
* `Changed` Project TODO text file is now generated in uppercase: `TODO.txt`.
* `Changed` Repository "ignore" files changed slightly.


### Changed for 0.6.0

* `Added` HASHDIR() host function for detecting folder changes.
* `Added` Modules added for asset and texture auto-conversion of graphics and sound assets.
* `Added` The "--disassemble" switch. Not many people will use this.
* `Changed` New projects now auto-detect project changes on build.
* `Changed` The "TODO" list that gets printed at the end is now added to the project.


### Changed for 0.5.0

* `Added` Relocation of `src` directory via project property. (Enhancement #38)
* `Changed` The run module checks for default files generated and runs them automatically.


### Changed for 0.4.0

* `Added` A wizard during project creation.
* `Added` "TODOs" afterward when a project is made.
* `Added` A target for texture projects that can rebuild the TEXTUREx definition lists.


### Changed for 0.3.0

* `Added` All scripts can now make use of a global scope called `global`.


### Changed for 0.2.0

* `Added` Ability to invoke DImgConv.


### Changed for 0.1.1

* `Fixed` The `run` module did not write properly to the make script.
* `Fixed` Projects that are created without the init stubs do not try to append.
* `Added` Mercurial repository support (creates "ignore" file).


### Changed for 0.1.0

* Initial release.


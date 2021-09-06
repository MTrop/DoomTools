DoomMake
--------

### Changed for 0.7.0

* `Added` The options for `TOOL::DOOMTOOLS`.
* `Fixed` Some tweaks to auto-detecting changes to projects.
* `Changed` The options for `TOOL::DECOHACK` to include the source export.


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


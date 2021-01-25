DoomTools (C) 2019-2021
=======================
by Matt Tropiano et al. (see AUTHORS.txt)


DoomMake
--------

### Changed for 1.0.0

* Initial release.


DecoHack
--------

### Changed for 0.6.0

* `Added` Better state referencing.


### Changed for 0.5.0

* `Added` Support for DEHExtra sound definitions (thanks @XaserAcheron).
* `Fixed` Better support for DEHExtra in general and code refactoring to accommodate.


### Changed for 0.4.1

* `Fixed` Changed "Friendly" things constants (`classpath:decohack/constants/friendly_things.dh`).
* `Changed` Updated help.


### Changed for 0.4.0

* `Added` "Friendly" things constants (`classpath:decohack/constants/friendly_things.dh`).
* `Added` Parser: Set weapon/thing states using other weapon/thing states.


### Changed for 0.3.0

* Initial Release.


DMXConv
-------

### Changed for 1.0.0

* Initial Release.


WadMerge
--------

### Changed for 1.4.0

* `Added` An argument for MERGEDIR to omit the directory markers.
* `Added` Support for argument expansion in scripts.
* `Fixed` Handling of PNAMES in MERGEDEUTEXFILE for multiple TEXTURE entries 
  ([Issue 4](https://github.com/MTrop/DoomTools/issues/4)).


### Changed for 1.3.0

* `Fixed` Some performance improvements and verbose output performance.
* `Added` MERGENAMESPACE[FILE] command for merging/importing entry namespaces.
* `Changed` Strings can be bound in double quotes and escape sequences for single tokens.


### Changed for 1.2.0

* `Fixed` Made directory sorting consistent across all merge commands.


### Changed for 1.1.0

* `Added` The `CREATEFILE` command for creating a File instead of a Buffer.
* `Changed` If a problem occurs in any command execution, a better error message is output.
* `Changed` The `MERGEDIR` command sorts files alphabetically, files first, then directories.


### Changed for 1.0.0

* Initial Release.


WSwAnTbl
--------

### Changed for 1.0.0

* Initial Release.


WadTex
------

### Changed for 1.1.0

* `Fixed` Handling of PNAMES for multiple TEXTURE entry import.


### Changed for 1.0.0

* Initial Release.


WTexScan
--------

### Changed for 1.1.0

* `Added` Map filtering switch feature (by @XaserAcheron).


### Changed for 1.0.0

* Initial Release.


WTEXport
--------

### Changed for 1.3.0

* `Fixed` Better support for texture WADs missing a TEXTURE1 lump (but having a TEXTURE2).


### Changed for 1.2.0

* `Fixed` A botched flat and namespace-texture ordering procedure that potentially messed up animations.


### Changed for 1.1.0

* `Changed` Removed an unnecessary sort step that butchered that Animation handling in flats and textures.
* `Changed` Added some needed help.


### Changed for 1.0.0

* Initial Release.


WadScript
---------

### Changed for 1.0.2

* `Added` Made it easier to interface with from other tools.


### Changed for 1.0.1

* `Fixed` Markdown documentation output.


### Changed for 1.0.0

* Initial Release.

WadMerge
--------

### Changed for 1.5.0

* `Changed` Added ability to create IWAD buffers via CREATE and CREATEFILE.


### Changed for 1.4.0

* `Added` An argument for MERGEDIR to omit the directory markers.
* `Added` Support for argument expansion in scripts.
* `Added` MERGEWADDIR for just merging in WADs found in a directory search.
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


WadMerge
--------

### Changed for 1.11.1

* `Fixed` WadMerge will not NPE on a bad directory read (Issue #151).


### Changed for 1.11.0

* `Changed` MERGETEXTUREDIR now works on a "P" namespace if it exists in the target buffer.


### Changed for 1.10.1

* `Fixed` [GUI] Editor would error out on workspace load with no files open.


### Changed for 1.10.0

* `Added` New command: CREATEBUFFER (Enhancement #93).


### Changed for 1.9.1

* `Fixed` [GUI-Editor] Opening a file filtered the wrong kind of files (WadScript, not WadMerge). (Issue #88)
* `Changed` [GUI-Executor] The working directory form field will not auto-fill unless it is blank.


### Changed for 1.9.0

* `Added` [GUI] Verbose output flag for running scripts.
* `Added` MERGEENTRY and MERGEENTRYFILE commands.


### Changed for 1.8.0

* `Added` A `--charset` switch for specifying the encoding of the script files (if not system default).
* `Added` The WadMerge GUI, plus a switch to start it (`--gui`).


### Changed for 1.7.0

* `Added` `FILECHARSUB` command for declaring filename character substitutions on import.
* `Changed` Some behavior to accommodate the character substitution logic.


### Changed for 1.6.0.1

* `Fixed` Some documentation inconsistencies.


### Changed for 1.6.0

* `Added` MERGEDEUTEXFILE can now write a Strife texture entry set.
* `Added` MERGETEXTUREDIR can now write a Strife texture entry set and replace/amend sets and namespaces.
* `Changed` MERGENAMESPACE/MERGENAMESPACEFILE can now optionally import into an existing matching namespace in the destination.


### Changed for 1.5.1

* `Fixed` Saving a WAD from a buffer didn't output a message (introduced in previous version).


### Changed for 1.5.0

* `Changed` Added ability to create IWAD buffers via CREATE and CREATEFILE.
* `Changed` Some non-verbose message output.


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


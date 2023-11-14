WadScript
---------

### Changed for 1.6.2

* `Fixed` [GUI] Editor would error out on workspace load with no files open.


### Changed for 1.6.1

* `Changed` [GUI-Executor] The working directory form field will not auto-fill unless it is blank.


### Changed for 1.6.0

* `Added` A directory tree for the GUI.


### Changed for 1.5.0

* `Fixed` The `--entry` switch didn't parse the command line properly after its use.
* `Added` The WadScript GUI, plus a switch to start it (`--gui`).
* `Added` A `--charset` switch for specifying the encoding of the script files (if not system default).


### Changed for 1.4.0

* `Added` A switch for printing the entry points for a script.


### Changed for 1.3.1

* `Changed` Improved HTML documentation output.


### Changed for 1.3.0

* `Added` A switch for dumping documentation as HTML. (Enhancement #45)


### Changed for 1.2.2

* `Changed` WadScript will now dump a stacktrace if a script host function produces an unhandled exception.


### Changed for 1.2.1

* `Fixed` WADSETTYPE() did not accept two parameters, as described.


### Changed for 1.2.0

* `Added` All scripts can now make use of a global scope called `global`.


### Changed for 1.1.0.1

* `Added` Clarifications to documentation that PKE files are also openable like PK3s.


### Changed for 1.1.0

* `Added` Functions: WADSETTYPE for setting the WAD type.


### Changed for 1.0.2

* `Added` Made it easier to interface with from other tools.


### Changed for 1.0.1

* `Fixed` Markdown documentation output.


### Changed for 1.0.0

* Initial Release.


DImgConv
--------

### Changed for 1.6.0

* `Fixed` Palette index 255 is considered for color matching again on Pictures. Error was in DoomStruct library.


### Changed for 1.5.2

* `Fixed` DIMGConv would not error out completely if a bad file was encountered.


### Changed for 1.5.1

* `Added` [GUI] A status bar and an auto-save switch.


### Changed for 1.5.0

* `Added` [GUI] A graphics offsetter for bulk offsetting a directory of patches. Accessible via GUI or in DoomMake Studio.


### Changed for 1.4.1

* `Fixed` Unrecognized image formats will no longer NullPointerException out.


### Changed for 1.4.0

* `Fixed` Palette index 255 was considered for color matching. This is incorrect on patches/graphics (but correct on flats).


### Changed for 1.3.2

* `Changed` Patches are not split at the first 128 pixels anymore.


### Changed for 1.3.1

* `Fixed` Tall patches not being exported/converted correctly.


### Changed for 1.3.0

* `Fixed` The recursive option now creates recursive directories properly. (Issue #100)
* `Fixed` [GUI] Some blank fields would NPE on workspace export.


### Changed for 1.2.0

* `Added` The GUI version of DImgConv.


### Changed for 1.1.0

* `Fixed` DImgConv would crash if no palette was provided but "palettes" default mode was set and a metadata file tried to change the mode for a file.


### Changed for 1.0.1

* `Fixed` DImgConv did not properly rename files to ".lmp" on convert if both source and destination were directories. (Issue #39)


### Changed for 1.0.0

* Initial Release.


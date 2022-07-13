WTEXport
--------

### Changed for 1.5.0

* `Fixed` Textures/Flats in ANIMATED were added in an incorrect order if the provided texture/flat was not the start of an animation loop. (Issue #75)
* `Added` More output info during the extraction process.
* `Added` The GUI version of WTEXport.
* `Changed` Removed some potential sorts that could ruin things.


### Changed for 1.4.0

* `Fixed` Textures added via ANIMATED searching did not also check for SWITCHES pairings.


### Changed for 1.3.0

* `Fixed` Better support for texture WADs missing a TEXTURE1 lump (but having a TEXTURE2).


### Changed for 1.2.0

* `Fixed` A botched flat and namespace-texture ordering procedure that potentially messed up animations.


### Changed for 1.1.0

* `Changed` Removed an unnecessary sort step that butchered that Animation handling in flats and textures.
* `Changed` Added some needed help.


### Changed for 1.0.0

* Initial Release.


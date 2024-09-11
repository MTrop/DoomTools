WTEXport
--------

### Changed for 1.5.4

* `Fixed` Some texture sets were being erroneously classified as STRIFE-formatted texture sets due to a typo.


### Changed for 1.5.3

* `Fixed` [GUI] Added message for requiring an output WAD file.


### Changed for 1.5.2

* `Fixed` [GUI] Texture list was not set properly from workspace state. (Issue #104)


### Changed for 1.5.1

* `Fixed` Textures can also be entry names. (Issue #96)


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


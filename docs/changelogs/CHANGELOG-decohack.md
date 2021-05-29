DecoHack
--------

### Changed for 0.10.1

* `Fixed` The background flat string indices in the constants files were incorrect.


### Changed for 0.10.0

* `Added` An index value clause for sound indices on parameters.


### Changed for 0.9.0

* `Changed` Handling of protected states - protected states are never treated as free, and cannot be altered directly.


### Changed for 0.8.0

* `Fixed` Parsing a single state body with just the next frame clause did nothing. (Issue #10) (thanks, Aurelius!)
* `Fixed` Certain non-blank DEH Extended states were not accessible.
* `Added` Ability to clear single states or labels on things and weapons. (Issue #11)
* `Changed` `S_FREE_START` in `classpath:decohack/constants/extended/states.dh` is now 1100.


### Changed for 0.7.0

* `Fixed` Disallow MBF action pointers in non-MBF patches.
* `Added` Support for using the "unused" state parameters as offsets (supported in Doom code). (Issue #9)


### Changed for 0.6.1

* `Fixed` Changed file export for compatibility with Java 8.


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


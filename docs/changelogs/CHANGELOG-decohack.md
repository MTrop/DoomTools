DecoHack
--------

### Changed for 0.14.0

* `Added` Each Thing/Weapon Characteristics Filling. (Enhancement #24)


### Changed for 0.13.0

* `Added` Multi-action pointer lines. (Enhancement #16)


### Changed for 0.12.0.1

* `Added` Missing docs for `ammopershot` weapon field.


### Changed for 0.12.0

**Major compiler change. Do not use numbers for your sound definitions and references!**

* `Added` Pre-emptive weapon flag checking on non-MBF21 patches.
* `Fixed` Weapon MBF21 flags were parsed incorrectly.
* `Fixed` Ensure blank files are written when no changes are made.
* `Changed` Sounds definitions now REQUIRE a name, not a number.
* `Changed` Some internal sound index/position handling to align with Doom internals rather than DeHackEd's mishandling of sound definitions.


### Changed for 0.11.2

* `Fixed` Extended (and higher) sound slots were not accessible via definition. (Issue #29)
* `Fixed` NPE if no state clauses between labels. (Issue #28)


### Changed for 0.11.1

* `Fixed` Flag detection priority.
* `Fixed` Splashgroup Outputs as Projectilegroup (Issue #27)
* `Fixed` Melee range not being written correctly (Issue #25)
* `Fixed` State search looped indefinitely if starting index was 0 with no free states. (Issue #26)
* `Fixed` Thing speed info being written on no change.
* `Changed` An error message for valid states.


### Changed for 0.11.0

* `Added` Missing DEHEXTRA thing fields and MBF21 support.
* `Added` Correct action pointer use detection on things and weapons.
* `Added` A way to combine flags per function pointer parameter.
* `Added` `#include` directive aliases for convenience.
* `Fixed` Sound indices for function calls were off by 1.
* `Fixed` Megasphere health misc entry was not parsed! Whoops!
* `Fixed` Megasphere health not initialized, either!


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


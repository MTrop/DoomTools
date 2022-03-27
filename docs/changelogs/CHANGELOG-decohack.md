DECOHack
--------

### Changed for 0.24.0

* `Added` A patch format for the Unity port, `doomunity`, which is `udoom19` but with no string limits (thanks, Xaser!). (PR #65).
* `Added` A way to dump all known action pointers to DECOHack to STDOUT via the `--dump-pointers` runtime switch.
* `Fixed` MBF21's `A_SeekTracer` parameters needed to check for FIXED angles, not UINT.
* `Fixed` Some Action Pointer parameter interpretation - if a field needs a fixed expression, integer values are coerced to fixed, and vice-versa.
* `Changed` Stricter (but safer) checking for types in parameters, such as Fixed values, and auto-detecting sounds, things, and states.


### Changed for 0.23.0

* `Changed` Better string length error messages (Issue/Enhancement #64).


### Changed for 0.22.0

* `Fixed` **MAJOR FIX** DECOHack was completely broken and would NPE out on any parse. Whoops. Fixed!


### Changed for 0.21.0

* `Added` STR_ `#defines` for BOOM's extended locked door strings were missing (thanks, Xaser!). (PR #61)
* `Changed` A_RandomJump and A_WeaponJump should take a UINT, not BYTE for probability (thanks, Altazimuth!). (Issue #62)


### Changed for 0.20.1

* `Fixed` Extended sounds were off by 1. (Issue #60)


### Changed for 0.20.0

* `Added` Multiple input files can be added, parsed in the order provided. (Enhancement #57)
* `Fixed` Bad tokens in action pointer blocks no longer hang the parser. (Issue #59)


### Changed for 0.19.2

* `Fixed` Changed maximum string lengths for Vanilla patches (doom19, udoom19). (Issue #56)


### Changed for 0.19.1

* `Fixed` Thing indices were not being parsed properly in action pointer parameters.


### Changed for 0.19.0

* `Fixed` `A_JumpIfHealthBelow` did not accept negative values for the health reference value. (Issue #53)
* `Fixed` Actor label values were not backfilled properly if the action that used them needed applying to many frames on one line.
* `Added` Auto-things and free-able things (all patches). (Enhancement #50)
* `Added` Aliases for things and weapons via `alias thing` and `alias weapon` statements.
* `Added` DSDHACKED patch support. (Enhancement #42)


### Changed for 0.18.1

* `Fixed` Editor keys were not being saved if a Thing body didn't have 
  parse-able content right after body start.
* `Changed` Copying a definition from the exact same one will not perform a copy,
  as there is nothing to do.


### Changed for 0.18.0

* `Added` Support for editor keys on Things like DECORATE. (Enhancement #47)


### Changed for 0.17.0

* `Added` Labels can now be used pre-declared in actor (thing/weapon) definitions. (Enhancement #35)
* `Added` Some keywords/delimiters to force interpretation of some action pointer parameters.
* `Added` The output source switch for outputting the combined source into one file.
* `Fixed` Some "next state" clauses did not throw errors properly in state blocks.
* `Changed` The budget output does not output the action pointer budget if the patch in Boom format or higher.


### Changed for 0.16.2

* `Fixed` The "Red Skull" pickup string should be `GOTREDSKULL`, with two Ls.
* `Changed` An error is now thrown if a string name is invalid (Boom and higher).


### Changed for 0.16.1

* `Fixed` Things did not have TRANSLUCENT set. (Issue #41)
* `Fixed` NPE on actorless state fill.


### Changed for 0.16.0

* `Changed` Label-Goto-Label syntax in state bodies now supported. (Issue #37)


### Changed for 0.15.0

* `Changed` Altering a state directly now removes its "free" status, if set.
* `Changed` Thing speed now accepts negative values.


### Changed for 0.14.1

* `Fixed` Lexer bugfix. Fixes inconsistent identifier concatenation. (Issue #32)


### Changed for 0.14.0

* `Added` Each Thing/Weapon Characteristics Filling. (Enhancement #24)
* `Added` Individual state property changes.
* `Fixed` A_AddFlags, A_RemoveFlags, A_JumpIfFlagSet used the wrong parameter type.


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


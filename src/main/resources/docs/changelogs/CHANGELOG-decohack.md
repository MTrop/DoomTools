DECOHack
--------

### Changed for 0.38.0

* `Changed` DECOHack now accepts any valid mnemonic for string entries in Boom patches or higher (alphanumeric plus underscore).


### Changed for 0.37.0

* `Fixed` Updated Preprocessor - some directives were not ignored on false code blocks when they should have been.
* `Added` [GUI] Some keywords to the syntax highlighter.


### Changed for 0.36.0

* `Added` State re-use safety for freed states in patches, but only if opted-in by the `set state free safety on` clause.
* `Added` `NOTDMATCH` as a Thing flag mnemonic. It was in the docs, but originally added as `NOTDEATHMATCH`. Oops.
* `Changed` Things, Ammo, and Weapons with changed names will be included in the output DeHackEd patch.
* `Changed` Things with editor keys only will be included in the output DeHackEd patch.


### Changed for 0.35.2

* `Added` Attempting to use `using` a second time will give the user a better error message.


### Changed for 0.35.1

* `Fixed` Setting a Thing's Pain state explicitly (not via States block) would set Pain Chance instead. (Issue #121).


### Changed for 0.35.0

* `Fixed` Soulsphere health misc value did not check the correct property range.
* `Changed` Some Thing bit documentation for Doom19.
* `Changed` Speed on MISSILE Things can now take an explicit fixed-point value (instead of a coerced one).
* `Changed` Thing Health property can go up to a max integer value.
* `Changed` Thing Damage property can go up to an integer value.
* `Changed` Ammo max and pickup properties can go up to a max integer value.
* `Changed` State duration property can go up to an integer value.


### Changed for 0.34.0

* `Fixed` Any clause that sets intervals (freeing things, protecting states) may create a condition that causes an endless loop. (Issue. #119)
* `Fixed` Sound entries were off by 1 due to a misunderstanding of what index sound entries started at. (Issue #120)


### Changed for 0.33.0

* `Added` `set next` clauses for manipulating the next sprite or sound index used in DSDHACKED patches. (Enh. #116)
* `Added` `set next` clause for manipulating the next thing index used in `auto thing`.


### Changed for 0.32.2

* `Fixed` CLEAR STATES in an Each Thing clause did not clear states. (Issue #115)
* `Fixed` CLEAR STATES in an Each Weapon clause did not clear states.


### Changed for 0.32.1

* `Fixed` Mass on Things can be negative, for real this time. (Issue #114)


### Changed for 0.32.0

* `Fixed` [GUI] Some autocomplete docs fixes/changes.
* `Added` Added more valid string mnemonics to Boom patches (notably, Woof/ZDoom obituaries).

### Changed for 0.31.2

* `Added` `TRANSLATION1` as a valid bit flag for Things.


### Changed for 0.31.1

* `Fixed` The Soulsphere, Megasphere, Blur Sphere, and Invulnerability didn't have their `TRANSLUCENT` flag set. (Issue #112)


### Changed for 0.31.0

* `Fixed` [GUI] Editor would error out on workspace load with no files open.
* `Added` A warning for when a user makes a Thing that is `SHOOTABLE` with 0 mass. (Enhancement #103)
* `Changed` Slightly improved some error messages.

### Changed for 0.30.4

* `Fixed` `Fast` and other MBF21 flags were not being respected in state bodies. (Issue #95)


### Changed for 0.30.3

* `Fixed` If DroppedItem was the only change to a Thing, it would not be saved in the patch. (Issue #91)


### Changed for 0.30.2

* `Fixed` Mass on Things can be negative. (Issue #90)


### Changed for 0.30.1

* `Fixed` Macros for `STR_PD_YELLOWK` and `STR_PD_REDK` were swapped (in doom19 and udoom19), plus `MTF_SHOTGUNGUY_DEAD` was added. (Issue #85)


### Changed for 0.30.0

* `Fixed` MBF21 pointer `A_ConsumeAmmo` had an incorrect signature/documentation, despite it compiling properly. (Issue #84)


### Changed for 0.29.0

* `Added` `monstersFightOwnSpecies` miscellany field. (Enhancement #82)


### Changed for 0.28.0

* `Added` Documentation for all pointers in is HTML when dumped with `--dump-pointers-html`.


### Changed for 0.27.0

* `Added` The `state protect` and `state unprotect` clauses now accept state index clauses, not just numbers. (Enhancement #12)
* `Added` Custom properties. (Enhancement #55, #54)


### Changed for 0.26.0

* `Fixed` Added some bit flags that were missing from the docs: UNUSEDX bits, FRIENDLY.
* `Added` A directory tree for the GUI.
* `Added` A warning on wrong int/fixed type use (Enhancement #78).
* `Added` A warning on wrong Thing type use (MISSILE vs. not) (Enhancement #77).


### Changed for 0.25.0

* `Fixed` Changed the z-position parameter type for MBF's A_Spawn pointer to "fixed".
* `Fixed` Changed the range parameter type for MBF21's A_MonsterMeleeAttack pointer to "fixed".
* `Fixed` Some inconsistent error messages around flag setting/removal.
* `Fixed` Thing `dropitem` didn't allow 0 as a viable value.
* `Added` The DECOHack GUI, plus a switch to start it (`--gui`).
* `Added` Documentation for all pointers when dumped with `--dump-pointers`.
* `Added` A `--charset` switch for specifying the encoding of the input source files (if not system default).
* `Added` The ability to write the patch and the source directly into a WAD.
* `Changed` MBF's A_FireOldBFG pointer should have been a weapon pointer instead of a thing pointer.
* `Changed` Added a better error message for bad action pointers.
* `Changed` Added an error message if the user does "flag mixing" on the same expression (see Issue #76).


### Changed for 0.24.1

* `Fixed` MBF Action Pointers were not recognized. This has been fixed.
* `Fixed` An error is now thrown if you are attempting to define a custom action pointer that already exists.


### Changed for 0.24.0

* `Added` The ability to read a DECOHack patch from STDIN.
* `Added` The ability to add custom action pointers. (Enhancement #72)


### Changed for 0.23.0

* `Added` A patch format for the Unity port, `doomunity`, which is `udoom19` but with no string limits (thanks, Xaser!). (PR #65).
* `Added` A way to dump all known action pointers to DECOHack to STDOUT via the `--dump-pointers` runtime switch.
* `Fixed` MBF21's `A_SeekTracer` parameters needed to check for FIXED angles, not UINT.
* `Fixed` Some Action Pointer parameter interpretation - if a field needs a fixed expression, integer values are coerced to fixed, and vice-versa.
* `Changed` Stricter (but safer) checking for types in parameters, such as Fixed values, and auto-detecting sounds, things, and states.
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


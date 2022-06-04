# Doom Tools

Copyright (c) 2019-2022 Matt Tropiano  

### Required Libraries

[Doom Struct](https://github.com/MTrop/DoomStruct) 2.14.2+  
[Black Rook JSON](https://blackrooksoftware.github.io/JSON/) 1.2.0+  
[RookScript](https://blackrooksoftware.github.io/RookScript/) 1.14.1+  
[RookScript-Desktop](https://blackrooksoftware.github.io/RookScript/) 1.10.2.1+

[FlatLaF](https://www.formdev.com/flatlaf/) 2.0.2+  
[RSyntaxTextArea](http://bobbylight.github.io/RSyntaxTextArea/) 3.0.2+

### Required Modules

[java.desktop](https://docs.oracle.com/en/java/javase/11/docs/api/java.desktop/module-summary.html)  
* [java.xml](https://docs.oracle.com/en/java/javase/11/docs/api/java.xml/module-summary.html)  
* [java.datatransfer](https://docs.oracle.com/en/java/javase/11/docs/api/java.datatransfer/module-summary.html)  
* [java.base](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/module-summary.html)  

### Source

The `master` branch contains stable code. Until a release is cut, the `master` branch will be shifting. 

### Introduction

A bunch of command-line utilities for Doom stuff. This time, they're useful.

### Why?

Because they are all super useful, and can run anywhere (that runs Java). This also enables authors to
create a Continuous Integration path for building ongoing WAD projects. How cool would it be to always 
have a demo copy buildable at all times?

### Compiling with Ant

**First**, to download dependencies for this project, type (`build.properties` will also be altered/created):

	ant dependencies

To compile this program with Apache Ant, type:

	ant compile

To make Maven-compatible JARs of this program (placed in the *build/jar* directory), type:

	ant jar

To make Javadocs (placed in the *build/docs* directory):

	ant javadoc

To compile main and test code and run tests (if any):

	ant test

To make Zip archives of everything (main src/resources, bin, javadocs, placed in the *build/zip* directory):

	ant zip

To create a Windows Installer in the `dist` folder:

	ant dist.installer -Dinno.setup.dir=[Path-To-Inno-Setup] -Dembedded.jre.source.dir=[Path-To-Embedded-JRE]

To create a distribution (Bash and CMD):

	ant dist -Dnatives.windows.dir=[PathTo-DoomTools-GUI-Native-Project] -Dinno.setup.dir=[Path-To-Inno-Setup] -Dembedded.jre.source.dir=[Path-To-Embedded-JRE]

To create a distribution and deploy it (THIS WILL DELETE AND REBUILD THE TARGET DIRECTORY, `TARGETPATH`):

	ant deploy.cmd -Ddeploy.dir=[TARGETPATH]
	ant deploy.bash -Ddeploy.dir=[TARGETPATH]

To clean up everything:

	ant clean


Note that the `deploy.dir`, `natives.windows.dir`, `inno.setup.dir`, and `embedded.jre.source.dir` 
properties are supplied above - it may be better to add local paths to your `build.properties` file for those.


### Installer Building and Native Code

Creating a bootstrap EXE for Windows GUIs requires another project, [DoomTools-GUI-Native](https://github.com/MTrop/DoomTools-GUI-Native).
It is not necessary for building or local deploying, but a package distributable will not be complete without it.

The EXE build and copy to `src/maim/resources/shell/exe` will be skipped if the `natives.windows.dir` property is not set. You
are better off not setting it for every build as the EXE will more than likely be built differently every time.

Building a Windows Installer requires [Inno Setup 6.2.0+](https://jrsoftware.org/isdl.php), and requires that the property `inno.setup.dir`
is set to Inno Setup's directory to make use of `iscc`.

The installer target will be skipped if the `inno.setup.dir` property is not set, and the embedded 
JRE version of the installer will not be built if `embedded.jre.source.dir` is not set.


### Recommended Minified JRE Setup

The JRE added to the installer (or even just for running DoomTools) should be one made with the following command line:

	jlink --add-modules java.desktop,jdk.crypto.ec --compress=2 --output [jredir]

That's `java.desktop` for the required packages and `jdk.crypto.ec` to ensure it can hit secure sites for updating itself
and perhaps future tools that pull resources from Internet addresses.


### Other Notes

Even though this project can technically be used as a library, use caution when integrating this package 
with your own programs - contents and APIs that are not part of other third-party libraries will be in 
flux as the utilities evolve. This project makes no promises about a consistent API structure within itself,
despite keeping things documented in a publicly-accessible fashion.


### Utilities

#### DoomTools

A program that just displays info about the toolset that you are using (or manage other things, in the future).

#### DecoHack

A utility that uses a DECORATE-like language scheme for creating DeHackEd patches.

#### DImgConv

A utility that bulk converts images to Doom formats.

#### DMXConv

A utility that converts all sorts of sounds to DMX digital sounds using either the Java SPI or FFmpeg.

#### DoomMake

A project build utility that is agnostic to all operating systems (for the most part).

#### WadMerge

A utility that performs Script-based WAD compilation and merging. 

#### WSwAnTbl

A utility that reads DEFSWANI/SWANTBLS data files and compiles it into ANIMATED and SWITCHES lumps.
Can also export a set of ANIMATED and SWITCHES lumps to a definition file.

#### WadTex

A utility that reads a DEUTEX-style texture file and imports it into a WAD file as TEXTUREx and PNAMES lumps.
Can also export TEXTUREx/PNAMES in the same way. 

#### WTexScan

A utility that scans maps in a WAD and outputs a list of found textures and flats, suitable for import into the next program...

#### WTEXport

A utility that exports textures from one WAD into another, including associated ANIMATED and SWITCHES textures.

#### WadScript

A scripting system for doing practically anything with Doom stuff. Also includes Rookscript, a subset script. 


### Special Thanks

Special thanks to, in no particular order, **Aurelius**, **Antares031**, **floatRand**, **Xaser**, **kraflab**, 
and **punchyouinthefaceman** for their incidental QA testing. Lots of bugs were fixed because of all of you!


### Other

These programs and the accompanying materials are made available under the 
terms of the MIT License, which accompanies this distribution.

A copy of the MIT License should have been included in this release (LICENSE.txt).
If it was not, please contact us for a copy, or to notify us of a distribution
that has not included it. 

This contains code copied from Black Rook Base, under the terms of the MIT License (docs/LICENSE-BlackRookBase.txt).

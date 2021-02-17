# Doom Tools

Copyright (c) 2019-2021 Matt Tropiano  

### Required Libraries

[Doom Struct](https://github.com/MTrop/DoomStruct) 2.11.0+  
[RookScript](https://blackrooksoftware.github.io/RookScript/) 1.10.2+

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

To download dependencies for this project, type (`build.properties` will also be altered/created):

	ant dependencies

To compile this library with Apache Ant, type:

	ant compile

To make Maven-compatible JARs of this library (placed in the *build/jar* directory), type:

	ant jar

To make Javadocs (placed in the *build/docs* directory):

	ant javadoc

To compile main and test code and run tests (if any):

	ant test

To make Zip archives of everything (main src/resources, bin, javadocs, placed in the *build/zip* directory):

	ant zip

To compile, JAR, test, and Zip up everything:

	ant release

To create a distribution (Bash and CMD):

	ant dist

To create a distribution and deploy it (THIS WILL DELETE AND REBUILD THE TARGET DIRECTORY):

	ant deploy.cmd -Ddeploy.dir=[TARGETPATH]
	ant deploy.bash -Ddeploy.dir=[TARGETPATH]

To clean up everything:

	ant clean


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


### Other

These programs and the accompanying materials are made available under the 
terms of the MIT License, which accompanies this distribution.

A copy of the MIT License should have been included in this release (LICENSE.txt).
If it was not, please contact us for a copy, or to notify us of a distribution
that has not included it. 

This contains code copied from Black Rook Base, under the terms of the MIT License (docs/LICENSE-BlackRookBase.txt).

# Doom Tools

Copyright (c) 2019 Matt Tropiano  

### Required Libraries

[Doom Struct](https://github.com/MTrop/DoomStruct) 2.6.0+

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

Because it's super useful, and can run anywhere (that runs Java).

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

### Other

This program/library and the accompanying materials
are made available under the terms of the GNU Lesser Public License v2.1
which accompanies this distribution, and is available at
http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html

A copy of the LGPL should have been included in this release (LICENSE.txt).
If it was not, please contact me for a copy, or to notify me of a distribution
that has not included it. 

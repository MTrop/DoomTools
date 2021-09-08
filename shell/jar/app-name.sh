#!/bin/bash

JAVAOPTS="{{JAVA_OPTIONS}}"
JAVAJAR={{JAR_NAME}}
MAINCLASS={{MAIN_CLASSNAME}}

CMD_READLINK="readlink -f"
if [[ "$OSTYPE" == "darwin"* ]]; then
	function realpath {
    	[ "." = "${1}" ] && n=${PWD} || n=${1}; while nn=$( readlink -n "$n" ); do n=$nn; done; echo "$n"
	}
	CMD_READLINK="realpath"
fi
SCRIPTDIR="$(cd "$(dirname $($CMD_READLINK "$0"))"; pwd)"

# Test for Java
if [ -f "$SCRIPTDIR/jre/bin/java" ]; then
	JAVACMD="$SCRIPTDIR/jre/bin/java"
elif hash java 2>/dev/null; then
	JAVACMD=java
elif [ -n "${JAVA_HOME}" ]; then
	JAVACMD="${JAVA_HOME}/bin/java"
elif [ -n "${JDK_HOME}" ]; then
	JAVACMD="${JDK_HOME}/bin/java"
elif [ -n "${JRE_HOME}" ]; then
	JAVACMD="${JRE_HOME}/java"
fi

if [[ -n "$JAVACMD" ]]; then
	"$JAVACMD" -cp "$SCRIPTDIR/$JAVAJAR" $JAVAOPTS $MAINCLASS $*
else
	echo "Java 8 or higher could not be detected. To use these tools, a JRE must be"
	echo "installed."
	echo
	echo "The environment variables JAVA_HOME, JRE_HOME, or JDK_HOME are not set to"
	echo "your JRE or JDK directories, nor were Java binaries detected on your PATH."
	echo
	echo "For help, visit https://www.java.com/."
fi

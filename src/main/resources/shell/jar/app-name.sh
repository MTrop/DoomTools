#!/bin/bash

# ===========================================================================

CMD_READLINK="readlink -f"
if [[ "$OSTYPE" == "darwin"* ]]; then
	function realpath {
		[ "." = "${1}" ] && n=${PWD} || n=${1}; while nn=$( readlink -n "$n" ); do n=$nn; done; echo "$n"
	}
	CMD_READLINK="realpath"
fi

# ===========================================================================

JAVAOPTS="{{JAVA_OPTIONS}}"
MAINCLASS={{MAIN_CLASSNAME}}

export DOOMTOOLS_PATH="$(cd "$(dirname $($CMD_READLINK "$0"))"; pwd)"
export DOOMTOOLS_JAR="jar/$((cd ${DOOMTOOLS_PATH}/jar && ls -1a *.jar) | sort | tail -1)"
JAR_PATH="${DOOMTOOLS_PATH}/${DOOMTOOLS_JAR}"
if [[ "$OSTYPE" == "cygwin"* ]]; then
	JAR_PATH="$(cygpath -w -a "${JAR_PATH}")"
	DOOMTOOLS_PATH="$(cygpath -w -a "${DOOMTOOLS_PATH}")"
fi

# ===========================================================================
# Test for Java
if [ -f "${DOOMTOOLS_PATH}/jre/bin/java" ]; then
	JAVACMD="${DOOMTOOLS_PATH}/jre/bin/java"
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
	"$JAVACMD" -cp "${JAR_PATH}" $JAVAOPTS $MAINCLASS $*
else
	echo "Java 8 or higher could not be detected. To use these tools, a JRE must be"
	echo "installed."
	echo
	echo "The environment variables JAVA_HOME, JRE_HOME, or JDK_HOME are not set to"
	echo "your JRE or JDK directories, nor were Java binaries detected on your PATH."
	echo
	echo "For help, visit https://www.java.com/."
	echo "Java can be downloaded from the following places:"
	echo
	echo "Azul:      https://www.azul.com/downloads/"
	echo "Adoptium:  https://adoptium.net/temurin/releases/"
	echo "Microsoft: https://www.microsoft.com/openjdk"
	echo "Oracle:    https://java.com/en/download/"
	echo
fi

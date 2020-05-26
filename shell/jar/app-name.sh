#!/bin/bash

CMD_READLINK="readlink -f"
if [[ "$OSTYPE" == "darwin"* ]]; then
	function realpath {
    	[ "." = "${1}" ] && n=${PWD} || n=${1}; while nn=$( readlink -n "$n" ); do n=$nn; done; echo "$n"
	}
	CMD_READLINK="realpath"
fi
SCRIPTDIR="$(cd "$(dirname $($CMD_READLINK "$0"))"; pwd)"

JAVAOPTS={{JAVA_OPTIONS}}
JAVAJAR={{JAR_NAME}}
MAINCLASS={{MAIN_CLASSNAME}}

java -cp "$SCRIPTDIR/$JAVAJAR" $JAVAOPTS $MAINCLASS $*

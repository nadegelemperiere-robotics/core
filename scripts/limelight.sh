#!/bin/sh

# Retrieve absolute path to this script
script=$(readlink -f $0)
scriptpath=`dirname $script`

# Configure routing in control hub
adb shell "su; sh -s" < $scriptpath/routing.sh
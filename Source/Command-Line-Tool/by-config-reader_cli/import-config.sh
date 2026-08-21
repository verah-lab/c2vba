#!/bin/sh

##########################################
#
# Settings:
#
##########################################

tool_version=0.1.2


##########################################
#
# Programm call:
# should not be neccessary to be changed
#
##########################################

cmdToRun="java -jar"

# add configuration file locations:
cmdToRun+=" -Dspring.config.location=file:application.properties"

# use a port, that is not used by another application at the same time:
cmdToRun+=" -Dserver.port=8090"

# name the application jar and the method to call (optional)
cmdToRun+=" by-config-reader-$tool_version-exec.jar import"

# write command line to console:
echo $cmdToRun

# run the command:
eval $cmdToRun

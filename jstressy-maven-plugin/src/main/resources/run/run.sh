#!/bin/bash
# DESCRIPTION: Felix OSGi Framework configuration script.
# COMMENT: JStressy configuration for running under Felix OSGi Framework

# Apply correct umask before doing anything else - some folders are created
# also before the actual run.
umask 000

# Configuration properties
CONFIGURATION_DIRECTORY="conf"
CONFIG_PROPERTIES_FILE="$CONFIGURATION_DIRECTORY/config.properties"
CONFIG_PROPERTIES_PARAMETER="-Dfelix.config.properties=file:$CONFIG_PROPERTIES_FILE"

#Parameters

MEMORY="-Xms512M -Xmx2048M"
DEBUG="-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n"
LOG="-Dlogback.configurationFile=./$CONFIGURATION_DIRECTORY/logback.xml"
STRESSY_CONF="-DconfigFolder=."

java $MEMORY $DEBUG $LOG $STRESSY_CONF -jar ./bin/felix.jar

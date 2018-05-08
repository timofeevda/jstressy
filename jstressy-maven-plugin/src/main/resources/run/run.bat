@echo off

rem Apache Felix Configuration properties

set CONFIGURATION_DIRECTORY=conf
set CONFIG_PROPERTIES_FILE=%CONFIGURATION_DIRECTORY%/config.properties
set CONFIG_PROPERTIES_PARAMETER="-Dfelix.config.properties=file:%CONFIG_PROPERTIES_FILE%"

rem Configuration parameters

set MEMORY=-Xms512M -Xmx2048M
set DEBUG=-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n
set LOG=-Dlogback.configurationFile=./%CONFIGURATION_DIRECTORY%/logback.xml
set STRESSY_CONF=-DconfigFile=stressy.yml

java %MEMORY% %DEBUG% %LOG% %STRESSY_CONF% -jar ./bin/felix.jar


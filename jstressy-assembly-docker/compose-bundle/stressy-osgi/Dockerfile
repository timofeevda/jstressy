FROM openjdk:8u111-jdk-alpine

LABEL maintainer="Denis Timofeev <timofeevda@gmail.com>"

ENV STRESSY_MEMORY "-Xms1024M -Xmx2048M"
ENV STRESSY_DEBUG "-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n"
ENV STRESSY_LOG "-Dlogback.configurationFile=logback.xml"
ENV STRESSY_LOG_LEVEL "info"
ENV STRESSY_CONF "-DconfigFolder=/etc/stressy"
ENV FELIX_CONF "-Dfelix.config.properties=file:conf/config.properties"

COPY bin bin
COPY bundles bundles
COPY conf conf
COPY logback.xml logback.xml

CMD java $STRESSY_MEMORY $STRESSY_DEBUG $STRESSY_LOG \
     $STRESSY_CONF $FELIX_CONF -jar ./bin/felix.jar


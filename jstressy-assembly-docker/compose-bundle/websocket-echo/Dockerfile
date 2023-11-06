FROM openjdk:8u111-jdk-alpine

LABEL maintainer="Denis Timofeev <timofeevda@gmail.com>"

ENV MEMORY "-Xms1024M -Xmx2048M"
ENV DEBUG "-Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n"
ENV LOG "-Dlogging.config=logback.xml"

COPY logback.xml logback.xml
COPY jstressy-websocket-echo.jar jstressy-websocket-echo.jar

CMD java $MEMORY $DEBUG $LOG -jar jstressy-websocket-echo.jar


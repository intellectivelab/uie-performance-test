#!/bin/sh

printHelp() {
echo
echo "Unity Intelligence Engine Performance Tests. Copyright (c) 2005-2019 Intellective Inc."
echo.
echo.
echo.
echo
}

if [ "$1" = '--help' ]; then
        printHelp
        exit 1
fi


${JAVA_HOME}/bin/java -Xmx3072m -XX:MaxGCPauseMillis=200 -XX:+UseG1GC -jar uie-performance-test.jar
#${JAVA_HOME}/bin/java -Xmx3072m -XX:MaxGCPauseMillis=200 -XX:+UseG1GC -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005 -jar uie-performance-test.jar

exit 0
#!/bin/sh
#
# Starts publet server
#
# You can change properties in `etc/server.properties` as they
# will override all system properties.
#

# find working dir and cd into it
cd `dirname $0`/..

JAVA_OPTS="-server -Xmx1024M -Djava.awt.headless=true -jar"
java $JAVA_OPTS bin/publet-server.jar --start

cd -
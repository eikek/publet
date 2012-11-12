#!/bin/sh
#
# Starts publet server
#
# You can change properties in `etc/server.properties` and using
# system properties. System properties override those settings in
# the configuration file.
#

# find utf8 locale and set it
LOC=`locale -a | grep -i utf8 | head -n1`
if [ -n "$LOC" ]; then
  export LC_ALL=$LOC
fi

# find working dir and cd into it
cd `dirname $0`/..

JAVA_OPTS="-server -Xmx512M -Djava.awt.headless=true -jar \
 -Dpublet.server.port=8080 \
 -Djava.io.tmpdir=`pwd`/temp \
 -Dlogback.configurationFile=`pwd`/etc/logback.xml"
java $JAVA_OPTS bin/publet-server.jar --start

cd -

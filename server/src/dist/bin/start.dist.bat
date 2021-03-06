@echo off
java -version

rem You can change properties in `etc/server.properties` and using
rem system properties. System properties override those settings in
rem the configuration file.

rem change in correct working directory
cd %~dp0\..

set JAVA_OPTS=-Xmx512M -Djava.awt.headless=true -jar -Dpublet.server.port=8080 -Djava.io.tmpdir=temp -Dlogback.configurationFile=etc\logback.xml
java %JAVA_OPTS% bin/publet-server.jar --start

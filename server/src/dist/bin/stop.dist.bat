@echo off
java -version

rem change in correct working directory
cd %~dp0\..

set JAVA_OPTS=-jar
java %JAVA_OPTS% bin/publet-server.jar --stop

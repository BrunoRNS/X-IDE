@echo off
rem X-IDE client launcher: run-client.cmd [endpoint] [route] [--message payload]
java -jar "%~dp0client-1.0-SNAPSHOT-all.jar" %*

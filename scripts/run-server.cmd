@echo off
rem X-IDE server launcher: requires a JRE 17+ on PATH and the DB_* environment
rem variables for MySQL.
java -jar "%~dp0server.jar" %*

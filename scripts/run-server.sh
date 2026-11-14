#!/bin/sh
# X-IDE server launcher: starts the shaded server JAR (WebSocket on 8082 and
# HTTP/Javalin on 8081, fronted by Nginx on 8080 in the container stack).
# Requires a JRE 17+ on PATH and the DB_* environment variables for MySQL.
set -eu
JAR_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
exec java -jar "$JAR_DIR/server-1.0-SNAPSHOT-all.jar" "$@"

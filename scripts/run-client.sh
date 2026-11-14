#!/bin/sh
# X-IDE client launcher: run-client.sh [endpoint] [route] [--message payload]
# Defaults: endpoint ws://localhost:8080/ws/socket, route /develop/ping.
set -eu
JAR_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
exec java -jar "$JAR_DIR/client-1.0-SNAPSHOT-all.jar" "$@"

#!/bin/sh
# X-IDE client launcher: run-client.sh [endpoint] [route] [--message payload]
# Defaults: endpoint wss://localhost:8443/ws/socket, route /develop/ping.
set -eu
JAR_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
exec java -jar "$JAR_DIR/client-all.jar" "$@"

#!/bin/sh
# X-IDE container entrypoint: WebSocket server (server.jar) + Nginx edge.
set -eu

java -jar /opt/xide/server.jar &
exec nginx -g 'daemon off;'

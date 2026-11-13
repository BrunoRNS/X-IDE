#!/bin/sh
set -eu

java -jar /opt/xide/server.jar &
exec nginx -g 'daemon off;'

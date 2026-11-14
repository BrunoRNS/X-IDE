#!/usr/bin/env bash

set -uo pipefail

ENDPOINT="${CLIENT_ENDPOINT:-wss://localhost:8443/ws/socket}"
JAR="client/target/client-1.0-SNAPSHOT-all.jar"

if [ ! -f "$JAR" ]; then
    echo "client JAR not found: $JAR (run 'make package-all' first)"
    exit 1
fi

host=$(printf '%s' "$ENDPOINT" | sed -n 's#^[a-zA-Z][a-zA-Z0-9+.-]*://\([^/:]*\).*#\1#p')
port=$(printf '%s' "$ENDPOINT" | sed -n 's#^[a-zA-Z][a-zA-Z0-9+.-]*://[^/]*:\([0-9]*\)/.*#\1#p')
host=${host:-127.0.0.1}
port=${port:-8443}

if ! (exec 3<>"/dev/tcp/$host/$port") 2>/dev/null; then
    echo "stack is not reachable at $host:$port (run 'make run-server' first)"
    exit 1
fi


passed=0
failed=0

expect() {
    local name="$1" expected="$2"
    shift 2
    local output
    if output=$(java -jar "$JAR" "$@") && [[ "$output" == *"$expected"* ]]; then
        echo "PASS $name"
        passed=$((passed + 1))
    else
        echo "FAIL $name (expected output containing: $expected)"
        failed=$((failed + 1))
    fi
}

expect "client ping -> server pong" '"payload":"pong"' \
    "$ENDPOINT" /develop/ping --message ping 
expect "server ping -> client pong (push)" '"payload":"pinged"' \
    "$ENDPOINT" /develop/ping-clients --message go 
expect "server recorded the client pong" '"payload":"pong"' \
    "$ENDPOINT" /develop/last-pong --message check 

echo "communication-test: $passed passed, $failed failed"
[ "$failed" -eq 0 ]

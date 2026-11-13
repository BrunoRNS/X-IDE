#!/usr/bin/env bash
# Verifies the ping/pong handshake in both directions against a running stack,
# using the real client JAR. `make package-all` builds the JAR and
# `make run-server` starts the stack.
set -uo pipefail

ENDPOINT="${CLIENT_ENDPOINT:-wss://localhost:8443/ws/socket}"
TRUST_CERT="${CLIENT_TRUST_CERT:-infra/nginx/certs/server.crt}"
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

scheme=${ENDPOINT%%://*}
tls_args=()
if [ "$scheme" = "wss" ]; then
    if [ -f "$TRUST_CERT" ]; then
        tls_args=(--trust-cert "$TRUST_CERT")
    else
        echo "WSS endpoint needs the edge certificate $TRUST_CERT (run 'make run-server' first)"
        exit 1
    fi
fi

passed=0
failed=0

expect() {
    local name="$1" expected="$2"
    shift 2
    local output
    if output=$(java -jar "$JAR" "$@" 2>/dev/null) && [[ "$output" == *"$expected"* ]]; then
        echo "PASS $name"
        passed=$((passed + 1))
    else
        echo "FAIL $name (expected output containing: $expected)"
        failed=$((failed + 1))
    fi
}

expect "client ping -> server pong" '"payload":"pong"' \
    "$ENDPOINT" /develop/ping --message ping "${tls_args[@]}"
expect "server ping -> client pong (push)" '"payload":"pinged"' \
    "$ENDPOINT" /develop/ping-clients --message go "${tls_args[@]}"
expect "server recorded the client pong" '"payload":"pong"' \
    "$ENDPOINT" /develop/last-pong --message check "${tls_args[@]}"

echo "communication-test: $passed passed, $failed failed"
[ "$failed" -eq 0 ]

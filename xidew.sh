#!/usr/bin/env sh
# Interactive X-IDE project manager for Linux/macOS.
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
BUILD_DIR=${XIDE_BUILD_DIR:-"$ROOT/build"}
ensure_configured() {
  [ -f "$BUILD_DIR/CMakeCache.txt" ] || cmake -S "$ROOT" -B "$BUILD_DIR"
  ENGINE=$(sed -n 's/^XIDE_CONTAINER_ENGINE:STRING=//p' "$BUILD_DIR/CMakeCache.txt" | head -n 1)
  [ -z "$ENGINE" ] || export XIDE_CONTAINER_ENGINE="$ENGINE"
}
usage() {
cat <<'EOF'
X-IDE project manager (Linux/macOS)

Build scopes (see each module's src/main/java and src/test/java package-info.java):
  build-common | build-client | build-server | build-integration | build-all
  package-all

JUnit scopes:
  test-common | test-client | test-server | test-all

Secure runtime:
  run-server [--user USER] [--password PASSWORD]
      Starts MySQL + Java server + Nginx TLS 1.3/WSS. If password is omitted,
      asks for it without echo. Existing MySQL user is verified, not overwritten.
  stop-server | server-logs | stack-status
  run-client --user USER --password PASSWORD [--url wss://HOST:8443/ws]
      Optional: --trust-cert PEM --message TEXT
  run-clients --user USER --password PASSWORD [--count N] [--url URL] [--trust-cert PEM]
  communication-test [--user USER] [--password PASSWORD]
  remote-test --url WSS_URL --user USER --password PASSWORD [--trust-cert PEM]

Project:
  configure | help | exit | quit

Example local workflow:
  run-server                         # prompts for the default account password
  run-client --user default --password 'your-password' --trust-cert deploy/certs/server.crt
  run-clients --user default --password 'your-password' --count 12 --trust-cert deploy/certs/server.crt
  stop-server

The local test certificate is self-signed and valid for localhost only. For a
remote host, install/use a trusted TLS 1.3 certificate and omit --trust-cert.
EOF
}
run_target() { ensure_configured; cmake --build "$BUILD_DIR" --target "$1"; }
run_clients() {
  USER_NAME= PASSWORD= URL=wss://localhost:8443/ws CERT= COUNT=5
  while [ "$#" -gt 0 ]; do case "$1" in
    --user|--password|--url|--trust-cert|--count) [ "$#" -ge 2 ] || { echo "$1 requires a value" >&2; return 2; }; k=$1; v=$2; shift 2; case "$k" in --user) USER_NAME=$v;; --password) PASSWORD=$v;; --url) URL=$v;; --trust-cert) CERT=$v;; --count) COUNT=$v;; esac;;
    *) echo "Unknown run-clients option: $1" >&2; return 2;; esac; done
  [ -n "$USER_NAME" ] && [ -n "$PASSWORD" ] || { echo 'run-clients requires --user and --password.' >&2; return 2; }
  case "$COUNT" in ''|*[!0-9]*) echo '--count must be an integer' >&2; return 2;; esac
  ensure_configured; cmake --build "$BUILD_DIR" --target build-integration
  [ -n "$CERT" ] || { case "$URL" in wss://localhost:8443/ws|wss://127.0.0.1:8443/ws) CERT="$ROOT/deploy/certs/server.crt";; esac; }
  set -- --url "$URL" --user "$USER_NAME" --password "$PASSWORD" --count "$COUNT"
  [ -z "$CERT" ] || set -- "$@" --trust-cert "$CERT"
  java -jar "$ROOT/integration/target/x-ide-concurrent-probe.jar" "$@"
}
dispatch() {
  cmd=${1:-help}; shift || true
  case "$cmd" in
    help|-h|--help) usage;;
    configure) cmake -S "$ROOT" -B "$BUILD_DIR";;
    build-common|build-client|build-server|build-integration|build-all|package-all|test-common|test-client|test-server|test-all|stack-status) run_target "$cmd";;
    package) run_target package-all;;
    run-server) ensure_configured; cmake --build "$BUILD_DIR" --target build-server; "$ROOT/scripts/run-server.sh" "$@";;
    stop-server) run_target stack-down;;
    server-logs) run_target stack-logs;;
    run-client) "$ROOT/scripts/run-client.sh" "$@";;
    run-clients) run_clients "$@";;
    communication-test) ensure_configured; cmake --build "$BUILD_DIR" --target build-server; cmake --build "$BUILD_DIR" --target build-integration; "$ROOT/scripts/communication-test.sh" "$@";;
    remote-test)
      URL= USER_NAME= PASSWORD= CERT=
      while [ "$#" -gt 0 ]; do case "$1" in --url|--user|--password|--trust-cert) [ "$#" -ge 2 ] || { echo "$1 requires a value" >&2; return 2; }; k=$1; v=$2; shift 2; case "$k" in --url) URL=$v;; --user) USER_NAME=$v;; --password) PASSWORD=$v;; --trust-cert) CERT=$v;; esac;; *) echo "Unknown option: $1" >&2; return 2;; esac; done
      [ -n "$URL" ] && [ -n "$USER_NAME" ] && [ -n "$PASSWORD" ] || { echo 'remote-test requires --url, --user and --password.' >&2; return 2; }
      ensure_configured; cmake --build "$BUILD_DIR" --target build-integration
      set -- --url "$URL" --user "$USER_NAME" --password "$PASSWORD"; [ -z "$CERT" ] || set -- "$@" --trust-cert "$CERT"
      java -jar "$ROOT/integration/target/x-ide-communication-probe.jar" "$@";;
    exit|quit) return 99;;
    *) echo "Unknown command: $cmd" >&2; echo "Use 'help'." >&2; return 2;;
  esac
}
parse_and_dispatch() {
  input=$1; token=; quote=; started=0; set --; tab=$(printf '\t')
  while [ -n "$input" ]; do
    char=${input%"${input#?}"}; input=${input#?}
    case "$char" in
      \\) [ -n "$input" ] || { echo 'Trailing escape.' >&2; return 2; }; char=${input%"${input#?}"}; input=${input#?}; token=$token$char; started=1;;
      \'|\") if [ -z "$quote" ]; then quote=$char; started=1; elif [ "$quote" = "$char" ]; then quote=; else token=$token$char; fi;;
      ' '|"$tab") if [ -n "$quote" ]; then token=$token$char; elif [ "$started" -eq 1 ]; then set -- "$@" "$token"; token=; started=0; fi;;
      *) token=$token$char; started=1;;
    esac
  done
  [ -z "$quote" ] || { echo 'Unclosed quote.' >&2; return 2; }
  [ "$started" -eq 0 ] || set -- "$@" "$token"
  [ "$#" -eq 0 ] || dispatch "$@"
}
if [ "$#" -gt 0 ]; then dispatch "$@"; exit $?; fi
usage
while :; do printf '\nxide> '; IFS= read -r line || break; [ -n "$line" ] || continue
  if parse_and_dispatch "$line"; then :; else status=$?; [ "$status" -eq 99 ] && break; fi
done

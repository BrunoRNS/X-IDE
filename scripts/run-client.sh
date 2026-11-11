#!/usr/bin/env sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
USER_NAME= PASSWORD= URL=wss://localhost:8443/ws CERT=
MESSAGE='hello from X-IDE'
while [ "$#" -gt 0 ]; do
  case "$1" in
    --user|--password|--url|--trust-cert|--message)
      [ "$#" -ge 2 ] || { echo "$1 requires a value" >&2; exit 2; }
      key=$1; value=$2; shift 2
      case "$key" in --user) USER_NAME=$value;; --password) PASSWORD=$value;; --url) URL=$value;; --trust-cert) CERT=$value;; --message) MESSAGE=$value;; esac ;;
    -h|--help) echo 'Usage: run-client.sh --user USER --password PASSWORD [--url wss://host:8443/ws] [--trust-cert CERT] [--message TEXT]'; exit 0 ;;
    *) echo "Unknown client option: $1" >&2; exit 2 ;;
  esac
done
[ -n "$USER_NAME" ] && [ -n "$PASSWORD" ] || { echo 'run-client requires --user and --password.' >&2; exit 2; }
[ -f "${XIDE_BUILD_DIR:-$ROOT/build}/CMakeCache.txt" ] || cmake -S "$ROOT" -B "${XIDE_BUILD_DIR:-$ROOT/build}"
cmake --build "${XIDE_BUILD_DIR:-$ROOT/build}" --target build-client
if [ -z "$CERT" ] && { [ "$URL" = 'wss://localhost:8443/ws' ] || [ "$URL" = 'wss://127.0.0.1:8443/ws' ]; }; then CERT="$ROOT/deploy/certs/server.crt"; fi
set -- --url "$URL" --user "$USER_NAME" --password "$PASSWORD" --message "$MESSAGE"
[ -z "$CERT" ] || set -- "$@" --trust-cert "$CERT"
java -jar "$ROOT/client/target/x-ide-client.jar" "$@"

#!/usr/bin/env sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
USER_NAME=default PASSWORD= AUTO_PASSWORD=0
while [ "$#" -gt 0 ]; do
  case "$1" in
    --user) [ "$#" -ge 2 ] || { echo '--user requires a value' >&2; exit 2; }; USER_NAME=$2; shift 2;;
    --password) [ "$#" -ge 2 ] || { echo '--password requires a value' >&2; exit 2; }; PASSWORD=$2; shift 2;;
    *) echo "Unknown option: $1" >&2; exit 2;;
  esac
done
if [ -z "$PASSWORD" ]; then
  command -v openssl >/dev/null 2>&1 || { echo 'openssl is required to generate a random test password.' >&2; exit 1; }
  ENV_FILE="$ROOT/deploy/.env"
  if [ -f "$ENV_FILE" ] && grep -q '^XIDE_TEST_PASSWORD=' "$ENV_FILE"; then
    PASSWORD=$(sed -n 's/^XIDE_TEST_PASSWORD=//p' "$ENV_FILE" | tail -n 1)
  else
    PASSWORD=$(openssl rand -hex 24)
    AUTO_PASSWORD=1
  fi
  echo "Using a generated integration-test password for user '$USER_NAME'."
fi
"$ROOT/scripts/run-server.sh" --user "$USER_NAME" --password "$PASSWORD"
if [ "$AUTO_PASSWORD" -eq 1 ]; then
  umask 077
  printf '\nXIDE_TEST_PASSWORD=%s\n' "$PASSWORD" >> "$ROOT/deploy/.env"
fi
echo "Test login: $USER_NAME"
echo "Test password: $PASSWORD"
java -jar "$ROOT/integration/target/x-ide-communication-probe.jar" --url wss://localhost:8443/ws --user "$USER_NAME" --password "$PASSWORD" --trust-cert "$ROOT/deploy/certs/server.crt"

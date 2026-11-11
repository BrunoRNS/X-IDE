#!/usr/bin/env sh
set -eu
ROOT=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
DEPLOY="$ROOT/deploy"
USER_NAME=default
PASSWORD=
while [ "$#" -gt 0 ]; do
  case "$1" in
    --user) [ "$#" -ge 2 ] || { echo '--user requires a value' >&2; exit 2; }; USER_NAME=$2; shift 2 ;;
    --password) [ "$#" -ge 2 ] || { echo '--password requires a value' >&2; exit 2; }; PASSWORD=$2; shift 2 ;;
    -h|--help) echo 'Usage: run-server.sh [--user USER] [--password PASSWORD]'; exit 0 ;;
    *) echo "Unknown run-server option: $1" >&2; exit 2 ;;
  esac
done
case "$USER_NAME" in *[!A-Za-z0-9_.-]*|'') echo 'Username: use 3-64 letters, digits, dot, underscore or hyphen.' >&2; exit 2;; esac
[ "${#USER_NAME}" -ge 3 ] && [ "${#USER_NAME}" -le 64 ] || { echo 'Username must be 3-64 characters.' >&2; exit 2; }
if [ -z "$PASSWORD" ]; then
  printf 'Create password for default X-IDE user (%s): ' "$USER_NAME"
  stty -echo 2>/dev/null || true
  IFS= read -r PASSWORD
  stty echo 2>/dev/null || true
  printf '\nConfirm password: '
  stty -echo 2>/dev/null || true
  IFS= read -r PASSWORD_CONFIRM
  stty echo 2>/dev/null || true
  printf '\n'
  [ "$PASSWORD" = "$PASSWORD_CONFIRM" ] || { echo 'Passwords do not match.' >&2; exit 2; }
fi
[ "${#PASSWORD}" -ge 12 ] || { echo 'Password must contain at least 12 characters.' >&2; exit 2; }
if [ ! -f "$DEPLOY/.env" ]; then
  command -v openssl >/dev/null 2>&1 || { echo 'openssl is required to create database secrets.' >&2; exit 1; }
  DB_PASSWORD=$(openssl rand -hex 32)
  DB_ROOT_PASSWORD=$(openssl rand -hex 32)
  umask 077
  cat > "$DEPLOY/.env" <<EOF
DB_NAME=xide
DB_USER=xide
DB_PASSWORD=$DB_PASSWORD
DB_ROOT_PASSWORD=$DB_ROOT_PASSWORD
EOF
  chmod 600 "$DEPLOY/.env"
fi
if [ ! -s "$DEPLOY/certs/server.crt" ] || [ ! -s "$DEPLOY/certs/server.key" ]; then
  command -v openssl >/dev/null 2>&1 || { echo 'openssl is required to generate the local TLS certificate.' >&2; exit 1; }
  mkdir -p "$DEPLOY/certs"
  openssl req -x509 -nodes -newkey rsa:3072 -sha256 -days 365 \
    -keyout "$DEPLOY/certs/server.key" -out "$DEPLOY/certs/server.crt" \
    -subj '/CN=localhost' -addext 'subjectAltName=DNS:localhost,IP:127.0.0.1'
  chmod 600 "$DEPLOY/certs/server.key"
fi
export XIDE_DEFAULT_USER="$USER_NAME" XIDE_DEFAULT_PASSWORD="$PASSWORD"
cd "$ROOT"
ENGINE=${XIDE_CONTAINER_ENGINE:-auto}
if [ "$ENGINE" = auto ] || [ "$ENGINE" = docker ]; then
  if docker compose version >/dev/null 2>&1; then
    docker compose --env-file "$DEPLOY/.env" -f "$DEPLOY/compose.yaml" up -d --build
    ENGINE=done
  elif [ "$ENGINE" = docker ]; then echo 'Docker Compose v2 is unavailable.' >&2; exit 1; fi
fi
if [ "$ENGINE" = auto ] || [ "$ENGINE" = podman ]; then
  if podman compose version >/dev/null 2>&1; then
    podman compose --env-file "$DEPLOY/.env" -f "$DEPLOY/compose.yaml" up -d --build
    ENGINE=done
  elif command -v podman-compose >/dev/null 2>&1; then
    podman-compose --env-file "$DEPLOY/.env" -f "$DEPLOY/compose.yaml" up -d --build
    ENGINE=done
  elif [ "$ENGINE" = podman ]; then echo 'Podman Compose is unavailable.' >&2; exit 1; fi
fi
[ "$ENGINE" = done ] || { echo 'Docker Compose v2 or Podman Compose is required.' >&2; exit 1; }

unset XIDE_DEFAULT_PASSWORD PASSWORD PASSWORD_CONFIRM 2>/dev/null || true
echo "X-IDE WSS endpoint: wss://localhost:8443/ws"
echo "Default login: $USER_NAME"
echo "Local test certificate: $DEPLOY/certs/server.crt (use --trust-cert on clients)"

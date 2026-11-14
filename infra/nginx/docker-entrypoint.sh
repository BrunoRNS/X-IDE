#!/bin/sh
# X-IDE container entrypoint: WebSocket server (server.jar) + Nginx edge.
set -eu

CERT_DIR=/etc/nginx/certs
CERT_FILE="$CERT_DIR/server.crt"
KEY_FILE="$CERT_DIR/server.key"
TLS_HOST="${TLS_HOST:-localhost}"

mkdir -p "$CERT_DIR"

if [ ! -e "$CERT_FILE" ] && [ ! -e "$KEY_FILE" ]; then
    case "$TLS_HOST" in
        *:*)
            echo "TLS_HOST must be an IPv4 address or DNS hostname" >&2
            exit 1
            ;;
        *[!A-Za-z0-9.-]*|'')
            echo "TLS_HOST must be an IPv4 address or DNS hostname" >&2
            exit 1
            ;;
        *[!0-9.]*)
            SAN="DNS:$TLS_HOST"
            ;;
        *)
            SAN="IP:$TLS_HOST"
            ;;
    esac

    openssl req -x509 -newkey rsa:3072 -sha256 -nodes -days 3650 \
        -keyout "$KEY_FILE.tmp" \
        -out "$CERT_FILE.tmp" \
        -subj "/CN=$TLS_HOST" \
        -addext "subjectAltName=$SAN" \
        -addext "basicConstraints=critical,CA:FALSE" \
        -addext "keyUsage=critical,digitalSignature,keyEncipherment" \
        -addext "extendedKeyUsage=serverAuth"
    chmod 600 "$KEY_FILE.tmp"
    chmod 644 "$CERT_FILE.tmp"
    mv "$KEY_FILE.tmp" "$KEY_FILE"
    mv "$CERT_FILE.tmp" "$CERT_FILE"
elif [ ! -s "$CERT_FILE" ] || [ ! -s "$KEY_FILE" ]; then
    echo "TLS certificate volume is incomplete; refusing to replace existing key material" >&2
    exit 1
fi

case "$TLS_HOST" in
    *[!0-9.]*)
        if ! openssl x509 -in "$CERT_FILE" -noout -checkhost "$TLS_HOST" >/dev/null; then
            echo "TLS certificate does not cover TLS_HOST=$TLS_HOST; verify TLS_HOST or replace the persistent certificate." >&2
            exit 1
        fi
        ;;
    *)
        if ! openssl x509 -in "$CERT_FILE" -noout -checkip "$TLS_HOST" >/dev/null; then
            echo "TLS certificate does not cover TLS_HOST=$TLS_HOST; verify TLS_HOST or replace the persistent certificate." >&2
            exit 1
        fi
        ;;
esac

printf 'WSS certificate SHA-256 fingerprint: '
openssl x509 -in "$CERT_FILE" -noout -fingerprint -sha256 | sed 's/.*=//'

java -jar /opt/xide/server.jar &
exec nginx -g 'daemon off;'

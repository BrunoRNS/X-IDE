#!/bin/sh
# X-IDE container entrypoint: WebSocket server (server.jar) + Nginx edge.
set -eu

CERT_DIR=/etc/nginx/certs
CERT_FILE="$CERT_DIR/server.crt"
KEY_FILE="$CERT_DIR/server.key"
TLS_HOST="${TLS_HOST:-localhost}"
# TLS_HOST_AUTO=1 (set by make run-server when TLS_HOST was auto-detected) allows
# regenerating the persistent certificate when it no longer covers TLS_HOST.
TLS_HOST_AUTO="${TLS_HOST_AUTO:-0}"

mkdir -p "$CERT_DIR"

covers_host() {
    case "$1" in
        *[!0-9.]*)
            openssl x509 -in "$CERT_FILE" -noout -checkhost "$1" >/dev/null
            ;;
        *)
            openssl x509 -in "$CERT_FILE" -noout -checkip "$1" >/dev/null
            ;;
    esac
}

generate_certificate() {
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
            HOST_SAN="DNS:$TLS_HOST"
            ;;
        *)
            HOST_SAN="IP:$TLS_HOST"
            ;;
    esac

    SAN="DNS:localhost,IP:127.0.0.1"
    case "$HOST_SAN" in
        DNS:localhost|IP:127.0.0.1) ;;
        *) SAN="$SAN,$HOST_SAN" ;;
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
}

if [ -e "$CERT_FILE" ] || [ -e "$KEY_FILE" ]; then
    if [ ! -s "$CERT_FILE" ] || [ ! -s "$KEY_FILE" ]; then
        echo "TLS certificate volume is incomplete; refusing to replace existing key material" >&2
        exit 1
    fi
    if ! covers_host "$TLS_HOST"; then
        if [ "$TLS_HOST_AUTO" = "1" ]; then
            echo "TLS_HOST=$TLS_HOST was auto-detected and is not covered by the existing certificate; regenerating it."
            echo "Student clients that pinned the old fingerprint must re-pin after verifying the new fingerprint."
            rm -f "$CERT_FILE" "$KEY_FILE"
            generate_certificate
        else
            echo "TLS certificate does not cover TLS_HOST=$TLS_HOST; verify TLS_HOST or replace the persistent tls_certs volume." >&2
            exit 1
        fi
    fi
else
    generate_certificate
fi

if ! openssl x509 -in "$CERT_FILE" -noout -checkhost localhost >/dev/null; then
    echo "TLS certificate must cover localhost; replace the persistent tls_certs volume." >&2
    exit 1
fi

covers_host "$TLS_HOST" || {
    echo "TLS certificate does not cover TLS_HOST=$TLS_HOST; verify TLS_HOST or replace the persistent tls_certs volume." >&2
    exit 1
}

printf 'WSS certificate covers localhost, 127.0.0.1, and TLS_HOST=%s\n' "$TLS_HOST"
printf 'WSS certificate SHA-256 fingerprint: '
openssl x509 -in "$CERT_FILE" -noout -fingerprint -sha256 | sed 's/.*=//'

java -jar /opt/xide/server.jar &
exec nginx -g 'daemon off;'

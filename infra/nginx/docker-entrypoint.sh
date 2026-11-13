#!/bin/sh
# X-IDE container entrypoint: WebSocket server (server.jar) + Nginx edge.
set -eu

CERT_DIR=/etc/nginx/certs
CERT_FILE="$CERT_DIR/server.crt"
KEY_FILE="$CERT_DIR/server.key"

# Generate a self-signed development certificate (for the TLS 1.3 edge) when
# the certificates volume is empty. The certificates are written to the
# git-ignored infra/nginx/certs/ bind mount; drop a trusted certificate there
# with the same file names and recreate the container for real deployments.
if [ ! -s "$CERT_FILE" ] || [ ! -s "$KEY_FILE" ]; then
    echo "No TLS certificate found; generating self-signed development certificate..."
    mkdir -p "$CERT_DIR"
    openssl req -x509 -nodes -newkey rsa:2048 -sha256 -days 365 \
        -keyout "$KEY_FILE" -out "$CERT_FILE" \
        -subj "/CN=localhost" \
        -addext "subjectAltName=DNS:localhost,DNS:server,IP:127.0.0.1,IP:::1" \
        -addext "keyUsage=digitalSignature,keyEncipherment" \
        -addext "extendedKeyUsage=serverAuth"
    chmod 600 "$KEY_FILE"
    echo "Generated $CERT_FILE (CN=localhost, valid for 365 days)."
fi

java -jar /opt/xide/server.jar &
exec nginx -g 'daemon off;'

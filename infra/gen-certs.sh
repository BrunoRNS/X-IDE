#!/usr/bin/env bash
# Generates the self-signed development certificate for the X-IDE TLS 1.3 edge
# on the HOST, so it works on Linux, macOS and Windows (Git Bash / WSL):
#
#     bash infra/gen-certs.sh          # or: make gen-certs
#
# The certificate is written to infra/nginx/certs/ and the Docker Compose stack
# mounts that directory into the container (./nginx/certs:/etc/nginx/certs), so
# the container always receives the certificate the host generated.
#
# Why through Docker? The edge image (nginx:1.27-alpine) does not ship the
# openssl binary; running the same image once with `apk add openssl` gives an
# identical toolchain on every OS without installing anything on the host.
# The in-container fallback in infra/nginx/docker-entrypoint.sh remains as a
# safety net (it works too once the image is rebuilt with openssl).
set -euo pipefail

CERT_DIR="infra/nginx/certs"
CERT_FILE="$CERT_DIR/server.crt"
KEY_FILE="$CERT_DIR/server.key"
NGINX_IMAGE="${NGINX_IMAGE:-nginx:1.27-alpine}"

# Idempotent: skip when a certificate (and its key) already exist.
if [ -s "$CERT_FILE" ] && [ -s "$KEY_FILE" ]; then
    echo "TLS certificate already exists at $CERT_FILE; skipping."
    echo "Delete $CERT_FILE and $KEY_FILE first to regenerate."
    exit 0
fi

if ! command -v docker >/dev/null 2>&1; then
    echo "docker is required to generate the certificate (run it inside Docker so"
    echo "every OS - Linux, macOS and Windows - uses the same openssl toolchain)."
    exit 1
fi

if ! docker info >/dev/null 2>&1; then
    echo "docker daemon is not reachable for this user."
    echo "  - Is Docker Desktop (Windows/macOS) or the Docker Engine (Linux) running?"
    echo "  - Linux permission denied? Run: sudo usermod -aG docker \"\$USER\""
    echo "    and log out and back in."
    exit 1
fi

mkdir -p "$CERT_DIR"

# Absolute host path of the certificate directory, in the form Docker accepts:
# on Windows (Git Bash/MSYS) `pwd -W` yields the Windows form (C:/...).
uname_s="$(uname -s)"
case "$uname_s" in
    MINGW*|MSYS*|CYGWIN*)
        # Stop MSYS from rewriting container paths like /certs into C:/... .
        export MSYS_NO_PATHCONV=1
        cert_dir_host="$(cd "$CERT_DIR" && { pwd -W 2>/dev/null || pwd; } | tail -n 1)"
        ;;
    *)
        cert_dir_host="$(cd "$CERT_DIR" && pwd)"
        ;;
esac

# Same arguments as the in-container fallback (docker-entrypoint.sh), so both
# paths produce interchangeable certificates.
openssl_cmd="openssl req -x509 -nodes -newkey rsa:2048 -sha256 -days 365 -keyout /certs/server.key -out /certs/server.crt -subj '/CN=localhost' -addext 'subjectAltName=DNS:localhost,DNS:server,IP:127.0.0.1,IP:::1' -addext 'keyUsage=digitalSignature,keyEncipherment' -addext 'extendedKeyUsage=serverAuth' && chmod 600 /certs/server.key"

# apk add needs root, so the container runs as root; on Linux the generated
# files are then handed back to the invoking user (Docker Desktop maps the
# ownership to the host user automatically).
case "$uname_s" in
    Linux*)
        openssl_cmd="$openssl_cmd && chown $(id -u):$(id -g) /certs/server.key /certs/server.crt"
        ;;
esac

echo "Generating self-signed development certificate with $NGINX_IMAGE (first run may pull the image)..."
docker run --rm -v "$cert_dir_host:/certs" "$NGINX_IMAGE" sh -c "apk add --no-cache openssl >/dev/null && $openssl_cmd"

echo "Generated $CERT_FILE (CN=localhost, valid for 365 days)."
echo "The infra stack mounts infra/nginx/certs/ into the container at /etc/nginx/certs."

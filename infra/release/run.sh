#!/bin/sh
# Load the image archives that match this Docker host and start the stack.
set -eu

cd "$(dirname "$0")"

if ! command -v docker >/dev/null 2>&1; then
    echo "Docker is required: https://docs.docker.com/get-docker/" >&2
    exit 1
fi

arch="$(docker info --format '{{.Architecture}}' 2>/dev/null || true)"
case "$arch" in
    x86_64|amd64) platform=linux-amd64 ;;
    aarch64|arm64) platform=linux-arm64 ;;
    *)
        echo "Unsupported Docker architecture: ${arch:-unknown}" >&2
        echo "Supported platforms: linux-amd64, linux-arm64" >&2
        exit 1
        ;;
esac

found=0
for archive in image-*-"$platform".tar; do
    [ -e "$archive" ] || continue
    echo "Loading $archive"
    docker load --input "$archive"
    found=1
done
if [ "$found" -eq 0 ]; then
    echo "No image archive for $platform was found in $(pwd)" >&2
    exit 1
fi

if [ ! -f .env ]; then
    cp .env.example .env
    echo "Created .env from .env.example; edit the database passwords before first use."
fi

exec docker compose --env-file .env up

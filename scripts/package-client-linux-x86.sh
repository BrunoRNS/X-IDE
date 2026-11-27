#!/usr/bin/env bash
# Build the 32-bit Linux client bundle. Adoptium does not publish a Linux x86
# JDK, so jpackage runs inside a Debian i386 container whose openjdk-17-jdk-headless
# package ships a working 32-bit jpackage. The shaded client JAR is architecture
# independent and is built by the caller with the host JDK.
#
# Usage: package-client-linux-x86.sh [version] [output-dir] [client-jar]

set -euo pipefail

ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
VERSION="${1:-1.0.0}"
OUT="${2:-dist}"
JAR="${3:-client/target/client-1.0-SNAPSHOT-all.jar}"

if [ ! -f "$ROOT/$JAR" ]; then
    echo "missing $JAR; run './mvnw -pl client -am -DskipTests package' first" >&2
    exit 1
fi

mkdir -p "$ROOT/$OUT"

docker run --rm --platform linux/386 \
    -v "$ROOT:/workspace" \
    -w /workspace \
    -e VERSION="$VERSION" \
    -e OUT_DIR="$OUT" \
    -e JAR="$JAR" \
    -e HOST_UID="$(id -u)" \
    -e HOST_GID="$(id -g)" \
    debian:bookworm-slim \
    bash -euc '
        export DEBIAN_FRONTEND=noninteractive
        apt-get update -qq
        apt-get install -y -qq --no-install-recommends \
            ca-certificates \
            openjdk-17-jdk-headless \
            binutils \
            python3 >/dev/null

        test "$(getconf LONG_BIT)" = "32"
        java -XshowSettings:properties -version 2>&1 \
            | grep -q "sun.arch.data.model = 32"

        python3 scripts/package-client.py \
            --target linux-x86 \
            --version "$VERSION" \
            --jar "$JAR" \
            --output-dir "$OUT_DIR"

        chown -R "$HOST_UID:$HOST_GID" "$OUT_DIR"
    '

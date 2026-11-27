#!/usr/bin/env bash
# Assemble the per-target X-IDE release bundles from the already-built shaded
# JARs. Java bytecode is architecture-independent, so every bundle ships the
# same JARs and only the launcher scripts and JRE guidance differ.
#
# Bundles produced (in <output-dir>):
#   x-ide-<version>-macos.tar.gz        universal (Intel + Apple Silicon)
#   x-ide-<version>-windows-x64.zip
#   x-ide-<version>-windows-x86.zip
#   x-ide-<version>-linux-x64.tar.gz
#   x-ide-<version>-linux-x86.tar.gz
#
# Usage: package-release.sh <version> <output-dir>

set -euo pipefail

ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
VERSION="${1:-$(python3 "$ROOT/scripts/version.py" --print)}"
OUT="${2:?usage: package-release.sh <version> <output-dir>}"
CLIENT_JAR="$ROOT/client/target/client-all.jar"
SERVER_JAR="$ROOT/server/target/server.jar"

for jar in "$CLIENT_JAR" "$SERVER_JAR"; do
    if [ ! -f "$jar" ]; then
        echo "missing $jar (run './mvnw -DskipTests package' first)" >&2
        exit 1
    fi
done

mkdir -p "$OUT"

emit_readme() {
    local target="$1" note="$2" stage="$3"
    cat > "$stage/README.txt" <<EOF
X-IDE $VERSION - $target bundle

Contents:
  client-all.jar   standalone WebSocket client (shaded, self-contained)
  server.jar       WebSocket + HTTP server (shaded, self-contained)
  run-server.sh / run-server.cmd   start the server (MySQL DB_* env vars required)
  run-client.sh / run-client.cmd   send a message: [endpoint] [route] [--message payload]

Java: JRE 17+ on PATH.
$note

Source: https://github.com/${GITHUB_REPOSITORY:-org/xide}
EOF
}

# make_zip <archive-name> <stage-name>: create <OUT>/<archive-name> from
# <OUT>/<stage-name>. Uses zip when available and falls back to the Python
# zipfile module (which is present everywhere the script runs).
make_zip() {
    if command -v zip >/dev/null 2>&1; then
        (cd "$OUT" && zip -qr "$1" "$2")
    else
        python3 - "$OUT" "$1" "$2" <<'PYEOF'
import os, sys, zipfile

out, archive, stage = sys.argv[1], sys.argv[2], sys.argv[3]
with zipfile.ZipFile(os.path.join(out, archive), "w", zipfile.ZIP_DEFLATED) as zf:
    for root, dirs, files in os.walk(os.path.join(out, stage)):
        for name in dirs + files:
            path = os.path.join(root, name)
            zf.write(path, os.path.relpath(path, out))
PYEOF
    fi
}

assemble() {
    local target="$1" note="$2" ext="$3"
    local stage="x-ide-$target"
    local archive="x-ide-$VERSION-$target.$ext"
    rm -rf "$OUT/$stage"
    mkdir -p "$OUT/$stage"
    cp "$CLIENT_JAR" "$SERVER_JAR" "$OUT/$stage/"
    if [ "$ext" = zip ]; then
        cp "$ROOT/scripts/run-server.cmd" "$ROOT/scripts/run-client.cmd" "$OUT/$stage/"
    else
        cp "$ROOT/scripts/run-server.sh" "$ROOT/scripts/run-client.sh" "$OUT/$stage/"
        chmod +x "$OUT/$stage/run-server.sh" "$OUT/$stage/run-client.sh"
    fi
    emit_readme "$target" "$note" "$OUT/$stage"
    if [ "$ext" = zip ]; then
        make_zip "$archive" "$stage"
    else
        tar -czf "$OUT/$archive" -C "$OUT" "$stage"
    fi
    rm -rf "$OUT/$stage"
    echo "bundle: $OUT/$archive"
}

assemble macos \
    'JRE 17+ for macOS (Temurin or Zulu). If macOS blocks the launchers after
extracting the archive, run: xattr -dr com.apple.quarantine x-ide-macos' \
    tar.gz

assemble windows-x64 \
    'JRE 17+ for 64-bit Windows (Temurin or Zulu, win_x64).' \
    zip

assemble windows-x86 \
    'JRE 17+ for 32-bit Windows, e.g. Eclipse Temurin 17 (win_x86) from
https://adoptium.net/. Otherwise use the windows-x64 bundle on a 64-bit
Windows.' \
    zip

assemble linux-x64 \
    'JRE 17+ for 64-bit Linux, e.g. Debian/Ubuntu: apt install
openjdk-17-jre-headless; Fedora: dnf install java-17-openjdk.' \
    tar.gz

assemble linux-x86 \
    '32-bit x86 Linux: Debian still ships openjdk-17-jre-headless for i386
(apt install openjdk-17-jre-headless); other distributions may require a
manual 32-bit JRE 17 install.' \
    tar.gz

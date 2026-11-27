#!/usr/bin/env python3
"""Check or normalize LF line endings across the X-IDE repository.

Binary files (images, archives, font/CMap data, class files, ...) are left
untouched. Build output, release bundles and Git internals are skipped.

Usage:
  python3 scripts/line_endings.py --check
  python3 scripts/line_endings.py --apply
"""

from __future__ import annotations

import argparse
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent

SKIP_DIRS = {
    ".git",
    ".idea",
    ".venv",
    "build",
    "dist",
    "node_modules",
    "out",
    "target",
    "venv",
}

BINARY_SUFFIXES = {
    ".bcmap",
    ".class",
    ".dll",
    ".dylib",
    ".exe",
    ".gif",
    ".gz",
    ".ico",
    ".jar",
    ".jpeg",
    ".jpg",
    ".mp3",
    ".mp4",
    ".pdf",
    ".png",
    ".so",
    ".tar",
    ".ttf",
    ".woff",
    ".woff2",
    ".zip",
}

# TAB, LF, FF and CR are the only control bytes allowed in a text file. Any
# other control byte marks the file as binary, even without a NUL byte.
TEXT_CONTROL_BYTES = {9, 10, 12, 13}
SAMPLE_SIZE = 8192


def is_binary(path: Path, data: bytes) -> bool:
    if path.suffix.lower() in BINARY_SUFFIXES:
        return True
    sample = data[:SAMPLE_SIZE]
    return b"\x00" in sample or any(
        byte < 32 and byte not in TEXT_CONTROL_BYTES for byte in sample
    )


def iter_text_files():
    for path in sorted(ROOT.rglob("*")):
        if not path.is_file() or path.is_symlink():
            continue
        relative = path.relative_to(ROOT)
        if any(part in SKIP_DIRS for part in relative.parts[:-1]):
            continue
        data = path.read_bytes()
        if not is_binary(path, data):
            yield relative, data


def check() -> None:
    offenders = [str(path) for path, data in iter_text_files() if b"\r" in data]
    if offenders:
        for path in offenders:
            print(f"non-LF line endings: {path}")
        raise SystemExit(
            f"{len(offenders)} text file(s) contain CR; run 'make format' to normalize them"
        )
    print("line endings: LF in all text files")


def apply() -> None:
    changed = 0
    for path, data in iter_text_files():
        if b"\r" not in data:
            continue
        normalized = data.replace(b"\r\n", b"\n").replace(b"\r", b"\n")
        path.write_bytes(normalized)
        print(f"normalized to LF: {path}")
        changed += 1
    print(f"line endings normalized in {changed} file(s)")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("--check", action="store_true", help="fail on CR in text files")
    group.add_argument("--apply", action="store_true", help="rewrite text files with LF")
    args = parser.parse_args()
    if args.check:
        check()
    else:
        apply()


if __name__ == "__main__":
    main()

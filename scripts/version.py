#!/usr/bin/env python3
"""Read, validate or update the X-IDE project version.

The ``VERSION`` file at the repository root is the single source of truth for
packaging. This script keeps the Maven coordinates and the version printed by
``java -jar client-all.jar --version`` in sync with it.

Usage:
  python3 scripts/version.py --print
  python3 scripts/version.py --check
  python3 scripts/version.py 1.2.3        # also accepts v1.2.3
  python3 scripts/version.py --validate v1.2.3

Javadoc ``@since``/``@version`` tags are documentation history and are never
changed by this script.
"""

from __future__ import annotations

import argparse
import re
import subprocess
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
VERSION_FILE = ROOT / "VERSION"
MAIN_JAVA = ROOT / "client" / "src" / "main" / "java" / "org" / "xide" / "Main.java"
POMS = {
    "root": ROOT / "pom.xml",
    "common": ROOT / "common" / "pom.xml",
    "client": ROOT / "client" / "pom.xml",
    "server": ROOT / "server" / "pom.xml",
}

VERSION_RE = re.compile(r"^[vV]?(\d{1,5})(?:\.(\d{1,5}))?(?:\.(\d{1,5}))?$")
PARENT_VERSION_RE = re.compile(
    r"(<artifactId>xide</artifactId>\s*<version>)([^<]+)(</version>)"
)
MODULE_VERSION_RE = {
    name: re.compile(
        rf"(<artifactId>{name}</artifactId>\s*<version>)(?!\$\{{)([^<]+)(</version>)"
    )
    for name in ("common", "client", "server")
}
MAIN_VERSION_RE = re.compile(
    r'(private\s+static\s+final\s+String\s+VERSION\s*=\s*")([^"]*)(")'
)


def normalize(value: str) -> str:
    """Return a plain ``X.Y.Z`` version, padding missing components with zero."""
    match = VERSION_RE.match(value.strip())
    if match is None:
        raise SystemExit(f"invalid version {value!r}; expected vX.Y.Z or X.Y.Z")
    return ".".join(str(int(part or 0)) for part in match.groups())


def read_version() -> str:
    if not VERSION_FILE.is_file():
        raise SystemExit(f"missing {VERSION_FILE}; cannot determine the project version")
    return normalize(VERSION_FILE.read_text(encoding="utf-8"))


def latest_local_tag() -> str | None:
    """Return the highest local vX.Y.Z/X.Y.Z tag, or None when there is no tag."""
    try:
        result = subprocess.run(
            ["git", "tag", "--list", "--sort=-v:refname"],
            cwd=ROOT,
            capture_output=True,
            text=True,
            check=True,
        )
    except (OSError, subprocess.CalledProcessError):
        return None
    for line in result.stdout.splitlines():
        candidate = line.strip()
        if VERSION_RE.match(candidate):
            return normalize(candidate)
    return None


def replace_once(text: str, pattern: re.Pattern[str], version: str, label: str) -> str:
    """Replace the single version captured by pattern, failing on ambiguity."""
    updated, count = pattern.subn(
        lambda match: match.group(1) + version + match.group(3), text
    )
    if count != 1:
        raise SystemExit(f"{label}: expected exactly one version occurrence, found {count}")
    return updated


def replace_main_version(text: str, version: str) -> str:
    updated, count = MAIN_VERSION_RE.subn(
        lambda match: match.group(1) + version + match.group(3), text
    )
    if count != 1:
        raise SystemExit(
            f"{MAIN_JAVA}: expected exactly one VERSION constant, found {count}"
        )
    return updated


def read_source(path: Path) -> tuple[str, bool]:
    """Read text and remember whether the file used CRLF line endings."""
    data = path.read_bytes()
    crlf = b"\r\n" in data
    text = data.decode("utf-8")
    return (text.replace("\r\n", "\n") if crlf else text), crlf


def write_source(path: Path, text: str, crlf: bool) -> None:
    if crlf:
        text = text.replace("\n", "\r\n")
    path.write_bytes(text.encode("utf-8"))


def write_if_changed(path: Path, updated: str, crlf: bool) -> bool:
    current, _ = read_source(path)
    if updated == current:
        return False
    write_source(path, updated, crlf)
    return True


def apply_version(version: str) -> None:
    VERSION_FILE.write_text(version + "\n", encoding="utf-8")
    changed: list[str] = []

    for name, path in POMS.items():
        text, crlf = read_source(path)
        updated = replace_once(
            text, PARENT_VERSION_RE, version, f"{path} (parent version)"
        )
        if name in MODULE_VERSION_RE:
            updated = replace_once(
                updated,
                MODULE_VERSION_RE[name],
                version,
                f"{path} (module version)",
            )
        if write_if_changed(path, updated, crlf):
            changed.append(str(path.relative_to(ROOT)))

    main_text, main_crlf = read_source(MAIN_JAVA)
    updated_main = replace_main_version(main_text, version)
    if write_if_changed(MAIN_JAVA, updated_main, main_crlf):
        changed.append(str(MAIN_JAVA.relative_to(ROOT)))

    print(f"Project version: {version}")
    print(f"{VERSION_FILE.relative_to(ROOT)}: {version}")
    for item in changed:
        print(f"updated {item}")
    if not changed:
        print("POMs and client version were already up to date")


def check_version() -> None:
    version = read_version()
    problems: list[str] = []

    for name, path in POMS.items():
        text, _ = read_source(path)
        parent = PARENT_VERSION_RE.search(text)
        if parent is None or parent.group(2) != version:
            problems.append(f"{path.relative_to(ROOT)}: parent version is not {version}")
        if name in MODULE_VERSION_RE:
            module = MODULE_VERSION_RE[name].search(text)
            if module is None or module.group(2) != version:
                problems.append(f"{path.relative_to(ROOT)}: module version is not {version}")

    main_text, _ = read_source(MAIN_JAVA)
    main = MAIN_VERSION_RE.search(main_text)
    if main is None or main.group(2) != version:
        problems.append(f"{MAIN_JAVA.relative_to(ROOT)}: displayed version is not {version}")

    if problems:
        for problem in problems:
            print(f"version check failed: {problem}")
        raise SystemExit(1)
    print(f"version check passed: {version}")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--print", action="store_true", help="print the current version")
    parser.add_argument("--check", action="store_true", help="verify all version locations")
    parser.add_argument("--validate", metavar="VERSION", help="validate a version string only")
    parser.add_argument("version", nargs="?", help="new version (vX.Y.Z or X.Y.Z)")
    args = parser.parse_args()

    if args.validate is not None:
        print(normalize(args.validate))
        return
    if args.print:
        print(read_version())
        return
    if args.check:
        check_version()
        return

    requested = args.version
    if not requested:
        requested = latest_local_tag()
        if requested is None:
            raise SystemExit(
                "no version supplied and no local vX.Y.Z tag found; "
                "use 'make version V=v1.2.3' or 'make version v1.2.3'"
            )
        print(f"Using version from local git tag: {requested}")

    apply_version(normalize(requested))


if __name__ == "__main__":
    main()

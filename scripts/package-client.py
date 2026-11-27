#!/usr/bin/env python3
"""Build the per-platform X-IDE desktop client bundles.

jpackage cannot cross-compile, so this script must run on the target OS with a
JDK whose architecture matches the requested target. Linux and Windows produce
an app-image archive (tar.gz / zip) containing the launcher, the shaded JAR and
the bundled runtime. macOS produces a DMG.

Targets:
  linux-x86, linux-x86_64, linux-arm64   -> .tar.gz
  macos-x64, macos-arm64                 -> .dmg
  windows-x86, windows-x64, windows-arm64 -> .zip (exe + deps)
"""

from __future__ import annotations

import argparse
import hashlib
import re
import shutil
import subprocess
import sys
import tarfile
import tempfile
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent


def project_version() -> str:
    """Return the repository version written by `make version`."""
    version_file = ROOT / "VERSION"
    if version_file.is_file():
        value = version_file.read_text(encoding="utf-8").strip()
        if value:
            return value
    return "1.0.0"


TARGETS = {
    "linux-x86": {"os": "linux", "arch": "x86", "bits": 32, "format": "tar.gz"},
    "linux-x86_64": {"os": "linux", "arch": "x86_64", "bits": 64, "format": "tar.gz"},
    "linux-arm64": {"os": "linux", "arch": "aarch64", "bits": 64, "format": "tar.gz"},
    "macos-x64": {"os": "macos", "arch": "x86_64", "bits": 64, "format": "dmg"},
    "macos-arm64": {"os": "macos", "arch": "aarch64", "bits": 64, "format": "dmg"},
    "windows-x86": {"os": "windows", "arch": "x86", "bits": 32, "format": "zip"},
    "windows-x64": {"os": "windows", "arch": "x86_64", "bits": 64, "format": "zip"},
    "windows-arm64": {"os": "windows", "arch": "aarch64", "bits": 64, "format": "zip"},
}


def host_os() -> str:
    if sys.platform.startswith("linux"):
        return "linux"
    if sys.platform == "darwin":
        return "macos"
    if sys.platform == "win32":
        return "windows"
    raise SystemExit(f"unsupported host operating system: {sys.platform}")


def run(cmd: list[object], cwd: Path | None = None) -> None:
    print("+ " + " ".join(str(part) for part in cmd), flush=True)
    subprocess.run([str(part) for part in cmd], cwd=cwd, check=True)


def java_properties() -> dict[str, str]:
    java = shutil.which("java")
    if java is None:
        raise SystemExit("java was not found on PATH; install a JDK 17+ first")
    result = subprocess.run(
        [java, "-XshowSettings:properties", "-version"],
        capture_output=True,
        text=True,
        check=False,
    )
    properties: dict[str, str] = {}
    for line in (result.stdout + result.stderr).splitlines():
        if "=" in line:
            key, value = line.split("=", 1)
            properties[key.strip()] = value.strip()
    return properties


def jdk_architecture() -> tuple[str, int]:
    properties = java_properties()
    arch = properties.get("os.arch", "").lower()
    try:
        bits = int(properties.get("sun.arch.data.model", "0"))
    except ValueError:
        bits = 0
    if arch in ("amd64", "x86_64"):
        return "x86_64", bits
    if arch in ("x86", "i386", "i486", "i586", "i686"):
        return "x86", bits
    if arch in ("aarch64", "arm64"):
        return "aarch64", bits
    return arch, bits


def detect_target() -> str:
    os_name = host_os()
    arch, bits = jdk_architecture()
    for name, spec in TARGETS.items():
        if spec["os"] == os_name and spec["arch"] == arch and spec["bits"] == bits:
            return name
    raise SystemExit(f"no client target for {os_name}/{arch}/{bits}-bit JDK")


def validate_target(target: str) -> None:
    spec = TARGETS[target]
    os_name = host_os()
    if spec["os"] != os_name:
        raise SystemExit(f"{target} must be built on {spec['os']}, not on {os_name}")
    arch, bits = jdk_architecture()
    if (arch, bits) != (spec["arch"], spec["bits"]):
        raise SystemExit(
            f"{target} needs a {spec['bits']}-bit {spec['arch']} JDK, "
            f"but this JDK is {bits}-bit {arch}"
        )


def normalize_version(value: str) -> str:
    value = (value or "1.0.0").strip().lstrip("vV")
    match = re.match(r"^(\d+(?:\.\d+)*)", value)
    if match is None:
        return "1.0.0"
    parts = match.group(1).split(".")
    while len(parts) < 3:
        parts.append("0")
    return ".".join(parts[:3])


def safe_version(value: str) -> str:
    value = (value or "1.0.0").strip().lstrip("vV")
    return re.sub(r"[^0-9A-Za-z._+-]", "-", value) or "1.0.0"


def find_client_jar(explicit: str | None) -> Path:
    if explicit:
        jar = Path(explicit)
        if not jar.is_absolute():
            jar = ROOT / jar
        if not jar.is_file():
            raise SystemExit(f"client JAR not found: {jar}")
        return jar.resolve()
    # The shaded jar has a stable name (see the shade plugin's outputFile in
    # client/pom.xml); the glob only accepts older versioned jars left in target/.
    stable = ROOT / "client" / "target" / "client-all.jar"
    if stable.is_file():
        return stable.resolve()
    candidates = sorted((ROOT / "client/target").glob("client-*-all.jar"))
    if not candidates:
        raise SystemExit(
            "client shaded JAR not found; run "
            "'./mvnw -pl client -am -DskipTests package' first"
        )
    return candidates[-1].resolve()


def jpackage_arguments(jar: Path, version: str, dest: Path) -> list[object]:
    return [
        "jpackage",
        "--name",
        "XIDE-Client",
        "--input",
        str(jar.parent),
        "--main-jar",
        jar.name,
        "--main-class",
        "org.xide.Main",
        "--dest",
        str(dest),
        "--app-version",
        version,
        "--vendor",
        "X-IDE",
        "--description",
        "X-IDE desktop client",
        "--java-options",
        "-Dfile.encoding=UTF-8",
    ]


def build_app_image(jar: Path, version: str, dest: Path, work: Path) -> Path:
    input_dir = work / "input"
    input_dir.mkdir(parents=True)
    shutil.copy2(jar, input_dir / jar.name)
    dest.mkdir(parents=True, exist_ok=True)
    arguments = jpackage_arguments(input_dir / jar.name, version, dest)
    run([arguments[0], "--type", "app-image", *arguments[1:]])
    app_image = dest / "XIDE-Client"
    if not app_image.is_dir():
        raise SystemExit(f"jpackage did not create the expected app image: {app_image}")
    return app_image


def build_dmg(jar: Path, version: str, dest: Path, work: Path) -> Path:
    input_dir = work / "input"
    input_dir.mkdir(parents=True)
    shutil.copy2(jar, input_dir / jar.name)
    dest.mkdir(parents=True, exist_ok=True)
    arguments = jpackage_arguments(input_dir / jar.name, version, dest)
    arguments += [
        "--mac-package-identifier",
        "org.xide.client",
        "--mac-package-name",
        "X-IDE Client",
    ]
    run([arguments[0], "--type", "dmg", *arguments[1:]])
    images = sorted(dest.glob("*.dmg"), key=lambda path: path.stat().st_mtime)
    if not images:
        raise SystemExit(f"jpackage did not create a DMG in {dest}")
    return images[-1]


def write_readme(app_image: Path, target: str) -> None:
    launcher = "bin/XIDE-Client" if host_os() == "linux" else "XIDE-Client.exe"
    text = (
        f"X-IDE client - {target}\n"
        "\n"
        "This bundle contains the shaded client JAR, the platform runtime and\n"
        "the native launcher generated by jpackage.\n"
        "\n"
        f"Run: {launcher}\n"
        "\n"
        "The client needs a graphical desktop. The server endpoint can be entered\n"
        "in the connection window or passed with --endpoint.\n"
    )
    (app_image / "README.txt").write_text(text, encoding="utf-8")


def make_tar(app_image: Path, archive: Path) -> None:
    with tarfile.open(archive, "w:gz") as bundle:
        bundle.add(app_image, arcname=app_image.name)


def make_zip(app_image: Path, archive: Path) -> None:
    with zipfile.ZipFile(archive, "w", zipfile.ZIP_DEFLATED) as bundle:
        for path in sorted(app_image.rglob("*")):
            if path.is_file():
                bundle.write(path, path.relative_to(app_image.parent).as_posix())


def write_checksum(path: Path) -> None:
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(chunk)
    checksum = path.with_name(path.name + ".sha256")
    checksum.write_text(f"{digest.hexdigest()}  {path.name}\n", encoding="utf-8")
    print(f"sha256: {checksum}")


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--target",
        default="auto",
        choices=["auto", *TARGETS],
        help="target bundle name, or 'auto' to detect the host JDK",
    )
    parser.add_argument("--version", default=project_version(), help="release version")
    parser.add_argument("--jar", help="path to the shaded client JAR")
    parser.add_argument("--output-dir", default="dist", help="bundle output directory")
    args = parser.parse_args()

    target = detect_target() if args.target == "auto" else args.target
    validate_target(target)
    jar = find_client_jar(args.jar)
    version = normalize_version(args.version)
    release = safe_version(args.version)

    out_dir = Path(args.output_dir)
    if not out_dir.is_absolute():
        out_dir = ROOT / out_dir
    out_dir.mkdir(parents=True, exist_ok=True)

    spec = TARGETS[target]
    with tempfile.TemporaryDirectory(prefix="xide-client-") as temporary:
        work = Path(temporary)
        if spec["format"] == "dmg":
            dmg = build_dmg(jar, version, out_dir, work)
            archive = out_dir / f"xide-client-{release}-{target}.dmg"
            if dmg != archive:
                if archive.exists():
                    archive.unlink()
                dmg.replace(archive)
        else:
            app_image = build_app_image(jar, version, work / "app", work)
            write_readme(app_image, target)
            extension = "tar.gz" if spec["format"] == "tar.gz" else "zip"
            archive = out_dir / f"xide-client-{release}-{target}.{extension}"
            if archive.exists():
                archive.unlink()
            if extension == "tar.gz":
                make_tar(app_image, archive)
            else:
                make_zip(app_image, archive)
        write_checksum(archive)
        print(f"bundle: {archive}")


if __name__ == "__main__":
    main()

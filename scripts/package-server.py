#!/usr/bin/env python3
"""Build the Docker-based X-IDE server release bundles.

Four bundles are produced:

  xide-server-full-<version>.zip      Java server + Ollama + MySQL compose stack
  xide-server-site-<version>.zip      Java HTTP site (database is external)
  xide-server-ai-<version>.zip        Ollama service
  xide-server-database-<version>.zip  standalone MySQL 8.4 database

Every bundle contains one Docker image archive per supported platform, a Compose
file without ``build:`` sections, generic launchers (``run.sh``/``run.cmd``/
``run.ps1``), ``.env.example`` and a README. The launchers load only the archive
that matches the host Docker architecture and then run Compose, so the same ZIP
works on Linux, macOS and Windows Docker installations.

The site and full servers use the same image, built from the regular Java JAR by
``infra/Dockerfile`` (JRE 17 + Nginx). It is built once per platform and shared by
both bundles. Images are built with ``docker buildx build --output type=docker``
because the resulting archive can be loaded with ``docker load`` on any host.
"""

from __future__ import annotations

import argparse
import hashlib
import re
import shutil
import subprocess
import tempfile
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RELEASE = ROOT / "infra" / "release"

VARIANTS = ("full", "site", "ai", "database")


def project_version() -> str:
    """Return the repository version written by `make version`."""
    version_file = ROOT / "VERSION"
    if version_file.is_file():
        value = version_file.read_text(encoding="utf-8").strip()
        if value:
            return value
    return "v0.0.0"


# Canonical images. `full` and `site` intentionally share one server image so the
# server image only needs to be built once per platform.
IMAGES = {
    "server": {"tag": "xide-server:local", "dockerfile": "infra/Dockerfile"},
    "ai": {"tag": "xide-server-ai:local", "dockerfile": "infra/Dockerfile.ai"},
    "database": {
        "tag": "xide-database:local",
        "dockerfile": "infra/Dockerfile.database",
    },
}

# Images that must be present in each bundle.
BUNDLE_IMAGES = {
    "full": ["server", "ai", "database"],
    "site": ["server"],
    "ai": ["ai"],
    "database": ["database"],
}

BUNDLE_COMPOSE = {
    "full": "compose.full.yaml",
    "site": "compose.site.yaml",
    "ai": "compose.ai.yaml",
    "database": "compose.database.yaml",
}

BUNDLE_README = {
    "full": "README.full.md",
    "site": "README.site.md",
    "ai": "README.ai.md",
    "database": "README.database.md",
}


def run(cmd: list[object], cwd: Path | None = None) -> None:
    print("+ " + " ".join(str(part) for part in cmd), flush=True)
    subprocess.run([str(part) for part in cmd], cwd=cwd, check=True)


def safe_version(value: str) -> str:
    """Normalize to `vX.Y.Z` so local and tagged releases share one naming style."""
    value = (value or "v0.0.0").strip()
    if value[:1] in ("v", "V"):
        value = "v" + value[1:]
    elif value[:1].isdigit():
        value = "v" + value
    return re.sub(r"[^0-9A-Za-z._+-]", "-", value) or "v0.0.0"


def platform_name(platform: str) -> str:
    return platform.strip().replace("/", "-")


def check_docker() -> None:
    if shutil.which("docker") is None:
        raise SystemExit("docker was not found on PATH")


def check_server_jar() -> None:
    # server/pom.xml sets <finalName>server</finalName>, so the jar name does not
    # change when `make version` updates the POM version.
    jar = ROOT / "server" / "target" / "server.jar"
    if not jar.is_file():
        raise SystemExit(
            "server JAR not found; run "
            "'./mvnw -pl server -am -DskipTests package' first"
        )


def build_image(image: str, platform: str, dest: Path) -> None:
    dest.parent.mkdir(parents=True, exist_ok=True)
    spec = IMAGES[image]
    command = [
        "docker",
        "buildx",
        "build",
        "--platform",
        platform,
        "--tag",
        spec["tag"],
        "--file",
        spec["dockerfile"],
        "--output",
        f"type=docker,dest={dest}",
        ".",
    ]
    run(command, cwd=ROOT)


def make_zip(stage: Path, archive: Path) -> None:
    with zipfile.ZipFile(archive, "w") as bundle:
        for path in sorted(stage.rglob("*")):
            if not path.is_file():
                continue
            arcname = path.relative_to(stage).as_posix()
            info = zipfile.ZipInfo.from_file(path, arcname)
            # Image archives are already compressed; storing them avoids a slow
            # second compression pass and keeps extraction fast.
            info.compress_type = (
                zipfile.ZIP_STORED
                if path.suffix == ".tar"
                else zipfile.ZIP_DEFLATED
            )
            info.external_attr = (0o755 if path.name.endswith(".sh") else 0o644) << 16
            with path.open("rb") as source, bundle.open(info, "w") as target:
                shutil.copyfileobj(source, target)


def write_checksum(path: Path) -> None:
    digest = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(chunk)
    checksum = path.with_name(path.name + ".sha256")
    checksum.write_text(f"{digest.hexdigest()}  {path.name}\n", encoding="utf-8")
    print(f"sha256: {checksum}")


def stage_bundle(variant: str, version: str, images: list[Path], stage: Path) -> None:
    shutil.copy2(RELEASE / BUNDLE_COMPOSE[variant], stage / "compose.yaml")
    for name in ("run.sh", "run.cmd", "run.ps1", ".env.example"):
        shutil.copy2(RELEASE / name, stage / name)
    readme = (RELEASE / BUNDLE_README[variant]).read_text(encoding="utf-8")
    (stage / "README.md").write_text(
        readme.replace("__VERSION__", version), encoding="utf-8"
    )
    for image in images:
        shutil.copy2(image, stage / image.name)


def parse_platforms(value: str) -> list[str]:
    platforms = [part.strip() for part in value.split(",") if part.strip()]
    if not platforms:
        raise SystemExit("at least one platform is required")
    return platforms


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--variant",
        choices=[*VARIANTS, "all"],
        default="all",
        help="bundle to build (default: all)",
    )
    parser.add_argument("--version", default=project_version(), help="release version")
    parser.add_argument(
        "--platforms",
        default="linux/amd64",
        help="comma-separated Docker platforms (default: linux/amd64)",
    )
    parser.add_argument("--output-dir", default="dist", help="bundle output directory")
    args = parser.parse_args()

    check_docker()
    if any(variant in ("full", "site") for variant in variants):
        check_server_jar()

    variants = VARIANTS if args.variant == "all" else (args.variant,)
    platforms = parse_platforms(args.platforms)

    out_dir = Path(args.output_dir)
    if not out_dir.is_absolute():
        out_dir = ROOT / out_dir
    out_dir.mkdir(parents=True, exist_ok=True)
    version = safe_version(args.version)

    required_images = sorted(
        {image for variant in variants for image in BUNDLE_IMAGES[variant]}
    )

    with tempfile.TemporaryDirectory(prefix="xide-server-images-") as cache:
        cache_dir = Path(cache)
        archives: dict[tuple[str, str], Path] = {}
        for image in required_images:
            for platform in platforms:
                dest = cache_dir / f"image-{image}-{platform_name(platform)}.tar"
                build_image(image, platform, dest)
                archives[(image, platform)] = dest

        for variant in variants:
            with tempfile.TemporaryDirectory(prefix=f"xide-{variant}-") as staging:
                stage = Path(staging)
                images = [
                    archives[(image, platform)]
                    for image in BUNDLE_IMAGES[variant]
                    for platform in platforms
                ]
                stage_bundle(variant, version, images, stage)
                archive = out_dir / f"xide-server-{variant}-{version}.zip"
                if archive.exists():
                    archive.unlink()
                make_zip(stage, archive)
                write_checksum(archive)
                print(f"bundle: {archive}")


if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""Build the Docker-based X-IDE server release bundles.

Three bundles are produced:

  xide-server-full-<version>.zip   full-server + server-ai + MySQL compose stack
  xide-server-site-<version>.zip   HTTP site + MySQL compose stack
  xide-server-ai-<version>.zip     Ollama service

Every bundle contains one Docker image archive per supported platform, a Compose
file without ``build:`` sections, generic launchers (``run.sh``/``run.cmd``/
``run.ps1``), ``.env.example`` and a README. The launchers load only the archive
that matches the host Docker architecture and then run Compose, so the same ZIP
works on Linux, macOS and Windows Docker installations.

Images are built with ``docker buildx build --output type=docker`` because the
resulting archive can be loaded with ``docker load`` on any host.
"""

from __future__ import annotations

import argparse
import hashlib
import re
import shutil
import subprocess
import sys
import tempfile
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RELEASE = ROOT / "infra" / "release"

IMAGE_TAGS = {
    "full": "xide-full-server:local",
    "site": "xide-server-site:local",
    "ai": "xide-server-ai:local",
}

DOCKERFILES = {
    "full": "infra/Dockerfile",
    "site": "infra/Dockerfile",
    "ai": "infra/Dockerfile.ai",
}

# Images that must be present in each bundle.
BUNDLE_IMAGES = {
    "full": ["full", "ai"],
    "site": ["site"],
    "ai": ["ai"],
}

BUNDLE_COMPOSE = {
    "full": "compose.full.yaml",
    "site": "compose.site.yaml",
    "ai": "compose.ai.yaml",
}

BUNDLE_README = {
    "full": "README.full.md",
    "site": "README.site.md",
    "ai": "README.ai.md",
}


def run(cmd: list[object], cwd: Path | None = None) -> None:
    print("+ " + " ".join(str(part) for part in cmd), flush=True)
    subprocess.run([str(part) for part in cmd], cwd=cwd, check=True)


def safe_version(value: str) -> str:
    value = (value or "0.0.0").strip().lstrip("vV")
    return re.sub(r"[^0-9A-Za-z._+-]", "-", value) or "0.0.0"


def platform_name(platform: str) -> str:
    return platform.strip().replace("/", "-")


def check_docker() -> None:
    if shutil.which("docker") is None:
        raise SystemExit("docker was not found on PATH")


def check_server_jar() -> None:
    jar = ROOT / "server" / "target" / "server-1.0-SNAPSHOT.jar"
    if not jar.is_file():
        raise SystemExit(
            "server JAR not found; run "
            "'./mvnw -pl server -am -DskipTests package' first"
        )


def build_image(variant: str, platform: str, dest: Path) -> None:
    dest.parent.mkdir(parents=True, exist_ok=True)
    command = [
        "docker",
        "buildx",
        "build",
        "--platform",
        platform,
        "--tag",
        IMAGE_TAGS[variant],
        "--file",
        DOCKERFILES[variant],
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
            if path.name.endswith(".sh"):
                info.external_attr = 0o755 << 16
            else:
                info.external_attr = 0o644 << 16
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
        choices=["full", "site", "ai", "all"],
        default="all",
        help="bundle to build (default: all)",
    )
    parser.add_argument("--version", default="0.0.0", help="release version")
    parser.add_argument(
        "--platforms",
        default="linux/amd64",
        help="comma-separated Docker platforms (default: linux/amd64)",
    )
    parser.add_argument("--output-dir", default="dist", help="bundle output directory")
    args = parser.parse_args()

    check_docker()

    variants = ["full", "site", "ai"] if args.variant == "all" else [args.variant]
    platforms = parse_platforms(args.platforms)
    if any(variant in ("full", "site") for variant in variants):
        check_server_jar()

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

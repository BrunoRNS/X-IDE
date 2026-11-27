# Load the image archives that match this Docker host and start the stack.
$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw 'Docker is required: https://docs.docker.com/get-docker/'
}

$arch = (docker info --format '{{.Architecture}}').Trim()
switch ($arch) {
    { $_ -in @('x86_64', 'amd64') } { $platform = 'linux-amd64' }
    { $_ -in @('aarch64', 'arm64') } { $platform = 'linux-arm64' }
    default { throw "Unsupported Docker architecture: $arch. Supported platforms: linux-amd64, linux-arm64" }
}

$archives = Get-ChildItem -Path "image-*-$platform.tar" -ErrorAction SilentlyContinue
if (-not $archives) {
    throw "No image archive for $platform was found in $PWD"
}
foreach ($archive in $archives) {
    Write-Host "Loading $($archive.Name)"
    docker load --input $archive.FullName
}

if (-not (Test-Path -LiteralPath '.env')) {
    Copy-Item -LiteralPath '.env.example' -Destination '.env'
    Write-Host 'Created .env from .env.example; edit the database passwords before first use.'
}

docker compose --env-file .env up

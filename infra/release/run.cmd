@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

where docker >nul 2>nul
if errorlevel 1 (
    echo Docker is required: https://docs.docker.com/get-docker/ 1>&2
    exit /b 1
)

for /f "delims=" %%A in ('docker info --format "{{.Architecture}}" 2^>nul') do set "ARCH=%%A"
if /i "%ARCH%"=="x86_64" set "PLATFORM=linux-amd64"
if /i "%ARCH%"=="amd64" set "PLATFORM=linux-amd64"
if /i "%ARCH%"=="aarch64" set "PLATFORM=linux-arm64"
if /i "%ARCH%"=="arm64" set "PLATFORM=linux-arm64"
if not defined PLATFORM (
    echo Unsupported Docker architecture: %ARCH% 1>&2
    exit /b 1
)

set "FOUND="
for %%F in (image-*-%PLATFORM%.tar) do (
    if exist "%%F" (
        echo Loading %%F
        docker load --input "%%F"
        set "FOUND=1"
    )
)
if not defined FOUND (
    echo No image archive for %PLATFORM% was found in %CD% 1>&2
    exit /b 1
)

if not exist ".env" (
    copy /Y ".env.example" ".env" >nul
    echo Created .env from .env.example; edit the database passwords before first use.
)

docker compose --env-file .env up

$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$Deploy = Join-Path $Root 'deploy'
$UserName = 'default'
$Password = ''
for ($i = 0; $i -lt $args.Count; $i++) {
    switch ($args[$i]) {
        '--user' { if (++$i -ge $args.Count) { throw '--user requires a value' }; $UserName = $args[$i] }
        '--password' { if (++$i -ge $args.Count) { throw '--password requires a value' }; $Password = $args[$i] }
        default { throw "Unknown run-server option: $($args[$i])" }
    }
}
if ($UserName -notmatch '^[A-Za-z0-9_.-]{3,64}$') { throw 'Username must be 3-64 letters, digits, dot, underscore or hyphen.' }
if ([string]::IsNullOrEmpty($Password)) {
    $Secure = Read-Host "Create password for default X-IDE user ($UserName)" -AsSecureString
    $Confirm = Read-Host 'Confirm password' -AsSecureString
    $B1 = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($Secure)
    $B2 = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($Confirm)
    try { $Password = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($B1); $Again = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($B2) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($B1); [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($B2) }
    if ($Password -cne $Again) { throw 'Passwords do not match.' }
}
if ($Password.Length -lt 12) { throw 'Password must contain at least 12 characters.' }
$EnvFile = Join-Path $Deploy '.env'
if (!(Test-Path $EnvFile)) {
    $Bytes = New-Object byte[] 32
    $Rng = [Security.Cryptography.RandomNumberGenerator]::Create()
    $Rng.GetBytes($Bytes)
    $DbPassword = ([BitConverter]::ToString($Bytes)).Replace('-', '').ToLowerInvariant()
    $Rng.GetBytes($Bytes)
    $RootPassword = ([BitConverter]::ToString($Bytes)).Replace('-', '').ToLowerInvariant()
    $Rng.Dispose()
    @("DB_NAME=xide", "DB_USER=xide", "DB_PASSWORD=$DbPassword", "DB_ROOT_PASSWORD=$RootPassword") | Set-Content -Path $EnvFile -Encoding ascii
}
$CertDir = Join-Path $Deploy 'certs'
$Cert = Join-Path $CertDir 'server.crt'
$Key = Join-Path $CertDir 'server.key'
if (!(Test-Path $Cert) -or !(Test-Path $Key)) {
    if (!(Get-Command openssl -ErrorAction SilentlyContinue)) { throw 'OpenSSL is required to generate the local TLS certificate.' }
    New-Item -ItemType Directory -Force -Path $CertDir | Out-Null
    & openssl req -x509 -nodes -newkey rsa:3072 -sha256 -days 365 -keyout $Key -out $Cert -subj '/CN=localhost' -addext 'subjectAltName=DNS:localhost,IP:127.0.0.1'
    if ($LASTEXITCODE -ne 0) { throw 'OpenSSL certificate generation failed.' }
}
$env:XIDE_DEFAULT_USER = $UserName
$env:XIDE_DEFAULT_PASSWORD = $Password
Push-Location $Root
try {
    if ((Get-Command docker -ErrorAction SilentlyContinue) -and (& docker compose version 2>$null)) {
        & docker compose --env-file $EnvFile -f (Join-Path $Deploy 'compose.yaml') up -d --build
    } elseif ((Get-Command podman -ErrorAction SilentlyContinue) -and (& podman compose version 2>$null)) {
        & podman compose --env-file $EnvFile -f (Join-Path $Deploy 'compose.yaml') up -d --build
    } elseif (Get-Command podman-compose -ErrorAction SilentlyContinue) {
        & podman-compose --env-file $EnvFile -f (Join-Path $Deploy 'compose.yaml') up -d --build
    } else { throw 'Docker Compose v2 or Podman Compose is required.' }
    if ($LASTEXITCODE -ne 0) { throw 'Compose failed to start the X-IDE stack.' }
} finally {
    Remove-Item Env:XIDE_DEFAULT_PASSWORD -ErrorAction SilentlyContinue
    Remove-Item Env:XIDE_DEFAULT_USER -ErrorAction SilentlyContinue
    Pop-Location
}
Write-Host 'X-IDE WSS endpoint: wss://localhost:8443/ws'
Write-Host "Default login: $UserName"
Write-Host "Local test certificate: $Cert (use --trust-cert on clients)"

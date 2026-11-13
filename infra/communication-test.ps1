# Verifies the ping/pong handshake in both directions against a running stack,
# using the real client JAR. Windows (PowerShell) equivalent of
# infra/communication-test.sh. `make package-all` (or `.\mvnw.cmd package`)
# builds the JAR and `make run-server` starts the stack.
#
# Usage (from the repository root):
#   powershell -NoProfile -ExecutionPolicy Bypass -File infra\communication-test.ps1
#   Optionally: -Endpoint wss://localhost:8443/ws/socket -TrustCert infra\nginx\certs\server.crt
#   The CLIENT_ENDPOINT and CLIENT_TRUST_CERT environment variables are also
#   honored, mirroring the bash script.

param(
    [string]$Endpoint = "wss://localhost:8443/ws/socket",
    [string]$TrustCert = "infra/nginx/certs/server.crt"
)

$ErrorActionPreference = "Continue"

if ($env:CLIENT_ENDPOINT) { $Endpoint = $env:CLIENT_ENDPOINT }
if ($env:CLIENT_TRUST_CERT) { $TrustCert = $env:CLIENT_TRUST_CERT }

$Jar = "client/target/client-1.0-SNAPSHOT-all.jar"

if (-not (Test-Path $Jar)) {
    Write-Host "client JAR not found: $Jar (run 'make package-all' or '.\mvnw.cmd -pl client -am package' first)"
    exit 1
}

# Extract host and port from the endpoint for the reachability probe.
$uri = [Uri]$Endpoint
$targetHost = if ($uri.Host) { $uri.Host } else { "127.0.0.1" }
$targetPort = if ($uri.Port -gt 0) { $uri.Port } else { 8443 }

try {
    $probe = New-Object System.Net.Sockets.TcpClient
    $connect = $probe.ConnectAsync($targetHost, $targetPort)
    if (-not $connect.Wait(5000)) {
        $probe.Close()
        throw "timeout"
    }
    $probe.Close()
} catch {
    Write-Host "stack is not reachable at ${targetHost}:$targetPort (run 'make run-server' first)"
    exit 1
}

# WSS endpoints need the edge certificate; plain ws:// connects without it.
$edgeCertArgs = @()
if ($uri.Scheme -eq "wss") {
    if (-not (Test-Path $TrustCert)) {
        Write-Host "WSS endpoint needs the edge certificate $TrustCert (run 'make run-server' first)"
        exit 1
    }
    $edgeCertArgs = @("--trust-cert", $TrustCert)
}

$script:passed = 0
$script:failed = 0

function Expect {
    param([string]$Name, [string]$Expected, [string[]]$ClientArgs)
    $output = (& java -jar $Jar @ClientArgs 2>$null | Out-String).Trim()
    if ($LASTEXITCODE -eq 0 -and $output.Contains($Expected)) {
        Write-Host "PASS $Name"
        $script:passed = $script:passed + 1
    } else {
        Write-Host "FAIL $Name (expected output containing: $Expected)"
        $script:failed = $script:failed + 1
    }
}

Expect "client ping -> server pong" '"payload":"pong"' (@($Endpoint, "/develop/ping", "--message", "ping") + $edgeCertArgs)
Expect "server ping -> client pong (push)" '"payload":"pinged"' (@($Endpoint, "/develop/ping-clients", "--message", "go") + $edgeCertArgs)
Expect "server recorded the client pong" '"payload":"pong"' (@($Endpoint, "/develop/last-pong", "--message", "check") + $edgeCertArgs)

Write-Host "communication-test: $passed passed, $failed failed"
if ($failed -gt 0) { exit 1 }
exit 0


param(
    [string]$Endpoint = "ws://localhost:8080/ws/socket"
)

$ErrorActionPreference = "Continue"

if ($env:CLIENT_ENDPOINT) { $Endpoint = $env:CLIENT_ENDPOINT }


$Jar = "client/target/client-1.0-SNAPSHOT-all.jar"

if (-not (Test-Path $Jar)) {
    Write-Host "client JAR not found: $Jar (run 'make package-all' or '.\mvnw.cmd -pl client -am package' first)"
    exit 1
}


$uri = [Uri]$Endpoint
$targetHost = if ($uri.Host) { $uri.Host } else { "127.0.0.1" }
$targetPort = if ($uri.Port -gt 0) { $uri.Port } else { 8080 }

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

Expect "client ping -> server pong" '"payload":"pong"' (@($Endpoint, "/develop/ping", "--message", "ping"))
Expect "server ping -> client pong (push)" '"payload":"pinged"' (@($Endpoint, "/develop/ping-clients", "--message", "go"))
Expect "server recorded the client pong" '"payload":"pong"' (@($Endpoint, "/develop/last-pong", "--message", "check"))

Write-Host "communication-test: $passed passed, $failed failed"
if ($failed -gt 0) { exit 1 }
exit 0

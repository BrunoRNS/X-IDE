# Infrastructure

The Compose stack builds an image containing the server JAR and an Nginx
edge. The edge terminates **TLS 1.3 only** for the public endpoints
(TLS 1.2 and older are rejected) and reverse-proxies to the Java services.
MySQL is available to the server at `mysql:3306`.

## Layout

- `compose.yaml`: the app (server JAR + Nginx edge) and MySQL services.
- `Dockerfile`: server JAR + Nginx + the edge entrypoint.
- `nginx/nginx.conf`: the TLS 1.3 edge configuration.
- `nginx/docker-entrypoint.sh`: starts the server JAR and Nginx; generates the
  self-signed development certificate on first start.
- `nginx/certs/`: edge certificates. Git-ignored. A development certificate
  (`server.crt` / `server.key`, `CN=localhost`, SANs `localhost`, `server`,
  `127.0.0.1`, `::1`) is generated automatically when the directory is empty.
  Drop a trusted certificate with the same file names there and recreate the
  container to use it instead.
- `communication-test.sh`: verifies ping/pong in both directions through the
  edge using the real client JAR (POSIX shell; Linux/macOS/Git Bash).
- `communication-test.ps1`: the same check for Windows (PowerShell).

## Ports

| Host port | Purpose |
|---|---|
| `HTTPS_PORT` (default `8443`) | Public TLS 1.3 edge: WSS at `/ws/socket` and the frontend |
| `HTTP_PORT` (default `8080`) | `/healthz` probe; every other request redirects to HTTPS |
| `MYSQL_PORT` (default `3306`) | MySQL, published for local tooling |

The public local WebSocket endpoint is `wss://localhost:8443/ws/socket`. With
the self-signed development certificate, trust it explicitly: the client JAR
accepts `--trust-cert infra/nginx/certs/server.crt` (or use `make run-client`,
which already does it).

```sh
cp .env.example .env
# Edit .env and replace the example passwords.
docker compose --env-file .env -f infra/compose.yaml up --build -d
```

```sh
curl http://localhost:8080/healthz        # ok (plain HTTP probe)
curl -k https://localhost:8443/healthz    # ok (TLS 1.3 edge)
openssl s_client -connect localhost:8443 -tls1_3 </dev/null  # handshake succeeds
openssl s_client -connect localhost:8443 -tls1_2 </dev/null  # handshake rejected
```

`HTTP_PORT`, `HTTPS_PORT` and `MYSQL_PORT` change the host-published ports.
`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, and `DB_PASSWORD` are passed to the
application container.

The WebSocket server is available through the edge at `/ws/socket`. Messages
are JSON objects with a `route` and `payload`; for example, route
`/develop/ping` returns the payload `pong`. MySQL and the configured database
environment are available for future server-side handlers.

## Scripts

`communication-test` verifies ping/pong in both directions through the edge
with the real client JAR. It ships as a POSIX shell script and as a Windows
PowerShell script with identical behavior: both check that the client JAR
exists, probe the endpoint host/port, and run three checks (client ping ->
server pong, server-pushed ping -> client pong, server-recorded pong).
Configuration is shared: `CLIENT_ENDPOINT` and `CLIENT_TRUST_CERT`
environment variables, or `-Endpoint` / `-TrustCert` parameters on the
PowerShell variant. Run both from the repository root.

| Platform | Command |
|---|---|
| Linux / macOS / Git Bash | `make communication-test` or `bash infra/communication-test.sh` |
| Windows (PowerShell) | `make communication-test-windows` or `powershell -NoProfile -ExecutionPolicy Bypass -File infra/communication-test.ps1` |

Both variants need the client JAR and a running stack first:

```sh
make package-all   # or: ./mvnw -pl client -am package
make run-server    # docker compose up --build -d (see below)
```

### Windows notes

- The Maven wrapper has a Windows entry point: use `mvnw.cmd` where the
  documentation says `./mvnw` (for example `.\mvnw.cmd -pl client -am package`).
- PowerShell blocks script execution by default; the `-ExecutionPolicy Bypass`
  flag in the command above handles that for a single invocation without
  changing the machine policy.
- Docker Compose commands work unchanged in PowerShell, using forward slashes:
  `docker compose --env-file .env -f infra/compose.yaml up --build -d`.
- `nginx/docker-entrypoint.sh` intentionally has no Windows variant: it runs
  inside the Linux container (`nginx:alpine`), never on the host.

## Integration tests

`make test-integration` starts this Compose stack, waits for `/healthz`, and
runs the real client against the server through the TLS 1.3 edge
(`wss://127.0.0.1:$HTTPS_PORT/ws/socket`, trusting the development
certificate): the client pings and expects a pong back, and asks the server
(route `/develop/ping-clients`) to push a ping so the client's pong is
recorded and verified (route `/develop/last-pong`). It needs Docker access
for the current user; when the stack is already up the test reuses it and
leaves it running.

## Client route handlers

There is no separate receive-only annotation. On the client, `@WebSocketRoute`
marks a method that handles incoming messages for that route. Client route
methods must be public, accept one `SocketMessage`, and return `void`; this
signature makes them receive-only. Register the controller before connecting:

```java
import org.xide.client.WebSocketClient;
import org.xide.protocol.SocketMessage;
import org.xide.sockets.annotations.WebSocketController;
import org.xide.sockets.annotations.WebSocketRoute;

@WebSocketController
public final class PingController {
    @WebSocketRoute("/develop/ping")
    public void onPong(SocketMessage response) {
        System.out.println(response.payload().asText());
    }
}

WebSocketClient client = new WebSocketClient();
client.registerController(new PingController());
```

The client still supports sending messages independently with
`client.send(SocketMessage.text("/develop/ping", "ping"))`. Incoming messages
are dispatched to a matching annotated method and remain available through
`client.receive(...)`. On the server, route methods instead accept and return a
`SocketMessage`.

# X-IDE

Java 17 Maven multi-module starter for a classroom IDE platform. It ships a **web UI**
(Javalin + Thymeleaf) and a **WebSocket API**, both served by Nginx with TLS 1.3 from a
single Docker/Podman Compose stack. A standalone Swing client JAR is also included for
testing WSS connections. MySQL stores the application data, and Ollama can be started
locally for model-backed features.

## Quick start

Requirements:

- JDK 17+ (the Maven wrapper is included).
- Docker Engine with Compose v2 (or Podman Compose; override with
  `make COMPOSE="podman compose"`).
- `make`, `bash` and `git`.
- On Windows, `make` works from `cmd.exe`/PowerShell (the Makefile uses
  `mvnw.cmd`) or from Git Bash/MSYS2 (it uses `./mvnw`). `make
  communication-test` still needs `bash`; use `make
  communication-test-windows` for the native PowerShell version.
- OpenSSL is only needed inside the container, which installs it automatically.

```sh
make configure     # creates .env from .env.example on first run
# edit .env and replace DB_PASSWORD and DB_ROOT_PASSWORD
make run-server    # builds the app image and starts MySQL + app + Nginx
```

`make run-server` runs Compose in the foreground; use a second terminal for the other
commands, and `make stop-server` to stop the stack. When `TLS_HOST` is not set, the
Makefile auto-detects the machine's LAN IP and prints it before starting:

```text
TLS_HOST not set; auto-detected LAN IP: 192.168.0.42
```

## Open the website (https://\<ip\>:\<port\>)

The web interface is published over **HTTPS on port 8443** by default
(`HTTPS_PORT` in `.env`). Classroom file servers opened from `/classroom` are the
exception: they use plain HTTP on the ports in `CLASSROOM_PORT_RANGE`
(`8083-8102` by default).

1. Start the stack with `make run-server` and wait for MySQL to become healthy and for
   the server to finish creating the database tables.
2. Find the address to open:
   - From the same machine: `https://localhost:8443/`
   - From another device on the LAN: `https://<LAN-IP>:8443/`

   The LAN IP is the one printed by `make run-server` (auto-detected). You can also get
   it with:

   ```sh
   hostname -I | awk '{print $1}'
   # or
   ip -4 route get 1.1.1.1 | sed -n 's/.*src \([0-9.]*\).*/\1/p'
   ```

3. For devices other than the server itself, make sure `.env` has
   `BIND_ADDRESS=0.0.0.0`, allow port 8443 in the host/router firewall, and do **not**
   forward it from the internet router.
4. The certificate is self-signed on first start, so the browser shows a warning.
   Verify the SHA-256 fingerprint (printed by `make server-logs` and by the server
   banner), then choose **Advanced → Proceed** (or import the certificate into the
   device trust store). Never bypass the warning without checking the fingerprint.
5. Available pages:
   - `/` — home page.
   - `/students` — students registered in the database.
   - `/classroom` — host a folder of HTML pages, PDFs and assets and see the open
     room cards.
   - `/competition` and the "create" links are placeholders and are not implemented
     yet (they return 404).

Useful URLs:

| URL | Purpose |
|---|---|
| `https://localhost:8443/` | Web UI from the server machine |
| `https://<LAN-IP>:8443/` | Web UI from a student/other device |
| `https://<LAN-IP>:8443/students` | Student list |
| `wss://<LAN-IP>:8443/ws/socket` | WebSocket endpoint used by the client JAR |

If you change `HTTPS_PORT` in `.env`, use the new value in the URLs. To confirm the
stack is running, use `make stack-status` and check that `app` and `mysql` are healthy.
Uploaded PDFs and classroom files are stored in the Compose volume `uploads_data`, so
they survive stopping or recreating the app container. Removing the Compose volumes
(for example, with `docker compose down -v`) also deletes these uploads.

### Dev mode (hot reload)

```sh
make run-dev-server
```

This bind-mounts `server/src/main/resources` into the container and disables the
Thymeleaf cache, so edits to `templates/*.html` and `public/*` show up on a browser
refresh. Java source changes still require rebuilding:

```sh
make package-server
docker compose --env-file .env -f infra/compose.yaml -f infra/compose.dev.yaml build app
```

The desktop client has a development mode. `make run-dev-client` starts it with
`-Dxide.dev.mode=true`, which disables the single-monitor kiosk check (any number of
monitors is allowed). When running the packaged JAR directly, enable it with
`XIDE_DEV_MODE=true java -jar ...` or `java -Dxide.dev.mode=true -jar ...`.

## Command surface

Run `make` or `make help` to list everything.

| Area | Commands |
|---|---|
| Build | `build-common`, `build-client`, `build-server`, `build-integration`, `build-all`, `package-server`, `package-all` |
| Tests | `test-common`, `test-client`, `test-server`, `test-integration`, `test-all` |
| Stack | `configure`, `run-server`, `run-dev-server`, `stop-server`, `server-logs`, `stack-status` |
| Model | `run-model` (Ollama, default `qwen2.5-coder:3b`) |
| Client | `run-client`, `communication-test`, `communication-test-windows`, `remote-test`, `run-clients` (not implemented) |
| Code style | `format` (rewrite), `lint` (validate) |

Common options:

```sh
make build-all MVN_ARGS='-DskipTests'
make run-server COMPOSE_ARGS='--remove-orphans'
make run-client CLIENT_ROUTE=/develop/ping CLIENT_MESSAGE=ping
make remote-test URL=wss://192.168.0.42:8443/ws/socket
make run-model MODEL_NAME=qwen2.5-coder:1.5b
```

All commands are documented in [`docs/makefile.md`](docs/makefile.md).

## Architecture

```text
Browser / Swing client
        │  HTTPS + WSS (TLS 1.3, port 8443)
        ▼
Nginx edge (inside the app container)
        ├── /      → Javalin HTTP on 127.0.0.1:8081 (Thymeleaf web UI + static files)
        └── /ws/   → Tyrus WebSocket on 127.0.0.1:8082 (routes under /ws)
                          │
                          └── MySQL 8.4 (private Compose network)
```

- The **app** container runs the shaded `server` JAR and Nginx together.
- **MySQL** is reachable only inside the Compose network, on port 3306.
- **Ollama** is published only on `127.0.0.1:11434` and is started separately with
  `make run-model`.
- On startup the server creates the schema (`users`, `problems`, restrictions, problem
  times, solutions, code and submissions) if it does not exist.

WebSocket routes currently registered:

| Route | Purpose |
|---|---|
| `/auth/login` | Validates `email` + `password` against MySQL |
| `/auth/logout` | Removes the requesting session from the logged-in set |
| `/develop/ping` | Returns `pong` |
| `/develop/ping-clients` | Pushes a ping to every connected client |
| `/develop/last-pong` | Returns the last pong recorded from a client |

## Repository layout

- `common/`: shared protocol (`SocketMessage`, codec) and WebSocket annotations/router/scanner.
- `client/`: standalone Swing WSS client JAR, with TOFU certificate pinning.
- `server/`: Javalin web UI, Thymeleaf templates, Tyrus WebSocket server, models and MySQL repositories.
- `infra/`: Compose files, Dockerfile, Nginx TLS configuration and communication-test scripts.
- `scripts/`: server/client launchers and release packaging helpers.
- `docs/`: command reference (`docs/makefile.md`).
- `Makefile`: build, test, format and runtime command surface.

## Configuration (`.env`)

`make configure` copies `.env.example` when `.env` is missing; it never overwrites an
existing file. Required values:

| Variable | Description |
|---|---|
| `DB_NAME`, `DB_USER` | Database name and user created by MySQL. |
| `DB_PASSWORD`, `DB_ROOT_PASSWORD` | Replace the example values before starting. |
| `BIND_ADDRESS` | Host interface for the HTTPS port. Use `0.0.0.0` for LAN access. |
| `HTTPS_PORT` | Published HTTPS/WSS port (default `8443`). |
| `CLASSROOM_PORT_RANGE` | Plain HTTP ports used by hosted classroom folders (default `8083-8102`). |
| `TLS_HOST` | Optional fixed certificate address. Leave unset to auto-detect the LAN IP. |

If `TLS_HOST` is set explicitly, changing it requires replacing only the persistent
`tls_certs` volume; do not remove the database volumes. With auto-detection, a changed
LAN IP regenerates the certificate automatically, and clients that pinned the old
fingerprint must re-pin after verifying the new one.

## Client and communication tests

```sh
make run-client                                  # opens the server connection window
make communication-test                          # ping/pong in both directions
make remote-test URL=wss://192.168.0.42:8443/ws/socket
```

The client verifies the server certificate on first connection (TOFU) and stores the
pin in `~/.xide/trusted-certificates.properties`. For automation, set
`XIDE_TLS_FINGERPRINT` to the fingerprint verified by the instructor. The client JAR
can also be run directly:

```sh
java -jar client/target/client-1.0-SNAPSHOT-all.jar \
  --endpoint wss://localhost:8443/ws/socket /develop/ping --message ping
```

## Security model and limitations

- TLS 1.3 is terminated by Nginx; the certificate is self-signed and generated on first
  start. The client rejects changed certificates until the pin is deliberately removed.
- **Passwords are currently stored and compared as plain text in MySQL.** Do not expose
  this stack to untrusted networks. Add password hashing, account lifecycle, session
  management, rate limiting, auditing, backups and database TLS before any public use.
- The stack is designed for a trusted classroom LAN. Do not forward port 8443 from the
  internet router; isolate student devices on a guest VLAN and restrict access with
  router/host firewall rules. Docker-published ports can bypass UFW, so verify filtering
  at the router or Docker firewall layer.
- Classroom folders are served over plain HTTP on the `CLASSROOM_PORT_RANGE` ports.
  Host only material that is safe to expose on the classroom network.
- WSS only encrypts transport; it does not replace application authentication and
  authorization.
- This is not a sandbox for running untrusted submissions. Execute submissions only in
  separately hardened workers.

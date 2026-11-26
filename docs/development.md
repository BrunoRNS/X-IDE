# X-IDE Development Guide

## Prerequisites

- **JDK 17+** (Maven wrapper included)
- **Docker Engine + Compose v2** (or Podman Compose)
- **make**, **bash**, **git**
- **OpenSSL** (installed in container automatically)

Windows: Use `cmd.exe`/PowerShell (`mvnw.cmd`) or Git Bash/MSYS2 (`./mvnw`).

---

## Project Structure

```
X-IDE/
├── common/                 # Shared protocol (SocketMessage, annotations, codec)
│   └── src/main/java/org/xide/protocol/
├── client/                 # Swing WSS client JAR
│   └── src/main/java/org/xide/client/
├── server/                 # Main backend (Javalin + Tyrus)
│   └── src/main/java/org/xide/
├── infra/                  # Docker, Nginx, Compose, test scripts
│   ├── compose.yaml
│   ├── compose.dev.yaml
│   ├── Dockerfile
│   └── nginx/
├── scripts/                # Launchers, packaging helpers
├── docs/                   # This documentation
├── Makefile                # Command surface
└── README.md
```

---

## Build Commands

| Command | Description |
|---------|-------------|
| `make build-common` | Compile `common` module |
| `make build-client` | Compile `client` + dependencies |
| `make build-server` | Compile `server` + dependencies |
| `make build-integration` | Compile integration tests |
| `make build-all` | Compile all modules |
| `make package-server` | Create shaded JAR for Docker |
| `make package-all` | `mvn package` all modules |
| `make format` | Reformat code (google-java-format) |
| `make lint` | Validate formatting |

```bash
# Skip tests during build
make build-all MVN_ARGS='-DskipTests'
```

---

## Test Commands

| Command | Description |
|---------|-------------|
| `make test-common` | Unit tests for `common` |
| `make test-client` | Unit tests for `client` |
| `make test-server` | Unit tests for `server` |
| `make test-integration` | Socket integration tests (needs running stack) |
| `make test-all` | All tests |

```bash
# Run single test class
cd server && ../mvnw test -Dtest=WebSocketRouterTest
```

---

## Running the Stack

### Production-like (Docker Compose)

```bash
make configure          # Creates .env from .env.example
# Edit .env: set DB_PASSWORD, DB_ROOT_PASSWORD
make run-server         # Builds image, starts MySQL + app + Nginx
```

- Web UI: `https://localhost:8443/` (or `https://<LAN-IP>:8443/`)
- WebSocket: `wss://<LAN-IP>:8443/ws/socket`
- Logs: `make server-logs` (Ctrl+C to stop)
- Status: `make stack-status`
- Stop: `make stop-server`

### Development Mode (Hot Reload)

```bash
make run-dev-server
```

- Bind-mounts `server/src/main/resources` into container
- Thymeleaf cache disabled → template/static edits visible on refresh
- Java changes still require rebuild:
  ```bash
  make package-server
  docker compose --env-file .env -f infra/compose.yaml -f infra/compose.dev.yaml build app
  ```

---

## Configuration (`.env`)

Created by `make configure` from `.env.example`. **Never overwrites existing.**

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `DB_NAME` | ✅ | — | MySQL database name |
| `DB_USER` | ✅ | — | MySQL user |
| `DB_PASSWORD` | ✅ | — | **Must change from example** |
| `DB_ROOT_PASSWORD` | ✅ | — | **Must change from example** |
| `DB_HOST` | | `mysql` | MySQL hostname (Compose service name) |
| `DB_PORT` | | `3306` | MySQL port |
| `BIND_ADDRESS` | | `0.0.0.0` | Host interface for HTTPS port |
| `HTTPS_PORT` | | `8443` | Published HTTPS/WSS port |
| `CLASSROOM_PORT_RANGE` | | `8083-8102` | Plain HTTP ports for classroom folders |
| `TLS_HOST` | | *(auto-detect)* | Fixed cert address; unset = auto-detect LAN IP |
| `XIDE_DEV_RESOURCES` | | *(unset)* | Path to `server/src/main/resources` for dev mode |

**TLS_HOST behavior:**
- Unset → `make run-server` auto-detects LAN IP, generates cert, prints URL
- Set → Uses fixed address; changing it requires replacing `tls_certs` volume only

---

## Adding a WebSocket Controller

1. Create controller class in appropriate sub-package:
   ```java
   package org.xide.sockets.controllers.myfeature;
   
   import org.xide.sockets.annotations.WebSocketRouter;
   import org.xide.sockets.annotations.OnMessage;
   import org.xide.protocol.SocketMessage;
   import jakarta.websocket.Session;
   
   @WebSocketRouter
   public class MyFeatureController {
   
       @OnMessage("MY_ACTION")
       public void handleMyAction(Session session, SocketMessage msg) {
           // Business logic
           session.getAsyncRemote().sendText("{\"type\":\"MY_RESULT\",\"payload\":{...}}");
       }
   }
   ```

2. Controller is auto-discovered by `WebSocketScanner` (scans `Main` class package).

3. Frame type `MY_ACTION` now routes to `handleMyAction`.

---

## Adding an HTTP Endpoint

1. Create handler method in a controller:
   ```java
   package org.xide.http.controllers;
   
   import io.javalin.http.Context;
   
   public class MyController {
       public static void myEndpoint(Context ctx) {
           ctx.json(Map.of("message", "Hello"));
       }
   }
   ```

2. Register route in `Routers.register()`:
   ```java
   config.routes.get("/api/my-endpoint", MyController::myEndpoint);
   ```

3. Access at `https://<host>:8443/api/my-endpoint`.

---

## Database Schema Changes

1. Modify `InitDatabase.init()` to add/migrate tables.
2. For production: write migration scripts (Flyway/Liquibase recommended).
3. Test: `make run-server` → schema auto-creates on first run.

Current tables: `users`, `problems`, `problem_examples`, `problem_test_cases`, `competitions`, `competition_problems`, `submissions`, `submission_test_results`, `classrooms`, `classroom_files`.

---

## Code Style

- **Formatter:** google-java-format (via `make format` / `make lint`)
- **Imports:** Organized automatically
- **Line length:** 100 chars
- **Indentation:** 4 spaces

```bash
make format   # Rewrite files
make lint     # Check only (CI-friendly)
```

---

## Debugging

### Server Logs
```bash
make server-logs          # Follow all services
docker compose logs app   # App only
```

### Database Access
```bash
docker compose exec mysql mysql -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME"
```

### WebSocket Inspection
```bash
# Use client JAR with verbose output
make run-client
# Or connect with wscat (npm install -g wscat)
wscat -c wss://localhost:8443/ws/socket
```

### Hot Reload Not Working?
- Verify `XIDE_DEV_RESOURCES` points to `server/src/main/resources`
- Check container mounts: `docker compose exec app ls /opt/xide/resources`
- Thymeleaf cache disabled only in dev mode

---

## Building Release Artifacts

```bash
make package-all          # All JARs in target/
make package-server       # Shaded server JAR for Docker
```

Docker image built by `make run-server` (uses `infra/Dockerfile`).

---

## Common Issues

| Issue | Solution |
|-------|----------|
| `make run-server` fails on cert generation | Ensure OpenSSL in container (it's in Dockerfile) |
| WebSocket connection refused | Check `BIND_ADDRESS=0.0.0.0`, firewall allows 8443 |
| MySQL connection failed | Verify `.env` passwords, `make stop-server && make run-server` |
| Template changes not showing | Use `make run-dev-server`, not `run-server` |
| Client certificate error | Verify fingerprint matches `make server-logs` output |
| Port 8443 in use | Change `HTTPS_PORT` in `.env` |

---

## IDE Setup

### IntelliJ IDEA
1. Open as Maven project (auto-detects `pom.xml` modules)
2. Enable annotation processing for `WebSocketScanner`
3. Run configurations:
   - `Main` class: `org.xide.Main` (server)
   - `Client Main`: `org.xide.Main` in `client` module

### VS Code
1. Install "Extension Pack for Java"
2. Maven: `Java: Import Maven Projects`
3. Tasks: `make build-all`, `make run-dev-server`

---

## Contributing

1. Fork → feature branch → PR
2. `make lint` must pass
3. `make test-all` must pass
4. Update relevant docs in `docs/`
5. Follow existing code patterns (see `package-info.java` for module overviews)

---

## Release Process

1. Update version in `pom.xml` files (all modules)
2. `make build-all && make test-all`
3. `make package-all`
4. Tag: `git tag v1.0.0beta`
5. Push tag → CI builds Docker image
6. Update `docs/changelog.md`
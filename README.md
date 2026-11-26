# X-IDE v1.0.0beta

Java 17 Maven multi-module platform for classroom programming competitions. Ships a **web UI** (Javalin + Thymeleaf) and a **WebSocket API** (Tyrus), both served by Nginx with TLS 1.3 from a single Docker/Podman Compose stack. A standalone Swing client JAR is included for testing WSS connections. MySQL stores application data; Ollama can run locally for AI features.

## Documentation

| Guide | Description |
|-------|-------------|
| [Getting Started](docs/getting-started.md) | 5-minute quick start, instructor/student workflows |
| [Architecture](docs/architecture.md) | System design, modules, data flow, scaling |
| [API Reference](docs/api.md) | HTTP REST + WebSocket frame specifications |
| [WebSocket Protocol](docs/websocket-protocol.md) | Frame types, connection lifecycle, client implementation |
| [Configuration](docs/configuration.md) | All environment variables, `.env`, TLS, firewall |
| [Development Guide](docs/development.md) | Building, testing, hot reload, adding features |
| [Makefile Commands](docs/makefile.md) | Complete command reference |

## Quick Start

### Prerequisites
- JDK 17+ (Maven wrapper included)
- Docker Engine + Compose v2 (or Podman Compose)
- `make`, `bash`, `git`

### Run
```bash
make configure     # creates .env from .env.example
# edit .env: set DB_PASSWORD and DB_ROOT_PASSWORD
make run-server    # builds image, starts MySQL + app + Nginx
```

Open `https://localhost:8443/` (or `https://<LAN-IP>:8443/` from other devices). Verify the certificate fingerprint printed on startup.

### Dev Mode (Hot Reload)
```bash
make run-dev-server
```
Edits to `server/src/main/resources/templates/*.html` and `public/*` appear on browser refresh.

## Command Surface

Run `make` or `make help` for full list.

| Area | Commands |
|------|----------|
| **Build** | `build-common`, `build-client`, `build-server`, `build-integration`, `build-all`, `package-server`, `package-all` |
| **Test** | `test-common`, `test-client`, `test-server`, `test-integration`, `test-all` |
| **Stack** | `configure`, `run-server`, `run-dev-server`, `stop-server`, `server-logs`, `stack-status` |
| **Model** | `run-model` (Ollama, default `qwen2.5-coder:3b`) |
| **Client** | `run-client`, `communication-test`, `communication-test-windows`, `remote-test`, `run-clients` (not implemented) |
| **Code Style** | `format` (rewrite), `lint` (validate) |

Common options:
```bash
make build-all MVN_ARGS='-DskipTests'
make run-server COMPOSE_ARGS='--remove-orphans'
make run-client CLIENT_ROUTE=/develop/ping CLIENT_MESSAGE=ping
make remote-test URL=wss://192.168.0.42:8443/ws/socket
make run-model MODEL_NAME=qwen2.5-coder:1.5b
```

## Architecture Overview

```
Browser / Swing client
        │  HTTPS + WSS (TLS 1.3, port 8443)
        ▼
Nginx edge (inside app container)
        ├── /      → Javalin HTTP on 127.0.0.1:8081 (Thymeleaf + static files)
        └── /ws/   → Tyrus WebSocket on 127.0.0.1:8082 (routes under /ws)
                          │
                          └── MySQL 8.4 (private Compose network)
```

**Modules:**
- `common/` — Shared protocol (`SocketMessage`, codec, annotations)
- `client/` — Swing WSS client with TOFU certificate pinning
- `server/` — Javalin web UI, Tyrus WS server, models, MySQL repositories
- `infra/` — Dockerfile, Compose, Nginx TLS config, test scripts
- `scripts/` — Launchers, packaging helpers

## Configuration (`.env`)

Created by `make configure` (never overwrites existing). Required:

| Variable | Description |
|----------|-------------|
| `DB_NAME`, `DB_USER` | Database name/user |
| `DB_PASSWORD`, `DB_ROOT_PASSWORD` | **Must change from example** |
| `BIND_ADDRESS` | Host interface for HTTPS (use `0.0.0.0` for LAN) |
| `HTTPS_PORT` | Published port (default `8443`) |
| `CLASSROOM_PORT_RANGE` | Plain HTTP ports for classroom folders (`8083-8102`) |
| `TLS_HOST` | Optional fixed cert address; unset = auto-detect LAN IP |

See [Configuration Guide](docs/configuration.md) for complete reference.

## Security Model

- TLS 1.3 terminated by Nginx (self-signed, auto-generated)
- TOFU certificate pinning in client (`~/.xide/trusted-certificates.properties`)
- JWT tokens (HS256) or email/password via WebSocket
- Role-based authorization (student/instructor)
- MySQL private to Compose network; Ollama on localhost only

⚠️ **Known Limitations:**
- Passwords stored as plain text — do not expose to untrusted networks
- No rate limiting, audit logging, or session management yet
- Classroom folders served over plain HTTP on `CLASSROOM_PORT_RANGE`
- Not a sandbox for untrusted code execution

## Repository Layout

```
X-IDE/
├── common/      # Shared protocol
├── client/      # Swing WSS client
├── server/      # Main backend (Javalin + Tyrus)
├── infra/       # Docker, Nginx, Compose
├── scripts/     # Launchers, packaging
├── docs/        # Documentation (this folder)
├── Makefile     # Command surface
└── README.md
```

## Links

- [Full Makefile Reference](docs/makefile.md)
- [Architecture Deep Dive](docs/architecture.md)
- [API & WebSocket Protocol](docs/api.md)
- [Development Workflow](docs/development.md)
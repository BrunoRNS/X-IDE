# X-IDE Architecture

## Overview

X-IDE is a competitive programming platform designed for classroom use. It consists of three main modules:

```
┌─────────────────────────────────────────────────────────────────┐
│                        X-IDE Platform                           │
├─────────────────┬─────────────────────────────┬─────────────────┤
│     common      │           client            │     server      │
│   (protocol)    │     (Swing WSS client)      │  (Javalin + WS) │
└─────────────────┴─────────────────────────────┴─────────────────┘
```

## Network Topology

```
┌──────────────┐     HTTPS/WSS (TLS 1.3)     ┌──────────────┐
│  Browser /   │ ◄──────────────────────────► │    Nginx     │
│ Swing client │        port 8443             │   (edge)     │
└──────────────┘                               └──────┬───────┘
                                                      │
                    ┌─────────────────────────────────┼─────────────────────────────────┐
                    ▼                                 ▼                                 ▼
            ┌───────────────┐               ┌───────────────┐               ┌───────────────┐
            │  Javalin HTTP │               │  Tyrus WS     │               │    MySQL      │
            │  127.0.0.1:8081               │  127.0.0.1:8082               │  (Compose net)│
            │  (Thymeleaf)  │               │  /ws/socket   │               │  port 3306    │
            └───────────────┘               └───────────────┘               └───────────────┘
```

## Module Details

### common (`common/`)

Shared WebSocket protocol definitions:
- `SocketMessage` — JSON frame structure
- JSON codec (Jackson)
- Ping/pong rule (`pong` payload = response, never re-dispatched)
- Annotations: `@WebSocketRouter`, `@OnMessage`
- Scanner for auto-registering controllers

**Build:** `make build-common` / `make test-common`

### client (`client/`)

Standalone Swing application for testing WSS connections:
- `WebSocketClient` — Connection singleton with auto-reconnect
- Annotated endpoint (`ClientWebSocketEndpoint`) handles server pings
- TOFU (Trust On First Use) certificate pinning to `~/.xide/trusted-certificates.properties`
- CLI entry point (`org.xide.Main`) for automation
- Kiosk mode (single monitor) with dev-mode override

**Build:** `make build-client` / `make package-client` / `make run-client`

### server (`server/`)

Main backend application:
- **HTTP Layer** (Javalin on 127.0.0.1:8081):
  - Thymeleaf templates for instructor dashboard
  - REST API for classrooms, competitions, problems, students
  - Static file serving (`/public`)
- **WebSocket Layer** (Tyrus on 127.0.0.1:8082, path `/ws`):
  - Frame router with annotation-based controllers
  - Per-connection session management (`ClientSessions`)
  - Controllers: auth, competition, submission, develop
- **Persistence** (JDBC → MySQL):
  - Repositories: UserRepo, ProblemRepository, SubmissionRepository
  - Schema auto-initialization on startup
- **Configuration**: `.env` via `EnvRead`, all settings from environment

**Build:** `make build-server` / `make package-server` / `make run-server`

### infra (`infra/`)

Deployment artifacts:
- `Dockerfile` — Multi-stage build (Maven → shaded JAR → runtime)
- `compose.yaml` — Production stack (app + MySQL)
- `compose.dev.yaml` — Dev override (bind-mount resources)
- `nginx/` — TLS termination, cert generation, proxy config
- Communication test scripts (Bash + PowerShell)

## Data Flow

### HTTP Request (Instructor Dashboard)

```
Browser → Nginx (TLS) → Javalin (8081) → Controller → Service → Repository → MySQL
                                                        ↓
                                              Thymeleaf Template
                                                        ↓
                                              HTML Response
```

### WebSocket Frame (Student Client)

```
Swing Client → Nginx (TLS) → Tyrus (8082) → WebSocketEndpoint
                                                        ↓
                                              WebSocketRouter
                                                        ↓
                                              @OnMessage handler
                                                        ↓
                                              Session.sendText(JSON)
```

### Competition Flow

1. Instructor creates competition via HTTP (`/competition/start`)
2. Server initializes `CompetitionState`, assigns problems/balloons
3. Students connect via WSS, authenticate (`/auth/login`)
4. Real-time scoreboard pushed via WebSocket frames
5. Submissions sent via WSS (`SUBMIT` → `SUBMISSION_STATUS` → `SUBMISSION_RESULT`)
6. Instructor stops competition (`/competition/stop`)

## Security Model

- **TLS 1.3** terminated by Nginx (self-signed cert, auto-generated)
- **TOFU** pinning in client (fingerprint stored locally)
- **Authentication**: JWT tokens (HS256) or email/password via WebSocket
- **Authorization**: Role-based (student/instructor) checked in controllers
- **Network**: MySQL private to Compose network; Ollama on localhost only
- **⚠️ Known limitation**: Passwords stored as plain text (see README Security section)

## Scaling Considerations

| Component | Current | Production Ready |
|-----------|---------|------------------|
| HTTP Server | Single Javalin instance | Add load balancer, multiple replicas |
| WebSocket | Single Tyrus instance | Sticky sessions or Redis-backed router |
| Database | Single MySQL | Read replicas, connection pooling (HikariCP) |
| File Storage | Docker volume | S3-compatible object storage |
| Judge Workers | Not implemented | Separate sandboxed worker pool |

## Technology Stack

| Layer | Technology | Version |
|-------|------------|---------|
| Language | Java | 17 |
| Build | Maven | 3.9+ (wrapper) |
| HTTP | Javalin | 6.x |
| Template | Thymeleaf | 3.x |
| WebSocket | Tyrus (Jakarta WS) | 2.x |
| JSON | Jackson | 2.17+ |
| Database | MySQL | 8.4 |
| JDBC | MySQL Connector/J | 8.x |
| Container | Docker/Podman | Compose v2 |
| Reverse Proxy | Nginx | 1.25+ |
| TLS | OpenSSL | 3.x |
| Testing | JUnit 5, Mockito | 5.x / 5.x |
| Mutation | PITest | 1.15+ |
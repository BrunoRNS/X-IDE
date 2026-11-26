# X-IDE Getting Started

## Quick Start (5 minutes)

### Prerequisites
- JDK 17+
- Docker + Compose v2 (or Podman Compose)
- `make`, `bash`, `git`

### 1. Clone & Configure
```bash
git clone <repo-url>
cd X-IDE
make configure
```

### 2. Edit `.env`
```bash
# Required: change these passwords
DB_PASSWORD=your_secure_password
DB_ROOT_PASSWORD=your_secure_root_password
```

### 3. Start Server
```bash
make run-server
```

Wait for:
```
MySQL healthy
Server started on 127.0.0.1:8081
WebSocket server started on 127.0.0.1:8082
Students connect to: wss://192.168.x.x:8443/ws/socket
Certificate SHA-256 fingerprint: AA:BB:CC:...
```

### 4. Open Web UI
- **Same machine:** `https://localhost:8443/`
- **Other device:** `https://<LAN-IP>:8443/` (printed above)

Accept browser certificate warning (verify fingerprint matches!).

---

## For Instructors: Running a Competition

### 1. Create Problems
1. Open `https://<host>:8443/`
2. Navigate to problem management
3. Create problems with:
   - Title, description, time/memory limits
   - Examples (visible to students)
   - Test cases (hidden, for judging)
   - Optional PDF statement

### 2. Create Classroom
1. Go to `/classroom`
2. Create class, add students (or import from PDF)
3. Students get credentials

### 3. Start Competition
1. Go to `/competition`
2. Create competition, select problems, set duration
3. Click **Start** — students see timer and problems

### 4. Monitor
- Real-time scoreboard at `/competition`
- Balloon colors appear as teams solve problems
- Click **Stop** to end

---

## For Students: Connecting

### Web Browser
1. Open `https://<server-ip>:8443/`
2. Login with provided credentials
3. Access competition, submit solutions

### Swing Client (Testing/Development)
```bash
# Interactive (prompts for server IP)
make run-client

# Automated (with verified fingerprint)
XIDE_TLS_FINGERPRINT="AA:BB:CC:..." \
make remote-test URL=wss://192.168.1.100:8443/ws/socket
```

Client verifies certificate fingerprint on first connect (TOFU).

---

## Development Workflow

### Hot Reload (Templates/Static)
```bash
make run-dev-server
# Edit server/src/main/resources/templates/*.html
# Edit server/src/main/resources/public/*
# Refresh browser → changes visible
```

### Java Changes
```bash
make package-server
docker compose --env-file .env -f infra/compose.yaml -f infra/compose.dev.yaml build app
```

### Run Tests
```bash
make test-all           # All modules
make test-server        # Server only
make test-integration   # Full stack (requires running server)
```

### Code Style
```bash
make format   # Auto-format
make lint     # Check only
```

---

## Common Tasks

### View Logs
```bash
make server-logs        # All services
docker compose logs app # Server only
```

### Database Access
```bash
docker compose exec mysql mysql -u"$DB_USER" -p"$DB_PASSWORD" "$DB_NAME"
```

### Reset Everything
```bash
make stop-server
docker compose down -v  # Removes all volumes (DB, certs, uploads)
make configure          # Fresh .env
make run-server
```

### Change TLS Host (Explicit)
```bash
# Edit .env: TLS_HOST=192.168.1.50
docker compose down -v tls_certs  # Remove only cert volume
make run-server
```

### Run Ollama (AI Features)
```bash
make run-model          # Downloads qwen2.5-coder:3b (~2GB)
# API at http://localhost:11434
```

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `make run-server` hangs | Check Docker daemon, `docker compose ps` |
| Certificate error in browser | Verify fingerprint matches `make server-logs` |
| Can't connect from other device | `BIND_ADDRESS=0.0.0.0`, firewall allows 8443 |
| MySQL connection refused | Wait for healthcheck, check `.env` passwords |
| Template changes not showing | Use `make run-dev-server`, not `run-server` |
| `make` not found | Install GNU Make (`apt install make` / `brew install make`) |
| Port 8443 in use | Change `HTTPS_PORT` in `.env` |

---

## Next Steps

- Read [Architecture](architecture.md) for system overview
- Read [API Reference](api.md) for endpoint details
- Read [WebSocket Protocol](websocket-protocol.md) for client development
- Read [Development Guide](development.md) for contributing
- Read [Configuration](configuration.md) for all env vars
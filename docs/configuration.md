# X-IDE Configuration Reference

All configuration is via environment variables, loaded from `.env` file (or system env).
The `.env` file is created by `make configure` from `.env.example`.

---

## Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_NAME` | MySQL database name | `xide` |
| `DB_USER` | MySQL username | `xide_user` |
| `DB_PASSWORD` | MySQL password (**change from example**) | `secure_password_123` |
| `DB_ROOT_PASSWORD` | MySQL root password (**change from example**) | `root_secure_456` |

---

## Database Connection

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | `mysql` | MySQL hostname (Compose service name) |
| `DB_PORT` | `3306` | MySQL port |

> The server constructs JDBC URL: `jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}`

---

## Network & TLS

| Variable | Default | Description |
|----------|---------|-------------|
| `BIND_ADDRESS` | `0.0.0.0` | Host interface for published HTTPS port. Use `0.0.0.0` for LAN access. |
| `HTTPS_PORT` | `8443` | Published HTTPS/WSS port (Nginx listens here). |
| `CLASSROOM_PORT_RANGE` | `8083-8102` | Plain HTTP ports for hosted classroom folders. |
| `TLS_HOST` | *(auto-detect)* | Fixed certificate address. Unset = auto-detect LAN IP. |

### TLS_HOST Behavior

**Auto-detect (unset):**
1. `make run-server` detects source IP of default route
2. Generates self-signed cert for that IP + `localhost` + `127.0.0.1`
3. Prints student URL: `wss://<LAN-IP>:8443/ws/socket`
4. Cert stored in `tls_certs` Docker volume
5. On LAN IP change → cert auto-regenerates

**Explicit (set):**
1. Uses fixed address for cert generation
2. Changing it requires: `docker compose down -v tls_certs` (removes only cert volume)
3. Database volumes preserved

---

## Development Mode

| Variable | Default | Description |
|----------|---------|-------------|
| `XIDE_DEV_RESOURCES` | *(unset)* | Path to `server/src/main/resources` for hot-reload. Set by `make run-dev-server`. |

When set:
- Thymeleaf reads templates from filesystem (not classpath)
- Template cache disabled
- Static files served from filesystem
- Edits to `templates/*.html` and `public/*` visible on browser refresh

---

## Client Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `XIDE_DEV_MODE` | `false` | Disable single-monitor kiosk check. Set by `make run-dev-client`. |
| `XIDE_TLS_FINGERPRINT` | *(unset)* | Pre-verified SHA-256 fingerprint for automation (TOFU bypass). |

---

## Ollama (Optional)

| Variable | Default | Description |
|----------|---------|-------------|
| `OLLAMA_HOST` | `http://localhost:11434` | Ollama API endpoint. Started separately with `make run-model`. |
| `OLLAMA_MODEL` | `qwen2.5-coder:3b` | Model name for AI features. |

---

## JWT / Authentication

| Variable | Default | Description |
|----------|---------|-------------|
| `JWT_SECRET` | *(generated)* | HS256 signing key for tokens. Auto-generated on first run, stored in `jwt_secret` Docker volume. |
| `JWT_EXPIRATION_HOURS` | `24` | Token expiration. |

---

## Complete `.env.example`

```bash
# Database (REQUIRED - CHANGE THESE)
DB_NAME=xide
DB_USER=xide_user
DB_PASSWORD=CHANGE_ME
DB_ROOT_PASSWORD=CHANGE_ME_ROOT
DB_HOST=mysql
DB_PORT=3306

# Network
BIND_ADDRESS=0.0.0.0
HTTPS_PORT=8443
CLASSROOM_PORT_RANGE=8083-8102

# TLS (optional - leave unset for auto-detect)
# TLS_HOST=192.168.1.100

# Development (set by make run-dev-server)
# XIDE_DEV_RESOURCES=/path/to/server/src/main/resources

# Client (set by make run-dev-client)
# XIDE_DEV_MODE=true

# Automation (optional)
# XIDE_TLS_FINGERPRINT=AA:BB:CC:DD:EE:FF...

# Ollama (optional)
# OLLAMA_HOST=http://localhost:11434
# OLLAMA_MODEL=qwen2.5-coder:3b
```

---

## Docker Volumes

| Volume | Purpose | Persistence |
|--------|---------|-------------|
| `mysql_data` | MySQL data directory | Survives `down`, removed by `down -v` |
| `tls_certs` | Nginx certificates | Survives `down`, removed by `down -v tls_certs` |
| `jwt_secret` | JWT signing key | Survives `down` |
| `uploads_data` | Uploaded PDFs, classroom files | Survives `down`, removed by `down -v` |

---

## Port Summary

| Port | Protocol | Service | Access |
|------|----------|---------|--------|
| 8443 | HTTPS/WSS | Nginx (edge) | LAN (via `BIND_ADDRESS`) |
| 8081 | HTTP | Javalin (internal) | Container only (127.0.0.1) |
| 8082 | WS | Tyrus (internal) | Container only (127.0.0.1) |
| 3306 | MySQL | MySQL | Compose network only |
| 11434 | HTTP | Ollama | Localhost only |
| 8083-8102 | HTTP | Classroom folders | LAN (plain HTTP) |

---

## Firewall Rules (Host)

```bash
# Allow LAN access to HTTPS/WSS
sudo ufw allow from 192.168.0.0/16 to any port 8443

# Allow classroom ports (if used)
sudo ufw allow from 192.168.0.0/16 to any port 8083:8102/tcp

# Block internet access to 8443
# (Do NOT forward port 8443 on internet router)
```

> ⚠️ Docker published ports can bypass UFW. Verify at router/Docker firewall layer.

---

## Security Checklist

- [ ] `DB_PASSWORD` changed from example
- [ ] `DB_ROOT_PASSWORD` changed from example
- [ ] `BIND_ADDRESS=0.0.0.0` for LAN (or specific interface IP)
- [ ] `TLS_HOST` set or auto-detect verified
- [ ] Certificate fingerprint verified by instructor
- [ ] Port 8443 NOT forwarded from internet router
- [ ] Student devices on guest VLAN / isolated network
- [ ] Host/router firewall allows only class network
- [ ] Regular database backups configured
- [ ] **TODO:** Password hashing (currently plaintext)
- [ ] **TODO:** Rate limiting, audit logging, session management
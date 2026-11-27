# Configuration Reference

Complete list of environment variables for `.env`.

## Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `DB_NAME` | MySQL database name | `xide` |
| `DB_USER` | MySQL application user | `xide` |
| `DB_PASSWORD` | Application user password | `secure_password_123` |
| `DB_ROOT_PASSWORD` | MySQL root password | `secure_root_password_456` |

Important: Replace the example passwords in `.env.example` before running `make run-server`.

## Network & TLS

| Variable | Description | Default | Notes |
|----------|-------------|---------|-------|
| `BIND_ADDRESS` | Host interface for published ports | `0.0.0.0` | Use `127.0.0.1` for local-only |
| `HTTPS_PORT` | Published HTTPS/WSS port | `8443` | Change if port conflicts |
| `TLS_HOST` | Fixed certificate host (FQDN or IP) | auto-detect | Leave unset for LAN IP auto-detection |
| `TLS_HOST_AUTO` | Internal flag for auto-detection | `1` | Set by Makefile, do not edit |

### TLS_HOST Behavior

- **Unset (default)**: Makefile detects LAN IP via default route, generates cert for that IP + `localhost` + `127.0.0.1`. Certificate regenerates automatically if LAN IP changes.
- **Set explicitly**: Certificate generated once for the specified host. Changing requires `docker volume rm xide_tls_certs` (or delete the volume in Docker Desktop).

#### How Auto-Detection Works

The Makefile determines the LAN IP by checking the source address of the default route:

```bash
ip -4 route get 1.1.1.1 | sed -n 's/.*src \([0-9.]*\).*/\1/p'
```

On Windows, it uses PowerShell's `Find-NetRoute`. This IP becomes the Subject Alternative Name (SAN) in the generated certificate, along with `localhost` and `127.0.0.1`.

The certificate and key are stored in the `tls_certs` Docker volume, which persists across container restarts. When the LAN IP changes, the entrypoint script detects the mismatch and regenerates the certificate.

### Certificate Generation Details

The entrypoint script (`infra/nginx/docker-entrypoint.sh`) uses OpenSSL to generate a self-signed certificate:

- Algorithm: RSA 2048-bit
- Validity: 365 days
- SANs: TLS_HOST (or auto-detected IP), localhost, 127.0.0.1
- TLS version: 1.3 only (enforced by Nginx configuration)

## Classroom File Server

| Variable | Description | Default |
|----------|-------------|---------|
| `CLASSROOM_PORT_RANGE` | Plain HTTP port range for hosted folders | `8083-8102` |

Each classroom folder gets a unique port from this range. Only accessible on LAN when `BIND_ADDRESS=0.0.0.0`.

### How Classroom Hosting Works

When a user creates a classroom folder through the web UI:
1. The server assigns an available port from the range
2. Nginx configuration is dynamically updated to proxy that port to a static file server
3. The folder contents are served over plain HTTP (no TLS)
4. Multiple classrooms can run simultaneously on different ports

This is designed for distributing course materials (HTML, PDF, assets) within a trusted classroom network.

## Optional Features

| Variable | Description | Default |
|----------|-------------|---------|
| `MODEL_NAME` | Ollama model for AI features | `qwen2.5-coder:3b` |
| `XIDE_SERVER_ROLE` | Server role: `full`, `site`, `websocket` | `full` |

### Server Roles

The `XIDE_SERVER_ROLE` variable controls which services start in the app container:

- **full** (default): Both HTTP site (Javalin) and WebSocket (Tyrus)
- **site**: Only HTTP site, no WebSocket
- **websocket**: Only WebSocket endpoint, no HTTP site

This allows deploying specialized instances for different purposes.

## Development Overrides

These can be passed as `make` arguments:

| Variable | Command | Default |
|----------|---------|---------|
| `MVN_ARGS` | Extra Maven arguments | empty |
| `COMPOSE_ARGS` | Extra Docker Compose arguments | empty |
| `CLIENT_ENDPOINT` | WebSocket endpoint for tests | `wss://localhost:8443/ws/socket` |
| `CLIENT_ROUTE` | Route for `run-client` | `/develop/ping` |
| `CLIENT_MESSAGE` | Message for `run-client` | `ping` |
| `CLIENT_DEV_MODE` | Disable single-monitor check | `true` |
| `RUN_SERVER_BUILD` | Set to `0` to skip image rebuild | `1` |

### Using Override Variables

```bash
# Skip tests during build
make build-all MVN_ARGS='-DskipTests'

# Remove orphan containers on startup
make run-server COMPOSE_ARGS='--remove-orphans'

# Custom client test parameters
make run-client CLIENT_ROUTE=/develop/ping CLIENT_MESSAGE=hello

# Test against remote server
make remote-test URL=wss://192.168.1.42:8443/ws/socket

# Use smaller Ollama model
make run-model MODEL_NAME=qwen2.5-coder:1.5b

# Skip Docker image rebuild (use existing image)
make run-server RUN_SERVER_BUILD=0
```

## Example `.env`

```bash
# Database (REQUIRED - replace before first run)
DB_NAME=xide
DB_USER=xide
DB_PASSWORD=your_secure_password_here
DB_ROOT_PASSWORD=your_secure_root_password_here

# Network
BIND_ADDRESS=0.0.0.0
HTTPS_PORT=8443
# TLS_HOST=192.168.1.42   # Uncomment to pin a fixed IP

# Classroom
CLASSROOM_PORT_RANGE=8083-8102

# Optional AI
MODEL_NAME=qwen2.5-coder:3b
```

## Volume Persistence

| Volume | Content | Survives `down -v` |
|--------|---------|-------------------|
| `mysql_data` | Database files | No - deleted |
| `uploads_data` | User uploads | No - deleted |
| `tls_certs` | TLS certificates | No - deleted |
| `ollama_data` | AI models | No - deleted |

To preserve data across restarts, use `make stop-server` (not `down -v`).

### Volume Details

- **mysql_data**: Contains MySQL data directory (`/var/lib/mysql`). Includes all databases, tables, and indexes.
- **uploads_data**: Stores files uploaded through the web UI (classroom materials, user avatars, etc.) at `/opt/xide/uploads`.
- **tls_certs**: Holds the generated TLS certificate (`server.crt`) and private key (`server.key`) at `/etc/nginx/certs`.
- **ollama_data**: Stores downloaded Ollama models at `/root/.ollama`.

### Backup Strategy

For production classroom use, consider backing up volumes:

```bash
# Backup database
docker run --rm -v xide_mysql_data:/data -v $(pwd):/backup alpine tar czf /backup/mysql_backup.tar.gz -C /data .

# Backup uploads
docker run --rm -v xide_uploads_data:/data -v $(pwd):/backup alpine tar czf /backup/uploads_backup.tar.gz -C /data .

# Backup TLS certificates
docker run --rm -v xide_tls_certs:/data -v $(pwd):/backup alpine tar czf /backup/tls_backup.tar.gz -C /data .
```
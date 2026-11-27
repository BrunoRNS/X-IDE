# Getting Started

This guide walks you through setting up and running X-IDE for the first time.

## Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| JDK | 17+ | Temurin, Zulu, or Oracle JDK |
| Docker Engine | 20.10+ | With Compose v2 plugin |
| Make | 4.3+ | GNU Make |
| Git | 2.30+ | For cloning |
| Bash | 4.0+ | Required for some scripts |

Windows users: Use Git Bash/MSYS2 for full compatibility, or cmd.exe/PowerShell (the Makefile auto-detects the shell).

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/BrunoRNS/X-IDE.git
cd X-IDE
```

### 2. Configure Environment

```bash
make configure
```

This creates `.env` from `.env.example`. You must edit `.env` and replace the example passwords:

```bash
# Required - replace these before starting
DB_PASSWORD=your_secure_password
DB_ROOT_PASSWORD=your_secure_root_password
```

The `.env.example` file contains placeholder values. Never commit real passwords to version control.

### 3. Start the Server Stack

```bash
make run-server
```

This command performs the following steps:
1. Validates the `.env` configuration
2. Builds the server Docker image (includes the shaded JAR, Nginx configuration, and entrypoint script)
3. Starts the Docker Compose stack with three services:
   - `mysql`: MySQL 8.4 database server
   - `app`: The X-IDE application server (Javalin + Tyrus) behind Nginx
4. Auto-detects your LAN IP address for TLS certificate generation
5. Prints the server URL and certificate SHA-256 fingerprint

Expected output:
```
TLS_HOST not set; auto-detected LAN IP: 192.168.1.42
...
Server URL: https://192.168.1.42:8443/
Certificate SHA-256: a1:b2:c3:...
```

The stack runs in the foreground. Use Ctrl+C to stop it, or open a second terminal for other commands.

### 4. Access the Web UI

Open in your browser:
- Local access: `https://localhost:8443/`
- From another device on the LAN: `https://<LAN-IP>:8443/` (use the IP printed above)

The certificate is self-signed. Verify the SHA-256 fingerprint matches what was printed at startup, then accept the browser warning (Advanced -> Proceed).

### 5. Test WebSocket Communication

Open a second terminal and run:

```bash
make communication-test
```

This verifies bidirectional ping/pong through the WebSocket stack. The test:
1. Packages the client JAR
2. Connects to the WebSocket endpoint
3. Sends a ping message and waits for pong
4. Verifies the server can push messages to connected clients

## Common First Steps

| Task | Command |
|------|---------|
| View running services | `make stack-status` |
| Follow logs | `make server-logs` |
| Stop the stack | `make stop-server` |
| Run in dev mode (hot reload) | `make run-dev-server` |
| Start Ollama for AI features | `make run-model` |

## Understanding the Stack Services

When `make run-server` completes, you have three containers running:

1. **mysql** - Database server on internal port 3306. Not exposed to host.
2. **app** - Combined Nginx + Java application:
   - Nginx listens on port 8443 (published to host)
   - Javalin HTTP server on internal port 8081
   - Tyrus WebSocket server on internal port 8082
   - Classroom static file servers on ports 8083-8102 (published to host)

## Next Steps

- [Configuration Reference](../reference/configuration.md) - All environment variables
- [Command Reference](../reference/commands.md) - Complete Makefile command list
- [WebSocket Protocol](../reference/websocket-protocol.md) - SocketMessage format and routes
- [Security Model](../guides/security.md) - TLS, authentication, and limitations

## Troubleshooting

| Issue | Solution |
|-------|----------|
| `make configure` says `.env` exists | Delete `.env` and re-run, or edit manually |
| Port 8443 already in use | Change `HTTPS_PORT` in `.env` |
| Can't connect from LAN device | Ensure `BIND_ADDRESS=0.0.0.0` and firewall allows port 8443 |
| Certificate fingerprint mismatch | Delete `tls_certs` volume: `docker volume rm xide_tls_certs` |
| MySQL connection failed | Verify `DB_PASSWORD` matches in `.env` |

See [Troubleshooting Guide](../guides/troubleshooting.md) for more issues.
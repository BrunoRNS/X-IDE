# Command Reference

Complete reference for all Makefile commands.

## Command Categories

### Build Commands

| Command | Description |
|---------|-------------|
| `make all` | Alias for `make build-all` |
| `make build-common` | Compiles the shared `common` module |
| `make build-client` | Compiles `client` and its required modules |
| `make build-server` | Compiles `server` and its required modules |
| `make build-integration` | Compiles the client tests and dependencies without running them |
| `make build-all` | Compiles all project modules |
| `make package-server` | Packages the server as a regular JAR (development only) |
| `make package-native-server` | Compiles the server as a GraalVM native executable (`server/target/server`) |
| `make package-all` | Runs the Maven `package` phase for all modules |
| `make package-client` | Builds native client bundle for the host OS/arch using jpackage |
| `make package-server-bundles` | Builds the four multi-arch Docker server release ZIPs (GraalVM native) |

### Test Commands

| Command | Description |
|---------|-------------|
| `make test-common` | Runs the tests for `common` module |
| `make test-client` | Runs the tests for `client` and its dependencies |
| `make test-server` | Runs the tests for `server` and its dependencies |
| `make test-integration` | Runs socket integration tests; the socket service must be available |
| `make test-all` | Runs the tests for all modules |

### Stack Commands

| Command | Description |
|---------|-------------|
| `make configure` | Creates `.env` from `.env.example` if `.env` does not exist; does not overwrite existing file |
| `make run-server` | Configures environment, builds the `app` image without cache, starts Compose stack in foreground (Ctrl+C stops it). Web UI at `https://<server-ip>:8443/` |
| `make run-dev-server` | Same stack, but bind-mounts `server/src/main/resources` with Thymeleaf cache disabled for hot reload |
| `make run-model` | Starts Ollama in CPU mode and downloads `qwen2.5-coder:3b` (about 2 GB on first run). API at `http://localhost:11434`. Integration with X-IDE is via WebSocket through Tyrus server. |
| `make stop-server` | Stops and removes the Compose stack services |
| `make server-logs` | Follows the Compose services' logs |
| `make stack-status` | Shows the status of the Compose services |

### Client Commands

| Command | Description |
|---------|-------------|
| `make run-client` | Packages the client, opens a window to enter the server IP, then sends a message to the route. Defaults to `ping` at `/develop/ping`. Runs in development mode (single-monitor requirement disabled) |
| `make communication-test` | Packages the client and runs the communication test using the Bash script |
| `make communication-test-windows` | Runs the same test using the PowerShell script |
| `make remote-test URL=<address>` | Packages the client and sends a message to the specified WebSocket address. `URL` is required |
| `make run-clients` | Not implemented yet; exits with an error |

### Code Style Commands

| Command | Description |
|---------|-------------|
| `make format` | Normalizes every text file to LF, rewrites Java sources and sorts imports |
| `make lint` | Fails on CR line endings, bad formatting or out-of-order imports |
| `make line-endings-check` | Fails if any text file contains CR |
| `make line-endings-apply` | Rewrites text files with LF line endings |
| `make imports-check` | Checks import order only |
| `make imports-apply` | Rewrites import order only |

### Utility Commands

| Command | Description |
|---------|-------------|
| `make help` | Displays command help |
| `make exit` / `make quit` | Prints `bye` |

### Versioning Commands

The version lives in the root `VERSION` file. `make version` updates that file, all four `pom.xml` files, and the constant printed by `java -jar client/target/client-all.jar --version`. Javadoc `@since` tags are documentation history and are never changed. Jar filenames are version-independent (`client-all.jar` via the shade plugin's `outputFile`, and `server.jar` via `<finalName>`).

| Command | Description |
|---------|-------------|
| `make version V=v1.2.3` | Sets the version from the `V` variable |
| `make version v1.2.3` | Same, using the version as a make goal |
| `make version VERSION=1.2.3` | Same, using the `VERSION` variable |
| `make version` | Uses the highest local `vX.Y.Z` tag; fails if none exists |
| `make version-check` | Fails if `VERSION`, the POMs and the client constant disagree |

## Options

Pass options on the same command line:

```bash
make build-all MVN_ARGS='-DskipTests'
make run-server COMPOSE_ARGS='--remove-orphans'
make run-client CLIENT_ROUTE=/develop/ping CLIENT_MESSAGE=ping
make remote-test URL=wss://host:8443/ws/socket
make run-model MODEL_NAME=qwen2.5-coder:1.5b
```

### Variable Reference

| Variable | Usage | Default |
|----------|-------|---------|
| `MVN_ARGS` | Additional arguments passed to Maven | empty |
| `COMPOSE_ARGS` | Additional arguments passed to Docker Compose by stack commands | empty |
| `CLIENT_ENDPOINT` | WebSocket endpoint used by communication tests | `wss://localhost:8443/ws/socket` |
| `CLIENT_ROUTE` | Route sent by the client | `/develop/ping` |
| `CLIENT_MESSAGE` | Message sent by the client | `ping` |
| `CLIENT_DEV_MODE` | Enables client development mode on `run-client`; disables the single-monitor requirement. Set to `false` to test the kiosk behavior | `true` |
| `MODEL_NAME` | Ollama model pulled by `run-model`; use `qwen2.5-coder:1.5b` for faster inference with less memory | `qwen2.5-coder:3b` |
| `URL` | WebSocket destination for `remote-test` | required for this command |
| `RUN_SERVER_BUILD` | Set to `0` to skip Docker image rebuild | `1` |

## Direct JAR Execution

When running the client JAR directly, omit `--endpoint` to enter the server IP in the window. To connect without the window, pass the endpoint using `--endpoint`; the route is the first positional argument:

```bash
java -jar client/target/client-all.jar \
  --endpoint wss://localhost:8443/ws/socket /develop/ping --message ping
```

## TLS_HOST Auto-Detection

When the shell and `.env` do not define `TLS_HOST`, `make run-server` uses the source IP of the machine's default route and generates the certificate for it. The server prints the student URL and the certificate fingerprint on startup.

Set `TLS_HOST` in `.env` only to pin a fixed address, and set `BIND_ADDRESS` to that interface's IP or `0.0.0.0` so the published port 8443 accepts LAN connections. The generated certificate covers that address as well as `localhost` and `127.0.0.1`, so students connect to `wss://<server-ip>:8443/ws/socket`.

With auto-detection, a changed LAN IP regenerates the persistent certificate automatically. If `TLS_HOST` is set explicitly, changing it requires replacing only the persistent `tls_certs` volume.

Verify the new fingerprint either way. Do not remove the database volumes.

The MySQL service is private to the Compose network. Ollama is published only on localhost.

On first connection, TOFU stores the server certificate pin in `~/.xide/trusted-certificates.properties`. For non-interactive clients, configure `XIDE_TLS_FINGERPRINT` with the fingerprint obtained from the server through a trusted channel.

## Security Notice

Do not forward port 8443 from the internet router. Put student devices on a guest VLAN or use router/host firewall rules to allow only the class network. Docker-published ports may bypass UFW rules, so verify filtering at the router or Docker firewall layer.

The application still needs authentication and authorization before untrusted students can safely access its routes. WSS alone only encrypts transport.
# Makefile Commands

Run commands from the project root using `make <command>`. Running `make` without
a command displays the help (`help`).

## Web interface

After `make run-server`, open the site over HTTPS:

- From the server machine: `https://localhost:8443/`
- From another device on the LAN: `https://<LAN-IP>:8443/` (the LAN IP is printed by
  `make run-server`; `hostname -I` also shows it)
- Pages: `/` (home) and `/students` (registered students)
- WebSocket: `wss://<LAN-IP>:8443/ws/socket`

The certificate is self-signed. Confirm the SHA-256 fingerprint printed by
`make server-logs`, then accept the browser warning (Advanced → Proceed) or import the
certificate into the device trust store. Keep `BIND_ADDRESS=0.0.0.0` for LAN access and
allow port 8443 in the host/router firewall; do not forward it from the internet.

## Build and package

| Command | Description |
|---|---|
| `make all` | Alias for `make build-all`. |
| `make build-common` | Compiles the shared `common` module. |
| `make build-client` | Compiles `client` and its required modules. |
| `make build-server` | Compiles `server` and its required modules. |
| `make build-integration` | Compiles the client tests and dependencies without running them. |
| `make build-all` | Compiles all project modules. |
| `make package-all` | Runs the Maven `package` phase for all modules. |

## Tests

| Command | Description |
|---|---|
| `make test-common` | Runs the tests for `common`. |
| `make test-client` | Runs the tests for `client` and its dependencies. |
| `make test-server` | Runs the tests for `server` and its dependencies. |
| `make test-integration` | Runs socket integration tests; the socket service must be available. |
| `make test-all` | Runs the tests for all modules. |

## Server and communication

| Command | Description |
|---|---|
| `make configure` | Creates `.env` from `.env.example` if `.env` does not exist; it does not overwrite an existing file. |
| `make run-server` | Configures the environment, builds the `app` image without cache, and starts the Compose stack in the foreground (Ctrl+C stops it). The web UI is available at `https://<server-ip>:8443/`. |
| `make run-dev-server` | Same stack, but bind-mounts `server/src/main/resources` with the Thymeleaf cache disabled, so template and static-file edits appear on browser refresh. |
| `make run-model` | Starts Ollama in CPU mode and downloads `qwen2.5-coder:3b` (about 2 GB on first run). Its API is available at `http://localhost:11434`. |
| `make stop-server` | Stops and removes the Compose stack services. |
| `make server-logs` | Follows the Compose services' logs. |
| `make stack-status` | Shows the status of the Compose services. |
| `make run-client` | Packages the client, opens a window to enter the server IP, then sends a message to the route. Defaults to `ping` at `/develop/ping`. |
| `make communication-test` | Packages the client and runs the communication test using the Bash script. |
| `make communication-test-windows` | Runs the same test using the PowerShell script. |
| `make remote-test URL=<address>` | Packages the client and sends a message to the specified WebSocket address. `URL` is required. |
| `make run-clients` | Not implemented yet; exits with an error. |

## Other commands

| Command | Description |
|---|---|
| `make help` | Displays command help. |
| `make exit` or `make quit` | Prints `bye`. |

## Options

Pass options on the same command line:

```sh
make build-all MVN_ARGS='-DskipTests'
make run-server COMPOSE_ARGS='--remove-orphans'
make run-client CLIENT_ROUTE=/develop/ping CLIENT_MESSAGE=ping
make remote-test URL=wss://host:8443/ws/socket
make run-model MODEL_NAME=qwen2.5-coder:3b
```

| Variable | Usage | Default |
|---|---|---|
| `MVN_ARGS` | Additional arguments passed to Maven. | empty |
| `COMPOSE_ARGS` | Additional arguments passed to Docker Compose by stack commands. | empty |
| `CLIENT_ENDPOINT` | WebSocket endpoint used by communication tests. | `wss://localhost:8443/ws/socket` |
| `CLIENT_ROUTE` | Route sent by the client. | `/develop/ping` |
| `CLIENT_MESSAGE` | Message sent by the client. | `ping` |
| `MODEL_NAME` | Ollama model pulled by `run-model`; use `qwen2.5-coder:1.5b` for faster inference with less memory. | `qwen2.5-coder:3b` |
| `URL` | WebSocket destination for `remote-test`. | required for this command |

When running the client JAR directly, omit `--endpoint` to enter the server IP in the
window. To connect without the window, pass the endpoint using `--endpoint`; the route
is the first positional argument:

```sh
java -jar client/target/client-1.0-SNAPSHOT-all.jar \
  --endpoint wss://localhost:8443/ws/socket /develop/ping --message ping
```

`TLS_HOST` is auto-detected: when the shell and `.env` do not define it, `make run-server`
uses the source IP of the machine's default route and generates the certificate for it.
The server prints the student URL and the certificate fingerprint on startup. Set
`TLS_HOST` in `.env` only to pin a fixed address, and set `BIND_ADDRESS` to that
interface's IP or `0.0.0.0` so the published port 8443 accepts LAN connections. The
generated certificate covers that address as well as `localhost` and `127.0.0.1`, so
students connect to `wss://<server-ip>:8443/ws/socket`. With auto-detection, a changed LAN
IP regenerates the persistent certificate automatically; if `TLS_HOST` is set explicitly,
changing it requires replacing only the persistent
`tls_certs` volume. Verify the new fingerprint either way; do not remove the database volumes. The MySQL service
is private to the Compose network; Ollama is published only on localhost. On first
connection, TOFU stores the server certificate pin in
`~/.xide/trusted-certificates.properties`; for non-interactive clients, configure
`XIDE_TLS_FINGERPRINT` with the fingerprint obtained from the server through a trusted
channel.

Do not forward port 8443 from the internet router. Put student devices on a guest VLAN
or use router/host firewall rules to allow only the class network. Docker-published
ports may bypass UFW rules, so verify filtering at the router or Docker firewall layer.
The application still needs authentication and authorization before untrusted students
can safely access its routes; WSS alone only encrypts transport.

# Makefile Commands

Run commands from the project root using `make <command>`. Running `make` without
a command displays the help (`help`).

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
| `make run-server` | Configures the environment, builds the `app` image without cache, and starts the `app` service in the background with Docker Compose. |
| `make run-model` | Starts Ollama in CPU mode and downloads `qwen2.5-coder:3b` (about 2 GB on first run). Its API is available at `http://localhost:11434`. |
| `make stop-server` | Stops and removes the Compose stack services. |
| `make server-logs` | Follows the Compose services' logs. |
| `make stack-status` | Shows the status of the Compose services. |
| `make run-client` | Packages the client and sends a message to the configured endpoint and route; defaults to `ping` at `/develop/ping`. |
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
make run-client CLIENT_ENDPOINT=wss://localhost:8443/ws/socket \
  CLIENT_ROUTE=/develop/ping CLIENT_MESSAGE=ping
make remote-test URL=wss://host:8443/ws/socket
make run-model MODEL_NAME=qwen2.5-coder:3b
```

| Variable | Usage | Default |
|---|---|---|
| `MVN_ARGS` | Additional arguments passed to Maven. | empty |
| `COMPOSE_ARGS` | Additional arguments passed to Docker Compose by stack commands. | empty |
| `CLIENT_ENDPOINT` | WebSocket endpoint used by the client and communication tests. | `wss://localhost:8443/ws/socket` |
| `CLIENT_ROUTE` | Route sent by the client. | `/develop/ping` |
| `CLIENT_MESSAGE` | Message sent by the client. | `ping` |
| `MODEL_NAME` | Ollama model pulled by `run-model`; use `qwen2.5-coder:1.5b` for faster inference with less memory. | `qwen2.5-coder:3b` |
| `URL` | WebSocket destination for `remote-test`. | required for this command |

The server binds to loopback by default. To allow students on the LAN, set both
`BIND_ADDRESS` and `TLS_HOST` in `.env` to the server's private IP (for example
`192.168.1.50`), then connect to `wss://192.168.1.50:8443/ws/socket`. The MySQL service
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

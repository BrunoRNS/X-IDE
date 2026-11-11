# X-IDE

Java 17 Maven multi-module starter for a WebSocket client/server application. The client
runs as an independent host JAR; the server, MySQL and Nginx run in Docker/Podman Compose.
Public communication is WSS with TLS 1.3 and a MySQL-backed default-account handshake.

## Quick start

Requirements: JDK 17+, Maven 3.9+, CMake 3.20+, Docker Compose v2 or Podman Compose, and
OpenSSL (for local credentials/certificate generation).

```sh
cmake -S . -B build
./xidew.sh
```

Inside the prompt:

```text
xide> run-server
Create password for default X-IDE user (default): [hidden input]
xide> run-client --user default --password 'your-password' --trust-cert deploy/certs/server.crt
xide> run-clients --user default --password 'your-password' --count 10 --trust-cert deploy/certs/server.crt
xide> stop-server
```

`run-server` asks for a password (minimum 12 characters) or accepts `--password` at runtime.
It generates random database secrets in ignored `deploy/.env` and a self-signed localhost
certificate in ignored `deploy/certs/`. The first server start creates the default account
in MySQL. Later starts verify the supplied password against the existing account and do not
overwrite it. `communication-test` generates a random temporary password for its test user.

The public local endpoint is `wss://localhost:8443/ws` (TLS 1.3 only). Local self-signed
certificate trust is explicit via `--trust-cert deploy/certs/server.crt`; the wrapper
adds it automatically for the default localhost client URL. Replace the certificate for
any public deployment.

## CMake/Maven build scopes

| Scope | Build | JUnit |
|---|---|---|
| Shared protocol | `build-common` | `test-common` |
| Client JAR | `build-client` | `test-client` |
| Server JAR + JDBC | `build-server` | `test-server` |
| External probes | `build-integration` | no JUnit (live integration only) |
| All modules | `build-all` | `test-all` |
| Full package lifecycle | `package-all` | includes tests |

See [`docs/package-info.md`](docs/package-info.md) for the Java `package-info.java` documentation layout.
See [`docs/README.md`](docs/README.md) for the CMake targets, wrapper commands, runtime,
security, deployment and test strategy.

## Repository layout

- `common/`: shared protocol records.
- `client/`: standalone WSS client JAR with Tyrus.
- `server/`: WebSocket server JAR, MySQL Connector/J, JDBC user/message repositories.
- `integration/`: single and concurrent authenticated client probes.
- `deploy/`: MySQL + server + Nginx TLS 1.3 Compose stack.
- `cmake/`: CMake build/test/runtime targets.
- `scripts/`: server/client helpers and cross-platform wrapper helpers.
- `*/src/main/java/**/package-info.java`: production package documentation aligned to each CMake build scope.
- `*/src/test/java/**/package-info.java`: documentation for isolated unit-test packages.

## Security model and limitations

The sample account is `default`; multiple simultaneous clients may share it. Authentication
uses a credential-bearing negotiated WebSocket subprotocol during the TLS-protected handshake and PBKDF2-HMAC-SHA256 hashes stored in MySQL.
The Nginx edge only permits TLS 1.3. The local self-signed cert is strictly for localhost
smoke tests. For production, replace it with a trusted certificate, use runtime secrets,
rotate the default password deliberately, and add account lifecycle, rate limiting, auditing,
backups and DB TLS as required. This is not yet a sandbox for running untrusted submissions:
execute submissions only in separately hardened workers.

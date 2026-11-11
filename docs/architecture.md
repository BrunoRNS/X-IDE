# Architecture

X-IDE is a Maven reactor whose modules correspond to focused CMake build scopes.

```text
X-IDE (Maven parent)
├── common       shared protocol DTOs
├── client       independent WSS client executable JAR
├── server       WebSocket server JAR + MySQL JDBC/authentication
└── integration  independent single/concurrent WSS probes

Each module keeps `package-info.java` next to its production sources and, where
unit tests exist, a separate `package-info.java` under its test source root. See
[Java package documentation](package-info.md).

deploy/
├── compose.yaml   MySQL + Java server + Nginx TLS edge
├── Dockerfile     Java 17 server image
└── nginx.conf     TLS 1.3-only WSS reverse proxy
```

Maven resolves dependencies, compiles and tests each module. CMake exposes a stable task
per build/test scope. `xidew.sh` is an interactive frontend to those scopes and runtime
helpers. The client is run directly on each client machine; only the server is containerized.

## Security/data flow

1. Client connects to `wss://host:8443/ws`.
2. Nginx accepts TLS 1.3 and forwards the WebSocket upgrade to the private Java listener.
3. Client offers the `xide` subprotocol plus a URL-safe Base64 credential token in the upgrade.
4. `AuthConfigurator.getNegotiatedSubprotocol` validates username/password via `JdbcUserRepository` and MySQL; it negotiates `xide` only after successful authentication.
5. `PasswordHasher` verifies PBKDF2-HMAC-SHA256 hashes with per-user salts.
6. Authenticated clients may send protocol messages; the server persists received messages via JDBC.

Only one default account is provisioned by this starter. Multiple simultaneous connections
may use the same account. The bootstrap is idempotent: create if absent, verify if present.

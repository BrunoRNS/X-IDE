# Testing strategy

## 1. Isolated JUnit unit tests

Each Java package documents its production and test responsibilities in `package-info.java`, placed separately under `src/main/java` and `src/test/java`. See [the package-info guide](package-info.md).

| CMake target | JUnit test classes | Coverage |
|---|---|---|
| `test-common` | `ProtocolMessageTest` | protocol DTO creation and validation |
| `test-client` | `ClientUriTest` | WSS URI parsing and malformed URI rejection |
| `test-server` | `MessageServiceTest`, `PasswordHasherTest` | protocol service behavior and PBKDF2 password hashing |
| `test-all` | all module JUnit tests | full Maven reactor test suite |

```sh
./xidew.sh test-common
./xidew.sh test-client
./xidew.sh test-server
./xidew.sh test-all
```

JUnit does not launch the client/server application, open sockets, require Docker/Podman,
or connect to MySQL. Maven may compile dependent modules, but CMake's focused targets
select only the target module's named tests. Reports are in each module's
`target/surefire-reports/`.

## 2. Authenticated communication test

```sh
./xidew.sh communication-test
```

This builds the server and probe, generates a random test password, starts MySQL + server +
Nginx, bootstraps the default account through JDBC, and performs a WSS/TLS 1.3 PING/PONG.
It prints the generated test credentials so you can run additional clients. It is an
integration test, not a JUnit test.

## 3. Concurrent clients

With the stack already running:

```sh
./xidew.sh run-clients --user default --password 'your-password' --count 25 \\
  --trust-cert deploy/certs/server.crt
```

This opens 25 independent authenticated sessions concurrently (maximum 500). All clients
can use the same default account at the same time. This is a concurrency smoke test, not a
performance benchmark.

## 4. Remote endpoint

```sh
./xidew.sh remote-test --url wss://judge.example.org/ws --user default --password '...'
```

Omit `--trust-cert` when the remote server uses a certificate trusted by the JVM. Do not
use the localhost self-signed certificate for public hostnames.

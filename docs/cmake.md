# CMake workflow

## Configure

```sh
cmake -S . -B build
# Optional engine selection:
cmake -S . -B build -DXIDE_CONTAINER_ENGINE=docker
cmake -S . -B build -DXIDE_CONTAINER_ENGINE=podman
```

Requires JDK 17+, Maven and CMake. Docker/Podman Compose is required for container tasks.

## Build scopes and package notes

```sh
cmake --build build --target build-common
cmake --build build --target build-client
cmake --build build --target build-server
cmake --build build --target build-integration
cmake --build build --target build-all
cmake --build build --target package-all
```

Every focused target maps to one Maven module. The Java package's `package-info.java`
documents the production scope; a matching file under `src/test/java` documents its unit
Test scope. See [the package-info guide](package-info.md). `build-*` targets skip tests;
`package-all` runs the normal Maven package lifecycle, including tests.

## JUnit scopes

```sh
cmake --build build --target test-common
test-client
test-server
test-all
```

Use the actual command prefix for each target, e.g. `cmake --build build --target test-client`.
Focused targets compile reactor dependencies but select only that module's unit test classes.
They never launch the client/server JAR or connect to MySQL.

## Runtime

```sh
cmake --build build --target run-server
cmake --build build --target stack-status
cmake --build build --target stack-logs
cmake --build build --target stack-down
```

`run-server` invokes the helper which asks for the default-account password if not passed
through `--password` (use `./xidew.sh run-server --user default --password '...'` for args).
The helper creates `deploy/.env` database secrets and the local self-signed certificate.

Client CMake targets can be configured with:

```sh
cmake -S . -B build \\
  -DXIDE_CLIENT_URL=wss://localhost:8443/ws \\
  -DXIDE_CLIENT_USER=default \\
  -DXIDE_CLIENT_PASSWORD='your-long-password' \\
  -DXIDE_CLIENT_COUNT=10
cmake --build build --target run-client
cmake --build build --target run-clients
```

Prefer `xidew.sh run-client --user ... --password ...` to avoid persisting a password in
`CMakeCache.txt`. For a remote endpoint, set `-DXIDE_CLIENT_TRUST_CERT=` to use the JVM's
normal CA trust store (or set it to a specific PEM certificate). CMake's `communication-test`
generates a random test password, provisions its default test account and performs a local
authenticated WSS round trip.

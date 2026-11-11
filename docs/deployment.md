# Container deployment

## Compose services

- `mysql`: MySQL 8.4, internal network only, persistent `mysql_data` volume.
- `server`: Java 17 JRE image with the shaded server JAR, private port 8081.
- `nginx`: TLS 1.3-only WebSocket reverse proxy, public port 8443.

Run `./xidew.sh run-server` to create the local runtime configuration and start the stack.
The script creates `deploy/.env` with random DB and root credentials and generates a
self-signed localhost certificate under `deploy/certs/`. Both paths are ignored by Git.
The default X-IDE account password is requested on each `run-server` invocation unless
passed as `--password`; the database user record is created/validated through JDBC. The
`communication-test` workflow saves its randomly generated test password as
`XIDE_TEST_PASSWORD` in the private `.env` so repeated tests work with the persistent MySQL
volume. Keep that file private and do not reuse test credentials in production.

## Runtime configuration

- `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_ROOT_PASSWORD`: stored in `deploy/.env`.
- `XIDE_DEFAULT_USER`, `XIDE_DEFAULT_PASSWORD`: injected into the server container at runtime
  by `run-server`; not written to `.env`.
- `XIDE_HOST`, `XIDE_PORT`: private Java listener settings.

For production, replace the self-signed files with a trusted certificate/key for your DNS
name, protect the private key, and configure renewal. Use a production secret manager for
all DB/account credentials. The example intentionally does not publish the Java or MySQL
ports to the host.

## Production hardening

This scaffold is not a complete public online-judge deployment. Add account management,
rate limits, connection quotas, logging/auditing, database TLS if required, migrations,
backup/restore, monitoring and incident response. Most importantly, never execute untrusted
submissions in the server/API container; use separate workers with CPU, memory, filesystem,
syscall and network isolation.

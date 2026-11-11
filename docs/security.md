# Authentication, JDBC and TLS

## Default account

This starter exposes one example account (default username `default`). Multiple client
connections may authenticate concurrently with the same username/password. There is no
public registration or role-management flow in this initial scaffold.

`run-server` always supplies `XIDE_DEFAULT_USER` and `XIDE_DEFAULT_PASSWORD` to the server.
The server connects to MySQL through MySQL Connector/J, creates `xide_users` if needed,
and creates the default account only if it does not exist. If the account already exists,
the supplied password is checked against the stored hash; it is never silently replaced.
Passwords are stored as salted PBKDF2-HMAC-SHA256 hashes (210,000 iterations), never as
plaintext. `received_messages` is also initialized through JDBC.

Start the local stack and choose a password interactively:

```sh
./xidew.sh run-server
```

Or pass it at runtime (avoid shell history/process-list exposure where possible):

```sh
./xidew.sh run-server --user default --password 'a-long-password-you-control'
```

Passwords must contain at least 12 characters. If a persistent MySQL volume already
contains `default`, use its current password. To rotate it, implement/use an explicit
administrative password-change process; deleting the database volume is destructive and
is not a password-reset mechanism for valuable data.

## Client authentication

Every client invocation requires explicit credentials:

```sh
./xidew.sh run-client --user default --password 'a-long-password-you-control' \\
  --trust-cert deploy/certs/server.crt
```

The client offers the `xide` WebSocket subprotocol together with a URL-safe Base64-encoded
username/password token in the upgrade handshake. `AuthConfigurator` validates that token
against MySQL through JDBC and negotiates `xide` only when authentication succeeds. This is
a custom handshake convention, not HTTP Basic; the token is only safe because the endpoint
requires WSS and TLS 1.3. The same default account can be used by multiple clients at once.

## TLS 1.3 / WSS

The public endpoint is **`wss://HOST:8443/ws`**. Nginx terminates TLS and is configured
with `ssl_protocols TLSv1.3;`; the Java WebSocket listener is private to the Compose network.
The local `run-server` command creates a self-signed certificate for `localhost` in
`deploy/certs/` if none exists. For local testing, clients trust that certificate using
`--trust-cert deploy/certs/server.crt` (the wrapper adds it automatically for the localhost
URL). Never use this local certificate for a public hostname.

For production, replace `deploy/certs/server.crt` and `server.key` with a valid certificate
and private key for the public DNS name, protect the private key, and configure renewal.
Remote clients should use the JVM's normal trusted CA store and omit `--trust-cert`.
Do not disable certificate verification. Keep TLS 1.3 enabled end-to-end at the public
TLS termination point and do not publish the internal Java port.

## Scope and production limitations

This is a secure-by-default example for transport and password verification, not a complete
production online judge. Before public deployment, add account lifecycle/admin operations,
rate limits, connection quotas, audit logs, secret management, database TLS where required,
backup/restore, and a hardened isolated execution worker for untrusted submissions. Never
run submitted code in the API/server container.

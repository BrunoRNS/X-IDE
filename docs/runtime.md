# Runtime workflows

## Start the containerized server

```sh
./xidew.sh run-server
```

The command builds the server module, creates database secrets in ignored `deploy/.env`
if needed, creates a local self-signed certificate in `deploy/certs/`, asks for the default
account password, then starts MySQL, the Java server and Nginx through Compose. The public
endpoint is `wss://localhost:8443/ws` (TLS 1.3 only). The Java server itself remains on the
private Compose network.

You may pass the password at runtime instead of using the prompt:

```sh
./xidew.sh run-server --user default --password 'a-long-password-you-control'
```

If the MySQL volume already has this account, startup validates the supplied password and
fails if it does not match. It does not replace an existing password automatically.

## Run clients

```sh
./xidew.sh run-client --user default --password 'your-password'
./xidew.sh run-client --user default --password 'your-password' --message 'second session'
./xidew.sh run-clients --user default --password 'your-password' --count 20
```

Each `run-client` starts a separate host-side client JAR. Every client must specify both
`--user` and `--password`. For the local self-signed certificate, the wrapper supplies
`deploy/certs/server.crt` automatically for the default localhost URL; you may also pass
`--trust-cert` explicitly. Multiple clients may authenticate concurrently with the same
`default` login.

For another machine, copy the client JAR and use a publicly trusted WSS endpoint:

```sh
java -jar x-ide-client.jar --url wss://judge.example.org/ws \\
  --user default --password '...' --message hello
```

The remote client needs Java 17+ but does not need Docker/Podman.

## Observe and stop

```sh
./xidew.sh stack-status
./xidew.sh server-logs
./xidew.sh stop-server
```

Stopping preserves the named MySQL volume. `deploy/.env` contains database secrets and
must remain private. Do not commit it.

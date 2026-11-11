# Deployment stack

Use the project manager rather than invoking Compose without its runtime credentials:

```sh
./xidew.sh run-server
./xidew.sh stack-status
./xidew.sh server-logs
./xidew.sh stop-server
```

`run-server` creates private `deploy/.env` database secrets and a self-signed localhost
certificate in `deploy/certs/`, then starts MySQL, the Java server and Nginx. The public
endpoint is `wss://localhost:8443/ws`; Nginx accepts TLS 1.3 only. The server and MySQL
ports are not published on the host.

For production, replace `certs/server.crt` and `certs/server.key` with a trusted certificate
for the deployment hostname, use a secret manager, set the default-account password through
the `run-server --user ... --password ...` runtime arguments, and apply the hardening listed
in [the security guide](../docs/security.md). Keep `.env` and the private key out of Git.

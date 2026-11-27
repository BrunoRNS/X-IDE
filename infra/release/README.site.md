# X-IDE site server __VERSION__

This bundle runs the HTTP site only, without the WebSocket server or a database:

- `server-site`: Javalin HTTP site behind Nginx (TLS 1.3), running on Java 17

The MySQL database is deployed separately with the `xide-server-database` bundle.
Set `DB_HOST` in `.env` to the hostname or IP of that database before starting.

## Requirements

- Docker Engine or Docker Desktop with Compose v2.
- A reachable MySQL 8.4 server (for example the database bundle) with the credentials from `.env`.

## Start

```sh
bash run.sh
```

On Windows, use `run.cmd` or `run.ps1`. The launcher loads the image archive that
matches your Docker architecture, copies `.env.example` to `.env` on first run, and
starts the stack.

Edit `.env` before the first start: set `DB_HOST` and replace `DB_PASSWORD` and
`DB_ROOT_PASSWORD`. `BIND_ADDRESS=0.0.0.0` allows students on the LAN to connect;
use `127.0.0.1` to keep the stack local.

Open `https://localhost:8443/` (or `https://<LAN-IP>:8443/`). The certificate is
self-signed; verify the SHA-256 fingerprint printed by
`docker compose logs server-site` before accepting it in a browser.

## Images

- `image-server-linux-amd64.tar`
- `image-server-linux-arm64.tar`

Load an archive manually with `docker load -i <archive>`.

## Stop

```sh
docker compose down
```

## Role

The container runs with `XIDE_SERVER_ROLE=site`, which starts the HTTP site and
leaves the WebSocket server stopped. The Nginx configuration has no `/ws/` route.

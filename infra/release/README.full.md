# X-IDE full server __VERSION__

This bundle runs the complete X-IDE stack with Docker:

- `full-server`: Javalin HTTP site + Tyrus WebSocket server behind Nginx (TLS 1.3),
  running on Java 17
- `mysql`: application database
- `server-ai`: Ollama with `MODEL_NAME` pulled at startup

## Requirements

- Docker Engine or Docker Desktop with Compose v2.
- Network access on first run to download the configured Ollama model.
- About 4 GB of RAM (Ollama model, MySQL and the server).

## Start

```sh
bash run.sh
```

On Windows, use `run.cmd` or `run.ps1`. The launcher loads the image archive that
matches your Docker architecture, copies `.env.example` to `.env` on first run, and
starts the stack.

Edit `.env` before the first start to replace `DB_PASSWORD` and
`DB_ROOT_PASSWORD`. `BIND_ADDRESS=0.0.0.0` allows students on the LAN to connect;
use `127.0.0.1` to keep the stack local.

Open `https://localhost:8443/` (or `https://<LAN-IP>:8443/`). The certificate is
self-signed; verify the SHA-256 fingerprint printed by
`docker compose logs full-server` before accepting it in a browser.

## Images

- `image-server-linux-amd64.tar` / `image-server-linux-arm64.tar`
- `image-ai-linux-amd64.tar` / `image-ai-linux-arm64.tar`
- `image-database-linux-amd64.tar` / `image-database-linux-arm64.tar`

Load an archive manually with `docker load -i <archive>`.

## Stop

```sh
docker compose down
```

Add `-v` only when you also want to delete the database, uploads, TLS certificate
and model volumes.

## Roles

The full server runs with `XIDE_SERVER_ROLE=full` (HTTP site and WebSocket).
`site` starts only the HTTP site and `websocket` starts only the WebSocket endpoint.

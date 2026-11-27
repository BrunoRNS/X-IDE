# X-IDE database __VERSION__

This bundle runs the MySQL 8.4 database used by the X-IDE site and full servers:

- `mysql`: application database, published on `DB_PORT` (default `3306`)

It is a separate package so the site can be deployed without bundling a database,
or so several site servers can share one database host.

## Requirements

- Docker Engine or Docker Desktop with Compose v2.

## Start

```sh
bash run.sh
```

On Windows, use `run.cmd` or `run.ps1`. The launcher loads the image archive that
matches your Docker architecture, copies `.env.example` to `.env` on first run, and
starts the service.

Edit `.env` before the first start: replace `DB_PASSWORD`, `DB_ROOT_PASSWORD`, and
set `DB_NAME`/`DB_USER` to match the values used by the site bundle. Use
`BIND_ADDRESS=0.0.0.0` to let clients on the LAN reach the database.

## Images

- `image-database-linux-amd64.tar`
- `image-database-linux-arm64.tar`

Load an archive manually with `docker load -i <archive>`.

## Stop

```sh
docker compose down
```

Add `-v` only when you also want to delete `mysql_data` (all application data).

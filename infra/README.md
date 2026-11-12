# Infrastructure

The Compose stack builds an image containing the server JAR and Nginx. Nginx
listens on port `8080` and reverse-proxies requests to a frontend running on
port `8081` on the host. The MySQL service is available to the server at
`mysql:3306`.

```sh
cp .env.example .env
# Edit .env and replace the example passwords.
docker compose --env-file .env -f infra/compose.yaml up --build -d
```

Open `http://localhost:8080` to access the proxied frontend and
`http://localhost:8080/healthz` for the Nginx health check. Set the frontend
server to listen on `0.0.0.0:8081` so the container can reach it. `HTTP_PORT`
and `MYSQL_PORT` change the host-published ports. `DB_HOST`, `DB_PORT`,
`DB_NAME`, `DB_USER`, and `DB_PASSWORD` are passed to the application container.

The current server code does not yet listen on `SERVER_PORT` or connect to
MySQL. This configuration starts the JAR and prepares its environment, but the
server must implement those features before it can expose endpoints or use the
database.

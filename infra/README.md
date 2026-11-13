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

The WebSocket server is available through Nginx at `/ws/socket`. Messages are
JSON objects with a `route` and `payload`; for example, route `/develop/ping`
returns the payload `pong`. MySQL and the configured database environment are
available for future server-side handlers.

## Client route handlers

There is no separate receive-only annotation. On the client, `@WebSocketRoute`
marks a method that handles incoming messages for that route. Client route
methods must be public, accept one `SocketMessage`, and return `void`; this
signature makes them receive-only. Register the controller before connecting:

```java
import org.xide.client.WebSocketClient;
import org.xide.protocol.SocketMessage;
import org.xide.sockets.annotations.WebSocketController;
import org.xide.sockets.annotations.WebSocketRoute;

@WebSocketController
public final class PingController {
    @WebSocketRoute("/develop/ping")
    public void onPong(SocketMessage response) {
        System.out.println(response.payload().asText());
    }
}

WebSocketClient client = new WebSocketClient();
client.registerController(new PingController());
```

The client still supports sending messages independently with
`client.send(SocketMessage.text("/develop/ping", "ping"))`. Incoming messages
are dispatched to a matching annotated method and remain available through
`client.receive(...)`. On the server, route methods instead accept and return a
`SocketMessage`.

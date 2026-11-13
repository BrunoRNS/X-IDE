package org.xide.sockets.controllers.develop;

import org.xide.protocol.SocketMessage;
import org.xide.sockets.annotations.WebSocketController;
import org.xide.sockets.annotations.WebSocketRoute;

@WebSocketController
public final class DevelopSocketController {
    @WebSocketRoute("/develop/ping")
    public SocketMessage ping(SocketMessage request) {
        return SocketMessage.text(request.route(), "pong");
    }
}

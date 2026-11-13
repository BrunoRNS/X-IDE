package org.xide.sockets.controllers;

import org.xide.protocol.SocketMessage;
import org.xide.sockets.utils.WebSocketController;
import org.xide.sockets.utils.WebSocketRoute;

@WebSocketController
public final class DevelopSocketController {
    @WebSocketRoute("/develop/ping")
    public SocketMessage ping(SocketMessage request) {
        return SocketMessage.text(request.route(), "pong");
    }
}

package org.xide.controller;

import org.xide.protocol.SocketMessage;
import org.xide.sockets.annotations.WebSocketController;
import org.xide.sockets.annotations.WebSocketRoute;

@WebSocketController 
public class PingController {
    @WebSocketRoute("/develop/ping")
    public SocketMessage ping(SocketMessage request) {
        System.out.println("Pong");
        return SocketMessage.text(request.route(), "pong");
    }
}

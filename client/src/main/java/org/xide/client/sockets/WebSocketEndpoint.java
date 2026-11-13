package org.xide.client.sockets;


import org.xide.protocol.SocketMessageCodec;
import org.xide.sockets.annotations.WebSocketRouter;

import com.fasterxml.jackson.core.JsonProcessingException;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.OnMessage;
import java.util.logging.Level;
import java.util.logging.Logger;

@ClientEndpoint
public class WebSocketEndpoint {
    private static final Logger LOGGER = Logger.getLogger(WebSocketEndpoint.class.getName());
    private final WebSocketRouter router;

    public WebSocketEndpoint(WebSocketRouter router) {
        this.router = router;
    }

    @OnMessage
    public void onMessage(String json) {
        try {
            router.dispatch(SocketMessageCodec.decode(json));
        } catch (JsonProcessingException | IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Invalid WebSocket response from server", e);
        } catch (IllegalStateException e) {
            LOGGER.log(Level.SEVERE, "Client response handler failed", e);
        }
    }
}

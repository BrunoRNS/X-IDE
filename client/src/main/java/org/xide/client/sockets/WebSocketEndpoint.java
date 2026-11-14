package org.xide.client.sockets;



import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xide.protocol.SocketMessage;
import org.xide.protocol.SocketMessageCodec;
import org.xide.sockets.annotations.WebSocketRouter;



import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;

@ClientEndpoint
public class WebSocketEndpoint {
    private static final Logger LOGGER = Logger.getLogger(WebSocketEndpoint.class.getName());
    private final WebSocketRouter router;
    private final Consumer<SocketMessage> messageSink;
    private volatile Session session;

    public WebSocketEndpoint(WebSocketRouter router) {
        this(router, message -> {
        });
    }

    public WebSocketEndpoint(WebSocketRouter router, Consumer<SocketMessage> messageSink) {
        this.router = router;
        this.messageSink = messageSink;
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
    }

    @OnMessage
    public void onMessage(String json) {
        try {
            SocketMessage message = SocketMessageCodec.decode(json);
            messageSink.accept(message);
            if (message.response()) {
                return;
            }
            Optional<SocketMessage> response = router.dispatch(message);
            response.ifPresent(this::send);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Invalid WebSocket response from server", e);
        } 
    }

    private void send(SocketMessage message) {
        Session active = session;
        if (active != null && active.isOpen()) {
            try {
                // Synchronous send so a reply is always flushed before the
                // next inbound frame is processed (or before the client exits).
                active.getBasicRemote().sendText(SocketMessageCodec.encode(message));
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to send WebSocket response", e);
            }
        }
    }
}

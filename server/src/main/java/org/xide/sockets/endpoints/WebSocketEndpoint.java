package org.xide.sockets.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xide.protocol.SocketMessage;
import org.xide.protocol.SocketMessageCodec;
import org.xide.sockets.ClientSessions;
import org.xide.sockets.WebSocketServer;
import org.xide.sockets.annotations.WebSocketRouter;

@ServerEndpoint("/socket")
public final class WebSocketEndpoint {
    private static final Logger LOGGER = Logger.getLogger(WebSocketEndpoint.class.getName());
    private volatile WebSocketRouter router;

    @OnOpen
    public void onOpen(Session session) {
        router = WebSocketServer.getInstance().getRouter();
        ClientSessions.register(session);
        LOGGER.info(() -> "WebSocket client connected: " + session.getId());
    }

    @OnMessage
    public void onMessage(Session session, String json) {
        WebSocketRouter activeRouter = router;
        if (activeRouter == null) {
            send(session, SocketMessage.text("/error", "WebSocket router is not ready"));
            return;
        }

        try {
            SocketMessage message = SocketMessageCodec.decode(json);
            if (message.isPong()) {
                // The client answered our ping: record it, but never dispatch
                // it back or the two peers would keep answering each other forever.
                ClientSessions.recordPong(message);
                return;
            }
            send(session, activeRouter.dispatch(message));
        } catch (JsonProcessingException | IllegalArgumentException e) {
            send(session, SocketMessage.text("/error", e.getMessage()));
        } catch (IllegalStateException e) {
            LOGGER.log(Level.SEVERE, "WebSocket route handler failed", e);
            send(session, SocketMessage.text("/error", "Internal route error"));
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        ClientSessions.unregister(session);
        LOGGER.info(() -> "WebSocket client disconnected: " + reason.getReasonPhrase());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        LOGGER.log(Level.WARNING, "WebSocket connection error", error);
    }

    private static void send(Session session, SocketMessage message) {
        try {
            // Synchronous send: a response is always flushed before the next
            // inbound frame is handled, and pushes are ordered before the
            // reply that triggered them.
            session.getBasicRemote().sendText(SocketMessageCodec.encode(message));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to send WebSocket response", e);
        }
    }
}

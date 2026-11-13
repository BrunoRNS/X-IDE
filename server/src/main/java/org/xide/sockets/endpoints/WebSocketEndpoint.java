package org.xide.sockets.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xide.protocol.SocketMessage;
import org.xide.protocol.SocketMessageCodec;
import org.xide.sockets.WebSocketServer;
import org.xide.sockets.annotations.WebSocketRouter;

@ServerEndpoint("/socket")
public final class WebSocketEndpoint {
    private static final Logger LOGGER = Logger.getLogger(WebSocketEndpoint.class.getName());
    private volatile WebSocketRouter router;

    @OnOpen
    public void onOpen(Session session) {
        router = WebSocketServer.getInstance().getRouter();
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
            SocketMessage request = SocketMessageCodec.decode(json);
            send(session, activeRouter.dispatch(request));
        } catch (JsonProcessingException | IllegalArgumentException e) {
            send(session, SocketMessage.text("/error", e.getMessage()));
        } catch (IllegalStateException e) {
            LOGGER.log(Level.SEVERE, "WebSocket route handler failed", e);
            send(session, SocketMessage.text("/error", "Internal route error"));
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        LOGGER.info(() -> "WebSocket client disconnected: " + reason.getReasonPhrase());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        LOGGER.log(Level.WARNING, "WebSocket connection error", error);
    }

    private static void send(Session session, SocketMessage message) {
        try {
            String json = SocketMessageCodec.encode(message);
            session.getAsyncRemote().sendText(json, result -> {
                if (!result.isOK()) {
                    LOGGER.log(
                        Level.WARNING,
                        "Failed to send WebSocket response",
                        result.getException()
                    );
                }
            });
        } catch (JsonProcessingException e) {
            LOGGER.log(Level.SEVERE, "Failed to serialize WebSocket response", e);
        }
    }
}

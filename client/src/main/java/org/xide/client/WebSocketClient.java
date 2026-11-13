package org.xide.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xide.client.routing.ClientWebSocketRouter;
import org.xide.protocol.SocketMessage;
import org.xide.protocol.SocketMessageCodec;

public final class WebSocketClient extends Endpoint implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(WebSocketClient.class.getName());

    private final LinkedBlockingQueue<SocketMessage> responses = new LinkedBlockingQueue<>();
    private final ClientWebSocketRouter router = new ClientWebSocketRouter();
    private volatile Session session;

    public void registerController(Object controller) {
        router.register(controller);
    }

    public void connect(URI uri) throws DeploymentException, IOException {
        validateUri(uri);
        if (isConnected()) {
            throw new IllegalStateException("Client is already connected.");
        }

        ContainerProvider.getWebSocketContainer().connectToServer(this, uri);
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
        session.addMessageHandler(String.class, this::onMessage);
    }

    @Override
    public void onClose(Session session, CloseReason reason) {
        this.session = null;
        LOGGER.info(() -> "Disconnected: " + reason.getReasonPhrase());
    }

    @Override
    public void onError(Session session, Throwable error) {
        LOGGER.log(Level.WARNING, "WebSocket connection error", error);
    }

    public void send(SocketMessage message) throws IOException {
        Objects.requireNonNull(message, "message");
        Session currentSession = session;
        if (currentSession == null || !currentSession.isOpen()) {
            throw new IllegalStateException("Client is not connected.");
        }
        currentSession.getBasicRemote().sendText(SocketMessageCodec.encode(message));
    }

    public SocketMessage receive(Duration timeout) throws InterruptedException, TimeoutException {
        Objects.requireNonNull(timeout, "timeout");
        if (timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must not be negative");
        }

        SocketMessage response = responses.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (response == null) {
            throw new TimeoutException("No WebSocket response received within " + timeout);
        }
        return response;
    }

    public boolean isConnected() {
        Session currentSession = session;
        return currentSession != null && currentSession.isOpen();
    }

    @Override
    public void close() throws IOException {
        Session currentSession = session;
        if (currentSession != null && currentSession.isOpen()) {
            currentSession.close(new CloseReason(
                CloseReason.CloseCodes.NORMAL_CLOSURE,
                "Client disconnecting"
            ));
        }
        session = null;
    }

    private void onMessage(String json) {
        SocketMessage message;
        try {
            message = SocketMessageCodec.decode(json);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Received an invalid WebSocket response", e);
            responses.offer(SocketMessage.text("/error", "Invalid response from server"));
            return;
        }

        responses.offer(message);
        dispatchToController(message);
    }

    private void dispatchToController(SocketMessage message) {
        try {
            router.dispatch(message);
        } catch (IllegalStateException e) {
            LOGGER.log(Level.WARNING, "Client WebSocket route handler failed", e);
        }
    }

    private static void validateUri(URI uri) {
        Objects.requireNonNull(uri, "uri");
        String scheme = uri.getScheme();
        if (scheme == null
                || !(scheme.equalsIgnoreCase("ws") || scheme.equalsIgnoreCase("wss"))
                || uri.getHost() == null) {
            throw new IllegalArgumentException("URI must be an absolute ws:// or wss:// URI.");
        }
    }
}

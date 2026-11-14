package org.xide.client.sockets;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.client.SslEngineConfigurator;
import org.xide.Main;
import org.xide.client.tls.ServerCertificateTrustManager;
import org.xide.protocol.SocketMessage;
import org.xide.protocol.SocketMessageCodec;
import org.xide.sockets.annotations.WebSocketRouter;
import org.xide.sockets.annotations.WebSocketScanner;

import jakarta.websocket.DeploymentException;
import jakarta.websocket.Session;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

public class WebSocketClient {
    private static WebSocketClient instance;
    private final WebSocketRouter router = new WebSocketRouter();
    private final ClientManager clientManager;
    private final Session session;
    private final Map<String, LinkedBlockingQueue<SocketMessage>> responses = new ConcurrentHashMap<>();

    private WebSocketClient(URI uri)
            throws IOException, DeploymentException, GeneralSecurityException {
        List<Object> controllers = WebSocketScanner.scan(Main.class.getPackageName());
        controllers.forEach(router::register);
        clientManager = ClientManager.createClient();
        if ("wss".equalsIgnoreCase(uri.getScheme())) {
            int port = uri.getPort() < 0 ? 443 : uri.getPort();
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(
                null,
                new TrustManager[] {
                    new ServerCertificateTrustManager(uri.getHost(), port)
                },
                null
            );
            SslEngineConfigurator sslConfigurator = new SslEngineConfigurator(sslContext);
            sslConfigurator.setHostVerificationEnabled(true);
            clientManager.getProperties().put(
                ClientProperties.SSL_ENGINE_CONFIGURATOR,
                sslConfigurator
            );
        }
        session = clientManager.connectToServer(new WebSocketEndpoint(router, this::recordResponse), uri);
    }

    private void recordResponse(SocketMessage message) {
        responses.computeIfAbsent(message.route(), route -> new LinkedBlockingQueue<>()).add(message);
    }

    public static synchronized WebSocketClient getInstance(URI uri)
            throws IOException, DeploymentException, GeneralSecurityException {
        if (instance == null) {
            instance = new WebSocketClient(uri);
        }
        return instance;
    }

    public synchronized WebSocketRouter getRouter(){
        return router;
    }

    public Future<Void> sendRequest(String path, String params) throws Exception {
        return sendRequest(SocketMessage.text(path, params));
    }

    public Future<Void> sendRequest(String path, JsonNode params) throws Exception {
        return sendRequest(new SocketMessage(path, params));
    }

    /**
     * Waits for the next response the server sent on the given route and
     * returns it, or {@code null} when nothing arrives within the timeout.
     */
    public SocketMessage receive(String route, long timeout, TimeUnit unit) throws InterruptedException {
        return responses.computeIfAbsent(route, key -> new LinkedBlockingQueue<>()).poll(timeout, unit);
    }

    public void close() throws IOException {
        if (session.isOpen()) {
            session.close();
        }
    }

    private Future<Void> sendRequest(SocketMessage message) throws Exception {
        if (!session.isOpen()) {
            throw new IllegalStateException("WebSocket connection is not open");
        }
        return session.getAsyncRemote().sendText(SocketMessageCodec.encode(message));
    }

}

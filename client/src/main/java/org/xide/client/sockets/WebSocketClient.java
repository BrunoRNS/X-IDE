package org.xide.client.sockets;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.Future;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.glassfish.tyrus.client.ClientManager;
import org.xide.Main;
import org.xide.protocol.SocketMessage;
import org.xide.protocol.SocketMessageCodec;
import org.xide.sockets.annotations.WebSocketRouter;
import org.xide.sockets.annotations.WebSocketScanner;

import jakarta.websocket.DeploymentException;
import jakarta.websocket.Session;

public class WebSocketClient {
    private static WebSocketClient instance;
    private final WebSocketRouter router = new WebSocketRouter();
    private final ClientManager clientManager;
    private final Session session;

    private WebSocketClient(URI uri)throws IOException, DeploymentException{
        List<Object> controllers = WebSocketScanner.scan(Main.class.getPackageName());
        controllers.forEach(router::register);
        clientManager = ClientManager.createClient();
        session = clientManager.connectToServer(new WebSocketEndpoint(router), uri);
    }

    public static synchronized WebSocketClient getInstance(URI uri) throws IOException, DeploymentException{
        if(instance == null){
            instance = new WebSocketClient(uri);
        }
        return instance;
    }

    public synchronized WebSocketRouter getRouter(){
        return router;
    }

    public Future<Void> sendRequest(String path, String params) throws JsonProcessingException {
        return sendRequest(SocketMessage.text(path, params));
    }

    public Future<Void> sendRequest(String path, JsonNode params) throws JsonProcessingException {
        return sendRequest(new SocketMessage(path, params));
    }

    public void close() throws IOException {
        if (session.isOpen()) {
            session.close();
        }
    }

    private Future<Void> sendRequest(SocketMessage message) throws JsonProcessingException {
        if (!session.isOpen()) {
            throw new IllegalStateException("WebSocket connection is not open");
        }
        return session.getAsyncRemote().sendText(SocketMessageCodec.encode(message));
    }

}

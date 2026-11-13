package org.xide;

import java.net.URI;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;


import org.xide.client.sockets.WebSocketClient;


public final class Main {
    public static void main(String[] args) throws Exception{
        WebSocketClient client =
            WebSocketClient.getInstance(URI.create("ws://127.0.0.1:8082/ws/socket"));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                client.close();
            } catch (IOException e) {
                System.err.println("Failed to close WebSocket client: " + e.getMessage());
            }
        }, "websocket-client-shutdown"));
        client.sendRequest("/develop/ping", "ping").get();
        System.out.println("Client connected. Press Ctrl+C to stop.");
        new CountDownLatch(1).await();
    }
}

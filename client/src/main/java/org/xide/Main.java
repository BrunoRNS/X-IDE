package org.xide;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.xide.client.sockets.WebSocketClient;
import org.xide.protocol.SocketMessage;
import org.xide.protocol.SocketMessageCodec;


public final class Main {
    
    private static final String DEFAULT_ENDPOINT = "wss://localhost:8443/ws/socket";
    private static final String DEFAULT_ROUTE = "/develop/ping";
    private static final String DEFAULT_MESSAGE = "";

    
    public static void main(String[] args) throws Exception{
        String endpoint = null;
        String route = null;
        String message = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--message" -> message = value(args, ++i, "--message");
                case "--user", "--password" -> {
                    System.err.println(args[i] + ": not yet implemented");
                    System.exit(2);
                }
                default -> {
                    if (endpoint == null) {
                        endpoint = args[i];
                    } else if (route == null) {
                        route = args[i];
                    } else {
                        System.err.println("Unexpected argument: " + args[i]);
                        System.exit(2);
                    }
                }
            }
        }

        URI uri = URI.create(endpoint != null ? endpoint : DEFAULT_ENDPOINT);
        String effectiveRoute = route != null ? route : DEFAULT_ROUTE;
        String payload = message != null ? message : DEFAULT_MESSAGE;

        WebSocketClient client = WebSocketClient.getInstance(uri);
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                client.close();
            } catch (IOException e) {
                System.err.println("Failed to close WebSocket client: " + e.getMessage());
            }
        }, "websocket-client-shutdown"));

        client.sendRequest(effectiveRoute, payload).get();
        SocketMessage response = client.receive(effectiveRoute, 10, TimeUnit.SECONDS);
        if (response == null) {
            System.err.println("No response for " + effectiveRoute + " within 10 seconds");
            System.exit(1);
        }
        System.out.println(SocketMessageCodec.encode(response));
        if (args.length == 0) {
            System.out.println("Client connected. Press Ctrl+C to stop.");
            new CountDownLatch(1).await();
        }
    }

    private static String value(String[] args, int index, String flag) {
        if (index >= args.length) {
            System.err.println(flag + " requires a value");
            System.exit(2);
        }
        return args[index];
    }
}

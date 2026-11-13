package org.xide;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


import org.xide.client.sockets.ClientSsl;
import org.xide.client.sockets.WebSocketClient;
import org.xide.protocol.SocketMessage;
import org.xide.protocol.SocketMessageCodec;


public final class Main {
    /** The public local endpoint: the TLS 1.3 edge of the infra stack. */
    private static final String DEFAULT_ENDPOINT = "wss://localhost:8443/ws/socket";
    private static final String DEFAULT_ROUTE = "/develop/ping";
    private static final String DEFAULT_MESSAGE = "";

    /**
     * Usage: java -jar client-all.jar [endpoint] [route]
     *        [--trust-cert certificate.pem] [--message text]
     * With arguments the client sends one request, prints the response and
     * exits (0 on response, 1 without); without arguments it stays connected
     * like an interactive client. {@code --trust-cert} trusts the given PEM
     * certificate for {@code wss://} endpoints (e.g. the self-signed
     * development certificate of the infra edge). {@code --user} and
     * {@code --password} are not yet implemented.
     */
    public static void main(String[] args) throws Exception{
        String endpoint = null;
        String route = null;
        String message = null;
        Path trustCertificate = null;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--trust-cert" -> trustCertificate = Path.of(value(args, ++i, "--trust-cert"));
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

        WebSocketClient client = trustCertificate != null
            ? WebSocketClient.getInstance(uri, ClientSsl.trustCertificate(trustCertificate))
            : WebSocketClient.getInstance(uri);
        if (trustCertificate != null && !"wss".equalsIgnoreCase(uri.getScheme())) {
            System.err.println("Warning: --trust-cert is only used for wss:// endpoints.");
        }
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

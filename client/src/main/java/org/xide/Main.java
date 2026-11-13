package org.xide;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

import jakarta.websocket.DeploymentException;
import org.xide.client.WebSocketClient;
import org.xide.client.controllers.develop.DevelopPingController;
import org.xide.protocol.SocketMessage;

public final class Main {
    private static final URI DEFAULT_SERVER = URI.create("ws://127.0.0.1:8082/ws/socket");
    private static final String DEFAULT_ROUTE = "/develop/ping";
    private static final String ERROR_ROUTE = "/error";
    private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(5);

    private Main() {
    }

    public static void main(String[] args)
            throws IOException, InterruptedException, TimeoutException,
                DeploymentException {
        if (args.length > 3) {
            throw new IllegalArgumentException(
                "Usage: client [ws://host:port/ws [route [message]]]"
            );
        }

        URI server = args.length > 0 ? URI.create(args[0]) : DEFAULT_SERVER;
        String route = args.length > 1 ? args[1] : DEFAULT_ROUTE;
        String payload = args.length > 2 ? args[2] : "ping";

        try (WebSocketClient client = new WebSocketClient()) {
            DevelopPingController pingController = registerPingController(client, route);
            client.connect(server);
            client.send(SocketMessage.text(route, payload));
            SocketMessage response = receiveResponse(client, pingController);
            printResponse(route, response);
        }
    }

    private static DevelopPingController registerPingController(
            WebSocketClient client,
            String route
    ) {
        if (!DEFAULT_ROUTE.equals(route)) {
            return null;
        }

        DevelopPingController controller = new DevelopPingController();
        client.registerController(controller);
        return controller;
    }

    private static SocketMessage receiveResponse(
            WebSocketClient client,
            DevelopPingController pingController
    ) throws InterruptedException, TimeoutException {
        if (pingController != null) {
            return pingController.awaitResponse(RESPONSE_TIMEOUT);
        }
        return client.receive(RESPONSE_TIMEOUT);
    }

    private static void printResponse(String expectedRoute, SocketMessage response) {
        if (ERROR_ROUTE.equals(response.route())) {
            throw new IllegalStateException(payloadText(response));
        }
        if (!expectedRoute.equals(response.route())) {
            throw new IllegalStateException(
                "Expected response route " + expectedRoute + " but received " + response.route()
            );
        }
        System.out.println(payloadText(response));
    }

    private static String payloadText(SocketMessage message) {
        return message.payload() == null ? "" : message.payload().asText();
    }
}

package org.xide;

import java.util.concurrent.CountDownLatch;
import org.xide.sockets.WebSocketServer;

public class Main {
    public static void main(String[] args)
            throws InterruptedException, jakarta.websocket.DeploymentException {
        WebSocketServer server = WebSocketServer.getInstance();
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop, "websocket-shutdown"));
        new CountDownLatch(1).await();
    }
}
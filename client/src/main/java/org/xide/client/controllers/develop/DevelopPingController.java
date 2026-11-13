package org.xide.client.controllers.develop;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.xide.protocol.SocketMessage;
import org.xide.sockets.annotations.WebSocketController;
import org.xide.sockets.annotations.WebSocketRoute;

@WebSocketController
public final class DevelopPingController {
    private final LinkedBlockingQueue<SocketMessage> responses = new LinkedBlockingQueue<>();

    @WebSocketRoute("/develop/ping")
    public void onPingResponse(SocketMessage response) {
        responses.offer(response);
    }

    @WebSocketRoute("/error")
    public void onServerError(SocketMessage response) {
        responses.offer(response);
    }

    public SocketMessage awaitResponse(Duration timeout)
            throws InterruptedException, TimeoutException {
        Objects.requireNonNull(timeout, "timeout");
        if (timeout.isNegative()) {
            throw new IllegalArgumentException("timeout must not be negative");
        }

        SocketMessage response = responses.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (response == null) {
            throw new TimeoutException("No ping response received within " + timeout);
        }
        return response;
    }
}

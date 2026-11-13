package org.xide.sockets.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xide.protocol.SocketMessage;
import org.xide.sockets.controllers.develop.DevelopSocketController;

public class WebSocketRouterTest {
    @Test
    public void dispatchesRequestToControllerAndReturnsItsResponse() {
        WebSocketRouter router = new WebSocketRouter();
        router.register(new DevelopSocketController());

        SocketMessage response =
            router.dispatch(SocketMessage.text("/develop/ping", "ping"));

        assertEquals(SocketMessage.text("/develop/ping", "pong"), response);
    }

    @Test(expected = IllegalStateException.class)
    public void rejectsDuplicateRoutes() {
        WebSocketRouter router = new WebSocketRouter();
        router.register(new DevelopSocketController());
        router.register(new DevelopSocketController());
    }
}

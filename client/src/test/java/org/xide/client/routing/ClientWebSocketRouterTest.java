package org.xide.client.routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.xide.protocol.SocketMessage;
import org.xide.sockets.annotations.WebSocketController;
import org.xide.sockets.annotations.WebSocketRoute;

public class ClientWebSocketRouterTest {
    @Test
    public void dispatchesMessagesToTheMatchingController() {
        ClientWebSocketRouter router = new ClientWebSocketRouter();
        PingController controller = new PingController();
        router.register(controller);

        boolean dispatched = router.dispatch(SocketMessage.text("/develop/ping", "pong"));

        assertTrue(dispatched);
        assertEquals("pong", controller.payload);
    }

    @Test
    public void leavesUnregisteredRoutesUndispatched() {
        ClientWebSocketRouter router = new ClientWebSocketRouter();
        router.register(new PingController());

        assertFalse(router.dispatch(SocketMessage.text("/develop/unknown", "data")));
    }

    @Test(expected = IllegalStateException.class)
    public void rejectsDuplicateRoutes() {
        ClientWebSocketRouter router = new ClientWebSocketRouter();
        router.register(new PingController());
        router.register(new PingController());
    }

    @WebSocketController
    public static final class PingController {
        private String payload;

        @WebSocketRoute("/develop/ping")
        public void onPing(SocketMessage message) {
            payload = message.payload().asText();
        }
    }
}

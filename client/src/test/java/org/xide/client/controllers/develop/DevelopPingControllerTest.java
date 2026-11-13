package org.xide.client.controllers.develop;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import org.junit.Test;
import org.xide.client.routing.ClientWebSocketRouter;
import org.xide.protocol.SocketMessage;

public class DevelopPingControllerTest {
    @Test
    public void receivesPongThroughItsAnnotatedRoute() throws Exception {
        DevelopPingController controller = new DevelopPingController();
        ClientWebSocketRouter router = new ClientWebSocketRouter();
        router.register(controller);

        router.dispatch(SocketMessage.text("/develop/ping", "pong"));

        assertEquals(
            "/develop/ping",
            controller.awaitResponse(Duration.ZERO).route()
        );
    }

    @Test
    public void receivesServerErrorsThroughTheirAnnotatedRoute() throws Exception {
        DevelopPingController controller = new DevelopPingController();
        ClientWebSocketRouter router = new ClientWebSocketRouter();
        router.register(controller);

        router.dispatch(SocketMessage.text("/error", "unknown route"));

        assertEquals(
            "/error",
            controller.awaitResponse(Duration.ZERO).route()
        );
    }
}

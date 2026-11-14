package org.xide.sockets.utils;

import static org.junit.Assert.assertEquals;

import java.util.Optional;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;
import org.xide.protocol.SocketMessage;
import org.xide.protocol.SocketMessageCodec;
import org.xide.sockets.annotations.WebSocketController;
import org.xide.sockets.annotations.WebSocketRoute;
import org.xide.sockets.annotations.WebSocketRouter;
import org.xide.sockets.controllers.develop.DevelopSocketController;

public class WebSocketRouterTest {
    @Test
    public void dispatchesRequestToControllerAndReturnsItsResponse() {
        WebSocketRouter router = new WebSocketRouter();
        router.register(new DevelopSocketController());

        Optional<SocketMessage> response =
            router.dispatch(SocketMessage.text("/develop/ping", "ping"));

        assertEquals(Optional.of(SocketMessage.textResponse("/develop/ping", "pong")), response);
    }

    @Test
    public void wrapsDtoResultsInTheMessagePayload() {
        WebSocketRouter router = new WebSocketRouter();
        router.register(new ResultController());

        Optional<SocketMessage> response =
            router.dispatch(SocketMessage.text("/result", "request"));

        JsonNode payload = SocketMessageCodec.toJsonNode(new ResultDto("ready"));
        assertEquals(Optional.of(SocketMessage.response("/result", payload)), response);
    }

    @Test
    public void allowsRoutesWithoutAResponse() {
        WebSocketRouter router = new WebSocketRouter();
        router.register(new ResultController());

        Optional<SocketMessage> response =
            router.dispatch(SocketMessage.text("/no-response", "request"));

        assertEquals(Optional.empty(), response);
    }

    @Test(expected = IllegalStateException.class)
    public void rejectsDuplicateRoutes() {
        WebSocketRouter router = new WebSocketRouter();
        router.register(new DevelopSocketController());
        router.register(new DevelopSocketController());
    }

    @WebSocketController
    public static final class ResultController {
        @WebSocketRoute("/result")
        public ResultDto result(SocketMessage request) {
            return new ResultDto("ready");
        }

        @WebSocketRoute("/no-response")
        public void noResponse(SocketMessage request) {
        }
    }

    public record ResultDto(String status) {
    }
}

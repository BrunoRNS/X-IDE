package org.xide.controller;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.xide.protocol.SocketMessage;
import org.xide.protocol.SocketMessageCodec;
import org.xide.sockets.annotations.WebSocketRouter;

public class AuthControllerTest {
    @Test
    public void sucessStatusRunsTheSucessFlow() {
        AuthController controller = new AuthController();
        List<String> handled = new ArrayList<>();
        controller.onSucess = handled::add;
        controller.onRejected = status -> handled.add("rejected:" + status);
        WebSocketRouter router = new WebSocketRouter();
        router.register(controller);

        router.dispatch(
                SocketMessage.response(
                        "/auth/login", SocketMessageCodec.toJsonNode(Map.of("status", "sucess"))));

        assertEquals(List.of("sucess"), handled);
    }

    @Test
    public void anyOtherStatusRunsTheRejectedFlow() {
        AuthController controller = new AuthController();
        List<String> handled = new ArrayList<>();
        controller.onSucess = handled::add;
        controller.onRejected = handled::add;
        WebSocketRouter router = new WebSocketRouter();
        router.register(controller);

        router.dispatch(
                SocketMessage.response(
                        "/auth/login", SocketMessageCodec.toJsonNode(Map.of("status", "invalid"))));

        assertEquals(List.of("invalid"), handled);
    }
}

package org.xide.sockets.controllers.auth;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import org.xide.models.User;
import org.xide.protocol.SocketMessage;
import org.xide.protocol.SocketMessageCodec;
import org.xide.sockets.annotations.WebSocketRouter;

public class LoginSocketControllerTest {
    @Test
    public void validCredentialsRespondWithSucessStatus() {
        LoginSocketController controller = new LoginSocketController();
        controller.userLookup = (email, password) -> new User("Ada", password, email, 1L);
        WebSocketRouter router = new WebSocketRouter();
        router.register(controller);

        Optional<SocketMessage> response =
                router.dispatch(
                        new SocketMessage(
                                "/auth/login",
                                SocketMessageCodec.toJsonNode(
                                        Map.of("email", "ada@x.io", "password", "123"))));

        SocketMessage expected =
                SocketMessage.response(
                        "/auth/login",
                        SocketMessageCodec.toJsonNode(new LoginSocketController.Dto("sucess")));
        assertEquals(Optional.of(expected), response);
    }

    @Test
    public void unknownUserRespondsWithInvalidStatus() {
        LoginSocketController controller = new LoginSocketController();
        controller.userLookup = (email, password) -> null;
        WebSocketRouter router = new WebSocketRouter();
        router.register(controller);

        Optional<SocketMessage> response =
                router.dispatch(
                        new SocketMessage(
                                "/auth/login",
                                SocketMessageCodec.toJsonNode(
                                        Map.of("email", "ghost@x.io", "password", "123"))));

        SocketMessage expected =
                SocketMessage.response(
                        "/auth/login",
                        SocketMessageCodec.toJsonNode(new LoginSocketController.Dto("invalid")));
        assertEquals(Optional.of(expected), response);
    }

    @Test(expected = IllegalStateException.class)
    public void databaseFailureFailsTheRoute() {
        LoginSocketController controller = new LoginSocketController();
        controller.userLookup =
                (email, password) -> {
                    throw new SQLException("db down");
                };
        WebSocketRouter router = new WebSocketRouter();
        router.register(controller);

        router.dispatch(
                new SocketMessage(
                        "/auth/login",
                        SocketMessageCodec.toJsonNode(
                                Map.of("email", "ada@x.io", "password", "123"))));
    }
}

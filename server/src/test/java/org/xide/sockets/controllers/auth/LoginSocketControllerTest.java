package org.xide.sockets.controllers.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import jakarta.websocket.Session;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import org.xide.models.User;
import org.xide.protocol.SocketMessage;
import org.xide.protocol.SocketMessageCodec;
import org.xide.sockets.ClientSessions;
import org.xide.sockets.annotations.WebSocketRouter;

public class LoginSocketControllerTest {

	@Test
	public void validCredentialsRespondWithSucessStatus() {
		LoginSocketController controller = new LoginSocketController();
		controller.userLookup = (email, password) -> new User("Ada", password, email, 1L);
		WebSocketRouter router = new WebSocketRouter();
		router.register(controller);

		Optional<SocketMessage> response = router.dispatch(new SocketMessage("/auth/login",
				SocketMessageCodec.toJsonNode(Map.of("email", "ada@x.io", "password", "123"))));

		SocketMessage expected = SocketMessage.response("/auth/login",
				SocketMessageCodec.toJsonNode(new LoginSocketController.Dto("sucess")));
		assertEquals(Optional.of(expected), response);
	}

	@Test
	public void unknownUserRespondsWithInvalidStatus() {
		LoginSocketController controller = new LoginSocketController();
		controller.userLookup = (email, password) -> null;
		WebSocketRouter router = new WebSocketRouter();
		router.register(controller);

		Optional<SocketMessage> response = router.dispatch(new SocketMessage("/auth/login",
				SocketMessageCodec.toJsonNode(Map.of("email", "ghost@x.io", "password", "123"))));

		SocketMessage expected = SocketMessage.response("/auth/login",
				SocketMessageCodec.toJsonNode(new LoginSocketController.Dto("invalid")));
		assertEquals(Optional.of(expected), response);
	}

	@Test
	public void logoutRemovesTheSessionFromTheLoggedSet() {
		Session session = fakeSession();
		ClientSessions.registerLogin(new User("Ada", "123", "ada@x.io", 1L), session);
		try {
			WebSocketRouter router = new WebSocketRouter();
			router.register(new LoginSocketController());

			Optional<SocketMessage> response = router.dispatch(new SocketMessage("/auth/logout", null), session);

			assertTrue(router.isProtected("/auth/logout"));
			assertEquals(Optional.of(SocketMessage.response("/auth/logout",
					SocketMessageCodec.toJsonNode(new LoginSocketController.Dto("sucess")))), response);
			assertFalse(ClientSessions.isLogged(session));
		}
		finally {
			ClientSessions.unregister(session);
		}
	}

	@Test(expected = IllegalStateException.class)
	public void databaseFailureFailsTheRoute() {
		LoginSocketController controller = new LoginSocketController();
		controller.userLookup = (email, password) -> {
			throw new SQLException("db down");
		};
		WebSocketRouter router = new WebSocketRouter();
		router.register(controller);

		router.dispatch(new SocketMessage("/auth/login",
				SocketMessageCodec.toJsonNode(Map.of("email", "ada@x.io", "password", "123"))));
	}

	private static Session fakeSession() {
		return (Session) Proxy.newProxyInstance(Session.class.getClassLoader(), new Class<?>[] { Session.class },
				(proxy, method, args) -> switch (method.getName()) {
					case "hashCode" -> System.identityHashCode(proxy);
					case "equals" -> proxy == args[0];
					case "toString" -> "fake-session";
					default -> null;
				});
	}

}

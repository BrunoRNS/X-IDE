package org.xide.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.xide.protocol.SocketMessage;
import org.xide.protocol.SocketMessageCodec;
import org.xide.sockets.annotations.WebSocketRouter;

/**
 * Unit tests for {@link org.xide.controller.AuthController} login-status dispatch.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
class AuthControllerTest {

	@Test
	void sucessStatusRunsTheSucessFlow() {
		AuthController controller = new AuthController();
		List<String> handled = new ArrayList<>();
		controller.onSucess = handled::add;
		controller.onRejected = status -> handled.add("rejected:" + status);
		WebSocketRouter router = new WebSocketRouter();
		router.register(controller);

		router
			.dispatch(SocketMessage.response("/auth/login", SocketMessageCodec.toJsonNode(Map.of("status", "sucess"))));

		assertEquals(List.of("sucess"), handled);
	}

	@Test
	void anyOtherStatusRunsTheRejectedFlow() {
		AuthController controller = new AuthController();
		List<String> handled = new ArrayList<>();
		controller.onSucess = handled::add;
		controller.onRejected = handled::add;
		WebSocketRouter router = new WebSocketRouter();
		router.register(controller);

		router.dispatch(
				SocketMessage.response("/auth/login", SocketMessageCodec.toJsonNode(Map.of("status", "invalid"))));

		assertEquals(List.of("invalid"), handled);
	}

}

package org.xide.client.sockets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.xide.protocol.SocketMessage;
import org.xide.protocol.SocketMessageCodec;
import org.xide.sockets.annotations.SocketParam;
import org.xide.sockets.annotations.WebSocketController;
import org.xide.sockets.annotations.WebSocketRoute;
import org.xide.sockets.annotations.WebSocketRouter;

class WebSocketEndpointTest {

	@Test
	void serverResponsesReachClientControllers() throws Exception {
		WebSocketRouter router = new WebSocketRouter();
		List<String> handled = new ArrayList<>();
		router.register(new RecordingController(handled));
		WebSocketEndpoint endpoint = new WebSocketEndpoint(router);

		endpoint.onMessage(SocketMessageCodec
			.encode(SocketMessage.response("/auth/login", SocketMessageCodec.toJsonNode(Map.of("status", "sucess")))));

		assertEquals(List.of("sucess"), handled);
	}

	@Test
	void unknownRoutesAreIgnoredWithoutFailing() throws Exception {
		WebSocketRouter router = new WebSocketRouter();
		List<String> handled = new ArrayList<>();
		router.register(new RecordingController(handled));
		WebSocketEndpoint endpoint = new WebSocketEndpoint(router);

		endpoint.onMessage(SocketMessageCodec.encode(SocketMessage.textResponse("/error", "boom")));

		assertEquals(List.of(), handled);
	}

	@WebSocketController
	public static final class RecordingController {

		private final List<String> handled;

		RecordingController(List<String> handled) {
			this.handled = handled;
		}

		@WebSocketRoute("/auth/login")
		public void login(@SocketParam("status") String status) {
			handled.add(status);
		}

	}

}

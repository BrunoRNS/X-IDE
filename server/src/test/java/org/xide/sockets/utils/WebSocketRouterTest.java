package org.xide.sockets.utils;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.xide.protocol.SocketMessage;
import org.xide.protocol.SocketMessageCodec;
import org.xide.sockets.annotations.SocketParam;
import org.xide.sockets.annotations.WebSocketController;
import org.xide.sockets.annotations.WebSocketRoute;
import org.xide.sockets.annotations.WebSocketRouter;
import org.xide.sockets.controllers.develop.DevelopSocketController;

public class WebSocketRouterTest {

	@Test
	void dispatchesRequestToControllerAndReturnsItsResponse() {
		WebSocketRouter router = new WebSocketRouter();
		router.register(new DevelopSocketController());

		Optional<SocketMessage> response = router.dispatch(SocketMessage.text("/develop/ping", "ping"));

		assertEquals(Optional.of(SocketMessage.textResponse("/develop/ping", "pong")), response);
	}

	@Test
	void wrapsDtoResultsInTheMessagePayload() {
		WebSocketRouter router = new WebSocketRouter();
		router.register(new ResultController());

		Optional<SocketMessage> response = router.dispatch(SocketMessage.text("/result", "request"));

		JsonNode payload = SocketMessageCodec.toJsonNode(new ResultDto("ready"));
		assertEquals(Optional.of(SocketMessage.response("/result", payload)), response);
	}

	@Test
	void allowsRoutesWithoutAResponse() {
		WebSocketRouter router = new WebSocketRouter();
		router.register(new ResultController());

		Optional<SocketMessage> response = router.dispatch(SocketMessage.text("/no-response", "request"));

		assertEquals(Optional.empty(), response);
	}

	@Test
	void rejectsDuplicateRoutes() {
		WebSocketRouter router = new WebSocketRouter();
		router.register(new DevelopSocketController());

		assertThatThrownBy(() -> router.register(new DevelopSocketController()))
			.isInstanceOf(IllegalStateException.class);
	}

	@Test
	void injectsPayloadParametersIntoRouteMethod() {
		WebSocketRouter router = new WebSocketRouter();
		router.register(new ParamsController());

		JsonNode payload = SocketMessageCodec.toJsonNode(new ParamsDto("ada", 3));
		Optional<SocketMessage> response = router.dispatch(new SocketMessage("/params", payload));

		assertEquals(Optional.of(SocketMessage.textResponse("/params", "ada:3")), response);
	}

	@Test
	void rejectsRequestMissingAnAnnotatedParameter() {
		WebSocketRouter router = new WebSocketRouter();
		router.register(new ParamsController());

		assertThatThrownBy(() -> router.dispatch(new SocketMessage("/params", SocketMessageCodec.toJsonNode(Map.of()))))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void rejectsRoutesWithUnresolvableParameters() {
		WebSocketRouter router = new WebSocketRouter();

		assertThatThrownBy(() -> router.register(new UnresolvableController()))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void flagsRoutesMarkedAsProtected() {
		WebSocketRouter router = new WebSocketRouter();
		router.register(new SecureController());

		assertEquals(true, router.isProtected("/secret"));
		assertEquals(false, router.isProtected("/open"));
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

	@WebSocketController
	public static final class ParamsController {

		@WebSocketRoute("/params")
		public String params(@SocketParam("name") String name, @SocketParam("attempts") int attempts) {
			return name + ":" + attempts;
		}

	}

	@WebSocketController
	public static final class UnresolvableController {

		@WebSocketRoute("/unresolvable")
		public String unresolvable(String name) {
			return name;
		}

	}

	@WebSocketController
	public static final class SecureController {

		@WebSocketRoute("/open")
		public String open(SocketMessage request) {
			return "ok";
		}

		@WebSocketRoute(value = "/secret", protectedRoute = true)
		public String secret(SocketMessage request) {
			return "ok";
		}

	}

	private record ResultDto(String status) {
	}

	private record ParamsDto(String name, int attempts) {
	}

}

package org.xide.sockets.utils;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import org.xide.protocol.SocketMessage;
import org.xide.protocol.SocketMessageCodec;
import org.xide.sockets.annotations.SocketParam;
import org.xide.sockets.annotations.WebSocketController;
import org.xide.sockets.annotations.WebSocketRoute;
import org.xide.sockets.annotations.WebSocketRouter;
import org.xide.sockets.controllers.develop.DevelopSocketController;

public class WebSocketRouterTest {

	@Test
	public void dispatchesRequestToControllerAndReturnsItsResponse() {
		WebSocketRouter router = new WebSocketRouter();
		router.register(new DevelopSocketController());

		Optional<SocketMessage> response = router.dispatch(SocketMessage.text("/develop/ping", "ping"));

		assertEquals(Optional.of(SocketMessage.textResponse("/develop/ping", "pong")), response);
	}

	@Test
	public void wrapsDtoResultsInTheMessagePayload() {
		WebSocketRouter router = new WebSocketRouter();
		router.register(new ResultController());

		Optional<SocketMessage> response = router.dispatch(SocketMessage.text("/result", "request"));

		JsonNode payload = SocketMessageCodec.toJsonNode(new ResultDto("ready"));
		assertEquals(Optional.of(SocketMessage.response("/result", payload)), response);
	}

	@Test
	public void allowsRoutesWithoutAResponse() {
		WebSocketRouter router = new WebSocketRouter();
		router.register(new ResultController());

		Optional<SocketMessage> response = router.dispatch(SocketMessage.text("/no-response", "request"));

		assertEquals(Optional.empty(), response);
	}

	@Test(expected = IllegalStateException.class)
	public void rejectsDuplicateRoutes() {
		WebSocketRouter router = new WebSocketRouter();
		router.register(new DevelopSocketController());
		router.register(new DevelopSocketController());
	}

	@Test
	public void injectsPayloadParametersIntoRouteMethod() {
		WebSocketRouter router = new WebSocketRouter();
		router.register(new ParamsController());

		JsonNode payload = SocketMessageCodec.toJsonNode(new ParamsDto("ada", 3));
		Optional<SocketMessage> response = router.dispatch(new SocketMessage("/params", payload));

		assertEquals(Optional.of(SocketMessage.textResponse("/params", "ada:3")), response);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsRequestMissingAnAnnotatedParameter() {
		WebSocketRouter router = new WebSocketRouter();
		router.register(new ParamsController());

		router.dispatch(new SocketMessage("/params", SocketMessageCodec.toJsonNode(Map.of())));
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsRoutesWithUnresolvableParameters() {
		WebSocketRouter router = new WebSocketRouter();
		router.register(new UnresolvableController());
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

	public record ResultDto(String status) {
	}

	public record ParamsDto(String name, int attempts) {
	}

}

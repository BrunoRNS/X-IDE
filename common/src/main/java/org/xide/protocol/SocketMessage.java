package org.xide.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

/**
 * Immutable WebSocket protocol frame. {@code route} selects the handler and must not be
 * blank; {@code payload} carries arbitrary JSON; {@code response} marks a message as a
 * response to a previous request (and is omitted from JSON when {@code false}).
 * {@link #PING} and {@link #PONG} are the ping/pong payloads;
 * {@link #response(String, JsonNode)} and {@link #textResponse(String, String)} build
 * response frames.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
public record SocketMessage(String route, JsonNode payload,
		@JsonInclude(JsonInclude.Include.NON_DEFAULT) boolean response) {

	public static final String PING = "ping";

	public static final String PONG = "pong";

	public SocketMessage(String route, JsonNode payload) {
		this(route, payload, false);
	}

	public SocketMessage {
		if (route == null || route.isBlank()) {
			throw new IllegalArgumentException("route must not be blank");
		}
	}

	public static SocketMessage text(String route, String payload) {
		return new SocketMessage(route, TextNode.valueOf(payload));
	}

	/**
	 * Builds a message whose payload is the given object converted to JSON (strings stay
	 * as text payloads). The conversion to a string happens when the message is encoded
	 * for sending.
	 */
	public static SocketMessage text(String route, Object payload) {
		return new SocketMessage(route, SocketMessageCodec.toJsonNode(payload));
	}

	public static SocketMessage response(String route, JsonNode payload) {
		return new SocketMessage(route, payload, true);
	}

	public static SocketMessage textResponse(String route, String payload) {
		return response(route, TextNode.valueOf(payload));
	}
}

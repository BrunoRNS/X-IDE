package org.xide.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

public record SocketMessage(
        String route,
        JsonNode payload,
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

    public static SocketMessage response(String route, JsonNode payload) {
        return new SocketMessage(route, payload, true);
    }

    public static SocketMessage textResponse(String route, String payload) {
        return response(route, TextNode.valueOf(payload));
    }
}

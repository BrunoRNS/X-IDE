package org.xide.protocol;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

public record SocketMessage(String route, JsonNode payload) {
    public static final String PING = "ping";
    public static final String PONG = "pong";

    public SocketMessage {
        if (route == null || route.isBlank()) {
            throw new IllegalArgumentException("route must not be blank");
        }
    }

    public static SocketMessage text(String route, String payload) {
        return new SocketMessage(route, TextNode.valueOf(payload));
    }

    /**
     * A frame whose payload is {@code pong} is a response, never a request:
     * peers record it (or hand it to receive-only controllers) but must not
     * dispatch-and-answer it again, otherwise they would ping-pong forever.
     */
    @JsonIgnore
    public boolean isPong() {
        return PONG.equals(payload.asText());
    }
}

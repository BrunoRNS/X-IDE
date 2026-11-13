package org.xide.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

public record SocketMessage(String route, JsonNode payload) {
    public SocketMessage {
        if (route == null || route.isBlank()) {
            throw new IllegalArgumentException("route must not be blank");
        }
    }

    public static SocketMessage text(String route, String payload) {
        return new SocketMessage(route, TextNode.valueOf(payload));
    }
}

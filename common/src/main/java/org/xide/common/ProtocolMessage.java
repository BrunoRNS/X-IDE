package org.xide.common;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProtocolMessage(String type, String requestId, String payload) {
    public ProtocolMessage {
        if (type == null || type.isBlank()) throw new IllegalArgumentException("type is required");
        if (requestId == null || requestId.isBlank()) throw new IllegalArgumentException("requestId is required");
    }
    public static ProtocolMessage ping(String id, String payload) { return new ProtocolMessage("PING", id, payload); }
    public static ProtocolMessage pong(String id, String payload) { return new ProtocolMessage("PONG", id, payload); }
}

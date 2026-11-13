package org.xide.protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class SocketMessageCodec {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SocketMessageCodec() {
    }

    public static SocketMessage decode(String json) throws JsonProcessingException {
        SocketMessage message = MAPPER.readValue(json, SocketMessage.class);
        if (message == null) {
            throw new IllegalArgumentException("WebSocket message must be a JSON object");
        }
        return message;
    }

    public static String encode(SocketMessage message) throws JsonProcessingException {
        return MAPPER.writeValueAsString(message);
    }
}

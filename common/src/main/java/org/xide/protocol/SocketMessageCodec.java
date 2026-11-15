package org.xide.protocol;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SocketMessageCodec {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public SocketMessageCodec() {}

    public static SocketMessage decode(String json) throws Exception {
        SocketMessage message = MAPPER.readValue(json, SocketMessage.class);
        if (message == null) {
            throw new IllegalArgumentException("WebSocket message must be a JSON object");
        }
        return message;
    }

    public static String encode(SocketMessage message) throws Exception {
        String in = MAPPER.writeValueAsString(message);
        return in;
    }

    public static JsonNode toJsonNode(Object value) {
        return MAPPER.valueToTree(value);
    }
}

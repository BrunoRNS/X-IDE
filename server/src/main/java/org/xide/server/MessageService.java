package org.xide.server;
import org.xide.common.ProtocolMessage;
public final class MessageService {
    public ProtocolMessage handle(ProtocolMessage message) {
        if (!"PING".equals(message.type())) throw new IllegalArgumentException("Unsupported message type: " + message.type());
        return ProtocolMessage.pong(message.requestId(), message.payload() == null ? "ok" : message.payload());
    }
}

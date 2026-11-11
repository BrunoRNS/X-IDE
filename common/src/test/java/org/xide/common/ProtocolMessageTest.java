package org.xide.common;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ProtocolMessageTest {
    @Test void createsPing() {
        var m = ProtocolMessage.ping("id-1", "hello");
        assertEquals("PING", m.type()); assertEquals("id-1", m.requestId()); assertEquals("hello", m.payload());
    }
    @Test void rejectsBlankId() { assertThrows(IllegalArgumentException.class, () -> new ProtocolMessage("PING", " ", null)); }
    @Test void createsPong() { assertEquals("PONG", ProtocolMessage.pong("id", "ok").type()); }
}

package org.xide.server;
import org.junit.jupiter.api.Test;
import org.xide.common.ProtocolMessage;
import static org.junit.jupiter.api.Assertions.*;
class MessageServiceTest {
    private final MessageService service=new MessageService();
    @Test void respondsToPing() {
        var response=service.handle(ProtocolMessage.ping("id","hello"));
        assertEquals("PONG",response.type()); assertEquals("id",response.requestId()); assertEquals("hello",response.payload());
    }
    @Test void rejectsUnsupportedMessage() {
        assertThrows(IllegalArgumentException.class,()->service.handle(new ProtocolMessage("SUBMIT","id","code")));
    }
}

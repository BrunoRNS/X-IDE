package org.xide.protocol;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SocketMessageCodecTest {
    @Test
    public void roundTripsSocketMessages() throws Exception {
        SocketMessage message = SocketMessage.text("/develop/ping", "ping");

        assertEquals(message, SocketMessageCodec.decode(SocketMessageCodec.encode(message)));
    }
}

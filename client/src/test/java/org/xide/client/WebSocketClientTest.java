package org.xide.client;

import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.net.URI;
import org.junit.Test;
import org.xide.protocol.SocketMessage;

public class WebSocketClientTest {
    @Test(expected = IllegalArgumentException.class)
    public void rejectsHttpUrisBeforeConnecting() throws Exception {
        new WebSocketClient().connect(URI.create("http://localhost/socket"));
    }

    @Test(expected = IllegalStateException.class)
    public void refusesToSendWhenDisconnected() throws IOException {
        new WebSocketClient().send(SocketMessage.text("/develop/ping", "ping"));
    }

    @Test
    public void closeBeforeConnectingIsSafe() throws IOException {
        WebSocketClient client = new WebSocketClient();

        client.close();

        assertFalse(client.isConnected());
    }
}

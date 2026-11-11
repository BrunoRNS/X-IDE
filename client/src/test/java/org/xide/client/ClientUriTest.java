package org.xide.client;

import org.junit.jupiter.api.Test;
import java.net.URI;
import static org.junit.jupiter.api.Assertions.*;

class ClientUriTest {
    @Test void acceptsSecureWebSocketUri() {
        var uri=URI.create("wss://judge.example.org/ws");
        assertEquals("wss",uri.getScheme()); assertEquals("/ws",uri.getPath());
    }
    @Test void rejectsMalformedUri() { assertThrows(IllegalArgumentException.class,()->URI.create("not a uri")); }
    @Test void refusesPlainWebSocketBeforeSendingCredentials() throws Exception {
        try (var client = new ClientConnection()) {
            assertThrows(IllegalArgumentException.class, () -> client.connect(
                    URI.create("ws://localhost:8081/ws"), "default", "secret-password".toCharArray(), null));
        }
    }
}

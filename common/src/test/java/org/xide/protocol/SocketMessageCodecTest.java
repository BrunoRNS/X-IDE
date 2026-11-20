package org.xide.protocol;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SocketMessageCodecTest {

	@Test
	public void roundTripsSocketMessages() throws Exception {
		SocketMessage message = SocketMessage.text("/develop/ping", "ping");

		assertEquals(message, SocketMessageCodec.decode(SocketMessageCodec.encode(message)));
	}

	@Test
	public void convertsObjectPayloadsIntoStructuredJson() throws Exception {
		SocketMessage message = SocketMessage.text("/auth/login", new LoginRequest("ada@x.io", "123"));

		assertEquals("ada@x.io", message.payload().get("email").asText());

		SocketMessage decoded = SocketMessageCodec.decode(SocketMessageCodec.encode(message));
		assertEquals("ada@x.io", decoded.payload().get("email").asText());
	}

	@Test
	public void keepsStringPayloadsAsText() {
		SocketMessage message = SocketMessage.text("/develop/ping", "ping");

		assertEquals("ping", message.payload().asText());
	}

	public record LoginRequest(String email, String password) {
	}

}

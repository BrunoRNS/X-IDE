package org.xide.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SocketMessageCodecTest {

	@Test
	void roundTripsSocketMessages() throws Exception {
		SocketMessage message = SocketMessage.text("/develop/ping", "ping");

		assertEquals(message, SocketMessageCodec.decode(SocketMessageCodec.encode(message)));
	}

	@Test
	void convertsObjectPayloadsIntoStructuredJson() throws Exception {
		SocketMessage message = SocketMessage.text("/auth/login", new LoginRequest("ada@x.io", "123"));

		assertEquals("ada@x.io", message.payload().get("email").asText());

		SocketMessage decoded = SocketMessageCodec.decode(SocketMessageCodec.encode(message));
		assertEquals("ada@x.io", decoded.payload().get("email").asText());
	}

	@Test
	void keepsStringPayloadsAsText() {
		SocketMessage message = SocketMessage.text("/develop/ping", "ping");

		assertEquals("ping", message.payload().asText());
	}

	private record LoginRequest(String email, String password) {
	}

}

package org.xide.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SocketMessageCodec} round trips, structured payloads, and text
 * payloads.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
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

	/**
	 * Simple payload fixture used to test object-to-JSON conversion.
	 *
	 * @since v1.0.0
	 * @author LuisOtavio13
	 */
	private record LoginRequest(String email, String password) {
	}

}

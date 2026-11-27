package org.xide;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ServerRole} parsing and capability flags.
 *
 * @since v1.0.0
 * @author BrunoRNS
 */
public class ServerRoleTest {

	@Test
	void blankValueSelectsTheFullServer() {
		assertSame(ServerRole.FULL, ServerRole.from(null));
		assertSame(ServerRole.FULL, ServerRole.from(""));
		assertSame(ServerRole.FULL, ServerRole.from("   "));
	}

	@Test
	void roleNamesAreCaseInsensitiveAndAcceptAliases() {
		assertSame(ServerRole.FULL, ServerRole.from("FULL"));
		assertSame(ServerRole.FULL, ServerRole.from("full-server"));
		assertSame(ServerRole.SITE, ServerRole.from("Site"));
		assertSame(ServerRole.SITE, ServerRole.from("server_site"));
		assertSame(ServerRole.WEBSOCKET, ServerRole.from("WS"));
		assertSame(ServerRole.WEBSOCKET, ServerRole.from("websocket-server"));
	}

	@Test
	void capabilitiesMatchTheSelectedRole() {
		assertTrue(ServerRole.FULL.startsHttp());
		assertTrue(ServerRole.FULL.startsWebSocket());
		assertTrue(ServerRole.SITE.startsHttp());
		assertFalse(ServerRole.SITE.startsWebSocket());
		assertFalse(ServerRole.WEBSOCKET.startsHttp());
		assertTrue(ServerRole.WEBSOCKET.startsWebSocket());
	}

	@Test
	void unknownValueIsRejected() {
		assertThatThrownBy(() -> ServerRole.from("database")).isInstanceOf(IllegalArgumentException.class);
	}

}

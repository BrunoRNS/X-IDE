package org.xide;

import java.util.Locale;

/**
 * Selects which parts of the X-IDE server start in the current process.
 *
 * <p>
 * The role is read from the {@value #ENVIRONMENT_VARIABLE} environment variable. When the
 * variable is absent or blank the server starts both the HTTP site and the WebSocket
 * server ({@link #FULL}), which keeps the local development workflow unchanged. Docker
 * images can start only the site ({@link #SITE}) or only the WebSocket endpoint
 * ({@link #WEBSOCKET}) by setting the variable.
 *
 * @since v1.0.0
 * @author BrunoRNS
 */
public enum ServerRole {

	/**
	 * HTTP site and WebSocket server (default).
	 */
	FULL(true, true),

	/**
	 * HTTP site only; the WebSocket server is not started.
	 */
	SITE(true, false),

	/**
	 * WebSocket server only; the HTTP site is not started.
	 */
	WEBSOCKET(false, true);

	/**
	 * Environment variable that selects the role.
	 */
	public static final String ENVIRONMENT_VARIABLE = "XIDE_SERVER_ROLE";

	private final boolean http;

	private final boolean webSocket;

	ServerRole(boolean http, boolean webSocket) {
		this.http = http;
		this.webSocket = webSocket;
	}

	/**
	 * Whether this role starts the Javalin HTTP site on port 8081.
	 * @return {@code true} when the HTTP site should start
	 */
	public boolean startsHttp() {
		return this.http;
	}

	/**
	 * Whether this role starts the Tyrus WebSocket server on port 8082.
	 * @return {@code true} when the WebSocket server should start
	 */
	public boolean startsWebSocket() {
		return this.webSocket;
	}

	/**
	 * Resolves the role from {@value #ENVIRONMENT_VARIABLE}, defaulting to {@link #FULL}.
	 * @return the configured role
	 */
	public static ServerRole fromEnvironment() {
		return from(System.getenv(ENVIRONMENT_VARIABLE));
	}

	/**
	 * Resolves a role from its textual name. Matching ignores case, surrounding
	 * whitespace and underscores, and accepts the Docker-friendly aliases used by the
	 * release bundles.
	 * @param value the raw role name; {@code null} or blank selects {@link #FULL}
	 * @return the matching role
	 * @throws IllegalArgumentException when the value is not a known role
	 */
	public static ServerRole from(String value) {
		if (value == null || value.isBlank()) {
			return FULL;
		}
		String normalized = value.trim().toLowerCase(Locale.ROOT).replace('_', '-');
		return switch (normalized) {
			case "full", "full-server", "server", "all" -> FULL;
			case "site", "server-site", "http", "http-server", "web" -> SITE;
			case "websocket", "websocket-server", "server-websocket", "ws", "ws-server" -> WEBSOCKET;
			default -> throw new IllegalArgumentException(
					"Unsupported " + ENVIRONMENT_VARIABLE + " value: " + value + " (expected full, site or websocket)");
		};
	}

}

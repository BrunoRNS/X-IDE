/**
 * Client side of the X-IDE WebSocket connection: the connection singleton ({@link
 * org.xide.client.sockets.WebSocketClient}), the annotated endpoint that answers server pings and
 * records responses, and the CLI entry point used by {@code run-client} (org.xide.Main).
 *
 * <p>Make scopes touching this module: {@code build-client}, {@code test-client}, {@code
 * test-integration}, {@code run-client}, {@code communication-test}.
 */
package org.xide.client.sockets;

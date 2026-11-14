/**
 * Server side of the X-IDE WebSocket connection: the Tyrus server wrapper,
 * the endpoint with the connected-session registry
 * ({@link org.xide.sockets.ClientSessions}) and the develop controllers
 * (ping, ping-clients push, last-pong).
 *
 * <p>Make scopes touching this module: {@code build-server}, {@code test-server},
 * {@code run-server}.</p>
 */
package org.xide.sockets;

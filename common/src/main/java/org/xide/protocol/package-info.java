/**
 * Shared socket protocol of the X-IDE common module: {@link org.xide.protocol.SocketMessage}
 * frames, the JSON codec, and the ping/pong rule (a frame whose payload is {@code pong} is a
 * response and is never dispatched again).
 *
 * <p>Make scopes touching this module: {@code build-common}, {@code test-common}.
 */
package org.xide.protocol;

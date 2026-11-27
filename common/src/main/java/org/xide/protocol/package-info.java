/**
 * Shared socket protocol of the X-IDE common module:
 * {@link org.xide.protocol.SocketMessage} frames, the JSON codec, and the ping/pong rule
 * (a frame whose payload is {@code pong} is a response and is never dispatched again).
 *
 * <p>
 * Make scopes touching this module: {@code build-common}, {@code test-common}.
 *
 * @author LuisOtavio13
 * @since v1.0.0
 */
package org.xide.protocol;

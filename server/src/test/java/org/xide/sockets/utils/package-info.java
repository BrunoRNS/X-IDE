/**
 * Unit tests for the WebSocket router of the server module.
 *
 * <p>
 * Tests the {@link org.xide.sockets.WebSocketRouter} in isolation by
 * registering test controllers and sending frames directly to the router,
 * without a real network connection.
 * </p>
 *
 * <h2>Test Class</h2>
 * <ul>
 *   <li>{@link org.xide.sockets.utils.WebSocketRouterTest} — Verifies frame
 *       routing, controller method invocation, parameter binding, and error handling.</li>
 * </ul>
 *
 * <h2>Run</h2>
 * <p>
 * Make scope: {@code test-server}.
 * </p>
 *
 * @version 1.0.0beta
 * @author LuisOtavio
 * @see org.xide.sockets.WebSocketRouter
 */
package org.xide.sockets.utils;
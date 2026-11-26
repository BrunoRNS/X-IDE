/**
 * Base package for WebSocket frame controllers.
 *
 * <p>
 * Controllers in this package (and sub-packages) handle incoming WebSocket
 * frames after authentication. They are annotated with
 * {@link org.xide.sockets.annotations.WebSocketRouter} and contain methods
 * annotated with {@link org.xide.sockets.annotations.OnMessage}.
 * </p>
 *
 * <h2>Sub-packages by Domain</h2>
 * <ul>
 *   <li>{@link org.xide.sockets.controllers.auth} — Authentication frames
 *       (login, token validation).</li>
 *   <li>{@link org.xide.sockets.controllers.competition} — Competition
 *       real-time events (scoreboard updates, timer, balloon pops).</li>
 *   <li>{@link org.xide.sockets.controllers.submission} — Code submission,
 *       compilation, execution, and result streaming.</li>
 *   <li>{@link org.xide.sockets.controllers.develop} — Development/debug
 *       endpoints (code execution sandbox, REPL).</li>
 * </ul>
 *
 * <h2>Controller Conventions</h2>
 * <ul>
 *   <li>Stateless — no instance fields holding request state</li>
 *   <li>Access user/session via {@link org.xide.sockets.ClientSessions}</li>
 *   <li>Send responses via {@code session.getAsyncRemote().sendText(...)}</li>
 *   <li>Throw runtime exceptions for protocol errors; router logs and closes session</li>
 * </ul>
 *
 * @version 1.0.0beta
 * @author LuisOtavio
 * @see org.xide.sockets.WebSocketRouter
 * @see org.xide.sockets.annotations.WebSocketRouter
 */
package org.xide.sockets.controllers;
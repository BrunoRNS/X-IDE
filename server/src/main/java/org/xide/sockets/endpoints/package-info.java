/**
 * Tyrus WebSocket endpoint implementation.
 *
 * <p>
 * Contains the {@link org.xide.sockets.endpoints.WebSocketEndpoint} class,
 * which extends Jakarta WebSocket's {@code Endpoint} to handle the low-level
 * connection lifecycle: {@code onOpen}, {@code onClose}, {@code onError}.
 * </p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Create a {@code Session} wrapper for each connection</li>
 *   <li>Register the session with {@link org.xide.sockets.ClientSessions}</li>
 *   <li>Decode incoming JSON frames to {@link org.xide.protocol.SocketMessage}</li>
 *   <li>Dispatch frames to {@link org.xide.sockets.WebSocketRouter}</li>
 *   <li>Clean up on close/error</li>
 * </ul>
 *
 * <h2>Threading</h2>
 * <p>
 * Tyrus calls endpoint methods on its own threads. The endpoint delegates
 * to the router, which executes controller handlers. Controllers must be
 * thread-safe (they are stateless).
 * </p>
 *
 * @version 1.0.0beta
 * @author LuisOtavio
 * @see org.xide.sockets.WebSocketServer
 * @see org.xide.sockets.WebSocketRouter
 */
package org.xide.sockets.endpoints;
/**
 * WebSocket server implementation using Tyrus (Jakarta WebSocket).
 *
 * <p>
 * This package contains the core WebSocket infrastructure:
 * the server singleton, the endpoint that accepts connections,
 * the router that dispatches frames to controllers, and the
 * annotation-based controller registration mechanism.
 * </p>
 *
 * <h2>Key Classes</h2>
 * <ul>
 *   <li>{@link org.xide.sockets.WebSocketServer} — Singleton server lifecycle
 *       (start/stop), runs on 127.0.0.1:8082, path {@code /ws}.</li>
 *   <li>{@link org.xide.sockets.WebSocketEndpoint} — Tyrus {@code Endpoint}
 *       implementation; creates per-connection sessions.</li>
 *   <li>{@link org.xide.sockets.WebSocketRouter} — Maps frame {@code type} to
 *       controller methods annotated with {@link org.xide.sockets.annotations.WebSocketRouter}.</li>
 *   <li>{@link org.xide.sockets.ClientSessions} — Tracks connected client sessions
 *       (user ID → session map) for targeted messaging.</li>
 * </ul>
 *
 * <h2>Annotations</h2>
 * <ul>
 *   <li>{@link org.xide.sockets.annotations.WebSocketRouter} — Marks a class
 *       as a WebSocket controller.</li>
 *   <li>{@link org.xide.sockets.annotations.OnMessage} — Maps a frame {@code type}
 *       to a handler method.</li>
 * </ul>
 *
 * <h2>Protocol</h2>
 * <p>
 * Frames are JSON objects defined in {@link org.xide.protocol.SocketMessage}.
 * The first frame must be {@code AUTH}; subsequent frames are routed by {@code type}.
 * Ping/pong uses a special {@code pong} payload (handled by the client).
 * </p>
 *
 * <h2>SSL/TLS</h2>
 * <p>
 * The raw WebSocket server runs unencrypted on localhost. In production, Nginx
 * terminates TLS (WSS) on port 8443 and proxies to 127.0.0.1:8082.
 * </p>
 *
 * @version 1.0.0beta
 * @author LuisOtavio
 * @see org.xide.sockets.controllers
 * @see org.xide.sockets.endpoints
 * @see org.xide.protocol
 */
package org.xide.sockets;
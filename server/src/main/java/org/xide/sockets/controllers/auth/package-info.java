/**
 * WebSocket authentication controllers.
 *
 * <p>
 * Handles the initial authentication handshake and token validation.
 * Every WebSocket connection must send an {@code AUTH} frame as its first
 * message; this package processes it and associates the session with a user.
 * </p>
 *
 * <h2>Frame Types</h2>
 * <ul>
 *   <li>{@code AUTH} — Payload: {@code { "token": "..." }} or {@code { "username": "...", "password": "..." }}</li>
 *   <li>{@code AUTH_RESULT} — Server responds with success/failure and user info</li>
 * </ul>
 *
 * <h2>Classes</h2>
 * <ul>
 *   <li>{@link org.xide.sockets.controllers.auth.LoginSocketController} —
 *       Processes {@code AUTH} frames, validates credentials/tokens against
 *       {@link org.xide.repository.UserRepo}, registers session in
 *       {@link org.xide.sockets.ClientSessions}.</li>
 * </ul>
 *
 * <h2>Security</h2>
 * <ul>
 *   <li>Tokens are JWTs (HS256) signed with a secret from environment</li>
 *   <li>Passwords are bcrypt-hashed in the database</li>
 *   <li>Failed auth closes the WebSocket connection immediately</li>
 * </ul>
 *
 * @version 1.0.0beta
 * @author LuisOtavio
 * @see org.xide.sockets.controllers
 * @see org.xide.repository.UserRepo
 * @see org.xide.sockets.ClientSessions
 */
package org.xide.sockets.controllers.auth;
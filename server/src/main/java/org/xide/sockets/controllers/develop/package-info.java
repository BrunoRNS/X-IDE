/**
 * WebSocket controllers for development/debug features.
 *
 * <p>
 * Provides a sandboxed code execution endpoint for the "Develop" mode
 * of the client IDE. Students can run arbitrary code snippets against
 * custom input without submitting to a problem.
 * </p>
 *
 * <h2>Frame Types</h2>
 * <ul>
 *   <li>{@code RUN_CODE} — Payload: {@code { "code": "...", "language": "CPP", "input": "..." }}</li>
 *   <li>{@code RUN_RESULT} — Server responds with stdout, stderr, exit code, time, memory</li>
 * </ul>
 *
 * <h2>Classes</h2>
 * <ul>
 *   <li>{@link org.xide.sockets.controllers.develop.DevelopSocketController} —
 *       Handles {@code RUN_CODE}, executes in isolated container/sandbox,
 *       returns {@code RUN_RESULT}.</li>
 * </ul>
 *
 * <h2>Safety</h2>
 * <ul>
 *   <li>Execution runs in a restricted container (no network, limited CPU/memory/time)</li>
 *   <li>No access to judge test cases or problem internals</li>
 *   <li>Rate-limited per user to prevent abuse</li>
 * </ul>
 *
 * @version 1.0.0beta
 * @author LuisOtavio
 * @see org.xide.sockets.controllers
 */
package org.xide.sockets.controllers.develop;
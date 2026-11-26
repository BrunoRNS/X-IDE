/**
 * X-IDE Server — Competitive Programming Platform Backend.
 *
 * <p>
 * This is the main server module of X-IDE, a platform for hosting programming competitions
 * and classroom exercises. It provides both an HTTP API (Javalin, port 8081) and a
 * WebSocket server (Tyrus, port 8082) for real-time interaction.
 * </p>
 *
 * <h2>Architecture</h2>
 * <ul>
 *   <li><b>HTTP layer</b> — {@link org.xide.http.routers.Routers} registers REST endpoints
 *       for classrooms, competitions, problems, and student management.</li>
 *   <li><b>WebSocket layer</b> — {@link org.xide.sockets.WebSocketServer} runs on
 *       {@code /ws/socket} and routes frames to controllers annotated with
 *       {@link org.xide.sockets.annotations.WebSocketRouter}.</li>
 *   <li><b>Persistence</b> — MySQL via {@link org.xide.repository.Connection}; schema
 *       initialized by {@link org.xide.repository.InitDatabase}.</li>
 *   <li><b>Templating</b> — Thymeleaf (classpath or filesystem with hot-reload via
 *       {@code XIDE_DEV_RESOURCES}).</li>
 * </ul>
 *
 * <h2>Key Packages</h2>
 * <table border="1" cellpadding="3" summary="Package overview">
 *   <tr><th>Package</th><th>Purpose</th></tr>
 *   <tr><td>{@link org.xide.http.controllers}</td><td>HTTP request handlers (classrooms, competitions, problems, students)</td></tr>
 *   <tr><td>{@link org.xide.http.service}</td><td>Business logic for problem management, competition flow, PDF generation</td></tr>
 *   <tr><td>{@link org.xide.http.dto}</td><td>Request/response data transfer objects</td></tr>
 *   <tr><td>{@link org.xide.sockets.controllers}</td><td>WebSocket frame handlers (auth, competition, submission, develop)</td></tr>
 *   <tr><td>{@link org.xide.sockets.endpoints}</td><td>Tyrus {@link jakarta.websocket.Endpoint} implementation</td></tr>
 *   <tr><td>{@link org.xide.models}</td><td>Domain entities (User, Problem, Submission, Competition, etc.)</td></tr>
 *   <tr><td>{@link org.xide.repository}</td><td>JDBC repositories for users, problems, submissions</td></tr>
 *   <tr><td>{@link org.xide.utils}</td><td>Environment config ({@link org.xide.utils.EnvRead}), SQL helpers</td></tr>
 * </table>
 *
 * <h2>Startup</h2>
 * <p>
 * Entry point: {@link org.xide.Main#main(String[])}. It:
 * </p>
 * <ol>
 *   <li>Starts the WebSocket server on 127.0.0.1:8082</li>
 *   <li>Starts the Javalin HTTP server on 0.0.0.0:8081</li>
 *   <li>Initializes the database schema</li>
 *   <li>Registers a shutdown hook to stop the WebSocket server cleanly</li>
 * </ol>
 *
 * <h2>Configuration</h2>
 * <p>All settings come from environment variables (or {@code .env} via {@link org.xide.utils.EnvRead}):</p>
 * <ul>
 *   <li>{@code DB_HOST}, {@code DB_PORT}, {@code DB_NAME}, {@code DB_USER}, {@code DB_PASSWORD}</li>
 *   <li>{@code TLS_HOST} — public hostname/IP for student WSS connections</li>
 *   <li>{@code HTTPS_PORT} — public WSS port (default 8443)</li>
 *   <li>{@code XIDE_DEV_RESOURCES} — path to {@code server/src/main/resources} for hot-reload</li>
 * </ul>
 *
 * <h2>WebSocket Protocol</h2>
 * <p>
 * Frames are JSON objects defined in the common module ({@code org.xide.protocol}).
 * The server expects an initial authentication frame, then routes subsequent frames to
 * the appropriate controller based on the {@code type} field.
 * </p>
 *
 * @version 1.0.0beta
 * @author LuisOtavio
 * @see org.xide.Main
 * @see org.xide.sockets.WebSocketServer
 * @see org.xide.http.routers.Routers
 */
package org.xide;
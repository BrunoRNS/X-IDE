/**
 * HTTP layer of the X-IDE server: REST API, templated pages, and WebSocket upgrade.
 *
 * <p>
 * This package and its sub-packages implement the entire HTTP surface of the server,
 * running on port 8081 via Javalin. It serves both machine-to-machine API endpoints
 * (JSON) and server-rendered HTML pages (Thymeleaf) for the instructor dashboard.
 * </p>
 *
 * <h2>Sub-packages</h2>
 * <ul>
 *   <li>{@link org.xide.http.controllers} — Request handlers for each resource.</li>
 *   <li>{@link org.xide.http.service} — Business logic, validation, PDF generation.</li>
 *   <li>{@link org.xide.http.dto} — Request/response data transfer objects.</li>
 *   <li>{@link org.xide.http.routers} — Central route registration ({@link org.xide.http.routers.Routers}).</li>
 *   <li>{@link org.xide.http.utils} — Template helpers (HTML sanitization, etc.).</li>
 * </ul>
 *
 * <h2>Key Endpoints</h2>
 * <table border="1" cellpadding="3" summary="HTTP endpoints overview">
 *   <tr><th>Method</th><th>Path</th><th>Controller</th><th>Description</th></tr>
 *   <tr><td>GET</td><td>/</td><td>{@link org.xide.http.controllers.HelloController}</td><td>Landing page</td></tr>
 *   <tr><td>GET</td><td>/students</td><td>{@link org.xide.http.controllers.StudentsController}</td><td>List students</td></tr>
 *   <tr><td>POST</td><td>/students/register</td><td>{@link org.xide.http.controllers.StudentsController}</td><td>Register student</td></tr>
 *   <tr><td>GET</td><td>/classroom</td><td>{@link org.xide.http.controllers.ClassController}</td><td>Classroom dashboard</td></tr>
 *   <tr><td>GET/POST</td><td>/class/{id}</td><td>{@link org.xide.http.controllers.ClassController}</td><td>Class detail / join</td></tr>
 *   <tr><td>GET</td><td>/competition</td><td>{@link org.xide.http.controllers.CompetitionController}</td><td>Competition list</td></tr>
 *   <tr><td>POST</td><td>/competition/start</td><td>{@link org.xide.http.controllers.CompetitionController}</td><td>Start competition</td></tr>
 *   <tr><td>DELETE</td><td>/competition/stop</td><td>{@link org.xide.http.controllers.CompetitionController}</td><td>Stop competition</td></tr>
 *   <tr><td>POST</td><td>/problens/new</td><td>{@link org.xide.http.controllers.ProblensController}</td><td>Create problem</td></tr>
 *   <tr><td>GET</td><td>/problens/all</td><td>{@link org.xide.http.controllers.ProblensController}</td><td>List all problems</td></tr>
 * </table>
 *
 * <h2>Templating</h2>
 * <p>
 * Thymeleaf templates live in {@code src/main/resources/templates}. In development
 * mode ({@code XIDE_DEV_RESOURCES} set), templates are hot-reloaded from the filesystem.
 * </p>
 *
 * @version 1.0.0beta
 * @author LuisOtavio
 * @see org.xide.Main
 * @see org.xide.http.routers.Routers
 */
package org.xide.http;
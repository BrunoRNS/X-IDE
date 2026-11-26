/**
 * Business logic services for the HTTP layer.
 *
 * <p>
 * Services contain the core application logic invoked by HTTP controllers.
 * They orchestrate repositories, enforce validation rules, generate PDFs,
 * manage competition state, and handle file uploads.
 * </p>
 *
 * <h2>Services</h2>
 * <ul>
 *   <li>{@link org.xide.http.service.ProblemManagementService} — CRUD for problems,
 *       parsing multipart form data (PDF, examples, test cases), PDF generation.</li>
 *   <li>{@link org.xide.http.service.CompetitionService} — Competition lifecycle:
 *       start/stop, scoreboard computation, balloon assignment.</li>
 *   <li>{@link org.xide.http.service.ClassService} — Classroom management:
 *       create, list, student enrollment, PDF roster export.</li>
 *   <li>{@link org.xide.http.service.ProblensService} — Legacy problem service
 *       (being migrated to {@code ProblemManagementService}).</li>
 *   <li>{@link org.xide.http.service.LoadPdfUsers} — Bulk student import from PDF.</li>
 * </ul>
 *
 * <h2>Validation Sub-package</h2>
 * <p>
 * {@link org.xide.http.service.validate} contains validators used by services
 * to ensure data integrity before persistence.
 * </p>
 *
 * <h2>Transaction Handling</h2>
 * <p>
 * Services obtain a {@code java.sql.Connection} from
 * {@link org.xide.repository.Connection} and manage transactions manually
 * (commit/rollback). No declarative transaction framework is used.
 * </p>
 *
 * @version 1.0.0beta
 * @author LuisOtavio
 * @see org.xide.http.controllers
 * @see org.xide.repository
 */
package org.xide.http.service;
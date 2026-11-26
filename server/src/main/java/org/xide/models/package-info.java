/**
 * Domain entities for the X-IDE platform.
 *
 * <p>
 * This package contains the core data models that represent the main concepts
 * of the competitive programming platform. All entities are plain Java classes
 * (POJOs) used across HTTP controllers, WebSocket handlers, repositories,
 * and the database schema.
 * </p>
 *
 * <h2>Entities</h2>
 * <ul>
 *   <li>{@link org.xide.models.User} — Platform user (student or instructor), with
 *       roles, classroom membership, and authentication fields.</li>
 *   <li>{@link org.xide.models.Problem} — Programming problem statement, including
 *       description, examples, constraints, and test cases.</li>
 *   <li>{@link org.xide.models.Submission} — A student's code submission for a problem,
 *       linked to a competition, with status, language, and timing.</li>
 *   <li>{@link org.xide.models.Soluction} — Legacy/alternative naming for a solution
 *       (kept for compatibility with existing database records).</li>
 *   <li>{@link org.xide.models.Example} — Input/output example pair for a problem.</li>
 *   <li>{@link org.xide.models.Code} — Source code content associated with a submission.</li>
 *   <li>{@link org.xide.models.Competition} — Competition aggregate (defined in states).</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <p>
 * Entities are instantiated by repositories when reading from MySQL, passed to
 * service layers for business logic, serialized to JSON for HTTP responses, and
 * sent over WebSocket frames to clients.
 * </p>
 *
 * @version 1.0.0beta
 * @author LuisOtavio
 * @see org.xide.repository
 * @see org.xide.http.service
 * @see org.xide.sockets.controllers
 */
package org.xide.models;
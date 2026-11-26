/**
 * Input validation utilities for HTTP service layer.
 *
 * <p>
 * Contains validators that enforce business rules on incoming data before
 * it reaches the database. Validators are stateless and throw
 * {@link IllegalArgumentException} (or custom exceptions) on failure.
 * </p>
 *
 * <h2>Validators</h2>
 * <ul>
 *   <li>{@link org.xide.http.service.validate.ProblensValidade} — Validates
 *       problem creation/update payloads: required fields, PDF presence,
 *       example/test case consistency, constraint ranges.</li>
 * </ul>
 *
 * <h2>Usage</h2>
 * <p>
 * Called from {@link org.xide.http.service.ProblemManagementService} before
 * persisting a problem. Controllers should not invoke validators directly;
 * services are the single entry point for business logic.
 * </p>
 *
 * @version 1.0.0beta
 * @author LuisOtavio
 * @see org.xide.http.service.ProblemManagementService
 */
package org.xide.http.service.validate;
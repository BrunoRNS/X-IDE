/**
 * Shared utilities for the server module.
 *
 * <p>
 * Contains cross-cutting helpers used by HTTP controllers, WebSocket handlers,
 * repositories, and services.
 * </p>
 *
 * <h2>Classes</h2>
 * <ul>
 *   <li>{@link org.xide.utils.EnvRead} — Loads {@code .env} file (if present) and
 *       provides typed access to environment variables with defaults.</li>
 *   <li>{@link org.xide.utils.SqlUtil} — JDBC helpers: {@code executeQuery},
 *       {@code executeUpdate}, {@code closeQuietly}, result set mappers.</li>
 * </ul>
 *
 * <h2>Configuration</h2>
 * <p>
 * All server configuration is via environment variables (see {@link org.xide.utils.EnvRead}).
 * The {@code .env} file is loaded from the working directory at startup.
 * </p>
 *
 * @version 1.0.0beta
 * @author LuisOtavio
 * @see org.xide.repository.Connection
 */
package org.xide.utils;
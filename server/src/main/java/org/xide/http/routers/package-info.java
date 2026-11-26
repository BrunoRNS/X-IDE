/**
 * Central route registration for the Javalin HTTP server.
 *
 * <p>
 * This package contains a single class, {@link org.xide.http.routers.Routers},
 * which registers all HTTP routes by delegating to controller methods.
 * Keeping routes in one place makes the API surface easy to audit.
 * </p>
 *
 * <h2>Route Groups</h2>
 * <ul>
 *   <li>Root: {@code /} → {@link org.xide.http.controllers.HelloController}</li>
 *   <li>Students: {@code /students*}</li>
 *   <li>Classrooms: {@code /class*}, {@code /classroom}</li>
 *   <li>Competitions: {@code /competition*}</li>
 *   <li>Problems (Problens): {@code /problens*}, {@code /problems*}</li>
 * </ul>
 *
 * <h2>Static Files</h2>
 * <p>
 * Static assets under {@code /public} are served by Javalin's static file handler
 * (configured in {@link org.xide.Main}). In dev mode, they are served from the
 * filesystem for hot-reload.
 * </p>
 *
 * @version 1.0.0beta
 * @author LuisOtavio
 * @see org.xide.http.controllers
 * @see org.xide.Main
 */
package org.xide.http.routers;
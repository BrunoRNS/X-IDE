/**
 * HTTP request handlers (controllers) for the Javalin server.
 *
 * <p>
 * Controllers are stateless classes with static handler methods that receive
 * a Javalin {@code Context} and produce a response (HTML, JSON, redirect).
 * They delegate all business logic to services ({@link org.xide.http.service}).
 * </p>
 *
 * <h2>Controllers</h2>
 * <ul>
 *   <li>{@link org.xide.http.controllers.HelloController} — Landing page ({@code /}).</li>
 *   <li>{@link org.xide.http.controllers.StudentsController} — Student CRUD,
 *       PDF import ({@code /students*}, {@code /pdf-users}).</li>
 *   <li>{@link org.xide.http.controllers.ClassController} — Classroom pages
 *       ({@code /classroom}, {@code /class*}).</li>
 *   <li>{@link org.xide.http.controllers.CompetitionController} — Competition
 *       list, start, stop ({@code /competition*}).</li>
 *   <li>{@link org.xide.http.controllers.ProblensController} — Problem
 *       management API ({@code /problens*}, {@code /problems*}).</li>
 * </ul>
 *
 * <h2>Conventions</h2>
 * <ul>
 *   <li>Method names: {@code index} (list), {@code show} (detail), {@code create} (form),
 *       {@code store} (POST), {@code edit} (edit form), {@code update} (PUT),
 *       {@code destroy} (DELETE).</li>
 *   <li>Return HTML for browser routes, JSON for API routes.</li>
 *   <li>Error handling: set status code, render error template or JSON error object.</li>
 * </ul>
 *
 * @version 1.0.0beta
 * @author LuisOtavio
 * @see org.xide.http.service
 * @see org.xide.http.routers.Routers
 */
package org.xide.http.controllers;
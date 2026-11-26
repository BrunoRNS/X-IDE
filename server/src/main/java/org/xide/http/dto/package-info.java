/**
 * Data Transfer Objects for the HTTP API.
 *
 * <p>
 * DTOs decouple the external API contract from internal domain models
 * ({@link org.xide.models}). They are used for:
 * </p>
 * <ul>
 *   <li>JSON request body binding (Javalin {@code ctx.bodyAsClass()})</li>
 *   <li>JSON response serialization</li>
 *   <li>Form data binding ({@code application/x-www-form-urlencoded})</li>
 * </ul>
 *
 * <h2>Classes</h2>
 * <ul>
 *   <li>{@link org.xide.http.dto.ProblemDtos} — Problem creation/update payloads,
 *       including nested example and test case DTOs.</li>
 * </ul>
 *
 * <h2>Design Principles</h2>
 * <ul>
 *   <li>No business logic — pure data carriers</li>
 *   <li>Validation annotations where applicable (JSR-303)</li>
 *   <li>Immutable where possible (final fields, no setters)</li>
 *   <li>Jackson-friendly (default constructor + getters/setters or records)</li>
 * </ul>
 *
 * @version 1.0.0beta
 * @author LuisOtavio
 * @see org.xide.http.controllers
 * @see org.xide.http.service
 */
package org.xide.http.dto;
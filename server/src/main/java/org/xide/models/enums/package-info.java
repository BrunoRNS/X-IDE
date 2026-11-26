/**
 * Enumeration types used across the X-IDE domain model.
 *
 * <p>
 * Centralizes all {@code enum} definitions to avoid scattering constants
 * across entity classes. Enums are used for database columns (via JDBC
 * {@code getString/setString}), JSON serialization, and WebSocket protocol
 * message fields.
 * </p>
 *
 * <h2>Enums</h2>
 * <ul>
 *   <li>{@link org.xide.models.enums.SubmissionState} — Lifecycle of a submission:
 *       {@code PENDING}, {@code RUNNING}, {@code ACCEPTED}, {@code WRONG_ANSWER},
 *       {@code TIME_LIMIT_EXCEEDED}, {@code MEMORY_LIMIT_EXCEEDED}, {@code RUNTIME_ERROR},
 *       {@code COMPILATION_ERROR}, {@code INTERNAL_ERROR}.</li>
 *   <li>{@link org.xide.models.enums.Lang} — Supported programming languages:
 *       {@code C}, {@code CPP}, {@code JAVA}, {@code PYTHON}, {@code JAVASCRIPT},
 *       {@code GO}, {@code RUST}, etc. Maps to compiler commands and file extensions.</li>
 *   <li>{@link org.xide.models.enums.BigO} — Algorithmic complexity categories for
 *       problem constraints: {@code O_1}, {@code O_LOG_N}, {@code O_N}, {@code O_N_LOG_N},
 *       {@code O_N2}, {@code O_N3}, {@code O_2N}, {@code O_FACTORIAL}.</li>
 *   <li>{@link org.xide.models.enums.BalloonColor} — ICPC-style balloon colors for
 *       problem identification during contests: {@code RED}, {@code BLUE}, {@code GREEN},
 *       {@code YELLOW}, {@code PURPLE}, {@code ORANGE}, {@code PINK}, {@code BLACK},
 *       {@code WHITE}, {@code BROWN}.</li>
 * </ul>
 *
 * @version 1.0.0beta
 * @author LuisOtavio
 * @see org.xide.models
 */
package org.xide.models.enums;
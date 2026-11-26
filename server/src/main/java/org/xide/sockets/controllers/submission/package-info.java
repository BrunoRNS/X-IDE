/**
 * WebSocket controllers for code submission handling.
 *
 * <p>
 * Handles the full submission lifecycle over WebSocket:
 * receive source code, enqueue for compilation/execution, stream
 * intermediate results, and deliver final verdict.
 * </p>
 *
 * <h2>Frame Types</h2>
 * <ul>
 *   <li>{@code SUBMIT} — Client sends source code + problem ID + language</li>
 *   <li>{@code SUBMISSION_STATUS} — Server pushes status updates (queue, compile, run)</li>
 *   <li>{@code SUBMISSION_RESULT} — Final verdict with output, time, memory</li>
 * </ul>
 *
 * <h2>Classes</h2>
 * <ul>
 *   <li>{@link org.xide.sockets.controllers.submission.SubmissionController} —
 *       Main handler for submission frames.</li>
 * </ul>
 *
 * <h2>Integration</h2>
 * <p>
 * Delegates to {@link org.xide.repository.SubmissionRepository} for persistence
 * and to an external judge worker (not in this codebase) for actual compilation
 * and execution. The controller manages the WebSocket-side protocol only.
 * </p>
 *
 * @version 1.0.0beta
 * @author LuisOtavio
 * @see org.xide.sockets.controllers
 * @see org.xide.repository.SubmissionRepository
 */
package org.xide.sockets.controllers.submission;
/**
 * Database repositories (JDBC) for the X-IDE server.
 *
 * <p>
 * Repositories encapsulate all SQL queries and provide a clean interface
 * for services and controllers. Each repository class corresponds to a
 * database table (or closely related tables).
 * </p>
 *
 * <h2>Repositories</h2>
 * <ul>
 *   <li>{@link org.xide.repository.UserRepo} — User CRUD, authentication,
 *       classroom membership, role management.</li>
 *   <li>{@link org.xide.repository.ProblemRepository} — Problem CRUD,
 *       search, test case management, PDF storage.</li>
 *   <li>{@link org.xide.repository.SubmissionRepository} — Submission CRUD,
 *       status updates, scoreboard queries.</li>
 * </ul>
 *
 * <h2>Connection Management</h2>
 * <p>
 * Repositories obtain a {@code java.sql.Connection} from
 * {@link org.xide.repository.Connection}, which manages a single pooled
 * connection (suitable for low-concurrency educational use). For higher
 * concurrency, replace with HikariCP.
 * </p>
 *
 * <h2>Schema Initialization</h2>
 * <p>
 * {@link org.xide.repository.InitDatabase} creates tables on first run.
 * It is invoked from {@link org.xide.Main} at startup.
 * </p>
 *
 * <h2>Testing</h2>
 * <p>
 * Mutation tests exist for {@code UserRepo} (see {@code UserRepoMutationTest}).
 * Integration tests use a real MySQL container.
 * </p>
 *
 * @version 1.0.0beta
 * @author LuisOtavio
 * @see org.xide.utils.SqlUtil
 * @see org.xide.repository.Connection
 * @see org.xide.repository.InitDatabase
 */
package org.xide.repository;
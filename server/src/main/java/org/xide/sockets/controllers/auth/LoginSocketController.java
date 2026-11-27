package org.xide.sockets.controllers.auth;

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.websocket.Session;
import org.xide.models.User;
import org.xide.repository.UserRepo;
import org.xide.sockets.ClientSessions;
import org.xide.sockets.annotations.SocketParam;
import org.xide.sockets.annotations.WebSocketController;
import org.xide.sockets.annotations.WebSocketRoute;

/**
 * WebSocket controller for authentication. {@code /auth/login} checks credentials via
 * {@link org.xide.repository.UserRepo} (pluggable {@link UserLookup}), registers the
 * login session, and returns a {@link Dto} status; {@code /auth/logout} (protected)
 * unregisters and returns status. (Spelling kept from the original code.)
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
@WebSocketController
public class LoginSocketController {

	private static final Logger LOGGER = Logger.getLogger(LoginSocketController.class.getName());

	/**
	 * Response payload carrying the status string ("sucess"/"invalid").
	 *
	 * @since v1.0.0
	 * @author LuisOtavio13
	 */
	public static record Dto(String status) {
	}

	/** Finds users for the login check; tests swap it out to skip the database. */
	UserLookup userLookup = UserRepo::getUserByEmailAndPassword;

	/**
	 * Functional interface looking up a {@link org.xide.models.User} by email and
	 * password; defaults to
	 * {@link org.xide.repository.UserRepo#getUserByEmailAndPassword(String, String)} and
	 * is swappable in tests.
	 *
	 * @since v1.0.0
	 * @author LuisOtavio13
	 */
	interface UserLookup {

		User find(String email, String password) throws Exception;

	}

	@WebSocketRoute(value = "/auth/logout", protectedRoute = true)
	public Dto logout(Session session) {
		if (session != null) {
			User user = ClientSessions.getUser(session);
			ClientSessions.unregisterLogin(session);
			LOGGER.log(Level.INFO, "Logout success {0}", user.getName());
			return new Dto("sucess");
		}
		LOGGER.info("Logout success: user not found");
		return new Dto("sucess");
	}

	@WebSocketRoute("/auth/login")
	public Dto login(Session session, @SocketParam("email") String email, @SocketParam("password") String password) {
		try {
			User user = userLookup.find(email, password);
			if (user == null) {
				LOGGER.info("Not found user, or email is invalid or password");
				return new Dto("invalid");
			}
			if (session != null) {
				ClientSessions.registerLogin(user, session);
			}
			LOGGER.info("Login success");
			return new Dto("sucess");
		}
		catch (Exception e) {
			LOGGER.log(Level.SEVERE, "No possible execute query for select user", e);
			throw new IllegalStateException("Login failed: could not check the user", e);
		}
	}

}

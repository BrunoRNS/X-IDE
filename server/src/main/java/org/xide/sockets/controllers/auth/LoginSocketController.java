package org.xide.sockets.controllers.auth;

import jakarta.websocket.Session;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xide.models.User;
import org.xide.repository.UserRepo;
import org.xide.sockets.annotations.SocketParam;
import org.xide.sockets.annotations.WebSocketController;
import org.xide.sockets.annotations.WebSocketRoute;
import org.xide.sockets.ClientSessions;

@WebSocketController
public class LoginSocketController {

	private static final Logger LOGGER = Logger.getLogger(LoginSocketController.class.getName());

	public static record Dto(String status) {
	}

	/** Finds users for the login check; tests swap it out to skip the database. */
	UserLookup userLookup = UserRepo::getUserByEmailAndPassword;

	interface UserLookup {

		User find(String email, String password) throws Exception;

	}

	@WebSocketRoute(value = "/auth/logout", protectedRoute = true)
	public Dto logout(Session session) {
		if (session != null) {
			User user = ClientSessions.getUser(session);
			ClientSessions.unregisterLogin(session);
			LOGGER.info("Logout success " + user.getName());
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

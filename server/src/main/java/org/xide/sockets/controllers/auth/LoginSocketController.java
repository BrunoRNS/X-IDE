package org.xide.sockets.controllers.auth;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.xide.models.User;
import org.xide.repository.UserRepo;
import org.xide.sockets.annotations.SocketParam;
import org.xide.sockets.annotations.WebSocketController;
import org.xide.sockets.annotations.WebSocketRoute;

@WebSocketController
public class LoginSocketController {
    private static final Logger LOGGER = Logger.getLogger(LoginSocketController.class.getName());

    public static record Dto(String status) {}

    /** Finds users for the login check; tests swap it out to skip the database. */
    UserLookup userLookup = UserRepo::getUser;

    interface UserLookup {
        User find(String email, String password) throws Exception;
    }

    /**
     * The router wraps the returned record into the response payload, so the client receives
     * {"status": "sucess"} on this same route.
     */
    @WebSocketRoute("/auth/login")
    public Dto login(@SocketParam("email") String email, @SocketParam("password") String password) {
        try {
            User user = userLookup.find(email, password);
            if (user == null) {
                LOGGER.info("Not found user, or email is invalid or password");
                return new Dto("invalid");
            }
            LOGGER.info("Login sucess");
            return new Dto("sucess");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "No possible execute query for select user", e);
            throw new IllegalStateException("Login failed: could not check the user", e);
        }
    }
}

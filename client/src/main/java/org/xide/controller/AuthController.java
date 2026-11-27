package org.xide.controller;

import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import org.xide.ExamIntegrityMonitor;
import org.xide.frame.holders.LoginFrameHolder;
import org.xide.frame.holders.MainFrameHolder;
import org.xide.sockets.annotations.SocketParam;
import org.xide.sockets.annotations.WebSocketController;
import org.xide.sockets.annotations.WebSocketRoute;

/**
 * Client-side WebSocket controller for the {@code /auth/login} response route. On a
 * {@code
 * sucess} status it disposes the login frame, opens the main frame, and starts
 * {@link org.xide.ExamIntegrityMonitor}; otherwise it shows an error message. Callbacks
 * are replaceable for tests.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
@WebSocketController
public class AuthController {

	/** Runs when the server confirms the login; tests swap it out. */
	Consumer<String> onSucess = status -> {
		System.out.println("Login sucess");
		SwingUtilities.invokeLater(() -> {
			LoginFrameHolder.disposeFrame();
			MainFrameHolder.startFrame();
			ExamIntegrityMonitor.start();
		});
	};

	/** Runs when the server rejects the login; tests swap it out. */
	Consumer<String> onRejected = status -> LoginFrameHolder
		.setMessage("Invalid email or password, or this user does not exist");

	@WebSocketRoute("/auth/login")
	public void login(@SocketParam("status") String status) {
		if ("sucess".equals(status)) {
			onSucess.accept(status);
		}
		else {
			onRejected.accept(status);
		}
	}

}

package org.xide.controller;

import java.util.function.Consumer;
import org.xide.sockets.annotations.SocketParam;
import org.xide.sockets.annotations.WebSocketController;
import org.xide.sockets.annotations.WebSocketRoute;
import org.xide.ui.LoginFrame;

@WebSocketController
public class AuthController {

	/** Runs when the server confirms the login; tests swap it out. */
	Consumer<String> onSucess = status -> {
		System.out.println("Login sucess");
		LoginFrame.disposeFrame();
	};

	/** Runs when the server rejects the login; tests swap it out. */
	Consumer<String> onRejected = status -> LoginFrame
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

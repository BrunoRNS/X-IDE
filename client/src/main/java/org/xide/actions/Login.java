package org.xide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.xide.client.sockets.WebSocketClient;
import org.xide.frame.holders.LoginFrameHolder;

/**
 * Swing action that submits the credentials entered in {@link org.xide.frame.LoginFrame}
 * by sending a {@code /auth/login} request to the server over the shared
 * {@link org.xide.client.sockets.WebSocketClient}.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
public class Login implements ActionListener {

	/**
	 * Payload sent with the {@code /auth/login} request, carrying the user credentials.
	 *
	 * @since v1.0.0
	 * @author LuisOtavio13
	 */
	public static record Dto(String email, String password) {
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String email = LoginFrameHolder.getEmail();
		String password = LoginFrameHolder.getPassword();
		try {
			WebSocketClient.getInstance().sendRequest("/auth/login", new Dto(email, password));
		}
		catch (Exception ea) {
			LoginFrameHolder.setMessage(ea.getMessage());
		}
	}

}

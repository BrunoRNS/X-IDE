package org.xide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.xide.client.sockets.WebSocketClient;
import org.xide.frame.holders.LoginFrameHolder;

public class Login implements ActionListener {

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

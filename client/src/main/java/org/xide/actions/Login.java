package org.xide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.xide.frame.LoginFrame;

public class Login implements ActionListener {

	public static record Dto(String email, String password) {
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String email = LoginFrame.getEmail();
		String password = LoginFrame.getPassword();
		try {
			org.xide.client.sockets.WebSocketClient.getInstance().sendRequest("/auth/login", new Dto(email, password));
		}
		catch (Exception ea) {
			ea.printStackTrace();
			LoginFrame.setMessage(ea.getMessage());
		}
	}

}

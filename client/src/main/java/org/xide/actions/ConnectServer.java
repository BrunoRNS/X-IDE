package org.xide.actions;

import java.awt.event.*;

import org.xide.frame.ConnectFrame;

public class ConnectServer implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		String ip = ConnectFrame.getTextField();

		if (ip == null || ip.isEmpty()) {
			ConnectFrame.setMessage("Please enter the server IP address.");
			return;
		}

		if (!ip.matches(
				"^(25[0-5]|2[0-4]\\d|1\\d\\d|[1]?\\d\\d?)\\.?(25[0-5]|2[0-4]\\d|1\\d\\d|[1]?\\d\\d?)\\.?(25[0-5]|2[0-4]\\d|1\\d\\d|[1]?\\d\\d?)\\.?(25[0-5]|2[0-4]\\d|1\\d\\d|[1]?\\d\\d?)$")) {
			ConnectFrame.setMessage("Invalid IP. Example: 192.168.1.50");
			return;
		}

		String endpoint = "wss://" + ip + ":8443/ws/socket";
		ConnectFrame.setEndpoint(endpoint);
		ConnectFrame.disposeFrame();
	}

}

package org.xide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.xide.frame.holders.ConnectFrameHolder;

public class ConnectServer implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		String ip = ConnectFrameHolder.getTextField();

		if (ip == null || ip.isEmpty()) {
			ConnectFrameHolder.setMessage("Please enter the server IP address.");
			return;
		}

		if (!ip.matches(
				"^(25[0-5]|2[0-4]\\d|1\\d\\d|[1]?\\d\\d?)\\.?(25[0-5]|2[0-4]\\d|1\\d\\d|[1]?\\d\\d?)\\.?(25[0-5]|2[0-4]\\d|1\\d\\d|[1]?\\d\\d?)\\.?(25[0-5]|2[0-4]\\d|1\\d\\d|[1]?\\d\\d?)$")) {
			ConnectFrameHolder.setMessage("Invalid IP. Example: 192.168.1.50");
			return;
		}

		String endpoint = "wss://" + ip + ":8443/ws/socket";
		ConnectFrameHolder.submitEndpoint(endpoint);
	}

}

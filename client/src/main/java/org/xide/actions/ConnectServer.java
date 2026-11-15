package org.xide.actions;

import javax.swing.*;

import org.xide.ui.LoginFrame;

import java.awt.event.*;

public class ConnectServer implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        String ip = LoginFrame.getTextField();

        if (ip == null || ip.isEmpty()) {
            LoginFrame.setMessage("Please enter the server IP address.");
            return;
        }

        if (!ip.matches("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d)$")) {
            LoginFrame.setMessage("Invalid IP. Example: 192.168.1.50");
            return;
        }

        String endpoint = "wss://" + ip + ":8443/ws/socket";
        LoginFrame.setEndpoint(endpoint);
        LoginFrame.disposeFrame();
    }

}

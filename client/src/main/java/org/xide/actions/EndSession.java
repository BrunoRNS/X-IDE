package org.xide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.xide.client.sockets.WebSocketClient;
import org.xide.ui.LoginFrame;
import org.xide.ui.MainFrame;

public class EndSession implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			WebSocketClient.getInstance().sendRequest("/auth/logout", "").get();
		}
		catch (Exception ea) {
			ea.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not end the session. Check the server connection.",
					"End session", JOptionPane.ERROR_MESSAGE);
			return;
		}
		MainFrame.disposeFrame();
		new LoginFrame();
	}

}

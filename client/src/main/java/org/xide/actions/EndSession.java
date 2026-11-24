package org.xide.actions;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.xide.ExamIntegrityMonitor;
import org.xide.client.sockets.WebSocketClient;
import org.xide.frame.LoginFrame;
import org.xide.frame.MainFrame;

public class EndSession implements ActionListener {

	private static final String END_LABEL = "End session";

	@Override
	public void actionPerformed(ActionEvent event) {
		JButton button = event.getSource() instanceof JButton source ? source : null;
		Window owner = button == null ? null : SwingUtilities.getWindowAncestor(button);

		int choice = JOptionPane.showConfirmDialog(owner, "End the exam session and return to the login screen?",
				"End session", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (choice != JOptionPane.YES_OPTION) {
			return;
		}

		if (button != null) {
			button.setEnabled(false);
			button.setText("Ending...");
		}

		Thread worker = new Thread(() -> endSession(button, owner), "end-session");
		worker.setDaemon(true);
		worker.start();
	}

	private static void endSession(JButton button, Window owner) {
		try {
			WebSocketClient.getInstance().sendRequest("/auth/logout", "").get(5, TimeUnit.SECONDS);
		}
		catch (Exception failure) {
			failure.printStackTrace();
			SwingUtilities.invokeLater(() -> {
				restore(button);
				JOptionPane.showMessageDialog(owner, "Could not end the session. Check the server connection.",
						"End session", JOptionPane.ERROR_MESSAGE);
			});
			return;
		}

		SwingUtilities.invokeLater(() -> {
			ExamIntegrityMonitor.stop();
			MainFrame.disposeFrame();
			new LoginFrame();
		});
	}

	private static void restore(JButton button) {
		if (button != null) {
			button.setText(END_LABEL);
			button.setEnabled(true);
		}
	}

}

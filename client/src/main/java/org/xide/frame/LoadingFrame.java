package org.xide.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

/** Shown while the client establishes its initial server connection. */
public class LoadingFrame extends JFrame {

	private static final Color TEXT_COLOR = new Color(34, 41, 52);

	public LoadingFrame() {
		setTitle("X-IDE - Connecting");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setResizable(false);

		JPanel content = new JPanel(new BorderLayout(0, 18));
		content.setBackground(Color.WHITE);
		content.setBorder(BorderFactory.createEmptyBorder(32, 40, 32, 40));

		JLabel message = new JLabel("Connecting to the exam server…", SwingConstants.CENTER);
		message.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
		message.setForeground(TEXT_COLOR);

		JProgressBar progress = new JProgressBar();
		progress.setIndeterminate(true);

		content.add(message, BorderLayout.CENTER);
		content.add(progress, BorderLayout.SOUTH);
		setContentPane(content);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

}

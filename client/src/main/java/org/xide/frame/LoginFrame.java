package org.xide.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import org.xide.actions.Login;

public class LoginFrame extends JFrame {

	private static JTextField email;

	private static LoginFrame instance;

	private static JPasswordField password;

	private static JLabel passwordOrEmailMessage;

	private static final char PASSWORD_ECHO_CHAR = '\u2022';

	private final Color TEXT_COLOR = new Color(34, 41, 52);

	private static final Color MUTED_COLOR = new Color(103, 113, 128);

	private static final Color ACCENT_COLOR = new Color(210, 28, 43);

	public LoginFrame() {
		instance = this;
		setTitle("X-IDE - Connect");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setLocationRelativeTo(null);
		setContentPane(createFrame());
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private JPanel createFrame() {
		JPanel background = new JPanel(new BorderLayout());
		background.setBackground(new Color(241, 244, 249));
		background.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

		JPanel card = new JPanel();
		card.setBackground(Color.WHITE);
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBorder(new CompoundBorder(new LineBorder(new Color(222, 227, 235), 1, true),
				BorderFactory.createEmptyBorder(24, 32, 28, 32)));

		ImageIcon source = new ImageIcon(getClass().getResource("/org/xide/ui/logo.png"));
		Image logo = source.getImage().getScaledInstance(190, 107, Image.SCALE_SMOOTH);
		JLabel logoLabel = new JLabel(new ImageIcon(logo));
		logoLabel.setAlignmentX(CENTER_ALIGNMENT);

		JLabel title = new JLabel("Login");
		title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
		title.setForeground(TEXT_COLOR);
		title.setAlignmentX(CENTER_ALIGNMENT);

		JLabel subtitle = new JLabel("Enter your X-IDE.");
		subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
		subtitle.setForeground(MUTED_COLOR);
		subtitle.setAlignmentX(CENTER_ALIGNMENT);

		JLabel addressLabel = new JLabel("Email");
		addressLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
		addressLabel.setForeground(TEXT_COLOR);
		addressLabel.setAlignmentX(LEFT_ALIGNMENT);

		email = new ConnectFrame.PlaceholderTextField("Example: email@gmail.com");
		email.setColumns(24);
		email.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
		email.setForeground(TEXT_COLOR);
		email.setToolTipText("Enter the server's IPv4 address");
		email.setBorder(new CompoundBorder(new LineBorder(new Color(202, 210, 222), 1, true),
				BorderFactory.createEmptyBorder(10, 12, 10, 12)));
		email.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
		email.setAlignmentX(LEFT_ALIGNMENT);

		JLabel passwordLabel = new JLabel("Password");
		passwordLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
		passwordLabel.setForeground(TEXT_COLOR);
		passwordLabel.setAlignmentX(LEFT_ALIGNMENT);

		password = new PlaceholderPasswordField("Example: 3123123123");
		password.setColumns(24);
		password.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
		password.setForeground(TEXT_COLOR);
		password.setToolTipText("Enter your password");
		password.setBorder(new CompoundBorder(new LineBorder(new Color(202, 210, 222), 1, true),
				BorderFactory.createEmptyBorder(10, 12, 10, 12)));
		password.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
		password.setAlignmentX(LEFT_ALIGNMENT);
		password.setEchoChar(PASSWORD_ECHO_CHAR);

		JButton togglePasswordButton = new JButton("Show");
		togglePasswordButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
		togglePasswordButton.setForeground(ACCENT_COLOR);
		togglePasswordButton.setContentAreaFilled(false);
		togglePasswordButton.setFocusPainted(false);
		togglePasswordButton.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 0));
		togglePasswordButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		togglePasswordButton.setToolTipText("Show password");
		togglePasswordButton.addActionListener(event -> {
			boolean hidden = password.getEchoChar() != 0;
			password.setEchoChar(hidden ? (char) 0 : PASSWORD_ECHO_CHAR);
			togglePasswordButton.setText(hidden ? "Hide" : "Show");
			togglePasswordButton.setToolTipText(hidden ? "Hide password" : "Show password");
		});

		JPanel passwordRow = new JPanel(new BorderLayout(4, 0));
		passwordRow.setOpaque(false);
		passwordRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
		passwordRow.setAlignmentX(LEFT_ALIGNMENT);
		passwordRow.add(password, BorderLayout.CENTER);
		passwordRow.add(togglePasswordButton, BorderLayout.EAST);

		passwordOrEmailMessage = new JLabel();
		passwordOrEmailMessage.setVisible(false);
		passwordOrEmailMessage.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		passwordOrEmailMessage.setForeground(new Color(190, 40, 50));
		passwordOrEmailMessage.setAlignmentX(LEFT_ALIGNMENT);

		JButton button = new JButton("Enter");
		button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
		button.setForeground(Color.WHITE);
		button.setBackground(ACCENT_COLOR);
		button.setOpaque(true);
		button.setFocusPainted(false);
		button.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
		button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
		button.setAlignmentX(LEFT_ALIGNMENT);
		button.addActionListener(new Login());

		card.add(logoLabel);
		card.add(Box.createVerticalStrut(12));
		card.add(title);
		card.add(Box.createVerticalStrut(6));
		card.add(subtitle);
		card.add(Box.createVerticalStrut(28));
		card.add(addressLabel);
		card.add(Box.createVerticalStrut(8));
		card.add(email);
		card.add(Box.createVerticalStrut(10));
		card.add(passwordLabel);
		card.add(Box.createVerticalStrut(10));
		card.add(passwordRow);
		card.add(Box.createVerticalStrut(10));
		card.add(passwordOrEmailMessage);
		card.add(Box.createVerticalStrut(12));
		card.add(button);

		background.add(card, BorderLayout.CENTER);
		return background;
	}

	public static void setMessage(String message) {
		SwingUtilities.invokeLater(() -> {
			passwordOrEmailMessage.setText(message);
			passwordOrEmailMessage.setVisible(true);
			passwordOrEmailMessage.revalidate();
			passwordOrEmailMessage.repaint();
		});
	}

	public static String getEmail() {
		return email.getText();
	}

	public static String getPassword() {
		return new String(password.getPassword());
	}

	public static void disposeFrame() {
		SwingUtilities.invokeLater(() -> instance.dispose());
	}

	private static class PlaceholderPasswordField extends JPasswordField {

		private final String placeholder;

		PlaceholderPasswordField(String placeholder) {
			this.placeholder = placeholder;
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);
			if (getPassword().length == 0) {
				Graphics2D copy = (Graphics2D) graphics.create();
				copy.setColor(new Color(145, 153, 165));
				FontMetrics metrics = copy.getFontMetrics();
				Insets insets = getInsets();
				int y = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
				copy.drawString(placeholder, insets.left, y);
				copy.dispose();
			}
		}

	}

}

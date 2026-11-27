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

public final class LoginFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final char PASSWORD_ECHO_CHAR = '\u2022';

	private static final Color BACKGROUND_COLOR = new Color(241, 244, 249);

	private static final Color CARD_BORDER_COLOR = new Color(222, 227, 235);

	private static final Color FIELD_BORDER_COLOR = new Color(202, 210, 222);

	private static final Color ERROR_COLOR = new Color(190, 40, 50);

	private static final Color TEXT_COLOR = new Color(34, 41, 52);

	private static final Color MUTED_COLOR = new Color(103, 113, 128);

	private static final Color ACCENT_COLOR = new Color(210, 28, 43);

	private JTextField email;

	private JPasswordField password;

	private JLabel passwordOrEmailMessage;

	public LoginFrame() {
		setTitle("X-IDE - Connect");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		createContent();
		finishLayout();
	}

	private void createContent() {
		JPanel background = createBackground();
		background.add(createCard(), BorderLayout.CENTER);
		setContentPane(background);
	}

	private JPanel createBackground() {
		JPanel background = new JPanel(new BorderLayout());
		background.setBackground(BACKGROUND_COLOR);
		background.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
		return background;
	}

	private JPanel createCard() {
		JPanel card = new JPanel();
		card.setBackground(Color.WHITE);
		card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
		card.setBorder(new CompoundBorder(new LineBorder(CARD_BORDER_COLOR, 1, true),
				BorderFactory.createEmptyBorder(24, 32, 28, 32)));

		card.add(createLogo());
		card.add(Box.createVerticalStrut(12));
		card.add(createTitle());
		card.add(Box.createVerticalStrut(6));
		card.add(createSubtitle());
		card.add(Box.createVerticalStrut(28));
		card.add(createFieldLabel("Email"));
		card.add(Box.createVerticalStrut(8));
		card.add(createEmailField());
		card.add(Box.createVerticalStrut(10));
		card.add(createFieldLabel("Password"));
		card.add(Box.createVerticalStrut(10));
		card.add(createPasswordRow());
		card.add(Box.createVerticalStrut(10));
		card.add(createMessageLabel());
		card.add(Box.createVerticalStrut(12));
		card.add(createActionButton());
		return card;
	}

	private JLabel createLogo() {
		ImageIcon source = new ImageIcon(getClass().getResource("/org/xide/ui/logo.png"));
		Image logo = source.getImage().getScaledInstance(190, 107, Image.SCALE_SMOOTH);
		JLabel logoLabel = new JLabel(new ImageIcon(logo));
		logoLabel.setAlignmentX(CENTER_ALIGNMENT);
		return logoLabel;
	}

	private JLabel createTitle() {
		JLabel title = new JLabel("Login");
		title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
		title.setForeground(TEXT_COLOR);
		title.setAlignmentX(CENTER_ALIGNMENT);
		return title;
	}

	private JLabel createSubtitle() {
		JLabel subtitle = new JLabel("Enter your X-IDE.");
		subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
		subtitle.setForeground(MUTED_COLOR);
		subtitle.setAlignmentX(CENTER_ALIGNMENT);
		return subtitle;
	}

	private JLabel createFieldLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
		label.setForeground(TEXT_COLOR);
		label.setAlignmentX(LEFT_ALIGNMENT);
		return label;
	}

	private JTextField createEmailField() {
		email = new ConnectFrame.PlaceholderTextField("Example: email@gmail.com");
		email.setColumns(24);
		email.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
		email.setForeground(TEXT_COLOR);
		email.setToolTipText("Enter the server's IPv4 address");
		email.setBorder(new CompoundBorder(new LineBorder(FIELD_BORDER_COLOR, 1, true),
				BorderFactory.createEmptyBorder(10, 12, 10, 12)));
		email.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
		email.setAlignmentX(LEFT_ALIGNMENT);
		return email;
	}

	private JPanel createPasswordRow() {
		JPasswordField passwordField = createPasswordField();
		JPanel passwordRow = new JPanel(new BorderLayout(4, 0));
		passwordRow.setOpaque(false);
		passwordRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
		passwordRow.setAlignmentX(LEFT_ALIGNMENT);
		passwordRow.add(passwordField, BorderLayout.CENTER);
		passwordRow.add(createTogglePasswordButton(passwordField), BorderLayout.EAST);
		return passwordRow;
	}

	private JPasswordField createPasswordField() {
		password = new PlaceholderPasswordField("Example: 3123123123");
		password.setColumns(24);
		password.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
		password.setForeground(TEXT_COLOR);
		password.setToolTipText("Enter your password");
		password.setBorder(new CompoundBorder(new LineBorder(FIELD_BORDER_COLOR, 1, true),
				BorderFactory.createEmptyBorder(10, 12, 10, 12)));
		password.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
		password.setAlignmentX(LEFT_ALIGNMENT);
		password.setEchoChar(PASSWORD_ECHO_CHAR);
		return password;
	}

	private JButton createTogglePasswordButton(JPasswordField passwordField) {
		JButton button = new JButton("Show");
		button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
		button.setForeground(ACCENT_COLOR);
		button.setContentAreaFilled(false);
		button.setFocusPainted(false);
		button.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 0));
		button.setCursor(new Cursor(Cursor.HAND_CURSOR));
		button.setToolTipText("Show password");
		button.addActionListener(event -> {
			boolean hidden = passwordField.getEchoChar() != 0;
			passwordField.setEchoChar(hidden ? (char) 0 : PASSWORD_ECHO_CHAR);
			button.setText(hidden ? "Hide" : "Show");
			button.setToolTipText(hidden ? "Hide password" : "Show password");
		});
		return button;
	}

	private JLabel createMessageLabel() {
		passwordOrEmailMessage = new JLabel();
		passwordOrEmailMessage.setVisible(false);
		passwordOrEmailMessage.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		passwordOrEmailMessage.setForeground(ERROR_COLOR);
		passwordOrEmailMessage.setAlignmentX(LEFT_ALIGNMENT);
		return passwordOrEmailMessage;
	}

	private JButton createActionButton() {
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
		return button;
	}

	private void finishLayout() {
		pack();
		setLocationRelativeTo(null);
	}

	public void setMessage(String message) {
		SwingUtilities.invokeLater(() -> {
			passwordOrEmailMessage.setText(message);
			passwordOrEmailMessage.setVisible(true);
			passwordOrEmailMessage.revalidate();
			passwordOrEmailMessage.repaint();
		});
	}

	public String getEmail() {
		return email.getText();
	}

	public String getPassword() {
		return new String(password.getPassword());
	}

	private static class PlaceholderPasswordField extends JPasswordField {

		private static final long serialVersionUID = 1L;

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

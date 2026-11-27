package org.xide.frame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

import org.xide.actions.ConnectServer;

public final class ConnectFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private static final Color BACKGROUND_COLOR = new Color(241, 244, 249);

	private static final Color CARD_BORDER_COLOR = new Color(222, 227, 235);

	private static final Color FIELD_BORDER_COLOR = new Color(202, 210, 222);

	private static final Color ERROR_COLOR = new Color(190, 40, 50);

	private static final Color TEXT_COLOR = new Color(34, 41, 52);

	private static final Color MUTED_COLOR = new Color(103, 113, 128);

	private static final Color ACCENT_COLOR = new Color(210, 28, 43);

	private final transient CompletableFuture<String> endpointSelection = new CompletableFuture<>();

	private JLabel message;

	private JTextField jTextField;

	public ConnectFrame() {
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
		card.add(createFieldLabel("Server IP address"));
		card.add(Box.createVerticalStrut(8));
		card.add(createAddressField());
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
		JLabel title = new JLabel("Connect to your server");
		title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
		title.setForeground(TEXT_COLOR);
		title.setAlignmentX(CENTER_ALIGNMENT);
		return title;
	}

	private JLabel createSubtitle() {
		JLabel subtitle = new JLabel("Enter your X-IDE server address to get started.");
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

	private JTextField createAddressField() {
		jTextField = new PlaceholderTextField("Example: 192.168.1.50");
		jTextField.setColumns(24);
		jTextField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
		jTextField.setForeground(TEXT_COLOR);
		jTextField.setToolTipText("Enter the server's IPv4 address");
		jTextField.setBorder(new CompoundBorder(new LineBorder(FIELD_BORDER_COLOR, 1, true),
				BorderFactory.createEmptyBorder(10, 12, 10, 12)));
		jTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
		jTextField.setAlignmentX(LEFT_ALIGNMENT);
		return jTextField;
	}

	private JLabel createMessageLabel() {
		message = new JLabel();
		message.setVisible(false);
		message.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
		message.setForeground(ERROR_COLOR);
		message.setAlignmentX(LEFT_ALIGNMENT);
		return message;
	}

	private JButton createActionButton() {
		JButton button = new JButton("Connect");
		button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
		button.setForeground(Color.WHITE);
		button.setBackground(ACCENT_COLOR);
		button.setOpaque(true);
		button.setFocusPainted(false);
		button.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
		button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
		button.setAlignmentX(LEFT_ALIGNMENT);
		button.addActionListener(new ConnectServer());
		return button;
	}

	private void finishLayout() {
		pack();
		setLocationRelativeTo(null);
	}

	public void setMessage(String message2) {
		SwingUtilities.invokeLater(() -> {
			message.setText(message2);
			message.setVisible(true);
			message.revalidate();
			message.repaint();
		});
	}

	public void showMessage() {
		message.setVisible(true);
	}

	public String getTextField() {
		return jTextField.getText();
	}

	public String awaitEndpoint() throws InterruptedException, ExecutionException {
		return endpointSelection.get();
	}

	public void setEndpoint(String endpoint2) {
		endpointSelection.complete(endpoint2);
	}

	public static class PlaceholderTextField extends JTextField {

		private static final long serialVersionUID = 1L;

		private final String placeholder;

		public PlaceholderTextField(String placeholder) {
			this.placeholder = placeholder;
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);
			if (getText().isEmpty()) {
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

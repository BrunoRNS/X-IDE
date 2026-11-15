package org.xide.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Image;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

import org.xide.actions.ConnectServer;

public class LoginFrame extends JFrame {

    private static LoginFrame instance;
    private static JLabel message;
    private static JTextField jTextField;
    private final CompletableFuture<String> endpointSelection = new CompletableFuture<>();
    private static final Color TEXT_COLOR = new Color(34, 41, 52);
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
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(222, 227, 235), 1, true),
                BorderFactory.createEmptyBorder(24, 32, 28, 32)));

        ImageIcon source = new ImageIcon(getClass().getResource("logo.png"));
        Image logo = source.getImage().getScaledInstance(190, 107, Image.SCALE_SMOOTH);
        JLabel logoLabel = new JLabel(new ImageIcon(logo));
        logoLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("Connect to your server");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        title.setForeground(TEXT_COLOR);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Enter your X-IDE server address to get started.");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        subtitle.setForeground(MUTED_COLOR);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        JLabel addressLabel = new JLabel("Server IP address");
        addressLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        addressLabel.setForeground(TEXT_COLOR);
        addressLabel.setAlignmentX(LEFT_ALIGNMENT);

        jTextField = new PlaceholderTextField("Example: 192.168.1.50");
        jTextField.setColumns(24);
        jTextField.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
        jTextField.setForeground(TEXT_COLOR);
        jTextField.setToolTipText("Enter the server's IPv4 address");
        jTextField.setBorder(new CompoundBorder(
                new LineBorder(new Color(202, 210, 222), 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        jTextField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        jTextField.setAlignmentX(LEFT_ALIGNMENT);

        message = new JLabel();
        message.setVisible(false);
        message.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        message.setForeground(new Color(190, 40, 50));
        message.setAlignmentX(LEFT_ALIGNMENT);

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

        card.add(logoLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(title);
        card.add(Box.createVerticalStrut(6));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(28));
        card.add(addressLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(jTextField);
        card.add(Box.createVerticalStrut(10));
        card.add(message);
        card.add(Box.createVerticalStrut(12));
        card.add(button);

        background.add(card, BorderLayout.CENTER);
        return background;
    }

    public static void setMessage(String message2) {
        SwingUtilities.invokeLater(() -> {
            message.setText(message2);
            message.setVisible(true);
            message.revalidate();
            message.repaint();
        });

    }

    public static void showMessage() {
        message.setVisible(true);
    }

    public static String getTextField() {
        return jTextField.getText();
    }

    public String awaitEndpoint() throws InterruptedException, ExecutionException {
        return endpointSelection.get();
    }

    public static void setEndpoint(String endpoint2) {
        instance.endpointSelection.complete(endpoint2);
    }

    public static void disposeFrame() {
        instance.dispose();
    }

    private static final class PlaceholderTextField extends JTextField {
        private final String placeholder;

        private PlaceholderTextField(String placeholder) {
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

package org.xide.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.xide.DevMode;
import org.xide.actions.EndSession;

public class MainFrame extends JFrame {

	private static MainFrame instance;

	private static final Color BACKGROUND_COLOR = new Color(241, 244, 249);

	private static final Color ACCENT_COLOR = new Color(210, 28, 43);

	public MainFrame() {
		instance = this;
		setUndecorated(true);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setContentPane(createFrame());

		GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		if (device.isFullScreenSupported()) {
			device.setFullScreenWindow(this);
		}
		else {
			Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
			setSize(size);
			setLocation(0, 0);
		}

		setAlwaysOnTop(true);

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.isAltDown() || e.isControlDown() && e.isAltDown() || e.getKeyCode() == KeyEvent.VK_WINDOWS) {
					e.consume();
				}
			}
		});

		addWindowFocusListener(new WindowFocusListener() {
			@Override
			public void windowLostFocus(WindowEvent e) {
				SwingUtilities.invokeLater(() -> {
					setAlwaysOnTop(true);
					toFront();
					requestFocus();
				});
			}

			@Override
			public void windowGainedFocus(WindowEvent e) {
			}
		});

		int monitores = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length;

		if (DevMode.isActive()) {
			System.out.printf("Dev mode: monitor check disabled (%d monitor(s)).%n", monitores);
		}
		else if (monitores > 1) {
			dispose();
			JOptionPane.showMessageDialog(null, "Disconnect additional monitors to continue.");
			System.exit(0);
		}

		setVisible(true);
	}

	private JPanel createFrame() {
		JPanel background = new JPanel(new BorderLayout());
		background.setBackground(BACKGROUND_COLOR);

		JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		toolbar.setOpaque(false);
		toolbar.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

		JButton endButton = new JButton("End");
		endButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
		endButton.setForeground(Color.WHITE);
		endButton.setBackground(ACCENT_COLOR);
		endButton.setOpaque(true);
		endButton.setFocusPainted(false);
		endButton.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
		endButton.setToolTipText("End session");
		endButton.addActionListener(new EndSession());
		toolbar.add(endButton);

		background.add(toolbar, BorderLayout.NORTH);
		return background;
	}

	public static void disposeFrame() {
		if (instance != null) {
			instance.dispose();
		}
	}

}
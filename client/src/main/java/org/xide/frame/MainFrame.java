package org.xide.frame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.xide.DevMode;
import org.xide.ui.TabbeMenu;

public class MainFrame extends JFrame {

	private static MainFrame instance;

	public static final Color BACKGROUND_COLOR = new Color(241, 244, 249);

	public static final Color ACCENT_COLOR = new Color(210, 28, 43);

	public MainFrame() {
		instance = this;
		setUndecorated(true);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		createFrame();

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

	private void createFrame() {
		new TabbeMenu(this);
	}

	public static void disposeFrame() {
		if (instance != null) {
			instance.dispose();
		}
	}

}
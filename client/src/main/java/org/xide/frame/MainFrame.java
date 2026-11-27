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

/**
 * Full-screen, undecorated main exam frame hosting the {@link org.xide.ui.TabbeMenu}.
 * Consumes key combinations that could escape fullscreen, keeps the window always on top,
 * enforces a single monitor when not in development mode ({@link org.xide.DevMode}), and
 * blocks accidental closing.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
public final class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	public static final Color BACKGROUND_COLOR = new Color(241, 244, 249);

	public static final Color ACCENT_COLOR = new Color(210, 28, 43);

	public MainFrame() {
		setUndecorated(true);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		createContent();
		toggleFullscreen();
		setAlwaysOnTop(true);
		addListeners();
		limitMonitors();
	}

	private void toggleFullscreen() {
		GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		if (device.isFullScreenSupported()) {
			device.setFullScreenWindow(this);
		}
		else {
			Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
			setSize(size);
			setLocation(0, 0);
		}
	}

	private void addListeners() {
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
	}

	private void limitMonitors() {
		int monitores = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length;

		if (DevMode.isActive()) {
			System.out.printf("Dev mode: monitor check disabled (%d monitor(s)).%n", monitores);
		}
		else if (monitores > 1) {
			dispose();
			JOptionPane.showMessageDialog(null, "Disconnect additional monitors to continue.");
			System.exit(0);
		}
	}

	private void createContent() {
		setContentPane(new TabbeMenu());
	}

}

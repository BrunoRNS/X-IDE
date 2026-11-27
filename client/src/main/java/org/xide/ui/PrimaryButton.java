package org.xide.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;

public final class PrimaryButton extends JButton {

	private static final Color BASE = new Color(210, 28, 43);

	private static final Color HOVER = new Color(184, 22, 36);

	private static final Color PRESSED = new Color(160, 18, 30);

	private static final Color DISABLED = new Color(214, 160, 166);

	private static final long serialVersionUID = 1L;

	private boolean hovering;

	PrimaryButton(String text) {
		super(text);
		setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
		setForeground(Color.WHITE);
		setContentAreaFilled(false);
		setBorderPainted(false);
		setFocusPainted(false);
		setOpaque(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent event) {
				hovering = true;
				repaint();
			}

			@Override
			public void mouseExited(MouseEvent event) {
				hovering = false;
				repaint();
			}
		});
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		Graphics2D background = (Graphics2D) graphics.create();
		background.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Color fill;
		if (!isEnabled()) {
			fill = DISABLED;
		}
		else if (getModel().isPressed()) {
			fill = PRESSED;
		}
		else if (hovering) {
			fill = HOVER;
		}
		else {
			fill = BASE;
		}

		background.setColor(fill);
		background.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
		background.dispose();

		super.paintComponent(graphics);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		setCursor(Cursor.getPredefinedCursor(enabled ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
		repaint();
	}

}
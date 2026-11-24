package org.xide.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import org.xide.actions.EndSession;
import org.xide.ui.menu.About;
import org.xide.ui.menu.Editor;
import org.xide.ui.menu.Problens;

public class TabbeMenu extends JPanel {

	public static final Color ACCENT_COLOR = new Color(210, 28, 43);

	private static final Color TEXT_COLOR = new Color(34, 41, 52);

	private static final Color MUTED_COLOR = new Color(103, 113, 128);

	private static final Color DIVIDER_COLOR = new Color(226, 231, 238);

	private static final Color HOVER_COLOR = new Color(245, 247, 250);

	private static final Font TAB_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 13);

	private final JTabbedPane tabs = new JTabbedPane();

	public TabbeMenu(JFrame jFrame) {
		super(new BorderLayout());
		setBackground(Color.WHITE);
		add(createHeader(), BorderLayout.NORTH);
		add(createTabs(), BorderLayout.CENTER);
		jFrame.setContentPane(this);
	}

	private JPanel createHeader() {
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(Color.WHITE);
		header.setBorder(BorderFactory.createCompoundBorder(new MatteBorder(0, 0, 1, 0, DIVIDER_COLOR),
				BorderFactory.createEmptyBorder(12, 20, 12, 20)));

		JPanel brand = new JPanel();
		brand.setOpaque(false);
		brand.setLayout(new BoxLayout(brand, BoxLayout.X_AXIS));
		brand.add(new JLabel(loadLogo(44)));
		brand.add(Box.createHorizontalStrut(14));

		JPanel names = new JPanel();
		names.setOpaque(false);
		names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));

		JLabel title = new JLabel("X-IDE");
		title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
		title.setForeground(TEXT_COLOR);

		JLabel subtitle = new JLabel("Exam environment");
		subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
		subtitle.setForeground(MUTED_COLOR);

		names.add(title);
		names.add(Box.createVerticalStrut(2));
		names.add(subtitle);
		brand.add(names);
		header.add(brand, BorderLayout.WEST);

		JButton endButton = new PrimaryButton("End session");
		endButton.setToolTipText("End the exam session and return to the login screen");
		endButton.addActionListener(new EndSession());

		JPanel actions = new JPanel();
		actions.setOpaque(false);
		actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
		actions.add(Box.createVerticalGlue());
		actions.add(endButton);
		actions.add(Box.createVerticalGlue());
		header.add(actions, BorderLayout.EAST);

		return header;
	}

	private JTabbedPane createTabs() {
		tabs.setUI(new ModernTabbedPaneUI());
		tabs.setBackground(Color.WHITE);
		tabs.setForeground(TEXT_COLOR);
		tabs.setFont(TAB_FONT);
		tabs.setBorder(BorderFactory.createEmptyBorder());

		addTab("About", new About());
		addTab("Editor", new Editor());
		addTab("Problems", new Problens());

		tabs.addChangeListener(event -> updateTabStyles());
		updateTabStyles();
		return tabs;
	}

	private void addTab(String title, JComponent page) {
		page.setBackground(Color.WHITE);
		page.setOpaque(true);
		page.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

		JScrollPane scroll = new JScrollPane(page, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getViewport().setBackground(Color.WHITE);

		tabs.addTab(title, scroll);
		int index = tabs.getTabCount() - 1;

		JLabel label = new JLabel(title, SwingConstants.CENTER);
		label.setFont(TAB_FONT);
		label.setForeground(MUTED_COLOR);
		label.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
		label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		tabs.setTabComponentAt(index, label);
	}

	private void updateTabStyles() {
		int selectedIndex = tabs.getSelectedIndex();
		for (int i = 0; i < tabs.getTabCount(); i++) {
			Component component = tabs.getTabComponentAt(i);
			if (component instanceof JLabel label) {
				boolean selected = i == selectedIndex;
				label.setFont(selected ? TAB_FONT.deriveFont(Font.BOLD) : TAB_FONT);
				label.setForeground(selected ? TEXT_COLOR : MUTED_COLOR);
			}
		}
		tabs.repaint();
	}

	private ImageIcon loadLogo(int height) {
		URL location = getClass().getResource("/org/xide/ui/logo.png");
		if (location == null) {
			return new ImageIcon();
		}
		ImageIcon source = new ImageIcon(location);
		int width = Math.max(1, source.getIconWidth() * height / Math.max(1, source.getIconHeight()));
		Image scaled = source.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
		return new ImageIcon(scaled);
	}

	private static final class ModernTabbedPaneUI extends BasicTabbedPaneUI {

		@Override
		protected void installDefaults() {
			super.installDefaults();
			tabAreaInsets = new Insets(6, 12, 0, 12);
			tabInsets = new Insets(8, 16, 8, 16);
			selectedTabPadInsets = new Insets(0, 0, 0, 0);
			contentBorderInsets = new Insets(0, 0, 0, 0);
		}

		@Override
		protected void paintTabArea(Graphics graphics, int tabPlacement, int selectedIndex) {
			Insets insets = tabPane.getInsets();
			int x = insets.left;
			int y = insets.top;
			int width = tabPane.getWidth() - insets.left - insets.right;
			int height = maxTabHeight + tabAreaInsets.top + tabAreaInsets.bottom;

			Graphics2D background = (Graphics2D) graphics.create();
			background.setColor(Color.WHITE);
			background.fillRect(x, y, width, height);
			background.setColor(DIVIDER_COLOR);
			background.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
			background.dispose();

			super.paintTabArea(graphics, tabPlacement, selectedIndex);

			if (selectedIndex >= 0 && selectedIndex < tabPane.getTabCount()) {
				Rectangle bounds = tabPane.getBoundsAt(selectedIndex);
				if (bounds != null && bounds.width > 28) {
					Graphics2D underline = (Graphics2D) graphics.create();
					underline.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					underline.setColor(ACCENT_COLOR);
					underline.fillRoundRect(bounds.x + 20, bounds.y + bounds.height - 8, bounds.width - 40, 3, 3, 3);
					underline.dispose();
				}
			}
		}

		@Override
		protected void paintTabBackground(Graphics graphics, int tabPlacement, int tabIndex, int x, int y, int width,
				int height, boolean isSelected) {
			if (!isSelected && getRolloverTab() == tabIndex) {
				Graphics2D hover = (Graphics2D) graphics.create();
				hover.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				hover.setColor(HOVER_COLOR);
				hover.fillRoundRect(x + 4, y + 2, width - 8, height - 4, 10, 10);
				hover.dispose();
			}
		}

		@Override
		protected void paintTabBorder(Graphics graphics, int tabPlacement, int tabIndex, int x, int y, int width,
				int height, boolean isSelected) {
			// Intentionally empty: only the accent underline marks the selection.
		}

		@Override
		protected void paintFocusIndicator(Graphics graphics, int tabPlacement, Rectangle[] rects, int tabIndex,
				Rectangle iconRect, Rectangle textRect, boolean isSelected) {
			// No dotted focus rectangle.
		}

		@Override
		protected void paintContentBorder(Graphics graphics, int tabPlacement, int selectedIndex) {
			// The divider is painted by paintTabArea.
		}

	}

}

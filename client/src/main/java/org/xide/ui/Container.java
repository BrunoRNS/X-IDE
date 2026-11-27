package org.xide.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

public final class Container extends JPanel {

	private static final long serialVersionUID = 1L;

	private final Color color;

	private final JEditorPane editor;

	public Container(String markdown, Color color) {
		this(markdown, color, isLight(color) ? new Color(34, 41, 52) : Color.WHITE);
	}

	public Container(String markdown, Color color, Color textColor) {
		this.color = color;
		setLayout(new BorderLayout());
		setOpaque(false);

		Parser parser = Parser.builder().build();
		Node document = parser.parse(markdown);
		HtmlRenderer renderer = HtmlRenderer.builder().build();
		String html = renderer.render(document);

		String fullHtml = "<html><body style='color:" + toHex(textColor)
				+ "; font-family:sans-serif; font-size:13px; margin:0;'>" + html + "</body></html>";

		editor = new JEditorPane("text/html", fullHtml);
		editor.setEditable(false);
		editor.setOpaque(false);
		editor.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
		editor.setCaretPosition(0);
		add(editor, BorderLayout.CENTER);
	}

	private static boolean isLight(Color background) {
		return (0.299 * background.getRed() + 0.587 * background.getGreen() + 0.114 * background.getBlue()) > 160;
	}

	private String toHex(Color value) {
		return String.format("#%02x%02x%02x", value.getRed(), value.getGreen(), value.getBlue());
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D gr = (Graphics2D) g;
		gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		gr.setColor(color);
		RoundRectangle2D.Double ret = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20);
		gr.fill(ret);

		gr.clip(ret);
	}

}

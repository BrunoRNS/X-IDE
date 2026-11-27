package org.xide.models.enums;

import java.util.Locale;

/**
 * Colors assigned to competition problems' balloons.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
public enum BalloonColor {

	RED("#dc2626"), BLUE("#2563eb"), GREEN("#16a34a"), YELLOW("#ca8a04"), ORANGE("#ea580c"), PURPLE("#9333ea"),
	PINK("#db2777"), CYAN("#0891b2"), LIME("#65a30d"), FUCHSIA("#c026d3"), INDIGO("#4f46e5"), TEAL("#0d9488"),
	ROSE("#e11d48"), SKY("#0284c7"), EMERALD("#059669"), AMBER("#d97706"), VIOLET("#7c3aed"), STONE("#78716c"),
	CORAL("#f43f5e"), NAVY("#1d4ed8");

	private final String cssColor;

	BalloonColor(String cssColor) {
		this.cssColor = cssColor;
	}

	public String cssColor() {
		return cssColor;
	}

	/**
	 * Accepts CSS colors, color names already stored in the database and legacy A-T
	 * labels.
	 */
	public static BalloonColor fromDatabase(String value) {
		if (value == null || value.isBlank()) {
			return RED;
		}

		String trimmed = value.trim();
		for (BalloonColor color : values()) {
			if (color.cssColor.equalsIgnoreCase(trimmed)) {
				return color;
			}
		}

		String normalized = trimmed.toUpperCase(Locale.ROOT);
		try {
			return valueOf(normalized);
		}
		catch (IllegalArgumentException ignored) {
			if (normalized.length() == 1 && normalized.charAt(0) >= 'A' && normalized.charAt(0) <= 'T') {
				return values()[normalized.charAt(0) - 'A'];
			}
			return RED;
		}
	}

}

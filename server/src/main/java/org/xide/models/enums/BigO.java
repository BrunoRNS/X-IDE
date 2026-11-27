package org.xide.models.enums;

import java.util.Locale;

public enum BigO {

	N1("n1"), LOGN("logn"), N("n"), NLOGN("nlogn"), Ne2("ne2"), Ne3("ne3");

	private final String name;

	private BigO(String name) {
		this.name = name;
	}

	/** Accepts the database values and the labels displayed in the problem form. */
	public static BigO fromDatabase(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Choose the minimum Big O.");
		}

		String normalized = value.trim().toLowerCase(Locale.ROOT).replace(" ", "");
		return switch (normalized) {
			case "1", "o(1)", "n1" -> N1;
			case "logn", "log n", "o(logn)", "o(log n)" -> LOGN;
			case "n", "o(n)" -> N;
			case "nlogn", "nlog n", "o(nlogn)", "o(n log n)" -> NLOGN;
			case "n2", "n²", "o(n2)", "o(n²)", "o(n^2)", "ne2" -> Ne2;
			case "n3", "n³", "o(n3)", "o(n³)", "o(n^3)", "ne3" -> Ne3;
			default -> throw new IllegalArgumentException("Unsupported Big O: " + value);
		};
	}

	@Override
	public String toString() {
		return name;
	}

}

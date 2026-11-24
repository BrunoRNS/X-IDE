package org.xide.models.enums;

import java.util.Locale;

public enum Lang {

	PYTHON("python"), KOTLIN("kotlin"), JAVA("java"), CPP("cpp"), C("c"), RUST("rust"), GO("go"), GROOVY("groovy"),
	JAVASCRIPT("javascript");

	private String name;

	private Lang(String name) {
		this.name = name;
	}

	public static String getTableCreate() {
		return """
				ENUM('python', 'kotlin', 'java', 'cpp', 'c', 'rust', 'go', 'groovy', 'javascript') NOT NULL
				""";
	}

	/** Accepts the database values and the labels displayed in the problem form. */
	public static Lang fromDatabase(String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("Choose a programming language.");
		}

		String normalized = value.trim().toLowerCase(Locale.ROOT);
		return switch (normalized) {
			case "python" -> PYTHON;
			case "kotlin" -> KOTLIN;
			case "java" -> JAVA;
			case "c++", "cpp" -> CPP;
			case "c" -> C;
			case "rust" -> RUST;
			case "go", "golang" -> GO;
			case "groovy" -> GROOVY;
			case "javascript", "js" -> JAVASCRIPT;
			default -> throw new IllegalArgumentException("Unsupported language: " + value);
		};
	}

	@Override
	public String toString() {
		return name;
	}

}

package org.xide.config;

/**
 * Holds bundled hello-world example programs for each supported language, loaded once
 * from classpath resources ({@code /code-examples/}) via {@link CodeExamplesUtil}.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
public final class CodeExamples {

	public static final String PYTHON = CodeExamplesUtil.load("python.txt");

	public static final String JAVA = CodeExamplesUtil.load("java.txt");

	public static final String JAVASCRIPT = CodeExamplesUtil.load("javascript.txt");

	public static final String GROOVY = CodeExamplesUtil.load("groovy.txt");

	public static final String KOTLIN = CodeExamplesUtil.load("kotlin.txt");

	public static final String CPP = CodeExamplesUtil.load("cpp.txt");

	public static final String C = CodeExamplesUtil.load("c.txt");

	public static final String CSHARP = CodeExamplesUtil.load("csharp.txt");

	public static final String GO = CodeExamplesUtil.load("go.txt");

	public static final String RUST = CodeExamplesUtil.load("rust.txt");

	private CodeExamples() {
	}

}

package org.xide.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Utility for loading text resources from the classpath, used to read the bundled code
 * example files. Instances cannot be created.
 *
 * @since v1.0.0
 * @author BrunoRNS
 */
public final class CodeExamplesUtil {

	private static final String CODE_EXAMPLES_PATH = "/code-examples/";

	private CodeExamplesUtil() {
	}

	/** Loads a code example from the classpath resources. */
	public static String load(String fileName) {
		String resource = CODE_EXAMPLES_PATH + fileName;
		try (InputStream input = CodeExamplesUtil.class.getResourceAsStream(resource)) {
			if (input == null) {
				throw new IllegalStateException("Code example resource not found: " + resource);
			}
			return new String(input.readAllBytes(), StandardCharsets.UTF_8);
		}
		catch (IOException e) {
			throw new UncheckedIOException("Failed to read code example resource: " + resource, e);
		}
	}

}

package org.xide.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Utilities to load classpath resources as UTF-8 text and print them to standard output.
 * Instances cannot be created.
 *
 * @since v1.0.0
 * @author BrunoRNS
 */
public final class SoutUtil {

	private SoutUtil() {
	}

	public static String loadFromResources(String resourcePath) {
		try (InputStream inputStream = SoutUtil.class.getClassLoader().getResourceAsStream(resourcePath)) {
			if (inputStream == null) {
				throw new IllegalArgumentException("Resource not found: " + resourcePath);
			}
			try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
				scanner.useDelimiter("\\A");
				return scanner.hasNext() ? scanner.next() : "";
			}
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to load resource: " + resourcePath, e);
		}
	}

	public static void printFromResources(String resourcePath) {
		System.out.print(loadFromResources(resourcePath));
	}

	public static void printlnFromResources(String resourcePath) {
		System.out.println(loadFromResources(resourcePath));
	}

}
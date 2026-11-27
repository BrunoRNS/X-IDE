package org.xide.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Reads SQL scripts from classpath resources as UTF-8 strings and can combine a directory
 * of {@code .sql} files into a single output file.
 *
 * @since v1.0.0
 * @author LuisOtavio
 */
public class SqlUtil {

	public static String readSqlToString(String resourcePath) {
		try (InputStream is = SqlUtil.class.getClassLoader().getResourceAsStream(resourcePath)) {
			if (is == null) {
				throw new IllegalArgumentException("Resource not found: " + resourcePath);
			}
			return new String(is.readAllBytes(), StandardCharsets.UTF_8);
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to read sql resource: " + resourcePath, e);
		}
	}

	public static void combineSqlFiles(String inputDir, String outputFile) throws IOException {
		Path in = Paths.get(inputDir);
		Path out = Paths.get(outputFile);
		List<String> combined = new ArrayList<>();
		try (Stream<Path> paths = Files.walk(in)) {
			paths.filter(p -> p.toString().toLowerCase().endsWith(".sql")).sorted().forEach(p -> {
				try {
					combined.add(Files.readString(p, StandardCharsets.UTF_8));
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		}
		Files.createDirectories(out.getParent());
		Files.writeString(out, String.join("\n\n", combined), StandardCharsets.UTF_8);
	}

}

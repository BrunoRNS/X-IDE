package org.xide.http.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

import io.javalin.http.UploadedFile;
import io.javalin.util.FileUtil;

public class ProblensService {

	private static final String UPLOADS_DIRECTORY_PROPERTY = "xide.uploads.dir";

	public static Path savePdf(UploadedFile file) throws IOException {
		Path directory = uploadsDirectory().resolve("pdf").toAbsolutePath().normalize();
		Files.createDirectories(directory);

		String filename = sanitizeFilename(file.filename());
		Path target = directory.resolve(UUID.randomUUID() + "-" + filename).normalize();
		if (!target.startsWith(directory)) {
			throw new IOException("Invalid file name: " + file.filename());
		}

		FileUtil.streamToFile(file.content(), target.toString());
		return target;
	}

	private static Path uploadsDirectory() {
		return Paths.get(System.getProperty(UPLOADS_DIRECTORY_PROPERTY, "uploads"));
	}

	private static String sanitizeFilename(String filename) {
		String name = filename == null ? "" : filename.replace('\\', '/');
		int slash = name.lastIndexOf('/');
		if (slash >= 0) {
			name = name.substring(slash + 1);
		}

		name = name.replaceAll("[^A-Za-z0-9._-]", "_");
		if (name.isBlank()) {
			name = "problem.pdf";
		}
		if (!name.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
			name += ".pdf";
		}
		return name;
	}

}

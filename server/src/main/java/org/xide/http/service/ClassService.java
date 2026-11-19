package org.xide.http.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import io.javalin.Javalin;
import io.javalin.http.UploadedFile;
import io.javalin.http.staticfiles.Location;
import io.javalin.util.FileUtil;

public class ClassService {

	private static final String UPLOADS_DIRECTORY_PROPERTY = "xide.uploads.dir";

	private static final String FIRST_PORT_PROPERTY = "xide.classroom.port";

	public static Javalin createServer(List<UploadedFile> files, List<String> paths, long id) throws IOException {
		Path directory = uploadDirectory(id);
		createFolder(files, paths, directory);

		Javalin server = Javalin.create(config -> {
			config.staticFiles.add(staticFiles -> {
				staticFiles.hostedPath = "/";
				staticFiles.directory = directory.toString();
				staticFiles.location = Location.EXTERNAL;
			});
		});
		server.start(port(Integer.getInteger(FIRST_PORT_PROPERTY, 8083)));
		return server;
	}

	public static Path uploadDirectory(long id) {
		String base = System.getProperty(UPLOADS_DIRECTORY_PROPERTY, "uploads");
		return Paths.get(base, String.valueOf(id)).toAbsolutePath().normalize();
	}

	private static int port(int initial) {
		for (int p = initial; p <= 65535; p++) {
			try (ServerSocket socket = new ServerSocket(p)) {
				return p;
			}
			catch (Exception ignored) {
			}
		}
		throw new RuntimeException("No free port between " + initial + " and 65535");
	}

	private static void createFolder(List<UploadedFile> files, List<String> paths, Path directory) throws IOException {
		delete(directory);
		Files.createDirectories(directory);

		for (int i = 0; i < files.size(); i++) {
			UploadedFile file = files.get(i);
			String filename = i < paths.size() && !paths.get(i).isBlank() ? paths.get(i) : file.filename();
			if (filename == null || filename.isBlank()) {
				continue;
			}

			Path target = directory.resolve(filename.replace('\\', '/')).normalize();
			if (!target.startsWith(directory)) {
				throw new IOException("Invalid file path: " + filename);
			}

			Path parent = target.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}

			FileUtil.streamToFile(file.content(), target.toString());
		}
	}

	private static void delete(Path directory) throws IOException {
		if (!Files.exists(directory)) {
			return;
		}

		try (Stream<Path> paths = Files.walk(directory)) {
			paths.sorted(Comparator.reverseOrder()).forEach((path) -> {
				try {
					Files.delete(path);
				}
				catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			});
		}
		catch (UncheckedIOException e) {
			throw e.getCause();
		}
	}

}

package org.xide.http.controllers;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import io.javalin.http.staticfiles.Location;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Starts one read-only HTTP file server for each folder a teacher shares. */
public class ClassController {

	private static final int FIRST_PORT = 8083;

	private static final Path SHARES_ROOT = Path.of(System.getProperty("java.io.tmpdir"), "xide-shares");

	private static final Map<Integer, SharedFolder> SERVERS = new ConcurrentHashMap<>();

	private ClassController() {
	}

	public static void index(Context ctx) {
		ctx.render("templates/share.html",
				Map.of("shares", SERVERS.values().stream().sorted(Comparator.comparingInt(SharedFolder::port)).toList(),
						"baseUrl", ctx.scheme() + "://" + ctx.req().getServerName()));
	}

	/**
	 * Receives the files selected with the browser's folder input, stores them in a
	 * temporary directory and starts a read-only server for that copy.
	 */
	public static void upload(Context ctx) {
		List<UploadedFile> files = ctx.uploadedFiles("files");
		if (files == null || files.isEmpty()) {
			ctx.status(400).result("Selecione uma pasta.");
			return;
		}

		List<String> relativePaths = ctx.formParams("path");
		String displayName = folderName(relativePaths);
		Path folder = SHARES_ROOT.resolve(UUID.randomUUID().toString());
		try {
			Files.createDirectories(folder);
			int written = 0;
			for (int i = 0; i < files.size(); i++) {
				UploadedFile file = files.get(i);
				String relative = i < relativePaths.size() ? relativePaths.get(i) : file.filename();
				Path destination = safeResolve(folder, relative);
				if (destination == null)
					continue;
				Files.createDirectories(destination.getParent());
				try (InputStream content = file.content()) {
					Files.copy(content, destination, StandardCopyOption.REPLACE_EXISTING);
				}
				written++;
			}
			if (written == 0)
				throw new IOException("No files could be written");

			SharedFolder share = startServer(displayName, folder, true);
			SERVERS.put(share.port(), share);
			ctx.redirect("/share");
		}
		catch (IOException | RuntimeException e) {
			deleteRecursively(folder);
			ctx.status(500).result("Não foi possível compartilhar essa pasta.");
		}
	}

	public static void stop(Context ctx) {
		int port;
		try {
			port = Integer.parseInt(ctx.pathParam("port"));
		}
		catch (NumberFormatException e) {
			ctx.status(400).result("Porta inválida.");
			return;
		}
		SharedFolder share = SERVERS.remove(port);
		if (share != null) {
			share.app().stop();
			if (share.temporary())
				deleteRecursively(share.localPath());
		}
		ctx.redirect("/share");
	}

	private static SharedFolder startServer(String displayName, Path folder, boolean temporary) throws IOException {
		for (int port = FIRST_PORT; port <= 65535; port++) {
			Javalin app = Javalin.create(config -> config.staticFiles.add(staticFiles -> {
				staticFiles.hostedPath = "/";
				staticFiles.directory = folder.toString();
				staticFiles.location = Location.EXTERNAL;
			}));
			try {
				app.start("0.0.0.0", port);
				return new SharedFolder(displayName, port, app, folder, temporary);
			}
			catch (Exception e) {
				app.stop();
				if (!isPortAvailable(port))
					continue;
				throw new IOException("Could not start file server", e);
			}
		}
		throw new IOException("No available port");
	}

	private static String folderName(List<String> relativePaths) {
		if (relativePaths == null || relativePaths.isEmpty())
			return "folder";
		String first = relativePaths.get(0).replace('\\', '/');
		int slash = first.indexOf('/');
		String name = slash > 0 ? first.substring(0, slash) : first;
		return name.isBlank() ? "folder" : name;
	}

	/**
	 * Resolves a browser-provided relative path without allowing it to escape the root.
	 */
	private static Path safeResolve(Path root, String relative) {
		if (relative == null || relative.isBlank())
			return null;
		Path resolved = root.resolve(relative.replace('\\', '/')).normalize();
		if (resolved.equals(root) || !resolved.startsWith(root))
			return null;
		return resolved;
	}

	private static boolean isPortAvailable(int port) {
		try (ServerSocket socket = new ServerSocket(port)) {
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}

	private static void deleteRecursively(Path path) {
		if (path == null)
			return;
		try (var stream = Files.walk(path)) {
			stream.sorted(Comparator.reverseOrder()).forEach(file -> {
				try {
					Files.deleteIfExists(file);
				}
				catch (IOException ignored) {
					// Best effort cleanup.
				}
			});
		}
		catch (IOException ignored) {
			// Best effort cleanup.
		}
	}

	public record SharedFolder(String folder, int port, Javalin app, Path localPath, boolean temporary) {
	}

}

package org.xide;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;
import org.xide.http.routers.Routers;
import org.xide.repository.Connection;
import org.xide.repository.InitDatabase;
import org.xide.sockets.WebSocketServer;

public class Main {

	public static void main(String[] args) throws Exception {
		WebSocketServer server = WebSocketServer.getInstance();
		Javalin.create(config -> {
			Path devResources = devResourcesRoot();
			config.fileRenderer(new JavalinThymeleaf(templateEngine(devResources)));
			if (devResources == null) {
				config.staticFiles.add(staticFiles -> {
					staticFiles.hostedPath = "/public";
					staticFiles.directory = "/public";
					staticFiles.location = Location.CLASSPATH;
				});
			}
			else {
				addExternalStatic(config, devResources.resolve("public"));
			}
			Routers.register(config);
		}).start(8081);
		server.start();
		try {
			java.sql.Connection c = Connection.connect();
			InitDatabase initDatabase = new InitDatabase(c);
			initDatabase.init();
		}
		catch (NullPointerException e) {
			System.out.println("DB_NAME or DB_PASSWORD or DB_USER is invalid or not exist");
			return;
		}
		Runtime.getRuntime().addShutdownHook(new Thread(server::stop, "websocket-shutdown"));
		new CountDownLatch(1).await();
	}

	/**
	 * Dev-mode resource root: XIDE_DEV_RESOURCES pointing at server/src/main/resources
	 * (bind-mounted by `make run-dev-server`). When set and present, templates are read
	 * from the filesystem with the Thymeleaf cache disabled, so HTML edits show up on
	 * browser refresh without rebuilding or restarting anything.
	 */
	private static Path devResourcesRoot() {
		String value = System.getenv("XIDE_DEV_RESOURCES");
		if (value == null || value.isBlank()) {
			return null;
		}
		Path root = Path.of(value).toAbsolutePath().normalize();
		return Files.isDirectory(root) ? root : null;
	}

	/**
	 * Thymeleaf engine for both modes. Templates resolve relative to the dev resources
	 * directory when XIDE_DEV_RESOURCES is active, otherwise from the classpath. The
	 * ".html" suffix lets fragment references omit the extension, e.g.
	 * "~{fragments/menu}".
	 */
	private static TemplateEngine templateEngine(Path devResources) {
		TemplateEngine engine = new TemplateEngine();
		if (devResources == null) {
			ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
			resolver.setTemplateMode(TemplateMode.HTML);
			resolver.setSuffix(".html");
			resolver.setCharacterEncoding("UTF-8");
			engine.setTemplateResolver(resolver);
			return engine;
		}
		FileTemplateResolver resolver = new FileTemplateResolver();
		resolver.setPrefix(devResources.toString() + "/");
		resolver.setSuffix(".html");
		resolver.setTemplateMode(TemplateMode.HTML);
		resolver.setCharacterEncoding("UTF-8");
		resolver.setCacheable(false); // hot reload: re-read templates on every request
		engine.setTemplateResolver(resolver);
		return engine;
	}

	/** Serves publicDir at /public from the filesystem instead of the jar. */
	private static void addExternalStatic(JavalinConfig config, Path publicDir) {
		if (!Files.isDirectory(publicDir)) {
			return;
		}
		config.staticFiles.add(staticFiles -> {
			staticFiles.hostedPath = "/public";
			staticFiles.directory = publicDir.toString();
			staticFiles.location = Location.EXTERNAL;
		});
	}

}

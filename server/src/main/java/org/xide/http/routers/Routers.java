package org.xide.http.routers;

import org.xide.http.controllers.HelloController;
import org.xide.http.controllers.StudentsController;

import io.javalin.config.JavalinConfig;

public class Routers {

	public static void register(JavalinConfig config) {
		config.routes.get("/", (cx) -> {
			HelloController.index(cx);
		});
		config.routes.get("/students", (cx) -> {
			StudentsController.index(cx);
		});
		config.routes.get("/pdf-users", (cx)->{
			StudentsController.loadPdf(cx);
		})
	}

}

package org.xide.http.routers;

import org.xide.http.controllers.ClassController;
import org.xide.http.controllers.CompetitionController;
import org.xide.http.controllers.HelloController;
import org.xide.http.controllers.ProblensController;
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
		config.routes.get("/pdf-users", (cx) -> {
			StudentsController.loadPdf(cx);
		});
		config.routes.post("/students/register", (cx) -> {
			StudentsController.register(cx);
		});

		config.routes.delete("/students/delete/<id>", (ctx) -> {
			StudentsController.deleteUser(ctx);
		});

		config.routes.put("/students/update/<id>", (ctx) -> {
			StudentsController.updateUser(ctx);
		});

		config.routes.get("/classroom", (ctx) -> {
			ClassController.page(ctx);
		});

		config.routes.get("/class", (ctx) -> {
			ClassController.list(ctx);
		});

		config.routes.get("/class/{id}", (ctx) -> {
			ClassController.show(ctx);
		});

		config.routes.post("/class/{id}", (ctx) -> {
			ClassController.index(ctx);
		});

		config.routes.get("/competition", (ctx) -> {
			CompetitionController.index(ctx);
		});

		config.routes.post("/problens/new", (ctx) -> {
			ProblensController.registerNewProblem(ctx);
		});

		config.routes.put("/problems/{id}", (ctx) -> {
			ProblensController.updateProblem(ctx);
		});

		config.routes.get("/problens/all", (ctx) -> {
			ProblensController.getAllProblem(ctx);
		});

		config.routes.post("/problens/id", (ctx) -> {
			ProblensController.getProblemById(ctx);
		});

		config.routes.get("/problems/{id}/pdf", (ctx) -> {
			ProblensController.getProblemPdf(ctx);
		});
	}

}

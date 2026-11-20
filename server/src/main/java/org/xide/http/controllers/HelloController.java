package org.xide.http.controllers;

import io.javalin.http.Context;
import java.util.Map;

public class HelloController {

	public static void index(Context ctx) {
		ctx.render("templates/hello.html", Map.of());
	}

}

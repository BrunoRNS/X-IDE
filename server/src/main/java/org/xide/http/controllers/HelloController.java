package org.xide.http.controllers;

import java.util.Map;

import io.javalin.http.Context;

public class HelloController {

	public static void index(Context ctx) {
		ctx.render("templates/hello.html", Map.of());
	}

}

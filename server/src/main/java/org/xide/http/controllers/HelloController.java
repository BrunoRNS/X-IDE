package org.xide.http.controllers;

import java.util.Map;

import io.javalin.http.Context;

/**
 * HTTP controller rendering the home page template.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
public class HelloController {

	public static void index(Context ctx) {
		ctx.render("templates/hello.html", Map.of());
	}

}

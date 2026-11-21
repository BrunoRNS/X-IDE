package org.xide.http.controllers;

import java.util.Map;
import java.util.logging.Logger;

import io.javalin.http.Context;

public class CompetitionController {

	private static final Logger LOGGER = Logger.getLogger(CompetitionController.class.getName());

	public static void index(Context ctx) {
		Map<String, Object> map = Map.of("status", true);
		ctx.render("templates/competition.html", map);
	}

}

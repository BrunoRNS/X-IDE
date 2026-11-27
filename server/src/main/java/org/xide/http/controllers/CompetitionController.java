package org.xide.http.controllers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.javalin.http.Context;
import org.xide.http.service.CompetitionService;
import org.xide.models.Problem;
import org.xide.repository.ProblemRepository;
import org.xide.states.CompetitionState;

/**
 * HTTP controller for competitions. Renders the competition page, starts a competition
 * with a time window and problem list ({@link org.xide.http.service.CompetitionService}),
 * and stops it ({@link org.xide.states.CompetitionState}).
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
public class CompetitionController {

	/**
	 * Request body for starting a competition: ISO start/end dates and the list of
	 * problem ids.
	 *
	 * @since v1.0.0
	 * @author LuisOtavio13
	 */
	public static record CompetitionRequest(String startDate, String endDate, List<Long> problemIds) {
	}

	public static void index(Context ctx) {
		Map<String, Object> map = Map.of("status", CompetitionState.isItStarted());
		ctx.render("templates/competition.html", map);
	}

	public static void start(Context ctx) {
		CompetitionRequest request = ctx.bodyAsClass(CompetitionRequest.class);
		LocalDateTime startDate;
		LocalDateTime endDate;
		try {
			startDate = java.time.OffsetDateTime.parse(request.startDate()).toLocalDateTime();
			endDate = java.time.OffsetDateTime.parse(request.endDate()).toLocalDateTime();
		}
		catch (java.time.format.DateTimeParseException e) {
			ctx.status(400).json(Map.of("error", "Invalid start or end date."));
			return;
		}
		List<Problem> problems = new ArrayList<>();

		for (Long id : request.problemIds()) {
			try {
				problems.add(ProblemRepository.selectProblemById(id));
			}
			catch (Exception e) {
				e.printStackTrace();
				ctx.status(404).json(Map.of("error", "Probem by id " + id));
				return;
			}
		}
		CompetitionService.start(endDate, startDate);
		ctx.json(Map.of("status", "started", "startDate", startDate.toString(), "endDate", endDate.toString()));
	}

	public static void stop(Context ctx) {
		CompetitionState.stop();
	}

}

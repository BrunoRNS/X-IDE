package org.xide.http.dto;

import java.util.List;

import org.xide.models.enums.BalloonColor;
import org.xide.models.enums.BigO;
import org.xide.models.enums.Lang;

/**
 * Request and response data structures for problem endpoints.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
public final class ProblemDtos {

	private ProblemDtos() {
	}

	/**
	 * DTO pairing a programming language with example source code for a problem.
	 *
	 * @since v1.0.0
	 * @author LuisOtavio13
	 */
	public record ExampleCode(Lang lang, String code) {
	}

	/**
	 * Request DTO carrying the id of the problem to fetch.
	 *
	 * @since v1.0.0
	 * @author LuisOtavio13
	 */
	public record GetProblemById(Long id) {
	}

	/**
	 * DTO with an example input/output pair for a problem.
	 *
	 * @since v1.0.0
	 * @author LuisOtavio13
	 */
	public record ExampleIO(String input, String output) {
	}

	/**
	 * DTO with per-language time limit in milliseconds for a problem.
	 *
	 * @since v1.0.0
	 * @author LuisOtavio13
	 */
	public record Limits(Lang lang, Double timeMs) {
	}

	/**
	 * Request DTO for creating or updating a problem: title, balloon color, expected
	 * {@link org.xide.models.enums.BigO}, recommendation, example codes, input/output
	 * examples, and per-language time limits.
	 *
	 * @since v1.0.0
	 * @author LuisOtavio13
	 */
	public record CreateOrUpdateProblem(String title, BalloonColor balloon, BigO bigO, String recommentation,
			List<ExampleCode> exampleCodes, List<ExampleIO> examples, List<Limits> limits) {
	}

	/**
	 * Summary DTO of a problem for listing: id, title, balloon CSS color, and
	 * {@link org.xide.models.enums.BigO}.
	 *
	 * @since v1.0.0
	 * @author LuisOtavio13
	 */
	public record DtoProblem(Long id, String title, String hexa, BigO bigo) {
	}

	/**
	 * Detailed DTO of a problem returned by id: id, title, balloon color,
	 * {@link org.xide.models.enums.BigO}, recommendation, example codes, input/output
	 * examples, per-language limits, and PDF URL.
	 *
	 * @since v1.0.0
	 * @author LuisOtavio13
	 */
	public record ProblemDetails(Long id, String title, String balloon, BigO bigo, String recommentation,
			List<ExampleCode> exampleCodes, List<ExampleIO> examples, List<Limits> limits, String pdfUrl) {
	}

}

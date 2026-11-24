package org.xide.http.dto;

import java.util.List;

import org.xide.models.enums.BalloonColor;
import org.xide.models.enums.BigO;
import org.xide.models.enums.Lang;

/** Request and response data structures for problem endpoints. */
public final class ProblemDtos {

	private ProblemDtos() {
	}

	public record ExampleCode(Lang lang, String code) {
	}

	public record GetProblemById(Long id) {
	}

	public record ExampleIO(String input, String output) {
	}

	public record Limits(Lang lang, Double timeMs) {
	}

	public record CreateOrUpdateProblem(String title, BalloonColor balloon, BigO bigO, String recommentation,
			List<ExampleCode> exampleCodes, List<ExampleIO> examples, List<Limits> limits) {
	}

	public record DtoProblem(Long id, String title, String hexa, BigO bigo) {
	}

	public record ProblemDetails(Long id, String title, String balloon, BigO bigo, String recommentation,
			List<ExampleCode> exampleCodes, List<ExampleIO> examples, List<Limits> limits, String pdfUrl) {
	}

}

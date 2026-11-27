package org.xide.http.service;

import java.util.ArrayList;
import java.util.List;

import io.javalin.http.Context;
import org.xide.http.dto.ProblemDtos.CreateOrUpdateProblem;
import org.xide.http.dto.ProblemDtos.ExampleCode;
import org.xide.http.dto.ProblemDtos.ExampleIO;
import org.xide.http.dto.ProblemDtos.Limits;
import org.xide.models.enums.BalloonColor;
import org.xide.models.enums.BigO;
import org.xide.models.enums.Lang;

/** Converts the multipart problem form into validated application data. */
public final class ProblemFormParser {

	private ProblemFormParser() {
	}

	public static CreateOrUpdateProblem parse(Context context, String title) {
		return new CreateOrUpdateProblem(title, BalloonColor.fromDatabase(context.formParam("color")),
				BigO.fromDatabase(context.formParam("bigO")), trim(context.formParam("recommentation")),
				parseCodeExamples(context), parseInputOutputExamples(context), parseLimits(context));
	}

	private static List<ExampleCode> parseCodeExamples(Context context) {
		List<String> codes = context.formParams("soluctionsLang[]");
		List<String> languages = context.formParams("langSoluctions[language]");
		List<ExampleCode> examples = new ArrayList<>();

		for (int i = 0; i < codes.size(); i++) {
			String code = trim(codes.get(i));
			if (code.isEmpty()) {
				continue;
			}

			String language = i < languages.size() ? languages.get(i) : null;
			examples.add(new ExampleCode(Lang.fromDatabase(language), code));
		}
		return examples;
	}

	private static List<ExampleIO> parseInputOutputExamples(Context context) {
		List<String> inputs = context.formParams("inputDescription[]");
		List<String> outputs = context.formParams("outputDescription[]");
		List<ExampleIO> examples = new ArrayList<>();

		for (int i = 0; i < Math.max(inputs.size(), outputs.size()); i++) {
			String input = i < inputs.size() ? trim(inputs.get(i)) : "";
			String output = i < outputs.size() ? trim(outputs.get(i)) : "";
			if (input.isEmpty() && output.isEmpty()) {
				continue;
			}
			if (input.isEmpty() || output.isEmpty()) {
				throw new IllegalArgumentException("Each example must have an input and an output.");
			}
			examples.add(new ExampleIO(input, output));
		}
		return examples;
	}

	private static List<Limits> parseLimits(Context context) {
		List<String> languages = context.formParams("langLimits[language]");
		List<String> times = context.formParams("langLimits[time]");
		List<Limits> limits = new ArrayList<>();

		for (int i = 0; i < languages.size(); i++) {
			Lang language = Lang.fromDatabase(languages.get(i));
			String rawTime = i < times.size() ? trim(times.get(i)) : "";
			double seconds;
			try {
				seconds = Double.parseDouble(rawTime);
			}
			catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid time limit for " + language + ".");
			}
			if (seconds < 1 || seconds > 60) {
				throw new IllegalArgumentException("Time limits must be between 1 and 60 seconds.");
			}
			limits.add(new Limits(language, seconds * 1000));
		}
		return limits;
	}

	private static String trim(String value) {
		return value == null ? "" : value.trim();
	}

}

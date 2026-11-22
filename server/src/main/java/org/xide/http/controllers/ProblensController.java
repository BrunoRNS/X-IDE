package org.xide.http.controllers;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xide.http.service.ProblensService;
import org.xide.http.service.validate.ProblensValidade;
import org.xide.models.Example;
import org.xide.models.Problem;
import org.xide.models.Soluction;
import org.xide.models.enums.BalloonColor;
import org.xide.models.enums.BigO;
import org.xide.models.enums.Lang;
import org.xide.repository.ProblemRepository;

import io.javalin.http.Context;
import io.javalin.http.UploadedFile;

public class ProblensController {

	private static final Logger LOGGER = Logger.getLogger(ProblensController.class.getName());

	public static record ExampleCode(Lang lang, String code) {
	}

	public static record ExampleIO(String input, String output) {
	}

	public static record Limits(Lang lang, Double timeMs) {
	}

	public static record DtoCreateNewProblem(String title, BalloonColor balloon, BigO bigO, String recommentation,
			List<ExampleCode> exampleCodes, List<ExampleIO> examples, List<Limits> limits) {
	}

	public static void registerNewProblem(Context ctx) {
		UploadedFile pdf = ctx.uploadedFile("pdf");
		String title = trim(ctx.formParam("title"));

		if (!ProblensValidade.validadeTitle(ctx, title) || !ProblensValidade.validadePdf(ctx, pdf)) {
			return;
		}

		try {
			DtoCreateNewProblem dto = parseForm(ctx, title);
			Path pdfPath = ProblensService.savePdf(pdf);
			Problem problem = save(dto, pdfPath);

			ctx.status(201)
				.json(Map.of("id", problem.getId(), "title", problem.getTitile(), "pdf", problem.getPdf().toString()));
		}
		catch (IllegalArgumentException e) {
			ctx.status(400).json(Map.of("error", e.getMessage()));
		}
		catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Could not create problem", e);
			ctx.status(500).json(Map.of("error", "Could not save the problem."));
		}
	}

	private static DtoCreateNewProblem parseForm(Context ctx, String title) {
		BalloonColor balloon = BalloonColor.fromDatabase(ctx.formParam("color"));
		BigO bigO = BigO.fromDatabase(ctx.formParam("bigO"));
		String recommentation = trim(ctx.formParam("recommentation"));

		return new DtoCreateNewProblem(title, balloon, bigO, recommentation, parseExampleCodes(ctx), parseExamples(ctx),
				parseLimits(ctx));
	}

	private static List<ExampleCode> parseExampleCodes(Context ctx) {
		List<String> codes = ctx.formParams("soluctionsLang[]");
		List<String> languages = ctx.formParams("langSoluctions[language]");
		List<ExampleCode> exampleCodes = new ArrayList<>();

		for (int i = 0; i < codes.size(); i++) {
			String code = trim(codes.get(i));
			if (code.isEmpty()) {
				continue;
			}
			String language = i < languages.size() ? languages.get(i) : null;
			exampleCodes.add(new ExampleCode(Lang.fromDatabase(language), code));
		}

		return exampleCodes;
	}

	private static List<ExampleIO> parseExamples(Context ctx) {
		List<String> inputs = ctx.formParams("inputDescription[]");
		List<String> outputs = ctx.formParams("outputDescription[]");
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

	private static List<Limits> parseLimits(Context ctx) {
		List<String> languages = ctx.formParams("langLimits[language]");
		List<String> times = ctx.formParams("langLimits[time]");
		List<Limits> limits = new ArrayList<>();

		for (int i = 0; i < languages.size(); i++) {
			Lang lang = Lang.fromDatabase(languages.get(i));
			String rawTime = i < times.size() ? trim(times.get(i)) : "";

			double seconds;
			try {
				seconds = Double.parseDouble(rawTime);
			}
			catch (NumberFormatException e) {
				throw new IllegalArgumentException("Invalid time limit for " + lang + ".");
			}
			if (seconds < 1 || seconds > 60) {
				throw new IllegalArgumentException("Time limits must be between 1 and 60 seconds.");
			}
			limits.add(new Limits(lang, seconds * 1000));
		}

		return limits;
	}

	private static Problem save(DtoCreateNewProblem dto, Path pdf) throws Exception {
		List<Example> examples = dto.exampleCodes()
			.stream()
			.map(exampleCode -> new Example(null, exampleCode.code(), exampleCode.lang()))
			.toList();
		List<Soluction> soluctions = dto.examples()
			.stream()
			.map(example -> new Soluction(example.input(), example.output(), null))
			.toList();

		Map<Lang, Double> times = new EnumMap<>(Lang.class);
		for (Limits limit : dto.limits()) {
			times.put(limit.lang(), limit.timeMs());
		}

		return ProblemRepository.insertProblem(dto.title(), dto.balloon(), dto.bigO(), pdf, dto.recommentation(), times,
				examples, soluctions);
	}

	private static String trim(String value) {
		return value == null ? "" : value.trim();
	}

}

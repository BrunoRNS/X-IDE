package org.xide.http.controllers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xide.http.dto.ProblemDtos.CreateOrUpdateProblem;
import org.xide.http.dto.ProblemDtos.ExampleCode;
import org.xide.http.dto.ProblemDtos.ExampleIO;
import org.xide.http.dto.ProblemDtos.GetProblemById;
import org.xide.http.dto.ProblemDtos.Limits;
import org.xide.http.dto.ProblemDtos.ProblemDetails;
import org.xide.http.service.ProblemFormParser;
import org.xide.http.service.ProblemManagementService;
import org.xide.http.service.validate.ProblensValidade;
import org.xide.models.Example;
import org.xide.models.Problem;
import org.xide.models.Soluction;
import org.xide.models.enums.BalloonColor;
import org.xide.repository.ProblemRepository;

import io.javalin.http.Context;
import io.javalin.http.UploadedFile;

public class ProblensController {

	private static final Logger LOGGER = Logger.getLogger(ProblensController.class.getName());

	public static void registerNewProblem(Context ctx) {
		UploadedFile pdf = ctx.uploadedFile("pdf");
		String title = trim(ctx.formParam("title"));

		if (!ProblensValidade.validadeTitle(ctx, title) || !ProblensValidade.validadePdf(ctx, pdf)) {
			return;
		}

		try {
			CreateOrUpdateProblem dto = ProblemFormParser.parse(ctx, title);
			Problem problem = ProblemManagementService.create(dto, pdf);

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

	public static void updateProblem(Context ctx) {
		Long id = parseProblemId(ctx);

		if (id == null) {
			return;
		}

		String title = trim(ctx.formParam("title"));

		if (!ProblensValidade.validadeTitle(ctx, title)) {
			return;
		}

		UploadedFile replacementPdf = optionalPdf(ctx);

		if (replacementPdf != null && !ProblensValidade.validadePdf(ctx, replacementPdf)) {
			return;
		}

		try {

			CreateOrUpdateProblem dto = ProblemFormParser.parse(ctx, title);

			if (!ProblemManagementService.update(id, dto, replacementPdf)) {
				ctx.status(404).json(Map.of("error", "Problem not found."));
				return;
			}

			ctx.status(200).json(Map.of("id", id, "title", title));
		}
		catch (IllegalArgumentException e) {
			ctx.status(400).json(Map.of("error", e.getMessage()));
		}
		catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Could not update problem " + id, e);
			ctx.status(500).json(Map.of("error", "Could not update the problem."));
		}
	}

	private static Long parseProblemId(Context ctx) {
		try {
			long id = Long.parseLong(ctx.pathParam("id"));
			if (id > 0) {
				return id;
			}
		}
		catch (NumberFormatException ignored) {
		}
		ctx.status(400).json(Map.of("error", "Invalid problem ID."));
		return null;
	}

	private static UploadedFile optionalPdf(Context ctx) {
		UploadedFile pdf = ctx.uploadedFile("pdf");
		return pdf == null || pdf.filename() == null || pdf.filename().isBlank() ? null : pdf;
	}

	public static void getProblemById(Context ctx) {
		GetProblemById body;
		try {
			body = ctx.bodyAsClass(GetProblemById.class);
		}
		catch (Exception e) {
			ctx.status(400).json(Map.of("error", "Send a JSON body with the problem id."));
			return;
		}

		if (body == null || body.id() == null || body.id() < 1) {
			ctx.status(400).json(Map.of("error", "The problem id is invalid."));
			return;
		}

		try {
			Problem problem = ProblemRepository.selectProblemById(body.id());
			if (problem == null) {
				ctx.status(404).json(Map.of("error", "Problem " + body.id() + " was not found."));
				return;
			}

			List<ExampleCode> exampleCodes = problem.getExamples() == null ? List.of()
					: problem.getExamples().stream().map(ProblensController::getExampleCode).toList();
			List<ExampleIO> examples = problem.getSoluctions() == null ? List.of()
					: problem.getSoluctions().stream().map(ProblensController::getExampleIO).toList();
			List<Limits> limits = problem.getTimes() == null ? List.of()
					: problem.getTimes()
						.entrySet()
						.stream()
						.map(entry -> new Limits(entry.getKey(), entry.getValue()))
						.toList();

			ProblemDetails dto = new ProblemDetails(problem.getId(), problem.getTitile(),
					problem.getBalao() == null ? BalloonColor.RED.cssColor() : problem.getBalao().cssColor(),
					problem.getBigO(), problem.getRestriction(), exampleCodes, examples, limits,
					"/problems/" + problem.getId() + "/pdf");
			ctx.status(200).json(dto);
		}
		catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Could not list problem by id= " + body.id(), e);
			ctx.status(500).json(Map.of("error", "Could not list problem by id= " + body.id()));
		}
	}

	public static void getProblemPdf(Context ctx) {
		Long id;

		try {
			id = Long.parseLong(ctx.pathParam("id"));

			if (id <= 0) {
				ctx.status(400).json(Map.of("error", "Invalid problem ID"));
				return;
			}
		}
		catch (NumberFormatException e) {
			ctx.status(400).json(Map.of("error", "Invalid problem ID"));
			return;
		}

		try {
			Problem problem = ProblemRepository.selectProblemById(id);

			if (problem == null || problem.getPdf() == null) {
				ctx.status(404).json(Map.of("error", "PDF not found"));
				return;
			}

			Path pdfPath = problem.getPdf();

			if (!Files.isRegularFile(pdfPath) || !Files.isReadable(pdfPath)) {
				ctx.status(404).json(Map.of("error", "PDF file not found"));
				return;
			}

			ctx.contentType("application/pdf");
			ctx.header("Content-Disposition", "inline; filename=\"problem-" + id + ".pdf\"");

			ctx.result(Files.readAllBytes(pdfPath));

		}
		catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Could not retrieve PDF for problem " + id, e);

			ctx.status(500).json(Map.of("error", "Could not retrieve PDF"));
		}
	}

	public static void deleteProblemById(Context ctx) {
		Long id = Long.parseLong(ctx.pathParam("id"));
		try {
			ProblemRepository.deleteProblemByid(id);
			ctx.status(200).json(Map.of("id", id));
		}
		catch (Exception e) {
			e.printStackTrace();
			LOGGER.log(Level.SEVERE, "Could not delete problem by id: " + id, e);
			ctx.status(500).json(Map.of("error", "Could not delete problem by id: " + id));
		}
	}

	private static ExampleCode getExampleCode(Example e) {
		return new ExampleCode(e.getLang(), e.getCode());
	}

	private static ExampleIO getExampleIO(Soluction solution) {
		return new ExampleIO(solution.getIn(), solution.getOut());
	}

	public static void getAllProblem(Context ctx) {
		try {
			ctx.status(200).json(ProblemRepository.findAllProblem());
		}
		catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Could not list all problems", e);
			ctx.status(500).json(Map.of("error", "Could not list all problems"));
		}
	}

	private static String trim(String value) {
		return value == null ? "" : value.trim();
	}

}

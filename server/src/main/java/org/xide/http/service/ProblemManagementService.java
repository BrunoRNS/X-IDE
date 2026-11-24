package org.xide.http.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xide.http.dto.ProblemDtos.CreateOrUpdateProblem;
import org.xide.http.dto.ProblemDtos.ExampleCode;
import org.xide.http.dto.ProblemDtos.ExampleIO;
import org.xide.http.dto.ProblemDtos.Limits;
import org.xide.models.Example;
import org.xide.models.Problem;
import org.xide.models.Soluction;
import org.xide.models.enums.Lang;
import org.xide.repository.ProblemRepository;

import io.javalin.http.UploadedFile;

public final class ProblemManagementService {

	private static final Logger LOGGER = Logger.getLogger(ProblemManagementService.class.getName());

	private ProblemManagementService() {
	}

	public static Problem create(CreateOrUpdateProblem data, UploadedFile uploadedPdf) throws Exception {
		Path pdf = ProblensService.savePdf(uploadedPdf);
		try {
			return ProblemRepository.insertProblem(data.title(), data.balloon(), data.bigO(), pdf,
					data.recommentation(), toTimes(data.limits()), toCodeExamples(data.exampleCodes()),
					toInputOutputExamples(data.examples()));
		}
		catch (Exception e) {
			deleteUploadedPdf(pdf, e);
			throw e;
		}
	}

	public static boolean update(long id, CreateOrUpdateProblem data, UploadedFile replacementPdf) throws Exception {
		Problem existing = ProblemRepository.selectProblemById(id);
		if (existing == null) {
			return false;
		}

		Path newPdf = replacementPdf == null ? null : ProblensService.savePdf(replacementPdf);
		boolean updated;
		try {
			updated = ProblemRepository.updateProblem(id, data.title(), data.balloon(), data.bigO(), newPdf,
					data.recommentation(), toTimes(data.limits()), toCodeExamples(data.exampleCodes()),
					toInputOutputExamples(data.examples()));
		}
		catch (Exception e) {
			deleteUploadedPdf(newPdf, e);
			throw e;
		}

		if (!updated) {
			deleteUploadedPdf(newPdf, null);
			return false;
		}
		if (newPdf != null && existing.getPdf() != null) {
			try {
				Files.deleteIfExists(existing.getPdf());
			}
			catch (Exception e) {
				LOGGER.log(Level.WARNING, "Could not remove replaced PDF for problem " + id, e);
			}
		}
		return true;
	}

	private static List<Example> toCodeExamples(List<ExampleCode> examples) {
		return examples.stream().map(example -> new Example(null, example.code(), example.lang())).toList();
	}

	private static List<Soluction> toInputOutputExamples(List<ExampleIO> examples) {
		return examples.stream().map(example -> new Soluction(example.input(), example.output(), null)).toList();
	}

	private static Map<Lang, Double> toTimes(List<Limits> limits) {
		Map<Lang, Double> times = new EnumMap<>(Lang.class);
		for (Limits limit : limits) {
			times.put(limit.lang(), limit.timeMs());
		}
		return times;
	}

	private static void deleteUploadedPdf(Path pdf, Exception originalError) {
		if (pdf == null) {
			return;
		}
		try {
			Files.deleteIfExists(pdf);
		}
		catch (Exception cleanupError) {
			if (originalError != null) {
				originalError.addSuppressed(cleanupError);
			}
			else {
				LOGGER.log(Level.WARNING, "Could not remove unused uploaded PDF " + pdf, cleanupError);
			}
		}
	}

}

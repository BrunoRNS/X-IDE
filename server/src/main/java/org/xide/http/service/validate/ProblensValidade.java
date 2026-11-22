package org.xide.http.service.validate;

import java.util.Locale;
import java.util.Map;

import io.javalin.http.Context;
import io.javalin.http.UploadedFile;

public class ProblensValidade {

	public static boolean validadePdf(Context ctx, UploadedFile uploadedFile) {
		if (uploadedFile == null) {
			ctx.status(400).json(Map.of("error", "Choose a PDF file."));
			return false;
		}

		String contentType = uploadedFile.contentType();
		String filename = uploadedFile.filename();
		boolean pdfContentType = contentType != null && "application/pdf".equalsIgnoreCase(contentType);
		boolean pdfExtension = filename != null && filename.toLowerCase(Locale.ROOT).endsWith(".pdf");
		if (!pdfContentType && !pdfExtension) {
			ctx.status(415).json(Map.of("error", "The problem statement must be a PDF file."));
			return false;
		}
		return true;
	}

	public static boolean validadeTitle(Context ctx, String title) {
		String value = title == null ? "" : title.trim();
		if (value.length() < 5 || value.length() > 100) {
			ctx.status(400).json(Map.of("error", "The title must have between 5 and 100 characters."));
			return false;
		}
		return true;
	}

}

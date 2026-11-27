package org.xide.http.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * Tests that the classroom page template and public script contain the expected elements
 * and markers.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
class ClassPageAssetsTest {

	@Test
	void templateContainsTheFolderPickerAndRoomsGrid() throws IOException {
		String html = resource("/templates/class.html");

		assertTrue(html.contains("id=\"folderInput\""));
		assertTrue(html.contains("webkitdirectory"));
		assertTrue(html.contains("id=\"roomsGrid\""));
		assertTrue(html.contains("id=\"emptyState\""));
		assertTrue(html.contains("/public/classroom.js"));
	}

	@Test
	void templateRendersTheFolderPickerAndHighlightsTheClassMenu() {
		ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
		resolver.setTemplateMode(TemplateMode.HTML);
		resolver.setSuffix(".html");
		resolver.setCharacterEncoding("UTF-8");

		TemplateEngine engine = new TemplateEngine();
		engine.setTemplateResolver(resolver);

		String html = engine.process("templates/class.html",
				new Context(Locale.US, Map.of("requestPath", "/classroom")));

		assertTrue(html.contains("id=\"folderInput\""));
		assertTrue(html.contains("id=\"roomsGrid\""));
		assertTrue(html.contains("text-red-600 border-red-600"));
	}

	@Test
	void scriptUploadsThePathManifestAndLoadsRoomsFromTheApi() throws IOException {
		String script = resource("/public/classroom.js");

		assertTrue(script.contains("formData.append('paths'"));
		assertTrue(script.contains("formData.append('files'"));
		assertTrue(script.contains("${API_URL}/${roomId}"));
		assertTrue(script.contains("fetch(API_URL"));
		assertTrue(script.contains("navigator.clipboard"));
	}

	private String resource(String path) throws IOException {
		try (InputStream stream = getClass().getResourceAsStream(path)) {
			assertNotNull(stream, path);
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

}

package org.xide.http.controllers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class CompetitionPageAssetsTest {

	@Test
	public void modalScriptSendsUtcIsoDates() throws IOException {
		String script = resource("/public/problems/modal.js");

		assertTrue(script.contains("new Date(startDate).toISOString()"));
		assertTrue(script.contains("new Date(endDate).toISOString()"));
	}

	@Test
	public void modalScriptDeclaresTheEndCountdownTimer() throws IOException {
		String script = resource("/public/problems/modal.js");

		assertTrue(script.contains("let endCountdownTimer = null;"));
	}

	@Test
	public void modalScriptDefinesTheCountdownLifecycle() throws IOException {
		String script = resource("/public/problems/modal.js");

		assertTrue(script.contains("function startCountdown()"));
		assertTrue(script.contains("function runCountdown("));
		assertTrue(script.contains("function runEndCountdown("));
		assertTrue(script.contains("function showCompetitionStarted()"));
		assertTrue(script.contains("function hideCountdown()"));
	}

	@Test
	public void competitionTemplateExposesTheCountdownOverlayElements() throws IOException {
		String html = resource("/templates/competition.html");

		assertTrue(html.contains("id=\"countdownOverlay\""));
		assertTrue(html.contains("id=\"countdownLabel\""));
		assertTrue(html.contains("id=\"countdownValue\""));
		assertTrue(html.contains("id=\"countdownEnd\""));
	}

	private String resource(String path) throws IOException {
		try (InputStream stream = getClass().getResourceAsStream(path)) {
			assertNotNull(path, stream);
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

}
package org.xide.http.controllers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xide.http.routers.Routers;
import org.xide.states.CompetitionState;

/**
 * Integration tests for {@link org.xide.http.controllers.CompetitionController} endpoints
 * with a real Javalin app and reset competition state.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
class CompetitionControllerTest {

	private static final ObjectMapper JSON = new ObjectMapper();

	private final HttpClient http = HttpClient.newHttpClient();

	private Javalin app;

	@BeforeEach
	@SuppressWarnings("unused")
	void setUp() {
		resetCompetition();
		app = Javalin.create(Routers::register).start(0);
	}

	@AfterEach
	@SuppressWarnings("unused")
	void tearDown() {
		if (app != null) {
			app.stop();
		}
		resetCompetition();
	}

	private static void resetCompetition() {
		CompetitionState.stop();
		CompetitionState.setStartDate(null);
		CompetitionState.setEndDate(null);
	}

	@Test
	void startAcceptsUtcIsoDatesAndStoresThemAsUtcLocalDateTime() throws Exception {
		HttpResponse<String> response = post("""
				{"startDate":"2026-11-25T17:30:00Z","endDate":"2026-11-25T19:30:00Z","problemIds":[]}""");

		assertEquals(200, response.statusCode());

		JsonNode body = JSON.readTree(response.body());
		assertEquals("started", body.get("status").asText());
		assertEquals("2026-11-25T17:30", body.get("startDate").asText());
		assertEquals("2026-11-25T19:30", body.get("endDate").asText());

		assertEquals(LocalDateTime.of(2026, 11, 25, 17, 30), CompetitionState.getStartDate());
		assertEquals(LocalDateTime.of(2026, 11, 25, 19, 30), CompetitionState.getEndDate());
	}

	@Test
	void startRejectsLocalDateTimeWithoutOffset() throws Exception {
		HttpResponse<String> response = post("""
				{"startDate":"2026-11-25T17:30:00","endDate":"2026-11-25T19:30:00","problemIds":[]}""");

		assertEquals(400, response.statusCode());
		assertTrue(JSON.readTree(response.body()).get("error").asText().contains("Invalid"));
	}

	@Test
	void startRejectsUnparseableDates() throws Exception {
		HttpResponse<String> response = post("""
				{"startDate":"not-a-date","endDate":"also-bad","problemIds":[]}""");

		assertEquals(400, response.statusCode());
	}

	private HttpResponse<String> post(String json) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(uri("/competition/start"))
			.header("Content-Type", "application/json")
			.header("Accept", "application/json")
			.POST(HttpRequest.BodyPublishers.ofString(json))
			.build();

		return http.send(request, HttpResponse.BodyHandlers.ofString());
	}

	private URI uri(String path) {
		return URI.create("http://127.0.0.1:" + app.port() + path);
	}

}
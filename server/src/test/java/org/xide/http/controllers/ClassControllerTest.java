package org.xide.http.controllers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xide.http.routers.Routers;

/**
 * Integration tests for {@link org.xide.http.controllers.ClassController}: folder upload
 * security, hosted server lifecycle, and JSON responses, using a Javalin app and
 * temporary directory.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
class ClassControllerTest {

	private static final ObjectMapper JSON = new ObjectMapper();

	private static final String UPLOADS_DIRECTORY_PROPERTY = "xide.uploads.dir";

	private static final String FIRST_PORT_PROPERTY = "xide.classroom.port";

	private final HttpClient http = HttpClient.newHttpClient();

	private Javalin app;

	private String previousUploadsDirectory;

	private String previousFirstPort;

	private int firstClassroomPort;

	@TempDir
	Path temporaryFolder;

	@BeforeEach
	@SuppressWarnings("unused")
	void setUp() throws IOException {
		try (ServerSocket socket = new ServerSocket(0)) {
			firstClassroomPort = socket.getLocalPort();
		}

		previousUploadsDirectory = System.getProperty(UPLOADS_DIRECTORY_PROPERTY);
		previousFirstPort = System.getProperty(FIRST_PORT_PROPERTY);
		System.setProperty(UPLOADS_DIRECTORY_PROPERTY, temporaryFolder.resolve("uploads").toAbsolutePath().toString());
		System.setProperty(FIRST_PORT_PROPERTY, String.valueOf(firstClassroomPort));
		app = Javalin.create(Routers::register).start(0);
	}

	@AfterEach
	@SuppressWarnings("unused")
	void tearDown() {
		ClassController.stopAll();

		if (app != null) {
			app.stop();
		}

		restoreProperty(UPLOADS_DIRECTORY_PROPERTY, previousUploadsDirectory);
		restoreProperty(FIRST_PORT_PROPERTY, previousFirstPort);
	}

	private static void restoreProperty(String name, String value) {
		if (value == null) {
			System.clearProperty(name);
		}
		else {
			System.setProperty(name, value);
		}
	}

	@Test
	void uploadsAFolderAndServesHtmlAssetsAndPdfs() throws Exception {
		HttpResponse<String> response = postClass(21,
				List.of(new Upload("index.html", "text/html", "<h1>Hello classroom</h1>"),
						new Upload("assets/style.css", "text/css", "h1 { color: #dc2626; }"),
						new Upload("notes.pdf", "application/pdf", "%PDF-1.4 fake")));

		assertEquals(200, response.statusCode());
		assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith("application/json"));

		JsonNode room = json(response);
		assertEquals(21, room.get("id").asLong());
		assertEquals(3, room.get("files").asInt());
		assertFalse(room.get("ip").asText().isBlank());
		assertTrue(room.get("port").asInt() >= firstClassroomPort);
		assertEquals("http://" + room.get("ip").asText() + ":" + room.get("port").asInt(), room.get("url").asText());

		int port = room.get("port").asInt();
		assertServed(port, "/", "text/html", "<h1>Hello classroom</h1>");
		assertServed(port, "/assets/style.css", "text/css", "h1 { color: #dc2626; }");
		assertServed(port, "/notes.pdf", "application/pdf", "%PDF-1.4 fake");
	}

	@Test
	void listsRoomsInIdOrderAndReturnsASingleRoom() throws Exception {
		JsonNode emptyRooms = JSON.readTree(get("/class").body());
		assertTrue(emptyRooms.isArray());
		assertEquals(0, emptyRooms.size());

		postClass(9, List.of(new Upload("index.html", "text/html", "nine")));
		JsonNode seven = json(postClass(7, List.of(new Upload("index.html", "text/html", "seven"))));

		HttpResponse<String> listResponse = get("/class");
		assertEquals(200, listResponse.statusCode());

		JsonNode rooms = JSON.readTree(listResponse.body());
		assertTrue(rooms.isArray());
		assertEquals(2, rooms.size());
		assertEquals(7, rooms.get(0).get("id").asLong());
		assertEquals(9, rooms.get(1).get("id").asLong());

		HttpResponse<String> roomResponse = get("/class/7");
		assertEquals(200, roomResponse.statusCode());

		JsonNode singleRoom = json(roomResponse);
		assertEquals(7, singleRoom.get("id").asLong());
		assertEquals(seven.get("port").asInt(), singleRoom.get("port").asInt());
		assertEquals(seven.get("url").asText(), singleRoom.get("url").asText());
		assertEquals(1, singleRoom.get("files").asInt());

		assertEquals(404, get("/class/99").statusCode());
		assertEquals(400, get("/class/not-a-number").statusCode());
	}

	@Test
	void uploadingTheSameRoomAgainReplacesItsServerAndFiles() throws Exception {
		JsonNode first = json(postClass(4,
				List.of(new Upload("index.html", "text/html", "first"), new Upload("old.txt", "text/plain", "old"))));
		JsonNode second = json(postClass(4, List.of(new Upload("index.html", "text/html", "second"))));

		assertNotEquals(first.get("port").asInt(), second.get("port").asInt());

		JsonNode rooms = JSON.readTree(get("/class").body());
		assertEquals(1, rooms.size());
		assertEquals(4, rooms.get(0).get("id").asLong());
		assertEquals(second.get("port").asInt(), rooms.get(0).get("port").asInt());

		assertServed(second.get("port").asInt(), "/", "text/html", "second");
		assertNotServed(second.get("port").asInt(), "/old.txt");
	}

	@Test
	void rejectsMissingFilesAndInvalidIds() throws Exception {
		assertEquals(400, postClass(3, List.of()).statusCode());
		assertEquals(400, postClass(0, List.of(new Upload("index.html", "text/html", "zero"))).statusCode());
	}

	@Test
	void rejectsPathsThatEscapeTheRoomDirectory() throws Exception {
		HttpResponse<String> response = postClass(5, List.of(new Upload("../escaped.txt", "text/plain", "escaped")));

		assertEquals(500, response.statusCode());
		assertFalse(Files.exists(Path.of(System.getProperty(UPLOADS_DIRECTORY_PROPERTY), "escaped.txt")));
		assertTrue(json(response).get("error").asText().contains("Could not host"));
	}

	private HttpResponse<String> postClass(long id, List<Upload> uploads) throws Exception {
		String boundary = "----xide-" + UUID.randomUUID();
		ByteArrayOutputStream body = new ByteArrayOutputStream();

		for (Upload upload : uploads) {
			writeField(body, boundary, "paths", upload.path());
			writeFile(body, boundary, upload);
		}

		body.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

		HttpRequest request = HttpRequest.newBuilder(uri("/class/" + id))
			.header("Content-Type", "multipart/form-data; boundary=" + boundary)
			.header("Accept", "application/json")
			.POST(HttpRequest.BodyPublishers.ofByteArray(body.toByteArray()))
			.build();

		return http.send(request, HttpResponse.BodyHandlers.ofString());
	}

	private HttpResponse<String> get(String path) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(uri(path)).header("Accept", "application/json").GET().build();

		return http.send(request, HttpResponse.BodyHandlers.ofString());
	}

	private URI uri(String path) {
		return URI.create("http://127.0.0.1:" + app.port() + path);
	}

	private JsonNode json(HttpResponse<String> response) throws IOException {
		return JSON.readTree(response.body());
	}

	private void assertServed(int port, String path, String contentType, String body) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).GET().build();
		HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(200, response.statusCode());
		assertTrue(response.headers().firstValue("Content-Type").orElse("").startsWith(contentType));
		assertEquals(body, response.body());
	}

	private void assertNotServed(int port, String path) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).GET().build();
		HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

		assertEquals(404, response.statusCode());
	}

	private static void writeField(ByteArrayOutputStream out, String boundary, String name, String value)
			throws IOException {
		out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
		out.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
		out.write(value.getBytes(StandardCharsets.UTF_8));
		out.write("\r\n".getBytes(StandardCharsets.UTF_8));
	}

	private static void writeFile(ByteArrayOutputStream out, String boundary, Upload upload) throws IOException {
		String filename = Path.of(upload.path()).getFileName().toString().replace("\"", "");
		byte[] content = upload.content().getBytes(StandardCharsets.UTF_8);

		out.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
		out.write(("Content-Disposition: form-data; name=\"files\"; filename=\"" + filename + "\"\r\n")
			.getBytes(StandardCharsets.UTF_8));
		out.write(("Content-Type: " + upload.contentType() + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
		out.write(content);
		out.write("\r\n".getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Test fixture pairing a file name with its content for uploads.
	 *
	 * @since v1.0.0
	 * @author LuisOtavio13
	 */
	private record Upload(String path, String contentType, String content) {
	}

}

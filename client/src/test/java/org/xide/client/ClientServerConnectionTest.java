package org.xide.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xide.client.sockets.WebSocketClient;
import org.xide.protocol.SocketMessage;

public class ClientServerConnectionTest {

	private static final String WS_PATH = "/ws/socket";

	private static final long RESPONSE_TIMEOUT_SECONDS = 10;

	private static final long CONNECT_TIMEOUT_SECONDS = 60;

	private static final long HEALTH_TIMEOUT_SECONDS = 180;

	private static final long STACK_TIMEOUT_MINUTES = 10;

	private static Path projectRoot;

	private static int httpPort;

	private static WebSocketClient client;

	private static boolean stackAlreadyRunning;

	@BeforeClass
	public static void startStackAndConnect() throws Exception {
		Assume.assumeTrue("Integration test disabled: enable it with `make test-integration` (or -Dsocket.it=true)",
				Boolean.getBoolean("socket.it"));
		projectRoot = findProjectRoot();
		requireDocker();
		ensureEnvFile();
		httpPort = readPort("HTTP_PORT", 8080);

		stackAlreadyRunning = !compose(STACK_TIMEOUT_MINUTES * 60_000L, false, "ps", "-q").output().trim().isEmpty();
		if (!stackAlreadyRunning) {
			compose(STACK_TIMEOUT_MINUTES * 60_000L, true, "up", "--build", "--detach");
		}
		awaitHealthy();
		client = connect();
	}

	@AfterClass
	public static void stopClientAndStack() {
		if (client != null) {
			try {
				client.close();
			}
			catch (IOException e) {
				System.err.println("Failed to close WebSocket client: " + e.getMessage());
			}
		}
		if (!stackAlreadyRunning && projectRoot != null) {
			compose(STACK_TIMEOUT_MINUTES * 60_000L, true, "down", "--remove-orphans");
		}
	}

	@Test(timeout = 60_000)
	public void serverAnswersPongWhenClientSendsPing() throws Exception {
		client.sendRequest("/develop/ping", SocketMessage.PING).get();

		SocketMessage response = client.receive("/develop/ping", RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);

		assertEquals(SocketMessage.textResponse("/develop/ping", SocketMessage.PONG), response);
	}

	@Test(timeout = 60_000)
	public void clientAnswersPongWhenServerSendsPing() throws Exception {
		// Ask the server to push a ping to every connected client (this one included).
		client.sendRequest("/develop/ping-clients", "go").get();

		// The client answers the pushed ping with a pong, which the server
		// records; poll until it shows up.
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(RESPONSE_TIMEOUT_SECONDS);
		SocketMessage response = null;
		while (System.nanoTime() < deadline) {
			client.sendRequest("/develop/last-pong", "check").get();
			SocketMessage last = client.receive("/develop/last-pong", 1, TimeUnit.SECONDS);
			if (last != null && SocketMessage.PONG.equals(last.payload().asText())) {
				response = last;
				break;
			}
			Thread.sleep(200);
		}

		assertEquals(SocketMessage.textResponse("/develop/last-pong", SocketMessage.PONG), response);
	}

	private static void requireDocker() {
		CommandResult result = run(15_000L, "docker", "info");
		if (!result.success()) {
			fail("Docker is required for this integration test but is not usable"
					+ " (try 'docker ps' yourself; on permission denied: sudo usermod -aG docker $USER"
					+ " and re-login): " + result.output().trim());
		}
	}

	private static Path findProjectRoot() {
		Path directory = Path.of(System.getProperty("user.dir")).toAbsolutePath();
		while (directory != null) {
			if (Files.exists(directory.resolve("infra/compose.yaml"))) {
				return directory;
			}
			directory = directory.getParent();
		}
		throw new IllegalStateException(
				"Project root with infra/compose.yaml not found from " + System.getProperty("user.dir"));
	}

	private static void ensureEnvFile() throws IOException {
		if (Files.notExists(projectRoot.resolve(".env"))) {
			Files.copy(projectRoot.resolve(".env.example"), projectRoot.resolve(".env"));
		}
	}

	private static int readPort(String key, int defaultPort) throws IOException {
		for (String line : Files.readAllLines(projectRoot.resolve(".env"))) {
			if (line.startsWith(key + "=")) {
				return Integer.parseInt(line.substring(key.length() + 1).trim());
			}
		}
		return defaultPort;
	}

	private static CommandResult compose(long timeoutMillis, boolean inheritOutput, String... args) {
		List<String> command = new ArrayList<>(
				List.of("docker", "compose", "--env-file", ".env", "-f", "infra/compose.yaml"));
		command.addAll(List.of(args));
		return run(timeoutMillis, inheritOutput, command.toArray(new String[0]));
	}

	private static void awaitHealthy() throws Exception {
		HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
		HttpRequest request = HttpRequest.newBuilder()
			.uri(URI.create("http://127.0.0.1:" + httpPort + "/healthz"))
			.timeout(Duration.ofSeconds(5))
			.build();
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(HEALTH_TIMEOUT_SECONDS);
		while (System.nanoTime() < deadline) {
			try {
				HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
				if (response.statusCode() == 200) {
					return;
				}
			}
			catch (IOException notUpYet) {
				// keep waiting for Nginx
			}
			Thread.sleep(1_000);
		}
		fail("Compose stack did not become healthy within " + HEALTH_TIMEOUT_SECONDS + "s");
	}

	private static WebSocketClient connect() throws Exception {

		URI uri = URI.create("ws://127.0.0.1:" + httpPort + WS_PATH);
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(CONNECT_TIMEOUT_SECONDS);
		while (true) {
			try {
				return WebSocketClient.getInstance(uri);
			}
			catch (IOException | jakarta.websocket.DeploymentException notUpYet) {
				if (System.nanoTime() > deadline) {
					throw notUpYet;
				}
				Thread.sleep(1_000);
			}
		}
	}

	private static CommandResult run(long timeoutMillis, String... command) {
		return run(timeoutMillis, false, command);
	}

	private static CommandResult run(long timeoutMillis, boolean inheritOutput, String... command) {
		ProcessBuilder builder = new ProcessBuilder(command).directory(projectRoot.toFile()).redirectErrorStream(true);
		if (inheritOutput) {
			builder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
		}
		try {
			Process process = builder.start();
			if (!process.waitFor(timeoutMillis, TimeUnit.MILLISECONDS)) {
				process.destroyForcibly();
				return new CommandResult(false, "timed out after " + timeoutMillis + "ms");
			}
			String output = inheritOutput ? "(output above)"
					: new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			return new CommandResult(process.exitValue() == 0, output);
		}
		catch (Exception e) {
			return new CommandResult(false, e.toString());
		}
	}

	private record CommandResult(boolean success, String output) {
	}

}

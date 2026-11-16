package org.xide.sockets;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HexFormat;
import java.util.List;
import org.glassfish.tyrus.server.Server;
import org.xide.Main;
import org.xide.sockets.annotations.WebSocketRouter;
import org.xide.sockets.annotations.WebSocketScanner;
import org.xide.sockets.endpoints.WebSocketEndpoint;
import org.xide.utils.EnvRead;

public final class WebSocketServer {

	private static WebSocketServer instance;

	private final WebSocketRouter router = new WebSocketRouter();

	private final Server server;

	private volatile boolean running;

	private WebSocketServer(String controllerPackage) {
		List<Object> controllers = WebSocketScanner.scan(controllerPackage);
		controllers.forEach(router::register);
		server = new Server("127.0.0.1", 8082, "/ws", null, WebSocketEndpoint.class);
	}

	public static synchronized WebSocketServer getInstance() {
		if (instance == null) {
			instance = new WebSocketServer(Main.class.getPackageName());
		}
		return instance;
	}

	public synchronized void start() throws Exception {
		if (running) {
			return;
		}
		server.start();
		running = true;
		System.out.println(
				"████████████████████████████████████████████████████████████████████████████████████████████████████\n"
						+ //
						"████████████████████████████████████████████████████████████████████████████████████████████████████\n"
						+ //
						"████████████████████████████████████████████████████████████████████████████████████████████████████\n"
						+ //
						"████████████████████████████████████████████████████████████████████████████████████████████████████\n"
						+ //
						"████████████████████████████████████████████████████████████████████████████████████████████████████\n"
						+ //
						"████████████████████████████████████████████████████████████████████████████████████████████████████\n"
						+ //
						"███████████████████████████████▓▓▓▓▓▓▓▓▒▒▓██████████████████████████████████████████████████████████\n"
						+ //
						"█████████████████████████████▓▒▒▓▓▓▓▓▓▓▓▒▒▓█████████████████████████████████████████████████████████\n"
						+ //
						"████████████████████████████▓▒▒▓▓▓▓▓▓▓▓▓▓▓▓▓████████████████████████████████████████████████████████\n"
						+ //
						"████████████████████████████▓▓▓▒░░░▓▓▓▒░░░▓█████████████████████████████████████████████████████████\n"
						+ //
						"███████████████████████████▓▓▓▓▓▓▒░░░░░░▓███████▓░░▓█████████░░░████████████████████████████████████\n"
						+ //
						"████████████████████████████▓▓▓▓██▓░░░▒▓█████████▒▒▓██▓▒░░░░▓░░░███▒░░░░▒███████████████████████████\n"
						+ //
						"████████████████████████████████▓▒░░░░░░▓███████▓░░▓▓░░░▓██▒░░░░█░░░▓██▓░░░█████████████████████████\n"
						+ //
						"███████████████████████████████▒░░░▓██▒░░░██████▓░░▒▒░▓██████░░░▒░░░░░░░░░░▓████████████████████████\n"
						+ //
						"████████████████████████████████████████████████▓░░▓▒░░██████░░▒▓░░▓████████████████████████████████\n"
						+ //
						"████████████████████████████████████████████████▓░░▓█▒░░░░░░░░▓██▓░░░░░░░░▓█████████████████████████\n"
						+ //
						"████████████████████████████████████████████████████████████████████████████████████████████████████\n"
						+ //
						"████████████████████████████████████████████████████████████████████████████████████████████████████\n"
						+ //
						"████████████████████████████████████████████████████████████████████████████████████████████████████\n"
						+ //
						"████████████████████████████████████████████████████████████████████████████████████████████████████\n"
						+ //
						"████████████████████████████████████████████████████████████████████████████████████████████████████\n"
						+ //
						"████████████████████████████████████████████████████████████████████████████████████████████████████\n"
						+ //
						"████████████████████████████████████████████████████████████████████████████████████████████████████\n"
						+ //
						"████████████████████████████████████████████████████████████████████████████████████████████████████\n"
						+ //
						"████████████████████████████████████████████████████████████████████████████████████████████████████\n"
						+ //
						"████████████████████████████████████████████████████████████████████████████████████████████████████\n"
						+ //
						"████████████████████████████████████████████████████████████████████████████████████████████████████\n"
						+ //
						"████████████████████████████████████████████████████████████████████████████████████████████████████\n"
						+ //
						"████████████████████████████████████████████████████████████████████████████████████████████████████\n"
						+ //
						"████████████████████████████████████████████████████████████████████████████████████████████████████");
		printStudentConnectionInfo();
	}

	/**
	 * Prints the WSS URL and certificate fingerprint the instructor shares with students.
	 * TLS_HOST comes from the container environment (auto-detected by
	 * {@code make run-server} when unset in .env); the fingerprint matches the format
	 * accepted by the client's {@code
	 * XIDE_TLS_FINGERPRINT} and TOFU pin.
	 */
	private void printStudentConnectionInfo() {
		System.out.println("Students connect to: wss://" + publicHost() + ":" + publicPort() + "/ws/socket");
		String fingerprint = certificateFingerprint();
		if (fingerprint != null) {
			System.out.println("Certificate SHA-256 fingerprint (XIDE_TLS_FINGERPRINT): " + fingerprint);
		}
	}

	/** TLS_HOST from the environment, falling back to a detected site-local IP. */
	private static String publicHost() {
		String configured = System.getenv("TLS_HOST");
		if (configured == null || configured.isBlank()) {
			configured = EnvRead.get("TLS_HOST");
		}
		if (configured != null && !configured.isBlank()) {
			return configured;
		}
		try {
			InetAddress address = InetAddress.getLocalHost();
			if (!address.isLoopbackAddress() && address.isSiteLocalAddress()) {
				return address.getHostAddress();
			}
		}
		catch (UnknownHostException e) {
			// No resolvable non-loopback address; fall through to localhost.
		}
		return "localhost";
	}

	/** Published HTTPS/WSS port of the Nginx edge (host-side port mapping). */
	private static String publicPort() {
		String port = System.getenv("HTTPS_PORT");
		if (port == null || port.isBlank()) {
			port = EnvRead.get("HTTPS_PORT");
		}
		return port == null || port.isBlank() ? "8443" : port;
	}

	/** Best-effort SHA-256 fingerprint of the Nginx edge certificate, or null. */
	private static String certificateFingerprint() {
		File certificateFile = new File("/etc/nginx/certs/server.crt");
		if (!certificateFile.isFile()) {
			return null;
		}
		try (FileInputStream input = new FileInputStream(certificateFile)) {
			CertificateFactory factory = CertificateFactory.getInstance("X.509");
			X509Certificate certificate = (X509Certificate) factory.generateCertificate(input);
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(certificate.getEncoded());
			return HexFormat.ofDelimiter(":").withUpperCase().formatHex(digest);
		}
		catch (Exception e) {
			return null;
		}
	}

	public synchronized void stop() {
		if (!running) {
			return;
		}
		server.stop();
		running = false;
	}

	public boolean isRunning() {
		return running;
	}

	public WebSocketRouter getRouter() {
		return router;
	}

}

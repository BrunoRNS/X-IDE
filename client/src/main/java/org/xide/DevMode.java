package org.xide;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * Detects whether the desktop client is running in development mode.
 *
 * <p>
 * Development mode relaxes kiosk restrictions (such as the single-monitor requirement) so
 * the client can run on a normal developer workstation with any number of monitors. It is
 * enabled by the {@code XIDE_DEV_MODE} environment variable or the {@code xide.dev.mode}
 * system property, and automatically when the client runs from exploded classes (IDE or
 * {@code target/classes}) instead of the packaged JAR.
 */
public final class DevMode {

	private static final String ENV_VARIABLE = "XIDE_DEV_MODE";

	private static final String SYSTEM_PROPERTY = "xide.dev.mode";

	private DevMode() {
	}

	/**
	 * @return whether the client should run with development restrictions relaxed
	 */
	public static boolean isActive() {
		Boolean configured = configuredValue(System.getenv(ENV_VARIABLE));
		if (configured == null) {
			configured = configuredValue(System.getProperty(SYSTEM_PROPERTY));
		}
		return configured != null ? configured : runsFromExplodedClasses();
	}

	private static Boolean configuredValue(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return switch (value.trim().toLowerCase(Locale.ROOT)) {
			case "1", "true", "yes", "on" -> true;
			case "0", "false", "no", "off" -> false;
			default -> null;
		};
	}

	private static boolean runsFromExplodedClasses() {
		try {
			URL location = DevMode.class.getProtectionDomain().getCodeSource().getLocation();
			return location != null && "file".equals(location.getProtocol())
					&& Files.isDirectory(Path.of(location.toURI()));
		}
		catch (URISyntaxException ex) {
			return false;
		}
	}

}

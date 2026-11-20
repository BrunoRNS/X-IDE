package org.xide.client.tls;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Properties;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public final class ServerCertificateTrustManager implements X509TrustManager {

	private static final Object STORE_LOCK = new Object();

	private static final Path PIN_FILE = Path.of(System.getProperty("user.home"), ".xide",
			"trusted-certificates.properties");

	private final X509TrustManager systemTrustManager;

	private final String endpoint;

	public ServerCertificateTrustManager(String host, int port) throws GeneralSecurityException {
		this.systemTrustManager = createSystemTrustManager();
		this.endpoint = host.toLowerCase(Locale.ROOT) + ":" + port;
	}

	@Override
	public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		systemTrustManager.checkClientTrusted(chain, authType);
	}

	@Override
	public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		if (chain == null || chain.length == 0) {
			throw new CertificateException("The server did not provide a certificate");
		}

		X509Certificate certificate = chain[0];
		certificate.checkValidity();
		String fingerprint = fingerprint(certificate);

		synchronized (STORE_LOCK) {
			String savedFingerprint = readPin();
			if (savedFingerprint != null) {
				if (!savedFingerprint.equals(fingerprint)) {
					throw new CertificateException("The pinned certificate for " + endpoint + " changed. "
							+ "Verify the new server fingerprint, then remove its entry from " + PIN_FILE
							+ " to trust it.");
				}
				return;
			}

			try {
				systemTrustManager.checkServerTrusted(chain, authType);
				return;
			}
			catch (CertificateException untrustedCertificate) {
				confirmAndSavePin(fingerprint);
			}
		}
	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return systemTrustManager.getAcceptedIssuers();
	}

	private String readPin() throws CertificateException {
		if (!Files.exists(PIN_FILE)) {
			return null;
		}

		Properties pins = new Properties();
		try (Reader reader = Files.newBufferedReader(PIN_FILE)) {
			pins.load(reader);
		}
		catch (IOException e) {
			throw new CertificateException("Unable to read certificate pins from " + PIN_FILE, e);
		}

		String value = pins.getProperty(endpoint);
		if (value == null) {
			return null;
		}
		try {
			return normalizeFingerprint(value);
		}
		catch (IllegalArgumentException e) {
			throw new CertificateException("Invalid stored certificate pin for " + endpoint, e);
		}
	}

	private void confirmAndSavePin(String fingerprint) throws CertificateException {
		String configuredFingerprint = System.getenv("XIDE_TLS_FINGERPRINT");
		if (configuredFingerprint != null && !configuredFingerprint.isBlank()) {
			try {
				if (normalizeFingerprint(configuredFingerprint).equals(fingerprint)) {
					savePin(fingerprint);
					return;
				}
			}
			catch (IllegalArgumentException e) {
				throw new CertificateException("XIDE_TLS_FINGERPRINT must contain a SHA-256 certificate fingerprint",
						e);
			}
			throw new CertificateException("The server certificate does not match XIDE_TLS_FINGERPRINT. "
					+ "Server fingerprint: " + displayFingerprint(fingerprint));
		}
		savePin(fingerprint);
	}

	private void savePin(String fingerprint) throws CertificateException {
		Properties pins = new Properties();
		if (Files.exists(PIN_FILE)) {
			try (Reader reader = Files.newBufferedReader(PIN_FILE)) {
				pins.load(reader);
			}
			catch (IOException e) {
				throw new CertificateException("Unable to read certificate pins from " + PIN_FILE, e);
			}
		}

		pins.setProperty(endpoint, fingerprint);
		Path parent = PIN_FILE.getParent();
		Path temporaryFile = null;
		IOException failure = null;
		try {
			Files.createDirectories(parent);
			temporaryFile = Files.createTempFile(parent, "trusted-certificates-", ".tmp");
			try (Writer writer = Files.newBufferedWriter(temporaryFile)) {
				pins.store(writer, "Trusted X-IDE server certificate fingerprints");
			}
			try {
				Files.move(temporaryFile, PIN_FILE, StandardCopyOption.ATOMIC_MOVE,
						StandardCopyOption.REPLACE_EXISTING);
			}
			catch (AtomicMoveNotSupportedException | FileAlreadyExistsException e) {
				Files.move(temporaryFile, PIN_FILE, StandardCopyOption.REPLACE_EXISTING);
			}
		}
		catch (IOException e) {
			failure = e;
		}
		if (temporaryFile != null) {
			try {
				Files.deleteIfExists(temporaryFile);
			}
			catch (IOException e) {
				if (failure == null) {
					failure = e;
				}
				else {
					failure.addSuppressed(e);
				}
			}
		}
		if (failure != null) {
			throw new CertificateException("Unable to save the certificate pin to " + PIN_FILE, failure);
		}
	}

	private static X509TrustManager createSystemTrustManager() throws GeneralSecurityException {
		TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		factory.init((java.security.KeyStore) null);
		for (TrustManager manager : factory.getTrustManagers()) {
			if (manager instanceof X509TrustManager x509TrustManager) {
				return x509TrustManager;
			}
		}
		throw new GeneralSecurityException("No system X.509 trust manager is available");
	}

	private static String fingerprint(X509Certificate certificate) throws CertificateException {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(certificate.getEncoded());
			return HexFormat.of().withUpperCase().formatHex(digest);
		}
		catch (GeneralSecurityException e) {
			throw new CertificateException("Unable to calculate server certificate fingerprint", e);
		}
	}

	private static String normalizeFingerprint(String value) {
		String normalized = value.replace(":", "").replaceAll("\\s", "").toUpperCase(Locale.ROOT);
		if (!normalized.matches("[0-9A-F]{64}")) {
			throw new IllegalArgumentException("Expected a 32-byte SHA-256 fingerprint");
		}
		return normalized;
	}

	private static String displayFingerprint(String fingerprint) {
		return HexFormat.ofDelimiter(":").withUpperCase().formatHex(HexFormat.of().parseHex(fingerprint));
	}

}

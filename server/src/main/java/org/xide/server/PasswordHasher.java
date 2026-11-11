package org.xide.server;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** PBKDF2-HMAC-SHA256 password hashing. Plain-text passwords are never stored in MySQL. */
public final class PasswordHasher {
    private static final int ITERATIONS = 210_000;
    private static final int KEY_BITS = 256;
    private static final int SALT_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();
    private PasswordHasher() {}

    public record PasswordHash(String salt, String hash) {}

    public static PasswordHash hash(char[] password) {
        if (password == null || password.length < 12) throw new IllegalArgumentException("Password must contain at least 12 characters");
        byte[] salt = new byte[SALT_BYTES]; RANDOM.nextBytes(salt);
        return new PasswordHash(Base64.getEncoder().encodeToString(salt),
                Base64.getEncoder().encodeToString(derive(password, salt)));
    }

    public static boolean verify(char[] password, String saltText, String expectedHash) {
        try {
            byte[] salt = Base64.getDecoder().decode(saltText);
            byte[] expected = Base64.getDecoder().decode(expectedHash);
            return MessageDigest.isEqual(expected, derive(password, salt));
        } catch (RuntimeException e) { return false; }
    }

    private static byte[] derive(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_BITS);
        try { return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded(); }
        catch (Exception e) { throw new IllegalStateException("PBKDF2 is unavailable", e); }
        finally { spec.clearPassword(); }
    }
}

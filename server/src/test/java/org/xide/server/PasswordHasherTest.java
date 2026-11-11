package org.xide.server;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {
    @Test void verifiesCorrectPasswordAndRejectsWrongPassword() {
        char[] password = "a-strong-test-password".toCharArray();
        var encoded = PasswordHasher.hash(password);
        assertTrue(PasswordHasher.verify(password, encoded.salt(), encoded.hash()));
        assertFalse(PasswordHasher.verify("not-the-password".toCharArray(), encoded.salt(), encoded.hash()));
    }

    @Test void generatesDifferentSaltForSamePassword() {
        var first = PasswordHasher.hash("another-strong-password".toCharArray());
        var second = PasswordHasher.hash("another-strong-password".toCharArray());
        assertNotEquals(first.salt(), second.salt());
        assertNotEquals(first.hash(), second.hash());
    }

    @Test void rejectsShortPassword() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash("short".toCharArray()));
    }
}

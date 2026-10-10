package com.myfinance.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordUtil & BCrypt Unit Tests")
class PasswordUtilTest {

    @Test
    @DisplayName("Password hashing produces valid BCrypt string and matches plaintext")
    void testHashAndCheckPassword() {
        String rawPassword = "securePassword123!";
        String hash = PasswordUtil.hashPassword(rawPassword);

        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$12$") || hash.startsWith("$2b$12$") || hash.startsWith("$2a$") || hash.startsWith("$2y$"));
        assertEquals(60, hash.length());

        assertTrue(PasswordUtil.checkPassword(rawPassword, hash));
        assertFalse(PasswordUtil.checkPassword("wrongPassword", hash));
    }

    @Test
    @DisplayName("Empty or null passwords fail safely without crashes")
    void testNullAndEmptyPasswords() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(null));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(""));

        assertFalse(PasswordUtil.checkPassword(null, "someHash"));
        assertFalse(PasswordUtil.checkPassword("pass", null));
        assertFalse(PasswordUtil.checkPassword("pass", ""));
    }
}

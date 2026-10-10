package com.myfinance.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Security utility for BCrypt password hashing and verification.
 */
public final class PasswordUtil {

    private static final int WORK_FACTOR = 12;

    private PasswordUtil() {
        // Utility class
    }

    /**
     * Hashes a plain-text password using BCrypt with a cost factor of 12.
     *
     * @param plainPassword plain text password
     * @return 60-character BCrypt salt + hash string
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty for hashing");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * Verifies a plain-text candidate password against a BCrypt hash.
     *
     * @param plainPassword candidate plain text password
     * @param hashedPassword stored BCrypt hash
     * @return true if password matches, false otherwise
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null || hashedPassword.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

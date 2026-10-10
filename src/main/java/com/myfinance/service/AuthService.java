package com.myfinance.service;

import com.myfinance.dao.impl.UserDAOImpl;
import com.myfinance.dao.interfaces.UserDAO;
import com.myfinance.exception.DatabaseException;
import com.myfinance.exception.ValidationException;
import com.myfinance.model.User;
import com.myfinance.util.ApiResponse;
import com.myfinance.util.PasswordUtil;

import java.sql.SQLException;

/**
 * Service managing user authentication, registration, and credential security.
 */
public class AuthService {

    private final UserDAO userDAO;

    /**
     * Default constructor initializing UserDAO with concrete implementation.
     */
    public AuthService() {
        this.userDAO = new UserDAOImpl();
    }

    /**
     * Dependency injection constructor for testing.
     *
     * @param userDAO user data access object
     */
    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Registers a new user with BCrypt password hashing and validation.
     *
     * @param user prospective user entity
     * @return {@link ApiResponse} with registered user or error
     */
    public ApiResponse<User> register(User user) {
        if (user == null) {
            return ApiResponse.error("User details cannot be empty.");
        }

        if (!user.validate()) {
            return ApiResponse.error("Please provide a valid name, email address, and password.");
        }

        try {
            User existing = userDAO.findByEmail(user.getEmail());
            if (existing != null) {
                return ApiResponse.error("An account with this email address already exists.");
            }

            // Hash password before persistence
            String hashedPassword = PasswordUtil.hashPassword(user.getPassword());
            user.setPassword(hashedPassword);

            if (user.getRole() == null) {
                user.setRole(User.Role.USER);
            }
            if (user.getStatus() == null) {
                user.setStatus(User.Status.ACTIVE);
            }

            boolean saved = userDAO.save(user);
            if (saved) {
                // Clear password from return payload for security
                user.setPassword(null);
                return ApiResponse.ok("Registration successful. Welcome to MyFinance!", user);
            } else {
                return ApiResponse.error("Failed to create account. Please try again.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error during registration: " + e.getMessage(), e);
        }
    }

    /**
     * Authenticates a user by email and plain password against stored BCrypt hash.
     *
     * @param email user email
     * @param plainPassword plain text password
     * @return {@link ApiResponse} with authenticated user or error
     */
    public ApiResponse<User> login(String email, String plainPassword) {
        if (email == null || email.trim().isEmpty() || plainPassword == null || plainPassword.isEmpty()) {
            return ApiResponse.error("Email and password are required.");
        }

        try {
            User user = userDAO.findByEmail(email.trim());
            if (user == null) {
                return ApiResponse.error("Invalid email address or password.");
            }

            if (!user.isActive()) {
                return ApiResponse.error("Your account is currently " + user.getStatus() + ". Please contact support.");
            }

            boolean passwordMatch = PasswordUtil.checkPassword(plainPassword, user.getPassword());
            if (!passwordMatch) {
                return ApiResponse.error("Invalid email address or password.");
            }

            // Clear password before returning
            user.setPassword(null);
            return ApiResponse.ok("Login successful", user);

        } catch (SQLException e) {
            throw new DatabaseException("Database error during login: " + e.getMessage(), e);
        }
    }
}

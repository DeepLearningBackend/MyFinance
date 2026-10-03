package com.myfinance.service;

import com.myfinance.dao.impl.UserDAOImpl;
import com.myfinance.exception.ValidationException;
import com.myfinance.model.User;
import com.myfinance.util.PasswordUtil;

import java.sql.SQLException;

public class AuthService {
    private UserDAOImpl userDAO;

    public AuthService() {
        this.userDAO = new UserDAOImpl();
    }

    public User register(User user) throws SQLException, ValidationException {
        if (!user.validate()) {
            throw new ValidationException("Invalid user data");
        }

        // Check if email already exists
        User existingUser = userDAO.findByEmail(user.getEmail());
        if (existingUser != null) {
            throw new ValidationException("Email already registered", "email");
        }

        // Hash password
        String hashedPassword = PasswordUtil.hashPassword(user.getPassword());
        user.setPassword(hashedPassword);

        userDAO.save(user);
        return user;
    }

    public User login(String email, String password) throws SQLException, ValidationException {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email is required", "email");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Password is required", "password");
        }

        User user = userDAO.findByEmail(email);
        if (user == null) {
            throw new ValidationException("Invalid email or password");
        }

        if (!PasswordUtil.checkPassword(password, user.getPassword())) {
            throw new ValidationException("Invalid email or password");
        }

        if (user.getStatus() != User.Status.ACTIVE) {
            throw new ValidationException("Account is not active");
        }

        return user;
    }
}

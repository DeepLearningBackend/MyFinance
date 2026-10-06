package com.myfinance.servlet;

import com.myfinance.exception.ValidationException;
import com.myfinance.model.User;
import com.myfinance.service.AuthService;
import com.myfinance.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        authService = new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        // Handle logout
        if ("logout".equals(action)) {
            SessionUtil.logout(request);
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        // Check if already logged in
        if (SessionUtil.isLoggedIn(request)) {
            String role = SessionUtil.getUserRole(request);
            if ("ADMIN".equals(role)) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            } else {
                response.sendRedirect(request.getContextPath() + "/dashboard");
            }
            return;
        }

        if ("register".equals(action)) {
            request.getRequestDispatcher("/jsp/auth/register.jsp").forward(request, response);
        } else {
            request.getRequestDispatcher("/jsp/auth/login.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        try {
            if ("login".equals(action)) {
                handleLogin(request, response);
            } else if ("register".equals(action)) {
                handleRegister(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/auth");
            }
        } catch (SQLException e) {
            request.setAttribute("error", "Database error: " + e.getMessage());
            request.getRequestDispatcher("/jsp/auth/login.jsp").forward(request, response);
        } catch (ValidationException e) {
            request.setAttribute("error", e.getMessage());
            if ("register".equals(action)) {
                request.getRequestDispatcher("/jsp/auth/register.jsp").forward(request, response);
            } else {
                request.getRequestDispatcher("/jsp/auth/login.jsp").forward(request, response);
            }
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, ServletException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        User user = authService.login(email, password);

        HttpSession session = request.getSession();
        session.setAttribute(SessionUtil.USER_ID, user.getId());
        session.setAttribute(SessionUtil.USER_NAME, user.getName());
        session.setAttribute(SessionUtil.USER_ROLE, user.getRole().name());

        if (user.getRole() == User.Role.ADMIN) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        } else {
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException, ServletException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (!password.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match");
            request.getRequestDispatcher("/jsp/auth/register.jsp").forward(request, response);
            return;
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(User.Role.USER);
        user.setStatus(User.Status.ACTIVE);

        authService.register(user);

        request.setAttribute("success", "Registration successful! Please login.");
        request.getRequestDispatcher("/jsp/auth/login.jsp").forward(request, response);
    }
}

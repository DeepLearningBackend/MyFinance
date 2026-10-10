package com.myfinance.servlet;

import com.myfinance.model.User;
import com.myfinance.service.AuthService;
import com.myfinance.util.ApiResponse;
import com.myfinance.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Controller handling user login, registration, and logout operations (/auth).
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/auth", "/login", "/register", "/logout"})
public class AuthServlet extends HttpServlet {

    private final AuthService authService;

    /**
     * Default constructor initializing AuthService.
     */
    public AuthServlet() {
        this.authService = new AuthService();
    }

    /**
     * Constructor for dependency injection and testing.
     *
     * @param authService authentication service instance
     */
    public AuthServlet(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getServletPath();

        // Handle Logout
        if ("/logout".equals(path) || "logout".equalsIgnoreCase(request.getParameter("action"))) {
            SessionUtil.invalidate(request);
            response.sendRedirect(request.getContextPath() + "/auth?loggedOut=true");
            return;
        }

        // If user already logged in, route to appropriate dashboard
        if (SessionUtil.isLoggedIn(request)) {
            if (SessionUtil.isAdmin(request)) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            } else {
                response.sendRedirect(request.getContextPath() + "/dashboard");
            }
            return;
        }

        String view = request.getParameter("view");
        if ("/register".equals(path) || "register".equalsIgnoreCase(view)) {
            request.getRequestDispatcher("/jsp/auth/register.jsp").forward(request, response);
        } else {
            request.getRequestDispatcher("/jsp/auth/login.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            String path = request.getServletPath();
            action = "/register".equals(path) ? "register" : "login";
        }

        if ("register".equalsIgnoreCase(action)) {
            handleRegister(request, response);
        } else {
            handleLogin(request, response);
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        ApiResponse<User> result = authService.login(email, password);
        if (result.isSuccess() && result.getData() != null) {
            User user = result.getData();
            SessionUtil.setSessionUser(request, user);

            if (user.isAdmin()) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            } else {
                response.sendRedirect(request.getContextPath() + "/dashboard");
            }
        } else {
            request.setAttribute("error", result.getMessage());
            request.setAttribute("email", email);
            request.getRequestDispatcher("/jsp/auth/login.jsp").forward(request, response);
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        if (password == null || !password.equals(confirmPassword)) {
            request.setAttribute("error", "Passwords do not match. Please verify.");
            request.setAttribute("name", name);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/jsp/auth/register.jsp").forward(request, response);
            return;
        }

        User user = new User(name, email, password, User.Role.USER, User.Status.ACTIVE);
        ApiResponse<User> result = authService.register(user);

        if (result.isSuccess()) {
            SessionUtil.setFlashSuccess(request, "Account created successfully! You can now log in.");
            response.sendRedirect(request.getContextPath() + "/auth?view=login");
        } else {
            request.setAttribute("error", result.getMessage());
            request.setAttribute("name", name);
            request.setAttribute("email", email);
            request.getRequestDispatcher("/jsp/auth/register.jsp").forward(request, response);
        }
    }
}

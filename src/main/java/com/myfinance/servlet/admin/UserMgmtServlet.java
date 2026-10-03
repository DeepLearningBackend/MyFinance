package com.myfinance.servlet.admin;

import com.myfinance.dao.impl.UserDAOImpl;
import com.myfinance.exception.ValidationException;
import com.myfinance.model.User;
import com.myfinance.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/admin/users")
public class UserMgmtServlet extends HttpServlet {
    private UserDAOImpl userDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        String role = SessionUtil.getUserRole(request);
        if (!"ADMIN".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        try {
            String search = request.getParameter("search");
            String statusFilter = request.getParameter("status");

            List<User> users = userDAO.findAll();

            // Apply filters (simplified)
            if (search != null && !search.isEmpty()) {
                users = users.stream()
                    .filter(u -> u.getName().toLowerCase().contains(search.toLowerCase()) ||
                                u.getEmail().toLowerCase().contains(search.toLowerCase()))
                    .toList();
            }

            if (statusFilter != null && !statusFilter.isEmpty()) {
                User.Status status = User.Status.valueOf(statusFilter);
                users = users.stream()
                    .filter(u -> u.getStatus() == status)
                    .toList();
            }

            request.setAttribute("users", users);
            request.getRequestDispatcher("/jsp/admin/user-management.jsp").forward(request, response);
        } catch (SQLException e) {
            request.setAttribute("error", "Error loading users: " + e.getMessage());
            request.getRequestDispatcher("/jsp/admin/user-management.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        String role = SessionUtil.getUserRole(request);
        if (!"ADMIN".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        String action = request.getParameter("action");

        try {
            if ("change-status".equals(action)) {
                int userId = Integer.parseInt(request.getParameter("id"));
                User user = userDAO.findById(userId);
                if (user != null) {
                    user.setStatus(User.Status.valueOf(request.getParameter("status")));
                    userDAO.update(user);
                    request.getSession().setAttribute("success", "User status updated");
                }
            } else if ("delete".equals(action)) {
                int userId = Integer.parseInt(request.getParameter("id"));
                userDAO.delete(userId);
                request.getSession().setAttribute("success", "User deleted");
            }

            response.sendRedirect(request.getContextPath() + "/admin/users");
        } catch (SQLException e) {
            request.getSession().setAttribute("error", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/users");
        }
    }
}

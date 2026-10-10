package com.myfinance.servlet.admin;

import com.myfinance.dao.impl.AuditLogDAOImpl;
import com.myfinance.dao.impl.UserDAOImpl;
import com.myfinance.dao.interfaces.AuditLogDAO;
import com.myfinance.dao.interfaces.UserDAO;
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
import java.sql.SQLException;
import java.util.List;

/**
 * Controller for User Management operations in the Admin panel (/admin/users).
 */
@WebServlet(name = "UserMgmtServlet", urlPatterns = {"/admin/users"})
public class UserMgmtServlet extends HttpServlet {

    private final UserDAO userDAO;
    private final AuditLogDAO auditLogDAO;
    private final AuthService authService;

    /**
     * Default constructor initializing DAOs and AuthService.
     */
    public UserMgmtServlet() {
        this.userDAO = new UserDAOImpl();
        this.auditLogDAO = new AuditLogDAOImpl();
        this.authService = new AuthService(this.userDAO);
    }

    /**
     * Constructor for dependency injection and testing.
     *
     * @param userDAO     user DAO instance
     * @param auditLogDAO audit log DAO instance
     * @param authService authentication service instance
     */
    public UserMgmtServlet(UserDAO userDAO, AuditLogDAO auditLogDAO, AuthService authService) {
        this.userDAO = userDAO;
        this.auditLogDAO = auditLogDAO;
        this.authService = authService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.checkAdminOrRedirect(request, response)) {
            return;
        }

        String search = request.getParameter("search");
        String role = request.getParameter("role");
        String status = request.getParameter("status");

        try {
            List<User> users = userDAO.searchUsers(search, role, status);
            int totalCount = userDAO.countUsers();
            int activeCount = userDAO.countActiveUsers();

            request.setAttribute("users", users);
            request.setAttribute("totalCount", totalCount);
            request.setAttribute("activeCount", activeCount);
            request.setAttribute("searchQuery", search != null ? search : "");
            request.setAttribute("selectedRole", role != null ? role : "ALL");
            request.setAttribute("selectedStatus", status != null ? status : "ALL");

            request.getRequestDispatcher("/jsp/admin/user-management.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Error searching users: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.checkAdminOrRedirect(request, response)) {
            return;
        }

        int adminId = SessionUtil.getSessionUserId(request);
        String action = request.getParameter("action");
        if (action == null) action = "";

        switch (action.toLowerCase()) {
            case "toggle-status" -> handleToggleStatus(request, response, adminId);
            case "update-role" -> handleUpdateRole(request, response, adminId);
            case "delete" -> handleDeleteUser(request, response, adminId);
            case "create" -> handleCreateUser(request, response, adminId);
            default -> response.sendRedirect(request.getContextPath() + "/admin/users");
        }
    }

    private void handleToggleStatus(HttpServletRequest request, HttpServletResponse response, int adminId) throws IOException {
        String userIdStr = request.getParameter("userId");
        String newStatusStr = request.getParameter("status");

        try {
            int targetUserId = Integer.parseInt(userIdStr);
            User.Status newStatus = User.Status.valueOf(newStatusStr.toUpperCase());

            boolean updated = userDAO.updateStatus(targetUserId, newStatus);
            if (updated) {
                auditLogDAO.log(adminId, "Updated User #" + targetUserId + " status to " + newStatus, targetUserId);
                SessionUtil.setFlashSuccess(request, "User status successfully changed to " + newStatus);
            } else {
                SessionUtil.setFlashError(request, "Failed to update user status.");
            }
        } catch (Exception e) {
            SessionUtil.setFlashError(request, "Error changing user status: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    private void handleUpdateRole(HttpServletRequest request, HttpServletResponse response, int adminId) throws IOException {
        String userIdStr = request.getParameter("userId");
        String newRoleStr = request.getParameter("role");

        try {
            int targetUserId = Integer.parseInt(userIdStr);
            User user = userDAO.findById(targetUserId);
            if (user != null) {
                user.setRole(User.Role.valueOf(newRoleStr.toUpperCase()));
                boolean updated = userDAO.update(user);
                if (updated) {
                    auditLogDAO.log(adminId, "Modified User #" + targetUserId + " role to " + user.getRole(), targetUserId);
                    SessionUtil.setFlashSuccess(request, "User role updated to " + user.getRole());
                }
            }
        } catch (Exception e) {
            SessionUtil.setFlashError(request, "Error updating role: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    private void handleDeleteUser(HttpServletRequest request, HttpServletResponse response, int adminId) throws IOException {
        String userIdStr = request.getParameter("userId");

        try {
            int targetUserId = Integer.parseInt(userIdStr);
            if (targetUserId == adminId) {
                SessionUtil.setFlashError(request, "Security violation: You cannot delete your own active admin account.");
                response.sendRedirect(request.getContextPath() + "/admin/users");
                return;
            }

            boolean deleted = userDAO.delete(targetUserId);
            if (deleted) {
                auditLogDAO.log(adminId, "Permanently deleted User #" + targetUserId, null);
                SessionUtil.setFlashSuccess(request, "User account #" + targetUserId + " and associated records deleted.");
            } else {
                SessionUtil.setFlashError(request, "Failed to delete user.");
            }
        } catch (Exception e) {
            SessionUtil.setFlashError(request, "Error deleting user: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    private void handleCreateUser(HttpServletRequest request, HttpServletResponse response, int adminId) throws IOException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String roleStr = request.getParameter("role");
        String statusStr = request.getParameter("status");

        try {
            User.Role role = (roleStr != null && !roleStr.isEmpty()) ? User.Role.valueOf(roleStr) : User.Role.USER;
            User.Status status = (statusStr != null && !statusStr.isEmpty()) ? User.Status.valueOf(statusStr) : User.Status.ACTIVE;

            User user = new User(name, email, password, role, status);
            ApiResponse<User> result = authService.register(user);

            if (result.isSuccess()) {
                auditLogDAO.log(adminId, "Admin provisioned new account: " + email + " (" + role + ")", user.getId());
                SessionUtil.setFlashSuccess(request, "User created successfully.");
            } else {
                SessionUtil.setFlashError(request, result.getMessage());
            }
        } catch (Exception e) {
            SessionUtil.setFlashError(request, "Error provisioning user: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/users");
    }
}

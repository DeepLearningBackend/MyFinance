package com.myfinance.servlet.admin;

import com.myfinance.dao.impl.AuditLogDAOImpl;
import com.myfinance.dao.impl.SystemSettingsDAOImpl;
import com.myfinance.dao.interfaces.AuditLogDAO;
import com.myfinance.dao.interfaces.SystemSettingsDAO;
import com.myfinance.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for System Security Controls and Compliance Logging (/admin/security).
 */
@WebServlet(name = "SecurityServlet", urlPatterns = {"/admin/security"})
public class SecurityServlet extends HttpServlet {

    private final SystemSettingsDAO systemSettingsDAO;
    private final AuditLogDAO auditLogDAO;

    /**
     * Default constructor initializing DAOs.
     */
    public SecurityServlet() {
        this.systemSettingsDAO = new SystemSettingsDAOImpl();
        this.auditLogDAO = new AuditLogDAOImpl();
    }

    /**
     * Constructor for dependency injection and testing.
     *
     * @param systemSettingsDAO system settings DAO instance
     * @param auditLogDAO       audit log DAO instance
     */
    public SecurityServlet(SystemSettingsDAO systemSettingsDAO, AuditLogDAO auditLogDAO) {
        this.systemSettingsDAO = systemSettingsDAO;
        this.auditLogDAO = auditLogDAO;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.checkAdminOrRedirect(request, response)) {
            return;
        }

        try {
            Map<String, String> settings = systemSettingsDAO.getAllSettings();
            List<Map<String, Object>> securityLogs = auditLogDAO.findRecentLogs(15);

            request.setAttribute("settings", settings);
            request.setAttribute("securityLogs", securityLogs);
            request.getRequestDispatcher("/jsp/admin/security.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Error loading security dashboard: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.checkAdminOrRedirect(request, response)) {
            return;
        }

        int adminId = SessionUtil.getSessionUserId(request);

        String sessionTimeout = request.getParameter("session_timeout");
        String maxLoginAttempts = request.getParameter("max_login_attempts");
        String twoFactorEnabled = request.getParameter("two_factor_auth") != null ? "true" : "false";
        String encryptionStandard = request.getParameter("encryption_standard");
        String ipWhitelistEnabled = request.getParameter("ip_whitelist_enabled") != null ? "true" : "false";

        Map<String, String> updates = new HashMap<>();
        if (sessionTimeout != null && !sessionTimeout.isEmpty()) updates.put("session_timeout", sessionTimeout);
        if (maxLoginAttempts != null && !maxLoginAttempts.isEmpty()) updates.put("max_login_attempts", maxLoginAttempts);
        updates.put("two_factor_auth", twoFactorEnabled);
        if (encryptionStandard != null) updates.put("encryption_standard", encryptionStandard);
        updates.put("ip_whitelist_enabled", ipWhitelistEnabled);

        try {
            systemSettingsDAO.updateSettings(updates, adminId);
            auditLogDAO.log(adminId, "Updated platform security configuration and encryption policies", null);
            SessionUtil.setFlashSuccess(request, "Security policies updated successfully.");
        } catch (SQLException e) {
            SessionUtil.setFlashError(request, "Failed to update security settings: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/security");
    }
}

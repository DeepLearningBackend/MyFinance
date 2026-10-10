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
 * Controller for Global Platform Settings and Feature Flags (/admin/settings).
 */
@WebServlet(name = "SettingsServlet", urlPatterns = {"/admin/settings"})
public class SettingsServlet extends HttpServlet {

    private final SystemSettingsDAO systemSettingsDAO;
    private final AuditLogDAO auditLogDAO;

    /**
     * Default constructor initializing DAOs.
     */
    public SettingsServlet() {
        this.systemSettingsDAO = new SystemSettingsDAOImpl();
        this.auditLogDAO = new AuditLogDAOImpl();
    }

    /**
     * Constructor for dependency injection and testing.
     *
     * @param systemSettingsDAO system settings DAO instance
     * @param auditLogDAO       audit log DAO instance
     */
    public SettingsServlet(SystemSettingsDAO systemSettingsDAO, AuditLogDAO auditLogDAO) {
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
            List<Map<String, Object>> changeLogs = auditLogDAO.findRecentLogs(10);

            request.setAttribute("settings", settings);
            request.setAttribute("changeLogs", changeLogs);
            request.getRequestDispatcher("/jsp/admin/settings.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Error loading settings: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.checkAdminOrRedirect(request, response)) {
            return;
        }

        int adminId = SessionUtil.getSessionUserId(request);

        String platformName = request.getParameter("platform_name");
        String defaultCurrency = request.getParameter("default_currency");
        String budgetAlertsEnabled = request.getParameter("budget_alerts_enabled") != null ? "true" : "false";
        String emailReportsEnabled = request.getParameter("email_reports_enabled") != null ? "true" : "false";
        String offlineSyncEnabled = request.getParameter("offline_sync_enabled") != null ? "true" : "false";
        String autoCategorizeEnabled = request.getParameter("auto_categorize_enabled") != null ? "true" : "false";

        Map<String, String> updates = new HashMap<>();
        if (platformName != null && !platformName.trim().isEmpty()) updates.put("platform_name", platformName.trim());
        if (defaultCurrency != null && !defaultCurrency.trim().isEmpty()) updates.put("default_currency", defaultCurrency.trim());
        updates.put("budget_alerts_enabled", budgetAlertsEnabled);
        updates.put("email_reports_enabled", emailReportsEnabled);
        updates.put("offline_sync_enabled", offlineSyncEnabled);
        updates.put("auto_categorize_enabled", autoCategorizeEnabled);

        try {
            systemSettingsDAO.updateSettings(updates, adminId);
            auditLogDAO.log(adminId, "Updated global platform parameters and feature flags", null);
            SessionUtil.setFlashSuccess(request, "Platform settings saved successfully.");
        } catch (SQLException e) {
            SessionUtil.setFlashError(request, "Failed to update platform settings: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/settings");
    }
}

package com.myfinance.servlet.admin;

import com.myfinance.dao.impl.AuditLogDAOImpl;
import com.myfinance.dao.impl.ExpenseDAOImpl;
import com.myfinance.dao.impl.SystemSettingsDAOImpl;
import com.myfinance.dao.impl.UserDAOImpl;
import com.myfinance.dao.interfaces.AuditLogDAO;
import com.myfinance.dao.interfaces.ExpenseDAO;
import com.myfinance.dao.interfaces.SystemSettingsDAO;
import com.myfinance.dao.interfaces.UserDAO;
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
import java.util.Map;

/**
 * Controller for Admin Central Command Dashboard (/admin/dashboard).
 */
@WebServlet(name = "AdminDashServlet", urlPatterns = {"/admin/dashboard", "/admin"})
public class AdminDashServlet extends HttpServlet {

    private final UserDAO userDAO;
    private final ExpenseDAO expenseDAO;
    private final AuditLogDAO auditLogDAO;
    private final SystemSettingsDAO systemSettingsDAO;

    /**
     * Default constructor initializing DAOs.
     */
    public AdminDashServlet() {
        this.userDAO = new UserDAOImpl();
        this.expenseDAO = new ExpenseDAOImpl();
        this.auditLogDAO = new AuditLogDAOImpl();
        this.systemSettingsDAO = new SystemSettingsDAOImpl();
    }

    /**
     * Constructor for dependency injection and testing.
     *
     * @param userDAO           user DAO instance
     * @param expenseDAO        expense DAO instance
     * @param auditLogDAO       audit log DAO instance
     * @param systemSettingsDAO system settings DAO instance
     */
    public AdminDashServlet(UserDAO userDAO, ExpenseDAO expenseDAO, AuditLogDAO auditLogDAO, SystemSettingsDAO systemSettingsDAO) {
        this.userDAO = userDAO;
        this.expenseDAO = expenseDAO;
        this.auditLogDAO = auditLogDAO;
        this.systemSettingsDAO = systemSettingsDAO;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.checkAdminOrRedirect(request, response)) {
            return;
        }

        try {
            int totalUsers = userDAO.countUsers();
            int activeUsers = userDAO.countActiveUsers();
            int totalExpensesCount = expenseDAO.countAllExpenses();
            int totalAuditLogs = auditLogDAO.countTotalLogs();

            List<User> recentUsers = userDAO.findAll();
            if (recentUsers.size() > 8) {
                recentUsers = recentUsers.subList(0, 8);
            }

            List<Map<String, Object>> recentLogs = auditLogDAO.findRecentLogs(10);
            Map<String, String> systemSettings = systemSettingsDAO.getAllSettings();

            request.setAttribute("totalUsers", totalUsers);
            request.setAttribute("activeUsers", activeUsers);
            request.setAttribute("inactiveUsers", Math.max(0, totalUsers - activeUsers));
            request.setAttribute("totalExpensesCount", totalExpensesCount);
            request.setAttribute("totalAuditLogs", totalAuditLogs);
            request.setAttribute("recentUsers", recentUsers);
            request.setAttribute("recentLogs", recentLogs);
            request.setAttribute("systemSettings", systemSettings);

            request.getRequestDispatcher("/jsp/admin/admin-dashboard.jsp").forward(request, response);
        } catch (SQLException e) {
            throw new ServletException("Database error loading admin dashboard: " + e.getMessage(), e);
        }
    }
}

package com.myfinance.thread;

import com.myfinance.dao.impl.AuditLogDAOImpl;
import com.myfinance.dao.impl.BudgetDAOImpl;
import com.myfinance.dao.impl.UserDAOImpl;
import com.myfinance.dao.interfaces.AuditLogDAO;
import com.myfinance.dao.interfaces.BudgetDAO;
import com.myfinance.dao.interfaces.UserDAO;
import com.myfinance.model.Budget;
import com.myfinance.model.User;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Background Daemon Thread for scheduled budget usage audits and alert monitoring (Multithreading Rubric).
 * Runs periodically to inspect active user budgets and log critical threshold breaches.
 */
public class BudgetAlertThread implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(BudgetAlertThread.class.getName());
    private static final long CHECK_INTERVAL_MS = 3600000L; // 1 Hour

    private final UserDAO userDAO;
    private final BudgetDAO budgetDAO;
    private final AuditLogDAO auditLogDAO;
    private volatile boolean running = true;

    /**
     * Default constructor initializing DAOs with concrete JDBC implementations.
     */
    public BudgetAlertThread() {
        this.userDAO = new UserDAOImpl();
        this.budgetDAO = new BudgetDAOImpl();
        this.auditLogDAO = new AuditLogDAOImpl();
    }

    /**
     * Dependency injection constructor for testing.
     *
     * @param userDAO user data access object
     * @param budgetDAO budget data access object
     * @param auditLogDAO audit log data access object
     */
    public BudgetAlertThread(UserDAO userDAO, BudgetDAO budgetDAO, AuditLogDAO auditLogDAO) {
        this.userDAO = userDAO;
        this.budgetDAO = budgetDAO;
        this.auditLogDAO = auditLogDAO;
    }

    /**
     * Executes the background daemon thread loop, auditing budgets every 1 hour.
     */
    @Override
    public void run() {
        LOGGER.info("BudgetAlertThread background monitor started.");
        // Daemon loop: runs periodically until interrupted or stopped
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                checkAllUserBudgets();
                Thread.sleep(CHECK_INTERVAL_MS);
            } catch (InterruptedException e) {
                LOGGER.info("BudgetAlertThread interrupted. Stopping daemon thread.");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Unexpected error in BudgetAlertThread execution: " + e.getMessage(), e);
            }
        }
        LOGGER.info("BudgetAlertThread background monitor terminated.");
    }

    /**
     * Synchronized method to inspect all active user budgets and log breaches.
     * Enforces thread-safe access during execution cycles.
     */
    public synchronized void checkAllUserBudgets() {
        try {
            List<User> users = userDAO.findAll();
            LocalDate today = LocalDate.now();

            for (User u : users) {
                if (!u.isActive()) {
                    continue;
                }

                List<Budget> budgets = budgetDAO.findByUserId(u.getId());
                for (Budget b : budgets) {
                    if (b.getEndDate() != null && b.getEndDate().isBefore(today)) {
                        continue;
                    }

                    // Recalculate up-to-date spent amount
                    budgetDAO.recalculateSpentAmount(u.getId(), b.getCategory(), b.getStartDate(), b.getEndDate());
                    Budget refreshed = budgetDAO.findByIdAndUserId(b.getId(), u.getId());
                    if (refreshed == null) continue;

                    double usage = refreshed.getUsagePercent();
                    if (usage >= 100.0) {
                        String alertMsg = String.format("SYSTEM_ALERT: User #%d exceeded '%s' budget (₹%,.2f / ₹%,.2f, %.1f%%)",
                                u.getId(), refreshed.getCategory(), refreshed.getSpentAmount(), refreshed.getLimitAmount(), usage);
                        LOGGER.warning(alertMsg);
                        auditLogDAO.log(1, alertMsg, u.getId());
                    } else if (usage >= 90.0) {
                        LOGGER.info(String.format("SYSTEM_NOTICE: User #%d near '%s' budget limit (%.1f%%)",
                                u.getId(), refreshed.getCategory(), usage));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL error during checkAllUserBudgets: " + e.getMessage(), e);
        }
    }

    /**
     * Signals the background daemon thread to terminate gracefully.
     */
    public void stop() {
        this.running = false;
    }
}

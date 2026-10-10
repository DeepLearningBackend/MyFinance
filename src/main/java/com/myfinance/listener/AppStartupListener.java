package com.myfinance.listener;

import com.myfinance.dao.impl.UserDAOImpl;
import com.myfinance.service.BudgetService;
import com.myfinance.thread.BudgetAlertThread;
import com.myfinance.thread.SmsProcessorThread;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.logging.Logger;

/**
 * ServletContextListener that initializes and terminates background daemon threads on application startup/shutdown.
 */
@WebListener
public class AppStartupListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(AppStartupListener.class.getName());
    private Thread alertThread;
    private BudgetAlertThread budgetTask;
    private Thread smsThread;

    /**
     * Initializes background services, diagnostic checks, and daemon worker threads upon application startup.
     *
     * @param sce servlet context event
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOGGER.info("MyFinance application initializing... Starting background worker threads.");
        com.myfinance.util.DBConnection.printTimezoneDiagnostics();

        // Fix existing budgets & expenses: normalize categories and recalculate spent_amount
        try {
            new BudgetService().fixExistingBudgetsAndExpenses();
            LOGGER.info("Existing budgets and expenses verified and recalculated.");
        } catch (Exception e) {
            LOGGER.warning("Notice updating existing budgets: " + e.getMessage());
        }

        // Backfill API tokens for existing users if any are null
        try {
            new UserDAOImpl().ensureApiTokensForAllUsers();
        } catch (Exception e) {
            LOGGER.warning("Notice verifying API tokens: " + e.getMessage());
        }

        // Budget Alert background thread
        budgetTask = new BudgetAlertThread();
        alertThread = new Thread(budgetTask, "BudgetAlertThread");
        alertThread.setDaemon(true);
        alertThread.start();
        LOGGER.info("BudgetAlertThread daemon started successfully.");

        // SMS Queue processor background thread
        SmsProcessorThread smsTask = new SmsProcessorThread();
        smsThread = new Thread(smsTask, "SmsProcessorThread");
        smsThread.setDaemon(true);
        smsThread.start();
        LOGGER.info("SmsProcessorThread daemon started successfully.");
    }

    /**
     * Stops and interrupts background worker threads when the application context is destroyed.
     *
     * @param sce servlet context event
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOGGER.info("MyFinance application shutting down... Stopping background threads.");
        if (budgetTask != null) {
            budgetTask.stop();
        }
        if (alertThread != null) {
            alertThread.interrupt();
        }
        if (smsThread != null) {
            smsThread.interrupt();
        }
    }
}

package com.myfinance.thread;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppStartupListener implements ServletContextListener {
    private Thread alertThread;
    private BudgetAlertThread budgetAlertTask;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        budgetAlertTask = new BudgetAlertThread();
        alertThread = new Thread(budgetAlertTask, "BudgetAlertThread");
        alertThread.setDaemon(true);
        alertThread.start();
        System.out.println("BudgetAlertThread started successfully");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (alertThread != null) {
            budgetAlertTask.stop();
            alertThread.interrupt();
            System.out.println("BudgetAlertThread stopped");
        }
    }
}

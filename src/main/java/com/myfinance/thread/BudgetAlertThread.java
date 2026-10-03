package com.myfinance.thread;

import com.myfinance.dao.impl.BudgetDAOImpl;
import com.myfinance.dao.impl.UserDAOImpl;
import com.myfinance.model.Budget;
import com.myfinance.model.User;

import java.sql.SQLException;
import java.util.List;

public class BudgetAlertThread implements Runnable {
    private UserDAOImpl userDAO;
    private BudgetDAOImpl budgetDAO;
    private volatile boolean running = true;

    public BudgetAlertThread() {
        this.userDAO = new UserDAOImpl();
        this.budgetDAO = new BudgetDAOImpl();
    }

    @Override
    public void run() {
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                checkAllUserBudgets();
                Thread.sleep(3600000); // Run every 1 hour
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            } catch (SQLException e) {
                System.err.println("Error in BudgetAlertThread: " + e.getMessage());
            }
        }
    }

    private synchronized void checkAllUserBudgets() throws SQLException {
        List<User> users = userDAO.findAll();

        for (User user : users) {
            if (user.getStatus() == User.Status.ACTIVE) {
                List<Budget> budgets = budgetDAO.findByUserId(user.getId());

                for (Budget budget : budgets) {
                    double usagePercent = budget.getUsagePercent();

                    if (usagePercent >= 100) {
                        System.out.println("ALERT: User " + user.getEmail() + " - " +
                            budget.getCategory() + " budget exceeded! " +
                            "Spent ₹" + budget.getSpentAmount() + " of ₹" + budget.getLimitAmount());
                    } else if (usagePercent >= 90) {
                        System.out.println("WARNING: User " + user.getEmail() + " - " +
                            budget.getCategory() + " budget at 90%! " +
                            "Spent ₹" + budget.getSpentAmount() + " of ₹" + budget.getLimitAmount());
                    }
                }
            }
        }
    }

    public void stop() {
        running = false;
    }
}

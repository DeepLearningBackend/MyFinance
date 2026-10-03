package com.myfinance.service;

import com.myfinance.dao.impl.BudgetDAOImpl;
import com.myfinance.exception.ValidationException;
import com.myfinance.model.Budget;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BudgetService {
    private BudgetDAOImpl budgetDAO;

    public BudgetService() {
        this.budgetDAO = new BudgetDAOImpl();
    }

    public void createBudget(Budget budget) throws SQLException, ValidationException {
        if (!budget.validate()) {
            throw new ValidationException("Invalid budget data");
        }

        // Check if budget already exists for this category
        Budget existing = budgetDAO.findByUserIdAndCategory(budget.getUserId(), budget.getCategory());
        if (existing != null) {
            throw new ValidationException("Budget already exists for this category", "category");
        }

        budgetDAO.save(budget);
    }

    public List<Budget> getBudgetStatus(int userId) throws SQLException {
        return budgetDAO.findByUserId(userId);
    }

    public List<String> checkAlerts(int userId) throws SQLException {
        List<Budget> budgets = budgetDAO.findByUserId(userId);
        List<String> alerts = new ArrayList<>();

        for (Budget budget : budgets) {
            double usagePercent = budget.getUsagePercent();

            if (usagePercent >= 100) {
                alerts.add("⚠️ " + budget.getCategory() + " budget exceeded! Spent ₹" + budget.getSpentAmount() + " of ₹" + budget.getLimitAmount());
            } else if (usagePercent >= 90) {
                alerts.add("⚠️ " + budget.getCategory() + " budget is 90% used! Spent ₹" + budget.getSpentAmount() + " of ₹" + budget.getLimitAmount());
            } else if (usagePercent >= 70) {
                alerts.add("ℹ️ " + budget.getCategory() + " budget is 70% used. Spent ₹" + budget.getSpentAmount() + " of ₹" + budget.getLimitAmount());
            }
        }

        return alerts;
    }

    public void updateSpentOnExpenseAdd(int userId, String category, double amount) throws SQLException {
        Budget budget = budgetDAO.findByUserIdAndCategory(userId, category);
        if (budget != null) {
            budgetDAO.updateSpentAmount(budget.getId(), amount);
        }
    }

    public void deleteBudget(int id) throws SQLException {
        budgetDAO.delete(id);
    }

    public void updateBudget(Budget budget) throws SQLException, ValidationException {
        if (!budget.validate()) {
            throw new ValidationException("Invalid budget data");
        }
        budgetDAO.update(budget);
    }
}

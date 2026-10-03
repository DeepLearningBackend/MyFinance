package com.myfinance.service;

import com.myfinance.dao.impl.BudgetDAOImpl;
import com.myfinance.dao.impl.ExpenseDAOImpl;
import com.myfinance.exception.ValidationException;
import com.myfinance.model.Budget;
import com.myfinance.model.Expense;
import com.myfinance.util.CategoryMapper;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseService {
    private ExpenseDAOImpl expenseDAO;
    private BudgetDAOImpl budgetDAO;

    public ExpenseService() {
        this.expenseDAO = new ExpenseDAOImpl();
        this.budgetDAO = new BudgetDAOImpl();
    }

    public void addExpense(Expense expense) throws SQLException, ValidationException {
        if (!expense.validate()) {
            throw new ValidationException("Invalid expense data");
        }

        // Auto-suggest category if not provided
        if (expense.getCategory() == null || expense.getCategory().trim().isEmpty()) {
            expense.setCategory(CategoryMapper.suggest(expense.getTitle()));
        }

        expenseDAO.save(expense);

        // Update budget spent amount
        Budget budget = budgetDAO.findByUserIdAndCategory(expense.getUserId(), expense.getCategory());
        if (budget != null) {
            budgetDAO.updateSpentAmount(budget.getId(), expense.getAmount());
        }
    }

    public List<Expense> getMonthlyExpenses(int userId, int month, int year) throws SQLException {
        return expenseDAO.findByUserIdAndMonth(userId, month, year);
    }

    public Map<String, Double> getCategoryTotals(int userId, int month, int year) throws SQLException {
        List<Expense> expenses = expenseDAO.findByUserIdAndMonth(userId, month, year);
        Map<String, Double> categoryTotals = new HashMap<>();

        for (Expense expense : expenses) {
            categoryTotals.merge(expense.getCategory(), expense.getAmount(), Double::sum);
        }

        return categoryTotals;
    }

    public double getMonthlyTotal(int userId, int month, int year) throws SQLException {
        return expenseDAO.getSumByUserIdAndMonth(userId, month, year);
    }

    public List<Expense> getAllExpenses(int userId) throws SQLException {
        return expenseDAO.findByUserId(userId);
    }

    public void deleteExpense(int id, int userId) throws SQLException {
        expenseDAO.delete(id, userId);
    }

    public void updateExpense(Expense expense) throws SQLException, ValidationException {
        if (!expense.validate()) {
            throw new ValidationException("Invalid expense data");
        }
        expenseDAO.update(expense);
    }
}

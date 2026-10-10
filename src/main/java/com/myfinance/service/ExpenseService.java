package com.myfinance.service;

import com.myfinance.dao.impl.BudgetDAOImpl;
import com.myfinance.dao.impl.ExpenseDAOImpl;
import com.myfinance.dao.interfaces.BudgetDAO;
import com.myfinance.dao.interfaces.ExpenseDAO;
import com.myfinance.exception.DatabaseException;
import com.myfinance.model.Budget;
import com.myfinance.model.Expense;
 import com.myfinance.model.FinanceOperation;
import com.myfinance.util.ApiResponse;
import com.myfinance.util.CategoryMapper;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Service managing expense records, monthly aggregations, and category breakdowns.
 * Demonstrates Collections & Generics (List, Map, Generics).
 * Implements {@link FinanceOperation} — execute() adds an expense,
 * summarize() returns the user's current-month total as a plain string.
 */
public class ExpenseService implements FinanceOperation<Expense, ApiResponse<Expense>> {

    private final ExpenseDAO expenseDAO;
    private final BudgetDAO budgetDAO;

    public ExpenseService() {
        this.expenseDAO = new ExpenseDAOImpl();
        this.budgetDAO = new BudgetDAOImpl();
    }

    public ExpenseService(ExpenseDAO expenseDAO, BudgetDAO budgetDAO) {
        this.expenseDAO = expenseDAO;
        this.budgetDAO = budgetDAO;
    }

    /**
     * Adds an expense, validates constraints, and updates active budget spent totals.
     *
     * @param expense new expense
     * @return {@link ApiResponse} with saved expense
     */
    public ApiResponse<Expense> addExpense(Expense expense) {
        if (expense == null) {
            return ApiResponse.error("Expense cannot be null.");
        }

        if (!expense.validate()) {
            return ApiResponse.error("Please provide valid expense title, positive amount, and date.");
        }

        try {
            boolean saved = expenseDAO.save(expense);
            if (!saved) {
                return ApiResponse.error("Could not save expense to database.");
            }

            // Sync with active category budget for month and year if present
            syncBudgetAfterExpense(expense.getUserId(), expense.getCategory(), expense.getExpenseDate());

            return ApiResponse.ok("Expense recorded successfully", expense);
        } catch (SQLException e) {
            throw new DatabaseException("Error adding expense: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing expense entity and recalculates associated budget spending.
     *
     * @param expense updated expense entity
     * @return ApiResponse containing updated expense or error
     * @throws DatabaseException if persistence fails
     */
    public ApiResponse<Expense> updateExpense(Expense expense) {
        if (expense == null || !expense.validate() || expense.getId() <= 0) {
            return ApiResponse.error("Invalid expense details for update.");
        }

        try {
            Expense oldExpense = expenseDAO.findByIdAndUserId(expense.getId(), expense.getUserId());
            if (oldExpense == null) {
                return ApiResponse.error("Expense not found or unauthorized.");
            }

            boolean updated = expenseDAO.update(expense);
            if (updated) {
                // Sync old category budget and new category budget
                syncBudgetAfterExpense(expense.getUserId(), oldExpense.getCategory(), oldExpense.getExpenseDate());
                if (!oldExpense.getCategory().equalsIgnoreCase(expense.getCategory())) {
                    syncBudgetAfterExpense(expense.getUserId(), expense.getCategory(), expense.getExpenseDate());
                }
                return ApiResponse.ok("Expense updated successfully", expense);
            }
            return ApiResponse.error("Failed to update expense.");
        } catch (SQLException e) {
            throw new DatabaseException("Error updating expense: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes an expense by ID and updates associated budget totals.
     *
     * @param id expense ID
     * @param userId owner user ID
     * @return ApiResponse with deletion status
     * @throws DatabaseException if persistence fails
     */
    public ApiResponse<Boolean> deleteExpense(int id, int userId) {
        try {
            Expense existing = expenseDAO.findByIdAndUserId(id, userId);
            if (existing == null) {
                return ApiResponse.error("Expense not found or unauthorized.");
            }

            boolean deleted = expenseDAO.delete(id, userId);
            if (deleted) {
                syncBudgetAfterExpense(userId, existing.getCategory(), existing.getExpenseDate());
                return ApiResponse.ok("Expense deleted successfully", true);
            }
            return ApiResponse.error("Failed to delete expense.");
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting expense: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a single expense by ID belonging to the user.
     *
     * @param id expense ID
     * @param userId owner user ID
     * @return Expense entity, or null
     * @throws DatabaseException if database query fails
     */
    public Expense getExpenseById(int id, int userId) {
        try {
            return expenseDAO.findByIdAndUserId(id, userId);
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching expense: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all expenses for a user in a given calendar month and year.
     *
     * @param userId user ID
     * @param month 1-12
     * @param year calendar year
     * @return list of expenses
     * @throws DatabaseException if database query fails
     */
    public List<Expense> getMonthlyExpenses(int userId, int month, int year) {
        try {
            return expenseDAO.findByUserIdAndMonth(userId, month, year);
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching monthly expenses: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves the most recent expenses for a user up to limit.
     *
     * @param userId user ID
     * @param limit maximum count
     * @return list of recent expenses
     * @throws DatabaseException if database query fails
     */
    public List<Expense> getRecentExpenses(int userId, int limit) {
        try {
            return expenseDAO.findRecentByUserId(userId, limit);
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching recent expenses: " + e.getMessage(), e);
        }
    }

    /**
     * Filters expenses by month, year, category, and text keyword search.
     *
     * @param userId user ID
     * @param month optional month filter
     * @param year optional year filter
     * @param category optional category filter
     * @param searchQuery optional search keyword
     * @return filtered list of expenses
     * @throws DatabaseException if database query fails
     */
    public List<Expense> filterExpenses(int userId, Integer month, Integer year, String category, String searchQuery) {
        try {
            return expenseDAO.filterExpenses(userId, month, year, category, searchQuery);
        } catch (SQLException e) {
            throw new DatabaseException("Error filtering expenses: " + e.getMessage(), e);
        }
    }

    /**
     * Groups a user's monthly spending by category.
     *
     * @param userId user ID
     * @param month 1-12
     * @param year calendar year
     * @return map of category to total spent
     * @throws DatabaseException if database query fails
     */
    public Map<String, Double> getCategoryTotals(int userId, int month, int year) {
        try {
            return expenseDAO.getCategoryTotalsByUserIdAndMonth(userId, month, year);
        } catch (SQLException e) {
            throw new DatabaseException("Error getting category totals: " + e.getMessage(), e);
        }
    }

    /**
     * Groups all-time spending by category.
     *
     * @param userId user ID
     * @return map of category to total spent
     * @throws DatabaseException if database query fails
     */
    public Map<String, Double> getAllTimeCategoryTotals(int userId) {
        try {
            return expenseDAO.getAllTimeCategoryTotalsByUserId(userId);
        } catch (SQLException e) {
            throw new DatabaseException("Error getting all time category totals: " + e.getMessage(), e);
        }
    }

    /**
     * Calculates the sum of all digital expenses for a user in a given month.
     *
     * @param userId user ID
     * @param month 1-12
     * @param year calendar year
     * @return monthly digital expense sum
     * @throws DatabaseException if database query fails
     */
    public double getMonthlyTotal(int userId, int month, int year) {
        try {
            return expenseDAO.getSumByUserIdAndMonth(userId, month, year);
        } catch (SQLException e) {
            throw new DatabaseException("Error calculating monthly sum: " + e.getMessage(), e);
        }
    }

    /**
     * Calculates total digital expenses across all time for a user.
     *
     * @param userId user ID
     * @return all-time digital expense sum
     * @throws DatabaseException if database query fails
     */
    public double getTotalSpent(int userId) {
        try {
            return expenseDAO.getTotalSpentByUserId(userId);
        } catch (SQLException e) {
            throw new DatabaseException("Error calculating total spent: " + e.getMessage(), e);
        }
    }

    private void syncBudgetAfterExpense(int userId, String category, LocalDate date) throws SQLException {
        if (date == null) {
            date = LocalDate.now();
        }
        String normalizedCategory = CategoryMapper.normalize(category);
        budgetDAO.recalculateSpentAmountForMonth(userId, normalizedCategory, date.getMonthValue(), date.getYear());
        Budget budget = budgetDAO.findActiveByUserIdAndCategory(userId, normalizedCategory, date);
        if (budget != null) {
            budgetDAO.recalculateSpentAmount(userId, normalizedCategory, budget.getStartDate(), budget.getEndDate());
        }
    }

    // ── FinanceOperation implementation ───────────────────────────────────

    /**
     * {@inheritDoc}
     * Delegates to {@link #addExpense(Expense)}.
     */
    @Override
    public ApiResponse<Expense> execute(Expense expense) {
        return addExpense(expense);
    }

    /**
     * {@inheritDoc}
     * Returns the user's digital expense total for the current month.
     */
    @Override
    public String summarize(int userId) {
        LocalDate now = LocalDate.now();
        double total = getMonthlyTotal(userId, now.getMonthValue(), now.getYear());
        return String.format("₹%.2f digital expenses this month", total);
    }
}

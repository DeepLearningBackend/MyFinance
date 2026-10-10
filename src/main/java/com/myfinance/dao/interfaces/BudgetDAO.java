package com.myfinance.dao.interfaces;

import com.myfinance.model.Budget;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object interface for {@link Budget} entity operations.
 */
public interface BudgetDAO {

    /**
     * Persists a new Budget record into the database.
     *
     * @param budget budget to save
     * @return true if inserted, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean save(Budget budget) throws SQLException;

    /**
     * Finds a budget by primary key ID.
     *
     * @param id budget ID
     * @return Budget if found, or null
     * @throws SQLException if database error occurs
     */
    Budget findById(int id) throws SQLException;

    /**
     * Finds a budget by ID and owning user ID for authorization checks.
     *
     * @param id budget ID
     * @param userId owner user ID
     * @return Budget if authorized, or null
     * @throws SQLException if database error occurs
     */
    Budget findByIdAndUserId(int id, int userId) throws SQLException;

    /**
     * Finds all budgets created by a specific user.
     *
     * @param userId user ID
     * @return list of budgets
     * @throws SQLException if database error occurs
     */
    List<Budget> findByUserId(int userId) throws SQLException;

    /**
     * Finds a budget for a user and category.
     *
     * @param userId user ID
     * @param category category name
     * @return Budget if found, or null
     * @throws SQLException if database error occurs
     */
    Budget findByUserIdAndCategory(int userId, String category) throws SQLException;

    /**
     * Finds the active budget for a category active on the specified date.
     *
     * @param userId user ID
     * @param category category name
     * @param currentDate target active date
     * @return active Budget, or null
     * @throws SQLException if database error occurs
     */
    Budget findActiveByUserIdAndCategory(int userId, String category, LocalDate currentDate) throws SQLException;

    /**
     * Finds budgets where spent_amount exceeds limit_amount.
     *
     * @param userId user ID
     * @return list of over-budget items
     * @throws SQLException if database error occurs
     */
    List<Budget> findOverBudget(int userId) throws SQLException;

    /**
     * Returns all budgets in the system (admin view).
     *
     * @return list of all budgets
     * @throws SQLException if database error occurs
     */
    List<Budget> findAll() throws SQLException;

    /**
     * Updates category, limit amount, and dates of an existing budget.
     *
     * @param budget updated budget entity
     * @return true if updated, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean update(Budget budget) throws SQLException;

    /**
     * Directly updates the cached spent_amount column for a budget.
     *
     * @param budgetId budget ID
     * @param newSpentAmount updated spent total
     * @return true if updated, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean updateSpentAmount(int budgetId, double newSpentAmount) throws SQLException;

    /**
     * Recalculates spent_amount from expenses table across a custom date range.
     *
     * @param userId user ID
     * @param category category name
     * @param startDate period start
     * @param endDate period end
     * @return true if recalculated, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean recalculateSpentAmount(int userId, String category, LocalDate startDate, LocalDate endDate) throws SQLException;

    /**
     * Finds an active budget spanning the given calendar month and year.
     *
     * @param userId user ID
     * @param category category name
     * @param month 1-12
     * @param year calendar year
     * @return Budget matching period, or null
     * @throws SQLException if database error occurs
     */
    Budget findActiveByUserIdAndCategoryAndPeriod(int userId, String category, int month, int year) throws SQLException;

    /**
     * Recalculates spent_amount from expenses for a given calendar month and year.
     *
     * @param userId user ID
     * @param category category name
     * @param month 1-12
     * @param year calendar year
     * @return true if recalculated, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean recalculateSpentAmountForMonth(int userId, String category, int month, int year) throws SQLException;

    /**
     * Maintenance method to normalize category names and synchronize all budget spending caches.
     *
     * @throws SQLException if database error occurs
     */
    void fixExistingBudgetsAndExpenses() throws SQLException;

    /**
     * Deletes a budget by ID for a user.
     *
     * @param id budget ID
     * @param userId user ID
     * @return true if deleted, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean delete(int id, int userId) throws SQLException;
}

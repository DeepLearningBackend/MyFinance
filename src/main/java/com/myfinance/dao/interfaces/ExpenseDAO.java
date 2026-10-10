package com.myfinance.dao.interfaces;

import com.myfinance.model.Expense;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object interface for {@link Expense} entity operations.
 */
public interface ExpenseDAO {

    /**
     * Persists an Expense record into the database.
     *
     * @param expense expense entity to insert
     * @return true if inserted, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean save(Expense expense) throws SQLException;

    /**
     * Finds an expense by primary key ID.
     *
     * @param id expense ID
     * @return Expense entity, or null
     * @throws SQLException if database error occurs
     */
    Expense findById(int id) throws SQLException;

    /**
     * Finds an expense by ID and owner user ID.
     *
     * @param id expense ID
     * @param userId owner user ID
     * @return Expense entity, or null
     * @throws SQLException if database error occurs
     */
    Expense findByIdAndUserId(int id, int userId) throws SQLException;

    /**
     * Finds all expenses logged by a specific user.
     *
     * @param userId owner user ID
     * @return list of expenses
     * @throws SQLException if database error occurs
     */
    List<Expense> findByUserId(int userId) throws SQLException;

    /**
     * Finds expenses logged in a specific calendar month and year.
     *
     * @param userId user ID
     * @param month 1-12
     * @param year calendar year
     * @return list of expenses
     * @throws SQLException if database error occurs
     */
    List<Expense> findByUserIdAndMonth(int userId, int month, int year) throws SQLException;

    /**
     * Finds expenses logged under a specific category name.
     *
     * @param userId user ID
     * @param category category name
     * @return list of expenses
     * @throws SQLException if database error occurs
     */
    List<Expense> findByUserIdAndCategory(int userId, String category) throws SQLException;

    /**
     * Finds the most recent expenses logged by a user.
     *
     * @param userId user ID
     * @param limit maximum count
     * @return list of recent expenses
     * @throws SQLException if database error occurs
     */
    List<Expense> findRecentByUserId(int userId, int limit) throws SQLException;

    /**
     * Filters expenses by month, year, category, and text search keyword.
     *
     * @param userId user ID
     * @param month optional month filter
     * @param year optional year filter
     * @param category optional category filter
     * @param searchQuery optional search keyword
     * @return filtered list of expenses
     * @throws SQLException if database error occurs
     */
    List<Expense> filterExpenses(int userId, Integer month, Integer year, String category, String searchQuery) throws SQLException;

    /**
     * Sums total digital expenses for a user in a given month and year.
     *
     * @param userId user ID
     * @param month 1-12
     * @param year calendar year
     * @return sum of expense amounts
     * @throws SQLException if database error occurs
     */
    double getSumByUserIdAndMonth(int userId, int month, int year) throws SQLException;

    /**
     * Sums total digital expenses for a user across all time.
     *
     * @param userId user ID
     * @return all-time total
     * @throws SQLException if database error occurs
     */
    double getTotalSpentByUserId(int userId) throws SQLException;

    /**
     * Groups monthly spending by category into a category-total map.
     *
     * @param userId user ID
     * @param month 1-12
     * @param year calendar year
     * @return map of category name to total spent
     * @throws SQLException if database error occurs
     */
    Map<String, Double> getCategoryTotalsByUserIdAndMonth(int userId, int month, int year) throws SQLException;

    /**
     * Groups all-time spending by category.
     *
     * @param userId user ID
     * @return map of category to total
     * @throws SQLException if database error occurs
     */
    Map<String, Double> getAllTimeCategoryTotalsByUserId(int userId) throws SQLException;

    /**
     * Returns all expenses in the system (admin view).
     *
     * @return list of all expenses
     * @throws SQLException if database error occurs
     */
    List<Expense> findAll() throws SQLException;

    /**
     * Updates amount, category, date, and description of an existing expense.
     *
     * @param expense updated entity
     * @return true if updated, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean update(Expense expense) throws SQLException;

    /**
     * Deletes an expense by ID for an authorized user.
     *
     * @param id expense ID
     * @param userId user ID
     * @return true if deleted, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean delete(int id, int userId) throws SQLException;

    /**
     * Counts the total number of expense rows in the database (admin stat).
     *
     * @return total count
     * @throws SQLException if database error occurs
     */
    int countAllExpenses() throws SQLException;

    /**
     * Returns category spend shares and day-of-week patterns needed by the
     * Personality Detector.  Uses SQL aggregates DAYOFWEEK and DATE_FORMAT.
     *
     * <p>Result keys:
     * <ul>
     *   <li>{@code category_totals}  — Map&lt;String,Double&gt; category → total spend</li>
     *   <li>{@code weekend_total}    — Double  (DAYOFWEEK IN (1,7))</li>
     *   <li>{@code weekday_total}    — Double  (DAYOFWEEK BETWEEN 2 AND 6)</li>
     *   <li>{@code night_total}      — Double  (placeholder; 0 when time unavailable)</li>
     *   <li>{@code total_expenses}   — Integer count of rows</li>
     * </ul>
     *
     * @param userId target user
     * @param days   look-back window in days (e.g. 30 or 90)
     */
    Map<String, Object> getPersonalityAggregates(int userId, int days) throws SQLException;

    /**
     * Count distinct calendar days on which the user logged at least one expense
     * within the last {@code days} days.
     */
    int countDistinctExpenseDays(int userId, int days) throws SQLException;
}

package com.myfinance.dao.interfaces;

import com.myfinance.model.Goal;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object interface for {@link Goal} entity operations.
 */
public interface GoalDAO {

    /**
     * Persists a financial savings goal to the database.
     *
     * @param goal goal entity to save
     * @return true if inserted, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean save(Goal goal) throws SQLException;

    /**
     * Finds a goal by primary key ID.
     *
     * @param id goal ID
     * @return Goal entity, or null
     * @throws SQLException if database error occurs
     */
    Goal findById(int id) throws SQLException;

    /**
     * Finds a goal by ID and user ID for access verification.
     *
     * @param id goal ID
     * @param userId owner user ID
     * @return Goal entity, or null
     * @throws SQLException if database error occurs
     */
    Goal findByIdAndUserId(int id, int userId) throws SQLException;

    /**
     * Finds all goals created by a user.
     *
     * @param userId user ID
     * @return list of goals
     * @throws SQLException if database error occurs
     */
    List<Goal> findByUserId(int userId) throws SQLException;

    /**
     * Finds goals for a user filtered by status (IN_PROGRESS, ACHIEVED, CANCELLED).
     *
     * @param userId user ID
     * @param status goal lifecycle status
     * @return list of matching goals
     * @throws SQLException if database error occurs
     */
    List<Goal> findByStatus(int userId, Goal.GoalStatus status) throws SQLException;

    /**
     * Returns all goals in the system (admin view).
     *
     * @return list of all goals
     * @throws SQLException if database error occurs
     */
    List<Goal> findAll() throws SQLException;

    /**
     * Updates target amount, title, target date, or status of a goal.
     *
     * @param goal updated entity
     * @return true if updated, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean update(Goal goal) throws SQLException;

    /**
     * Adds incremental savings to a goal's saved_amount.
     *
     * @param goalId goal ID
     * @param userId user ID
     * @param additionalAmount incremental amount to deposit
     * @return true if updated, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean updateSavedAmount(int goalId, int userId, double additionalAmount) throws SQLException;

    /**
     * Transitions a goal's status to ACHIEVED upon completion.
     *
     * @param goalId goal ID
     * @param userId user ID
     * @return true if updated, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean markAsAchieved(int goalId, int userId) throws SQLException;

    /**
     * Deletes a goal by ID for an authorized user.
     *
     * @param id goal ID
     * @param userId user ID
     * @return true if deleted, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean delete(int id, int userId) throws SQLException;
}

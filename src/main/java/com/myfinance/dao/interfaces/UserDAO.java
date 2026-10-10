package com.myfinance.dao.interfaces;

import com.myfinance.model.User;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object interface for {@link User} entity operations.
 */
public interface UserDAO {

    /**
     * Persists a new user record with hashed password into the database.
     *
     * @param user user entity
     * @return true if inserted, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean save(User user) throws SQLException;

    /**
     * Finds a user by primary key ID.
     *
     * @param id user ID
     * @return User entity, or null
     * @throws SQLException if database error occurs
     */
    User findById(int id) throws SQLException;

    /**
     * Finds a user by unique email address.
     *
     * @param email user email
     * @return User entity, or null
     * @throws SQLException if database error occurs
     */
    User findByEmail(String email) throws SQLException;

    /**
     * Returns all registered users in the platform (admin view).
     *
     * @return list of all users
     * @throws SQLException if database error occurs
     */
    List<User> findAll() throws SQLException;

    /**
     * Searches and filters users by name/email query, role, and active status.
     *
     * @param query search text
     * @param role optional role filter
     * @param status optional status filter
     * @return list of matching users
     * @throws SQLException if database error occurs
     */
    List<User> searchUsers(String query, String role, String status) throws SQLException;

    /**
     * Updates profile attributes (name, email, role, status) of an existing user.
     *
     * @param user updated entity
     * @return true if updated, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean update(User user) throws SQLException;

    /**
     * Updates the active/inactive status of a user account.
     *
     * @param userId user ID
     * @param status new status
     * @return true if updated, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean updateStatus(int userId, User.Status status) throws SQLException;

    /**
     * Updates the BCrypt-hashed password of a user account.
     *
     * @param userId user ID
     * @param hashedPassword new BCrypt hash
     * @return true if updated, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean updatePassword(int userId, String hashedPassword) throws SQLException;

    /**
     * Permanently deletes a user account by primary key ID.
     *
     * @param id user ID
     * @return true if deleted, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean delete(int id) throws SQLException;

    /**
     * Returns total registered user count.
     *
     * @return total count
     * @throws SQLException if database error occurs
     */
    int countUsers() throws SQLException;

    /**
     * Returns total active user count.
     *
     * @return active count
     * @throws SQLException if database error occurs
     */
    int countActiveUsers() throws SQLException;

    /**
     * Finds a user matching an SMS webhook API token.
     *
     * @param token secret token
     * @return User entity, or null
     * @throws SQLException if database error occurs
     */
    User findByApiToken(String token) throws SQLException;

    /**
     * Generates and stores a new API token for a user.
     *
     * @param userId user ID
     * @param apiToken new secret token
     * @return true if updated, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean updateApiToken(int userId, String apiToken) throws SQLException;
}

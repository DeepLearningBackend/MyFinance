package com.myfinance.dao.interfaces;

import com.myfinance.model.SmsQueueItem;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object interface for {@link SmsQueueItem} entity operations.
 * Powers the Real-Time SMS Sync innovation.
 */
public interface SmsDAO {

    /**
     * Inserts a newly parsed or raw SMS into the queue with PENDING status.
     *
     * @param item SMS queue entity
     * @return true if inserted, false otherwise
     * @throws SQLException on database error
     */
    boolean insert(SmsQueueItem item) throws SQLException;

    /**
     * Retrieves all pending SMS items for a given user awaiting confirmation.
     *
     * @param userId user identifier
     * @return list of pending SMS items
     * @throws SQLException on database error
     */
    List<SmsQueueItem> findPendingByUserId(int userId) throws SQLException;

    /**
     * Finds an SMS item by its unique ID.
     *
     * @param id SMS record identifier
     * @return SmsQueueItem or null if not found
     * @throws SQLException on database error
     */
    SmsQueueItem findById(int id) throws SQLException;

    /**
     * Updates the status of an SMS item (CONFIRMED or REJECTED).
     *
     * @param id SMS record identifier
     * @param status new status
     * @return true if updated, false otherwise
     * @throws SQLException on database error
     */
    boolean updateStatus(int id, SmsQueueItem.Status status) throws SQLException;

    /**
     * Retrieves the most recently created SMS item for a user (to determine Live Sync status).
     *
     * @param userId user identifier
     * @return latest SmsQueueItem or null if none
     * @throws SQLException on database error
     */
    SmsQueueItem findLatestByUserId(int userId) throws SQLException;

    /**
     * Retrieves all SMS queue items for a user.
     *
     * @param userId user identifier
     * @return list of all user SMS items
     * @throws SQLException on database error
     */
    List<SmsQueueItem> findAllByUserId(int userId) throws SQLException;

    /**
     * Retrieves all pending SMS items across all users (for background worker).
     *
     * @return list of all pending SMS items
     * @throws SQLException on database error
     */
    List<SmsQueueItem> findPendingAll() throws SQLException;
}

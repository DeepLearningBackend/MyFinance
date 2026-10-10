package com.myfinance.dao.interfaces;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object interface for audit logging of administrative and security events.
 */
public interface AuditLogDAO {

    /**
     * Records an administrative or security action in the audit log table.
     *
     * @param adminId ID of the admin performing the action
     * @param action description of the event or action
     * @param targetUserId optional affected user ID, or null
     * @return true if successfully inserted, false otherwise
     * @throws SQLException if database execution fails
     */
    boolean log(int adminId, String action, Integer targetUserId) throws SQLException;

    /**
     * Retrieves the most recent audit log entries ordered newest first.
     *
     * @param limit maximum count of log records to return
     * @return list of audit log row maps
     * @throws SQLException if database query fails
     */
    List<Map<String, Object>> findRecentLogs(int limit) throws SQLException;

    /**
     * Returns the aggregate count of logged audit events.
     *
     * @return total count of audit rows
     * @throws SQLException if database query fails
     */
    int countTotalLogs() throws SQLException;
}

package com.myfinance.dao.interfaces;

import java.sql.SQLException;
import java.util.Map;

/**
 * Data Access Object interface for managing platform-wide configuration settings.
 */
public interface SystemSettingsDAO {

    /**
     * Retrieves all global platform configuration keys and values.
     *
     * @return map of setting key to value
     * @throws SQLException if database error occurs
     */
    Map<String, String> getAllSettings() throws SQLException;

    /**
     * Reads a specific setting value by key, returning a fallback default if absent.
     *
     * @param key configuration key name
     * @param defaultValue default value if key is not configured
     * @return setting value string
     * @throws SQLException if database error occurs
     */
    String getSetting(String key, String defaultValue) throws SQLException;

    /**
     * Updates or inserts a single system setting key and value.
     *
     * @param key configuration key
     * @param value configuration value
     * @param updatedBy administrator user ID
     * @return true if updated, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean updateSetting(String key, String value, int updatedBy) throws SQLException;

    /**
     * Batch updates multiple platform configuration key-value pairs.
     *
     * @param settings map of settings to update
     * @param updatedBy administrator user ID
     * @return true if all updated, false otherwise
     * @throws SQLException if database error occurs
     */
    boolean updateSettings(Map<String, String> settings, int updatedBy) throws SQLException;
}

package com.myfinance.dao.impl;

import com.myfinance.dao.interfaces.SystemSettingsDAO;
import com.myfinance.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Raw JDBC implementation of {@link SystemSettingsDAO}.
 */
public class SystemSettingsDAOImpl implements SystemSettingsDAO {

    @Override
    public Map<String, String> getAllSettings() throws SQLException {
        String sql = "SELECT setting_key, setting_value FROM system_settings ORDER BY setting_key ASC";
        Map<String, String> settings = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                settings.put(rs.getString("setting_key"), rs.getString("setting_value"));
            }
        }
        return settings;
    }

    @Override
    public String getSetting(String key, String defaultValue) throws SQLException {
        String sql = "SELECT setting_value FROM system_settings WHERE setting_key = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String val = rs.getString("setting_value");
                    return val != null ? val : defaultValue;
                }
            }
        }
        return defaultValue;
    }

    @Override
    public boolean updateSetting(String key, String value, int updatedBy) throws SQLException {
        String sql = "INSERT INTO system_settings (setting_key, setting_value, updated_by) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), updated_by = VALUES(updated_by)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, key);
            ps.setString(2, value);
            ps.setInt(3, updatedBy);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateSettings(Map<String, String> settings, int updatedBy) throws SQLException {
        if (settings == null || settings.isEmpty()) {
            return false;
        }
        String sql = "INSERT INTO system_settings (setting_key, setting_value, updated_by) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE setting_value = VALUES(setting_value), updated_by = VALUES(updated_by)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);
            for (Map.Entry<String, String> entry : settings.entrySet()) {
                ps.setString(1, entry.getKey());
                ps.setString(2, entry.getValue());
                ps.setInt(3, updatedBy);
                ps.addBatch();
            }

            int[] results = ps.executeBatch();
            conn.commit();
            return results.length > 0;
        } catch (SQLException e) {
            throw e;
        }
    }
}

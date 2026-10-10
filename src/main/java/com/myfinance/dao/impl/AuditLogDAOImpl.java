package com.myfinance.dao.impl;

import com.myfinance.dao.interfaces.AuditLogDAO;
import com.myfinance.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Raw JDBC implementation of {@link AuditLogDAO}.
 */
public class AuditLogDAOImpl implements AuditLogDAO {

    @Override
    public boolean log(int adminId, String action, Integer targetUserId) throws SQLException {
        String sql = "INSERT INTO audit_logs (admin_id, action, target_user) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, adminId);
            ps.setString(2, action);
            if (targetUserId != null && targetUserId > 0) {
                ps.setInt(3, targetUserId);
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Map<String, Object>> findRecentLogs(int limit) throws SQLException {
        String sql = "SELECT a.id, a.admin_id, u.name AS admin_name, u.email AS admin_email, " +
                "a.action, a.target_user, tu.name AS target_user_name, a.timestamp " +
                "FROM audit_logs a " +
                "LEFT JOIN users u ON a.admin_id = u.id " +
                "LEFT JOIN users tu ON a.target_user = tu.id " +
                "ORDER BY a.timestamp DESC LIMIT ?";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getInt("id"));
                    row.put("adminId", rs.getInt("admin_id"));
                    row.put("adminName", rs.getString("admin_name") != null ? rs.getString("admin_name") : "System");
                    row.put("adminEmail", rs.getString("admin_email"));
                    row.put("action", rs.getString("action"));
                    row.put("targetUser", rs.getObject("target_user"));
                    row.put("targetUserName", rs.getString("target_user_name"));
                    row.put("timestamp", rs.getTimestamp("timestamp"));
                    list.add(row);
                }
            }
        }
        return list;
    }

    @Override
    public int countTotalLogs() throws SQLException {
        String sql = "SELECT COUNT(*) FROM audit_logs";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}

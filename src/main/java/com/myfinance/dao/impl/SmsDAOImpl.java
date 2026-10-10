package com.myfinance.dao.impl;

import com.myfinance.dao.interfaces.SmsDAO;
import com.myfinance.model.SmsQueueItem;
import com.myfinance.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Raw JDBC implementation of {@link SmsDAO}.
 * Strictly adheres to PreparedStatement and try-with-resources conventions.
 */
public class SmsDAOImpl implements SmsDAO {

    @Override
    public boolean insert(SmsQueueItem item) throws SQLException {
        String sql = "INSERT INTO sms_queue (user_id, raw_sms, parsed_amount, parsed_merchant, upi_ref, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, item.getUserId());
            ps.setString(2, item.getRawSms());
            ps.setDouble(3, item.getParsedAmount());
            ps.setString(4, item.getParsedMerchant());
            ps.setString(5, item.getUpiRef());
            ps.setString(6, item.getStatus() != null ? item.getStatus().name() : SmsQueueItem.Status.PENDING.name());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public List<SmsQueueItem> findPendingByUserId(int userId) throws SQLException {
        String sql = "SELECT id, user_id, raw_sms, parsed_amount, parsed_merchant, upi_ref, status, created_at FROM sms_queue WHERE user_id = ? AND status = 'PENDING' ORDER BY id DESC";
        List<SmsQueueItem> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToSmsItem(rs));
                }
            }
        }
        return list;
    }

    @Override
    public SmsQueueItem findById(int id) throws SQLException {
        String sql = "SELECT id, user_id, raw_sms, parsed_amount, parsed_merchant, upi_ref, status, created_at FROM sms_queue WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToSmsItem(rs);
                }
            }
        }
        return null;
    }

    @Override
    public boolean updateStatus(int id, SmsQueueItem.Status status) throws SQLException {
        String sql = "UPDATE sms_queue SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public SmsQueueItem findLatestByUserId(int userId) throws SQLException {
        String sql = "SELECT id, user_id, raw_sms, parsed_amount, parsed_merchant, upi_ref, status, created_at FROM sms_queue WHERE user_id = ? ORDER BY created_at DESC, id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToSmsItem(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<SmsQueueItem> findAllByUserId(int userId) throws SQLException {
        String sql = "SELECT id, user_id, raw_sms, parsed_amount, parsed_merchant, upi_ref, status, created_at FROM sms_queue WHERE user_id = ? ORDER BY id DESC";
        List<SmsQueueItem> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToSmsItem(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<SmsQueueItem> findPendingAll() throws SQLException {
        String sql = "SELECT id, user_id, raw_sms, parsed_amount, parsed_merchant, upi_ref, status, created_at FROM sms_queue WHERE status = 'PENDING' ORDER BY id ASC";
        List<SmsQueueItem> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToSmsItem(rs));
            }
        }
        return list;
    }

    private SmsQueueItem mapRowToSmsItem(ResultSet rs) throws SQLException {
        SmsQueueItem item = new SmsQueueItem();
        item.setId(rs.getInt("id"));
        item.setUserId(rs.getInt("user_id"));
        item.setRawSms(rs.getString("raw_sms"));
        item.setParsedAmount(rs.getDouble("parsed_amount"));
        item.setParsedMerchant(rs.getString("parsed_merchant"));
        item.setUpiRef(rs.getString("upi_ref"));

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try {
                item.setStatus(SmsQueueItem.Status.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                item.setStatus(SmsQueueItem.Status.PENDING);
            }
        }

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            item.setCreatedAt(ts.toLocalDateTime());
        }

        return item;
    }
}

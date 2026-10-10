package com.myfinance.dao.impl;

import com.myfinance.dao.interfaces.CashDAO;
import com.myfinance.exception.DatabaseException;
import com.myfinance.model.CashEntry;
import com.myfinance.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Raw JDBC implementation of {@link CashDAO}.
 * <p>
 * Every write that changes a user's physical cash balance also updates
 * {@code users.cash_in_hand} atomically so the running balance is always
 * consistent (no separate recalculation step needed).
 * </p>
 * Follows project golden rules:
 * <ul>
 *   <li>PreparedStatement for every query — no string concatenation in SQL</li>
 *   <li>try-with-resources on every Connection and PreparedStatement</li>
 *   <li>Throws {@link DatabaseException} (RuntimeException) on unexpected failure</li>
 * </ul>
 */
public class CashDAOImpl implements CashDAO {

    // ── INSERT ────────────────────────────────────────────────────────────

    @Override
    public boolean save(CashEntry entry) throws SQLException {
        // 1. Insert the cash_entry row
        String insertSql = "INSERT INTO cash_entries (user_id, amount, category, entry_date, description) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, entry.getUserId());
            ps.setDouble(2, entry.getAmount());
            ps.setString(3, entry.getCategory());
            ps.setDate(4, Date.valueOf(entry.getEntryDate()));
            ps.setString(5, entry.getDescription());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        entry.setId(rs.getInt(1));
                    }
                }
                // 2. Deduct from users.cash_in_hand
                deductCashInHand(entry.getUserId(), entry.getAmount());
                return true;
            }
            return false;
        }
    }

    // ── SELECT ────────────────────────────────────────────────────────────

    @Override
    public CashEntry findById(int id) throws SQLException {
        String sql = "SELECT id, user_id, amount, category, entry_date, description, created_at "
                + "FROM cash_entries WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<CashEntry> findByUserId(int userId) throws SQLException {
        String sql = "SELECT id, user_id, amount, category, entry_date, description, created_at "
                + "FROM cash_entries WHERE user_id = ? ORDER BY entry_date DESC, created_at DESC";
        List<CashEntry> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<CashEntry> findByUserIdAndMonth(int userId, int month, int year) throws SQLException {
        String sql = "SELECT id, user_id, amount, category, entry_date, description, created_at "
                + "FROM cash_entries "
                + "WHERE user_id = ? AND MONTH(entry_date) = ? AND YEAR(entry_date) = ? "
                + "ORDER BY entry_date DESC, created_at DESC";
        List<CashEntry> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    // ── UPDATE ────────────────────────────────────────────────────────────

    @Override
    public boolean update(CashEntry entry) throws SQLException {
        // Fetch old amount first so we can reconcile cash_in_hand
        CashEntry old = findById(entry.getId());
        if (old == null || old.getUserId() != entry.getUserId()) {
            return false; // security: must own the row
        }

        String sql = "UPDATE cash_entries SET amount = ?, category = ?, entry_date = ?, description = ? "
                + "WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, entry.getAmount());
            ps.setString(2, entry.getCategory());
            ps.setDate(3, Date.valueOf(entry.getEntryDate()));
            ps.setString(4, entry.getDescription());
            ps.setInt(5, entry.getId());
            ps.setInt(6, entry.getUserId());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                // Adjust cash_in_hand: restore old amount then deduct new
                double diff = old.getAmount() - entry.getAmount();
                if (diff != 0) {
                    adjustCashInHand(entry.getUserId(), diff); // positive diff → restore balance
                }
                return true;
            }
            return false;
        }
    }

    // ── DELETE ────────────────────────────────────────────────────────────

    @Override
    public boolean delete(int id, int userId) throws SQLException {
        // Read the amount before deleting so we can restore cash_in_hand
        CashEntry entry = findById(id);
        if (entry == null || entry.getUserId() != userId) {
            return false; // not found or wrong user
        }

        String sql = "DELETE FROM cash_entries WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                // Restore the deleted amount back to cash_in_hand
                adjustCashInHand(userId, entry.getAmount());
                return true;
            }
            return false;
        }
    }

    // ── cash_in_hand helpers ──────────────────────────────────────────────

    @Override
    public boolean setCashInHand(int userId, double amount) throws SQLException {
        String sql = "UPDATE users SET cash_in_hand = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public double getCashInHand(int userId) throws SQLException {
        String sql = "SELECT cash_in_hand FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("cash_in_hand");
                }
            }
        }
        return 0.0;
    }

    // ── aggregates ────────────────────────────────────────────────────────

    @Override
    public double getTotalSpentByMonth(int userId, int month, int year) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM cash_entries "
                + "WHERE user_id = ? AND MONTH(entry_date) = ? AND YEAR(entry_date) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    @Override
    public double getTotalSpentAllTime(int userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM cash_entries WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    // ── private helpers ───────────────────────────────────────────────────

    /** Deduct {@code amount} from cash_in_hand (called on save). */
    private void deductCashInHand(int userId, double amount) throws SQLException {
        String sql = "UPDATE users SET cash_in_hand = cash_in_hand - ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    /** Add {@code delta} to cash_in_hand (positive = restore, negative = deduct). */
    private void adjustCashInHand(int userId, double delta) throws SQLException {
        String sql = "UPDATE users SET cash_in_hand = cash_in_hand + ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, delta);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    /** Map a ResultSet row → CashEntry. */
    private CashEntry mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int userId = rs.getInt("user_id");
        double amount = rs.getDouble("amount");
        String category = rs.getString("category");
        LocalDate entryDate = rs.getDate("entry_date").toLocalDate();
        String description = rs.getString("description");
        Timestamp ts = rs.getTimestamp("created_at");
        return new CashEntry(id, userId, amount, category, entryDate, description,
                ts != null ? ts.toLocalDateTime() : null);
    }
}

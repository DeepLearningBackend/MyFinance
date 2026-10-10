package com.myfinance.dao.impl;

import com.myfinance.dao.interfaces.ExpenseDAO;
import com.myfinance.model.Expense;
import com.myfinance.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Raw JDBC implementation of {@link ExpenseDAO}.
 * Supports secure operations scoped by userId to enforce data isolation.
 */
public class ExpenseDAOImpl implements ExpenseDAO {

    @Override
    public boolean save(Expense expense) throws SQLException {
        String sql = "INSERT INTO expenses (user_id, title, amount, category, expense_date, source, upi_ref, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, expense.getUserId());
            ps.setString(2, expense.getTitle());
            ps.setDouble(3, expense.getAmount());
            ps.setString(4, expense.getCategory());
            ps.setDate(5, Date.valueOf(expense.getExpenseDate()));
            ps.setString(6, expense.getSource() != null ? expense.getSource() : "MANUAL");
            ps.setString(7, expense.getUpiRef());
            ps.setString(8, expense.getDescription());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        expense.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public Expense findById(int id) throws SQLException {
        String sql = "SELECT id, user_id, title, amount, category, expense_date, description, created_at FROM expenses WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToExpense(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Expense findByIdAndUserId(int id, int userId) throws SQLException {
        String sql = "SELECT id, user_id, title, amount, category, expense_date, description, created_at FROM expenses WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToExpense(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Expense> findByUserId(int userId) throws SQLException {
        String sql = "SELECT id, user_id, title, amount, category, expense_date, description, created_at FROM expenses WHERE user_id = ? ORDER BY expense_date DESC, id DESC";
        List<Expense> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToExpense(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Expense> findByUserIdAndMonth(int userId, int month, int year) throws SQLException {
        String sql = "SELECT id, user_id, title, amount, category, expense_date, description, created_at " +
                "FROM expenses WHERE user_id = ? AND MONTH(expense_date) = ? AND YEAR(expense_date) = ? " +
                "ORDER BY expense_date DESC, id DESC";
        List<Expense> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToExpense(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Expense> findByUserIdAndCategory(int userId, String category) throws SQLException {
        String sql = "SELECT id, user_id, title, amount, category, expense_date, description, created_at " +
                "FROM expenses WHERE user_id = ? AND category = ? ORDER BY expense_date DESC, id DESC";
        List<Expense> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToExpense(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Expense> findRecentByUserId(int userId, int limit) throws SQLException {
        String sql = "SELECT id, user_id, title, amount, category, expense_date, description, created_at " +
                "FROM expenses WHERE user_id = ? ORDER BY expense_date DESC, id DESC LIMIT ?";
        List<Expense> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToExpense(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Expense> filterExpenses(int userId, Integer month, Integer year, String category, String searchQuery) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT id, user_id, title, amount, category, expense_date, description, created_at FROM expenses WHERE user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (month != null && month > 0) {
            sql.append(" AND MONTH(expense_date) = ?");
            params.add(month);
        }
        if (year != null && year > 0) {
            sql.append(" AND YEAR(expense_date) = ?");
            params.add(year);
        }
        if (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category)) {
            sql.append(" AND category = ?");
            params.add(category.trim());
        }
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            sql.append(" AND (LOWER(title) LIKE ? OR LOWER(description) LIKE ?)");
            String wildcard = "%" + searchQuery.trim().toLowerCase() + "%";
            params.add(wildcard);
            params.add(wildcard);
        }
        sql.append(" ORDER BY expense_date DESC, id DESC");

        List<Expense> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToExpense(rs));
                }
            }
        }
        return list;
    }

    @Override
    public double getSumByUserIdAndMonth(int userId, int month, int year) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE user_id = ? AND MONTH(expense_date) = ? AND YEAR(expense_date) = ?";
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
    public double getTotalSpentByUserId(int userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE user_id = ?";
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

    @Override
    public Map<String, Double> getCategoryTotalsByUserIdAndMonth(int userId, int month, int year) throws SQLException {
        String sql = "SELECT category, COALESCE(SUM(amount), 0.0) AS total FROM expenses " +
                "WHERE user_id = ? AND MONTH(expense_date) = ? AND YEAR(expense_date) = ? " +
                "GROUP BY category ORDER BY total DESC";
        Map<String, Double> map = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("category"), rs.getDouble("total"));
                }
            }
        }
        return map;
    }

    @Override
    public Map<String, Double> getAllTimeCategoryTotalsByUserId(int userId) throws SQLException {
        String sql = "SELECT category, COALESCE(SUM(amount), 0.0) AS total FROM expenses " +
                "WHERE user_id = ? GROUP BY category ORDER BY total DESC";
        Map<String, Double> map = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getString("category"), rs.getDouble("total"));
                }
            }
        }
        return map;
    }

    @Override
    public List<Expense> findAll() throws SQLException {
        String sql = "SELECT id, user_id, title, amount, category, expense_date, description, created_at FROM expenses ORDER BY id DESC";
        List<Expense> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToExpense(rs));
            }
        }
        return list;
    }

    @Override
    public boolean update(Expense expense) throws SQLException {
        String sql = "UPDATE expenses SET title = ?, amount = ?, category = ?, expense_date = ?, description = ? WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, expense.getTitle());
            ps.setDouble(2, expense.getAmount());
            ps.setString(3, expense.getCategory());
            ps.setDate(4, Date.valueOf(expense.getExpenseDate()));
            ps.setString(5, expense.getDescription());
            ps.setInt(6, expense.getId());
            ps.setInt(7, expense.getUserId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id, int userId) throws SQLException {
        String sql = "DELETE FROM expenses WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int countAllExpenses() throws SQLException {
        String sql = "SELECT COUNT(*) FROM expenses";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private Expense mapRowToExpense(ResultSet rs) throws SQLException {
        Expense expense = new Expense();
        expense.setId(rs.getInt("id"));
        expense.setUserId(rs.getInt("user_id"));
        expense.setTitle(rs.getString("title"));
        expense.setAmount(rs.getDouble("amount"));
        expense.setCategory(rs.getString("category"));

        Date d = rs.getDate("expense_date");
        if (d != null) {
            expense.setExpenseDate(d.toLocalDate());
        }

        expense.setDescription(rs.getString("description"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            expense.setCreatedAt(ts.toLocalDateTime());
        }

        try {
            String src = rs.getString("source");
            if (src != null) {
                expense.setSource(src);
            }
        } catch (SQLException ignored) {
        }

        try {
            expense.setUpiRef(rs.getString("upi_ref"));
        } catch (SQLException ignored) {
        }

        return expense;
    }

    // ── Spending Personality aggregates (Innovation 6) ────────────────────

    /**
     * Fetches category totals + weekend vs weekday breakdown using SQL
     * aggregates (DAYOFWEEK, DATE_FORMAT) for the Personality Detector.
     */
    @Override
    public Map<String, Object> getPersonalityAggregates(int userId, int days) throws SQLException {
        Map<String, Object> result = new java.util.HashMap<>();

        // 1. Category totals using DATE_FORMAT window
        String catSql = "SELECT category, COALESCE(SUM(amount), 0) AS total, COUNT(*) AS cnt " +
                "FROM expenses " +
                "WHERE user_id = ? AND expense_date >= DATE_SUB(CURDATE(), INTERVAL ? DAY) " +
                "GROUP BY category ORDER BY total DESC";

        Map<String, Double> categoryTotals = new java.util.LinkedHashMap<>();
        int totalCount = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(catSql)) {
            ps.setInt(1, userId);
            ps.setInt(2, days);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categoryTotals.put(rs.getString("category"), rs.getDouble("total"));
                    totalCount += rs.getInt("cnt");
                }
            }
        }
        result.put("category_totals", categoryTotals);
        result.put("total_expenses", totalCount);

        // 2. Weekend vs weekday spend using DAYOFWEEK (1=Sun, 7=Sat)
        String dowSql = "SELECT " +
                "COALESCE(SUM(CASE WHEN DAYOFWEEK(expense_date) IN (1,7) THEN amount ELSE 0 END), 0) AS weekend_total, " +
                "COALESCE(SUM(CASE WHEN DAYOFWEEK(expense_date) BETWEEN 2 AND 6 THEN amount ELSE 0 END), 0) AS weekday_total " +
                "FROM expenses " +
                "WHERE user_id = ? AND expense_date >= DATE_SUB(CURDATE(), INTERVAL ? DAY)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(dowSql)) {
            ps.setInt(1, userId);
            ps.setInt(2, days);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result.put("weekend_total", rs.getDouble("weekend_total"));
                    result.put("weekday_total", rs.getDouble("weekday_total"));
                }
            }
        }

        // night_total is a placeholder (expense_date has no time component)
        result.put("night_total", 0.0);
        return result;
    }

    @Override
    public int countDistinctExpenseDays(int userId, int days) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT expense_date) " +
                "FROM expenses " +
                "WHERE user_id = ? AND expense_date >= DATE_SUB(CURDATE(), INTERVAL ? DAY)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, days);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }
}


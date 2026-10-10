package com.myfinance.dao.impl;

import com.myfinance.dao.interfaces.BudgetDAO;
import com.myfinance.model.Budget;
import com.myfinance.util.CategoryMapper;
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
 * Raw JDBC implementation of {@link BudgetDAO}.
 * Enforces secure queries, spent amount calculation, and alert thresholds.
 */
public class BudgetDAOImpl implements BudgetDAO {

    @Override
    public boolean save(Budget budget) throws SQLException {
        String sql = "INSERT INTO budgets (user_id, category, limit_amount, spent_amount, start_date, end_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, budget.getUserId());
            ps.setString(2, budget.getCategory());
            ps.setDouble(3, budget.getLimitAmount());
            ps.setDouble(4, budget.getSpentAmount());
            ps.setDate(5, Date.valueOf(budget.getStartDate()));
            ps.setDate(6, Date.valueOf(budget.getEndDate()));

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        budget.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public Budget findById(int id) throws SQLException {
        String sql = "SELECT id, user_id, category, limit_amount, spent_amount, start_date, end_date, created_at FROM budgets WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBudget(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Budget findByIdAndUserId(int id, int userId) throws SQLException {
        String sql = "SELECT id, user_id, category, limit_amount, spent_amount, start_date, end_date, created_at FROM budgets WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBudget(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Budget> findByUserId(int userId) throws SQLException {
        String sql = "SELECT id, user_id, category, limit_amount, spent_amount, start_date, end_date, created_at FROM budgets WHERE user_id = ? ORDER BY id DESC";
        List<Budget> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToBudget(rs));
                }
            }
        }
        return list;
    }

    @Override
    public Budget findByUserIdAndCategory(int userId, String category) throws SQLException {
        String sql = "SELECT id, user_id, category, limit_amount, spent_amount, start_date, end_date, created_at " +
                "FROM budgets WHERE user_id = ? AND category = ? ORDER BY end_date DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, category);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBudget(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Budget findActiveByUserIdAndCategory(int userId, String category, LocalDate currentDate) throws SQLException {
        if (currentDate == null) {
            currentDate = LocalDate.now();
        }
        String normalizedCategory = CategoryMapper.normalize(category);
        String sql = "SELECT id, user_id, category, limit_amount, spent_amount, start_date, end_date, created_at " +
                "FROM budgets WHERE user_id = ? AND category = ? " +
                "AND ((start_date <= ? AND end_date >= ?) " +
                "     OR (MONTH(start_date) = ? AND YEAR(start_date) = ?)) " +
                "ORDER BY id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, normalizedCategory);
            ps.setDate(3, Date.valueOf(currentDate));
            ps.setDate(4, Date.valueOf(currentDate));
            ps.setInt(5, currentDate.getMonthValue());
            ps.setInt(6, currentDate.getYear());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBudget(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Budget findActiveByUserIdAndCategoryAndPeriod(int userId, String category, int month, int year) throws SQLException {
        String normalizedCategory = CategoryMapper.normalize(category);
        LocalDate sampleDate = LocalDate.of(year, month, 15);
        String sql = "SELECT id, user_id, category, limit_amount, spent_amount, start_date, end_date, created_at " +
                "FROM budgets WHERE user_id = ? AND category = ? " +
                "AND ((MONTH(start_date) = ? AND YEAR(start_date) = ?) " +
                "     OR (MONTH(end_date) = ? AND YEAR(end_date) = ?) " +
                "     OR (start_date <= ? AND end_date >= ?)) " +
                "ORDER BY id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, normalizedCategory);
            ps.setInt(3, month);
            ps.setInt(4, year);
            ps.setInt(5, month);
            ps.setInt(6, year);
            ps.setDate(7, Date.valueOf(sampleDate));
            ps.setDate(8, Date.valueOf(sampleDate));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToBudget(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Budget> findOverBudget(int userId) throws SQLException {
        String sql = "SELECT id, user_id, category, limit_amount, spent_amount, start_date, end_date, created_at " +
                "FROM budgets WHERE user_id = ? AND spent_amount >= limit_amount ORDER BY (spent_amount - limit_amount) DESC";
        List<Budget> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToBudget(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Budget> findAll() throws SQLException {
        String sql = "SELECT id, user_id, category, limit_amount, spent_amount, start_date, end_date, created_at FROM budgets ORDER BY id DESC";
        List<Budget> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToBudget(rs));
            }
        }
        return list;
    }

    @Override
    public boolean update(Budget budget) throws SQLException {
        String sql = "UPDATE budgets SET category = ?, limit_amount = ?, spent_amount = ?, start_date = ?, end_date = ? " +
                "WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, budget.getCategory());
            ps.setDouble(2, budget.getLimitAmount());
            ps.setDouble(3, budget.getSpentAmount());
            ps.setDate(4, Date.valueOf(budget.getStartDate()));
            ps.setDate(5, Date.valueOf(budget.getEndDate()));
            ps.setInt(6, budget.getId());
            ps.setInt(7, budget.getUserId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateSpentAmount(int budgetId, double newSpentAmount) throws SQLException {
        String sql = "UPDATE budgets SET spent_amount = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, newSpentAmount);
            ps.setInt(2, budgetId);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean recalculateSpentAmount(int userId, String category, LocalDate startDate, LocalDate endDate) throws SQLException {
        String normalizedCategory = CategoryMapper.normalize(category);
        String sumSql = "SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE user_id = ? AND category = ? AND expense_date >= ? AND expense_date <= ?";
        String updateSql = "UPDATE budgets SET spent_amount = ? WHERE user_id = ? AND category = ? AND start_date = ? AND end_date = ?";

        try (Connection conn = DBConnection.getConnection()) {
            double totalSpent = 0.0;
            try (PreparedStatement psSum = conn.prepareStatement(sumSql)) {
                psSum.setInt(1, userId);
                psSum.setString(2, normalizedCategory);
                psSum.setDate(3, Date.valueOf(startDate));
                psSum.setDate(4, Date.valueOf(endDate));
                try (ResultSet rs = psSum.executeQuery()) {
                    if (rs.next()) {
                        totalSpent = rs.getDouble(1);
                    }
                }
            }

            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setDouble(1, totalSpent);
                psUpdate.setInt(2, userId);
                psUpdate.setString(3, normalizedCategory);
                psUpdate.setDate(4, Date.valueOf(startDate));
                psUpdate.setDate(5, Date.valueOf(endDate));
                int rows = psUpdate.executeUpdate();
                if (rows == 0) {
                    // Fallback to update by user_id and category if exact date range didn't match
                    String fallbackSql = "UPDATE budgets SET spent_amount = ? WHERE user_id = ? AND category = ? AND start_date <= ? AND end_date >= ?";
                    try (PreparedStatement psFb = conn.prepareStatement(fallbackSql)) {
                        psFb.setDouble(1, totalSpent);
                        psFb.setInt(2, userId);
                        psFb.setString(3, normalizedCategory);
                        psFb.setDate(4, Date.valueOf(endDate));
                        psFb.setDate(5, Date.valueOf(startDate));
                        return psFb.executeUpdate() > 0;
                    }
                }
                return rows > 0;
            }
        }
    }

    @Override
    public boolean recalculateSpentAmountForMonth(int userId, String category, int month, int year) throws SQLException {
        String normalizedCategory = CategoryMapper.normalize(category);
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);

        String sumSql = "SELECT COALESCE(SUM(amount), 0.0) FROM expenses " +
                "WHERE user_id = ? AND category = ? AND expense_date >= ? AND expense_date <= ?";
        String updateSql = "UPDATE budgets SET spent_amount = ? " +
                "WHERE user_id = ? AND category = ? " +
                "AND ((MONTH(start_date) = ? AND YEAR(start_date) = ?) " +
                "     OR (MONTH(end_date) = ? AND YEAR(end_date) = ?) " +
                "     OR (start_date <= ? AND end_date >= ?))";

        try (Connection conn = DBConnection.getConnection()) {
            double totalSpent = 0.0;
            try (PreparedStatement psSum = conn.prepareStatement(sumSql)) {
                psSum.setInt(1, userId);
                psSum.setString(2, normalizedCategory);
                psSum.setDate(3, Date.valueOf(startOfMonth));
                psSum.setDate(4, Date.valueOf(endOfMonth));
                try (ResultSet rs = psSum.executeQuery()) {
                    if (rs.next()) {
                        totalSpent = rs.getDouble(1);
                    }
                }
            }

            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setDouble(1, totalSpent);
                psUpdate.setInt(2, userId);
                psUpdate.setString(3, normalizedCategory);
                psUpdate.setInt(4, month);
                psUpdate.setInt(5, year);
                psUpdate.setInt(6, month);
                psUpdate.setInt(7, year);
                psUpdate.setDate(8, Date.valueOf(startOfMonth));
                psUpdate.setDate(9, Date.valueOf(endOfMonth));
                return psUpdate.executeUpdate() > 0;
            }
        }
    }

    @Override
    public void fixExistingBudgetsAndExpenses() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            // 1. Normalize expense categories in DB
            stmt.executeUpdate("UPDATE expenses SET category = 'Food & Dining' WHERE category IN ('Food', 'Dining')");
            stmt.executeUpdate("UPDATE expenses SET category = 'Transport & Fuel' WHERE category IN ('Transport', 'Travel', 'Fuel')");
            stmt.executeUpdate("UPDATE expenses SET category = 'Bills & Utilities' WHERE category IN ('Bills', 'Utilities')");
            stmt.executeUpdate("UPDATE expenses SET category = 'Rent & Housing' WHERE category IN ('Housing', 'Rent')");
            stmt.executeUpdate("UPDATE expenses SET category = 'Health & Medical' WHERE category IN ('Health', 'Medical')");
            stmt.executeUpdate("UPDATE expenses SET category = 'Investment & Savings' WHERE category IN ('Investment', 'Savings')");

            // 2. Normalize budget categories in DB
            stmt.executeUpdate("UPDATE budgets SET category = 'Food & Dining' WHERE category IN ('Food', 'Dining')");
            stmt.executeUpdate("UPDATE budgets SET category = 'Transport & Fuel' WHERE category IN ('Transport', 'Travel', 'Fuel')");
            stmt.executeUpdate("UPDATE budgets SET category = 'Bills & Utilities' WHERE category IN ('Bills', 'Utilities')");
            stmt.executeUpdate("UPDATE budgets SET category = 'Rent & Housing' WHERE category IN ('Housing', 'Rent')");
            stmt.executeUpdate("UPDATE budgets SET category = 'Health & Medical' WHERE category IN ('Health', 'Medical')");
            stmt.executeUpdate("UPDATE budgets SET category = 'Investment & Savings' WHERE category IN ('Investment', 'Savings')");
        }

        // 3. Recalculate spent_amount for all existing budgets from expenses table
        List<Budget> allBudgets = findAll();
        for (Budget b : allBudgets) {
            String sumSql = "SELECT COALESCE(SUM(amount), 0.0) FROM expenses " +
                    "WHERE user_id = ? AND category = ? AND expense_date >= ? AND expense_date <= ?";
            String updateSql = "UPDATE budgets SET spent_amount = ? WHERE id = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement psSum = conn.prepareStatement(sumSql);
                 PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {

                psSum.setInt(1, b.getUserId());
                psSum.setString(2, b.getCategory());
                psSum.setDate(3, Date.valueOf(b.getStartDate()));
                psSum.setDate(4, Date.valueOf(b.getEndDate()));

                double total = 0.0;
                try (ResultSet rs = psSum.executeQuery()) {
                    if (rs.next()) {
                        total = rs.getDouble(1);
                    }
                }

                psUpdate.setDouble(1, total);
                psUpdate.setInt(2, b.getId());
                psUpdate.executeUpdate();
            }
        }
    }

    @Override
    public boolean delete(int id, int userId) throws SQLException {
        String sql = "DELETE FROM budgets WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        }
    }

    private Budget mapRowToBudget(ResultSet rs) throws SQLException {
        Budget budget = new Budget();
        budget.setId(rs.getInt("id"));
        budget.setUserId(rs.getInt("user_id"));
        budget.setCategory(rs.getString("category"));
        budget.setLimitAmount(rs.getDouble("limit_amount"));
        budget.setSpentAmount(rs.getDouble("spent_amount"));

        Date start = rs.getDate("start_date");
        if (start != null) {
            budget.setStartDate(start.toLocalDate());
        }
        Date end = rs.getDate("end_date");
        if (end != null) {
            budget.setEndDate(end.toLocalDate());
        }

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            budget.setCreatedAt(ts.toLocalDateTime());
        }
        return budget;
    }
}

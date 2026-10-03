package com.myfinance.dao.impl;

import com.myfinance.dao.interfaces.BudgetDAO;
import com.myfinance.model.Budget;
import com.myfinance.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BudgetDAOImpl implements BudgetDAO {

    @Override
    public void save(Budget budget) throws SQLException {
        String sql = "INSERT INTO budgets (user_id, category, limit_amount, spent_amount, start_date, end_date) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, budget.getUserId());
            pstmt.setString(2, budget.getCategory());
            pstmt.setDouble(3, budget.getLimitAmount());
            pstmt.setDouble(4, budget.getSpentAmount());
            pstmt.setDate(5, Date.valueOf(budget.getStartDate()));
            pstmt.setDate(6, Date.valueOf(budget.getEndDate()));

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        budget.setId(generatedKeys.getInt(1));
                    }
                }
            }
        }
    }

    @Override
    public Budget findById(int id) throws SQLException {
        String sql = "SELECT * FROM budgets WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToBudget(rs);
            }
        }
        return null;
    }

    @Override
    public List<Budget> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM budgets WHERE user_id = ?";
        List<Budget> budgets = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                budgets.add(mapResultSetToBudget(rs));
            }
        }
        return budgets;
    }

    @Override
    public Budget findByUserIdAndCategory(int userId, String category) throws SQLException {
        String sql = "SELECT * FROM budgets WHERE user_id = ? AND category = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, category);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToBudget(rs);
            }
        }
        return null;
    }

    @Override
    public List<Budget> findOverBudget(int userId) throws SQLException {
        String sql = "SELECT * FROM budgets WHERE user_id = ? AND spent_amount >= limit_amount";
        List<Budget> budgets = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                budgets.add(mapResultSetToBudget(rs));
            }
        }
        return budgets;
    }

    @Override
    public void updateSpentAmount(int budgetId, double amount) throws SQLException {
        String sql = "UPDATE budgets SET spent_amount = spent_amount + ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, amount);
            pstmt.setInt(2, budgetId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public List<Budget> findAll() throws SQLException {
        String sql = "SELECT * FROM budgets";
        List<Budget> budgets = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                budgets.add(mapResultSetToBudget(rs));
            }
        }
        return budgets;
    }

    @Override
    public void update(Budget budget) throws SQLException {
        String sql = "UPDATE budgets SET category = ?, limit_amount = ?, spent_amount = ?, start_date = ?, end_date = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, budget.getCategory());
            pstmt.setDouble(2, budget.getLimitAmount());
            pstmt.setDouble(3, budget.getSpentAmount());
            pstmt.setDate(4, Date.valueOf(budget.getStartDate()));
            pstmt.setDate(5, Date.valueOf(budget.getEndDate()));
            pstmt.setInt(6, budget.getId());

            pstmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM budgets WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    private Budget mapResultSetToBudget(ResultSet rs) throws SQLException {
        Budget budget = new Budget();
        budget.setId(rs.getInt("id"));
        budget.setUserId(rs.getInt("user_id"));
        budget.setCategory(rs.getString("category"));
        budget.setLimitAmount(rs.getDouble("limit_amount"));
        budget.setSpentAmount(rs.getDouble("spent_amount"));
        budget.setStartDate(rs.getDate("start_date").toLocalDate());
        budget.setEndDate(rs.getDate("end_date").toLocalDate());
        budget.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return budget;
    }
}

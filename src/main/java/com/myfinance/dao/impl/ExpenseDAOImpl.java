package com.myfinance.dao.impl;

import com.myfinance.dao.interfaces.ExpenseDAO;
import com.myfinance.model.Expense;
import com.myfinance.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDAOImpl implements ExpenseDAO {

    @Override
    public void save(Expense expense) throws SQLException {
        String sql = "INSERT INTO expenses (user_id, title, amount, category, expense_date, description) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, expense.getUserId());
            pstmt.setString(2, expense.getTitle());
            pstmt.setDouble(3, expense.getAmount());
            pstmt.setString(4, expense.getCategory());
            pstmt.setDate(5, Date.valueOf(expense.getExpenseDate()));
            pstmt.setString(6, expense.getDescription());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        expense.setId(generatedKeys.getInt(1));
                    }
                }
            }
        }
    }

    @Override
    public Expense findById(int id) throws SQLException {
        String sql = "SELECT * FROM expenses WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToExpense(rs);
            }
        }
        return null;
    }

    @Override
    public List<Expense> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM expenses WHERE user_id = ? ORDER BY expense_date DESC";
        List<Expense> expenses = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                expenses.add(mapResultSetToExpense(rs));
            }
        }
        return expenses;
    }

    @Override
    public List<Expense> findByUserIdAndMonth(int userId, int month, int year) throws SQLException {
        String sql = "SELECT * FROM expenses WHERE user_id = ? AND MONTH(expense_date) = ? AND YEAR(expense_date) = ? ORDER BY expense_date DESC";
        List<Expense> expenses = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                expenses.add(mapResultSetToExpense(rs));
            }
        }
        return expenses;
    }

    @Override
    public List<Expense> findByUserIdAndCategory(int userId, String category) throws SQLException {
        String sql = "SELECT * FROM expenses WHERE user_id = ? AND category = ? ORDER BY expense_date DESC";
        List<Expense> expenses = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, category);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                expenses.add(mapResultSetToExpense(rs));
            }
        }
        return expenses;
    }

    @Override
    public double getSumByUserIdAndMonth(int userId, int month, int year) throws SQLException {
        String sql = "SELECT SUM(amount) as total FROM expenses WHERE user_id = ? AND MONTH(expense_date) = ? AND YEAR(expense_date) = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("total");
            }
        }
        return 0.0;
    }

    @Override
    public List<Expense> findAll() throws SQLException {
        String sql = "SELECT * FROM expenses";
        List<Expense> expenses = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                expenses.add(mapResultSetToExpense(rs));
            }
        }
        return expenses;
    }

    @Override
    public void update(Expense expense) throws SQLException {
        String sql = "UPDATE expenses SET title = ?, amount = ?, category = ?, expense_date = ?, description = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, expense.getTitle());
            pstmt.setDouble(2, expense.getAmount());
            pstmt.setString(3, expense.getCategory());
            pstmt.setDate(4, Date.valueOf(expense.getExpenseDate()));
            pstmt.setString(5, expense.getDescription());
            pstmt.setInt(6, expense.getId());

            pstmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id, int userId) throws SQLException {
        String sql = "DELETE FROM expenses WHERE id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }

    private Expense mapResultSetToExpense(ResultSet rs) throws SQLException {
        Expense expense = new Expense();
        expense.setId(rs.getInt("id"));
        expense.setUserId(rs.getInt("user_id"));
        expense.setTitle(rs.getString("title"));
        expense.setAmount(rs.getDouble("amount"));
        expense.setCategory(rs.getString("category"));
        expense.setExpenseDate(rs.getDate("expense_date").toLocalDate());
        expense.setDescription(rs.getString("description"));
        expense.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return expense;
    }
}

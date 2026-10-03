package com.myfinance.dao.impl;

import com.myfinance.dao.interfaces.GoalDAO;
import com.myfinance.model.Goal;
import com.myfinance.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GoalDAOImpl implements GoalDAO {

    @Override
    public void save(Goal goal) throws SQLException {
        String sql = "INSERT INTO goals (user_id, title, description, target_amount, saved_amount, deadline, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, goal.getUserId());
            pstmt.setString(2, goal.getTitle());
            pstmt.setString(3, goal.getDescription());
            pstmt.setDouble(4, goal.getTargetAmount());
            pstmt.setDouble(5, goal.getSavedAmount());
            pstmt.setDate(6, Date.valueOf(goal.getDeadline()));
            pstmt.setString(7, goal.getStatus().name());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        goal.setId(generatedKeys.getInt(1));
                    }
                }
            }
        }
    }

    @Override
    public Goal findById(int id) throws SQLException {
        String sql = "SELECT * FROM goals WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToGoal(rs);
            }
        }
        return null;
    }

    @Override
    public List<Goal> findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM goals WHERE user_id = ?";
        List<Goal> goals = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                goals.add(mapResultSetToGoal(rs));
            }
        }
        return goals;
    }

    @Override
    public List<Goal> findByStatus(int userId, String status) throws SQLException {
        String sql = "SELECT * FROM goals WHERE user_id = ? AND status = ?";
        List<Goal> goals = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, status);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                goals.add(mapResultSetToGoal(rs));
            }
        }
        return goals;
    }

    @Override
    public void updateSavedAmount(int goalId, double amount) throws SQLException {
        String sql = "UPDATE goals SET saved_amount = saved_amount + ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, amount);
            pstmt.setInt(2, goalId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void markAsAchieved(int goalId) throws SQLException {
        String sql = "UPDATE goals SET status = 'ACHIEVED' WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, goalId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public List<Goal> findAll() throws SQLException {
        String sql = "SELECT * FROM goals";
        List<Goal> goals = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                goals.add(mapResultSetToGoal(rs));
            }
        }
        return goals;
    }

    @Override
    public void update(Goal goal) throws SQLException {
        String sql = "UPDATE goals SET title = ?, description = ?, target_amount = ?, saved_amount = ?, deadline = ?, status = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, goal.getTitle());
            pstmt.setString(2, goal.getDescription());
            pstmt.setDouble(3, goal.getTargetAmount());
            pstmt.setDouble(4, goal.getSavedAmount());
            pstmt.setDate(5, Date.valueOf(goal.getDeadline()));
            pstmt.setString(6, goal.getStatus().name());
            pstmt.setInt(7, goal.getId());

            pstmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM goals WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    private Goal mapResultSetToGoal(ResultSet rs) throws SQLException {
        Goal goal = new Goal();
        goal.setId(rs.getInt("id"));
        goal.setUserId(rs.getInt("user_id"));
        goal.setTitle(rs.getString("title"));
        goal.setDescription(rs.getString("description"));
        goal.setTargetAmount(rs.getDouble("target_amount"));
        goal.setSavedAmount(rs.getDouble("saved_amount"));
        goal.setDeadline(rs.getDate("deadline").toLocalDate());
        goal.setStatus(Goal.GoalStatus.valueOf(rs.getString("status")));
        goal.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return goal;
    }
}

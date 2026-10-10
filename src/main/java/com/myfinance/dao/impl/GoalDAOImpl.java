package com.myfinance.dao.impl;

import com.myfinance.dao.interfaces.GoalDAO;
import com.myfinance.model.Goal;
import com.myfinance.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Raw JDBC implementation of {@link GoalDAO}.
 * Manages savings progress, deadline updates, and milestone statuses.
 */
public class GoalDAOImpl implements GoalDAO {

    @Override
    public boolean save(Goal goal) throws SQLException {
        String sql = "INSERT INTO goals (user_id, title, description, target_amount, saved_amount, deadline, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, goal.getUserId());
            ps.setString(2, goal.getTitle());
            ps.setString(3, goal.getDescription());
            ps.setDouble(4, goal.getTargetAmount());
            ps.setDouble(5, goal.getSavedAmount());
            ps.setDate(6, Date.valueOf(goal.getDeadline()));
            ps.setString(7, goal.getStatus() != null ? goal.getStatus().name() : Goal.GoalStatus.IN_PROGRESS.name());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        goal.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public Goal findById(int id) throws SQLException {
        String sql = "SELECT id, user_id, title, description, target_amount, saved_amount, deadline, status, created_at FROM goals WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToGoal(rs);
                }
            }
        }
        return null;
    }

    @Override
    public Goal findByIdAndUserId(int id, int userId) throws SQLException {
        String sql = "SELECT id, user_id, title, description, target_amount, saved_amount, deadline, status, created_at FROM goals WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToGoal(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Goal> findByUserId(int userId) throws SQLException {
        String sql = "SELECT id, user_id, title, description, target_amount, saved_amount, deadline, status, created_at FROM goals WHERE user_id = ? ORDER BY status ASC, deadline ASC";
        List<Goal> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToGoal(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Goal> findByStatus(int userId, Goal.GoalStatus status) throws SQLException {
        String sql = "SELECT id, user_id, title, description, target_amount, saved_amount, deadline, status, created_at " +
                "FROM goals WHERE user_id = ? AND status = ? ORDER BY deadline ASC";
        List<Goal> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToGoal(rs));
                }
            }
        }
        return list;
    }

    @Override
    public List<Goal> findAll() throws SQLException {
        String sql = "SELECT id, user_id, title, description, target_amount, saved_amount, deadline, status, created_at FROM goals ORDER BY id DESC";
        List<Goal> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToGoal(rs));
            }
        }
        return list;
    }

    @Override
    public boolean update(Goal goal) throws SQLException {
        String sql = "UPDATE goals SET title = ?, description = ?, target_amount = ?, saved_amount = ?, deadline = ?, status = ? " +
                "WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, goal.getTitle());
            ps.setString(2, goal.getDescription());
            ps.setDouble(3, goal.getTargetAmount());
            ps.setDouble(4, goal.getSavedAmount());
            ps.setDate(5, Date.valueOf(goal.getDeadline()));
            ps.setString(6, goal.getStatus() != null ? goal.getStatus().name() : Goal.GoalStatus.IN_PROGRESS.name());
            ps.setInt(7, goal.getId());
            ps.setInt(8, goal.getUserId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateSavedAmount(int goalId, int userId, double additionalAmount) throws SQLException {
        String selectSql = "SELECT target_amount, saved_amount FROM goals WHERE id = ? AND user_id = ?";
        String updateSql = "UPDATE goals SET saved_amount = ?, status = ? WHERE id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            double currentSaved = 0.0;
            double targetAmount = 0.0;

            try (PreparedStatement psSelect = conn.prepareStatement(selectSql)) {
                psSelect.setInt(1, goalId);
                psSelect.setInt(2, userId);
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (rs.next()) {
                        targetAmount = rs.getDouble("target_amount");
                        currentSaved = rs.getDouble("saved_amount");
                    } else {
                        return false;
                    }
                }
            }

            double newSaved = Math.max(0.0, currentSaved + additionalAmount);
            String newStatus = newSaved >= targetAmount ? Goal.GoalStatus.ACHIEVED.name() : Goal.GoalStatus.IN_PROGRESS.name();

            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setDouble(1, newSaved);
                psUpdate.setString(2, newStatus);
                psUpdate.setInt(3, goalId);
                psUpdate.setInt(4, userId);
                return psUpdate.executeUpdate() > 0;
            }
        }
    }

    @Override
    public boolean markAsAchieved(int goalId, int userId) throws SQLException {
        String sql = "UPDATE goals SET status = 'ACHIEVED' WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, goalId);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id, int userId) throws SQLException {
        String sql = "DELETE FROM goals WHERE id = ? AND user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        }
    }

    private Goal mapRowToGoal(ResultSet rs) throws SQLException {
        Goal goal = new Goal();
        goal.setId(rs.getInt("id"));
        goal.setUserId(rs.getInt("user_id"));
        goal.setTitle(rs.getString("title"));
        goal.setDescription(rs.getString("description"));
        goal.setTargetAmount(rs.getDouble("target_amount"));
        goal.setSavedAmount(rs.getDouble("saved_amount"));

        Date d = rs.getDate("deadline");
        if (d != null) {
            goal.setDeadline(d.toLocalDate());
        }

        String st = rs.getString("status");
        if (st != null) {
            try {
                goal.setStatus(Goal.GoalStatus.valueOf(st));
            } catch (IllegalArgumentException e) {
                goal.setStatus(Goal.GoalStatus.IN_PROGRESS);
            }
        }

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            goal.setCreatedAt(ts.toLocalDateTime());
        }
        return goal;
    }
}

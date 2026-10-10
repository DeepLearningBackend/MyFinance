package com.myfinance.dao.impl;

import com.myfinance.dao.interfaces.UserDAO;
import com.myfinance.model.User;
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
 * Raw JDBC implementation of {@link UserDAO}.
 * Follows strict PreparedStatement and try-with-resources conventions.
 */
public class UserDAOImpl implements UserDAO {

    @Override
    public boolean save(User user) throws SQLException {
        if (user.getApiToken() == null || user.getApiToken().trim().isEmpty()) {
            user.setApiToken(User.generateApiToken());
        }
        String sql = "INSERT INTO users (name, email, password, role, status, monthly_income, cash_in_hand, api_token) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getRole() != null ? user.getRole().name() : User.Role.USER.name());
            ps.setString(5, user.getStatus() != null ? user.getStatus().name() : User.Status.ACTIVE.name());
            ps.setDouble(6, user.getMonthlyIncome());
            ps.setDouble(7, user.getCashInHand());
            ps.setString(8, user.getApiToken());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public User findById(int id) throws SQLException {
        String sql = "SELECT id, name, email, password, role, status, monthly_income, cash_in_hand, api_token, created_at FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        }
        return null;
    }

    @Override
    public User findByEmail(String email) throws SQLException {
        if (email == null) {
            return null;
        }
        String sql = "SELECT id, name, email, password, role, status, monthly_income, cash_in_hand, api_token, created_at FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        }
        return null;
    }

    @Override
    public User findByApiToken(String token) throws SQLException {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }
        String sql = "SELECT id, name, email, password, role, status, monthly_income, cash_in_hand, api_token, created_at FROM users WHERE api_token = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, token.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        }
        return null;
    }

    @Override
    public boolean updateApiToken(int userId, String apiToken) throws SQLException {
        String sql = "UPDATE users SET api_token = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, apiToken);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<User> findAll() throws SQLException {
        String sql = "SELECT id, name, email, password, role, status, monthly_income, cash_in_hand, api_token, created_at FROM users ORDER BY id ASC";
        List<User> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapRowToUser(rs));
            }
        }
        return list;
    }

    @Override
    public List<User> searchUsers(String query, String role, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT id, name, email, password, role, status, monthly_income, cash_in_hand, api_token, created_at FROM users WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (LOWER(name) LIKE ? OR LOWER(email) LIKE ?)");
            String wildcard = "%" + query.trim().toLowerCase() + "%";
            params.add(wildcard);
            params.add(wildcard);
        }
        if (role != null && !role.trim().isEmpty() && !"ALL".equalsIgnoreCase(role)) {
            sql.append(" AND role = ?");
            params.add(role.trim().toUpperCase());
        }
        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND status = ?");
            params.add(status.trim().toUpperCase());
        }
        sql.append(" ORDER BY id DESC");

        List<User> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToUser(rs));
                }
            }
        }
        return list;
    }

    @Override
    public boolean update(User user) throws SQLException {
        String sql = "UPDATE users SET name = ?, email = ?, role = ?, status = ?, monthly_income = ?, cash_in_hand = ?, api_token = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getRole() != null ? user.getRole().name() : User.Role.USER.name());
            ps.setString(4, user.getStatus() != null ? user.getStatus().name() : User.Status.ACTIVE.name());
            ps.setDouble(5, user.getMonthlyIncome());
            ps.setDouble(6, user.getCashInHand());
            ps.setString(7, user.getApiToken());
            ps.setInt(8, user.getId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateStatus(int userId, User.Status status) throws SQLException {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status.name());
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updatePassword(int userId, String hashedPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, hashedPassword);
            ps.setInt(2, userId);

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public int countUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public int countActiveUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE status = 'ACTIVE'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));

        String roleStr = rs.getString("role");
        if (roleStr != null) {
            try {
                user.setRole(User.Role.valueOf(roleStr));
            } catch (IllegalArgumentException e) {
                user.setRole(User.Role.USER);
            }
        }

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try {
                user.setStatus(User.Status.valueOf(statusStr));
            } catch (IllegalArgumentException e) {
                user.setStatus(User.Status.ACTIVE);
            }
        }

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            user.setCreatedAt(ts.toLocalDateTime());
        }

        try {
            user.setMonthlyIncome(rs.getDouble("monthly_income"));
        } catch (SQLException ignored) {
        }

        try {
            user.setCashInHand(rs.getDouble("cash_in_hand"));
        } catch (SQLException ignored) {
        }

        try {
            String token = rs.getString("api_token");
            if (token == null || token.trim().isEmpty()) {
                token = User.generateApiToken();
            }
            user.setApiToken(token);
        } catch (SQLException ignored) {
            user.setApiToken(User.generateApiToken());
        }

        return user;
    }

    /**
     * Checks all users in the database and generates a unique secure API token
     * for any existing accounts that currently lack one.
     */
    public void ensureApiTokensForAllUsers() {
        String selectSql = "SELECT id, api_token FROM users WHERE api_token IS NULL OR api_token = ''";
        String updateSql = "UPDATE users SET api_token = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psSel = conn.prepareStatement(selectSql);
             ResultSet rs = psSel.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String token = User.generateApiToken();
                try (PreparedStatement psUpd = conn.prepareStatement(updateSql)) {
                    psUpd.setString(1, token);
                    psUpd.setInt(2, id);
                    psUpd.executeUpdate();
                }
            }
        } catch (Exception e) {
            System.err.println("[UserDAOImpl] Notice ensuring API tokens: " + e.getMessage());
        }
    }
}

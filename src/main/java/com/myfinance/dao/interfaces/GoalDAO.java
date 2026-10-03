package com.myfinance.dao.interfaces;

import com.myfinance.model.Goal;
import java.sql.SQLException;
import java.util.List;

public interface GoalDAO {
    void save(Goal goal) throws SQLException;
    Goal findById(int id) throws SQLException;
    List<Goal> findByUserId(int userId) throws SQLException;
    List<Goal> findByStatus(int userId, String status) throws SQLException;
    void updateSavedAmount(int goalId, double amount) throws SQLException;
    void markAsAchieved(int goalId) throws SQLException;
    List<Goal> findAll() throws SQLException;
    void update(Goal goal) throws SQLException;
    void delete(int id) throws SQLException;
}

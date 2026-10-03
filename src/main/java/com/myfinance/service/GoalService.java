package com.myfinance.service;

import com.myfinance.dao.impl.GoalDAOImpl;
import com.myfinance.exception.ValidationException;
import com.myfinance.model.Goal;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoalService {
    private GoalDAOImpl goalDAO;

    public GoalService() {
        this.goalDAO = new GoalDAOImpl();
    }

    public void createGoal(Goal goal) throws SQLException, ValidationException {
        if (!goal.validate()) {
            throw new ValidationException("Invalid goal data");
        }

        goalDAO.save(goal);
    }

    public void addSavings(int goalId, double amount) throws SQLException, ValidationException {
        if (amount <= 0) {
            throw new ValidationException("Amount must be positive", "amount");
        }

        goalDAO.updateSavedAmount(goalId, amount);

        // Check if goal is achieved
        Goal goal = goalDAO.findById(goalId);
        if (goal != null && goal.getSavedAmount() >= goal.getTargetAmount()) {
            goalDAO.markAsAchieved(goalId);
        }
    }

    public Map<String, Object> getProgressDetails(Goal goal) {
        Map<String, Object> details = new HashMap<>();
        details.put("percent", goal.getProgressPercent());
        details.put("remaining", goal.getRemainingAmount());
        details.put("daysLeft", goal.getDaysLeft());
        return details;
    }

    public List<Goal> getAllGoals(int userId) throws SQLException {
        return goalDAO.findByUserId(userId);
    }

    public List<Goal> getGoalsByStatus(int userId, String status) throws SQLException {
        return goalDAO.findByStatus(userId, status);
    }

    public void deleteGoal(int id) throws SQLException {
        goalDAO.delete(id);
    }

    public void updateGoal(Goal goal) throws SQLException, ValidationException {
        if (!goal.validate()) {
            throw new ValidationException("Invalid goal data");
        }
        goalDAO.update(goal);
    }
}

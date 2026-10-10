package com.myfinance.service;

import com.myfinance.dao.impl.GoalDAOImpl;
import com.myfinance.dao.interfaces.GoalDAO;
import com.myfinance.exception.DatabaseException;
import com.myfinance.model.FinanceOperation;
import com.myfinance.model.Goal;
import com.myfinance.util.ApiResponse;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service managing financial goals and the Goal Motivation Engine (Innovation 2).
 * Computes milestone badges (25%, 50%, 75%, 100%) and deadline countdowns.
 * Implements {@link FinanceOperation} — execute() creates a goal,
 * summarize() reports active goal count and total saved amount.
 */
public class GoalService implements FinanceOperation<Goal, ApiResponse<Goal>> {

    private final GoalDAO goalDAO;

    public GoalService() {
        this.goalDAO = new GoalDAOImpl();
    }

    public GoalService(GoalDAO goalDAO) {
        this.goalDAO = goalDAO;
    }

    /**
     * Creates a new financial savings goal after validating input fields.
     *
     * @param goal goal entity to save
     * @return ApiResponse containing saved goal or validation error
     * @throws DatabaseException if persistence fails
     */
    public ApiResponse<Goal> createGoal(Goal goal) {
        if (goal == null) {
            return ApiResponse.error("Goal cannot be null.");
        }

        if (!goal.validate()) {
            return ApiResponse.error("Please provide a valid goal title, positive target amount, and future deadline.");
        }

        try {
            boolean saved = goalDAO.save(goal);
            if (saved) {
                return ApiResponse.ok("Goal created successfully! Stay focused on your target.", goal);
            }
            return ApiResponse.error("Failed to save goal.");
        } catch (SQLException e) {
            throw new DatabaseException("Error creating goal: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing financial goal's details (title, target amount, deadline).
     *
     * @param goal updated goal entity
     * @return ApiResponse containing updated goal or error
     * @throws DatabaseException if persistence fails
     */
    public ApiResponse<Goal> updateGoal(Goal goal) {
        if (goal == null || !goal.validate() || goal.getId() <= 0) {
            return ApiResponse.error("Invalid goal details for update.");
        }

        try {
            boolean updated = goalDAO.update(goal);
            if (updated) {
                Goal refreshed = goalDAO.findByIdAndUserId(goal.getId(), goal.getUserId());
                return ApiResponse.ok("Goal updated successfully", refreshed);
            }
            return ApiResponse.error("Failed to update goal.");
        } catch (SQLException e) {
            throw new DatabaseException("Error updating goal: " + e.getMessage(), e);
        }
    }

    /**
     * Adds incremental savings towards a goal and automatically marks it ACHIEVED when target is reached.
     *
     * @param goalId goal ID
     * @param userId user ID
     * @param amount amount deposited
     * @return updated Goal
     */
    public ApiResponse<Goal> addSavings(int goalId, int userId, double amount) {
        if (amount <= 0.0) {
            return ApiResponse.error("Savings amount must be positive.");
        }

        try {
            Goal goal = goalDAO.findByIdAndUserId(goalId, userId);
            if (goal == null) {
                return ApiResponse.error("Goal not found or unauthorized.");
            }

            boolean updated = goalDAO.updateSavedAmount(goalId, userId, amount);
            if (updated) {
                Goal refreshed = goalDAO.findByIdAndUserId(goalId, userId);
                String msg = refreshed.getStatus() == Goal.GoalStatus.ACHIEVED
                        ? "🎉 CONGRATULATIONS! You have fully achieved this financial goal!"
                        : String.format("Deposited ₹%,.2f towards '%s'. Current progress: %.1f%%",
                        amount, refreshed.getTitle(), refreshed.getProgressPercent());
                return ApiResponse.ok(msg, refreshed);
            }
            return ApiResponse.error("Failed to record savings.");
        } catch (SQLException e) {
            throw new DatabaseException("Error updating savings: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a goal by ID for an authorized user.
     *
     * @param id goal ID
     * @param userId user ID
     * @return ApiResponse with deletion status
     * @throws DatabaseException if persistence fails
     */
    public ApiResponse<Boolean> deleteGoal(int id, int userId) {
        try {
            boolean deleted = goalDAO.delete(id, userId);
            if (deleted) {
                return ApiResponse.ok("Goal removed successfully", true);
            }
            return ApiResponse.error("Goal not found or unauthorized.");
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting goal: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves all goals for a user, optionally filtered by status.
     *
     * @param userId user ID
     * @param status optional status filter (IN_PROGRESS, ACHIEVED, CANCELLED)
     * @return list of goals
     * @throws DatabaseException if query fails
     */
    public List<Goal> getUserGoals(int userId, Goal.GoalStatus status) {
        try {
            if (status != null) {
                return goalDAO.findByStatus(userId, status);
            }
            return goalDAO.findByUserId(userId);
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching goals: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves a single goal by ID for an authorized user.
     *
     * @param id goal ID
     * @param userId user ID
     * @return Goal entity, or null
     * @throws DatabaseException if query fails
     */
    public Goal getGoalById(int id, int userId) {
        try {
            return goalDAO.findByIdAndUserId(id, userId);
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching goal: " + e.getMessage(), e);
        }
    }

    /**
     * Goal Motivation Engine helper: computes detailed progress metrics and milestone badge.
     *
     * @param goal target goal
     * @return Map with percent, remaining, daysLeft, milestoneBadge, isAchieved
     */
    public Map<String, Object> getProgressDetails(Goal goal) {
        Map<String, Object> details = new LinkedHashMap<>();
        if (goal == null) {
            return details;
        }

        double percent = goal.getProgressPercent();
        double remaining = goal.getRemainingAmount();
        long daysLeft = goal.getDaysLeft();
        boolean isAchieved = goal.getStatus() == Goal.GoalStatus.ACHIEVED || percent >= 100.0;
        boolean isOverdue = !isAchieved && daysLeft < 0;

        String milestoneBadge;
        String milestoneLevel;

        if (percent >= 100.0) {
            milestoneBadge = "🏆 Goal Master (100%)";
            milestoneLevel = "MASTER";
        } else if (percent >= 75.0) {
            milestoneBadge = "🥇 Home Stretch (75%)";
            milestoneLevel = "GOLD";
        } else if (percent >= 50.0) {
            milestoneBadge = "🥈 Halfway Hero (50%)";
            milestoneLevel = "SILVER";
        } else if (percent >= 25.0) {
            milestoneBadge = "🥉 Momentum Builder (25%)";
            milestoneLevel = "BRONZE";
        } else {
            milestoneBadge = "🌱 First Steps (0%)";
            milestoneLevel = "SEED";
        }

        details.put("percent", percent);
        details.put("percentFormatted", String.format("%.1f", percent));
        details.put("remaining", remaining);
        details.put("daysLeft", daysLeft);
        details.put("milestoneBadge", milestoneBadge);
        details.put("milestoneLevel", milestoneLevel);
        details.put("isAchieved", isAchieved);
        details.put("isOverdue", isOverdue);

        return details;
    }

    // ── FinanceOperation ─────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     * Delegates to {@link #createGoal(Goal)}.
     */
    @Override
    public ApiResponse<Goal> execute(Goal goal) {
        return createGoal(goal);
    }

    /**
     * {@inheritDoc}
     * Returns the number of active (IN_PROGRESS) goals and the total saved so far.
     * Example: "2 active goals · ₹8,500.00 saved so far"
     */
    @Override
    public String summarize(int userId) {
        List<Goal> active = getUserGoals(userId, Goal.GoalStatus.IN_PROGRESS);
        double totalSaved = active.stream()
                .mapToDouble(Goal::getSavedAmount)
                .sum();
        return String.format("%d active goal%s · ₹%,.2f saved so far",
                active.size(), active.size() == 1 ? "" : "s", totalSaved);
    }
}

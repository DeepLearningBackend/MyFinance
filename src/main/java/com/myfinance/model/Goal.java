package com.myfinance.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Goal entity representing target financial savings with deadlines and milestone tracking.
 */
public class Goal extends BaseEntity {

    public enum GoalStatus {
        IN_PROGRESS,
        ACHIEVED,
        FAILED
    }

    private int userId;
    private String title;
    private String description;
    private double targetAmount;
    private double savedAmount;
    private LocalDate deadline;
    private GoalStatus status;

    public Goal() {
        super();
        this.savedAmount = 0.0;
        this.status = GoalStatus.IN_PROGRESS;
        this.deadline = LocalDate.now().plusMonths(6);
    }

    public Goal(int userId, String title, String description, double targetAmount, double savedAmount, LocalDate deadline, GoalStatus status) {
        super();
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.deadline = deadline;
        this.status = status != null ? status : GoalStatus.IN_PROGRESS;
    }

    public Goal(int id, int userId, String title, String description, double targetAmount, double savedAmount, LocalDate deadline, GoalStatus status, LocalDateTime createdAt) {
        super(id, createdAt);
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.deadline = deadline;
        this.status = status != null ? status : GoalStatus.IN_PROGRESS;
    }

    @Override
    public boolean validate() {
        if (userId <= 0) {
            return false;
        }
        if (title == null || title.trim().isEmpty() || title.trim().length() > 150) {
            return false;
        }
        if (targetAmount <= 0.0) {
            return false;
        }
        if (savedAmount < 0.0) {
            return false;
        }
        if (deadline == null) {
            return false;
        }
        return true;
    }

    /**
     * Calculates the savings completion percentage.
     *
     * @return progress percentage (0.0 to 100.0)
     */
    public double getProgressPercent() {
        if (targetAmount <= 0.0) {
            return 0.0;
        }
        double percent = (savedAmount / targetAmount) * 100.0;
        return Math.min(100.0, Math.max(0.0, percent));
    }

    /**
     * Calculates the remaining amount needed to achieve the goal.
     *
     * @return remaining amount in currency units
     */
    public double getRemainingAmount() {
        return Math.max(0.0, targetAmount - savedAmount);
    }

    /**
     * Calculates the number of days remaining until the deadline.
     *
     * @return days remaining (can be negative if deadline has passed)
     */
    public long getDaysLeft() {
        if (deadline == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), deadline);
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public double getSavedAmount() {
        return savedAmount;
    }

    public void setSavedAmount(double savedAmount) {
        this.savedAmount = savedAmount;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public GoalStatus getStatus() {
        return status;
    }

    public void setStatus(GoalStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Goal{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", targetAmount=" + targetAmount +
                ", savedAmount=" + savedAmount +
                ", deadline=" + deadline +
                ", status=" + status +
                ", progress=" + String.format("%.1f", getProgressPercent()) + "%" +
                ", daysLeft=" + getDaysLeft() +
                ", createdAt=" + createdAt +
                '}';
    }
}

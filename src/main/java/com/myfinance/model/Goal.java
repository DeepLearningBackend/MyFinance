package com.myfinance.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Goal extends BaseEntity {
    private int userId;
    private String title;
    private String description;
    private double targetAmount;
    private double savedAmount;
    private LocalDate deadline;
    private GoalStatus status;

    public enum GoalStatus {
        IN_PROGRESS, ACHIEVED, FAILED
    }

    public Goal() {
        this.savedAmount = 0.0;
        this.status = GoalStatus.IN_PROGRESS;
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

    public double getProgressPercent() {
        if (targetAmount == 0) return 0;
        return (savedAmount / targetAmount) * 100;
    }

    public double getRemainingAmount() {
        return targetAmount - savedAmount;
    }

    public long getDaysLeft() {
        if (deadline == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), deadline);
    }

    @Override
    public boolean validate() {
        if (targetAmount <= 0) {
            return false;
        }
        if (deadline == null) {
            return false;
        }
        if (deadline.isBefore(LocalDate.now())) {
            return false;
        }
        if (title == null || title.trim().isEmpty()) {
            return false;
        }
        return true;
    }
}

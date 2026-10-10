package com.myfinance.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Budget entity representing monthly spending limits set by users per category.
 */
public class Budget extends BaseEntity {

    private int userId;
    private String category;
    private double limitAmount;
    private double spentAmount;
    private LocalDate startDate;
    private LocalDate endDate;

    public Budget() {
        super();
        this.spentAmount = 0.0;
    }

    public Budget(int userId, String category, double limitAmount, double spentAmount, LocalDate startDate, LocalDate endDate) {
        super();
        this.userId = userId;
        this.category = category;
        this.limitAmount = limitAmount;
        this.spentAmount = spentAmount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Budget(int id, int userId, String category, double limitAmount, double spentAmount, LocalDate startDate, LocalDate endDate, LocalDateTime createdAt) {
        super(id, createdAt);
        this.userId = userId;
        this.category = category;
        this.limitAmount = limitAmount;
        this.spentAmount = spentAmount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public boolean validate() {
        if (userId <= 0) {
            return false;
        }
        if (category == null || category.trim().isEmpty() || category.trim().length() > 50) {
            return false;
        }
        if (limitAmount <= 0.0) {
            return false;
        }
        if (startDate == null || endDate == null) {
            return false;
        }
        if (startDate.isAfter(endDate)) {
            return false;
        }
        return true;
    }

    /**
     * Calculates the percentage of budget spent.
     *
     * @return usage percentage (0.0 to 100.0+)
     */
    public double getUsagePercent() {
        if (limitAmount <= 0.0) {
            return 0.0;
        }
        return (spentAmount / limitAmount) * 100.0;
    }

    /**
     * Calculates the remaining funds under this budget limit.
     *
     * @return remaining amount (can be negative if over budget)
     */
    public double getRemainingAmount() {
        return limitAmount - spentAmount;
    }

    /**
     * Checks whether the budget limit has been exceeded.
     *
     * @return true if spent amount is greater than or equal to limit
     */
    public boolean isOverBudget() {
        return spentAmount >= limitAmount;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(double limitAmount) {
        this.limitAmount = limitAmount;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "Budget{" +
                "id=" + id +
                ", userId=" + userId +
                ", category='" + category + '\'' +
                ", limitAmount=" + limitAmount +
                ", spentAmount=" + spentAmount +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", usagePercent=" + String.format("%.1f", getUsagePercent()) + "%" +
                ", createdAt=" + createdAt +
                '}';
    }
}

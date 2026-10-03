package com.myfinance.model;

import java.time.LocalDate;

public class Budget extends BaseEntity {
    private int userId;
    private String category;
    private double limitAmount;
    private double spentAmount;
    private LocalDate startDate;
    private LocalDate endDate;

    public Budget() {
        this.spentAmount = 0.0;
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

    public double getUsagePercent() {
        if (limitAmount == 0) return 0;
        return (spentAmount / limitAmount) * 100;
    }

    public double getRemainingAmount() {
        return limitAmount - spentAmount;
    }

    @Override
    public boolean validate() {
        if (limitAmount <= 0) {
            return false;
        }
        if (startDate == null || endDate == null) {
            return false;
        }
        if (startDate.isAfter(endDate)) {
            return false;
        }
        if (category == null || category.trim().isEmpty()) {
            return false;
        }
        return true;
    }
}

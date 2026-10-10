package com.myfinance.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * CashEntry entity representing physical cash spending (Innovation: Cash Tracker).
 * Tracks cash expenditures reducing the user's physical cash-in-hand baseline.
 */
public class CashEntry extends BaseEntity {

    private int userId;
    private double amount;
    private String category;
    private LocalDate entryDate;
    private String description;

    public CashEntry() {
        super();
        this.entryDate = LocalDate.now();
    }

    public CashEntry(int userId, double amount, String category, LocalDate entryDate, String description) {
        super();
        this.userId = userId;
        this.amount = amount;
        this.category = category;
        this.entryDate = entryDate != null ? entryDate : LocalDate.now();
        this.description = description;
    }

    public CashEntry(int id, int userId, double amount, String category, LocalDate entryDate, String description, LocalDateTime createdAt) {
        super(id, createdAt);
        this.userId = userId;
        this.amount = amount;
        this.category = category;
        this.entryDate = entryDate != null ? entryDate : LocalDate.now();
        this.description = description;
    }

    @Override
    public boolean validate() {
        if (userId <= 0) {
            return false;
        }
        if (amount <= 0.0) {
            return false;
        }
        if (category == null || category.trim().isEmpty() || category.trim().length() > 50) {
            return false;
        }
        if (entryDate == null) {
            return false;
        }
        return true;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "CashEntry{" +
                "id=" + id +
                ", userId=" + userId +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", entryDate=" + entryDate +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

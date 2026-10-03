package com.myfinance.model;

import java.time.LocalDate;

public class Expense extends BaseEntity {
    private int userId;
    private String title;
    private double amount;
    private String category;
    private LocalDate expenseDate;
    private String description;

    public Expense() {
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

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean validate() {
        if (amount <= 0) {
            return false;
        }
        if (title == null || title.trim().isEmpty()) {
            return false;
        }
        if (expenseDate == null) {
            return false;
        }
        if (category == null || category.trim().isEmpty()) {
            return false;
        }
        return true;
    }
}

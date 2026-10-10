package com.myfinance.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Expense entity representing a financial expenditure recorded by a user.
 * Supports manual entry as well as automated mobile SMS sync with UPI reference tracking.
 */
public class Expense extends BaseEntity {

    private int userId;
    private String title;
    private double amount;
    private String category;
    private LocalDate expenseDate;
    private String source; // 'MANUAL' or 'SMS_SYNC'
    private String upiRef;
    private String description;

    public Expense() {
        super();
        this.expenseDate = LocalDate.now();
        this.source = "MANUAL";
    }

    public Expense(int userId, String title, double amount, String category, LocalDate expenseDate, String description) {
        super();
        this.userId = userId;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.expenseDate = expenseDate != null ? expenseDate : LocalDate.now();
        this.source = "MANUAL";
        this.upiRef = null;
        this.description = description;
    }

    public Expense(int userId, String title, double amount, String category, LocalDate expenseDate,
                   String source, String upiRef, String description) {
        super();
        this.userId = userId;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.expenseDate = expenseDate != null ? expenseDate : LocalDate.now();
        this.source = source != null ? source : "MANUAL";
        this.upiRef = upiRef;
        this.description = description;
    }

    public Expense(int id, int userId, String title, double amount, String category, LocalDate expenseDate,
                   String description, LocalDateTime createdAt) {
        super(id, createdAt);
        this.userId = userId;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.expenseDate = expenseDate != null ? expenseDate : LocalDate.now();
        this.source = "MANUAL";
        this.upiRef = null;
        this.description = description;
    }

    public Expense(int id, int userId, String title, double amount, String category, LocalDate expenseDate,
                   String source, String upiRef, String description, LocalDateTime createdAt) {
        super(id, createdAt);
        this.userId = userId;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.expenseDate = expenseDate != null ? expenseDate : LocalDate.now();
        this.source = source != null ? source : "MANUAL";
        this.upiRef = upiRef;
        this.description = description;
    }

    @Override
    public boolean validate() {
        if (userId <= 0) {
            return false;
        }
        if (title == null || title.trim().isEmpty() || title.trim().length() > 150) {
            return false;
        }
        if (amount <= 0.0) {
            return false;
        }
        if (category == null || category.trim().isEmpty()) {
            return false;
        }
        if (expenseDate == null) {
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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUpiRef() {
        return upiRef;
    }

    public void setUpiRef(String upiRef) {
        this.upiRef = upiRef;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Expense{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", expenseDate=" + expenseDate +
                ", source='" + source + '\'' +
                ", upiRef='" + upiRef + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}

package com.myfinance.service;

import com.myfinance.dao.impl.ExpenseDAOImpl;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class InsightService {
    private ExpenseDAOImpl expenseDAO;

    public InsightService() {
        this.expenseDAO = new ExpenseDAOImpl();
    }

    public String getWeeklyInsight(int userId) throws SQLException {
        LocalDate now = LocalDate.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        LocalDate lastMonth = now.minusMonths(1);
        int lastMonthValue = lastMonth.getMonthValue();
        int lastMonthYear = lastMonth.getYear();

        double currentTotal = expenseDAO.getSumByUserIdAndMonth(userId, currentMonth, currentYear);
        double lastMonthTotal = expenseDAO.getSumByUserIdAndMonth(userId, lastMonthValue, lastMonthYear);

        double difference = currentTotal - lastMonthTotal;

        if (difference > 0) {
            return "You spent ₹" + String.format("%.0f", difference) + " more this month compared to last month.";
        } else if (difference < 0) {
            return "Great job! You spent ₹" + String.format("%.0f", Math.abs(difference)) + " less this month compared to last month.";
        } else {
            return "Your spending is consistent with last month.";
        }
    }

    public Map<String, Double> getCategoryComparison(int userId, int month, int year) throws SQLException {
        Map<String, Double> categoryTotals = new HashMap<>();
        // This can be expanded to compare with previous month
        return categoryTotals;
    }
}

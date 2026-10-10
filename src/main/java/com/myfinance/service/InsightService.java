package com.myfinance.service;

import com.myfinance.dao.impl.ExpenseDAOImpl;
import com.myfinance.dao.interfaces.ExpenseDAO;
import com.myfinance.exception.DatabaseException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;

/**
 * Plain Insight Cards Engine (Innovation 4).
 * Generates personalized, plain-language financial insights and weekly tips.
 */
public class InsightService {

    private final ExpenseDAO expenseDAO;

    /**
     * Default constructor initializing ExpenseDAO with concrete implementation.
     */
    public InsightService() {
        this.expenseDAO = new ExpenseDAOImpl();
    }

    /**
     * Dependency injection constructor for testing.
     *
     * @param expenseDAO expense data access object
     */
    public InsightService(ExpenseDAO expenseDAO) {
        this.expenseDAO = expenseDAO;
    }

    /**
     * Generates a plain-language financial insight sentence comparing monthly spending trends.
     *
     * @param userId user ID
     * @return contextual insight string
     */
    public String getWeeklyInsight(int userId) {
        try {
            LocalDate now = LocalDate.now();
            int curMonth = now.getMonthValue();
            int curYear = now.getYear();

            LocalDate lastMonthDate = now.minusMonths(1);
            int prevMonth = lastMonthDate.getMonthValue();
            int prevYear = lastMonthDate.getYear();

            double thisMonthTotal = expenseDAO.getSumByUserIdAndMonth(userId, curMonth, curYear);
            double lastMonthTotal = expenseDAO.getSumByUserIdAndMonth(userId, prevMonth, prevYear);

            Map<String, Double> currentCategories = expenseDAO.getCategoryTotalsByUserIdAndMonth(userId, curMonth, curYear);
            Map<String, Double> prevCategories = expenseDAO.getCategoryTotalsByUserIdAndMonth(userId, prevMonth, prevYear);

            // 1. If no expenses recorded yet
            if (thisMonthTotal <= 0.0 && lastMonthTotal <= 0.0) {
                return "💡 Welcome to MyFinance! Log your first expense or set a category budget to receive personalized financial insights.";
            }

            // 2. Compare category surges
            String highestSurgeCategory = null;
            double maxIncrease = 0.0;

            for (Map.Entry<String, Double> entry : currentCategories.entrySet()) {
                String cat = entry.getKey();
                double currentAmt = entry.getValue();
                double prevAmt = prevCategories.getOrDefault(cat, 0.0);

                if (prevAmt > 0 && currentAmt > prevAmt) {
                    double diff = currentAmt - prevAmt;
                    if (diff > maxIncrease) {
                        maxIncrease = diff;
                        highestSurgeCategory = cat;
                    }
                }
            }

            if (highestSurgeCategory != null && maxIncrease > 500) {
                return String.format("💡 You spent ₹%,.0f more on %s this month compared to last month. Setting a budget can help keep it in check.",
                        maxIncrease, highestSurgeCategory);
            }

            // 3. Compare overall month-on-month trend
            if (lastMonthTotal > 0 && thisMonthTotal < lastMonthTotal) {
                double savings = lastMonthTotal - thisMonthTotal;
                double pct = (savings / lastMonthTotal) * 100.0;
                return String.format("🎉 Great job! Your total spending is down by %.0f%% (₹%,.0f less) compared to last month.",
                        pct, savings);
            }

            // 4. Highlight top category percentage
            if (!currentCategories.isEmpty()) {
                Map.Entry<String, Double> top = currentCategories.entrySet().iterator().next();
                double pctOfTotal = (top.getValue() / Math.max(1.0, thisMonthTotal)) * 100.0;
                if (pctOfTotal >= 35.0) {
                    return String.format("📊 %s accounts for %.0f%% of your total monthly spending (₹%,.0f).",
                            top.getKey(), pctOfTotal, top.getValue());
                }
            }

            return "💡 Tip: Categorizing your daily expenses helps uncover hidden subscription fees and small recurring leakages.";

        } catch (SQLException e) {
            throw new DatabaseException("Error generating weekly insight: " + e.getMessage(), e);
        }
    }
}

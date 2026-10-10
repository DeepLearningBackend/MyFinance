package com.myfinance.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

/**
 * Pure-math helper: computes the daily allowance remaining from a monthly budget.
 *
 * <p>Formula (Innovation 4 – Allowance Engine):
 * <pre>
 *   pool           = sum of this month's budget limits
 *   spent          = this month's spending (digital + cash) ONLY in categories that have a budget
 *   remaining      = pool − spent
 *   daysLeft       = daysInMonth − today.dayOfMonth + 1   (includes today)
 *   dailyAllowance = (pool − spent) ÷ daysLeft            (never clamped: negative when over budget)
 * </pre>
 */
public class AllowanceCalculator {

    private final double pool;
    private final double budgetedDigitalSpend;
    private final double budgetedCashSpend;
    private final double unbudgetedSpend;
    private final int    month;
    private final int    year;
    private final boolean hasBudgets;

    /**
     * Full constructor with budgeted category breakdown and unbudgeted spending.
     */
    public AllowanceCalculator(double pool,
                               double budgetedDigitalSpend,
                               double budgetedCashSpend,
                               double unbudgetedSpend,
                               int month,
                               int year,
                               boolean hasBudgets) {
        this.pool                 = pool;
        this.budgetedDigitalSpend = budgetedDigitalSpend;
        this.budgetedCashSpend    = budgetedCashSpend;
        this.unbudgetedSpend      = unbudgetedSpend;
        this.month                = month;
        this.year                 = year;
        this.hasBudgets           = hasBudgets;
    }

    /**
     * Backward-compatible constructor for testing / callers.
     */
    public AllowanceCalculator(double totalMonthlyLimit,
                               double digitalSpend,
                               double cashSpend,
                               int month,
                               int year) {
        this(totalMonthlyLimit, digitalSpend, cashSpend, 0.0, month, year, totalMonthlyLimit > 0);
    }

    /** Total money spent this month in categories that have a budget (digital + cash). */
    public double getBudgetedSpent() {
        return budgetedDigitalSpend + budgetedCashSpend;
    }

    /** Total money spent in budgeted categories (alias for backward compatibility). */
    public double getTotalSpent() {
        return getBudgetedSpent();
    }

    /** Money left from the total monthly budget in budgeted categories (pool − spent). */
    public double getMonthlyRemaining() {
        return pool - getBudgetedSpent();
    }

    /**
     * Days remaining in the month, including today (in Asia/Kolkata).
     * Returns 1 minimum so division by zero never occurs.
     */
    public int getDaysLeft() {
        int daysInMonth = YearMonth.of(year, month).lengthOfMonth();
        // Resolves current day of month in Asia/Kolkata timezone to avoid date rollover skew
        int today       = LocalDate.now(ZoneId.of("Asia/Kolkata")).getDayOfMonth();
        int left        = daysInMonth - today + 1;
        return Math.max(left, 1);
    }

    /**
     * Daily allowance = (pool − spent) ÷ daysLeft.
     * When spent > pool, returns negative per-day value (never clamped to 0.00).
     */
    public double getDailyAllowance() {
        if (!hasBudgets) {
            return 0.0;
        }
        // Daily allowance formula: unclamped (pool − budgetedSpent) ÷ daysLeft
        return getMonthlyRemaining() / getDaysLeft();
    }

    /**
     * One-line transparency formula with real numbers.
     * Example: "₹200 budget − ₹250 spent in budgeted categories = −₹50 ÷ 22 days"
     * Example: "₹200 budget − ₹150 spent in budgeted categories = ₹50 left ÷ 22 days"
     */
    public String getFormula() {
        if (!hasBudgets) {
            return "Set a budget to see your daily allowance";
        }
        double remaining = getMonthlyRemaining();
        String remainingPart = remaining < 0
                ? String.format("−\u20b9%,.0f", Math.abs(remaining))
                : String.format("\u20b9%,.0f left", remaining);

        return String.format(
                "\u20b9%,.0f budget \u2212 \u20b9%,.0f spent in budgeted categories = %s \u00f7 %d day%s",
                pool,
                getBudgetedSpent(),
                remainingPart,
                getDaysLeft(),
                getDaysLeft() == 1 ? "" : "s");
    }

    // ── Getters (for JSP EL / transparency display) ───────────────────────

    public double getPool()                 { return pool; }
    public double getTotalMonthlyLimit()    { return pool; }
    public double getBudgetedDigitalSpend() { return budgetedDigitalSpend; }
    public double getDigitalSpend()         { return budgetedDigitalSpend; }
    public double getBudgetedCashSpend()    { return budgetedCashSpend; }
    public double getCashSpend()            { return budgetedCashSpend; }
    public double getUnbudgetedSpend()      { return unbudgetedSpend; }
    public boolean isHasBudgets()           { return hasBudgets; }
    public boolean getHasBudgets()          { return hasBudgets; }
    public int getMonth()                   { return month; }
    public int getYear()                    { return year; }
}

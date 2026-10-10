package com.myfinance.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.myfinance.dao.impl.CashDAOImpl;
import com.myfinance.dao.interfaces.CashDAO;
import com.myfinance.model.Budget;
import com.myfinance.model.CashEntry;
import com.myfinance.util.CategoryMapper;

/**
 * Orchestrates the "₹X left today" allowance calculation for the dashboard hero card.
 *
 * <p>Calculation rules:
 * <ul>
 *   <li>Allowance pool = sum of this month's budget limits.</li>
 *   <li>Spent = this month's spending (digital + cash) ONLY in categories that have a budget.</li>
 *   <li>Per day = (pool − spent) ÷ remaining days including today.</li>
 *   <li>Negative values are never clamped to ₹0.00.</li>
 * </ul>
 */
public class AllowanceService {

    private final ExpenseService expenseService;
    private final CashService    cashService;
    private final BudgetService  budgetService;
    private final CashDAO        cashDAO;

    /**
     * Default constructor initializing dependencies with default implementations.
     */
    public AllowanceService() {
        this(new ExpenseService(), new CashService(), new BudgetService(), new CashDAOImpl());
    }

    /**
     * Constructor for dependency injection and testing.
     *
     * @param expenseService expense service instance
     * @param cashService    cash service instance
     * @param budgetService  budget service instance
     */
    public AllowanceService(ExpenseService expenseService,
                             CashService cashService,
                             BudgetService budgetService) {
        this(expenseService, cashService, budgetService, new CashDAOImpl());
    }

    /**
     * Full injection constructor.
     *
     * @param expenseService expense service instance
     * @param cashService    cash service instance
     * @param budgetService  budget service instance
     * @param cashDAO        cash DAO instance
     */
    public AllowanceService(ExpenseService expenseService,
                             CashService cashService,
                             BudgetService budgetService,
                             CashDAO cashDAO) {
        this.expenseService = expenseService;
        this.cashService    = cashService;
        this.budgetService  = budgetService;
        this.cashDAO        = cashDAO != null ? cashDAO : new CashDAOImpl();
    }

    /**
     * Builds an {@link AllowanceCalculator} populated with live data for the
     * given user and the current calendar month.
     *
     * @param userId authenticated user
     * @return fully-populated calculator ready for {@link AllowanceCalculator#getDailyAllowance()}
     */
    public AllowanceCalculator buildCalculator(int userId) {
        // Evaluate current day and month using Asia/Kolkata timezone
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        int month = today.getMonthValue();
        int year  = today.getYear();

        // 1. Allowance pool = sum of this month's active budget limits
        List<Budget> budgets = budgetService.getUserBudgets(userId);
        List<Budget> activeBudgets = (budgets != null ? budgets : List.<Budget>of()).stream()
                .filter(b -> b.getEndDate() == null || !b.getEndDate().isBefore(today))
                .toList();

        boolean hasBudgets = !activeBudgets.isEmpty();
        double pool = activeBudgets.stream()
                .mapToDouble(Budget::getLimitAmount)
                .sum();

        // Collect normalized budgeted categories
        Set<String> budgetedCategories = new HashSet<>();
        for (Budget b : activeBudgets) {
            if (b.getCategory() != null) {
                budgetedCategories.add(CategoryMapper.normalize(b.getCategory()).toLowerCase(Locale.ENGLISH));
            }
        }

        // 2. Digital spending this month (budgeted vs unbudgeted)
        Map<String, Double> digitalTotals = expenseService.getCategoryTotals(userId, month, year);
        double budgetedDigitalSpend = 0.0;
        double unbudgetedDigitalSpend = 0.0;
        if (digitalTotals != null) {
            for (Map.Entry<String, Double> entry : digitalTotals.entrySet()) {
                String norm = CategoryMapper.normalize(entry.getKey()).toLowerCase(Locale.ENGLISH);
                if (budgetedCategories.contains(norm)) {
                    budgetedDigitalSpend += entry.getValue();
                } else {
                    unbudgetedDigitalSpend += entry.getValue();
                }
            }
        }

        // 3. Physical cash spending this month from cash_entries (budgeted vs unbudgeted)
        double budgetedCashSpend = 0.0;
        double unbudgetedCashSpend = 0.0;
        try {
            List<CashEntry> cashEntries = cashDAO.findByUserIdAndMonth(userId, month, year);
            if (cashEntries != null) {
                for (CashEntry c : cashEntries) {
                    String norm = CategoryMapper.normalize(c.getCategory()).toLowerCase(Locale.ENGLISH);
                    if (budgetedCategories.contains(norm)) {
                        budgetedCashSpend += c.getAmount();
                    } else {
                        unbudgetedCashSpend += c.getAmount();
                    }
                }
            }
        } catch (Exception ignored) {}

        double totalUnbudgetedSpend = unbudgetedDigitalSpend + unbudgetedCashSpend;

        return new AllowanceCalculator(
                pool,
                budgetedDigitalSpend,
                budgetedCashSpend,
                totalUnbudgetedSpend,
                month,
                year,
                hasBudgets
        );
    }

    /**
     * Convenience: returns just the daily allowance number for the hero card.
     *
     * @param userId authenticated user
     * @return daily allowance (₹ per day), unclamped
     */
    public double getDailyAllowance(int userId) {
        return buildCalculator(userId).getDailyAllowance();
    }

    /**
     * Convenience: one-line FinanceOperation summaries for each of the four
     * implementors. Used by the transparency panel below the hero card.
     *
     * @param userId authenticated user
     * @return array of four summary strings [expenses, budgets, goals, cash]
     */
    public String[] getOperationSummaries(int userId) {
        GoalService goalService = new GoalService();
        return new String[]{
            expenseService.summarize(userId),
            budgetService.summarize(userId),
            goalService.summarize(userId),
            cashService.summarize(userId)
        };
    }
}

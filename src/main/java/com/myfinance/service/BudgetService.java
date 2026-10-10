package com.myfinance.service;

import com.myfinance.dao.impl.BudgetDAOImpl;
import com.myfinance.dao.impl.ExpenseDAOImpl;
import com.myfinance.dao.interfaces.BudgetDAO;
import com.myfinance.dao.interfaces.ExpenseDAO;
import com.myfinance.exception.DatabaseException;
import com.myfinance.model.Budget;
import com.myfinance.model.Category;
import com.myfinance.model.FinanceOperation;
import com.myfinance.model.SpendGuardAlert;
import com.myfinance.util.ApiResponse;
import com.myfinance.util.CategoryMapper;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service managing budget creation, tracking, and Smart Budget Alerts (Innovation 1).
 * Implements {@link FinanceOperation} — execute() creates a budget,
 * summarize() returns the count of active budgets and their aggregate limit.
 */
public class BudgetService implements FinanceOperation<Budget, ApiResponse<Budget>> {

    private final BudgetDAO budgetDAO;
    private final ExpenseDAO expenseDAO;

    public BudgetService() {
        this.budgetDAO = new BudgetDAOImpl();
        this.expenseDAO = new ExpenseDAOImpl();
    }

    public BudgetService(BudgetDAO budgetDAO, ExpenseDAO expenseDAO) {
        this.budgetDAO = budgetDAO;
        this.expenseDAO = expenseDAO;
    }

    /**
     * Creates a new budget, automatically calculates already-logged spending in that date range,
     * and persists the budget.
     *
     * @param budget prospective budget entity
     * @return {@link ApiResponse}
     */
    public ApiResponse<Budget> createBudget(Budget budget) {
        if (budget == null) {
            return ApiResponse.error("Budget cannot be null.");
        }

        if (!budget.validate()) {
            return ApiResponse.error("Please provide valid category, positive limit amount, and valid date range.");
        }

        try {
            // Check if active budget already exists for this category in same period
            Budget active = budgetDAO.findActiveByUserIdAndCategory(budget.getUserId(), budget.getCategory(), budget.getStartDate());
            if (active != null) {
                return ApiResponse.error("An active budget already exists for '" + budget.getCategory() + "' in this time period.");
            }

            boolean saved = budgetDAO.save(budget);
            if (saved) {
                // Compute initial spent amount based on existing expenses in date range
                budgetDAO.recalculateSpentAmount(budget.getUserId(), budget.getCategory(), budget.getStartDate(), budget.getEndDate());
                Budget refreshed = budgetDAO.findByIdAndUserId(budget.getId(), budget.getUserId());
                return ApiResponse.ok("Budget created successfully", refreshed != null ? refreshed : budget);
            }
            return ApiResponse.error("Failed to create budget.");
        } catch (SQLException e) {
            throw new DatabaseException("Error creating budget: " + e.getMessage(), e);
        }
    }

    public ApiResponse<Budget> updateBudget(Budget budget) {
        if (budget == null || !budget.validate() || budget.getId() <= 0) {
            return ApiResponse.error("Invalid budget details for update.");
        }

        try {
            boolean updated = budgetDAO.update(budget);
            if (updated) {
                budgetDAO.recalculateSpentAmount(budget.getUserId(), budget.getCategory(), budget.getStartDate(), budget.getEndDate());
                Budget refreshed = budgetDAO.findByIdAndUserId(budget.getId(), budget.getUserId());
                return ApiResponse.ok("Budget updated successfully", refreshed);
            }
            return ApiResponse.error("Failed to update budget.");
        } catch (SQLException e) {
            throw new DatabaseException("Error updating budget: " + e.getMessage(), e);
        }
    }

    public ApiResponse<Boolean> deleteBudget(int id, int userId) {
        try {
            boolean deleted = budgetDAO.delete(id, userId);
            if (deleted) {
                return ApiResponse.ok("Budget deleted successfully", true);
            }
            return ApiResponse.error("Budget not found or unauthorized.");
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting budget: " + e.getMessage(), e);
        }
    }

    public List<Budget> getUserBudgets(int userId) {
        try {
            budgetDAO.fixExistingBudgetsAndExpenses();
            List<Budget> budgets = budgetDAO.findByUserId(userId);
            // Ensure spent amounts are up-to-date
            for (Budget b : budgets) {
                budgetDAO.recalculateSpentAmount(userId, b.getCategory(), b.getStartDate(), b.getEndDate());
            }
            return budgetDAO.findByUserId(userId);
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching budgets: " + e.getMessage(), e);
        }
    }

    /**
     * Smart Budget Alerts Engine (Innovation 1).
     * Inspects active user budgets and returns contextual alert banners for 70%, 90%, and 100%+ usage.
     *
     * @param userId target user ID
     * @return list of human-readable alert message strings
     */
    public List<String> checkAlerts(int userId) {
        List<String> alerts = new ArrayList<>();
        try {
            List<Budget> budgets = budgetDAO.findByUserId(userId);
            LocalDate today = LocalDate.now();

            for (Budget b : budgets) {
                // Only alert for active or future budgets
                if (b.getEndDate() != null && b.getEndDate().isBefore(today)) {
                    continue;
                }

                double usage = b.getUsagePercent();
                String icon = Category.getIconForCategory(b.getCategory());
                double limit = b.getLimitAmount();
                double spent = b.getSpentAmount();
                double remaining = b.getRemainingAmount();

                if (usage >= 100.0) {
                    double over = Math.abs(remaining);
                    alerts.add(String.format("🚨 %s %s: ₹%,.2f budget − ₹%,.2f spent = −₹%,.2f (over limit) (OVER LIMIT, %,.2f%% used)",
                            icon, b.getCategory(), limit, spent, over, usage));
                } else if (usage >= 90.0) {
                    alerts.add(String.format("⚠️ %s %s: ₹%,.2f budget − ₹%,.2f spent = ₹%,.2f left (critically close to limit, %,.2f%% used)",
                            icon, b.getCategory(), limit, spent, remaining, usage));
                } else if (usage >= 70.0) {
                    alerts.add(String.format("🔔 %s %s: ₹%,.2f budget − ₹%,.2f spent = ₹%,.2f left (near limit, %,.2f%% used, %.0f%%)",
                            icon, b.getCategory(), limit, spent, remaining, usage, usage));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking budget alerts: " + e.getMessage(), e);
        }
        return alerts;
    }

    /**
     * Spend Guard simulation check before inserting an expense (Innovation 1: check BEFORE the INSERT).
     * Runs a fresh JDBC SELECT for that user's budget for that category, month and year,
     * and compares spent_amount + new amount against limit_amount.
     *
     * @param userId user ID
     * @param category expense category
     * @param newAmount prospective expense amount
     * @return {@link SpendGuardAlert} or null if within normal safe threshold
     */
    public SpendGuardAlert checkBeforeExpense(int userId, String category, double newAmount) {
        return checkBeforeExpense(userId, category, newAmount, LocalDate.now());
    }

    /**
     * Spend Guard simulation check before inserting an expense for a specific date.
     *
     * @param userId user ID
     * @param category expense category
     * @param newAmount prospective expense amount
     * @param expenseDate date of the prospective expense
     * @return {@link SpendGuardAlert} or null if within normal safe threshold
     */
    public SpendGuardAlert checkBeforeExpense(int userId, String category, double newAmount, LocalDate expenseDate) {
        if (expenseDate == null) {
            expenseDate = LocalDate.now();
        }
        String normalizedCategory = CategoryMapper.normalize(category);
        try {
            budgetDAO.recalculateSpentAmountForMonth(userId, normalizedCategory, expenseDate.getMonthValue(), expenseDate.getYear());
            Budget budget = budgetDAO.findActiveByUserIdAndCategoryAndPeriod(
                    userId, normalizedCategory, expenseDate.getMonthValue(), expenseDate.getYear()
            );
            if (budget == null) {
                budget = budgetDAO.findActiveByUserIdAndCategory(userId, normalizedCategory, expenseDate);
            }
            if (budget == null) {
                return null;
            }

            double limit = budget.getLimitAmount();
            if (limit <= 0) {
                return null;
            }

            double currentSpent = budget.getSpentAmount();
            double prospectiveSpent = currentSpent + newAmount;
            double prospectivePercent = (prospectiveSpent / limit) * 100.0;
            String formula = SpendGuardAlert.buildFormula(currentSpent, newAmount, prospectiveSpent, limit);

            // Spend Guard threshold: prospective spending > 100% triggers OVER_LIMIT alert
            if (prospectivePercent > 100.0) {
                // Exceeds 100% -> "over limit"
                String msg = "Oops, you've gone over 🙈 (" + formula + ")";
                return new SpendGuardAlert(
                        SpendGuardAlert.LEVEL_OVER_LIMIT, msg, formula, normalizedCategory,
                        currentSpent, newAmount, limit, prospectiveSpent, prospectivePercent
                );
            // Spend Guard threshold: prospective spending >= 80% triggers NEAR_LIMIT warning banner
            } else if (prospectivePercent >= 80.0) {
                // Reaches 80% or more -> "near limit"
                String msg = "Heads up! Approaching limit (" + formula + ")";
                return new SpendGuardAlert(
                        SpendGuardAlert.LEVEL_NEAR_LIMIT, msg, formula, normalizedCategory,
                        currentSpent, newAmount, limit, prospectiveSpent, prospectivePercent
                );
            }
        } catch (SQLException e) {
            // Silently fail open on check error
        }
        return null;
    }

    /**
     * Spend Guard simulation check before inserting an expense (Legacy helper).
     */
    public String checkSpendGuard(int userId, String category, double newAmount) {
        SpendGuardAlert alert = checkBeforeExpense(userId, category, newAmount);
        return alert != null ? alert.getMessage() : null;
    }

    /**
     * Fixes existing data: normalizes categories and recalculates spent_amount from
     * the expenses table for all current budgets.
     */
    public void fixExistingBudgetsAndExpenses() {
        try {
            budgetDAO.fixExistingBudgetsAndExpenses();
        } catch (SQLException e) {
            throw new DatabaseException("Error fixing existing budgets: " + e.getMessage(), e);
        }
    }

    // ── FinanceOperation ─────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     * Delegates to {@link #createBudget(Budget)}.
     */
    @Override
    public ApiResponse<Budget> execute(Budget budget) {
        return createBudget(budget);
    }

    /**
     * {@inheritDoc}
     * Returns the number of active budgets and their combined monthly limit for this user.
     * Example: "3 active budgets · ₹10,000.00 total limit"
     */
    @Override
    public String summarize(int userId) {
        List<Budget> budgets = getUserBudgets(userId);
        long active = budgets.stream()
                .filter(b -> b.getEndDate() == null || !b.getEndDate().isBefore(LocalDate.now()))
                .count();
        double totalLimit = budgets.stream()
                .filter(b -> b.getEndDate() == null || !b.getEndDate().isBefore(LocalDate.now()))
                .mapToDouble(Budget::getLimitAmount)
                .sum();
        return String.format("%d active budget%s · ₹%,.2f total limit",
                active, active == 1 ? "" : "s", totalLimit);
    }
}

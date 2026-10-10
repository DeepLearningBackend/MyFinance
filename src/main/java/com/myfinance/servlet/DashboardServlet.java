package com.myfinance.servlet;

import com.myfinance.model.Budget;
import com.myfinance.model.Category;
import com.myfinance.model.Expense;
import com.myfinance.model.Goal;
import com.myfinance.service.AllowanceCalculator;
import com.myfinance.service.AllowanceService;
import com.myfinance.service.BudgetService;
import com.myfinance.service.ExpenseService;
import com.myfinance.service.GoalService;
import com.myfinance.service.InsightService;
import com.myfinance.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for User Dashboard (/dashboard).
 * Aggregates monthly totals, budget statuses, goals, weekly insights, and charts.
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

    private final ExpenseService expenseService;
    private final BudgetService budgetService;
    private final GoalService goalService;
    private final InsightService insightService;
    private final AllowanceService allowanceService;

    /**
     * Default constructor initializing all dependent services.
     */
    public DashboardServlet() {
        this.expenseService  = new ExpenseService();
        this.budgetService   = new BudgetService();
        this.goalService     = new GoalService();
        this.insightService  = new InsightService();
        this.allowanceService = new AllowanceService();
    }

    /**
     * Constructor for dependency injection and testing.
     *
     * @param expenseService expense service instance
     * @param budgetService  budget service instance
     * @param goalService    goal service instance
     * @param insightService insight service instance
     */
    public DashboardServlet(ExpenseService expenseService, BudgetService budgetService, GoalService goalService, InsightService insightService) {
        this.expenseService   = expenseService;
        this.budgetService    = budgetService;
        this.goalService      = goalService;
        this.insightService   = insightService;
        this.allowanceService = new AllowanceService(expenseService, new com.myfinance.service.CashService(), budgetService);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.checkSessionOrRedirect(request, response)) {
            return;
        }

        if (SessionUtil.isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        int userId = SessionUtil.getSessionUserId(request);
        // Timezone: Asia/Kolkata ensures current month and day match local user time
        LocalDate now = LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"));
        int curMonth = now.getMonthValue();
        int curYear = now.getYear();

        // 1. Spending Metrics
        double monthlyTotal = expenseService.getMonthlyTotal(userId, curMonth, curYear);
        double allTimeTotal = expenseService.getTotalSpent(userId);
        List<Expense> recentExpenses = expenseService.getRecentExpenses(userId, 5);

        // 2. Category totals for donut/bar charts
        Map<String, Double> categoryTotals = expenseService.getCategoryTotals(userId, curMonth, curYear);
        if (categoryTotals.isEmpty()) {
            categoryTotals = expenseService.getAllTimeCategoryTotals(userId);
        }

        // 3. Budgets & Smart Alerts
        List<Budget> budgets = budgetService.getUserBudgets(userId);
        List<String> budgetAlerts = budgetService.checkAlerts(userId);

        double totalBudgetLimit = 0.0;
        double totalBudgetSpent = 0.0;
        for (Budget b : budgets) {
            totalBudgetLimit += b.getLimitAmount();
            totalBudgetSpent += b.getSpentAmount();
        }

        // 4. Goals & Milestone Badges
        List<Goal> goals = goalService.getUserGoals(userId, null);
        List<Map<String, Object>> goalCards = new ArrayList<>();
        for (Goal g : goals) {
            Map<String, Object> details = goalService.getProgressDetails(g);
            details.put("goal", g);
            goalCards.add(details);
        }

        // 5. Weekly Plain Insight Card
        String weeklyInsight = insightService.getWeeklyInsight(userId);

        // 6. Daily Allowance Hero (Innovation 4 — transparent automation)
        AllowanceCalculator allowance = allowanceService.buildCalculator(userId);
        String[] opSummaries = allowanceService.getOperationSummaries(userId);

        // Pass attributes to JSP
        request.setAttribute("monthlyTotal", monthlyTotal);
        request.setAttribute("allTimeTotal", allTimeTotal);
        request.setAttribute("recentExpenses", recentExpenses);
        request.setAttribute("categoryTotals", categoryTotals);
        request.setAttribute("budgets", budgets);
        request.setAttribute("totalBudgetLimit", totalBudgetLimit);
        request.setAttribute("totalBudgetSpent", totalBudgetSpent);
        request.setAttribute("budgetAlerts", budgetAlerts);
        request.setAttribute("goalCards", goalCards);
        request.setAttribute("weeklyInsight", weeklyInsight);
        request.setAttribute("currentMonthName", now.getMonth().name());
        request.setAttribute("currentYear", curYear);
        // Allowance hero attributes
        request.setAttribute("allowance", allowance);
        request.setAttribute("opSummaries", opSummaries);

        request.getRequestDispatcher("/jsp/user/dashboard.jsp").forward(request, response);
    }
}

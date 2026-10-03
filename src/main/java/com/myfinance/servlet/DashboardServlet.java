package com.myfinance.servlet;

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
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private ExpenseService expenseService;
    private BudgetService budgetService;
    private GoalService goalService;
    private InsightService insightService;

    @Override
    public void init() throws ServletException {
        expenseService = new ExpenseService();
        budgetService = new BudgetService();
        goalService = new GoalService();
        insightService = new InsightService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        String role = SessionUtil.getUserRole(request);
        if ("ADMIN".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        int userId = SessionUtil.getUserId(request);
        LocalDate now = LocalDate.now();

        try {
            // Get monthly total
            double monthlyTotal = expenseService.getMonthlyTotal(userId, now.getMonthValue(), now.getYear());

            // Get recent expenses (last 5)
            List<?> recentExpenses = expenseService.getMonthlyExpenses(userId, now.getMonthValue(), now.getYear());
            if (recentExpenses.size() > 5) {
                recentExpenses = recentExpenses.subList(0, 5);
            }

            // Get budgets with alerts
            List<?> budgets = budgetService.getBudgetStatus(userId);
            List<String> alerts = budgetService.checkAlerts(userId);

            // Get goals
            List<?> goals = goalService.getAllGoals(userId);

            // Get weekly insight
            String insight = insightService.getWeeklyInsight(userId);

            request.setAttribute("monthlyTotal", monthlyTotal);
            request.setAttribute("recentExpenses", recentExpenses);
            request.setAttribute("budgets", budgets);
            request.setAttribute("alerts", alerts);
            request.setAttribute("goals", goals);
            request.setAttribute("insight", insight);

            request.getRequestDispatcher("/jsp/user/dashboard.jsp").forward(request, response);
        } catch (SQLException e) {
            request.setAttribute("error", "Error loading dashboard: " + e.getMessage());
            request.getRequestDispatcher("/jsp/user/dashboard.jsp").forward(request, response);
        }
    }
}

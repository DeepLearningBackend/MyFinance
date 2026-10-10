package com.myfinance.servlet;

import com.myfinance.model.Budget;
import com.myfinance.model.Category;
import com.myfinance.service.BudgetService;
import com.myfinance.util.ApiResponse;
import com.myfinance.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for managing category budgets and smart threshold monitoring (/budgets).
 */
@WebServlet(name = "BudgetServlet", urlPatterns = {"/budgets"})
public class BudgetServlet extends HttpServlet {

    private final BudgetService budgetService;

    /**
     * Default constructor initializing BudgetService.
     */
    public BudgetServlet() {
        this.budgetService = new BudgetService();
    }

    /**
     * Constructor for dependency injection and testing.
     *
     * @param budgetService budget service instance
     */
    public BudgetServlet(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.checkSessionOrRedirect(request, response)) {
            return;
        }

        int userId = SessionUtil.getSessionUserId(request);
        List<Budget> budgets = budgetService.getUserBudgets(userId);
        List<String> alerts = budgetService.checkAlerts(userId);

        double totalBudget = 0.0;
        double totalSpent = 0.0;
        for (Budget b : budgets) {
            totalBudget += b.getLimitAmount();
            totalSpent += b.getSpentAmount();
        }

        request.setAttribute("budgets", budgets);
        request.setAttribute("alerts", alerts);
        request.setAttribute("totalBudget", totalBudget);
        request.setAttribute("totalSpent", totalSpent);
        request.setAttribute("totalRemaining", totalBudget - totalSpent);
        request.setAttribute("categories", Category.ALL_CATEGORIES);

        request.getRequestDispatcher("/jsp/user/budgets.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.checkSessionOrRedirect(request, response)) {
            return;
        }

        int userId = SessionUtil.getSessionUserId(request);
        String action = request.getParameter("action");
        if (action == null) action = "create";

        switch (action.toLowerCase()) {
            case "edit" -> handleEdit(request, response, userId);
            case "delete" -> handleDelete(request, response, userId);
            default -> handleCreate(request, response, userId);
        }
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException {
        String category = request.getParameter("category");
        String limitStr = request.getParameter("limitAmount");
        String startStr = request.getParameter("startDate");
        String endStr = request.getParameter("endDate");

        try {
            double limit = Double.parseDouble(limitStr);
            LocalDate start = (startStr != null && !startStr.isEmpty()) ? LocalDate.parse(startStr) : LocalDate.now().withDayOfMonth(1);
            LocalDate end = (endStr != null && !endStr.isEmpty()) ? LocalDate.parse(endStr) : start.plusMonths(1).minusDays(1);

            Budget budget = new Budget(userId, category, limit, 0.0, start, end);
            ApiResponse<Budget> result = budgetService.createBudget(budget);

            if (result.isSuccess()) {
                SessionUtil.setFlashSuccess(request, "Budget for " + category + " established successfully.");
            } else {
                SessionUtil.setFlashError(request, result.getMessage());
            }
        } catch (Exception e) {
            SessionUtil.setFlashError(request, "Invalid budget parameters: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/budgets");
    }

    private void handleEdit(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException {
        String idStr = request.getParameter("id");
        String category = request.getParameter("category");
        String limitStr = request.getParameter("limitAmount");
        String startStr = request.getParameter("startDate");
        String endStr = request.getParameter("endDate");

        try {
            int id = Integer.parseInt(idStr);
            double limit = Double.parseDouble(limitStr);
            LocalDate start = LocalDate.parse(startStr);
            LocalDate end = LocalDate.parse(endStr);

            Budget budget = new Budget(id, userId, category, limit, 0.0, start, end, null);
            ApiResponse<Budget> result = budgetService.updateBudget(budget);

            if (result.isSuccess()) {
                SessionUtil.setFlashSuccess(request, "Budget updated.");
            } else {
                SessionUtil.setFlashError(request, result.getMessage());
            }
        } catch (Exception e) {
            SessionUtil.setFlashError(request, "Error updating budget: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/budgets");
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException {
        String idStr = request.getParameter("id");
        try {
            int id = Integer.parseInt(idStr);
            ApiResponse<Boolean> result = budgetService.deleteBudget(id, userId);
            if (result.isSuccess()) {
                SessionUtil.setFlashSuccess(request, "Budget deleted.");
            } else {
                SessionUtil.setFlashError(request, result.getMessage());
            }
        } catch (Exception e) {
            SessionUtil.setFlashError(request, "Error deleting budget: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/budgets");
    }
}

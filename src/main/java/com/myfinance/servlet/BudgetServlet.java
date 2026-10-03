package com.myfinance.servlet;

import com.myfinance.exception.ValidationException;
import com.myfinance.model.Budget;
import com.myfinance.service.BudgetService;
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

@WebServlet("/budgets")
public class BudgetServlet extends HttpServlet {
    private BudgetService budgetService;

    @Override
    public void init() throws ServletException {
        budgetService = new BudgetService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        int userId = SessionUtil.getUserId(request);

        try {
            List<Budget> budgets = budgetService.getBudgetStatus(userId);
            List<String> alerts = budgetService.checkAlerts(userId);

            request.setAttribute("budgets", budgets);
            request.setAttribute("alerts", alerts);

            request.getRequestDispatcher("/jsp/user/budgets.jsp").forward(request, response);
        } catch (SQLException e) {
            request.setAttribute("error", "Error loading budgets: " + e.getMessage());
            request.getRequestDispatcher("/jsp/user/budgets.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        int userId = SessionUtil.getUserId(request);
        String action = request.getParameter("action");

        try {
            if ("create".equals(action)) {
                Budget budget = new Budget();
                budget.setUserId(userId);
                budget.setCategory(request.getParameter("category"));
                budget.setLimitAmount(Double.parseDouble(request.getParameter("limitAmount")));
                budget.setSpentAmount(0.0);
                budget.setStartDate(LocalDate.parse(request.getParameter("startDate")));
                budget.setEndDate(LocalDate.parse(request.getParameter("endDate")));

                budgetService.createBudget(budget);
                request.getSession().setAttribute("success", "Budget created successfully");
            } else if ("delete".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id"));
                budgetService.deleteBudget(id);
                request.getSession().setAttribute("success", "Budget deleted successfully");
            } else if ("edit".equals(action)) {
                Budget budget = new Budget();
                budget.setId(Integer.parseInt(request.getParameter("id")));
                budget.setUserId(userId);
                budget.setCategory(request.getParameter("category"));
                budget.setLimitAmount(Double.parseDouble(request.getParameter("limitAmount")));
                budget.setSpentAmount(Double.parseDouble(request.getParameter("spentAmount")));
                budget.setStartDate(LocalDate.parse(request.getParameter("startDate")));
                budget.setEndDate(LocalDate.parse(request.getParameter("endDate")));

                budgetService.updateBudget(budget);
                request.getSession().setAttribute("success", "Budget updated successfully");
            }

            response.sendRedirect(request.getContextPath() + "/budgets");
        } catch (SQLException | ValidationException e) {
            request.getSession().setAttribute("error", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/budgets");
        }
    }
}

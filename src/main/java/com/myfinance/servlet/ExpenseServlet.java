package com.myfinance.servlet;

import com.myfinance.exception.ValidationException;
import com.myfinance.model.Expense;
import com.myfinance.service.ExpenseService;
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
import java.util.Map;

@WebServlet("/expenses")
public class ExpenseServlet extends HttpServlet {
    private ExpenseService expenseService;

    @Override
    public void init() throws ServletException {
        expenseService = new ExpenseService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        int userId = SessionUtil.getUserId(request);
        LocalDate now = LocalDate.now();

        try {
            // Get filter parameters
            String monthStr = request.getParameter("month");
            String yearStr = request.getParameter("year");
            String category = request.getParameter("category");

            int month = monthStr != null ? Integer.parseInt(monthStr) : now.getMonthValue();
            int year = yearStr != null ? Integer.parseInt(yearStr) : now.getYear();

            List<Expense> expenses;
            if (category != null && !category.isEmpty()) {
                expenses = expenseService.getMonthlyExpenses(userId, month, year);
                // Filter by category (simplified)
            } else {
                expenses = expenseService.getMonthlyExpenses(userId, month, year);
            }

            Map<String, Double> categoryTotals = expenseService.getCategoryTotals(userId, month, year);
            double monthlyTotal = expenseService.getMonthlyTotal(userId, month, year);

            request.setAttribute("expenses", expenses);
            request.setAttribute("categoryTotals", categoryTotals);
            request.setAttribute("monthlyTotal", monthlyTotal);
            request.setAttribute("currentMonth", month);
            request.setAttribute("currentYear", year);

            request.getRequestDispatcher("/jsp/user/expenses.jsp").forward(request, response);
        } catch (SQLException e) {
            request.setAttribute("error", "Error loading expenses: " + e.getMessage());
            request.getRequestDispatcher("/jsp/user/expenses.jsp").forward(request, response);
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
            if ("add".equals(action)) {
                Expense expense = new Expense();
                expense.setUserId(userId);
                expense.setTitle(request.getParameter("title"));
                expense.setAmount(Double.parseDouble(request.getParameter("amount")));
                expense.setCategory(request.getParameter("category"));
                expense.setExpenseDate(LocalDate.parse(request.getParameter("expenseDate")));
                expense.setDescription(request.getParameter("description"));

                expenseService.addExpense(expense);
                request.getSession().setAttribute("success", "Expense added successfully");
            } else if ("delete".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id"));
                expenseService.deleteExpense(id, userId);
                request.getSession().setAttribute("success", "Expense deleted successfully");
            } else if ("edit".equals(action)) {
                Expense expense = new Expense();
                expense.setId(Integer.parseInt(request.getParameter("id")));
                expense.setUserId(userId);
                expense.setTitle(request.getParameter("title"));
                expense.setAmount(Double.parseDouble(request.getParameter("amount")));
                expense.setCategory(request.getParameter("category"));
                expense.setExpenseDate(LocalDate.parse(request.getParameter("expenseDate")));
                expense.setDescription(request.getParameter("description"));

                expenseService.updateExpense(expense);
                request.getSession().setAttribute("success", "Expense updated successfully");
            }

            response.sendRedirect(request.getContextPath() + "/expenses");
        } catch (SQLException | ValidationException e) {
            request.getSession().setAttribute("error", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/expenses");
        }
    }
}

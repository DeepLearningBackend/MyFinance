package com.myfinance.servlet;

import com.myfinance.model.Category;
import com.myfinance.model.Expense;
import com.myfinance.model.SpendGuardAlert;
import com.myfinance.service.BudgetService;
import com.myfinance.service.ExpenseService;
import com.myfinance.util.ApiResponse;
import com.myfinance.util.CategoryMapper;
import com.myfinance.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing user expenses, filtering, category auto-suggestions, and Spend Guard (/expenses).
 */
@WebServlet(name = "ExpenseServlet", urlPatterns = {"/expenses"})
public class ExpenseServlet extends HttpServlet {

    private final ExpenseService expenseService;
    private final BudgetService budgetService;

    /**
     * Default constructor initializing ExpenseService and BudgetService.
     */
    public ExpenseServlet() {
        this.expenseService = new ExpenseService();
        this.budgetService = new BudgetService();
    }

    /**
     * Constructor for dependency injection and testing.
     *
     * @param expenseService expense service instance
     * @param budgetService  budget service instance
     */
    public ExpenseServlet(ExpenseService expenseService, BudgetService budgetService) {
        this.expenseService = expenseService;
        this.budgetService = budgetService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.checkSessionOrRedirect(request, response)) {
            return;
        }

        int userId = SessionUtil.getSessionUserId(request);
        // Populate view attributes (filters, expenses, totals, etc.)
        populateExpenseViewAttributes(request, userId);
        request.getRequestDispatcher("/jsp/user/expenses.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.checkSessionOrRedirect(request, response)) {
            return;
        }

        int userId = SessionUtil.getSessionUserId(request);
        String action = request.getParameter("action");
        if (action == null) action = "add";

        switch (action.toLowerCase()) {
            case "suggest" -> handleAutoSuggest(request, response);
            case "edit" -> handleEdit(request, response, userId);
            case "delete" -> handleDelete(request, response, userId);
            default -> handleAdd(request, response, userId);
        }
    }

    private void handleAdd(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException {
        String title = request.getParameter("title");
        String amountStr = request.getParameter("amount");
        String category = request.getParameter("category");
        String dateStr = request.getParameter("expenseDate");
        String description = request.getParameter("description");
        String forceStr = request.getParameter("force");
        boolean force = "true".equalsIgnoreCase(forceStr);

        try {
            double amount = Double.parseDouble(amountStr);
            LocalDate date = (dateStr != null && !dateStr.isEmpty()) ? LocalDate.parse(dateStr) : LocalDate.now();

            if (category == null || category.trim().isEmpty() || Category.OTHER.equals(category)) {
                category = CategoryMapper.suggest(title);
            }
            category = CategoryMapper.normalize(category);

            // Spend Guard check (Innovation 1: Golden rule 6: check BEFORE the INSERT)
            if (!force) {
                SpendGuardAlert alert = budgetService.checkBeforeExpense(userId, category, amount, date);
                if (alert != null) {
                    // Do NOT insert! Pass alert to expenses.jsp and show warning modal
                    request.setAttribute("spendGuardAlert", alert);
                    request.setAttribute("pendingTitle", title);
                    request.setAttribute("pendingAmount", amount);
                    request.setAttribute("pendingCategory", category);
                    request.setAttribute("pendingDate", date.toString());
                    request.setAttribute("pendingDescription", description != null ? description : "");
                    try {
                        doGet(request, response);
                    } catch (ServletException e) {
                        throw new IOException(e);
                    }
                    return;
                }
            }

            Expense expense = new Expense(userId, title, amount, category, date, description);
            ApiResponse<Expense> result = expenseService.addExpense(expense);
            if (result.isSuccess()) {
                SessionUtil.setFlashSuccess(request, "Expense added successfully.");
            } else {
                SessionUtil.setFlashError(request, result.getMessage());
            }
        } catch (Exception e) {
            SessionUtil.setFlashError(request, "Invalid expense data: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/expenses");
    }

    private void handleEdit(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException {
        String idStr = request.getParameter("id");
        String title = request.getParameter("title");
        String amountStr = request.getParameter("amount");
        String category = request.getParameter("category");
        String dateStr = request.getParameter("expenseDate");
        String description = request.getParameter("description");

        try {
            int id = Integer.parseInt(idStr);
            double amount = Double.parseDouble(amountStr);
            LocalDate date = (dateStr != null && !dateStr.isEmpty()) ? LocalDate.parse(dateStr) : LocalDate.now();

            Expense expense = new Expense(id, userId, title, amount, category, date, description, null);
            ApiResponse<Expense> result = expenseService.updateExpense(expense);

            if (result.isSuccess()) {
                SessionUtil.setFlashSuccess(request, "Expense updated successfully.");
            } else {
                SessionUtil.setFlashError(request, result.getMessage());
            }
        } catch (Exception e) {
            SessionUtil.setFlashError(request, "Error updating expense: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/expenses");
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException {
        String idStr = request.getParameter("id");
        try {
            int id = Integer.parseInt(idStr);
            ApiResponse<Boolean> result = expenseService.deleteExpense(id, userId);
            if (result.isSuccess()) {
                SessionUtil.setFlashSuccess(request, "Expense removed.");
            } else {
                SessionUtil.setFlashError(request, result.getMessage());
            }
        } catch (Exception e) {
            SessionUtil.setFlashError(request, "Error deleting expense: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/expenses");
    }

    /**
     * Populates request attributes needed for the expenses view, including filtered expenses,
     * totals, category breakdowns, and selected filter values.
     */
    private void populateExpenseViewAttributes(HttpServletRequest request, int userId) {
        // Retrieve filter parameters
        String categoryParam = request.getParameter("category");
        String monthParam = request.getParameter("month");
        String yearParam = request.getParameter("year");
        String searchQuery = request.getParameter("search");

        String selectedCategory = (categoryParam == null || categoryParam.isEmpty()) ? "ALL" : categoryParam;
        Integer selectedMonth = null;
        Integer selectedYear = null;

        if (monthParam != null && !"ALL".equalsIgnoreCase(monthParam)) {
            try {
                selectedMonth = Integer.parseInt(monthParam);
            } catch (NumberFormatException ignored) { }
        }
        if (yearParam != null && !yearParam.isEmpty()) {
            try {
                selectedYear = Integer.parseInt(yearParam);
            } catch (NumberFormatException ignored) { }
        }

        // Resolve actual filter values for service call
        String categoryFilter = "ALL".equalsIgnoreCase(selectedCategory) ? null : selectedCategory;
        String searchFilter = (searchQuery != null && !searchQuery.isEmpty()) ? searchQuery : null;

        // Fetch filtered expenses
        List<Expense> expenses = expenseService.filterExpenses(userId, selectedMonth, selectedYear, categoryFilter, searchFilter);
        double totalFilteredSum = expenses.stream().mapToDouble(Expense::getAmount).sum();

        // Category totals for charts (fallback to all‑time if month/year not provided)
        Map<String, Double> categoryTotals;
        if (selectedMonth != null && selectedYear != null) {
            categoryTotals = expenseService.getCategoryTotals(userId, selectedMonth, selectedYear);
        } else {
            categoryTotals = expenseService.getAllTimeCategoryTotals(userId);
        }
        if (categoryTotals == null || categoryTotals.isEmpty()) {
            categoryTotals = expenseService.getAllTimeCategoryTotals(userId);
        }

        // Populate request attributes expected by expenses.jsp
        request.setAttribute("expenses", expenses);
        request.setAttribute("totalFilteredSum", totalFilteredSum);
        request.setAttribute("categoryTotals", categoryTotals);
        request.setAttribute("categories", Category.ALL_CATEGORIES);
        request.setAttribute("selectedCategory", selectedCategory);
        request.setAttribute("selectedMonth", selectedMonth);
        request.setAttribute("selectedYear", selectedYear);
        request.setAttribute("searchQuery", searchQuery != null ? searchQuery : "");
    }



    private void handleAutoSuggest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String query = request.getParameter("title");
        String suggested = CategoryMapper.suggest(query);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.print(String.format("{\"suggestedCategory\":\"%s\"}", suggested));
            out.flush();
        }
    }
}

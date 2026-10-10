package com.myfinance.servlet;

import com.myfinance.model.CashEntry;
import com.myfinance.model.Category;
import com.myfinance.service.CashService;
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
 * Controller for the Cash Tracker (Innovation 3) — /cash.
 *
 * <p>Actions (doPost):
 * <ul>
 *   <li>{@code set-balance}  — set / reset the opening cash-in-hand for the month</li>
 *   <li>{@code add}          — record a new physical cash expense</li>
 *   <li>{@code delete}       — remove a cash entry (restores cash_in_hand)</li>
 *   <li>{@code reconcile}    — run end-of-month reconciliation check</li>
 * </ul>
 */
@WebServlet(name = "CashServlet", urlPatterns = {"/cash"})
public class CashServlet extends HttpServlet {

    private final CashService cashService;

    /**
     * Default constructor initializing CashService.
     */
    public CashServlet() {
        this.cashService = new CashService();
    }

    /**
     * Constructor for dependency injection and testing.
     *
     * @param cashService cash service instance
     */
    public CashServlet(CashService cashService) {
        this.cashService = cashService;
    }

    // ── GET ───────────────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Golden rule 1: always check session first
        if (!SessionUtil.checkSessionOrRedirect(request, response)) {
            return;
        }

        int userId = SessionUtil.getSessionUserId(request);
        populateCashViewAttributes(request, userId);
        request.getRequestDispatcher("/jsp/user/cash.jsp").forward(request, response);
    }

    // ── POST ──────────────────────────────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.checkSessionOrRedirect(request, response)) {
            return;
        }

        int userId = SessionUtil.getSessionUserId(request);
        String action = request.getParameter("action");
        if (action == null) action = "add";

        switch (action.toLowerCase()) {
            case "set-balance" -> handleSetBalance(request, response, userId);
            case "delete"      -> handleDelete(request, response, userId);
            case "reconcile"   -> handleReconcile(request, response, userId);
            default            -> handleAdd(request, response, userId);
        }
    }

    // ── Handlers ──────────────────────────────────────────────────────────

    /**
     * Set the opening cash-in-hand balance (beginning of month reset).
     */
    private void handleSetBalance(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {
        String balanceStr = request.getParameter("openingBalance");
        try {
            double balance = Double.parseDouble(balanceStr);
            ApiResponse<Double> result = cashService.setOpeningBalance(userId, balance);
            if (result.isSuccess()) {
                SessionUtil.setFlashSuccess(request, "Cash balance set to ₹" + String.format("%.2f", balance));
            } else {
                SessionUtil.setFlashError(request, result.getMessage());
            }
        } catch (NumberFormatException e) {
            SessionUtil.setFlashError(request, "Invalid balance amount.");
        }
        response.sendRedirect(request.getContextPath() + "/cash");
    }

    /**
     * Record a new cash expense entry.
     */
    private void handleAdd(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {
        String amountStr    = request.getParameter("amount");
        String category     = request.getParameter("category");
        String dateStr      = request.getParameter("entryDate");
        String description  = request.getParameter("description");

        try {
            double amount    = Double.parseDouble(amountStr);
            LocalDate date   = (dateStr != null && !dateStr.isEmpty())
                    ? LocalDate.parse(dateStr) : LocalDate.now();

            CashEntry entry = new CashEntry(userId, amount, category, date, description);
            ApiResponse<CashEntry> result = cashService.addCashExpense(entry);

            if (result.isSuccess()) {
                SessionUtil.setFlashSuccess(request, "Cash expense recorded.");
            } else {
                SessionUtil.setFlashError(request, result.getMessage());
            }
        } catch (NumberFormatException e) {
            SessionUtil.setFlashError(request, "Invalid amount: " + amountStr);
        } catch (Exception e) {
            SessionUtil.setFlashError(request, "Could not record cash expense: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/cash");
    }

    /**
     * Delete a cash entry. Only the owning user can delete their own entry.
     */
    private void handleDelete(HttpServletRequest request, HttpServletResponse response, int userId)
            throws IOException {
        String idStr = request.getParameter("id");
        try {
            int id = Integer.parseInt(idStr);
            ApiResponse<Boolean> result = cashService.deleteEntry(id, userId);
            if (result.isSuccess()) {
                SessionUtil.setFlashSuccess(request, "Cash entry removed.");
            } else {
                SessionUtil.setFlashError(request, result.getMessage());
            }
        } catch (NumberFormatException e) {
            SessionUtil.setFlashError(request, "Invalid entry ID.");
        }
        response.sendRedirect(request.getContextPath() + "/cash");
    }

    /**
     * End-of-month reconciliation: compare expected vs actual cash balance.
     */
    private void handleReconcile(HttpServletRequest request, HttpServletResponse response, int userId)
            throws ServletException, IOException {
        String openingStr = request.getParameter("openingBalance");
        String monthStr   = request.getParameter("month");
        String yearStr    = request.getParameter("year");

        try {
            double opening = Double.parseDouble(openingStr);
            int month = (monthStr != null && !monthStr.isEmpty())
                    ? Integer.parseInt(monthStr) : LocalDate.now().getMonthValue();
            int year  = (yearStr  != null && !yearStr.isEmpty())
                    ? Integer.parseInt(yearStr)  : LocalDate.now().getYear();

            String reconcileMsg = cashService.reconcile(userId, opening, month, year);
            // Render cash.jsp with the reconciliation result
            populateCashViewAttributes(request, userId);
            request.setAttribute("reconcileResult", reconcileMsg);
            request.getRequestDispatcher("/jsp/user/cash.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            SessionUtil.setFlashError(request, "Invalid reconciliation parameters.");
            response.sendRedirect(request.getContextPath() + "/cash");
        }
    }

    // ── View attribute helper ─────────────────────────────────────────────

    /**
     * Populate all request attributes that cash.jsp needs:
     * cashEntries, cashInHand, totalSpentThisMonth, categories,
     * selectedMonth, selectedYear.
     */
    private void populateCashViewAttributes(HttpServletRequest request, int userId) {
        String monthParam = request.getParameter("month");
        String yearParam  = request.getParameter("year");

        LocalDate today = LocalDate.now();
        int selectedMonth = today.getMonthValue();
        int selectedYear  = today.getYear();

        if (monthParam != null && !monthParam.isEmpty()) {
            try { selectedMonth = Integer.parseInt(monthParam); } catch (NumberFormatException ignored) {}
        }
        if (yearParam != null && !yearParam.isEmpty()) {
            try { selectedYear = Integer.parseInt(yearParam); } catch (NumberFormatException ignored) {}
        }

        List<CashEntry> entries;
        // If month/year differ from current, show filtered list; else show all
        if (selectedMonth == today.getMonthValue() && selectedYear == today.getYear()) {
            entries = cashService.getAllEntries(userId);
        } else {
            entries = cashService.getEntriesForMonth(userId, selectedMonth, selectedYear);
        }

        double cashInHand          = cashService.getCashInHand(userId);
        double totalSpentThisMonth = cashService.getTotalSpentThisMonth(userId);

        request.setAttribute("cashEntries",         entries);
        request.setAttribute("cashInHand",          cashInHand);
        request.setAttribute("totalSpentThisMonth", totalSpentThisMonth);
        request.setAttribute("categories",          Category.ALL_CATEGORIES);
        request.setAttribute("selectedMonth",       selectedMonth);
        request.setAttribute("selectedYear",        selectedYear);
    }
}

package com.myfinance.service;

import com.myfinance.dao.impl.CashDAOImpl;
import com.myfinance.dao.interfaces.CashDAO;
import com.myfinance.exception.DatabaseException;
import com.myfinance.model.CashEntry;
import com.myfinance.model.Category;
import com.myfinance.model.FinanceOperation;
import com.myfinance.util.ApiResponse;
import com.myfinance.util.CategoryMapper;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Business-logic layer for the Cash Tracker (Innovation 3).
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Set / reset the physical cash-in-hand balance at the start of a month.</li>
 *   <li>Record every physical cash expense and reduce the running balance.</li>
 *   <li>End-of-month reconciliation: compare opening balance − cash entries against
 *       the actual recorded cash_in_hand and flag a discrepancy if they diverge.</li>
 * </ul>
 * Implements {@link FinanceOperation} — execute() records a cash expense,
 * summarize() returns the current cash-in-hand and this month's total cash spend.
 */
public class CashService implements FinanceOperation<CashEntry, ApiResponse<CashEntry>> {

    private final CashDAO cashDAO;

    /**
     * Default constructor initializing CashDAO.
     */
    public CashService() {
        this.cashDAO = new CashDAOImpl();
    }

    /**
     * Constructor for dependency injection and testing.
     *
     * @param cashDAO cash DAO instance
     */
    public CashService(CashDAO cashDAO) {
        this.cashDAO = cashDAO;
    }

    // ── Set opening balance ───────────────────────────────────────────────

    /**
     * Set (or reset) the user's physical cash-in-hand to {@code openingBalance}.
     * Call this at the beginning of each month so the tracker starts fresh.
     *
     * @param userId         the authenticated user
     * @param openingBalance the cash balance the user currently holds (must be ≥ 0)
     * @return {@link ApiResponse}
     */
    public ApiResponse<Double> setOpeningBalance(int userId, double openingBalance) {
        if (openingBalance < 0) {
            return ApiResponse.error("Opening cash balance cannot be negative.");
        }
        try {
            cashDAO.setCashInHand(userId, openingBalance);
            return ApiResponse.ok("Cash balance set to \u20b9" + String.format("%.2f", openingBalance), openingBalance);
        } catch (SQLException e) {
            throw new DatabaseException("Could not update cash balance: " + e.getMessage(), e);
        }
    }

    // ── Record a cash expense ─────────────────────────────────────────────

    /**
     * Record a physical cash expense. Validates the entry, auto-suggests category
     * if not provided, then calls {@link CashDAO#save(CashEntry)} which atomically
     * deducts the amount from {@code users.cash_in_hand}.
     *
     * @param entry CashEntry populated by the caller (userId, amount, category, date)
     * @return {@link ApiResponse} wrapping the saved entry
     */
    public ApiResponse<CashEntry> addCashExpense(CashEntry entry) {
        if (entry == null) {
            return ApiResponse.error("Cash entry cannot be null.");
        }

        // Category auto-suggest when blank
        if (entry.getCategory() == null || entry.getCategory().trim().isEmpty()) {
            entry.setCategory(CategoryMapper.suggest(entry.getDescription()));
        }
        entry.setCategory(CategoryMapper.normalize(entry.getCategory()));

        if (!entry.validate()) {
            return ApiResponse.error("Invalid cash entry: amount must be > 0, category and date are required.");
        }

        // Guard: cannot spend more cash than available
        try {
            double available = cashDAO.getCashInHand(entry.getUserId());
            if (entry.getAmount() > available) {
                return ApiResponse.error(String.format(
                        "Insufficient cash: you have ₹%.2f but tried to spend ₹%.2f.",
                        available, entry.getAmount()));
            }

            boolean saved = cashDAO.save(entry);
            if (saved) {
                return ApiResponse.ok("Cash expense recorded.", entry);
            }
            return ApiResponse.error("Failed to save cash entry.");
        } catch (SQLException e) {
            throw new DatabaseException("Error recording cash expense: " + e.getMessage(), e);
        }
    }

    // ── Read ──────────────────────────────────────────────────────────────

    /**
     * Retrieve all cash entries for a user ordered newest-first.
     *
     * @param userId user ID
     * @return list of {@link CashEntry}
     * @throws DatabaseException on JDBC failure
     */
    public List<CashEntry> getAllEntries(int userId) {
        try {
            return cashDAO.findByUserId(userId);
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching cash entries: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieve cash entries for a specific month and year.
     *
     * @param userId user ID
     * @param month  calendar month (1-12)
     * @param year   calendar year
     * @return list of {@link CashEntry}
     * @throws DatabaseException on JDBC failure
     */
    public List<CashEntry> getEntriesForMonth(int userId, int month, int year) {
        try {
            return cashDAO.findByUserIdAndMonth(userId, month, year);
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching cash entries for month: " + e.getMessage(), e);
        }
    }

    /**
     * Current cash-in-hand balance for a user.
     *
     * @param userId user ID
     * @return available cash amount
     * @throws DatabaseException on JDBC failure
     */
    public double getCashInHand(int userId) {
        try {
            return cashDAO.getCashInHand(userId);
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching cash balance: " + e.getMessage(), e);
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────

    /**
     * Delete a cash entry (restores its amount to cash_in_hand automatically).
     *
     * @param id     entry ID
     * @param userId user ID
     * @return {@link ApiResponse} indicating success or failure
     * @throws DatabaseException on JDBC failure
     */
    public ApiResponse<Boolean> deleteEntry(int id, int userId) {
        try {
            boolean deleted = cashDAO.delete(id, userId);
            if (deleted) {
                return ApiResponse.ok("Cash entry removed.", true);
            }
            return ApiResponse.error("Entry not found or access denied.");
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting cash entry: " + e.getMessage(), e);
        }
    }

    // ── End-of-month reconciliation ───────────────────────────────────────

    /**
     * End-of-month reconciliation.
     *
     * <p>Compares the user's recorded cash_in_hand against what it <em>should</em>
     * be given an opening balance and all logged cash entries for the given month.
     *
     * <p>Formula: {@code expected = openingBalance − totalSpentThisMonth}
     *
     * <p>If {@code |expected − actual| > 1} (a ₹1 tolerance for rounding), a
     * reconciliation warning is returned.
     *
     * @param userId         the authenticated user
     * @param openingBalance the balance set at the beginning of the month
     * @param month          calendar month (1–12)
     * @param year           calendar year
     * @return a human-readable reconciliation message
     */
    public String reconcile(int userId, double openingBalance, int month, int year) {
        try {
            double totalSpent = cashDAO.getTotalSpentByMonth(userId, month, year);
            double expected   = openingBalance - totalSpent;
            double actual     = cashDAO.getCashInHand(userId);

            double discrepancy = Math.abs(expected - actual);

            if (discrepancy <= 1.0) {
                return String.format(
                        "✅ Reconciled: Opening ₹%.2f − Spent ₹%.2f = ₹%.2f. Balance matches.",
                        openingBalance, totalSpent, actual);
            } else {
                return String.format(
                        "⚠️ Discrepancy of ₹%.2f detected! "
                                + "Opening ₹%.2f − Logged spend ₹%.2f = expected ₹%.2f, "
                                + "but current balance is ₹%.2f. "
                                + "Some cash transactions may be unrecorded.",
                        discrepancy, openingBalance, totalSpent, expected, actual);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Reconciliation failed: " + e.getMessage(), e);
        }
    }

    // ── Aggregates for dashboard widget ──────────────────────────────────

    /**
     * Total cash spent this month (for the combined digital + cash view).
     *
     * @param userId user ID
     * @return sum of cash expenses for current calendar month
     * @throws DatabaseException on JDBC failure
     */
    public double getTotalSpentThisMonth(int userId) {
        LocalDate today = LocalDate.now();
        try {
            return cashDAO.getTotalSpentByMonth(userId, today.getMonthValue(), today.getYear());
        } catch (SQLException e) {
            throw new DatabaseException("Error calculating monthly cash spend: " + e.getMessage(), e);
        }
    }

    // ── FinanceOperation ─────────────────────────────────────────────────────

    /**
     * {@inheritDoc}
     * Delegates to {@link #addCashExpense(CashEntry)}.
     */
    @Override
    public ApiResponse<CashEntry> execute(CashEntry entry) {
        return addCashExpense(entry);
    }

    /**
     * {@inheritDoc}
     * Returns current cash-in-hand and this month's total cash spend.
     * Example: "₹1,200.00 cash in hand · ₹3,800.00 spent this month"
     */
    @Override
    public String summarize(int userId) {
        double inHand  = getCashInHand(userId);
        double spent   = getTotalSpentThisMonth(userId);
        return String.format("₹%,.2f cash in hand · ₹%,.2f spent this month", inHand, spent);
    }
}

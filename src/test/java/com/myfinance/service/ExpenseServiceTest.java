package com.myfinance.service;

import com.myfinance.dao.interfaces.BudgetDAO;
import com.myfinance.dao.interfaces.ExpenseDAO;
import com.myfinance.model.Budget;
import com.myfinance.model.Expense;
import com.myfinance.util.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExpenseService Unit Tests")
class ExpenseServiceTest {

    private ExpenseService expenseService;
    private MockExpenseDAO mockExpenseDAO;
    private MockBudgetDAO mockBudgetDAO;

    static class MockExpenseDAO implements ExpenseDAO {
        private final Map<Integer, Expense> store = new HashMap<>();
        private int idSeq = 1;

        @Override
        public boolean save(Expense expense) {
            expense.setId(idSeq++);
            store.put(expense.getId(), expense);
            return true;
        }

        @Override
        public Expense findById(int id) {
            return store.get(id);
        }

        @Override
        public Expense findByIdAndUserId(int id, int userId) {
            Expense e = store.get(id);
            return (e != null && e.getUserId() == userId) ? e : null;
        }

        @Override
        public List<Expense> findByUserId(int userId) {
            List<Expense> list = new ArrayList<>();
            for (Expense e : store.values()) {
                if (e.getUserId() == userId) list.add(e);
            }
            return list;
        }

        @Override
        public List<Expense> findByUserIdAndMonth(int userId, int month, int year) {
            List<Expense> list = new ArrayList<>();
            for (Expense e : store.values()) {
                if (e.getUserId() == userId && e.getExpenseDate().getMonthValue() == month && e.getExpenseDate().getYear() == year) {
                    list.add(e);
                }
            }
            return list;
        }

        @Override
        public List<Expense> findByUserIdAndCategory(int userId, String category) {
            List<Expense> list = new ArrayList<>();
            for (Expense e : store.values()) {
                if (e.getUserId() == userId && e.getCategory().equalsIgnoreCase(category)) {
                    list.add(e);
                }
            }
            return list;
        }

        @Override
        public List<Expense> findRecentByUserId(int userId, int limit) {
            return findByUserId(userId);
        }

        @Override
        public List<Expense> filterExpenses(int userId, Integer month, Integer year, String category, String searchQuery) {
            List<Expense> list = new ArrayList<>();
            for (Expense e : store.values()) {
                if (e.getUserId() != userId) continue;
                if (month != null && e.getExpenseDate().getMonthValue() != month) continue;
                if (year != null && e.getExpenseDate().getYear() != year) continue;
                if (category != null && !category.equalsIgnoreCase("ALL") && !e.getCategory().equalsIgnoreCase(category)) continue;
                if (searchQuery != null && !searchQuery.trim().isEmpty() && !e.getTitle().toLowerCase().contains(searchQuery.toLowerCase())) continue;
                list.add(e);
            }
            return list;
        }

        @Override
        public double getSumByUserIdAndMonth(int userId, int month, int year) {
            return findByUserIdAndMonth(userId, month, year).stream().mapToDouble(Expense::getAmount).sum();
        }

        @Override
        public double getTotalSpentByUserId(int userId) {
            return findByUserId(userId).stream().mapToDouble(Expense::getAmount).sum();
        }

        @Override
        public Map<String, Double> getCategoryTotalsByUserIdAndMonth(int userId, int month, int year) {
            Map<String, Double> totals = new HashMap<>();
            for (Expense e : findByUserIdAndMonth(userId, month, year)) {
                totals.put(e.getCategory(), totals.getOrDefault(e.getCategory(), 0.0) + e.getAmount());
            }
            return totals;
        }

        @Override
        public Map<String, Double> getAllTimeCategoryTotalsByUserId(int userId) {
            Map<String, Double> totals = new HashMap<>();
            for (Expense e : findByUserId(userId)) {
                totals.put(e.getCategory(), totals.getOrDefault(e.getCategory(), 0.0) + e.getAmount());
            }
            return totals;
        }

        @Override
        public List<Expense> findAll() {
            return new ArrayList<>(store.values());
        }

        @Override
        public boolean update(Expense expense) {
            if (store.containsKey(expense.getId())) {
                store.put(expense.getId(), expense);
                return true;
            }
            return false;
        }

        @Override
        public boolean delete(int id, int userId) {
            Expense e = store.get(id);
            if (e != null && e.getUserId() == userId) {
                store.remove(id);
                return true;
            }
            return false;
        }

        @Override
        public int countAllExpenses() {
            return store.size();
        }

        @Override
        public Map<String, Object> getPersonalityAggregates(int userId, int days) {
            return new HashMap<>();
        }

        @Override
        public int countDistinctExpenseDays(int userId, int days) {
            return 0;
        }
    }

    static class MockBudgetDAO implements BudgetDAO {
        @Override public boolean save(Budget budget) { return true; }
        @Override public Budget findById(int id) { return null; }
        @Override public Budget findByIdAndUserId(int id, int userId) { return null; }
        @Override public List<Budget> findByUserId(int userId) { return List.of(); }
        @Override public Budget findByUserIdAndCategory(int userId, String category) { return null; }
        @Override public Budget findActiveByUserIdAndCategory(int userId, String category, LocalDate currentDate) { return null; }
        @Override public Budget findActiveByUserIdAndCategoryAndPeriod(int userId, String category, int month, int year) { return null; }
        @Override public List<Budget> findOverBudget(int userId) { return List.of(); }
        @Override public List<Budget> findAll() { return List.of(); }
        @Override public boolean update(Budget budget) { return true; }
        @Override public boolean updateSpentAmount(int budgetId, double newSpentAmount) { return true; }
        @Override public boolean recalculateSpentAmount(int userId, String category, LocalDate startDate, LocalDate endDate) { return true; }
        @Override public boolean recalculateSpentAmountForMonth(int userId, String category, int month, int year) { return true; }
        @Override public void fixExistingBudgetsAndExpenses() {}
        @Override public boolean delete(int id, int userId) { return true; }
    }

    @BeforeEach
    void setUp() {
        mockExpenseDAO = new MockExpenseDAO();
        mockBudgetDAO = new MockBudgetDAO();
        expenseService = new ExpenseService(mockExpenseDAO, mockBudgetDAO);
    }

    @Test
    @DisplayName("Should successfully add valid expense and reject invalid ones")
    void testAddExpenseValidation() {
        // Valid expense
        Expense valid = new Expense(1, "Grocery Store", 1250.50, "Food", LocalDate.now(), "Supermarket items");
        ApiResponse<Expense> res = expenseService.addExpense(valid);
        assertTrue(res.isSuccess());
        assertNotNull(res.getData());
        assertTrue(res.getData().getId() > 0);

        // Invalid expense (null)
        ApiResponse<Expense> nullRes = expenseService.addExpense(null);
        assertFalse(nullRes.isSuccess());

        // Invalid amount (negative)
        Expense invalidAmount = new Expense(1, "Invalid", -10.0, "Food", LocalDate.now(), "Error test");
        ApiResponse<Expense> errRes = expenseService.addExpense(invalidAmount);
        assertFalse(errRes.isSuccess());
    }

    @Test
    @DisplayName("Should accurately calculate category totals and monthly sums")
    void testCategoryTotalsAndMonthlySums() {
        LocalDate today = LocalDate.of(2026, 10, 7);
        expenseService.addExpense(new Expense(1, "Swiggy Order", 500.0, "Food", today, "Lunch"));
        expenseService.addExpense(new Expense(1, "Zomato Order", 300.0, "Food", today, "Dinner"));
        expenseService.addExpense(new Expense(1, "Uber Cab", 400.0, "Transportation", today, "Ride"));

        Map<String, Double> categoryTotals = expenseService.getCategoryTotals(1, 10, 2026);
        assertEquals(800.0, categoryTotals.get("Food"));
        assertEquals(400.0, categoryTotals.get("Transportation"));

        double monthlyTotal = expenseService.getMonthlyTotal(1, 10, 2026);
        assertEquals(1200.0, monthlyTotal);
    }

    @Test
    @DisplayName("Should filter expenses by search query and category")
    void testFilterExpenses() {
        LocalDate today = LocalDate.of(2026, 10, 7);
        expenseService.addExpense(new Expense(1, "Coffee at Starbucks", 250.0, "Food", today, "Latte"));
        expenseService.addExpense(new Expense(1, "Book Purchase", 600.0, "Education", today, "Java book"));

        List<Expense> foodList = expenseService.filterExpenses(1, 10, 2026, "Food", null);
        assertEquals(1, foodList.size());
        assertEquals("Coffee at Starbucks", foodList.get(0).getTitle());

        List<Expense> searchList = expenseService.filterExpenses(1, 10, 2026, "ALL", "Starbucks");
        assertEquals(1, searchList.size());
    }
}

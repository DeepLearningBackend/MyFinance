package com.myfinance.service;

import com.myfinance.dao.interfaces.CashDAO;
import com.myfinance.model.Budget;
import com.myfinance.model.CashEntry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Daily Allowance Calculation Tests (Innovation 4)")
class AllowanceServiceTest {

    private final ZoneId kolkataZone = ZoneId.of("Asia/Kolkata");

    @Test
    @DisplayName("Case 1: No budgets set - hasBudgets is false, allowance is 0.0, prompt shown")
    void testNoBudgets() {
        BudgetService stubBudgetService = new BudgetService() {
            @Override
            public List<Budget> getUserBudgets(int userId) {
                return Collections.emptyList();
            }
        };

        ExpenseService stubExpenseService = new ExpenseService() {
            @Override
            public Map<String, Double> getCategoryTotals(int userId, int month, int year) {
                Map<String, Double> map = new HashMap<>();
                map.put("Food & Dining", 500.0);
                return map;
            }
        };

        CashDAO stubCashDAO = new CashDAOStub(Collections.emptyList());
        AllowanceService service = new AllowanceService(stubExpenseService, new CashService(), stubBudgetService, stubCashDAO);

        AllowanceCalculator calc = service.buildCalculator(1);
        assertFalse(calc.getHasBudgets(), "hasBudgets should be false when user has no active budgets");
        assertEquals(0.0, calc.getPool(), 0.001, "Allowance pool should be 0");
        assertEquals(0.0, calc.getDailyAllowance(), 0.001, "Daily allowance must be 0.0 when no budgets are set");
        assertEquals("Set a budget to see your daily allowance", calc.getFormula());
    }

    @Test
    @DisplayName("Case 2: Under budget - positive daily allowance, unclamped")
    void testUnderBudget() {
        LocalDate today = LocalDate.now(kolkataZone);
        Budget foodBudget = new Budget(1, 1, "Food & Dining", 200.0, 0.0, today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth()), LocalDateTime.now());

        BudgetService stubBudgetService = new BudgetService() {
            @Override
            public List<Budget> getUserBudgets(int userId) {
                return List.of(foodBudget);
            }
        };

        ExpenseService stubExpenseService = new ExpenseService() {
            @Override
            public Map<String, Double> getCategoryTotals(int userId, int month, int year) {
                Map<String, Double> map = new HashMap<>();
                map.put("Food & Dining", 150.0);
                map.put("Entertainment", 100.0); // unbudgeted category
                return map;
            }
        };

        CashDAO stubCashDAO = new CashDAOStub(Collections.emptyList());
        AllowanceService service = new AllowanceService(stubExpenseService, new CashService(), stubBudgetService, stubCashDAO);

        AllowanceCalculator calc = service.buildCalculator(1);
        assertTrue(calc.getHasBudgets());
        assertEquals(200.0, calc.getPool(), 0.001, "Pool should equal sum of budget limits (₹200)");
        assertEquals(150.0, calc.getBudgetedSpent(), 0.001, "Spent should only count budgeted categories (₹150)");
        assertEquals(50.0, calc.getMonthlyRemaining(), 0.001, "Remaining should be 200 - 150 = ₹50");
        assertEquals(100.0, calc.getUnbudgetedSpend(), 0.001, "Unbudgeted spend should track Entertainment (₹100)");

        int daysLeft = calc.getDaysLeft();
        assertTrue(daysLeft >= 1, "Days left must be >= 1");
        double expectedPerDay = 50.0 / daysLeft;
        assertEquals(expectedPerDay, calc.getDailyAllowance(), 0.001, "Daily allowance should be remaining / daysLeft");
        assertTrue(calc.getDailyAllowance() > 0, "Daily allowance must be positive when under budget");

        String formula = calc.getFormula();
        assertTrue(formula.contains("200 budget"), "Formula should include pool: " + formula);
        assertTrue(formula.contains("150 spent in budgeted categories"), "Formula should include budgeted spend: " + formula);
        assertTrue(formula.contains("50 left"), "Formula should include positive remaining balance: " + formula);
    }

    @Test
    @DisplayName("Case 3: Over budget - negative daily allowance, never clamped to 0.00")
    void testOverBudget() {
        LocalDate today = LocalDate.now(kolkataZone);
        Budget foodBudget = new Budget(1, 1, "Food & Dining", 200.0, 0.0, today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth()), LocalDateTime.now());

        BudgetService stubBudgetService = new BudgetService() {
            @Override
            public List<Budget> getUserBudgets(int userId) {
                return List.of(foodBudget);
            }
        };

        ExpenseService stubExpenseService = new ExpenseService() {
            @Override
            public Map<String, Double> getCategoryTotals(int userId, int month, int year) {
                Map<String, Double> map = new HashMap<>();
                map.put("Food & Dining", 250.0);
                return map;
            }
        };

        CashDAO stubCashDAO = new CashDAOStub(Collections.emptyList());
        AllowanceService service = new AllowanceService(stubExpenseService, new CashService(), stubBudgetService, stubCashDAO);

        AllowanceCalculator calc = service.buildCalculator(1);
        assertEquals(200.0, calc.getPool(), 0.001);
        assertEquals(250.0, calc.getBudgetedSpent(), 0.001);
        assertEquals(-50.0, calc.getMonthlyRemaining(), 0.001);

        int daysLeft = calc.getDaysLeft();
        double expectedPerDay = -50.0 / daysLeft;
        assertEquals(expectedPerDay, calc.getDailyAllowance(), 0.001, "Daily allowance should be negative when over budget");
        assertTrue(calc.getDailyAllowance() < 0, "Allowance must NEVER be clamped to 0.00 when over budget");

        String formula = calc.getFormula();
        assertTrue(formula.contains("200 budget"), "Formula should show budget pool: " + formula);
        assertTrue(formula.contains("250 spent in budgeted categories"), "Formula should show budgeted spend: " + formula);
        assertTrue(formula.contains("−₹50") || formula.contains("-₹50"), "Formula should show negative remaining balance: " + formula);
    }

    @Test
    @DisplayName("Case 4: Cash included in calculation - only budgeted cash counts towards spent, unbudgeted cash isolated")
    void testCashIncluded() {
        LocalDate today = LocalDate.now(kolkataZone);
        Budget foodBudget = new Budget(1, 1, "Food & Dining", 1000.0, 0.0, today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth()), LocalDateTime.now());

        BudgetService stubBudgetService = new BudgetService() {
            @Override
            public List<Budget> getUserBudgets(int userId) {
                return List.of(foodBudget);
            }
        };

        // Digital spending: 200 in Food & Dining
        ExpenseService stubExpenseService = new ExpenseService() {
            @Override
            public Map<String, Double> getCategoryTotals(int userId, int month, int year) {
                Map<String, Double> map = new HashMap<>();
                map.put("Food & Dining", 200.0);
                return map;
            }
        };

        // Cash spending: 300 in Food & Dining (budgeted), 400 in Shopping (unbudgeted)
        CashEntry cashFood = new CashEntry(1, 300.0, "Food & Dining", today, "Grocery cash");
        CashEntry cashShopping = new CashEntry(1, 400.0, "Shopping", today, "Clothes cash");

        CashDAO stubCashDAO = new CashDAOStub(List.of(cashFood, cashShopping));
        AllowanceService service = new AllowanceService(stubExpenseService, new CashService(), stubBudgetService, stubCashDAO);

        AllowanceCalculator calc = service.buildCalculator(1);
        assertEquals(1000.0, calc.getPool(), 0.001, "Pool should be 1000");
        assertEquals(200.0, calc.getBudgetedDigitalSpend(), 0.001, "Budgeted digital spend should be 200");
        assertEquals(300.0, calc.getBudgetedCashSpend(), 0.001, "Budgeted cash spend should be 300");
        assertEquals(500.0, calc.getBudgetedSpent(), 0.001, "Total budgeted spent should be 200 + 300 = 500");
        assertEquals(500.0, calc.getMonthlyRemaining(), 0.001, "Remaining pool should be 1000 - 500 = 500");
        assertEquals(400.0, calc.getUnbudgetedSpend(), 0.001, "Unbudgeted spend should only be unbudgeted cash (400)");

        int daysLeft = calc.getDaysLeft();
        assertEquals(500.0 / daysLeft, calc.getDailyAllowance(), 0.001);
    }

    /** Simple stub for CashDAO to provide controlled test fixtures. */
    private static class CashDAOStub implements CashDAO {
        private final List<CashEntry> entries;

        CashDAOStub(List<CashEntry> entries) {
            this.entries = entries;
        }

        @Override
        public List<CashEntry> findByUserIdAndMonth(int userId, int month, int year) {
            return entries;
        }

        @Override public boolean save(CashEntry entry) { return false; }
        @Override public CashEntry findById(int id) { return null; }
        @Override public List<CashEntry> findByUserId(int userId) { return entries; }
        @Override public boolean update(CashEntry entry) { return false; }
        @Override public boolean delete(int id, int userId) { return false; }
        @Override public boolean setCashInHand(int userId, double amount) { return false; }
        @Override public double getCashInHand(int userId) { return 0.0; }
        @Override public double getTotalSpentByMonth(int userId, int month, int year) { return 0.0; }
        @Override public double getTotalSpentAllTime(int userId) { return 0.0; }
    }
}

package com.myfinance.service;

import com.myfinance.dao.interfaces.BudgetDAO;
import com.myfinance.dao.interfaces.ExpenseDAO;
import com.myfinance.model.Budget;
import com.myfinance.model.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BudgetService Smart Alerts & Spend Guard Tests (Innovation 1)")
class BudgetServiceAlertTest {

    static class MockBudgetDAO implements BudgetDAO {
        private Budget mockBudget;

        public MockBudgetDAO(Budget mockBudget) {
            this.mockBudget = mockBudget;
        }

        @Override public boolean save(Budget budget) { return true; }
        @Override public Budget findById(int id) { return mockBudget; }
        @Override public Budget findByIdAndUserId(int id, int userId) { return mockBudget; }
        @Override public List<Budget> findByUserId(int userId) { return List.of(mockBudget); }
        @Override public Budget findByUserIdAndCategory(int userId, String category) { return mockBudget; }
        @Override public Budget findActiveByUserIdAndCategory(int userId, String category, LocalDate currentDate) { return mockBudget; }
        @Override public Budget findActiveByUserIdAndCategoryAndPeriod(int userId, String category, int month, int year) { return mockBudget; }
        @Override public List<Budget> findOverBudget(int userId) { return List.of(); }
        @Override public List<Budget> findAll() { return List.of(); }
        @Override public boolean update(Budget budget) { return true; }
        @Override public boolean updateSpentAmount(int budgetId, double newSpentAmount) { return true; }
        @Override public boolean recalculateSpentAmount(int userId, String category, LocalDate startDate, LocalDate endDate) { return true; }
        @Override public boolean recalculateSpentAmountForMonth(int userId, String category, int month, int year) { return true; }
        @Override public void fixExistingBudgetsAndExpenses() {}
        @Override public boolean delete(int id, int userId) { return true; }
    }

    static class MockExpenseDAO implements ExpenseDAO {
        @Override public boolean save(com.myfinance.model.Expense expense) { return true; }
        @Override public com.myfinance.model.Expense findById(int id) { return null; }
        @Override public com.myfinance.model.Expense findByIdAndUserId(int id, int userId) { return null; }
        @Override public List<com.myfinance.model.Expense> findByUserId(int userId) { return List.of(); }
        @Override public List<com.myfinance.model.Expense> findByUserIdAndMonth(int userId, int month, int year) { return List.of(); }
        @Override public List<com.myfinance.model.Expense> findByUserIdAndCategory(int userId, String category) { return List.of(); }
        @Override public List<com.myfinance.model.Expense> findRecentByUserId(int userId, int limit) { return List.of(); }
        @Override public List<com.myfinance.model.Expense> filterExpenses(int userId, Integer month, Integer year, String category, String searchQuery) { return List.of(); }
        @Override public double getSumByUserIdAndMonth(int userId, int month, int year) { return 0; }
        @Override public double getTotalSpentByUserId(int userId) { return 0; }
        @Override public java.util.Map<String, Double> getCategoryTotalsByUserIdAndMonth(int userId, int month, int year) { return java.util.Map.of(); }
        @Override public java.util.Map<String, Double> getAllTimeCategoryTotalsByUserId(int userId) { return java.util.Map.of(); }
        @Override public List<com.myfinance.model.Expense> findAll() { return List.of(); }
        @Override public boolean update(com.myfinance.model.Expense expense) { return true; }
        @Override public boolean delete(int id, int userId) { return true; }
        @Override public int countAllExpenses() { return 0; }
        @Override public java.util.Map<String, Object> getPersonalityAggregates(int userId, int days) { return java.util.Map.of(); }
        @Override public int countDistinctExpenseDays(int userId, int days) { return 0; }
    }

    @Test
    @DisplayName("Smart Alert triggers warning at 70%, critical at 90%, and over-limit at 100%")
    void testBudgetAlertThresholds() {
        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate end = LocalDate.now().plusDays(25);

        // Case 1: 72% used -> Notice alert
        Budget b70 = new Budget(1, 1, Category.FOOD, 10000.0, 7200.0, start, end, null);
        BudgetService service70 = new BudgetService(new MockBudgetDAO(b70), new MockExpenseDAO());
        List<String> alerts70 = service70.checkAlerts(1);
        assertEquals(1, alerts70.size());
        assertTrue(alerts70.get(0).contains("72%"));

        // Case 2: 95% used -> Critical warning
        Budget b90 = new Budget(2, 1, Category.SHOPPING, 10000.0, 9500.0, start, end, null);
        BudgetService service90 = new BudgetService(new MockBudgetDAO(b90), new MockExpenseDAO());
        List<String> alerts90 = service90.checkAlerts(1);
        assertEquals(1, alerts90.size());
        assertTrue(alerts90.get(0).contains("critically close"));

        // Case 3: 110% used -> Over Limit Alert
        Budget b100 = new Budget(3, 1, Category.ENTERTAINMENT, 5000.0, 5500.0, start, end, null);
        BudgetService service100 = new BudgetService(new MockBudgetDAO(b100), new MockExpenseDAO());
        List<String> alerts100 = service100.checkAlerts(1);
        assertEquals(1, alerts100.size());
        assertTrue(alerts100.get(0).contains("OVER LIMIT"));
    }

    @Test
    @DisplayName("Spend Guard checkBeforeExpense: under limit, near limit (80%+), and over limit (>100%)")
    void testCheckBeforeExpenseThresholds() {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        // 1. Under Limit: Limit ₹250, Spent ₹50, adding ₹50 -> ₹100 of ₹250 (40% < 80%) -> null
        Budget budgetUnder = new Budget(1, 1, Category.FOOD, 250.0, 50.0, start, end, null);
        BudgetService serviceUnder = new BudgetService(new MockBudgetDAO(budgetUnder), new MockExpenseDAO());
        com.myfinance.model.SpendGuardAlert alertUnder = serviceUnder.checkBeforeExpense(1, "Food & Dining", 50.0);
        assertNull(alertUnder, "Prospective total under 80% should not trigger an alert");

        // 2. Near Limit: Limit ₹250, Spent ₹150, adding ₹50 -> ₹200 of ₹250 (80%) -> "near limit"
        Budget budgetNear = new Budget(2, 1, Category.FOOD, 250.0, 150.0, start, end, null);
        BudgetService serviceNear = new BudgetService(new MockBudgetDAO(budgetNear), new MockExpenseDAO());
        com.myfinance.model.SpendGuardAlert alertNear = serviceNear.checkBeforeExpense(1, "Food", 50.0); // Also verifies "Food" -> "Food & Dining"
        assertNotNull(alertNear, "Prospective total at 80% should trigger near limit alert");
        assertEquals(com.myfinance.model.SpendGuardAlert.LEVEL_NEAR_LIMIT, alertNear.getLevel());
        assertTrue(alertNear.isNearLimit());
        assertFalse(alertNear.isOverLimit());
        assertEquals("₹150 spent + ₹50 new = ₹200 of ₹250 limit", alertNear.getFormula());
        assertTrue(alertNear.getMessage().contains("₹150 spent + ₹50 new = ₹200 of ₹250 limit"));

        // 3. Over Limit: Limit ₹250, Spent ₹250, adding ₹250 -> ₹500 of ₹250 (200% > 100%) -> "over limit"
        Budget budgetOver = new Budget(3, 1, Category.FOOD, 250.0, 250.0, start, end, null);
        BudgetService serviceOver = new BudgetService(new MockBudgetDAO(budgetOver), new MockExpenseDAO());
        com.myfinance.model.SpendGuardAlert alertOver = serviceOver.checkBeforeExpense(1, "Food & Dining", 250.0);
        assertNotNull(alertOver, "Prospective total over 100% should trigger over limit alert");
        assertEquals(com.myfinance.model.SpendGuardAlert.LEVEL_OVER_LIMIT, alertOver.getLevel());
        assertTrue(alertOver.isOverLimit());
        assertFalse(alertOver.isNearLimit());
        assertEquals("₹250 spent + ₹250 new = ₹500 of ₹250 limit", alertOver.getFormula());
        assertTrue(alertOver.getMessage().contains("Oops, you've gone over 🙈"));
        assertTrue(alertOver.getMessage().contains("₹250 spent + ₹250 new = ₹500 of ₹250 limit"));
    }

    @Test
    @DisplayName("Spend Guard legacy helper checkSpendGuard produces alert strings")
    void testLegacyCheckSpendGuard() {
        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate end = LocalDate.now().plusDays(25);

        Budget budget = new Budget(1, 1, Category.FOOD, 250.0, 250.0, start, end, null);
        BudgetService service = new BudgetService(new MockBudgetDAO(budget), new MockExpenseDAO());

        String warning = service.checkSpendGuard(1, Category.FOOD, 250.0);
        assertNotNull(warning);
        assertTrue(warning.contains("Oops, you've gone over 🙈"));
        assertTrue(warning.contains("₹250 spent + ₹250 new = ₹500 of ₹250 limit"));
    }
}

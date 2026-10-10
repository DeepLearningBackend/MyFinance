package com.myfinance.service;

import com.myfinance.dao.interfaces.BudgetDAO;
import com.myfinance.dao.interfaces.CashDAO;
import com.myfinance.dao.interfaces.ExpenseDAO;
import com.myfinance.dao.interfaces.GoalDAO;
import com.myfinance.model.Budget;
import com.myfinance.model.CashEntry;
import com.myfinance.model.Category;
import com.myfinance.model.Goal;
import com.myfinance.service.FinCoachService.FinCoachResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FinCoachServiceTest {

    private TestableFinCoachService testableService;

    // Testable subclass overriding executeGeminiCall to capture prompts and mock responses
    static class TestableFinCoachService extends FinCoachService {
        int geminiCallCount = 0;
        String lastCapturedPayload = null;
        boolean simulateGeminiConfigured = true;
        boolean throwOnExecute = false;

        TestableFinCoachService(ExpenseDAO expenseDAO, BudgetDAO budgetDAO, GoalDAO goalDAO, CashDAO cashDAO) {
            super(expenseDAO, budgetDAO, goalDAO, cashDAO, new AllowanceService(), new InsightService());
        }

        @Override
        public boolean isGeminiConfigured() {
            return simulateGeminiConfigured;
        }

        @Override
        protected String getGeminiApiKey() {
            return simulateGeminiConfigured ? "mock-gemini-key" : null;
        }

        @Override
        protected String executeGeminiCall(String url, String jsonPayload) throws Exception {
            geminiCallCount++;
            lastCapturedPayload = jsonPayload;
            if (throwOnExecute) {
                throw new RuntimeException("Simulated HTTP connection timeout / 503 Service Unavailable");
            }
            return "{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"A Systematic Investment Plan (SIP) allows investing fixed amounts regularly. It harnesses rupee cost averaging and compounding.\"}]}}]}";
        }
    }

    // Lightweight mock implementations
    static class MockExpenseDAO implements ExpenseDAO {
        @Override public boolean save(com.myfinance.model.Expense expense) { return true; }
        @Override public com.myfinance.model.Expense findById(int id) { return null; }
        @Override public com.myfinance.model.Expense findByIdAndUserId(int id, int userId) { return null; }
        @Override public List<com.myfinance.model.Expense> findByUserId(int userId) { return List.of(); }
        @Override public List<com.myfinance.model.Expense> findByUserIdAndMonth(int userId, int month, int year) { return List.of(); }
        @Override public List<com.myfinance.model.Expense> findByUserIdAndCategory(int userId, String category) { return List.of(); }
        @Override public List<com.myfinance.model.Expense> findRecentByUserId(int userId, int limit) { return List.of(); }
        @Override public List<com.myfinance.model.Expense> filterExpenses(int userId, Integer month, Integer year, String category, String searchQuery) { return List.of(); }
        @Override public double getSumByUserIdAndMonth(int userId, int month, int year) { return 12000.0; }
        @Override public double getTotalSpentByUserId(int userId) { return 12000.0; }
        @Override public Map<String, Double> getCategoryTotalsByUserIdAndMonth(int userId, int month, int year) {
            return Map.of(Category.FOOD, 5000.0, Category.SHOPPING, 4000.0, Category.TRANSPORT, 3000.0);
        }
        @Override public Map<String, Double> getAllTimeCategoryTotalsByUserId(int userId) { return Map.of(); }
        @Override public List<com.myfinance.model.Expense> findAll() { return List.of(); }
        @Override public boolean update(com.myfinance.model.Expense expense) { return true; }
        @Override public boolean delete(int id, int userId) { return true; }
        @Override public int countAllExpenses() { return 10; }
        @Override public Map<String, Object> getPersonalityAggregates(int userId, int days) { return Map.of(); }
        @Override public int countDistinctExpenseDays(int userId, int days) { return 5; }
    }

    static class MockBudgetDAO implements BudgetDAO {
        @Override public boolean save(Budget budget) { return true; }
        @Override public Budget findById(int id) { return null; }
        @Override public Budget findByIdAndUserId(int id, int userId) { return null; }
        @Override public List<Budget> findByUserId(int userId) {
            return List.of(new Budget(1, userId, Category.FOOD, 8000.0, 5000.0, LocalDate.now().minusDays(5), LocalDate.now().plusDays(25), null));
        }
        @Override public Budget findByUserIdAndCategory(int userId, String category) { return null; }
        @Override public Budget findActiveByUserIdAndCategory(int userId, String category, LocalDate currentDate) { return null; }
        @Override public List<Budget> findOverBudget(int userId) { return List.of(); }
        @Override public List<Budget> findAll() { return List.of(); }
        @Override public boolean update(Budget budget) { return true; }
        @Override public boolean updateSpentAmount(int budgetId, double newSpentAmount) { return true; }
        @Override public boolean recalculateSpentAmount(int userId, String category, LocalDate startDate, LocalDate endDate) { return true; }
        @Override public Budget findActiveByUserIdAndCategoryAndPeriod(int userId, String category, int month, int year) { return null; }
        @Override public boolean recalculateSpentAmountForMonth(int userId, String category, int month, int year) { return true; }
        @Override public void fixExistingBudgetsAndExpenses() {}
        @Override public boolean delete(int id, int userId) { return true; }
    }

    static class MockGoalDAO implements GoalDAO {
        @Override public boolean save(Goal goal) { return true; }
        @Override public Goal findById(int id) { return null; }
        @Override public Goal findByIdAndUserId(int id, int userId) { return null; }
        @Override public List<Goal> findByUserId(int userId) {
            return List.of(new Goal(1, userId, "Vacation", "Trip", 20000.0, 10000.0, LocalDate.now().plusDays(30), Goal.GoalStatus.IN_PROGRESS, null));
        }
        @Override public List<Goal> findByStatus(int userId, Goal.GoalStatus status) { return List.of(); }
        @Override public List<Goal> findAll() { return List.of(); }
        @Override public boolean update(Goal goal) { return true; }
        @Override public boolean updateSavedAmount(int goalId, int userId, double additionalAmount) { return true; }
        @Override public boolean markAsAchieved(int goalId, int userId) { return true; }
        @Override public boolean delete(int id, int userId) { return true; }
    }

    static class MockCashDAO implements CashDAO {
        @Override public boolean save(CashEntry entry) { return true; }
        @Override public CashEntry findById(int id) { return null; }
        @Override public List<CashEntry> findByUserId(int userId) { return List.of(); }
        @Override public List<CashEntry> findByUserIdAndMonth(int userId, int month, int year) { return List.of(); }
        @Override public boolean update(CashEntry entry) { return true; }
        @Override public boolean delete(int id, int userId) { return true; }
        @Override public boolean setCashInHand(int userId, double amount) { return true; }
        @Override public double getCashInHand(int userId) { return 3500.0; }
        @Override public double getTotalSpentByMonth(int userId, int month, int year) { return 1500.0; }
        @Override public double getTotalSpentAllTime(int userId) { return 1500.0; }
    }

    @BeforeEach
    void setUp() {
        testableService = new TestableFinCoachService(
                new MockExpenseDAO(),
                new MockBudgetDAO(),
                new MockGoalDAO(),
                new MockCashDAO()
        );
    }

    @Test
    @DisplayName("Rule 7: Personal finance questions NEVER trigger a Gemini call")
    void testPersonalQuestionsNeverCallGemini() {
        String[] personalQueries = {
            "How much did I spend this month?",
            "What is my budget for Food?",
            "How are my goals doing?",
            "How much cash in hand do I have?",
            "Can I afford dinner today?",
            "What is my spending personality?"
        };

        for (String query : personalQueries) {
            FinCoachResponse resp = testableService.ask(1, query);
            assertNotNull(resp);
            assertEquals(FinCoachService.SOURCE_USER_DATA, resp.getSource(), "Query '" + query + "' must be labeled as '📊 Your data'");
            assertEquals("rule-based", resp.getMode());
            assertTrue(resp.getAnswer().contains(FinCoachService.SEBI_DISCLAIMER));
        }

        // Assert 0 calls to Gemini were made
        assertEquals(0, testableService.geminiCallCount, "Personal finance questions must NEVER trigger Gemini calls");
    }

    @Test
    @DisplayName("Rule 7: General education queries call Gemini with ZERO user numbers in the prompt")
    void testEducationalQueryContainsZeroUserNumbersInPrompt() {
        FinCoachResponse resp = testableService.ask(1, "What is SIP and how does compound interest work?");
        assertNotNull(resp);

        // Assert Gemini was called
        assertEquals(1, testableService.geminiCallCount, "General educational query should route to Gemini");
        assertEquals(FinCoachService.SOURCE_AI, resp.getSource());
        assertEquals("gemini", resp.getMode());
        assertTrue(resp.getAnswer().contains(FinCoachService.SEBI_DISCLAIMER));

        // Inspect captured payload sent to Gemini
        String payload = testableService.lastCapturedPayload;
        assertNotNull(payload);

        // Assert ZERO user amounts or database context are in the prompt
        assertFalse(payload.contains("12000"), "Payload must not contain user digital spend");
        assertFalse(payload.contains("1500"), "Payload must not contain user cash spend");
        assertFalse(payload.contains("3500"), "Payload must not contain user cash in hand");
        assertFalse(payload.contains("8000"), "Payload must not contain user budget limit");
        assertFalse(payload.contains("5000"), "Payload must not contain user budget spent");
        assertFalse(payload.contains("20000"), "Payload must not contain user goal target");
        assertFalse(payload.contains("10000"), "Payload must not contain user goal saved");
        assertFalse(payload.contains("User Financial Context"), "Payload must not contain user financial context");
        assertFalse(payload.contains("Digital Spend"), "Payload must not contain digital spend context");
        assertFalse(payload.contains("Category Breakdown"), "Payload must not contain category breakdown");

        // Assert strict educational system prompt is present
        assertTrue(payload.contains("Financial EDUCATION only"));
        assertTrue(payload.contains("NEVER provide specific investment advice"));
        assertTrue(payload.contains(FinCoachService.SEBI_DISCLAIMER));
        assertTrue(payload.contains("What is SIP"));
    }

    @Test
    @DisplayName("Rule 7: Question containing user rupee amount is routed to rule-based or stripped")
    void testRupeeAmountsAreNotSentToGemini() {
        FinCoachResponse resp = testableService.ask(1, "Can I spend ₹5000 today from my budget?");
        assertNotNull(resp);
        // Rupee amounts / budget query must be routed to rule-based
        assertEquals(0, testableService.geminiCallCount);
        assertEquals(FinCoachService.SOURCE_USER_DATA, resp.getSource());
    }

    @Test
    @DisplayName("Fallback: Gemini HTTP failure gracefully falls back to rule-based with zero crash")
    void testGeminiFailureFallsBackGracefully() {
        testableService.throwOnExecute = true;

        assertDoesNotThrow(() -> {
            FinCoachResponse resp = testableService.ask(1, "Explain what an emergency fund is");
            assertNotNull(resp);
            assertEquals(FinCoachService.SOURCE_USER_DATA, resp.getSource());
            assertTrue(resp.getAnswer().contains(FinCoachService.SEBI_DISCLAIMER));
        });
    }

    @Test
    @DisplayName("Rule-based mode provides calculation sources for every user number")
    void testRuleBasedCalculationSources() {
        FinCoachResponse resp = testableService.answerRuleBased(1, "How much did I spend this month?");
        assertNotNull(resp);
        assertFalse(resp.getCalculationSources().isEmpty());
        assertTrue(resp.getAnswer().contains("Calculation Source:"));
    }
}

package com.myfinance.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Model Entities & BaseEntity Polymorphism Tests")
class ModelEntityTest {

    @Test
    @DisplayName("User validation enforces email pattern, non-empty names, and default API tokens")
    void testUserValidation() {
        User validUser = new User("Jane Doe", "jane@example.com", "pass123", User.Role.USER, User.Status.ACTIVE);
        assertTrue(validUser.validate());
        assertNotNull(validUser.getApiToken(), "User should have an auto-generated API token");
        assertTrue(validUser.getApiToken().startsWith("myf_"));

        User invalidEmail = new User("Jane Doe", "invalid-email-address", "pass123", User.Role.USER, User.Status.ACTIVE);
        assertFalse(invalidEmail.validate());

        User blankName = new User("   ", "jane@example.com", "pass123", User.Role.USER, User.Status.ACTIVE);
        assertFalse(blankName.validate());
    }

    @Test
    @DisplayName("Expense validation requires positive amount, title, and valid date")
    void testExpenseValidation() {
        Expense validExp = new Expense(1, "Swiggy Dinner", 450.0, Category.FOOD, LocalDate.now(), "Dinner with team");
        assertTrue(validExp.validate());
        assertEquals("MANUAL", validExp.getSource());

        Expense smsExp = new Expense(1, "Zomato", 320.0, Category.FOOD, LocalDate.now(), "SMS_SYNC", "UPI987654", "Auto parsed");
        assertTrue(smsExp.validate());
        assertEquals("SMS_SYNC", smsExp.getSource());
        assertEquals("UPI987654", smsExp.getUpiRef());

        Expense zeroAmount = new Expense(1, "Swiggy Dinner", 0.0, Category.FOOD, LocalDate.now(), null);
        assertFalse(zeroAmount.validate());

        Expense negativeAmount = new Expense(1, "Swiggy Dinner", -100.0, Category.FOOD, LocalDate.now(), null);
        assertFalse(negativeAmount.validate());

        Expense blankTitle = new Expense(1, "  ", 450.0, Category.FOOD, LocalDate.now(), null);
        assertFalse(blankTitle.validate());
    }

    @Test
    @DisplayName("Budget calculates usage percentage and remaining balance accurately")
    void testBudgetCalculations() {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        Budget budget = new Budget(1, Category.FOOD, 10000.0, 7500.0, start, end);
        assertTrue(budget.validate());
        assertEquals(75.0, budget.getUsagePercent(), 0.001);
        assertEquals(2500.0, budget.getRemainingAmount(), 0.001);
        assertFalse(budget.isOverBudget());

        budget.setSpentAmount(11200.0);
        assertEquals(112.0, budget.getUsagePercent(), 0.001);
        assertEquals(-1200.0, budget.getRemainingAmount(), 0.001);
        assertTrue(budget.isOverBudget());
    }

    @Test
    @DisplayName("Goal progress percent and milestone calculations work correctly")
    void testGoalCalculations() {
        LocalDate deadline = LocalDate.now().plusMonths(3);
        Goal goal = new Goal(1, "MacBook Pro M3", "Savings for dev machine", 150000.0, 75000.0, deadline, Goal.GoalStatus.IN_PROGRESS);

        assertTrue(goal.validate());
        assertEquals(50.0, goal.getProgressPercent(), 0.001);
        assertEquals(75000.0, goal.getRemainingAmount(), 0.001);
        assertTrue(goal.getDaysLeft() > 0);

        goal.setSavedAmount(150000.0);
        assertEquals(100.0, goal.getProgressPercent(), 0.001);
        assertEquals(0.0, goal.getRemainingAmount(), 0.001);
    }

    @Test
    @DisplayName("CashEntry validation enforces positive cash amount and category")
    void testCashEntryValidation() {
        CashEntry cash = new CashEntry(1, 150.0, Category.FOOD, LocalDate.now(), "Chai and snacks");
        assertTrue(cash.validate());

        CashEntry invalidCash = new CashEntry(1, -50.0, Category.FOOD, LocalDate.now(), "Negative");
        assertFalse(invalidCash.validate());
    }

    @Test
    @DisplayName("SmsQueueItem and SpendingPersonality validate correctly")
    void testSmsQueueAndPersonality() {
        SmsQueueItem sms = new SmsQueueItem(1, "Rs 500 debited to Swiggy", 500.0, "Swiggy", "UPI1234");
        assertTrue(sms.validate());
        assertEquals(SmsQueueItem.Status.PENDING, sms.getStatus());

        SpendingPersonality personality = new SpendingPersonality(1, "THE STRATEGIST", "High savings rate", "Consistently saves >30%", 10, 2026);
        assertTrue(personality.validate());
    }

    @Test
    @DisplayName("Polymorphism: all models extend BaseEntity and implement validate()")
    void testBaseEntityPolymorphism() {
        BaseEntity[] entities = {
            new User("Alice", "alice@example.com", "pass", User.Role.USER, User.Status.ACTIVE),
            new Expense(1, "Book", 299.0, Category.EDUCATION, LocalDate.now(), "Study"),
            new Budget(1, Category.FOOD, 5000.0, 1000.0, LocalDate.now(), LocalDate.now().plusMonths(1)),
            new Goal(1, "Emergency Fund", "Savings", 50000.0, 10000.0, LocalDate.now().plusMonths(6), Goal.GoalStatus.IN_PROGRESS),
            new CashEntry(1, 100.0, Category.TRANSPORT, LocalDate.now(), "Auto rickshaw"),
            new SmsQueueItem(1, "Raw SMS", 100.0, "Uber", "UPI777"),
            new SpendingPersonality(1, "MINIMALIST", "Low spend", "Disciplined", 10, 2026)
        };

        for (BaseEntity e : entities) {
            assertNotNull(e.getCreatedAt());
            assertTrue(e.validate(), e.getClass().getSimpleName() + " should validate to true");
        }
    }
}

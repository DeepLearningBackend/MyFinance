package com.myfinance.service;

import com.myfinance.model.Goal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Goal Motivation Engine Milestone & Badge Tests (Innovation 2)")
class GoalServiceTest {

    private final GoalService goalService = new GoalService();

    @Test
    @DisplayName("Milestone badges transition through 0%, 25%, 50%, 75%, and 100%")
    void testMilestoneBadges() {
        LocalDate future = LocalDate.now().plusMonths(6);
        Goal goal = new Goal(1, "Goa Trip", "Vacation", 10000.0, 0.0, future, Goal.GoalStatus.IN_PROGRESS);

        // 0% -> First Steps
        Map<String, Object> details0 = goalService.getProgressDetails(goal);
        assertEquals("SEED", details0.get("milestoneLevel"));
        assertTrue(details0.get("milestoneBadge").toString().contains("First Steps"));

        // 25% -> Bronze
        goal.setSavedAmount(2500.0);
        Map<String, Object> details25 = goalService.getProgressDetails(goal);
        assertEquals("BRONZE", details25.get("milestoneLevel"));
        assertTrue(details25.get("milestoneBadge").toString().contains("25%"));

        // 50% -> Silver
        goal.setSavedAmount(5000.0);
        Map<String, Object> details50 = goalService.getProgressDetails(goal);
        assertEquals("SILVER", details50.get("milestoneLevel"));
        assertTrue(details50.get("milestoneBadge").toString().contains("Halfway Hero"));

        // 75% -> Gold
        goal.setSavedAmount(7500.0);
        Map<String, Object> details75 = goalService.getProgressDetails(goal);
        assertEquals("GOLD", details75.get("milestoneLevel"));
        assertTrue(details75.get("milestoneBadge").toString().contains("75%"));

        // 100% -> Master
        goal.setSavedAmount(10000.0);
        Map<String, Object> details100 = goalService.getProgressDetails(goal);
        assertEquals("MASTER", details100.get("milestoneLevel"));
        assertTrue(details100.get("milestoneBadge").toString().contains("Goal Master"));
        assertTrue((Boolean) details100.get("isAchieved"));
    }
}

package com.myfinance.service;

import com.myfinance.model.PersonalityReport;

import java.util.HashMap;
import java.util.Map;

/**
 * Pure scoring engine for the Spending Personality feature (Innovation 6).
 *
 * <p>Accepts the raw aggregates produced by
 * {@link com.myfinance.dao.impl.ExpenseDAOImpl#getPersonalityAggregates} and
 * converts them into a {@link PersonalityReport} by applying weighted rules:
 *
 * <pre>
 * Rule weight table:
 *   Food        ≥ 35 % of spend  → +40 Foodie
 *   Shopping    ≥ 30 % of spend  → +40 Impulsive Shopper
 *   Transport   ≥ 25 % of spend  → +35 Commuter
 *   Travel      ≥ 20 % of spend  → +50 Explorer
 *   Entertainment≥ 20 %          → +35 Experience Seeker
 *   Health      ≥ 20 %           → +35 Wellness Warrior
 *   Weekend %   ≥ 55 % of spend  → +20 Weekend Warrior (stacks)
 *   Highest category             → base score of 10
 * </pre>
 *
 * The category with the highest total accumulated score wins.
 * All intermediate scores are stored in the HashMap kept inside
 * {@link PersonalityReport#getScores()} for the JSP transparency panel.
 */
public class PersonalityDetector {

    // Personality definitions: type → [primaryTrait, description, emoji, colorAccent]
    private static final Map<String, String[]> DEFINITIONS = new HashMap<>();

    static {
        DEFINITIONS.put("Foodie", new String[]{
            "You live to eat 🍕",
            "Food and dining dominate your budget. Whether it's Swiggy at midnight or a weekend brunch, " +
            "you prioritise experiences on the plate. Your spending shows a consistent pattern of food-first thinking. " +
            "Consider meal-prepping on weekdays to save without sacrificing quality.",
            "🍕", "#f97316"
        });
        DEFINITIONS.put("Impulsive Shopper", new String[]{
            "Retail therapy is your cardio 🛍️",
            "Shopping is your biggest expense category. You're quick to add to cart and often buy on impulse. " +
            "Your weekend spending spikes suggest emotional buying patterns. " +
            "Try the 24-hour rule: wait a day before any non-essential purchase over ₹500.",
            "🛍️", "#ec4899"
        });
        DEFINITIONS.put("Commuter", new String[]{
            "Life is a journey — especially yours 🚌",
            "A significant chunk of your money goes to getting around. Cab rides, fuel, metro passes — " +
            "transport is the backbone of your daily spend. Look into monthly passes or carpooling to cut this down.",
            "🚌", "#3b82f6"
        });
        DEFINITIONS.put("Explorer", new String[]{
            "Wanderlust on a budget ✈️",
            "Travel and experiences take the top slot in your spending. You invest in memories over material things. " +
            "Your spending bursts correlate with weekends and holidays. " +
            "Keep a travel fund goal active so your adventures stay within plan.",
            "✈️", "#8b5cf6"
        });
        DEFINITIONS.put("Experience Seeker", new String[]{
            "Every weekend is an event 🎭",
            "Entertainment — movies, concerts, dining-out — drives your spending. " +
            "You value experiences and social activities above saving. " +
            "Try a monthly entertainment cap budget to keep the fun without the guilt.",
            "🎭", "#f59e0b"
        });
        DEFINITIONS.put("Wellness Warrior", new String[]{
            "Investing in health first 💪",
            "Health and wellness consistently top your spend categories — gym, pharmacy, medical visits. " +
            "You're building long-term capital in yourself. " +
            "Make sure your health expenses are planned for in a dedicated budget line.",
            "💪", "#10b981"
        });
        DEFINITIONS.put("Planner", new String[]{
            "Balanced and intentional 📋",
            "Your spending is spread evenly across categories with no single dominant area. " +
            "This is the mark of a disciplined, goal-oriented spender. " +
            "You are already doing great — keep using budgets to maintain your balance.",
            "📋", "#6366f1"
        });
    }

    /**
     * Runs the scoring algorithm and produces a full {@link PersonalityReport}.
     *
     * @param userId           authenticated user
     * @param aggregates       raw data from {@code ExpenseDAO.getPersonalityAggregates()}
     * @param dataPoints       distinct spending days in the window
     * @param month            analysis month
     * @param year             analysis year
     * @return fully-populated PersonalityReport
     */
    @SuppressWarnings("unchecked")
    public PersonalityReport detect(int userId,
                                    Map<String, Object> aggregates,
                                    int dataPoints,
                                    int month,
                                    int year) {

        Map<String, Double> categoryTotals =
                (Map<String, Double>) aggregates.getOrDefault("category_totals", new HashMap<>());
        double weekendTotal  = toDouble(aggregates.get("weekend_total"));
        double weekdayTotal  = toDouble(aggregates.get("weekday_total"));
        int    totalExpenses = toInt(aggregates.get("total_expenses"));

        double grandTotal = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();
        if (grandTotal <= 0) {
            return buildDefault(userId, month, year, totalExpenses, dataPoints);
        }

        // ── Score accumulation into HashMap ───────────────────────────────
        Map<String, Integer> scores = new HashMap<>();
        scores.put("Foodie", 0);
        scores.put("Impulsive Shopper", 0);
        scores.put("Commuter", 0);
        scores.put("Explorer", 0);
        scores.put("Experience Seeker", 0);
        scores.put("Wellness Warrior", 0);
        scores.put("Planner", 10); // base score — wins if all else is balanced

        String topCategory = "";
        double topCategoryAmount = 0;

        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            String cat    = entry.getKey();
            double amount = entry.getValue();
            double pct    = (amount / grandTotal) * 100.0;

            if (amount > topCategoryAmount) {
                topCategoryAmount = amount;
                topCategory       = cat;
            }

            switch (cat) {
                case "Food & Dining":
                case "Food":
                    if (pct >= 35) scores.merge("Foodie", 40, Integer::sum);
                    else if (pct >= 20) scores.merge("Foodie", 20, Integer::sum);
                    break;
                case "Shopping":
                    if (pct >= 30) scores.merge("Impulsive Shopper", 40, Integer::sum);
                    else if (pct >= 15) scores.merge("Impulsive Shopper", 15, Integer::sum);
                    break;
                case "Transport":
                    if (pct >= 25) scores.merge("Commuter", 35, Integer::sum);
                    else if (pct >= 12) scores.merge("Commuter", 15, Integer::sum);
                    break;
                case "Travel":
                    if (pct >= 20) scores.merge("Explorer", 50, Integer::sum);
                    else if (pct >= 10) scores.merge("Explorer", 20, Integer::sum);
                    break;
                case "Entertainment":
                    if (pct >= 20) scores.merge("Experience Seeker", 35, Integer::sum);
                    else if (pct >= 10) scores.merge("Experience Seeker", 15, Integer::sum);
                    break;
                case "Health & Fitness":
                case "Health":
                    if (pct >= 20) scores.merge("Wellness Warrior", 35, Integer::sum);
                    else if (pct >= 10) scores.merge("Wellness Warrior", 15, Integer::sum);
                    break;
                default:
                    break;
            }
        }

        // Weekend bonus: stacks on the current leader
        double totalForDow = weekendTotal + weekdayTotal;
        if (totalForDow > 0) {
            double weekendPct = (weekendTotal / totalForDow) * 100.0;
            if (weekendPct >= 55) {
                // Add weekend bonus to the current highest scorer
                String leader = scores.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey)
                        .orElse("Planner");
                scores.merge(leader, 20, Integer::sum);
            }
        }

        // ── Pick winner ───────────────────────────────────────────────────
        String winner = scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Planner");

        String[] def = DEFINITIONS.getOrDefault(winner, DEFINITIONS.get("Planner"));

        double topPct = grandTotal > 0 ? (topCategoryAmount / grandTotal) * 100.0 : 0;

        // Build formula string (transparency)
        StringBuilder formulaSb = new StringBuilder();
        scores.forEach((k, v) -> {
            if (v > 0) {
                if (formulaSb.length() > 0) formulaSb.append(" + ");
                formulaSb.append(k).append(" ").append(v).append("pt");
            }
        });
        formulaSb.append(" → ").append(winner);

        return new PersonalityReport(
                userId, winner, def[0], def[1], def[2], def[3],
                scores, topCategory, topPct, totalExpenses, dataPoints,
                month, year, formulaSb.toString()
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private PersonalityReport buildDefault(int userId, int month, int year,
                                            int totalExpenses, int dataPoints) {
        String[] def = DEFINITIONS.get("Planner");
        Map<String, Integer> scores = new HashMap<>();
        scores.put("Planner", 10);
        return new PersonalityReport(
                userId, "Planner", def[0], def[1], def[2], def[3],
                scores, "—", 0, totalExpenses, dataPoints,
                month, year, "No dominant category → Planner"
        );
    }

    private static double toDouble(Object o) {
        if (o instanceof Number) return ((Number) o).doubleValue();
        return 0.0;
    }

    private static int toInt(Object o) {
        if (o instanceof Number) return ((Number) o).intValue();
        return 0;
    }
}

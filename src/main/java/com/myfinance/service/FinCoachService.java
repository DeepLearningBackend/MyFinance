package com.myfinance.service;

import com.myfinance.dao.impl.BudgetDAOImpl;
import com.myfinance.dao.impl.CashDAOImpl;
import com.myfinance.dao.impl.ExpenseDAOImpl;
import com.myfinance.dao.impl.GoalDAOImpl;
import com.myfinance.dao.interfaces.BudgetDAO;
import com.myfinance.dao.interfaces.CashDAO;
import com.myfinance.dao.interfaces.ExpenseDAO;
import com.myfinance.dao.interfaces.GoalDAO;
import com.myfinance.model.Budget;
import com.myfinance.model.Category;
import com.myfinance.model.Goal;
import com.myfinance.util.CategoryMapper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * FinCoach Engine (Innovations 4, 5 &amp; Golden Rules).
 *
 * <p>Compliance Architecture (Rule 7: AI is NEVER used for the user's personal numbers. DB only):
 * <ul>
 *   <li><b>Rule-based mode (DB only):</b> All questions regarding the user's personal finances
 *       (spending, balances, budgets, goals, cash in hand, daily allowance, personality) are
 *       evaluated strictly from database records. Every calculated number displays its source.
 *       Source label: <code>📊 Your data</code>. Gemini is NEVER called for these.</li>
 *   <li><b>Gemini mode (General education only):</b> Only conceptual financial literacy questions
 *       (e.g., What is SIP? How does compound interest work? How to build an emergency fund?)
 *       are routed to Google Gemini API. NO user amounts, balances, budgets, expense rows, goals,
 *       or allowances are ever included in the prompt. Source label: <code>🤖 AI</code>.</li>
 * </ul>
 *
 * <p>Compliance &amp; Golden Rules:
 * <ul>
 *   <li>Financial education only — never specific investment advice, individual stock tips, or buy/sell calls.</li>
 *   <li>Always appends mandatory SEBI educational disclaimer on every AI response.</li>
 *   <li>API key is read strictly from environment variable <code>GEMINI_API_KEY</code>, never hardcoded.</li>
 *   <li>Seamless fallback to rule-based mode if the key is missing or the call fails (no crash, no stack trace).</li>
 * </ul>
 */
public class FinCoachService {

    public static final String SEBI_DISCLAIMER =
            "⚠️ Disclaimer: Educational purposes only. Not SEBI registered investment advice. "
                    + "Consult a certified financial advisor before making investment decisions.";

    public static final String SOURCE_USER_DATA = "📊 Your data";
    public static final String SOURCE_AI        = "🤖 AI";

    private final ExpenseDAO expenseDAO;
    private final BudgetDAO budgetDAO;
    private final GoalDAO goalDAO;
    private final CashDAO cashDAO;
    private final AllowanceService allowanceService;
    private final InsightService insightService;

    private final HttpClient httpClient;

    /**
     * Default constructor initializing DAOs and helper services.
     */
    public FinCoachService() {
        this(new ExpenseDAOImpl(), new BudgetDAOImpl(), new GoalDAOImpl(), new CashDAOImpl(),
                new AllowanceService(), new InsightService());
    }

    /**
     * Dependency injection constructor for testing.
     *
     * @param expenseDAO expense DAO
     * @param budgetDAO budget DAO
     * @param goalDAO goal DAO
     * @param cashDAO cash DAO
     * @param allowanceService daily allowance service
     * @param insightService financial insight service
     */
    public FinCoachService(ExpenseDAO expenseDAO, BudgetDAO budgetDAO, GoalDAO goalDAO, CashDAO cashDAO,
                           AllowanceService allowanceService, InsightService insightService) {
        this.expenseDAO = expenseDAO;
        this.budgetDAO = budgetDAO;
        this.goalDAO = goalDAO;
        this.cashDAO = cashDAO;
        this.allowanceService = allowanceService;
        this.insightService = insightService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(6))
                .build();
    }

    /**
     * DTO containing FinCoach response details.
     */
    public static class FinCoachResponse {
        private final String answer;
        private final String source;
        private final String mode;
        private final String disclaimer;
        private final List<String> calculationSources;

        public FinCoachResponse(String answer, String source, String mode, String disclaimer, List<String> calculationSources) {
            this.answer = answer;
            this.source = source;
            this.mode = mode;
            this.disclaimer = disclaimer;
            this.calculationSources = calculationSources != null ? calculationSources : new ArrayList<>();
        }

        public String getAnswer() { return answer; }
        public String getSource() { return source; }
        public String getMode() { return mode; }
        public String getDisclaimer() { return disclaimer; }
        public List<String> getCalculationSources() { return calculationSources; }
    }

    /**
     * Check if Gemini API key is configured in the environment.
     */
    public boolean isGeminiConfigured() {
        String key = getGeminiApiKey();
        return key != null && !key.trim().isEmpty() && !key.equalsIgnoreCase("dummy");
    }

    /**
     * Retrieves the Gemini API key strictly from the GEMINI_API_KEY environment variable.
     */
    protected String getGeminiApiKey() {
        return System.getenv("GEMINI_API_KEY");
    }

    /**
     * Main entry point to answer a user question.
     * <p>
     * Strict Question Routing (Rule 7):
     * 1. If question is about user's own money: Answer rule-based from DB, label "📊 Your data", NEVER call Gemini.
     * 2. If general financial education: Call Gemini with system prompt + sanitized question, label "🤖 AI".
     * 3. If key is missing or call fails: Fall back safely to rule-based mode with zero stack trace.
     * </p>
     */
    public FinCoachResponse ask(int userId, String question) {
        String cleanQuestion = question != null ? question.trim() : "";
        if (cleanQuestion.isEmpty()) {
            cleanQuestion = "Give me a summary of my finances";
        }

        // Rule 7 Routing: Personal finance questions are answered exclusively from DB data
        if (isPersonalFinanceQuery(cleanQuestion)) {
            return answerRuleBased(userId, cleanQuestion);
        }

        // General financial education questions go to Gemini mode if configured
        if (isGeminiConfigured()) {
            try {
                String sanitized = sanitizeEducationalQuestion(cleanQuestion);
                FinCoachResponse aiResponse = callGeminiApi(sanitized);
                if (aiResponse != null && aiResponse.getAnswer() != null && !aiResponse.getAnswer().isBlank()) {
                    return aiResponse;
                }
            } catch (Exception e) {
                // Graceful fallback with no crash and no stack trace exposed to caller
            }
        }

        // Fallback for educational questions when Gemini key is missing or unavailable
        return answerRuleBased(userId, cleanQuestion);
    }

    /**
     * Identifies whether a question pertains to the user's personal financial state or database numbers.
     */
    public boolean isPersonalFinanceQuery(String question) {
        if (question == null || question.isBlank()) {
            return true;
        }
        String q = question.toLowerCase(Locale.ENGLISH).trim();

        // 1. Personal pronouns / possessives
        if (q.matches(".*\\b(my|mine|i|me|we|our|myself)\\b.*")) {
            return true;
        }

        // 2. Direct user personal state keywords
        if (q.contains("how much left") || q.contains("left today") || q.contains("allowance")
                || q.contains("can i afford") || q.contains("afford") || q.contains("cash in hand")
                || q.contains("today") || q.contains("yesterday") || q.contains("this month")
                || q.contains("last month") || q.contains("personality")
                || q.contains("summary") || q.contains("overview")
                || q.contains("recent") || q.contains("transaction") || q.contains("balance")) {
            return true;
        }

        // 3. Spending / budget / goal keywords
        if (q.matches(".*\\b(spend|spent|spending|expense|expenses|cost|costs|budget|budgets|goal|goals)\\b.*")) {
            return true;
        }

        // 4. Specific rupee amounts or account details in question
        if (containsUserFinancialDetails(question)) {
            return true;
        }

        // 5. Merchant lookups (e.g. Swiggy, Uber, Zomato) unless framed as pure definition
        String suggested = CategoryMapper.suggest(question);
        if (suggested != null && !Category.OTHER.equals(suggested)) {
            if (!q.contains("what is") && !q.contains("explain") && !q.contains("how does") && !q.contains("define")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if text contains explicit rupee symbols, currency indicators, or account numbers.
     */
    public boolean containsUserFinancialDetails(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase(Locale.ENGLISH);
        return lower.matches(".*(₹|rs\\.?|inr|rupee|rupees)\\s*\\d+.*")
                || lower.matches(".*\\b\\d{4,}\\b.*")
                || lower.matches(".*\\b(a/c|acc|account)\\s*#?\\d+.*");
    }

    /**
     * Strips any residual currency values from an educational question before sending to Gemini.
     */
    public String sanitizeEducationalQuestion(String text) {
        if (text == null) return "";
        return text.replaceAll("(?i)(₹|rs\\.?|inr)\\s*\\d+(?:,\\d+)*(?:\\.\\d+)?", "[amount]")
                   .replaceAll("\\b\\d{4,}\\b", "[number]");
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GEMINI AI MODE (General Education Only, Zero User Numbers, SEBI Disclaimer)
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Calls Gemini API for conceptual financial education only.
     * <p>
     * Rule 7 Compliance: Sends ONLY a strict educational system prompt + sanitized question.
     * ZERO user amounts, balances, budgets, expense rows, goals, or allowances are included.
     * </p>
     */
    protected FinCoachResponse callGeminiApi(String question) throws Exception {
        String apiKey = getGeminiApiKey();
        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equalsIgnoreCase("dummy")) {
            return null;
        }

        // STRICT SYSTEM PROMPT: Financial education only, never specific investment advice, always SEBI disclaimer.
        String systemPrompt = "You are FinCoach, a personal finance educational assistant for MyFinance.\n"
                + "CRITICAL COMPLIANCE RULES:\n"
                + "1. Financial EDUCATION only. Provide conceptual explanations of personal finance topics (e.g., SIP, compound interest, emergency funds, 50/30/20 rule, debt payoff, index funds).\n"
                + "2. NEVER provide specific investment advice, individual stock tips, crypto recommendations, or buy/sell calls.\n"
                + "3. You MUST end your response with this exact disclaimer:\n"
                + SEBI_DISCLAIMER;

        JSONObject contentObj = new JSONObject();
        JSONArray partsArr = new JSONArray();

        JSONObject textPart = new JSONObject();
        textPart.put("text", systemPrompt + "\n\nUser Question: " + question);
        partsArr.put(textPart);
        contentObj.put("parts", partsArr);

        JSONObject requestBody = new JSONObject();
        JSONArray contentsArr = new JSONArray();
        contentsArr.put(contentObj);
        requestBody.put("contents", contentsArr);

        String jsonPayload = requestBody.toString();
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey.trim();

        String responseBody = executeGeminiCall(url, jsonPayload);

        if (responseBody != null && !responseBody.isBlank()) {
            JSONObject json = new JSONObject(responseBody);
            JSONArray candidates = json.optJSONArray("candidates");
            if (candidates != null && candidates.length() > 0) {
                JSONObject firstCandidate = candidates.getJSONObject(0);
                JSONObject content = firstCandidate.optJSONObject("content");
                if (content != null) {
                    JSONArray parts = content.optJSONArray("parts");
                    if (parts != null && parts.length() > 0) {
                        String rawText = parts.getJSONObject(0).optString("text", "");
                        if (!rawText.isBlank()) {
                            // Ensure SEBI disclaimer is present on every AI answer
                            if (!rawText.contains("SEBI")) {
                                rawText = rawText.trim() + "\n\n---\n" + SEBI_DISCLAIMER;
                            }
                            List<String> calcSources = new ArrayList<>();
                            calcSources.add("Financial education synthesis (Google Gemini)");
                            return new FinCoachResponse(rawText, SOURCE_AI, "gemini", SEBI_DISCLAIMER, calcSources);
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Executes HTTP POST call to Gemini API. Can be overridden in tests for HTTP mocking.
     */
    protected String executeGeminiCall(String url, String jsonPayload) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(8))
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        }
        throw new RuntimeException("Gemini API returned status " + response.statusCode());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // RULE-BASED MODE (User DB data only, calculation source on every number)
    // ──────────────────────────────────────────────────────────────────────────

    public FinCoachResponse answerRuleBased(int userId, String question) {
        String q = question.toLowerCase(Locale.ENGLISH);
        List<String> calcSources = new ArrayList<>();
        StringBuilder answer = new StringBuilder();

        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        try {
            double digitalSpend = expenseDAO.getSumByUserIdAndMonth(userId, month, year);
            double cashSpend = cashDAO.getTotalSpentByMonth(userId, month, year);
            double cashInHand = cashDAO.getCashInHand(userId);
            double totalSpend = digitalSpend + cashSpend;

            Map<String, Double> categoryTotals = expenseDAO.getCategoryTotalsByUserIdAndMonth(userId, month, year);
            List<Budget> budgets = budgetDAO.findByUserId(userId);
            List<Goal> goals = goalDAO.findByUserId(userId);
            double dailyAllowance = allowanceService.getDailyAllowance(userId);

            // Intent 1: Budget / Can I afford / Allowance
            if (q.contains("afford") || q.contains("allowance") || q.contains("today") || q.contains("left today")) {
                answer.append("### 🎯 Daily Spending Allowance\n\n");
                answer.append(String.format("You can safely spend **₹%,.2f today** without exceeding your active monthly budget limits.\n\n", dailyAllowance));

                String calc = String.format("Allowance calculation: Active budgets remaining amount ÷ %d days left in month = ₹%,.2f/day.",
                        today.lengthOfMonth() - today.getDayOfMonth() + 1, dailyAllowance);
                calcSources.add(calc);

                if (dailyAllowance <= 0) {
                    answer.append("⚠️ **Notice:** Your current budget is either exhausted or no active category budgets are set. Consider reviewing your budgets.\n\n");
                } else {
                    answer.append(String.format("💡 If you are deciding on a purchase today, keeping it under ₹%,.2f will keep you on track for the month.\n\n", dailyAllowance));
                }
                answer.append(String.format("> *Calculation Source:* %s\n\n", calc));
            }
            // Intent 2: Spending / Expenses / How much did I spend
            else if (q.contains("spend") || q.contains("expense") || q.contains("spent") || q.contains("how much") || q.contains("cost")) {
                answer.append(String.format("### 💸 Monthly Spending Overview (%s %d)\n\n", today.getMonth().name(), year));
                answer.append(String.format("Your total spending so far this month is **₹%,.2f**:\n", totalSpend));
                answer.append(String.format("- **Digital Expenses:** ₹%,.2f\n", digitalSpend));
                answer.append(String.format("- **Physical Cash Spent:** ₹%,.2f\n", cashSpend));
                answer.append(String.format("- **Cash in Hand Remaining:** ₹%,.2f\n\n", cashInHand));

                String calc1 = String.format("expenses table SUM(amount) for month %d/%d = ₹%,.2f", month, year, digitalSpend);
                String calc2 = String.format("cash_entries table SUM(amount) for month %d/%d = ₹%,.2f", month, year, cashSpend);
                calcSources.add(calc1);
                calcSources.add(calc2);

                if (!categoryTotals.isEmpty()) {
                    answer.append("#### 📊 Top Spending Categories:\n");
                    int count = 0;
                    for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                        double pct = totalSpend > 0 ? (entry.getValue() / totalSpend) * 100.0 : 0;
                        answer.append(String.format("- **%s:** ₹%,.2f (%.1f%% of month spend)\n", entry.getKey(), entry.getValue(), pct));
                        calcSources.add(String.format("%s: (₹%,.2f ÷ ₹%,.2f) × 100 = %.1f%%", entry.getKey(), entry.getValue(), totalSpend, pct));
                        if (++count >= 4) break;
                    }
                    answer.append("\n");
                }

                answer.append(String.format("> *Calculation Source:* Combined digital (₹%,.2f) + cash (₹%,.2f) recorded in database for %s %d.\n\n",
                        digitalSpend, cashSpend, today.getMonth().name(), year));
            }
            // Intent 3: Budgets overview / Alert check
            else if (q.contains("budget")) {
                answer.append("### 🛡️ Budget Status Report\n\n");
                if (budgets.isEmpty()) {
                    answer.append("You have not created any active category budgets yet. Creating budgets helps you prevent overspending and unlocks smart alerts!\n\n");
                    calcSources.add("budgets table: 0 rows found for user.");
                } else {
                    answer.append("Here is how your budgets look this month:\n\n");
                    for (Budget b : budgets) {
                        double limit = b.getLimitAmount();
                        double spent = b.getSpentAmount();
                        double rem = Math.max(0, limit - spent);
                        double pct = limit > 0 ? (spent / limit) * 100.0 : 0.0;

                        String statusIcon = pct >= 100 ? "🚨 OVER LIMIT" : pct >= 90 ? "⚠️ CRITICAL" : pct >= 70 ? "⚡ WARNING" : "✅ ON TRACK";

                        answer.append(String.format("- **%s** (%s): ₹%,.2f spent of ₹%,.2f limit (%.1f%% used, **₹%,.2f left**)\n",
                                b.getCategory(), statusIcon, spent, limit, pct, rem));

                        calcSources.add(String.format("%s budget: limit ₹%,.2f − spent ₹%,.2f = ₹%,.2f remaining (%.1f%% used)",
                                b.getCategory(), limit, spent, rem, pct));
                    }
                    answer.append("\n> *Calculation Source:* budgets table limit_amount minus current spent_amount.\n\n");
                }
            }
            // Intent 4: Goals / Savings
            else if (q.contains("goal") || q.contains("save") || q.contains("saving")) {
                answer.append("### 🎯 Financial Goals Progress\n\n");
                if (goals.isEmpty()) {
                    answer.append("You have not set any financial goals yet. Setting clear targets (e.g., Emergency Fund, Vacation) keeps your savings on track!\n\n");
                    calcSources.add("goals table: 0 active records.");
                } else {
                    for (Goal g : goals) {
                        double target = g.getTargetAmount();
                        double saved = g.getSavedAmount();
                        double pct = target > 0 ? (saved / target) * 100.0 : 0.0;
                        long daysLeft = g.getDeadline() != null ? ChronoUnit.DAYS.between(today, g.getDeadline()) : 0;

                        answer.append(String.format("- **%s:** ₹%,.2f saved of ₹%,.2f (**%.1f%% achieved**) · %d days remaining\n",
                                g.getTitle(), saved, target, pct, daysLeft));

                        calcSources.add(String.format("Goal '%s': (saved ₹%,.2f ÷ target ₹%,.2f) × 100 = %.1f%%; Days until %s = %d days",
                                g.getTitle(), saved, target, pct, g.getDeadline(), daysLeft));
                    }
                    answer.append("\n> *Calculation Source:* goals table saved_amount ÷ target_amount, deadline calendar diff.\n\n");
                }
            }
            // Intent 5: Category auto-suggest inquiry (Innovation 5)
            else if (q.contains("category") || q.contains("categorize") || q.contains("tag") || q.contains("swiggy") || q.contains("zomato") || q.contains("uber")) {
                String suggestedCat = CategoryMapper.suggest(question);
                answer.append("### 🏷️ Category Smart Auto-Suggestion (Innovation 5)\n\n");
                answer.append(String.format("Transactions matching your query are automatically classified under **%s**.\n\n", suggestedCat));
                answer.append(String.format("MyFinance uses keyword taxonomy to auto-suggest categories (e.g., Swiggy/Zomato → Food, Uber/Ola/Fuel → Transport, Amazon/Flipkart → Shopping).\n\n"));
                calcSources.add(String.format("CategoryMapper taxonomy lookup matching keywords in: \"%s\" → %s", question, suggestedCat));
                answer.append(String.format("> *Calculation Source:* CategoryMapper keyword rules mapped to standard categories.\n\n"));
            }
            // Intent 6: 50/30/20 Rule or Financial Education in rule-based mode
            else if (q.contains("50/30/20") || q.contains("rule") || q.contains("emergency") || q.contains("education") || q.contains("invest") || q.contains("sip") || q.contains("compound")) {
                answer.append("### 📚 Financial Education: The 50/30/20 Framework\n\n");
                answer.append("The 50/30/20 rule is an intuitive budgeting framework:\n");
                answer.append("- **50% Needs:** Rent, groceries, utilities, loan EMIs, transport\n");
                answer.append("- **30% Wants:** Dining out, shopping, entertainment, hobbies\n");
                answer.append("- **20% Savings:** Emergency fund, goal savings, retirement\n\n");

                if (totalSpend > 0) {
                    double needsCalc = totalSpend * 0.50;
                    double wantsCalc = totalSpend * 0.30;
                    double savingsCalc = totalSpend * 0.20;
                    answer.append(String.format("Applied to your current tracked monthly outflow of **₹%,.2f**:\n", totalSpend));
                    answer.append(String.format("- **Target Needs (50%%):** ₹%,.2f\n", needsCalc));
                    answer.append(String.format("- **Target Wants (30%%):** ₹%,.2f\n", wantsCalc));
                    answer.append(String.format("- **Target Savings (20%%):** ₹%,.2f\n\n", savingsCalc));

                    String src = String.format("Outflow ₹%,.2f applied to 50%% (₹%,.2f), 30%% (₹%,.2f), 20%% (₹%,.2f)",
                            totalSpend, needsCalc, wantsCalc, savingsCalc);
                    calcSources.add(src);
                    answer.append(String.format("> *Calculation Source:* %s.\n\n", src));
                }
            }
            // Default: Holistic Financial Health Snapshot
            else {
                answer.append("### 💎 Your Financial Health Summary\n\n");
                answer.append(String.format("Here is a quick live look at your personal finances for **%s %d**:\n\n", today.getMonth().name(), year));
                answer.append(String.format("- **Total Outflow:** ₹%,.2f (Digital: ₹%,.2f · Cash: ₹%,.2f)\n", totalSpend, digitalSpend, cashSpend));
                answer.append(String.format("- **Cash in Hand:** ₹%,.2f\n", cashInHand));
                answer.append(String.format("- **Safe Daily Allowance:** ₹%,.2f/day\n", dailyAllowance));
                answer.append(String.format("- **Active Budgets:** %d category budget(s)\n", budgets.size()));
                answer.append(String.format("- **Goals Tracked:** %d financial goal(s)\n\n", goals.size()));

                String summaryCalc = String.format("Live aggregated DB metrics: Digital ₹%,.2f + Cash ₹%,.2f; Daily Allowance: ₹%,.2f/day",
                        digitalSpend, cashSpend, dailyAllowance);
                calcSources.add(summaryCalc);

                String tip = insightService.getWeeklyInsight(userId);
                if (tip != null && !tip.isBlank()) {
                    answer.append(String.format("**Weekly Insight:** %s\n\n", tip));
                }

                answer.append("> *Calculation Source:* Real-time database queries across expenses, budgets, goals, and cash_entries tables.\n\n");
            }

        } catch (Exception e) {
            answer.append("Could not retrieve some database statistics: ").append(e.getMessage());
            calcSources.add("Database query exception: " + e.getMessage());
        }

        // Always append mandatory SEBI disclaimer
        answer.append("\n---\n").append(SEBI_DISCLAIMER);

        return new FinCoachResponse(answer.toString(), SOURCE_USER_DATA, "rule-based", SEBI_DISCLAIMER, calcSources);
    }
}

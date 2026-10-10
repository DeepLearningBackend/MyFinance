package com.myfinance.model;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Data-Transfer Object for the Spending Personality Engine (Innovation 6).
 *
 * <p>Carries everything the JSP needs to render the personality card:
 * <ul>
 *   <li>Personality type  (e.g. "Foodie", "Planner", "Impulsive")</li>
 *   <li>Primary trait     (one-line description shown in the reveal banner)</li>
 *   <li>Full description  (3-4 sentence explanation)</li>
 *   <li>Emoji icon        (used in the animated card)</li>
 *   <li>Color accent      (CSS hex, for the gradient ring)</li>
 *   <li>Score map         (HashMap&lt;String,Integer&gt; — category → weighted score)</li>
 *   <li>Top category      (the category with the highest spend share)</li>
 *   <li>Month / year      (which period was analysed)</li>
 *   <li>totalExpenses     (number of expenses in the window)</li>
 *   <li>dataPoints        (days spanned — shown in formula transparency line)</li>
 * </ul>
 */
public class PersonalityReport {

    // ── Core result ───────────────────────────────────────────────────────
    private int    userId;
    private String personalityType;     // e.g. "Foodie"
    private String primaryTrait;        // e.g. "You live to eat 🍕"
    private String description;         // Full paragraph
    private String emoji;               // 🍕 / 🏡 / 🛍️ / ✈️ / 🎮 / 💡
    private String colorAccent;         // CSS hex e.g. "#f97316"

    // ── Evidence ─────────────────────────────────────────────────────────
    private Map<String, Integer> scores;  // category → weighted score (HashMap)
    private String topCategory;
    private double topCategoryPercent;    // % of total spend in top category
    private int    totalExpenses;         // count of rows analysed
    private int    dataPoints;            // distinct days in the window

    // ── Period ────────────────────────────────────────────────────────────
    private int    calculationMonth;
    private int    calculationYear;
    private LocalDateTime detectedAt;

    // ── Formula string (transparency) ────────────────────────────────────
    private String formula;   // e.g. "Food 45% + Weekend 30% → Foodie"

    // ── Constructors ─────────────────────────────────────────────────────

    public PersonalityReport() {}

    public PersonalityReport(int userId, String personalityType, String primaryTrait,
                              String description, String emoji, String colorAccent,
                              Map<String, Integer> scores, String topCategory,
                              double topCategoryPercent, int totalExpenses, int dataPoints,
                              int calculationMonth, int calculationYear, String formula) {
        this.userId              = userId;
        this.personalityType     = personalityType;
        this.primaryTrait        = primaryTrait;
        this.description         = description;
        this.emoji               = emoji;
        this.colorAccent         = colorAccent;
        this.scores              = scores;
        this.topCategory         = topCategory;
        this.topCategoryPercent  = topCategoryPercent;
        this.totalExpenses       = totalExpenses;
        this.dataPoints          = dataPoints;
        this.calculationMonth    = calculationMonth;
        this.calculationYear     = calculationYear;
        this.formula             = formula;
        this.detectedAt          = LocalDateTime.now();
    }

    // ── Getters & Setters ────────────────────────────────────────────────

    public int    getUserId()              { return userId; }
    public void   setUserId(int v)         { this.userId = v; }

    public String getPersonalityType()     { return personalityType; }
    public void   setPersonalityType(String v) { this.personalityType = v; }

    public String getPrimaryTrait()        { return primaryTrait; }
    public void   setPrimaryTrait(String v){ this.primaryTrait = v; }

    public String getDescription()         { return description; }
    public void   setDescription(String v) { this.description = v; }

    public String getEmoji()               { return emoji; }
    public void   setEmoji(String v)       { this.emoji = v; }

    public String getColorAccent()         { return colorAccent; }
    public void   setColorAccent(String v) { this.colorAccent = v; }

    public Map<String, Integer> getScores(){ return scores; }
    public void   setScores(Map<String, Integer> v) { this.scores = v; }

    public String getTopCategory()         { return topCategory; }
    public void   setTopCategory(String v) { this.topCategory = v; }

    public double getTopCategoryPercent()  { return topCategoryPercent; }
    public void   setTopCategoryPercent(double v) { this.topCategoryPercent = v; }

    public int    getTotalExpenses()       { return totalExpenses; }
    public void   setTotalExpenses(int v)  { this.totalExpenses = v; }

    public int    getDataPoints()          { return dataPoints; }
    public void   setDataPoints(int v)     { this.dataPoints = v; }

    public int    getCalculationMonth()    { return calculationMonth; }
    public void   setCalculationMonth(int v){ this.calculationMonth = v; }

    public int    getCalculationYear()     { return calculationYear; }
    public void   setCalculationYear(int v){ this.calculationYear = v; }

    public LocalDateTime getDetectedAt()   { return detectedAt; }
    public void   setDetectedAt(LocalDateTime v) { this.detectedAt = v; }

    public String getFormula()             { return formula; }
    public void   setFormula(String v)     { this.formula = v; }
}

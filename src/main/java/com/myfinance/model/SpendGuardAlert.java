package com.myfinance.model;

import org.json.JSONObject;

/**
 * Model representing a Spend Guard alert evaluation (Innovation 1).
 * Raised before inserting or confirming an expense when prospective spending
 * approaches or breaches the category budget limit.
 */
public class SpendGuardAlert {

    public static final String LEVEL_NEAR_LIMIT = "near limit";
    public static final String LEVEL_OVER_LIMIT = "over limit";

    private String level; // "near limit" or "over limit"
    private String message;
    private String formula;
    private String category;
    private double spentAmount;
    private double newAmount;
    private double limitAmount;
    private double prospectiveTotal;
    private double prospectivePercent;

    public SpendGuardAlert() {
    }

    public SpendGuardAlert(String level, String message, String formula, String category,
                           double spentAmount, double newAmount, double limitAmount,
                           double prospectiveTotal, double prospectivePercent) {
        this.level = level;
        this.message = message;
        this.formula = formula;
        this.category = category;
        this.spentAmount = spentAmount;
        this.newAmount = newAmount;
        this.limitAmount = limitAmount;
        this.prospectiveTotal = prospectiveTotal;
        this.prospectivePercent = prospectivePercent;
    }

    public static String formatCurrency(double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) {
            return "₹0";
        }
        if (val == Math.floor(val)) {
            return String.format("₹%,.0f", val);
        } else {
            return String.format("₹%,.2f", val);
        }
    }

    public static String buildFormula(double spent, double newAmt, double total, double limit) {
        return String.format("%s spent + %s new = %s of %s limit",
                formatCurrency(spent), formatCurrency(newAmt), formatCurrency(total), formatCurrency(limit));
    }

    public boolean isOverLimit() {
        return LEVEL_OVER_LIMIT.equalsIgnoreCase(level) || "over_limit".equalsIgnoreCase(level);
    }

    public boolean isNearLimit() {
        return LEVEL_NEAR_LIMIT.equalsIgnoreCase(level) || "near_limit".equalsIgnoreCase(level);
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("level", level);
        obj.put("isOverLimit", isOverLimit());
        obj.put("isNearLimit", isNearLimit());
        obj.put("message", message);
        obj.put("formula", formula);
        obj.put("category", category);
        obj.put("spentAmount", spentAmount);
        obj.put("newAmount", newAmount);
        obj.put("limitAmount", limitAmount);
        obj.put("prospectiveTotal", prospectiveTotal);
        obj.put("prospectivePercent", prospectivePercent);
        return obj;
    }

    // Getters and Setters
    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    public double getNewAmount() {
        return newAmount;
    }

    public void setNewAmount(double newAmount) {
        this.newAmount = newAmount;
    }

    public double getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(double limitAmount) {
        this.limitAmount = limitAmount;
    }

    public double getProspectiveTotal() {
        return prospectiveTotal;
    }

    public void setProspectiveTotal(double prospectiveTotal) {
        this.prospectiveTotal = prospectiveTotal;
    }

    public double getProspectivePercent() {
        return prospectivePercent;
    }

    public void setProspectivePercent(double prospectivePercent) {
        this.prospectivePercent = prospectivePercent;
    }

    @Override
    public String toString() {
        return "SpendGuardAlert{" +
                "level='" + level + '\'' +
                ", message='" + message + '\'' +
                ", formula='" + formula + '\'' +
                ", category='" + category + '\'' +
                ", spentAmount=" + spentAmount +
                ", newAmount=" + newAmount +
                ", limitAmount=" + limitAmount +
                ", prospectiveTotal=" + prospectiveTotal +
                ", prospectivePercent=" + prospectivePercent +
                '}';
    }
}

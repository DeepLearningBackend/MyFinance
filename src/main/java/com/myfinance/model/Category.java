package com.myfinance.model;

import java.util.Arrays;
import java.util.List;

/**
 * Category model and constants for expense and budget classifications.
 */
public class Category {

    public static final String FOOD = "Food & Dining";
    public static final String SHOPPING = "Shopping";
    public static final String TRANSPORT = "Transport & Fuel";
    public static final String HOUSING = "Rent & Housing";
    public static final String UTILITIES = "Bills & Utilities";
    public static final String ENTERTAINMENT = "Entertainment";
    public static final String HEALTH = "Health & Medical";
    public static final String EDUCATION = "Education";
    public static final String INVESTMENT = "Investment & Savings";
    public static final String OTHER = "Other";

    public static final List<String> ALL_CATEGORIES = List.of(
            FOOD,
            SHOPPING,
            TRANSPORT,
            HOUSING,
            UTILITIES,
            ENTERTAINMENT,
            HEALTH,
            EDUCATION,
            INVESTMENT,
            OTHER
    );

    private String name;
    private String icon;
    private String color;

    public Category() {
    }

    public Category(String name, String icon, String color) {
        this.name = name;
        this.icon = icon;
        this.color = color;
    }

    public static String getIconForCategory(String category) {
        if (category == null) return "📦";
        return switch (category.trim()) {
            case FOOD, "Food", "Dining" -> "🍔";
            case SHOPPING -> "🛍️";
            case TRANSPORT, "Transport", "Travel" -> "🚗";
            case HOUSING, "Housing", "Rent" -> "🏠";
            case UTILITIES, "Utilities", "Bills" -> "💡";
            case ENTERTAINMENT -> "🎬";
            case HEALTH, "Health", "Medical" -> "💊";
            case EDUCATION -> "📚";
            case INVESTMENT, "Investment", "Savings" -> "📈";
            default -> "💳";
        };
    }

    public static String getColorForCategory(String category) {
        if (category == null) return "#a855f7";
        return switch (category.trim()) {
            case FOOD, "Food", "Dining" -> "#f97316";       // Orange
            case SHOPPING -> "#ec4899";                     // Pink
            case TRANSPORT, "Transport", "Travel" -> "#06b6d4"; // Cyan
            case HOUSING, "Housing", "Rent" -> "#8b5cf6";   // Purple
            case UTILITIES, "Utilities", "Bills" -> "#eab308"; // Yellow
            case ENTERTAINMENT -> "#a855f7";                 // Violet
            case HEALTH, "Health", "Medical" -> "#ef4444";  // Red
            case EDUCATION -> "#3b82f6";                    // Blue
            case INVESTMENT, "Investment", "Savings" -> "#10b981"; // Emerald
            default -> "#6b7280";                           // Slate/Gray
        };
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}

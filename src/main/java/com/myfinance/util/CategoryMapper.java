package com.myfinance.util;

import com.myfinance.model.Category;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Category Auto-Suggestion Engine (Innovation 5).
 * Automatically maps transaction descriptions and merchant names to standard spending categories.
 */
public final class CategoryMapper {

    private static final Map<String, String> KEYWORD_MAP = new HashMap<>();

    static {
        // Food & Dining
        registerKeywords(Category.FOOD,
                "swiggy", "zomato", "mcdonalds", "mcdonald", "kfc", "starbucks", "dominos", "pizza", "burger",
                "cafe", "restaurant", "dining", "groceries", "blinkit", "zepto", "instamart",
                "bigbasket", "subway", "barbeque", "dosa", "biryani", "food", "supermarket",
                "tea", "chai", "coffee", "baker", "bakery");

        // Transport & Fuel
        registerKeywords(Category.TRANSPORT,
                "uber", "ola", "rapido", "metro", "fuel", "petrol", "diesel", "shell", "hp petrol",
                "indian oil", "irctc", "flight", "indigo", "air india", "bus", "redbus",
                "parking", "fastag", "toll", "cab", "taxi", "train");

        // Shopping
        registerKeywords(Category.SHOPPING,
                "amazon", "flipkart", "myntra", "ajio", "zara", "h&m", "nike", "adidas",
                "clothes", "shoes", "electronics", "croma", "reliance digital", "apple",
                "mall", "shopping", "lifestyle", "meesho", "nykaa");

        // Bills & Utilities
        registerKeywords(Category.UTILITIES,
                "electricity", "bescom", "water bill", "gas bill", "cylinder", "broadband", "wifi",
                "airtel", "jio", "vi", "vodafone", "recharge", "dth", "tata play",
                "tatasky", "utility", "bills", "electricity bill");

        // Rent & Housing
        registerKeywords(Category.HOUSING,
                "rent", "maintenance", "landlord", "flat", "pg", "society", "apartment",
                "furniture", "ikea", "urban company", "plumber");

        // Entertainment
        registerKeywords(Category.ENTERTAINMENT,
                "netflix", "spotify", "prime video", "hotstar", "disney", "cinema", "pvr",
                "inox", "bookmyshow", "movie", "game", "steam", "playstation", "theatre");

        // Health & Medical
        registerKeywords(Category.HEALTH,
                "pharmacy", "apollo", "medplus", "hospital", "doctor", "medicine", "1mg",
                "pharmeasy", "clinic", "dental", "gym", "cult.fit", "fitness", "lab", "diagnostics");

        // Education
        registerKeywords(Category.EDUCATION,
                "coursera", "udemy", "tuition", "books", "college", "fees", "course",
                "exam", "school", "stationery", "kindle");

        // Investment & Savings
        registerKeywords(Category.INVESTMENT,
                "zerodha", "groww", "mutual fund", "sip", "stocks", "crypto", "fixed deposit",
                "fd", "rd", "gold", "upstox", "coin", "etmoney");
    }

    private CategoryMapper() {
        // Utility class
    }

    private static void registerKeywords(String category, String... keywords) {
        for (String kw : keywords) {
            KEYWORD_MAP.put(kw.toLowerCase(Locale.ROOT), category);
        }
    }

    /**
     * Inspects input text (merchant, title, or description) and suggests the best matching category.
     * Uses word-boundary matching to prevent false-positive substring captures.
     *
     * @param input text to analyze
     * @return predicted category name, or {@link Category#OTHER} if no keyword matches
     */
    public static String suggest(String input) {
        if (input == null || input.trim().isEmpty()) {
            return Category.OTHER;
        }

        String lower = input.toLowerCase(Locale.ROOT);

        // 1. Check exact word/phrase boundary matches
        for (Map.Entry<String, String> entry : KEYWORD_MAP.entrySet()) {
            String keyword = entry.getKey();
            String regex = "\\b" + Pattern.quote(keyword) + "\\b";
            if (Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(lower).find()) {
                return entry.getValue();
            }
        }

        // 2. Tokenized direct lookup
        String[] tokens = lower.split("[^a-zA-Z0-9]+");
        for (String token : tokens) {
            if (KEYWORD_MAP.containsKey(token)) {
                return KEYWORD_MAP.get(token);
            }
        }

        return Category.OTHER;
    }

    /**
     * Normalizes any category string (e.g. "Food", "Dining", "Food & Dining") into the
     * canonical Category constant to guarantee consistent matching between expenses,
     * budgets, and Spend Guard.
     *
     * @param category category input string
     * @return canonical category string from {@link Category}
     */
    public static String normalize(String category) {
        if (category == null || category.trim().isEmpty()) {
            return Category.OTHER;
        }

        String trimmed = category.trim();
        for (String c : Category.ALL_CATEGORIES) {
            if (c.equalsIgnoreCase(trimmed)) {
                return c;
            }
        }

        String lower = trimmed.toLowerCase(Locale.ROOT);
        if (lower.equals("food") || lower.equals("dining") || lower.contains("food")) {
            return Category.FOOD;
        }
        if (lower.equals("transport") || lower.equals("travel") || lower.equals("fuel") || lower.contains("transport")) {
            return Category.TRANSPORT;
        }
        if (lower.equals("shopping") || lower.contains("shop")) {
            return Category.SHOPPING;
        }
        if (lower.equals("bills") || lower.equals("utilities") || lower.equals("utility") || lower.contains("bill")) {
            return Category.UTILITIES;
        }
        if (lower.equals("housing") || lower.equals("rent") || lower.contains("rent") || lower.contains("house")) {
            return Category.HOUSING;
        }
        if (lower.equals("entertainment") || lower.contains("entertain")) {
            return Category.ENTERTAINMENT;
        }
        if (lower.equals("health") || lower.equals("medical") || lower.contains("health") || lower.contains("medic")) {
            return Category.HEALTH;
        }
        if (lower.equals("education") || lower.contains("educat")) {
            return Category.EDUCATION;
        }
        if (lower.equals("investment") || lower.equals("savings") || lower.contains("invest") || lower.contains("saving")) {
            return Category.INVESTMENT;
        }

        return Category.OTHER;
    }
}

package com.myfinance.util;

import java.util.HashMap;
import java.util.Map;

public class CategoryMapper {
    private static final Map<String, String> keywordToCategory = new HashMap<>();

    static {
        // Food
        keywordToCategory.put("swiggy", "Food");
        keywordToCategory.put("zomato", "Food");
        keywordToCategory.put("food", "Food");
        keywordToCategory.put("restaurant", "Food");
        keywordToCategory.put("cafe", "Food");
        keywordToCategory.put("pizza", "Food");
        keywordToCategory.put("burger", "Food");
        keywordToCategory.put("lunch", "Food");
        keywordToCategory.put("dinner", "Food");
        keywordToCategory.put("breakfast", "Food");

        // Transport
        keywordToCategory.put("ola", "Transport");
        keywordToCategory.put("uber", "Transport");
        keywordToCategory.put("taxi", "Transport");
        keywordToCategory.put("auto", "Transport");
        keywordToCategory.put("rickshaw", "Transport");
        keywordToCategory.put("bus", "Transport");
        keywordToCategory.put("metro", "Transport");
        keywordToCategory.put("train", "Transport");
        keywordToCategory.put("petrol", "Transport");
        keywordToCategory.put("fuel", "Transport");
        keywordToCategory.put("diesel", "Transport");

        // Shopping
        keywordToCategory.put("amazon", "Shopping");
        keywordToCategory.put("flipkart", "Shopping");
        keywordToCategory.put("myntra", "Shopping");
        keywordToCategory.put("ajio", "Shopping");
        keywordToCategory.put("shop", "Shopping");
        keywordToCategory.put("mall", "Shopping");
        keywordToCategory.put("clothes", "Shopping");
        keywordToCategory.put("shoes", "Shopping");
        keywordToCategory.put("electronics", "Shopping");

        // Entertainment
        keywordToCategory.put("netflix", "Entertainment");
        keywordToCategory.put("prime", "Entertainment");
        keywordToCategory.put("spotify", "Entertainment");
        keywordToCategory.put("movie", "Entertainment");
        keywordToCategory.put("cinema", "Entertainment");
        keywordToCategory.put("concert", "Entertainment");
        keywordToCategory.put("game", "Entertainment");

        // Utilities
        keywordToCategory.put("electricity", "Utilities");
        keywordToCategory.put("water", "Utilities");
        keywordToCategory.put("gas", "Utilities");
        keywordToCategory.put("internet", "Utilities");
        keywordToCategory.put("mobile", "Utilities");
        keywordToCategory.put("phone", "Utilities");
        keywordToCategory.put("recharge", "Utilities");

        // Health
        keywordToCategory.put("medicine", "Health");
        keywordToCategory.put("doctor", "Health");
        keywordToCategory.put("hospital", "Health");
        keywordToCategory.put("pharmacy", "Health");
        keywordToCategory.put("gym", "Health");
        keywordToCategory.put("fitness", "Health");

        // Education
        keywordToCategory.put("book", "Education");
        keywordToCategory.put("course", "Education");
        keywordToCategory.put("tuition", "Education");
        keywordToCategory.put("class", "Education");
        keywordToCategory.put("school", "Education");
        keywordToCategory.put("college", "Education");

        // Others
        keywordToCategory.put("gift", "Others");
        keywordToCategory.put("donation", "Others");
        keywordToCategory.put("charity", "Others");
    }

    public static String suggest(String title) {
        if (title == null || title.isEmpty()) {
            return "Others";
        }

        String lowerTitle = title.toLowerCase();
        for (Map.Entry<String, String> entry : keywordToCategory.entrySet()) {
            if (lowerTitle.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return "Others";
    }
}

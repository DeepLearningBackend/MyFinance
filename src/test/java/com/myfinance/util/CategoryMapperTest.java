package com.myfinance.util;

import com.myfinance.model.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("CategoryMapper Smart Auto-Suggest Tests (Innovation 5)")
class CategoryMapperTest {

    @Test
    @DisplayName("Maps known food & delivery vendor keywords to Food & Dining")
    void testFoodKeywords() {
        assertEquals(Category.FOOD, CategoryMapper.suggest("Swiggy Biryani Order"));
        assertEquals(Category.FOOD, CategoryMapper.suggest("Zomato dinner with friends"));
        assertEquals(Category.FOOD, CategoryMapper.suggest("Starbucks Cold Brew"));
        assertEquals(Category.FOOD, CategoryMapper.suggest("Domino's Pizza delivery"));
        assertEquals(Category.FOOD, CategoryMapper.suggest("Blinkit grocery quick commerce"));
    }

    @Test
    @DisplayName("Maps transport, fuel, and cab keywords to Transport & Fuel")
    void testTransportKeywords() {
        assertEquals(Category.TRANSPORT, CategoryMapper.suggest("Uber Premier to Airport"));
        assertEquals(Category.TRANSPORT, CategoryMapper.suggest("Ola Auto ride"));
        assertEquals(Category.TRANSPORT, CategoryMapper.suggest("HP Petrol Pump fill"));
        assertEquals(Category.TRANSPORT, CategoryMapper.suggest("IRCTC Train Ticket booking"));
        assertEquals(Category.TRANSPORT, CategoryMapper.suggest("IndiGo flight to Mumbai"));
    }

    @Test
    @DisplayName("Maps e-commerce and retail keywords to Shopping")
    void testShoppingKeywords() {
        assertEquals(Category.SHOPPING, CategoryMapper.suggest("Amazon electronics sale"));
        assertEquals(Category.SHOPPING, CategoryMapper.suggest("Flipkart Big Billion Days"));
        assertEquals(Category.SHOPPING, CategoryMapper.suggest("Myntra jacket"));
        assertEquals(Category.SHOPPING, CategoryMapper.suggest("Zara summer collection"));
    }

    @Test
    @DisplayName("Maps entertainment, subscriptions, and movies to Entertainment")
    void testEntertainmentKeywords() {
        assertEquals(Category.ENTERTAINMENT, CategoryMapper.suggest("Netflix 4K subscription"));
        assertEquals(Category.ENTERTAINMENT, CategoryMapper.suggest("Spotify Premium Family"));
        assertEquals(Category.ENTERTAINMENT, CategoryMapper.suggest("BookMyShow PVR cinema tickets"));
    }

    @Test
    @DisplayName("Maps investments and trading keywords to Investment & Savings")
    void testInvestmentKeywords() {
        assertEquals(Category.INVESTMENT, CategoryMapper.suggest("Zerodha Kite equity purchase"));
        assertEquals(Category.INVESTMENT, CategoryMapper.suggest("Groww Nifty 50 Index Mutual Fund SIP"));
    }

    @Test
    @DisplayName("Unknown or generic keywords fallback gracefully to Other")
    void testFallback() {
        assertEquals(Category.OTHER, CategoryMapper.suggest("Random payment #12345"));
        assertEquals(Category.OTHER, CategoryMapper.suggest(null));
        assertEquals(Category.OTHER, CategoryMapper.suggest(""));
    }
}

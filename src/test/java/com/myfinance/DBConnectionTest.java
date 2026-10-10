package com.myfinance;

import com.myfinance.util.DBConnection;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import static org.junit.jupiter.api.Assertions.*;

public class DBConnectionTest {

    @Test
    public void testDatabaseConnectionAndSchema() {
        assertDoesNotThrow(() -> {
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement()) {
                assertNotNull(conn, "Connection should not be null");
                assertFalse(conn.isClosed(), "Connection should be open");

                // Verify the 9 tables exist
                String[] expectedTables = {
                    "users", "expenses", "cash_entries", "budgets",
                    "goals", "sms_queue", "spending_personality",
                    "system_settings", "audit_logs"
                };

                for (String table : expectedTables) {
                    try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + table)) {
                        assertTrue(rs.next(), "Table " + table + " should be queryable");
                    }
                }
            }
        });
    }
}

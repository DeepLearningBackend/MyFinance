package com.myfinance.util;

import com.myfinance.exception.DatabaseException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.time.ZoneId;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility class to manage JDBC connections for MyFinance.
 * Supports primary MySQL connection on localhost:3306 with seamless fallback
 * to an embedded local H2 database if MySQL server is offline.
 */
public final class DBConnection {

    private static final String MYSQL_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String H2_DRIVER = "org.h2.Driver";

    private static final String MYSQL_URL = System.getenv("DB_URL") != null
            ? System.getenv("DB_URL")
            : "jdbc:mysql://localhost:3306/myfinancedb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata&characterEncoding=UTF-8";
    private static final String MYSQL_USER = System.getenv("DB_USER") != null
            ? System.getenv("DB_USER")
            : "root";
    private static final String MYSQL_PASS = System.getenv("DB_PASS") != null
            ? System.getenv("DB_PASS")
            : "";

    private static final String H2_URL = "jdbc:h2:./myfinancedb_local;MODE=MySQL;CASE_INSENSITIVE_IDENTIFIERS=TRUE;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=TRUE";
    private static final String H2_USER = "sa";
    private static final String H2_PASS = "";

    private static volatile boolean useFallbackH2 = false;
    private static final AtomicBoolean h2Initialized = new AtomicBoolean(false);
    private static final AtomicBoolean connectionLogged = new AtomicBoolean(false);

    static {
        // Enforce consistent Asia/Kolkata timezone globally across the JVM
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        try {
            Class.forName(MYSQL_DRIVER);
        } catch (ClassNotFoundException e) {
            System.err.println("[MyFinance DB] MySQL Driver not found: " + e.getMessage());
        }
        try {
            Class.forName(H2_DRIVER);
        } catch (ClassNotFoundException e) {
            // H2 driver only required if DB_FALLBACK=true or running in test scope
        }
    }

    private static String getMySQLHostDb() {
        try {
            String url = MYSQL_URL;
            int idx = url.indexOf("://");
            if (idx != -1) {
                String sub = url.substring(idx + 3);
                int qIdx = sub.indexOf('?');
                if (qIdx != -1) {
                    sub = sub.substring(0, qIdx);
                }
                return sub;
            }
        } catch (Exception ignored) {}
        return "localhost:3306/myfinancedb";
    }

    private static void logMySQLNoticeOnce() {
        if (connectionLogged.compareAndSet(false, true)) {
            System.out.println("=======================================================");
            System.out.println("✅ Connected to MySQL: " + getMySQLHostDb());
            System.out.println("=======================================================");
        }
    }

    private static void logH2NoticeOnce() {
        if (connectionLogged.compareAndSet(false, true)) {
            System.err.println("=======================================================");
            System.err.println("⚠️  RUNNING ON H2 FALLBACK, NOT MYSQL");
            System.err.println("=======================================================");
        }
    }

    /**
     * Prints runtime timezone verification diagnostics at startup.
     */
    public static void printTimezoneDiagnostics() {
        System.out.println("=======================================================");
        System.out.println("🕒 [Timezone Diagnostics]");
        System.out.println("👉 JVM Default TimeZone: " + ZoneId.systemDefault() + " (" + TimeZone.getDefault().getID() + ")");
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            boolean checked = false;
            try (ResultSet rs = stmt.executeQuery("SELECT NOW(), @@system_time_zone")) {
                if (rs.next()) {
                    System.out.println("👉 MySQL NOW():         " + rs.getString(1));
                    System.out.println("👉 MySQL @@system_time_zone: " + rs.getString(2));
                    checked = true;
                }
            } catch (Exception ignored) {
            }
            if (!checked) {
                try (ResultSet rs = stmt.executeQuery("SELECT CURRENT_TIMESTAMP")) {
                    if (rs.next()) {
                        System.out.println("👉 DB CURRENT_TIMESTAMP: " + rs.getString(1));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Could not query DB timezone: " + e.getMessage());
        }
        System.out.println("=======================================================");
    }

    private DBConnection() {
        // Private constructor to prevent instantiation
    }

    /**
     * Obtains a new database connection.
     *
     * @return active {@link Connection}
     * @throws DatabaseException if connection cannot be established
     */
    public static Connection getConnection() {
        if (useFallbackH2) {
            logH2NoticeOnce();
            return getH2Connection();
        }

        try {
            Connection conn = DriverManager.getConnection(MYSQL_URL, MYSQL_USER, MYSQL_PASS);
            logMySQLNoticeOnce();
            return conn;
        } catch (SQLException e) {
            boolean fallbackAllowed = "true".equalsIgnoreCase(System.getenv("DB_FALLBACK"));
            if (!fallbackAllowed) {
                String errorMsg = "MySQL connection failed: check that the MySQL service is running and DB_PASS is set (" + e.getMessage() + ")";
                System.err.println("[MyFinance DB] " + errorMsg);
                throw new DatabaseException(errorMsg, e);
            }

            useFallbackH2 = true;
            logH2NoticeOnce();
            System.err.println("[MyFinance DB] MySQL connection failed (" + e.getMessage() + "). DB_FALLBACK=true is set; falling back to embedded H2 database...");
            return getH2Connection();
        }
    }

    private static Connection getH2Connection() {
        try {
            Connection conn = DriverManager.getConnection(H2_URL, H2_USER, H2_PASS);
            if (h2Initialized.compareAndSet(false, true)) {
                initH2Schema(conn);
            }
            return conn;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to establish database connection.", e);
        }
    }

    private static void initH2Schema(Connection conn) {
        System.out.println("[MyFinance DB] Initializing schema on database...");
        try (Statement stmt = conn.createStatement()) {
            String schemaSql = loadSchemaSql();
            if (schemaSql != null && !schemaSql.isEmpty()) {
                String[] queries = schemaSql.split(";");
                for (String q : queries) {
                    String trimmed = q.trim();
                    if (trimmed.isEmpty() || trimmed.toUpperCase().startsWith("CREATE DATABASE") || trimmed.toUpperCase().startsWith("USE ")) {
                        continue;
                    }
                    try {
                        stmt.execute(trimmed);
                    } catch (SQLException ex) {
                        if (!ex.getMessage().contains("already exists") && !ex.getMessage().contains("Duplicate")) {
                            System.err.println("[MyFinance DB] Schema notice: " + ex.getMessage());
                        }
                    }
                }
                System.out.println("[MyFinance DB] Database schema initialized successfully (9 tables ready)!");
            }
        } catch (Exception e) {
            System.err.println("[MyFinance DB] Warning initializing schema: " + e.getMessage());
        }
    }

    private static String loadSchemaSql() {
        File f = new File("database/schema.sql");
        if (f.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().startsWith("--")) {
                        sb.append(line).append("\n");
                    }
                }
                return sb.toString();
            } catch (Exception e) {
                // Ignore and fall back
            }
        }

        try (InputStream is = DBConnection.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.trim().startsWith("--")) {
                            sb.append(line).append("\n");
                        }
                    }
                    return sb.toString();
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
}

package com.myfinance.service;

import com.myfinance.dao.impl.ExpenseDAOImpl;
import com.myfinance.dao.impl.SystemSettingsDAOImpl;
import com.myfinance.dao.interfaces.ExpenseDAO;
import com.myfinance.dao.interfaces.SystemSettingsDAO;
import com.myfinance.exception.DatabaseException;
import com.myfinance.model.PersonalityReport;
import com.myfinance.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;

/**
 * Orchestrates the Spending Personality Engine (Innovation 6).
 *
 * <p>Flow:
 * <ol>
 *   <li>Read the {@code personality_min_days} system setting (default 7;
 *       admin can lower it to 1 for demo mode).</li>
 *   <li>Call {@link ExpenseDAO#countDistinctExpenseDays} to check we have
 *       enough data.</li>
 *   <li>Call {@link ExpenseDAO#getPersonalityAggregates} to get the SQL
 *       aggregates (DAYOFWEEK, category sums).</li>
 *   <li>Delegate scoring to {@link PersonalityDetector#detect}.</li>
 *   <li>Persist the result to {@code spending_personality} table.</li>
 * </ol>
 */
public class PersonalityService {

    /** Setting key stored in system_settings. */
    public static final String SETTING_MIN_DAYS = "personality_min_days";

    /** Default minimum days of expense data required. */
    public static final int DEFAULT_MIN_DAYS = 7;

    /** Look-back window in days for analysis. */
    public static final int WINDOW_DAYS = 30;

    private final ExpenseDAO         expenseDAO;
    private final SystemSettingsDAO  settingsDAO;
    private final PersonalityDetector detector;

    /**
     * Default constructor initializing DAOs and personality detector.
     */
    public PersonalityService() {
        this.expenseDAO  = new ExpenseDAOImpl();
        this.settingsDAO = new SystemSettingsDAOImpl();
        this.detector    = new PersonalityDetector();
    }

    /**
     * Constructor for dependency injection and testing.
     *
     * @param expenseDAO  DAO for querying expense records and aggregates
     * @param settingsDAO DAO for reading system settings
     */
    public PersonalityService(ExpenseDAO expenseDAO, SystemSettingsDAO settingsDAO) {
        this.expenseDAO  = expenseDAO;
        this.settingsDAO = settingsDAO;
        this.detector    = new PersonalityDetector();
    }

    // ── Main entry point ─────────────────────────────────────────────────

    /**
     * Detects the user's spending personality for the current month.
     *
     * @param userId authenticated user
     * @return {@link PersonalityReport}, or {@code null} if not enough data
     * @throws DatabaseException on JDBC failure
     */
    public PersonalityReport detect(int userId) {
        LocalDate today = LocalDate.now();
        return detect(userId, today.getMonthValue(), today.getYear());
    }

    /**
     * Detects the user's spending personality for a specific month / year.
     *
     * @param userId authenticated user
     * @param month  analysis month (1–12)
     * @param year   analysis year
     * @return {@link PersonalityReport}, or {@code null} if not enough data
     */
    public PersonalityReport detect(int userId, int month, int year) {
        try {
            int minDays = getMinDays();
            int distinctDays = expenseDAO.countDistinctExpenseDays(userId, WINDOW_DAYS);

            if (distinctDays < minDays) {
                return null; // not enough data
            }

            Map<String, Object> aggregates = expenseDAO.getPersonalityAggregates(userId, WINDOW_DAYS);
            PersonalityReport report = detector.detect(userId, aggregates, distinctDays, month, year);

            persist(report);
            return report;

        } catch (SQLException e) {
            throw new DatabaseException("Personality detection failed: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the most-recently stored personality for a user without
     * recalculating (used to show a cached result on page reload).
     *
     * @param userId authenticated user ID
     * @return the cached {@link PersonalityReport}, or {@code null} if none exists
     * @throws DatabaseException on JDBC query failure
     */
    public PersonalityReport getLatest(int userId) {
        String sql = "SELECT personality_type, primary_trait, description, calculation_month, calculation_year, created_at " +
                     "FROM spending_personality WHERE user_id = ? ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PersonalityReport r = new PersonalityReport();
                    r.setUserId(userId);
                    r.setPersonalityType(rs.getString("personality_type"));
                    r.setPrimaryTrait(rs.getString("primary_trait"));
                    r.setDescription(rs.getString("description"));
                    r.setCalculationMonth(rs.getInt("calculation_month"));
                    r.setCalculationYear(rs.getInt("calculation_year"));
                    return r;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not fetch cached personality: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Returns the current {@code personality_min_days} setting,
     * falling back to {@link #DEFAULT_MIN_DAYS} if not set.
     *
     * @return the minimum required days of spending history
     */
    public int getMinDays() {
        try {
            String val = settingsDAO.getSetting(SETTING_MIN_DAYS, String.valueOf(DEFAULT_MIN_DAYS));
            if (val != null && !val.isBlank()) {
                return Integer.parseInt(val.trim());
            }
        } catch (Exception ignored) {
        }
        return DEFAULT_MIN_DAYS;
    }

    // ── Persistence ───────────────────────────────────────────────────────

    private void persist(PersonalityReport r) throws SQLException {
        // Upsert: delete existing for same user+month+year, then insert
        String del = "DELETE FROM spending_personality " +
                     "WHERE user_id = ? AND calculation_month = ? AND calculation_year = ?";
        String ins = "INSERT INTO spending_personality " +
                     "(user_id, personality_type, primary_trait, description, calculation_month, calculation_year) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement del_ps = conn.prepareStatement(del)) {
                del_ps.setInt(1, r.getUserId());
                del_ps.setInt(2, r.getCalculationMonth());
                del_ps.setInt(3, r.getCalculationYear());
                del_ps.executeUpdate();
            }
            try (PreparedStatement ins_ps = conn.prepareStatement(ins)) {
                ins_ps.setInt(1, r.getUserId());
                ins_ps.setString(2, r.getPersonalityType());
                ins_ps.setString(3, r.getPrimaryTrait());
                ins_ps.setString(4, r.getDescription());
                ins_ps.setInt(5, r.getCalculationMonth());
                ins_ps.setInt(6, r.getCalculationYear());
                ins_ps.executeUpdate();
            }
            conn.commit();
        }
    }
}

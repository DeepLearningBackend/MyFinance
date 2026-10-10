package com.myfinance.dao.interfaces;

import com.myfinance.model.CashEntry;

import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for cash_entries table (Innovation 3: Cash Tracker).
 * Manages physical cash spending records and the users.cash_in_hand running balance.
 */
public interface CashDAO {

    /** Persist a new cash entry; reduces users.cash_in_hand by entry.amount. */
    boolean save(CashEntry entry) throws SQLException;

    /** Find a single entry by primary key. */
    CashEntry findById(int id) throws SQLException;

    /** All cash entries for a user, newest first. */
    List<CashEntry> findByUserId(int userId) throws SQLException;

    /** Cash entries for a specific calendar month / year. */
    List<CashEntry> findByUserIdAndMonth(int userId, int month, int year) throws SQLException;

    /** Update description / category of an existing entry. */
    boolean update(CashEntry entry) throws SQLException;

    /** Delete one cash entry and restore its amount to users.cash_in_hand. */
    boolean delete(int id, int userId) throws SQLException;

    // ── cash_in_hand helpers ──────────────────────────────────────────────

    /** Set the users.cash_in_hand to an absolute amount (beginning-of-month reset). */
    boolean setCashInHand(int userId, double amount) throws SQLException;

    /** Read the current cash_in_hand for a user. */
    double getCashInHand(int userId) throws SQLException;

    // ── aggregates ────────────────────────────────────────────────────────

    /** Total cash spent in a given month / year. */
    double getTotalSpentByMonth(int userId, int month, int year) throws SQLException;

    /** Total cash spent across all time. */
    double getTotalSpentAllTime(int userId) throws SQLException;
}

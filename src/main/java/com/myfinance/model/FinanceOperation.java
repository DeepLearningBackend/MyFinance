package com.myfinance.model;

/**
 * Interface representing a financial calculation or reporting operation.
 * (Interfaces rubric — each of the four services implements this differently.)
 *
 * <p>The contract:
 * <ul>
 *   <li>{@link #execute(Object)} — process / transform a single entity of type {@code T}
 *       and return a result of type {@code R}.</li>
 *   <li>{@link #summarize(int)} — return a one-line plain-English summary of this
 *       service's current financial state for the given user.  Each service
 *       overrides this with domain-specific logic.</li>
 * </ul>
 *
 * @param <T> input entity type  (e.g. Expense, Budget, Goal, CashEntry)
 * @param <R> output result type (e.g. ApiResponse, Double, String)
 */
public interface FinanceOperation<T, R> {

    /**
     * Executes a financial calculation or operational transformation on the
     * supplied entity.
     *
     * @param entity target financial entity
     * @return calculated / transformed result
     */
    R execute(T entity);

    /**
     * Returns a one-line summary of this service's key financial metric
     * for the given user (used by the Allowance transparency card).
     *
     * @param userId authenticated user id
     * @return human-readable summary string, e.g. "₹3,200 spent this month"
     */
    String summarize(int userId);
}

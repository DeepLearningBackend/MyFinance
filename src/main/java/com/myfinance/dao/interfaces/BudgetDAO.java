package com.myfinance.dao.interfaces;

import com.myfinance.model.Budget;
import java.sql.SQLException;
import java.util.List;

public interface BudgetDAO {
    void save(Budget budget) throws SQLException;
    Budget findById(int id) throws SQLException;
    List<Budget> findByUserId(int userId) throws SQLException;
    Budget findByUserIdAndCategory(int userId, String category) throws SQLException;
    List<Budget> findOverBudget(int userId) throws SQLException;
    void updateSpentAmount(int budgetId, double amount) throws SQLException;
    List<Budget> findAll() throws SQLException;
    void update(Budget budget) throws SQLException;
    void delete(int id) throws SQLException;
}

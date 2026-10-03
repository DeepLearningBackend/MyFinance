package com.myfinance.dao.interfaces;

import com.myfinance.model.Expense;
import java.sql.SQLException;
import java.util.List;

public interface ExpenseDAO {
    void save(Expense expense) throws SQLException;
    Expense findById(int id) throws SQLException;
    List<Expense> findByUserId(int userId) throws SQLException;
    List<Expense> findByUserIdAndMonth(int userId, int month, int year) throws SQLException;
    List<Expense> findByUserIdAndCategory(int userId, String category) throws SQLException;
    double getSumByUserIdAndMonth(int userId, int month, int year) throws SQLException;
    List<Expense> findAll() throws SQLException;
    void update(Expense expense) throws SQLException;
    void delete(int id, int userId) throws SQLException;
}

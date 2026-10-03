package com.myfinance.dao.interfaces;

import com.myfinance.model.User;
import java.sql.SQLException;
import java.util.List;

public interface UserDAO {
    void save(User user) throws SQLException;
    User findById(int id) throws SQLException;
    User findByEmail(String email) throws SQLException;
    List<User> findAll() throws SQLException;
    void update(User user) throws SQLException;
    void delete(int id) throws SQLException;
}

package com.myfinance.dao;

import com.myfinance.dao.impl.UserDAOImpl;
import com.myfinance.dao.interfaces.UserDAO;
import com.myfinance.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserDAOImpl JDBC Operations Test")
class UserDAOImplTest {

    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        userDAO = new UserDAOImpl();
    }

    @Test
    @DisplayName("Should successfully save, find, and delete a user")
    void testSaveFindAndDeleteUser() throws SQLException {
        String uniqueEmail = "testuser_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        User user = new User("Test Student", uniqueEmail, "$2a$12$dummyHashedPassword", User.Role.USER, User.Status.ACTIVE);

        // 1. Save
        boolean saved = userDAO.save(user);
        assertTrue(saved, "User should be successfully saved");
        assertTrue(user.getId() > 0, "User ID should be auto-generated");

        // 2. Find by Email
        User foundByEmail = userDAO.findByEmail(uniqueEmail);
        assertNotNull(foundByEmail, "Should find user by email");
        assertEquals("Test Student", foundByEmail.getName());
        assertEquals(User.Role.USER, foundByEmail.getRole());

        // 3. Find by ID
        User foundById = userDAO.findById(user.getId());
        assertNotNull(foundById, "Should find user by ID");
        assertEquals(uniqueEmail, foundById.getEmail());

        // 4. Update
        foundById.setName("Updated Student");
        boolean updated = userDAO.update(foundById);
        assertTrue(updated, "User should be updated");

        User verified = userDAO.findById(user.getId());
        assertEquals("Updated Student", verified.getName());

        // 5. Delete
        boolean deleted = userDAO.delete(user.getId());
        assertTrue(deleted, "User should be deleted");

        User postDelete = userDAO.findById(user.getId());
        assertNull(postDelete, "User should not exist after deletion");
    }
}

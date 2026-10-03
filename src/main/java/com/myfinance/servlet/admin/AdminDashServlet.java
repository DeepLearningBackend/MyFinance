package com.myfinance.servlet.admin;

import com.myfinance.dao.impl.UserDAOImpl;
import com.myfinance.dao.impl.ExpenseDAOImpl;
import com.myfinance.model.User;
import com.myfinance.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/admin/dashboard")
public class AdminDashServlet extends HttpServlet {
    private UserDAOImpl userDAO;
    private ExpenseDAOImpl expenseDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAOImpl();
        expenseDAO = new ExpenseDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        String role = SessionUtil.getUserRole(request);
        if (!"ADMIN".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        try {
            List<User> allUsers = userDAO.findAll();
            int totalUsers = allUsers.size();
            int activeUsers = (int) allUsers.stream().filter(u -> u.getStatus() == User.Status.ACTIVE).count();
            int totalExpenses = expenseDAO.findAll().size();

            request.setAttribute("totalUsers", totalUsers);
            request.setAttribute("activeUsers", activeUsers);
            request.setAttribute("totalExpenses", totalExpenses);
            request.setAttribute("recentUsers", allUsers.size() > 5 ? allUsers.subList(0, 5) : allUsers);

            request.getRequestDispatcher("/jsp/admin/admin-dashboard.jsp").forward(request, response);
        } catch (SQLException e) {
            request.setAttribute("error", "Error loading admin dashboard: " + e.getMessage());
            request.getRequestDispatcher("/jsp/admin/admin-dashboard.jsp").forward(request, response);
        }
    }
}

package com.myfinance.servlet;

import com.myfinance.dao.impl.UserDAOImpl;
import com.myfinance.dao.interfaces.UserDAO;
import com.myfinance.model.User;
import com.myfinance.util.ApiResponse;
import com.myfinance.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * Controller for user profile management, personal baselines, and API token generation.
 * Endpoint: /profile
 */
@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile"})
public class ProfileServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAOImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        int userId = SessionUtil.getUserId(request);
        try {
            User user = userDAO.findById(userId);
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/auth");
                return;
            }

            // Ensure user has an API token
            if (user.getApiToken() == null || user.getApiToken().trim().isEmpty()) {
                String newToken = User.generateApiToken();
                user.setApiToken(newToken);
                userDAO.updateApiToken(userId, newToken);
            }

            request.setAttribute("user", user);
            request.setAttribute("activeNav", "profile");
            request.getRequestDispatcher("/jsp/user/profile.jsp").forward(request, response);

        } catch (SQLException e) {
            SessionUtil.setFlashError(request, "Failed to load user profile: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        int userId = SessionUtil.getUserId(request);
        String action = request.getParameter("action");

        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))
                || "application/json".equalsIgnoreCase(request.getHeader("Accept"));

        try {
            if ("regenerate-token".equalsIgnoreCase(action)) {
                String newToken = User.generateApiToken();
                userDAO.updateApiToken(userId, newToken);

                if (isAjax) {
                    response.setContentType("application/json");
                    JSONObject res = new JSONObject();
                    res.put("success", true);
                    res.put("token", newToken);
                    res.put("message", "API token refreshed successfully");
                    PrintWriter out = response.getWriter();
                    out.print(res.toString());
                    return;
                }

                SessionUtil.setFlashSuccess(request, "New API token generated successfully!");
                response.sendRedirect(request.getContextPath() + "/profile");
                return;
            }

            if ("update-profile".equalsIgnoreCase(action)) {
                User user = userDAO.findById(userId);
                if (user != null) {
                    String name = request.getParameter("name");
                    String incomeStr = request.getParameter("monthlyIncome");
                    String cashStr = request.getParameter("cashInHand");

                    if (name != null && !name.trim().isEmpty()) {
                        user.setName(name.trim());
                    }
                    if (incomeStr != null && !incomeStr.trim().isEmpty()) {
                        try {
                            user.setMonthlyIncome(Double.parseDouble(incomeStr.trim()));
                        } catch (NumberFormatException ignored) {}
                    }
                    if (cashStr != null && !cashStr.trim().isEmpty()) {
                        try {
                            user.setCashInHand(Double.parseDouble(cashStr.trim()));
                        } catch (NumberFormatException ignored) {}
                    }

                    userDAO.update(user);
                    SessionUtil.setUserName(request, user.getName());
                    SessionUtil.setFlashSuccess(request, "Profile updated successfully!");
                }
                response.sendRedirect(request.getContextPath() + "/profile");
                return;
            }

            response.sendRedirect(request.getContextPath() + "/profile");

        } catch (SQLException e) {
            if (isAjax) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType("application/json");
                JSONObject err = new JSONObject();
                err.put("success", false);
                err.put("message", e.getMessage());
                response.getWriter().print(err.toString());
                return;
            }
            SessionUtil.setFlashError(request, "Error updating profile: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/profile");
        }
    }
}

package com.myfinance.servlet;

import com.myfinance.exception.ValidationException;
import com.myfinance.model.Goal;
import com.myfinance.service.GoalService;
import com.myfinance.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

@WebServlet("/goals")
public class GoalServlet extends HttpServlet {
    private GoalService goalService;

    @Override
    public void init() throws ServletException {
        goalService = new GoalService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        int userId = SessionUtil.getUserId(request);

        try {
            List<Goal> goals = goalService.getAllGoals(userId);

            request.setAttribute("goals", goals);

            request.getRequestDispatcher("/jsp/user/goals.jsp").forward(request, response);
        } catch (SQLException e) {
            request.setAttribute("error", "Error loading goals: " + e.getMessage());
            request.getRequestDispatcher("/jsp/user/goals.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        String action = request.getParameter("action");

        try {
            if ("create".equals(action)) {
                int userId = SessionUtil.getUserId(request);
                Goal goal = new Goal();
                goal.setUserId(userId);
                goal.setTitle(request.getParameter("title"));
                goal.setDescription(request.getParameter("description"));
                goal.setTargetAmount(Double.parseDouble(request.getParameter("targetAmount")));
                goal.setSavedAmount(0.0);
                goal.setDeadline(LocalDate.parse(request.getParameter("deadline")));
                goal.setStatus(Goal.GoalStatus.IN_PROGRESS);

                goalService.createGoal(goal);
                request.getSession().setAttribute("success", "Goal created successfully");
            } else if ("add-savings".equals(action)) {
                int goalId = Integer.parseInt(request.getParameter("id"));
                double amount = Double.parseDouble(request.getParameter("amount"));
                goalService.addSavings(goalId, amount);
                request.getSession().setAttribute("success", "Savings added successfully");
            } else if ("delete".equals(action)) {
                int id = Integer.parseInt(request.getParameter("id"));
                goalService.deleteGoal(id);
                request.getSession().setAttribute("success", "Goal deleted successfully");
            } else if ("edit".equals(action)) {
                Goal goal = new Goal();
                goal.setId(Integer.parseInt(request.getParameter("id")));
                goal.setUserId(SessionUtil.getUserId(request));
                goal.setTitle(request.getParameter("title"));
                goal.setDescription(request.getParameter("description"));
                goal.setTargetAmount(Double.parseDouble(request.getParameter("targetAmount")));
                goal.setSavedAmount(Double.parseDouble(request.getParameter("savedAmount")));
                goal.setDeadline(LocalDate.parse(request.getParameter("deadline")));
                goal.setStatus(Goal.GoalStatus.valueOf(request.getParameter("status")));

                goalService.updateGoal(goal);
                request.getSession().setAttribute("success", "Goal updated successfully");
            }

            response.sendRedirect(request.getContextPath() + "/goals");
        } catch (SQLException | ValidationException e) {
            request.getSession().setAttribute("error", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/goals");
        }
    }
}

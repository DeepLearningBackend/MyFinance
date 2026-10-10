package com.myfinance.servlet;

import com.myfinance.model.Goal;
import com.myfinance.service.GoalService;
import com.myfinance.util.ApiResponse;
import com.myfinance.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller for managing financial savings goals and milestone badges (/goals).
 */
@WebServlet(name = "GoalServlet", urlPatterns = {"/goals"})
public class GoalServlet extends HttpServlet {

    private final GoalService goalService;

    /**
     * Default constructor initializing GoalService.
     */
    public GoalServlet() {
        this.goalService = new GoalService();
    }

    /**
     * Constructor for dependency injection and testing.
     *
     * @param goalService goal service instance
     */
    public GoalServlet(GoalService goalService) {
        this.goalService = goalService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.checkSessionOrRedirect(request, response)) {
            return;
        }

        int userId = SessionUtil.getSessionUserId(request);
        String filterParam = request.getParameter("status");

        Goal.GoalStatus statusFilter = null;
        if (filterParam != null && !filterParam.isEmpty() && !"ALL".equalsIgnoreCase(filterParam)) {
            try {
                statusFilter = Goal.GoalStatus.valueOf(filterParam.toUpperCase());
            } catch (IllegalArgumentException e) {
                statusFilter = null;
            }
        }

        List<Goal> goals = goalService.getUserGoals(userId, statusFilter);
        List<Map<String, Object>> goalCards = new ArrayList<>();

        double totalTarget = 0.0;
        double totalSaved = 0.0;
        int achievedCount = 0;

        for (Goal g : goals) {
            Map<String, Object> details = goalService.getProgressDetails(g);
            details.put("goal", g);
            goalCards.add(details);

            totalTarget += g.getTargetAmount();
            totalSaved += g.getSavedAmount();
            if (g.getStatus() == Goal.GoalStatus.ACHIEVED) {
                achievedCount++;
            }
        }

        request.setAttribute("goalCards", goalCards);
        request.setAttribute("selectedFilter", filterParam != null ? filterParam : "ALL");
        request.setAttribute("totalTarget", totalTarget);
        request.setAttribute("totalSaved", totalSaved);
        request.setAttribute("achievedCount", achievedCount);
        request.setAttribute("totalGoalsCount", goals.size());

        request.getRequestDispatcher("/jsp/user/goals.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.checkSessionOrRedirect(request, response)) {
            return;
        }

        int userId = SessionUtil.getSessionUserId(request);
        String action = request.getParameter("action");
        if (action == null) action = "create";

        switch (action.toLowerCase()) {
            case "add-savings" -> handleAddSavings(request, response, userId);
            case "edit" -> handleEdit(request, response, userId);
            case "delete" -> handleDelete(request, response, userId);
            default -> handleCreate(request, response, userId);
        }
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException {
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String targetStr = request.getParameter("targetAmount");
        String deadlineStr = request.getParameter("deadline");

        try {
            double target = Double.parseDouble(targetStr);
            LocalDate deadline = (deadlineStr != null && !deadlineStr.isEmpty())
                    ? LocalDate.parse(deadlineStr)
                    : LocalDate.now().plusMonths(6);

            Goal goal = new Goal(userId, title, description, target, 0.0, deadline, Goal.GoalStatus.IN_PROGRESS);
            ApiResponse<Goal> result = goalService.createGoal(goal);

            if (result.isSuccess()) {
                SessionUtil.setFlashSuccess(request, "Target goal '" + title + "' initialized!");
            } else {
                SessionUtil.setFlashError(request, result.getMessage());
            }
        } catch (Exception e) {
            SessionUtil.setFlashError(request, "Invalid goal values: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/goals");
    }

    private void handleAddSavings(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException {
        String goalIdStr = request.getParameter("goalId");
        String amountStr = request.getParameter("amount");

        try {
            int goalId = Integer.parseInt(goalIdStr);
            double amount = Double.parseDouble(amountStr);

            ApiResponse<Goal> result = goalService.addSavings(goalId, userId, amount);
            if (result.isSuccess()) {
                SessionUtil.setFlashSuccess(request, result.getMessage());
            } else {
                SessionUtil.setFlashError(request, result.getMessage());
            }
        } catch (Exception e) {
            SessionUtil.setFlashError(request, "Invalid savings amount: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/goals");
    }

    private void handleEdit(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException {
        String idStr = request.getParameter("id");
        String title = request.getParameter("title");
        String description = request.getParameter("description");
        String targetStr = request.getParameter("targetAmount");
        String savedStr = request.getParameter("savedAmount");
        String deadlineStr = request.getParameter("deadline");
        String statusStr = request.getParameter("status");

        try {
            int id = Integer.parseInt(idStr);
            double target = Double.parseDouble(targetStr);
            double saved = Double.parseDouble(savedStr);
            LocalDate deadline = LocalDate.parse(deadlineStr);
            Goal.GoalStatus status = Goal.GoalStatus.valueOf(statusStr);

            Goal goal = new Goal(id, userId, title, description, target, saved, deadline, status, null);
            ApiResponse<Goal> result = goalService.updateGoal(goal);

            if (result.isSuccess()) {
                SessionUtil.setFlashSuccess(request, "Goal updated successfully.");
            } else {
                SessionUtil.setFlashError(request, result.getMessage());
            }
        } catch (Exception e) {
            SessionUtil.setFlashError(request, "Error updating goal: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/goals");
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response, int userId) throws IOException {
        String idStr = request.getParameter("id");
        try {
            int id = Integer.parseInt(idStr);
            ApiResponse<Boolean> result = goalService.deleteGoal(id, userId);
            if (result.isSuccess()) {
                SessionUtil.setFlashSuccess(request, "Goal deleted.");
            } else {
                SessionUtil.setFlashError(request, result.getMessage());
            }
        } catch (Exception e) {
            SessionUtil.setFlashError(request, "Error deleting goal: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/goals");
    }
}

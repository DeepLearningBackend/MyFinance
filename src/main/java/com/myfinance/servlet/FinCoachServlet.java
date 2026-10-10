package com.myfinance.servlet;

import com.myfinance.service.FinCoachService;
import com.myfinance.service.FinCoachService.FinCoachResponse;
import com.myfinance.util.SessionUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Controller for FinCoach AI Financial Education & Personal Data Coach (/fincoach).
 *
 * <p>Golden rules compliance:
 * <ul>
 *   <li>Golden rule 1: Always check session first before processing</li>
 *   <li>Throws user-friendly errors and supports both AJAX JSON responses and JSP views</li>
 * </ul>
 */
@WebServlet(name = "FinCoachServlet", urlPatterns = {"/fincoach"})
public class FinCoachServlet extends HttpServlet {

    private final FinCoachService finCoachService;

    /**
     * Default constructor initializing FinCoachService.
     */
    public FinCoachServlet() {
        this.finCoachService = new FinCoachService();
    }

    /**
     * Constructor for dependency injection and testing.
     *
     * @param finCoachService financial coach service instance
     */
    public FinCoachServlet(FinCoachService finCoachService) {
        this.finCoachService = finCoachService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Golden rule 1: Always check session first
        if (!SessionUtil.checkSessionOrRedirect(request, response)) {
            return;
        }

        int userId = SessionUtil.getSessionUserId(request);
        boolean isGemini = finCoachService.isGeminiConfigured();

        request.setAttribute("isGeminiConfigured", isGemini);
        request.setAttribute("coachMode", isGemini ? "Gemini AI + Live Data" : "Rule-Based (Your Data Only)");
        request.setAttribute("activeNav", "fincoach");

        // If AJAX request asking for initial state
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("application/json")) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            JSONObject json = new JSONObject();
            json.put("isGeminiConfigured", isGemini);
            json.put("mode", isGemini ? "gemini" : "rule-based");
            response.getWriter().write(json.toString());
            return;
        }

        request.getRequestDispatcher("/jsp/user/fincoach.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Golden rule 1: Always check session first
        if (!SessionUtil.checkSessionOrRedirect(request, response)) {
            return;
        }

        int userId = SessionUtil.getSessionUserId(request);

        String question = request.getParameter("question");
        if (question == null || question.trim().isEmpty()) {
            question = request.getParameter("message");
        }

        FinCoachResponse coachResponse = finCoachService.ask(userId, question);

        // Check if AJAX request
        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(requestedWith) || (accept != null && accept.contains("application/json"));

        if (isAjax) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            JSONObject json = new JSONObject();
            json.put("success", true);
            json.put("answer", coachResponse.getAnswer());
            json.put("source", coachResponse.getSource());
            json.put("mode", coachResponse.getMode());
            json.put("disclaimer", coachResponse.getDisclaimer());

            JSONArray calcSourcesArr = new JSONArray();
            if (coachResponse.getCalculationSources() != null) {
                for (String src : coachResponse.getCalculationSources()) {
                    calcSourcesArr.put(src);
                }
            }
            json.put("calculationSources", calcSourcesArr);

            response.getWriter().write(json.toString());
        } else {
            // Traditional form submit fallback
            request.setAttribute("lastQuestion", question);
            request.setAttribute("lastResponse", coachResponse);
            request.setAttribute("isGeminiConfigured", finCoachService.isGeminiConfigured());
            request.setAttribute("activeNav", "fincoach");
            request.getRequestDispatcher("/jsp/user/fincoach.jsp").forward(request, response);
        }
    }
}

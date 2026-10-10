package com.myfinance.servlet;

import com.myfinance.model.PersonalityReport;
import com.myfinance.service.PersonalityService;
import com.myfinance.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Controller for the Spending Personality page (/personality).
 *
 * <p>GET  — runs detection and forwards to personality.jsp.
 * <p>POST — action=refresh forces a fresh detection; action=dismiss clears
 *           the session flag so the reveal animation plays again.
 */
@WebServlet(name = "PersonalityServlet", urlPatterns = {"/personality"})
public class PersonalityServlet extends HttpServlet {

    private final PersonalityService personalityService;

    /**
     * Default constructor initializing PersonalityService.
     */
    public PersonalityServlet() {
        this.personalityService = new PersonalityService();
    }

    /**
     * Constructor for dependency injection and testing.
     *
     * @param personalityService personality service instance
     */
    public PersonalityServlet(PersonalityService personalityService) {
        this.personalityService = personalityService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.checkSessionOrRedirect(request, response)) return;
        if (SessionUtil.isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            return;
        }

        int userId  = SessionUtil.getSessionUserId(request);
        int minDays = personalityService.getMinDays();

        // Try to detect (returns null if insufficient data)
        PersonalityReport report = personalityService.detect(userId);

        // Flag: first time the result is shown this session → trigger animation
        boolean isNewReveal = false;
        if (report != null) {
            String sessionKey = "personality_seen_" + report.getPersonalityType();
            if (request.getSession().getAttribute(sessionKey) == null) {
                isNewReveal = true;
                request.getSession().setAttribute(sessionKey, true);
            }
        }

        request.setAttribute("report",      report);
        request.setAttribute("minDays",     minDays);
        request.setAttribute("isNewReveal", isNewReveal);
        request.setAttribute("windowDays",  PersonalityService.WINDOW_DAYS);

        request.getRequestDispatcher("/jsp/user/personality.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!SessionUtil.checkSessionOrRedirect(request, response)) return;

        String action = request.getParameter("action");
        if ("refresh".equals(action)) {
            // Clear the "seen" flag so animation plays again on next GET
            int userId = SessionUtil.getSessionUserId(request);
            PersonalityReport report = personalityService.detect(userId);
            if (report != null) {
                request.getSession().removeAttribute(
                        "personality_seen_" + report.getPersonalityType());
            }
        }

        response.sendRedirect(request.getContextPath() + "/personality");
    }
}

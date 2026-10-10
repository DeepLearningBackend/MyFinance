package com.myfinance.util;

import com.myfinance.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Utility helper for managing HTTP Sessions and role-based access control.
 */
public final class SessionUtil {

    public static final String SESSION_USER = "loggedInUser";
    public static final String SESSION_USER_ID = "userId";
    public static final String SESSION_USER_NAME = "userName";
    public static final String SESSION_USER_ROLE = "userRole";
    public static final String SESSION_FLASH_ERROR = "flashError";
    public static final String SESSION_FLASH_SUCCESS = "flashSuccess";

    private SessionUtil() {
        // Utility class
    }

    /**
     * Stores the authenticated User entity and attributes in the current HTTP session.
     *
     * @param request current HTTP request
     * @param user authenticated user entity
     */
    public static void setSessionUser(HttpServletRequest request, User user) {
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_USER, user);
        session.setAttribute(SESSION_USER_ID, user.getId());
        session.setAttribute(SESSION_USER_NAME, user.getName());
        session.setAttribute(SESSION_USER_ROLE, user.getRole().name());
    }

    /**
     * Retrieves the authenticated User object from the current HTTP session.
     *
     * @param request current HTTP request
     * @return User object if logged in, or null
     */
    public static User getSessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object obj = session.getAttribute(SESSION_USER);
            if (obj instanceof User) {
                return (User) obj;
            }
        }
        return null;
    }

    /**
     * Retrieves the authenticated user's ID from session.
     *
     * @param request current HTTP request
     * @return user ID as Integer, or null if unauthenticated
     */
    public static Integer getSessionUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object obj = session.getAttribute(SESSION_USER_ID);
            if (obj instanceof Integer) {
                return (Integer) obj;
            }
        }
        return null;
    }

    /**
     * Alias for {@link #getSessionUserId(HttpServletRequest)}.
     *
     * @param request current HTTP request
     * @return user ID as Integer, or null if unauthenticated
     */
    public static Integer getUserId(HttpServletRequest request) {
        return getSessionUserId(request);
    }

    /**
     * Updates the user's display name attribute stored in session.
     *
     * @param request current HTTP request
     * @param name new display name
     */
    public static void setUserName(HttpServletRequest request, String name) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute(SESSION_USER_NAME, name);
        }
    }

    /**
     * Checks whether an active authenticated user session exists.
     *
     * @param request current HTTP request
     * @return true if logged in, false otherwise
     */
    public static boolean isLoggedIn(HttpServletRequest request) {
        return getSessionUserId(request) != null;
    }

    /**
     * Checks whether the currently logged-in user possesses the ADMIN role.
     *
     * @param request current HTTP request
     * @return true if logged in as admin, false otherwise
     */
    public static boolean isAdmin(HttpServletRequest request) {
        User user = getSessionUser(request);
        return user != null && user.isAdmin();
    }

    /**
     * Invalidates the active HTTP session, logging the user out.
     *
     * @param request current HTTP request
     */
    public static void invalidate(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * Sets a temporary flash error banner message in session.
     *
     * @param request current HTTP request
     * @param message error text
     */
    public static void setFlashError(HttpServletRequest request, String message) {
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_FLASH_ERROR, message);
    }

    /**
     * Sets a temporary flash success banner message in session.
     *
     * @param request current HTTP request
     * @param message success text
     */
    public static void setFlashSuccess(HttpServletRequest request, String message) {
        HttpSession session = request.getSession(true);
        session.setAttribute(SESSION_FLASH_SUCCESS, message);
    }

    /**
     * Guards endpoints requiring login; redirects unauthenticated visitors to login.
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @return true if authenticated, false if redirected
     * @throws IOException if redirect response fails
     */
    public static boolean checkSessionOrRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return false;
        }
        return true;
    }

    /**
     * Guards endpoints requiring admin role; redirects non-admins appropriately.
     *
     * @param request current HTTP request
     * @param response current HTTP response
     * @return true if authorized admin, false if redirected
     * @throws IOException if redirect response fails
     */
    public static boolean checkAdminOrRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return false;
        }
        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return false;
        }
        return true;
    }
}

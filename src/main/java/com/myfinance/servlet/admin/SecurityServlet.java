package com.myfinance.servlet.admin;

import com.myfinance.util.SessionUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/admin/security")
public class SecurityServlet extends HttpServlet {

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

        request.getRequestDispatcher("/jsp/admin/security.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!SessionUtil.isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/auth");
            return;
        }

        String role = SessionUtil.getUserRole(request);
        if (!"ADMIN".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        String action = request.getParameter("action");

        if ("update".equals(action)) {
            // Update security settings (placeholder for implementation)
            request.getSession().setAttribute("success", "Security settings updated");
        }

        response.sendRedirect(request.getContextPath() + "/admin/security");
    }
}

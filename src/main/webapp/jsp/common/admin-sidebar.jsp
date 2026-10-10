<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="sidebar-backdrop" id="sidebarBackdrop" onclick="toggleSidebar()"></div>

<aside class="app-sidebar" id="appSidebar">
    <div class="sidebar-brand">
        <div class="sidebar-logo" style="background: linear-gradient(135deg, #ec4899, #8b5cf6);">⚡</div>
        <div class="sidebar-title brand-font">AdminOps</div>
    </div>

    <nav class="sidebar-nav">
        <a href="${pageContext.request.contextPath}/admin/dashboard" 
           class="nav-item ${activeNav eq 'admin-dash' ? 'active' : ''}">
            <span class="nav-icon">📈</span>
            <span>Overview</span>
        </a>

        <a href="${pageContext.request.contextPath}/admin/users" 
           class="nav-item ${activeNav eq 'admin-users' ? 'active' : ''}">
            <span class="nav-icon">👥</span>
            <span>User Accounts</span>
        </a>

        <a href="${pageContext.request.contextPath}/admin/security" 
           class="nav-item ${activeNav eq 'admin-security' ? 'active' : ''}">
            <span class="nav-icon">🛡️</span>
            <span>Security &amp; Audit</span>
        </a>

        <a href="${pageContext.request.contextPath}/admin/settings" 
           class="nav-item ${activeNav eq 'admin-settings' ? 'active' : ''}">
            <span class="nav-icon">⚙️</span>
            <span>System Settings</span>
        </a>

        <div style="margin-top: 14px; padding: 4px 12px; font-size: 0.7rem; font-weight: 700; color: var(--text-dim); text-transform: uppercase;">
            Switch View
        </div>
        <a href="${pageContext.request.contextPath}/dashboard" class="nav-item">
            <span class="nav-icon">👤</span>
            <span>User Dashboard</span>
        </a>
    </nav>

    <div class="sidebar-footer">
        <div class="user-profile-badge">
            <div class="user-avatar" style="background: linear-gradient(135deg, #ec4899, #8b5cf6);">
                A
            </div>
            <div class="user-info">
                <span class="user-name">${sessionScope.userName}</span>
                <span class="user-role-label" style="color: #ec4899;">SYSTEM ADMINISTRATOR</span>
            </div>
        </div>

        <a href="${pageContext.request.contextPath}/logout" class="btn btn-secondary btn-sm" style="width: 100%; justify-content: center; margin-top: 4px;">
            <span>🚪 Sign Out</span>
        </a>
    </div>
</aside>

<script src="${pageContext.request.contextPath}/js/main.js"></script>


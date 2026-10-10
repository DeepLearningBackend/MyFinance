<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="sidebar-backdrop" id="sidebarBackdrop" onclick="toggleSidebar()"></div>

<aside class="app-sidebar" id="appSidebar">
    <div class="sidebar-brand">
        <div class="sidebar-logo">💎</div>
        <div class="sidebar-title brand-font">MyFinance</div>
    </div>

    <nav class="sidebar-nav">
        <a href="${pageContext.request.contextPath}/dashboard" 
           class="nav-item ${activeNav eq 'dashboard' ? 'active' : ''}">
            <span class="nav-icon">📊</span>
            <span>Dashboard</span>
        </a>

        <a href="${pageContext.request.contextPath}/expenses" 
           class="nav-item ${activeNav eq 'expenses' ? 'active' : ''}">
            <span class="nav-icon">💸</span>
            <span>Expenses</span>
        </a>

        <a href="${pageContext.request.contextPath}/budgets" 
           class="nav-item ${activeNav eq 'budgets' ? 'active' : ''}">
            <span class="nav-icon">🛡️</span>
            <span>Budgets</span>
        </a>

        <a href="${pageContext.request.contextPath}/goals" 
           class="nav-item ${activeNav eq 'goals' ? 'active' : ''}">
            <span class="nav-icon">🎯</span>
            <span>Goals</span>
        </a>

        <a href="${pageContext.request.contextPath}/cash" 
           class="nav-item ${activeNav eq 'cash' ? 'active' : ''}">
            <span class="nav-icon">💵</span>
            <span>Cash Tracker</span>
        </a>

        <a href="${pageContext.request.contextPath}/personality" 
           class="nav-item ${activeNav eq 'personality' ? 'active' : ''}">
            <span class="nav-icon">🧠</span>
            <span>My Personality</span>
        </a>

        <a href="${pageContext.request.contextPath}/fincoach" 
           class="nav-item ${activeNav eq 'fincoach' ? 'active' : ''}">
            <span class="nav-icon">🤖</span>
            <span>FinCoach AI</span>
        </a>

        <a href="${pageContext.request.contextPath}/profile" 
           class="nav-item ${activeNav eq 'profile' ? 'active' : ''}">
            <span class="nav-icon">👤</span>
            <span>Profile & Sync</span>
        </a>

        <c:if test="${sessionScope.userRole eq 'ADMIN'}">
            <div style="margin-top: 14px; padding: 4px 12px; font-size: 0.7rem; font-weight: 700; color: var(--primary-light); text-transform: uppercase;">
                Admin Portal
            </div>
            <a href="${pageContext.request.contextPath}/admin/dashboard" class="nav-item">
                <span class="nav-icon">⚡</span>
                <span>Admin Console</span>
            </a>
        </c:if>
    </nav>

    <div class="sidebar-footer">
        <a href="${pageContext.request.contextPath}/profile" class="user-profile-badge" style="text-decoration: none; color: inherit; display: flex;">
            <div class="user-avatar">
                ${not empty sessionScope.userName ? sessionScope.userName.substring(0,1).toUpperCase() : 'U'}
            </div>
            <div class="user-info">
                <span class="user-name">${sessionScope.userName}</span>
                <span class="user-role-label">${sessionScope.userRole}</span>
            </div>
        </a>

        <a href="${pageContext.request.contextPath}/logout" class="btn btn-secondary btn-sm" style="width: 100%; justify-content: center; margin-top: 4px;">
            <span>🚪 Sign Out</span>
        </a>
    </div>
</aside>

<!-- Mobile Bottom Navigation Bar (Screens <= 768px) -->
<nav class="mobile-bottom-nav">
    <a href="${pageContext.request.contextPath}/dashboard" class="mobile-nav-item ${activeNav eq 'dashboard' ? 'active' : ''}">
        <span class="mobile-nav-icon">📊</span>
        <span>Home</span>
    </a>
    <a href="${pageContext.request.contextPath}/expenses" class="mobile-nav-item ${activeNav eq 'expenses' ? 'active' : ''}">
        <span class="mobile-nav-icon">💸</span>
        <span>Expenses</span>
    </a>
    <a href="${pageContext.request.contextPath}/budgets" class="mobile-nav-item ${activeNav eq 'budgets' ? 'active' : ''}">
        <span class="mobile-nav-icon">🛡️</span>
        <span>Budgets</span>
    </a>
    <a href="${pageContext.request.contextPath}/goals" class="mobile-nav-item ${activeNav eq 'goals' ? 'active' : ''}">
        <span class="mobile-nav-icon">🎯</span>
        <span>Goals</span>
    </a>
    <a href="${pageContext.request.contextPath}/cash" class="mobile-nav-item ${activeNav eq 'cash' ? 'active' : ''}">
        <span class="mobile-nav-icon">💵</span>
        <span>Cash</span>
    </a>
    <a href="${pageContext.request.contextPath}/personality" class="mobile-nav-item ${activeNav eq 'personality' ? 'active' : ''}">
        <span class="mobile-nav-icon">🧠</span>
        <span>Personality</span>
    </a>
    <a href="${pageContext.request.contextPath}/fincoach" class="mobile-nav-item ${activeNav eq 'fincoach' ? 'active' : ''}">
        <span class="mobile-nav-icon">🤖</span>
        <span>Coach</span>
    </a>
    <a href="${pageContext.request.contextPath}/profile" class="mobile-nav-item ${activeNav eq 'profile' ? 'active' : ''}">
        <span class="mobile-nav-icon">👤</span>
        <span>Profile</span>
    </a>
</nav>

<script src="${pageContext.request.contextPath}/js/main.js"></script>


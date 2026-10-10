<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<header class="app-topbar">
    <div class="topbar-left">
        <button class="hamburger-btn" onclick="toggleSidebar()" aria-label="Toggle Navigation">
            ☰
        </button>
        <h1 class="topbar-page-title">${pageTitle}</h1>
    </div>

    <div class="topbar-right">
        <div id="liveSyncIndicator" title="Real-Time Mobile SMS Sync" style="font-size: 0.85rem; color: var(--text-muted); display: flex; align-items: center; gap: 8px; cursor: pointer;" onclick="location.href='${pageContext.request.contextPath}/profile'">
            <span id="liveSyncDot" style="display: inline-block; width: 8px; height: 8px; border-radius: 50%; background: #64748b; transition: all 0.3s ease;"></span>
            <span id="liveSyncText">No SMS synced yet</span>
        </div>
        <a href="${pageContext.request.contextPath}/expenses" class="btn btn-primary btn-sm">
            <span>+ Quick Expense</span>
        </a>
    </div>
</header>

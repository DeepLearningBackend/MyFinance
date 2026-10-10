<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="activeNav" value="budgets" scope="request"/>
<c:set var="pageTitle" value="Budget Guard & Limits" scope="request"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Budgets — MyFinance</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/responsive.css">
</head>
<body>
    <div class="app-wrapper">
        <jsp:include page="/jsp/common/sidebar.jsp"/>

        <div class="app-main">
            <jsp:include page="/jsp/common/topbar.jsp"/>

            <div class="content-body">
                <!-- Flash Feedback -->
                <c:if test="${not empty sessionScope.flashSuccess}">
                    <div class="alert-banner alert-banner-success">
                        <span>✨ ${sessionScope.flashSuccess}</span>
                    </div>
                    <c:remove var="flashSuccess" scope="session"/>
                </c:if>

                <c:if test="${not empty sessionScope.flashError}">
                    <div class="alert-banner alert-banner-danger">
                        <span>⚠️ ${sessionScope.flashError}</span>
                    </div>
                    <c:remove var="flashError" scope="session"/>
                </c:if>

                <!-- Smart Budget Alerts (Innovation 1) -->
                <c:forEach var="alert" items="${alerts}">
                    <c:set var="isOver" value="${alert.contains('over limit') || alert.contains('OVER LIMIT') || alert.contains('−')}"/>
                    <div class="alert-banner ${isOver ? 'alert-banner-danger' : 'alert-banner-warning'}">
                        <span>${alert}</span>
                    </div>
                </c:forEach>

                <!-- Stats Overview -->
                <div class="stats-grid">
                    <div class="glass-card stat-card">
                        <div class="stat-info">
                            <span class="stat-label">Total Allocated</span>
                            <span class="stat-value">₹<fmt:formatNumber value="${totalBudget}" pattern="#,##0.00"/></span>
                            <span class="stat-trend" style="color: var(--primary-light);">🛡️ Monthly spending ceiling</span>
                        </div>
                        <div class="stat-icon-wrapper" style="color: #a855f7;">📋</div>
                    </div>

                    <div class="glass-card stat-card">
                        <div class="stat-info">
                            <span class="stat-label">Total Spent</span>
                            <span class="stat-value">₹<fmt:formatNumber value="${totalSpent}" pattern="#,##0.00"/></span>
                            <span class="stat-trend" style="color: ${totalSpent > totalBudget ? '#ef4444' : '#10b981'};">
                                ${totalBudget > 0 ? String.format('%.2f', (totalSpent/totalBudget)*100) : '0.00'}% of allocation
                            </span>
                        </div>
                        <div class="stat-icon-wrapper" style="color: #06b6d4;">💸</div>
                    </div>

                    <div class="glass-card stat-card">
                        <div class="stat-info">
                            <span class="stat-label">Remaining Buffer</span>
                            <c:choose>
                                <c:when test="${totalRemaining < 0}">
                                    <span class="stat-value" style="color: #ef4444;">−₹<fmt:formatNumber value="${Math.abs(totalRemaining)}" pattern="#,##0.00"/></span>
                                    <span class="stat-trend" style="color: #ef4444;">🚨 Over budget limit</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="stat-value">₹<fmt:formatNumber value="${totalRemaining}" pattern="#,##0.00"/></span>
                                    <span class="stat-trend" style="color: #10b981;">✨ Available to spend</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="stat-icon-wrapper" style="color: ${totalRemaining < 0 ? '#ef4444' : '#10b981'};">🛡️</div>
                    </div>

                    <div class="glass-card stat-card">
                        <div class="stat-info">
                            <span class="stat-label">Active Categories</span>
                            <span class="stat-value">${budgets.size()}</span>
                            <span class="stat-trend" style="color: #ec4899;">📊 Tracked categories</span>
                        </div>
                        <div class="stat-icon-wrapper" style="color: #ec4899;">🎯</div>
                    </div>
                </div>

                <!-- Two Column Layout: Budget Cards + Create Form -->
                <div class="grid-main-side">
                    <!-- Left: Active Budget Progress Cards -->
                    <div style="display: flex; flex-direction: column; gap: 20px;">
                        <c:forEach var="b" items="${budgets}">
                            <c:set var="pct" value="${b.getUsagePercent()}"/>
                            <c:set var="remaining" value="${b.getRemainingAmount()}"/>
                            <div class="glass-card">
                                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px;">
                                    <div>
                                        <h3 style="font-size: 1.15rem; margin-bottom: 4px;">${b.category}</h3>
                                        <div style="font-size: 0.8rem; color: var(--text-dim);">
                                            Period: ${b.startDate} to ${b.endDate}
                                        </div>
                                    </div>
                                    <div style="display: flex; align-items: center; gap: 8px;">
                                        <span class="badge ${pct > 100 ? 'badge-danger' : (pct >= 80 ? 'badge-warning' : 'badge-success')}">
                                            <fmt:formatNumber value="${pct}" pattern="#,##0.00"/>% Used
                                        </span>
                                        <form action="${pageContext.request.contextPath}/budgets" method="post" style="display: inline;"
                                              onsubmit="return confirm('Remove budget limit for ${b.category}?');">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${b.id}">
                                            <button type="submit" class="btn btn-danger btn-sm" title="Delete Budget">🗑️</button>
                                        </form>
                                    </div>
                                </div>

                                <div class="progress-bar-container" style="height: 12px; margin-bottom: 14px;">
                                    <div class="progress-bar-fill ${pct > 100 ? 'progress-red' : (pct >= 80 ? 'progress-amber' : 'progress-green')}" 
                                         style="width: ${pct > 100 ? 100 : pct}%;"></div>
                                </div>

                                <div style="display: flex; justify-content: space-between; font-size: 0.9rem; align-items: center; flex-wrap: wrap; gap: 8px;">
                                    <div>
                                        <span style="color: var(--text-secondary); font-size: 0.88rem;">
                                            ₹<fmt:formatNumber value="${b.limitAmount}" pattern="#,##0.00"/> budget − ₹<fmt:formatNumber value="${b.spentAmount}" pattern="#,##0.00"/> spent = 
                                        </span>
                                        <strong style="color: ${pct > 100 ? '#ef4444' : (pct >= 80 ? '#f59e0b' : '#10b981')}; font-size: 0.92rem;">
                                            <c:choose>
                                                <c:when test="${remaining < 0}">
                                                    −₹<fmt:formatNumber value="${Math.abs(remaining)}" pattern="#,##0.00"/> (over limit)
                                                </c:when>
                                                <c:otherwise>
                                                    ₹<fmt:formatNumber value="${remaining}" pattern="#,##0.00"/> left
                                                </c:otherwise>
                                            </c:choose>
                                        </strong>
                                    </div>
                                    <div style="font-weight: 600; font-size: 0.85rem; color: ${pct > 100 ? '#ef4444' : (pct >= 80 ? '#f59e0b' : '#10b981')};">
                                        <fmt:formatNumber value="${pct}" pattern="#,##0.00"/>% used
                                    </div>
                                </div>

                                <c:if test="${pct >= 80}">
                                    <div style="margin-top: 14px; padding: 10px 14px; background: ${pct > 100 ? 'rgba(239, 68, 68, 0.12)' : 'rgba(245, 158, 11, 0.12)'}; border: 1px solid ${pct > 100 ? 'var(--danger-border)' : 'rgba(245, 158, 11, 0.3)'}; border-radius: var(--radius-sm); font-size: 0.825rem; color: ${pct > 100 ? '#fca5a5' : '#fde68a'};">
                                        <c:choose>
                                            <c:when test="${pct > 100}">
                                                🚨 <strong>Spend Guard Alert:</strong> Oops, you've gone over 🙈! You are over budget for ${b.category}.
                                            </c:when>
                                            <c:otherwise>
                                                ⚠️ <strong>Spend Guard Warning:</strong> You have consumed over 80% of your allocated budget for ${b.category}.
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </c:if>
                            </div>
                        </c:forEach>

                        <c:if test="${empty budgets}">
                            <div class="glass-card" style="text-align: center; padding: 40px; color: var(--text-muted);">
                                <div style="font-size: 2.2rem; margin-bottom: 10px;">🛡️</div>
                                <h3>No Budgets Set</h3>
                                <p style="margin-top: 6px;">Setting category budgets protects you from overspending before it happens.</p>
                            </div>
                        </c:if>
                    </div>

                    <!-- Right: Establish Budget Card -->
                    <div class="glass-card" style="height: fit-content;">
                        <div style="margin-bottom: 20px;">
                            <h3 style="font-size: 1.2rem;">Set Category Budget</h3>
                            <p style="font-size: 0.85rem; color: var(--text-muted);">Define spending limits with automatic alerts</p>
                        </div>

                        <form action="${pageContext.request.contextPath}/budgets" method="post" id="budgetForm">
                            <input type="hidden" name="action" value="create">

                            <div class="form-group">
                                <label class="form-label" for="category">Category</label>
                                <select id="category" name="category" class="form-control" required>
                                    <c:forEach var="cat" items="${categories}">
                                        <option value="${cat}">${cat}</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="form-group">
                                <label class="form-label" for="limitAmount">Monthly Limit (₹)</label>
                                <input type="number" step="1" min="100" id="limitAmount" name="limitAmount" class="form-control" 
                                       placeholder="5000" required>
                            </div>

                            <div class="form-group">
                                <label class="form-label" for="startDate">Start Date</label>
                                <input type="date" id="startDate" name="startDate" class="form-control" required>
                            </div>

                            <div class="form-group">
                                <label class="form-label" for="endDate">End Date</label>
                                <input type="date" id="endDate" name="endDate" class="form-control" required>
                            </div>

                            <div style="margin-top: 24px;">
                                <button type="submit" class="btn btn-primary" style="width: 100%; padding: 12px;">
                                    🛡️ Establish Budget Guard
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script src="${pageContext.request.contextPath}/js/validation.js"></script>
    <script>
        // Set default month start and end dates
        const date = new Date();
        const firstDay = new Date(date.getFullYear(), date.getMonth(), 1);
        const lastDay = new Date(date.getFullYear(), date.getMonth() + 1, 0);
        document.getElementById('startDate').valueAsDate = firstDay;
        document.getElementById('endDate').valueAsDate = lastDay;

        function toggleSidebar() {
            const sidebar = document.getElementById('appSidebar');
            const backdrop = document.getElementById('sidebarBackdrop');
            sidebar.classList.toggle('open');
            backdrop.classList.toggle('active');
        }
    </script>
</body>
</html>

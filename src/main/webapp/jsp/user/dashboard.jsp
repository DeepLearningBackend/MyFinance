<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="activeNav" value="dashboard" scope="request"/>
<c:set var="pageTitle" value="Financial Overview" scope="request"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard — MyFinance</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/responsive.css">
</head>
<body>
    <div class="app-wrapper">
        <!-- Sidebar Navigation -->
        <jsp:include page="/jsp/common/sidebar.jsp"/>

        <div class="app-main">
            <!-- Topbar -->
            <jsp:include page="/jsp/common/topbar.jsp"/>

            <div class="content-body">
                <!-- Flash Messages -->
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
                <c:forEach var="alert" items="${budgetAlerts}">
                    <c:set var="isOver" value="${alert.contains('over limit') || alert.contains('OVER LIMIT') || alert.contains('−')}"/>
                    <div class="alert-banner ${isOver ? 'alert-banner-danger' : 'alert-banner-warning'}">
                        <span>${alert}</span>
                        <a href="${pageContext.request.contextPath}/budgets" class="badge ${isOver ? 'badge-danger' : 'badge-warning'}" style="text-decoration:none;">View Budget</a>
                    </div>
                </c:forEach>

                <!-- Plain Insight Card (Innovation 4) -->
                <div class="insight-card pulse-card">
                    <div style="display: flex; align-items: center; gap: 14px;">
                        <span style="font-size: 1.6rem;">💡</span>
                        <div class="insight-text">
                            ${weeklyInsight}
                        </div>
                    </div>
                    <a href="${pageContext.request.contextPath}/expenses" class="btn btn-secondary btn-sm" style="white-space: nowrap;">
                        View Breakdown &rarr;
                    </a>
                </div>

                <!-- ══════════════════════════════════════════════════════════
                     Daily Allowance Hero Card  (Innovation 4 – Allowance Engine)
                     Transparent automation: every number in the formula is shown.
                     ══════════════════════════════════════════════════════════ -->
                <div class="glass-card allowance-hero-card" style="
                        background: linear-gradient(135deg,#1a1040 0%,#0f172a 100%);
                        border: 1px solid rgba(168,85,247,0.35);
                        border-radius: 18px;
                        padding: 28px 32px;
                        margin-bottom: 24px;
                        display: flex;
                        flex-wrap: wrap;
                        align-items: center;
                        gap: 24px;">

                    <!-- Big hero number -->
                    <div style="flex: 1; min-width: 200px;">
                        <p style="font-size: 0.8rem; font-weight: 600; letter-spacing: .12em;
                                  text-transform: uppercase; color: #a78bfa; margin-bottom: 4px;">
                            💰 Left To Spend Today
                        </p>
                        <c:choose>
                            <c:when test="${!allowance.hasBudgets}">
                                <div style="margin: 10px 0;">
                                    <p style="font-size: 1.1rem; font-weight: 600; color: #cbd5e1; margin-bottom: 8px;">
                                        Set a budget to see your daily allowance
                                    </p>
                                    <a href="${pageContext.request.contextPath}/budgets" class="btn btn-primary btn-sm" style="font-weight: 600;">
                                        🛡️ Set Budget &rarr;
                                    </a>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <c:choose>
                                    <c:when test="${allowance.dailyAllowance < 0}">
                                        <p style="font-size: 2.8rem; font-weight: 800; color: #ef4444;
                                                  line-height: 1; margin-bottom: 6px;">
                                            −₹<fmt:formatNumber value="${Math.abs(allowance.monthlyRemaining)}" maxFractionDigits="2" minFractionDigits="0"/>
                                            <span style="font-size: 1rem; font-weight: 600; color: #ef4444;">over budget this month</span>
                                        </p>
                                        <p style="font-size: 0.85rem; color: #fca5a5; margin-bottom: 8px; font-weight: 500;">
                                            That's −₹<fmt:formatNumber value="${Math.abs(allowance.dailyAllowance)}" pattern="#,##0.00"/>/day over your remaining ${allowance.daysLeft} day${allowance.daysLeft == 1 ? '' : 's'}
                                        </p>
                                    </c:when>
                                    <c:otherwise>
                                        <p style="font-size: 2.8rem; font-weight: 800; color: #f0fdf4;
                                                  line-height: 1; margin-bottom: 6px;">
                                            ₹<fmt:formatNumber value="${allowance.dailyAllowance}" pattern="#,##0.00"/>
                                            <span style="font-size: 1rem; font-weight: 400; color: #86efac;">/ day</span>
                                        </p>
                                    </c:otherwise>
                                </c:choose>
                            </c:otherwise>
                        </c:choose>
                        <!-- Transparent formula (no hidden magic) -->
                        <p style="font-size: 0.78rem; color: #94a3b8; font-family: monospace;">
                            ${allowance.formula}
                        </p>
                        <c:if test="${allowance.hasBudgets}">
                            <p style="font-size: 0.76rem; color: #94a3b8; margin-top: 6px;">
                                Spending outside your budgets: ₹<fmt:formatNumber value="${allowance.unbudgetedSpend}" pattern="#,##0.00"/> (not counted above)
                            </p>
                        </c:if>
                    </div>

                    <!-- FinanceOperation summaries panel -->
                    <div style="flex: 2; min-width: 260px;
                                display: grid; grid-template-columns: 1fr 1fr;
                                gap: 10px 20px;">
                        <c:forEach var="summary" items="${opSummaries}">
                            <div style="background: rgba(255,255,255,0.05);
                                        border-radius: 10px;
                                        padding: 10px 14px;
                                        font-size: 0.8rem;
                                        color: #cbd5e1;
                                        border-left: 3px solid #a855f7;">
                                ${summary}
                            </div>
                        </c:forEach>
                    </div>
                </div>

                <!-- Stats Overview Grid -->
                <div class="stats-grid">
                    <div class="glass-card stat-card">
                        <div class="stat-info">
                            <span class="stat-label">This Month (${currentMonthName})</span>
                            <span class="stat-value">₹<fmt:formatNumber value="${monthlyTotal}" pattern="#,##0.00"/></span>
                            <small style="font-size: 0.75rem; color: var(--text-dim); display: block; margin-top: 2px;">All categories, digital</small>
                            <span class="stat-trend" style="color: var(--primary-light);">📅 Current billing cycle</span>
                        </div>
                        <div class="stat-icon-wrapper" style="color: #a855f7;">💳</div>
                    </div>

                    <div class="glass-card stat-card">
                        <div class="stat-info">
                            <span class="stat-label">All-Time Expenditure</span>
                            <span class="stat-value">₹<fmt:formatNumber value="${allTimeTotal}" pattern="#,##0.00"/></span>
                            <small style="font-size: 0.75rem; color: var(--text-dim); display: block; margin-top: 2px;">All categories, cumulative</small>
                            <span class="stat-trend" style="color: var(--secondary);">📊 Cumulative logged</span>
                        </div>
                        <div class="stat-icon-wrapper" style="color: #06b6d4;">💸</div>
                    </div>

                    <div class="glass-card stat-card">
                        <div class="stat-info">
                            <span class="stat-label">Budget Allocated</span>
                            <span class="stat-value">₹<fmt:formatNumber value="${totalBudgetLimit}" pattern="#,##0.00"/></span>
                            <small style="font-size: 0.75rem; color: var(--text-dim); display: block; margin-top: 2px;">Only categories with a budget</small>
                            <span class="stat-trend" style="color: ${totalBudgetSpent > totalBudgetLimit ? '#ef4444' : '#10b981'};">
                                <c:choose>
                                    <c:when test="${totalBudgetSpent > totalBudgetLimit}">
                                        −₹<fmt:formatNumber value="${totalBudgetSpent - totalBudgetLimit}" pattern="#,##0.00"/> (over limit)
                                    </c:when>
                                    <c:otherwise>
                                        ₹<fmt:formatNumber value="${totalBudgetLimit - totalBudgetSpent}" pattern="#,##0.00"/> left
                                    </c:otherwise>
                                </c:choose>
                                (${totalBudgetLimit > 0 ? String.format('%.2f', (totalBudgetSpent/totalBudgetLimit)*100) : '0.00'}% used)
                            </span>
                        </div>
                        <div class="stat-icon-wrapper" style="color: #10b981;">🛡️</div>
                    </div>

                    <div class="glass-card stat-card">
                        <div class="stat-info">
                            <span class="stat-label">Active Goals</span>
                            <span class="stat-value">${goalCards.size()}</span>
                            <small style="font-size: 0.75rem; color: var(--text-dim); display: block; margin-top: 2px;">Target savings milestones</small>
                            <span class="stat-trend" style="color: #ec4899;">🎯 Milestones in progress</span>
                        </div>
                        <div class="stat-icon-wrapper" style="color: #ec4899;">🏆</div>
                    </div>
                </div>

                <!-- Main Section: Charts + Recent Transactions & Budgets/Goals -->
                <div class="grid-main-side">
                    <!-- Left: Spending Donut + Recent Transactions -->
                    <div style="display: flex; flex-direction: column; gap: 24px;">
                        <!-- Spending Chart Card -->
                        <div class="glass-card">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px;">
                                <div>
                                    <h3 style="font-size: 1.15rem;">Spending by Category</h3>
                                    <p style="font-size: 0.85rem; color: var(--text-muted);">Visual breakdown for active period</p>
                                </div>
                                <span class="badge badge-primary">${currentMonthName} ${currentYear}</span>
                            </div>

                            <c:choose>
                                <c:when test="${not empty categoryTotals}">
                                    <div style="display: flex; align-items: center; justify-content: space-around; flex-wrap: wrap; gap: 20px;">
                                        <canvas id="categoryDonutChart" width="220" height="220"></canvas>
                                        <div id="categoryLegend" style="display: flex; flex-direction: column; gap: 8px; max-height: 220px; overflow-y: auto; padding-right: 8px;">
                                            <!-- Dynamically populated by charts.js -->
                                        </div>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div style="text-align: center; padding: 40px 20px; color: var(--text-muted);">
                                        <div style="font-size: 2.2rem; margin-bottom: 8px;">📊</div>
                                        <p>No expenses logged for this month yet.</p>
                                        <a href="${pageContext.request.contextPath}/expenses" class="btn btn-secondary btn-sm" style="margin-top: 12px;">+ Record First Expense</a>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <!-- Recent Transactions -->
                        <div class="glass-card">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px;">
                                <div>
                                    <h3 style="font-size: 1.15rem;">Recent Expenses</h3>
                                    <p style="font-size: 0.85rem; color: var(--text-muted);">Latest logged expenditures</p>
                                </div>
                                <a href="${pageContext.request.contextPath}/expenses" class="btn btn-secondary btn-sm">View All &rarr;</a>
                            </div>

                            <div class="table-responsive">
                                <table class="custom-table">
                                    <thead>
                                        <tr>
                                            <th>Date</th>
                                            <th>Description</th>
                                            <th>Category</th>
                                            <th style="text-align: right;">Amount</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="exp" items="${recentExpenses}">
                                            <tr>
                                                <td style="color: var(--text-muted);">${exp.expenseDate}</td>
                                                <td style="font-weight: 500;">${exp.title}</td>
                                                <td>
                                                    <span class="badge badge-primary">${exp.category}</span>
                                                </td>
                                                <td style="text-align: right; font-family: 'Space Grotesk', sans-serif; font-weight: 700; color: #f8fafc;">
                                                    -₹<fmt:formatNumber value="${exp.amount}" pattern="#,##0.00"/>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        <c:if test="${empty recentExpenses}">
                                            <tr>
                                                <td colspan="4" style="text-align: center; color: var(--text-muted); padding: 30px;">
                                                    No expenses recorded. Click "+ Quick Expense" to get started!
                                                </td>
                                            </tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                    <!-- Right Column: Active Budgets & Target Goals -->
                    <div style="display: flex; flex-direction: column; gap: 24px;">
                        <!-- Budget Trackers -->
                        <div class="glass-card">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px;">
                                <h3 style="font-size: 1.15rem;">Budget Limits</h3>
                                <a href="${pageContext.request.contextPath}/budgets" class="btn btn-secondary btn-sm">+ Manage</a>
                            </div>

                            <div style="display: flex; flex-direction: column; gap: 16px;">
                                <c:forEach var="b" items="${budgets}">
                                    <c:set var="usage" value="${b.getUsagePercent()}"/>
                                    <c:set var="rem" value="${b.getRemainingAmount()}"/>
                                    <div>
                                        <div style="display: flex; justify-content: space-between; font-size: 0.85rem; margin-bottom: 6px;">
                                            <span style="font-weight: 600;">${b.category}</span>
                                            <span style="color: ${usage > 100 ? '#ef4444' : (usage >= 80 ? '#f59e0b' : '#10b981')}; font-weight: 600;">
                                                <c:choose>
                                                    <c:when test="${rem < 0}">
                                                        ₹<fmt:formatNumber value="${b.limitAmount}" pattern="#,##0.00"/> budget − ₹<fmt:formatNumber value="${b.spentAmount}" pattern="#,##0.00"/> spent = −₹<fmt:formatNumber value="${Math.abs(rem)}" pattern="#,##0.00"/> (over limit)
                                                    </c:when>
                                                    <c:otherwise>
                                                        ₹<fmt:formatNumber value="${b.limitAmount}" pattern="#,##0.00"/> budget − ₹<fmt:formatNumber value="${b.spentAmount}" pattern="#,##0.00"/> spent = ₹<fmt:formatNumber value="${rem}" pattern="#,##0.00"/> left
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>
                                        <div class="progress-bar-container">
                                            <div class="progress-bar-fill ${usage > 100 ? 'progress-red' : (usage >= 80 ? 'progress-amber' : 'progress-green')}" 
                                                 style="width: ${usage > 100 ? 100 : usage}%;"></div>
                                        </div>
                                    </div>
                                </c:forEach>

                                <c:if test="${empty budgets}">
                                    <div style="text-align: center; color: var(--text-muted); padding: 20px 0;">
                                        <p>No budgets configured.</p>
                                        <a href="${pageContext.request.contextPath}/budgets" class="btn btn-primary btn-sm" style="margin-top: 8px;">Set Category Budget</a>
                                    </div>
                                </c:if>
                            </div>
                        </div>

                        <!-- Target Goals -->
                        <div class="glass-card">
                            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px;">
                                <h3 style="font-size: 1.15rem;">Goal Progress</h3>
                                <a href="${pageContext.request.contextPath}/goals" class="btn btn-secondary btn-sm">+ Set Goal</a>
                            </div>

                            <div style="display: flex; flex-direction: column; gap: 16px;">
                                <c:forEach var="item" items="${goalCards}">
                                    <div style="background: rgba(255, 255, 255, 0.02); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); padding: 14px;">
                                        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
                                            <span style="font-weight: 600; font-size: 0.95rem;">${item.goal.title}</span>
                                            <span class="goal-milestone-pill">${item.milestoneBadge}</span>
                                        </div>

                                        <div class="progress-bar-container" style="margin-bottom: 8px;">
                                            <div class="progress-bar-fill progress-purple" style="width: ${item.percent}%;"></div>
                                        </div>

                                        <div style="display: flex; justify-content: space-between; font-size: 0.8rem; color: var(--text-muted);">
                                            <span>₹<fmt:formatNumber value="${item.goal.savedAmount}" pattern="#,##0"/> of ₹<fmt:formatNumber value="${item.goal.targetAmount}" pattern="#,##0"/> (${item.percentFormatted}%)</span>
                                            <span class="goal-countdown-badge">⏳ ${item.daysLeft}d left</span>
                                        </div>
                                    </div>
                                </c:forEach>

                                <c:if test="${empty goalCards}">
                                    <div style="text-align: center; color: var(--text-muted); padding: 20px 0;">
                                        <p>No active financial goals.</p>
                                        <a href="${pageContext.request.contextPath}/goals" class="btn btn-primary btn-sm" style="margin-top: 8px;">Create First Goal</a>
                                    </div>
                                </c:if>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Pass category data to JS charts -->
    <script>
        window.categoryTotalsData = {
            <c:forEach var="entry" items="${categoryTotals}" varStatus="status">
                "${entry.key}": ${entry.value}${!status.last ? ',' : ''}
            </c:forEach>
        };
    </script>
    <script src="${pageContext.request.contextPath}/js/charts.js"></script>
    <script src="${pageContext.request.contextPath}/js/offline.js"></script>
    <script src="${pageContext.request.contextPath}/js/sms-sync.js"></script>
    <script>
        function toggleSidebar() {
            const sidebar = document.getElementById('appSidebar');
            const backdrop = document.getElementById('sidebarBackdrop');
            sidebar.classList.toggle('open');
            backdrop.classList.toggle('active');
        }

        document.addEventListener('DOMContentLoaded', () => {
            if (window.categoryTotalsData && Object.keys(window.categoryTotalsData).length > 0) {
                renderCategoryDonut('categoryDonutChart', 'categoryLegend', window.categoryTotalsData);
            }
        });
    </script>
</body>
</html>

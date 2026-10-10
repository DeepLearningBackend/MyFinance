<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="activeNav" value="goals" scope="request"/>
<c:set var="pageTitle" value="Goal Motivation Engine" scope="request"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Goals — MyFinance</title>
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

                <!-- Stats Overview -->
                <div class="stats-grid">
                    <div class="glass-card stat-card">
                        <div class="stat-info">
                            <span class="stat-label">Total Target</span>
                            <span class="stat-value">₹<fmt:formatNumber value="${totalTarget}" pattern="#,##0.00"/></span>
                            <span class="stat-trend" style="color: var(--primary-light);">🎯 Target goal portfolio</span>
                        </div>
                        <div class="stat-icon-wrapper" style="color: #a855f7;">🏆</div>
                    </div>

                    <div class="glass-card stat-card">
                        <div class="stat-info">
                            <span class="stat-label">Total Saved</span>
                            <span class="stat-value">₹<fmt:formatNumber value="${totalSaved}" pattern="#,##0.00"/></span>
                            <span class="stat-trend" style="color: #10b981;">
                                ${totalTarget > 0 ? String.format('%.1f', (totalSaved/totalTarget)*100) : 0}% aggregate progress
                            </span>
                        </div>
                        <div class="stat-icon-wrapper" style="color: #10b981;">💰</div>
                    </div>

                    <div class="glass-card stat-card">
                        <div class="stat-info">
                            <span class="stat-label">Achieved Goals</span>
                            <span class="stat-value">${achievedCount}</span>
                            <span class="stat-trend" style="color: #ec4899;">🎉 Targets unlocked</span>
                        </div>
                        <div class="stat-icon-wrapper" style="color: #ec4899;">⭐</div>
                    </div>

                    <div class="glass-card stat-card">
                        <div class="stat-info">
                            <span class="stat-label">In Progress</span>
                            <span class="stat-value">${totalGoalsCount - achievedCount}</span>
                            <span class="stat-trend" style="color: #06b6d4;">⏳ Active countdowns</span>
                        </div>
                        <div class="stat-icon-wrapper" style="color: #06b6d4;">🚀</div>
                    </div>
                </div>

                <!-- Filter Header Pills -->
                <div style="display: flex; gap: 10px; margin-bottom: 24px; flex-wrap: wrap;">
                    <a href="${pageContext.request.contextPath}/goals" 
                       class="btn ${selectedFilter eq 'ALL' ? 'btn-primary' : 'btn-secondary'} btn-sm">
                        All Goals (${totalGoalsCount})
                    </a>
                    <a href="${pageContext.request.contextPath}/goals?status=IN_PROGRESS" 
                       class="btn ${selectedFilter eq 'IN_PROGRESS' ? 'btn-primary' : 'btn-secondary'} btn-sm">
                        ⏳ In Progress (${totalGoalsCount - achievedCount})
                    </a>
                    <a href="${pageContext.request.contextPath}/goals?status=ACHIEVED" 
                       class="btn ${selectedFilter eq 'ACHIEVED' ? 'btn-primary' : 'btn-secondary'} btn-sm">
                        🏆 Achieved (${achievedCount})
                    </a>
                </div>

                <!-- Two Column Layout: Goal Cards List + Create Form -->
                <div class="grid-main-side">
                    <!-- Left: Goal Motivation Cards List -->
                    <div style="display: flex; flex-direction: column; gap: 20px;">
                        <c:forEach var="item" items="${goalCards}">
                            <c:set var="g" value="${item.goal}"/>
                            <div class="glass-card ${item.isAchieved ? 'pulse-card' : ''}" 
                                 style="${item.isAchieved ? 'border-color: rgba(16, 185, 129, 0.4);' : ''}">
                                
                                <c:if test="${item.isAchieved}">
                                    <div style="margin-bottom: 12px; padding: 6px 12px; background: var(--success-bg); border: 1px solid var(--success-border); border-radius: var(--radius-sm); font-size: 0.8rem; font-weight: 700; color: #6ee7b7; display: flex; align-items: center; gap: 6px;">
                                        🎉 TARGET FULLY ACHIEVED! CONGRATULATIONS!
                                    </div>
                                </c:if>

                                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px;">
                                    <div>
                                        <h3 style="font-size: 1.25rem; margin-bottom: 4px;">${g.title}</h3>
                                        <c:if test="${not empty g.description}">
                                            <p style="font-size: 0.85rem; color: var(--text-muted);">${g.description}</p>
                                        </c:if>
                                    </div>

                                    <div style="display: flex; align-items: center; gap: 8px;">
                                        <span class="goal-milestone-pill">${item.milestoneBadge}</span>
                                        <form action="${pageContext.request.contextPath}/goals" method="post" style="display: inline;"
                                              onsubmit="return confirm('Remove goal \'${g.title}\'?');">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${g.id}">
                                            <button type="submit" class="btn btn-danger btn-sm" title="Delete Goal">🗑️</button>
                                        </form>
                                    </div>
                                </div>

                                <div class="progress-bar-container" style="height: 12px; margin-bottom: 12px;">
                                    <div class="progress-bar-fill ${item.isAchieved ? 'progress-green' : 'progress-purple'}" 
                                         style="width: ${item.percent}%;"></div>
                                </div>

                                <div style="display: flex; justify-content: space-between; font-size: 0.9rem; align-items: center; margin-bottom: 16px;">
                                    <div>
                                        <span style="color: var(--text-muted);">Saved: </span>
                                        <strong style="color: #ffffff;">₹<fmt:formatNumber value="${g.savedAmount}" pattern="#,##0.00"/></strong>
                                        <span style="color: var(--text-dim);"> of ₹<fmt:formatNumber value="${g.targetAmount}" pattern="#,##0.00"/></span>
                                        <span style="font-weight: 700; color: var(--primary-light);"> (${item.percentFormatted}%)</span>
                                    </div>
                                    <div class="goal-countdown-badge">
                                        <c:choose>
                                            <c:when test="${item.isAchieved}">✅ Unlocked</c:when>
                                            <c:when test="${item.daysLeft >= 0}">⏳ ${item.daysLeft} days remaining</c:when>
                                            <c:otherwise>⚠️ Deadline Passed</c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>

                                <!-- Inline Add Savings Form -->
                                <c:if test="${!item.isAchieved}">
                                    <form action="${pageContext.request.contextPath}/goals" method="post" 
                                          style="display: flex; gap: 8px; align-items: center; padding-top: 12px; border-top: 1px solid var(--border-subtle);">
                                        <input type="hidden" name="action" value="add-savings">
                                        <input type="hidden" name="goalId" value="${g.id}">
                                        
                                        <input type="number" name="amount" class="form-control" placeholder="Deposit Amount (₹)" 
                                               step="50" min="1" required style="padding: 8px 12px; font-size: 0.85rem;">
                                        
                                        <button type="submit" class="btn btn-primary btn-sm" style="white-space: nowrap; height: 38px;">
                                            + Deposit
                                        </button>
                                    </form>
                                </c:if>
                            </div>
                        </c:forEach>

                        <c:if test="${empty goalCards}">
                            <div class="glass-card" style="text-align: center; padding: 40px; color: var(--text-muted);">
                                <div style="font-size: 2.2rem; margin-bottom: 10px;">🎯</div>
                                <h3>No Goals Found</h3>
                                <p style="margin-top: 6px;">Set a savings target with a deadline using the form on the right.</p>
                            </div>
                        </c:if>
                    </div>

                    <!-- Right: Create Goal Form -->
                    <div class="glass-card" style="height: fit-content;">
                        <div style="margin-bottom: 20px;">
                            <h3 style="font-size: 1.2rem;">Create Savings Goal</h3>
                            <p style="font-size: 0.85rem; color: var(--text-muted);">Define target and track milestone badges</p>
                        </div>

                        <form action="${pageContext.request.contextPath}/goals" method="post" id="goalForm">
                            <input type="hidden" name="action" value="create">

                            <div class="form-group">
                                <label class="form-label" for="title">Goal Title</label>
                                <input type="text" id="title" name="title" class="form-control" 
                                       placeholder="e.g. Emergency Fund, Goa Vacation, MacBook" required>
                            </div>

                            <div class="form-group">
                                <label class="form-label" for="targetAmount">Target Amount (₹)</label>
                                <input type="number" step="100" min="500" id="targetAmount" name="targetAmount" class="form-control" 
                                       placeholder="50000" required>
                            </div>

                            <div class="form-group">
                                <label class="form-label" for="deadline">Target Deadline</label>
                                <input type="date" id="deadline" name="deadline" class="form-control" required>
                            </div>

                            <div class="form-group">
                                <label class="form-label" for="description">Motivation / Notes (Optional)</label>
                                <textarea id="description" name="description" class="form-control" rows="2" 
                                          placeholder="Why is this goal important?"></textarea>
                            </div>

                            <div style="margin-top: 24px;">
                                <button type="submit" class="btn btn-primary" style="width: 100%; padding: 12px;">
                                    🎯 Launch Financial Goal
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
        // Set default deadline 6 months from now
        const sixMonths = new Date();
        sixMonths.setMonth(sixMonths.getMonth() + 6);
        document.getElementById('deadline').valueAsDate = sixMonths;

        function toggleSidebar() {
            const sidebar = document.getElementById('appSidebar');
            const backdrop = document.getElementById('sidebarBackdrop');
            sidebar.classList.toggle('open');
            backdrop.classList.toggle('active');
        }
    </script>
</body>
</html>

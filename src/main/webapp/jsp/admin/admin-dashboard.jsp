<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="activeNav" value="admin-dash" scope="request"/>
<c:set var="pageTitle" value="Admin Command Center" scope="request"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard — MyFinance</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/responsive.css">
</head>
<body>
    <div class="app-wrapper">
        <jsp:include page="/jsp/common/admin-sidebar.jsp"/>

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

                <!-- Stats Grid -->
                <div class="stats-grid">
                    <div class="glass-card stat-card">
                        <div class="stat-info">
                            <span class="stat-label">Total Users</span>
                            <span class="stat-value">${totalUsers}</span>
                            <span class="stat-trend" style="color: #a855f7;">👥 Platform accounts</span>
                        </div>
                        <div class="stat-icon-wrapper" style="color: #a855f7;">👥</div>
                    </div>

                    <div class="glass-card stat-card">
                        <div class="stat-info">
                            <span class="stat-label">Active Users</span>
                            <span class="stat-value">${activeUsers}</span>
                            <span class="stat-trend" style="color: #10b981;">✨ Fully verified</span>
                        </div>
                        <div class="stat-icon-wrapper" style="color: #10b981;">⚡</div>
                    </div>

                    <div class="glass-card stat-card">
                        <div class="stat-info">
                            <span class="stat-label">Total Expenses</span>
                            <span class="stat-value">${totalExpensesCount}</span>
                            <span class="stat-trend" style="color: #06b6d4;">💳 Transactions logged</span>
                        </div>
                        <div class="stat-icon-wrapper" style="color: #06b6d4;">💸</div>
                    </div>

                    <div class="glass-card stat-card">
                        <div class="stat-info">
                            <span class="stat-label">Audit Events</span>
                            <span class="stat-value">${totalAuditLogs}</span>
                            <span class="stat-trend" style="color: #ec4899;">🛡️ Security logs</span>
                        </div>
                        <div class="stat-icon-wrapper" style="color: #ec4899;">📜</div>
                    </div>
                </div>

                <!-- Two Column Layout: Users Table + Live Audit Logs -->
                <div class="grid-main-side">
                    <!-- Left: Recent User Registrations -->
                    <div class="glass-card">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                            <div>
                                <h3 style="font-size: 1.2rem;">Registered Accounts</h3>
                                <p style="font-size: 0.85rem; color: var(--text-muted);">Recent user registrations and access roles</p>
                            </div>
                            <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-secondary btn-sm">Manage All &rarr;</a>
                        </div>

                        <div class="table-responsive">
                            <table class="custom-table">
                                <thead>
                                    <tr>
                                        <th>User</th>
                                        <th>Email</th>
                                        <th>Role</th>
                                        <th>Status</th>
                                        <th>Registered</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="u" items="${recentUsers}">
                                        <tr>
                                            <td style="font-weight: 600; color: #ffffff;">${u.name}</td>
                                            <td style="color: var(--text-muted);">${u.email}</td>
                                            <td>
                                                <span class="badge ${u.admin ? 'badge-danger' : 'badge-primary'}">
                                                    ${u.role}
                                                </span>
                                            </td>
                                            <td>
                                                <span class="badge ${u.active ? 'badge-success' : 'badge-warning'}">
                                                    ${u.status}
                                                </span>
                                            </td>
                                            <td style="color: var(--text-dim); font-size: 0.85rem;">${u.createdAt}</td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- Right: Live System Audit Feed -->
                    <div class="glass-card">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                            <div>
                                <h3 style="font-size: 1.2rem;">Live Audit Trail</h3>
                                <p style="font-size: 0.85rem; color: var(--text-muted);">Real-time security events</p>
                            </div>
                            <a href="${pageContext.request.contextPath}/admin/security" class="btn btn-secondary btn-sm">View Logs</a>
                        </div>

                        <div style="display: flex; flex-direction: column; gap: 14px;">
                            <c:forEach var="log" items="${recentLogs}">
                                <div style="padding: 10px 14px; background: rgba(255, 255, 255, 0.02); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 0.85rem;">
                                    <div style="display: flex; justify-content: space-between; margin-bottom: 4px;">
                                        <strong style="color: var(--primary-light);">${log.adminName}</strong>
                                        <span style="color: var(--text-dim); font-size: 0.75rem;">${log.timestamp}</span>
                                    </div>
                                    <div style="color: var(--text-main); font-size: 0.825rem;">${log.action}</div>
                                </div>
                            </c:forEach>

                            <c:if test="${empty recentLogs}">
                                <div style="text-align: center; color: var(--text-muted); padding: 30px;">
                                    No audit events logged yet.
                                </div>
                            </c:if>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        function toggleSidebar() {
            const sidebar = document.getElementById('appSidebar');
            const backdrop = document.getElementById('sidebarBackdrop');
            sidebar.classList.toggle('open');
            backdrop.classList.toggle('active');
        }
    </script>
</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="activeNav" value="admin-settings" scope="request"/>
<c:set var="pageTitle" value="Platform Configuration" scope="request"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>System Settings — MyFinance Admin</title>
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

                <c:if test="${not empty sessionScope.flashError}">
                    <div class="alert-banner alert-banner-danger">
                        <span>⚠️ ${sessionScope.flashError}</span>
                    </div>
                    <c:remove var="flashError" scope="session"/>
                </c:if>

                <!-- Two Column Layout: Configuration Form + Change Log -->
                <div class="grid-main-side">
                    <!-- Left: Platform Settings Form -->
                    <div class="glass-card">
                        <div style="margin-bottom: 24px;">
                            <h3 style="font-size: 1.25rem;">Global System Preferences</h3>
                            <p style="font-size: 0.85rem; color: var(--text-muted);">Manage platform naming, default currency, and innovation feature flags</p>
                        </div>

                        <form action="${pageContext.request.contextPath}/admin/settings" method="post">
                            <div class="form-group">
                                <label class="form-label" for="platform_name">Platform Name</label>
                                <input type="text" id="platform_name" name="platform_name" class="form-control" 
                                       value="${settings['platform_name']}" required>
                            </div>

                            <div class="form-group" style="margin-top: 14px;">
                                <label class="form-label" for="default_currency">Default Currency Code</label>
                                <select id="default_currency" name="default_currency" class="form-control">
                                    <option value="INR" ${settings['default_currency'] eq 'INR' ? 'selected' : ''}>INR (₹) — Indian Rupee</option>
                                    <option value="USD" ${settings['default_currency'] eq 'USD' ? 'selected' : ''}>USD ($) — US Dollar</option>
                                    <option value="EUR" ${settings['default_currency'] eq 'EUR' ? 'selected' : ''}>EUR (€) — Euro</option>
                                    <option value="GBP" ${settings['default_currency'] eq 'GBP' ? 'selected' : ''}>GBP (£) — British Pound</option>
                                </select>
                            </div>

                            <div style="margin-top: 24px;">
                                <h4 style="font-size: 1rem; margin-bottom: 14px; color: var(--primary-light);">Innovation Feature Flags</h4>
                                
                                <div style="display: flex; flex-direction: column; gap: 14px;">
                                    <label style="display: flex; align-items: center; gap: 10px; cursor: pointer;">
                                        <input type="checkbox" name="budget_alerts_enabled" value="true" 
                                               ${settings['budget_alerts_enabled'] eq 'true' ? 'checked' : ''} style="width: 18px; height: 18px;">
                                        <div>
                                            <span style="font-weight: 600; font-size: 0.9rem;">Smart Budget Alerts (70%, 90%, 100%)</span>
                                            <div style="font-size: 0.75rem; color: var(--text-dim);">Enables background daemon alerts &amp; live spend guard checks</div>
                                        </div>
                                    </label>

                                    <label style="display: flex; align-items: center; gap: 10px; cursor: pointer;">
                                        <input type="checkbox" name="auto_categorize_enabled" value="true" 
                                               ${settings['auto_categorize_enabled'] ne 'false' ? 'checked' : ''} style="width: 18px; height: 18px;">
                                        <div>
                                            <span style="font-weight: 600; font-size: 0.9rem;">Category Auto-Suggest Engine</span>
                                            <div style="font-size: 0.75rem; color: var(--text-dim);">Auto-predicts spending categories from 40+ vendor keywords</div>
                                        </div>
                                    </label>

                                    <label style="display: flex; align-items: center; gap: 10px; cursor: pointer;">
                                        <input type="checkbox" name="offline_sync_enabled" value="true" 
                                               ${settings['offline_sync_enabled'] ne 'false' ? 'checked' : ''} style="width: 18px; height: 18px;">
                                        <div>
                                            <span style="font-weight: 600; font-size: 0.9rem;">Offline Expense Entry &amp; Live Sync</span>
                                            <div style="font-size: 0.75rem; color: var(--text-dim);">Queues client transactions in LocalStorage when disconnected</div>
                                        </div>
                                    </label>

                                    <label style="display: flex; align-items: center; gap: 10px; cursor: pointer;">
                                        <input type="checkbox" name="email_reports_enabled" value="true" 
                                               ${settings['email_reports_enabled'] eq 'true' ? 'checked' : ''} style="width: 18px; height: 18px;">
                                        <div>
                                            <span style="font-weight: 600; font-size: 0.9rem;">Plain-Language Weekly Insights</span>
                                            <div style="font-size: 0.75rem; color: var(--text-dim);">Generates weekly insight tip cards comparing monthly trends</div>
                                        </div>
                                    </label>
                                </div>
                            </div>

                            <div style="margin-top: 28px;">
                                <button type="submit" class="btn btn-primary" style="padding: 12px 24px;">
                                    ⚙️ Save Configuration Changes
                                </button>
                            </div>
                        </form>
                    </div>

                    <!-- Right: Change Log History -->
                    <div class="glass-card">
                        <div style="margin-bottom: 20px;">
                            <h3 style="font-size: 1.2rem;">Configuration Audit Log</h3>
                            <p style="font-size: 0.85rem; color: var(--text-muted);">Historical modification trail</p>
                        </div>

                        <div style="display: flex; flex-direction: column; gap: 12px; max-height: 520px; overflow-y: auto;">
                            <c:forEach var="log" items="${changeLogs}">
                                <div style="padding: 12px; background: rgba(255, 255, 255, 0.02); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 0.85rem;">
                                    <div style="display: flex; justify-content: space-between; margin-bottom: 4px;">
                                        <strong style="color: var(--primary-light);">${log.adminName}</strong>
                                        <span style="color: var(--text-dim); font-size: 0.75rem;">${log.timestamp}</span>
                                    </div>
                                    <div style="color: var(--text-main); font-size: 0.825rem;">${log.action}</div>
                                </div>
                            </c:forEach>
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

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="activeNav" value="admin-security" scope="request"/>
<c:set var="pageTitle" value="Security & Compliance Controls" scope="request"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Security Policies — MyFinance Admin</title>
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

                <!-- Stats Overview -->
                <div class="stats-grid">
                    <div class="glass-card stat-card">
                        <div class="stat-info">
                            <span class="stat-label">Security Health</span>
                            <span class="stat-value" style="color: #10b981;">98%</span>
                            <span class="stat-trend" style="color: #10b981;">🛡️ Zero active breaches</span>
                        </div>
                        <div class="stat-icon-wrapper" style="color: #10b981;">🛡️</div>
                    </div>

                    <div class="glass-card stat-card">
                        <div class="stat-info">
                            <span class="stat-label">Encryption Standard</span>
                            <span class="stat-value" style="font-size: 1.3rem;">BCrypt + AES</span>
                            <span class="stat-trend" style="color: var(--primary-light);">🔒 Cost Factor 12</span>
                        </div>
                        <div class="stat-icon-wrapper" style="color: #a855f7;">🔐</div>
                    </div>

                    <div class="glass-card stat-card">
                        <div class="stat-info">
                            <span class="stat-label">Session Lifetime</span>
                            <span class="stat-value">${settings['session_timeout']} min</span>
                            <span class="stat-trend" style="color: #06b6d4;">⏱️ Auto-invalidation</span>
                        </div>
                        <div class="stat-icon-wrapper" style="color: #06b6d4;">⏳</div>
                    </div>

                    <div class="glass-card stat-card">
                        <div class="stat-info">
                            <span class="stat-label">Max Login Retries</span>
                            <span class="stat-value">${settings['max_login_attempts']}</span>
                            <span class="stat-trend" style="color: #ec4899;">🚫 Brute-force block</span>
                        </div>
                        <div class="stat-icon-wrapper" style="color: #ec4899;">⚡</div>
                    </div>
                </div>

                <!-- Two Column Layout: Security Controls + Audit Log -->
                <div class="grid-main-side">
                    <!-- Left: Security Policy Form -->
                    <div class="glass-card">
                        <div style="margin-bottom: 24px;">
                            <h3 style="font-size: 1.25rem;">Platform Security Policies</h3>
                            <p style="font-size: 0.85rem; color: var(--text-muted);">Manage session timeouts, rate limits, and authentication rules</p>
                        </div>

                        <form action="${pageContext.request.contextPath}/admin/security" method="post">
                            <div class="form-group">
                                <label class="form-label" for="session_timeout">HTTP Session Expiry (Minutes)</label>
                                <input type="number" id="session_timeout" name="session_timeout" class="form-control" 
                                       value="${settings['session_timeout']}" min="5" max="180" required>
                                <small style="color: var(--text-dim); font-size: 0.75rem;">Inactive sessions will be invalidated after this duration.</small>
                            </div>

                            <div class="form-group" style="margin-top: 14px;">
                                <label class="form-label" for="max_login_attempts">Max Failed Login Attempts</label>
                                <input type="number" id="max_login_attempts" name="max_login_attempts" class="form-control" 
                                       value="${settings['max_login_attempts']}" min="3" max="20" required>
                                <small style="color: var(--text-dim); font-size: 0.75rem;">Protects user accounts against brute-force attacks.</small>
                            </div>

                            <div class="form-group" style="margin-top: 14px;">
                                <label class="form-label" for="encryption_standard">Hash &amp; Cipher Suite</label>
                                <select id="encryption_standard" name="encryption_standard" class="form-control">
                                    <option value="BCrypt-12" selected>BCrypt (Cost factor 12) + SHA-256 (Default)</option>
                                    <option value="BCrypt-14">BCrypt (Cost factor 14) — Ultra High Security</option>
                                </select>
                            </div>

                            <div style="margin-top: 20px; display: flex; flex-direction: column; gap: 12px;">
                                <label style="display: flex; align-items: center; gap: 10px; cursor: pointer;">
                                    <input type="checkbox" name="two_factor_auth" value="true" 
                                           ${settings['two_factor_auth'] eq 'true' ? 'checked' : ''} style="width: 18px; height: 18px;">
                                    <div>
                                        <span style="font-weight: 600; font-size: 0.9rem;">Enforce Two-Factor Authentication (2FA)</span>
                                        <div style="font-size: 0.75rem; color: var(--text-dim);">Requires secondary one-time verification on unknown logins</div>
                                    </div>
                                </label>

                                <label style="display: flex; align-items: center; gap: 10px; cursor: pointer;">
                                    <input type="checkbox" name="ip_whitelist_enabled" value="true" 
                                           ${settings['ip_whitelist_enabled'] eq 'true' ? 'checked' : ''} style="width: 18px; height: 18px;">
                                    <div>
                                        <span style="font-weight: 600; font-size: 0.9rem;">Admin IP Whitelisting &amp; Anomaly Detection</span>
                                        <div style="font-size: 0.75rem; color: var(--text-dim);">Restricts administrative login from unrecognized geographic nodes</div>
                                    </div>
                                </label>
                            </div>

                            <div style="margin-top: 28px;">
                                <button type="submit" class="btn btn-primary" style="padding: 12px 24px;">
                                    🛡️ Save Security Policies
                                </button>
                            </div>
                        </form>
                    </div>

                    <!-- Right: Audit Trail -->
                    <div class="glass-card">
                        <div style="margin-bottom: 20px;">
                            <h3 style="font-size: 1.2rem;">Security Logs (${securityLogs.size()})</h3>
                            <p style="font-size: 0.85rem; color: var(--text-muted);">Immutable audit event records</p>
                        </div>

                        <div style="display: flex; flex-direction: column; gap: 12px; max-height: 540px; overflow-y: auto; padding-right: 4px;">
                            <c:forEach var="log" items="${securityLogs}">
                                <div style="padding: 12px; background: rgba(255, 255, 255, 0.02); border: 1px solid var(--border-subtle); border-radius: var(--radius-sm); font-size: 0.85rem;">
                                    <div style="display: flex; justify-content: space-between; margin-bottom: 4px;">
                                        <strong style="color: var(--primary-light);">${log.adminName}</strong>
                                        <span style="color: var(--text-dim); font-size: 0.75rem;">${log.timestamp}</span>
                                    </div>
                                    <div style="color: #e2e8f0; font-size: 0.825rem;">${log.action}</div>
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

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign In — MyFinance</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/responsive.css">
</head>
<body>
    <div class="auth-container">
        <!-- Left Branding Hero -->
        <div class="auth-hero">
            <div class="auth-brand">
                <div class="auth-brand-logo">💎</div>
                <div class="auth-brand-title brand-font">MyFinance</div>
            </div>

            <div class="auth-hero-center">
                <h1 class="auth-hero-headline">Master your wealth with clarity and focus.</h1>
                <p class="auth-hero-sub">
                    A private, proactive personal finance engine built for real goals. No SMS scrapers. No ad trackers. Just your money, organized.
                </p>

                <div class="auth-feature-list">
                    <div class="auth-feature-item">
                        <div class="auth-feature-icon">🛡️</div>
                        <span>Smart budget alerts at 70%, 90%, and 100% thresholds</span>
                    </div>
                    <div class="auth-feature-item">
                        <div class="auth-feature-icon">🎯</div>
                        <span>Target savings engine with milestone badges</span>
                    </div>
                    <div class="auth-feature-item">
                        <div class="auth-feature-icon">⚡</div>
                        <span>Instant category auto-suggestions and offline entry</span>
                    </div>
                </div>
            </div>

            <div class="auth-hero-footer">
                &copy; 2026 MyFinance Platform &bull; Built with Java Servlets, Raw JDBC &amp; Tomcat 10.1
            </div>
        </div>

        <!-- Right Login Card -->
        <div class="auth-form-column">
            <div class="glass-card auth-card">
                <div class="auth-header">
                    <h2 class="auth-title">Welcome back</h2>
                    <p class="auth-subtitle">Sign in to your private financial dashboard</p>
                </div>

                <c:if test="${not empty error}">
                    <div class="alert-banner alert-banner-danger">
                        <span>⚠️ ${error}</span>
                    </div>
                </c:if>

                <c:if test="${not empty sessionScope.flashSuccess}">
                    <div class="alert-banner alert-banner-success">
                        <span>✨ ${sessionScope.flashSuccess}</span>
                    </div>
                    <c:remove var="flashSuccess" scope="session"/>
                </c:if>

                <c:if test="${param.loggedOut eq 'true'}">
                    <div class="alert-banner alert-banner-info">
                        <span>👋 You have been safely logged out.</span>
                    </div>
                </c:if>

                <form action="${pageContext.request.contextPath}/auth" method="post" id="loginForm">
                    <input type="hidden" name="action" value="login">

                    <div class="form-group">
                        <label class="form-label" for="email">Email Address</label>
                        <input type="email" id="email" name="email" class="form-control" 
                               value="${email}" placeholder="you@domain.com" required autofocus>
                    </div>

                    <div class="form-group">
                        <div style="display: flex; justify-content: space-between; align-items: center;">
                            <label class="form-label" for="password">Password</label>
                        </div>
                        <input type="password" id="password" name="password" class="form-control" 
                               placeholder="••••••••" required>
                    </div>

                    <div style="margin-top: 24px;">
                        <button type="submit" class="btn btn-primary" style="width: 100%; padding: 12px;">
                            Sign In to Account &rarr;
                        </button>
                    </div>
                </form>

                <div class="auth-footer-link">
                    Don't have an account yet? 
                    <a href="${pageContext.request.contextPath}/auth?view=register" style="font-weight: 600;">Create one free</a>
                </div>

                <div style="margin-top: 20px; padding-top: 15px; border-top: 1px solid var(--border-subtle); font-size: 0.75rem; color: var(--text-dim); text-align: center;">
                    Admin demo: <code>admin@myfinance.com</code> / <code>admin123</code>
                </div>
            </div>
        </div>
    </div>
</body>
</html>

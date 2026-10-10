<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create Account — MyFinance</title>
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
                <h1 class="auth-hero-headline">Start building your financial freedom today.</h1>
                <p class="auth-hero-sub">
                    Join thousands of smart savers who budget proactively, track expenditures in seconds, and hit savings milestones without spreadsheets.
                </p>

                <div class="auth-feature-list">
                    <div class="auth-feature-item">
                        <div class="auth-feature-icon">🔒</div>
                        <span>100% Private — BCrypt password hashing &amp; zero data selling</span>
                    </div>
                    <div class="auth-feature-item">
                        <div class="auth-feature-icon">📊</div>
                        <span>Interactive visual expense charts with zero bloat</span>
                    </div>
                    <div class="auth-feature-item">
                        <div class="auth-feature-icon">💡</div>
                        <span>Plain English weekly insights to plug money leaks</span>
                    </div>
                </div>
            </div>

            <div class="auth-hero-footer">
                &copy; 2026 MyFinance Platform &bull; Built with Java Servlets, Raw JDBC &amp; Tomcat 10.1
            </div>
        </div>

        <!-- Right Register Card -->
        <div class="auth-form-column">
            <div class="glass-card auth-card">
                <div class="auth-header">
                    <h2 class="auth-title">Create your account</h2>
                    <p class="auth-subtitle">Get started with private wealth tracking in 30 seconds</p>
                </div>

                <c:if test="${not empty error}">
                    <div class="alert-banner alert-banner-danger">
                        <span>⚠️ ${error}</span>
                    </div>
                </c:if>

                <form action="${pageContext.request.contextPath}/auth" method="post" id="registerForm">
                    <input type="hidden" name="action" value="register">

                    <div class="form-group">
                        <label class="form-label" for="name">Full Name</label>
                        <input type="text" id="name" name="name" class="form-control" 
                               value="${name}" placeholder="Alex Sharma" required autofocus>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="email">Email Address</label>
                        <input type="email" id="email" name="email" class="form-control" 
                               value="${email}" placeholder="alex@domain.com" required>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="password">Create Password</label>
                        <input type="password" id="password" name="password" class="form-control" 
                               placeholder="Min 6 characters" minlength="6" required>
                    </div>

                    <div class="form-group">
                        <label class="form-label" for="confirmPassword">Confirm Password</label>
                        <input type="password" id="confirmPassword" name="confirmPassword" class="form-control" 
                               placeholder="Repeat password" minlength="6" required>
                    </div>

                    <div style="margin-top: 24px;">
                        <button type="submit" class="btn btn-primary" style="width: 100%; padding: 12px;">
                            Create My Account &rarr;
                        </button>
                    </div>
                </form>

                <div class="auth-footer-link">
                    Already have an account? 
                    <a href="${pageContext.request.contextPath}/auth?view=login" style="font-weight: 600;">Sign in here</a>
                </div>
            </div>
        </div>
    </div>
</body>
</html>

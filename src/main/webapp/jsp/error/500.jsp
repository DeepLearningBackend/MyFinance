<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>500 — Server Error | MyFinance</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
</head>
<body style="display: flex; align-items: center; justify-content: center; min-height: 100vh; padding: 20px;">
    <div class="glass-card" style="max-width: 520px; text-align: center; padding: 48px 32px;">
        <div style="font-size: 3.5rem; margin-bottom: 16px;">⚡</div>
        <h1 style="font-size: 2.2rem; margin-bottom: 8px; color: var(--danger);">500 — System Exception</h1>
        <p style="color: var(--text-muted); margin-bottom: 24px;">
            An unexpected error occurred while processing your request. The system administrator has been logged and notified.
        </p>
        <% if (exception != null && exception.getMessage() != null) { %>
            <div style="text-align: left; background: rgba(255, 68, 102, 0.1); border: 1px solid rgba(255, 68, 102, 0.3); border-radius: 8px; padding: 12px; margin-bottom: 24px; font-family: monospace; font-size: 12px; color: #ff8888; overflow-x: auto;">
                <%= exception.getMessage() %>
            </div>
        <% } %>
        <div style="display: flex; gap: 12px; justify-content: center;">
            <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary" style="padding: 12px 24px;">
                &larr; Return to Dashboard
            </a>
            <a href="${pageContext.request.contextPath}/auth" class="btn" style="padding: 12px 24px;">
                Sign In
            </a>
        </div>
    </div>
</body>
</html>

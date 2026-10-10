<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>404 — Page Not Found | MyFinance</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
</head>
<body style="display: flex; align-items: center; justify-content: center; min-height: 100vh; padding: 20px;">
    <div class="glass-card" style="max-width: 480px; text-align: center; padding: 48px 32px;">
        <div style="font-size: 3.5rem; margin-bottom: 16px;">🔍</div>
        <h1 style="font-size: 2.2rem; margin-bottom: 8px;">404 — Page Not Found</h1>
        <p style="color: var(--text-muted); margin-bottom: 28px;">
            The page or resource you are looking for has been moved, renamed, or is unavailable.
        </p>
        <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary" style="padding: 12px 28px;">
            &larr; Return to Dashboard
        </a>
    </div>
</body>
</html>

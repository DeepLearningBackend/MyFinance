<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login - MyFinance</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/auth.css">
</head>
<body>
    <div class="auth-container">
        <div class="auth-left">
            <div class="branding">
                <h1>MyFinance</h1>
                <p>Track your expenses, achieve your goals</p>
            </div>
        </div>
        <div class="auth-right">
            <div class="auth-card">
                <h2>Welcome Back</h2>
                <p class="subtitle">Sign in to continue to your account</p>

                <% if (request.getAttribute("error") != null) { %>
                    <div class="alert alert-error">
                        ${request.getAttribute("error")}
                    </div>
                <% } %>

                <% if (request.getAttribute("success") != null) { %>
                    <div class="alert alert-success">
                        ${request.getAttribute("success")}
                    </div>
                <% } %>

                <form action="${pageContext.request.contextPath}/auth" method="post">
                    <input type="hidden" name="action" value="login">

                    <div class="form-group">
                        <label for="email">Email</label>
                        <input type="email" id="email" name="email" required>
                    </div>

                    <div class="form-group">
                        <label for="password">Password</label>
                        <input type="password" id="password" name="password" required>
                    </div>

                    <button type="submit" class="btn btn-primary">Login</button>
                </form>

                <div class="auth-footer">
                    <p>Don't have an account? <a href="${pageContext.request.contextPath}/auth?action=register">Register</a></p>
                </div>
            </div>
        </div>
    </div>
</body>
</html>

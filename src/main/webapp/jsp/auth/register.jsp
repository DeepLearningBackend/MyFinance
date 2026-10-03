<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register - MyFinance</title>
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
                <h2>Create Account</h2>
                <p class="subtitle">Start managing your finances today</p>

                <% if (request.getAttribute("error") != null) { %>
                    <div class="alert alert-error">
                        ${request.getAttribute("error")}
                    </div>
                <% } %>

                <form action="${pageContext.request.contextPath}/auth" method="post">
                    <input type="hidden" name="action" value="register">

                    <div class="form-group">
                        <label for="name">Full Name</label>
                        <input type="text" id="name" name="name" required>
                    </div>

                    <div class="form-group">
                        <label for="email">Email</label>
                        <input type="email" id="email" name="email" required>
                    </div>

                    <div class="form-group">
                        <label for="password">Password</label>
                        <input type="password" id="password" name="password" required minlength="6">
                    </div>

                    <div class="form-group">
                        <label for="confirmPassword">Confirm Password</label>
                        <input type="password" id="confirmPassword" name="confirmPassword" required minlength="6">
                    </div>

                    <button type="submit" class="btn btn-primary">Register</button>
                </form>

                <div class="auth-footer">
                    <p>Already have an account? <a href="${pageContext.request.contextPath}/auth">Login</a></p>
                </div>
            </div>
        </div>
    </div>
</body>
</html>

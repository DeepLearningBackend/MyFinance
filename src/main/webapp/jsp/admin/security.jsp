<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Security - MyFinance</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/responsive.css">
</head>
<body>
    <div class="app-container">
        <!-- Sidebar -->
        <aside class="sidebar">
            <div class="sidebar-header">
                <h2>MyFinance Admin</h2>
            </div>
            <nav class="sidebar-nav">
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="nav-item">
                    <span>Dashboard</span>
                </a>
                <a href="${pageContext.request.contextPath}/admin/users" class="nav-item">
                    <span>User Management</span>
                </a>
                <a href="${pageContext.request.contextPath}/admin/security" class="nav-item active">
                    <span>Security</span>
                </a>
                <a href="${pageContext.request.contextPath}/admin/settings" class="nav-item">
                    <span>Settings</span>
                </a>
                <a href="${pageContext.request.contextPath}/auth?action=logout" class="nav-item">
                    <span>Logout</span>
                </a>
            </nav>
        </aside>

        <!-- Main Content -->
        <main class="main-content">
            <header class="topbar">
                <div class="topbar-left">
                    <h1>Security Settings</h1>
                </div>
                <div class="topbar-right">
                    <span class="user-name">${sessionScope.userName} (Admin)</span>
                </div>
            </header>

            <div class="section">
                <h2>Security Controls</h2>
                <p class="info-text">Configure security settings for the platform.</p>
                <form action="${pageContext.request.contextPath}/admin/security" method="post" class="form-card">
                    <input type="hidden" name="action" value="update">
                    <div class="form-group">
                        <label>
                            <input type="checkbox" name="encryption" checked> Enable AES-256 Encryption
                        </label>
                    </div>
                    <div class="form-group">
                        <label>
                            <input type="checkbox" name="twofa"> Enable Two-Factor Authentication
                        </label>
                    </div>
                    <div class="form-group">
                        <label>
                            <input type="checkbox" name="ipwhitelist"> Enable IP Whitelist
                        </label>
                    </div>
                    <div class="form-group">
                        <label>Session Timeout (minutes)</label>
                        <input type="number" name="sessionTimeout" value="30">
                    </div>
                    <button type="submit" class="btn btn-primary">Update Security Settings</button>
                </form>
            </div>
        </main>
    </div>
</body>
</html>

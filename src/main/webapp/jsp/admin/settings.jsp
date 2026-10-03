<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Settings - MyFinance</title>
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
                <a href="${pageContext.request.contextPath}/admin/security" class="nav-item">
                    <span>Security</span>
                </a>
                <a href="${pageContext.request.contextPath}/admin/settings" class="nav-item active">
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
                    <h1>System Settings</h1>
                </div>
                <div class="topbar-right">
                    <span class="user-name">${sessionScope.userName} (Admin)</span>
                </div>
            </header>

            <div class="section">
                <h2>Platform Configuration</h2>
                <form action="${pageContext.request.contextPath}/admin/settings" method="post" class="form-card">
                    <input type="hidden" name="action" value="update">
                    <div class="form-group">
                        <label>Platform Name</label>
                        <input type="text" name="platformName" value="MyFinance">
                    </div>
                    <div class="form-group">
                        <label>Default Currency</label>
                        <select name="currency">
                            <option value="INR" selected>INR (₹)</option>
                            <option value="USD">USD ($)</option>
                            <option value="EUR">EUR (€)</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>
                            <input type="checkbox" name="budgetAlerts" checked> Enable Budget Alerts
                        </label>
                    </div>
                    <div class="form-group">
                        <label>
                            <input type="checkbox" name="emailReports" checked> Enable Email Reports
                        </label>
                    </div>
                    <button type="submit" class="btn btn-primary">Update Settings</button>
                </form>
            </div>
        </main>
    </div>
</body>
</html>

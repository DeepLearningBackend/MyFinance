<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - MyFinance</title>
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
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="nav-item active">
                    <span>Dashboard</span>
                </a>
                <a href="${pageContext.request.contextPath}/admin/users" class="nav-item">
                    <span>User Management</span>
                </a>
                <a href="${pageContext.request.contextPath}/admin/security" class="nav-item">
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
                    <h1>Admin Dashboard</h1>
                </div>
                <div class="topbar-right">
                    <span class="user-name">${sessionScope.userName} (Admin)</span>
                </div>
            </header>

            <!-- Stats Row -->
            <div class="stats-grid">
                <div class="stat-card">
                    <h3>Total Users</h3>
                    <p class="stat-value">${totalUsers}</p>
                </div>
                <div class="stat-card">
                    <h3>Active Users</h3>
                    <p class="stat-value">${activeUsers}</p>
                </div>
                <div class="stat-card">
                    <h3>Total Expenses</h3>
                    <p class="stat-value">${totalExpenses}</p>
                </div>
            </div>

            <!-- Recent Users -->
            <div class="section">
                <h2>Recent Registrations</h2>
                <div class="table-container">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Name</th>
                                <th>Email</th>
                                <th>Role</th>
                                <th>Status</th>
                                <th>Joined</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="user" items="${recentUsers}">
                                <tr>
                                    <td>${user.name}</td>
                                    <td>${user.email}</td>
                                    <td>${user.role}</td>
                                    <td>
                                        <span class="badge badge-${user.status}">${user.status}</span>
                                    </td>
                                    <td>${user.createdAt}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </main>
    </div>
</body>
</html>

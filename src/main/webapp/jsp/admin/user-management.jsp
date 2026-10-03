<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Management - MyFinance</title>
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
                <a href="${pageContext.request.contextPath}/admin/users" class="nav-item active">
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
                    <h1>User Management</h1>
                </div>
                <div class="topbar-right">
                    <span class="user-name">${sessionScope.userName} (Admin)</span>
                </div>
            </header>

            <!-- Search/Filter -->
            <div class="section">
                <form action="${pageContext.request.contextPath}/admin/users" method="get" class="filter-form">
                    <input type="text" name="search" placeholder="Search by name or email">
                    <select name="status">
                        <option value="">All Status</option>
                        <option value="ACTIVE">Active</option>
                        <option value="INACTIVE">Inactive</option>
                        <option value="PENDING">Pending</option>
                    </select>
                    <button type="submit" class="btn btn-primary">Filter</button>
                </form>
            </div>

            <!-- Users Table -->
            <div class="section">
                <div class="table-container">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Name</th>
                                <th>Email</th>
                                <th>Role</th>
                                <th>Status</th>
                                <th>Joined</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="user" items="${users}">
                                <tr>
                                    <td>${user.id}</td>
                                    <td>${user.name}</td>
                                    <td>${user.email}</td>
                                    <td>${user.role}</td>
                                    <td>
                                        <span class="badge badge-${user.status}">${user.status}</span>
                                    </td>
                                    <td>${user.createdAt}</td>
                                    <td>
                                        <form action="${pageContext.request.contextPath}/admin/users" method="post" style="display:inline;">
                                            <input type="hidden" name="action" value="change-status">
                                            <input type="hidden" name="id" value="${user.id}">
                                            <input type="hidden" name="status" value="${user.status == 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'}">
                                            <button type="submit" class="btn btn-sm">
                                                ${user.status == 'ACTIVE' ? 'Deactivate' : 'Activate'}
                                            </button>
                                        </form>
                                        <form action="${pageContext.request.contextPath}/admin/users" method="post" style="display:inline;">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${user.id}">
                                            <button type="submit" class="btn btn-danger btn-sm">Delete</button>
                                        </form>
                                    </td>
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

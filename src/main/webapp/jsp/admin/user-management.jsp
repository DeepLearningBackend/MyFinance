<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="activeNav" value="admin-users" scope="request"/>
<c:set var="pageTitle" value="User Account Management" scope="request"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Management — MyFinance Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/responsive.css">
</head>
<body>
    <div class="app-wrapper">
        <jsp:include page="/jsp/common/admin-sidebar.jsp"/>

        <div class="app-main">
            <jsp:include page="/jsp/common/topbar.jsp"/>

            <div class="content-body">
                <!-- Flash Feedback -->
                <c:if test="${not empty sessionScope.flashSuccess}">
                    <div class="alert-banner alert-banner-success">
                        <span>✨ ${sessionScope.flashSuccess}</span>
                    </div>
                    <c:remove var="flashSuccess" scope="session"/>
                </c:if>

                <c:if test="${not empty sessionScope.flashError}">
                    <div class="alert-banner alert-banner-danger">
                        <span>⚠️ ${sessionScope.flashError}</span>
                    </div>
                    <c:remove var="flashError" scope="session"/>
                </c:if>

                <!-- Filter Controls Card -->
                <div class="glass-card" style="margin-bottom: 24px; padding: 18px 24px;">
                    <form action="${pageContext.request.contextPath}/admin/users" method="get" style="display: flex; flex-wrap: wrap; gap: 14px; align-items: flex-end;">
                        <div style="flex: 2; min-width: 220px;">
                            <label class="form-label" for="search">Search Users</label>
                            <input type="text" id="search" name="search" class="form-control" 
                                   placeholder="Search by name, email..." value="${searchQuery}">
                        </div>

                        <div style="flex: 1; min-width: 140px;">
                            <label class="form-label" for="role">Role</label>
                            <select id="role" name="role" class="form-control">
                                <option value="ALL">All Roles</option>
                                <option value="USER" ${selectedRole eq 'USER' ? 'selected' : ''}>USER</option>
                                <option value="ADMIN" ${selectedRole eq 'ADMIN' ? 'selected' : ''}>ADMIN</option>
                            </select>
                        </div>

                        <div style="flex: 1; min-width: 140px;">
                            <label class="form-label" for="status">Status</label>
                            <select id="status" name="status" class="form-control">
                                <option value="ALL">All Statuses</option>
                                <option value="ACTIVE" ${selectedStatus eq 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                                <option value="INACTIVE" ${selectedStatus eq 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                                <option value="PENDING" ${selectedStatus eq 'PENDING' ? 'selected' : ''}>PENDING</option>
                            </select>
                        </div>

                        <div style="display: flex; gap: 8px;">
                            <button type="submit" class="btn btn-primary btn-sm" style="height: 42px; padding: 0 18px;">
                                Filter
                            </button>
                            <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-secondary btn-sm" style="height: 42px; padding: 0 14px; display: inline-flex; align-items: center;">
                                Reset
                            </a>
                        </div>
                    </form>
                </div>

                <!-- Two Column Layout: User Accounts Table + Provision User -->
                <div class="grid-main-side">
                    <!-- Left: Users Directory -->
                    <div class="glass-card">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                            <div>
                                <h3 style="font-size: 1.2rem;">User Accounts (${users.size()})</h3>
                                <p style="font-size: 0.85rem; color: var(--text-muted);">Manage platform privileges, status toggles, and accounts</p>
                            </div>
                        </div>

                        <div class="table-responsive">
                            <table class="custom-table">
                                <thead>
                                    <tr>
                                        <th>Name</th>
                                        <th>Email</th>
                                        <th>Role</th>
                                        <th>Status</th>
                                        <th>Joined</th>
                                        <th style="text-align: center;">Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="u" items="${users}">
                                        <tr>
                                            <td style="font-weight: 600; color: #ffffff;">${u.name}</td>
                                            <td style="color: var(--text-muted);">${u.email}</td>
                                            <td>
                                                <form action="${pageContext.request.contextPath}/admin/users" method="post" style="display: inline;">
                                                    <input type="hidden" name="action" value="update-role">
                                                    <input type="hidden" name="userId" value="${u.id}">
                                                    <input type="hidden" name="role" value="${u.admin ? 'USER' : 'ADMIN'}">
                                                    <button type="submit" class="badge ${u.admin ? 'badge-danger' : 'badge-primary'}" 
                                                            style="cursor: pointer; border: none;" title="Click to toggle Role">
                                                        ${u.role} 🔄
                                                    </button>
                                                </form>
                                            </td>
                                            <td>
                                                <form action="${pageContext.request.contextPath}/admin/users" method="post" style="display: inline;">
                                                    <input type="hidden" name="action" value="toggle-status">
                                                    <input type="hidden" name="userId" value="${u.id}">
                                                    <input type="hidden" name="status" value="${u.active ? 'INACTIVE' : 'ACTIVE'}">
                                                    <button type="submit" class="badge ${u.active ? 'badge-success' : 'badge-warning'}" 
                                                            style="cursor: pointer; border: none;" title="Click to toggle Status">
                                                        ${u.status} 🔄
                                                    </button>
                                                </form>
                                            </td>
                                            <td style="color: var(--text-dim); font-size: 0.825rem;">${u.createdAt}</td>
                                            <td style="text-align: center; white-space: nowrap;">
                                                <c:choose>
                                                    <c:when test="${u.id eq sessionScope.userId}">
                                                        <span style="font-size: 0.75rem; color: var(--text-dim);">Current Admin</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <form action="${pageContext.request.contextPath}/admin/users" method="post" style="display: inline;"
                                                              onsubmit="return confirm('Permanently delete account ${u.email}? This action cannot be undone.');">
                                                            <input type="hidden" name="action" value="delete">
                                                            <input type="hidden" name="userId" value="${u.id}">
                                                            <button type="submit" class="btn btn-danger btn-sm" title="Delete User">
                                                                🗑️
                                                            </button>
                                                        </form>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- Right: Provision Account Card -->
                    <div class="glass-card" style="height: fit-content;">
                        <div style="margin-bottom: 20px;">
                            <h3 style="font-size: 1.2rem;">Provision Account</h3>
                            <p style="font-size: 0.85rem; color: var(--text-muted);">Create user or admin manually</p>
                        </div>

                        <form action="${pageContext.request.contextPath}/admin/users" method="post">
                            <input type="hidden" name="action" value="create">

                            <div class="form-group">
                                <label class="form-label" for="userName">Full Name</label>
                                <input type="text" id="userName" name="name" class="form-control" required placeholder="Jane Doe">
                            </div>

                            <div class="form-group">
                                <label class="form-label" for="userEmail">Email</label>
                                <input type="email" id="userEmail" name="email" class="form-control" required placeholder="jane@domain.com">
                            </div>

                            <div class="form-group">
                                <label class="form-label" for="userPassword">Initial Password</label>
                                <input type="password" id="userPassword" name="password" class="form-control" required placeholder="••••••••" minlength="6">
                            </div>

                            <div class="form-group">
                                <label class="form-label" for="userRole">Account Role</label>
                                <select id="userRole" name="role" class="form-control">
                                    <option value="USER">USER (Standard Account)</option>
                                    <option value="ADMIN">ADMIN (Full Privileges)</option>
                                </select>
                            </div>

                            <div style="margin-top: 24px;">
                                <button type="submit" class="btn btn-primary" style="width: 100%; padding: 12px;">
                                    + Provision User Account
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        function toggleSidebar() {
            const sidebar = document.getElementById('appSidebar');
            const backdrop = document.getElementById('sidebarBackdrop');
            sidebar.classList.toggle('open');
            backdrop.classList.toggle('active');
        }
    </script>
</body>
</html>

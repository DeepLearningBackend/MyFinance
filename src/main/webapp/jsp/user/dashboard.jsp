<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard - MyFinance</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/responsive.css">
</head>
<body>
    <div class="app-container">
        <!-- Sidebar -->
        <aside class="sidebar">
            <div class="sidebar-header">
                <h2>MyFinance</h2>
            </div>
            <nav class="sidebar-nav">
                <a href="${pageContext.request.contextPath}/dashboard" class="nav-item active">
                    <span>Dashboard</span>
                </a>
                <a href="${pageContext.request.contextPath}/expenses" class="nav-item">
                    <span>Expenses</span>
                </a>
                <a href="${pageContext.request.contextPath}/budgets" class="nav-item">
                    <span>Budgets</span>
                </a>
                <a href="${pageContext.request.contextPath}/goals" class="nav-item">
                    <span>Goals</span>
                </a>
                <a href="${pageContext.request.contextPath}/auth?action=logout" class="nav-item">
                    <span>Logout</span>
                </a>
            </nav>
        </aside>

        <!-- Main Content -->
        <main class="main-content">
            <!-- Topbar -->
            <header class="topbar">
                <div class="topbar-left">
                    <h1>Dashboard</h1>
                </div>
                <div class="topbar-right">
                    <span class="user-name">${sessionScope.userName}</span>
                </div>
            </header>

            <!-- Alerts -->
            <c:if test="${not empty alerts}">
                <div class="alerts-container">
                    <c:forEach var="alert" items="${alerts}">
                        <div class="alert alert-warning">${alert}</div>
                    </c:forEach>
                </div>
            </c:if>

            <!-- Stats Row -->
            <div class="stats-grid">
                <div class="stat-card">
                    <h3>Monthly Spending</h3>
                    <p class="stat-value">₹${monthlyTotal}</p>
                </div>
                <div class="stat-card">
                    <h3>Active Budgets</h3>
                    <p class="stat-value">${budgets.size()}</p>
                </div>
                <div class="stat-card">
                    <h3>Active Goals</h3>
                    <p class="stat-value">${goals.size()}</p>
                </div>
            </div>

            <!-- Insight Card -->
            <div class="insight-card">
                <h3>Weekly Insight</h3>
                <p>${insight}</p>
            </div>

            <!-- Recent Expenses -->
            <div class="section">
                <h2>Recent Expenses</h2>
                <div class="expense-list">
                    <c:forEach var="expense" items="${recentExpenses}">
                        <div class="expense-item">
                            <div class="expense-info">
                                <span class="expense-title">${expense.title}</span>
                                <span class="expense-category">${expense.category}</span>
                            </div>
                            <span class="expense-amount">₹${expense.amount}</span>
                        </div>
                    </c:forEach>
                    <c:if test="${empty recentExpenses}">
                        <p class="empty-state">No expenses yet. Start tracking!</p>
                    </c:if>
                </div>
            </div>

            <!-- Goals Preview -->
            <div class="section">
                <h2>Your Goals</h2>
                <div class="goals-grid">
                    <c:forEach var="goal" items="${goals}">
                        <div class="goal-card">
                            <h4>${goal.title}</h4>
                            <div class="progress-bar">
                                <div class="progress-fill" style="width: ${goal.progressPercent}%"></div>
                            </div>
                            <p>₹${goal.savedAmount} / ₹${goal.targetAmount}</p>
                        </div>
                    </c:forEach>
                    <c:if test="${empty goals}">
                        <p class="empty-state">No goals set. Create your first goal!</p>
                    </c:if>
                </div>
            </div>
        </main>
    </div>
</body>
</html>

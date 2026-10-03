<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Budgets - MyFinance</title>
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
                <a href="${pageContext.request.contextPath}/dashboard" class="nav-item">
                    <span>Dashboard</span>
                </a>
                <a href="${pageContext.request.contextPath}/expenses" class="nav-item">
                    <span>Expenses</span>
                </a>
                <a href="${pageContext.request.contextPath}/budgets" class="nav-item active">
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
            <header class="topbar">
                <div class="topbar-left">
                    <h1>Budgets</h1>
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

            <!-- Create Budget Form -->
            <div class="section">
                <h2>Create Budget</h2>
                <form action="${pageContext.request.contextPath}/budgets" method="post" class="form-card">
                    <input type="hidden" name="action" value="create">
                    <div class="form-row">
                        <div class="form-group">
                            <label>Category</label>
                            <select name="category" required>
                                <option value="">Select Category</option>
                                <option value="Food">Food</option>
                                <option value="Transport">Transport</option>
                                <option value="Shopping">Shopping</option>
                                <option value="Entertainment">Entertainment</option>
                                <option value="Utilities">Utilities</option>
                                <option value="Health">Health</option>
                                <option value="Education">Education</option>
                                <option value="Others">Others</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Limit Amount (₹)</label>
                            <input type="number" name="limitAmount" step="0.01" required>
                        </div>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label>Start Date</label>
                            <input type="date" name="startDate" required>
                        </div>
                        <div class="form-group">
                            <label>End Date</label>
                            <input type="date" name="endDate" required>
                        </div>
                    </div>
                    <button type="submit" class="btn btn-primary">Create Budget</button>
                </form>
            </div>

            <!-- Budget List -->
            <div class="section">
                <h2>Your Budgets</h2>
                <div class="budgets-grid">
                    <c:forEach var="budget" items="${budgets}">
                        <div class="budget-card">
                            <h3>${budget.category}</h3>
                            <div class="progress-bar">
                                <c:set var="usagePercent" value="${budget.usagePercent}"/>
                                <c:choose>
                                    <c:when test="${usagePercent >= 90}">
                                        <c:set var="progressColor" value="progress-danger"/>
                                    </c:when>
                                    <c:when test="${usagePercent >= 70}">
                                        <c:set var="progressColor" value="progress-warning"/>
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="progressColor" value="progress-success"/>
                                    </c:otherwise>
                                </c:choose>
                                <div class="progress-fill ${progressColor}" style="width: ${usagePercent}%"></div>
                            </div>
                            <p>₹${budget.spentAmount} / ₹${budget.limitAmount} (${usagePercent}%)</p>
                            <p class="remaining">Remaining: ₹${budget.remainingAmount}</p>
                            <form action="${pageContext.request.contextPath}/budgets" method="post">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="id" value="${budget.id}">
                                <button type="submit" class="btn btn-danger btn-sm">Delete</button>
                            </form>
                        </div>
                    </c:forEach>
                    <c:if test="${empty budgets}">
                        <p class="empty-state">No budgets set. Create your first budget!</p>
                    </c:if>
                </div>
            </div>
        </main>
    </div>
</body>
</html>

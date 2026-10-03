<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Goals - MyFinance</title>
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
                <a href="${pageContext.request.contextPath}/budgets" class="nav-item">
                    <span>Budgets</span>
                </a>
                <a href="${pageContext.request.contextPath}/goals" class="nav-item active">
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
                    <h1>Goals</h1>
                </div>
                <div class="topbar-right">
                    <span class="user-name">${sessionScope.userName}</span>
                </div>
            </header>

            <!-- Create Goal Form -->
            <div class="section">
                <h2>Create Goal</h2>
                <form action="${pageContext.request.contextPath}/goals" method="post" class="form-card">
                    <input type="hidden" name="action" value="create">
                    <div class="form-group">
                        <label>Goal Title</label>
                        <input type="text" name="title" required>
                    </div>
                    <div class="form-group">
                        <label>Description</label>
                        <textarea name="description" rows="2"></textarea>
                    </div>
                    <div class="form-row">
                        <div class="form-group">
                            <label>Target Amount (₹)</label>
                            <input type="number" name="targetAmount" step="0.01" required>
                        </div>
                        <div class="form-group">
                            <label>Deadline</label>
                            <input type="date" name="deadline" required>
                        </div>
                    </div>
                    <button type="submit" class="btn btn-primary">Create Goal</button>
                </form>
            </div>

            <!-- Goals List -->
            <div class="section">
                <h2>Your Goals</h2>
                <div class="goals-grid">
                    <c:forEach var="goal" items="${goals}">
                        <div class="goal-card">
                            <h3>${goal.title}</h3>
                            <c:if test="${not empty goal.description}">
                                <p class="goal-description">${goal.description}</p>
                            </c:if>
                            <div class="progress-bar">
                                <c:set var="progressPercent" value="${goal.progressPercent}"/>
                                <c:choose>
                                    <c:when test="${progressPercent >= 100}">
                                        <c:set var="progressColor" value="progress-success"/>
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="progressColor" value="progress-primary"/>
                                    </c:otherwise>
                                </c:choose>
                                <div class="progress-fill ${progressColor}" style="width: ${progressPercent}%"></div>
                            </div>
                            <p>₹${goal.savedAmount} / ₹${goal.targetAmount} (${progressPercent}%)</p>
                            <p class="days-left">${goal.daysLeft} days left</p>

                            <!-- Add Savings Form -->
                            <form action="${pageContext.request.contextPath}/goals" method="post" class="add-savings-form">
                                <input type="hidden" name="action" value="add-savings">
                                <input type="hidden" name="id" value="${goal.id}">
                                <input type="number" name="amount" step="0.01" placeholder="Add savings" required>
                                <button type="submit" class="btn btn-primary btn-sm">Add</button>
                            </form>

                            <form action="${pageContext.request.contextPath}/goals" method="post">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="id" value="${goal.id}">
                                <button type="submit" class="btn btn-danger btn-sm">Delete</button>
                            </form>
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

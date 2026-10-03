<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Expenses - MyFinance</title>
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
                <a href="${pageContext.request.contextPath}/expenses" class="nav-item active">
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
            <header class="topbar">
                <div class="topbar-left">
                    <h1>Expenses</h1>
                </div>
                <div class="topbar-right">
                    <span class="user-name">${sessionScope.userName}</span>
                </div>
            </header>

            <!-- Add Expense Form -->
            <div class="section">
                <h2>Add Expense</h2>
                <form action="${pageContext.request.contextPath}/expenses" method="post" class="form-card">
                    <input type="hidden" name="action" value="add">
                    <div class="form-row">
                        <div class="form-group">
                            <label>Title</label>
                            <input type="text" name="title" required>
                        </div>
                        <div class="form-group">
                            <label>Amount (₹)</label>
                            <input type="number" name="amount" step="0.01" required>
                        </div>
                    </div>
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
                            <label>Date</label>
                            <input type="date" name="expenseDate" required>
                        </div>
                    </div>
                    <div class="form-group">
                        <label>Description</label>
                        <textarea name="description" rows="2"></textarea>
                    </div>
                    <button type="submit" class="btn btn-primary">Add Expense</button>
                </form>
            </div>

            <!-- Expense List -->
            <div class="section">
                <h2>Expense History</h2>
                <div class="expense-list">
                    <c:forEach var="expense" items="${expenses}">
                        <div class="expense-item">
                            <div class="expense-info">
                                <span class="expense-title">${expense.title}</span>
                                <span class="expense-category">${expense.category}</span>
                                <span class="expense-date">${expense.expenseDate}</span>
                            </div>
                            <div class="expense-actions">
                                <span class="expense-amount">₹${expense.amount}</span>
                                <form action="${pageContext.request.contextPath}/expenses" method="post" style="display:inline;">
                                    <input type="hidden" name="action" value="delete">
                                    <input type="hidden" name="id" value="${expense.id}">
                                    <button type="submit" class="btn btn-danger btn-sm">Delete</button>
                                </form>
                            </div>
                        </div>
                    </c:forEach>
                    <c:if test="${empty expenses}">
                        <p class="empty-state">No expenses recorded yet.</p>
                    </c:if>
                </div>
            </div>
        </main>
    </div>
</body>
</html>

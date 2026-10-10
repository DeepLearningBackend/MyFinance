<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="activeNav" value="expenses" scope="request"/>
<c:set var="pageTitle" value="Expenses & Transactions" scope="request"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Expenses — MyFinance</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/responsive.css">
</head>
<body>
    <div class="app-wrapper">
        <jsp:include page="/jsp/common/sidebar.jsp"/>

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

                <!-- Filter Header Card -->
                <div class="glass-card" style="margin-bottom: 24px; padding: 18px 24px;">
                    <form action="${pageContext.request.contextPath}/expenses" method="get" style="display: flex; flex-wrap: wrap; gap: 14px; align-items: flex-end;">
                        <div style="flex: 1.5; min-width: 200px;">
                            <label class="form-label" for="search">Search</label>
                            <input type="text" id="search" name="search" class="form-control" 
                                   placeholder="Search by title, vendor..." value="${searchQuery}">
                        </div>

                        <div style="flex: 1; min-width: 140px;">
                            <label class="form-label" for="filterCategory">Category</label>
                            <select id="filterCategory" name="category" class="form-control">
                                <option value="ALL">All Categories</option>
                                <c:forEach var="cat" items="${categories}">
                                    <option value="${cat}" ${selectedCategory eq cat ? 'selected' : ''}>${cat}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <div style="width: 130px;">
                            <label class="form-label" for="filterMonth">Month</label>
                            <select id="filterMonth" name="month" class="form-control">
                                <option value="ALL">All Months</option>
                                <c:forEach var="m" begin="1" end="12">
                                    <option value="${m}" ${selectedMonth eq m ? 'selected' : ''}>Month ${m}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <div style="width: 110px;">
                            <label class="form-label" for="filterYear">Year</label>
                            <input type="number" id="filterYear" name="year" class="form-control" 
                                   value="${selectedYear}" min="2020" max="2030">
                        </div>

                        <div style="display: flex; gap: 8px;">
                            <button type="submit" class="btn btn-primary btn-sm" style="height: 42px; padding: 0 18px;">
                                Filter
                            </button>
                            <a href="${pageContext.request.contextPath}/expenses" class="btn btn-secondary btn-sm" style="height: 42px; padding: 0 14px; align-items: center; display: inline-flex;">
                                Reset
                            </a>
                        </div>
                    </form>
                </div>

                <!-- Real-Time Mobile SMS Sync Section -->
                <div class="glass-card" style="margin-bottom: 24px; padding: 20px 24px;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; flex-wrap: wrap; gap: 10px;">
                        <div style="display: flex; align-items: center; gap: 12px;">
                            <h3 style="font-size: 1.15rem; margin: 0; display: flex; align-items: center; gap: 8px;">
                                <span>📱 Mobile SMS Live Sync</span>
                                <span class="badge" id="pendingSmsCount" style="display: none; background: #f43f5e; color: #fff; font-size: 0.72rem; border-radius: 12px;">0</span>
                            </h3>
                            <div style="display: flex; align-items: center; gap: 8px; font-size: 0.82rem; color: var(--text-muted); background: rgba(255,255,255,0.03); padding: 4px 12px; border-radius: 20px;">
                                <span id="pageLiveSyncDot" style="display: inline-block; width: 8px; height: 8px; border-radius: 50%; background: #64748b;"></span>
                                <span id="pageLiveSyncText">Checking sync status...</span>
                            </div>
                        </div>
                        <div>
                            <a href="${pageContext.request.contextPath}/profile" class="btn btn-outline btn-sm" style="font-size: 0.8rem;">
                                ⚙️ Webhook & Token Setup &rarr;
                            </a>
                        </div>
                    </div>

                    <!-- Pending SMS Items Container (Rendered dynamically by sms-sync.js) -->
                    <div id="pendingSmsContainer" style="margin-bottom: 16px;">
                        <div style="padding: 16px; text-align: center; color: var(--text-muted); font-size: 0.85rem;">
                            Scanning for pending bank SMS sync records...
                        </div>
                    </div>

                    <!-- Backup: Quick Paste SMS Box -->
                    <details style="background: rgba(0,0,0,0.2); border-radius: var(--radius-sm); padding: 12px 16px; border: 1px solid var(--border-color);">
                        <summary style="font-size: 0.85rem; color: var(--text-muted); cursor: pointer; user-select: none; font-weight: 600;">
                            📋 Or Paste Bank SMS Text Manually (Instant Auto-Parse)
                        </summary>
                        <div style="margin-top: 12px; display: flex; flex-direction: column; gap: 10px;">
                            <textarea id="pasteSmsInput" class="form-control" rows="2" 
                                      placeholder="Paste any bank debit SMS (e.g. 'Your A/C debited by INR 450.00 to Swiggy. UPI Ref: 123456789012')"></textarea>
                            <div style="display: flex; justify-content: flex-end;">
                                <button type="button" id="pasteSmsSubmitBtn" class="btn btn-primary btn-sm">
                                    ⚡ Parse &amp; Queue SMS
                                </button>
                            </div>
                        </div>
                    </details>
                </div>

                <!-- Two Column Layout: Add Form + Expense Table -->
                <div class="grid-main-side">
                    <!-- Left: Expenses List Table -->
                    <div class="glass-card">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                            <div>
                                <h3 style="font-size: 1.2rem;">Transaction Records</h3>
                                <p style="font-size: 0.85rem; color: var(--text-muted);">${expenses.size()} entries matching criteria</p>
                            </div>
                            <div style="text-align: right;">
                                <span style="font-size: 0.75rem; text-transform: uppercase; color: var(--text-dim); font-weight: 700;">Filtered Total</span>
                                <div style="font-family: 'Space Grotesk', sans-serif; font-size: 1.4rem; font-weight: 700; color: #f43f5e;">
                                    ₹<fmt:formatNumber value="${totalFilteredSum}" pattern="#,##0.00"/>
                                </div>
                            </div>
                        </div>

                        <div class="table-responsive">
                            <table class="custom-table">
                                <thead>
                                    <tr>
                                        <th>Date</th>
                                        <th>Title &amp; Notes</th>
                                        <th>Category</th>
                                        <th style="text-align: right;">Amount</th>
                                        <th style="text-align: center;">Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="exp" items="${expenses}">
                                        <tr>
                                            <td style="color: var(--text-muted); white-space: nowrap;">${exp.expenseDate}</td>
                                            <td>
                                                <div style="font-weight: 600; color: #ffffff; display: flex; align-items: center; gap: 8px;">
                                                    <span>${exp.title}</span>
                                                    <c:if test="${exp.source eq 'SMS_SYNC'}">
                                                        <span class="badge" style="background: rgba(56, 189, 248, 0.15); color: #38bdf8; font-size: 0.68rem; padding: 2px 6px;">📱 SMS Sync</span>
                                                    </c:if>
                                                </div>
                                                <c:if test="${not empty exp.upiRef}">
                                                    <div style="font-size: 0.75rem; color: #38bdf8; font-family: monospace;">UPI: ${exp.upiRef}</div>
                                                </c:if>
                                                <c:if test="${not empty exp.description}">
                                                    <div style="font-size: 0.8rem; color: var(--text-dim);">${exp.description}</div>
                                                </c:if>
                                            </td>
                                            <td>
                                                <span class="badge badge-primary">${exp.category}</span>
                                            </td>
                                            <td style="text-align: right; font-family: 'Space Grotesk', sans-serif; font-weight: 700; color: #ffffff;">
                                                -₹<fmt:formatNumber value="${exp.amount}" pattern="#,##0.00"/>
                                            </td>
                                            <td style="text-align: center; white-space: nowrap;">
                                                <form action="${pageContext.request.contextPath}/expenses" method="post" style="display: inline;"
                                                      onsubmit="return confirm('Are you sure you want to delete this expense?');">
                                                    <input type="hidden" name="action" value="delete">
                                                    <input type="hidden" name="id" value="${exp.id}">
                                                    <button type="submit" class="btn btn-danger btn-sm" title="Delete Expense">
                                                        🗑️
                                                    </button>
                                                </form>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty expenses}">
                                        <tr>
                                            <td colspan="5" style="text-align: center; color: var(--text-muted); padding: 40px;">
                                                No expense records found. Record a new transaction using the form on the right.
                                            </td>
                                        </tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <!-- Right: Add Expense Card with Category Auto-Suggest -->
                    <div class="glass-card" style="height: fit-content;">
                        <div style="margin-bottom: 20px;">
                            <h3 style="font-size: 1.2rem;">Record New Expense</h3>
                            <p style="font-size: 0.85rem; color: var(--text-muted);">Instant logging with smart auto-tagging</p>
                        </div>

                        <form action="${pageContext.request.contextPath}/expenses" method="post" id="expenseForm">
                            <input type="hidden" name="action" value="add">

                            <div class="form-group">
                                <label class="form-label" for="title">Transaction Title</label>
                                <input type="text" id="title" name="title" class="form-control" 
                                       placeholder="e.g. Swiggy Lunch, Uber Ride, Petrol" required
                                       oninput="handleCategoryAutoSuggest(this.value)">
                                <div id="suggestBadge" style="display: none; margin-top: 6px; font-size: 0.8rem; color: var(--secondary);">
                                    ⚡ Suggested: <span id="suggestedName" style="font-weight: 700;"></span>
                                </div>
                            </div>

                            <div class="form-group">
                                <label class="form-label" for="amount">Amount (₹)</label>
                                <input type="number" step="0.01" min="0.01" id="amount" name="amount" class="form-control" 
                                       placeholder="0.00" required>
                            </div>

                            <div class="form-group">
                                <label class="form-label" for="categorySelect">Category</label>
                                <select id="categorySelect" name="category" class="form-control" required>
                                    <c:forEach var="cat" items="${categories}">
                                        <option value="${cat}">${cat}</option>
                                    </c:forEach>
                                </select>
                            </div>

                            <div class="form-group">
                                <label class="form-label" for="expenseDate">Date</label>
                                <input type="date" id="expenseDate" name="expenseDate" class="form-control" required>
                            </div>

                            <div class="form-group">
                                <label class="form-label" for="description">Notes / Remarks (Optional)</label>
                                <textarea id="description" name="description" class="form-control" rows="2" 
                                          placeholder="Additional details..."></textarea>
                            </div>

                            <div style="margin-top: 24px;">
                                <button type="submit" class="btn btn-primary" style="width: 100%; padding: 12px;">
                                    + Add Expense
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Spend Guard Pre-Insert Warning Modal (Innovation 1: Golden Rule 6) -->
    <c:if test="${not empty spendGuardAlert}">
        <div id="spendGuardModal" style="position: fixed; inset: 0; background: rgba(0, 0, 0, 0.78); backdrop-filter: blur(8px); display: flex; align-items: center; justify-content: center; z-index: 10000; padding: 20px;">
            <div class="glass-card" style="width: 100%; max-width: 480px; border: 2px solid ${spendGuardAlert.isOverLimit() ? '#ef4444' : '#f59e0b'}; box-shadow: 0 0 35px ${spendGuardAlert.isOverLimit() ? 'rgba(239, 68, 68, 0.45)' : 'rgba(245, 158, 11, 0.45)'}; padding: 28px; border-radius: var(--radius-md);">
                <div style="display: flex; align-items: center; gap: 14px; margin-bottom: 16px;">
                    <div style="font-size: 2.2rem;">${spendGuardAlert.isOverLimit() ? '🚨' : '⚠️'}</div>
                    <div>
                        <h3 style="font-size: 1.25rem; font-weight: 700; color: ${spendGuardAlert.isOverLimit() ? '#f87171' : '#fbbf24'}; margin: 0 0 4px 0;">
                            <c:choose>
                                <c:when test="${spendGuardAlert.isOverLimit()}">Oops, you've gone over 🙈</c:when>
                                <c:otherwise>Spend Guard Warning</c:otherwise>
                            </c:choose>
                        </h3>
                        <span class="badge" style="background: ${spendGuardAlert.isOverLimit() ? 'rgba(239, 68, 68, 0.2)' : 'rgba(245, 158, 11, 0.2)'}; color: ${spendGuardAlert.isOverLimit() ? '#fca5a5' : '#fcd34d'}; font-size: 0.72rem; text-transform: uppercase;">
                            ${spendGuardAlert.level}
                        </span>
                    </div>
                </div>

                <p style="font-size: 0.95rem; color: var(--text-color); margin-bottom: 16px; line-height: 1.5;">
                    <c:choose>
                        <c:when test="${spendGuardAlert.isOverLimit()}">
                            Adding this expense will exceed your <strong>${spendGuardAlert.category}</strong> budget!
                        </c:when>
                        <c:otherwise>
                            Heads up! Adding this expense will reach <strong><fmt:formatNumber value="${spendGuardAlert.prospectivePercent}" pattern="#,##0"/>%</strong> of your <strong>${spendGuardAlert.category}</strong> budget.
                        </c:otherwise>
                    </c:choose>
                </p>

                <div style="background: rgba(15, 23, 42, 0.85); border: 1px dashed ${spendGuardAlert.isOverLimit() ? 'rgba(239, 68, 68, 0.5)' : 'rgba(245, 158, 11, 0.5)'}; border-radius: 8px; padding: 14px; margin-bottom: 22px; text-align: center;">
                    <div style="font-size: 0.72rem; color: var(--text-muted); margin-bottom: 4px; text-transform: uppercase; letter-spacing: 0.5px;">Formula</div>
                    <div style="font-family: 'Space Grotesk', monospace; font-size: 1.05rem; font-weight: 700; color: ${spendGuardAlert.isOverLimit() ? '#f87171' : '#fbbf24'};">
                        ${spendGuardAlert.formula}
                    </div>
                </div>

                <div style="display: flex; gap: 12px; justify-content: flex-end;">
                    <button type="button" class="btn btn-secondary" onclick="document.getElementById('spendGuardModal').style.display='none';">
                        Cancel
                    </button>
                    <form action="${pageContext.request.contextPath}/expenses" method="post" style="margin: 0;">
                        <input type="hidden" name="action" value="add">
                        <input type="hidden" name="force" value="true">
                        <input type="hidden" name="title" value="<c:out value="${pendingTitle}"/>">
                        <input type="hidden" name="amount" value="${pendingAmount}">
                        <input type="hidden" name="category" value="<c:out value="${pendingCategory}"/>">
                        <input type="hidden" name="expenseDate" value="${pendingDate}">
                        <input type="hidden" name="description" value="<c:out value="${pendingDescription}"/>">
                        <button type="submit" class="btn btn-primary" style="background: ${spendGuardAlert.isOverLimit() ? 'linear-gradient(135deg, #ef4444, #dc2626)' : 'linear-gradient(135deg, #f59e0b, #d97706)'}; border: none; padding: 10px 20px;">
                            Log Anyway
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </c:if>

    <script src="${pageContext.request.contextPath}/js/validation.js"></script>
    <script src="${pageContext.request.contextPath}/js/offline.js"></script>
    <script src="${pageContext.request.contextPath}/js/sms-sync.js"></script>
    <script>
        // Set today's date by default
        document.getElementById('expenseDate').valueAsDate = new Date();

        function toggleSidebar() {
            const sidebar = document.getElementById('appSidebar');
            const backdrop = document.getElementById('sidebarBackdrop');
            sidebar.classList.toggle('open');
            backdrop.classList.toggle('active');
        }

        // Live Category Auto-Suggest (Innovation 5)
        let suggestTimeout = null;
        function handleCategoryAutoSuggest(title) {
            clearTimeout(suggestTimeout);
            if (!title || title.trim().length < 2) {
                document.getElementById('suggestBadge').style.display = 'none';
                return;
            }

            suggestTimeout = setTimeout(() => {
                fetch('${pageContext.request.contextPath}/expenses', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/x-www-form-urlencoded'},
                    body: 'action=suggest&title=' + encodeURIComponent(title)
                })
                .then(r => r.json())
                .then(data => {
                    if (data && data.suggestedCategory && data.suggestedCategory !== 'Other') {
                        document.getElementById('suggestedName').textContent = data.suggestedCategory;
                        document.getElementById('suggestBadge').style.display = 'block';
                        
                        const select = document.getElementById('categorySelect');
                        for (let opt of select.options) {
                            if (opt.value === data.suggestedCategory) {
                                select.value = data.suggestedCategory;
                                break;
                            }
                        }
                    } else {
                        document.getElementById('suggestBadge').style.display = 'none';
                    }
                })
                .catch(() => {});
            }, 250);
        }
    </script>
</body>
</html>

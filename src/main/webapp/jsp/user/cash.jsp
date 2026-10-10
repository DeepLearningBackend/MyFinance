<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="activeNav" value="cash" scope="request"/>
<c:set var="pageTitle" value="Cash Tracker" scope="request"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cash Tracker — MyFinance</title>
    <meta name="description" content="Track your physical cash spending, set your opening balance and reconcile at month end.">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/responsive.css">
    <style>
        /* ── Cash Tracker page-specific styles ─────────────────────── */
        .cash-hero {
            display: flex;
            gap: 16px;
            flex-wrap: wrap;
            margin-bottom: 28px;
        }
        .cash-stat-card {
            flex: 1;
            min-width: 200px;
            background: var(--surface-card, #1e1e3a);
            border: 1px solid var(--border, #2a2a4a);
            border-radius: var(--radius, 12px);
            padding: 22px 24px;
            display: flex;
            flex-direction: column;
            gap: 6px;
            position: relative;
            overflow: hidden;
            transition: transform .18s, box-shadow .18s;
        }
        .cash-stat-card:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(0,0,0,.25); }
        .cash-stat-card::before {
            content: '';
            position: absolute;
            top: 0; left: 0; right: 0;
            height: 3px;
        }
        .cash-stat-card.green::before  { background: linear-gradient(90deg, #22c55e, #4ade80); }
        .cash-stat-card.orange::before { background: linear-gradient(90deg, #f97316, #fb923c); }
        .cash-stat-card.violet::before { background: linear-gradient(90deg, #8b5cf6, #a78bfa); }
        .stat-icon { font-size: 1.6rem; line-height: 1; }
        .stat-label { font-size: .75rem; font-weight: 600; color: var(--text-muted, #888); text-transform: uppercase; letter-spacing: .05em; }
        .stat-value { font-size: 1.75rem; font-weight: 800; color: var(--text, #e0e0ff); }
        .stat-sub   { font-size: .78rem; color: var(--text-muted, #888); }

        /* ── Two-column layout ──────────────────────────────────────── */
        .cash-grid {
            display: grid;
            grid-template-columns: 380px 1fr;
            gap: 24px;
            align-items: start;
        }
        @media (max-width: 900px) { .cash-grid { grid-template-columns: 1fr; } }

        /* ── Form card ──────────────────────────────────────────────── */
        .form-card {
            background: var(--surface-card, #1e1e3a);
            border: 1px solid var(--border, #2a2a4a);
            border-radius: var(--radius, 12px);
            padding: 24px;
        }
        .form-card-title {
            font-size: 1rem;
            font-weight: 700;
            color: var(--text, #e0e0ff);
            margin-bottom: 18px;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .tab-pills {
            display: flex;
            gap: 6px;
            margin-bottom: 20px;
            background: var(--bg, #13132a);
            border-radius: 8px;
            padding: 4px;
        }
        .tab-pill {
            flex: 1;
            text-align: center;
            padding: 7px 0;
            border-radius: 6px;
            font-size: .8rem;
            font-weight: 600;
            cursor: pointer;
            color: var(--text-muted, #888);
            transition: background .15s, color .15s;
        }
        .tab-pill.active {
            background: var(--primary, #f4a261);
            color: #fff;
        }

        /* ── Entry list ─────────────────────────────────────────────── */
        .entry-list { display: flex; flex-direction: column; gap: 10px; }
        .entry-row {
            display: flex;
            align-items: center;
            gap: 12px;
            background: var(--surface-card, #1e1e3a);
            border: 1px solid var(--border, #2a2a4a);
            border-radius: 10px;
            padding: 13px 16px;
            transition: background .15s;
        }
        .entry-row:hover { background: var(--surface-hover, #252548); }
        .entry-cat-icon {
            width: 40px; height: 40px;
            border-radius: 10px;
            display: grid;
            place-items: center;
            font-size: 1.2rem;
            background: rgba(139,92,246,.15);
            flex-shrink: 0;
        }
        .entry-meta { flex: 1; min-width: 0; }
        .entry-cat  { font-size: .85rem; font-weight: 700; color: var(--text, #e0e0ff); }
        .entry-desc { font-size: .75rem; color: var(--text-muted, #888); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
        .entry-date { font-size: .72rem; color: var(--text-muted, #888); }
        .entry-amount {
            font-size: 1rem;
            font-weight: 800;
            color: #f97316;
            white-space: nowrap;
        }
        .entry-delete-btn {
            background: none;
            border: none;
            cursor: pointer;
            font-size: 1rem;
            color: var(--text-muted, #888);
            padding: 4px;
            border-radius: 6px;
            transition: color .15s, background .15s;
        }
        .entry-delete-btn:hover { color: var(--danger, #ef4444); background: rgba(239,68,68,.12); }

        /* ── Reconcile box ──────────────────────────────────────────── */
        .reconcile-result {
            margin-top: 20px;
            padding: 14px 18px;
            border-radius: 10px;
            font-size: .9rem;
            font-weight: 600;
            border: 1px solid;
        }
        .reconcile-ok      { background: rgba(34,197,94,.1);  border-color: #22c55e; color: #4ade80; }
        .reconcile-warning { background: rgba(245,158,11,.1); border-color: #f59e0b; color: #fbbf24; }

        /* ── Empty state ────────────────────────────────────────────── */
        .empty-state { text-align: center; padding: 48px 24px; color: var(--text-muted, #888); }
        .empty-state-icon { font-size: 3rem; margin-bottom: 12px; }
        .empty-state-text { font-size: .95rem; }

        /* ── Section header ─────────────────────────────────────────── */
        .section-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 16px;
        }
        .section-title { font-size: 1rem; font-weight: 700; color: var(--text, #e0e0ff); }
        .badge-count {
            background: var(--primary, #f4a261);
            color: #fff;
            border-radius: 20px;
            padding: 2px 10px;
            font-size: .75rem;
            font-weight: 700;
        }
    </style>
</head>
<body>
<div class="app-wrapper">
    <jsp:include page="/jsp/common/sidebar.jsp"/>

    <div class="app-main">
        <jsp:include page="/jsp/common/topbar.jsp"/>

        <div class="content-body">

            <%-- Flash messages --%>
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

            <%-- ── Page header ────────────────────────────────────── --%>
            <div class="page-header">
                <div>
                    <h1 class="page-title">💵 Cash Tracker</h1>
                    <p class="page-subtitle">Track physical cash, set your opening balance and reconcile at month end.</p>
                </div>
            </div>

            <%-- ── Stats row ──────────────────────────────────────── --%>
            <div class="cash-hero">
                <div class="cash-stat-card green">
                    <span class="stat-icon">💰</span>
                    <span class="stat-label">Cash in Hand</span>
                    <span class="stat-value">₹<fmt:formatNumber value="${cashInHand}" pattern="#,##0.00"/></span>
                    <span class="stat-sub">Current running balance</span>
                </div>
                <div class="cash-stat-card orange">
                    <span class="stat-icon">🧾</span>
                    <span class="stat-label">Spent This Month</span>
                    <span class="stat-value">₹<fmt:formatNumber value="${totalSpentThisMonth}" pattern="#,##0.00"/></span>
                    <span class="stat-sub">Total cash entries logged</span>
                </div>
                <div class="cash-stat-card violet">
                    <span class="stat-icon">📋</span>
                    <span class="stat-label">Entries</span>
                    <span class="stat-value"><c:out value="${cashEntries.size()}"/></span>
                    <span class="stat-sub">Cash transactions recorded</span>
                </div>
            </div>

            <%-- ── Reconciliation result (after POST) ─────────────── --%>
            <c:if test="${not empty reconcileResult}">
                <c:set var="isOk" value="${reconcileResult.startsWith('✅')}"/>
                <div class="reconcile-result ${isOk ? 'reconcile-ok' : 'reconcile-warning'}">
                    ${reconcileResult}
                </div>
            </c:if>

            <%-- ── Two-column grid ────────────────────────────────── --%>
            <div class="cash-grid">

                <%-- ── LEFT: Forms ───────────────────────────────── --%>
                <div>
                    <%-- Tab switcher --%>
                    <div class="form-card">
                        <div class="tab-pills" id="tabPills">
                            <div class="tab-pill active" onclick="showTab('add')"      id="tab-add">💸 Add Expense</div>
                            <div class="tab-pill"        onclick="showTab('balance')"  id="tab-balance">💰 Set Balance</div>
                            <div class="tab-pill"        onclick="showTab('reconcile')" id="tab-reconcile">📊 Reconcile</div>
                        </div>

                        <%-- Add cash expense form --%>
                        <div id="panel-add">
                            <div class="form-card-title">💸 Record Cash Expense</div>
                            <form action="${pageContext.request.contextPath}/cash" method="post" id="cashAddForm">
                                <input type="hidden" name="action" value="add">

                                <div class="form-group">
                                    <label class="form-label" for="cashAmount">Amount (₹) *</label>
                                    <input type="number" id="cashAmount" name="amount"
                                           class="form-control" placeholder="e.g. 150.00"
                                           step="0.01" min="0.01" required>
                                </div>

                                <div class="form-group">
                                    <label class="form-label" for="cashCategory">Category *</label>
                                    <select id="cashCategory" name="category" class="form-control" required>
                                        <option value="">— Select category —</option>
                                        <c:forEach var="cat" items="${categories}">
                                            <option value="${cat}">${cat}</option>
                                        </c:forEach>
                                    </select>
                                </div>

                                <div class="form-group">
                                    <label class="form-label" for="cashDate">Date *</label>
                                    <input type="date" id="cashDate" name="entryDate"
                                           class="form-control" required
                                           value="${pageContext.request.contextPath}">
                                </div>

                                <div class="form-group">
                                    <label class="form-label" for="cashDesc">Description</label>
                                    <input type="text" id="cashDesc" name="description"
                                           class="form-control" placeholder="e.g. Chai at corner shop" maxlength="200">
                                </div>

                                <button type="submit" class="btn btn-primary" style="width:100%;">
                                    💸 Record Cash Expense
                                </button>
                            </form>
                        </div>

                        <%-- Set balance form --%>
                        <div id="panel-balance" style="display:none;">
                            <div class="form-card-title">💰 Set Opening Balance</div>
                            <p style="font-size:.83rem;color:var(--text-muted);margin-bottom:16px;">
                                Set how much physical cash you have right now.
                                This resets your running balance to this amount.
                            </p>
                            <form action="${pageContext.request.contextPath}/cash" method="post">
                                <input type="hidden" name="action" value="set-balance">
                                <div class="form-group">
                                    <label class="form-label" for="openingBal">Cash in Hand (₹) *</label>
                                    <input type="number" id="openingBal" name="openingBalance"
                                           class="form-control" placeholder="e.g. 2000.00"
                                           step="0.01" min="0" required
                                           value="<fmt:formatNumber value='${cashInHand}' pattern='#0.00'/>">
                                </div>
                                <button type="submit" class="btn btn-primary" style="width:100%;">
                                    💰 Update Balance
                                </button>
                            </form>
                        </div>

                        <%-- Reconcile form --%>
                        <div id="panel-reconcile" style="display:none;">
                            <div class="form-card-title">📊 End-of-Month Reconciliation</div>
                            <p style="font-size:.83rem;color:var(--text-muted);margin-bottom:16px;">
                                Enter your opening balance for the month. The system will compare
                                it against your logged entries and flag any discrepancy.
                            </p>
                            <form action="${pageContext.request.contextPath}/cash" method="post">
                                <input type="hidden" name="action" value="reconcile">
                                <div class="form-group">
                                    <label class="form-label" for="recOpening">Opening Balance (₹) *</label>
                                    <input type="number" id="recOpening" name="openingBalance"
                                           class="form-control" placeholder="e.g. 5000.00"
                                           step="0.01" min="0" required>
                                </div>
                                <div class="form-row" style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
                                    <div class="form-group">
                                        <label class="form-label" for="recMonth">Month *</label>
                                        <select id="recMonth" name="month" class="form-control" required>
                                            <c:forEach begin="1" end="12" var="m">
                                                <option value="${m}" ${m == selectedMonth ? 'selected' : ''}>
                                                    <fmt:parseDate value="${m}" pattern="M" var="parsedM"/>
                                                    <fmt:formatDate value="${parsedM}" pattern="MMMM"/>
                                                </option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <div class="form-group">
                                        <label class="form-label" for="recYear">Year *</label>
                                        <input type="number" id="recYear" name="year"
                                               class="form-control" value="${selectedYear}"
                                               min="2020" max="2099" required>
                                    </div>
                                </div>
                                <button type="submit" class="btn btn-primary" style="width:100%;">
                                    📊 Run Reconciliation
                                </button>
                            </form>
                        </div>
                    </div>
                </div>

                <%-- ── RIGHT: Cash entries list ───────────────────── --%>
                <div>
                    <div class="section-header">
                        <span class="section-title">🧾 Cash Entries</span>
                        <span class="badge-count">${cashEntries.size()}</span>
                    </div>

                    <c:choose>
                        <c:when test="${empty cashEntries}">
                            <div class="empty-state">
                                <div class="empty-state-icon">💵</div>
                                <div class="empty-state-text">No cash entries yet.<br>
                                    Set your opening balance and record your first cash expense.</div>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="entry-list">
                                <c:forEach var="entry" items="${cashEntries}">
                                    <div class="entry-row">
                                        <div class="entry-cat-icon">
                                            <c:choose>
                                                <c:when test="${entry.category eq 'Food & Dining'}">🍔</c:when>
                                                <c:when test="${entry.category eq 'Shopping'}">🛍️</c:when>
                                                <c:when test="${entry.category eq 'Transport & Fuel'}">🚗</c:when>
                                                <c:when test="${entry.category eq 'Rent & Housing'}">🏠</c:when>
                                                <c:when test="${entry.category eq 'Bills & Utilities'}">💡</c:when>
                                                <c:when test="${entry.category eq 'Entertainment'}">🎬</c:when>
                                                <c:when test="${entry.category eq 'Health & Medical'}">💊</c:when>
                                                <c:when test="${entry.category eq 'Education'}">📚</c:when>
                                                <c:when test="${entry.category eq 'Investment & Savings'}">📈</c:when>
                                                <c:otherwise>💳</c:otherwise>
                                            </c:choose>
                                        </div>
                                        <div class="entry-meta">
                                            <div class="entry-cat">${entry.category}</div>
                                            <div class="entry-desc">
                                                <c:out value="${not empty entry.description ? entry.description : '—'}"/>
                                            </div>
                                            <div class="entry-date">
                                                ${entry.entryDate}
                                            </div>
                                        </div>
                                        <div class="entry-amount">
                                            − ₹<fmt:formatNumber value="${entry.amount}" pattern="#,##0.00"/>
                                        </div>
                                        <form action="${pageContext.request.contextPath}/cash" method="post"
                                              style="display:inline;"
                                              onsubmit="return confirm('Remove this cash entry?');">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id"     value="${entry.id}">
                                            <button type="submit" class="entry-delete-btn" title="Delete">🗑️</button>
                                        </form>
                                    </div>
                                </c:forEach>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div><%-- end cash-grid --%>

        </div><%-- end content-body --%>
    </div><%-- end app-main --%>
</div><%-- end app-wrapper --%>

<script>
    // Set today's date as default for the entry date input
    (function () {
        const d = document.getElementById('cashDate');
        if (d && !d.value) {
            const now = new Date();
            d.value = now.toISOString().split('T')[0];
        }
    })();

    // Tab switcher
    function showTab(name) {
        ['add', 'balance', 'reconcile'].forEach(t => {
            document.getElementById('panel-' + t).style.display  = t === name ? 'block' : 'none';
            document.getElementById('tab-'   + t).className =
                'tab-pill' + (t === name ? ' active' : '');
        });
    }

    // If reconcile result is visible, auto-open reconcile tab
    <c:if test="${not empty reconcileResult}">
    showTab('reconcile');
    </c:if>
</script>
</body>
</html>

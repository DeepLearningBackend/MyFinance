<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="activeNav" value="personality" scope="request"/>
<c:set var="pageTitle" value="Spending Personality" scope="request"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Spending Personality — MyFinance</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/responsive.css">
    <style>
        /* ── Personality page styles ─────────────────────────────── */
        .personality-hero {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 0;
            padding: 48px 24px 32px;
            text-align: center;
        }
        .personality-ring {
            width: 160px; height: 160px;
            border-radius: 50%;
            display: flex; align-items: center; justify-content: center;
            font-size: 4.5rem;
            margin-bottom: 24px;
            position: relative;
            background: conic-gradient(var(--accent-color, #a855f7) 0%, rgba(168,85,247,.15) 100%);
            box-shadow: 0 0 0 6px rgba(168,85,247,.18), 0 0 40px rgba(168,85,247,.25);
            animation: ringPulse 2.5s ease-in-out infinite;
        }
        @keyframes ringPulse {
            0%,100% { box-shadow: 0 0 0 6px rgba(168,85,247,.18), 0 0 40px rgba(168,85,247,.25); }
            50%      { box-shadow: 0 0 0 14px rgba(168,85,247,.08), 0 0 60px rgba(168,85,247,.35); }
        }
        /* ── Reveal animation (plays only on first view) ─────────── */
        .reveal-container { perspective: 800px; }
        .reveal-card {
            opacity: 0;
            transform: rotateY(90deg) scale(0.8);
            transition: opacity .7s ease, transform .7s cubic-bezier(.34,1.56,.64,1);
        }
        .reveal-card.revealed {
            opacity: 1;
            transform: rotateY(0deg) scale(1);
        }
        .personality-type-label {
            font-size: 2rem;
            font-weight: 800;
            margin-bottom: 6px;
            background: linear-gradient(135deg, #f0fdf4, var(--accent-color, #a855f7));
            -webkit-background-clip: text;
            -webkit-text-fill-color: transparent;
            background-clip: text;
        }
        .personality-trait {
            font-size: 1.1rem;
            color: #94a3b8;
            margin-bottom: 20px;
        }
        .personality-description {
            max-width: 560px;
            font-size: 0.92rem;
            color: #cbd5e1;
            line-height: 1.7;
        }

        /* ── Score breakdown grid ────────────────────────────────── */
        .score-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
            gap: 12px;
            margin-top: 28px;
        }
        .score-chip {
            background: rgba(255,255,255,0.05);
            border: 1px solid rgba(255,255,255,0.08);
            border-radius: 12px;
            padding: 14px 16px;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .score-chip.winner {
            border-color: var(--accent-color, #a855f7);
            background: rgba(168,85,247,0.1);
        }
        .score-bar-wrap {
            flex: 1;
        }
        .score-bar-label { font-size: 0.75rem; color: #94a3b8; margin-bottom: 4px; }
        .score-bar-track {
            height: 6px;
            background: rgba(255,255,255,0.08);
            border-radius: 99px;
            overflow: hidden;
        }
        .score-bar-fill {
            height: 100%;
            border-radius: 99px;
            background: var(--accent-color, #a855f7);
            width: 0;
            transition: width 1.2s ease;
        }
        .score-value { font-size: 0.9rem; font-weight: 700; color: #e2e8f0; min-width: 28px; text-align: right; }

        /* ── Not enough data state ───────────────────────────────── */
        .no-data-card {
            text-align: center;
            padding: 56px 32px;
            max-width: 480px;
            margin: 0 auto;
        }
        .no-data-icon { font-size: 3.5rem; margin-bottom: 16px; opacity: .7; }
        .no-data-title { font-size: 1.4rem; font-weight: 700; color: #f1f5f9; margin-bottom: 10px; }
        .no-data-sub   { font-size: 0.9rem; color: #64748b; line-height: 1.6; }

        /* ── Formula transparency strip ──────────────────────────── */
        .formula-strip {
            font-family: monospace;
            font-size: 0.78rem;
            color: #64748b;
            background: rgba(255,255,255,0.03);
            border: 1px solid rgba(255,255,255,0.06);
            border-radius: 8px;
            padding: 10px 16px;
            margin-top: 16px;
            word-break: break-word;
        }
    </style>
</head>
<body>
<div class="app-wrapper">
    <jsp:include page="/jsp/common/sidebar.jsp"/>

    <div class="app-main">
        <jsp:include page="/jsp/common/topbar.jsp"/>

        <div class="content-body">

            <div style="margin-bottom: 28px;">
                <h2 style="font-size: 1.5rem; font-weight: 700; color: #f1f5f9;">
                    🧠 Spending Personality
                </h2>
                <p style="color: #64748b; font-size: 0.88rem; margin-top: 4px;">
                    Based on your last ${windowDays} days of expenses · minimum ${minDays} day(s) of data required
                </p>
            </div>

            <c:choose>
                <%-- ══ RESULT ══════════════════════════════════════════════════ --%>
                <c:when test="${not empty report}">

                    <c:set var="accentColor" value="${report.colorAccent}"/>

                    <div class="glass-card reveal-container" style="
                            background: linear-gradient(135deg, #0f172a 0%, #1a1040 100%);
                            border: 1px solid rgba(168,85,247,0.25);
                            overflow: hidden;">

                        <%-- Hero section --%>
                        <div class="personality-hero reveal-card" id="revealCard">

                            <div class="personality-ring"
                                 style="--accent-color: ${accentColor};
                                        background: conic-gradient(${accentColor} 0%, rgba(30,30,60,.3) 100%);
                                        box-shadow: 0 0 0 6px ${accentColor}30, 0 0 50px ${accentColor}40;">
                                ${report.emoji}
                            </div>

                            <div class="personality-type-label"
                                 style="background: linear-gradient(135deg, #f0fdf4, ${accentColor});
                                        -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;">
                                ${report.personalityType}
                            </div>
                            <div class="personality-trait">${report.primaryTrait}</div>
                            <div class="personality-description">${report.description}</div>

                            <%-- Formula transparency --%>
                            <div class="formula-strip">
                                🔍 How we decided: ${report.formula}
                            </div>

                            <%-- Evidence strip --%>
                            <div style="display: flex; gap: 20px; margin-top: 20px; flex-wrap: wrap; justify-content: center;">
                                <div style="text-align: center;">
                                    <div style="font-size: 1.4rem; font-weight: 700; color: ${accentColor};">${report.totalExpenses}</div>
                                    <div style="font-size: 0.75rem; color: #64748b;">expenses analysed</div>
                                </div>
                                <div style="text-align: center;">
                                    <div style="font-size: 1.4rem; font-weight: 700; color: ${accentColor};">${report.dataPoints}</div>
                                    <div style="font-size: 0.75rem; color: #64748b;">spending days</div>
                                </div>
                                <div style="text-align: center;">
                                    <div style="font-size: 1.4rem; font-weight: 700; color: ${accentColor};">
                                        <fmt:formatNumber value="${report.topCategoryPercent}" pattern="0"/>%
                                    </div>
                                    <div style="font-size: 0.75rem; color: #64748b;">${report.topCategory}</div>
                                </div>
                            </div>
                        </div>

                        <%-- Score breakdown --%>
                        <div style="padding: 0 32px 32px;">
                            <h4 style="font-size: 0.82rem; font-weight: 600; color: #64748b;
                                        text-transform: uppercase; letter-spacing: .1em; margin-bottom: 14px;">
                                Score breakdown (HashMap)
                            </h4>
                            <div class="score-grid" id="scoreGrid">
                                <c:forEach var="entry" items="${report.scores}">
                                    <div class="score-chip ${entry.key eq report.personalityType ? 'winner' : ''}"
                                         style="--accent-color: ${accentColor};">
                                        <div class="score-bar-wrap">
                                            <div class="score-bar-label">${entry.key}</div>
                                            <div class="score-bar-track">
                                                <div class="score-bar-fill" data-score="${entry.value}"></div>
                                            </div>
                                        </div>
                                        <div class="score-value">${entry.value}</div>
                                    </div>
                                </c:forEach>
                            </div>

                            <%-- Refresh button --%>
                            <form method="post" action="${pageContext.request.contextPath}/personality"
                                  style="margin-top: 24px; text-align: center;">
                                <input type="hidden" name="action" value="refresh"/>
                                <button type="submit" class="btn btn-secondary btn-sm">
                                    🔄 Recalculate Personality
                                </button>
                            </form>
                        </div>
                    </div>

                </c:when>

                <%-- ══ NOT ENOUGH DATA ══════════════════════════════════════════ --%>
                <c:otherwise>
                    <div class="glass-card no-data-card">
                        <div class="no-data-icon">🧩</div>
                        <div class="no-data-title">Not Enough Data Yet</div>
                        <div class="no-data-sub">
                            We need at least <strong>${minDays}</strong> day(s) of expense history
                            within the last ${windowDays} days to detect your spending personality.<br><br>
                            Keep logging your expenses — your personality profile will unlock soon!
                        </div>
                        <a href="${pageContext.request.contextPath}/expenses"
                           class="btn btn-primary btn-sm" style="margin-top: 24px;">
                            ➕ Add Expenses
                        </a>
                    </div>
                </c:otherwise>
            </c:choose>

        </div><%-- content-body --%>
    </div><%-- app-main --%>
</div><%-- app-wrapper --%>

<script>
/* ── Reveal animation ────────────────────────────────────────────── */
(function () {
    var isNewReveal = ${isNewReveal};
    var card = document.getElementById('revealCard');
    if (!card) return;

    if (isNewReveal) {
        // Small delay so the page renders first, then flip in
        setTimeout(function () {
            card.classList.add('revealed');
        }, 180);
    } else {
        // Already seen — show immediately without animation
        card.classList.add('revealed');
    }

    /* ── Animate score bars after reveal ──────────────────────── */
    var fills = document.querySelectorAll('.score-bar-fill');
    var maxScore = 0;
    fills.forEach(function(f) {
        var s = parseInt(f.getAttribute('data-score'), 10) || 0;
        if (s > maxScore) maxScore = s;
    });
    if (maxScore === 0) maxScore = 1;

    var delay = isNewReveal ? 900 : 300;
    setTimeout(function () {
        fills.forEach(function(f) {
            var s = parseInt(f.getAttribute('data-score'), 10) || 0;
            f.style.width = ((s / maxScore) * 100) + '%';
        });
    }, delay);
})();
</script>
</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="activeNav" value="fincoach" scope="request"/>
<c:set var="pageTitle" value="FinCoach AI" scope="request"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>FinCoach AI — MyFinance</title>
    <meta name="description" content="AI Financial Education and Data-Driven Spending Coach.">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/responsive.css">
    <style>
        .fincoach-container {
            max-width: 900px;
            margin: 0 auto;
            display: flex;
            flex-direction: column;
            gap: 20px;
        }

        .fincoach-header-card {
            background: var(--surface-card, #1e1e3a);
            border: 1px solid var(--border, #2a2a4a);
            border-radius: var(--radius, 12px);
            padding: 24px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 16px;
        }

        .fincoach-title-area h1 {
            font-size: 1.5rem;
            font-weight: 800;
            color: var(--text, #e0e0ff);
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 6px;
        }

        .fincoach-subtitle {
            font-size: 0.88rem;
            color: var(--text-muted, #888);
        }

        .engine-badge {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 6px 14px;
            border-radius: 20px;
            font-size: 0.78rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.04em;
        }

        .engine-badge.gemini {
            background: rgba(139, 92, 246, 0.15);
            color: #a78bfa;
            border: 1px solid rgba(139, 92, 246, 0.35);
        }

        .engine-badge.rule-based {
            background: rgba(34, 197, 94, 0.15);
            color: #4ade80;
            border: 1px solid rgba(34, 197, 94, 0.35);
        }

        /* Chat Panel */
        .fincoach-chat-card {
            background: var(--surface-card, #1e1e3a);
            border: 1px solid var(--border, #2a2a4a);
            border-radius: var(--radius, 12px);
            display: flex;
            flex-direction: column;
            height: 600px;
            overflow: hidden;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.25);
        }

        .fincoach-quick-pills {
            display: flex;
            gap: 8px;
            padding: 12px 18px;
            background: rgba(0, 0, 0, 0.2);
            border-bottom: 1px solid var(--border, #2a2a4a);
            overflow-x: auto;
            white-space: nowrap;
        }

        .fincoach-pill {
            background: var(--bg, #13132a);
            border: 1px solid var(--border, #2a2a4a);
            color: var(--text, #e0e0ff);
            font-size: 0.78rem;
            font-weight: 600;
            padding: 6px 12px;
            border-radius: 16px;
            cursor: pointer;
            transition: all 0.15s ease;
        }

        .fincoach-pill:hover {
            border-color: var(--primary, #f4a261);
            color: var(--primary, #f4a261);
            transform: translateY(-1px);
        }

        .fincoach-messages {
            flex: 1;
            padding: 20px;
            overflow-y: auto;
            display: flex;
            flex-direction: column;
            gap: 16px;
        }

        .fincoach-msg-row {
            display: flex;
            gap: 12px;
            max-width: 85%;
        }

        .fincoach-msg-row.coach-row {
            align-self: flex-start;
        }

        .fincoach-msg-row.user-row {
            align-self: flex-end;
            flex-direction: row-reverse;
        }

        .coach-avatar-sm, .user-avatar-sm {
            width: 36px;
            height: 36px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.1rem;
            flex-shrink: 0;
        }

        .coach-avatar-sm {
            background: linear-gradient(135deg, #6366f1, #8b5cf6);
            border: 1px solid rgba(255, 255, 255, 0.2);
        }

        .user-avatar-sm {
            background: linear-gradient(135deg, #f4a261, #e76f51);
        }

        .fincoach-bubble {
            padding: 14px 18px;
            border-radius: 14px;
            font-size: 0.92rem;
            line-height: 1.55;
            position: relative;
        }

        .coach-bubble {
            background: var(--bg, #13132a);
            border: 1px solid var(--border, #2a2a4a);
            color: var(--text, #e0e0ff);
            border-top-left-radius: 4px;
        }

        .user-bubble {
            background: linear-gradient(135deg, #f4a261, #e76f51);
            color: #fff;
            border-top-right-radius: 4px;
        }

        .coach-bubble-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 8px;
            font-size: 0.72rem;
        }

        .source-badge {
            font-weight: 700;
            padding: 2px 8px;
            border-radius: 10px;
            font-size: 0.72rem;
        }

        .badge-data {
            background: rgba(34, 197, 94, 0.2);
            color: #4ade80;
            border: 1px solid rgba(34, 197, 94, 0.3);
        }

        .badge-ai {
            background: rgba(139, 92, 246, 0.2);
            color: #c084fc;
            border: 1px solid rgba(139, 92, 246, 0.3);
        }

        .coach-timestamp {
            color: var(--text-muted, #777);
        }

        .calc-source-box {
            margin-top: 10px;
            padding: 8px 12px;
            background: rgba(255, 255, 255, 0.04);
            border-left: 3px solid var(--primary, #f4a261);
            border-radius: 4px;
            font-size: 0.8rem;
            color: #cbd5e1;
            font-style: italic;
        }

        .calc-source-box .source-tag {
            font-weight: 700;
            color: var(--primary, #f4a261);
            margin-right: 4px;
        }

        .fincoach-disclaimer-box {
            margin-top: 12px;
            padding: 8px 12px;
            background: rgba(239, 68, 68, 0.08);
            border: 1px solid rgba(239, 68, 68, 0.25);
            border-radius: 6px;
            font-size: 0.75rem;
            color: #fca5a5;
            display: flex;
            align-items: center;
            gap: 6px;
        }

        .fincoach-h4 {
            margin: 10px 0 6px 0;
            font-size: 0.98rem;
            font-weight: 700;
            color: #fff;
        }

        .fincoach-h5 {
            margin: 8px 0 4px 0;
            font-size: 0.88rem;
            font-weight: 600;
            color: var(--text-muted, #aaa);
        }

        .fincoach-bullet {
            padding: 2px 0 2px 8px;
        }

        /* Input Bar */
        .fincoach-input-bar {
            display: flex;
            padding: 14px 18px;
            background: rgba(0, 0, 0, 0.2);
            border-top: 1px solid var(--border, #2a2a4a);
            gap: 10px;
        }

        .fincoach-input {
            flex: 1;
            background: var(--bg, #13132a);
            border: 1px solid var(--border, #2a2a4a);
            border-radius: 8px;
            padding: 10px 14px;
            color: var(--text, #fff);
            font-size: 0.9rem;
            outline: none;
            transition: border-color 0.15s;
        }

        .fincoach-input:focus {
            border-color: var(--primary, #f4a261);
        }

        .fincoach-send-btn {
            background: var(--primary, #f4a261);
            color: #1a1a2e;
            border: none;
            border-radius: 8px;
            padding: 0 18px;
            font-weight: 700;
            font-size: 1.1rem;
            cursor: pointer;
            transition: background 0.15s, transform 0.15s;
        }

        .fincoach-send-btn:hover {
            background: var(--primary-dk, #e07b35);
            transform: scale(1.03);
        }

        /* Floating Widget Styles */
        .fincoach-fab {
            position: fixed;
            bottom: 24px;
            right: 24px;
            background: linear-gradient(135deg, #6366f1, #8b5cf6);
            color: #fff;
            border: none;
            border-radius: 30px;
            padding: 12px 20px;
            display: flex;
            align-items: center;
            gap: 8px;
            font-weight: 700;
            box-shadow: 0 8px 24px rgba(99, 102, 241, 0.4);
            cursor: pointer;
            z-index: 1000;
            transition: transform 0.2s, box-shadow 0.2s;
        }

        .fincoach-fab:hover {
            transform: translateY(-2px);
            box-shadow: 0 12px 28px rgba(99, 102, 241, 0.6);
        }

        .fincoach-floating-panel {
            position: fixed;
            bottom: 84px;
            right: 24px;
            width: 380px;
            height: 520px;
            background: var(--surface-card, #1e1e3a);
            border: 1px solid var(--border, #2a2a4a);
            border-radius: 14px;
            display: flex;
            flex-direction: column;
            box-shadow: 0 16px 40px rgba(0, 0, 0, 0.45);
            z-index: 1001;
            overflow: hidden;
        }

        .floating-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 14px 16px;
            background: var(--bg, #13132a);
            border-bottom: 1px solid var(--border, #2a2a4a);
        }

        .floating-title {
            display: flex;
            align-items: center;
            gap: 10px;
        }

        .floating-title .icon {
            font-size: 1.3rem;
        }

        .floating-title .sub {
            font-size: 0.72rem;
            color: var(--text-muted, #888);
        }

        .floating-close-btn {
            background: none;
            border: none;
            color: var(--text-muted, #888);
            font-size: 1.4rem;
            cursor: pointer;
        }

        /* Typing Dots */
        .typing-bubble {
            display: flex;
            align-items: center;
            gap: 4px;
            padding: 12px 16px;
        }

        .typing-dot {
            width: 7px;
            height: 7px;
            background: var(--text-muted, #888);
            border-radius: 50%;
            animation: bounce 1.2s infinite ease-in-out;
        }

        .typing-dot:nth-child(2) { animation-delay: 0.2s; }
        .typing-dot:nth-child(3) { animation-delay: 0.4s; }

        @keyframes bounce {
            0%, 60%, 100% { transform: translateY(0); }
            30% { transform: translateY(-5px); }
        }
    </style>
</head>
<body class="dashboard-body">
<div class="app-container">
    <jsp:include page="/jsp/common/sidebar.jsp"/>

    <main class="main-content">
        <header class="topbar">
            <div class="topbar-left">
                <button class="menu-btn" onclick="toggleSidebar()" aria-label="Toggle navigation">☰</button>
                <div class="topbar-title">
                    <h2>FinCoach AI</h2>
                    <span class="topbar-subtitle">Personal Financial Literacy & Data-Driven Spending Coach</span>
                </div>
            </div>
            <div class="topbar-right">
                <span class="user-greeting">Welcome, <strong>${sessionScope.userName}</strong></span>
            </div>
        </header>

        <div class="page-content">
            <div class="fincoach-container">

                <div class="fincoach-header-card">
                    <div class="fincoach-title-area">
                        <h1>🤖 FinCoach AI Engine</h1>
                        <div class="fincoach-subtitle">
                            Financial Education · Live Database Calculations · SEBI Compliant Guidance
                        </div>
                    </div>
                    <div>
                        <c:choose>
                            <c:when test="${isGeminiConfigured}">
                                <span class="engine-badge gemini">🤖 Gemini AI + Live Data</span>
                            </c:when>
                            <c:otherwise>
                                <span class="engine-badge rule-based">📊 Rule-Based Mode (Your Data)</span>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <div class="fincoach-chat-card" id="fincoachInlineChat">
                    <div class="fincoach-quick-pills">
                        <button class="fincoach-pill" data-prompt="How much did I spend this month?">💸 Month Spend</button>
                        <button class="fincoach-pill" data-prompt="Can I afford to spend today? What is my daily allowance?">🎯 Daily Allowance</button>
                        <button class="fincoach-pill" data-prompt="How are my budgets performing?">🛡️ Budget Health</button>
                        <button class="fincoach-pill" data-prompt="How are my savings goals progressing?">🎯 Goals Status</button>
                        <button class="fincoach-pill" data-prompt="Explain the 50/30/20 budgeting rule">📚 50/30/20 Rule</button>
                        <button class="fincoach-pill" data-prompt="How should I build an emergency fund?">🛡️ Emergency Fund</button>
                    </div>

                    <div class="fincoach-messages" id="inlineMessages">
                        <div class="fincoach-msg-row coach-row">
                            <div class="coach-avatar-sm">🤖</div>
                            <div class="fincoach-bubble coach-bubble">
                                <div class="coach-bubble-header">
                                    <span class="source-badge badge-data">📊 Your data</span>
                                    <span class="coach-timestamp">Just now</span>
                                </div>
                                <div class="bubble-text">
                                    Hello! I'm <strong>FinCoach</strong>, your financial literacy guide. I can calculate your live spending trends, explain budgeting models like 50/30/20, and show safe daily allowances.<br><br>
                                    Every calculation explicitly cites its database source. How can I help you today?
                                </div>
                                <div class="fincoach-disclaimer-box">
                                    <span class="disclaimer-icon">⚖️</span>
                                    <span>⚠️ Disclaimer: Educational purposes only. Not SEBI registered investment advice. Consult a certified financial advisor before making investment decisions.</span>
                                </div>
                            </div>
                        </div>

                        <c:if test="${not empty lastQuestion}">
                            <div class="fincoach-msg-row user-row">
                                <div class="fincoach-bubble user-bubble">
                                    <div class="bubble-text"><c:out value="${lastQuestion}"/></div>
                                </div>
                                <div class="user-avatar-sm">👤</div>
                            </div>

                            <c:if test="${not empty lastResponse}">
                                <div class="fincoach-msg-row coach-row">
                                    <div class="coach-avatar-sm">🤖</div>
                                    <div class="fincoach-bubble coach-bubble">
                                        <div class="coach-bubble-header">
                                            <span class="source-badge ${lastResponse.source eq '🤖 AI' ? 'badge-ai' : 'badge-data'}">
                                                <c:out value="${lastResponse.source}"/>
                                            </span>
                                        </div>
                                        <div class="bubble-text" style="white-space: pre-wrap;">
                                            <c:out value="${lastResponse.answer}"/>
                                        </div>
                                    </div>
                                </div>
                            </c:if>
                        </c:if>
                    </div>

                    <div class="fincoach-input-bar">
                        <input type="text" class="fincoach-input" placeholder="Ask about your expenses, daily allowance, or financial frameworks..." autofocus />
                        <button class="fincoach-send-btn" title="Send message">➤</button>
                    </div>
                </div>

            </div>
        </div>
    </main>
</div>

<script src="${pageContext.request.contextPath}/js/fincoach.js"></script>
<script>
    function toggleSidebar() {
        const sidebar = document.getElementById('appSidebar');
        const backdrop = document.getElementById('sidebarBackdrop');
        if (sidebar) sidebar.classList.toggle('open');
        if (backdrop) backdrop.classList.toggle('open');
    }
</script>
</body>
</html>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="activeNav" value="profile" scope="request"/>
<c:set var="pageTitle" value="User Profile & SMS Sync" scope="request"/>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Profile & API Token — MyFinance</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/global.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/dashboard.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/responsive.css">
    <style>
        .token-box {
            background: rgba(15, 23, 42, 0.85);
            border: 1px solid rgba(56, 189, 248, 0.3);
            border-radius: var(--radius-sm);
            padding: 14px 18px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
            font-family: 'JetBrains Mono', 'Courier New', monospace;
            font-size: 0.95rem;
            color: #38bdf8;
            margin: 12px 0 20px 0;
            word-break: break-all;
        }
        .code-snippet {
            background: #090d16;
            border: 1px solid var(--border-color);
            border-radius: var(--radius-sm);
            padding: 14px 16px;
            font-family: 'JetBrains Mono', monospace;
            font-size: 0.82rem;
            color: #a5f3fc;
            overflow-x: auto;
            line-height: 1.5;
        }
    </style>
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

                <div class="grid-two-col">
                    <!-- Left Column: User Profile & Financial Baselines -->
                    <div class="glass-card">
                        <div style="display: flex; align-items: center; gap: 16px; margin-bottom: 24px;">
                            <div style="width: 58px; height: 58px; border-radius: 16px; background: linear-gradient(135deg, var(--primary), var(--secondary)); display: flex; align-items: center; justify-content: center; font-size: 1.6rem; font-weight: 700; color: #fff;">
                                ${not empty user.name ? user.name.substring(0,1).toUpperCase() : 'U'}
                            </div>
                            <div>
                                <h2 style="font-size: 1.3rem; margin-bottom: 4px;">${user.name}</h2>
                                <span style="font-size: 0.85rem; color: var(--text-muted);">${user.email} &bull; <strong style="color: var(--primary);">${user.role}</strong></span>
                            </div>
                        </div>

                        <h3 style="font-size: 1.05rem; margin-bottom: 16px; border-bottom: 1px solid var(--border-color); padding-bottom: 8px;">
                            Financial Baselines & Settings
                        </h3>

                        <form action="${pageContext.request.contextPath}/profile" method="post">
                            <input type="hidden" name="action" value="update-profile">

                            <div class="form-group" style="margin-bottom: 16px;">
                                <label class="form-label" for="userName">Full Name</label>
                                <input type="text" id="userName" name="name" class="form-control" value="${user.name}" required>
                            </div>

                            <div class="form-group" style="margin-bottom: 16px;">
                                <label class="form-label" for="monthlyIncome">Monthly Income (₹)</label>
                                <input type="number" step="0.01" id="monthlyIncome" name="monthlyIncome" class="form-control" 
                                       value="${user.monthlyIncome}" placeholder="e.g. 50000.00" required>
                                <small style="color: var(--text-muted); font-size: 0.78rem;">Used for smart budget recommendations & daily allowance.</small>
                            </div>

                            <div class="form-group" style="margin-bottom: 24px;">
                                <label class="form-label" for="cashInHand">Current Cash in Hand (₹)</label>
                                <input type="number" step="0.01" id="cashInHand" name="cashInHand" class="form-control" 
                                       value="${user.cashInHand}" placeholder="e.g. 3500.00" required>
                                <small style="color: var(--text-muted); font-size: 0.78rem;">Used by the Cash Tracker to calculate physical cash remaining.</small>
                            </div>

                            <button type="submit" class="btn btn-primary" style="width: 100%; justify-content: center;">
                                Save Profile Changes
                            </button>
                        </form>
                    </div>

                    <!-- Right Column: API Token & Real-Time SMS Sync Setup -->
                    <div class="glass-card">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
                            <h3 style="font-size: 1.15rem; display: flex; align-items: center; gap: 8px;">
                                <span>📱 Mobile SMS Sync API Token</span>
                            </h3>
                            <span class="badge badge-success" style="font-size: 0.75rem;">Active</span>
                        </div>

                        <p style="font-size: 0.85rem; color: var(--text-muted); line-height: 1.5;">
                            Use your unique token to push Indian bank debit SMS notifications directly to MyFinance in real-time.
                        </p>

                        <div class="token-box">
                            <span id="apiTokenText">${user.apiToken}</span>
                            <div style="display: flex; gap: 8px; flex-shrink: 0;">
                                <button type="button" class="btn btn-secondary btn-sm" onclick="copyToken()" title="Copy API Token">
                                    📋 Copy
                                </button>
                                <form action="${pageContext.request.contextPath}/profile" method="post" style="margin: 0;" onsubmit="return confirm('Regenerating will invalidate your old token. Continue?');">
                                    <input type="hidden" name="action" value="regenerate-token">
                                    <button type="submit" class="btn btn-outline btn-sm" title="Regenerate Token">
                                        🔄 Regenerate
                                    </button>
                                </form>
                            </div>
                        </div>

                        <h4 style="font-size: 0.95rem; margin-bottom: 8px; color: var(--text-main);">
                            Webhook Integration Endpoint
                        </h4>
                        <div class="code-snippet" style="margin-bottom: 16px;">
POST http://&lt;your-server-ip&gt;:8080/myfinance/api/sms-sync<br>
Header: X-API-Token: <span style="color: #38bdf8;">${user.apiToken}</span><br>
Content-Type: application/json<br><br>
{"sms": "Your A/C debited by INR 450.00 to Swiggy. UPI Ref: 123456789012"}
                        </div>

                        <h4 style="font-size: 0.95rem; margin-bottom: 8px; color: var(--text-main);">
                            Sample cURL Test Command
                        </h4>
                        <div class="code-snippet" style="margin-bottom: 16px; user-select: all;">
curl -X POST "http://localhost:8080/myfinance/api/sms-sync" \<br>
&nbsp;&nbsp;-H "X-API-Token: ${user.apiToken}" \<br>
&nbsp;&nbsp;-H "Content-Type: application/json" \<br>
&nbsp;&nbsp;-d '{"sms": "Alert: Rs 780.00 debited from HDFC Bank A/C ending 5678 towards UBER. UPI: 112233445566."}'
                        </div>

                        <div style="font-size: 0.82rem; color: var(--text-muted); background: rgba(255,255,255,0.03); border-radius: var(--radius-sm); padding: 12px;">
                            💡 <strong>Tip for Phone Sync:</strong> Install any Android SMS Webhook app (e.g. <em>SMS Forwarder</em> or <em>Automate</em>). Configure it to send an HTTP POST whenever an SMS contains "debited" or "INR" to your computer's IP address on your local Wi-Fi.
                        </div>
                    </div>
                </div>

                <!-- Your Data and Privacy Card -->
                <div class="glass-card" style="margin-top: 24px;">
                    <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 14px;">
                        <span style="font-size: 1.3rem;">🔒</span>
                        <h3 style="font-size: 1.15rem; margin: 0;">Your Data and Privacy</h3>
                    </div>
                    <p style="font-size: 0.88rem; color: var(--text-muted); line-height: 1.6; margin-bottom: 16px;">
                        We believe in complete privacy and transparency. Here is how your personal financial information is handled:
                    </p>
                    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 16px;">
                        <div style="background: rgba(255,255,255,0.02); border: 1px solid var(--border-color); border-radius: var(--radius-sm); padding: 14px;">
                            <div style="font-weight: 600; color: var(--text-main); margin-bottom: 6px; font-size: 0.92rem;">🔑 Password Security</div>
                            <div style="font-size: 0.84rem; color: var(--text-muted); line-height: 1.5;">Passwords are stored as BCrypt hashes with salted one-way encryption, so your plain text password is never saved.</div>
                        </div>
                        <div style="background: rgba(255,255,255,0.02); border: 1px solid var(--border-color); border-radius: var(--radius-sm); padding: 14px;">
                            <div style="font-weight: 600; color: var(--text-main); margin-bottom: 6px; font-size: 0.92rem;">👤 Isolated Data Access</div>
                            <div style="font-size: 0.84rem; color: var(--text-muted); line-height: 1.5;">You only see your own data. Every expense, budget, goal, and cash record is strictly verified against your user account.</div>
                        </div>
                        <div style="background: rgba(255,255,255,0.02); border: 1px solid var(--border-color); border-radius: var(--radius-sm); padding: 14px;">
                            <div style="font-weight: 600; color: var(--text-main); margin-bottom: 6px; font-size: 0.92rem;">⏱️ Session Protection</div>
                            <div style="font-size: 0.84rem; color: var(--text-muted); line-height: 1.5;">Sessions automatically expire after 30 minutes of inactivity to keep your dashboard secure if you step away.</div>
                        </div>
                        <div style="background: rgba(255,255,255,0.02); border: 1px solid var(--border-color); border-radius: var(--radius-sm); padding: 14px;">
                            <div style="font-weight: 600; color: var(--text-main); margin-bottom: 6px; font-size: 0.92rem;">📱 Phone & SMS Privacy</div>
                            <div style="font-size: 0.84rem; color: var(--text-muted); line-height: 1.5;">We never read your phone. Only SMS forwarded by your own phone app are received, and nothing becomes an expense until you confirm it. The full message text is stored so it can be read.</div>
                        </div>
                        <div style="background: rgba(255,255,255,0.02); border: 1px solid var(--border-color); border-radius: var(--radius-sm); padding: 14px;">
                            <div style="font-weight: 600; color: var(--text-main); margin-bottom: 6px; font-size: 0.92rem;">🛡️ Personal API Token</div>
                            <div style="font-size: 0.84rem; color: var(--text-muted); line-height: 1.5;">Your SMS token works only for your account and can be regenerated at any time, instantly revoking older tokens.</div>
                        </div>
                        <div style="background: rgba(255,255,255,0.02); border: 1px solid var(--border-color); border-radius: var(--radius-sm); padding: 14px;">
                            <div style="font-weight: 600; color: var(--text-main); margin-bottom: 6px; font-size: 0.92rem;">🤖 AI Privacy Boundary</div>
                            <div style="font-size: 0.84rem; color: var(--text-muted); line-height: 1.5;">Your amounts and balances are never sent to the AI. FinCoach answers personal queries from your database, and queries sent to Gemini are conceptual and sanitized.</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        function copyToken() {
            const tokenText = document.getElementById('apiTokenText').innerText.trim();
            navigator.clipboard.writeText(tokenText).then(() => {
                alert('API Token copied to clipboard!');
            }).catch(err => {
                prompt('Copy your API Token:', tokenText);
            });
        }
    </script>
</body>
</html>

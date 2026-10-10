/**
 * FinCoach AI Financial Education & Personal Data Chat Widget
 * Supports inline chat panel and floating widget with transparent calculation sources.
 */

(function () {
    const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1)) || '';
    const endpoint = `${contextPath}/fincoach`;

    document.addEventListener('DOMContentLoaded', () => {
        initFinCoach();
    });

    function initFinCoach() {
        const inlineContainer = document.getElementById('fincoachInlineChat');
        if (inlineContainer) {
            setupChat(inlineContainer, false);
        } else {
            // Mount floating widget on user pages if not already on /fincoach
            mountFloatingWidget();
        }
    }

    function setupChat(container, isFloating) {
        const messageList = container.querySelector('.fincoach-messages');
        const inputField  = container.querySelector('.fincoach-input');
        const sendBtn     = container.querySelector('.fincoach-send-btn');
        const pills       = container.querySelectorAll('.fincoach-pill');

        if (!messageList || !inputField || !sendBtn) return;

        // Quick prompt clicks
        pills.forEach(pill => {
            pill.addEventListener('click', () => {
                const text = pill.getAttribute('data-prompt') || pill.textContent.trim();
                inputField.value = text;
                sendMessage(text, messageList, inputField, sendBtn);
            });
        });

        // Send button click
        sendBtn.addEventListener('click', () => {
            const text = inputField.value.trim();
            if (text) {
                sendMessage(text, messageList, inputField, sendBtn);
            }
        });

        // Enter key press
        inputField.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                const text = inputField.value.trim();
                if (text) {
                    sendMessage(text, messageList, inputField, sendBtn);
                }
            }
        });
    }

    function sendMessage(text, messageList, inputField, sendBtn) {
        // Append user bubble
        appendUserMessage(text, messageList);
        inputField.value = '';
        inputField.disabled = true;
        sendBtn.disabled = true;

        // Show typing indicator
        const typingId = 'typing-' + Date.now();
        appendTypingIndicator(typingId, messageList);
        scrollToBottom(messageList);

        // Send AJAX request
        const params = new URLSearchParams();
        params.append('question', text);

        fetch(endpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Requested-With': 'XMLHttpRequest',
                'Accept': 'application/json'
            },
            body: params.toString()
        })
        .then(res => {
            if (!res.ok) throw new Error('HTTP ' + res.status);
            return res.json();
        })
        .then(data => {
            removeTypingIndicator(typingId);
            appendCoachMessage(data, messageList);
            scrollToBottom(messageList);
        })
        .catch(err => {
            removeTypingIndicator(typingId);
            appendCoachMessage({
                answer: 'Could not connect to FinCoach right now. Please check your connection.',
                source: '📊 Your data',
                mode: 'rule-based',
                disclaimer: '⚠️ Disclaimer: Educational purposes only. Not SEBI registered investment advice.'
            }, messageList);
            scrollToBottom(messageList);
        })
        .finally(() => {
            inputField.disabled = false;
            sendBtn.disabled = false;
            inputField.focus();
        });
    }

    function appendUserMessage(text, list) {
        const row = document.createElement('div');
        row.className = 'fincoach-msg-row user-row';
        row.innerHTML = `
            <div class="fincoach-bubble user-bubble">
                <div class="bubble-text">${escapeHtml(text)}</div>
            </div>
            <div class="user-avatar-sm">👤</div>
        `;
        list.appendChild(row);
    }

    function appendCoachMessage(data, list) {
        const row = document.createElement('div');
        row.className = 'fincoach-msg-row coach-row';

        const sourceLabel = data.source || '📊 Your data';
        const sourceBadgeClass = sourceLabel.includes('AI') ? 'badge-ai' : 'badge-data';
        const parsedContent = formatMarkdown(data.answer || '');

        row.innerHTML = `
            <div class="coach-avatar-sm">🤖</div>
            <div class="fincoach-bubble coach-bubble">
                <div class="coach-bubble-header">
                    <span class="source-badge ${sourceBadgeClass}">${escapeHtml(sourceLabel)}</span>
                    <span class="coach-timestamp">${new Date().toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</span>
                </div>
                <div class="bubble-text">${parsedContent}</div>
                ${renderDisclaimer(data.disclaimer)}
            </div>
        `;
        list.appendChild(row);
    }

    function renderDisclaimer(disclaimer) {
        if (!disclaimer) return '';
        return `
            <div class="fincoach-disclaimer-box">
                <span class="disclaimer-icon">⚖️</span>
                <span>${escapeHtml(disclaimer)}</span>
            </div>
        `;
    }

    function appendTypingIndicator(id, list) {
        const row = document.createElement('div');
        row.className = 'fincoach-msg-row coach-row';
        row.id = id;
        row.innerHTML = `
            <div class="coach-avatar-sm">🤖</div>
            <div class="fincoach-bubble coach-bubble typing-bubble">
                <span class="typing-dot"></span>
                <span class="typing-dot"></span>
                <span class="typing-dot"></span>
            </div>
        `;
        list.appendChild(row);
    }

    function removeTypingIndicator(id) {
        const el = document.getElementById(id);
        if (el) el.remove();
    }

    function scrollToBottom(container) {
        container.scrollTop = container.scrollHeight;
    }

    function escapeHtml(str) {
        if (!str) return '';
        return str.replace(/&/g, '&amp;')
                  .replace(/</g, '&lt;')
                  .replace(/>/g, '&gt;')
                  .replace(/"/g, '&quot;');
    }

    function formatMarkdown(text) {
        if (!text) return '';
        let html = escapeHtml(text);

        // Headers
        html = html.replace(/^### (.*$)/gim, '<h4 class="fincoach-h4">$1</h4>');
        html = html.replace(/^#### (.*$)/gim, '<h5 class="fincoach-h5">$1</h5>');

        // Bold
        html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');

        // Blockquote / Calculation Source
        html = html.replace(/^&gt; \*(.*?)\*/gim, '<div class="calc-source-box"><span class="source-tag">🔍 Calculation:</span> $1</div>');
        html = html.replace(/^&gt; (.*$)/gim, '<div class="calc-source-box">$1</div>');

        // Bullet lists
        html = html.replace(/^\- (.*$)/gim, '<div class="fincoach-bullet">• $1</div>');

        // Line breaks
        html = html.replace(/\n\n/g, '<br><br>');
        return html;
    }

    function mountFloatingWidget() {
        if (document.getElementById('fincoachFloatingRoot')) return;

        const root = document.createElement('div');
        root.id = 'fincoachFloatingRoot';
        root.innerHTML = `
            <button class="fincoach-fab" id="fincoachFab" title="Ask FinCoach AI">
                <span class="fab-icon">🤖</span>
                <span class="fab-label">FinCoach</span>
            </button>

            <div class="fincoach-floating-panel" id="fincoachFloatingPanel" style="display:none;">
                <div class="floating-header">
                    <div class="floating-title">
                        <span class="icon">🤖</span>
                        <div>
                            <strong>FinCoach</strong>
                            <div class="sub">Personal Financial Education</div>
                        </div>
                    </div>
                    <button class="floating-close-btn" id="fincoachCloseBtn">&times;</button>
                </div>

                <div class="fincoach-quick-pills">
                    <button class="fincoach-pill" data-prompt="How much did I spend this month?">💸 Month Spend</button>
                    <button class="fincoach-pill" data-prompt="What is my safe daily allowance?">🎯 Left Today</button>
                    <button class="fincoach-pill" data-prompt="Explain the 50/30/20 budgeting rule">📚 50/30/20 Rule</button>
                </div>

                <div class="fincoach-messages" id="floatingMessages">
                    <div class="fincoach-msg-row coach-row">
                        <div class="coach-avatar-sm">🤖</div>
                        <div class="fincoach-bubble coach-bubble">
                            <div class="coach-bubble-header">
                                <span class="source-badge badge-data">📊 Your data</span>
                            </div>
                            <div class="bubble-text">
                                Hello! I'm <strong>FinCoach</strong>, your financial literacy and spending coach. Ask me about your real-time expenses, budget status, or educational frameworks like 50/30/20.
                            </div>
                            <div class="fincoach-disclaimer-box">
                                <span class="disclaimer-icon">⚖️</span>
                                <span>Educational purposes only. Not SEBI registered investment advice.</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="fincoach-input-bar">
                    <input type="text" class="fincoach-input" placeholder="Ask about expenses, budget, or 50/30/20..." />
                    <button class="fincoach-send-btn" title="Send">➤</button>
                </div>
            </div>
        `;
        document.body.appendChild(root);

        const fab = document.getElementById('fincoachFab');
        const panel = document.getElementById('fincoachFloatingPanel');
        const closeBtn = document.getElementById('fincoachCloseBtn');

        fab.addEventListener('click', () => {
            const isOpen = panel.style.display !== 'none';
            panel.style.display = isOpen ? 'none' : 'flex';
            if (!isOpen) {
                panel.querySelector('.fincoach-input').focus();
            }
        });

        closeBtn.addEventListener('click', () => {
            panel.style.display = 'none';
        });

        setupChat(panel, true);
    }
})();

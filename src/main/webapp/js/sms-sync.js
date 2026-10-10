/**
 * MyFinance 2.0 — Real-Time SMS Sync Client Engine
 * Handles background polling (every 5s), Live Sync Active indicators,
 * Quick Confirm modal, Pending SMS cards, and Paste SMS backup box.
 */

(function () {
    const POLL_INTERVAL = 5000; // 5 seconds
    const contextPath = window.location.pathname.substring(0, window.location.pathname.indexOf('/', 1)) || '';
    const apiEndpoint = `${contextPath}/api/sms-sync`;

    let knownPendingIds = new Set();
    let isPolling = false;

    // Initialize on DOM ready
    document.addEventListener('DOMContentLoaded', () => {
        initLiveSync();
        setupPasteBox();
        setupModal();
        startPolling();
    });

    function initLiveSync() {
        checkSyncStatus();
    }

    function startPolling() {
        if (isPolling) return;
        isPolling = true;
        setInterval(() => {
            checkSyncStatus();
        }, POLL_INTERVAL);
    }

    function checkSyncStatus() {
        fetch(apiEndpoint, {
            headers: {
                'Accept': 'application/json'
            }
        })
        .then(res => {
            if (!res.ok) return null;
            return res.json();
        })
        .then(data => {
            if (!data || !data.success) return;

            updateIndicator(data.liveSyncActive, data.lastSyncedText);
            renderPendingItems(data.pendingItems || []);
        })
        .catch(err => {
            // Server or network temporarily unavailable
        });
    }

    function updateIndicator(isActive, text) {
        const dot = document.getElementById('liveSyncDot');
        const label = document.getElementById('liveSyncText');

        if (dot) {
            if (isActive) {
                dot.style.background = '#10b981';
                dot.style.boxShadow = '0 0 10px #10b981';
            } else {
                dot.style.background = '#64748b';
                dot.style.boxShadow = 'none';
            }
        }

        if (label) {
            if (isActive) {
                label.textContent = text ? `Last synced: ${text}` : 'Live Sync Active';
                label.style.color = '#34d399';
            } else {
                label.textContent = text || 'No SMS synced yet';
                label.style.color = 'var(--text-muted)';
            }
        }

        // Also update expenses page banner if present
        const pageDot = document.getElementById('pageLiveSyncDot');
        const pageText = document.getElementById('pageLiveSyncText');
        if (pageDot) {
            pageDot.style.background = isActive ? '#10b981' : '#64748b';
            pageDot.style.boxShadow = isActive ? '0 0 10px #10b981' : 'none';
        }
        if (pageText) {
            pageText.textContent = isActive ? (text ? `Last synced: ${text}` : 'Live Sync Active') : (text || 'No SMS synced yet');
        }
    }

    function renderPendingItems(items) {
        const container = document.getElementById('pendingSmsContainer');
        const countBadge = document.getElementById('pendingSmsCount');

        if (countBadge) {
            countBadge.textContent = items.length;
            countBadge.style.display = items.length > 0 ? 'inline-block' : 'none';
        }

        if (!container) return;

        if (items.length === 0) {
            container.innerHTML = `
                <div style="padding: 24px; text-align: center; color: var(--text-muted); font-size: 0.88rem; background: rgba(255,255,255,0.01); border-radius: var(--radius-sm); border: 1px dashed rgba(255,255,255,0.08);">
                    ✨ All synced SMS transactions confirmed! No pending items.
                </div>
            `;
            return;
        }

        // Check if there is a brand new item to trigger quick confirm
        items.forEach(item => {
            if (!knownPendingIds.has(item.id)) {
                knownPendingIds.add(item.id);
            }
        });

        let html = '';
        items.forEach(item => {
            html += `
                <div class="sms-pending-card" id="sms-card-${item.id}" style="background: rgba(15, 23, 42, 0.7); border: 1px solid rgba(56, 189, 248, 0.25); border-radius: var(--radius-sm); padding: 14px 18px; margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; transition: all 0.2s ease;">
                    <div style="flex: 1; min-width: 200px;">
                        <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 4px;">
                            <span style="font-weight: 700; font-size: 1.05rem; color: #fff;">${escapeHtml(item.merchant || 'Unknown Merchant')}</span>
                            <span class="badge" style="background: rgba(56, 189, 248, 0.15); color: #38bdf8; font-size: 0.72rem;">Pending Confirmation</span>
                        </div>
                        <div style="font-size: 0.8rem; color: var(--text-muted);">
                            <span>UPI Ref: <code>${escapeHtml(item.upiRef || 'N/A')}</code></span>
                            ${item.timeDisplay ? `<span style="margin-left: 10px; color: var(--text-dim); font-size: 0.75rem;">🕒 ${escapeHtml(item.timeDisplay)}</span>` : ''}
                            ${item.rawSms ? `<br><small style="color: var(--text-dim); display: block; margin-top: 4px; max-width: 480px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;" title="${escapeHtml(item.rawSms)}">"${escapeHtml(item.rawSms)}"</small>` : ''}
                        </div>
                    </div>

                    <div style="display: flex; align-items: center; gap: 14px;">
                        <div style="text-align: right;">
                            <div style="font-family: 'Space Grotesk', sans-serif; font-size: 1.25rem; font-weight: 700; color: #f43f5e;">
                                ₹${Number(item.amount).toFixed(2)}
                            </div>
                            <span style="font-size: 0.72rem; color: var(--text-dim);">SMS Debit</span>
                        </div>

                        <div style="display: flex; gap: 6px;">
                            <button class="btn btn-primary btn-sm" onclick="window.smsSync.openConfirmModal(${item.id}, '${escapeHtml(item.merchant)}', ${item.amount}, '${escapeHtml(item.upiRef)}')">
                                ✅ Confirm?
                            </button>
                            <button class="btn btn-outline btn-sm" onclick="window.smsSync.rejectSms(${item.id})">
                                ✕ Dismiss
                            </button>
                        </div>
                    </div>
                </div>
            `;
        });

        container.innerHTML = html;
    }

    function setupPasteBox() {
        const btn = document.getElementById('pasteSmsSubmitBtn');
        const textarea = document.getElementById('pasteSmsInput');

        if (!btn || !textarea) return;

        btn.addEventListener('click', () => {
            const text = textarea.value.trim();
            if (!text) {
                alert('Please enter or paste debit SMS text.');
                return;
            }

            btn.disabled = true;
            btn.textContent = 'Parsing & Ingesting...';

            fetch(apiEndpoint, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify({ sms: text })
            })
            .then(res => res.json())
            .then(data => {
                btn.disabled = false;
                btn.textContent = '⚡ Parse & Queue SMS';
                if (data.success) {
                    textarea.value = '';
                    showToast('SMS parsed and queued for confirmation!');
                    checkSyncStatus();
                } else {
                    alert('Failed to parse SMS: ' + (data.message || 'Unknown error'));
                }
            })
            .catch(err => {
                btn.disabled = false;
                btn.textContent = '⚡ Parse & Queue SMS';
                alert('Network error while submitting SMS: ' + err.message);
            });
        });
    }

    // Modal creation for Quick Confirm
    function setupModal() {
        if (document.getElementById('smsConfirmModal')) return;

        const modal = document.createElement('div');
        modal.id = 'smsConfirmModal';
        modal.style.cssText = `
            display: none;
            position: fixed;
            top: 0; left: 0; right: 0; bottom: 0;
            background: rgba(0, 0, 0, 0.7);
            backdrop-filter: blur(6px);
            z-index: 1000;
            align-items: center;
            justify-content: center;
            padding: 16px;
        `;

        modal.innerHTML = `
            <div style="background: #0f172a; border: 1px solid rgba(56, 189, 248, 0.3); border-radius: var(--radius-md, 16px); padding: 24px; max-width: 440px; width: 100%; box-shadow: 0 20px 40px rgba(0,0,0,0.8);">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
                    <h3 style="font-size: 1.15rem; margin: 0; display: flex; align-items: center; gap: 8px;">
                        <span>📱 Confirm SMS Expense</span>
                    </h3>
                    <button type="button" style="background: none; border: none; color: var(--text-muted); font-size: 1.2rem; cursor: pointer;" onclick="window.smsSync.closeModal()">&times;</button>
                </div>

                <div style="background: rgba(255,255,255,0.03); border-radius: var(--radius-sm, 8px); padding: 14px; margin-bottom: 16px;">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                        <span style="font-size: 0.85rem; color: var(--text-muted);">Detected Merchant</span>
                        <strong id="modalMerchant" style="font-size: 1.05rem; color: #fff;">-</strong>
                    </div>
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px;">
                        <span style="font-size: 0.85rem; color: var(--text-muted);">Debit Amount</span>
                        <strong id="modalAmount" style="font-family: 'Space Grotesk', sans-serif; font-size: 1.35rem; color: #f43f5e;">₹0.00</strong>
                    </div>
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <span style="font-size: 0.85rem; color: var(--text-muted);">UPI Ref</span>
                        <code id="modalUpi" style="font-size: 0.85rem; color: #38bdf8;">-</code>
                    </div>
                </div>

                <div class="form-group" style="margin-bottom: 20px;">
                    <label class="form-label" for="modalCategory">Category</label>
                    <select id="modalCategory" class="form-control">
                        <option value="Food & Dining">Food & Dining</option>
                        <option value="Transport & Fuel">Transport & Fuel</option>
                        <option value="Shopping">Shopping</option>
                        <option value="Bills & Utilities">Bills & Utilities</option>
                        <option value="Rent & Housing">Rent & Housing</option>
                        <option value="Entertainment">Entertainment</option>
                        <option value="Health & Medical">Health & Medical</option>
                        <option value="Education">Education</option>
                        <option value="Investment & Savings">Investment & Savings</option>
                        <option value="Other">Other</option>
                    </select>
                </div>

                <input type="hidden" id="modalSmsId" value="">

                <div style="display: flex; gap: 10px;">
                    <button type="button" id="modalConfirmBtn" class="btn btn-primary" style="flex: 1; justify-content: center;" onclick="window.smsSync.confirmSms()">
                        ✅ Confirm & Add Expense
                    </button>
                    <button type="button" class="btn btn-secondary" onclick="window.smsSync.closeModal()">
                        Cancel
                    </button>
                </div>
            </div>
        `;

        document.body.appendChild(modal);
    }

    // Exposed global API for onclick triggers
    window.smsSync = {
        openConfirmModal(id, merchant, amount, upiRef) {
            const modal = document.getElementById('smsConfirmModal');
            if (!modal) return;

            document.getElementById('modalSmsId').value = id;
            document.getElementById('modalMerchant').textContent = merchant || 'Unknown Merchant';
            document.getElementById('modalAmount').textContent = '₹' + Number(amount).toFixed(2);
            document.getElementById('modalUpi').textContent = upiRef || 'N/A';

            // Auto-select category if merchant matches keyword (Innovation 5 / Consistent Category Matching)
            const catSelect = document.getElementById('modalCategory');
            const m = (merchant || '').toLowerCase();
            if (m.includes('swiggy') || m.includes('zomato') || m.includes('chai') || m.includes('starbucks') ||
                m.includes('mcdonald') || m.includes('kfc') || m.includes('burger') || m.includes('pizza') ||
                m.includes('dining') || m.includes('restaurant') || m.includes('blinkit') || m.includes('zepto') ||
                m.includes('instamart') || m.includes('bigbasket') || m.includes('food')) {
                catSelect.value = 'Food & Dining';
            } else if (m.includes('uber') || m.includes('ola') || m.includes('rapido') || m.includes('metro') ||
                       m.includes('fuel') || m.includes('petrol') || m.includes('diesel') || m.includes('transport')) {
                catSelect.value = 'Transport & Fuel';
            } else if (m.includes('amazon') || m.includes('flipkart') || m.includes('myntra') || m.includes('ajio') || m.includes('shopping')) {
                catSelect.value = 'Shopping';
            } else if (m.includes('electricity') || m.includes('water') || m.includes('airtel') || m.includes('jio') ||
                       m.includes('bill') || m.includes('recharge') || m.includes('broadband') || m.includes('utilities')) {
                catSelect.value = 'Bills & Utilities';
            } else if (m.includes('rent') || m.includes('maintenance') || m.includes('society') || m.includes('housing')) {
                catSelect.value = 'Rent & Housing';
            } else if (m.includes('netflix') || m.includes('spotify') || m.includes('hotstar') || m.includes('bookmyshow') ||
                       m.includes('cinema') || m.includes('pvr') || m.includes('movie')) {
                catSelect.value = 'Entertainment';
            } else if (m.includes('apollo') || m.includes('medplus') || m.includes('pharmacy') || m.includes('1mg') ||
                       m.includes('hospital') || m.includes('clinic') || m.includes('medical') || m.includes('health')) {
                catSelect.value = 'Health & Medical';
            } else if (m.includes('coursera') || m.includes('udemy') || m.includes('tuition') || m.includes('books') || m.includes('education')) {
                catSelect.value = 'Education';
            } else if (m.includes('zerodha') || m.includes('groww') || m.includes('mutual') || m.includes('sip') ||
                       m.includes('stocks') || m.includes('investment')) {
                catSelect.value = 'Investment & Savings';
            } else {
                catSelect.value = 'Other';
            }

            modal.style.display = 'flex';
        },

        closeModal() {
            const modal = document.getElementById('smsConfirmModal');
            if (modal) modal.style.display = 'none';
        },

        confirmSms(force) {
            const id = document.getElementById('modalSmsId').value;
            const category = document.getElementById('modalCategory').value;
            const btn = document.getElementById('modalConfirmBtn');

            if (!id) return;
            if (btn) {
                btn.disabled = true;
                btn.textContent = force ? 'Logging Expense Anyway...' : 'Saving Expense...';
            }

            let url = `${apiEndpoint}?action=confirm&id=${encodeURIComponent(id)}&category=${encodeURIComponent(category)}`;
            if (force) {
                url += '&force=true';
            }

            fetch(url, {
                method: 'POST',
                headers: {
                    'Accept': 'application/json'
                }
            })
            .then(res => res.json())
            .then(data => {
                if (btn) {
                    btn.disabled = false;
                    btn.textContent = '✅ Confirm & Add Expense';
                }

                // Spend Guard Check: Intercept if over limit or near limit!
                if (data.needsConfirmation && data.alert) {
                    window.smsSync.closeModal();
                    showSpendGuardModal(id, category, data.alert);
                    return;
                }

                window.smsSync.closeModal();

                if (data.success) {
                    let msg = 'Expense confirmed and recorded via SMS_SYNC!';
                    if (data.warning) {
                        msg += '\n\n⚠️ ' + data.warning;
                    }
                    alert(msg);
                    window.location.reload();
                } else {
                    alert('Error confirming SMS: ' + data.message);
                }
            })
            .catch(err => {
                if (btn) {
                    btn.disabled = false;
                    btn.textContent = '✅ Confirm & Add Expense';
                }
                alert('Request failed: ' + err.message);
            });
        },

        rejectSms(id) {
            if (!confirm('Dismiss this SMS transaction?')) return;

            fetch(`${apiEndpoint}?action=reject&id=${encodeURIComponent(id)}`, {
                method: 'POST',
                headers: {
                    'Accept': 'application/json'
                }
            })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    const card = document.getElementById(`sms-card-${id}`);
                    if (card) card.remove();
                    checkSyncStatus();
                } else {
                    alert('Error: ' + data.message);
                }
            })
            .catch(err => {
                alert('Request failed: ' + err.message);
            });
        }
    };

    // Spend Guard Pre-Insert Warning Modal for SMS Confirm (Innovation 1 / Golden Rule 6)
    function showSpendGuardModal(id, category, alert) {
        let guardModal = document.getElementById('smsSpendGuardModal');
        if (!guardModal) {
            guardModal = document.createElement('div');
            guardModal.id = 'smsSpendGuardModal';
            guardModal.style.position = 'fixed';
            guardModal.style.inset = '0';
            guardModal.style.background = 'rgba(0, 0, 0, 0.78)';
            guardModal.style.backdropFilter = 'blur(8px)';
            guardModal.style.display = 'flex';
            guardModal.style.alignItems = 'center';
            guardModal.style.justifyContent = 'center';
            guardModal.style.zIndex = '10001';
            guardModal.style.padding = '20px';
            document.body.appendChild(guardModal);
        }

        const isOver = alert.isOverLimit || alert.level === 'over limit' || alert.level === 'over_limit';
        const accentColor = isOver ? '#ef4444' : '#f59e0b';
        const accentText = isOver ? '#f87171' : '#fbbf24';
        const badgeBg = isOver ? 'rgba(239, 68, 68, 0.2)' : 'rgba(245, 158, 11, 0.2)';
        const badgeColor = isOver ? '#fca5a5' : '#fcd34d';
        const titleText = isOver ? "Oops, you've gone over 🙈" : "Spend Guard Warning";
        const levelLabel = alert.level || (isOver ? 'over limit' : 'near limit');
        const descText = isOver
            ? `Adding this expense will exceed your <strong>${escapeHtml(alert.category || category)}</strong> budget!`
            : `Heads up! Adding this expense reaches <strong>${Math.round(alert.prospectivePercent || 80)}%</strong> of your <strong>${escapeHtml(alert.category || category)}</strong> budget.`;

        guardModal.innerHTML = `
            <div class="glass-card" style="width: 100%; max-width: 480px; border: 2px solid ${accentColor}; box-shadow: 0 0 35px ${isOver ? 'rgba(239, 68, 68, 0.45)' : 'rgba(245, 158, 11, 0.45)'}; padding: 28px; border-radius: var(--radius-md);">
                <div style="display: flex; align-items: center; gap: 14px; margin-bottom: 16px;">
                    <div style="font-size: 2.2rem;">${isOver ? '🚨' : '⚠️'}</div>
                    <div>
                        <h3 style="font-size: 1.25rem; font-weight: 700; color: ${accentText}; margin: 0 0 4px 0;">${titleText}</h3>
                        <span class="badge" style="background: ${badgeBg}; color: ${badgeColor}; font-size: 0.72rem; text-transform: uppercase;">
                            ${escapeHtml(levelLabel)}
                        </span>
                    </div>
                </div>

                <p style="font-size: 0.95rem; color: var(--text-color); margin-bottom: 16px; line-height: 1.5;">
                    ${descText}
                </p>

                <div style="background: rgba(15, 23, 42, 0.85); border: 1px dashed ${isOver ? 'rgba(239, 68, 68, 0.5)' : 'rgba(245, 158, 11, 0.5)'}; border-radius: 8px; padding: 14px; margin-bottom: 22px; text-align: center;">
                    <div style="font-size: 0.72rem; color: var(--text-muted); margin-bottom: 4px; text-transform: uppercase; letter-spacing: 0.5px;">Formula</div>
                    <div style="font-family: 'Space Grotesk', monospace; font-size: 1.05rem; font-weight: 700; color: ${accentText};">
                        ${escapeHtml(alert.formula || '')}
                    </div>
                </div>

                <div style="display: flex; gap: 12px; justify-content: flex-end;">
                    <button type="button" class="btn btn-secondary" onclick="document.getElementById('smsSpendGuardModal').style.display='none';">
                        Cancel
                    </button>
                    <button type="button" class="btn btn-primary" id="smsLogAnywayBtn" style="background: ${isOver ? 'linear-gradient(135deg, #ef4444, #dc2626)' : 'linear-gradient(135deg, #f59e0b, #d97706)'}; border: none; padding: 10px 20px;">
                        Log Anyway
                    </button>
                </div>
            </div>
        `;

        guardModal.style.display = 'flex';

        document.getElementById('smsLogAnywayBtn').onclick = function () {
            guardModal.style.display = 'none';
            // Set fields and trigger confirm with force=true
            document.getElementById('modalSmsId').value = id;
            document.getElementById('modalCategory').value = category;
            window.smsSync.confirmSms(true);
        };
    }

    function showToast(msg) {
        alert(msg);
    }

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }
})();

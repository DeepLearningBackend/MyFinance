/**
 * MyFinance 2.0 — Offline Expense Entry & Background Sync (Innovation 6)
 * Stores pending expenses in localStorage when internet is disconnected and auto-syncs when online.
 */

const OFFLINE_QUEUE_KEY = 'myfinance_offline_queue';

function getOfflineQueue() {
    try {
        const data = localStorage.getItem(OFFLINE_QUEUE_KEY);
        return data ? JSON.parse(data) : [];
    } catch (e) {
        return [];
    }
}

function saveOfflineQueue(queue) {
    localStorage.setItem(OFFLINE_QUEUE_KEY, JSON.stringify(queue));
}

function showToast(message, type = 'info') {
    let container = document.getElementById('offlineSyncToast');
    if (!container) {
        container = document.createElement('div');
        container.id = 'offlineSyncToast';
        container.className = 'offline-sync-indicator';
        document.body.appendChild(container);
    }

    const icon = type === 'offline' ? '📡' : (type === 'success' ? '✨' : '⚡');
    container.innerHTML = `<span>${icon}</span><span>${message}</span>`;
    container.style.display = 'flex';

    setTimeout(() => {
        container.style.display = 'none';
    }, 4500);
}

// Sync queued transactions when online connection is restored
async function syncOfflineQueue() {
    const queue = getOfflineQueue();
    if (!queue || queue.length === 0) return;

    showToast(`Syncing ${queue.length} offline expense(s)...`, 'sync');

    let syncedCount = 0;
    const remaining = [];

    for (const item of queue) {
        try {
            const formData = new URLSearchParams();
            formData.append('action', 'add');
            formData.append('title', item.title);
            formData.append('amount', item.amount);
            formData.append('category', item.category);
            formData.append('expenseDate', item.expenseDate);
            formData.append('description', item.description || '');

            const response = await fetch(item.endpoint || window.location.pathname, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: formData.toString()
            });

            if (response.ok) {
                syncedCount++;
            } else {
                remaining.push(item);
            }
        } catch (e) {
            remaining.push(item);
        }
    }

    saveOfflineQueue(remaining);

    if (syncedCount > 0) {
        showToast(`Successfully synced ${syncedCount} expense(s)! Refreshing...`, 'success');
        setTimeout(() => {
            window.location.reload();
        }, 1500);
    }
}

// Initialize listeners
document.addEventListener('DOMContentLoaded', () => {
    // Monitor connection events
    window.addEventListener('online', () => {
        showToast('Internet connection restored. Synchronizing data...', 'success');
        syncOfflineQueue();
    });

    window.addEventListener('offline', () => {
        showToast('You are offline. Transactions will be saved locally and synced later.', 'offline');
    });

    // Check for pending queue on page load
    if (navigator.onLine) {
        syncOfflineQueue();
    }

    // Intercept expense forms if offline
    const expenseForm = document.getElementById('expenseForm');
    if (expenseForm) {
        expenseForm.addEventListener('submit', (e) => {
            if (!navigator.onLine) {
                e.preventDefault();

                const formData = new FormData(expenseForm);
                const item = {
                    title: formData.get('title'),
                    amount: formData.get('amount'),
                    category: formData.get('category'),
                    expenseDate: formData.get('expenseDate'),
                    description: formData.get('description'),
                    endpoint: expenseForm.action,
                    timestamp: new Date().toISOString()
                };

                const queue = getOfflineQueue();
                queue.push(item);
                saveOfflineQueue(queue);

                showToast(`Saved "${item.title}" offline. Will sync automatically when back online.`, 'offline');
                expenseForm.reset();
                const dateInput = document.getElementById('expenseDate');
                if (dateInput) dateInput.valueAsDate = new Date();
            }
        });
    }
});
